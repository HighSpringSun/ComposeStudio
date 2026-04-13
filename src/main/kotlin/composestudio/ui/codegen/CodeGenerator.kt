package composestudio.ui.codegen

import composestudio.model.ComponentType
import composestudio.model.DesignComponent
import composestudio.model.DesignState
import composestudio.model.PropertyValue

/**
 * Generates Compose Multiplatform Kotlin code from the current design state.
 */
object CodeGenerator {

    /**
     * Generate the complete Composable function code.
     */
    fun generate(designState: DesignState): String {
        val sb = StringBuilder()
        sb.appendLine("@Composable")
        sb.appendLine("fun DesignedScreen() {")
        sb.appendLine("    Box(")
        sb.appendLine("        modifier = Modifier.fillMaxSize()")
        sb.appendLine("    ) {")

        designState.rootComponentIds.forEach { id ->
            val component = designState.components[id] ?: return@forEach
            generateComponent(component, sb, indent = 8)
        }

        sb.appendLine("    }")
        sb.appendLine("}")

        return buildImports(designState) + "\n\n" + sb.toString()
    }

    private fun generateComponent(
        component: DesignComponent,
        sb: StringBuilder,
        indent: Int
    ) {
        val pad = " ".repeat(indent)
        val props = component.properties

        when (component.type) {
            ComponentType.Text -> {
                val text = (props["text"] as? PropertyValue.StringValue)?.value ?: "Text"
                val fontSize = (props["fontSize"] as? PropertyValue.IntValue)?.value ?: 16
                val fontWeight = (props["fontWeight"] as? PropertyValue.ChoiceValue)?.selected ?: "Normal"
                val color = (props["color"] as? PropertyValue.ColorValue)?.hex ?: "#000000"
                val textAlign = (props["textAlign"] as? PropertyValue.ChoiceValue)?.selected ?: "Start"

                sb.appendLine("${pad}Text(")
                sb.appendLine("${pad}    text = \"$text\",")
                sb.appendLine("${pad}    fontSize = ${fontSize}.sp,")
                if (fontWeight != "Normal") {
                    sb.appendLine("${pad}    fontWeight = FontWeight.$fontWeight,")
                }
                if (textAlign != "Start") {
                    sb.appendLine("${pad}    textAlign = TextAlign.$textAlign,")
                }
                if (color != "#000000") {
                    sb.appendLine("${pad}    color = Color(0xFF${color.removePrefix("#")}),")
                }
                sb.appendLine("${pad}    modifier = ${generateModifier(component)}")
                sb.appendLine("${pad})")
            }

            ComponentType.Button -> {
                val text = (props["text"] as? PropertyValue.StringValue)?.value ?: "Button"
                val enabled = (props["enabled"] as? PropertyValue.BooleanValue)?.value ?: true

                sb.appendLine("${pad}Button(")
                sb.appendLine("${pad}    onClick = { /* TODO */ },")
                if (!enabled) {
                    sb.appendLine("${pad}    enabled = false,")
                }
                sb.appendLine("${pad}    modifier = ${generateModifier(component)}")
                sb.appendLine("${pad}) {")
                sb.appendLine("${pad}    Text(\"$text\")")
                sb.appendLine("${pad}}")
            }

            ComponentType.TextField -> {
                val placeholder = (props["placeholder"] as? PropertyValue.StringValue)?.value ?: ""
                val label = (props["label"] as? PropertyValue.StringValue)?.value ?: ""
                val singleLine = (props["singleLine"] as? PropertyValue.BooleanValue)?.value ?: true

                sb.appendLine("${pad}var textFieldValue by remember { mutableStateOf(\"\") }")
                sb.appendLine("${pad}OutlinedTextField(")
                sb.appendLine("${pad}    value = textFieldValue,")
                sb.appendLine("${pad}    onValueChange = { textFieldValue = it },")
                if (label.isNotEmpty()) {
                    sb.appendLine("${pad}    label = { Text(\"$label\") },")
                }
                if (placeholder.isNotEmpty()) {
                    sb.appendLine("${pad}    placeholder = { Text(\"$placeholder\") },")
                }
                sb.appendLine("${pad}    singleLine = $singleLine,")
                sb.appendLine("${pad}    modifier = ${generateModifier(component)}")
                sb.appendLine("${pad})")
            }

            ComponentType.Image -> {
                val contentDescription = (props["contentDescription"] as? PropertyValue.StringValue)?.value ?: "Image"
                val contentScale = (props["contentScale"] as? PropertyValue.ChoiceValue)?.selected ?: "Fit"

                sb.appendLine("${pad}Image(")
                sb.appendLine("${pad}    painter = painterResource(\"image.png\"), // TODO: Set image resource")
                sb.appendLine("${pad}    contentDescription = \"$contentDescription\",")
                sb.appendLine("${pad}    contentScale = ContentScale.$contentScale,")
                sb.appendLine("${pad}    modifier = ${generateModifier(component)}")
                sb.appendLine("${pad})")
            }

            ComponentType.Checkbox -> {
                val label = (props["label"] as? PropertyValue.StringValue)?.value ?: "Checkbox"
                val checked = (props["checked"] as? PropertyValue.BooleanValue)?.value ?: false

                sb.appendLine("${pad}var checkboxChecked by remember { mutableStateOf($checked) }")
                sb.appendLine("${pad}Row(")
                sb.appendLine("${pad}    verticalAlignment = Alignment.CenterVertically,")
                sb.appendLine("${pad}    modifier = ${generateModifier(component)}")
                sb.appendLine("${pad}) {")
                sb.appendLine("${pad}    Checkbox(")
                sb.appendLine("${pad}        checked = checkboxChecked,")
                sb.appendLine("${pad}        onCheckedChange = { checkboxChecked = it }")
                sb.appendLine("${pad}    )")
                sb.appendLine("${pad}    Spacer(Modifier.width(4.dp))")
                sb.appendLine("${pad}    Text(\"$label\")")
                sb.appendLine("${pad}}")
            }

            ComponentType.Switch -> {
                val label = (props["label"] as? PropertyValue.StringValue)?.value ?: "Switch"
                val checked = (props["checked"] as? PropertyValue.BooleanValue)?.value ?: false

                sb.appendLine("${pad}var switchChecked by remember { mutableStateOf($checked) }")
                sb.appendLine("${pad}Row(")
                sb.appendLine("${pad}    verticalAlignment = Alignment.CenterVertically,")
                sb.appendLine("${pad}    modifier = ${generateModifier(component)}")
                sb.appendLine("${pad}) {")
                sb.appendLine("${pad}    Text(\"$label\")")
                sb.appendLine("${pad}    Spacer(Modifier.width(8.dp))")
                sb.appendLine("${pad}    Switch(")
                sb.appendLine("${pad}        checked = switchChecked,")
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
                sb.appendLine("${pad}    modifier = ${generateModifier(component)}")
                sb.appendLine("${pad}) {")
                sb.appendLine("${pad}    // Card content")
                sb.appendLine("${pad}}")
            }

            ComponentType.Column -> {
                val alignment = (props["horizontalAlignment"] as? PropertyValue.ChoiceValue)?.selected ?: "Start"
                val spacing = (props["spacing"] as? PropertyValue.IntValue)?.value ?: 8

                sb.appendLine("${pad}Column(")
                sb.appendLine("${pad}    horizontalAlignment = Alignment.$alignment,")
                sb.appendLine("${pad}    verticalArrangement = Arrangement.spacedBy(${spacing}.dp),")
                sb.appendLine("${pad}    modifier = ${generateModifier(component)}")
                sb.appendLine("${pad}) {")
                sb.appendLine("${pad}    // Column content")
                sb.appendLine("${pad}}")
            }

            ComponentType.Row -> {
                val alignment = (props["verticalAlignment"] as? PropertyValue.ChoiceValue)?.selected ?: "Top"
                val spacing = (props["spacing"] as? PropertyValue.IntValue)?.value ?: 8

                sb.appendLine("${pad}Row(")
                sb.appendLine("${pad}    verticalAlignment = Alignment.$alignment,")
                sb.appendLine("${pad}    horizontalArrangement = Arrangement.spacedBy(${spacing}.dp),")
                sb.appendLine("${pad}    modifier = ${generateModifier(component)}")
                sb.appendLine("${pad}) {")
                sb.appendLine("${pad}    // Row content")
                sb.appendLine("${pad}}")
            }

            ComponentType.Box -> {
                val alignment = (props["contentAlignment"] as? PropertyValue.ChoiceValue)?.selected ?: "TopStart"

                sb.appendLine("${pad}Box(")
                sb.appendLine("${pad}    contentAlignment = Alignment.$alignment,")
                sb.appendLine("${pad}    modifier = ${generateModifier(component)}")
                sb.appendLine("${pad}) {")
                sb.appendLine("${pad}    // Box content")
                sb.appendLine("${pad}}")
            }

            ComponentType.Spacer -> {
                sb.appendLine("${pad}Spacer(modifier = ${generateModifier(component)})")
            }

            ComponentType.Divider -> {
                val thickness = (props["thickness"] as? PropertyValue.IntValue)?.value ?: 1
                val color = (props["color"] as? PropertyValue.ColorValue)?.hex ?: "#CCCCCC"

                sb.appendLine("${pad}Divider(")
                sb.appendLine("${pad}    thickness = ${thickness}.dp,")
                sb.appendLine("${pad}    color = Color(0xFF${color.removePrefix("#")}),")
                sb.appendLine("${pad}    modifier = ${generateModifier(component)}")
                sb.appendLine("${pad})")
            }

            ComponentType.Slider -> {
                val value = (props["value"] as? PropertyValue.FloatValue)?.value ?: 0.5f

                sb.appendLine("${pad}var sliderValue by remember { mutableStateOf(${value}f) }")
                sb.appendLine("${pad}Slider(")
                sb.appendLine("${pad}    value = sliderValue,")
                sb.appendLine("${pad}    onValueChange = { sliderValue = it },")
                sb.appendLine("${pad}    modifier = ${generateModifier(component)}")
                sb.appendLine("${pad})")
            }

            ComponentType.IconButton -> {
                val icon = (props["icon"] as? PropertyValue.ChoiceValue)?.selected ?: "Star"

                sb.appendLine("${pad}IconButton(")
                sb.appendLine("${pad}    onClick = { /* TODO */ },")
                sb.appendLine("${pad}    modifier = ${generateModifier(component)}")
                sb.appendLine("${pad}) {")
                sb.appendLine("${pad}    Icon(Icons.Default.$icon, contentDescription = null)")
                sb.appendLine("${pad}}")
            }

            ComponentType.FloatingActionButton -> {
                val icon = (props["icon"] as? PropertyValue.ChoiceValue)?.selected ?: "Add"

                sb.appendLine("${pad}FloatingActionButton(")
                sb.appendLine("${pad}    onClick = { /* TODO */ },")
                sb.appendLine("${pad}    modifier = ${generateModifier(component)}")
                sb.appendLine("${pad}) {")
                sb.appendLine("${pad}    Icon(Icons.Default.$icon, contentDescription = null)")
                sb.appendLine("${pad}}")
            }

            ComponentType.LinearProgressIndicator -> {
                val progress = (props["progress"] as? PropertyValue.FloatValue)?.value ?: 0.6f

                sb.appendLine("${pad}LinearProgressIndicator(")
                sb.appendLine("${pad}    progress = ${progress}f,")
                sb.appendLine("${pad}    modifier = ${generateModifier(component)}")
                sb.appendLine("${pad})")
            }

            ComponentType.CircularProgressIndicator -> {
                val progress = (props["progress"] as? PropertyValue.FloatValue)?.value ?: 0.6f

                sb.appendLine("${pad}CircularProgressIndicator(")
                sb.appendLine("${pad}    progress = ${progress}f,")
                sb.appendLine("${pad}    modifier = ${generateModifier(component)}")
                sb.appendLine("${pad})")
            }
        }
    }

    private fun generateModifier(component: DesignComponent): String {
        val w = component.size.width.toInt()
        val h = component.size.height.toInt()
        return "Modifier.offset(x = ${component.position.x.toInt()}.dp, y = ${component.position.y.toInt()}.dp).size(width = ${w}.dp, height = ${h}.dp)"
    }

    private fun buildImports(designState: DesignState): String {
        val imports = mutableSetOf(
            "androidx.compose.foundation.layout.Box",
            "androidx.compose.foundation.layout.fillMaxSize",
            "androidx.compose.foundation.layout.offset",
            "androidx.compose.foundation.layout.size",
            "androidx.compose.runtime.Composable",
            "androidx.compose.ui.Modifier",
            "androidx.compose.ui.unit.dp"
        )

        val types = designState.components.values.map { it.type }.toSet()

        if (ComponentType.Text in types) {
            imports += setOf(
                "androidx.compose.material3.Text",
                "androidx.compose.ui.unit.sp",
                "androidx.compose.ui.text.font.FontWeight",
                "androidx.compose.ui.text.style.TextAlign",
                "androidx.compose.ui.graphics.Color"
            )
        }
        if (ComponentType.Button in types) {
            imports += "androidx.compose.material3.Button"
        }
        if (ComponentType.TextField in types) {
            imports += setOf(
                "androidx.compose.material3.OutlinedTextField",
                "androidx.compose.material3.Text",
                "androidx.compose.runtime.mutableStateOf",
                "androidx.compose.runtime.remember",
                "androidx.compose.runtime.getValue",
                "androidx.compose.runtime.setValue"
            )
        }
        if (ComponentType.Image in types) {
            imports += setOf(
                "androidx.compose.foundation.Image",
                "androidx.compose.ui.res.painterResource",
                "androidx.compose.ui.layout.ContentScale"
            )
        }
        if (ComponentType.Checkbox in types || ComponentType.Switch in types) {
            imports += setOf(
                "androidx.compose.foundation.layout.Row",
                "androidx.compose.foundation.layout.Spacer",
                "androidx.compose.foundation.layout.width",
                "androidx.compose.ui.Alignment",
                "androidx.compose.runtime.mutableStateOf",
                "androidx.compose.runtime.remember",
                "androidx.compose.runtime.getValue",
                "androidx.compose.runtime.setValue"
            )
        }
        if (ComponentType.Checkbox in types) {
            imports += "androidx.compose.material3.Checkbox"
        }
        if (ComponentType.Switch in types) {
            imports += "androidx.compose.material3.Switch"
        }
        if (ComponentType.Card in types) {
            imports += setOf(
                "androidx.compose.material3.ElevatedCard",
                "androidx.compose.material3.CardDefaults",
                "androidx.compose.foundation.shape.RoundedCornerShape"
            )
        }
        if (ComponentType.Column in types) {
            imports += setOf(
                "androidx.compose.foundation.layout.Column",
                "androidx.compose.foundation.layout.Arrangement",
                "androidx.compose.ui.Alignment"
            )
        }
        if (ComponentType.Row in types) {
            imports += setOf(
                "androidx.compose.foundation.layout.Row",
                "androidx.compose.foundation.layout.Arrangement",
                "androidx.compose.ui.Alignment"
            )
        }
        if (ComponentType.Box in types) {
            imports += "androidx.compose.ui.Alignment"
        }
        if (ComponentType.Spacer in types) {
            imports += "androidx.compose.foundation.layout.Spacer"
        }
        if (ComponentType.Divider in types) {
            imports += setOf(
                "androidx.compose.material3.Divider",
                "androidx.compose.ui.graphics.Color"
            )
        }
        if (ComponentType.Slider in types) {
            imports += setOf(
                "androidx.compose.material3.Slider",
                "androidx.compose.runtime.mutableStateOf",
                "androidx.compose.runtime.remember",
                "androidx.compose.runtime.getValue",
                "androidx.compose.runtime.setValue"
            )
        }
        if (ComponentType.IconButton in types || ComponentType.FloatingActionButton in types) {
            imports += setOf(
                "androidx.compose.material.icons.Icons",
                "androidx.compose.material.icons.filled.*",
                "androidx.compose.material3.Icon"
            )
        }
        if (ComponentType.IconButton in types) {
            imports += "androidx.compose.material3.IconButton"
        }
        if (ComponentType.FloatingActionButton in types) {
            imports += "androidx.compose.material3.FloatingActionButton"
        }
        if (ComponentType.LinearProgressIndicator in types) {
            imports += "androidx.compose.material3.LinearProgressIndicator"
        }
        if (ComponentType.CircularProgressIndicator in types) {
            imports += "androidx.compose.material3.CircularProgressIndicator"
        }

        return imports.sorted().joinToString("\n") { "import $it" }
    }
}
