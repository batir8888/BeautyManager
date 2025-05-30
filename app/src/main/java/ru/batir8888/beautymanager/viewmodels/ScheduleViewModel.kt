package ru.batir8888.beautymanager.viewmodels

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import ru.batir8888.beautymanager.data.dao.AppointmentDao
import ru.batir8888.beautymanager.data.dao.ClientDao
import ru.batir8888.beautymanager.data.model.Appointment
import ru.batir8888.beautymanager.data.model.Client
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class ScheduleViewModel(
    private val appointmentDao: AppointmentDao,
    private val clientDao: ClientDao
) : ViewModel()  {

    private val _selectedDate = MutableStateFlow(todayStart())
    val selectedDate: StateFlow<Long> = _selectedDate

    @OptIn(ExperimentalCoroutinesApi::class)
    val appointments: Flow<List<Appointment>> = selectedDate.flatMapLatest { start ->
        val end = start + Duration.ofDays(1).toMillis() - 1
        appointmentDao.between(start, end)
    }

    fun appointmentsForDate(dateStart: Long): Flow<List<Appointment>> {
        val dayEnd = dateStart + Duration.ofDays(1).toMillis() - 1
        return appointmentDao.between(dateStart, dayEnd)
    }

    fun changeDay(offsetDays: Long) {
        _selectedDate.value += Duration.ofDays(offsetDays).toMillis()
    }

    fun save(app: Appointment) = viewModelScope.launch {
        if (app.id == 0) appointmentDao.insert(app) else appointmentDao.update(app)
    }

    fun delete(app: Appointment) = viewModelScope.launch {
        appointmentDao.delete(app)
    }

    val clients = clientDao.getAllClients()

    /**
     * Создает нового клиента и возвращает его ID
     * @param name имя клиента (обязательно)
     * @param phone телефон клиента (опционально)
     * @return ID созданного клиента
     */
    suspend fun createClient(name: String, phone: String?): Int =
        clientDao.insertClient(
            Client(
                name = name.trim(),
                phone = phone?.trim()?.takeIf { it.isNotBlank() },
                lastVisit = System.currentTimeMillis(), // Устанавливаем текущее время как последний визит
                notes = null,
                portfolioPhotos = emptyList()
            )
        ).toInt()

    private fun todayStart(): Long = LocalDate.now()
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

    fun dayStart(date: LocalDate): Long = date
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

    @SuppressLint("NewApi")
    suspend fun hasTimeConflicts(
        startTime: Long,
        endTime: Long,
        excludeAppointmentId: Int = 0
    ): Boolean {
        val dayStart = LocalDate.ofInstant(
            Instant.ofEpochMilli(startTime),
            ZoneId.systemDefault()
        ).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val dayEnd = dayStart + Duration.ofDays(1).toMillis() - 1

        return appointmentDao.hasConflicts(startTime, endTime, dayStart, dayEnd, excludeAppointmentId)
    }
}