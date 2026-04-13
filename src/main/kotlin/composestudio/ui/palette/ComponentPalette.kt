package composestudio.ui.palette

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CropLandscape
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.SmartButton
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
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
                        onClick = { onComponentSelected(componentType) }
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
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .size(72.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(StudioColors.SurfaceVariant)
            .border(1.dp, StudioColors.Border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
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
        ComponentType.Spacer -> Icons.Default.SpaceBar
        ComponentType.Divider -> Icons.Default.HorizontalRule
        ComponentType.Slider -> Icons.Default.LinearScale
        ComponentType.IconButton -> Icons.Default.Star
        ComponentType.FloatingActionButton -> Icons.Default.Add
        ComponentType.LinearProgressIndicator -> Icons.Default.HorizontalRule
        ComponentType.CircularProgressIndicator -> Icons.Default.RadioButtonChecked
    }
