package com.example.domain.pdf

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.local.entity.FuelExpenseEntity
import com.example.data.local.entity.PersonEntity
import com.example.data.local.entity.VehicleEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfReportGenerator {

    data class ReportFilter(
        val monthYearText: String, // e.g. "Septiembre 2026"
        val selectedPerson: PersonEntity? = null,
        val selectedVehicle: VehicleEntity? = null
    )

    fun generateMonthlyReport(
        context: Context,
        expenses: List<FuelExpenseEntity>,
        vehicles: List<VehicleEntity>,
        persons: List<PersonEntity>,
        filter: ReportFilter
    ): File? {
        val pageNumber = 1
        val pageWidth = 595 // Standard A4 width in points
        val pageHeight = 842 // Standard A4 height in points

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val vehicleMap = vehicles.associateBy { it.id }
        val personMap = persons.associateBy { it.id }

        // Filtered expenses
        val filteredExpenses = expenses.filter { expense ->
            val matchPerson = filter.selectedPerson == null || expense.personId == filter.selectedPerson.id
            val matchVehicle = filter.selectedVehicle == null || expense.vehicleId == filter.selectedVehicle.id
            matchPerson && matchVehicle
        }

        val totalSpent = filteredExpenses.sumOf { it.totalCostEuros }
        val totalLiters = filteredExpenses.sumOf { it.liters }
        val avgPricePerLiter = if (totalLiters > 0) totalSpent / totalLiters else 0.0

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val generatedDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        // Paints
        val primaryPaint = Paint().apply {
            color = Color.rgb(2, 132, 199) // Petrol Blue
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val amberPaint = Paint().apply {
            color = Color.rgb(234, 88, 12) // Fuel Amber
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val cardBgPaint = Paint().apply {
            color = Color.rgb(241, 245, 249) // Slate 100
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val tableHeaderPaint = Paint().apply {
            color = Color.rgb(30, 41, 59) // Slate 800
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val textTitlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val textSubtitlePaint = Paint().apply {
            color = Color.rgb(224, 242, 254)
            textSize = 11f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }

        val sectionTitlePaint = Paint().apply {
            color = Color.rgb(15, 23, 42) // Slate 900
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val bodyTextPaint = Paint().apply {
            color = Color.rgb(51, 65, 85) // Slate 700
            textSize = 9.5f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }

        val boldBodyPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val kpiValuePaint = Paint().apply {
            color = Color.rgb(2, 132, 199)
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val kpiLabelPaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 8.5f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        // 1. Header Banner
        val headerRect = RectF(0f, 0f, pageWidth.toFloat(), 85f)
        canvas.drawRect(headerRect, primaryPaint)

        // Accent strip
        val accentRect = RectF(0f, 82f, pageWidth.toFloat(), 85f)
        canvas.drawRect(accentRect, amberPaint)

        canvas.drawText("GASOLINA HOY • INFORME DE GASTOS", 24f, 38f, textTitlePaint)
        val filterSubtitle = buildString {
            append("Período: ${filter.monthYearText}")
            filter.selectedVehicle?.let { append(" | Vehículo: ${it.name} (${it.licensePlate})") }
            filter.selectedPerson?.let { append(" | Conductor: ${it.name} (${it.relationship})") }
        }
        canvas.drawText(filterSubtitle, 24f, 56f, textSubtitlePaint)
        canvas.drawText("Generado el: ${generatedDateFormat.format(Date())}", 24f, 70f, textSubtitlePaint)

        var currentY = 105f

        // 2. Summary KPI Cards
        val cardWidth = 125f
        val cardHeight = 52f
        val cardMargin = 12f
        var startX = 24f

        val kpis = listOf(
            Pair("GASTO TOTAL", String.format(Locale.getDefault(), "%.2f €", totalSpent)),
            Pair("LITROS TOTALES", String.format(Locale.getDefault(), "%.2f L", totalLiters)),
            Pair("PRECIO MEDIO", String.format(Locale.getDefault(), "%.3f €/L", avgPricePerLiter)),
            Pair("REPOSTAJES", "${filteredExpenses.size}")
        )

        for ((label, value) in kpis) {
            val r = RectF(startX, currentY, startX + cardWidth, currentY + cardHeight)
            canvas.drawRoundRect(r, 6f, 6f, cardBgPaint)
            canvas.drawText(label, startX + 10f, currentY + 18f, kpiLabelPaint)
            canvas.drawText(value, startX + 10f, currentY + 40f, kpiValuePaint)
            startX += cardWidth + cardMargin
        }

        currentY += cardHeight + 25f

        // 3. Summary By Person & Vehicle
        canvas.drawText("RESUMEN POR CONDUCTOR Y VEHÍCULO", 24f, currentY, sectionTitlePaint)
        currentY += 12f

        // Group by person
        val expensesByPerson = filteredExpenses.groupBy { it.personId }
        val expensesByVehicle = filteredExpenses.groupBy { it.vehicleId }

        val splitColWidth = (pageWidth - 48f - 16f) / 2f

        // Left Col: Persons
        var py = currentY + 12f
        val personRect = RectF(24f, py - 8f, 24f + splitColWidth, py + (expensesByPerson.size.coerceAtLeast(1) * 18f) + 12f)
        canvas.drawRoundRect(personRect, 4f, 4f, cardBgPaint)
        canvas.drawText("Por Persona / Conductor:", 32f, py + 6f, boldBodyPaint)
        py += 20f

        if (expensesByPerson.isEmpty()) {
            canvas.drawText("Sin registros", 32f, py, bodyTextPaint)
        } else {
            for ((pId, pExpenses) in expensesByPerson) {
                val pName = personMap[pId]?.name ?: "Conductor #$pId"
                val pCost = pExpenses.sumOf { it.totalCostEuros }
                val pLit = pExpenses.sumOf { it.liters }
                val line = "$pName: ${String.format(Locale.getDefault(), "%.2f €", pCost)} (${String.format(Locale.getDefault(), "%.1f L", pLit)})"
                canvas.drawText(line, 32f, py, bodyTextPaint)
                py += 16f
            }
        }

        // Right Col: Vehicles
        var vy = currentY + 12f
        val vStartX = 24f + splitColWidth + 16f
        val vRect = RectF(vStartX, vy - 8f, vStartX + splitColWidth, vy + (expensesByVehicle.size.coerceAtLeast(1) * 18f) + 12f)
        canvas.drawRoundRect(vRect, 4f, 4f, cardBgPaint)
        canvas.drawText("Por Vehículo:", vStartX + 8f, vy + 6f, boldBodyPaint)
        vy += 20f

        if (expensesByVehicle.isEmpty()) {
            canvas.drawText("Sin registros", vStartX + 8f, vy, bodyTextPaint)
        } else {
            for ((vId, vExpenses) in expensesByVehicle) {
                val v = vehicleMap[vId]
                val vName = if (v != null) "${v.name} (${v.licensePlate})" else "Vehículo #$vId"
                val vCost = vExpenses.sumOf { it.totalCostEuros }
                val vLit = vExpenses.sumOf { it.liters }
                val line = "$vName: ${String.format(Locale.getDefault(), "%.2f €", vCost)} (${String.format(Locale.getDefault(), "%.1f L", vLit)})"
                canvas.drawText(line, vStartX + 8f, vy, bodyTextPaint)
                vy += 16f
            }
        }

        currentY = maxOf(py, vy) + 20f

        // 4. Detail Table
        canvas.drawText("DETALLE DE REPOSTAJES", 24f, currentY, sectionTitlePaint)
        currentY += 12f

        // Table Header
        val tableHeaderRect = RectF(24f, currentY, pageWidth - 24f, currentY + 22f)
        canvas.drawRoundRect(tableHeaderRect, 4f, 4f, tableHeaderPaint)

        val headerTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val colX = floatArrayOf(30f, 85f, 175f, 255f, 345f, 410f, 465f, 515f)
        canvas.drawText("FECHA", colX[0], currentY + 14f, headerTextPaint)
        canvas.drawText("VEHÍCULO", colX[1], currentY + 14f, headerTextPaint)
        canvas.drawText("CONDUCTOR", colX[2], currentY + 14f, headerTextPaint)
        canvas.drawText("ESTACIÓN", colX[3], currentY + 14f, headerTextPaint)
        canvas.drawText("COMB.", colX[4], currentY + 14f, headerTextPaint)
        canvas.drawText("LITROS", colX[5], currentY + 14f, headerTextPaint)
        canvas.drawText("€/L", colX[6], currentY + 14f, headerTextPaint)
        canvas.drawText("TOTAL", colX[7], currentY + 14f, headerTextPaint)

        currentY += 26f

        val tableRowAltBg = Paint().apply {
            color = Color.rgb(248, 250, 252)
            style = Paint.Style.FILL
        }

        if (filteredExpenses.isEmpty()) {
            canvas.drawText("No hay gastos registrados en este período.", 30f, currentY + 15f, bodyTextPaint)
            currentY += 30f
        } else {
            val maxRows = 20
            filteredExpenses.take(maxRows).forEachIndexed { index, exp ->
                if (index % 2 == 1) {
                    val rowRect = RectF(24f, currentY - 2f, pageWidth - 24f, currentY + 16f)
                    canvas.drawRect(rowRect, tableRowAltBg)
                }

                val dateStr = dateFormat.format(Date(exp.timestamp))
                val vName = vehicleMap[exp.vehicleId]?.name ?: "V-${exp.vehicleId}"
                val pName = personMap[exp.personId]?.name ?: "P-${exp.personId}"
                val stationStr = if (exp.stationName.length > 14) exp.stationName.take(13) + "…" else exp.stationName
                val fuelStr = if (exp.fuelType.contains("95")) "G95" else if (exp.fuelType.contains("98")) "G98" else if (exp.fuelType.contains("diesel", ignoreCase = true)) "Diésel" else exp.fuelType

                canvas.drawText(dateStr, colX[0], currentY + 10f, bodyTextPaint)
                canvas.drawText(if (vName.length > 15) vName.take(14) + "…" else vName, colX[1], currentY + 10f, bodyTextPaint)
                canvas.drawText(if (pName.length > 12) pName.take(11) + "…" else pName, colX[2], currentY + 10f, bodyTextPaint)
                canvas.drawText(stationStr, colX[3], currentY + 10f, bodyTextPaint)
                canvas.drawText(fuelStr, colX[4], currentY + 10f, bodyTextPaint)
                canvas.drawText(String.format(Locale.getDefault(), "%.1f L", exp.liters), colX[5], currentY + 10f, bodyTextPaint)
                canvas.drawText(String.format(Locale.getDefault(), "%.3f", exp.pricePerLiter), colX[6], currentY + 10f, bodyTextPaint)
                canvas.drawText(String.format(Locale.getDefault(), "%.2f €", exp.totalCostEuros), colX[7], currentY + 10f, boldBodyPaint)

                canvas.drawLine(24f, currentY + 16f, pageWidth - 24f, currentY + 16f, linePaint)
                currentY += 18f
            }
        }

        // 5. Footer
        val footerY = pageHeight - 30f
        canvas.drawLine(24f, footerY - 10f, pageWidth - 24f, footerY - 10f, linePaint)
        val footerPaint = Paint().apply {
            color = Color.rgb(148, 163, 184)
            textSize = 8f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }
        canvas.drawText("Gasolina Hoy • Reporte Mensual Certificado • Datos oficiales MITECO", 24f, footerY, footerPaint)
        canvas.drawText("Página 1 de 1", pageWidth - 75f, footerY, footerPaint)

        document.finishPage(page)

        // Write to cache file
        val reportsDir = File(context.cacheDir, "reports").apply { if (!exists()) mkdirs() }
        val sanitizedMonth = filter.monthYearText.replace(" ", "_").lowercase()
        val file = File(reportsDir, "reporte_gasolina_${sanitizedMonth}_${System.currentTimeMillis()}.pdf")

        try {
            FileOutputStream(file).use { out ->
                document.writeTo(out)
            }
            document.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            document.close()
            return null
        }
    }

    fun sharePdfReport(context: Context, pdfFile: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Informe de Gastos de Gasolina")
            putExtra(Intent.EXTRA_TEXT, "Adjunto el informe mensual de gastos de gasolina.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(sendIntent, "Compartir informe PDF")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun viewPdfReport(context: Context, pdfFile: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(viewIntent)
        } catch (e: Exception) {
            // Fallback to chooser
            sharePdfReport(context, pdfFile)
        }
    }
}
