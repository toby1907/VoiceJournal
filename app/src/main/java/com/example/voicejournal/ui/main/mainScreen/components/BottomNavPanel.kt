package com.example.voicejournal.ui.main.mainScreen.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.voicejournal.R
import com.example.voicejournal.ui.theme.Variables

@Composable
fun BottomNavPanel(navController2: NavHostController) {
    var selectedItem by remember { mutableIntStateOf(0) }
    val bottomAppItems = listOf(
        BottomAppItem(name = "Notes",painterResource(id = R.drawable.notes_stack) ),
        BottomAppItem(name = "Favorites",painterResource(id = R.drawable.favorite_icon) ),
        BottomAppItem(name = "Calendar",painterResource(id = R.drawable.calendar_icon)),
        BottomAppItem(name = "Media",painterResource(id = R.drawable.media_file)),

        )
   LaunchedEffect(Unit){
       if (selectedItem == 0) {
           navController2.navigate("home") {
               popUpTo(0)
           }
       }
   }

    NavigationBar( containerColor = Variables.SchemesPrimaryContainer,
        contentColor = Variables.SchemesOnPrimaryContainer
    ) {
        bottomAppItems.forEachIndexed { index, item ->
            NavigationBarItem(
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor =Variables.SchemesOnSecondary ,
                    selectedTextColor = Variables.SchemesSecondary,
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
                            textAlign = TextAlign.Center,
                            letterSpacing = 0.5.sp,
                        )
                    )
                        },
                selected = selectedItem == index,
                onClick = {
                    selectedItem = index
                    if (index == 0) {
                        navController2.navigate("home") {
                            popUpTo(0)
                        }
                    }
                    if (index == 1) {
                    navController2.navigate("favourite") {
                        popUpTo(0)
                    }

                }
                    if (index == 2) {
                        navController2.navigate("calendar") {
                            popUpTo(0)
                        }
                    }
                    if (index == 3) {
                        navController2.navigate("media") {
                            popUpTo(0)
                        }
                    }
                }
            )
        }
    }

}

data class BottomAppItem(val name:String,val painter: Painter)