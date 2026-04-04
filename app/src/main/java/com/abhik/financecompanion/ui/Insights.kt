package com.abhik.financecompanion.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhik.financecompanion.R
import com.abhik.financecompanion.viewmodel.FinanceViewModel
import java.text.NumberFormat
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun Insights(viewModel: FinanceViewModel) {
    val transactions by viewModel.transactions.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()

    val monthlyExpenses = transactions.filter {
        val txnMonth = YearMonth.from(Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()))
        it.type == "EXPENSE" && txnMonth == selectedMonth
    }
    val categoryTotals = monthlyExpenses.groupBy { it.category }
        .mapValues { it.value.sumOf { txn -> txn.amount } }
        .toList().sortedByDescending { it.second }
    val totalExpenseAmount = categoryTotals.sumOf { it.second }

    val past6Months = (0..5).map { YearMonth.now().minusMonths(it.toLong()) }.reversed()
    val comparisonData = past6Months.map { month ->
        val monthTxns = transactions.filter {
            YearMonth.from(Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault())) == month
        }
        val inc = monthTxns.filter { it.type == "INCOME" }.sumOf { it.amount }
        val exp = monthTxns.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        Triple(month.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()), inc, exp)
    }

    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
    val chartColors = listOf(Color(0xFFFF6384), Color(0xFF36A2EB), Color(0xFFFFCE56), Color(0xFF4BC0C0), Color(0xFF9966FF), Color(0xFFFF9F40))

    Box(modifier = Modifier.fillMaxSize().paint(painter = painterResource(id = R.drawable.backgroundimg), contentScale = ContentScale.Crop)) {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            item {
                Text(text = "Financial Insights", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }

            item {
                Text("Monthly Breakdown: ${selectedMonth.month}", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.size(150.dp)) {
                            var startAngle = -90f
                            if (totalExpenseAmount > 0) {
                                categoryTotals.forEachIndexed { index, pair ->
                                    val sweep = ((pair.second / totalExpenseAmount) * 360f).toFloat()
                                    drawArc(color = chartColors[index % chartColors.size], startAngle = startAngle, sweepAngle = sweep, useCenter = false, style = Stroke(40f, cap = StrokeCap.Round))
                                    startAngle += sweep
                                }
                            } else {
                                drawCircle(Color.LightGray, style = Stroke(40f))
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Spent", fontSize = 12.sp, color = Color.Gray)
                            Text(currencyFormatter.format(totalExpenseAmount), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }

            item {
                Text("Spending by Category", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            items(categoryTotals) { (cat, amt) ->
                CategoryProgressItem(cat, amt, (amt / totalExpenseAmount).toFloat(), chartColors[categoryTotals.indexOfFirst { it.first == cat } % chartColors.size], currencyFormatter)
            }
            item { Spacer(modifier = Modifier.height(5.dp)) }

            item {
                Text("Income vs Expenses (Last 6 Months)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Card(modifier = Modifier.fillMaxWidth().height(250.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))) {
                    DoubleBarGraph(data = comparisonData)
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun DoubleBarGraph(data: List<Triple<String, Double, Double>>) {
    val maxVal = data.maxOf { maxOf(it.second, it.third) }.coerceAtLeast(1.0).toFloat()

    Column(modifier = Modifier.padding(16.dp)) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height - 60f
                val barWidth = 25f
                val spacing = canvasWidth / data.size

                data.forEachIndexed { index, (month, income, expense) ->
                    val xBase = index * spacing + (spacing / 4)

                    val incHeight = ((income.toFloat() / maxVal) * canvasHeight)
                    drawRoundRect(
                        color = Color(0xFF4CAF50),
                        topLeft = Offset(xBase, canvasHeight - incHeight),
                        size = Size(barWidth, incHeight),
                        cornerRadius = CornerRadius(4f, 4f)
                    )

                    val expHeight = ((expense.toFloat() / maxVal) * canvasHeight)
                    drawRoundRect(
                        color = Color(0xFFF44336),
                        topLeft = Offset(xBase + barWidth + 8f, canvasHeight - expHeight),
                        size = Size(barWidth, expHeight),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart)) {
                data.forEach { (month, _, _) ->
                    Text(text = month, modifier = Modifier.weight(1f), fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CategoryProgressItem(label: String, amount: Double, progress: Float, color: Color, formatter: NumberFormat) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text(formatter.format(amount), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.1f)
        )
    }
}