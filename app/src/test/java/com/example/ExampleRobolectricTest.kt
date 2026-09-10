package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.EqualizerPreset
import com.example.model.RepeatMode
import com.example.viewmodel.MusicPlayerViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Música", appName)
  }

  @Test
  fun `verify initial songs and playback state in viewModel`() {
    val viewModel = MusicPlayerViewModel()
    val songs = viewModel.songs.value
    assertTrue("Songs repository should not be empty", songs.isNotEmpty())

    val firstSong = songs.first()
    assertNotNull(viewModel.currentSong.value)

    viewModel.playSong(firstSong)
    assertTrue("Should be playing after playSong", viewModel.isPlaying.value)
    assertEquals(firstSong.id, viewModel.currentSong.value?.id)

    viewModel.togglePlayPause()
    assertFalse("Should be paused after togglePlayPause", viewModel.isPlaying.value)

    viewModel.setPlaybackSpeed(1.5f)
    assertEquals(1.5f, viewModel.playbackSpeed.value, 0.01f)

    viewModel.setEqualizerPreset(EqualizerPreset.BASS_BOOST)
    assertEquals(EqualizerPreset.BASS_BOOST, viewModel.equalizerPreset.value)

    viewModel.cycleRepeatMode()
    assertEquals(RepeatMode.ONE, viewModel.repeatMode.value)
  }
}

