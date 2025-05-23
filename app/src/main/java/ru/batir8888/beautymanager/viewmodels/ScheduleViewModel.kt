package ru.batir8888.beautymanager.viewmodels

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

    fun changeDay(offsetDays: Long) {
        _selectedDate.value += Duration.ofDays(offsetDays).toMillis()
    }

    fun save(app: Appointment) = viewModelScope.launch {
        if (app.id == 0) appointmentDao.insert(app) else appointmentDao.update(app)
    }
    fun delete(app: Appointment) = viewModelScope.launch { appointmentDao.delete(app) }

    val clients = clientDao.getAllClients()

    suspend fun createClient(name: String, phone: String?): Int =
        clientDao.insertClient(
            Client(name = name.trim(), phone = phone?.trim(), lastVisit = null, notes = null)
        ).toInt()

    private fun todayStart(): Long = LocalDate.now()
        .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}