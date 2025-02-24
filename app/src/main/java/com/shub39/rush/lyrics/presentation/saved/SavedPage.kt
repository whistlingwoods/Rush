package com.shub39.rush.lyrics.presentation.saved

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shub39.rush.R
import com.shub39.rush.core.data.Settings
import com.shub39.rush.core.domain.Route
import com.shub39.rush.core.presentation.ArtFromUrl
import com.shub39.rush.lyrics.presentation.lyrics.SongUi
import com.shub39.rush.lyrics.presentation.lyrics.component.Empty
import com.shub39.rush.lyrics.presentation.saved.component.GroupedCard
import com.shub39.rush.lyrics.presentation.saved.component.SongCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedPage(
    state: SavedPageState,
    currentSong: SongUi?,
    notificationAccess: Boolean,
    action: (SavedPageAction) -> Unit,
    settings: Settings,
    navigator: (Route) -> Unit
) {
    val sortOrderChips = remember { SortOrder.entries.toTypedArray() }

    var showInfoDialog by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    AnimatedContent(
                        targetState = currentSong
                    ) {
                        when (it) {
                            null -> {}
                            else -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(0.6f)
                                        .clickable { navigator(Route.LyricsGraph) }
                                ) {
                                    ArtFromUrl(
                                        imageUrl = it.artUrl,
                                        modifier = Modifier
                                            .clip(MaterialTheme.shapes.extraSmall)
                                    )

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column {
                                        Text(
                                            text = it.title,
                                            maxLines = 1,
                                            style = MaterialTheme.typography.titleMedium,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Text(
                                            text = it.artists,
                                            maxLines = 1,
                                            style = MaterialTheme.typography.bodyMedium,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (notificationAccess) {
                        IconButton(
                            onClick = {
                                action(SavedPageAction.OnToggleAutoChange)
                                if (!state.autoChange) {
                                    navigator(Route.LyricsGraph)
                                }
                            },
                            colors = if (state.autoChange) {
                                IconButtonDefaults.filledIconButtonColors()
                            } else {
                                IconButtonDefaults.iconButtonColors()
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.rush_transparent),
                                contentDescription = null,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { action(SavedPageAction.OnToggleSearchSheet) }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.round_search_24),
                            contentDescription = null
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 500.dp)
                    .fillMaxSize()
            ) {
                TopAppBar(
                    title = { Text(stringResource(R.string.saved)) },
                    actions = {
                        if (notificationAccess) {
                            IconButton(
                                onClick = { showInfoDialog = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Info"
                                )
                            }
                        }

                        IconButton(
                            onClick = { navigator(Route.SettingsGraph) }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.round_settings_24),
                                contentDescription = null
                            )
                        }
                    }
                )

                if (state.songsAsc.isEmpty()) {

                    Empty()

                } else {

                    LazyRow(
                        modifier = Modifier.animateContentSize()
                    ) {
                        items(sortOrderChips, key = { it.textId }) {
                            FilterChip(
                                selected = it.sortOrder == settings.sortOrder,
                                onClick = {
                                    action(SavedPageAction.UpdateSortOrder(it.sortOrder))
                                },
                                label = { Text(stringResource(id = it.textId)) },
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }

                    AnimatedContent(
                        targetState = settings.sortOrder
                    ) { sortOrder ->
                        when (sortOrder) {
                            SortOrder.DATE_ADDED.sortOrder -> {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .animateContentSize()
                                ) {
                                    items(state.songsByTime, key = { it.id }) {
                                        SongCard(
                                            result = it,
                                            onDelete = {
                                                action(SavedPageAction.OnDeleteSong(it))
                                            },
                                            onClick = {
                                                action(SavedPageAction.ChangeCurrentSong(it.id))
                                                navigator(Route.LyricsGraph)
                                            }
                                        )
                                    }

                                    item {
                                        Spacer(modifier = Modifier.padding(60.dp))
                                    }
                                }
                            }

                            SortOrder.TITLE_ASC.sortOrder -> {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .animateContentSize()
                                ) {
                                    items(state.songsAsc, key = { it.id }) {
                                        SongCard(
                                            result = it,
                                            onDelete = {
                                                action(SavedPageAction.OnDeleteSong(it))
                                            },
                                            onClick = {
                                                action(SavedPageAction.ChangeCurrentSong(it.id))
                                                navigator(Route.LyricsGraph)
                                            }
                                        )
                                    }

                                    item {
                                        Spacer(modifier = Modifier.padding(60.dp))
                                    }
                                }
                            }

                            SortOrder.TITLE_DESC.sortOrder -> {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .animateContentSize()
                                ) {
                                    items(state.songsDesc, key = { it.id }) {
                                        SongCard(
                                            result = it,
                                            onDelete = {
                                                action(SavedPageAction.OnDeleteSong(it))
                                            },
                                            onClick = {
                                                action(SavedPageAction.ChangeCurrentSong(it.id))
                                                navigator(Route.LyricsGraph)
                                            }
                                        )
                                    }

                                    item {
                                        Spacer(modifier = Modifier.padding(60.dp))
                                    }
                                }
                            }

                            SortOrder.ARTISTS_ASC.sortOrder -> {
                                var expandedCardId by rememberSaveable {
                                    mutableStateOf<String?>(
                                        null
                                    )
                                }

                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .animateContentSize()
                                ) {
                                    items(state.groupedArtist, key = { it.key }) { map ->
                                        GroupedCard(
                                            map = map,
                                            isExpanded = expandedCardId == map.key,
                                            onClick = {
                                                action(SavedPageAction.ChangeCurrentSong(it.id))
                                                navigator(Route.LyricsGraph)
                                            },
                                            onCardClick = {
                                                expandedCardId =
                                                    if (expandedCardId == map.key) null else map.key
                                            }
                                        )
                                    }

                                    item {
                                        Spacer(modifier = Modifier.padding(60.dp))
                                    }
                                }
                            }

                            SortOrder.ALBUM_ASC.sortOrder -> {
                                var expandedCardId by rememberSaveable {
                                    mutableStateOf<String?>(
                                        null
                                    )
                                }

                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .animateContentSize()
                                ) {
                                    items(state.groupedAlbum, key = { it.key }) { map ->
                                        GroupedCard(
                                            map = map,
                                            isExpanded = expandedCardId == map.key,
                                            onClick = {
                                                action(SavedPageAction.ChangeCurrentSong(it.id))
                                                navigator(Route.LyricsGraph)
                                            },
                                            onCardClick = {
                                                expandedCardId =
                                                    if (expandedCardId == map.key) null else map.key
                                            }
                                        )
                                    }

                                    item {
                                        Spacer(modifier = Modifier.padding(60.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showInfoDialog) {
        BasicAlertDialog(
            onDismissRequest = { showInfoDialog = false }
        ) {
            Card {
                Column(
                    modifier = Modifier
                        .widthIn(max = 500.dp)
                        .fillMaxWidth()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(R.drawable.rush_transparent),
                        modifier = Modifier.padding(32.dp).size(128.dp),
                        contentDescription = "App Icon"
                    )

                    Text(
                        text = stringResource(R.string.rush_mode),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = stringResource(R.string.rush_mode_desc),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}