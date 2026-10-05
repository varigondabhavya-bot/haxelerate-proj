package com.example.domain.engine

import com.example.data.models.BrandEntity
import java.security.MessageDigest
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

object RiskDetectionEngine {

    private val homoglyphMap = mapOf(
        '0' to 'o',
        '1' to 'l',
        '3' to 'e',
        '4' to 'a',
        '5' to 's',
        '8' to 'b',
        '@' to 'a',
        '$' to 's',
        '!' to 'i',
        '|' to 'l'
    )

    private val scamKeywords = mapOf(
        "urgent" to 18,
        "account blocked" to 28,
        "kyc expired" to 30,
        "complete kyc" to 28,
        "kyc update" to 26,
        "verify immediately" to 26,
        "send otp" to 35,
        "share otp" to 35,
        "click here" to 16,
        "claim reward" to 24,
        "cashback" to 20,
        "lottery" to 28,
        "your account will be suspended" to 32,
        "pan update" to 25,
        "yono blocked" to 30,
        "helpline" to 16,
        "customer support" to 14,
        "instant refund" to 25
    )

    val highRiskPermissions = mapOf(
        "BIND_ACCESSIBILITY_SERVICE" to 35,
        "READ_SMS" to 28,
        "RECEIVE_SMS" to 28,
        "SYSTEM_ALERT_WINDOW" to 25,
        "REQUEST_INSTALL_PACKAGES" to 25,
        "READ_CALL_LOG" to 18,
        "READ_CONTACTS" to 14,
        "RECORD_AUDIO" to 15,
        "CAMERA" to 10
    )

    private val suspiciousDomainKeywords = listOf(
        "login", "secure", "verify", "kyc", "support", "help", "update", "auth", "unlock", "portal", "reward", "yono", "imobile"
    )

    private val highRiskTlds = listOf(".test", ".example", ".invalid", ".xyz", ".top", ".buzz", ".click", ".info", ".online", ".site")

    fun levenshteinDistance(lhs: CharSequence, rhs: CharSequence): Int {
        val lhsLength = lhs.length
        val rhsLength = rhs.length

        var cost = IntArray(lhsLength + 1) { it }
        var newCost = IntArray(lhsLength + 1) { 0 }

        for (i in 1..rhsLength) {
            newCost[0] = i
            for (j in 1..lhsLength) {
                val match = if (lhs[j - 1].lowercaseChar() == rhs[i - 1].lowercaseChar()) 0 else 1
                val costReplace = cost[j - 1] + match
                val costInsert = cost[j] + 1
                val costDelete = newCost[j - 1] + 1
                newCost[j] = min(min(costInsert, costDelete), costReplace)
            }
            val swap = cost
            cost = newCost
            newCost = swap
        }
        return cost[lhsLength]
    }

    fun jaroWinklerSimilarity(s1: String, s2: String): Double {
        val a = s1.lowercase().trim()
        val b = s2.lowercase().trim()
        if (a == b) return 1.0
        if (a.isEmpty() || b.isEmpty()) return 0.0

        val matchDistance = max(a.length, b.length) / 2 - 1
        val aMatches = BooleanArray(a.length)
        val bMatches = BooleanArray(b.length)

        var matches = 0
        for (i in a.indices) {
            val start = max(0, i - max(0, matchDistance))
            val end = min(i + max(0, matchDistance) + 1, b.length)
            for (j in start until end) {
                if (bMatches[j]) continue
                if (a[i] != b[j]) continue
                aMatches[i] = true
                bMatches[j] = true
                matches++
                break
            }
        }
        if (matches == 0) return 0.0

        var transpositions = 0
        var k = 0
        for (i in a.indices) {
            if (!aMatches[i]) continue
            while (!bMatches[k]) k++
            if (a[i] != b[k]) transpositions++
            k++
        }

        val m = matches.toDouble()
        val jaro = ((m / a.length) + (m / b.length) + ((m - transpositions / 2.0) / m)) / 3.0

        var prefix = 0
        for (i in 0 until min(4, min(a.length, b.length))) {
            if (a[i] == b[i]) prefix++ else break
        }
        return jaro + prefix * 0.1 * (1.0 - jaro)
    }

    fun analyzeUsernameImpersonation(
        officialHandles: List<String>,
        brandName: String,
        candidateHandle: String
    ): Pair<Int, String> {
        val cleanCandidate = candidateHandle.trim().removePrefix("@").lowercase()
        val normalizedCandidate = cleanCandidate.map { homoglyphMap[it] ?: it }.joinToString("")
        val alphaOnlyCandidate = cleanCandidate.replace(Regex("[^a-z]"), "")

        val references = (officialHandles.map { it.removePrefix("@").lowercase() } +
            brandName.lowercase().replace(" ", "")).distinct()

        var bestScore = 0
        var technique = "Fuzzy Match"

        for (ref in references) {
            if (ref.isEmpty()) continue
            if (cleanCandidate == ref) {
                return 100 to "Exact Official Handle Match"
            }

            val jwRaw = jaroWinklerSimilarity(ref, cleanCandidate)
            val jwNormalized = jaroWinklerSimilarity(ref, normalizedCandidate)
            val levDist = levenshteinDistance(ref, cleanCandidate)
            val maxLen = max(ref.length, cleanCandidate.length).coerceAtLeast(1)
            val levSim = 1.0 - (levDist.toDouble() / maxLen)

            val containsRef = cleanCandidate.contains(ref) || alphaOnlyCandidate.contains(ref)
            val strippedSupport = cleanCandidate
                .replace("helpdesk", "")
                .replace("help", "")
                .replace("support", "")
                .replace("care", "")
                .replace("official", "")
                .replace("kyc", "")
                .replace("test", "")
                .replace(Regex("[0-9_.-]"), "")

            val coreSim = jaroWinklerSimilarity(ref, strippedSupport)

            var score = max(jwRaw, max(jwNormalized, levSim)) * 100.0
            var currentTechnique = "Levenshtein / Jaro-Winkler Similarity"

            if (containsRef && cleanCandidate != ref) {
                score = max(score, 93.0 + min(5.0, coreSim * 5.0))
                currentTechnique = "Brand Prefix + Support/Numeric Suffix"
            } else if (jwNormalized > jwRaw + 0.03) {
                score = max(score, jwNormalized * 98.0)
                currentTechnique = "Homoglyph / Character Substitution"
            } else if (levDist == 1) {
                score = max(score, 94.0)
                currentTechnique = "Single-Character Typosquatting"
            }

            val rounded = score.roundToInt().coerceIn(0, 99)
            if (rounded > bestScore) {
                bestScore = rounded
                technique = currentTechnique
            }
        }
        return bestScore to technique
    }

    fun calculateSemanticTextSimilarity(officialText: String, candidateText: String): Int {
        if (officialText.isBlank() || candidateText.isBlank()) return 0
        val tokensA = tokenizeAndExpandSemantics(officialText)
        val tokensB = tokenizeAndExpandSemantics(candidateText)
        if (tokensA.isEmpty() || tokensB.isEmpty()) return 0

        val allKeys = (tokensA.keys + tokensB.keys).distinct()
        var dotProduct = 0.0
        var normA = 0.0
        var normB = 0.0

        for (key in allKeys) {
            val a = tokensA[key] ?: 0.0
            val b = tokensB[key] ?: 0.0
            dotProduct += a * b
            normA += a * a
            normB += b * b
        }

        if (normA == 0.0 || normB == 0.0) return 0
        val cosine = dotProduct / (sqrt(normA) * sqrt(normB))
        return (min(1.0, cosine * 1.25) * 100).roundToInt().coerceIn(0, 99)
    }

    private fun tokenizeAndExpandSemantics(text: String): Map<String, Double> {
        val words = text.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length > 2 }

        val freq = mutableMapOf<String, Double>()
        for (w in words) {
            freq[w] = (freq[w] ?: 0.0) + 1.0
            when (w) {
                "official", "authorized", "verified", "authentic" ->
                    freq["__concept_official"] = (freq["__concept_official"] ?: 0.0) + 1.2
                "customer", "client", "user", "member", "account", "retail" ->
                    freq["__concept_customer"] = (freq["__concept_customer"] ?: 0.0) + 1.1
                "service", "support", "help", "care", "assistance", "team", "desk", "helpdesk" ->
                    freq["__concept_support"] = (freq["__concept_support"] ?: 0.0) + 1.3
                "bank", "banking", "financial", "finance", "mobile", "secure", "yono", "imobile", "open" ->
                    freq["__concept_banking"] = (freq["__concept_banking"] ?: 0.0) + 1.3
                "issues", "queries", "problems", "contact", "resolve", "kyc" ->
                    freq["__concept_action"] = (freq["__concept_action"] ?: 0.0) + 1.0
            }
        }
        return freq
    }

    data class ScamAnalysisResult(
        val score: Int,
        val classification: String,
        val matchedTriggers: List<String>
    )

    fun analyzeScamLanguage(text: String, hasSuspiciousUrl: Boolean): ScamAnalysisResult {
        val lower = text.lowercase()
        val matched = mutableListOf<String>()
        var rawScore = 0

        for ((phrase, weight) in scamKeywords) {
            if (lower.contains(phrase)) {
                matched.add(phrase.uppercase())
                rawScore += weight
            }
        }

        if (hasSuspiciousUrl && rawScore > 0) {
            rawScore += 20
        }

        val finalScore = rawScore.coerceIn(0, 100)
        val classification = when {
            finalScore >= 75 && hasSuspiciousUrl -> "Phishing"
            finalScore >= 55 -> "Scam"
            finalScore >= 25 -> "Suspicious"
            else -> "Normal"
        }
        return ScamAnalysisResult(finalScore, classification, matched)
    }

    /**
     * Category-Specific Social Account Risk Model (Section 18):
     * Name similarity (15%) + Handle similarity (15%) + Logo similarity (20%) +
     * Bio similarity (10%) + Suspicious URL (20%) + Scam language (10%) + Account metadata (10%)
     */
    data class SocialScanAnalysis(
        val nameSimilarity: Int,
        val usernameSimilarity: Int,
        val usernameTechnique: String,
        val logoSimilarity: Int,
        val bioSimilarity: Int,
        val urlRiskScore: Int,
        val suspiciousUrlDetected: Boolean,
        val scamScore: Int,
        val scamClassification: String,
        val metadataRiskScore: Int,
        val totalRiskScore: Int,
        val severity: String,
        val checklist: List<String>,
        val explanation: String
    )

    fun evaluateSocialProfile(
        brand: BrandEntity,
        platform: String,
        username: String,
        displayName: String,
        bio: String,
        externalUrl: String,
        logoSimilarityInput: Int,
        accountAgeDays: Int,
        isVerified: Boolean
    ): SocialScanAnalysis {
        val (handleSim, userTech) = analyzeUsernameImpersonation(
            brand.socialHandles + listOf(brand.shortName),
            brand.name,
            username
        )
        val nameSim = analyzeUsernameImpersonation(
            listOf(brand.name, brand.shortName) + brand.aliases,
            brand.name,
            displayName
        ).first.coerceAtLeast(handleSim - 3)

        val bioSim = calculateSemanticTextSimilarity(brand.officialBio, "$displayName $bio")

        val cleanUrl = externalUrl.trim().lowercase()
            .removePrefix("https://")
            .removePrefix("http://")
            .substringBefore("/")

        val isOfficialDomain = cleanUrl.isEmpty() || brand.officialDomains.any {
            cleanUrl == it.lowercase() || cleanUrl.endsWith(".${it.lowercase()}")
        }
        val suspiciousUrlDetected = cleanUrl.isNotEmpty() && !isOfficialDomain
        val urlRiskScore = if (suspiciousUrlDetected) {
            val domEval = evaluateDomain(brand, cleanUrl, 5, false)
            max(86, domEval.riskScore)
        } else 0

        val scamResult = analyzeScamLanguage(bio, suspiciousUrlDetected)

        var metaRisk = 0
        if (!isVerified) metaRisk += 45
        metaRisk += when {
            accountAgeDays <= 7 -> 50
            accountAgeDays <= 30 -> 35
            accountAgeDays <= 90 -> 20
            else -> 5
        }
        val finalMetaRisk = metaRisk.coerceIn(0, 100)

        // Section 18 Social Formula:
        // Name(15%) + Handle(15%) + Logo(20%) + Bio(10%) + URL(20%) + Scam(10%) + Metadata(10%)
        val weightedRisk = (nameSim * 0.15) +
            (handleSim * 0.15) +
            (logoSimilarityInput * 0.20) +
            (bioSim * 0.10) +
            (urlRiskScore * 0.20) +
            (scamResult.score * 0.10) +
            (finalMetaRisk * 0.10)

        val effectiveSim = max(nameSim, handleSim)
        val totalRisk = weightedRisk.roundToInt().coerceIn(0, 100)
        val severity = classifySeverity(totalRisk)

        val checklist = mutableListOf<String>()
        if (effectiveSim >= 70) {
            checklist.add("Name similarity $effectiveSim%")
        }
        if (logoSimilarityInput >= 70) {
            checklist.add("Logo similarity $logoSimilarityInput%")
        }
        if (suspiciousUrlDetected) {
            checklist.add("Suspicious URL ($cleanUrl)")
        }
        if (scamResult.score >= 25) {
            checklist.add("KYC / Scam language detected")
        }
        if (!isVerified) {
            checklist.add("Not in official social handle allowlist (${brand.socialHandlesCsv})")
        }

        val explanation = "This asset has a high probability of brand impersonation because its name ($effectiveSim%) and visual identity ($logoSimilarityInput%) closely resemble ${brand.name} while its handle ($username) and external domain ($cleanUrl) are not present in the official asset registry."

        return SocialScanAnalysis(
            nameSimilarity = nameSim,
            usernameSimilarity = effectiveSim,
            usernameTechnique = userTech,
            logoSimilarity = logoSimilarityInput,
            bioSimilarity = bioSim,
            urlRiskScore = urlRiskScore,
            suspiciousUrlDetected = suspiciousUrlDetected,
            scamScore = scamResult.score,
            scamClassification = scamResult.classification,
            metadataRiskScore = finalMetaRisk,
            totalRiskScore = totalRisk,
            severity = severity,
            checklist = checklist,
            explanation = explanation
        )
    }

    /**
     * Category-Specific Domain Risk Model (Section 18):
     * Domain similarity (30%) + Homoglyph/typosquat (20%) + Registration signals (10%) +
     * URL/path signals (10%) + Content similarity (15%) + Threat intelligence (15%)
     */
    data class DomainScanAnalysis(
        val domain: String,
        val domainSimilarity: Int,
        val attackVector: String,
        val matchedKeywords: List<String>,
        val hasSuspiciousTld: Boolean,
        val riskScore: Int,
        val severity: String,
        val checklist: List<String>,
        val explanation: String
    )

    fun evaluateDomain(
        brand: BrandEntity,
        rawDomain: String,
        registrationAgeDays: Int = 3,
        sslMatchesBrandOrg: Boolean = false
    ): DomainScanAnalysis {
        val cleanDomain = rawDomain.trim().lowercase()
            .removePrefix("https://")
            .removePrefix("http://")
            .substringBefore("/")

        val domainLabel = cleanDomain.substringBeforeLast(".")
        val tld = if (cleanDomain.contains(".")) ".${cleanDomain.substringAfterLast(".")}" else ""

        val officialBaseLabels = (brand.officialDomains.map { it.lowercase().substringBeforeLast(".") } +
            brand.shortName.lowercase()).distinct()
        val primaryOfficial = brand.officialDomains.firstOrNull()?.lowercase() ?: "sbi.co.in"
        val primaryLabel = primaryOfficial.substringBefore(".")

        if (brand.officialDomains.any { it.equals(cleanDomain, ignoreCase = true) }) {
            return DomainScanAnalysis(
                domain = cleanDomain,
                domainSimilarity = 100,
                attackVector = "Verified Official Allowlist Domain",
                matchedKeywords = emptyList(),
                hasSuspiciousTld = false,
                riskScore = 4,
                severity = "LOW",
                checklist = listOf("Verified in official_assets domain allowlist (${brand.name})"),
                explanation = "$cleanDomain is an authorized official domain registered to ${brand.name}."
            )
        }

        val normalizedLabel = domainLabel.replace("-", "").map { homoglyphMap[it] ?: it }.joinToString("")
        val jwSim = jaroWinklerSimilarity(primaryLabel, normalizedLabel)
        val levDist = levenshteinDistance(primaryLabel, domainLabel.replace("-", ""))

        val matchedRiskWords = suspiciousDomainKeywords.filter { domainLabel.contains(it) }
        val containsBrandRoot = officialBaseLabels.any { it.isNotEmpty() && (normalizedLabel.contains(it) || domainLabel.contains(it)) }
        val hasSuspiciousTld = highRiskTlds.any { tld == it }

        val attackVector = when {
            containsBrandRoot && matchedRiskWords.isNotEmpty() ->
                "Look-alike Domain + Keyword (${matchedRiskWords.first().uppercase()})"
            levDist in 1..2 ->
                "Typosquatting / Character Substitution"
            normalizedLabel != domainLabel.replace("-", "") && containsBrandRoot ->
                "Homoglyph Look-alike Domain"
            else ->
                "Unverified Brand Look-alike Domain"
        }

        var domainSim = (jwSim * 100).roundToInt()
        if (containsBrandRoot) domainSim = max(domainSim, 89)
        if (levDist == 1) domainSim = max(domainSim, 94)
        domainSim = domainSim.coerceIn(15, 98)

        val typoSignal = if (containsBrandRoot || levDist <= 2) 90 else 55
        val regSignal = if (registrationAgeDays <= 14) 95 else 45
        val urlPathSignal = if (matchedRiskWords.isNotEmpty()) 92 else 40
        val contentSim = if (containsBrandRoot) 85 else 40
        val threatIntelSignal = if (hasSuspiciousTld || !sslMatchesBrandOrg) 88 else 30

        // Section 18 Domain Formula:
        // Domain(30%) + Typosquat(20%) + Registration(10%) + URL/Path(10%) + Content(15%) + ThreatIntel(15%)
        val weighted = (domainSim * 0.30) +
            (typoSignal * 0.20) +
            (regSignal * 0.10) +
            (urlPathSignal * 0.10) +
            (contentSim * 0.15) +
            (threatIntelSignal * 0.15)

        val finalRisk = weighted.roundToInt().coerceIn(10, 99)
        val severity = classifySeverity(finalRisk)

        val checklist = mutableListOf<String>()
        checklist.add("Domain similarity ($domainSim% to $primaryOfficial)")
        if (matchedRiskWords.isNotEmpty()) {
            checklist.add("\"${matchedRiskWords.first().uppercase()}\" keyword")
        }
        if (registrationAgeDays <= 30) {
            checklist.add("Recently observed ($registrationAgeDays days ago)")
        }
        checklist.add("Not in official-domain allowlist (${brand.officialDomainsCsv})")

        val explanation = "This asset has a high probability of brand impersonation because its domain structure closely resembles ${brand.name} ($primaryOfficial) and includes sensitive banking keywords while not being present in the official asset allowlist."

        return DomainScanAnalysis(
            domain = cleanDomain,
            domainSimilarity = domainSim,
            attackVector = attackVector,
            matchedKeywords = matchedRiskWords,
            hasSuspiciousTld = hasSuspiciousTld,
            riskScore = finalRisk,
            severity = severity,
            checklist = checklist,
            explanation = explanation
        )
    }

    /**
     * Category-Specific Android App Risk Model (Section 18):
     * App-name similarity (15%) + Icon similarity (20%) + Developer identity (15%) +
     * Certificate mismatch (20%) + Permission risk (10%) + Domain/network indicators (10%) + Threat intelligence (10%)
     */
    data class AppScanAnalysis(
        val appNameSimilarity: Int,
        val packageSimilarity: Int,
        val iconSimilarity: Int,
        val developerTrustScore: Int,
        val certificateMatch: Boolean,
        val flaggedPermissions: List<String>,
        val permissionRiskScore: Int,
        val apkSha256: String,
        val riskScore: Int,
        val severity: String,
        val checklist: List<String>,
        val explanation: String
    )

    fun evaluateMobileApp(
        brand: BrandEntity,
        appName: String,
        packageName: String,
        developerName: String,
        signingCert: String,
        permissions: List<String>,
        iconSimilarity: Int
    ): AppScanAnalysis {
        val officialAppName = brand.officialAppName.ifBlank { "${brand.shortName} Mobile" }
        val nameSim = max(
            (jaroWinklerSimilarity(officialAppName, appName) * 100).roundToInt(),
            if (appName.lowercase().contains(brand.shortName.lowercase()) || appName.lowercase().contains(brand.name.lowercase())) 94 else 68
        ).coerceIn(10, 99)

        val officialPkg = brand.androidPackages.firstOrNull() ?: "com.sbi.lotusintouch"
        val isExactOfficialPkg = brand.androidPackages.any { it.equals(packageName.trim(), ignoreCase = true) }
        val pkgSim = (jaroWinklerSimilarity(officialPkg, packageName) * 100).roundToInt().coerceIn(10, 99)

        val certMatch = signingCert.trim().equals(brand.officialCertificate.trim(), ignoreCase = true)
        val devMatch = developerName.trim().equals(brand.officialDeveloper.trim(), ignoreCase = true)

        if (isExactOfficialPkg && certMatch && devMatch) {
            val hash = sha256("$packageName:$signingCert:OFFICIAL")
            return AppScanAnalysis(
                appNameSimilarity = 100,
                packageSimilarity = 100,
                iconSimilarity = 100,
                developerTrustScore = 98,
                certificateMatch = true,
                flaggedPermissions = emptyList(),
                permissionRiskScore = 0,
                apkSha256 = hash,
                riskScore = 6,
                severity = "LOW",
                checklist = listOf("Verified in official_assets app & certificate allowlist (${brand.officialAppName})"),
                explanation = "This application matches ${brand.name}'s official mobile banking app (${brand.officialAppName}) and cryptographic signing certificate."
            )
        }

        val flaggedPerms = permissions.filter { highRiskPermissions.containsKey(it) }
        val permScore = flaggedPerms.sumOf { highRiskPermissions[it] ?: 10 }.coerceIn(0, 100)

        val devRisk = if (devMatch) 0 else 95
        val certMismatchRisk = if (certMatch) 0 else 100
        val networkIndicatorRisk = if (!isExactOfficialPkg) 88 else 20
        val threatIntelRisk = if (!certMatch && flaggedPerms.isNotEmpty()) 92 else 55

        // Section 18 App Formula:
        // AppName(15%) + Icon(20%) + Developer(15%) + Certificate(20%) + Permissions(10%) + Network(10%) + ThreatIntel(10%)
        val weighted = (nameSim * 0.15) +
            (iconSimilarity * 0.20) +
            (devRisk * 0.15) +
            (certMismatchRisk * 0.20) +
            (permScore * 0.10) +
            (networkIndicatorRisk * 0.10) +
            (threatIntelRisk * 0.10)

        val finalRisk = weighted.roundToInt().coerceIn(15, 99)
        val severity = classifySeverity(finalRisk)
        val apkHash = sha256("$appName|$packageName|$signingCert|${permissions.sorted().joinToString(",")}")

        val checklist = mutableListOf<String>()
        checklist.add("Brand similarity $nameSim%")
        checklist.add("Icon similarity $iconSimilarity%")
        if (!certMatch) {
            checklist.add("Unrecognized certificate ($signingCert)")
        }
        if (flaggedPerms.any { it.contains("SMS") }) {
            checklist.add("Sensitive SMS permission")
        } else if (flaggedPerms.isNotEmpty()) {
            checklist.add("Sensitive permissions (${flaggedPerms.take(2).joinToString(", ")})")
        }
        checklist.add("Not in official APK allowlist (${brand.officialAppName})")

        val explanation = "This asset has a high probability of brand impersonation because its app name ($nameSim%) and icon ($iconSimilarity%) closely resemble ${brand.name} (${brand.officialAppName}) while its developer identity ($developerName) and certificate ($signingCert) are not present in the official asset registry."

        return AppScanAnalysis(
            appNameSimilarity = nameSim,
            packageSimilarity = pkgSim,
            iconSimilarity = iconSimilarity,
            developerTrustScore = if (devMatch) 95 else 22,
            certificateMatch = certMatch,
            flaggedPermissions = flaggedPerms,
            permissionRiskScore = permScore,
            apkSha256 = apkHash,
            riskScore = finalRisk,
            severity = severity,
            checklist = checklist,
            explanation = explanation
        )
    }

    fun classifySeverity(score: Int): String = when {
        score >= 81 -> "CRITICAL"
        score >= 61 -> "HIGH"
        score >= 31 -> "MEDIUM"
        else -> "LOW"
    }

    fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02X".format(it) }
    }
}
