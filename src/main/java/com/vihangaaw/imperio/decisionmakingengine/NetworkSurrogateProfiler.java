package com.vihangaaw.imperio.decisionmakingengine;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class NetworkSurrogateProfiler {
    public static int receivedData = 1;
    public static JSONObject receivedDataJSON;
    public static void offload(String surrogateAddress, int surrogatePort){
        receivedData = 0;
        try (
                Socket s = new Socket(surrogateAddress, surrogatePort);
                PrintWriter pw = new PrintWriter(s.getOutputStream());
                OutputStreamWriter out = new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8);
                InputStreamReader streamReader = new InputStreamReader(s.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        ) {

            String strJson = "{'message':'Hello World from Pixel 3'}";
            JSONObject jsonObj = new JSONObject(strJson);



            out.write(jsonObj.toString());
            out.flush();
            //out.close();
            //read the server response message
            //Get the response message and print it to console
            String responseMessage = reader.readLine();
            receivedData = 1;
            try {
                JSONObject objectOutput = new JSONObject(responseMessage);
                receivedDataJSON = objectOutput;
            }catch (JSONException e){
                e.printStackTrace();
            }

        } catch (IOException | JSONException e) {
            receivedData = 1;
            e.printStackTrace();
        }
    }


}
