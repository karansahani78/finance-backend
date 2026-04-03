package com.finance.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class DashboardSummaryResponse {

    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netBalance;
    private Map<String, BigDecimal> incomeByCategory;
    private Map<String, BigDecimal> expenseByCategory;
    private List<TransactionResponse> recentActivity;
    private List<MonthlyTrendEntry> monthlyTrend;
    private List<WeeklyTrendEntry> weeklyTrend;

    @Getter
    @Builder
    public static class MonthlyTrendEntry {
        private String month;
        private BigDecimal income;
        private BigDecimal expenses;
    }

    @Getter
    @Builder
    public static class WeeklyTrendEntry {
        private String week;
        private BigDecimal income;
        private BigDecimal expenses;
    }
}
