package com.example.appwebteck.Location;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import com.example.appwebteck.LocalDB.AppDatabase;
import com.example.appwebteck.LocalDB.Location.LocationTable;
import com.example.appwebteck.R;
import com.example.appwebteck.Sync;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;


public class LocationService extends Service {
    private static final String TAG = "LocationService";

    private static final String CHANNEL_ID = "appWebTeckLocationServiceChannel";
    private static final int NOTIFICATION_ID = 12345;

    private FusedLocationProviderClient fusedLocationClient;

    private LocationCallback locationCallback;

    private LocationRequest locationRequest;

    private int locationInerval = 90 * 1000;// 90 seconds

    private int locationMinUpdateInterval = 60 * 1000; // 60 seconds

    private int locationMaxUpdateDelay = 30 * 1000;// 30 seconds

    private  int locationMinUpdateDistance = 1000; // 10 meters
   private PowerManager powerManager ;

   private PowerManager.WakeLock wakeLock;


    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(NOTIFICATION_ID, createNotification());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {

                    if (shouldTrack()){
                        sendLocationToServer(location, getApplicationContext());
                    }else{
                        stopSelf();
                        fusedLocationClient.removeLocationUpdates(locationCallback);
                        if (wakeLock != null && wakeLock.isHeld()){
                            wakeLock.release();
                        }

                    }
                   // FileManger.logThis("Location Update callback");
                }
            }
        };

        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, locationInerval)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(locationMinUpdateInterval)
                .setMaxUpdateDelayMillis(locationMaxUpdateDelay)
                .setMinUpdateDistanceMeters(locationMinUpdateDistance)
                .build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }

         powerManager = (PowerManager) getSystemService(POWER_SERVICE);

         wakeLock = powerManager.newWakeLock( PowerManager.PARTIAL_WAKE_LOCK,
                    this.getPackageName() + ":LocationServiceWakeLock");


        if (wakeLock != null ) {
            if (!wakeLock.isHeld()){
                wakeLock.acquire();
            }
        }


    }

    private Notification createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Location Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Location Service")
                .setContentText("07:45:00 - 18:45:00")
                .setSmallIcon(R.mipmap.irs)
                .setAutoCancel(false)
                .setOngoing(true)
                .build();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (wakeLock != null && wakeLock.isHeld()){
            fusedLocationClient.removeLocationUpdates(locationCallback);
            wakeLock.release();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    private void sendLocationToServer(Location location , Context context) {

        SharedPreferences sharedPref = context.getSharedPreferences("appWebTeck", Context.MODE_PRIVATE);
        String workerId = sharedPref.getString("workerId", "");
        String sessionId = sharedPref.getString("sessionId", "");

        JSONObject point = new JSONObject();

        if(workerId.equals("") || sessionId.equals("")){
            Log.e(TAG, "sendLocationToServer: workerId or sessionId is empty");
            return;
        }
        long timestamp = location.getTime();
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String utcTimeString = sdf.format(date);

        HashMap<String, String> postParams = new HashMap<>();
        JSONArray jarray = new JSONArray();
        JSONObject json = new JSONObject();
        try {

            //point
            point.put("x", location.getLatitude());
            point.put("y", location.getLongitude());

            //locations
            json.put("point", point);
            json.put("accuracy", location.getAccuracy());
            json.put("workerId", workerId);
            json.put("time", utcTimeString);
            json.put("deviceId", 42069);
            json.put("provider", "gps");
            json.put("timestamp", new Date().toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        jarray.put(json);
        postParams.put("locations", jarray.toString());

        //auth params
        postParams.put("request_type", "set_locations");
        postParams.put("session_id", sessionId);
        postParams.put("token", md5(Math.random()+ "9" + Math.random()));
        postParams.put("os_version", android.os.Build.VERSION.RELEASE);
        postParams.put("version", "4");

        String query = getQuery(postParams);
        Sync sendSync = new Sync(context);

        //save location to local db
        AppDatabase db = AppDatabase.getINSTANCE(context);
        long[] id = db.locationDao().insertLocation(new LocationTable(query,false));

        sendSync.sendJsonData(query,id[0]);



    }

    private String getQuery(HashMap<String,String> postParams) {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> pairs : postParams.entrySet()) {
            if (first) {
                first = false;
            } else {
                result.append("&");
            }

            try {
                result.append(URLEncoder.encode(pairs.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(pairs.getValue(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();

                return "";
            }
        }

        return result.toString();
    }
    public static String md5(String source) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");

            byte[] bytesOfMessage = source.getBytes("UTF-8");
            byte[] thedigest = digest.digest(bytesOfMessage);

            StringBuilder sb = new StringBuilder(2 * thedigest.length);

            for (byte b : thedigest)
                sb.append(String.format("%02x", b & 0xff));

            return sb.toString();
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static boolean shouldTrack() {
        boolean track = false;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        String startTime = "07:45:00";
        String endTime = "18:45:00";
        try {
            Date current = sdf.parse(currentTime);
            Date start = sdf.parse(startTime);
            Date end = sdf.parse(endTime);

            if (current.after(start) && current.before(end)) {
                track = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return track;
    }

}


