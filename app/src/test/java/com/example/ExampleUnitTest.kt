package com.example

import com.example.data.models.BrandEntity
import com.example.domain.engine.RiskDetectionEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    private val sbiBrand = BrandEntity(
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
        officialBio = "Official account of State Bank of India. Download YONO SBI only from Google Play.",
        keywordsCsv = "YONO SBI, OnlineSBI"
    )

    private val hdfcBrand = BrandEntity(
        brandId = "HDFC",
        name = "HDFC Bank",
        shortName = "HDFC Bank",
        officialAppName = "HDFC Bank MobileBanking",
        aliasesCsv = "HDFC, HDFC Bank, PayZapp",
        officialDomainsCsv = "hdfcbank.com, netbanking.hdfcbank.com",
        socialHandlesCsv = "@HDFC_Bank",
        androidPackagesCsv = "com.snapwork.hdfc",
        officialDeveloper = "HDFC Bank Ltd.",
        officialCertificate = "HDFCBank_Official_Cert_SHA256",
        officialBio = "Official HDFC Bank social channel.",
        keywordsCsv = "HDFC, PayZapp"
    )

    @Test
    fun socialRiskFormula_detectsSbiHelpdeskImpersonation() {
        val eval = RiskDetectionEngine.evaluateSocialProfile(
            brand = sbiBrand,
            platform = "Instagram",
            username = "@sbi_helpdesk_test01",
            displayName = "SBI Customer Assistance",
            bio = "Official SBI support. Urgent: Your KYC expired, verify immediately and send OTP!",
            externalUrl = "sbi-yono-kyc-update.test",
            logoSimilarityInput = 97,
            accountAgeDays = 2,
            isVerified = false
        )
        assertTrue("Expected Critical Risk Score >= 81, got ${eval.totalRiskScore}", eval.totalRiskScore >= 81)
        assertEquals("CRITICAL", eval.severity)
        assertTrue(eval.suspiciousUrlDetected)
    }

    @Test
    fun domainScanner_detectsControlledTestDomainAndAllowsOfficialSbi() {
        val fakeEval = RiskDetectionEngine.evaluateDomain(
            brand = sbiBrand,
            rawDomain = "sbi-yono-kyc-update.test",
            registrationAgeDays = 2,
            sslMatchesBrandOrg = false
        )
        assertTrue(fakeEval.riskScore >= 81)
        assertEquals("CRITICAL", fakeEval.severity)

        val officialEval = RiskDetectionEngine.evaluateDomain(
            brand = sbiBrand,
            rawDomain = "sbi.co.in",
            registrationAgeDays = 3650,
            sslMatchesBrandOrg = true
        )
        assertEquals("LOW", officialEval.severity)
    }

    @Test
    fun mobileAppScanner_detectsHdfcDemoApkAndUnrecognizedCert() {
        val appEval = RiskDetectionEngine.evaluateMobileApp(
            brand = hdfcBrand,
            appName = "HDFC Mobile Secure",
            packageName = "com.demo.hdfc.securebank",
            developerName = "Unverified Publisher",
            signingCert = "UnrecognizedDemoCert_SHA256",
            permissions = listOf("READ_SMS", "RECEIVE_SMS", "BIND_ACCESSIBILITY_SERVICE"),
            iconSimilarity = 91
        )
        assertFalse(appEval.certificateMatch)
        assertTrue(appEval.riskScore >= 81)
        assertEquals("CRITICAL", appEval.severity)
    }
}
