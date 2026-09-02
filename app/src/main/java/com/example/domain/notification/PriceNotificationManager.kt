package com.example.domain.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.dao.FavoriteStationDao
import com.example.data.model.FuelType
import com.example.data.model.GasStation
import java.util.Locale

class PriceNotificationManager(
    private val context: Context,
    private val favoriteDao: FavoriteStationDao
) {
    companion object {
        const val CHANNEL_ID = "fuel_price_alerts_channel"
        const val CHANNEL_NAME = "Alertas de Bajada de Precios"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones cuando el precio de la gasolina baje en tus estaciones favoritas"
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    suspend fun checkPriceDrops(currentStations: List<GasStation>): Int {
        val favorites = favoriteDao.getAllFavoritesList()
        if (favorites.isEmpty()) return 0

        var dropCount = 0
        val stationMap = currentStations.associateBy { it.id }

        for (fav in favorites) {
            val station = stationMap[fav.stationId] ?: continue
            val fuelType = FuelType.fromId(fav.preferredFuel)
            val currentPrice = station.getPriceFor(fuelType) ?: continue

            if (fav.lastKnownPrice > 0 && currentPrice < fav.lastKnownPrice) {
                val diff = fav.lastKnownPrice - currentPrice
                sendPriceDropNotification(
                    stationName = fav.name,
                    fuelName = fuelType.displayName,
                    newPrice = currentPrice,
                    oldPrice = fav.lastKnownPrice,
                    savings = diff
                )
                dropCount++
            }

            // Update recorded price
            favoriteDao.updateFavorite(
                fav.copy(
                    lastKnownPrice = currentPrice,
                    lastNotifiedPrice = currentPrice
                )
            )
        }

        return dropCount
    }

    fun sendPriceDropNotification(
        stationName: String,
        fuelName: String,
        newPrice: Double,
        oldPrice: Double,
        savings: Double
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val priceStr = String.format(Locale.getDefault(), "%.3f €/L", newPrice)
        val diffStr = String.format(Locale.getDefault(), "%.3f €/L", savings)

        val title = "¡Bajada de precio en $stationName!"
        val content = "$fuelName ha bajado a $priceStr (Ahorras $diffStr)"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_gas_launcher)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$content.\n¡Aprovecha para repostar al mejor precio!"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            val manager = NotificationManagerCompat.from(context)
            manager.notify(stationName.hashCode(), notification)
        } catch (e: SecurityException) {
            // Notifications permission not yet granted by user
        }
    }
}
