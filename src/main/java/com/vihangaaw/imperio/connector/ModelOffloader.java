package com.vihangaaw.imperio.connector;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;

public class ModelOffloader extends AsyncTask<String,Void,Void> {
    private Socket s;
    private String host="";
    private int port;
    private Context context;
    private String modelName ="";

    private int ExecuteTranfer() {
        try {
            s = new Socket(host, port);
            AssetManager assetManager = context.getAssets();
            //Get file list on assets folder
            //String[] files = null;
            //System.out.println("ModelOffloader: ExecuteTransfer starts");
            //try {
            //    files = assetManager.list("");
            //    Log.d("AAA", Arrays.toString(files));
            //    System.out.println("ModelOffloader: files found in asset folder");
            //} catch (IOException e) {
            //    Log.e("MODELOFFLOAD", "Failed to get asset file list.", e);
            //}
            System.out.println("ModelOffloader: send files");
            sendFile(modelName,  assetManager);
            s.close();
            //return 0 : model successfully offloaded
            return 0;
        } catch (Exception e) {
            Log.d("MODELOFFLOAD", "Model transfer failed");
            e.printStackTrace();
            //return 0 : model offloaded failed
            return -1;
        }
    }

    public ModelOffloader(String host, int port, Context context, String modelName) {
        this.host = host;
        this.port = port;
        this.context = context;
        this.modelName = modelName;
    }

    public void sendFile(String file, AssetManager assetManager) throws IOException, JSONException {
        DataOutputStream dos = new DataOutputStream(s.getOutputStream());
        System.out.println("Start sending file inside method 1");
//        get last modified date of tflite file
        File fileData = new File(file);
        Date lastModDate = new Date(fileData.lastModified());

        AssetFileDescriptor fileDescriptor = assetManager.openFd(file);
        FileInputStream fis = fileDescriptor.createInputStream();
        //FileInputStream fis = new FileInputStream(file);
        //Send Device Name
        //Send model modified date
        OutputStreamWriter out = new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8);
        JSONObject obj = new JSONObject();
        obj.put("ApplicationName",getApplicationName(context));
        obj.put("Device", Build.MODEL);
        obj.put("Model",file);
        //Need to get Last modified date, following on doesnt work
        obj.put("ModelModifiedDate",lastModDate);

        out.write(obj.toString());
        System.out.println("Data has been sent");
        out.flush();

        // Send Model File
        System.out.println("Start sending file inside method 2");
        byte[] buffer = new byte[1024];
        while ((fis.read(buffer) > 0)) {
            dos.write(buffer);
        }
        fis.close();
        dos.close();
    }

    @Override
    protected Void doInBackground(String... voids) {
        System.out.println("ModelOffloader: doInBackground starts");
        int offload = ExecuteTranfer();
        return null;
    }

    //exists method is used to check whether a file is exists in Assets folder
    public static boolean exists(AssetManager assetManager,
                                 String directory, String fileName) throws IOException {
        final String[] assets = assetManager.list(directory);
        for (String asset : assets)
            if (asset.equals(fileName))
                return true;
        return false;
    }

    public static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

}