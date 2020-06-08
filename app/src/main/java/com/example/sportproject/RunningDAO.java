package com.example.sportproject;



import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface RunningDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(Runningdata runningdata);

    @Update
    public void update(Runningdata runningdata);

    @Delete
    public void delete(Runningdata runningdata);

    @Query("SELECT * FROM running_table")
    public List<Runningdata> getAllRuningdata();

    @Query("DELETE FROM running_table WHERE id = :runningId")
    abstract void deleteById(int runningId);// Supprimer by ID

    @Query("DELETE FROM running_table")
    abstract void deleteall();



}