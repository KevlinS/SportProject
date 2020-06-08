package com.example.sportproject;


import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Runningdata.class}, version =1,exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract RunningDAO getRunningdataDAO();

}

