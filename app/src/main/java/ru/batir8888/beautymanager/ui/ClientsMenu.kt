package ru.batir8888.beautymanager.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.batir8888.beautymanager.data.model.Client
import ru.batir8888.beautymanager.navigation.Window
import ru.batir8888.beautymanager.viewmodels.ClientsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsMenuScreen(
    navController: NavController,
    vm: ClientsViewModel = viewModel()
) {
    val clients by vm.clients.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Клиенты") }) }
    ) { inner ->
        LazyColumn(Modifier.padding(inner)) {
            items(clients, key = { it.id }) { client ->
                ClientRow(
                    client = client,
                    onClick  = { navController.navigate(Window.ClientCard.createRoute(client.id)) },
                    onDelete = { vm.deleteClient(client) }
                )
            }
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ExtendedFloatingActionButton(
                        onClick = { navController.navigate(Window.ClientCard.createRoute(0)) },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("Добавить клиента") }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ClientRow(
    client: Client,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var confirm by remember { mutableStateOf(false) }

    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("Удалить клиента?") },
            text = { Text("Действие нельзя отменить.") },
            confirmButton = {
                TextButton(onClick = { onDelete(); confirm = false }) { Text("Удалить") }
            },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text("Отмена") } }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { onClick() }
            .combinedClickable(onLongClick = { confirm = true }, onClick = onClick),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(client.name, style = MaterialTheme.typography.titleMedium)
            Text(client.phone ?: "Телефон не указан", style = MaterialTheme.typography.bodyMedium)
        }
    }
}


/* ------------------------------------------------------------
 * Экран карточки клиента – открывается при нажатии на клиента или
 * при создании нового. Все поля сохраняются автоматически при вводе.
 * -----------------------------------------------------------*/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientCardScreen(
    clientId: Int,
    navController: NavController,
    vm: ClientsViewModel = viewModel()
) {
    val dbClient by vm.clientFlow(clientId).collectAsState(initial = null)

    var id    by rememberSaveable { mutableIntStateOf(clientId) }
    var name  by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(dbClient) {
        dbClient?.let {
            id    = it.id
            name  = it.name
            phone = it.phone.orEmpty()
            notes = it.notes.orEmpty()
        }
    }

    Scaffold(
        topBar = { TopAppBar(
            title = { Text(if (clientId == 0) "Новый клиент" else "Карточка клиента") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                }
            })
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            AutoField("ФИО",  name)  { name  = it }
            AutoField("Тел.", phone) { phone = it }
            Spacer(Modifier.height(8.dp))
            AutoField("Заметки", notes, singleLine = false, minLines = 3) { notes = it }
        }
    }

    val scope = rememberCoroutineScope()
    var saveJob by remember { mutableStateOf<Job?>(null) }
    fun scheduleSave() {
        saveJob?.cancel()
        saveJob = scope.launch {
            delay(500)
            if (name.isBlank()) return@launch
            val client = Client(
                id = id,
                name = name.trim(),
                phone = phone.trim().ifBlank { null },
                lastVisit = System.currentTimeMillis(),
                notes = notes.trim().ifBlank { null }
            )
            if (client.id == 0) {
                id = vm.insertAndGetId(client)
            } else {
                vm.updateClient(client)
            }
        }
    }

    LaunchedEffect(name, phone, notes) { scheduleSave() }
}

@Composable
private fun AutoField(
    label: String,
    value: String,
    singleLine: Boolean = true,
    minLines: Int = 1,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        singleLine = singleLine,
        minLines = minLines,
        maxLines = Int.MAX_VALUE
    )
}
