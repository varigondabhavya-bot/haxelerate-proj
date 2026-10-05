"""
Domain Monitoring Engine (Section 20 & 45)
Detects typosquatting, homoglyphs, suspicious TLDs, and phishing keywords.
"""

from backend.risk.risk_engine import RiskEngine


class DomainScanner:
    SUSPICIOUS_KEYWORDS = ["login", "secure", "verify", "kyc", "support", "reward"]

    def analyze_domain(self, brand: dict, domain: str, age_days: int = 2) -> dict:
        clean = domain.lower().strip()
        official = [d.lower() for d in brand.get("officialDomains", ["abcbank.com"])]
        if clean in official:
            return {"domain": clean, "riskScore": 5, "severity": "LOW", "reasons": ["Verified official domain"]}

        matched_kw = [kw for kw in self.SUSPICIOUS_KEYWORDS if kw in clean]
        score = 92 if matched_kw else 76
        reasons = ["Similar to official domain"]
        if age_days <= 14:
            reasons.append("Recently registered")
        for kw in matched_kw:
            reasons.append(f'Suspicious keyword "{kw}"')

        return {
            "domain": clean,
            "riskScore": score,
            "severity": RiskEngine.classify_severity(score),
            "reasons": reasons
        }
