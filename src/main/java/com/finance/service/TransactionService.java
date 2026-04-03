package com.finance.service;

import com.finance.dto.request.CreateTransactionRequest;
import com.finance.dto.request.UpdateTransactionRequest;
import com.finance.dto.response.PageResponse;
import com.finance.dto.response.TransactionResponse;
import com.finance.exception.BusinessException;
import com.finance.exception.ResourceNotFoundException;
import com.finance.model.Transaction;
import com.finance.model.User;
import com.finance.model.enums.TransactionCategory;
import com.finance.model.enums.TransactionType;
import com.finance.repository.TransactionRepository;
import com.finance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Transactional
    public TransactionResponse create(CreateTransactionRequest request, String creatorEmail) {
        User creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new BusinessException("Could not resolve the requesting user"));

        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory())
                .txnDate(request.getTxnDate())
                .description(request.getDescription())
                .createdBy(creator)
                .build();

        return TransactionResponse.from(transactionRepository.save(transaction));
    }

    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> list(
            TransactionType type,
            TransactionCategory category,
            LocalDate from,
            LocalDate to,
            String search,
            Pageable pageable
    ) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BusinessException("Start date cannot be after end date");
        }

        return PageResponse.of(
                transactionRepository.findWithFilters(type, category, from, to, search, pageable),
                TransactionResponse::from
        );
    }

    @Transactional(readOnly = true)
    public TransactionResponse getById(Long id) {
        return TransactionResponse.from(fetchOrThrow(id));
    }

    @Transactional
    public TransactionResponse update(Long id, UpdateTransactionRequest request) {
        Transaction transaction = fetchOrThrow(id);

        if (request.getAmount() != null) transaction.setAmount(request.getAmount());
        if (request.getType() != null) transaction.setType(request.getType());
        if (request.getCategory() != null) transaction.setCategory(request.getCategory());
        if (request.getTxnDate() != null) transaction.setTxnDate(request.getTxnDate());
        if (request.getDescription() != null) transaction.setDescription(request.getDescription());

        return TransactionResponse.from(transactionRepository.save(transaction));
    }

    @Transactional
    public void softDelete(Long id) {
        Transaction transaction = fetchOrThrow(id);
        if (transaction.getDeletedAt() != null) {
            throw new BusinessException("Transaction is already deleted");
        }
        transaction.setDeletedAt(LocalDateTime.now());
        transactionRepository.save(transaction);
    }

    private Transaction fetchOrThrow(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.transaction(id));
    }
}
