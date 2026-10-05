"""
BrandShield AI — Risk Engine (Section 29–32)
Calculates weighted 0-100 Risk Scores, Severity Classification, and Explainable AI reasons.
"""


class RiskEngine:
    @staticmethod
    def classify_severity(score: int) -> str:
        if score >= 81:
            return "CRITICAL"
        elif score >= 61:
            return "HIGH"
        elif score >= 31:
            return "MEDIUM"
        return "LOW"

    @classmethod
    def calculate_social_risk(
        cls,
        username_sim: int,
        logo_sim: int,
        bio_sim: int,
        url_risk: int,
        scam_score: int,
        metadata_risk: int
    ) -> dict:
        """
        Formula (Section 30):
        Risk = Username * 0.20 + Logo * 0.25 + Bio * 0.15 + URL * 0.20 + Scam * 0.10 + Metadata * 0.10
        """
        weighted = (
            username_sim * 0.20
            + logo_sim * 0.25
            + bio_sim * 0.15
            + url_risk * 0.20
            + scam_score * 0.10
            + metadata_risk * 0.10
        )
        score = max(0, min(100, round(weighted)))
        severity = cls.classify_severity(score)
        reasons = []
        if username_sim >= 75:
            reasons.append(f"Username is {username_sim}% similar")
        if logo_sim >= 75:
            reasons.append(f"Logo is {logo_sim}% similar")
        if url_risk >= 70:
            reasons.append("Suspicious domain detected")
        if metadata_risk >= 60:
            reasons.append("Account was created recently")
        if scam_score >= 50:
            reasons.append("Scam-related language detected")

        return {
            "riskScore": score,
            "severity": severity,
            "because": reasons
        }
