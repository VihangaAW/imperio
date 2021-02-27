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
//                    Instant testingStart = Instant.now();
                    //JSONArray newarray= TokenizedInputOffload.convertToJSON(input);
                    out.write(input);
//                    Instant testingEnd1 = Instant.now();
//                    System.out.println("Data has been sent");
                    out.flush();
//                    Instant testingEnd2 = Instant.now();
                    //out.close();
                    //read the server response message
                    //Get the response message and print it to console
                    String responseMessage = reader.readLine();
//                    Instant testingEnd3 = Instant.now();

                    JSONObject object = new JSONObject(responseMessage);
//                    Instant testingEnd4 = Instant.now();
                    //for(int i = 0; i< output.length; i++){
                    //    output[0][i] = Float.parseFloat(object.getString(outputTypes[i]));
                    //}
                    output[0][0] = Float.parseFloat(object.getString("Negative"));
                    output[0][1] = Float.parseFloat(object.getString("Positive"));
//                    Instant testingEnd = Instant.now();
//                    Duration testingTimeElapsed = Duration.between(testingStart, testingEnd);
//                    System.out.println("DECISIONMAKINGTESTING: INSIDE TOKENIZEDINPUT" + testingTimeElapsed.toMillis() +" milliseconds");
//                    System.out.println("DECISIONMAKINGTESTING: INSIDE TOKENIZEDINPUT  TIME EXECUTIONS");
//                    System.out.println("testing1: " + Duration.between(testingStart, testingEnd1).toMillis());
//                    System.out.println("testing2: " + Duration.between(testingEnd1, testingEnd2).toMillis());
//                    System.out.println("testing3: " + Duration.between(testingEnd2, testingEnd3).toMillis());
//                    System.out.println("testing4: " + Duration.between(testingEnd3, testingEnd4).toMillis());
//                    imperioSQLiteDBHelper = new ImperioSQLiteDBHelper(context);
//                    imperioSQLiteDBHelper.updateAverageTimeOffload("TextClassificationClient1",testingTimeElapsed.toMillis());
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

        //Calculate time
//        Instant rStart = Instant.now();
        //Creates a threadpool
        ExecutorService executor = Executors.newFixedThreadPool(1);
        Future<Integer> future = executor.submit(task);
//        Instant end1 = Instant.now();

        System.out.println("future done? " + future.isDone());
//        Instant end2 = Instant.now();

        Integer result = 0;
//        Instant end3 = Instant.now();
        try {
//            result = future.get();
            //DECISIONMAKINGENGINE
//            result = future.get(offloadTimeExetute, TimeUnit.MILLISECONDS);
            result = future.get(1000, TimeUnit.MILLISECONDS);
//            end3 = Instant.now();

            //DECISIONMAKINGENGINE
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
        //Calculate time
//        Instant rEnd = Instant.now();
//        Duration rTimeElapsed = Duration.between(rStart, rEnd);
//        System.out.println(rTimeElapsed.toMillis() +" milliseconds");
        //get time spent for the task
//        timeElapsed = rTimeElapsed.toMillis();
//        System.out.println("result: " + result);
//        System.out.println(result.toString());
//        System.out.println("testingFUTURE1: " + Duration.between(rStart, end1).toMillis());
//        System.out.println("testingFUTURE2: " + Duration.between(end1, end2).toMillis());
//        System.out.println("testingFUTURE3: " + Duration.between(end2, end3).toMillis());
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