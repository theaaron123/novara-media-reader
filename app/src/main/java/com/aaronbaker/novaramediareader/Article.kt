package com.aaronbaker.novaramediareader

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Article(
        @ColumnInfo(name = "title") var title: String? = null,
        @ColumnInfo(name = "description") var description: String? = null,
        @ColumnInfo(name = "permalink") var permalink: String? = null,
        @ColumnInfo(name = "image") var image: String? = null,
        @ColumnInfo(name = "body") var body: String? = null,
) {
    @PrimaryKey(autoGenerate = true)
    var uid: Long = 0
}