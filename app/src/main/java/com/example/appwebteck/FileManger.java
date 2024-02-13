package com.example.appwebteck;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class FileManger {
  public static String TAG = "FileManger";

    public static Uri createDir(Context mContext, String dirName, String fileName) {
        File parent = new File(mContext.getExternalFilesDir(null), dirName);
        if (!parent.exists()) {
            parent.mkdirs();
        }

        File fileUri = new File(parent, fileName);
        Uri fURI = FileProvider.getUriForFile(mContext,
                "com.example.appwebteck.fileprovider",
                fileUri);
        return fURI;

    }
    public  static void deleteFile(Context mContext, String dirName, String fileName) {
        File parent = new File(mContext.getExternalFilesDir(null), dirName);
        if (!parent.exists()) {
            parent.mkdirs();
        }

        File fileUri = new File(parent, fileName);
        fileUri.delete();
    }

    public static  HashMap<String,String> loadOrdersPhotos(Context mContext, String dirName, String orderID) {
        HashMap<String,String> photos = new HashMap<>();
        File parent = new File(mContext.getExternalFilesDir(null), dirName);
        if (parent.exists()) {
            File[] files = parent.listFiles();
            if(Objects.requireNonNull(files).length > 0){
                for (File file : files) {
                    if (file.getName().contains(orderID)) {
                        try {
                           if (file.getName().contains("jpg") || file.getName().contains("png")) {
                            photos.put(file.getName(),toBase64Faster(mContext, Uri.fromFile(file)));
                           }else{
                               photos.put(file.getName(),convertVideoToBytes(mContext, Uri.fromFile(file)));
                           }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return  photos;
    }



    public static String load (Context mContext, String dirName, String fileName) throws IOException {
        File parent = new File(mContext.getExternalFilesDir(null), dirName);
        if (parent.exists()) {
            File fileUri = new File(parent, fileName);
            if(fileName.contains(".jpg") || fileName.contains(".png") || fileName.contains(".jpeg")){
                return toBase64(mContext, Uri.fromFile(fileUri));
            }else if (fileName.contains(".mp4")){
                return convertVideoToBytes(mContext,Uri.fromFile(fileUri));
            }else if (fileName.contains(".json")){
                // read content of json file
              return  readContentOfJsonFile(fileUri);
            }
        }

        return "dir does not exist";
    }

    public static void createFile(Context mContext, String dirName, String fileName, String content) throws IOException {
        File parent = new File(mContext.getExternalFilesDir(null), dirName);
        if (!parent.exists()) {
            parent.mkdirs();
        }

        File fileUri = new File(parent, fileName);
        if (!fileUri.exists()) {
            fileUri.createNewFile();
        }
        java.io.FileWriter writer = new java.io.FileWriter(fileUri);
        writer.append(content);
        writer.flush();
        writer.close();
    }


    public static String toBase64(Context mContext, Uri uri) throws IOException {


            Bitmap myBitmap =  MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), uri);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            myBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream .toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);


    }
    public static String toBase64Faster(Context mContext, Uri uri) throws IOException {
        InputStream inputStream = mContext.getContentResolver().openInputStream(uri);
        byte[] bytes;
        byte[] buffer = new byte[8192];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        bytes = output.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);

    }
    public static String convertVideoToBytes(Context mContext, Uri uri) {
        String encodedVideo = null;
        try {
            InputStream inputStream = mContext.getContentResolver().openInputStream(uri);
            byte[] bytes;
            byte[] buffer = new byte[8192];
            int bytesRead;
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try {
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            bytes = output.toByteArray();
            encodedVideo = Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return encodedVideo;
    }

    public static String readContentOfJsonFile(File file) throws IOException {
        FileReader reader = new FileReader(file);
        char[] chars = new char[(int) file.length()];
        reader.read(chars);
        String content = new String(chars);
        reader.close();
        return content;
    }
    public  static void saveFiles (Context context , String fileName, String base64Data) throws IOException {

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) +"/"+ fileName );
        if (!file.exists()) {
           file.createNewFile();

        }else {
            boolean fi =  file.delete();
            if (fi){
                file.createNewFile();
            }else {
                Log.d(TAG, "saveFiles: " +
                        "file not deleted");
            }
        }

        byte[] data = Base64.decode(base64Data, Base64.DEFAULT);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(data);
        fos.flush();
        fos.close();


        if(file.exists()){
            context.startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
        }else {
            Toast.makeText(context, "file not exist", Toast.LENGTH_SHORT).show();
        }



    }

    public static Uri saveBitmapToFile(Context context, Bitmap bitmap) {
        // Create a folder named appWebTeck in external storage
        String extStorageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
        File parent = new File(extStorageDirectory, "appWebTeck");

        if (!parent.exists()) {
            parent.mkdirs();
        }

        OutputStream outStream = null;
        String random = String.valueOf(System.currentTimeMillis()).replaceAll(":", ".");
        String date = DateFormat.format("yyyy-MM-dd_hh:mm:ss", new Date()).toString().replace(":", "-");
        String name = "image_" + random + "_" + date;

        File file = new File(parent, name + ".jpg");
        if (file.exists()) {
            file.delete();
            file = new File(parent, name + ".jpg");
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                outStream = Files.newOutputStream(file.toPath());
            } else {
                outStream = new FileOutputStream(file);
            }

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream); // No compression

            outStream.flush();
            outStream.close();

            MediaScannerConnection.scanFile(context, new String[]{file.getPath()}, new String[]{"image/jpeg"}, null);
            return Uri.fromFile(file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static Uri createImageFile(){
        //path = appWebTeck/yyyyMMdd/image_yyyyMMdd_HHmmss.jpg
        String parentName = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String extStorageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
        File parent = new File(extStorageDirectory, "/appWebTeck/"+parentName);
        if (!parent.exists()) {
            parent.mkdirs();
        }
        String imgName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File image = new File(parent, "img_" + imgName + ".jpg");
        Uri uriSavedImage = Uri.fromFile(image);

        return uriSavedImage;
    }

    public static Uri createVideoFile(){
        //path = appWebTeck/20210101/videos/video_20210101_000000.mp4

        String parentName = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String extStorageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
        File parent = new File(extStorageDirectory, "/appWebTeck/"+parentName+"/videos");
        if (!parent.exists()) {
            parent.mkdirs();
        }
        String videoName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File video = new File(parent, "video_" + videoName + ".mp4");

        Uri uriSavedVideo = Uri.fromFile(video);

        return uriSavedVideo;

    }

    public static void logThis (String logString){
        try {
            String parentName = "log";
            String extStorageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
            File parent = new File(extStorageDirectory, "/appWebTeck/"+parentName);
            if (!parent.exists()) {
                parent.mkdirs();
            }
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String fileName = new SimpleDateFormat("yyyyMMdd").format(new Date());
            File file = new File(parent, "Log_"+fileName+".txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            file.setWritable(true);
            java.io.FileWriter writer = new java.io.FileWriter(file, true);
            writer.append(timeStamp+" -> " +logString +"\n");
            writer.flush();
            writer.close();

        }catch ( Exception e){
            Log.e(TAG, "log error -> : "+e.getMessage());
        }

    }

}
