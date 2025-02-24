package com.shub39.rush.lyrics.presentation.setting

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import com.shub39.rush.R
import com.shub39.rush.core.data.Settings
import com.shub39.rush.core.domain.AppTheme
import com.shub39.rush.core.domain.CardColors
import com.shub39.rush.core.domain.Route
import com.shub39.rush.core.presentation.theme.RushTheme
import com.shub39.rush.lyrics.presentation.setting.component.BetterIconButton
import kotlinx.coroutines.launch

@SuppressLint("NewApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingPage(
    notificationAccess: Boolean,
    action: (SettingsPageAction) -> Unit,
    navigator: (Route) -> Unit,
    settings: Settings,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var deleteButtonStatus by remember { mutableStateOf(true) }
    var deleteConfirmationDialog by remember { mutableStateOf(false) }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 500.dp)
                    .fillMaxSize()
            ) {
                item {
                    TopAppBar(
                        title = {
                            Text(stringResource(R.string.settings))
                        }
                    )
                }

                item {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.theme)) },
                        trailingContent = {
                            Row {
                                val darkTheme = isSystemInDarkTheme()
                                val themes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    listOf(AppTheme.MATERIAL, AppTheme.YELLOW, AppTheme.LIME)
                                } else {
                                    listOf(AppTheme.YELLOW, AppTheme.LIME)
                                }

                                themes.forEach { theme ->
                                    val containerColor = when (theme) {
                                        AppTheme.YELLOW -> if (darkTheme) Color(0xFFDBC66E) else Color(0xFF6D5E0F)
                                        AppTheme.LIME -> if (darkTheme) Color(0xFFB1D18A) else  Color(0xFF4C662B)
                                        AppTheme.MATERIAL -> if (darkTheme) dynamicDarkColorScheme(context).primary else dynamicLightColorScheme(context).primary
                                    }

                                    IconButton(
                                        onClick = { action(SettingsPageAction.OnUpdateTheme(theme.type)) },
                                        colors = IconButtonDefaults.iconButtonColors(
                                            containerColor = containerColor
                                        )
                                    ) {
                                        AnimatedVisibility(
                                            visible = settings.toggleTheme == theme.type,
                                            enter = fadeIn(),
                                            exit = fadeOut()
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.round_check_circle_outline_24),
                                                tint = contentColorFor(containerColor),
                                                contentDescription = "Check Mark"
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    )
                }

                item {
                    ListItem(
                        headlineContent = { Text(text = stringResource(id = R.string.vibrant_colors)) },
                        supportingContent = { Text(text = stringResource(id = R.string.vibrant_colors_info)) },
                        trailingContent = {
                            Switch(
                                checked = settings.lyricsColor == CardColors.VIBRANT.color,
                                onCheckedChange = {
                                    coroutineScope.launch {
                                        when (it) {
                                            true -> action(
                                                SettingsPageAction.OnUpdateLyricsColor(
                                                    CardColors.VIBRANT.color
                                                )
                                            )

                                            else -> action(
                                                SettingsPageAction.OnUpdateLyricsColor(
                                                    CardColors.MUTED.color
                                                )
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    )
                }

                item {
                    ListItem(
                        headlineContent = { Text(text = stringResource(id = R.string.max_lines)) },
                        supportingContent = {
                            Column {
                                Text(settings.maxLines.toString())

                                Slider(
                                    value = settings.maxLines.toFloat(),
                                    valueRange = 2f..8f,
                                    steps = 5,
                                    onValueChange = {
                                        action(SettingsPageAction.OnUpdateMaxLines(it.toInt()))
                                    }
                                )
                            }
                        }
                    )
                }

                item {
                    ListItem(
                        headlineContent = { Text(text = stringResource(id = R.string.delete_all)) },
                        trailingContent = {
                            BetterIconButton(
                                onClick = { deleteConfirmationDialog = true },
                                enabled = deleteButtonStatus
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null
                                )
                            }
                        }
                    )
                }

                item {
                    ListItem(
                        headlineContent = { Text(text = stringResource(id = R.string.batch_download)) },
                        supportingContent = { Text(text = stringResource(id = R.string.batch_download_info)) },
                        trailingContent = {
                            BetterIconButton(
                                onClick = { navigator(Route.BatchDownloaderPage) },
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.round_arrow_forward_ios_24),
                                    contentDescription = null
                                )
                            }
                        }
                    )
                }

                item {
                    ListItem(
                        headlineContent = { Text(text = stringResource(R.string.backup)) },
                        supportingContent = { Text(text = stringResource(R.string.backup_info)) },
                        trailingContent = {
                            BetterIconButton(
                                onClick = { navigator(Route.BackupPage) }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.round_arrow_forward_ios_24),
                                    contentDescription = null
                                )
                            }
                        }
                    )
                }

                if (!notificationAccess) {
                    item {
                        val intent =
                            Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")

                        ListItem(
                            headlineContent = { Text(text = stringResource(id = R.string.grant_permission)) },
                            supportingContent = { Text(text = stringResource(id = R.string.notification_permission)) },
                            trailingContent = {
                                BetterIconButton(
                                    onClick = { context.startActivity(intent) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null
                                    )
                                }
                            }
                        )
                    }
                }

                item {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.about)) },
                        trailingContent = {
                            BetterIconButton(
                                onClick = { navigator(Route.AboutPage) }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.round_arrow_forward_ios_24),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    )
                }
            }
        }
    }

    if (deleteConfirmationDialog) {
        BasicAlertDialog(
            onDismissRequest = { deleteConfirmationDialog = false }
        ) {
            Card(shape = MaterialTheme.shapes.extraLarge) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 300.dp)
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.round_warning_24),
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.padding(8.dp))

                    Text(
                        text = stringResource(R.string.delete_all),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = stringResource(id = R.string.delete_confirmation),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.padding(8.dp))

                    Button(
                        onClick = {
                            action(SettingsPageAction.OnDeleteSongs)
                            deleteConfirmationDialog = false
                            deleteButtonStatus = false
                        },
                        shape = MaterialTheme.shapes.extraLarge,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = stringResource(id = R.string.delete_all))
                    }
                }
            }
        }
    }
}

@Preview(
    showSystemUi = true, showBackground = true, backgroundColor = 0xFFFFFFFF,
    device = "spec:width=411dp,height=891dp,dpi=160",
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE
)
@Composable
private fun SettingPagePreview() {
    RushTheme(theme = "Yellow") {
        SettingPage(
            action = {},
            notificationAccess = false,
            settings = Settings(),
            navigator = {}
        )
    }
}