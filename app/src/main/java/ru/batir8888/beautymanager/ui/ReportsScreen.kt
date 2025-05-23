package ru.batir8888.beautymanager.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.batir8888.beautymanager.BeautyManagerApplication
import ru.batir8888.beautymanager.viewmodels.ReportsViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    vm: ReportsViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(c: Class<T>): T {
                val db = BeautyManagerApplication.database
                @Suppress("UNCHECKED_CAST")
                return ReportsViewModel(db.reportDao()) as T
            }
        }
    )
) {
    val zone      = ZoneId.systemDefault()
    val month0    by vm.month.collectAsState()
    val reports   by vm.reports.collectAsState(emptyList())

    /* состояния диалогов */
    var showAdd   by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = { vm.changeMonth(-1) },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                title = {
                    val d = Instant.ofEpochMilli(month0).atZone(zone).toLocalDate()
                    Text(
                        d.format(DateTimeFormatter.ofPattern("LLLL yyyy"))
                            .replaceFirstChar { it.titlecase() }
                    )
                },
                actions = {
                    IconButton(onClick = { vm.changeMonth(1) },
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
            LazyColumn(Modifier.weight(1f)) {
                items(reports, key = { it.id }) { rep ->
                    ReportCard(
                        report = rep,
                        onDelete = { vm.delete(rep) }
                    )
                }
                item {
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { showAdd = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.onPrimary,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) { Text("+ Добавить отчёт") }
                }
            }
        }
    }

    if (showAdd) {
        AddReportDialog(
            monthStart = month0,
            onDismiss = { showAdd = false },
            onConfirm = {
                vm.save(it)
                showAdd = false
            }
        )
    }
}
