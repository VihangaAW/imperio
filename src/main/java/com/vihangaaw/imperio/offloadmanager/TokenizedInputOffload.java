package com.vihangaaw.imperio.offloadmanager;
import android.content.Context;

import com.vihangaaw.imperio.decisionmakingengine.historicaldatadb.ImperioSQLiteDBHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TokenizedInputOffload implements InputOffload{
    private static boolean taskTimeOut = false;
    private static boolean taskHasErrors = false;
    private static long timeElapsed;


    private volatile static Socket oneSocket;

    private volatile static InputStream oneInput;
    private volatile static InputStreamReader oneReader;

    private volatile static OutputStream oneOutput;
    private volatile static PrintWriter oneWriter;




        private static ImperioSQLiteDBHelper imperioSQLiteDBHelper;

    public TokenizedInputOffload() throws IOException {

    }

 public static void startOne(String surrogateIpAddress) throws IOException {
        oneSocket = new Socket(surrogateIpAddress, 1238);
        oneInput = oneSocket.getInputStream();
        oneReader = new InputStreamReader(oneInput);
        oneOutput = oneSocket.getOutputStream();
        oneWriter = new PrintWriter(oneOutput, true);
    }

    public static boolean isTaskHasErrors() {
        return taskHasErrors;
    }

    public static boolean isTaskTimeOut() {
        return taskTimeOut;
    }

    public static long getTimeElapsed() {
        return timeElapsed;
    }



    // Only use this method
    public static void offload(Instant startTime, String input, float[][] output) throws Exception{
        Callable<Integer> task = () -> {
            try {
                int  character;
                StringBuilder data = new StringBuilder();

                //Calculate time
                Instant rStart = Instant.now();
                oneWriter.println(input);
                while((character = oneReader.read()) != -1 && character != '\n')
                {
                    data.append((char) character);
                }
                //Calculate time
                Instant rEnd = Instant.now();

                if(data.length()>0){
                    JSONObject receivedJson = new JSONObject(data.toString());
                    output[0][0] = Float.parseFloat(receivedJson.getString("negative"));
                    output[0][1] = Float.parseFloat(receivedJson.getString("positive"));
                }

            }
            catch (Exception e) {
                e.printStackTrace(); //throw new IllegalStateException("task interrupted", e);
            }
            return 0;
        };


        //Creates a threadpool
        ExecutorService executor = Executors.newFixedThreadPool(1);

        Future<Integer> future = executor.submit(task);

        System.out.println("future done? " + future.isDone());

        Integer result = 0;
        try {
            result = future.get(5000, TimeUnit.MILLISECONDS);
        }
        catch (TimeoutException e) {
            System.out.println("DECISION MAKING ENGINE: TIME OUT");
            taskTimeOut = true;
            future.cancel(true);
        }
        catch (Exception e)
        {
            taskHasErrors = true;
            e.printStackTrace();
        }
        System.out.println("future done? " + future.isDone());


    }






}