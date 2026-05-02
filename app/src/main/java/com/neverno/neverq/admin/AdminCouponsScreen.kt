package com.neverno.neverq.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neverno.neverq.core.models.Coupon
import com.neverno.neverq.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCouponsScreen(viewModel: AdminViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadCoupons() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Coupons", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 18.sp) },
                actions = {
                    IconButton(onClick = { viewModel.loadCoupons() }) {
                        Icon(Icons.Default.Refresh, "Refresh", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CfNavy),
            )
        },
        containerColor = CfSurface,
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = CfBlue)
                uiState.error != null -> Text(uiState.error!!, Modifier.align(Alignment.Center), color = CfRed)
                uiState.coupons.isEmpty() -> CouponEmptyState(Modifier.align(Alignment.Center))
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(uiState.coupons, key = { it.id }) { coupon ->
                        CouponCard(coupon)
                    }
                }
            }
        }
    }
}

@Composable
private fun CouponCard(coupon: Coupon) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(if (coupon.isActive) CfOrangeLight else CfBorder),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.LocalOffer, null, tint = if (coupon.isActive) CfOrange else CfMuted, modifier = Modifier.size(22.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(coupon.code, color = CfNavy, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(coupon.description, color = CfMuted, fontSize = 12.sp)
                }
                CouponBadge(if (coupon.isActive) "Active" else "Inactive", if (coupon.isActive) CfGreen else CfRed, if (coupon.isActive) CfGreenLight else CfRedLight)
            }
            HorizontalDivider(color = CfBorder)
            CouponLine("Discount", "${coupon.discountValue} ${coupon.discountType}")
            CouponLine("Minimum order", "Rs. ${coupon.minOrder}")
            CouponLine("Usage", "${coupon.usedCount}/${coupon.usageLimit}")
            coupon.validTo?.let { CouponLine("Valid till", it.take(10)) }
        }
    }
}

@Composable
private fun CouponLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = CfMuted, fontSize = 13.sp)
        Text(value, color = CfText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CouponBadge(text: String, color: Color, background: Color) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(background).padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(text, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CouponEmptyState(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Default.LocalOffer, null, tint = CfMuted, modifier = Modifier.size(48.dp))
        Text("No coupons found", fontWeight = FontWeight.SemiBold, color = CfNavy)
    }
}
