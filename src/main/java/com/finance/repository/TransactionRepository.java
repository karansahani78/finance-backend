package com.finance.repository;

import com.finance.model.Transaction;
import com.finance.model.enums.TransactionCategory;
import com.finance.model.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t WHERE " +
           "(:type IS NULL OR t.type = :type) AND " +
           "(:category IS NULL OR t.category = :category) AND " +
           "(:from IS NULL OR t.txnDate >= :from) AND " +
           "(:to IS NULL OR t.txnDate <= :to) AND " +
           "(:search IS NULL OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Transaction> findWithFilters(
            @Param("type") TransactionType type,
            @Param("category") TransactionCategory category,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = :type")
    BigDecimal sumByType(@Param("type") TransactionType type);

    @Query("SELECT t.category, COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE (:type IS NULL OR t.type = :type) " +
           "GROUP BY t.category ORDER BY SUM(t.amount) DESC")
    List<Object[]> sumGroupedByCategory(@Param("type") TransactionType type);

    @Query("SELECT FUNCTION('TO_CHAR', t.txnDate, 'YYYY-MM'), COALESCE(SUM(t.amount), 0) " +
           "FROM Transaction t WHERE t.type = :type " +
           "GROUP BY FUNCTION('TO_CHAR', t.txnDate, 'YYYY-MM') " +
           "ORDER BY FUNCTION('TO_CHAR', t.txnDate, 'YYYY-MM') DESC")
    List<Object[]> monthlyTrendByType(@Param("type") TransactionType type);

    @Query("SELECT t FROM Transaction t ORDER BY t.createdAt DESC")
    List<Transaction> findRecentActivity(Pageable pageable);

    @Query("SELECT FUNCTION('TO_CHAR', t.txnDate, 'IYYY-IW'), t.type, COALESCE(SUM(t.amount), 0) " +
           "FROM Transaction t " +
           "GROUP BY FUNCTION('TO_CHAR', t.txnDate, 'IYYY-IW'), t.type " +
           "ORDER BY FUNCTION('TO_CHAR', t.txnDate, 'IYYY-IW') DESC")
    List<Object[]> weeklyTrend();
}
