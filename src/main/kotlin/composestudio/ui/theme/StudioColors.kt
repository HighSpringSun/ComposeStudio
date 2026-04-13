package composestudio.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Application color scheme - dark theme optimized for design tools.
 */
object StudioColors {
    val Background = Color(0xFF1E1E1E)
    val Surface = Color(0xFF252526)
    val SurfaceVariant = Color(0xFF2D2D30)
    val SurfaceBright = Color(0xFF333337)
    val Primary = Color(0xFF007ACC)
    val PrimaryVariant = Color(0xFF0098FF)
    val Secondary = Color(0xFF569CD6)
    val OnBackground = Color(0xFFD4D4D4)
    val OnSurface = Color(0xFFCCCCCC)
    val OnSurfaceVariant = Color(0xFF969696)
    val OnPrimary = Color(0xFFFFFFFF)
    val Accent = Color(0xFF4EC9B0)
    val Warning = Color(0xFFDCDCAA)
    val Error = Color(0xFFF44747)
    val CanvasBackground = Color(0xFF1A1A2E)
    val CanvasGrid = Color(0xFF2A2A4E)
    val SelectionBorder = Color(0xFF007ACC)
    val ResizeHandle = Color(0xFFFFFFFF)
    val DropTarget = Color(0x3300FF00)
    val DragPreview = Color(0x55007ACC)
    val Border = Color(0xFF3F3F46)
    val PanelHeader = Color(0xFF2D2D30)

    val DarkColorScheme = darkColorScheme(
        primary = Primary,
        onPrimary = OnPrimary,
        secondary = Secondary,
        background = Background,
        surface = Surface,
        surfaceVariant = SurfaceVariant,
        onBackground = OnBackground,
        onSurface = OnSurface,
        onSurfaceVariant = OnSurfaceVariant,
        error = Error
    )
}
