package com.example.appwebteck.LocalDB.Location;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class LocationTable {

    @PrimaryKey(autoGenerate = true)
    public int id;
    @ColumnInfo(name = "locationJson")
    public String locationJson;

    @ColumnInfo(name = "isSynced")
    public boolean isSynced;


    public LocationTable( String locationJson ,boolean isSynced) {
        this.locationJson = locationJson;
        this.isSynced = isSynced;
    }
}

