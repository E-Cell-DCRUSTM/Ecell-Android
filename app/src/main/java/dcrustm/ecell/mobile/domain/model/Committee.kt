package dcrustm.ecell.mobile.domain.model

enum class Committee(val displayName: String, val shortName: String) {
    EVENTS("Events & Operations", "Events"),
    CORPORATE("Corporate Relations & Partnerships", "Corporate"),
    MARKETING("Marketing, PR & Outreach", "Marketing"),
    DESIGN("Design & Creatives", "Design"),
    SOCIAL_MEDIA("Social Media & Video Editing", "Social Media"),
    STARTUP("Startup Development & Ideation", "Startup"),
    WEB_TECH("Web & Technology", "Web & Tech"),
    STRATEGY("Strategy, Research & Documentation", "Strategy"),
    NONE("—", "—")
}
