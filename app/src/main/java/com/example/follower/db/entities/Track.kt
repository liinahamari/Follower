package com.example.follower.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Track(@PrimaryKey val time: Long, val title: String)