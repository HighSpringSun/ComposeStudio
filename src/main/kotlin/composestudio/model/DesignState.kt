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
            .sortedBy { it.childOrder }
            .map { it.id }

    fun childComponents(parentId: String, slot: String? = null): List<DesignComponent> {
        return _components.values
            .filter { it.parentId == parentId && (slot == null || it.childSlot == slot) }
            .sortedBy { it.childOrder }
    }

    fun subtreeComponents(rootId: String): List<DesignComponent> {
        val result = mutableListOf<DesignComponent>()

        fun visit(id: String) {
            val component = _components[id] ?: return
            result += component
            childComponents(id).forEach { visit(it.id) }
        }

        visit(rootId)
        return result
    }

    fun componentDepth(id: String): Int {
        var depth = 0
        var current = _components[id]?.parentId
        while (current != null) {
            depth++
            current = _components[current]?.parentId
        }
        return depth
    }

    fun selectComponent(id: String?) {
        selectedComponentId = id
    }

    fun addComponent(
        type: ComponentType,
        position: Offset,
        parentId: String? = null,
        childSlot: String = "content"
    ): String {
        val id = UUID.randomUUID().toString().take(8)
        val resolvedSlot = if (parentId == null) "root" else childSlot
        val component = DesignComponent(
            id = id,
            type = type,
            position = position,
            size = type.defaultSize,
            properties = type.defaultProperties,
            parentId = parentId,
            childSlot = resolvedSlot,
            childOrder = nextChildOrder(parentId, resolvedSlot)
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
        if (id !in _components) return
        val action = DeleteComponentAction(subtreeComponents(id))
        executeAction(action)
        if (selectedComponentId == id || selectedComponentId in action.components.map { it.id }) {
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

    private fun nextChildOrder(parentId: String?, childSlot: String): Int {
        return _components.values
            .filter { it.parentId == parentId && it.childSlot == childSlot }
            .maxOfOrNull { it.childOrder }
            ?.plus(1)
            ?: 0
    }

    // Internal methods used by Actions
    internal fun internalAddComponent(component: DesignComponent) {
        _components[component.id] = component
    }

    internal fun internalRestoreComponents(components: List<DesignComponent>) {
        components.forEach { _components[it.id] = it }
    }

    internal fun internalRemoveComponent(id: String) {
        _components.remove(id)
    }

    internal fun internalRemoveComponents(ids: Collection<String>) {
        ids.forEach(_components::remove)
    }

    internal fun internalUpdateComponent(id: String, updater: (DesignComponent) -> DesignComponent) {
        val component = _components[id] ?: return
        _components[id] = updater(component)
    }
}
