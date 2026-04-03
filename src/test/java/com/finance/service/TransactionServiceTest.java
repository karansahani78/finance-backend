package com.finance.service;

import com.finance.dto.request.CreateTransactionRequest;
import com.finance.dto.response.TransactionResponse;
import com.finance.exception.BusinessException;
import com.finance.exception.ResourceNotFoundException;
import com.finance.model.Transaction;
import com.finance.model.User;
import com.finance.model.enums.*;
import com.finance.repository.TransactionRepository;
import com.finance.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User mockUser;
    private Transaction mockTransaction;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .name("Admin")
                .email("admin@finance.io")
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();

        mockTransaction = Transaction.builder()
                .id(10L)
                .amount(new BigDecimal("2500.00"))
                .type(TransactionType.INCOME)
                .category(TransactionCategory.SALARY)
                .txnDate(LocalDate.now().minusDays(1))
                .description("Monthly salary")
                .createdBy(mockUser)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createTransactionLinksToRequestingUser() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAmount(new BigDecimal("2500.00"));
        request.setType(TransactionType.INCOME);
        request.setCategory(TransactionCategory.SALARY);
        request.setTxnDate(LocalDate.now().minusDays(1));
        request.setDescription("Monthly salary");

        when(userRepository.findByEmail("admin@finance.io")).thenReturn(Optional.of(mockUser));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTransaction);

        TransactionResponse response = transactionService.create(request, "admin@finance.io");

        assertThat(response.getAmount()).isEqualByComparingTo("2500.00");
        assertThat(response.getType()).isEqualTo(TransactionType.INCOME);
        assertThat(response.getCreatedByName()).isEqualTo("Admin");
    }

    @Test
    void createTransactionFailsWhenUserNotFound() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setType(TransactionType.EXPENSE);
        request.setCategory(TransactionCategory.FOOD);
        request.setTxnDate(LocalDate.now());

        when(userRepository.findByEmail("ghost@finance.io")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.create(request, "ghost@finance.io"))
                .isInstanceOf(BusinessException.class);

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void getByIdReturnsTransaction() {
        when(transactionRepository.findById(10L)).thenReturn(Optional.of(mockTransaction));

        TransactionResponse response = transactionService.getById(10L);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getCategory()).isEqualTo(TransactionCategory.SALARY);
    }

    @Test
    void getByIdThrowsWhenNotFound() {
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void softDeleteSetsDeletedAt() {
        when(transactionRepository.findById(10L)).thenReturn(Optional.of(mockTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTransaction);

        transactionService.softDelete(10L);

        assertThat(mockTransaction.getDeletedAt()).isNotNull();
        verify(transactionRepository).save(mockTransaction);
    }

    @Test
    void softDeleteAlreadyDeletedThrowsBusinessException() {
        mockTransaction.setDeletedAt(LocalDateTime.now());
        when(transactionRepository.findById(10L)).thenReturn(Optional.of(mockTransaction));

        assertThatThrownBy(() -> transactionService.softDelete(10L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already deleted");
    }

    @Test
    void listWithInvalidDateRangeThrowsBusinessException() {
        LocalDate from = LocalDate.now();
        LocalDate to = LocalDate.now().minusDays(5);

        assertThatThrownBy(() -> transactionService.list(null, null, from, to, null, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Start date");
    }
}
