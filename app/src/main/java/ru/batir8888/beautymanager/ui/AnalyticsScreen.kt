package ru.batir8888.beautymanager.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
                    IconButton(
                        onClick = { vm.changeMonth(-1) },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                title = {
                    val d = Instant.ofEpochMilli(monthStart).atZone(zone).toLocalDate()
                    Text(
                        text = d.format(DateTimeFormatter.ofPattern("LLLL yyyy"))
                            .replaceFirstChar { it.titlecase() },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    IconButton(
                        onClick = { vm.changeMonth(1) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            OutlinedButton(
                onClick = { vm.build() },
                enabled = canBuild,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = if (canBuild) {
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    } else {
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "📈 Составить аналитику",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(16.dp))

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
        Section(
            title = "💰 ИТОГО",
            color = MaterialTheme.colorScheme.primary,
            isMain = true
        ) {
            kv("Доход от клиентов:", money.format(a.incomeClients), valueColor = MaterialTheme.colorScheme.primary)
            kv("Прочий доход:", money.format(a.incomeReports), valueColor = MaterialTheme.colorScheme.primary)
            kvBold("ИТОГО доход:", money.format(a.totalIncome),
                labelColor = MaterialTheme.colorScheme.primary,
                valueColor = MaterialTheme.colorScheme.primary)

            Spacer(Modifier.height(8.dp))
            kv("Расходы клиента:", money.format(a.expenseClients), valueColor = MaterialTheme.colorScheme.secondary)
            kv("Прочие расходы:", money.format(a.expenseReports), valueColor = MaterialTheme.colorScheme.secondary)
            kvBold("ИТОГО расход:", money.format(a.totalExpense),
                labelColor = MaterialTheme.colorScheme.secondary,
                valueColor = MaterialTheme.colorScheme.secondary)

            Spacer(Modifier.height(8.dp))
            kvBold("💎 Чистая прибыль:", money.format(a.profit),
                labelColor = MaterialTheme.colorScheme.tertiary,
                valueColor = MaterialTheme.colorScheme.tertiary)
        }

        Section(
            title = "📊 СРЕДНИЕ ПОКАЗАТЕЛИ",
            color = MaterialTheme.colorScheme.secondary
        ) {
            val avgCheck = if (a.uniqueClients == 0) 0 else a.incomeClients / a.uniqueClients
            val days = (a.daysWorked.takeIf { it > 0 } ?: 1)
            kv("Средний чек:", money.format(avgCheck))
            kv("Средний дневной доход:", money.format(a.totalIncome / days))
            kv("Средний дневной расход:", money.format(a.totalExpense / days))
            kv("Средняя дневная прибыль:", money.format(a.profit / days))
        }

        Section(
            title = "👥 АКТИВНОСТЬ",
            color = MaterialTheme.colorScheme.tertiary
        ) {
            kv("Клиентов:", a.uniqueClients.toString())
            kv("Записей:", a.recordsCount.toString())
            a.busiestDay?.let {
                kv("Самый загруженный день:",
                    "${it.dayOfMonth}.${it.monthValue} (${a.busiestCount})")
            }
        }

        Section(
            title = "📈 СРАВНЕНИЕ С ПРОШЛЫМ МЕСЯЦЕМ",
            color = MaterialTheme.colorScheme.primary
        ) {
            kvDelta("Доход:", delta(a.diffIncome, a.diffIncomePct), a.diffIncome >= 0)
            kvDelta("Расход:", delta(a.diffExpense, a.diffExpensePct), a.diffExpense <= 0)
            kvDelta("Прибыль:", delta(a.diffProfit, a.diffProfitPct), a.diffProfit >= 0)
        }
    }
}

@Composable
private fun Section(
    title: String,
    color: Color = MaterialTheme.colorScheme.primary,
    isMain: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Spacer(Modifier.height(16.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isMain) 8.dp else 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                color.copy(alpha = 0.2f),
                                color.copy(alpha = 0.1f)
                            )
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = title,
                    style = if (isMain) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }

            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun kv(
    label: String,
    value: String,
    bold: Boolean = false,
    labelColor: Color = MaterialTheme.colorScheme.onSurface,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) = Row(
    Modifier
        .fillMaxWidth()
        .padding(vertical = 2.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    Text(
        text = label,
        modifier = Modifier.weight(1f),
        color = labelColor,
        fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal
    )
    Text(
        text = value,
        textAlign = TextAlign.End,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
        color = valueColor
    )
}

@Composable
private fun kvBold(
    label: String,
    value: String,
    labelColor: Color = MaterialTheme.colorScheme.onSurface,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) = kv(label, value, bold = true, labelColor = labelColor, valueColor = valueColor)

@Composable
private fun kvDelta(
    label: String,
    value: String,
    isPositive: Boolean
) = Row(
    Modifier
        .fillMaxWidth()
        .padding(vertical = 2.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    Text(
        text = label,
        modifier = Modifier.weight(1f),
        color = MaterialTheme.colorScheme.onSurface
    )
    Text(
        text = value,
        textAlign = TextAlign.End,
        fontWeight = FontWeight.Medium,
        color = if (isPositive) {
            Color(0xFF10B981) // Зеленый для положительных изменений
        } else {
            Color(0xFFEF4444) // Красный для отрицательных изменений
        }
    )
}

private fun delta(abs: Long, pct: Double): String =
    (if (abs >= 0) "↗️" else "↘️") +
            " ${NumberFormat.getInstance().format(kotlin.math.abs(abs))} ₽ " +
            "(${String.format("%.1f", kotlin.math.abs(pct))} %)"