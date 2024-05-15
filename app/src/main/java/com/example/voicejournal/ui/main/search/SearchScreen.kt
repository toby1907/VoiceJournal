package com.example.voicejournal.ui.main.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.voicejournal.R
import com.example.voicejournal.ui.theme.Variables
import com.example.voicejournal.ui.theme.Variables.SchemesSecondary
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class)
@Composable
fun SearchScreen(navController: NavHostController, function: () -> Unit) {
val viewModel: SearchScreenViewModel = hiltViewModel()
    val viewState = viewModel.viewState.collectAsState().value
    val searchFieldState = viewModel.searchFieldState.collectAsState().value
    val inputText = viewModel.inputText.collectAsState().value

    LaunchedEffect(null) {
        viewModel.initialize()
    }

    SearchScreenLayout(
        viewState = viewState,
        searchFieldState = searchFieldState,
        inputText = inputText,
        onSearchInputChanged = { input -> viewModel.updateInput(input) },
        onSearchInputClicked = { viewModel.searchFieldActivated() },
        onClearInputClicked = { viewModel.clearInput() },
        onChevronClicked = { viewModel.revertToInitialState() },
        onItemClicked = {  }

    )
}


@Composable
fun SearchInputField(
    searchFieldState: SearchScreenViewModel.SearchFieldState,
    inputText: String,
    onSearchInputChanged: (String) -> Unit,
    onClearInputClicked: () -> Unit,
    onClicked: () -> Unit,
    onChevronClicked: () -> Unit,
) {
    val leadingIcon: @Composable (() -> Unit) = when (searchFieldState) {
        SearchScreenViewModel.SearchFieldState.Idle -> {
            {
                SearchIcon()
            }
        }
        SearchScreenViewModel.SearchFieldState.EmptyActive,
        SearchScreenViewModel.SearchFieldState.WithInputActive -> {
            { ChevronIcon(onClick = onChevronClicked) }
        }
    }
    val trailingIcon: @Composable (() -> Unit)? = when (searchFieldState) {
        is SearchScreenViewModel.SearchFieldState.WithInputActive -> {
            { ClearIcon(onClick = onClearInputClicked) }
        }

        else -> null
    }
    val colors: TextFieldColors = when (searchFieldState) {
        SearchScreenViewModel.SearchFieldState.Idle -> searchFieldColorsStateIdle()
        SearchScreenViewModel.SearchFieldState.EmptyActive,
        SearchScreenViewModel.SearchFieldState.WithInputActive -> searchFieldColorsStateActive()
    }
    val color: Color = when (searchFieldState) {
        SearchScreenViewModel.SearchFieldState.EmptyActive -> Variables.SchemesOnSurfaceVariant
        SearchScreenViewModel.SearchFieldState.Idle -> Variables.SchemesSecondary
        SearchScreenViewModel.SearchFieldState.WithInputActive -> Variables.SchemesOnSurfaceVariant
        }

    androidx.compose.material3.TextField(
        value = inputText,
        onValueChange = { newInput: String -> onSearchInputChanged(newInput) },
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        trailingIcon = trailingIcon,
        leadingIcon = leadingIcon,
        colors = colors,
        placeholder = {
            Text(
                text = "What are you searching for?",
                color = color
            )
        },
        interactionSource = remember { MutableInteractionSource() }.also { interactionSource ->
            LaunchedEffect(key1 = interactionSource) {
                interactionSource.interactions.collect { interaction ->
                    if (interaction is PressInteraction.Release) {
                        onClicked.invoke()
                    }
                }
            }
        },
    )
}

@Composable
fun ClearIcon(onClick: () -> Unit) {
    IconButton(onClick = { onClick() } ) {
        Icon(imageVector = Icons.Default.Clear, contentDescription = "Back")
    }
}

@Composable
fun SearchIcon() {
    IconButton(onClick = { } ) {
        Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
    }
}

@Composable
fun ChevronIcon(onClick: () -> Unit) {
    IconButton(onClick = { onClick() } ) {
        Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Chevron")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun searchFieldColorsStateIdle() = TextFieldDefaults.textFieldColors(
    containerColor = Variables.SchemesOnPrimary,// color_soft_white
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
    disabledTextColor = Variables.SchemesSecondary.copy(alpha = 0.6f),
    cursorColor = Color.Transparent,
    focusedLabelColor = Color.Transparent,
    unfocusedLabelColor = Color.Transparent,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun searchFieldColorsStateActive() = TextFieldDefaults.textFieldColors(
    containerColor = Variables.SchemesSecondary,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
    /* textColor = color_soft_white,*/
    disabledTextColor = Variables.SchemesOnPrimary,
    cursorColor = SchemesSecondary,
    focusedLabelColor = Color.Transparent,
    unfocusedLabelColor = Color.Transparent,
)
@Composable
private fun SearchHeader(
    searchFieldState: SearchScreenViewModel.SearchFieldState
) {
    AnimatedVisibility(visible = searchFieldState == SearchScreenViewModel.SearchFieldState.Idle) {
        Text(
            text = "Search",
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp, top = 16.dp),
            style = TextStyle(
                fontWeight = FontWeight(700),
                fontSize = 32.sp,
            )
        )
    }
}

@Composable
private fun SearchScreenLayout(
    viewState: SearchScreenViewModel.ViewState,
    searchFieldState: SearchScreenViewModel.SearchFieldState,
    inputText: String,
    onSearchInputChanged: (String) -> Unit,
    onSearchInputClicked: () -> Unit,
    onClearInputClicked: () -> Unit,
    onChevronClicked: () -> Unit,
    onItemClicked: (SearchResult) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Variables.SchemesSecondary)
    ) {
        SearchHeader(searchFieldState = searchFieldState)
        SearchInputField(
            searchFieldState = searchFieldState,
            inputText = inputText,
            onClearInputClicked = onClearInputClicked,
            onSearchInputChanged = onSearchInputChanged,
            onClicked = onSearchInputClicked,
            onChevronClicked = onChevronClicked,
        )
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Variables.SchemesOnSurfaceVariant.copy(alpha = 0.2f))
        )
        when (viewState) {
            SearchScreenViewModel.ViewState.IdleScreen -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Image(
                        // Just a stock image from UnDraw
                        painter = painterResource(id = R.drawable.baseline_graphic_eq_black_24),
                        contentDescription = "Illustration",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            SearchScreenViewModel.ViewState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Error :(", color = Variables.SchemesOnPrimary)
                }
            }

            SearchScreenViewModel.ViewState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            SearchScreenViewModel.ViewState.NoResults -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No results for this input :(", color = Variables.SchemesOnPrimary)
                }
            }

            is SearchScreenViewModel.ViewState.SearchResultsFetched -> {
                SearchResultsList(items = viewState.results, onItemClicked = onItemClicked)
            }
        }
    }
}
@Composable
fun SearchResultsList(items: List<SearchResult>, onItemClicked: (SearchResult) -> Unit) {
    LazyColumn {
        itemsIndexed(items = items) { index, searchResult ->
            Column(modifier = Modifier.fillMaxWidth().clickable { onItemClicked.invoke(searchResult) }) {
                Spacer(
                    modifier = Modifier.height(height = if (index == 0) 16.dp else 4.dp)
                )
                Text(
                    text = searchResult.title,
                    color = Variables.SchemesOnPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = searchResult.subtitle,
                    color = Variables.SchemesOnSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .padding(start = 16.dp)
                        .background(Variables.SchemesOnSurfaceVariant.copy(alpha = 0.2f))
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}