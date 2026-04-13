package composestudio.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import composestudio.model.ComponentType
import composestudio.model.DesignComponent
import composestudio.model.DesignState
import composestudio.model.PropertyValue
import composestudio.ui.palette.PaletteDragSession
import composestudio.ui.theme.StudioColors

/**
 * The main design canvas where components are placed and manipulated.
 */
@Composable
fun DesignCanvas(
    designState: DesignState,
    placingComponentType: ComponentType?,
    paletteDragSession: PaletteDragSession?,
    onComponentPlaced: () -> Unit,
    onPaletteDropHandled: () -> Unit,
    modifier: Modifier = Modifier
) {
    val componentBounds = remember { mutableMapOf<String, Rect>() }
    var canvasBounds by remember { mutableStateOf<Rect?>(null) }

    fun placeComponentFromCanvasPosition(type: ComponentType, localOffset: Offset, windowOffset: Offset) {
        val dropTarget = resolveDropTarget(designState, componentBounds, windowOffset)
        if (dropTarget == null) {
            designState.addComponent(
                type = type,
                position = Offset(
                    x = (localOffset.x - type.defaultSize.width / 2).coerceAtLeast(0f),
                    y = (localOffset.y - type.defaultSize.height / 2).coerceAtLeast(0f)
                )
            )
        } else {
            designState.addComponent(
                type = type,
                position = Offset.Zero,
                parentId = dropTarget.parentId,
                childSlot = dropTarget.childSlot
            )
        }
    }

    androidx.compose.runtime.LaunchedEffect(paletteDragSession) {
        val session = paletteDragSession ?: return@LaunchedEffect
        val bounds = canvasBounds ?: return@LaunchedEffect
        if (!session.dropPending) return@LaunchedEffect

        if (bounds.contains(session.positionInWindow)) {
            val localOffset = session.positionInWindow - bounds.topLeft
            placeComponentFromCanvasPosition(session.componentType, localOffset, session.positionInWindow)
        }

        onPaletteDropHandled()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(StudioColors.CanvasBackground)
            .onGloballyPositioned { canvasBounds = it.boundsInWindow() }
            .pointerInput(placingComponentType) {
                detectTapGestures { offset ->
                    val windowOffset = canvasBounds?.topLeft?.plus(offset) ?: offset
                    if (placingComponentType != null) {
                        placeComponentFromCanvasPosition(placingComponentType, offset, windowOffset)
                        onComponentPlaced()
                    } else {
                        val clickedOnComponent = componentBounds.values.any { it.contains(windowOffset) }
                        if (!clickedOnComponent) {
                            designState.selectComponent(null)
                        }
                    }
                }
            }
    ) {
        // Draw grid
        GridBackground()

        // Placement indicator
        if (placingComponentType != null) {
            PlacementIndicator()
        }

        // Render all components
        designState.rootComponentIds.forEach { id ->
            val component = designState.components[id] ?: return@forEach
            CanvasComponent(
                component = component,
                isSelected = component.id == designState.selectedComponentId,
                designState = designState,
                componentBounds = componentBounds
            )
        }
    }
}

@Composable
private fun GridBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val gridSize = 20f
        val color = StudioColors.CanvasGrid

        var x = 0f
        while (x < size.width) {
            drawLine(
                color = color,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 0.5f
            )
            x += gridSize
        }

        var y = 0f
        while (y < size.height) {
            drawLine(
                color = color,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 0.5f
            )
            y += gridSize
        }
    }
}

@Composable
private fun PlacementIndicator() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Click to place component",
            color = StudioColors.OnSurfaceVariant,
            fontSize = 14.sp
        )
    }
}

/**
 * Renders a single component on the canvas with selection and drag handles.
 */
@Composable
private fun CanvasComponent(
    component: DesignComponent,
    isSelected: Boolean,
    designState: DesignState,
    componentBounds: MutableMap<String, Rect>
) {
    var dragOffset by remember(component.id) { mutableStateOf(Offset.Zero) }
    val isRoot = component.parentId == null

    Box(
        modifier = Modifier
            .then(
                if (isRoot) {
                    Modifier.offset {
                        IntOffset(
                            (component.position.x + dragOffset.x).toInt(),
                            (component.position.y + dragOffset.y).toInt()
                        )
                    }
                } else {
                    Modifier
                }
            )
            .size(
                width = component.size.width.dp,
                height = component.size.height.dp
            )
            .onGloballyPositioned { coordinates ->
                componentBounds[component.id] = coordinates.boundsInWindow()
            }
    ) {
        val interactionModifier = Modifier
            .fillMaxSize()
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = StudioColors.SelectionBorder,
                        shape = RoundedCornerShape(4.dp)
                    )
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = StudioColors.Border.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(4.dp)
                    )
                }
            )
            .clip(RoundedCornerShape(4.dp))
            .background(StudioColors.Surface.copy(alpha = 0.9f))
            .pointerInput(component.id) {
                detectTapGestures {
                    designState.selectComponent(component.id)
                }
            }
            .then(
                if (isRoot) {
                    Modifier.pointerInput(component.id) {
                        detectDragGestures(
                            onDragStart = {
                                designState.selectComponent(component.id)
                                dragOffset = Offset.Zero
                            },
                            onDrag = { change, drag ->
                                change.consume()
                                dragOffset += drag
                            },
                            onDragEnd = {
                                designState.moveComponent(
                                    component.id,
                                    component.position + dragOffset
                                )
                                dragOffset = Offset.Zero
                            },
                            onDragCancel = {
                                dragOffset = Offset.Zero
                            }
                        )
                    }
                } else {
                    Modifier
                }
            )
            .padding(4.dp)

        Box(modifier = interactionModifier) {
            ComponentPreview(component, designState, componentBounds)
        }

        if (isSelected && isRoot) {
            ResizeHandles(component, designState)
        }
    }
}

/**
 * Renders a preview of the component based on its type and properties.
 */
@Composable
private fun ComponentPreview(
    component: DesignComponent,
    designState: DesignState,
    componentBounds: MutableMap<String, Rect>
) {
    val props = component.properties

    when (component.type) {
        ComponentType.Text -> {
            val text = (props["text"] as? PropertyValue.StringValue)?.value ?: "Text"
            val fontSize = (props["fontSize"] as? PropertyValue.IntValue)?.value ?: 16
            val fontWeight = when ((props["fontWeight"] as? PropertyValue.ChoiceValue)?.selected) {
                "Bold" -> FontWeight.Bold
                "Light" -> FontWeight.Light
                "Medium" -> FontWeight.Medium
                "SemiBold" -> FontWeight.SemiBold
                "ExtraBold" -> FontWeight.ExtraBold
                else -> FontWeight.Normal
            }
            val textAlign = when ((props["textAlign"] as? PropertyValue.ChoiceValue)?.selected) {
                "Center" -> TextAlign.Center
                "End" -> TextAlign.End
                else -> TextAlign.Start
            }
            Text(
                text = text,
                fontSize = fontSize.sp,
                fontWeight = fontWeight,
                textAlign = textAlign,
                color = parseColor((props["color"] as? PropertyValue.ColorValue)?.hex ?: "#D4D4D4"),
                modifier = Modifier.padding(4.dp)
            )
        }

        ComponentType.Button -> {
            val text = (props["text"] as? PropertyValue.StringValue)?.value ?: "Button"
            Button(
                onClick = {},
                enabled = (props["enabled"] as? PropertyValue.BooleanValue)?.value ?: true,
                colors = ButtonDefaults.buttonColors(
                    containerColor = StudioColors.Primary
                ),
                modifier = Modifier.padding(2.dp)
            ) {
                Text(text, fontSize = 12.sp)
            }
        }

        ComponentType.TextField -> {
            val placeholder = (props["placeholder"] as? PropertyValue.StringValue)?.value ?: ""
            val label = (props["label"] as? PropertyValue.StringValue)?.value ?: ""
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = if (label.isNotEmpty()) { { Text(label, fontSize = 11.sp) } } else null,
                placeholder = { Text(placeholder, fontSize = 11.sp) },
                enabled = (props["enabled"] as? PropertyValue.BooleanValue)?.value ?: true,
                singleLine = (props["singleLine"] as? PropertyValue.BooleanValue)?.value ?: true,
                modifier = Modifier.fillMaxSize().padding(2.dp)
            )
        }

        ComponentType.Image -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(StudioColors.SurfaceVariant)
                    .padding(8.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        tint = StudioColors.OnSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = (props["contentDescription"] as? PropertyValue.StringValue)?.value ?: "Image",
                        color = StudioColors.OnSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }
        }

        ComponentType.Checkbox -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(4.dp)
            ) {
                Checkbox(
                    checked = (props["checked"] as? PropertyValue.BooleanValue)?.value ?: false,
                    onCheckedChange = null,
                    enabled = (props["enabled"] as? PropertyValue.BooleanValue)?.value ?: true
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = (props["label"] as? PropertyValue.StringValue)?.value ?: "Checkbox",
                    color = StudioColors.OnSurface,
                    fontSize = 12.sp
                )
            }
        }

        ComponentType.Switch -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(4.dp)
            ) {
                Text(
                    text = (props["label"] as? PropertyValue.StringValue)?.value ?: "Switch",
                    color = StudioColors.OnSurface,
                    fontSize = 12.sp
                )
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = (props["checked"] as? PropertyValue.BooleanValue)?.value ?: false,
                    onCheckedChange = null,
                    enabled = (props["enabled"] as? PropertyValue.BooleanValue)?.value ?: true
                )
            }
        }

        ComponentType.Card -> {
            ElevatedCard(
                elevation = CardDefaults.elevatedCardElevation(
                    defaultElevation = ((props["elevation"] as? PropertyValue.IntValue)?.value ?: 4).dp
                ),
                shape = RoundedCornerShape(
                    ((props["cornerRadius"] as? PropertyValue.IntValue)?.value ?: 12).dp
                ),
                modifier = Modifier.fillMaxSize().padding(2.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    Text("Card", color = StudioColors.OnSurfaceVariant, fontSize = 10.sp)
                    Spacer(Modifier.height(8.dp))
                    LayoutChildren(
                        designState = designState,
                        parent = component,
                        componentBounds = componentBounds,
                        slot = "content"
                    )
                }
            }
        }

        ComponentType.Column -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .dashedBorder(StudioColors.Secondary.copy(alpha = 0.5f))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(
                    ((props["spacing"] as? PropertyValue.IntValue)?.value ?: 8).dp
                ),
                horizontalAlignment = when ((props["horizontalAlignment"] as? PropertyValue.ChoiceValue)?.selected) {
                    "CenterHorizontally" -> Alignment.CenterHorizontally
                    "End" -> Alignment.End
                    else -> Alignment.Start
                }
            ) {
                Text("Column", color = StudioColors.Secondary, fontSize = 10.sp)
                LayoutChildren(
                    designState = designState,
                    parent = component,
                    componentBounds = componentBounds,
                    slot = "content"
                )
            }
        }

        ComponentType.Row -> {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .dashedBorder(StudioColors.Secondary.copy(alpha = 0.5f))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(
                    ((props["spacing"] as? PropertyValue.IntValue)?.value ?: 8).dp
                ),
                verticalAlignment = when ((props["verticalAlignment"] as? PropertyValue.ChoiceValue)?.selected) {
                    "CenterVertically" -> Alignment.CenterVertically
                    "Bottom" -> Alignment.Bottom
                    else -> Alignment.Top
                }
            ) {
                Text("Row", color = StudioColors.Secondary, fontSize = 10.sp)
                LayoutChildren(
                    designState = designState,
                    parent = component,
                    componentBounds = componentBounds,
                    slot = "content"
                )
            }
        }

        ComponentType.Box -> {
            Box(
                contentAlignment = when ((props["contentAlignment"] as? PropertyValue.ChoiceValue)?.selected) {
                    "TopCenter" -> Alignment.TopCenter
                    "TopEnd" -> Alignment.TopEnd
                    "CenterStart" -> Alignment.CenterStart
                    "Center" -> Alignment.Center
                    "CenterEnd" -> Alignment.CenterEnd
                    "BottomStart" -> Alignment.BottomStart
                    "BottomCenter" -> Alignment.BottomCenter
                    "BottomEnd" -> Alignment.BottomEnd
                    else -> Alignment.TopStart
                },
                modifier = Modifier
                    .fillMaxSize()
                    .dashedBorder(StudioColors.Accent.copy(alpha = 0.5f))
                    .padding(8.dp)
            ) {
                if (designState.childComponents(component.id).isEmpty()) {
                    Text("Box", color = StudioColors.Accent, fontSize = 10.sp)
                }
                LayoutChildren(
                    designState = designState,
                    parent = component,
                    componentBounds = componentBounds,
                    slot = "content"
                )
            }
        }

        ComponentType.Scaffold -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(StudioColors.SurfaceBright)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    ScaffoldSlot(
                        title = "Top Bar",
                        designState = designState,
                        parent = component,
                        componentBounds = componentBounds,
                        slot = "topBar",
                        minHeight = 52.dp,
                        paddingValues = PaddingValues(6.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .dashedBorder(StudioColors.Secondary.copy(alpha = 0.35f))
                            .padding(8.dp)
                    ) {
                        LayoutChildren(
                            designState = designState,
                            parent = component,
                            componentBounds = componentBounds,
                            slot = "content",
                            placeholder = "Drop into Scaffold content"
                        )
                    }
                    ScaffoldSlot(
                        title = "Bottom Bar",
                        designState = designState,
                        parent = component,
                        componentBounds = componentBounds,
                        slot = "bottomBar",
                        minHeight = 52.dp,
                        paddingValues = PaddingValues(6.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                ) {
                    LayoutChildren(
                        designState = designState,
                        parent = component,
                        componentBounds = componentBounds,
                        slot = "floatingActionButton",
                        placeholder = "FAB"
                    )
                }
            }
        }

        ComponentType.Spacer -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .dashedBorder(StudioColors.OnSurfaceVariant.copy(alpha = 0.3f))
            ) {
                Text("Spacer", color = StudioColors.OnSurfaceVariant, fontSize = 9.sp)
            }
        }

        ComponentType.Divider -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                HorizontalDivider(
                    thickness = ((props["thickness"] as? PropertyValue.IntValue)?.value ?: 1).dp,
                    color = parseColor((props["color"] as? PropertyValue.ColorValue)?.hex ?: "#CCCCCC")
                )
            }
        }

        ComponentType.Slider -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)
            ) {
                Slider(
                    value = (props["value"] as? PropertyValue.FloatValue)?.value ?: 0.5f,
                    onValueChange = {},
                    enabled = (props["enabled"] as? PropertyValue.BooleanValue)?.value ?: true
                )
            }
        }

        ComponentType.IconButton -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = getIconByName((props["icon"] as? PropertyValue.ChoiceValue)?.selected ?: "Star"),
                        contentDescription = null,
                        tint = StudioColors.OnSurface
                    )
                }
            }
        }

        ComponentType.FloatingActionButton -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                FloatingActionButton(
                    onClick = {},
                    containerColor = StudioColors.Primary
                ) {
                    Icon(
                        imageVector = getIconByName((props["icon"] as? PropertyValue.ChoiceValue)?.selected ?: "Add"),
                        contentDescription = null,
                        tint = StudioColors.OnPrimary
                    )
                }
            }
        }

        ComponentType.LinearProgressIndicator -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)
            ) {
                LinearProgressIndicator(
                    progress = { (props["progress"] as? PropertyValue.FloatValue)?.value ?: 0.6f },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        ComponentType.CircularProgressIndicator -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                CircularProgressIndicator(
                    progress = { (props["progress"] as? PropertyValue.FloatValue)?.value ?: 0.6f }
                )
            }
        }
    }
}

@Composable
private fun LayoutChildren(
    designState: DesignState,
    parent: DesignComponent,
    componentBounds: MutableMap<String, Rect>,
    slot: String,
    placeholder: String = "Drop components here"
) {
    val children = designState.childComponents(parent.id, slot)

    if (children.isEmpty()) {
        Text(
            text = placeholder,
            color = StudioColors.OnSurfaceVariant,
            fontSize = 9.sp
        )
        return
    }

    children.forEach { child ->
        CanvasComponent(
            component = child,
            isSelected = child.id == designState.selectedComponentId,
            designState = designState,
            componentBounds = componentBounds
        )
    }
}

@Composable
private fun ScaffoldSlot(
    title: String,
    designState: DesignState,
    parent: DesignComponent,
    componentBounds: MutableMap<String, Rect>,
    slot: String,
    minHeight: androidx.compose.ui.unit.Dp,
    paddingValues: PaddingValues
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .background(StudioColors.SurfaceVariant)
            .padding(paddingValues)
    ) {
        val children = designState.childComponents(parent.id, slot)
        if (children.isEmpty()) {
            Text(
                text = title,
                color = StudioColors.OnSurfaceVariant,
                fontSize = 9.sp,
                modifier = Modifier.align(Alignment.CenterStart)
            )
        } else {
            children.forEach { child ->
                CanvasComponent(
                    component = child,
                    isSelected = child.id == designState.selectedComponentId,
                    designState = designState,
                    componentBounds = componentBounds
                )
            }
        }
    }
}

private data class DropTarget(val parentId: String, val childSlot: String)

private fun resolveDropTarget(
    designState: DesignState,
    componentBounds: Map<String, Rect>,
    windowPosition: Offset
): DropTarget? {
    val candidates = designState.components.values
        .filter { it.type.supportsChildren }
        .mapNotNull { component ->
            val bounds = componentBounds[component.id] ?: return@mapNotNull null
            if (!bounds.contains(windowPosition)) return@mapNotNull null
            Triple(component, bounds, designState.componentDepth(component.id))
        }
        .sortedWith(compareByDescending<Triple<DesignComponent, Rect, Int>> { it.third }.thenBy { it.second.width * it.second.height })

    val target = candidates.firstOrNull() ?: return null
    val slot = when (target.first.type) {
        ComponentType.Scaffold -> resolveScaffoldSlot(target.second, windowPosition)
        else -> "content"
    }
    return DropTarget(target.first.id, slot)
}

private fun resolveScaffoldSlot(bounds: Rect, windowPosition: Offset): String {
    val relativeY = (windowPosition.y - bounds.top) / bounds.height
    val relativeX = (windowPosition.x - bounds.left) / bounds.width

    return when {
        relativeY <= 0.16f -> "topBar"
        relativeY >= 0.84f && relativeX >= 0.7f -> "floatingActionButton"
        relativeY >= 0.84f -> "bottomBar"
        else -> "content"
    }
}

/**
 * Resize handles shown at corners and edges of selected components.
 */
@Composable
private fun ResizeHandles(
    component: DesignComponent,
    designState: DesignState
) {
    val handleSize = 8.dp
    val handles = listOf(
        Alignment.BottomEnd
    )

    handles.forEach { alignment ->
        var resizeDrag by remember { mutableStateOf(Offset.Zero) }

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .align(alignment)
                    .offset(x = handleSize / 2, y = handleSize / 2)
                    .size(handleSize)
                    .clip(CircleShape)
                    .background(StudioColors.ResizeHandle)
                    .border(1.dp, StudioColors.SelectionBorder, CircleShape)
                    .pointerInput(component.id) {
                        detectDragGestures(
                            onDragStart = { resizeDrag = Offset.Zero },
                            onDrag = { _, drag ->
                                resizeDrag += drag
                            },
                            onDragEnd = {
                                designState.resizeComponent(
                                    component.id,
                                    Size(
                                        component.size.width + resizeDrag.x,
                                        component.size.height + resizeDrag.y
                                    )
                                )
                                resizeDrag = Offset.Zero
                            },
                            onDragCancel = {
                                resizeDrag = Offset.Zero
                            }
                        )
                    }
            )
        }
    }
}

private fun Modifier.dashedBorder(color: Color): Modifier {
    return this.then(
        Modifier.border(
            width = 1.dp,
            color = color,
            shape = RoundedCornerShape(4.dp)
        )
    )
}

private fun parseColor(hex: String): Color {
    return try {
        val cleanHex = hex.removePrefix("#")
        when (cleanHex.length) {
            6 -> Color(("FF$cleanHex").toLong(16))
            8 -> Color(cleanHex.toLong(16))
            else -> Color.White
        }
    } catch (_: Exception) {
        Color.White
    }
}

private fun getIconByName(name: String): ImageVector {
    return when (name) {
        "Star" -> Icons.Default.Star
        "Home" -> Icons.Default.Home
        "Settings" -> Icons.Default.Settings
        "Search" -> Icons.Default.Search
        "Add" -> Icons.Default.Add
        "Delete" -> Icons.Default.Delete
        "Edit" -> Icons.Default.Edit
        "Favorite" -> Icons.Default.Favorite
        "Share" -> Icons.Default.Share
        "Menu" -> Icons.Default.Menu
        else -> Icons.Default.Star
    }
}
