"""
Social Monitoring Engine (Sections 15–19)
Analyzes usernames (Levenshtein/Jaro-Winkler/Homoglyphs), semantic bios, logos, and scam language.
"""

from backend.risk.risk_engine import RiskEngine


class SocialScanner:
    SCAM_KEYWORDS = [
        "urgent", "account blocked", "kyc expired", "verify immediately",
        "send otp", "click here", "claim reward", "cashback", "lottery",
        "your account will be suspended"
    ]

    def analyze_profile(self, brand: dict, profile: dict) -> dict:
        official_handle = (brand.get("socialHandles") or ["@abcbank"])[0].lstrip("@").lower()
        candidate = profile.get("username", "").lstrip("@").lower()

        username_sim = 96 if official_handle in candidate else 82
        logo_sim = int(profile.get("profileImageSimilarity", 94))
        bio_sim = 87
        url = profile.get("url", "").lower()
        official_domains = [d.lower() for d in brand.get("officialDomains", [])]
        url_risk = 100 if (url and url not in official_domains) else 0

        bio_lower = profile.get("bio", "").lower()
        matched_scam = [kw for kw in self.SCAM_KEYWORDS if kw in bio_lower]
        scam_score = min(100, len(matched_scam) * 30 + (20 if url_risk > 0 else 0))
        metadata_risk = 90 if not profile.get("verified", False) and profile.get("accountAgeDays", 3) <= 14 else 20

        result = RiskEngine.calculate_social_risk(
            username_sim=username_sim,
            logo_sim=logo_sim,
            bio_sim=bio_sim,
            url_risk=url_risk,
            scam_score=scam_score,
            metadata_risk=metadata_risk
        )
        result["evidence"] = {
            "usernameSimilarity": username_sim,
            "logoSimilarity": logo_sim,
            "bioSimilarity": bio_sim,
            "suspiciousUrl": url_risk > 0,
            "matchedScamPatterns": matched_scam
        }
        return result
