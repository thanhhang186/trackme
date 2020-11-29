package com.khtn.trackme.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.khtn.trackme.model.Location

/**
 * Created by NguyenHang on 11/24/2020.
 */

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLocation(location: Location)

    @Query("SELECT * FROM Location WHERE trackId = :trackId ORDER BY id DESC")
    fun getLocationByTrackId(trackId: Long): List<Location>

}