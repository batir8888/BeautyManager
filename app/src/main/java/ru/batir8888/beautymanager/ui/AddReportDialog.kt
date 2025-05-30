package ru.batir8888.beautymanager.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.batir8888.beautymanager.data.ReportType
import ru.batir8888.beautymanager.data.model.Report

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
            TextButton(
                onClick = {
                    val amt = amountStr.toLongOrNull() ?: return@TextButton
                    if (label.isBlank()) return@TextButton
                    onConfirm(
                        Report(
                            monthStart = monthStart,
                            type = type,
                            label = label.trim(),
                            amount = amt
                        )
                    )
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                enabled = label.isNotBlank() && amountStr.isNotBlank()
            ) {
                Text("💾 Сохранить", fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("Отмена", fontWeight = FontWeight.Medium)
            }
        },
        title = {
            Text(
                "📊 Новый отчёт",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column {
                // Выбор типа отчёта
                Text(
                    text = "Тип операции",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(
                        selected = type == ReportType.INCOME,
                        onClick = { type = ReportType.INCOME },
                        label = {
                            Text(
                                "💰 Доход",
                                fontWeight = if (type == ReportType.INCOME) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF10B981).copy(alpha = 0.2f),
                            selectedLabelColor = Color(0xFF10B981),
                            selectedLeadingIconColor = Color(0xFF10B981)
                        )
                    )
                    Spacer(Modifier.width(12.dp))
                    FilterChip(
                        selected = type == ReportType.EXPENSE,
                        onClick = { type = ReportType.EXPENSE },
                        label = {
                            Text(
                                "💸 Расход",
                                fontWeight = if (type == ReportType.EXPENSE) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFEF4444).copy(alpha = 0.2f),
                            selectedLabelColor = Color(0xFFEF4444),
                            selectedLeadingIconColor = Color(0xFFEF4444)
                        )
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Поле описания
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("📝 Описание") },
                    placeholder = { Text("Например: Аренда салона, Продажа косметики...") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(12.dp))

                // Поле суммы
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = {
                        val filtered = it.filter { char -> char.isDigit() }
                        if (filtered.length <= 10) { // Ограничение на 10 символов
                            amountStr = filtered
                        }
                    },
                    label = { Text("💳 Сумма (₽)") },
                    placeholder = { Text("0") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    suffix = { Text("₽", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shape = RoundedCornerShape(16.dp)
    )
}