package composestudio.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import composestudio.action.Action
import composestudio.action.AddComponentAction
import composestudio.action.DeleteComponentAction
import composestudio.action.ModifyPropertyAction
import composestudio.action.MoveComponentAction
import composestudio.action.ResizeComponentAction
import java.util.UUID

/**
 * Central state manager for the design canvas.
 * Manages all components, selection state, and undo/redo history.
 */
class DesignState {
    /** All components currently on the canvas, keyed by ID. */
    private val _components = mutableStateMapOf<String, DesignComponent>()
    val components: Map<String, DesignComponent> get() = _components

    /** Currently selected component ID. */
    var selectedComponentId by mutableStateOf<String?>(null)
        private set

    /** The selected component, derived from the ID. */
    val selectedComponent: DesignComponent?
        get() = selectedComponentId?.let { _components[it] }

    /** Undo stack. */
    private val undoStack = mutableStateListOf<Action>()

    /** Redo stack. */
    private val redoStack = mutableStateListOf<Action>()

    /** Whether undo is available. */
    val canUndo: Boolean get() = undoStack.isNotEmpty()

    /** Whether redo is available. */
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    /** Whether code generation panel is shown. */
    var showCodeGeneration by mutableStateOf(false)

    /** Canvas zoom level. */
    var zoomLevel by mutableStateOf(1f)

    /** Canvas pan offset. */
    var canvasOffset by mutableStateOf(Offset.Zero)

    /** Root-level component IDs (those without a parent). */
    val rootComponentIds: List<String>
        get() = _components.values
            .filter { it.parentId == null }
            .sortedBy { it.id }
            .map { it.id }

    fun selectComponent(id: String?) {
        selectedComponentId = id
    }

    fun addComponent(type: ComponentType, position: Offset): String {
        val id = UUID.randomUUID().toString().take(8)
        val component = DesignComponent(
            id = id,
            type = type,
            position = position,
            size = type.defaultSize,
            properties = type.defaultProperties
        )
        val action = AddComponentAction(component)
        executeAction(action)
        selectedComponentId = id
        return id
    }

    fun moveComponent(id: String, newPosition: Offset) {
        val component = _components[id] ?: return
        val action = MoveComponentAction(id, component.position, newPosition)
        executeAction(action)
    }

    fun resizeComponent(id: String, newSize: Size) {
        val component = _components[id] ?: return
        val constrainedSize = Size(
            width = newSize.width.coerceAtLeast(20f),
            height = newSize.height.coerceAtLeast(20f)
        )
        val action = ResizeComponentAction(id, component.size, constrainedSize)
        executeAction(action)
    }

    fun updateProperty(id: String, propertyName: String, value: PropertyValue) {
        val component = _components[id] ?: return
        val oldValue = component.properties[propertyName] ?: return
        val action = ModifyPropertyAction(id, propertyName, oldValue, value)
        executeAction(action)
    }

    fun deleteComponent(id: String) {
        val component = _components[id] ?: return
        val action = DeleteComponentAction(component)
        executeAction(action)
        if (selectedComponentId == id) {
            selectedComponentId = null
        }
    }

    fun deleteSelectedComponent() {
        selectedComponentId?.let { deleteComponent(it) }
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val action = undoStack.removeLast()
        action.undo(this)
        redoStack.add(action)
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val action = redoStack.removeLast()
        action.execute(this)
        undoStack.add(action)
    }

    private fun executeAction(action: Action) {
        action.execute(this)
        undoStack.add(action)
        redoStack.clear()
    }

    // Internal methods used by Actions
    internal fun internalAddComponent(component: DesignComponent) {
        _components[component.id] = component
    }

    internal fun internalRemoveComponent(id: String) {
        _components.remove(id)
    }

    internal fun internalUpdateComponent(id: String, updater: (DesignComponent) -> DesignComponent) {
        val component = _components[id] ?: return
        _components[id] = updater(component)
    }
}
