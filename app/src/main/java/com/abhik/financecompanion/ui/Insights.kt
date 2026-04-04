package com.abhik.financecompanion.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
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
import java.util.Locale

@Composable
fun Insights(viewModel: FinanceViewModel) {

    val transactions by viewModel.transactions.collectAsState()
    val expenses = transactions.filter { it.type == "EXPENSE" }

    val categoryTotals = expenses.groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }

    val totalExpenseAmount = categoryTotals.sumOf { it.second }
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())

    val chartColors = listOf(
        Color(0xFFFF6384),
        Color(0xFF36A2EB),
        Color(0xFFFFCE56),
        Color(0xFF4BC0C0),
        Color(0xFF9966FF),
        Color(0xFFFF9F40)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                painter = painterResource(id = R.drawable.backgroundimg),
                contentScale = ContentScale.Crop
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Spending Insights",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (expenses.isEmpty()){
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Log some expenses to see your patterns!",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            }
            else{
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(200.dp)) {
                        var startAngle = -90f

                        categoryTotals.forEachIndexed { index, pair ->
                            val amount = pair.second
                            val sweepAngle = ((amount / totalExpenseAmount) * 360f).toFloat()
                            val color = chartColors[index % chartColors.size]

                            drawArc(
                                color = color,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = 60f, cap = StrokeCap.Butt)
                            )
                            startAngle += sweepAngle
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Spent", color = Color.Gray, fontSize = 14.sp)
                        Text(
                            text = currencyFormatter.format(totalExpenseAmount),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Highest Spending",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ){
                    items(categoryTotals.size) { index ->
                        val (category, amount) = categoryTotals[index]
                        val color = chartColors[index % chartColors.size]
                        val percentage = ((amount / totalExpenseAmount) * 100).toInt()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ){
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(16.dp),
                                    shape = MaterialTheme.shapes.small,
                                    color = color
                                ) {}
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = category, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = currencyFormatter.format(amount),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(text = "$percentage%", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}