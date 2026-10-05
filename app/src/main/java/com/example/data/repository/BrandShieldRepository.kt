package com.example.data.repository

import com.example.data.database.BrandShieldDao
import com.example.data.models.AlertEntity
import com.example.data.models.BrandEntity
import com.example.data.models.CoordinatedCampaign
import com.example.data.models.FirebaseUserEntity
import com.example.data.models.InvestigatorNoteEntity
import com.example.data.models.ThreatIncidentEntity
import com.example.data.models.UserRole
import com.example.domain.engine.RiskDetectionEngine
import kotlinx.coroutines.flow.Flow

class BrandShieldRepository(private val dao: BrandShieldDao) {

    val allBrands: Flow<List<BrandEntity>> = dao.getAllBrands()
    val allThreats: Flow<List<ThreatIncidentEntity>> = dao.getAllThreats()
    val allAlerts: Flow<List<AlertEntity>> = dao.getAllAlerts()
    val allFirebaseUsers: Flow<List<FirebaseUserEntity>> = dao.getAllFirebaseUsers()

    suspend fun getFirebaseUserByEmail(email: String): FirebaseUserEntity? =
        dao.getFirebaseUserByEmail(email)

    suspend fun upsertFirebaseUser(user: FirebaseUserEntity) =
        dao.upsertFirebaseUser(user)

    fun observeThreat(threatId: String): Flow<ThreatIncidentEntity?> = dao.observeThreatById(threatId)
    fun observeNotes(threatId: String): Flow<List<InvestigatorNoteEntity>> = dao.getNotesForThreat(threatId)

    suspend fun getBrandById(brandId: String): BrandEntity? = dao.getBrandById(brandId)
    suspend fun getThreatById(threatId: String): ThreatIncidentEntity? = dao.getThreatById(threatId)

    suspend fun insertBrand(brand: BrandEntity) {
        dao.insertBrand(brand)
    }

    suspend fun deleteBrand(brandId: String) {
        dao.deleteBrandById(brandId)
        dao.deleteThreatsByBrandId(brandId)
    }

    suspend fun insertThreatAndAlertIfCritical(threat: ThreatIncidentEntity): Boolean {
        dao.insertThreat(threat)
        if (threat.riskScore >= 81) {
            dao.insertAlert(
                AlertEntity(
                    threatId = threat.threatId,
                    title = "Potential ${threat.brandName} Impersonation",
                    platform = threat.platform,
                    targetIdentifier = threat.targetIdentifier,
                    riskScore = threat.riskScore,
                    severity = threat.severity,
                    summary = "High-similarity unverified asset (${threat.typeLabel}) flagged by real-time AI Engine."
                )
            )
            return true
        }
        return false
    }

    suspend fun updateThreatStatus(
        threatId: String,
        newStatus: String,
        analystName: String,
        analystRole: String,
        noteText: String
    ) {
        dao.updateThreatStatus(threatId, newStatus)
        dao.insertNote(
            InvestigatorNoteEntity(
                threatId = threatId,
                analystName = analystName,
                analystRole = analystRole,
                noteText = noteText,
                actionTaken = "Status -> $newStatus",
                formattedDate = "05-Oct-2026"
            )
        )
    }

    suspend fun assignThreatAnalyst(
        threatId: String,
        assignedAnalyst: String,
        currentStatus: String,
        authorName: String,
        authorRole: String
    ) {
        val nextStatus = if (currentStatus == "DETECTED" || currentStatus == "OPEN") "UNDER REVIEW" else currentStatus
        dao.assignThreatAnalyst(threatId, assignedAnalyst, nextStatus)
        dao.insertNote(
            InvestigatorNoteEntity(
                threatId = threatId,
                analystName = authorName,
                analystRole = authorRole,
                noteText = "Incident assigned to $assignedAnalyst for official allowlist verification and takedown review.",
                actionTaken = "Assigned -> $assignedAnalyst",
                formattedDate = "05-Oct-2026"
            )
        )
    }

    suspend fun addInvestigatorNote(
        threatId: String,
        analystName: String,
        analystRole: String,
        noteText: String,
        actionTaken: String = "Investigation Note"
    ) {
        dao.insertNote(
            InvestigatorNoteEntity(
                threatId = threatId,
                analystName = analystName,
                analystRole = analystRole,
                noteText = noteText,
                actionTaken = actionTaken,
                formattedDate = "05-Oct-2026"
            )
        )
    }

    suspend fun updateThreatAiExplanation(threatId: String, explanation: String, checklist: List<String>) {
        dao.updateThreatAiAnalysis(threatId, explanation, checklist.joinToString("|"))
    }

    suspend fun markAlertRead(alertId: Int) = dao.markAlertRead(alertId)
    suspend fun markAllAlertsRead() = dao.markAllAlertsRead()

    /**
     * Cross-platform Threat Correlation Engine (Section 1, 19, 20, 25).
     * Creates CAMPAIGN-1023 ("Potential SBI Impersonation Campaign") and other correlated multi-asset clusters.
     */
    fun correlateCampaigns(threats: List<ThreatIncidentEntity>): List<CoordinatedCampaign> {
        val grouped = threats
            .filter { it.status != "FALSE POSITIVE" && it.riskScore >= 65 }
            .groupBy { threat ->
                if (threat.campaignId.isNotBlank()) {
                    threat.campaignId
                } else {
                    "CAMPAIGN-${threat.brandId}"
                }
            }

        val campaigns = mutableListOf<CoordinatedCampaign>()
        for ((campId, list) in grouped) {
            if (list.size >= 2) {
                val primary = list.first()
                val isSbiPrimary = campId == "CAMPAIGN-1023" || primary.brandId == "SBI"
                val combinedRisk = if (isSbiPrimary) 96 else (list.maxOf { it.riskScore } + 3).coerceIn(82, 98)

                val assetVectors = if (isSbiPrimary) {
                    listOf(
                        "Suspicious social profile",
                        "Look-alike domain",
                        "Unverified Android APK"
                    )
                } else {
                    list.map {
                        when (it.category) {
                            "SOCIAL" -> "Suspicious social profile (${it.targetIdentifier})"
                            "DOMAIN" -> "Look-alike domain (${it.targetTitle})"
                            "APP" -> "Unverified Android APK (${it.targetTitle})"
                            else -> it.typeLabel
                        }
                    }.distinct()
                }

                val sharedIndicators = listOf(
                    "Shared look-alike domain (sbi-yono-kyc-update.test)",
                    "96%+ visual logo vector similarity across Social, Web & APK",
                    "Synchronized KYC / PAN verification lure text",
                    "Absent from official_assets registry for ${primary.brandName}"
                )

                val summary = "Fake ${primary.brandName} support profile links to a look-alike domain (.test) and promotes an unverified Android APK outside the official allowlist. " +
                    "Correlated as a single multi-vector impersonation campaign rather than isolated incidents."

                campaigns.add(
                    CoordinatedCampaign(
                        campaignId = campId,
                        title = if (isSbiPrimary) "Potential SBI Impersonation Campaign" else "Potential ${primary.brandName} Impersonation Campaign",
                        subtitle = "Social + Domain + APK",
                        targetBrandId = primary.brandId,
                        targetBrandName = primary.brandName,
                        combinedRiskScore = combinedRisk,
                        severity = RiskDetectionEngine.classifySeverity(combinedRisk),
                        statusLabel = "Under Investigation",
                        detectedAgo = "32 seconds ago",
                        assetVectorsSummary = assetVectors,
                        sharedSignals = sharedIndicators,
                        linkedThreats = list.sortedByDescending { it.riskScore },
                        aiCampaignSummary = summary
                    )
                )
            }
        }
        return campaigns.sortedByDescending { it.combinedRiskScore }
    }

    /**
     * Seeds the 10 Indian Banks in `official_assets` (brands table) and controlled synthetic
     * threat incidents using safe `.test` / `.example` domains and `com.demo.*` packages.
     */
    suspend fun ensureSeeded() {
        val existingCount = dao.getBrandCount()
        if (existingCount >= 10) return

        val now = System.currentTimeMillis()

        val indianBanks = listOf(
            BrandEntity(
                brandId = "SBI",
                name = "State Bank of India",
                shortName = "SBI",
                officialAppName = "YONO SBI",
                aliasesCsv = "SBI, State Bank of India, YONO SBI, OnlineSBI",
                officialDomainsCsv = "sbi.co.in, onlinesbi.sbi",
                socialHandlesCsv = "@TheOfficialSBI, @StateBankOfIndia",
                androidPackagesCsv = "com.sbi.lotusintouch, com.sbi.SBIFreedomPlus",
                officialDeveloper = "State Bank of India",
                officialCertificate = "SBI_Official_Prod_Cert_SHA256",
                officialBio = "Official account of State Bank of India. Download YONO SBI only from Google Play or App Store.",
                keywordsCsv = "YONO SBI, OnlineSBI, SBI KYC, State Bank",
                officialDomainsCount = 2,
                officialAppsCount = 3,
                officialSocialProfilesCount = 6,
                officialLogosCount = 4,
                activeThreatsCount = 14,
                criticalThreatsCount = 3,
                highThreatsCount = 6,
                mediumThreatsCount = 4,
                lowThreatsCount = 1,
                createdAt = now - 86_400_000L * 60
            ),
            BrandEntity(
                brandId = "HDFC",
                name = "HDFC Bank",
                shortName = "HDFC Bank",
                officialAppName = "HDFC Bank MobileBanking",
                aliasesCsv = "HDFC, HDFC Bank, HDFC NetBanking, PayZapp",
                officialDomainsCsv = "hdfcbank.com, netbanking.hdfcbank.com",
                socialHandlesCsv = "@HDFC_Bank, @HDFCBank_Cares",
                androidPackagesCsv = "com.snapwork.hdfc, com.enstage.wibmo.hdfc",
                officialDeveloper = "HDFC Bank Ltd.",
                officialCertificate = "HDFCBank_Official_Cert_SHA256",
                officialBio = "Official HDFC Bank social channel. We never ask for OTP, PIN, or CVV over phone or DM.",
                keywordsCsv = "HDFC NetBanking, PayZapp, HDFC KYC",
                officialDomainsCount = 2,
                officialAppsCount = 3,
                officialSocialProfilesCount = 5,
                officialLogosCount = 3,
                activeThreatsCount = 9,
                criticalThreatsCount = 2,
                highThreatsCount = 4,
                mediumThreatsCount = 2,
                lowThreatsCount = 1,
                createdAt = now - 86_400_000L * 55
            ),
            BrandEntity(
                brandId = "ICICI",
                name = "ICICI Bank",
                shortName = "ICICI Bank",
                officialAppName = "iMobile Pay",
                aliasesCsv = "ICICI, ICICI Bank, iMobile Pay by ICICI Bank",
                officialDomainsCsv = "icicibank.com, infinity.icicibank.com",
                socialHandlesCsv = "@ICICIBank, @ICICIBank_Care",
                androidPackagesCsv = "com.csam.icici.bank.imobile",
                officialDeveloper = "ICICI Bank Ltd.",
                officialCertificate = "ICICIBank_iMobile_Cert_SHA256",
                officialBio = "Official ICICI Bank digital banking and iMobile Pay support channel.",
                keywordsCsv = "iMobile Pay, ICICI Infinity, ICICI Card",
                officialDomainsCount = 2,
                officialAppsCount = 2,
                officialSocialProfilesCount = 5,
                officialLogosCount = 3,
                activeThreatsCount = 7,
                criticalThreatsCount = 1,
                highThreatsCount = 3,
                mediumThreatsCount = 2,
                lowThreatsCount = 1,
                createdAt = now - 86_400_000L * 50
            ),
            BrandEntity(
                brandId = "AXIS",
                name = "Axis Bank",
                shortName = "Axis Bank",
                officialAppName = "open by Axis Bank",
                aliasesCsv = "Axis Bank, open by Axis Bank, Axis Mobile",
                officialDomainsCsv = "axisbank.com, omni.axisbank.co.in",
                socialHandlesCsv = "@AxisBank, @AxisBankSupport",
                androidPackagesCsv = "com.axis.mobile",
                officialDeveloper = "Axis Bank Ltd.",
                officialCertificate = "AxisBank_Open_Cert_SHA256",
                officialBio = "Official Axis Bank channel. Experience digital banking with open by Axis Bank.",
                keywordsCsv = "open by Axis Bank, Axis KYC, Axis Support",
                officialDomainsCount = 2,
                officialAppsCount = 2,
                officialSocialProfilesCount = 4,
                officialLogosCount = 3,
                activeThreatsCount = 5,
                criticalThreatsCount = 0,
                highThreatsCount = 3,
                mediumThreatsCount = 2,
                lowThreatsCount = 0,
                createdAt = now - 86_400_000L * 45
            ),
            BrandEntity(
                brandId = "KOTAK",
                name = "Kotak Mahindra Bank",
                shortName = "Kotak Mahindra Bank",
                officialAppName = "Kotak811 & Mobile Banking",
                aliasesCsv = "Kotak, Kotak Mahindra Bank, Kotak 811",
                officialDomainsCsv = "kotak.com, kotak811.com",
                socialHandlesCsv = "@KotakBankLtd, @KotakCares",
                androidPackagesCsv = "com.msf.kbank.mobile",
                officialDeveloper = "Kotak Mahindra Bank Ltd.",
                officialCertificate = "KotakBank_Official_Cert_SHA256",
                officialBio = "Official Kotak Mahindra Bank & Kotak811 digital banking updates.",
                keywordsCsv = "Kotak811, Kotak Mobile, Kotak Care",
                officialDomainsCount = 2,
                officialAppsCount = 2,
                officialSocialProfilesCount = 4,
                officialLogosCount = 2,
                activeThreatsCount = 4,
                criticalThreatsCount = 1,
                highThreatsCount = 2,
                mediumThreatsCount = 1,
                lowThreatsCount = 0,
                createdAt = now - 86_400_000L * 40
            ),
            BrandEntity(
                brandId = "PNB",
                name = "Punjab National Bank",
                shortName = "Punjab National Bank",
                officialAppName = "PNB ONE",
                aliasesCsv = "PNB, Punjab National Bank, PNB ONE",
                officialDomainsCsv = "pnbindia.in, netpnb.com",
                socialHandlesCsv = "@pnbindia, @PNBCares",
                androidPackagesCsv = "com.Version1",
                officialDeveloper = "Punjab National Bank",
                officialCertificate = "PNB_One_Official_Cert_SHA256",
                officialBio = "Official Punjab National Bank social presence and PNB ONE mobile banking.",
                keywordsCsv = "PNB ONE, netpnb, PNB Support",
                officialDomainsCount = 2,
                officialAppsCount = 2,
                officialSocialProfilesCount = 4,
                officialLogosCount = 2,
                activeThreatsCount = 3,
                criticalThreatsCount = 0,
                highThreatsCount = 2,
                mediumThreatsCount = 1,
                lowThreatsCount = 0,
                createdAt = now - 86_400_000L * 35
            ),
            BrandEntity(
                brandId = "CANARA",
                name = "Canara Bank",
                shortName = "Canara Bank",
                officialAppName = "Canara ai1",
                aliasesCsv = "Canara Bank, Canara ai1",
                officialDomainsCsv = "canarabank.com, canarabank.in",
                socialHandlesCsv = "@canarabank, @CanaraBankCares",
                androidPackagesCsv = "com.canarabank.mobility",
                officialDeveloper = "Canara Bank",
                officialCertificate = "Canara_ai1_Official_Cert_SHA256",
                officialBio = "Official Canara Bank super app Canara ai1 and customer service.",
                keywordsCsv = "Canara ai1, Canara NetBanking",
                officialDomainsCount = 2,
                officialAppsCount = 2,
                officialSocialProfilesCount = 3,
                officialLogosCount = 2,
                activeThreatsCount = 3,
                criticalThreatsCount = 0,
                highThreatsCount = 1,
                mediumThreatsCount = 2,
                lowThreatsCount = 0,
                createdAt = now - 86_400_000L * 30
            ),
            BrandEntity(
                brandId = "BOB",
                name = "Bank of Baroda",
                shortName = "Bank of Baroda",
                officialAppName = "bob World",
                aliasesCsv = "BoB, Bank of Baroda, bob World",
                officialDomainsCsv = "bankofbaroda.in, bobibanking.com",
                socialHandlesCsv = "@bankofbaroda, @bobWorldApp",
                androidPackagesCsv = "com.bankofbaroda.mconnect",
                officialDeveloper = "Bank of Baroda",
                officialCertificate = "BoB_World_Official_Cert_SHA256",
                officialBio = "Official Bank of Baroda & bob World digital banking updates.",
                keywordsCsv = "bob World, Baroda Connect",
                officialDomainsCount = 2,
                officialAppsCount = 2,
                officialSocialProfilesCount = 4,
                officialLogosCount = 2,
                activeThreatsCount = 2,
                criticalThreatsCount = 1,
                highThreatsCount = 0,
                mediumThreatsCount = 1,
                lowThreatsCount = 0,
                createdAt = now - 86_400_000L * 25
            ),
            BrandEntity(
                brandId = "UNION",
                name = "Union Bank of India",
                shortName = "Union Bank of India",
                officialAppName = "Vyom by Union Bank",
                aliasesCsv = "Union Bank, Union Bank of India, Vyom",
                officialDomainsCsv = "unionbankofindia.co.in, unionbankonline.co.in",
                socialHandlesCsv = "@UnionBankTweets",
                androidPackagesCsv = "com.infrasoft.uboi",
                officialDeveloper = "Union Bank of India",
                officialCertificate = "UnionBank_Vyom_Cert_SHA256",
                officialBio = "Official Union Bank of India & Vyom digital banking channel.",
                keywordsCsv = "Vyom, Union Bank KYC",
                officialDomainsCount = 2,
                officialAppsCount = 2,
                officialSocialProfilesCount = 3,
                officialLogosCount = 2,
                activeThreatsCount = 2,
                criticalThreatsCount = 0,
                highThreatsCount = 1,
                mediumThreatsCount = 1,
                lowThreatsCount = 0,
                createdAt = now - 86_400_000L * 20
            ),
            BrandEntity(
                brandId = "IDFC_FIRST",
                name = "IDFC FIRST Bank",
                shortName = "IDFC FIRST Bank",
                officialAppName = "IDFC FIRST Bank MobileBanking",
                aliasesCsv = "IDFC FIRST, IDFC FIRST Bank",
                officialDomainsCsv = "idfcfirstbank.com, my.idfcfirstbank.com",
                socialHandlesCsv = "@IDFCFIRSTBank, @IDFCFIRSTCares",
                androidPackagesCsv = "com.idfcfirstbank.optimus",
                officialDeveloper = "IDFC FIRST Bank Ltd.",
                officialCertificate = "IDFCFirst_Optimus_Cert_SHA256",
                officialBio = "Official IDFC FIRST Bank Always You First customer service channel.",
                keywordsCsv = "IDFC FIRST, IDFC Mobile",
                officialDomainsCount = 2,
                officialAppsCount = 2,
                officialSocialProfilesCount = 3,
                officialLogosCount = 2,
                activeThreatsCount = 2,
                criticalThreatsCount = 0,
                highThreatsCount = 1,
                mediumThreatsCount = 0,
                lowThreatsCount = 1,
                createdAt = now - 86_400_000L * 15
            )
        )

        indianBanks.forEach { dao.insertBrand(it) }

        val seededThreats = listOf(
            // 1. SBI — Instagram • Potential Impersonation — Risk 94 CRITICAL (Exact Section 2 & 8)
            ThreatIncidentEntity(
                threatId = "THR-20261005-1001",
                brandId = "SBI",
                brandName = "State Bank of India",
                category = "SOCIAL",
                typeLabel = "Potential Social Impersonation",
                platform = "Instagram",
                targetTitle = "SBI Customer Assistance",
                targetIdentifier = "@sbi_helpdesk_test01",
                riskScore = 94,
                severity = "CRITICAL",
                status = "OPEN",
                assignedTo = "Hari (Senior SOC Analyst)",
                detectedAt = now - 18_000L, // 18 seconds ago
                detectedAgoLabel = "18 sec ago",
                usernameSimilarity = 93,
                logoSimilarity = 97,
                bioSimilarity = 89,
                developerTrustScore = 18,
                suspiciousUrlDetected = true,
                suspiciousUrlValue = "sbi-yono-kyc-update.test",
                scamScore = 87,
                scamClassification = "Phishing",
                metadataRiskScore = 92,
                accountAgeDays = 2,
                isVerified = false,
                followerCount = 118,
                aiChecklistCsv = "Name similarity 93%|Logo similarity 97%|Suspicious URL (sbi-yono-kyc-update.test)|KYC language detected",
                aiExplanation = "This asset has a high probability of brand impersonation because its name and visual identity closely resemble the protected brand (State Bank of India) while its handle (@sbi_helpdesk_test01) and linked domain (sbi-yono-kyc-update.test) are not present in the official asset registry.",
                sharedPhoneOrEmail = "help@sbi-yono-kyc-update.test",
                campaignId = "CAMPAIGN-1023"
            ),
            // 2. HDFC Bank — Android APK • Third-Party Source — Risk 91 CRITICAL (Exact Section 2, 5 & 8)
            ThreatIncidentEntity(
                threatId = "THR-20261005-1002",
                brandId = "HDFC",
                brandName = "HDFC Bank",
                category = "APP",
                typeLabel = "Suspicious Android App",
                platform = "Android APK • Third-Party Source",
                targetTitle = "HDFC Mobile Secure",
                targetIdentifier = "com.demo.hdfc.securebank",
                riskScore = 91,
                severity = "CRITICAL",
                status = "UNDER REVIEW",
                assignedTo = "Hari (Senior SOC Analyst)",
                detectedAt = now - 60_000L, // 1 minute ago
                detectedAgoLabel = "1 min ago",
                usernameSimilarity = 95,
                logoSimilarity = 91,
                bioSimilarity = 86,
                developerTrustScore = 22,
                suspiciousUrlDetected = true,
                suspiciousUrlValue = "hdfc-netbanking-verify.test",
                scamScore = 85,
                scamClassification = "Phishing",
                metadataRiskScore = 90,
                accountAgeDays = 3,
                isVerified = false,
                appDeveloper = "Unverified FinApp Publisher",
                certificateSubject = "UnrecognizedDemoCert_SHA256",
                certificateMatch = false,
                sensitivePermissionsCsv = "READ_SMS, RECEIVE_SMS, BIND_ACCESSIBILITY_SERVICE, SYSTEM_ALERT_WINDOW",
                apkSha256 = "8FA21C904E7B11D35209AC8412F67E390B14C8D2E5F6A10B23C4D5E6F7A8B9C0",
                appDownloads = "840+ observed",
                aiChecklistCsv = "Brand similarity 95%|Icon similarity 91%|Unrecognized certificate|Sensitive SMS permission",
                aiExplanation = "This asset has a high probability of brand impersonation because its name and visual identity closely resemble the protected brand (HDFC Bank) while its package (com.demo.hdfc.securebank), developer identity and certificate are not present in the official asset registry.",
                sharedPhoneOrEmail = "apk-feed@hdfc-netbanking-verify.test",
                campaignId = "CAMPAIGN-1024"
            ),
            // 3. ICICI Bank — Look-alike Domain — Risk 79 HIGH (Exact Section 2 & 25)
            ThreatIncidentEntity(
                threatId = "THR-20261005-1003",
                brandId = "ICICI",
                brandName = "ICICI Bank",
                category = "DOMAIN",
                typeLabel = "Look-alike Domain",
                platform = "Domain",
                targetTitle = "icici-kyc-verify.test",
                targetIdentifier = "https://icici-kyc-verify.test",
                riskScore = 79,
                severity = "HIGH",
                status = "OPEN",
                assignedTo = "Priya Nair (Threat Intel)",
                detectedAt = now - 180_000L, // 3 minutes ago
                detectedAgoLabel = "3 min ago",
                usernameSimilarity = 92,
                logoSimilarity = 88,
                bioSimilarity = 89,
                developerTrustScore = 25,
                suspiciousUrlDetected = true,
                suspiciousUrlValue = "icici-kyc-verify.test",
                scamScore = 82,
                scamClassification = "Suspicious",
                metadataRiskScore = 84,
                accountAgeDays = 2,
                isVerified = false,
                domainRegistrar = "Controlled Sandbox Registrar (.test)",
                sslValid = false,
                redirectChain = "icici-kyc-verify.test -> /imobile-session-check",
                attackVector = "Look-alike Domain + Keyword (KYC)",
                aiChecklistCsv = "Domain similarity|\"KYC\" keyword|Recently observed|Not in official-domain allowlist",
                aiExplanation = "This asset has a high probability of brand impersonation because its domain name closely resembles ICICI Bank (icicibank.com) and uses the 'KYC' verification lure while not being present in the official-domain allowlist.",
                sharedPhoneOrEmail = "verify@icici-kyc-verify.test",
                campaignId = ""
            ),
            // 4. SBI Correlated Look-alike Domain (Part of CAMPAIGN-1023, Section 16 & 19)
            ThreatIncidentEntity(
                threatId = "THR-20261005-1004",
                brandId = "SBI",
                brandName = "State Bank of India",
                category = "DOMAIN",
                typeLabel = "Look-alike Domain",
                platform = "Domain",
                targetTitle = "sbi-yono-kyc-update.test",
                targetIdentifier = "https://sbi-yono-kyc-update.test",
                riskScore = 86,
                severity = "CRITICAL",
                status = "UNDER REVIEW",
                assignedTo = "Hari (Senior SOC Analyst)",
                detectedAt = now - 32_000L, // 32 seconds ago
                detectedAgoLabel = "32 sec ago",
                usernameSimilarity = 96,
                logoSimilarity = 93,
                bioSimilarity = 89,
                developerTrustScore = 20,
                suspiciousUrlDetected = true,
                suspiciousUrlValue = "sbi-yono-kyc-update.test",
                scamScore = 88,
                scamClassification = "Phishing",
                metadataRiskScore = 90,
                accountAgeDays = 1,
                isVerified = false,
                domainRegistrar = "Controlled Sandbox Registrar (.test)",
                sslValid = false,
                redirectChain = "sbi-yono-kyc-update.test -> /yono-apk-download",
                attackVector = "Combo-Squatting (SBI + YONO + KYC)",
                aiChecklistCsv = "Domain similarity 89% to sbi.co.in|\"YONO\" and \"KYC\" keywords|Linked from @sbi_helpdesk_test01|Not in official-domain allowlist",
                aiExplanation = "This asset has a high probability of brand impersonation because its domain name closely resembles State Bank of India's official YONO portal while not being present in the official asset registry.",
                sharedPhoneOrEmail = "help@sbi-yono-kyc-update.test",
                campaignId = "CAMPAIGN-1023"
            ),
            // 5. SBI Correlated Suspicious APK (Part of CAMPAIGN-1023, Section 16 & 19)
            ThreatIncidentEntity(
                threatId = "THR-20261005-1005",
                brandId = "SBI",
                brandName = "State Bank of India",
                category = "APP",
                typeLabel = "Suspicious APK",
                platform = "Android APK • Third-Party Feed",
                targetTitle = "YONO SBI Lite KYC",
                targetIdentifier = "com.demo.sbi.yonokyc",
                riskScore = 83,
                severity = "CRITICAL",
                status = "UNDER REVIEW",
                assignedTo = "Hari (Senior SOC Analyst)",
                detectedAt = now - 45_000L,
                detectedAgoLabel = "45 sec ago",
                usernameSimilarity = 96,
                logoSimilarity = 94,
                bioSimilarity = 87,
                developerTrustScore = 15,
                suspiciousUrlDetected = true,
                suspiciousUrlValue = "sbi-yono-kyc-update.test/apk",
                scamScore = 86,
                scamClassification = "Phishing",
                metadataRiskScore = 88,
                accountAgeDays = 2,
                isVerified = false,
                appDeveloper = "Untrusted Mobile APK Build",
                certificateSubject = "UnverifiedDemoCert_09",
                certificateMatch = false,
                sensitivePermissionsCsv = "READ_SMS, RECEIVE_SMS, REQUEST_INSTALL_PACKAGES",
                apkSha256 = "3E91A04B7C22D88F1904E6A12B55C78D90E11F23A45B67C89D01E23F45A67B89",
                appDownloads = "390+ observed",
                aiChecklistCsv = "Brand similarity 96% to YONO SBI|Icon similarity 94%|Unrecognized certificate|Not in official-app allowlist (com.sbi.lotusintouch)",
                aiExplanation = "This asset has a high probability of brand impersonation because its name and icon mimic YONO SBI while its package (com.demo.sbi.yonokyc) and signing certificate are not present in State Bank of India's official asset registry.",
                sharedPhoneOrEmail = "help@sbi-yono-kyc-update.test",
                campaignId = "CAMPAIGN-1023"
            ),
            // 6. Axis Bank — Suspicious Support Profile — Risk 78 HIGH (Section 8)
            ThreatIncidentEntity(
                threatId = "THR-20261005-1006",
                brandId = "AXIS",
                brandName = "Axis Bank",
                category = "SOCIAL",
                typeLabel = "Suspicious Support Profile",
                platform = "X (Twitter)",
                targetTitle = "Axis Open Support Desk",
                targetIdentifier = "@axis_open_care_demo",
                riskScore = 78,
                severity = "HIGH",
                status = "OPEN",
                assignedTo = "Alex Chen (SOC Tier-2)",
                detectedAt = now - 300_000L, // 5 min ago
                detectedAgoLabel = "5 min ago",
                usernameSimilarity = 91,
                logoSimilarity = 88,
                bioSimilarity = 84,
                developerTrustScore = 24,
                suspiciousUrlDetected = true,
                suspiciousUrlValue = "axis-open-reward.example",
                scamScore = 76,
                scamClassification = "Suspicious",
                metadataRiskScore = 75,
                accountAgeDays = 5,
                isVerified = false,
                followerCount = 215,
                aiChecklistCsv = "Name similarity 91% to 'open by Axis Bank'|Logo similarity 88%|Suspicious URL (axis-open-reward.example)|Not in official social handle allowlist (@AxisBank)",
                aiExplanation = "This asset has a high probability of brand impersonation because it mimics 'open by Axis Bank' customer support while linking to an external .example domain not in Axis Bank's official asset registry.",
                campaignId = ""
            ),
            // 7. Kotak Mahindra Bank — Possible Fake Customer-Care Account — Risk 73 HIGH (Section 8)
            ThreatIncidentEntity(
                threatId = "THR-20261005-1007",
                brandId = "KOTAK",
                brandName = "Kotak Mahindra Bank",
                category = "SOCIAL",
                typeLabel = "Possible Fake Customer-Care Account",
                platform = "Telegram",
                targetTitle = "Kotak 811 Instant Care",
                targetIdentifier = "@kotak811_support_demo",
                riskScore = 73,
                severity = "HIGH",
                status = "OPEN",
                assignedTo = "Priya Nair (Threat Intel)",
                detectedAt = now - 480_000L, // 8 min ago
                detectedAgoLabel = "8 min ago",
                usernameSimilarity = 89,
                logoSimilarity = 85,
                bioSimilarity = 80,
                developerTrustScore = 26,
                suspiciousUrlDetected = true,
                suspiciousUrlValue = "kotak811-verify.test",
                scamScore = 72,
                scamClassification = "Suspicious",
                metadataRiskScore = 70,
                accountAgeDays = 7,
                isVerified = false,
                followerCount = 430,
                aiChecklistCsv = "Name similarity 89% to Kotak 811|Logo similarity 85%|Suspicious URL (kotak811-verify.test)|Not in official social allowlist (@KotakBankLtd)",
                aiExplanation = "This asset has a high probability of brand impersonation because its display name and emblem closely resemble Kotak Mahindra Bank's Kotak811 channel while operating outside the official allowlist.",
                campaignId = ""
            ),
            // 8. Punjab National Bank — Brand Logo Reuse — Risk 66 HIGH (Section 8)
            ThreatIncidentEntity(
                threatId = "THR-20261005-1008",
                brandId = "PNB",
                brandName = "Punjab National Bank",
                category = "DOMAIN",
                typeLabel = "Brand Logo Reuse",
                platform = "Domain",
                targetTitle = "pnb-one-rewards.example",
                targetIdentifier = "https://pnb-one-rewards.example",
                riskScore = 66,
                severity = "HIGH",
                status = "UNDER REVIEW",
                assignedTo = "Alex Chen (SOC Tier-2)",
                detectedAt = now - 720_000L, // 12 min ago
                detectedAgoLabel = "12 min ago",
                usernameSimilarity = 86,
                logoSimilarity = 94,
                bioSimilarity = 75,
                developerTrustScore = 30,
                suspiciousUrlDetected = true,
                suspiciousUrlValue = "pnb-one-rewards.example",
                scamScore = 68,
                scamClassification = "Suspicious",
                metadataRiskScore = 65,
                accountAgeDays = 9,
                isVerified = false,
                domainRegistrar = "Controlled Sandbox (.example)",
                sslValid = false,
                redirectChain = "pnb-one-rewards.example -> /claim-points",
                attackVector = "Unauthorized Brand Logo & App Name Reuse",
                aiChecklistCsv = "Logo similarity 94% to official PNB emblem|References 'PNB ONE' mobile app|Not in official-domain allowlist (pnbindia.in)",
                aiExplanation = "This asset has a high probability of brand impersonation because it reuses Punjab National Bank's official logo and PNB ONE branding on an unverified .example domain.",
                campaignId = ""
            ),
            // 9. Bank of Baroda — Unverified APK — Risk 84 CRITICAL
            ThreatIncidentEntity(
                threatId = "THR-20261005-1009",
                brandId = "BOB",
                brandName = "Bank of Baroda",
                category = "APP",
                typeLabel = "Unverified Android APK",
                platform = "Android APK • Third-Party Source",
                targetTitle = "bob World Reward Plus",
                targetIdentifier = "com.demo.bobworld.rewards",
                riskScore = 84,
                severity = "CRITICAL",
                status = "OPEN",
                assignedTo = "Hari (Senior SOC Analyst)",
                detectedAt = now - 900_000L, // 15 min ago
                detectedAgoLabel = "15 min ago",
                usernameSimilarity = 92,
                logoSimilarity = 90,
                bioSimilarity = 82,
                developerTrustScore = 20,
                suspiciousUrlDetected = true,
                suspiciousUrlValue = "bobworld-points.test",
                scamScore = 80,
                scamClassification = "Scam",
                metadataRiskScore = 85,
                accountAgeDays = 4,
                isVerified = false,
                appDeveloper = "Unverified Rewards Dev",
                certificateSubject = "UnknownDemoCert_BoB",
                certificateMatch = false,
                sensitivePermissionsCsv = "READ_SMS, RECEIVE_SMS, READ_CONTACTS",
                apkSha256 = "91C4E802A11B3F77D60E92A4C55B18F03E72C91D44A8B05E6F12C34D56E78F90",
                appDownloads = "290+ observed",
                aiChecklistCsv = "Brand similarity 92% to 'bob World'|Icon similarity 90%|Unrecognized certificate|Sensitive SMS permission",
                aiExplanation = "This asset has a high probability of brand impersonation because its title and icon closely resemble Bank of Baroda's official 'bob World' app while its package and certificate are absent from the official asset registry.",
                campaignId = ""
            )
        )

        dao.insertThreats(seededThreats)

        dao.insertNote(
            InvestigatorNoteEntity(
                threatId = "THR-20261005-1001",
                analystName = "Hari",
                analystRole = "Security Analyst",
                noteText = "Profile @sbi_helpdesk_test01 is not in State Bank of India's official social allowlist and directs users to sbi-yono-kyc-update.test. Correlated with CAMPAIGN-1023.",
                actionTaken = "Allowlist & Campaign Correlation",
                formattedDate = "05-Oct-2026",
                timestamp = now - 15_000L
            )
        )

        dao.insertAlert(
            AlertEntity(
                threatId = "THR-20261005-1001",
                title = "Potential SBI Social Impersonation",
                platform = "Instagram",
                targetIdentifier = "@sbi_helpdesk_test01",
                riskScore = 94,
                severity = "CRITICAL",
                summary = "Name similarity 93%, Logo similarity 97%, links to sbi-yono-kyc-update.test. Tap to investigate.",
                isRead = false,
                timestamp = now - 18_000L
            )
        )
        dao.insertAlert(
            AlertEntity(
                threatId = "THR-20261005-1002",
                title = "Suspicious HDFC Bank APK Observed",
                platform = "Android APK",
                targetIdentifier = "com.demo.hdfc.securebank",
                riskScore = 91,
                severity = "CRITICAL",
                summary = "HDFC Mobile Secure observed with unrecognized certificate and SMS permissions.",
                isRead = false,
                timestamp = now - 60_000L
            )
        )

        dao.upsertFirebaseUser(
            FirebaseUserEntity(
                uid = "fb-google-bhavya-01",
                email = "varigondabhavya@gmail.com",
                displayName = "Bhavya Varigonda",
                passwordHash = "",
                authProvider = "google.com",
                roleName = UserRole.SECURITY_ANALYST.name,
                organization = "Google Workspace • BrandShield SOC",
                createdAt = now - 86_400_000L * 14,
                lastLoginAt = now - 3600_000L,
                syncedToFirebaseCloud = true
            )
        )
        dao.upsertFirebaseUser(
            FirebaseUserEntity(
                uid = "fb-pwd-hari-02",
                email = "hari.soc@brandshield.in",
                displayName = "Hari (Senior SOC Analyst)",
                passwordHash = "",
                authProvider = "password",
                roleName = UserRole.SECURITY_ANALYST.name,
                organization = "BrandShield AI SOC India",
                createdAt = now - 86_400_000L * 30,
                lastLoginAt = now - 1800_000L,
                syncedToFirebaseCloud = true
            )
        )
        dao.upsertFirebaseUser(
            FirebaseUserEntity(
                uid = "fb-facebook-priya-03",
                email = "priya.nair.fb@brandshield.in",
                displayName = "Priya Nair (Facebook Auth)",
                passwordHash = "",
                authProvider = "facebook.com",
                roleName = UserRole.INVESTIGATOR.name,
                organization = "Meta Threat Intel • BrandShield SOC",
                createdAt = now - 86_400_000L * 10,
                lastLoginAt = now - 7200_000L,
                syncedToFirebaseCloud = true
            )
        )
    }
}
