package com.example.data

import android.content.Context
import android.content.SharedPreferences

object MusicRepository {
    private const val PREFS_NAME = "music_player_prefs"
    private const val KEY_FAVORITES = "favorite_song_ids"

    fun getFavoriteIds(context: Context): Set<String> {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }

    fun saveFavoriteIds(context: Context, favoriteIds: Set<String>) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_FAVORITES, favoriteIds).apply()
    }
}
