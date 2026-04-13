package composestudio.ui.palette

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import composestudio.model.ComponentCategory
import composestudio.model.ComponentType
import composestudio.ui.theme.StudioColors

/**
 * Component palette panel showing available components organized by category.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ComponentPalette(
    onComponentSelected: (ComponentType) -> Unit,
    onDragStart: (ComponentType, Offset) -> Unit,
    onDrag: (ComponentType, Offset) -> Unit,
    onDragEnd: (ComponentType, Offset) -> Unit,
    onDragCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(StudioColors.Surface)
            .verticalScroll(rememberScrollState())
            .padding(8.dp)
    ) {
        Text(
            text = "Components",
            color = StudioColors.OnSurface,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
        )

        ComponentCategory.entries.forEach { category ->
            val componentsInCategory = ComponentType.entries.filter { it.category == category }

            Text(
                text = category.displayName,
                color = StudioColors.OnSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 6.dp, start = 4.dp, top = 8.dp)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                componentsInCategory.forEach { componentType ->
                    PaletteItem(
                        componentType = componentType,
                        onClick = { onComponentSelected(componentType) },
                        onDragStart = onDragStart,
                        onDrag = onDrag,
                        onDragEnd = onDragEnd,
                        onDragCancel = onDragCancel
                    )
                }
            }
        }
    }
}

@Composable
private fun PaletteItem(
    componentType: ComponentType,
    onClick: () -> Unit,
    onDragStart: (ComponentType, Offset) -> Unit,
    onDrag: (ComponentType, Offset) -> Unit,
    onDragEnd: (ComponentType, Offset) -> Unit,
    onDragCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var itemWindowPosition by remember { mutableStateOf(Offset.Zero) }
    var currentPointerWindowPosition by remember { mutableStateOf(Offset.Zero) }
    var dragStarted by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .size(72.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(StudioColors.SurfaceVariant)
            .border(1.dp, StudioColors.Border, RoundedCornerShape(8.dp))
            .onGloballyPositioned { coordinates ->
                itemWindowPosition = coordinates.positionInWindow()
            }
            .pointerInput(componentType) {
                detectTapGestures(onTap = { onClick() })
            }
            .pointerInput(componentType) {
                detectDragGestures(
                    onDragStart = { offset ->
                        dragStarted = true
                        currentPointerWindowPosition = itemWindowPosition + offset
                        onDragStart(componentType, currentPointerWindowPosition)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        currentPointerWindowPosition += dragAmount
                        onDrag(componentType, currentPointerWindowPosition)
                    },
                    onDragEnd = {
                        if (dragStarted) onDragEnd(componentType, currentPointerWindowPosition)
                        dragStarted = false
                    },
                    onDragCancel = {
                        dragStarted = false
                        onDragCancel()
                    }
                )
            }
            .padding(4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = componentType.icon,
                contentDescription = componentType.displayName,
                tint = StudioColors.Primary,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = componentType.displayName,
            color = StudioColors.OnSurface,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private val ComponentType.icon: ImageVector
    get() = when (this) {
        ComponentType.Text -> Icons.Default.TextFields
        ComponentType.Button -> Icons.Default.SmartButton
        ComponentType.TextField -> Icons.Default.Edit
        ComponentType.Image -> Icons.Default.Image
        ComponentType.Checkbox -> Icons.Default.CheckBox
        ComponentType.Switch -> Icons.Default.ToggleOn
        ComponentType.Card -> Icons.Default.CropLandscape
        ComponentType.Column -> Icons.Default.ViewColumn
        ComponentType.Row -> Icons.Default.TableRows
        ComponentType.Box -> Icons.Default.ViewInAr
        ComponentType.Scaffold -> Icons.Default.CropLandscape
        ComponentType.Spacer -> Icons.Default.SpaceBar
        ComponentType.Divider -> Icons.Default.HorizontalRule
        ComponentType.Slider -> Icons.Default.LinearScale
        ComponentType.IconButton -> Icons.Default.Star
        ComponentType.FloatingActionButton -> Icons.Default.Add
        ComponentType.LinearProgressIndicator -> Icons.Default.HorizontalRule
        ComponentType.CircularProgressIndicator -> Icons.Default.RadioButtonChecked
    }
