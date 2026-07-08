package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.WalkEarnViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WalletScreen(viewModel: WalkEarnViewModel) {
    val userState by viewModel.activeUser.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val isAr = viewModel.currentLanguage.collectAsState().value == "ar"

    val user = userState ?: return

    var activeTab by remember { mutableStateOf("withdraw") } // "withdraw" or "deposit"

    // Form inputs
    var withdrawAmount by remember { mutableStateOf("1.00") }
    var destinationAddress by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("USDT (TRC-20)") }
    
    var depositAmount by remember { mutableStateOf("5.00") }
    var depositMethod by remember { mutableStateOf("Binance Pay") }

    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isSuccessMessage by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Core Balance Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = viewModel.getLabel("balance"),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = String.format(Locale.US, "$%.2f", user.balance),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = "Points",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${user.points} PTS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tab Selector (Withdraw / Deposit)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp)
        ) {
            Button(
                onClick = { activeTab = "withdraw" },
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTab == "withdraw") MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (activeTab == "withdraw") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(viewModel.getLabel("withdraw"), fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { activeTab = "deposit" },
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTab == "deposit") MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (activeTab == "deposit") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(viewModel.getLabel("deposit"), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status message toast banner
        if (statusMessage != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSuccessMessage) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isSuccessMessage) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = "Status icon",
                        tint = if (isSuccessMessage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = statusMessage!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSuccessMessage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { statusMessage = null }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = if (isSuccessMessage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // Form fields matching tab
        if (activeTab == "withdraw") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = viewModel.getLabel("withdraw"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = withdrawAmount,
                        onValueChange = { withdrawAmount = it },
                        label = { Text(viewModel.getLabel("amount_usd")) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_amount_input"),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = "Dollar") }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = destinationAddress,
                        onValueChange = { destinationAddress = it },
                        label = { Text(viewModel.getLabel("withdraw_address")) },
                        placeholder = { Text(if (isAr) "أدخل معرف Binance Pay أو عنوان المحفظة" else "Enter Binance Pay ID or Wallet address") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_address_input"),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Wallet") }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Simulated Binance & Crypto integration toggle
                    Text(
                        text = viewModel.getLabel("payment_method"),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val methods = listOf("USDT (TRC-20)", "Binance Pay API", "PayPal", "Bitcoin (BTC)")
                    methods.forEach { method ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedMethod == method) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedMethod == method,
                                onClick = { selectedMethod = method }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = method, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val amount = withdrawAmount.toDoubleOrNull() ?: 0.0
                            if (amount <= 0.0) {
                                statusMessage = if (isAr) "يرجى إدخال قيمة صحيحة" else "Please enter a valid amount"
                                isSuccessMessage = false
                                return@Button
                            }
                            if (destinationAddress.isBlank()) {
                                statusMessage = if (isAr) "يرجى كتابة عنوان المحفظة" else "Please provide a destination wallet address"
                                isSuccessMessage = false
                                return@Button
                            }
                            viewModel.requestWithdrawal(
                                amount,
                                destinationAddress,
                                selectedMethod,
                                onSuccess = {
                                    statusMessage = viewModel.getLabel("withdraw_success")
                                    isSuccessMessage = true
                                    destinationAddress = ""
                                },
                                onError = {
                                    statusMessage = viewModel.getLabel("withdraw_error")
                                    isSuccessMessage = false
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("withdraw_submit_button"),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(viewModel.getLabel("withdraw"), fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Deposit Tab
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = viewModel.getLabel("deposit"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = depositAmount,
                        onValueChange = { depositAmount = it },
                        label = { Text(viewModel.getLabel("amount_usd")) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("deposit_amount_input"),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = "Dollar") }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val amount = depositAmount.toDoubleOrNull() ?: 0.0
                            if (amount <= 0) {
                                statusMessage = if (isAr) "الرجاء إدخال مبلغ صحيح" else "Please enter a valid amount"
                                isSuccessMessage = false
                                return@Button
                            }
                            viewModel.requestDeposit(amount, depositMethod)
                            statusMessage = viewModel.getLabel("deposit_success")
                            isSuccessMessage = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("deposit_submit_button"),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(viewModel.getLabel("deposit"), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Binance / Crypto Readiness Hooks Block
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Construction,
                        contentDescription = "Readiness",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAr) "مؤشرات الربط الخارجي" else "External Integrations Layouts",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                
                IntegrationIndicatorItem(label = viewModel.getLabel("binance_ready"))
                IntegrationIndicatorItem(label = viewModel.getLabel("crypto_ready"))
                IntegrationIndicatorItem(label = viewModel.getLabel("db_ready"))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. Transaction Ledger List
        Text(
            text = viewModel.getLabel("transaction_history"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            textAlign = TextAlign.Start
        )

        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isAr) "لا توجد عمليات مسجلة حتى الآن." else "No transactions recorded yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        } else {
            transactions.take(10).forEach { tx ->
                TransactionCardItem(tx = tx, isAr = isAr)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun IntegrationIndicatorItem(label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.LockClock,
            contentDescription = "Ready Lock",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun TransactionCardItem(tx: com.example.data.model.TransactionRecord, isAr: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (tx.type == "WITHDRAWAL") MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (tx.type == "WITHDRAWAL") Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = "Tx Type",
                        tint = if (tx.type == "WITHDRAWAL") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (tx.type == "WITHDRAWAL") {
                            if (isAr) "سحب أرباح" else "Withdrawal"
                        } else if (tx.type == "DEPOSIT") {
                            if (isAr) "عملية شحن" else "Deposit Wallet"
                        } else {
                            if (isAr) "شراء من المتجر" else "Shop Purchase"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = tx.paymentMethod,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (tx.type == "WITHDRAWAL") "-$${String.format(Locale.US, "%.2f", tx.amount)}" else "+$${String.format(Locale.US, "%.2f", tx.amount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (tx.type == "WITHDRAWAL") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                
                // Status badge
                val badgeColor = when (tx.status) {
                    "COMPLETED" -> MaterialTheme.colorScheme.primary
                    "PENDING" -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.error
                }
                Text(
                    text = tx.status,
                    style = MaterialTheme.typography.labelSmall,
                    color = badgeColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
