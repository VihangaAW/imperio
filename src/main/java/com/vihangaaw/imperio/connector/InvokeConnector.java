package com.vihangaaw.imperio.connector;

import android.content.Context;
import android.util.Log;

import com.vihangaaw.imperio.decisionmakingengine.InvokeNetworkSurrogateProfiler;

public class InvokeConnector {
    private String surrogateIpAddress;
    private Context context;
    private String modelPath;
    private Boolean isOffloadEnabled;

    public String getSurrogateIpAddress() {
        return surrogateIpAddress;
    }

    public Boolean getOffloadEnabled() {
        return isOffloadEnabled;
    }

    public InvokeConnector(Context context, String modelPath) {
        this.context = context;
        this.modelPath = modelPath;
    }

    public void invokeReceiveBroadcast() {
        System.out.println("Invoked invokeReceiveBroadcast");
        ReceiveBroadcast rb = new ReceiveBroadcast(context);
        rb.run();
        surrogateIpAddress = rb.getSurrogateIpAddress();
        isOffloadEnabled  = rb.getOffloadEnabled();
    }


    public void start(){
        invokeReceiveBroadcast();
        if(isOffloadEnabled) {
            //Execute model offloader
            System.out.println("IP Address of the surrogate device: " + surrogateIpAddress);
            ModelOffloader oe = new ModelOffloader(surrogateIpAddress, 1231, context, modelPath);
            oe.execute();
            Log.d("OFFLOAD", "===========================offload executed");

            //Execute decision making process data information
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        InvokeNetworkSurrogateProfiler invokeNetworkSurrogateProfiler = new InvokeNetworkSurrogateProfiler(surrogateIpAddress);
                        invokeNetworkSurrogateProfiler.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        }
    }

}
