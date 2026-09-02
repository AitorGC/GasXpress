package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PriceHighRed
import com.example.ui.theme.PriceLowGreen
import com.example.ui.theme.PriceMidAmber
import java.util.Locale

@Composable
fun FuelPriceBadge(
    price: Double,
    avgPrice: Double? = null,
    modifier: Modifier = Modifier
) {
    val (statusColor, statusBg) = when {
        avgPrice == null -> Pair(Color(0xFF0284C7), Color(0xFFE0F2FE))
        price <= avgPrice - 0.03 -> Pair(PriceLowGreen, Color(0xFFD1FAE5)) // Cheap
        price >= avgPrice + 0.03 -> Pair(PriceHighRed, Color(0xFFFEE2E2)) // Expensive
        else -> Pair(PriceMidAmber, Color(0xFFFEF3C7)) // Average
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = statusBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = String.format(Locale.getDefault(), "%.3f", price),
                color = statusColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "€/L",
                color = statusColor.copy(alpha = 0.85f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
    }
}
