package com.vihangaaw.imperio.decisionmakingengine;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

public class NetworkSurrogateProfiler {
    private static long roundTripTime = 0;
    private static double currentCpuUsage = 0;
    private static double currentMemoryUsage = 0;
    private static double availableBattery = 0;
    private static String batteryStatus;

    public static double getCurrentCpuUsage() {
        return currentCpuUsage;
    }

    public static double getCurrentMemoryUsage() {
        return currentMemoryUsage;
    }

    public static double getAvailableBattery() {
        return availableBattery;
    }

    public static String getBatteryStatus() {
        return batteryStatus;
    }

    public static long getRoundTripTime() {
        return roundTripTime;
    }

    public static void offload(String surrogateAddress, int surrogatePort) throws Exception{

        try (Socket socket = new Socket(surrogateAddress, surrogatePort)) {

            InputStream input = socket.getInputStream();
            InputStreamReader reader = new InputStreamReader(input);

            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);


            int  character;
            StringBuilder data = new StringBuilder();

            while(true){
                Thread.sleep(4000);
                String strJson = "{'message':'Hello World from Pixel 3'}";
                JSONObject jsonObj = new JSONObject(strJson);
                System.out.println(jsonObj);
                //Calculate time
                Instant rStart = Instant.now();
                writer.println(jsonObj.toString());
//                System.out.println("before receive data network surrogate");
                while((character = reader.read()) != -1) {
                    data.append((char) character);
                }
                //Calculate time
                Instant rEnd = Instant.now();
                Duration rTimeElapsed = Duration.between(rStart, rEnd);
                roundTripTime = rTimeElapsed.toMillis();
                System.out.println(data);

                JSONObject receivedJson = new JSONObject(data.toString());
                currentCpuUsage = receivedJson.getDouble("CpuUsage");
                currentMemoryUsage = receivedJson.getDouble("RamUsage");
                availableBattery = receivedJson.getDouble("BatteryUsage");
                batteryStatus = receivedJson.getString("PluggedInStatus");

            }
        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }


}
