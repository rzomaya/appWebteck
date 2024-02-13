package com.example.appwebteck.Helper;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.appwebteck.FileManger;
import com.example.appwebteck.LocalDB.AppDatabase;
import com.example.appwebteck.LocalDB.Location.LocationTable;
import com.example.appwebteck.Sync;

import java.util.List;

public class BackgroundSyncServes extends Worker {
    private static final String TAG = "BackgroundSyncServes";

    private final Context context;

    public BackgroundSyncServes(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        AppDatabase db = AppDatabase.getINSTANCE(context);
        Sync sendSync = new Sync(context);

        List<LocationTable> locationTables = db.locationDao().getUnSyncedLocations();
       // FileManger.logThis( "send UnSyncedLocations service : " + locationTables.size());
        for (LocationTable locationTable : locationTables) {
            sendSync.sendJsonData (locationTable.locationJson, locationTable.id);
        }


        return Result.success();
    }
}
