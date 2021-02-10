package com.vihangaaw.imperio.decisionmakingengine;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class NetworkSurrogateProfiler {
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
                writer.println(jsonObj.toString());
//                System.out.println("before receive data network surrogate");
                while((character = reader.read()) != -1) {
                    data.append((char) character);
                }
                System.out.println(data);
            }
        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }


}
