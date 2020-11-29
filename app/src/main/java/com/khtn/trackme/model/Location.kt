package com.khtn.trackme.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.khtn.trackme.model.Track

/**
 * Created by NguyenHang on 11/24/2020.
 */

@Entity(
    tableName = "Location",
    foreignKeys = [ForeignKey(
        entity = Track::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("trackId")
    )]
)
class Location {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0

    @ColumnInfo(name = "trackId")
    var trackId: Long? = null

    @ColumnInfo(name = "latitude")
    var latitude: Double? = null

    @ColumnInfo(name = "longitude")
    var longitude: Double? = null

    constructor(trackId: Long, latitude: Double, longitude: Double) {
        this.trackId = trackId
        this.latitude = latitude
        this.longitude = longitude
    }


}