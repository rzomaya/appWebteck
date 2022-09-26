package com.example.appwebteck;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import androidx.core.content.FileProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

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
            if(files.length > 0)
            for (File file : files) {
                if (file.getName().contains(orderID)) {
                    try {
                        photos.put(file.getName(),toBase64(mContext, Uri.fromFile(file)));
                    } catch (IOException e) {
                        e.printStackTrace();
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
    public static  String readContentOfJsonFile(  File fileUri) throws IOException {

            FileReader reader = new FileReader(fileUri);
            int character;
            StringBuilder stringBuilder = new StringBuilder();
            while ((character = reader.read()) != -1) {
                stringBuilder.append((char) character);
            }
            reader.close();
            return stringBuilder.toString();

    }


}
