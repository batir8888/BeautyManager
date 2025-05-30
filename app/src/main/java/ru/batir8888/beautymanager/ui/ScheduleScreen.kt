package ru.batir8888.beautymanager.ui

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import java.time.Duration
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
    val clients by vm.clients.collectAsState(emptyList())
    var showAdd by remember { mutableStateOf(false) }
    var editing  by remember { mutableStateOf<Appointment?>(null) }

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = { vm.changeDay(-7) },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                title = {
                    val loc = Instant.ofEpochMilli(dateStart)
                        .atZone(zone).toLocalDate()
                    Text(
                        text = loc.format(DateTimeFormatter.ofPattern("LLLL yyyy"))
                            .replaceFirstChar { it.titlecase() },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    IconButton(
                        onClick = { vm.changeDay(7) },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
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

            Spacer(Modifier.height(16.dp))

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
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { showAdd = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        )
                    ) {
                        Text(
                            "✨ Добавить запись",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
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
            title = {
                Text(
                    "Удалить запись?",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    "Действие нельзя отменить.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteConfirmed()
                        ask = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(
                    onClick = { ask = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) { Text("Отмена") }
            },
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        )
    }

    val zone = ZoneId.systemDefault()
    val fmt  = DateTimeFormatter.ofPattern("HH:mm")
    val start = Instant.ofEpochMilli(app.dateStart).atZone(zone).toLocalTime().format(fmt)
    val end   = Instant.ofEpochMilli(app.dateEnd).atZone(zone).toLocalTime().format(fmt)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .combinedClickable(
                onClick = {},
                onLongClick = { ask = true }
            ),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    ) {
        Row(
            Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = client?.name ?: "Клиент #${app.clientId}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = client?.phone ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "🕒 $start – $end",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
            IconButton(
                onClick = onEditMoney,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                    contentColor = MaterialTheme.colorScheme.secondary
                )
            ) {
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
    val appointments by vm.appointmentsForDate(dateStart).collectAsState(initial = emptyList())

    var useExisting by remember { mutableStateOf(true) }
    var filter      by remember { mutableStateOf("") }
    var selected    by remember { mutableStateOf<Client?>(null) }

    var newName  by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }

    var from by remember { mutableStateOf(LocalTime.of(10, 0)) }
    var to   by remember { mutableStateOf(LocalTime.of(11, 0)) }

    val scope = rememberCoroutineScope()

    // Валидация времени
    val isTimeValid = from.isBefore(to)
    val timeConflicts = checkTimeConflicts(from, to, appointments)
    val hasLongConflict = timeConflicts.any { it.overlapMinutes > 30 }

    val canCreate = (if (useExisting) selected != null else newName.isNotBlank()) &&
            isTimeValid && !hasLongConflict

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    if (!canCreate) return@TextButton
                    scope.launch {
                        val id = if (useExisting) {
                            selected?.id ?: return@launch
                        } else {
                            vm.createClient(newName.trim(), newPhone.trim().takeIf { it.isNotBlank() })
                        }

                        val zone = ZoneId.systemDefault()
                        val base = Instant.ofEpochMilli(dateStart).atZone(zone).toLocalDate()
                        val start = base.atTime(from).atZone(zone).toInstant().toEpochMilli()
                        val end   = base.atTime(to).atZone(zone).toInstant().toEpochMilli()

                        vm.save(Appointment(clientId = id, dateStart = start, dateEnd = end))
                        onDismiss()
                    }
                },
                enabled = canCreate,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Text("✨ Создать", fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) { Text("Отмена", fontWeight = FontWeight.Medium) }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "📅",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Новая запись",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        text = {
            Column(Modifier.fillMaxWidth()) {
                // Секция выбора клиента
                Text(
                    text = "👤 Клиент",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(
                        selected = !useExisting,
                        onClick = {
                            useExisting = false
                            selected = null
                        },
                        label = {
                            Text(
                                "➕ Новый",
                                fontWeight = if (!useExisting) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(Modifier.width(12.dp))
                    FilterChip(
                        selected = useExisting,
                        onClick = {
                            useExisting = true
                            newName = ""
                            newPhone = ""
                        },
                        label = {
                            Text(
                                "👥 Существующий",
                                fontWeight = if (useExisting) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Spacer(Modifier.height(16.dp))

                if (useExisting) {
                    OutlinedTextField(
                        value = filter,
                        onValueChange = { filter = it },
                        label = { Text("🔍 Поиск по ФИО") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(Modifier.height(8.dp))

                    val list = clients.filter { it.name.contains(filter, true) }
                    if (list.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 160.dp)
                                    .padding(4.dp)
                            ) {
                                items(list) { c ->
                                    val isSelected = selected?.id == c.id
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(2.dp)
                                            .clickable { selected = if (isSelected) null else c },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) {
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                            } else {
                                                Color.Transparent
                                            }
                                        )
                                    ) {
                                        ListItem(
                                            headlineContent = {
                                                Text(
                                                    c.name,
                                                    color = if (isSelected) {
                                                        MaterialTheme.colorScheme.primary
                                                    } else {
                                                        MaterialTheme.colorScheme.onSurface
                                                    },
                                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                                )
                                            },
                                            supportingContent = {
                                                Text(
                                                    c.phone ?: "Телефон не указан",
                                                    color = if (isSelected) {
                                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                                    } else {
                                                        MaterialTheme.colorScheme.onSurfaceVariant
                                                    }
                                                )
                                            },
                                            leadingContent = if (isSelected) {
                                                {
                                                    Icon(
                                                        Icons.Default.CheckCircle,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            } else null
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("👤 ФИО клиента") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        isError = newName.isBlank()
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text("📞 Телефон (опционально)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Секция времени
                Text(
                    text = "⏰ Время",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                TimeFields(
                    from = from,
                    to = to,
                    onFromChange = { from = it },
                    onToChange = { to = it },
                    isValid = isTimeValid
                )

                // Показываем ошибки валидации
                if (!isTimeValid) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "⚠️ Время начала должно быть раньше времени окончания",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (timeConflicts.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    timeConflicts.forEach { conflict ->
                        val color = if (conflict.overlapMinutes > 30) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.tertiary
                        }
                        val icon = if (conflict.overlapMinutes > 30) "❌" else "⚠️"

                        Text(
                            text = "$icon Пересечение с записью на ${conflict.timeRange}: ${conflict.overlapMinutes} мин",
                            style = MaterialTheme.typography.bodySmall,
                            color = color
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun TimeFields(
    from: LocalTime,
    to: LocalTime,
    onFromChange: (LocalTime) -> Unit,
    onToChange: (LocalTime) -> Unit,
    isValid: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isValid) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TimeChip(
                label = "🕐 С",
                time = from,
                onPick = {
                    onFromChange(it)
                    // Автоматически корректируем время окончания, если оно стало меньше начала
                    if (!it.isBefore(to)) {
                        onToChange(it.plusHours(1))
                    }
                },
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.width(16.dp))

            TimeChip(
                label = "🕐 До",
                time = to,
                onPick = {
                    onToChange(it)
                    // Автоматически корректируем время начала, если оно стало больше окончания
                    if (!from.isBefore(it)) {
                        onFromChange(it.minusHours(1))
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TimeChip(
    label: String,
    time: LocalTime,
    onPick: (LocalTime) -> Unit,
    modifier: Modifier = Modifier
) {
    var show by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .clickable { show = true },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = time.format(DateTimeFormatter.ofPattern("HH:mm")),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }

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

// Вспомогательные классы и функции для валидации времени
data class TimeConflict(
    val timeRange: String,
    val overlapMinutes: Int
)

private fun checkTimeConflicts(
    from: LocalTime,
    to: LocalTime,
    appointments: List<Appointment>,
): List<TimeConflict> {
    val zone = ZoneId.systemDefault()

    return appointments.mapNotNull { appointment ->
        val appointmentStart = Instant.ofEpochMilli(appointment.dateStart)
            .atZone(zone).toLocalTime()
        val appointmentEnd = Instant.ofEpochMilli(appointment.dateEnd)
            .atZone(zone).toLocalTime()

        val overlapStart = maxOf(from, appointmentStart)
        val overlapEnd = minOf(to, appointmentEnd)

        if (overlapStart.isBefore(overlapEnd)) {
            val overlapMinutes = Duration.between(overlapStart, overlapEnd).toMinutes().toInt()
            TimeConflict(
                timeRange = "${appointmentStart.format(DateTimeFormatter.ofPattern("HH:mm"))}-${appointmentEnd.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                overlapMinutes = overlapMinutes
            )
        } else null
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
            TextButton(
                onClick = {
                    onConfirm(
                        appointment.copy(
                            income  = income.toLongOrNull(),
                            expense = expense.toLongOrNull(),
                            note    = note.ifBlank { null }
                        )
                    )
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) { Text("💰 Сохранить", fontWeight = FontWeight.Medium) }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) { Text("Отмена") }
        },
        title = {
            Text(
                "Доход / Расход",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = income,
                    onValueChange = { income = it.filter(Char::isDigit) },
                    label = { Text("💰 Доход") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = expense,
                    onValueChange = { expense = it.filter(Char::isDigit) },
                    label = { Text("💸 Расход") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.secondary,
                        focusedLabelColor = MaterialTheme.colorScheme.secondary
                    )
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("📝 Заметка") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                        focusedLabelColor = MaterialTheme.colorScheme.tertiary
                    )
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
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
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
    ) {
        repeat(7) { i ->
            val day      = monday.plusDays(i.toLong())
            val selected = day == selDate

            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (selected) {
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        } else {
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                                )
                            )
                        }
                    )
                    .clickable {
                        onOffset(ChronoUnit.DAYS.between(selDate, day))
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day.dayOfMonth.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
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