package com.example.alfajr.core.background

/**
 * Identifier for Al Fajr watchface background themes.
 */
enum class BackgroundThemeId(val displayName: String) {
    CLASSIC_BLACK("Classic Black"),
    RAMADAN("Ramadan"),
    EID("Eid al-Fitr"),
    DAWN("Al Fajr Dawn"),
    NIGHT("Night Sky")
}

sealed interface BackgroundAsset {
    data class PureColor(
        val colorHex: Long = 0xFF000000
    ) : BackgroundAsset
}

data class WatchBackgroundTheme(
    val id: BackgroundThemeId,
    val name: String,
    val asset: BackgroundAsset
)

interface BackgroundProvider {
    fun getDefaultTheme(): WatchBackgroundTheme
    fun getAvailableThemes(): List<WatchBackgroundTheme>
    fun getThemeById(id: BackgroundThemeId): WatchBackgroundTheme
}

class DefaultBackgroundProvider : BackgroundProvider {

    private val classicBlackTheme = WatchBackgroundTheme(
        id = BackgroundThemeId.CLASSIC_BLACK,
        name = "Classic Black",
        asset = BackgroundAsset.PureColor(0xFF000000)
    )

    override fun getDefaultTheme(): WatchBackgroundTheme = classicBlackTheme

    override fun getAvailableThemes(): List<WatchBackgroundTheme> {
        return listOf(classicBlackTheme)
    }

    override fun getThemeById(id: BackgroundThemeId): WatchBackgroundTheme {
        return classicBlackTheme
    }
}
