package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.SectionTab
import com.example.model.Song
import com.example.ui.components.AddToPlaylistDialog
import com.example.ui.components.AlbumsView
import com.example.ui.components.ArtistsView
import com.example.ui.components.AudioEnhancerBottomSheet
import com.example.ui.components.FoldersView
import com.example.ui.components.GenresView
import com.example.ui.components.HistoryView
import com.example.ui.components.MetadataEditorDialog
import com.example.ui.components.MiniPlayer
import com.example.ui.components.PermissionScreen
import com.example.ui.components.PlayerFullScreen
import com.example.ui.components.PlaylistsView
import com.example.ui.components.QueueBottomSheet
import com.example.ui.components.SearchBarView
import com.example.ui.components.SettingsDialog
import com.example.ui.components.SongCard
import com.example.ui.components.SongDetailsDialog
import com.example.viewmodel.MusicPlayerViewModel

@Composable
fun MainScreen(
    viewModel: MusicPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val artists by viewModel.artists.collectAsStateWithLifecycle()
    val albums by viewModel.albums.collectAsStateWithLifecycle()
    val folders by viewModel.folders.collectAsStateWithLifecycle()
    val genres by viewModel.genres.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val playHistory by viewModel.playHistory.collectAsStateWithLifecycle()
    val topPlayedSongs by viewModel.topPlayedSongs.collectAsStateWithLifecycle()

    val currentSong by viewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val playbackPositionMs by viewModel.playbackPositionMs.collectAsStateWithLifecycle()
    val isShuffleEnabled by viewModel.isShuffleEnabled.collectAsStateWithLifecycle()
    val repeatMode by viewModel.repeatMode.collectAsStateWithLifecycle()
    val queue by viewModel.queue.collectAsStateWithLifecycle()

    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val selectedArtist by viewModel.selectedArtist.collectAsStateWithLifecycle()
    val selectedAlbum by viewModel.selectedAlbum.collectAsStateWithLifecycle()
    val selectedFolder by viewModel.selectedFolder.collectAsStateWithLifecycle()
    val selectedGenre by viewModel.selectedGenre.collectAsStateWithLifecycle()
    val selectedPlaylist by viewModel.selectedPlaylist.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isSearchActive by viewModel.isSearchActive.collectAsStateWithLifecycle()
    val isPlayerExpanded by viewModel.isPlayerExpanded.collectAsStateWithLifecycle()
    val isQueueSheetVisible by viewModel.isQueueSheetVisible.collectAsStateWithLifecycle()
    val isSettingsDialogVisible by viewModel.isSettingsDialogVisible.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val visualizerBands by viewModel.visualizerBands.collectAsStateWithLifecycle()
    val playbackSpeed by viewModel.playbackSpeed.collectAsStateWithLifecycle()
    val equalizerPreset by viewModel.equalizerPreset.collectAsStateWithLifecycle()
    val bassBoostStrength by viewModel.bassBoostStrength.collectAsStateWithLifecycle()
    val virtualizerStrength by viewModel.virtualizerStrength.collectAsStateWithLifecycle()
    val selectedSongForDetails by viewModel.selectedSongForDetails.collectAsStateWithLifecycle()
    val sleepTimerMinutes by viewModel.sleepTimerMinutes.collectAsStateWithLifecycle()
    val volume by viewModel.volume.collectAsStateWithLifecycle()

    var songToEdit by remember { mutableStateOf<Song?>(null) }
    var songToAddToPlaylist by remember { mutableStateOf<Song?>(null) }
    var isAudioEnhancerVisible by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.importAudioUris(uris, context)
        }
    }

    val permissionToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var permissionGrantedState by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, permissionToRequest) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGrantedState = granted
        viewModel.setPermissionGranted(granted, context)
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Notification permission callback */ }

    LaunchedEffect(Unit) {
        viewModel.initialize(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notifGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            if (!notifGranted) {
                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (!permissionGrantedState) {
            permissionLauncher.launch(permissionToRequest)
        }
    }

    val favoriteSongs = songs.filter { it.isFavorite }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (!permissionGrantedState) {
                PermissionScreen(
                    isScanning = isScanning,
                    onRequestPermission = { permissionLauncher.launch(permissionToRequest) },
                    onRescan = {
                        if (permissionGrantedState) {
                            viewModel.scanDeviceMusic(context)
                        } else {
                            permissionLauncher.launch(permissionToRequest)
                        }
                    }
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    if (isSearchActive) {
                        SearchBarView(
                            query = searchQuery,
                            onQueryChange = viewModel::setSearchQuery,
                            onCloseSearch = { viewModel.toggleSearch(false) },
                            songs = songs,
                            artists = artists,
                            albums = albums,
                            currentSong = currentSong,
                            isPlaying = isPlaying,
                            onSongClick = { song -> viewModel.playSong(song, songs) },
                            onArtistClick = { artist ->
                                viewModel.selectTab(SectionTab.ARTISTS)
                                viewModel.selectArtist(artist)
                                viewModel.toggleSearch(false)
                            },
                            onAlbumClick = { album ->
                                viewModel.selectTab(SectionTab.ALBUMS)
                                viewModel.selectAlbum(album)
                                viewModel.toggleSearch(false)
                            },
                            onToggleFavorite = viewModel::toggleFavorite
                        )
                    } else {
                        // Header Bar with App Name ("Pura Música"), Rescan, Equalizer, Search, and Settings
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GraphicEq,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Pura Música",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = "${songs.size} canciones en biblioteca",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { filePickerLauncher.launch(arrayOf("audio/*")) },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("import_audio_header_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FolderOpen,
                                        contentDescription = "Importar canciones",
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.scanDeviceMusic(context) },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("rescan_header_btn")
                                ) {
                                    if (isScanning) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Volver a escanear música",
                                            tint = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { isAudioEnhancerVisible = true },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("audio_enhancer_header_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Equalizer,
                                        contentDescription = "Mejoras de Audio",
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.toggleSearch(true) },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("search_header_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Buscar",
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.setSettingsDialogVisible(true) },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("settings_header_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Ajustes",
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }

                        // Navigation Tabs (8 sections)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SectionTab.values().forEach { tab ->
                                val isSelected = selectedTab == tab
                                val countLabel = when (tab) {
                                    SectionTab.ALL_SONGS -> "${songs.size}"
                                    SectionTab.ARTISTS -> "${artists.size}"
                                    SectionTab.ALBUMS -> "${albums.size}"
                                    SectionTab.FOLDERS -> "${folders.size}"
                                    SectionTab.GENRES -> "${genres.size}"
                                    SectionTab.PLAYLISTS -> "${playlists.size}"
                                    SectionTab.FAVORITES -> "${favoriteSongs.size}"
                                    SectionTab.HISTORY -> "${playHistory.size}"
                                }

                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable { viewModel.selectTab(tab) }
                                        .testTag("tab_${tab.name}"),
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = tab.label,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) {
                                                MaterialTheme.colorScheme.onPrimary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = countLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) {
                                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Tab Contents
                        AnimatedContent(
                            targetState = selectedTab,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            modifier = Modifier.weight(1f),
                            label = "tab_content_anim"
                        ) { currentTab ->
                            when (currentTab) {
                                SectionTab.ALL_SONGS -> {
                                    if (isScanning && songs.isEmpty()) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(32.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(48.dp),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.height(20.dp))
                                            Text(
                                                text = "Escaneando música...",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Buscando archivos de audio en las carpetas de tu teléfono",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    } else if (songs.isEmpty()) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(32.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MusicNote,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                                modifier = Modifier.size(64.dp)
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = "Tu biblioteca está lista",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Puedes cargar pistas reales de demostración con sonido de alta fidelidad, importar archivos de audio desde tu almacenamiento o volver a escanear.",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(20.dp))
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Button(
                                                    onClick = { viewModel.loadDemoTracks(context) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                    modifier = Modifier.testTag("load_demo_tracks_btn")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.GraphicEq,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("🎵 Cargar música real de muestra")
                                                }

                                                Button(
                                                    onClick = { filePickerLauncher.launch(arrayOf("audio/*")) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                                    modifier = Modifier.testTag("import_audio_btn")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.FolderOpen,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("📂 Importar canciones del dispositivo")
                                                }

                                                TextButton(
                                                    onClick = { viewModel.scanDeviceMusic(context) },
                                                    modifier = Modifier.testTag("rescan_btn")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Refresh,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("🔄 Escanear almacenamiento")
                                                }
                                            }
                                        }
                                    } else {
                                        LazyColumn(
                                            modifier = Modifier.fillMaxSize(),
                                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            items(songs, key = { it.id }) { song ->
                                                SongCard(
                                                    song = song,
                                                    isCurrentSong = currentSong?.id == song.id,
                                                    isPlaying = isPlaying,
                                                    onSongClick = { viewModel.playSong(song, songs) },
                                                    onToggleFavorite = { viewModel.toggleFavorite(song.id) },
                                                    onEditMetadata = { songToEdit = song },
                                                    onAddToPlaylist = { songToAddToPlaylist = song },
                                                    onShowDetails = { viewModel.selectSongForDetails(song) }
                                                )
                                            }
                                        }
                                    }
                                }

                                SectionTab.ARTISTS -> {
                                    ArtistsView(
                                        artists = artists,
                                        selectedArtist = selectedArtist,
                                        allSongs = songs,
                                        currentSong = currentSong,
                                        isPlaying = isPlaying,
                                        onArtistClick = viewModel::selectArtist,
                                        onBackFromArtist = { viewModel.selectArtist(null) },
                                        onSongClick = { song -> viewModel.playSong(song) },
                                        onToggleFavorite = viewModel::toggleFavorite,
                                        onPlayAll = { artistSongs ->
                                            if (artistSongs.isNotEmpty()) {
                                                viewModel.playSong(artistSongs.first(), artistSongs)
                                            }
                                        }
                                    )
                                }

                                SectionTab.ALBUMS -> {
                                    AlbumsView(
                                        albums = albums,
                                        selectedAlbum = selectedAlbum,
                                        allSongs = songs,
                                        currentSong = currentSong,
                                        isPlaying = isPlaying,
                                        onAlbumClick = viewModel::selectAlbum,
                                        onBackFromAlbum = { viewModel.selectAlbum(null) },
                                        onSongClick = { song -> viewModel.playSong(song) },
                                        onToggleFavorite = viewModel::toggleFavorite,
                                        onPlayAll = { albumSongs ->
                                            if (albumSongs.isNotEmpty()) {
                                                viewModel.playSong(albumSongs.first(), albumSongs)
                                            }
                                        }
                                    )
                                }

                                SectionTab.FOLDERS -> {
                                    FoldersView(
                                        folders = folders,
                                        selectedFolder = selectedFolder,
                                        currentSong = currentSong,
                                        isPlaying = isPlaying,
                                        onSelectFolder = viewModel::selectFolder,
                                        onSongClick = { song, list -> viewModel.playSong(song, list) },
                                        onPlayFolder = { folderSongs, shuffle ->
                                            if (folderSongs.isNotEmpty()) {
                                                val toPlay = if (shuffle) folderSongs.shuffled() else folderSongs
                                                viewModel.playSong(toPlay.first(), toPlay)
                                            }
                                        },
                                        onToggleFavorite = viewModel::toggleFavorite,
                                        onEditSong = { songToEdit = it },
                                        onAddToPlaylist = { songToAddToPlaylist = it }
                                    )
                                }

                                SectionTab.GENRES -> {
                                    GenresView(
                                        genres = genres,
                                        selectedGenre = selectedGenre,
                                        currentSong = currentSong,
                                        isPlaying = isPlaying,
                                        onSelectGenre = viewModel::selectGenre,
                                        onSongClick = { song, list -> viewModel.playSong(song, list) },
                                        onPlayGenre = { genreSongs, shuffle ->
                                            if (genreSongs.isNotEmpty()) {
                                                val toPlay = if (shuffle) genreSongs.shuffled() else genreSongs
                                                viewModel.playSong(toPlay.first(), toPlay)
                                            }
                                        },
                                        onToggleFavorite = viewModel::toggleFavorite,
                                        onEditSong = { songToEdit = it },
                                        onAddToPlaylist = { songToAddToPlaylist = it }
                                    )
                                }

                                SectionTab.PLAYLISTS -> {
                                    PlaylistsView(
                                        playlists = playlists,
                                        selectedPlaylist = selectedPlaylist,
                                        currentSong = currentSong,
                                        isPlaying = isPlaying,
                                        onSelectPlaylist = viewModel::selectPlaylist,
                                        onCreatePlaylist = { name -> viewModel.createPlaylist(name) },
                                        onRenamePlaylist = { id, name -> viewModel.renamePlaylist(id, name) },
                                        onDeletePlaylist = { id -> viewModel.deletePlaylist(id) },
                                        onRemoveSongFromPlaylist = { playlistId, songId ->
                                            viewModel.removeSongFromPlaylist(playlistId, songId)
                                        },
                                        onSongClick = { song, list -> viewModel.playSong(song, list) },
                                        onPlayPlaylist = { playlistSongs, shuffle ->
                                            if (playlistSongs.isNotEmpty()) {
                                                val toPlay = if (shuffle) playlistSongs.shuffled() else playlistSongs
                                                viewModel.playSong(toPlay.first(), toPlay)
                                            }
                                        },
                                        onToggleFavorite = viewModel::toggleFavorite,
                                        onEditSong = { songToEdit = it }
                                    )
                                }

                                SectionTab.FAVORITES -> {
                                    if (favoriteSongs.isEmpty()) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(32.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.FavoriteBorder,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
                                                modifier = Modifier.size(64.dp)
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = "Aún no tienes favoritos",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Toca el icono de corazón en cualquier canción para guardarla en esta lista.",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    } else {
                                        LazyColumn(
                                            modifier = Modifier.fillMaxSize(),
                                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            items(favoriteSongs, key = { it.id }) { song ->
                                                SongCard(
                                                    song = song,
                                                    isCurrentSong = currentSong?.id == song.id,
                                                    isPlaying = isPlaying,
                                                    onSongClick = { viewModel.playSong(song, favoriteSongs) },
                                                    onToggleFavorite = { viewModel.toggleFavorite(song.id) },
                                                    onEditMetadata = { songToEdit = song },
                                                    onAddToPlaylist = { songToAddToPlaylist = song }
                                                )
                                            }
                                        }
                                    }
                                }

                                SectionTab.HISTORY -> {
                                    HistoryView(
                                        historyItems = playHistory,
                                        topPlayedSongs = topPlayedSongs,
                                        currentSong = currentSong,
                                        isPlaying = isPlaying,
                                        onSongClick = { song, list -> viewModel.playSong(song, list) },
                                        onToggleFavorite = viewModel::toggleFavorite,
                                        onClearHistory = viewModel::clearPlayHistory,
                                        onEditSong = { songToEdit = it },
                                        onAddToPlaylist = { songToAddToPlaylist = it }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Fixed Mini Player at bottom
            MiniPlayer(
                currentSong = currentSong,
                isPlaying = isPlaying,
                playbackPositionMs = playbackPositionMs,
                onPlayPauseClick = viewModel::togglePlayPause,
                onNextClick = viewModel::playNextTrack,
                onPreviousClick = viewModel::playPreviousTrack,
                onOpenPlayer = { viewModel.setPlayerExpanded(true) },
                visualizerBands = visualizerBands,
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            // Full-screen Player overlay
            PlayerFullScreen(
                isVisible = isPlayerExpanded,
                song = currentSong,
                isPlaying = isPlaying,
                playbackPositionMs = playbackPositionMs,
                isShuffleEnabled = isShuffleEnabled,
                repeatMode = repeatMode,
                onCollapse = { viewModel.setPlayerExpanded(false) },
                onPlayPauseClick = viewModel::togglePlayPause,
                onNextClick = viewModel::playNextTrack,
                onPreviousClick = viewModel::playPreviousTrack,
                onSeek = viewModel::seekTo,
                onToggleFavorite = {
                    currentSong?.let { viewModel.toggleFavorite(it.id) }
                },
                onToggleShuffle = viewModel::toggleShuffle,
                onCycleRepeat = viewModel::cycleRepeatMode,
                onOpenQueue = { viewModel.setQueueSheetVisible(true) },
                visualizerBands = visualizerBands,
                playbackSpeed = playbackSpeed,
                onCycleSpeed = viewModel::cyclePlaybackSpeed,
                sleepTimerMinutes = sleepTimerMinutes,
                volume = volume,
                onVolumeChange = viewModel::setVolume,
                onOpenEnhancer = { isAudioEnhancerVisible = true },
                modifier = Modifier.fillMaxSize()
            )

            // Audio Enhancer Bottom Sheet
            AudioEnhancerBottomSheet(
                isVisible = isAudioEnhancerVisible,
                onDismiss = { isAudioEnhancerVisible = false },
                equalizerPreset = equalizerPreset,
                onSelectEqualizer = viewModel::setEqualizerPreset,
                playbackSpeed = playbackSpeed,
                onSelectSpeed = viewModel::setPlaybackSpeed,
                sleepTimerMinutes = sleepTimerMinutes,
                onSelectSleepTimer = viewModel::setSleepTimer,
                volume = volume,
                onVolumeChange = viewModel::setVolume,
                visualizerBands = visualizerBands,
                isPlaying = isPlaying,
                bassBoostStrength = bassBoostStrength,
                onBassBoostChange = viewModel::setBassBoostStrength,
                virtualizerStrength = virtualizerStrength,
                onVirtualizerChange = viewModel::setVirtualizerStrength
            )

            // Queue Bottom Sheet
            QueueBottomSheet(
                isVisible = isQueueSheetVisible,
                queue = queue,
                currentSong = currentSong,
                isPlaying = isPlaying,
                onSongSelected = { song ->
                    viewModel.playSong(song)
                    viewModel.setQueueSheetVisible(false)
                },
                onDismiss = { viewModel.setQueueSheetVisible(false) }
            )

            // Settings Dialog
            SettingsDialog(
                isVisible = isSettingsDialogVisible,
                currentThemeMode = themeMode,
                onThemeModeChange = viewModel::setThemeMode,
                onDismiss = { viewModel.setSettingsDialogVisible(false) }
            )

            // Song Details Dialog
            selectedSongForDetails?.let { song ->
                SongDetailsDialog(
                    song = song,
                    onDismiss = { viewModel.selectSongForDetails(null) }
                )
            }

            // Metadata Tag Editor Dialog
            songToEdit?.let { song ->
                MetadataEditorDialog(
                    song = song,
                    onDismiss = { songToEdit = null },
                    onSave = { title, artist, album, genre ->
                        viewModel.updateSongMetadata(song.id, title, artist, album, genre)
                    }
                )
            }

            // Add To Playlist Dialog
            songToAddToPlaylist?.let { song ->
                AddToPlaylistDialog(
                    song = song,
                    playlists = playlists,
                    onDismiss = { songToAddToPlaylist = null },
                    onAddToPlaylist = { playlistId, s ->
                        viewModel.addSongToPlaylist(playlistId, s)
                    },
                    onCreatePlaylistAndAdd = { name, s ->
                        viewModel.createPlaylist(name, s.id)
                    }
                )
            }
        }
    }
}
