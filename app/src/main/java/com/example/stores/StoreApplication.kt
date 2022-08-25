package com.example.stores

import android.app.Application
import androidx.room.Room

class StoreApplication : Application (){
    companion object{
        lateinit var dataBase: StoreDataBase
    }

    override fun onCreate() {
        super.onCreate()
        dataBase = Room.databaseBuilder(
            this,
            StoreDataBase::class.java,
            "DataBase").build()
    }
}