package com.example.presentation.dashboard

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.models.CoordinatedCampaign
import com.example.data.models.ThreatIncidentEntity
import com.example.presentation.DashboardMetrics
import com.example.presentation.LiveMonitoringTelemetry
import com.example.presentation.components.PulsingLiveDot
import com.example.presentation.components.RiskScoreGauge
import com.example.presentation.components.SeverityBadge
import com.example.presentation.components.ThreatTrendChart
import com.example.presentation.components.platformIcon
import com.example.presentation.components.severityColor
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.RiskCritical
import com.example.ui.theme.RiskHigh
import com.example.ui.theme.RiskLow
import com.example.ui.theme.RiskMedium
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardScreen(
    liveTelemetry: LiveMonitoringTelemetry,
    metrics: DashboardMetrics,
    threats: List<ThreatIncidentEntity>,
    campaigns: List<CoordinatedCampaign>,
    onThreatClick: (String) -> Unit,
    onNavigateToFeedWithFilter: (String) -> Unit,
    onNavigateToScanners: () -> Unit,
    onNavigateToCampaigns: () -> Unit,
    onSimulateWsEvent: (Context) -> Unit,
    onTriggerTestPush: (Context) -> Unit
) {
    val context = LocalContext.current
    val topCampaign = campaigns.firstOrNull()
    val numberFormatter = NumberFormat.getNumberInstance(Locale.US)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. PROFESSIONAL LIVE MONITORING HEADER WITH CURRENT SCAN STATUS
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                    .testTag("live_monitoring_header_card")
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_banner),
                        contentDescription = "Live Monitoring Telemetry",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(255.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(255.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        CyberBackground.copy(alpha = 0.42f),
                                        CyberBackground.copy(alpha = 0.88f),
                                        CyberBackground
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Top Status Row: ● LIVE MONITORING + Current Scan Status Badge + IST Timestamp
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PulsingLiveDot(color = RiskLow)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "LIVE MONITORING",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = RiskLow,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = ElectricCyan.copy(alpha = 0.18f),
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(50))
                                ) {
                                    Text(
                                        text = "SCANNING ACTIVE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ElectricCyan,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Updated ${liveTelemetry.updatedIstTime}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "${liveTelemetry.protectedBrandsCount} Protected Banks • ${liveTelemetry.activeScannersCount} Active Scanners • ${liveTelemetry.sourcesOnlineCount} Sources Online",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Current Scan Status Progress Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Current Scan Status: AppIntelWorker & Social Graph Stream",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricCyan
                            )
                            Text(
                                text = "Last scan: ${liveTelemetry.lastScanSecondsAgo}s ago",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                                color = RiskLow
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            color = ElectricCyan,
                            trackColor = ElectricCyan.copy(alpha = 0.2f),
                            strokeCap = StrokeCap.Round,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Live Telemetry Counters Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LiveStatPill(
                                label = "Protected Brands",
                                value = "${liveTelemetry.protectedBrandsCount}",
                                modifier = Modifier.weight(1f)
                            )
                            LiveStatPill(
                                label = "Active Scanners",
                                value = "${liveTelemetry.activeScannersCount}",
                                modifier = Modifier.weight(1f)
                            )
                            LiveStatPill(
                                label = "Sources Online",
                                value = "${liveTelemetry.sourcesOnlineCount}",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LiveStatPill(
                                label = "Events Processed Today",
                                value = numberFormatter.format(liveTelemetry.eventsProcessedToday),
                                modifier = Modifier.weight(1f)
                            )
                            LiveStatPill(
                                label = "Threats Created Today",
                                value = "${liveTelemetry.threatsCreatedToday}",
                                highlightCyan = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Buttons: Stream Live WS Event + Test Alert
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { onSimulateWsEvent(context) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElectricCyan,
                                    contentColor = Color(0xFF002026)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("run_demo_scenario_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Bolt,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Stream Live Threat Event",
                                    style = MaterialTheme.typography.labelLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            OutlinedButton(
                                onClick = { onTriggerTestPush(context) },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("test_push_alert_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.NotificationsActive,
                                    contentDescription = "Test Push Notification",
                                    tint = RiskCritical,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Alert",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. RISK LEVEL COUNTERS FOR CRITICAL / HIGH / MEDIUM (plus Low & Today's summary)
        item {
            SectionHeader(
                title = "RISK LEVEL OVERVIEW",
                subtitle = "Tap Critical, High, or Medium to filter active incidents"
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Primary 3-Column Hero Row for Critical / High / Medium
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PrimaryRiskLevelCard(
                    label = "Critical",
                    count = metrics.criticalCount,
                    accentColor = RiskCritical,
                    scoreBand = "81–100",
                    onClick = { onNavigateToFeedWithFilter("Critical") },
                    modifier = Modifier.weight(1f).testTag("risk_card_critical")
                )
                PrimaryRiskLevelCard(
                    label = "High",
                    count = metrics.highCount,
                    accentColor = RiskHigh,
                    scoreBand = "61–80",
                    onClick = { onNavigateToFeedWithFilter("High") },
                    modifier = Modifier.weight(1f).testTag("risk_card_high")
                )
                PrimaryRiskLevelCard(
                    label = "Medium",
                    count = metrics.mediumCount,
                    accentColor = RiskMedium,
                    scoreBand = "31–60",
                    onClick = { onNavigateToFeedWithFilter("Medium") },
                    modifier = Modifier.weight(1f).testTag("risk_card_medium")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Secondary Row: Low Risk, New Threats Today, Correlated Campaigns
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RiskCounterCard(
                    label = "Low",
                    count = metrics.lowCount,
                    accentColor = RiskLow,
                    subtitle = "Score 0–30",
                    onClick = { onNavigateToFeedWithFilter("Low") },
                    modifier = Modifier.weight(1f).testTag("risk_card_low")
                )
                RiskCounterCard(
                    label = "New Today",
                    count = metrics.newThreatsToday,
                    accentColor = ElectricCyan,
                    subtitle = "4 Campaigns",
                    onClick = { onNavigateToFeedWithFilter("All") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. DYNAMIC THREAT VELOCITY CHART (Recharts-style Visualization for Last 7 Days)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .testTag("threat_velocity_chart_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "THREAT VELOCITY — LAST 7 DAYS",
                                style = MaterialTheme.typography.labelLarge,
                                color = ElectricCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Dynamic Recharts-style area visualization • Tap any day to inspect",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.Radar,
                            contentDescription = null,
                            tint = ElectricCyan
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    ThreatTrendChart(points = metrics.weeklyTrend)
                }
            }
        }

        // 4. COORDINATED CAMPAIGN SPOTLIGHT (Potential SBI Impersonation Campaign • 96/100)
        if (topCampaign != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF260918)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, RiskCritical.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                        .clickable { onNavigateToCampaigns() }
                        .testTag("coordinated_campaign_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Hub,
                                    contentDescription = null,
                                    tint = RiskCritical,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "COORDINATED CAMPAIGN",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = RiskCritical,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            SeverityBadge(severity = topCampaign.severity)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = topCampaign.title,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${topCampaign.linkedThreats.size} correlated assets • ${topCampaign.subtitle}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = ElectricCyan
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                topCampaign.assetVectorsSummary.forEach { bullet ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = null,
                                            tint = RiskCritical,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = bullet,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.9f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            RiskScoreGauge(
                                score = topCampaign.combinedRiskScore,
                                size = 84.dp,
                                strokeWidth = 8.dp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = RiskCritical.copy(alpha = 0.25f))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Detected: ${topCampaign.detectedAgo}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                            Text(
                                text = "Status: ${topCampaign.statusLabel}",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricCyan
                            )
                        }
                    }
                }
            }
        }

        // 5. PRIORITY INCIDENTS ROTATING ACROSS INDIAN BANKS
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(
                    title = "PRIORITY INCIDENTS (MULTI-BANK)",
                    subtitle = "Live stream across SBI, HDFC, ICICI, Axis, Kotak & PNB"
                )
                Surface(
                    color = ElectricCyan.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.clickable { onNavigateToFeedWithFilter("All") }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "All Threats",
                            style = MaterialTheme.typography.labelMedium,
                            color = ElectricCyan
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        val priorityBankThreats = threats
            .distinctBy { it.brandId }
            .take(6)

        items(
            items = priorityBankThreats,
            key = { it.threatId }
        ) { threat ->
            CompactThreatRowCard(
                threat = threat,
                onClick = { onThreatClick(threat.threatId) }
            )
        }
    }
}

@Composable
private fun PrimaryRiskLevelCard(
    label: String,
    count: Int,
    accentColor: Color,
    scoreBand: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        modifier = modifier
            .border(1.5.dp, accentColor.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(accentColor)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$count",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Score $scoreBand",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LiveStatPill(
    label: String,
    value: String,
    highlightCyan: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF101B3B).copy(alpha = 0.85f),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.border(
            width = 1.dp,
            color = if (highlightCyan) ElectricCyan.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.12f),
            shape = RoundedCornerShape(10.dp)
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                color = if (highlightCyan) ElectricCyan else Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = ElectricCyan
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RiskCounterCard(
    label: String,
    count: Int,
    accentColor: Color,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .border(1.dp, accentColor.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(accentColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = label.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = accentColor
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "$count",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun CompactThreatRowCard(
    threat: ThreatIncidentEntity,
    onClick: () -> Unit
) {
    val accent = severityColor(threat.severity)
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("threat_card_${threat.threatId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SeverityBadge(severity = threat.severity)
                    Text(
                        text = threat.brandName,
                        style = MaterialTheme.typography.labelLarge,
                        color = ElectricCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "• ${threat.detectedAgoLabel}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = platformIcon(threat.platform, threat.category),
                        contentDescription = threat.platform,
                        tint = ElectricCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = threat.typeLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${threat.targetTitle} (${threat.targetIdentifier})",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            RiskScoreGauge(
                score = threat.riskScore,
                size = 64.dp,
                strokeWidth = 6.dp,
                showSubtitle = false
            )
        }
    }
}
