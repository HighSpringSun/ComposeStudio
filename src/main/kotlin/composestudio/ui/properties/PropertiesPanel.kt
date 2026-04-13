package composestudio.ui.properties

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import composestudio.model.DesignComponent
import composestudio.model.DesignState
import composestudio.model.PropertyValue
import composestudio.ui.theme.StudioColors

/**
 * Properties panel for editing the selected component's attributes.
 */
@Composable
fun PropertiesPanel(
    designState: DesignState,
    modifier: Modifier = Modifier
) {
    val selected = designState.selectedComponent

    Column(
        modifier = modifier
            .background(StudioColors.Surface)
            .verticalScroll(rememberScrollState())
            .padding(8.dp)
    ) {
        Text(
            text = "Properties",
            color = StudioColors.OnSurface,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
        )

        if (selected == null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth().height(100.dp)
            ) {
                Text(
                    text = "Select a component\nto edit properties",
                    color = StudioColors.OnSurfaceVariant,
                    fontSize = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            ComponentInfo(selected)
            Spacer(Modifier.height(12.dp))
            PositionSizeEditor(selected, designState)
            Spacer(Modifier.height(12.dp))
            PropertyEditors(selected, designState)
        }
    }
}

@Composable
private fun ComponentInfo(component: DesignComponent) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(StudioColors.SurfaceVariant)
            .padding(10.dp)
    ) {
        Text(
            text = component.type.displayName,
            color = StudioColors.Primary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "ID: ${component.id}",
            color = StudioColors.OnSurfaceVariant,
            fontSize = 10.sp
        )
        if (component.parentId != null) {
            Text(
                text = "Parent: ${component.parentId} • Slot: ${component.childSlot}",
                color = StudioColors.OnSurfaceVariant,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun PositionSizeEditor(component: DesignComponent, designState: DesignState) {
    SectionHeader("Layout")

    if (component.parentId != null) {
        Text(
            text = "This component is managed by its parent layout. You can still edit width/height and properties below.",
            color = StudioColors.OnSurfaceVariant,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
        )
    }

    if (component.parentId == null) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CompactNumberField(
                label = "X",
                value = component.position.x.toInt().toString(),
                onValueChange = { newVal ->
                    newVal.toFloatOrNull()?.let { x ->
                        designState.moveComponent(
                            component.id,
                            component.position.copy(x = x)
                        )
                    }
                },
                modifier = Modifier.weight(1f)
            )
            CompactNumberField(
                label = "Y",
                value = component.position.y.toInt().toString(),
                onValueChange = { newVal ->
                    newVal.toFloatOrNull()?.let { y ->
                        designState.moveComponent(
                            component.id,
                            component.position.copy(y = y)
                        )
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(4.dp))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CompactNumberField(
            label = "W",
            value = component.size.width.toInt().toString(),
            onValueChange = { newVal ->
                newVal.toFloatOrNull()?.let { w ->
                    designState.resizeComponent(
                        component.id,
                        component.size.copy(width = w)
                    )
                }
            },
            modifier = Modifier.weight(1f)
        )
        CompactNumberField(
            label = "H",
            value = component.size.height.toInt().toString(),
            onValueChange = { newVal ->
                newVal.toFloatOrNull()?.let { h ->
                    designState.resizeComponent(
                        component.id,
                        component.size.copy(height = h)
                    )
                }
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PropertyEditors(component: DesignComponent, designState: DesignState) {
    SectionHeader("Attributes")

    component.properties.forEach { (name, value) ->
        Spacer(Modifier.height(6.dp))

        when (value) {
            is PropertyValue.StringValue -> {
                StringPropertyEditor(
                    name = name,
                    value = value.value,
                    onValueChange = {
                        designState.updateProperty(component.id, name, PropertyValue.StringValue(it))
                    }
                )
            }
            is PropertyValue.IntValue -> {
                IntPropertyEditor(
                    name = name,
                    value = value.value,
                    onValueChange = {
                        designState.updateProperty(component.id, name, PropertyValue.IntValue(it))
                    }
                )
            }
            is PropertyValue.FloatValue -> {
                FloatPropertyEditor(
                    name = name,
                    value = value.value,
                    onValueChange = {
                        designState.updateProperty(component.id, name, PropertyValue.FloatValue(it))
                    }
                )
            }
            is PropertyValue.BooleanValue -> {
                BooleanPropertyEditor(
                    name = name,
                    value = value.value,
                    onValueChange = {
                        designState.updateProperty(component.id, name, PropertyValue.BooleanValue(it))
                    }
                )
            }
            is PropertyValue.ColorValue -> {
                ColorPropertyEditor(
                    name = name,
                    hex = value.hex,
                    onValueChange = {
                        designState.updateProperty(component.id, name, PropertyValue.ColorValue(it))
                    }
                )
            }
            is PropertyValue.ChoiceValue -> {
                ChoicePropertyEditor(
                    name = name,
                    selected = value.selected,
                    options = value.options,
                    onValueChange = {
                        designState.updateProperty(
                            component.id, name,
                            PropertyValue.ChoiceValue(it, value.options)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = StudioColors.OnSurfaceVariant,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    )
}

@Composable
private fun CompactNumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = label,
            color = StudioColors.OnSurfaceVariant,
            fontSize = 11.sp,
            modifier = Modifier.width(16.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = StudioColors.OnSurface,
                unfocusedTextColor = StudioColors.OnSurface,
                focusedBorderColor = StudioColors.Primary,
                unfocusedBorderColor = StudioColors.Border
            ),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
            modifier = Modifier.fillMaxWidth().height(36.dp)
        )
    }
}

@Composable
private fun StringPropertyEditor(
    name: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column {
        PropertyLabel(name)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = StudioColors.OnSurface,
                unfocusedTextColor = StudioColors.OnSurface,
                focusedBorderColor = StudioColors.Primary,
                unfocusedBorderColor = StudioColors.Border
            ),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
            modifier = Modifier.fillMaxWidth().height(36.dp)
        )
    }
}

@Composable
private fun IntPropertyEditor(
    name: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Column {
        PropertyLabel(name)
        OutlinedTextField(
            value = value.toString(),
            onValueChange = { it.toIntOrNull()?.let(onValueChange) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = StudioColors.OnSurface,
                unfocusedTextColor = StudioColors.OnSurface,
                focusedBorderColor = StudioColors.Primary,
                unfocusedBorderColor = StudioColors.Border
            ),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
            modifier = Modifier.fillMaxWidth().height(36.dp)
        )
    }
}

@Composable
private fun FloatPropertyEditor(
    name: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column {
        PropertyLabel(name)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = 0f..1f,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = String.format("%.2f", value),
                color = StudioColors.OnSurface,
                fontSize = 11.sp,
                modifier = Modifier.width(36.dp)
            )
        }
    }
}

@Composable
private fun BooleanPropertyEditor(
    name: String,
    value: Boolean,
    onValueChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Checkbox(
            checked = value,
            onCheckedChange = onValueChange
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = name,
            color = StudioColors.OnSurface,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun ColorPropertyEditor(
    name: String,
    hex: String,
    onValueChange: (String) -> Unit
) {
    Column {
        PropertyLabel(name)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Color preview swatch
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(parseColorSafe(hex))
                    .border(1.dp, StudioColors.Border, RoundedCornerShape(4.dp))
            )
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = hex,
                onValueChange = onValueChange,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = StudioColors.OnSurface,
                    unfocusedTextColor = StudioColors.OnSurface,
                    focusedBorderColor = StudioColors.Primary,
                    unfocusedBorderColor = StudioColors.Border
                ),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                modifier = Modifier.weight(1f).height(36.dp)
            )
        }
    }
}

@Composable
private fun ChoicePropertyEditor(
    name: String,
    selected: String,
    options: List<String>,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        PropertyLabel(name)
        Box {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .border(1.dp, StudioColors.Border, RoundedCornerShape(4.dp))
                    .clickable { expanded = true }
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    text = selected,
                    color = StudioColors.OnSurface,
                    fontSize = 12.sp
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                fontSize = 12.sp,
                                fontWeight = if (option == selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PropertyLabel(name: String) {
    Text(
        text = name,
        color = StudioColors.OnSurfaceVariant,
        fontSize = 11.sp,
        modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
    )
}

private fun parseColorSafe(hex: String): androidx.compose.ui.graphics.Color {
    return try {
        val cleanHex = hex.removePrefix("#")
        when (cleanHex.length) {
            6 -> androidx.compose.ui.graphics.Color(("FF$cleanHex").toLong(16))
            8 -> androidx.compose.ui.graphics.Color(cleanHex.toLong(16))
            else -> androidx.compose.ui.graphics.Color.Gray
        }
    } catch (_: Exception) {
        androidx.compose.ui.graphics.Color.Gray
    }
}
