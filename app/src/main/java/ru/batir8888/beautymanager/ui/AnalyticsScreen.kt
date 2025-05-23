package ru.batir8888.beautymanager.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.batir8888.beautymanager.BeautyManagerApplication
import ru.batir8888.beautymanager.data.model.AnalyticsResult
import ru.batir8888.beautymanager.viewmodels.AnalyticsViewModel
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    vm: AnalyticsViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = BeautyManagerApplication.database
                @Suppress("UNCHECKED_CAST")
                return AnalyticsViewModel(db.appointmentDao(), db.reportDao()) as T
            }
        }
    )
) {
    val zone       = ZoneId.systemDefault()
    val monthStart by vm.month.collectAsState()
    val canBuild   by vm.canBuild.collectAsState()
    val data       by vm.result.collectAsState()

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
                    val d = Instant.ofEpochMilli(monthStart).atZone(zone).toLocalDate()
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
                .padding(12.dp),
            verticalArrangement = Arrangement.Top
        ) {
            OutlinedButton(
                onClick = { vm.build() },
                enabled = canBuild,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Составить аналитику") }

            Spacer(Modifier.height(8.dp))

            data?.let { ShowReport(it) }
        }
    }
}

/* ---------- отображение отчёта ---------- */
@Composable
private fun ShowReport(a: AnalyticsResult) {
    val money = NumberFormat.getInstance().apply { maximumFractionDigits = 0 }
    val scroll = rememberScrollState()

    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(scroll)
    ) {
        Section("ИТОГО") {
            kv("Доход от клиентов:",  money.format(a.incomeClients))
            kv("Прочий доход:",        money.format(a.incomeReports))
            kvBold("ИТОГО доход:",     money.format(a.totalIncome))

            Spacer(Modifier.height(4.dp))
            kv("Расходы клиента:",     money.format(a.expenseClients))
            kv("Прочие расходы:",      money.format(a.expenseReports))
            kvBold("ИТОГО расход:",    money.format(a.totalExpense))

            Spacer(Modifier.height(4.dp))
            kvBold("Чистая прибыль:",  money.format(a.profit))
        }

        Section("СРЕДНИЕ ПОКАЗАТЕЛИ") {
            val avgCheck = if (a.uniqueClients == 0) 0 else a.incomeClients / a.uniqueClients
            val days     = (a.daysWorked.takeIf { it > 0 } ?: 1)
            kv("Средний чек:",             money.format(avgCheck))
            kv("Средний дневной доход:",   money.format(a.totalIncome / days))
            kv("Средний дневной расход:",  money.format(a.totalExpense / days))
            kv("Средняя дневная прибыль:", money.format(a.profit / days))
        }

        Section("АКТИВНОСТЬ") {
            kv("Клиентов:",  a.uniqueClients.toString())
            kv("Записей:",   a.recordsCount.toString())
            a.busiestDay?.let {
                kv("Самый загруженный день:",
                    "${it.dayOfMonth}.${it.monthValue} (${a.busiestCount})")
            }
        }

        Section("СРАВНЕНИЕ С ПРОШЛЫМ МЕСЯЦЕМ") {
            kv("Доход:",   delta(a.diffIncome,  a.diffIncomePct))
            kv("Расход:",  delta(a.diffExpense, a.diffExpensePct))
            kv("Прибыль:", delta(a.diffProfit,  a.diffProfitPct))
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Spacer(Modifier.height(8.dp))
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Column {
        content()
    }
}

@Composable
private fun kv(label: String, value: String, bold: Boolean = false) = Row(
    Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
) {
    Text(label, Modifier.weight(1f))
    Text(
        value,
        textAlign = TextAlign.End,
        fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal
    )
}

@Composable
private fun kvBold(label: String, value: String) = kv(label, value, bold = true)

private fun delta(abs: Long, pct: Double): String =
    (if (abs >= 0) "↑" else "↓") +
            " ${NumberFormat.getInstance().format(kotlin.math.abs(abs))} ₽ " +
            "(${String.format("%.1f", kotlin.math.abs(pct))} %)"
