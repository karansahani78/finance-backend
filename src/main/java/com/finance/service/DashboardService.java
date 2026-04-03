package com.finance.service;

import com.finance.dto.response.DashboardSummaryResponse;
import com.finance.dto.response.DashboardSummaryResponse.MonthlyTrendEntry;
import com.finance.dto.response.DashboardSummaryResponse.WeeklyTrendEntry;
import com.finance.dto.response.TransactionResponse;
import com.finance.model.enums.TransactionType;
import com.finance.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        BigDecimal totalIncome = transactionRepository.sumByType(TransactionType.INCOME);
        BigDecimal totalExpenses = transactionRepository.sumByType(TransactionType.EXPENSE);
        BigDecimal netBalance = totalIncome.subtract(totalExpenses);

        Map<String, BigDecimal> incomeByCategory = buildCategoryMap(TransactionType.INCOME);
        Map<String, BigDecimal> expenseByCategory = buildCategoryMap(TransactionType.EXPENSE);

        List<TransactionResponse> recent = transactionRepository
                .findRecentActivity(PageRequest.of(0, 10))
                .stream()
                .map(TransactionResponse::from)
                .toList();

        List<MonthlyTrendEntry> monthly = buildMonthlyTrend();
        List<WeeklyTrendEntry> weekly = buildWeeklyTrend();

        return DashboardSummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netBalance(netBalance)
                .incomeByCategory(incomeByCategory)
                .expenseByCategory(expenseByCategory)
                .recentActivity(recent)
                .monthlyTrend(monthly)
                .weeklyTrend(weekly)
                .build();
    }

    private Map<String, BigDecimal> buildCategoryMap(TransactionType type) {
        Map<String, BigDecimal> result = new HashMap<>();
        transactionRepository.sumGroupedByCategory(type)
                .forEach(row -> result.put(row[0].toString(), (BigDecimal) row[1]));
        return result;
    }

    private List<MonthlyTrendEntry> buildMonthlyTrend() {
        List<Object[]> incomeRows = transactionRepository.monthlyTrendByType(TransactionType.INCOME);
        List<Object[]> expenseRows = transactionRepository.monthlyTrendByType(TransactionType.EXPENSE);

        Map<String, BigDecimal> incomeMap = new HashMap<>();
        Map<String, BigDecimal> expenseMap = new HashMap<>();

        incomeRows.forEach(row -> incomeMap.put(row[0].toString(), (BigDecimal) row[1]));
        expenseRows.forEach(row -> expenseMap.put(row[0].toString(), (BigDecimal) row[1]));

        return incomeMap.keySet().stream()
                .map(month -> MonthlyTrendEntry.builder()
                        .month(month)
                        .income(incomeMap.getOrDefault(month, BigDecimal.ZERO))
                        .expenses(expenseMap.getOrDefault(month, BigDecimal.ZERO))
                        .build())
                .toList();
    }

    private List<WeeklyTrendEntry> buildWeeklyTrend() {
        List<Object[]> rows = transactionRepository.weeklyTrend();

        Map<String, BigDecimal> incomeMap = new HashMap<>();
        Map<String, BigDecimal> expenseMap = new HashMap<>();

        rows.forEach(row -> {
            String week = row[0].toString();
            String type = row[1].toString();
            BigDecimal amount = (BigDecimal) row[2];

            if ("INCOME".equals(type)) {
                incomeMap.merge(week, amount, BigDecimal::add);
            } else {
                expenseMap.merge(week, amount, BigDecimal::add);
            }
        });

        return incomeMap.keySet().stream()
                .map(week -> WeeklyTrendEntry.builder()
                        .week(week)
                        .income(incomeMap.getOrDefault(week, BigDecimal.ZERO))
                        .expenses(expenseMap.getOrDefault(week, BigDecimal.ZERO))
                        .build())
                .toList();
    }
}
