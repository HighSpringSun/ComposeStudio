package composestudio.ui.palette

import androidx.compose.ui.geometry.Offset
import composestudio.model.ComponentType

data class PaletteDragSession(
    val componentType: ComponentType,
    val positionInWindow: Offset,
    val isDragging: Boolean,
    val dropPending: Boolean = false
)

