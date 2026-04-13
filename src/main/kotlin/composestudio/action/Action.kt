package composestudio.action

import composestudio.model.DesignComponent
import composestudio.model.DesignState
import composestudio.model.PropertyValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

/**
 * Represents an undoable/redoable action on the design canvas.
 */
sealed interface Action {
    fun execute(state: DesignState)
    fun undo(state: DesignState)
    val description: String
}

/**
 * Action: Add a component to the canvas.
 */
data class AddComponentAction(
    val component: DesignComponent
) : Action {
    override val description = "Add ${component.type.displayName}"

    override fun execute(state: DesignState) {
        state.internalAddComponent(component)
    }

    override fun undo(state: DesignState) {
        state.internalRemoveComponent(component.id)
    }
}

/**
 * Action: Delete a component from the canvas.
 */
data class DeleteComponentAction(
    val components: List<DesignComponent>
) : Action {
    private val rootComponent: DesignComponent = components.first()

    override val description = "Delete ${rootComponent.type.displayName}"

    override fun execute(state: DesignState) {
        state.internalRemoveComponents(components.map { it.id })
    }

    override fun undo(state: DesignState) {
        state.internalRestoreComponents(components)
    }
}

/**
 * Action: Move a component to a new position.
 */
data class MoveComponentAction(
    val componentId: String,
    val oldPosition: Offset,
    val newPosition: Offset
) : Action {
    override val description = "Move component"

    override fun execute(state: DesignState) {
        state.internalUpdateComponent(componentId) { it.copy(position = newPosition) }
    }

    override fun undo(state: DesignState) {
        state.internalUpdateComponent(componentId) { it.copy(position = oldPosition) }
    }
}

/**
 * Action: Resize a component.
 */
data class ResizeComponentAction(
    val componentId: String,
    val oldSize: Size,
    val newSize: Size
) : Action {
    override val description = "Resize component"

    override fun execute(state: DesignState) {
        state.internalUpdateComponent(componentId) { it.copy(size = newSize) }
    }

    override fun undo(state: DesignState) {
        state.internalUpdateComponent(componentId) { it.copy(size = oldSize) }
    }
}

/**
 * Action: Modify a property of a component.
 */
data class ModifyPropertyAction(
    val componentId: String,
    val propertyName: String,
    val oldValue: PropertyValue,
    val newValue: PropertyValue
) : Action {
    override val description = "Modify $propertyName"

    override fun execute(state: DesignState) {
        state.internalUpdateComponent(componentId) {
            it.copy(properties = it.properties + (propertyName to newValue))
        }
    }

    override fun undo(state: DesignState) {
        state.internalUpdateComponent(componentId) {
            it.copy(properties = it.properties + (propertyName to oldValue))
        }
    }
}
