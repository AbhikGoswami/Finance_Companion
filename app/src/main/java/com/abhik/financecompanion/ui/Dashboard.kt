package com.abhik.financecompanion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhik.financecompanion.viewmodel.FinanceViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun Dashboard(viewModel: FinanceViewModel) {

    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val streak by viewModel.noSpendStreak.collectAsState()

    var showAddSheet by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(false)
    }

    val currentBalance = totalIncome - totalExpense
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Welcome Back!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("🔥 No-Spend Streak", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Keep it up! You're doing great.")
                        }
                        Text(
                            text = "$streak Days",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Current Balance", fontSize = 16.sp)
                        Text(
                            text = currencyFormatter.format(currentBalance),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Income", fontSize = 14.sp, color = Color(0xFF4CAF50))
                                Text(
                                    text = currencyFormatter.format(totalIncome),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Expenses", fontSize = 14.sp, color = Color(0xFFF44336))
                                Text(
                                    text = currencyFormatter.format(totalExpense),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Transaction")
        }

        if (showAddSheet) {
            AddTransaction(
                onDismiss = { showAddSheet = false },
                onSave = { amount, type, category, note ->

                    viewModel.addTransaction(
                        amount = amount,
                        type = type,
                        category = category,
                        dateMillis = System.currentTimeMillis(),
                        note = note
                    )
                    showAddSheet = false
                }
            )
        }

    }
}