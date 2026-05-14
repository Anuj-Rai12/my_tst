package com.pos10.view.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pos10.R


val dialColorList = listOf(
    "Black",
    "White",
    "Silver",
    "Blue",
    "Green",
    "Champagne",
    "Brown",
    "Red",
    "Skeleton",
    "Other"
)

val movementTypeList = listOf(
    "Automatic",
    "Manual",
    "Quartz",
    "Mechanical",
    "Chronograph",
    "Smart",
    "Digital",)

val braceletColorList = listOf(
    "Black",
    "Brown",
    "Silver",
    "Gold",
    "Rose Gold",)

@Composable
fun HeaderWithIcon(
    title: String,
    iconResId: Int,
    screenWidth: Dp,
    fontSize: TextUnit = 16.sp,
    textColor: Color = Color.Black,
    screenHeight: Dp,
    onIconClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .offset(y = screenHeight * 0.05f)
            .padding(horizontal = screenWidth * (14f / screenWidth.value))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            val iconSize = screenWidth * 0.06f

            if (onIconClick != null) {
                Image(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = null,
                    modifier = Modifier
                        .size(iconSize)
                        .clickable { onIconClick() }
                )
            } else {
                Image(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    modifier = Modifier.size(iconSize)
                )
            }

            Spacer(modifier = Modifier.width(screenWidth * 0.04f))

            Text(
                text = title,
                style = TextStyle(
                    fontSize = fontSize, color = textColor, fontFamily = FontFamily(Font(R.font.instrument_sans_bold))),
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}

@Composable
fun CustomImage(
    imageRes: Int,
    contentDescription: String = "",
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    Image(
        painter = painterResource(id = imageRes),
        contentDescription = contentDescription,
        modifier = modifier,

        contentScale = contentScale
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomText(
    text: String,
    style: TextStyle = MaterialTheme.typography.titleLarge,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        maxLines = maxLines,
        style = style,
        overflow = overflow,
        modifier = modifier.semantics {
            this.text = AnnotatedString(text)
        }
    )
}

@Composable
fun CustomProgressBar(
    progress: Float, // value from 0f to 1f
    modifier: Modifier = Modifier,
    barHeight: Dp = 10.dp,
    backgroundColor: Color = Color(0xFFD0DEE3), // light grey
    progressColor: Color = Color(0xFF003B49),   // dark teal
    cornerRadius: Dp = 50.dp,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(barHeight)
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(cornerRadius))
                .background(progressColor)
        )
    }
}

@Composable
fun LabeledInputBoxHalfCheck(
    label: String,
    modifier: Modifier = Modifier,
    screenHeight: Dp,
    borderColor: Color = Color(0xFF7F9CA6),
    cornerRadius: Dp = 12.dp,
    selectValue: String = "",
    hint: String,
    onClick: () -> Boolean = { false },
    textColor: Color = Color.Black,
    hintColor: Color = Color(0xFF7F9CA6),
    dropdownExpanded: Boolean,
    dropdownOptions: List<String>,
    onDismissDropdown: () -> Unit,
    onDropdownSelect: (String) -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 14.sp,
                color = Color(0xFF35494F),
                fontFamily = FontFamily(Font(R.font.instrument_sans_regular))
            )
        )

        Spacer(modifier = Modifier.padding(screenHeight * 0.006f))

        var parentSize by remember { mutableStateOf(IntSize.Zero) }

        Box(
            modifier = Modifier
                .onGloballyPositioned { coordinates ->
                    parentSize = coordinates.size
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeight * 0.07f)
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(Color.White)
                    .border(
                        width = 1.dp,
                        shape = RoundedCornerShape(cornerRadius),
                        color = borderColor
                    )
                    .clickable { onClick() }
                    .padding(start = 14.dp, end = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                CustomTextSelect(
                    text = selectValue,
                    hint = hint,
                    textColor = textColor,
                    hintColor = hintColor,
                    style = TextStyle(fontSize = 14.sp)
                )

                Icon(
                    painter = painterResource(id = R.drawable.ic_down_arrow),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(14.dp)
                )
            }

            DropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = onDismissDropdown,
                modifier = Modifier
                    .width(with(LocalDensity.current) { parentSize.width.toDp() })
            ) {
                dropdownOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = { onDropdownSelect(option) }
                    )
                }
            }
        }
    }
}

@Composable
fun CustomTextSelect(
    text: String,
    hint: String,
    style: TextStyle = MaterialTheme.typography.titleLarge,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    modifier: Modifier = Modifier,
    textColor: Color = Color.Black,
    hintColor: Color = Color.Gray) {
    Text(
        text = if (text.isEmpty()) hint else text,
        maxLines = maxLines,
        style = style,
        fontSize = 14.sp,
        fontFamily = FontFamily(Font(R.font.instrument_sans_regular)),
        color = if (text.isEmpty()) hintColor else textColor,
        overflow = overflow,
        modifier = modifier.semantics {
            this.text = AnnotatedString(text)
        }
    )
}


@Composable
fun DropdownTextField(
    modifier: Modifier = Modifier,
    label: String,
    placeholder: String,
    selectedText: String,
    onValueChange: (String) -> Unit,
    onClick: () -> Boolean = { false },
    itemList: List<String>,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    font: FontFamily = FontFamily(Font(R.font.instrument_sans_regular))
) {
    Column(modifier = modifier.fillMaxWidth().padding(top = 16.dp, end = 0.dp, start = 16.dp)) {
        Row {
            Text(
                text = label,
                style = TextStyle(
                    color = Color(0xff35494F),
                    fontFamily = font,
                    fontSize = 14.sp
                )
            )

            Text(
                text = " *",
                fontSize = 14.sp,
                color = Color.Red
            )
        }

        TextField(
            value = selectedText,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable { onClick() }
                .border(
                    width = 1.dp,
                    color = Color(0XFF7F9CA6),
                    shape = RoundedCornerShape(12.dp)
                ),
            placeholder = {
                Text(
                    text = placeholder,
                    color = Color(0XFF7F9CA6),
                    fontSize = 12.sp,
                    fontFamily = font
                )
            },
            trailingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_down_arrow),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .clickable {
                            onExpandedChange(!isExpanded)
                        }
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            readOnly = true // important to disable keyboard
        )

        if (isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, Color.LightGray, RoundedCornerShape(12.dp))
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 250.dp)
                        .padding(4.dp)
                ) {
                    items(itemList) { item ->
                        Text(
                            text = item,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                                .clickable {
                                    onValueChange(item)
                                    onExpandedChange(false)
                                },
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontFamily = font
                        )
                    }
                }
            }
        }
    }
}