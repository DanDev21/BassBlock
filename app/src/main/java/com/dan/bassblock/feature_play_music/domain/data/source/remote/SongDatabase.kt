package com.dan.bassblock.feature_play_music.domain.data.source.remote

import com.dan.bassblock.feature_play_music.domain.data.model.Song
import com.dan.bassblock.feature_play_music.domain.util.Constants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class SongDatabase {

    private val songsCollection = FirebaseFirestore.getInstance()
        .collection(Constants.COLLECTION_SONGS)

    suspend fun getSongs() : List<Song> =
        try {
            this.songsCollection.get().await().toObjects(Song::class.java)
        } catch (_: Exception) {
            emptyList()
        }
}