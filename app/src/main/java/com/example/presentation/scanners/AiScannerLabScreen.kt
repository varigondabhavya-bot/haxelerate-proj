package com.example.presentation.scanners

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.models.BrandEntity
import com.example.data.models.ScannerEngineStatus
import com.example.data.models.SourceConnectorHealth
import com.example.data.models.ThreatIncidentEntity
import com.example.domain.engine.RiskDetectionEngine
import com.example.presentation.components.PulsingLiveDot
import com.example.presentation.components.RiskScoreGauge
import com.example.presentation.components.SeverityBadge
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.RiskCritical
import com.example.ui.theme.RiskHigh
import com.example.ui.theme.RiskLow
import com.example.ui.theme.RiskMedium
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiScannerLabScreen(
    brands: List<BrandEntity>,
    scannerEngines: List<ScannerEngineStatus>,
    connectorHealthList: List<SourceConnectorHealth>,
    latestCreatedThreat: ThreatIncidentEntity?,
    onRunSocialScan: (Context, BrandEntity, String, String, String, String, String, Int, Int, Boolean) -> Unit,
    onRunDomainScan: (Context, BrandEntity, String, Int, Boolean) -> Unit,
    onRunAppScan: (Context, BrandEntity, String, String, String, String, List<String>, Int) -> Unit,
    onInspectThreat: (String) -> Unit
) {
    val context = LocalContext.current
    val numberFormatter = NumberFormat.getNumberInstance(Locale.US)

    var selectedBrandIdx by remember { mutableIntStateOf(0) }
    val activeBrand = brands.getOrNull(selectedBrandIdx) ?: BrandEntity(
        brandId = "SBI",
        name = "State Bank of India",
        shortName = "SBI",
        officialAppName = "YONO SBI",
        aliasesCsv = "SBI, State Bank of India, YONO SBI",
        officialDomainsCsv = "sbi.co.in, onlinesbi.sbi",
        socialHandlesCsv = "@TheOfficialSBI",
        androidPackagesCsv = "com.sbi.lotusintouch",
        officialDeveloper = "State Bank of India",
        officialCertificate = "SBI_Official_Prod_Cert_SHA256",
        officialBio = "Official account of State Bank of India.",
        keywordsCsv = "YONO SBI, OnlineSBI"
    )

    var selectedEngineTab by remember { mutableIntStateOf(0) }
    val engineTabs = listOf(
        "Social Monitor",
        "AppIntelWorker",
        "Domain Monitor",
        "Scam Text AI"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Scanner Telemetry Banner (using generated img_scanner_telemetry)
        Card(
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ElectricCyan.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(id = R.drawable.img_scanner_telemetry),
                    contentDescription = "AI Scanners Telemetry",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(145.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(145.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    CyberBackground.copy(alpha = 0.35f),
                                    CyberBackground.copy(alpha = 0.9f),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PulsingLiveDot(color = RiskLow)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI SCANNERS & COLLECTION WORKERS",
                            style = MaterialTheme.typography.labelLarge,
                            color = ElectricCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Real-Time Queue & Connector Pipeline",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Live Sources → Collection Workers → Event Queue → AI & Risk Engine → WebSocket",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // 2. AI SCANNERS STATUS CARDS (Exact Section 13: Social Monitor, App Monitor, Domain Monitor, Logo Intelligence, Scam Text AI)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "ACTIVE AI SCANNERS (LIVE TELEMETRY)",
                style = MaterialTheme.typography.labelLarge,
                color = ElectricCyan
            )

            scannerEngines.forEach { engine ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                        .testTag("scanner_status_${engine.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PulsingLiveDot(color = RiskLow)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = engine.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = RiskLow.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = engine.status,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = RiskLow,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Last scan: ${engine.lastScanSecondsAgo} sec ago • ${engine.pollingIntervalLabel}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = numberFormatter.format(engine.metricCount),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = ElectricCyan
                            )
                            Text(
                                text = engine.metricLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 3. SOURCE CONNECTOR HEALTH & POLLING FREQUENCIES (Section 4, 14, 23)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "SOURCE ADAPTERS & CONNECTOR HEALTH",
                    style = MaterialTheme.typography.labelLarge,
                    color = ElectricCyan
                )
                Text(
                    text = "Authorized APIs & feeds with adaptive polling frequencies (1–60 min)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                connectorHealthList.forEach { conn ->
                    val badgeColor = when (conn.healthState) {
                        "HEALTHY" -> RiskLow
                        "RATE LIMITED" -> RiskHigh
                        "DELAYED" -> RiskMedium
                        else -> RiskCritical
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = conn.connectorName,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "• ${conn.platformCategory}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Last Successful Scan: ${conn.lastSuccessfulScan} (${conn.pollingPolicy})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = conn.note,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElectricCyan
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = badgeColor.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.border(1.dp, badgeColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            ) {
                                Text(
                                    text = conn.healthState,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = badgeColor,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. INTERACTIVE SANDBOX TESTBENCH (Select any of the 10 Indian Banks & test live scanning)
        Text(
            text = "INTERACTIVE AI SCANNER SANDBOX",
            style = MaterialTheme.typography.labelLarge,
            color = ElectricCyan
        )

        // Bank Selector Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            brands.forEachIndexed { idx, bank ->
                val isSelected = selectedBrandIdx == idx
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedBrandIdx = idx },
                    label = { Text(bank.shortName.ifBlank { bank.name }, style = MaterialTheme.typography.labelLarge) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElectricCyan.copy(alpha = 0.22f),
                        selectedLabelColor = ElectricCyan
                    )
                )
            }
        }

        // Scanner Mode Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            engineTabs.forEachIndexed { idx, title ->
                val isSelected = selectedEngineTab == idx
                val icon = when (idx) {
                    0 -> Icons.Filled.Share
                    1 -> Icons.Filled.Android
                    2 -> Icons.Filled.Language
                    else -> Icons.Filled.TextFields
                }
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedEngineTab = idx },
                    leadingIcon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) ElectricCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = { Text(title, style = MaterialTheme.typography.labelLarge) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElectricCyan.copy(alpha = 0.22f),
                        selectedLabelColor = ElectricCyan
                    ),
                    modifier = Modifier.testTag("scanner_tab_$idx")
                )
            }
        }

        // Latest Scan Result Banner
        if (latestCreatedThreat != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, ElectricCyan, RoundedCornerShape(18.dp))
                    .clickable { onInspectThreat(latestCreatedThreat.threatId) }
                    .testTag("latest_scan_result_card")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SeverityBadge(severity = latestCreatedThreat.severity)
                            Text(
                                text = "NEW_THREAT: ${latestCreatedThreat.threatId}",
                                style = MaterialTheme.typography.labelMedium,
                                color = ElectricCyan
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${latestCreatedThreat.brandName}: ${latestCreatedThreat.targetTitle}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        latestCreatedThreat.aiChecklist.take(3).forEach { reason ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = reason,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    RiskScoreGauge(
                        score = latestCreatedThreat.riskScore,
                        size = 78.dp,
                        strokeWidth = 7.dp
                    )
                }
            }
        }

        when (selectedEngineTab) {
            0 -> SocialMonitoringScannerPanel(
                brand = activeBrand,
                onScan = { platform, username, display, bio, url, logoSim, age, verified ->
                    onRunSocialScan(context, activeBrand, platform, username, display, bio, url, logoSim, age, verified)
                }
            )
            1 -> MobileAppMonitoringScannerPanel(
                brand = activeBrand,
                onScan = { appName, pkg, dev, cert, perms, iconSim ->
                    onRunAppScan(context, activeBrand, appName, pkg, dev, cert, perms, iconSim)
                }
            )
            2 -> DomainMonitoringScannerPanel(
                brand = activeBrand,
                onScan = { domain, age, sslValid ->
                    onRunDomainScan(context, activeBrand, domain, age, sslValid)
                }
            )
            else -> ScamTextClassifierPanel()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SocialMonitoringScannerPanel(
    brand: BrandEntity,
    onScan: (String, String, String, String, String, Int, Int, Boolean) -> Unit
) {
    var platform by remember(brand.brandId) { mutableStateOf("Instagram") }
    var username by remember(brand.brandId) { mutableStateOf("@${brand.shortName.lowercase().replace(" ", "_")}_helpdesk_test01") }
    var displayName by remember(brand.brandId) { mutableStateOf("${brand.shortName} Customer Assistance") }
    var bio by remember(brand.brandId) {
        mutableStateOf("Official ${brand.shortName} customer support desk. Urgent: Your KYC expired! Complete KYC immediately at our link or your account will be blocked.")
    }
    var externalUrl by remember(brand.brandId) { mutableStateOf("${brand.shortName.lowercase().replace(" ", "-")}-kyc-update.test") }
    var logoSimilarity by remember { mutableIntStateOf(97) }
    var accountAgeDays by remember { mutableIntStateOf(2) }
    var isVerified by remember { mutableStateOf(false) }

    val livePreview = remember(brand, username, displayName, bio, externalUrl, logoSimilarity, accountAgeDays, isVerified) {
        RiskDetectionEngine.evaluateSocialProfile(
            brand = brand,
            platform = platform,
            username = username,
            displayName = displayName,
            bio = bio,
            externalUrl = externalUrl,
            logoSimilarityInput = logoSimilarity,
            accountAgeDays = accountAgeDays,
            isVerified = isVerified
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "SOCIAL IMPERSONATION SCANNER (${brand.name.uppercase()})",
                style = MaterialTheme.typography.labelLarge,
                color = ElectricCyan
            )
            Text(
                text = "Weights: Name(15%) + Handle(15%) + Logo(20%) + Bio(10%) + URL(20%) + Scam(10%) + Metadata(10%)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = platform,
                    onValueChange = { platform = it },
                    label = { Text("Platform") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.weight(0.42f)
                )
                TextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Observed Handle") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .weight(0.58f)
                        .testTag("social_username_input")
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display Name") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.weight(0.5f)
                )
                TextField(
                    value = externalUrl,
                    onValueChange = { externalUrl = it },
                    label = { Text("Website (.test / .example)") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.weight(0.5f)
                )
            }

            TextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Observed Bio / Post Content") },
                minLines = 2,
                maxLines = 4,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Logo Similarity (CLIP / Perceptual Hash)", style = MaterialTheme.typography.bodySmall)
                    Text("$logoSimilarity%", style = MaterialTheme.typography.labelLarge, color = ElectricCyan)
                }
                Slider(
                    value = logoSimilarity.toFloat(),
                    onValueChange = { logoSimilarity = it.toInt() },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan)
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "LIVE SOCIAL RISK MODEL",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricCyan
                        )
                        Text(
                            text = "Name: ${livePreview.usernameSimilarity}% • Logo: ${livePreview.logoSimilarity}% • Scam: ${livePreview.scamClassification}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    SeverityBadge(severity = livePreview.severity)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${livePreview.totalRiskScore}/100",
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                        color = ElectricCyan
                    )
                }
            }

            Button(
                onClick = {
                    onScan(platform, username, displayName, bio, externalUrl, logoSimilarity, accountAgeDays, isVerified)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan,
                    contentColor = Color(0xFF002026)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("execute_social_scan_button")
            ) {
                Icon(Icons.Filled.Radar, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PUBLISH SOCIAL THREAT EVENT",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DomainMonitoringScannerPanel(
    brand: BrandEntity,
    onScan: (String, Int, Boolean) -> Unit
) {
    var domainInput by remember(brand.brandId) {
        mutableStateOf("${brand.shortName.lowercase().replace(" ", "-")}-kyc-verify.test")
    }
    var registrationAgeDays by remember { mutableIntStateOf(2) }
    var sslMatchesOrg by remember { mutableStateOf(false) }

    val liveDomainEval = remember(brand, domainInput, registrationAgeDays, sslMatchesOrg) {
        RiskDetectionEngine.evaluateDomain(
            brand = brand,
            rawDomain = domainInput,
            registrationAgeDays = registrationAgeDays,
            sslMatchesBrandOrg = sslMatchesOrg
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "DOMAIN MONITOR (${brand.name.uppercase()})",
                style = MaterialTheme.typography.labelLarge,
                color = ElectricCyan
            )
            Text(
                text = "Weights: Domain(30%) + Typosquat(20%) + Reg(10%) + Path(10%) + Content(15%) + Intel(15%)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "icici-kyc-verify.test",
                    "sbi-yono-kyc-update.test",
                    "hdfc-netbanking-verify.test",
                    "axis-open-reward.example",
                    brand.officialDomains.firstOrNull() ?: "sbi.co.in"
                ).forEach { preset ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable {
                            domainInput = preset
                            sslMatchesOrg = brand.officialDomains.any { it.equals(preset, ignoreCase = true) }
                        }
                    ) {
                        Text(
                            text = preset,
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricCyan,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            TextField(
                value = domainInput,
                onValueChange = { domainInput = it },
                label = { Text("Observed Domain (.test / .example)") },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("domain_scanner_input")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Observed Age: $registrationAgeDays Days • In Official Allowlist: ${if (sslMatchesOrg) "YES" else "NO"}",
                    style = MaterialTheme.typography.bodySmall
                )
                Switch(
                    checked = sslMatchesOrg,
                    onCheckedChange = { sslMatchesOrg = it }
                )
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Vector: ${liveDomainEval.attackVector}",
                            style = MaterialTheme.typography.labelLarge,
                            color = ElectricCyan
                        )
                        SeverityBadge(severity = liveDomainEval.severity)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = liveDomainEval.explanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = { onScan(domainInput, registrationAgeDays, sslMatchesOrg) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan,
                    contentColor = Color(0xFF002026)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("execute_domain_scan_button")
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SCAN DOMAIN & PUBLISH EVENT (RISK: ${liveDomainEval.riskScore})",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MobileAppMonitoringScannerPanel(
    brand: BrandEntity,
    onScan: (String, String, String, String, List<String>, Int) -> Unit
) {
    var appName by remember(brand.brandId) { mutableStateOf("${brand.shortName} Mobile Secure") }
    var packageName by remember(brand.brandId) { mutableStateOf("com.demo.${brand.shortName.lowercase().replace(" ", "")}.securebank") }
    var developerName by remember(brand.brandId) { mutableStateOf("Unverified Third-Party Dev") }
    var signingCert by remember(brand.brandId) { mutableStateOf("UnrecognizedDemoCert_SHA256") }
    var iconSimilarity by remember { mutableIntStateOf(91) }

    val selectedPermissions = remember {
        mutableStateListOf(
            "READ_SMS",
            "RECEIVE_SMS",
            "BIND_ACCESSIBILITY_SERVICE"
        )
    }

    val allDangerousPermissions = RiskDetectionEngine.highRiskPermissions.keys.toList()

    val liveAppEval = remember(brand, appName, packageName, developerName, signingCert, iconSimilarity, selectedPermissions.size) {
        RiskDetectionEngine.evaluateMobileApp(
            brand = brand,
            appName = appName,
            packageName = packageName,
            developerName = developerName,
            signingCert = signingCert,
            permissions = selectedPermissions.toList(),
            iconSimilarity = iconSimilarity
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "APPINTELWORKER • APK ALLOWLIST COMPARATOR",
                style = MaterialTheme.typography.labelLarge,
                color = ElectricCyan
            )
            Text(
                text = "Official App: ${brand.officialAppName} (${brand.androidPackagesCsv})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = appName,
                    onValueChange = { appName = it },
                    label = { Text("Observed App Name") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.weight(0.5f)
                )
                TextField(
                    value = packageName,
                    onValueChange = { packageName = it },
                    label = { Text("Package (com.demo.*)") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .weight(0.5f)
                        .testTag("apk_package_input")
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = developerName,
                    onValueChange = { developerName = it },
                    label = { Text("Developer") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.weight(0.5f)
                )
                TextField(
                    value = signingCert,
                    onValueChange = { signingCert = it },
                    label = { Text("Certificate Fingerprint") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.weight(0.5f)
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                allDangerousPermissions.forEach { perm ->
                    val active = selectedPermissions.contains(perm)
                    Surface(
                        color = if (active) RiskCritical.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = if (active) RiskCritical else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                if (active) selectedPermissions.remove(perm) else selectedPermissions.add(perm)
                            }
                    ) {
                        Text(
                            text = perm,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (active) RiskCritical else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Developer Verified: ${if (liveAppEval.certificateMatch) "YES" else "NO"} • Certificate Match: ${if (liveAppEval.certificateMatch) "YES" else "NO"}",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (liveAppEval.certificateMatch) RiskLow else RiskCritical
                        )
                        SeverityBadge(severity = liveAppEval.severity)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "App-name similarity: ${liveAppEval.appNameSimilarity}% • Icon similarity: ${liveAppEval.iconSimilarity}% • Risk = ${liveAppEval.riskScore}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Button(
                onClick = {
                    onScan(appName, packageName, developerName, signingCert, selectedPermissions.toList(), iconSimilarity)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan,
                    contentColor = Color(0xFF002026)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("execute_apk_scan_button")
            ) {
                Icon(Icons.Filled.Android, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ANALYZE OBSERVED APK (RISK: ${liveAppEval.riskScore})",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ScamTextClassifierPanel() {
    var officialSample by remember {
        mutableStateOf("Official account of State Bank of India. Download YONO SBI only from Google Play or App Store.")
    }
    var suspiciousSample by remember {
        mutableStateOf("Dear customer, your YONO SBI account will be suspended today due to KYC expired. Click here to verify immediately and send OTP.")
    }

    val semanticSim = remember(officialSample, suspiciousSample) {
        RiskDetectionEngine.calculateSemanticTextSimilarity(officialSample, suspiciousSample)
    }
    val scamResult = remember(suspiciousSample) {
        RiskDetectionEngine.analyzeScamLanguage(suspiciousSample, hasSuspiciousUrl = true)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "SCAM TEXT AI • SEMANTIC & LURE CLASSIFIER",
                style = MaterialTheme.typography.labelLarge,
                color = ElectricCyan
            )
            TextField(
                value = officialSample,
                onValueChange = { officialSample = it },
                label = { Text("Official Bank Reference Bio") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            )
            TextField(
                value = suspiciousSample,
                onValueChange = { suspiciousSample = it },
                label = { Text("Observed Social Post / SMS / Bio") },
                minLines = 3,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Semantic Similarity:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "$semanticSim%",
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                            color = ElectricCyan
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Scam Language Score:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "${scamResult.classification.uppercase()} (${scamResult.score}%)",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (scamResult.score >= 55) RiskCritical else RiskLow
                        )
                    }
                    if (scamResult.matchedTriggers.isNotEmpty()) {
                        Text(
                            text = "Detected Triggers: ${scamResult.matchedTriggers.joinToString(", ")}",
                            style = MaterialTheme.typography.labelSmall,
                            color = RiskHigh
                        )
                    }
                }
            }
        }
    }
}
