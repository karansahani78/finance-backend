package com.finance.dto.response;

import com.finance.model.Transaction;
import com.finance.model.enums.TransactionCategory;
import com.finance.model.enums.TransactionType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class TransactionResponse {

    private Long id;
    private BigDecimal amount;
    private TransactionType type;
    private TransactionCategory category;
    private LocalDate txnDate;
    private String description;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TransactionResponse from(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .amount(t.getAmount())
                .type(t.getType())
                .category(t.getCategory())
                .txnDate(t.getTxnDate())
                .description(t.getDescription())
                .createdById(t.getCreatedBy().getId())
                .createdByName(t.getCreatedBy().getName())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
