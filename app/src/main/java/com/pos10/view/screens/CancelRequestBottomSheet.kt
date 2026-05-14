package com.pos10.view.screens

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pos10.model.local.CancelReasonListResponse
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CancelRequestBottomSheet(
    cancelList:List<CancelReasonListResponse.Data>,
    showSheet: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit // reasonId, description, remark
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                    onDismiss()
                }
            },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            dragHandle = null//it remove gray dashed line which is visible on top
        ) {
            CancelReasonContent(
                cancelList,
                onDismiss = {
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                        onDismiss()
                    }
                },
                onSubmit = { reasonId, remark ->
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                        onSubmit(reasonId, remark)
                    }
                }
            )
        }
    }
}
