package composestudio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import composestudio.model.ComponentType
import composestudio.model.DesignState
import composestudio.ui.canvas.DesignCanvas
import composestudio.ui.codegen.CodeGenerationPanel
import composestudio.ui.hierarchy.ComponentHierarchy
import composestudio.ui.palette.ComponentPalette
import composestudio.ui.properties.PropertiesPanel
import composestudio.ui.theme.StudioColors

fun main() = application {
    val windowState = rememberWindowState(
        placement = WindowPlacement.Maximized,
        size = DpSize(1400.dp, 900.dp)
    )

    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "ComposeStudio — Visual UI Designer"
    ) {
        val designState = remember { DesignState() }

        MenuBar {
            Menu("File") {
                Item("New Design", shortcut = KeyShortcut(Key.N, ctrl = true)) {
                    // Could clear the canvas
                }
                Separator()
                Item("Exit", shortcut = KeyShortcut(Key.Q, ctrl = true)) {
                    exitApplication()
                }
            }
            Menu("Edit") {
                Item("Undo", shortcut = KeyShortcut(Key.Z, ctrl = true), enabled = designState.canUndo) {
                    designState.undo()
                }
                Item("Redo", shortcut = KeyShortcut(Key.Z, ctrl = true, shift = true), enabled = designState.canRedo) {
                    designState.redo()
                }
                Separator()
                Item("Delete Selected", shortcut = KeyShortcut(Key.Delete), enabled = designState.selectedComponentId != null) {
                    designState.deleteSelectedComponent()
                }
            }
            Menu("View") {
                Item("Toggle Code Panel", shortcut = KeyShortcut(Key.G, ctrl = true)) {
                    designState.showCodeGeneration = !designState.showCodeGeneration
                }
            }
        }

        MaterialTheme(colorScheme = StudioColors.DarkColorScheme) {
            ComposeStudioApp(designState)
        }
    }
}

@Composable
fun ComposeStudioApp(designState: DesignState) {
    var placingComponentType by remember { mutableStateOf<ComponentType?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioColors.Background)
    ) {
        // Toolbar
        Toolbar(
            designState = designState,
            placingComponentType = placingComponentType
        )

        HorizontalDivider(color = StudioColors.Border, thickness = 1.dp)

        // Main content
        Row(modifier = Modifier.fillMaxSize()) {
            // Left panel: Component Palette + Hierarchy
            Column(
                modifier = Modifier
                    .width(200.dp)
                    .fillMaxHeight()
            ) {
                ComponentPalette(
                    onComponentSelected = { placingComponentType = it },
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )

                HorizontalDivider(color = StudioColors.Border, thickness = 1.dp)

                ComponentHierarchy(
                    designState = designState,
                    modifier = Modifier.weight(0.6f).fillMaxWidth()
                )
            }

            VerticalDivider(
                color = StudioColors.Border,
                modifier = Modifier.width(1.dp).fillMaxHeight()
            )

            // Center: Canvas or Code
            if (designState.showCodeGeneration) {
                CodeGenerationPanel(
                    designState = designState,
                    onClose = { designState.showCodeGeneration = false },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            } else {
                DesignCanvas(
                    designState = designState,
                    placingComponentType = placingComponentType,
                    onComponentPlaced = { placingComponentType = null },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }

            VerticalDivider(
                color = StudioColors.Border,
                modifier = Modifier.width(1.dp).fillMaxHeight()
            )

            // Right panel: Properties
            PropertiesPanel(
                designState = designState,
                modifier = Modifier.width(260.dp).fillMaxHeight()
            )
        }
    }
}

@Composable
private fun Toolbar(
    designState: DesignState,
    placingComponentType: ComponentType?
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(StudioColors.PanelHeader)
            .padding(horizontal = 8.dp)
    ) {
        Text(
            text = "ComposeStudio",
            color = StudioColors.Primary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.width(24.dp))

        // Undo/Redo
        IconButton(
            onClick = { designState.undo() },
            enabled = designState.canUndo,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Undo,
                contentDescription = "Undo",
                tint = if (designState.canUndo) StudioColors.OnSurface else StudioColors.OnSurfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.size(18.dp)
            )
        }

        IconButton(
            onClick = { designState.redo() },
            enabled = designState.canRedo,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Redo,
                contentDescription = "Redo",
                tint = if (designState.canRedo) StudioColors.OnSurface else StudioColors.OnSurfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.width(8.dp))

        // Delete selected
        IconButton(
            onClick = { designState.deleteSelectedComponent() },
            enabled = designState.selectedComponentId != null,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete",
                tint = if (designState.selectedComponentId != null) StudioColors.Error else StudioColors.OnSurfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.width(8.dp))

        // Code generation toggle
        IconButton(
            onClick = { designState.showCodeGeneration = !designState.showCodeGeneration },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.Code,
                contentDescription = "Toggle Code",
                tint = if (designState.showCodeGeneration) StudioColors.Primary else StudioColors.OnSurface,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.weight(1f))

        // Placement indicator
        if (placingComponentType != null) {
            Surface(
                color = StudioColors.Primary.copy(alpha = 0.15f),
                shape = MaterialTheme.shapes.small
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Placing: ${placingComponentType.displayName}",
                        color = StudioColors.Primary,
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Click canvas to place • ESC to cancel",
                        color = StudioColors.OnSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }
        } else {
            Text(
                text = "${designState.components.size} component(s)",
                color = StudioColors.OnSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}
