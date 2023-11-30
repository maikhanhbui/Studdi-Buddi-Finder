package com.group7.studdibuddi.session

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session_table")
data class Session (

    @PrimaryKey(autoGenerate = true)
    var sessionId: Long = 0L,

    @ColumnInfo(name = "session_name_column")
    var sessionName: String = "",

    @ColumnInfo(name = "course_id_column")
    var courseId: String = "",

    @ColumnInfo(name = "location_column")
    var location: Int = -1,

    @ColumnInfo(name = "latitude_column")
    var latitude: Double = 49.279,

    @ColumnInfo(name = "longitude_column")
    var longitude: Double = -122.918,

    @ColumnInfo(name = "description_column")
    var description: String = "",

    @ColumnInfo(name = "owner_id_column")
    var ownerId: String = "",

    @ColumnInfo(name = "is_public_column")
    var isPublic: Boolean = false
)