package com.example.voicejournal.ui.main.mainScreen.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.voicejournal.R
import com.example.voicejournal.ui.theme.Variables

@Composable
fun BottomNavPanel(navController2: NavHostController) {
    val navBackStackEntry by navController2.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    val bottomAppItems = listOf(
        BottomAppItem(name = "Notes", route = "home", painter = painterResource(id = R.drawable.notes_stack)),
        BottomAppItem(name = "Favorites", route = "favourite", painter = painterResource(id = R.drawable.favorite_icon)),
        BottomAppItem(name = "Calendar", route = "calendar", painter = painterResource(id = R.drawable.calendar_icon)),
        BottomAppItem(name = "Media", route = "media", painter = painterResource(id = R.drawable.media_file)),
    )

    NavigationBar(
        containerColor = Variables.SchemesPrimaryContainer,
        contentColor = Variables.SchemesOnPrimaryContainer,
        tonalElevation = 0.dp
    ) {
        bottomAppItems.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Variables.SchemesOnPrimary,
                    selectedTextColor = Variables.SchemesPrimary,
                    indicatorColor = Variables.SchemesPrimary,
                    unselectedIconColor = Variables.SchemesOnPrimaryContainer,
                    unselectedTextColor = Variables.SchemesOnSurfaceVariant
                ),
                icon = { Icon(painter = item.painter, contentDescription = item.name) },
                label = {
                    Text(
                        text = item.name,
                        style = TextStyle(
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            letterSpacing = 0.3.sp,
                        )
                    )
                },
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        navController2.navigate(item.route) {
                            popUpTo(navController2.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

data class BottomAppItem(
    val name: String,
    val painter: Painter,
    val route: String = "home",
)