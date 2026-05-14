package com.pos10.view.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.pos10.R

/*
@Composable
fun MainScreenWithBottomNav() {
    var selectedTab by remember { mutableStateOf(0) }

    val tabs = listOf("Home", "Search", "Profile")
    val icons = listOf(R.drawable.ic_email, R.drawable.ic_search_ic, R.drawable.ic_email)

    androidx.compose.material3.Scaffold(
        bottomBar = {
            BottomNavigation(
                containerColor = Color.White, // background color
            ) {
                tabs.forEachIndexed { index, tab ->
                    BottomNavigationItem(
                        icon = { Icon(painterResource(id = icons[index]), contentDescription = tab) },
                        label = { Text(tab) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        selectedContentColor = Color.Blue,
                        unselectedContentColor = Color.Gray
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (selectedTab) {
                0 -> DemoScreen()
                1 -> SearchScreen()
                2 -> ProfileScreen()
            }
        }
    }
}

*/

@Composable
fun SearchScreen(){

}

@Composable
fun ProfileScreen(){}

@Composable
fun DemoScreen() {
    var search by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top greeting row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 50.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Good Morning!",
                        fontSize = 12.sp,
                        fontFamily = FontFamily(Font(R.font.instrument_sans_regular)),
                        color = Color.Gray
                    )
                    Text(
                        text = "James",
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.instrument_sans_bold)),
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Image(
                    painter = painterResource(R.drawable.ic_notifications),
                    contentDescription = "Notification Icon",
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Search bar
        item {
            BasicTextField(
                value = search,
                onValueChange = { search = it },
                singleLine = true,
                textStyle = TextStyle(
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.instrument_sans_regular))
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 1.dp,
                        color = Color(0xFFEAEEF0),
                        shape = RoundedCornerShape(8.dp))
                    .background(Color.White, shape = RoundedCornerShape(8.dp)),
                decorationBox = { innerTextField ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_search_ic),
                            contentDescription = "Search",
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (search.isEmpty()) {
                                Text(
                                    text = "Search services",
                                    color = Color(0xFF35494F),
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily(Font(R.font.instrument_sans_regular))
                                )
                            }
                            innerTextField()
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        if (search.isNotEmpty()) {
                            Icon(
                                painter = painterResource(R.drawable.close_ic),
                                contentDescription = "Clear search",
                                tint = Color.Gray,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { search = "" }
                            )
                        }
                    }
                })
        }

        // Image banner
        item {
            Image(
                painter = painterResource(R.drawable.upload_image),
                contentDescription = "Icon",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(color = Color.LightGray, shape = RoundedCornerShape(12.dp))
            )
        }

        // Grid of services
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(4) { GridCard() }
            }
        }

        // Upcoming Appointments text
        item {
            Text(
                text = "Upcoming Appointments",
                fontSize = 14.sp,
                fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold)),
                color = Color.Black,
                modifier = Modifier
            )
        }

        // Upcoming appointments image
        item {
            Image(
                painter = painterResource(R.drawable.upload_image),
                contentDescription = "Icon",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(color = Color.LightGray, shape = RoundedCornerShape(12.dp))
            )
        }

        // Grid for medical appointments
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(2) { GridMedical() }
            }
        }
    }
}

@Composable
fun GridCard() {
    Box(
        modifier = Modifier
            .size(120.dp)
            .background(color = Color.LightGray, shape = RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color = Color.White, shape = CircleShape)
                .align(Alignment.TopStart)
                .zIndex(1f)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_email),
                contentDescription = "Icon",
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.Center)
            )
        }

        Text(
            text = "GP Booking",
            fontSize = 12.sp,
            fontFamily = FontFamily(Font(R.font.instrument_sans_regular)),
            color = Color.Black,
            modifier = Modifier.align(Alignment.BottomStart)
        )
    }
}

@Composable
fun GridMedical() {
    Box(
        modifier = Modifier
            .size(120.dp)
            .border(width = 0.5.dp, color = Color.Gray, shape = RoundedCornerShape(12.dp))
            .background(color = Color.White, shape = RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color = Color.LightGray, shape = CircleShape)
                .align(Alignment.TopStart)
                .zIndex(1f)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_email),
                contentDescription = "Icon",
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.Center)
            )
        }

        Text(
            text = "GP Booking",
            fontSize = 12.sp,
            fontFamily = FontFamily(Font(R.font.instrument_sans_regular)),
            color = Color.Black,
            modifier = Modifier.align(Alignment.BottomStart)
        )
    }
}

