package com.vihangaaw.imperio.offloadmanager;
import android.content.Context;

import com.vihangaaw.imperio.decisionmakingengine.historicaldatadb.ImperioSQLiteDBHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
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


    private static ImperioSQLiteDBHelper imperioSQLiteDBHelper;

    public static boolean isTaskHasErrors() {
        return taskHasErrors;
    }

    public static boolean isTaskTimeOut() {
        return taskTimeOut;
    }

    public static long getTimeElapsed() {
        return timeElapsed;
    }

    public static void offload(String input, float[][] output, String[] outputTypes, String surrogateAddress, int surrogatePort, long offloadTimeExetute){
        //clear data
        taskTimeOut = false;
        taskHasErrors = false;
        timeElapsed = 0;

        Callable<Integer> task = () -> {
            try {
                try (
                        Socket s = new Socket(surrogateAddress, surrogatePort);
                        PrintWriter pw = new PrintWriter(s.getOutputStream());
                        OutputStreamWriter out = new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8);
                        InputStreamReader streamReader = new InputStreamReader(s.getInputStream());
                        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
                ) {
                    out.write(input);
                    out.flush();
                    //read the server response message
                    //Get the response message and print it to console
                    String responseMessage = reader.readLine();
                    JSONObject object = new JSONObject(responseMessage);
                    output[0][0] = Float.parseFloat(object.getString("Negative"));
                    output[0][1] = Float.parseFloat(object.getString("Positive"));
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                return null;
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
            result = future.get(1000, TimeUnit.MILLISECONDS);
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
        System.out.println("OUTPUT OF WITHOUT GET: "+ output[0][0] + "    " + output[0][1]);
    }

    public static JSONArray convertToJSON(int[][] input){
        JSONArray convertedInputJson = new JSONArray();
        for(int[] value_i : input)
        {
            for(int value_j : value_i)
            {
                convertedInputJson.put(value_j);
            }
        }
        return convertedInputJson;
    }
}