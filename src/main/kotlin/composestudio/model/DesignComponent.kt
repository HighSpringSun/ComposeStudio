package composestudio.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

/**
 * Represents a UI component placed on the design canvas.
 */
data class DesignComponent(
    val id: String,
    val type: ComponentType,
    val position: Offset = Offset.Zero,
    val size: Size = type.defaultSize,
    val properties: Map<String, PropertyValue> = type.defaultProperties,
    val children: List<String> = emptyList(),
    val parentId: String? = null
)

/**
 * Available component types in the palette.
 */
enum class ComponentType(
    val displayName: String,
    val category: ComponentCategory,
    val defaultSize: Size,
    val defaultProperties: Map<String, PropertyValue>,
    val supportsChildren: Boolean = false
) {
    Text(
        displayName = "Text",
        category = ComponentCategory.Basic,
        defaultSize = Size(120f, 40f),
        defaultProperties = mapOf(
            "text" to PropertyValue.StringValue("Text"),
            "fontSize" to PropertyValue.IntValue(16),
            "fontWeight" to PropertyValue.ChoiceValue("Normal", listOf("Normal", "Bold", "Light", "Medium", "SemiBold", "ExtraBold")),
            "color" to PropertyValue.ColorValue("#000000"),
            "textAlign" to PropertyValue.ChoiceValue("Start", listOf("Start", "Center", "End"))
        )
    ),
    Button(
        displayName = "Button",
        category = ComponentCategory.Basic,
        defaultSize = Size(140f, 48f),
        defaultProperties = mapOf(
            "text" to PropertyValue.StringValue("Button"),
            "enabled" to PropertyValue.BooleanValue(true)
        )
    ),
    TextField(
        displayName = "TextField",
        category = ComponentCategory.Input,
        defaultSize = Size(200f, 56f),
        defaultProperties = mapOf(
            "placeholder" to PropertyValue.StringValue("Enter text..."),
            "label" to PropertyValue.StringValue("Label"),
            "enabled" to PropertyValue.BooleanValue(true),
            "singleLine" to PropertyValue.BooleanValue(true)
        )
    ),
    Image(
        displayName = "Image",
        category = ComponentCategory.Basic,
        defaultSize = Size(150f, 150f),
        defaultProperties = mapOf(
            "contentDescription" to PropertyValue.StringValue("Image"),
            "contentScale" to PropertyValue.ChoiceValue("Fit", listOf("Fit", "Crop", "FillBounds", "FillWidth", "FillHeight", "Inside", "None"))
        )
    ),
    Checkbox(
        displayName = "Checkbox",
        category = ComponentCategory.Input,
        defaultSize = Size(160f, 40f),
        defaultProperties = mapOf(
            "label" to PropertyValue.StringValue("Checkbox"),
            "checked" to PropertyValue.BooleanValue(false),
            "enabled" to PropertyValue.BooleanValue(true)
        )
    ),
    Switch(
        displayName = "Switch",
        category = ComponentCategory.Input,
        defaultSize = Size(160f, 40f),
        defaultProperties = mapOf(
            "label" to PropertyValue.StringValue("Switch"),
            "checked" to PropertyValue.BooleanValue(false),
            "enabled" to PropertyValue.BooleanValue(true)
        )
    ),
    Card(
        displayName = "Card",
        category = ComponentCategory.Layout,
        defaultSize = Size(250f, 180f),
        defaultProperties = mapOf(
            "elevation" to PropertyValue.IntValue(4),
            "cornerRadius" to PropertyValue.IntValue(12)
        ),
        supportsChildren = true
    ),
    Column(
        displayName = "Column",
        category = ComponentCategory.Layout,
        defaultSize = Size(200f, 300f),
        defaultProperties = mapOf(
            "horizontalAlignment" to PropertyValue.ChoiceValue("Start", listOf("Start", "CenterHorizontally", "End")),
            "verticalArrangement" to PropertyValue.ChoiceValue("Top", listOf("Top", "Center", "Bottom", "SpaceBetween", "SpaceAround", "SpaceEvenly")),
            "spacing" to PropertyValue.IntValue(8)
        ),
        supportsChildren = true
    ),
    Row(
        displayName = "Row",
        category = ComponentCategory.Layout,
        defaultSize = Size(300f, 60f),
        defaultProperties = mapOf(
            "verticalAlignment" to PropertyValue.ChoiceValue("Top", listOf("Top", "CenterVertically", "Bottom")),
            "horizontalArrangement" to PropertyValue.ChoiceValue("Start", listOf("Start", "Center", "End", "SpaceBetween", "SpaceAround", "SpaceEvenly")),
            "spacing" to PropertyValue.IntValue(8)
        ),
        supportsChildren = true
    ),
    Box(
        displayName = "Box",
        category = ComponentCategory.Layout,
        defaultSize = Size(200f, 200f),
        defaultProperties = mapOf(
            "contentAlignment" to PropertyValue.ChoiceValue("TopStart", listOf(
                "TopStart", "TopCenter", "TopEnd",
                "CenterStart", "Center", "CenterEnd",
                "BottomStart", "BottomCenter", "BottomEnd"
            ))
        ),
        supportsChildren = true
    ),
    Spacer(
        displayName = "Spacer",
        category = ComponentCategory.Layout,
        defaultSize = Size(50f, 50f),
        defaultProperties = emptyMap()
    ),
    Divider(
        displayName = "Divider",
        category = ComponentCategory.Basic,
        defaultSize = Size(200f, 4f),
        defaultProperties = mapOf(
            "thickness" to PropertyValue.IntValue(1),
            "color" to PropertyValue.ColorValue("#CCCCCC")
        )
    ),
    Slider(
        displayName = "Slider",
        category = ComponentCategory.Input,
        defaultSize = Size(200f, 40f),
        defaultProperties = mapOf(
            "value" to PropertyValue.FloatValue(0.5f),
            "enabled" to PropertyValue.BooleanValue(true)
        )
    ),
    IconButton(
        displayName = "IconButton",
        category = ComponentCategory.Basic,
        defaultSize = Size(48f, 48f),
        defaultProperties = mapOf(
            "icon" to PropertyValue.ChoiceValue("Star", listOf("Star", "Home", "Settings", "Search", "Add", "Delete", "Edit", "Favorite", "Share", "Menu")),
            "enabled" to PropertyValue.BooleanValue(true)
        )
    ),
    FloatingActionButton(
        displayName = "FAB",
        category = ComponentCategory.Basic,
        defaultSize = Size(56f, 56f),
        defaultProperties = mapOf(
            "icon" to PropertyValue.ChoiceValue("Add", listOf("Star", "Home", "Settings", "Search", "Add", "Delete", "Edit", "Favorite", "Share", "Menu"))
        )
    ),
    LinearProgressIndicator(
        displayName = "Progress Bar",
        category = ComponentCategory.Basic,
        defaultSize = Size(200f, 8f),
        defaultProperties = mapOf(
            "progress" to PropertyValue.FloatValue(0.6f)
        )
    ),
    CircularProgressIndicator(
        displayName = "Progress Circle",
        category = ComponentCategory.Basic,
        defaultSize = Size(48f, 48f),
        defaultProperties = mapOf(
            "progress" to PropertyValue.FloatValue(0.6f)
        )
    )
}

/**
 * Categories for organizing components in the palette.
 */
enum class ComponentCategory(val displayName: String) {
    Basic("Basic"),
    Input("Input"),
    Layout("Layout")
}

/**
 * Typed property values for component configuration.
 */
sealed class PropertyValue {
    data class StringValue(val value: String) : PropertyValue()
    data class IntValue(val value: Int) : PropertyValue()
    data class FloatValue(val value: Float) : PropertyValue()
    data class BooleanValue(val value: Boolean) : PropertyValue()
    data class ColorValue(val hex: String) : PropertyValue()
    data class ChoiceValue(val selected: String, val options: List<String>) : PropertyValue()
}
