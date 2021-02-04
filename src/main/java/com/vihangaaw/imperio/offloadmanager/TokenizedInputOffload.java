package com.vihangaaw.imperio.offloadmanager;
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

public class TokenizedInputOffload implements InputOffload{
    public static void offload(JSONArray convertedInputJson, float[][] output, String[] outputTypes, String surrogateAddress, int surrogatePort){
        Callable<Integer> task = () -> {
            try {
                try (
                        Socket s = new Socket(surrogateAddress, surrogatePort);
                        PrintWriter pw = new PrintWriter(s.getOutputStream());
                        OutputStreamWriter out = new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8);
                        InputStreamReader streamReader = new InputStreamReader(s.getInputStream());
                        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
                ) {
                    out.write(convertedInputJson.toString());
                    System.out.println("Data has been sent");
                    out.flush();
                    //out.close();
                    //read the server response message
                    //Get the response message and print it to console
                    String responseMessage = reader.readLine();

                    JSONObject object = new JSONObject(responseMessage);
                    //for(int i = 0; i< output.length; i++){
                    //    output[0][i] = Float.parseFloat(object.getString(outputTypes[i]));
                    //}
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

        //Calculate time
        Instant rStart = Instant.now();
        //Creates a threadpool
        ExecutorService executor = Executors.newFixedThreadPool(1);
        Future<Integer> future = executor.submit(task);

        System.out.println("future done? " + future.isDone());

        Integer result = 0;
        try {
            result = future.get();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        System.out.println("future done? " + future.isDone());
        //Calculate time
        Instant rEnd = Instant.now();
        Duration rTimeElapsed = Duration.between(rStart, rEnd);
        System.out.println(rTimeElapsed.toMillis() +" milliseconds");
        System.out.println("result: " + result);
        System.out.println(result.toString());
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
