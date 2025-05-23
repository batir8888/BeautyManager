package ru.batir8888.beautymanager.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.batir8888.beautymanager.data.ReportType
import ru.batir8888.beautymanager.data.dao.AppointmentDao
import ru.batir8888.beautymanager.data.dao.ReportDao
import ru.batir8888.beautymanager.data.model.AnalyticsResult
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class AnalyticsViewModel(
    private val appointmentDao: AppointmentDao,
    private val reportDao: ReportDao
) : ViewModel() {

    /** millis 1-го числа выбранного месяца 00:00 */
    private val _month = MutableStateFlow(firstDayOfThisMonth())
    val  month: StateFlow<Long> = _month

    /** доступность кнопки «Составить» */
    val canBuild = _month.map { it < firstDayOfThisMonth() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _result = MutableStateFlow<AnalyticsResult?>(null)
    val result: StateFlow<AnalyticsResult?> = _result.asStateFlow()

    /* смена месяца */
    fun changeMonth(offset: Long) = _month.update { old ->
        Instant.ofEpochMilli(old).atZone(zone()).toLocalDate()
            .plusMonths(offset)
            .withDayOfMonth(1)
            .atStartOfDay(zone()).toInstant().toEpochMilli()
    }

    /* главный запуск расчёта */
    fun build() = viewModelScope.launch {
        val start = _month.value
        val res   = calculateForMonth(start)
        val prev  = calculateForMonth(prevMonth(start))

        _result.value = res.copy(
            diffIncome  = res.totalIncome  - prev.totalIncome,
            diffExpense = res.totalExpense - prev.totalExpense,
            diffProfit  = res.profit       - prev.profit,
            diffIncomePct  = pct(prev.totalIncome,  res.totalIncome),
            diffExpensePct = pct(prev.totalExpense, res.totalExpense),
            diffProfitPct  = pct(prev.profit,       res.profit)
        )
    }

    /* ---------- приватные утилиты ---------- */

    private suspend fun calculateForMonth(monthStart: Long): AnalyticsResult {
        val (from, to) = monthRange(monthStart)

        val apps = appointmentDao.between(from, to).first()
        val reps = reportDao.getByMonth(monthStart).first()

        val incomeClients  = apps.sumOf { it.income ?: 0 }
        val expenseClients = apps.sumOf { it.expense ?: 0 }
        val incomeReports  = reps.filter { it.type == ReportType.INCOME  }.sumOf { it.amount }
        val expenseReports = reps.filter { it.type == ReportType.EXPENSE }.sumOf { it.amount }

        val byDay = apps.groupBy { millisToLocalDate(it.dateStart) }
        val busiest = byDay.maxByOrNull { it.value.size }

        return AnalyticsResult(
            incomeClients, incomeReports,
            expenseClients, expenseReports,
            uniqueClients = apps.map { it.clientId }.distinct().size,
            recordsCount  = apps.size,
            daysWorked    = byDay.size,
            busiestDay    = busiest?.key,
            busiestCount  = busiest?.value?.size ?: 0,
            diffIncome = 0, diffExpense = 0, diffProfit = 0,
            diffIncomePct = 0.0, diffExpensePct = 0.0, diffProfitPct = 0.0
        )
    }

    private fun firstDayOfThisMonth(): Long = LocalDate.now()
        .withDayOfMonth(1)
        .atStartOfDay(zone()).toInstant().toEpochMilli()

    private fun prevMonth(monthStart: Long) =
        Instant.ofEpochMilli(monthStart).atZone(zone()).toLocalDate()
            .minusMonths(1)
            .withDayOfMonth(1)
            .atStartOfDay(zone()).toInstant().toEpochMilli()

    private fun monthRange(monthStart: Long): Pair<Long, Long> {
        val local = millisToLocalDate(monthStart)
        val from  = local.withDayOfMonth(1)
            .atStartOfDay(zone()).toInstant().toEpochMilli()
        val to    = local.withDayOfMonth(local.lengthOfMonth())
            .atTime(23, 59, 59).atZone(zone()).toInstant().toEpochMilli()
        return from to to
    }

    private fun pct(old: Long, new: Long): Double =
        if (old == 0L) 0.0 else ((new - old) * 100.0) / old

    private fun millisToLocalDate(ms: Long): LocalDate =
        Instant.ofEpochMilli(ms).atZone(zone()).toLocalDate()

    private fun zone(): ZoneId = ZoneId.systemDefault()
}
