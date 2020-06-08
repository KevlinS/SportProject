package com.example.sportproject;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "running_table")
public class Runningdata {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "calorie")
    private double calorie;

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

    public double getCalorie() {
        return calorie;
    }

    public void setCalorie(double running_calorie) {
        this.calorie = running_calorie;
    }

   public double getDistance() { return distance; }

    public void setDistance(double running_distance) {
        this.distance = running_distance;
    }

}