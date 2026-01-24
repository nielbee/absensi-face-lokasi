package com.example.absensi.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserEntity (

    @PrimaryKey val nisn:String,
    val nama:String,
    val id_jurusan:String,
    val tahun_masuk:Int,
    val api_key:String

)