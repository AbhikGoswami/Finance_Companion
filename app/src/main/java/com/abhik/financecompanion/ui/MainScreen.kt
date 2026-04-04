package com.abhik.financecompanion.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.abhik.financecompanion.viewmodel.FinanceViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.CloudUpload

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : BottomNavItem("dashboard", "Dashboard", Icons.Default.Home)
    object Transactions : BottomNavItem("transactions", "Transactions", Icons.Default.List)
    object Insights : BottomNavItem("insights", "Insights", Icons.Default.PieChart)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: FinanceViewModel,
    userEmail: String,
    onSignOut: () -> Unit
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val transactions by viewModel.transactions.collectAsState()

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let {
            coroutineScope.launch {
                try {
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        val writer = outputStream.bufferedWriter()
                        writer.write("Amount,Type,Category,Note,Date\n")
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

                        transactions.forEach { txn ->
                            val dateStr = dateFormat.format(Date(txn.timestamp))
                            val cleanCategory = txn.category.replace(",", " ")
                            val cleanNote = txn.note.replace(",", " ")
                            writer.write("${txn.amount},${txn.type},${cleanCategory},${cleanNote},${dateStr}\n")
                        }
                        writer.flush()
                    }
                    Toast.makeText(context, "Data exported successfully!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Export failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val items = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Transactions,
        BottomNavItem.Insights
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(300.dp)) {

                Spacer(modifier = Modifier.height(32.dp))
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile Icon",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Welcome back,", fontSize = 14.sp, color = Color.Gray)
                    Text(text = userEmail, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))


                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Download, contentDescription = null) },
                    label = { Text("Export to CSV") },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        exportLauncher.launch("Finance_Report_${System.currentTimeMillis()}.csv")
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    "App Settings",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(start = 28.dp, top = 8.dp, bottom = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )

                val bioEnabled by viewModel.isBiometricEnabled.collectAsState()

                NavigationDrawerItem(
                    label = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Biometric Lock")
                            Switch(
                                checked = bioEnabled,
                                onCheckedChange = { viewModel.toggleBiometric(it) }
                            )
                        }
                    },
                    icon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                    selected = false,
                    onClick = { /* Toggle handled by Switch */ },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Backup to Cloud") },
                    icon = { Icon(Icons.Filled.CloudUpload, contentDescription = null) },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                        if (user != null) {
                            // UPDATED: Now passing context to show Toasts
                            viewModel.syncDataToCloud(user.uid, context)
                        } else {
                            Toast.makeText(context, "Log in to use Cloud Backup", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                Spacer(modifier = Modifier.weight(1f))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Logout, contentDescription = null, tint = Color.Red) },
                    label = { Text("Sign Out", color = Color.Red) },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        onSignOut()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding).padding(bottom = 24.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Finance Companion", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Open Menu")
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    items.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = BottomNavItem.Dashboard.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(BottomNavItem.Dashboard.route) { Dashboard(viewModel) }
                composable(BottomNavItem.Transactions.route) { TransactionsScreen(viewModel) }
                composable(BottomNavItem.Insights.route) { Insights(viewModel) }
            }
        }
    }
}