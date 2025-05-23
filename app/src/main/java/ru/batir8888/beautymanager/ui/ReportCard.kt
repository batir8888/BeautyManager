package ru.batir8888.beautymanager.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ReportCard(
    report: Report,
    onDelete: () -> Unit
) {
    var dialog by remember { mutableStateOf(false) }
    if (dialog) {
        AlertDialog(
            onDismissRequest = { dialog = false },
            title = { Text("Удалить отчёт?") },
            confirmButton = {
                TextButton(onClick = { onDelete(); dialog = false }) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { dialog = false }) { Text("Отмена") }
            }
        )
    }

    Card(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .combinedClickable(
                onLongClick = { dialog = true },
                onClick = {}
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (report.type == ReportType.INCOME)
                MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (report.type == ReportType.INCOME) "Доход" else "Расход",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Text(report.amount.toString(), style = MaterialTheme.typography.titleMedium)
        }
        Text(report.label, Modifier.padding(start = 16.dp, bottom = 12.dp))
    }
}
