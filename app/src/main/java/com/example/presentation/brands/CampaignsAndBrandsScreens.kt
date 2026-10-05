package com.example.presentation.brands

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.models.BrandEntity
import com.example.data.models.CoordinatedCampaign
import com.example.data.models.ThreatIncidentEntity
import com.example.presentation.components.PulsingLiveDot
import com.example.presentation.components.RiskScoreGauge
import com.example.presentation.components.SeverityBadge
import com.example.presentation.components.StatusChip
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

@Composable
fun CampaignsScreen(
    campaigns: List<CoordinatedCampaign>,
    onThreatClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("campaigns_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Hub,
                        contentDescription = null,
                        tint = RiskCritical,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CORRELATION ENGINE & CAMPAIGNS",
                        style = MaterialTheme.typography.labelLarge,
                        color = ElectricCyan
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Fake support profile → links to look-alike domain → promotes unverified APK. Grouped into unified multi-vector campaigns.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(items = campaigns, key = { it.campaignId }) { campaign ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, RiskCritical.copy(alpha = 0.65f), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SeverityBadge(severity = campaign.severity)
                                Text(
                                    text = campaign.campaignId,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ElectricCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = campaign.title,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Assets: ${campaign.linkedThreats.size} • ${campaign.subtitle} • Status: ${campaign.statusLabel}",
                                style = MaterialTheme.typography.labelMedium,
                                color = ElectricCyan
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        RiskScoreGauge(
                            score = campaign.combinedRiskScore,
                            size = 86.dp,
                            strokeWidth = 8.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "CORRELATED ATTACK GRAPH",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricCyan
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            campaign.assetVectorsSummary.forEachIndexed { index, vec ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = RiskCritical,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = vec,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                if (index < campaign.assetVectorsSummary.lastIndex) {
                                    Text(
                                        text = "   ↓ links / promotes",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ElectricCyan,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "SHARED INDICATORS",
                        style = MaterialTheme.typography.labelLarge,
                        color = ElectricCyan
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    campaign.sharedSignals.forEach { signal ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = signal,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "CORRELATED INCIDENT ASSETS (${campaign.linkedThreats.size})",
                        style = MaterialTheme.typography.labelLarge,
                        color = ElectricCyan
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    campaign.linkedThreats.forEach { threat ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onThreatClick(threat.threatId) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = platformIcon(threat.platform, threat.category),
                                        contentDescription = null,
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "${threat.platform}: ${threat.targetTitle}",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = threat.targetIdentifier,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ElectricCyan
                                        )
                                    }
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    StatusChip(status = threat.status)
                                    Text(
                                        text = "${threat.riskScore}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                                        color = RiskCritical,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BrandManagementScreen(
    brands: List<BrandEntity>,
    allThreats: List<ThreatIncidentEntity>,
    selectedBrand: BrandEntity?,
    onSelectBrand: (String) -> Unit,
    onBackFromBrandDetail: () -> Unit,
    onThreatClick: (String) -> Unit,
    onSaveBrand: (String?, String, String, String, String, String, String, String, String, String, String, String) -> Unit,
    onDeleteBrand: (String, String) -> Unit
) {
    var editingBrand by remember { mutableStateOf<BrandEntity?>(null) }
    var showBrandDialog by remember { mutableStateOf(false) }
    var brandPendingDeletion by remember { mutableStateOf<BrandEntity?>(null) }

    if (selectedBrand != null) {
        val brandThreats = allThreats.filter {
            it.brandId.equals(selectedBrand.brandId, ignoreCase = true) ||
                it.brandName.equals(selectedBrand.name, ignoreCase = true)
        }
        BankDetailDashboardScreen(
            brand = selectedBrand,
            brandThreats = brandThreats,
            onBack = onBackFromBrandDetail,
            onEditBrand = {
                editingBrand = selectedBrand
                showBrandDialog = true
            },
            onDeleteBrandRequest = {
                brandPendingDeletion = selectedBrand
            },
            onThreatClick = onThreatClick
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("brands_list"),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, ElectricCyan.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Image(
                                painter = painterResource(id = R.drawable.img_bank_vault_banner),
                                contentDescription = "Protected Indian Banks Allowlist",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(148.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(148.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                CyberBackground.copy(alpha = 0.35f),
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    PulsingLiveDot(color = RiskLow)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "PROTECTED BRANDS • OFFICIAL_ASSETS REGISTRY",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = ElectricCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${brands.size} Protected Indian Banks",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Tap any bank for its dashboard, or use Edit / Delete to manage monitored banks",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }

                items(items = brands, key = { it.brandId }) { brand ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, ElectricCyan.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                            .clickable { onSelectBrand(brand.brandId) }
                            .testTag("brand_card_${brand.brandId}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        color = ElectricCyan.copy(alpha = 0.16f),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Filled.Business,
                                                contentDescription = null,
                                                tint = ElectricCyan
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = brand.name,
                                                style = MaterialTheme.typography.titleLarge,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Filled.Verified,
                                                contentDescription = "Protected Bank",
                                                tint = RiskLow,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Text(
                                            text = "App: ${brand.officialAppName} • Domains: ${brand.officialDomainsCsv}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            editingBrand = brand
                                            showBrandDialog = true
                                        },
                                        modifier = Modifier.testTag("edit_brand_${brand.brandId}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Edit,
                                            contentDescription = "Edit Bank",
                                            tint = ElectricCyan
                                        )
                                    }
                                    IconButton(
                                        onClick = { brandPendingDeletion = brand },
                                        modifier = Modifier.testTag("delete_brand_${brand.brandId}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.DeleteOutline,
                                            contentDescription = "Delete Bank",
                                            tint = RiskCritical
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(
                                        color = ElectricCyan.copy(alpha = 0.14f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "${brand.activeThreatsCount} active threats",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = ElectricCyan,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                    Surface(
                                        color = if (brand.criticalThreatsCount > 0) RiskCritical.copy(alpha = 0.18f)
                                        else RiskLow.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "${brand.criticalThreatsCount} critical",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (brand.criticalThreatsCount > 0) RiskCritical else RiskLow,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "ID: ${brand.brandId}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Open Bank Dashboard",
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = {
                    editingBrand = null
                    showBrandDialog = true
                },
                containerColor = ElectricCyan,
                contentColor = Color(0xFF002026),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("register_brand_fab")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Bank")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Add Bank",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showBrandDialog) {
        BrandEditorDialog(
            initialBrand = editingBrand,
            onDismiss = { showBrandDialog = false },
            onConfirm = { id, name, shortName, appName, aliases, domains, handles, pkgs, dev, cert, bio, kw ->
                onSaveBrand(id, name, shortName, appName, aliases, domains, handles, pkgs, dev, cert, bio, kw)
                showBrandDialog = false
            },
            onDeleteFromDialog = editingBrand?.let { target ->
                {
                    showBrandDialog = false
                    brandPendingDeletion = target
                }
            }
        )
    }

    brandPendingDeletion?.let { target ->
        AlertDialog(
            onDismissRequest = { brandPendingDeletion = null },
            title = {
                Text(
                    text = "Remove Protected Bank?",
                    style = MaterialTheme.typography.titleLarge,
                    color = RiskCritical
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete '${target.name}' (${target.brandId}) and its associated allowlist configuration from BrandShield AI?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteBrand(target.brandId, target.name)
                        brandPendingDeletion = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RiskCritical,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("confirm_delete_brand_button")
                ) {
                    Icon(Icons.Filled.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete Bank")
                }
            },
            dismissButton = {
                TextButton(onClick = { brandPendingDeletion = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BankDetailDashboardScreen(
    brand: BrandEntity,
    brandThreats: List<ThreatIncidentEntity>,
    onBack: () -> Unit,
    onEditBrand: () -> Unit,
    onDeleteBrandRequest: () -> Unit,
    onThreatClick: (String) -> Unit
) {
    BackHandler { onBack() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("bank_detail_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("bank_detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Protected Brands",
                            tint = ElectricCyan
                        )
                    }
                    Column {
                        Text(
                            text = brand.name.uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Protection Status: ",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            PulsingLiveDot(color = RiskLow)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Active",
                                style = MaterialTheme.typography.labelMedium,
                                color = RiskLow,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEditBrand) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit Official Allowlist",
                            tint = ElectricCyan
                        )
                    }
                    IconButton(
                        onClick = onDeleteBrandRequest,
                        modifier = Modifier.testTag("bank_detail_delete_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = "Delete Bank",
                            tint = RiskCritical
                        )
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ElectricCyan.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "OFFICIAL ASSETS (TRUSTED ALLOWLIST)",
                        style = MaterialTheme.typography.labelLarge,
                        color = ElectricCyan
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OfficialAssetCounterBox("Domains", brand.officialDomainsCount, ElectricCyan, Modifier.weight(1f))
                        OfficialAssetCounterBox("Apps", brand.officialAppsCount, RiskLow, Modifier.weight(1f))
                        OfficialAssetCounterBox("Social Profiles", brand.officialSocialProfilesCount, ElectricBlue, Modifier.weight(1f))
                        OfficialAssetCounterBox("Logos", brand.officialLogosCount, NeonPurple, Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                    Spacer(modifier = Modifier.height(10.dp))

                    AssetCategoryRow("Aliases", brand.aliases, ElectricBlue)
                    Spacer(modifier = Modifier.height(8.dp))
                    AssetCategoryRow("Official Domains", brand.officialDomains, ElectricCyan)
                    Spacer(modifier = Modifier.height(8.dp))
                    AssetCategoryRow(
                        "Official Mobile App & Packages",
                        listOf(brand.officialAppName) + brand.androidPackages,
                        RiskLow
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AssetCategoryRow("Official Social Handles", brand.socialHandles, NeonPurple)
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "THREATS TARGETING ${brand.shortName.uppercase()}",
                        style = MaterialTheme.typography.labelLarge,
                        color = ElectricCyan
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OfficialAssetCounterBox("Critical", brand.criticalThreatsCount, RiskCritical, Modifier.weight(1f))
                        OfficialAssetCounterBox("High", brand.highThreatsCount, RiskHigh, Modifier.weight(1f))
                        OfficialAssetCounterBox("Medium", brand.mediumThreatsCount, RiskMedium, Modifier.weight(1f))
                        OfficialAssetCounterBox("Low", brand.lowThreatsCount, RiskLow, Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            Text(
                text = "LATEST DETECTIONS",
                style = MaterialTheme.typography.labelLarge,
                color = ElectricCyan
            )
        }

        items(items = brandThreats, key = { it.threatId }) { threat ->
            val accent = severityColor(threat.severity)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .clickable { onThreatClick(threat.threatId) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SeverityBadge(severity = threat.severity)
                            Text(
                                text = threat.typeLabel,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${threat.targetTitle} (${threat.targetIdentifier})",
                            style = MaterialTheme.typography.labelMedium,
                            color = ElectricCyan
                        )
                        Text(
                            text = "Detected ${threat.detectedAgoLabel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    RiskScoreGauge(
                        score = threat.riskScore,
                        size = 62.dp,
                        strokeWidth = 6.dp,
                        showSubtitle = false
                    )
                }
            }
        }
    }
}

@Composable
private fun OfficialAssetCounterBox(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp)
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AssetCategoryRow(
    label: String,
    items: List<String>,
    chipColor: Color
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items.forEach { item ->
                Surface(
                    color = chipColor.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.border(1.dp, chipColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = item,
                        style = MaterialTheme.typography.labelMedium,
                        color = chipColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BrandEditorDialog(
    initialBrand: BrandEntity?,
    onDismiss: () -> Unit,
    onConfirm: (String?, String, String, String, String, String, String, String, String, String, String, String) -> Unit,
    onDeleteFromDialog: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(initialBrand?.name ?: "") }
    var shortName by remember { mutableStateOf(initialBrand?.shortName ?: "") }
    var officialAppName by remember { mutableStateOf(initialBrand?.officialAppName ?: "YONO SBI") }
    var aliases by remember { mutableStateOf(initialBrand?.aliasesCsv ?: "SBI, State Bank of India, YONO SBI") }
    var domains by remember { mutableStateOf(initialBrand?.officialDomainsCsv ?: "sbi.co.in, onlinesbi.sbi") }
    var handles by remember { mutableStateOf(initialBrand?.socialHandlesCsv ?: "@TheOfficialSBI") }
    var packages by remember { mutableStateOf(initialBrand?.androidPackagesCsv ?: "com.sbi.lotusintouch") }
    var developer by remember { mutableStateOf(initialBrand?.officialDeveloper ?: "State Bank of India") }
    var cert by remember { mutableStateOf(initialBrand?.officialCertificate ?: "SBI_Official_Prod_Cert_SHA256") }
    var bio by remember { mutableStateOf(initialBrand?.officialBio ?: "Official account of State Bank of India.") }
    var keywords by remember { mutableStateOf(initialBrand?.keywordsCsv ?: "YONO SBI, OnlineSBI") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialBrand == null) "Register Protected Bank (official_assets)" else "Edit / Delete Protected Bank",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Bank Name (e.g. State Bank of India)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("brand_name_input")
                )
                TextField(
                    value = shortName,
                    onValueChange = { shortName = it },
                    label = { Text("Short Name (e.g. SBI, HDFC Bank)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = officialAppName,
                    onValueChange = { officialAppName = it },
                    label = { Text("Official Mobile App (e.g. YONO SBI, open by Axis Bank)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = aliases,
                    onValueChange = { aliases = it },
                    label = { Text("Brand Aliases (comma-separated)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = domains,
                    onValueChange = { domains = it },
                    label = { Text("Official Domains Allowlist (e.g. sbi.co.in)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = handles,
                    onValueChange = { handles = it },
                    label = { Text("Official Social Handles") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = packages,
                    onValueChange = { packages = it },
                    label = { Text("Official Android Packages") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (onDeleteFromDialog != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = onDeleteFromDialog,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_delete_brand_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = "Delete Bank",
                            tint = RiskCritical
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Delete Bank from Allowlist",
                            color = RiskCritical,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            initialBrand?.brandId,
                            name,
                            shortName,
                            officialAppName,
                            aliases,
                            domains,
                            handles,
                            packages,
                            developer,
                            cert,
                            bio,
                            keywords
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan,
                    contentColor = Color(0xFF002026)
                ),
                modifier = Modifier.testTag("save_brand_button")
            ) {
                Text("Save to Allowlist")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
