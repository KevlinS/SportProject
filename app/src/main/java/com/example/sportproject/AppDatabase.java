package com.example.sportproject;


import androidx.room.Database;
import androidx.room.RoomDatabase;


@Database(entities = {Runningdata.class}, version =1,exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract RunningDAO getRunningdataDAO();

}

