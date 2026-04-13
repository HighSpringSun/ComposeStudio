# AGENTS.md

## Project snapshot
- `ComposeStudio` is a single-module Kotlin/JVM desktop app built with Compose Multiplatform (`build.gradle.kts`); entry point is `composestudio.MainKt`.
- Main dependencies are `compose.desktop.currentOs`, Material 3, and `material-icons-extended`; there is no backend/service layer in this repo.
- State lives entirely in-process in `src/main/kotlin/composestudio/model/DesignState.kt`.

## Big picture architecture
- `Main.kt` composes the whole shell: left palette + hierarchy, center canvas or code panel, right properties panel.
- `ComposeStudioApp()` owns the transient `placingComponentType`; placement starts in `ui/palette/ComponentPalette.kt` and completes in `ui/canvas/DesignCanvas.kt` when the user clicks the canvas.
- `DesignState` is the hub for all cross-panel communication: selection, component map, undo/redo stacks, and `showCodeGeneration`.
- Mutations are expected to go through `DesignState` methods (`addComponent`, `moveComponent`, `resizeComponent`, `updateProperty`, `deleteComponent`) so undo/redo stays correct.
- Undo/redo behavior is implemented by sealed `Action` types in `action/Action.kt`; if you add a new design mutation, add an `Action` instead of mutating `_components` directly.
- `DesignComponent.kt` defines the domain model: `ComponentType` supplies palette grouping, default size, default properties, and whether a component can host children.
- `PropertyValue` is the schema for editable attributes. `PropertiesPanel.kt`, `DesignCanvas.kt`, and `ui/codegen/CodeGenerator.kt` all branch on the same sealed property/value types.

## Important project-specific patterns
- Adding a new component type is a cross-file change: update `ComponentType` defaults in `model/DesignComponent.kt`, palette icon mapping in `ui/palette/ComponentPalette.kt`, on-canvas preview in `ui/canvas/DesignCanvas.kt`, and code emission in `ui/codegen/CodeGenerator.kt`.
- UI styling is mostly direct `StudioColors.*` usage (`ui/theme/StudioColors.kt`), not deep theming abstractions. Match the existing dark-tooling palette instead of introducing new color sources.
- Layout/property editing is intentionally immediate: editors in `PropertiesPanel.kt` call `DesignState` on each valid change instead of buffering form state.
- `CodeGenerationPanel.kt` uses `remember(designState.components.size, designState.components.values.toList())` to recompute generated code; if generation should react to new state, make sure those dependencies change.
- `CodeGenerator` emits absolute-positioned Compose using `Modifier.offset(...).size(...)`; generated code mirrors the canvas, not responsive layout best practices.
- `DesignComponent` has `children`, `parentId`, and `supportsChildren`, but current canvas rendering is flat (`designState.components.values.forEach` in `DesignCanvas.kt`). Treat nested layout support as incomplete unless you wire all affected panels.

## Build, run, and verification
- Verified on this workspace with Windows wrapper commands:
  - `.\gradlew.bat build`
  - `.\gradlew.bat run`
  - `.\gradlew.bat packageMsi`
- `.\gradlew.bat tasks --all` also exposes Compose desktop packaging for macOS/Linux plus hot reload tasks such as `hotRun`, `hotDev`, and `reload`.
- There is currently no automated test source tree (`src` only contains `main/`; `gradlew build` reports `test NO-SOURCE`). Use `build` as the baseline verification unless you add tests.
- `gradle.properties` pins `org.gradle.java.home` to a user-local JDK path (`C:\Users\cygao\.jdks\temurin-25.0.2`); on another machine, expect to override or remove that setting before running Gradle.

## Files worth opening first
- `src/main/kotlin/composestudio/Main.kt`
- `src/main/kotlin/composestudio/model/DesignState.kt`
- `src/main/kotlin/composestudio/model/DesignComponent.kt`
- `src/main/kotlin/composestudio/ui/canvas/DesignCanvas.kt`
- `src/main/kotlin/composestudio/ui/properties/PropertiesPanel.kt`
- `src/main/kotlin/composestudio/ui/codegen/CodeGenerator.kt`


