package composestudio.ui.codegen

import composestudio.model.ComponentType
import composestudio.model.DesignComponent
import composestudio.model.DesignState
import composestudio.model.PropertyValue

/**
 * Generates Compose Multiplatform Kotlin code from the current design state.
 */
object CodeGenerator {

    fun generate(designState: DesignState): String {
        val roots = designState.rootComponentIds.mapNotNull { designState.components[it] }
        val sb = StringBuilder()

        sb.appendLine("@Composable")
        sb.appendLine("fun DesignedScreen() {")

        when {
            roots.isEmpty() -> {
                sb.appendLine("    Box(modifier = Modifier.fillMaxSize())")
            }
            roots.size == 1 -> {
                generateComponent(
                    component = roots.first(),
                    designState = designState,
                    sb = sb,
                    indent = 4,
                    isRoot = true
                )
            }
            else -> {
                sb.appendLine("    Column(")
                sb.appendLine("        verticalArrangement = Arrangement.spacedBy(12.dp),")
                sb.appendLine("        modifier = Modifier.fillMaxSize().padding(16.dp)")
                sb.appendLine("    ) {")
                roots.forEach { root ->
                    generateComponent(root, designState, sb, indent = 8)
                }
                sb.appendLine("    }")
            }
        }

        sb.appendLine("}")
        return buildImports() + "\n\n" + sb.toString()
    }

    private fun generateComponent(
        component: DesignComponent,
        designState: DesignState,
        sb: StringBuilder,
        indent: Int,
        isRoot: Boolean = false
    ) {
        val pad = " ".repeat(indent)
        val props = component.properties
        val children = designState.childComponents(component.id, "content")

        when (component.type) {
            ComponentType.Text -> {
                val text = (props["text"] as? PropertyValue.StringValue)?.value ?: "Text"
                val fontSize = (props["fontSize"] as? PropertyValue.IntValue)?.value ?: 16
                val fontWeight = (props["fontWeight"] as? PropertyValue.ChoiceValue)?.selected ?: "Normal"
                val color = (props["color"] as? PropertyValue.ColorValue)?.hex ?: "#000000"
                val textAlign = (props["textAlign"] as? PropertyValue.ChoiceValue)?.selected ?: "Start"

                sb.appendLine("${pad}Text(")
                sb.appendLine("${pad}    text = \"${escapeString(text)}\",")
                sb.appendLine("${pad}    fontSize = ${fontSize}.sp,")
                if (fontWeight != "Normal") sb.appendLine("${pad}    fontWeight = FontWeight.$fontWeight,")
                if (textAlign != "Start") sb.appendLine("${pad}    textAlign = TextAlign.$textAlign,")
                if (color != "#000000") sb.appendLine("${pad}    color = ${colorLiteral(color)},")
                sb.appendLine("${pad}    modifier = ${generateModifier(component, isRoot)}")
                sb.appendLine("${pad})")
            }

            ComponentType.Button -> {
                val text = (props["text"] as? PropertyValue.StringValue)?.value ?: "Button"
                val enabled = (props["enabled"] as? PropertyValue.BooleanValue)?.value ?: true

                sb.appendLine("${pad}Button(")
                sb.appendLine("${pad}    onClick = { /* TODO */ },")
                if (!enabled) sb.appendLine("${pad}    enabled = false,")
                sb.appendLine("${pad}    modifier = ${generateModifier(component, isRoot)}")
                sb.appendLine("${pad}) {")
                sb.appendLine("${pad}    Text(\"${escapeString(text)}\")")
                sb.appendLine("${pad}}")
            }

            ComponentType.TextField -> {
                val placeholder = (props["placeholder"] as? PropertyValue.StringValue)?.value ?: ""
                val label = (props["label"] as? PropertyValue.StringValue)?.value ?: ""
                val enabled = (props["enabled"] as? PropertyValue.BooleanValue)?.value ?: true
                val singleLine = (props["singleLine"] as? PropertyValue.BooleanValue)?.value ?: true

                sb.appendLine("${pad}var textFieldValue by remember { mutableStateOf(\"\") }")
                sb.appendLine("${pad}OutlinedTextField(")
                sb.appendLine("${pad}    value = textFieldValue,")
                sb.appendLine("${pad}    onValueChange = { textFieldValue = it },")
                if (label.isNotEmpty()) sb.appendLine("${pad}    label = { Text(\"${escapeString(label)}\") },")
                if (placeholder.isNotEmpty()) sb.appendLine("${pad}    placeholder = { Text(\"${escapeString(placeholder)}\") },")
                if (!enabled) sb.appendLine("${pad}    enabled = false,")
                sb.appendLine("${pad}    singleLine = $singleLine,")
                sb.appendLine("${pad}    modifier = ${generateModifier(component, isRoot)}")
                sb.appendLine("${pad})")
            }

            ComponentType.Image -> {
                val contentDescription = (props["contentDescription"] as? PropertyValue.StringValue)?.value ?: "Image"
                val contentScale = (props["contentScale"] as? PropertyValue.ChoiceValue)?.selected ?: "Fit"

                sb.appendLine("${pad}Image(")
                sb.appendLine("${pad}    painter = painterResource(\"image.png\"),")
                sb.appendLine("${pad}    contentDescription = \"${escapeString(contentDescription)}\",")
                sb.appendLine("${pad}    contentScale = ContentScale.$contentScale,")
                sb.appendLine("${pad}    modifier = ${generateModifier(component, isRoot)}")
                sb.appendLine("${pad})")
            }

            ComponentType.Checkbox -> {
                val label = (props["label"] as? PropertyValue.StringValue)?.value ?: "Checkbox"
                val checked = (props["checked"] as? PropertyValue.BooleanValue)?.value ?: false
                val enabled = (props["enabled"] as? PropertyValue.BooleanValue)?.value ?: true

                sb.appendLine("${pad}var checkboxChecked by remember { mutableStateOf($checked) }")
                sb.appendLine("${pad}Row(")
                sb.appendLine("${pad}    verticalAlignment = Alignment.CenterVertically,")
                sb.appendLine("${pad}    modifier = ${generateModifier(component, isRoot)}")
                sb.appendLine("${pad}) {")
                sb.appendLine("${pad}    Checkbox(")
                sb.appendLine("${pad}        checked = checkboxChecked,")
                if (!enabled) sb.appendLine("${pad}        enabled = false,")
                sb.appendLine("${pad}        onCheckedChange = { checkboxChecked = it }")
                sb.appendLine("${pad}    )")
                sb.appendLine("${pad}    Spacer(Modifier.width(4.dp))")
                sb.appendLine("${pad}    Text(\"${escapeString(label)}\")")
                sb.appendLine("${pad}}")
            }

            ComponentType.Switch -> {
                val label = (props["label"] as? PropertyValue.StringValue)?.value ?: "Switch"
                val checked = (props["checked"] as? PropertyValue.BooleanValue)?.value ?: false
                val enabled = (props["enabled"] as? PropertyValue.BooleanValue)?.value ?: true

                sb.appendLine("${pad}var switchChecked by remember { mutableStateOf($checked) }")
                sb.appendLine("${pad}Row(")
                sb.appendLine("${pad}    verticalAlignment = Alignment.CenterVertically,")
                sb.appendLine("${pad}    modifier = ${generateModifier(component, isRoot)}")
                sb.appendLine("${pad}) {")
                sb.appendLine("${pad}    Text(\"${escapeString(label)}\")")
                sb.appendLine("${pad}    Spacer(Modifier.width(8.dp))")
                sb.appendLine("${pad}    Switch(")
                sb.appendLine("${pad}        checked = switchChecked,")
                if (!enabled) sb.appendLine("${pad}        enabled = false,")
                sb.appendLine("${pad}        onCheckedChange = { switchChecked = it }")
                sb.appendLine("${pad}    )")
                sb.appendLine("${pad}}")
            }

            ComponentType.Card -> {
                val elevation = (props["elevation"] as? PropertyValue.IntValue)?.value ?: 4
                val cornerRadius = (props["cornerRadius"] as? PropertyValue.IntValue)?.value ?: 12

                sb.appendLine("${pad}ElevatedCard(")
                sb.appendLine("${pad}    elevation = CardDefaults.elevatedCardElevation(defaultElevation = ${elevation}.dp),")
                sb.appendLine("${pad}    shape = RoundedCornerShape(${cornerRadius}.dp),")
                sb.appendLine("${pad}    modifier = ${generateModifier(component, isRoot)}")
                sb.appendLine("${pad}) {")
                if (children.isEmpty()) {
                    sb.appendLine("${pad}    // Card content")
                } else {
                    sb.appendLine("${pad}    Column(")
                    sb.appendLine("${pad}        verticalArrangement = Arrangement.spacedBy(8.dp),")
                    sb.appendLine("${pad}        modifier = Modifier.padding(16.dp)")
                    sb.appendLine("${pad}    ) {")
                    children.forEach { child -> generateComponent(child, designState, sb, indent + 8) }
                    sb.appendLine("${pad}    }")
                }
                sb.appendLine("${pad}}")
            }

            ComponentType.Column -> {
                val spacing = (props["spacing"] as? PropertyValue.IntValue)?.value ?: 8
                val alignment = when ((props["horizontalAlignment"] as? PropertyValue.ChoiceValue)?.selected) {
                    "CenterHorizontally" -> "Alignment.CenterHorizontally"
                    "End" -> "Alignment.End"
                    else -> "Alignment.Start"
                }

                sb.appendLine("${pad}Column(")
                sb.appendLine("${pad}    verticalArrangement = Arrangement.spacedBy(${spacing}.dp),")
                sb.appendLine("${pad}    horizontalAlignment = $alignment,")
                sb.appendLine("${pad}    modifier = ${generateModifier(component, isRoot)}")
                sb.appendLine("${pad}) {")
                if (children.isEmpty()) {
                    sb.appendLine("${pad}    // Add children here")
                } else {
                    children.forEach { child -> generateComponent(child, designState, sb, indent + 4) }
                }
                sb.appendLine("${pad}}")
            }

            ComponentType.Row -> {
                val spacing = (props["spacing"] as? PropertyValue.IntValue)?.value ?: 8
                val alignment = when ((props["verticalAlignment"] as? PropertyValue.ChoiceValue)?.selected) {
                    "CenterVertically" -> "Alignment.CenterVertically"
                    "Bottom" -> "Alignment.Bottom"
                    else -> "Alignment.Top"
                }

                sb.appendLine("${pad}Row(")
                sb.appendLine("${pad}    horizontalArrangement = Arrangement.spacedBy(${spacing}.dp),")
                sb.appendLine("${pad}    verticalAlignment = $alignment,")
                sb.appendLine("${pad}    modifier = ${generateModifier(component, isRoot)}")
                sb.appendLine("${pad}) {")
                if (children.isEmpty()) {
                    sb.appendLine("${pad}    // Add children here")
                } else {
                    children.forEach { child -> generateComponent(child, designState, sb, indent + 4) }
                }
                sb.appendLine("${pad}}")
            }

            ComponentType.Box -> {
                val alignment = when ((props["contentAlignment"] as? PropertyValue.ChoiceValue)?.selected) {
                    "TopCenter" -> "Alignment.TopCenter"
                    "TopEnd" -> "Alignment.TopEnd"
                    "CenterStart" -> "Alignment.CenterStart"
                    "Center" -> "Alignment.Center"
                    "CenterEnd" -> "Alignment.CenterEnd"
                    "BottomStart" -> "Alignment.BottomStart"
                    "BottomCenter" -> "Alignment.BottomCenter"
                    "BottomEnd" -> "Alignment.BottomEnd"
                    else -> "Alignment.TopStart"
                }

                sb.appendLine("${pad}Box(")
                sb.appendLine("${pad}    contentAlignment = $alignment,")
                sb.appendLine("${pad}    modifier = ${generateModifier(component, isRoot)}")
                sb.appendLine("${pad}) {")
                if (children.isEmpty()) {
                    sb.appendLine("${pad}    // Add children here")
                } else {
                    children.forEach { child -> generateComponent(child, designState, sb, indent + 4) }
                }
                sb.appendLine("${pad}}")
            }

            ComponentType.Scaffold -> {
                val topBarChildren = designState.childComponents(component.id, "topBar")
                val contentChildren = designState.childComponents(component.id, "content")
                val bottomBarChildren = designState.childComponents(component.id, "bottomBar")
                val fabChildren = designState.childComponents(component.id, "floatingActionButton")

                sb.appendLine("${pad}Scaffold(")
                if (topBarChildren.isNotEmpty()) {
                    sb.appendLine("${pad}    topBar = {")
                    generateSlotContent(topBarChildren, designState, sb, indent + 8)
                    sb.appendLine("${pad}    },")
                }
                if (bottomBarChildren.isNotEmpty()) {
                    sb.appendLine("${pad}    bottomBar = {")
                    generateSlotContent(bottomBarChildren, designState, sb, indent + 8)
                    sb.appendLine("${pad}    },")
                }
                if (fabChildren.isNotEmpty()) {
                    sb.appendLine("${pad}    floatingActionButton = {")
                    generateSlotContent(fabChildren, designState, sb, indent + 8)
                    sb.appendLine("${pad}    },")
                }
                sb.appendLine("${pad}    modifier = ${generateModifier(component, isRoot)}")
                sb.appendLine("${pad}) { innerPadding ->")
                sb.appendLine("${pad}    Box(")
                sb.appendLine("${pad}        modifier = Modifier.fillMaxSize().padding(innerPadding)")
                sb.appendLine("${pad}    ) {")
                if (contentChildren.isEmpty()) {
                    sb.appendLine("${pad}        // Add Scaffold content here")
                } else {
                    contentChildren.forEach { child -> generateComponent(child, designState, sb, indent + 8) }
                }
                sb.appendLine("${pad}    }")
                sb.appendLine("${pad}}")
            }

            ComponentType.Spacer -> {
                sb.appendLine("${pad}Spacer(modifier = ${generateModifier(component, isRoot)})")
            }

            ComponentType.Divider -> {
                val thickness = (props["thickness"] as? PropertyValue.IntValue)?.value ?: 1
                val color = (props["color"] as? PropertyValue.ColorValue)?.hex ?: "#CCCCCC"

                sb.appendLine("${pad}HorizontalDivider(")
                sb.appendLine("${pad}    thickness = ${thickness}.dp,")
                sb.appendLine("${pad}    color = ${colorLiteral(color)},")
                sb.appendLine("${pad}    modifier = ${generateModifier(component, isRoot)}")
                sb.appendLine("${pad})")
            }

            ComponentType.Slider -> {
                val value = (props["value"] as? PropertyValue.FloatValue)?.value ?: 0.5f
                val enabled = (props["enabled"] as? PropertyValue.BooleanValue)?.value ?: true

                sb.appendLine("${pad}var sliderValue by remember { mutableStateOf(${value}f) }")
                sb.appendLine("${pad}Slider(")
                sb.appendLine("${pad}    value = sliderValue,")
                sb.appendLine("${pad}    onValueChange = { sliderValue = it },")
                if (!enabled) sb.appendLine("${pad}    enabled = false,")
                sb.appendLine("${pad}    modifier = ${generateModifier(component, isRoot)}")
                sb.appendLine("${pad})")
            }

            ComponentType.IconButton -> {
                val icon = (props["icon"] as? PropertyValue.ChoiceValue)?.selected ?: "Star"
                val enabled = (props["enabled"] as? PropertyValue.BooleanValue)?.value ?: true

                sb.appendLine("${pad}IconButton(")
                sb.appendLine("${pad}    onClick = { /* TODO */ },")
                if (!enabled) sb.appendLine("${pad}    enabled = false,")
                sb.appendLine("${pad}    modifier = ${generateModifier(component, isRoot)}")
                sb.appendLine("${pad}) {")
                sb.appendLine("${pad}    Icon(Icons.Default.$icon, contentDescription = null)")
                sb.appendLine("${pad}}")
            }

            ComponentType.FloatingActionButton -> {
                val icon = (props["icon"] as? PropertyValue.ChoiceValue)?.selected ?: "Add"

                sb.appendLine("${pad}FloatingActionButton(")
                sb.appendLine("${pad}    onClick = { /* TODO */ },")
                sb.appendLine("${pad}    modifier = ${generateModifier(component, isRoot)}")
                sb.appendLine("${pad}) {")
                sb.appendLine("${pad}    Icon(Icons.Default.$icon, contentDescription = null)")
                sb.appendLine("${pad}}")
            }

            ComponentType.LinearProgressIndicator -> {
                val progress = (props["progress"] as? PropertyValue.FloatValue)?.value ?: 0.6f

                sb.appendLine("${pad}LinearProgressIndicator(")
                sb.appendLine("${pad}    progress = { ${progress}f },")
                sb.appendLine("${pad}    modifier = ${generateModifier(component, isRoot)}")
                sb.appendLine("${pad})")
            }

            ComponentType.CircularProgressIndicator -> {
                val progress = (props["progress"] as? PropertyValue.FloatValue)?.value ?: 0.6f

                sb.appendLine("${pad}CircularProgressIndicator(")
                sb.appendLine("${pad}    progress = { ${progress}f },")
                sb.appendLine("${pad}    modifier = ${generateModifier(component, isRoot)}")
                sb.appendLine("${pad})")
            }
        }
    }

    private fun generateSlotContent(
        components: List<DesignComponent>,
        designState: DesignState,
        sb: StringBuilder,
        indent: Int
    ) {
        if (components.size == 1) {
            generateComponent(components.first(), designState, sb, indent)
            return
        }

        val pad = " ".repeat(indent)
        sb.appendLine("${pad}Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {")
        components.forEach { component ->
            generateComponent(component, designState, sb, indent + 4)
        }
        sb.appendLine("${pad}}")
    }

    private fun generateModifier(component: DesignComponent, isRoot: Boolean): String {
        return if (isRoot && component.type.supportsChildren) {
            "Modifier.fillMaxSize()"
        } else {
            "Modifier.size(width = ${component.size.width.toInt()}.dp, height = ${component.size.height.toInt()}.dp)"
        }
    }

    private fun buildImports(): String {
        return listOf(
            "import androidx.compose.foundation.Image",
            "import androidx.compose.foundation.layout.*",
            "import androidx.compose.foundation.shape.RoundedCornerShape",
            "import androidx.compose.material.icons.Icons",
            "import androidx.compose.material.icons.filled.*",
            "import androidx.compose.material3.*",
            "import androidx.compose.runtime.*",
            "import androidx.compose.ui.Alignment",
            "import androidx.compose.ui.Modifier",
            "import androidx.compose.ui.graphics.Color",
            "import androidx.compose.ui.layout.ContentScale",
            "import androidx.compose.ui.res.painterResource",
            "import androidx.compose.ui.text.font.FontWeight",
            "import androidx.compose.ui.text.style.TextAlign",
            "import androidx.compose.ui.unit.dp",
            "import androidx.compose.ui.unit.sp"
        ).joinToString("\n")
    }

    private fun colorLiteral(hex: String): String {
        val clean = hex.removePrefix("#").uppercase()
        return when (clean.length) {
            6 -> "Color(0xFF$clean)"
            8 -> "Color(0x$clean)"
            else -> "Color.Unspecified"
        }
    }

    private fun escapeString(value: String): String {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
    }
}
