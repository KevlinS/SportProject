package com.example.sportproject;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "running_table")
public class Runningdata {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "finishtime")
    private String finishtime;

    @ColumnInfo(name = "starttime")
    private String starttime;

    @ColumnInfo(name = "distance")
    public double distance;

    public int getId() {
        return id;
    }

    public String getStarttime() {
        return starttime;
    }

    public void setStarttime(String running_starttime) {
        this.starttime = running_starttime;
    }

    public String getFinishtime() {
        return finishtime;
    }

    public void setFinishtime(String running_finishtime) {
        this.finishtime = running_finishtime;
    }

   public double getDistance() { return distance; }

    public void setDistance(double running_distance) {
        this.distance = running_distance;
    }

}