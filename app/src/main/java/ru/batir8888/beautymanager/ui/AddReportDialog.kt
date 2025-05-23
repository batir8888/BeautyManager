package ru.batir8888.beautymanager.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.batir8888.beautymanager.data.ReportType
import ru.batir8888.beautymanager.data.model.Report

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReportDialog(
    monthStart: Long,
    onDismiss: () -> Unit,
    onConfirm: (Report) -> Unit
) {
    var type by remember { mutableStateOf(ReportType.INCOME) }
    var label by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val amt = amountStr.toLongOrNull() ?: return@TextButton
                onConfirm(
                    Report(
                        monthStart = monthStart,
                        type = type,
                        label = label,
                        amount = amt
                    )
                )
            }) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
        title = { Text("Новый отчёт") },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(
                        selected = type == ReportType.INCOME,
                        onClick = { type = ReportType.INCOME },
                        label = { Text("Доход") }
                    )
                    Spacer(Modifier.width(8.dp))
                    FilterChip(
                        selected = type == ReportType.EXPENSE,
                        onClick = { type = ReportType.EXPENSE },
                        label = { Text("Расход") }
                    )
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("От чего") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it.filter(Char::isDigit) },
                    label = { Text("Сумма") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}
