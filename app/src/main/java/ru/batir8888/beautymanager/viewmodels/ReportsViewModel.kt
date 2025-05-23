package ru.batir8888.beautymanager.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import ru.batir8888.beautymanager.data.dao.ReportDao
import ru.batir8888.beautymanager.data.model.Report
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class ReportsViewModel(
    private val reportDao: ReportDao
) : ViewModel() {

    /** выбранный месяц (millis на 1-е число) */
    private val _month = MutableStateFlow(firstDayOfCurrentMonth())
    val month: StateFlow<Long> = _month

    /** отчёты для UI */
    @OptIn(ExperimentalCoroutinesApi::class)
    val reports: Flow<List<Report>> =
        month.flatMapLatest { reportDao.getByMonth(it) }

    /* смена месяца: -1 / +1 */
    fun changeMonth(offset: Long) {
        _month.value = _month.value
            .toLocalDate()
            .plusMonths(offset)
            .withDayOfMonth(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant().toEpochMilli()
    }

    fun save(report: Report) = viewModelScope.launch {
        if (report.id == 0) reportDao.insert(report)
        else                 reportDao.update(report)
    }

    fun delete(report: Report) = viewModelScope.launch { reportDao.delete(report) }

    private fun Long.toLocalDate() =
        Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

    private fun firstDayOfCurrentMonth(): Long = LocalDate.now()
        .withDayOfMonth(1)
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant().toEpochMilli()
}
