package com.vihangaaw.imperio.connector;

import android.content.Context;
import android.util.Log;

import com.vihangaaw.imperio.decisionmakingengine.InvokeNetworkSurrogateProfiler;
import com.vihangaaw.imperio.offloadmanager.TokenizedInputOffload;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class InvokeConnector {
    private String surrogateIpAddress;
    private Context context;
    private String modelPath;
    private String inferenceApi;
    private Boolean isOffloadEnabled;

    private Socket taskExecutorSocket;
    InputStream taskExecutorInput;
    InputStreamReader taskExecutorReader;

    OutputStream taskExecutorOutput;
    PrintWriter taskExecutorWriter;

    private TokenizedInputOffload tokenizedInputOffload;

    public String getSurrogateIpAddress() {
        return surrogateIpAddress;
    }

    public Boolean getOffloadEnabled() {
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

    public InvokeConnector(Context context, String modelPath, String inferenceApi) {
        this.context = context;
        this.modelPath = modelPath;
        this.inferenceApi = inferenceApi;
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
            ModelOffloader oe = new ModelOffloader(surrogateIpAddress, 1231, context, modelPath, inferenceApi);
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

            Thread threadOffload = new Thread(new Runnable() {
                @Override
                public void run() {
                    try  {
                        TokenizedInputOffload.startOne(surrogateIpAddress);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            threadOffload.start();
        }


    }

}
