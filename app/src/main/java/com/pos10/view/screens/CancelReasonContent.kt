package com.pos10.view.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pos10.R
import com.pos10.helper.CommonUtils.showToastC
import com.pos10.model.local.CancelReasonListResponse

@Composable
fun CancelReasonContent(
    cancelList:List<CancelReasonListResponse.Data>,
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit
) {

    var selectedReason by remember { mutableStateOf<CancelReasonListResponse.Data?>(null) }
    var remark by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Cancel Request",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                color = Color.Black)
            IconButton(onClick = onDismiss) {
                Icon(imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.Gray)
            }
        }

        // Reasons list
        cancelList.forEach { reason ->
            val isSelected = selectedReason?.valueTypeId == reason.valueTypeId
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(indication = null,
                        interactionSource = remember { MutableInteractionSource() }) { selectedReason = reason }
                    .padding(vertical = 4.dp)) {
                val icon =
                    if (isSelected) R.drawable.ic_radio_checked else R.drawable.ic_radio_unchecked

                Image(painter = painterResource(id = icon),
                    contentDescription = "Select reason",
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .size(18.dp))

                Text(text = reason.description,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = if (isSelected) Color.Black else Color.DarkGray)
            }
        }

        Spacer(Modifier.height(4.dp))
        Text(
            text = "Remark *",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            color = Color.Black
        )
        OutlinedTextField(
            value = remark,
            onValueChange = { remark = it },
            placeholder = { Text("Reason to failed request..") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF757575),
                unfocusedBorderColor = Color.LightGray,
                cursorColor = Color.Black
            ),
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp))

        Spacer(Modifier.height(16.dp))

        // Submit Button
        Button(
            onClick = {
                when {
                  /*  selectedReason == null ->
                        showToastC(context, "Please select a reason to cancel the work")*/

                    remark.isBlank() ->
                        showToastC(context, "Please provide a remark")

                    else ->
                        onSubmit(selectedReason?.valueTypeId?:0, remark)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFff6900)),
            shape = RoundedCornerShape(10.dp)) {
            Text("Submit", color = Color.White, fontSize = 15.sp)
        }
    }
}
