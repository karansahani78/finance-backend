package com.finance.controller;

import com.finance.dto.request.CreateTransactionRequest;
import com.finance.dto.request.UpdateTransactionRequest;
import com.finance.dto.response.PageResponse;
import com.finance.dto.response.TransactionResponse;
import com.finance.model.enums.TransactionCategory;
import com.finance.model.enums.TransactionType;
import com.finance.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            @Valid @RequestBody CreateTransactionRequest request,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transactionService.create(request, currentUser.getUsername()));
    }

    @GetMapping
    public ResponseEntity<PageResponse<TransactionResponse>> list(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) TransactionCategory category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "txnDate") String sort,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        // ✅ FIXED SORT HANDLING (supports both formats)
        Sort sortObj;

        if (sort.contains(",")) {
            // supports: sort=txnDate,desc
            String[] parts = sort.split(",");
            String field = parts[0];
            Sort.Direction dir = (parts.length > 1 && parts[1].equalsIgnoreCase("asc"))
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;
            sortObj = Sort.by(dir, field);
        } else {
            // supports: sort=txnDate&direction=desc
            Sort.Direction dir = direction.equalsIgnoreCase("asc")
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;
            sortObj = Sort.by(dir, sort);
        }

        PageRequest pageable = PageRequest.of(page, Math.min(size, 100), sortObj);

        return ResponseEntity.ok(
                transactionService.list(type, category, from, to, search, pageable)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTransactionRequest request
    ) {
        return ResponseEntity.ok(transactionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        transactionService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}