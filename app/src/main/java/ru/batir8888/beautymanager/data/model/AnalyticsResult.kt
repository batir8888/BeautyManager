package ru.batir8888.beautymanager.data.model

import java.time.LocalDate

data class AnalyticsResult(
    // доходы / расходы
    val incomeClients: Long,
    val incomeReports: Long,
    val expenseClients: Long,
    val expenseReports: Long,

    // активность, средние
    val uniqueClients: Int,
    val recordsCount: Int,
    val daysWorked: Int,
    val busiestDay: LocalDate?,
    val busiestCount: Int,

    // сравнение с прошлым месяцем
    val diffIncome: Long,
    val diffExpense: Long,
    val diffProfit: Long,
    val diffIncomePct: Double,
    val diffExpensePct: Double,
    val diffProfitPct: Double
) {
    val totalIncome  get() = incomeClients + incomeReports
    val totalExpense get() = expenseClients + expenseReports
    val profit       get() = totalIncome - totalExpense
}
