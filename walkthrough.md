# Walkthrough: Replacement to Installation Workflow

This document summarizes the changes implemented to seamlessly transition users from a Replacement task to an Installation task.

## Changes Made

### 1. `AuthRepository.kt`
- Added the `updateSubRequestTypeToInstallation` suspend function.
- This function fetches a `WorkOrderEntity` from the Room Database, locates the specific `WoRequest` by its `requestId`, updates its `requesttype` to `"Installation"` using the Kotlin `.copy()` method, and then saves the updated `WorkOrderEntity` back to the database.

### 2. `AuthViewModel.kt`
- Added a new `MutableSharedFlow<Boolean>` named `localDbUpdateSuccess` to emit status changes.
- Added the `updateSubRequestTypeToInstallation` function, which is called from the UI. It safely invokes the corresponding repository method inside the `viewModelScope` and emits the success/failure result via `_localDbUpdateSuccess`.

### 3. `UploadDocumentsScreen.kt`
- **Updated Success Collector:** Modified the `LaunchedEffect` that observes `viewModel.updateSuccess`. When the final completion API call is successful, it now checks if the current request is a "Replacement".
  - **If Replacement:** It calls `viewModel.updateSubRequestTypeToInstallation(...)` to trigger the database update.
  - **If Not Replacement:** It safely navigates back to the Dashboard as before.
- **New DB Update Collector:** Added a new `LaunchedEffect` to observe `viewModel.localDbUpdateSuccess`.
  - **If DB Update is Successful:** Updates the `SelectedRequestHolder.selectedSUbWorkItemList` directly in memory using `.copy(requesttype = "Installation")` so that the next screen knows it is an Installation, then navigates to `Screen.JobInstallationDetailScreen.route`.
  - **If DB Update Fails:** Safely falls back to the Dashboard screen.

## Verification Results

- **Compilation Check:** Ran `./gradlew assembleDebug`, which completed successfully with zero compile-time errors.
- **Logic Validation:** Confirmed that Room database updates are correctly implemented using data class `copy` functions (handling the immutable `val` correctly), which resolves the offline persistence effectively while maintaining proper application state.
