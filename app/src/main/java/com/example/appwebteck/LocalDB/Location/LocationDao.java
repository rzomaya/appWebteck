package com.example.appwebteck.LocalDB.Location;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LocationDao {

    @Query("SELECT * FROM LocationTable WHERE isSynced = 0")
    List<LocationTable> getUnSyncedLocations();

    @Query("UPDATE LocationTable SET isSynced = 1 WHERE id = :id")
    void updateSyncedLocation(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertLocation(LocationTable...locationTable);

    @Query("DELETE FROM LocationTable WHERE id = :id")
    void deleteLocation(int id);



    @Delete
    void delete(LocationTable locationTable);


}

