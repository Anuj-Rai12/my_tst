# Navigate to Installation Detail Screen after Replacement

This plan details the changes required to redirect the user to the Installation Detail Screen after completing a Replacement request, according to the revised approach.

## Confirmation on User's Approach

**Q: Is updating the attribute of `requestType` in Room DB possible without any issue?**
**A:** Yes, it is absolutely possible and a very solid approach for a seamless offline-first experience! Since `WoRequestEntity` defines `requesttype` as a `val` (read-only), we cannot reassign it directly like `item.requesttype = "Installation"`. However, Kotlin provides a `.copy()` method for data classes. We can fetch the `WorkOrderEntity`, find the corresponding sub-request, use `.copy(requesttype = "Installation")` (and potentially update the `requesttypeId` if needed), and then call `workOrderDao.updateWorkOrder()` to save the modified entity. Finally, we will update the `SelectedRequestHolder` to reflect this change before navigating. 

*Note: Since this modifies the local database without hitting a specific "create installation" backend API, this assumes the backend is already aware of this state transition or that the next Installation API call will be accepted using the same `requestid`.*

## Proposed Changes

### 1. [AuthRepository.kt]
#### [MODIFY] `AuthRepository.kt`
- Add a suspend function `updateSubRequestTypeToInstallation(workId: Int, requestId: String)`.
- This function will:
  1. Fetch the `WorkOrderEntity` using `workOrderDao.getWorkOrderById(workId)`.
  2. Iterate through its `woRequest` list to find the matching `requestId`.
  3. Replace that specific item with a copied version: `it.copy(requesttype = "Installation")` (and map any changes back to the entity list).
  4. Call `workOrderDao.updateWorkOrder()` with the updated `WorkOrderEntity`.
  5. Return `true` if successful, `false` otherwise.

### 2. [AuthViewModel.kt]
#### [MODIFY] `AuthViewModel.kt`
- Add a new function or state flow to trigger the repository DB update safely on a background thread.
- Expose a `SharedFlow` or `LiveData` (e.g., `_localDbUpdateSuccess`) so the UI can react when the DB is successfully updated, or when an error occurs.

### 3. [UploadDocumentsScreen.kt]
#### [MODIFY] `UploadDocumentsScreen.kt`
- **Navigation Logic in `CustomDialogBoxActive` dismissal**:
  - If `terminalStatus.contains("Pending")` -> Navigate to `DashboardScreen.route` (existing behavior).
  - If "Active" -> Call `viewModel.hitUpdateRequest(..., COMPLETED)` (existing behavior).

- **Navigation Logic in `LaunchedEffect(viewModel.updateSuccess)`**:
  - When `status` is `COMPLETED`:
    - **If the current item was a "Replacement"**: Call the new ViewModel function to update the DB record to "Installation". 
    - **If NOT Replacement**: Navigate to `DashboardScreen.route`.
  
- **New `LaunchedEffect` for DB Update Success**:
  - Observe `viewModel.localDbUpdateSuccess`.
  - **If True**: 
    - Update `SelectedRequestHolder.selectedSUbWorkItemList` to reflect `requesttype = "Installation"`.
    - Navigate to `Screen.JobInstallationDetailScreen.route`.
  - **If False (or Error)**: 
    - Fallback: Navigate to `Screen.DashboardScreen.route`.

## Verification Plan

### Automated Tests
- Static analysis and compilation to ensure data classes and DAO methods are correctly referenced.

### Manual Verification
- Test Replacement flow completion.
- Verify that if the API call succeeds, the local DB is updated and the app seamlessly transitions to `JobInstallationDetailScreen`.
- Verify that if the DB update fails, the app falls back to the Dashboard.
- Verify that if the Terminal Status is "Pending", the app correctly falls back to the Dashboard.
