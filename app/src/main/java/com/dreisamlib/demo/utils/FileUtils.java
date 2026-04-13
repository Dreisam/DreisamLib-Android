package com.dreisamlib.demo.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;

public class FileUtils {



    // Write string content into a text file
    public static void writeTxtToFile(String strcontent, String filePath, String fileName) {
        // Create the directory first, then create the file to avoid errors
        makeFilePath(filePath, fileName);

        String strFilePath = filePath + fileName;
        // Append a new line for each write
        String strContent = strcontent + "\r\n";
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                Log.d("TestFile", "Create the file:" + strFilePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("TestFile", "Error on write File:" + e);
        }
    }

    /**
     * Delete file
     */
    public static void deleteFile(Context context, String fileName) {
        File file =new  File(context.getCacheDir().getPath() + "/"+fileName);
        if (file.exists()) {
            file.delete();
        }
    }

    // Create file
    public static File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    // Create directory
    public static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            Log.i("error:", e + "");
        }
    }

    // Read content from all TXT files in the specified directory
    public static String getFileContent(File file) {
        Log.i("File Path", "getFileContent: "+file.getPath());
        String content = "";
        if (!file.isDirectory()) {  // Check whether this path is a directory (folder)
            if (file.getName().endsWith("txt")) {// Check whether file format is txt
                try {
                    InputStream instream = new FileInputStream(file);
                    if (instream != null) {
                        InputStreamReader inputreader
                                = new InputStreamReader(instream, "UTF-8");
                        BufferedReader buffreader = new BufferedReader(inputreader);
                        String line = "";
                        // Read line by line
                        while ((line = buffreader.readLine()) != null) {
                            content += line + "\n";
                        }
                        instream.close();//Close input stream
                    }
                } catch (java.io.FileNotFoundException e) {
                    Log.d("TestFile", "The File doesn't not exist.");
                } catch (IOException e) {
                    Log.d("TestFile", e.getMessage());
                }
            }
        }
        return content;
    }

}
