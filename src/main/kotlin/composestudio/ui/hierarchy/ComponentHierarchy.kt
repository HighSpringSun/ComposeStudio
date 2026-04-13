package composestudio.ui.hierarchy

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import composestudio.model.DesignState
import composestudio.ui.theme.StudioColors

/**
 * Component hierarchy tree showing all components on the canvas.
 */
@Composable
fun ComponentHierarchy(
    designState: DesignState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(StudioColors.Surface)
            .verticalScroll(rememberScrollState())
            .padding(8.dp)
    ) {
        Text(
            text = "Hierarchy",
            color = StudioColors.OnSurface,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )

        if (designState.components.isEmpty()) {
            Text(
                text = "No components yet.\nAdd from the palette.",
                color = StudioColors.OnSurfaceVariant,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )
        } else {
            designState.rootComponentIds.forEach { id ->
                val component = designState.components[id] ?: return@forEach
                HierarchyItem(
                    name = component.type.displayName,
                    id = component.id,
                    isSelected = component.id == designState.selectedComponentId,
                    onSelect = { designState.selectComponent(component.id) },
                    onDelete = { designState.deleteComponent(component.id) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Summary
        Text(
            text = "${designState.components.size} component(s)",
            color = StudioColors.OnSurfaceVariant,
            fontSize = 10.sp,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
private fun HierarchyItem(
    name: String,
    id: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(
                if (isSelected) StudioColors.Primary.copy(alpha = 0.2f)
                else StudioColors.Surface
            )
            .clickable(onClick = onSelect)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Widgets,
            contentDescription = null,
            tint = if (isSelected) StudioColors.Primary else StudioColors.OnSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(6.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                color = StudioColors.OnSurface,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = id,
                color = StudioColors.OnSurfaceVariant,
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = StudioColors.OnSurfaceVariant,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
