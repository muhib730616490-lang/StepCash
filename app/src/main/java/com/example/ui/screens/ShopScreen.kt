package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.WalkEarnViewModel

@Composable
fun ShopScreen(viewModel: WalkEarnViewModel) {
    val shopItems by viewModel.shopItems.collectAsState()
    val userState by viewModel.activeUser.collectAsState()
    val isAr = viewModel.currentLanguage.collectAsState().value == "ar"
    val context = LocalContext.current

    val user = userState ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Balance Header Panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isAr) "نقاطك المتاحة للتسوق" else "Your Shopping Points",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "${user.points} PTS",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = "Coins",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. Title Section
        Text(
            text = viewModel.getLabel("shop"),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            textAlign = TextAlign.Start
        )

        // 3. Render Catalog Items
        if (shopItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isAr) "لا توجد ترقيات متوفرة في المتجر حالياً." else "No upgrades available in the store currently.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        } else {
            shopItems.forEach { item ->
                ShopItemRow(
                    item = item,
                    userPoints = user.points,
                    isAr = isAr,
                    onPurchase = {
                        viewModel.buyUpgrade(
                            item,
                            onSuccess = {
                                Toast.makeText(
                                    context,
                                    if (isAr) "تهانينا! تم شراء الترقية وتفعيل الميزة بنجاح." else "Congratulations! Feature purchased and active.",
                                    Toast.LENGTH_LONG
                                ).show()
                            },
                            onError = { error ->
                                Toast.makeText(
                                    context,
                                    if (isAr) "نقاطك لا تكفي لإتمام هذه العملية." else "You do not have enough points for this.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        )
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun ShopItemRow(
    item: com.example.data.model.ShopItem,
    userPoints: Int,
    isAr: Boolean,
    onPurchase: () -> Unit
) {
    val iconVector = when (item.iconName) {
        "flash_on" -> Icons.Default.FlashOn
        "star" -> Icons.Default.Star
        "speed" -> Icons.Default.Speed
        else -> Icons.Default.DirectionsRun
    }

    val iconColor = if (item.isVip) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.purchased) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (item.isVip && !item.purchased) {
            Spacer(modifier = Modifier.height(0.dp))
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f))
        } else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(modifier = Modifier.weight(1.5f)) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(iconColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = "Item Icon",
                            tint = iconColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isAr) item.titleAr else item.titleEn,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (item.isVip) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface
                        )
                        if (item.isVip) {
                            Text(
                                text = "VIP / مميز",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                // Price tag
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${item.pricePoints} PTS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isAr) item.descriptionAr else item.descriptionEn,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Button
            if (item.purchased) {
                Button(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        disabledContentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Active"
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isAr) "تم الشراء والتفعيل" else "Purchased & Active",
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                val isAffordable = userPoints >= item.pricePoints
                Button(
                    onClick = onPurchase,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("buy_shop_item_${item.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (item.isVip) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = if (isAr) "شراء الترقية الآن" else "Purchase Upgrade Now",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
