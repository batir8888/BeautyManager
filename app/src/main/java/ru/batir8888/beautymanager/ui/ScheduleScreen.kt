package ru.batir8888.beautymanager.ui

import android.app.TimePickerDialog
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import ru.batir8888.beautymanager.BeautyManagerApplication
import ru.batir8888.beautymanager.data.model.Appointment
import ru.batir8888.beautymanager.data.model.Client
import ru.batir8888.beautymanager.viewmodels.ScheduleViewModel
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    vm: ScheduleViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = BeautyManagerApplication.database
                @Suppress("UNCHECKED_CAST")
                return ScheduleViewModel(db.appointmentDao(), db.clientDao()) as T
            }
        }
    )
) {
    val zone = ZoneId.systemDefault()
    val dateStart by vm.selectedDate.collectAsState()
    val appointments by vm.appointments.collectAsState(emptyList())
    val clients by vm.clients.collectAsState(emptyList())   // ←
    var showAdd by remember { mutableStateOf(false) }
    var editing  by remember { mutableStateOf<Appointment?>(null) }

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = { vm.changeDay(-7) },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                title = {
                    val loc = Instant.ofEpochMilli(dateStart)
                        .atZone(zone).toLocalDate()
                    Text(loc.format(DateTimeFormatter.ofPattern("LLLL yyyy"))
                        .replaceFirstChar { it.titlecase() })
                },
                actions = {
                    IconButton(onClick = { vm.changeDay(7) },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
                    }
                }
            )
        }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            DaySelectorRow(dateStart) { vm.changeDay(it) }

            Spacer(Modifier.height(8.dp))

            LazyColumn(Modifier.weight(1f)) {
                items(appointments, key = { it.id }) { app ->
                    AppointmentRow(
                        app = app,
                        client = clients.firstOrNull { it.id == app.clientId },
                        onEditMoney = { editing = app },
                        onDeleteConfirmed = {
                            scope.launch { vm.delete(app) }
                        }
                    )
                }
                item {
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { showAdd = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.onPrimary,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) { Text("+ Добавить запись") }
                }
            }
        }
    }

    if (showAdd) {
        AddAppointmentDialog(
            vm = vm,
            dateStart = dateStart,
            onDismiss = { showAdd = false }
        )
    }
    editing?.let { app ->
        IncomeExpenseDialog(
            appointment = app,
            onDismiss = { editing = null },
            onConfirm = { scope.launch { vm.save(it) }; editing = null }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppointmentRow(
    app: Appointment,
    client: Client?,
    onEditMoney: () -> Unit,
    onDeleteConfirmed: () -> Unit
) {
    var ask by remember { mutableStateOf(false) }
    if (ask) {
        AlertDialog(
            onDismissRequest = { ask = false },
            title = { Text("Удалить запись?") },
            text  = { Text("Действие нельзя отменить.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteConfirmed()
                    ask = false
                }) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { ask = false }) { Text("Отмена") }
            }
        )
    }

    val zone = ZoneId.systemDefault()
    val fmt  = DateTimeFormatter.ofPattern("HH:mm")
    val start = Instant.ofEpochMilli(app.dateStart).atZone(zone).toLocalTime().format(fmt)
    val end   = Instant.ofEpochMilli(app.dateEnd).atZone(zone).toLocalTime().format(fmt)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .combinedClickable(
                onClick = {},
                onLongClick = { ask = true }
            ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(client?.name ?: "Клиент #${app.clientId}", style = MaterialTheme.typography.titleMedium)
                Text(client?.phone ?: "", style = MaterialTheme.typography.bodySmall)
                Text("$start – $end", style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onEditMoney) {
                Icon(Icons.Default.Edit, contentDescription = "Доход/Расход")
            }
        }
    }
}

@Composable
private fun AddAppointmentDialog(
    vm: ScheduleViewModel,
    dateStart: Long,
    onDismiss: () -> Unit
) {
    val clients by vm.clients.collectAsState(initial = emptyList())

    var useExisting by remember { mutableStateOf(true) }
    var filter      by remember { mutableStateOf("") }
    var selected    by remember { mutableStateOf<Client?>(null) }

    var newName  by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }

    var from by remember { mutableStateOf(LocalTime.of(10, 0)) }
    var to   by remember { mutableStateOf(LocalTime.of(11, 0)) }

    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                scope.launch {
                    val id = if (useExisting) {
                        selected?.id ?: return@launch
                    } else {
                        if (newName.isBlank()) return@launch
                        vm.createClient(newName, newPhone.takeIf { it.isNotBlank() })
                    }

                    val zone = ZoneId.systemDefault()
                    val base = Instant.ofEpochMilli(dateStart).atZone(zone).toLocalDate()
                    val start = base.atTime(from).atZone(zone).toInstant().toEpochMilli()
                    val end   = base.atTime(to).atZone(zone).toInstant().toEpochMilli()

                    vm.save(Appointment(clientId = id, dateStart = start, dateEnd = end))
                    onDismiss()
                }
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
        title = { Text("Новая запись") },
        text = {
            Column(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(!useExisting, { useExisting = false }, label = { Text("Новый") })
                    Spacer(Modifier.width(8.dp))
                    FilterChip(useExisting,  { useExisting = true  }, label = { Text("Существующий") })
                }

                Spacer(Modifier.height(12.dp))

                if (useExisting) {
                    OutlinedTextField(
                        value = filter,
                        onValueChange = { filter = it },
                        label = { Text("Поиск по ФИО") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    val list = clients.filter { it.name.contains(filter, true) }
                    LazyColumn(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp)
                    ) {
                        items(list) { c ->
                            ListItem(
                                headlineContent = { Text(c.name) },
                                supportingContent = { Text(c.phone ?: "") },
                                modifier = Modifier.clickable { selected = c }
                            )
                        }
                    }
                    selected?.let {
                        Text("Выбрано: ${it.name}", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("ФИО клиента") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text("Телефон") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(12.dp))
                TimeFields(from, to, onFromChange = { from = it }, onToChange = { to = it })
            }
        }
    )
}

@Composable
private fun TimeFields(
    from: LocalTime,
    to: LocalTime,
    onFromChange: (LocalTime) -> Unit,
    onToChange:   (LocalTime) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        TimeChip("С", from) { onFromChange(it) }
        Spacer(Modifier.width(16.dp))
        TimeChip("По", to) { onToChange(it) }
    }
}

@Composable
private fun TimeChip(label: String, time: LocalTime, onPick: (LocalTime) -> Unit) {
    var show by remember { mutableStateOf(false) }
    AssistChip(
        onClick = { show = true },
        label = { Text("$label ${time.format(DateTimeFormatter.ofPattern("HH:mm"))}") }
        )
    if (show) {
        TimePickerDialog(
            LocalContext.current,
            { _, hour, minute ->
                onPick(LocalTime.of(hour, minute))
                show = false
            },
            time.hour,
            time.minute,
            true
        ).show()
    }
}

@Composable
private fun IncomeExpenseDialog(
    appointment: Appointment,
    onDismiss: () -> Unit,
    onConfirm: (Appointment) -> Unit
) {
    var income  by remember { mutableStateOf(appointment.income?.toString() ?: "") }
    var expense by remember { mutableStateOf(appointment.expense?.toString() ?: "") }
    var note    by remember { mutableStateOf(appointment.note ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onConfirm(
                    appointment.copy(
                        income  = income.toLongOrNull(),
                        expense = expense.toLongOrNull(),
                        note    = note.ifBlank { null }
                    )
                )
            }) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
        title = { Text("Доход / Расход") },
        text = {
            Column {
                OutlinedTextField(
                    value = income,
                    onValueChange = { income = it.filter(Char::isDigit) },
                    label = { Text("Доход") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = expense,
                    onValueChange = { expense = it.filter(Char::isDigit) },
                    label = { Text("Расход") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Заметка") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

@Composable
private fun DaySelectorRow(
    dateStart: Long,
    onOffset: (Long) -> Unit
) {
    val zone     = ZoneId.systemDefault()
    val selDate  = Instant.ofEpochMilli(dateStart).atZone(zone).toLocalDate()
    val monday   = selDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
    ) {
        repeat(7) { i ->
            val day      = monday.plusDays(i.toLong())
            val selected = day == selDate

            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary
                        else Color.Transparent
                    )
                    .clickable {
                        onOffset(ChronoUnit.DAYS.between(selDate, day))
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    color = if (selected)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}