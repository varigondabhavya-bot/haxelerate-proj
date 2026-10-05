package com.example.presentation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.models.AlertEntity
import com.example.data.models.UserRole
import com.example.presentation.brands.BrandManagementScreen
import com.example.presentation.brands.CampaignsScreen
import com.example.presentation.components.SeverityBadge
import com.example.presentation.dashboard.DashboardScreen
import com.example.presentation.login.LoginScreen
import com.example.presentation.login.SplashScreen
import com.example.presentation.scanners.AiScannerLabScreen
import com.example.presentation.threats.ThreatDetailScreen
import com.example.presentation.threats.ThreatFeedScreen
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.RiskCritical
import kotlinx.coroutines.delay

fun tabIcon(tab: MainTab): ImageVector = when (tab) {
    MainTab.DASHBOARD -> Icons.Filled.Dashboard
    MainTab.THREATS -> Icons.Filled.DynamicFeed
    MainTab.SCANNERS -> Icons.Filled.Radar
    MainTab.CAMPAIGNS -> Icons.Filled.Hub
    MainTab.BRANDS -> Icons.Filled.Business
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrandShieldAppShell(
    viewModel: BrandShieldViewModel
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val selectedThreat by viewModel.selectedThreat.collectAsStateWithLifecycle()
    val selectedThreatNotes by viewModel.selectedThreatNotes.collectAsStateWithLifecycle()
    val selectedBrand by viewModel.selectedBrand.collectAsStateWithLifecycle()
    val liveTelemetry by viewModel.liveTelemetry.collectAsStateWithLifecycle()
    val scannerEngines by viewModel.scannerEngines.collectAsStateWithLifecycle()
    val connectorHealthList by viewModel.connectorHealthList.collectAsStateWithLifecycle()
    val dashboardMetrics by viewModel.dashboardMetrics.collectAsStateWithLifecycle()
    val allThreats by viewModel.allThreats.collectAsStateWithLifecycle()
    val filteredThreats by viewModel.filteredThreats.collectAsStateWithLifecycle()
    val threatFilter by viewModel.threatFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val brands by viewModel.brands.collectAsStateWithLifecycle()
    val campaigns by viewModel.coordinatedCampaigns.collectAsStateWithLifecycle()
    val alerts by viewModel.alerts.collectAsStateWithLifecycle()
    val firebaseUsers by viewModel.firebaseUsers.collectAsStateWithLifecycle()
    val showAlertsSheet by viewModel.showAlertsSheet.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBannerMessage.collectAsStateWithLifecycle()
    val isAiGenerating by viewModel.isAiGenerating.collectAsStateWithLifecycle()
    val latestScanThreat by viewModel.latestScanCreatedThreat.collectAsStateWithLifecycle()
    val darkTheme by viewModel.darkTheme.collectAsStateWithLifecycle()

    var showRoleDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { /* handled gracefully */ }
    )

    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(statusBanner) {
        if (statusBanner != null) {
            delay(4000)
            viewModel.clearBanner()
        }
    }

    if (authState.isSplashVisible) {
        SplashScreen(onSkipSplash = { viewModel.dismissSplashNow() })
        return
    }

    if (!authState.isAuthenticated) {
        LoginScreen(
            initialEmail = authState.analystEmail,
            initialRole = authState.role,
            loginError = authState.loginError,
            firebaseUsers = firebaseUsers,
            onLogin = { email, password, role ->
                viewModel.login(email, password, role)
            },
            onFirebaseEmailAuth = { ctx, email, pwd, confirmPwd, name, org, role, isSignUp ->
                viewModel.authenticateWithFirebaseEmail(
                    ctx, email, pwd, confirmPwd, name, org, role, isSignUp
                )
            },
            onGoogleFirebaseAuth = { ctx, email, name, role, org, isSignUp ->
                viewModel.authenticateWithGoogleFirebase(ctx, email, name, role, org, isSignUp)
            },
            onFacebookFirebaseAuth = { ctx, email, name, role, org, isSignUp ->
                viewModel.authenticateWithFacebookFirebase(ctx, email, name, role, org, isSignUp)
            },
            onQuickAuth = { method, role ->
                viewModel.quickSsoOrBiometricLogin(method, role)
            }
        )
        return
    }

    val unreadAlertsCount = alerts.count { !it.isRead }
    val criticalThreatsCount = allThreats.count { it.severity == "CRITICAL" && it.status != "RESOLVED" && it.status != "FALSE POSITIVE" }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpandedScreen = maxWidth >= 700.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Shield,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "BRANDSHIELD AI",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = authState.role.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElectricCyan
                                )
                            }
                        }
                    },
                    actions = {
                        Surface(
                            color = Color(authState.role.badgeColorHex).copy(alpha = 0.18f),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .border(
                                    1.dp,
                                    Color(authState.role.badgeColorHex).copy(alpha = 0.6f),
                                    RoundedCornerShape(50)
                                )
                                .clickable { showRoleDialog = true }
                                .testTag("top_role_switcher")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                when (authState.authProvider) {
                                    "google.com" -> Image(
                                        painter = painterResource(id = R.drawable.ic_google_logo),
                                        contentDescription = "Google Auth",
                                        modifier = Modifier.size(14.dp)
                                    )
                                    "facebook.com" -> Image(
                                        painter = painterResource(id = R.drawable.ic_facebook_logo),
                                        contentDescription = "Facebook Auth",
                                        modifier = Modifier.size(14.dp)
                                    )
                                    else -> Icon(
                                        imageVector = Icons.Filled.AdminPanelSettings,
                                        contentDescription = "Switch Analyst Role",
                                        tint = Color(authState.role.badgeColorHex),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = authState.analystName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(authState.role.badgeColorHex)
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.setShowAlertsSheet(true) },
                            modifier = Modifier.testTag("alerts_bell_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (unreadAlertsCount > 0) {
                                        Badge(containerColor = RiskCritical) {
                                            Text(
                                                text = "$unreadAlertsCount",
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Notifications,
                                    contentDescription = "Critical Alerts",
                                    tint = if (unreadAlertsCount > 0) RiskCritical else ElectricCyan
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.toggleTheme() },
                            modifier = Modifier.testTag("theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (darkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                contentDescription = "Toggle Theme",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { viewModel.logout() },
                            modifier = Modifier.testTag("logout_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Logout",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            },
            bottomBar = {
                if (!isExpandedScreen) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        MainTab.entries.forEach { tab ->
                            val selected = currentTab == tab && selectedThreat == null
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    viewModel.clearLatestScanPreview()
                                    viewModel.selectTab(tab)
                                },
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            if (tab == MainTab.THREATS && criticalThreatsCount > 0) {
                                                Badge(containerColor = RiskCritical) {
                                                    Text("$criticalThreatsCount", color = Color.White)
                                                }
                                            } else if (tab == MainTab.CAMPAIGNS && campaigns.isNotEmpty()) {
                                                Badge(containerColor = ElectricCyan) {
                                                    Text("${campaigns.size}", color = Color(0xFF002026))
                                                }
                                            } else if (tab == MainTab.BRANDS && brands.isNotEmpty()) {
                                                Badge(containerColor = ElectricCyan) {
                                                    Text("${brands.size}", color = Color(0xFF002026))
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = tabIcon(tab),
                                            contentDescription = tab.label
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = tab.label,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = ElectricCyan,
                                    selectedTextColor = ElectricCyan,
                                    indicatorColor = ElectricCyan.copy(alpha = 0.16f)
                                ),
                                modifier = Modifier.testTag("nav_tab_${tab.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isExpandedScreen) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        MainTab.entries.forEach { tab ->
                            val selected = currentTab == tab && selectedThreat == null
                            NavigationRailItem(
                                selected = selected,
                                onClick = {
                                    viewModel.clearLatestScanPreview()
                                    viewModel.selectTab(tab)
                                },
                                icon = {
                                    Icon(
                                        imageVector = tabIcon(tab),
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                modifier = Modifier.testTag("rail_tab_${tab.route}")
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    AnimatedVisibility(visible = statusBanner != null) {
                        statusBanner?.let { msg ->
                            Surface(
                                color = ElectricCyan.copy(alpha = 0.18f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, ElectricCyan.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = ElectricCyan,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { viewModel.clearBanner() },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "Dismiss notification banner",
                                            tint = ElectricCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    val activeThreat = selectedThreat
                    if (activeThreat != null) {
                        ThreatDetailScreen(
                            threat = activeThreat,
                            notes = selectedThreatNotes,
                            isAiGenerating = isAiGenerating,
                            onBack = { viewModel.closeThreatDetail() },
                            onAnalystAction = { action, customNote ->
                                viewModel.performAnalystAction(activeThreat, action, customNote)
                            },
                            onAssignAnalyst = { assignee ->
                                viewModel.assignAnalystToThreat(activeThreat, assignee)
                            },
                            onAddNote = { noteText ->
                                viewModel.addNoteToThreat(activeThreat.threatId, noteText)
                            },
                            onRunDeepGeminiAnalysis = {
                                viewModel.runGeminiDeepForensicAnalysis(activeThreat)
                            }
                        )
                    } else {
                        when (currentTab) {
                            MainTab.DASHBOARD -> DashboardScreen(
                                liveTelemetry = liveTelemetry,
                                metrics = dashboardMetrics,
                                threats = allThreats,
                                campaigns = campaigns,
                                onThreatClick = { viewModel.openThreatDetail(it) },
                                onNavigateToFeedWithFilter = { filter ->
                                    viewModel.setThreatFilter(filter)
                                    viewModel.selectTab(MainTab.THREATS)
                                },
                                onNavigateToScanners = {
                                    viewModel.selectTab(MainTab.SCANNERS)
                                },
                                onNavigateToCampaigns = {
                                    viewModel.selectTab(MainTab.CAMPAIGNS)
                                },
                                onSimulateWsEvent = { ctx ->
                                    viewModel.simulateIncomingWebSocketThreatEvent(ctx)
                                },
                                onTriggerTestPush = { ctx ->
                                    viewModel.triggerTestPushAlert(ctx)
                                }
                            )
                            MainTab.THREATS -> ThreatFeedScreen(
                                threats = filteredThreats,
                                selectedFilter = threatFilter,
                                searchQuery = searchQuery,
                                onFilterChange = { viewModel.setThreatFilter(it) },
                                onSearchChange = { viewModel.setSearchQuery(it) },
                                onThreatClick = { viewModel.openThreatDetail(it) }
                            )
                            MainTab.SCANNERS -> AiScannerLabScreen(
                                brands = brands,
                                scannerEngines = scannerEngines,
                                connectorHealthList = connectorHealthList,
                                latestCreatedThreat = latestScanThreat,
                                onRunSocialScan = { ctx, brand, plat, user, disp, bio, url, logo, age, ver ->
                                    viewModel.runLiveSocialScan(ctx, brand, plat, user, disp, bio, url, logo, age, ver)
                                },
                                onRunDomainScan = { ctx, brand, dom, age, ssl ->
                                    viewModel.runLiveDomainScan(ctx, brand, dom, age, ssl)
                                },
                                onRunAppScan = { ctx, brand, name, pkg, dev, cert, perms, iconSim ->
                                    viewModel.runLiveAppScan(ctx, brand, name, pkg, dev, cert, perms, iconSim)
                                },
                                onInspectThreat = { viewModel.openThreatDetail(it) }
                            )
                            MainTab.CAMPAIGNS -> CampaignsScreen(
                                campaigns = campaigns,
                                onThreatClick = { viewModel.openThreatDetail(it) }
                            )
                            MainTab.BRANDS -> BrandManagementScreen(
                                brands = brands,
                                allThreats = allThreats,
                                selectedBrand = selectedBrand,
                                onSelectBrand = { viewModel.openBrandDetail(it) },
                                onBackFromBrandDetail = { viewModel.closeBrandDetail() },
                                onThreatClick = { viewModel.openThreatDetail(it) },
                                onSaveBrand = { id, name, shortName, appName, aliases, domains, handles, pkgs, dev, cert, bio, kw ->
                                    viewModel.registerOrUpdateBrand(
                                        id, name, shortName, appName, aliases, domains, handles, pkgs, dev, cert, bio, kw
                                    )
                                },
                                onDeleteBrand = { brandId, brandName ->
                                    viewModel.deleteBrand(brandId, brandName)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAlertsSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.setShowAlertsSheet(false) },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            AlertsDrawerContent(
                alerts = alerts,
                onMarkAllRead = { viewModel.markAllAlertsRead() },
                onAlertClick = { alert ->
                    viewModel.setShowAlertsSheet(false)
                    viewModel.openThreatDetail(alert.threatId)
                },
                onTriggerPush = {
                    viewModel.triggerTestPushAlert(context)
                }
            )
        }
    }

    if (showRoleDialog) {
        AlertDialog(
            onDismissRequest = { showRoleDialog = false },
            title = { Text("Switch SOC Analyst Role") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    UserRole.entries.forEach { role ->
                        Surface(
                            color = if (authState.role == role) Color(role.badgeColorHex).copy(alpha = 0.22f)
                            else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.switchRole(role)
                                    showRoleDialog = false
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = role.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color(role.badgeColorHex),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = role.permissionsDesc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRoleDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun AlertsDrawerContent(
    alerts: List<AlertEntity>,
    onMarkAllRead: () -> Unit,
    onAlertClick: (AlertEntity) -> Unit,
    onTriggerPush: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CRITICAL DIGITAL THREAT ALERTS",
                    style = MaterialTheme.typography.labelLarge,
                    color = RiskCritical
                )
                Text(
                    text = "Real-time WebSocket & Push notifications (Risk Score >= 81)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row {
                TextButton(onClick = onTriggerPush) {
                    Text("Send OS Push", color = ElectricCyan)
                }
                IconButton(onClick = onMarkAllRead) {
                    Icon(
                        imageVector = Icons.Filled.DoneAll,
                        contentDescription = "Mark all alerts read",
                        tint = ElectricCyan
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.height(360.dp)
        ) {
            items(items = alerts, key = { it.alertId }) { alert ->
                Surface(
                    color = if (!alert.isRead) RiskCritical.copy(alpha = 0.14f)
                    else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (!alert.isRead) RiskCritical.copy(alpha = 0.6f) else Color.Transparent,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { onAlertClick(alert) }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SeverityBadge(severity = alert.severity)
                            Text(
                                text = "Risk: ${alert.riskScore}/100",
                                style = MaterialTheme.typography.labelLarge,
                                color = RiskCritical
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "⚠ ${alert.title} (${alert.platform})",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = alert.targetIdentifier,
                            style = MaterialTheme.typography.labelMedium,
                            color = ElectricCyan
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = alert.summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
