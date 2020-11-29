package com.khtn.trackme.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.khtn.trackme.model.Track

/**
 * Created by NguyenHang on 11/23/2020.
 */

@Dao
interface TrackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTrack(track: Track)

    @Query("SELECT * FROM Track ORDER BY id DESC")
    fun getAllTracks(): List<Track>

}