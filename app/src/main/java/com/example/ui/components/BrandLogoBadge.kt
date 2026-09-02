package com.example.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.BrandCategory

@Composable
fun BrandLogoBadge(
    brand: BrandCategory,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current
    val assetPath = "file:///android_asset/brand_logos/${brand.assetFileName}"
    
    val dynamicFilter: ColorFilter? = when {
        brand == BrandCategory.OTHER && isDark -> ColorFilter.tint(Color(0xFFE2E8F0))
        brand == BrandCategory.OTHER && !isDark -> ColorFilter.tint(Color(0xFF334155))
        else -> null
    }

    Box(
        modifier = modifier.size(46.dp).padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(assetPath)
                .crossfade(true)
                .build(),
            contentDescription = brand.displayName,
            contentScale = ContentScale.Fit,
            colorFilter = dynamicFilter,
            modifier = Modifier.fillMaxSize()
        )
    }
}
