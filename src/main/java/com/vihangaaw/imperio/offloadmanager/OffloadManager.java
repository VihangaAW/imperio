package com.vihangaaw.imperio.offloadmanager;

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

public class OffloadManager {
    private static boolean taskTimeOut = false;
    private static boolean taskHasErrors = false;
    private static long timeElapsed;


    private volatile static Socket socket;

    private volatile static InputStream inputStream;
    private volatile static InputStreamReader inputStreamReader;

    private volatile static OutputStream outputStream;
    private volatile static PrintWriter printWriter;




    private static ImperioSQLiteDBHelper imperioSQLiteDBHelper;

    public OffloadManager() throws IOException {
    }

    /**
     * Starts Task Offloading Manager
     *
     * @param  surrogateIpAddress  String Surrogate IP Address
     * @param  taskExecutorPort  int Port reserved for Task Offloading Manager
     *
     * @return void
     */
    public static void start(String surrogateIpAddress, int taskExecutorPort) throws IOException {
        socket = new Socket(surrogateIpAddress, taskExecutorPort);
        inputStream = socket.getInputStream();
        inputStreamReader = new InputStreamReader(inputStream);
        outputStream = socket.getOutputStream();
        printWriter = new PrintWriter(outputStream, true);
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


    /**
     * Offload machine learning task and get the output
     *
     * @param  timeoutvalue  long timeout value (local execution value)
     * @param  input  String input
     * @return int      returns the status of connectivity
     */
    public static JSONObject offload(long timeoutvalue, String input) throws Exception{
        JSONArray responseJson =  new JSONArray();
        Callable<Integer> task = () -> {
            try {
                int  character;
                StringBuilder data = new StringBuilder();

                printWriter.println(input);
                while((character = inputStreamReader.read()) != -1 && character != '\n')
                {
                    data.append((char) character);
                }

                if(data.length()>0){
                    System.out.println(data.toString());
                    JSONObject receivedJson = new JSONObject(data.toString());
                    responseJson.put(receivedJson);
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
            result = future.get(timeoutvalue, TimeUnit.MILLISECONDS);
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
        return responseJson.getJSONObject(0);
    }






}