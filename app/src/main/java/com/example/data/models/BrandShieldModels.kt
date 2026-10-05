package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "brands")
data class BrandEntity(
    @PrimaryKey val brandId: String, // e.g., "SBI", "HDFC", "ICICI", "AXIS", "KOTAK", "PNB", "CANARA", "BOB", "UNION", "IDFC_FIRST"
    val name: String, // e.g. "State Bank of India"
    val shortName: String = "", // e.g. "SBI"
    val officialAppName: String = "", // e.g. "YONO SBI", "open by Axis Bank", "iMobile Pay"
    val aliasesCsv: String,
    val officialDomainsCsv: String,
    val socialHandlesCsv: String,
    val androidPackagesCsv: String,
    val officialDeveloper: String,
    val officialCertificate: String,
    val officialBio: String,
    val keywordsCsv: String,
    val officialDomainsCount: Int = 2,
    val officialAppsCount: Int = 3,
    val officialSocialProfilesCount: Int = 6,
    val officialLogosCount: Int = 4,
    val activeThreatsCount: Int = 5,
    val criticalThreatsCount: Int = 1,
    val highThreatsCount: Int = 2,
    val mediumThreatsCount: Int = 1,
    val lowThreatsCount: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
) {
    val aliases: List<String> get() = aliasesCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    val officialDomains: List<String> get() = officialDomainsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    val socialHandles: List<String> get() = socialHandlesCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    val androidPackages: List<String> get() = androidPackagesCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    val keywords: List<String> get() = keywordsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}

@Entity(tableName = "threats")
data class ThreatIncidentEntity(
    @PrimaryKey val threatId: String,
    val brandId: String,
    val brandName: String,
    val category: String, // "SOCIAL", "APP", "DOMAIN", "SCAM_POST"
    val typeLabel: String, // e.g. "Potential Social Impersonation", "Android APK • Third-Party Source", "Look-alike Domain"
    val platform: String, // "Instagram", "X (Twitter)", "Telegram", "Android APK", "Domain", "YouTube"
    val targetTitle: String, // e.g. "SBI Customer Assistance", "HDFC Mobile Secure", "icici-kyc-verify.test"
    val targetIdentifier: String, // e.g. "@sbi_helpdesk_test01", "com.demo.hdfc.securebank", "https://icici-kyc-verify.test"
    val riskScore: Int, // 0..100
    val severity: String, // "CRITICAL", "HIGH", "MEDIUM", "LOW"
    val status: String, // "DETECTED", "OPEN", "UNDER REVIEW", "CONFIRMED", "REPORTED", "RESOLVED", "FALSE POSITIVE"
    val assignedTo: String,
    val detectedAt: Long,
    val detectedAgoLabel: String,

    // Multimodal AI Evidence Signals (Section 17 & 18)
    val usernameSimilarity: Int = 0, // Brand Name / Handle / App Name Similarity
    val logoSimilarity: Int = 0, // Logo / Icon Similarity
    val bioSimilarity: Int = 0, // Domain / Bio / Content Similarity
    val developerTrustScore: Int = 22, // Developer Trust (e.g., 22% for unverified third-party)
    val suspiciousUrlDetected: Boolean = false,
    val suspiciousUrlValue: String = "",
    val scamScore: Int = 0,
    val scamClassification: String = "Normal", // "Normal", "Suspicious", "Scam", "Phishing"
    val metadataRiskScore: Int = 0,
    val accountAgeDays: Int = 0,
    val isVerified: Boolean = false,
    val followerCount: Int = 0,

    // App-specific metadata
    val appDeveloper: String = "",
    val certificateSubject: String = "",
    val certificateMatch: Boolean = false,
    val sensitivePermissionsCsv: String = "",
    val apkSha256: String = "",
    val appDownloads: String = "",

    // Domain-specific metadata
    val domainRegistrar: String = "",
    val sslValid: Boolean = false,
    val redirectChain: String = "",
    val attackVector: String = "",

    // Explainable AI reasons & assessment
    val aiChecklistCsv: String, // "|" separated explainable signals
    val aiExplanation: String, // AI Assessment text

    // Correlation signals
    val sharedPhoneOrEmail: String = "",
    val campaignId: String = ""
) {
    val aiChecklist: List<String>
        get() = aiChecklistCsv.split("|").map { it.trim() }.filter { it.isNotEmpty() }

    val sensitivePermissions: List<String>
        get() = sensitivePermissionsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}

@Entity(tableName = "investigator_notes")
data class InvestigatorNoteEntity(
    @PrimaryKey(autoGenerate = true) val noteId: Int = 0,
    val threatId: String,
    val analystName: String,
    val analystRole: String,
    val noteText: String,
    val actionTaken: String,
    val formattedDate: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true) val alertId: Int = 0,
    val threatId: String,
    val title: String,
    val platform: String,
    val targetIdentifier: String,
    val riskScore: Int,
    val severity: String,
    val summary: String,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class CoordinatedCampaign(
    val campaignId: String, // e.g., "CAMPAIGN-1023"
    val title: String, // e.g., "Potential SBI Impersonation Campaign"
    val subtitle: String, // e.g., "Social + Domain + APK"
    val targetBrandId: String,
    val targetBrandName: String,
    val combinedRiskScore: Int, // e.g., 98 or 96
    val severity: String,
    val statusLabel: String, // e.g., "Under Investigation"
    val detectedAgo: String, // e.g., "32 seconds ago"
    val assetVectorsSummary: List<String>, // e.g., ["Suspicious social profile", "Look-alike domain", "Unverified Android APK"]
    val sharedSignals: List<String>,
    val linkedThreats: List<ThreatIncidentEntity>,
    val aiCampaignSummary: String
)

data class ScannerEngineStatus(
    val id: String,
    val name: String,
    val status: String, // "RUNNING"
    val lastScanSecondsAgo: Int,
    val metricLabel: String, // e.g. "Events", "Apps analyzed", "Domains analyzed", "Images analyzed", "Texts analyzed"
    val metricCount: Int,
    val pollingIntervalLabel: String
)

data class SourceConnectorHealth(
    val id: String,
    val connectorName: String,
    val platformCategory: String,
    val healthState: String, // "HEALTHY", "RATE LIMITED", "DELAYED", "DISCONNECTED", "ERROR"
    val lastSuccessfulScan: String,
    val pollingPolicy: String,
    val note: String
)

enum class UserRole(val title: String, val badgeColorHex: Long, val permissionsDesc: String) {
    ADMINISTRATOR("Administrator", 0xFF00E5FF, "Full Brand Allowlist, Connector, Incident & Takedown Control"),
    SECURITY_ANALYST("Security Analyst", 0xFF2979FF, "Real-Time Threat Triage, AI Scanning, Confirmation & Escalation"),
    INVESTIGATOR("Investigator", 0xFFB388FF, "Deep Forensics, APK Hash Inspection & Evidence Notes"),
    VIEWER("Viewer", 0xFF00E676, "Read-Only Executive Threat Dashboard & Reports")
}

@Entity(tableName = "firebase_users")
data class FirebaseUserEntity(
    @PrimaryKey val uid: String, // e.g., "fb-uid-google-101"
    val email: String,
    val displayName: String,
    val passwordHash: String = "",
    val authProvider: String, // "google.com", "facebook.com", "password"
    val roleName: String, // UserRole.name
    val organization: String = "BrandShield AI SOC",
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis(),
    val syncedToFirebaseCloud: Boolean = true
)

