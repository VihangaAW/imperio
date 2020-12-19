package com.vihangaaw.imperio.offloadmanager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TokenizedInputOffload implements InputOffload{
    public static float[][] offload(JSONArray convertedInputJson, float[][] output, String[] outputTypes, String surrogateAddress, int surrogatePort){

        try (
                Socket s = new Socket(surrogateAddress,surrogatePort);
                PrintWriter pw = new PrintWriter(s.getOutputStream());
                OutputStreamWriter out = new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8);
                InputStreamReader streamReader = new InputStreamReader(s.getInputStream());
                BufferedReader reader = new BufferedReader( new InputStreamReader(s.getInputStream()));
        )
        {
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
            return output;
        }
        catch(IOException | JSONException e)
        {
            e.printStackTrace();
        }
        return null;
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
