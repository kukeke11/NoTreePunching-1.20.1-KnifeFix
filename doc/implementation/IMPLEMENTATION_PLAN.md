# Implementation Plan

This plan outlines the steps to implement the tag-based sharp tool harvesting system.

## 1. Tag System and Data Generation
- [ ] **Step 1: Define Tags and Update Data Generation**
  - **Task**: Define the new `REQUIRES_SHARP_TOOL` block tag and `PLANT_FIBER_SOURCES` tag in `ModTags.java`. Update the Python data generation script (`Data/main.py`) to create the corresponding JSON files and populate them with default values as specified in the technical spec. This includes referencing Minecraft's plant tags and adding NTP knives to the existing `sharp_tools` item tag.
  - **Files**:
    - `Common/src/main/java/com/alcatrazescapee/notreepunching/common/ModTags.java`
    - `Data/main.py`
  - **Step Dependencies**: None.
  - **User Instructions**: Run the `Data/main.py` script to generate the new tag JSON files in `Forge/src/main/resources/data/` and `Common/src/main/resources/data/`.

## 2. Core Logic Implementation
- [ ] **Step 2: Create SharpToolUtil**
  - **Task**: Create the `SharpToolUtil.java` class to centralize the sharp tool logic. Implement the methods `isSharpTool`, `requiresSharpTool`, `getDestroySpeed`, and `shouldDamageToolOnPlant` as detailed in the technical specification, including the suggested caching mechanism. Initially, these methods will not be called from anywhere.
  - **Files**:
    - `Common/src/main/java/com/alcatrazescapee/notreepunching/util/SharpToolUtil.java`
  - **Step Dependencies**: Step 1.
  - **User Instructions**: Create the new Java file and implement its methods. Ensure it compiles correctly.

## 3. Integration and Refactoring
- [ ] **Step 3: Integrate SharpToolUtil into KnifeItem**
  - **Task**: Refactor `KnifeItem.java` to use the new `SharpToolUtil`. The methods `isCorrectToolForDrops`, `getDestroySpeed`, and `mineBlock` should be modified to delegate their logic to `SharpToolUtil`. This will make knives the first adopters of the new tag-based system while preserving their exact behavior.
  - **Files**:
    - `Common/src/main/java/com/alcatrazescapee/notreepunching/common/items/KnifeItem.java`
  - **Step Dependencies**: Step 2.
  - **User Instructions**: Test in-game to confirm that knives still harvest plants and take durability damage exactly as they did before the changes.

- [ ] **Step 4: Integrate Sharp Tool System into EventHandler**
  - **Task**: Modify `EventHandler.java` to incorporate the sharp tool system for all items. The `modifyHarvestCheck` and `modifyBreakSpeed` methods should be updated to call `SharpToolUtil`, effectively applying the sharp tool logic to any item tagged `#notreepunching:sharp_tools`.
  - **Files**:
    - `Common/src/main/java/com/alcatrazescapee/notreepunching/EventHandler.java`
  - **Step Dependencies**: Step 3.
  - **User Instructions**: Test with a non-knife item that has been added to the `sharp_tools` tag (via the generated JSON file). Verify that it can now harvest plants and that untagged items cannot.

## 4. Configuration
- [ ] **Step 5: Add Configuration Options**
  - **Task**: Add the `enableSharpToolSystem` and `requireSharpToolForPlants` boolean toggles to the configuration system. This involves adding them to `ForgeConfig.java` and exposing them through the `Config.java` compatibility layer.
  - **Files**:
    - `Common/src/main/java/com/alcatrazescapee/notreepunching/ForgeConfig.java`
    - `Common/src/main/java/com/alcatrazescapee/notreepunching/Config.java`
  - **Step Dependencies**: None.
  - **User Instructions**: Check that the new options appear in the generated `notreepunching.toml` config file.

- [ ] **Step 6: Make SharpToolUtil Respect Configuration**
  - **Task**: Update `SharpToolUtil.java` to use the newly created configuration options. The methods should check the `enableSharpToolSystem` and `requireSharpToolForPlants` booleans and alter their behavior accordingly, returning early or falling back to vanilla behavior if the system is disabled.
  - **Files**:
    - `Common/src/main/java/com/alcatrazescapee/notreepunching/util/SharpToolUtil.java`
  - **Step Dependencies**: Step 2, Step 5.
  - **User Instructions**: Test the new configuration options in-game. Verify that setting `enableSharpToolSystem` to `false` disables the feature and restores vanilla harvesting behavior for all tools.