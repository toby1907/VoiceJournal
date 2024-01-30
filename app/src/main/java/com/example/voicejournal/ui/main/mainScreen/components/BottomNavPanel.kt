package com.example.voicejournal.ui.main.mainScreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voicejournal.R
import com.example.voicejournal.ui.theme.Variables

@Composable
fun BottomNavPanel(){
    var selectedItem by remember { mutableIntStateOf(0) }
    val bottomAppItems = listOf(
        BottomAppItem(name = "Notes",painterResource(id = R.drawable.notes_stack) ),
        BottomAppItem(name = "Favorites",painterResource(id = R.drawable.favorite_icon) ),
        BottomAppItem(name = "Calendar",painterResource(id = R.drawable.calendar_icon)),
        BottomAppItem(name = "Notifications",painterResource(id = R.drawable.notifications_icon)),

        )

    NavigationBar( containerColor = Variables.SchemesPrimaryContainer,
        contentColor = Variables.SchemesOnPrimaryContainer) {
        bottomAppItems.forEachIndexed { index, item ->
            NavigationBarItem(
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor =Variables.SchemesOnSecondary ,
                    selectedTextColor = Variables.SchemesOnSurface,
                    indicatorColor = Variables.SchemesSecondary ,
                    unselectedIconColor =Variables.SchemesOnPrimaryContainer ,
                    unselectedTextColor =Variables.SchemesOnSurfaceVariant
                ),
                icon = { Icon(painter = item.painter, contentDescription = item.name) },
                label = {
                    Text(
                        text = item.name,
                        style = TextStyle(
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight(500),
                            color = Color(0xFF49454F),

                            textAlign = TextAlign.Center,
                            letterSpacing = 0.5.sp,
                        )
                    )
                        },
                selected = selectedItem == index,
                onClick = { selectedItem = index }
            )
        }
    }



  /*  BottomAppBar(
        actions = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween // evenly space the iconbuttons
            ) {
                IconButton(
                    modifier = Modifier
                        .width(80.dp)
                        .height(80.dp)
                        .padding(top = 12.dp, bottom = 16.dp),
                    onClick = { *//* doSomething() *//* }) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(
                            4.dp,
                            Alignment.CenterVertically
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.notes_stack),
                            contentDescription = "Notes Stack"
                        )
                        Text(
                            text = "Notes",
                            style = TextStyle(
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                fontWeight = FontWeight(500),
                                color = Color(0xFF49454F),

                                textAlign = TextAlign.Center,
                                letterSpacing = 0.5.sp,
                            )
                        )
                    }
                }
                IconButton(modifier = Modifier
                    .width(80.dp)
                    .height(80.dp)
                    .padding(top = 12.dp, bottom = 16.dp),
                    onClick = { *//* doSomething() *//* }) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(
                            4.dp,
                            Alignment.CenterVertically
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.favorite_icon),
                            contentDescription = "Favorite Icon"
                        )
                        Text(
                            text = "Favorite", style = TextStyle(
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                fontWeight = FontWeight(500),
                                color = Color(0xFF49454F),

                                textAlign = TextAlign.Center,
                                letterSpacing = 0.5.sp,
                            )
                        )
                    }
                }
                IconButton(modifier = Modifier
                    .width(80.dp)
                    .height(80.dp)
                    .padding(top = 12.dp, bottom = 16.dp),
                    onClick = { *//* doSomething() *//* }) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(
                            4.dp,
                            Alignment.CenterVertically
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
// Child views.
                        Icon(
                            painter = painterResource(id = R.drawable.calendar_icon),
                            contentDescription = "Calendar Icon"
                        )
                        Text(
                            text = "Calendar",
                            style = TextStyle(
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                fontWeight = FontWeight(500),
                                color = Color(0xFF49454F),

                                textAlign = TextAlign.Center,
                                letterSpacing = 0.5.sp,
                            )
                        )
                    }

                }
                IconButton(modifier = Modifier
                    .width(80.dp)
                    .height(80.dp)
                    .padding(top = 12.dp, bottom = 16.dp),
                    onClick = { *//* doSomething() *//* }) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(
                            4.dp,
                            Alignment.CenterVertically
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.notifications_icon),
                            contentDescription = "Notifications Icon"
                        )
                        Text(
                            text = "Notifications",
                            style = TextStyle(
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                fontWeight = FontWeight(500),
                                color = Color(0xFF49454F),

                                textAlign = TextAlign.Center,
                                letterSpacing = 0.5.sp,
                            )

                        )
                    }

                }
            }
        },
        containerColor = Variables.SchemesPrimaryContainer,
        contentColor = Variables.SchemesOnPrimaryContainer
    )*/
}

data class BottomAppItem(val name:String,val painter: Painter)