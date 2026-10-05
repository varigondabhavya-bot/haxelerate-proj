package com.example.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.RiskCritical
import com.example.ui.theme.RiskCriticalContainer
import com.example.ui.theme.RiskHigh
import com.example.ui.theme.RiskHighContainer
import com.example.ui.theme.RiskLow
import com.example.ui.theme.RiskLowContainer
import com.example.ui.theme.RiskMedium
import com.example.ui.theme.RiskMediumContainer
import kotlin.math.abs

fun severityColor(severity: String): Color = when (severity.uppercase()) {
    "CRITICAL" -> RiskCritical
    "HIGH" -> RiskHigh
    "MEDIUM" -> RiskMedium
    else -> RiskLow
}

fun severityContainerColor(severity: String): Color = when (severity.uppercase()) {
    "CRITICAL" -> RiskCriticalContainer
    "HIGH" -> RiskHighContainer
    "MEDIUM" -> RiskMediumContainer
    else -> RiskLowContainer
}

fun riskScoreColor(score: Int): Color = when {
    score >= 81 -> RiskCritical
    score >= 61 -> RiskHigh
    score >= 31 -> RiskMedium
    else -> RiskLow
}

fun platformIcon(platform: String, category: String): ImageVector = when {
    category == "APP" || platform.contains("Android", ignoreCase = true) || platform.contains("APK", ignoreCase = true) ->
        Icons.Filled.Android
    category == "DOMAIN" || platform.contains("DNS", ignoreCase = true) || platform.contains("Domain", ignoreCase = true) ->
        Icons.Filled.Language
    platform.contains("Instagram", ignoreCase = true) || platform.contains("Telegram", ignoreCase = true) || platform.contains("X", ignoreCase = true) ->
        Icons.Filled.Share
    else -> Icons.Filled.Public
}

@Composable
fun SeverityBadge(
    severity: String,
    modifier: Modifier = Modifier
) {
    val fg = severityColor(severity)
    val bg = severityContainerColor(severity)
    val icon = when (severity.uppercase()) {
        "CRITICAL" -> Icons.Filled.Error
        "HIGH" -> Icons.Filled.Warning
        "MEDIUM" -> Icons.Filled.Shield
        else -> Icons.Filled.CheckCircle
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(50),
        modifier = modifier.border(1.dp, fg.copy(alpha = 0.6f), RoundedCornerShape(50))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = severity,
                tint = fg,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = severity.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = fg,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StatusChip(
    status: String,
    modifier: Modifier = Modifier
) {
    val color = when (status.uppercase()) {
        "CONFIRMED", "REPORTED" -> RiskCritical
        "UNDER REVIEW", "OPEN", "DETECTED" -> ElectricCyan
        "RESOLVED" -> RiskLow
        "FALSE POSITIVE" -> NeonPurple
        else -> ElectricBlue
    }

    Surface(
        color = color.copy(alpha = 0.14f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
    ) {
        Text(
            text = status.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun RiskScoreGauge(
    score: Int,
    size: Dp = 92.dp,
    strokeWidth: Dp = 8.dp,
    showSubtitle: Boolean = true
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (score.coerceIn(0, 100)) / 100f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "risk_gauge"
    )
    val color = riskScoreColor(score)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(size)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)

            drawArc(
                color = color.copy(alpha = 0.16f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            drawArc(
                color = color,
                startAngle = 135f,
                sweepAngle = 270f * animatedProgress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = color
            )
            if (showSubtitle) {
                Text(
                    text = "/ 100",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EvidenceProgressBar(
    label: String,
    percentage: Int,
    weightLabel: String? = null
) {
    val clamped = percentage.coerceIn(0, 100)
    val barColor = riskScoreColor(clamped)

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (weightLabel != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "($weightLabel)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = "$clamped%",
                style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                color = barColor
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        LinearProgressIndicator(
            progress = { clamped / 100f },
            color = barColor,
            trackColor = barColor.copy(alpha = 0.15f),
            strokeCap = StrokeCap.Round,
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}

/**
 * Interactive Recharts-style AreaChart visualization for the last 7 days of Threat Velocity.
 * Features:
 * - Smooth cubic bezier curve (`monotone` interpolation)
 * - Multi-stop vertical gradient area fill
 * - Dashed CartesianGrid with Y-axis scale ticks
 * - Interactive tap-to-inspect crosshair line + floating Recharts-style Tooltip card
 */
@Composable
fun ThreatTrendChart(
    points: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return
    var selectedIndex by remember(points) { mutableIntStateOf(points.lastIndex) }
    val safeSelectedIdx = selectedIndex.coerceIn(0, points.lastIndex)
    val activePoint = points[safeSelectedIdx]
    val prevPoint = if (safeSelectedIdx > 0) points[safeSelectedIdx - 1] else activePoint
    val delta = activePoint.second - prevPoint.second
    val deltaText = if (delta >= 0) "+$delta vs prior day" else "$delta vs prior day"

    val maxVal = ((points.maxOfOrNull { it.second } ?: 50) + 5).coerceAtLeast(50)
    val minVal = ((points.minOfOrNull { it.second } ?: 20) - 5).coerceAtLeast(0)
    val range = (maxVal - minVal).coerceAtLeast(10)

    Column(modifier = modifier.fillMaxWidth()) {
        // Recharts-style Interactive Floating Tooltip Header
        Surface(
            color = Color(0xFF0B142E),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(ElectricCyan)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Date: ${activePoint.first} 2026",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap any node on chart to inspect daily velocity",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 9.sp
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${activePoint.second} Threats",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = ElectricCyan
                    )
                    Text(
                        text = deltaText,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (delta > 0) RiskCritical else RiskLow
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Y-Axis Scale Labels
            Column(
                modifier = Modifier
                    .height(165.dp)
                    .padding(end = 6.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                val ticks = listOf(
                    maxVal,
                    minVal + (range * 2 / 3),
                    minVal + (range / 3),
                    minVal
                )
                ticks.forEach { tick ->
                    Text(
                        text = "$tick",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }

            // Recharts-Style Smooth Cubic Bezier Area Canvas
            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(165.dp)
                    .pointerInput(points) {
                        detectTapGestures { tapOffset ->
                            val stepX = if (points.size > 1) size.width.toFloat() / (points.size - 1) else size.width.toFloat()
                            val nearestIdx = points.indices.minByOrNull { idx ->
                                abs((idx * stepX) - tapOffset.x)
                            } ?: points.lastIndex
                            selectedIndex = nearestIdx
                        }
                    }
            ) {
                val w = size.width
                val h = size.height
                val stepX = if (points.size > 1) w / (points.size - 1) else w
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                // CartesianGrid Horizontal Dashed Lines
                for (i in 0..3) {
                    val y = h * (i / 3f)
                    drawLine(
                        color = Color.White.copy(alpha = 0.1f),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = dashEffect
                    )
                }

                // CartesianGrid Vertical Dashed Lines
                for (i in points.indices) {
                    val x = i * stepX
                    drawLine(
                        color = Color.White.copy(alpha = 0.05f),
                        start = Offset(x, 0f),
                        end = Offset(x, h),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = dashEffect
                    )
                }

                val coordinates = points.mapIndexed { index, pair ->
                    val x = index * stepX
                    val normalized = (pair.second - minVal).toFloat() / range.toFloat()
                    val y = h - (normalized * (h * 0.82f)) - (h * 0.08f)
                    Offset(x, y)
                }

                // Build Smooth Monotone Cubic Bezier Path
                val smoothLinePath = Path().apply {
                    moveTo(coordinates.first().x, coordinates.first().y)
                    for (i in 0 until coordinates.size - 1) {
                        val p0 = coordinates[i]
                        val p1 = coordinates[i + 1]
                        val controlX1 = p0.x + (p1.x - p0.x) * 0.45f
                        val controlY1 = p0.y
                        val controlX2 = p0.x + (p1.x - p0.x) * 0.55f
                        val controlY2 = p1.y
                        cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                    }
                }

                val smoothFillPath = Path().apply {
                    addPath(smoothLinePath)
                    lineTo(coordinates.last().x, h)
                    lineTo(coordinates.first().x, h)
                    close()
                }

                // AreaChart LinearGradient Fill
                drawPath(
                    path = smoothFillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            ElectricCyan.copy(alpha = 0.45f),
                            ElectricBlue.copy(alpha = 0.18f),
                            Color.Transparent
                        )
                    )
                )

                // Active Crosshair ReferenceLine on selected point
                val activeCoord = coordinates[safeSelectedIdx]
                drawLine(
                    color = ElectricCyan.copy(alpha = 0.7f),
                    start = Offset(activeCoord.x, 0f),
                    end = Offset(activeCoord.x, h),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = dashEffect
                )

                // Smooth Stroke Path
                drawPath(
                    path = smoothLinePath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(ElectricBlue, ElectricCyan, RiskCritical)
                    ),
                    style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
                )

                // Data Dots & Active Halo
                coordinates.forEachIndexed { idx, pt ->
                    val isSelected = idx == safeSelectedIdx
                    if (isSelected) {
                        drawCircle(
                            color = ElectricCyan.copy(alpha = 0.28f),
                            radius = 12.dp.toPx(),
                            center = pt
                        )
                    }
                    drawCircle(
                        color = if (isSelected) RiskCritical else ElectricCyan,
                        radius = if (isSelected) 6.5.dp.toPx() else 4.5.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = Color(0xFF0A1128),
                        radius = 2.2.dp.toPx(),
                        center = pt
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // X-Axis Day Labels + Values
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 28.dp, end = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEachIndexed { idx, (day, count) ->
                val isSelected = idx == safeSelectedIdx
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) ElectricCyan.copy(alpha = 0.16f) else Color.Transparent)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                        color = if (isSelected) ElectricCyan else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 10.sp
                    )
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) ElectricCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PulsingLiveDot(color: Color = RiskLow) {
    Box(
        modifier = Modifier
            .size(9.dp)
            .clip(CircleShape)
            .background(color)
    )
}
