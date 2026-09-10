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
    assertTrue(viewModel.songs.value.isEmpty())
    assertFalse(viewModel.isPlaying.value)
    assertEquals(0.85f, viewModel.volume.value, 0.01f)

    viewModel.setPlaybackSpeed(1.5f)
    assertEquals(1.5f, viewModel.playbackSpeed.value, 0.01f)

    viewModel.setEqualizerPreset(EqualizerPreset.BASS_BOOST)
    assertEquals(EqualizerPreset.BASS_BOOST, viewModel.equalizerPreset.value)

    viewModel.cycleRepeatMode()
    assertEquals(RepeatMode.ONE, viewModel.repeatMode.value)

    viewModel.setVolume(0.5f)
    assertEquals(0.5f, viewModel.volume.value, 0.01f)
  }
}

