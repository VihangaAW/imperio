package com.vihangaaw.imperio.connector;

import android.app.Application;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.util.Log;

import com.vihangaaw.imperio.decisionmakingengine.InvokeNetworkSurrogateProfiler;
import com.vihangaaw.imperio.offloadmanager.OffloadManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class InvokeConnector {
    private String surrogateIpAddress;
    private Context context;
    private String modelPath;
    private String inferenceApi;
    private boolean isOffloadEnabled;
    private int offloadStatus = 0;

    private int modelOffloaderPort;
    private int surrogateProfilerPort;
    private int taskExecutorPort;

    private Socket taskExecutorSocket;
    InputStream taskExecutorInput;
    InputStreamReader taskExecutorReader;

    OutputStream taskExecutorOutput;
    PrintWriter taskExecutorWriter;

    private OffloadManager tokenizedInputOffload;

    public String getSurrogateIpAddress() {
        return surrogateIpAddress;
    }

    public int getModelOffloaderPort() {
        return modelOffloaderPort;
    }

    public int getSurrogateProfilerPort() {
        return surrogateProfilerPort;
    }

    public int getTaskExecutorPort() {
        return taskExecutorPort;
    }

    public boolean getOffloadEnabled() {
        return isOffloadEnabled;
    }

    public InputStream getTaskExecutorInput() {
        return taskExecutorInput;
    }

    public InputStreamReader getTaskExecutorReader() {
        return taskExecutorReader;
    }

    public OutputStream getTaskExecutorOutput() {
        return taskExecutorOutput;
    }

    public PrintWriter getTaskExecutorWriter() {
        return taskExecutorWriter;
    }

    public Socket getTaskExecutorSocket() {
        return taskExecutorSocket;
    }

    public InvokeConnector(Context context, String modelPath, String inferenceApi) throws IOException {
        this.context = context;
        this.modelPath = modelPath;
        this.inferenceApi = inferenceApi;
        this.isOffloadEnabled = checkOffloadEnabled();
        if(isWifiConnected(context)==1 && this.isOffloadEnabled){
            this.offloadStatus = 1;
        }
        else {
            this.offloadStatus = 0;
        }
    }

    /**
     * If the mobile device is connected to a network, it will start waiting for a broadcast message
     * from a registered surrogate device in the same network. After the device is successfully 
     * connected with the surrogate device it collects ports which are used in the surrogate device
     * for model offloading, surrogate profiler and task executor.
     *
     * @return void
     */
    public void invokeReceiveBroadcast() {
        if(this.offloadStatus==1){
            // Start waiting for a message from a surrogate device connected to the same network
            ReceiveBroadcast rb = new ReceiveBroadcast(context);
            rb.run();
            surrogateIpAddress = rb.getSurrogateIpAddress();
            isOffloadEnabled  = rb.getOffloadEnabled();
            modelOffloaderPort = rb.getModelOffloaderPort();
            surrogateProfilerPort = rb.getSurrogateProfilerPort();
            taskExecutorPort = rb.getTaskExecutorPort();
            System.out.println("PORTS: Mobile Offloader Port: " + modelOffloaderPort + "  Surrogate Profiler Port: " + surrogateProfilerPort + "  Task Executor Port:" + taskExecutorPort);
        }
    }



    /**
     * If the mobile device is connected to a network, it will start waiting for a message from a
     * registered surrogate device in the same network. After the device is successfully connected with the
     * surrogate device it starts Surrogate Profiler and Task Offloading Manager
     *
     * @return void   
     */
    public void start(){
        if(this.offloadStatus==1) {
            invokeReceiveBroadcast();
            //Execute model offloader
            System.out.println("IP Address of the surrogate device: " + surrogateIpAddress);
            ModelOffloader oe = new ModelOffloader(surrogateIpAddress, modelOffloaderPort, context, modelPath, inferenceApi);
            oe.execute();

            //Execute decision making process data information
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Invoke Network and Surrogate Profilers
                        InvokeNetworkSurrogateProfiler invokeNetworkSurrogateProfiler = new InvokeNetworkSurrogateProfiler(surrogateIpAddress, surrogateProfilerPort);
                        invokeNetworkSurrogateProfiler.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();

            Thread threadOffload = new Thread(new Runnable() {
                @Override
                public void run() {
                    try  {
                        // Invoke task offloading manager
                        OffloadManager.start(surrogateIpAddress, taskExecutorPort);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            threadOffload.start();
        }
    }



    /**
     * Returns 1 is the device is connected to a network, else 0
     *
     * @param  context  context
     * @return int      returns the status of connectivity
     */
    public static int isWifiConnected(Context context) {
        int temp = 0;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // 23 is for Marshmellow.
        if (Build.VERSION.SDK_INT >= 23) {
            if (connectivityManager != null) {
                NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                if (networkCapabilities != null) {
                    if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        temp = 1;
                    }
                }
            }
        } else {
            if (connectivityManager != null) {
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo != null) {
                    if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        temp = 1;
                    }
                }
            }
        }
        return temp;
    }



    /**
     * Returns true if the user has enabled the offload functionality on Surrogate Manager
     * mobile application, else false
     *
     * @return boolean   returns the status of offload functionality
     */
    public boolean checkOffloadEnabled() throws SocketException, UnknownHostException, IOException {
        // Get offload enabled status from the offload manager mobile app
        // vihangaaw content provider
        boolean isOffloadEnabled = false;
        Uri offloadStatusContentUri = Uri.parse("content://com.vihangaaw.imperiooffloadmanager/cp_offload_info");
        ContentProviderClient offloadStatusContentProviderClient =  context.getContentResolver().acquireContentProviderClient(offloadStatusContentUri);

        Cursor offloadStatusCursor = null;
        try {
            offloadStatusCursor = offloadStatusContentProviderClient.query(offloadStatusContentUri, null, null, null, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (offloadStatusCursor != null && offloadStatusCursor.getCount() > 0) {
            offloadStatusCursor.moveToFirst();
            // Loop in the cursor to get the values of each row
            do {
                // Get offload_enabled value.
                int columnOffloadEnabledIndex = offloadStatusCursor.getColumnIndex("offload_enabled");
                String column1Value = offloadStatusCursor.getString(columnOffloadEnabledIndex);
                if (column1Value.equals("True")) {
                    isOffloadEnabled = true;
                    System.out.println("INSIDE Offload Enabled Status from Offload Manager: " + isOffloadEnabled);
                } else {
                    isOffloadEnabled = false;
                    System.out.println("INSIDE Offload Enabled Status from Offload Manager: " + isOffloadEnabled);
                }
            } while (offloadStatusCursor.moveToNext());
        }
        return isOffloadEnabled;
    }


}
