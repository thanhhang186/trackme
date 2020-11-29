package com.khtn.trackme.model

import androidx.room.*
import com.khtn.trackme.model.Location

/**
 * Created by NguyenHang on 11/23/2020.
 */

@Entity(tableName =  "Track")
class Track {
    @PrimaryKey
    @ColumnInfo(name = "id")
    var id: Long = 0

    @ColumnInfo(name = "distance")
    var distance: Double = 0.0

    @ColumnInfo(name = "duration")
    var duration: Long = 0

    @ColumnInfo(name = "averageSpeed")
    var averageSpeed: Double = 0.0

    @Ignore
    var locations: ArrayList<Location>? = null


    override fun toString(): String {
        return "Track(id=$id, distance=$distance, duration=$duration, averageSpeed=$averageSpeed, locations=$locations)"
    }

}