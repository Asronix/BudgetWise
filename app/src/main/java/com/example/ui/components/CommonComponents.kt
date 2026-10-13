package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
    private val numberFormat: NumberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 2
    }

    private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val shortMonthFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())

    var activeCurrencySymbol: String = "$"

    fun formatCurrency(amount: Double, symbol: String = activeCurrencySymbol): String {
        return "$symbol${numberFormat.format(amount)}"
    }

    fun formatDate(timeMillis: Long): String {
        return dateFormat.format(Date(timeMillis))
    }

    fun formatMonthYear(year: Int, monthZeroIndexed: Int): String {
        val cal = java.util.Calendar.getInstance()
        cal.set(year, monthZeroIndexed, 1)
        return monthYearFormat.format(cal.time)
    }

    fun formatShortMonth(year: Int, monthZeroIndexed: Int): String {
        val cal = java.util.Calendar.getInstance()
        cal.set(year, monthZeroIndexed, 1)
        return shortMonthFormat.format(cal.time)
    }
}

fun getCategoryIcon(name: String): ImageVector {
    val lower = name.lowercase()
    return when {
        "food" in lower || "dining" in lower || "restaurant" in lower || "cafe" in lower -> Icons.Default.Restaurant
        "grocer" in lower || "market" in lower -> Icons.Default.ShoppingCart
        "housing" in lower || "rent" in lower || "home" in lower -> Icons.Default.Home
        "transport" in lower || "car" in lower || "gas" in lower || "commute" in lower -> Icons.Default.DirectionsCar
        "utilit" in lower || "electric" in lower || "water" in lower || "bill" in lower -> Icons.Default.ElectricBolt
        "entertain" in lower || "movie" in lower || "game" in lower -> Icons.Default.Movie
        "shop" in lower || "cloth" in lower -> Icons.Default.LocalMall
        "health" in lower || "med" in lower || "doctor" in lower -> Icons.Default.MedicalServices
        "saving" in lower || "invest" in lower -> Icons.Default.Savings
        "flight" in lower || "trip" in lower || "travel" in lower -> Icons.Default.Flight
        "laptop" in lower || "tech" in lower || "work" in lower -> Icons.Default.Laptop
        "shield" in lower || "emergenc" in lower -> Icons.Default.Security
        "income" in lower || "salary" in lower || "pay" in lower -> Icons.Default.Payments
        else -> Icons.Default.Category
    }
}

fun getCategoryColor(name: String): Color {
    val lower = name.lowercase()
    return when {
        "housing" in lower || "rent" in lower -> Color(0xFF0F766E)
        "food" in lower || "dining" in lower -> Color(0xFFF59E0B)
        "grocer" in lower -> Color(0xFF10B981)
        "transport" in lower -> Color(0xFF3B82F6)
        "utilit" in lower -> Color(0xFF8B5CF6)
        "entertain" in lower -> Color(0xFFEC4899)
        "shop" in lower -> Color(0xFFF97316)
        "health" in lower -> Color(0xFFEF4444)
        "saving" in lower || "invest" in lower -> Color(0xFF14B8A6)
        "income" in lower || "salary" in lower -> Color(0xFF10B981)
        else -> Color(0xFF64748B)
    }
}

@Composable
fun CategoryIconBadge(
    category: String,
    modifier: Modifier = Modifier,
    size: Int = 42
) {
    val color = getCategoryColor(category)
    val icon = getCategoryIcon(category)

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = category,
            tint = color,
            modifier = Modifier.size((size * 0.55).dp)
        )
    }
}

@Composable
fun TransactionTypeBadge(
    type: String,
    modifier: Modifier = Modifier
) {
    val isIncome = type.equals("INCOME", ignoreCase = true)
    val bgColor = if (isIncome) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)
    val textColor = if (isIncome) Color(0xFF065F46) else Color(0xFF991B1B)
    val text = if (isIncome) "Income" else "Expense"

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
