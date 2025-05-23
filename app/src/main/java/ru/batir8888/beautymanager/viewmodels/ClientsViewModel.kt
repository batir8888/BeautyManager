package ru.batir8888.beautymanager.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.batir8888.beautymanager.BeautyManagerApplication
import ru.batir8888.beautymanager.data.model.Client

class ClientsViewModel : ViewModel() {
    private val clientDao = BeautyManagerApplication.database.clientDao()

    val clients = clientDao.getAllClients()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun clientFlow(id: Int) = clientDao.getClientById(id)

    suspend fun insertAndGetId(client: Client): Int =
        clientDao.insertClient(client).toInt()

    suspend fun updateClient(client: Client) =
        clientDao.updateClient(client)

    /** Удаление клиента */
    fun deleteClient(client: Client) = viewModelScope.launch {
        clientDao.deleteClient(client)
    }
}