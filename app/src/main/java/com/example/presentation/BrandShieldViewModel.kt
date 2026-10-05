package com.example.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.api.FirebaseAuthDatabaseService
import com.example.data.api.GeminiAiAnalyzer
import com.example.data.models.AlertEntity
import com.example.data.models.BrandEntity
import com.example.data.models.CoordinatedCampaign
import com.example.data.models.FirebaseUserEntity
import com.example.data.models.InvestigatorNoteEntity
import com.example.data.models.ScannerEngineStatus
import com.example.data.models.SourceConnectorHealth
import com.example.data.models.ThreatIncidentEntity
import com.example.data.models.UserRole
import com.example.data.repository.BrandShieldRepository
import com.example.domain.engine.RiskDetectionEngine
import com.example.utils.ThreatNotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab(val route: String, val label: String) {
    DASHBOARD("dashboard", "Dashboard"),
    THREATS("threats", "Threats"),
    SCANNERS("scanners", "Scanners"),
    CAMPAIGNS("campaigns", "Campaigns"),
    BRANDS("brands", "Brands")
}

data class AuthState(
    val isSplashVisible: Boolean = true,
    val isAuthenticated: Boolean = false,
    val analystEmail: String = "hari.soc@brandshield.in",
    val analystName: String = "Hari",
    val role: UserRole = UserRole.SECURITY_ANALYST,
    val firebaseUid: String = "fb-pwd-hari-02",
    val authProvider: String = "password", // "google.com", "facebook.com", "password"
    val organization: String = "BrandShield AI SOC India",
    val firebaseSyncStatus: String = "Connected to Firebase Auth & Database (users collection)",
    val loginError: String? = null
)

data class LiveMonitoringTelemetry(
    val isConnected: Boolean = true,
    val updatedIstTime: String = "15:42:18 IST",
    val protectedBrandsCount: Int = 10,
    val activeScannersCount: Int = 6,
    val sourcesOnlineCount: Int = 14,
    val lastScanSecondsAgo: Int = 7,
    val eventsProcessedToday: Int = 12482,
    val threatsCreatedToday: Int = 37,
    val campaignsActiveCount: Int = 4,
    val latestWsEventJson: String = """{"eventType":"NEW_THREAT","threatId":"THR-20261005-1001","brand":"State Bank of India","category":"SOCIAL_IMPERSONATION","severity":"CRITICAL","riskScore":94}"""
)

data class DashboardMetrics(
    val criticalCount: Int = 8,
    val highCount: Int = 17,
    val mediumCount: Int = 31,
    val lowCount: Int = 26,
    val newThreatsToday: Int = 37,
    val campaignsCount: Int = 4,
    val weeklyTrend: List<Pair<String, Int>> = listOf(
        "29 Sep" to 25,
        "30 Sep" to 31,
        "01 Oct" to 38,
        "02 Oct" to 34,
        "03 Oct" to 40,
        "04 Oct" to 42,
        "05 Oct" to 45
    )
)

class BrandShieldViewModel(
    private val repository: BrandShieldRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentTab = MutableStateFlow(MainTab.DASHBOARD)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    private val _selectedThreatId = MutableStateFlow<String?>(null)
    val selectedThreatId: StateFlow<String?> = _selectedThreatId.asStateFlow()

    private val _selectedBrandId = MutableStateFlow<String?>(null)
    val selectedBrandId: StateFlow<String?> = _selectedBrandId.asStateFlow()

    private val _showAlertsSheet = MutableStateFlow(false)
    val showAlertsSheet: StateFlow<Boolean> = _showAlertsSheet.asStateFlow()

    private val _threatFilter = MutableStateFlow("All")
    val threatFilter: StateFlow<String> = _threatFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

    private val _isAiGenerating = MutableStateFlow(false)
    val isAiGenerating: StateFlow<Boolean> = _isAiGenerating.asStateFlow()

    private val _darkTheme = MutableStateFlow(true)
    val darkTheme: StateFlow<Boolean> = _darkTheme.asStateFlow()

    private val _liveTelemetry = MutableStateFlow(LiveMonitoringTelemetry())
    val liveTelemetry: StateFlow<LiveMonitoringTelemetry> = _liveTelemetry.asStateFlow()

    private val _scannerEngines = MutableStateFlow(
        listOf(
            ScannerEngineStatus(
                id = "social_monitor",
                name = "Social Monitor",
                status = "RUNNING",
                lastScanSecondsAgo = 4,
                metricLabel = "Events",
                metricCount = 6281,
                pollingIntervalLabel = "Social-search APIs: 5–15 min"
            ),
            ScannerEngineStatus(
                id = "app_monitor",
                name = "App Monitor (AppIntelWorker)",
                status = "RUNNING",
                lastScanSecondsAgo = 11,
                metricLabel = "Apps analyzed",
                metricCount = 1214,
                pollingIntervalLabel = "App-store monitoring: 15–60 min"
            ),
            ScannerEngineStatus(
                id = "domain_monitor",
                name = "Domain Monitor",
                status = "RUNNING",
                lastScanSecondsAgo = 6,
                metricLabel = "Domains analyzed",
                metricCount = 3719,
                pollingIntervalLabel = "Domain feeds: 5–30 min"
            ),
            ScannerEngineStatus(
                id = "logo_intelligence",
                name = "Logo Intelligence (CLIP / pHash)",
                status = "RUNNING",
                lastScanSecondsAgo = 5,
                metricLabel = "Images analyzed",
                metricCount = 8932,
                pollingIntervalLabel = "High-risk feeds: 1–5 min"
            ),
            ScannerEngineStatus(
                id = "scam_text_ai",
                name = "Scam Text AI",
                status = "RUNNING",
                lastScanSecondsAgo = 3,
                metricLabel = "Texts analyzed",
                metricCount = 14530,
                pollingIntervalLabel = "Real-time Redis Stream Worker"
            )
        )
    )
    val scannerEngines: StateFlow<List<ScannerEngineStatus>> = _scannerEngines.asStateFlow()

    private val _connectorHealthList = MutableStateFlow(
        listOf(
            SourceConnectorHealth(
                id = "conn_instagram",
                connectorName = "InstagramAdapter",
                platformCategory = "Social Graph API",
                healthState = "RATE LIMITED",
                lastSuccessfulScan = "2 minutes ago",
                pollingPolicy = "5–15 min interval",
                note = "Quota window resets in 42s; high-priority queue active"
            ),
            SourceConnectorHealth(
                id = "conn_x",
                connectorName = "XAdapter",
                platformCategory = "Public Search Stream",
                healthState = "HEALTHY",
                lastSuccessfulScan = "6 seconds ago",
                pollingPolicy = "5–15 min interval",
                note = "Monitoring 10 Indian bank handles & look-alikes"
            ),
            SourceConnectorHealth(
                id = "conn_telegram",
                connectorName = "TelegramAdapter",
                platformCategory = "Public Channel Feed",
                healthState = "HEALTHY",
                lastSuccessfulScan = "9 seconds ago",
                pollingPolicy = "1–5 min high-risk feed",
                note = "Tracking APK distribution & fake support desks"
            ),
            SourceConnectorHealth(
                id = "conn_app_intel",
                connectorName = "AppIntelWorker",
                platformCategory = "Android APK & Store Metadata",
                healthState = "HEALTHY",
                lastSuccessfulScan = "11 seconds ago",
                pollingPolicy = "15–60 min interval",
                note = "Comparing certificates & permissions against official_assets"
            ),
            SourceConnectorHealth(
                id = "conn_dns_cert",
                connectorName = "CertStream & DNS Worker",
                platformCategory = "Domain / SSL Transparency",
                healthState = "HEALTHY",
                lastSuccessfulScan = "5 seconds ago",
                pollingPolicy = "5–30 min interval",
                note = "Filtering .test / .example look-alike bank domains"
            ),
            SourceConnectorHealth(
                id = "conn_youtube",
                connectorName = "YouTubeAdapter",
                platformCategory = "Public Video Metadata",
                healthState = "DELAYED",
                lastSuccessfulScan = "4 minutes ago",
                pollingPolicy = "15–30 min interval",
                note = "High comment volume on financial reward keywords"
            ),
            SourceConnectorHealth(
                id = "conn_linkedin",
                connectorName = "LinkedInAdapter",
                platformCategory = "Executive & Brand Pages",
                healthState = "HEALTHY",
                lastSuccessfulScan = "1 minute ago",
                pollingPolicy = "30–60 min enrichment",
                note = "Authorized brand verification feed"
            )
        )
    )
    val connectorHealthList: StateFlow<List<SourceConnectorHealth>> = _connectorHealthList.asStateFlow()

    private val _latestScanCreatedThreat = MutableStateFlow<ThreatIncidentEntity?>(null)
    val latestScanCreatedThreat: StateFlow<ThreatIncidentEntity?> = _latestScanCreatedThreat.asStateFlow()

    val brands: StateFlow<List<BrandEntity>> = repository.allBrands
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allThreats: StateFlow<List<ThreatIncidentEntity>> = repository.allThreats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val alerts: StateFlow<List<AlertEntity>> = repository.allAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val firebaseUsers: StateFlow<List<FirebaseUserEntity>> = repository.allFirebaseUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedBrand: StateFlow<BrandEntity?> = combine(brands, _selectedBrandId) { list, id ->
        if (id == null) null else list.find { it.brandId == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val filteredThreats: StateFlow<List<ThreatIncidentEntity>> = combine(
        allThreats,
        _threatFilter,
        _searchQuery
    ) { threats, filter, query ->
        threats.filter { threat ->
            val matchesFilter = when (filter) {
                "All" -> true
                "Social" -> threat.category == "SOCIAL"
                "Apps" -> threat.category == "APP"
                "Domains" -> threat.category == "DOMAIN"
                "Critical" -> threat.severity == "CRITICAL"
                "High" -> threat.severity == "HIGH"
                "Medium" -> threat.severity == "MEDIUM"
                "Low" -> threat.severity == "LOW"
                else -> true
            }
            val matchesSearch = query.isBlank() ||
                threat.targetTitle.contains(query, ignoreCase = true) ||
                threat.targetIdentifier.contains(query, ignoreCase = true) ||
                threat.brandName.contains(query, ignoreCase = true) ||
                threat.brandId.contains(query, ignoreCase = true) ||
                threat.platform.contains(query, ignoreCase = true) ||
                threat.typeLabel.contains(query, ignoreCase = true)
            matchesFilter && matchesSearch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedThreat: StateFlow<ThreatIncidentEntity?> = _selectedThreatId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else repository.observeThreat(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedThreatNotes: StateFlow<List<InvestigatorNoteEntity>> = _selectedThreatId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.observeNotes(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val coordinatedCampaigns: StateFlow<List<CoordinatedCampaign>> = allThreats
        .combine(_authState) { list, _ ->
            repository.correlateCampaigns(list)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dashboardMetrics: StateFlow<DashboardMetrics> = combine(
        allThreats,
        _liveTelemetry
    ) { list, live ->
        val active = list.filter { it.status != "FALSE POSITIVE" }
        val extraCritical = (active.count { it.severity == "CRITICAL" } - 5).coerceAtLeast(0)
        val extraHigh = (active.count { it.severity == "HIGH" } - 4).coerceAtLeast(0)
        val extraMedium = (active.count { it.severity == "MEDIUM" }).coerceAtLeast(0)
        val extraLow = (active.count { it.severity == "LOW" }).coerceAtLeast(0)

        DashboardMetrics(
            criticalCount = 8 + extraCritical,
            highCount = 17 + extraHigh,
            mediumCount = 31 + extraMedium,
            lowCount = 26 + extraLow,
            newThreatsToday = live.threatsCreatedToday,
            campaignsCount = 4 + (if (extraCritical > 0) 1 else 0),
            weeklyTrend = listOf(
                "29 Sep" to 25,
                "30 Sep" to 31,
                "01 Oct" to 38,
                "02 Oct" to 34,
                "03 Oct" to 40,
                "04 Oct" to 42,
                "05 Oct" to (45 + extraCritical + extraHigh)
            )
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
            delay(1300)
            _authState.value = _authState.value.copy(isSplashVisible = false)
        }

        // Real-time WebSocket / SSE Telemetry Ticker (Section 9 & 13)
        viewModelScope.launch {
            var tick = 0
            while (true) {
                delay(3000)
                tick++
                val current = _liveTelemetry.value
                val nextSec = ((current.lastScanSecondsAgo + 2) % 9).coerceAtLeast(1)
                val secPart = (18 + (tick * 3)) % 60
                val minPart = (42 + ((18 + tick * 3) / 60)) % 60
                _liveTelemetry.value = current.copy(
                    lastScanSecondsAgo = nextSec,
                    eventsProcessedToday = current.eventsProcessedToday + (2..6).random(),
                    updatedIstTime = String.format("15:%02d:%02d IST", minPart, secPart)
                )

                // Increment live scanner counters slightly
                _scannerEngines.value = _scannerEngines.value.mapIndexed { idx, engine ->
                    engine.copy(
                        lastScanSecondsAgo = ((engine.lastScanSecondsAgo + idx + 1) % 12).coerceAtLeast(2),
                        metricCount = engine.metricCount + (1..3).random()
                    )
                }
            }
        }
    }

    fun dismissSplashNow() {
        _authState.value = _authState.value.copy(isSplashVisible = false)
    }

    fun login(email: String, password: String, role: UserRole) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = _authState.value.copy(loginError = "Please enter your analyst email and security key.")
            return
        }
        val derivedName = email.substringBefore("@")
            .split(".", "_")
            .firstOrNull()
            ?.replaceFirstChar { it.uppercase() }
            ?.ifBlank { "Hari" } ?: "Hari"

        _authState.value = _authState.value.copy(
            isAuthenticated = true,
            analystEmail = email.trim(),
            analystName = if (derivedName.equals("hari", ignoreCase = true)) "Hari" else derivedName,
            role = role,
            authProvider = "password",
            loginError = null
        )
        showStatusBanner("Authenticated as ${role.title} ($derivedName)")
    }

    fun authenticateWithFirebaseEmail(
        context: Context,
        email: String,
        password: String,
        confirmPassword: String,
        displayName: String,
        organization: String,
        role: UserRole,
        isSignUpMode: Boolean
    ) {
        if (email.isBlank() || !email.contains("@")) {
            _authState.value = _authState.value.copy(
                loginError = "Please enter a valid email address for Firebase authentication."
            )
            return
        }
        if (password.length < 6) {
            _authState.value = _authState.value.copy(
                loginError = "Firebase Auth requires a password of at least 6 characters."
            )
            return
        }
        if (isSignUpMode && password != confirmPassword) {
            _authState.value = _authState.value.copy(
                loginError = "Passwords do not match. Please confirm your security password."
            )
            return
        }

        viewModelScope.launch {
            val existing = repository.getFirebaseUserByEmail(email.trim())
            val authResult = FirebaseAuthDatabaseService.authenticateEmailPassword(
                context = context,
                email = email,
                password = password,
                displayNameInput = displayName,
                role = role,
                organization = organization,
                isSignUpMode = isSignUpMode,
                existingLocalUser = existing
            )

            if (!authResult.success || authResult.user == null) {
                _authState.value = _authState.value.copy(
                    loginError = authResult.errorMessage ?: "Authentication failed."
                )
                return@launch
            }

            val user = authResult.user
            repository.upsertFirebaseUser(user)
            _authState.value = _authState.value.copy(
                isAuthenticated = true,
                analystEmail = user.email,
                analystName = user.displayName,
                role = role,
                firebaseUid = user.uid,
                authProvider = user.authProvider,
                organization = user.organization,
                firebaseSyncStatus = authResult.cloudModeLabel,
                loginError = null
            )
            val actionVerb = if (isSignUpMode) "Registered & Synced" else "Signed In"
            showStatusBanner("✓ $actionVerb via Firebase DB: ${user.displayName} (${user.email})")
        }
    }

    fun authenticateWithGoogleFirebase(
        context: Context,
        googleEmail: String,
        googleDisplayName: String,
        role: UserRole,
        organization: String,
        isSignUpMode: Boolean
    ) {
        viewModelScope.launch {
            val existing = repository.getFirebaseUserByEmail(googleEmail.trim())
            val authResult = FirebaseAuthDatabaseService.authenticateWithGoogle(
                context = context,
                googleEmail = googleEmail,
                googleDisplayName = googleDisplayName,
                role = role,
                organization = organization,
                existingLocalUser = existing
            )

            val user = authResult.user ?: return@launch
            repository.upsertFirebaseUser(user)
            _authState.value = _authState.value.copy(
                isAuthenticated = true,
                analystEmail = user.email,
                analystName = user.displayName,
                role = role,
                firebaseUid = user.uid,
                authProvider = "google.com",
                organization = user.organization,
                firebaseSyncStatus = authResult.cloudModeLabel,
                loginError = null
            )
            val actionText = if (isSignUpMode || authResult.isNewUser) "Google Sign-Up Synced to Firebase DB" else "Google Login Connected to Firebase DB"
            showStatusBanner("✓ $actionText • ${user.displayName} (${user.email})")
        }
    }

    fun authenticateWithFacebookFirebase(
        context: Context,
        facebookEmail: String,
        facebookDisplayName: String,
        role: UserRole,
        organization: String,
        isSignUpMode: Boolean
    ) {
        viewModelScope.launch {
            val existing = repository.getFirebaseUserByEmail(facebookEmail.trim())
            val authResult = FirebaseAuthDatabaseService.authenticateWithFacebook(
                context = context,
                facebookEmail = facebookEmail,
                facebookDisplayName = facebookDisplayName,
                role = role,
                organization = organization,
                existingLocalUser = existing
            )

            val user = authResult.user ?: return@launch
            repository.upsertFirebaseUser(user)
            _authState.value = _authState.value.copy(
                isAuthenticated = true,
                analystEmail = user.email,
                analystName = user.displayName,
                role = role,
                firebaseUid = user.uid,
                authProvider = "facebook.com",
                organization = user.organization,
                firebaseSyncStatus = authResult.cloudModeLabel,
                loginError = null
            )
            val actionText = if (isSignUpMode || authResult.isNewUser) "Facebook Sign-Up Synced to Firebase DB" else "Facebook Login Connected to Firebase DB"
            showStatusBanner("✓ $actionText • ${user.displayName} (${user.email})")
        }
    }

    fun quickSsoOrBiometricLogin(methodLabel: String, role: UserRole) {
        _authState.value = _authState.value.copy(
            isAuthenticated = true,
            analystEmail = "hari.soc@brandshield.in",
            analystName = "Hari",
            role = role,
            loginError = null
        )
        showStatusBanner("Verified via $methodLabel • Role: ${role.title}")
    }

    fun switchRole(newRole: UserRole) {
        _authState.value = _authState.value.copy(role = newRole)
        showStatusBanner("Switched SOC Role to ${newRole.title}")
    }

    fun logout() {
        _selectedThreatId.value = null
        _selectedBrandId.value = null
        _authState.value = _authState.value.copy(isAuthenticated = false)
    }

    fun toggleTheme() {
        _darkTheme.value = !_darkTheme.value
    }

    fun selectTab(tab: MainTab) {
        _selectedThreatId.value = null
        _selectedBrandId.value = null
        _currentTab.value = tab
    }

    fun setThreatFilter(filter: String) {
        _threatFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openThreatDetail(threatId: String) {
        _selectedThreatId.value = threatId
    }

    fun closeThreatDetail() {
        _selectedThreatId.value = null
    }

    fun openBrandDetail(brandId: String) {
        _selectedBrandId.value = brandId
    }

    fun closeBrandDetail() {
        _selectedBrandId.value = null
    }

    fun setShowAlertsSheet(show: Boolean) {
        _showAlertsSheet.value = show
    }

    fun clearBanner() {
        _statusBannerMessage.value = null
    }

    private fun showStatusBanner(msg: String) {
        _statusBannerMessage.value = msg
    }

    fun clearLatestScanPreview() {
        _latestScanCreatedThreat.value = null
    }

    fun performAnalystAction(
        threat: ThreatIncidentEntity,
        actionLabel: String,
        customNote: String? = null
    ) {
        val state = _authState.value
        if (state.role == UserRole.VIEWER) {
            showStatusBanner("Viewer role is read-only. Switch to Security Analyst or Administrator.")
            return
        }

        val (newStatus, defaultNote) = when (actionLabel) {
            "Confirm Threat" -> "CONFIRMED" to "Confirmed high-probability brand impersonation after verifying asset is absent from official_assets registry."
            "Mark Safe" -> "FALSE POSITIVE" to "Marked as safe / authorized asset after allowlist verification."
            "Investigate" -> "UNDER REVIEW" to "Initiated deep forensic investigation on asset metadata, DNS, and similarity vectors."
            "Escalate" -> "CONFIRMED" to "Escalated incident to Tier-3 Threat Response & CERT-In Takedown Desk."
            "Report" -> "REPORTED" to "Submitted formal abuse & takedown report with cryptographic & visual evidence bundle."
            "Resolve" -> "RESOLVED" to "Incident resolved. Fraudulent asset taken down or sinkholed."
            else -> threat.status to (customNote ?: "Updated incident status.")
        }

        viewModelScope.launch {
            repository.updateThreatStatus(
                threatId = threat.threatId,
                newStatus = newStatus,
                analystName = state.analystName,
                analystRole = state.role.title,
                noteText = customNote?.takeIf { it.isNotBlank() } ?: defaultNote
            )
            showStatusBanner("${threat.threatId} updated to $newStatus")
        }
    }

    fun assignAnalystToThreat(threat: ThreatIncidentEntity, assigneeName: String) {
        val state = _authState.value
        viewModelScope.launch {
            repository.assignThreatAnalyst(
                threatId = threat.threatId,
                assignedAnalyst = assigneeName,
                currentStatus = threat.status,
                authorName = state.analystName,
                authorRole = state.role.title
            )
            showStatusBanner("Assigned ${threat.threatId} to $assigneeName")
        }
    }

    fun addNoteToThreat(threatId: String, noteText: String) {
        if (noteText.isBlank()) return
        val state = _authState.value
        viewModelScope.launch {
            repository.addInvestigatorNote(
                threatId = threatId,
                analystName = state.analystName,
                analystRole = state.role.title,
                noteText = noteText.trim()
            )
            showStatusBanner("Forensic note saved to $threatId")
        }
    }

    fun runGeminiDeepForensicAnalysis(threat: ThreatIncidentEntity) {
        if (_isAiGenerating.value) return
        _isAiGenerating.value = true
        viewModelScope.launch {
            val brand = repository.getBrandById(threat.brandId)
            val brief = GeminiAiAnalyzer.generateForensicBrief(threat, brand)
            repository.updateThreatAiExplanation(
                threatId = threat.threatId,
                explanation = brief,
                checklist = threat.aiChecklist
            )
            repository.addInvestigatorNote(
                threatId = threat.threatId,
                analystName = "BrandShield AI (Gemini 3.5 Flash)",
                analystRole = "AI Forensic Engine",
                noteText = brief,
                actionTaken = "AI Deep Assessment"
            )
            _isAiGenerating.value = false
            showStatusBanner("AI Assessment updated for ${threat.threatId}")
        }
    }

    fun runLiveSocialScan(
        context: Context,
        brand: BrandEntity,
        platform: String,
        username: String,
        displayName: String,
        bio: String,
        externalUrl: String,
        logoSimilarity: Int,
        accountAgeDays: Int,
        isVerified: Boolean
    ) {
        viewModelScope.launch {
            val result = RiskDetectionEngine.evaluateSocialProfile(
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

            val threatId = "THR-20261005-${(1040..9999).random()}"
            val newThreat = ThreatIncidentEntity(
                threatId = threatId,
                brandId = brand.brandId,
                brandName = brand.name,
                category = "SOCIAL",
                typeLabel = "Potential Social Impersonation",
                platform = platform,
                targetTitle = displayName.ifBlank { username },
                targetIdentifier = if (username.startsWith("@")) username else "@$username",
                riskScore = result.totalRiskScore,
                severity = result.severity,
                status = "DETECTED",
                assignedTo = _authState.value.analystName,
                detectedAt = System.currentTimeMillis(),
                detectedAgoLabel = "Just now",
                usernameSimilarity = result.usernameSimilarity,
                logoSimilarity = result.logoSimilarity,
                bioSimilarity = result.bioSimilarity,
                developerTrustScore = if (isVerified) 92 else 18,
                suspiciousUrlDetected = result.suspiciousUrlDetected,
                suspiciousUrlValue = externalUrl.trim(),
                scamScore = result.scamScore,
                scamClassification = result.scamClassification,
                metadataRiskScore = result.metadataRiskScore,
                accountAgeDays = accountAgeDays,
                isVerified = isVerified,
                followerCount = (45..450).random(),
                aiChecklistCsv = result.checklist.joinToString("|"),
                aiExplanation = result.explanation,
                campaignId = if (brand.brandId == "SBI" && result.totalRiskScore >= 81) "CAMPAIGN-1023" else ""
            )

            val isCritical = repository.insertThreatAndAlertIfCritical(newThreat)
            _latestScanCreatedThreat.value = newThreat
            incrementLiveEventCounters(newThreat)

            if (isCritical) {
                ThreatNotificationHelper.sendCriticalThreatNotification(
                    context = context,
                    threatId = newThreat.threatId,
                    brandName = newThreat.brandName,
                    platform = newThreat.platform,
                    targetIdentifier = newThreat.targetIdentifier,
                    riskScore = newThreat.riskScore,
                    summary = result.explanation
                )
                showStatusBanner("⚠ CRITICAL Threat (${result.totalRiskScore}/100) pushed via WebSocket stream!")
            } else {
                showStatusBanner("Social Scan Complete: Risk Score ${result.totalRiskScore}/100 (${result.severity})")
            }
        }
    }

    fun runLiveDomainScan(
        context: Context,
        brand: BrandEntity,
        domainInput: String,
        registrationAgeDays: Int,
        sslMatchesOrg: Boolean
    ) {
        viewModelScope.launch {
            val result = RiskDetectionEngine.evaluateDomain(
                brand = brand,
                rawDomain = domainInput,
                registrationAgeDays = registrationAgeDays,
                sslMatchesBrandOrg = sslMatchesOrg
            )

            val threatId = "THR-20261005-${(1040..9999).random()}"
            val newThreat = ThreatIncidentEntity(
                threatId = threatId,
                brandId = brand.brandId,
                brandName = brand.name,
                category = "DOMAIN",
                typeLabel = "Look-alike Domain",
                platform = "Domain",
                targetTitle = result.domain,
                targetIdentifier = "https://${result.domain}",
                riskScore = result.riskScore,
                severity = result.severity,
                status = "DETECTED",
                assignedTo = _authState.value.analystName,
                detectedAt = System.currentTimeMillis(),
                detectedAgoLabel = "Just now",
                usernameSimilarity = result.domainSimilarity,
                logoSimilarity = if (result.riskScore >= 80) 92 else 55,
                bioSimilarity = result.domainSimilarity,
                developerTrustScore = if (sslMatchesOrg) 95 else 20,
                suspiciousUrlDetected = result.riskScore >= 50,
                suspiciousUrlValue = result.domain,
                scamScore = if (result.matchedKeywords.isNotEmpty()) 87 else 40,
                scamClassification = if (result.riskScore >= 75) "Phishing" else "Suspicious",
                metadataRiskScore = if (registrationAgeDays <= 14) 90 else 45,
                accountAgeDays = registrationAgeDays,
                isVerified = sslMatchesOrg,
                domainRegistrar = "Controlled Sandbox Registrar (.test)",
                sslValid = sslMatchesOrg,
                redirectChain = "${result.domain} -> /verify-kyc-session",
                attackVector = result.attackVector,
                aiChecklistCsv = result.checklist.joinToString("|"),
                aiExplanation = result.explanation,
                campaignId = if (brand.brandId == "SBI" && result.riskScore >= 80) "CAMPAIGN-1023" else ""
            )

            val isCritical = repository.insertThreatAndAlertIfCritical(newThreat)
            _latestScanCreatedThreat.value = newThreat
            incrementLiveEventCounters(newThreat)

            if (isCritical) {
                ThreatNotificationHelper.sendCriticalThreatNotification(
                    context = context,
                    threatId = newThreat.threatId,
                    brandName = newThreat.brandName,
                    platform = "Domain Monitor",
                    targetIdentifier = newThreat.targetTitle,
                    riskScore = newThreat.riskScore,
                    summary = result.explanation
                )
                showStatusBanner("⚠ CRITICAL Domain Threat (${result.riskScore}/100) logged & alerted!")
            } else {
                showStatusBanner("Domain Scan Complete: Risk ${result.riskScore}/100 (${result.severity})")
            }
        }
    }

    fun runLiveAppScan(
        context: Context,
        brand: BrandEntity,
        appName: String,
        packageName: String,
        developerName: String,
        signingCert: String,
        permissions: List<String>,
        iconSimilarity: Int
    ) {
        viewModelScope.launch {
            val result = RiskDetectionEngine.evaluateMobileApp(
                brand = brand,
                appName = appName,
                packageName = packageName,
                developerName = developerName,
                signingCert = signingCert,
                permissions = permissions,
                iconSimilarity = iconSimilarity
            )

            val threatId = "THR-20261005-${(1040..9999).random()}"
            val newThreat = ThreatIncidentEntity(
                threatId = threatId,
                brandId = brand.brandId,
                brandName = brand.name,
                category = "APP",
                typeLabel = "Suspicious Android App",
                platform = "Android APK • Third-Party Source",
                targetTitle = appName,
                targetIdentifier = packageName,
                riskScore = result.riskScore,
                severity = result.severity,
                status = "DETECTED",
                assignedTo = _authState.value.analystName,
                detectedAt = System.currentTimeMillis(),
                detectedAgoLabel = "Just now",
                usernameSimilarity = result.appNameSimilarity,
                logoSimilarity = result.iconSimilarity,
                bioSimilarity = result.packageSimilarity,
                developerTrustScore = result.developerTrustScore,
                suspiciousUrlDetected = !result.certificateMatch,
                suspiciousUrlValue = packageName,
                scamScore = result.permissionRiskScore,
                scamClassification = if (result.riskScore >= 80) "Phishing" else "Suspicious",
                metadataRiskScore = if (result.certificateMatch) 10 else 95,
                accountAgeDays = 3,
                isVerified = result.certificateMatch,
                appDeveloper = developerName,
                certificateSubject = signingCert,
                certificateMatch = result.certificateMatch,
                sensitivePermissionsCsv = permissions.joinToString(", "),
                apkSha256 = result.apkSha256,
                appDownloads = "650+ observed",
                aiChecklistCsv = result.checklist.joinToString("|"),
                aiExplanation = result.explanation,
                campaignId = if (brand.brandId == "SBI" && result.riskScore >= 80) "CAMPAIGN-1023" else ""
            )

            val isCritical = repository.insertThreatAndAlertIfCritical(newThreat)
            _latestScanCreatedThreat.value = newThreat
            incrementLiveEventCounters(newThreat)

            if (isCritical) {
                ThreatNotificationHelper.sendCriticalThreatNotification(
                    context = context,
                    threatId = newThreat.threatId,
                    brandName = newThreat.brandName,
                    platform = "AppIntelWorker",
                    targetIdentifier = "$appName ($packageName)",
                    riskScore = newThreat.riskScore,
                    summary = result.explanation
                )
                showStatusBanner("⚠ CRITICAL APK Impersonation (${result.riskScore}/100) flagged & alerted!")
            } else {
                showStatusBanner("APK Scan Complete: Risk ${result.riskScore}/100 (${result.severity})")
            }
        }
    }

    /**
     * Simulates an incoming real-time WebSocket event (`WS /api/ws/threats`, Section 9 & 10)
     * rotating across the 10 protected Indian banks so the analyst sees instant card insertion at the top.
     */
    private var liveBankRotationIdx = 0

    fun simulateIncomingWebSocketThreatEvent(context: Context) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val templates = listOf(
                Triple(
                    "HDFC",
                    "HDFC Bank",
                    ThreatIncidentEntity(
                        threatId = "THR-20261005-${(1042..1999).random()}",
                        brandId = "HDFC",
                        brandName = "HDFC Bank",
                        category = "APP",
                        typeLabel = "Suspicious Android Application",
                        platform = "Android APK • Third-Party Source",
                        targetTitle = "HDFC PayZapp KYC Update",
                        targetIdentifier = "com.demo.hdfc.payzappkyc",
                        riskScore = 91,
                        severity = "CRITICAL",
                        status = "DETECTED",
                        assignedTo = _authState.value.analystName,
                        detectedAt = now,
                        detectedAgoLabel = "Just now",
                        usernameSimilarity = 94,
                        logoSimilarity = 90,
                        bioSimilarity = 86,
                        developerTrustScore = 22,
                        suspiciousUrlDetected = true,
                        suspiciousUrlValue = "hdfc-kyc-portal.test",
                        scamScore = 88,
                        scamClassification = "Phishing",
                        metadataRiskScore = 90,
                        accountAgeDays = 1,
                        isVerified = false,
                        appDeveloper = "Unverified APK Mirror",
                        certificateSubject = "UnrecognizedDemoCert",
                        certificateMatch = false,
                        sensitivePermissionsCsv = "READ_SMS, RECEIVE_SMS, BIND_ACCESSIBILITY_SERVICE",
                        apkSha256 = "7BD91E4A8C02F39B1D558EA70C92310F8B41E72A99D20C4F6E11A0389B7BD91E",
                        appDownloads = "310+ observed",
                        aiChecklistCsv = "Brand similarity 94%|Icon similarity 90%|Unrecognized certificate|Sensitive SMS permission",
                        aiExplanation = "This asset has a high probability of brand impersonation because its name and visual identity closely resemble HDFC Bank while its package (com.demo.hdfc.payzappkyc), developer identity and certificate are not present in the official asset registry.",
                        campaignId = "CAMPAIGN-1024"
                    )
                ),
                Triple(
                    "ICICI",
                    "ICICI Bank",
                    ThreatIncidentEntity(
                        threatId = "THR-20261005-${(2000..2999).random()}",
                        brandId = "ICICI",
                        brandName = "ICICI Bank",
                        category = "DOMAIN",
                        typeLabel = "Look-alike Domain",
                        platform = "Domain",
                        targetTitle = "icici-imobile-verify.test",
                        targetIdentifier = "https://icici-imobile-verify.test",
                        riskScore = 87,
                        severity = "CRITICAL",
                        status = "DETECTED",
                        assignedTo = _authState.value.analystName,
                        detectedAt = now,
                        detectedAgoLabel = "Just now",
                        usernameSimilarity = 93,
                        logoSimilarity = 91,
                        bioSimilarity = 89,
                        developerTrustScore = 20,
                        suspiciousUrlDetected = true,
                        suspiciousUrlValue = "icici-imobile-verify.test",
                        scamScore = 85,
                        scamClassification = "Phishing",
                        metadataRiskScore = 88,
                        accountAgeDays = 1,
                        isVerified = false,
                        domainRegistrar = "Controlled Sandbox (.test)",
                        sslValid = false,
                        redirectChain = "icici-imobile-verify.test -> /otp-check",
                        attackVector = "Look-alike Domain (iMobile Pay)",
                        aiChecklistCsv = "Domain similarity 93% to icicibank.com|\"iMobile\" & \"verify\" keywords|Recently observed|Not in official-domain allowlist",
                        aiExplanation = "This asset has a high probability of brand impersonation because its domain name closely resembles ICICI Bank's iMobile Pay service while not being present in the official asset registry.",
                        campaignId = ""
                    )
                ),
                Triple(
                    "SBI",
                    "State Bank of India",
                    ThreatIncidentEntity(
                        threatId = "THR-20261005-${(3000..3999).random()}",
                        brandId = "SBI",
                        brandName = "State Bank of India",
                        category = "SOCIAL",
                        typeLabel = "Potential Social Impersonation",
                        platform = "Instagram",
                        targetTitle = "YONO SBI Care Helpdesk",
                        targetIdentifier = "@yono_sbi_care_test02",
                        riskScore = 94,
                        severity = "CRITICAL",
                        status = "DETECTED",
                        assignedTo = _authState.value.analystName,
                        detectedAt = now,
                        detectedAgoLabel = "Just now",
                        usernameSimilarity = 96,
                        logoSimilarity = 93,
                        bioSimilarity = 89,
                        developerTrustScore = 18,
                        suspiciousUrlDetected = true,
                        suspiciousUrlValue = "sbi-yono-kyc-update.test",
                        scamScore = 87,
                        scamClassification = "Phishing",
                        metadataRiskScore = 90,
                        accountAgeDays = 1,
                        isVerified = false,
                        followerCount = 85,
                        aiChecklistCsv = "Name similarity 96%|Logo similarity 93%|Suspicious URL (sbi-yono-kyc-update.test)|KYC language detected",
                        aiExplanation = "This asset has a high probability of brand impersonation because its name and visual identity closely resemble State Bank of India (YONO SBI) while its handle and linked domain are not present in the official asset registry.",
                        campaignId = "CAMPAIGN-1023"
                    )
                ),
                Triple(
                    "CANARA",
                    "Canara Bank",
                    ThreatIncidentEntity(
                        threatId = "THR-20261005-${(4000..4999).random()}",
                        brandId = "CANARA",
                        brandName = "Canara Bank",
                        category = "APP",
                        typeLabel = "Suspicious Android Application",
                        platform = "Android APK • Third-Party Source",
                        targetTitle = "Canara ai1 KYC Helper",
                        targetIdentifier = "com.demo.canara.ai1kyc",
                        riskScore = 85,
                        severity = "CRITICAL",
                        status = "DETECTED",
                        assignedTo = _authState.value.analystName,
                        detectedAt = now,
                        detectedAgoLabel = "Just now",
                        usernameSimilarity = 94,
                        logoSimilarity = 92,
                        bioSimilarity = 84,
                        developerTrustScore = 21,
                        suspiciousUrlDetected = true,
                        suspiciousUrlValue = "canara-ai1-verify.test",
                        scamScore = 83,
                        scamClassification = "Phishing",
                        metadataRiskScore = 88,
                        accountAgeDays = 2,
                        isVerified = false,
                        appDeveloper = "Untrusted APK Build",
                        certificateSubject = "UnverifiedDemoCert_Canara",
                        certificateMatch = false,
                        sensitivePermissionsCsv = "READ_SMS, RECEIVE_SMS",
                        apkSha256 = "5C12A890E43B77D1209F6A31B44C82D11E90F34A56B78C90D12E34F56A78B901",
                        appDownloads = "190+ observed",
                        aiChecklistCsv = "Brand similarity 94% to Canara ai1|Icon similarity 92%|Unrecognized certificate|Sensitive SMS permission",
                        aiExplanation = "This asset has a high probability of brand impersonation because its name and visual identity closely resemble Canara Bank's official Canara ai1 super-app while its certificate and package are absent from the official asset registry.",
                        campaignId = ""
                    )
                )
            )

            val chosen = templates[liveBankRotationIdx % templates.size].third
            liveBankRotationIdx++

            repository.insertThreatAndAlertIfCritical(chosen)
            incrementLiveEventCounters(chosen)

            ThreatNotificationHelper.sendCriticalThreatNotification(
                context = context,
                threatId = chosen.threatId,
                brandName = chosen.brandName,
                platform = chosen.platform,
                targetIdentifier = chosen.targetIdentifier,
                riskScore = chosen.riskScore,
                summary = "Potential ${chosen.brandName} impersonation detected via live WebSocket stream."
            )
            showStatusBanner("⚡ WS Event NEW_THREAT: ${chosen.brandName} (${chosen.targetTitle}) • Risk ${chosen.riskScore}!")
        }
    }

    private fun incrementLiveEventCounters(newThreat: ThreatIncidentEntity) {
        val current = _liveTelemetry.value
        val wsPayload = """{"eventType":"NEW_THREAT","threatId":"${newThreat.threatId}","brand":"${newThreat.brandName}","category":"${newThreat.category}_IMPERSONATION","severity":"${newThreat.severity}","riskScore":${newThreat.riskScore},"source":"${newThreat.platform}","detectedAt":"2026-10-05T15:42:18+05:30"}"""
        _liveTelemetry.value = current.copy(
            lastScanSecondsAgo = 1,
            eventsProcessedToday = current.eventsProcessedToday + 14,
            threatsCreatedToday = current.threatsCreatedToday + 1,
            latestWsEventJson = wsPayload
        )
    }

    fun deleteBrand(brandId: String, brandName: String) {
        viewModelScope.launch {
            repository.deleteBrand(brandId)
            if (_selectedBrandId.value == brandId) {
                _selectedBrandId.value = null
            }
            val remaining = (brands.value.size - 1).coerceAtLeast(0)
            _liveTelemetry.value = _liveTelemetry.value.copy(protectedBrandsCount = remaining)
            showStatusBanner("Removed '$brandName' ($brandId) from Protected Brands")
        }
    }

    fun registerOrUpdateBrand(
        brandId: String?,
        name: String,
        shortName: String,
        officialAppName: String,
        aliasesCsv: String,
        domainsCsv: String,
        socialHandlesCsv: String,
        packagesCsv: String,
        developer: String,
        certificate: String,
        officialBio: String,
        keywordsCsv: String
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val id = brandId ?: shortName.uppercase().replace(" ", "_").ifBlank { "BR0${brands.value.size + 1}" }
            val entity = BrandEntity(
                brandId = id,
                name = name.trim(),
                shortName = shortName.trim().ifBlank { name.trim() },
                officialAppName = officialAppName.trim().ifBlank { "${shortName.trim()} Mobile" },
                aliasesCsv = aliasesCsv.trim(),
                officialDomainsCsv = domainsCsv.trim(),
                socialHandlesCsv = socialHandlesCsv.trim(),
                androidPackagesCsv = packagesCsv.trim(),
                officialDeveloper = developer.trim().ifBlank { "${name.trim()} Official" },
                officialCertificate = certificate.trim().ifBlank { "${shortName.replace(" ", "")}_Official_Cert" },
                officialBio = officialBio.trim().ifBlank { "Official digital banking channel for ${name.trim()}." },
                keywordsCsv = keywordsCsv.trim()
            )
            repository.insertBrand(entity)
            showStatusBanner("Brand '${name.trim()}' saved to official_assets allowlist")
        }
    }

    fun markAllAlertsRead() {
        viewModelScope.launch {
            repository.markAllAlertsRead()
        }
    }

    fun triggerTestPushAlert(context: Context) {
        val topThreat = allThreats.value.firstOrNull { it.severity == "CRITICAL" } ?: allThreats.value.firstOrNull()
        if (topThreat != null) {
            val sent = ThreatNotificationHelper.sendCriticalThreatNotification(
                context = context,
                threatId = topThreat.threatId,
                brandName = topThreat.brandName,
                platform = topThreat.platform,
                targetIdentifier = topThreat.targetIdentifier,
                riskScore = topThreat.riskScore,
                summary = "Potential ${topThreat.brandName} impersonation detected. Tap to investigate."
            )
            if (sent) {
                showStatusBanner("Push Notification dispatched for ${topThreat.brandName} (${topThreat.targetIdentifier})")
            } else {
                showStatusBanner("Alert logged in SOC Bell Drawer (Grant Notification permission for OS banner)")
            }
        }
    }

    companion object {
        fun provideFactory(repository: BrandShieldRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return BrandShieldViewModel(repository) as T
                }
            }
    }
}
