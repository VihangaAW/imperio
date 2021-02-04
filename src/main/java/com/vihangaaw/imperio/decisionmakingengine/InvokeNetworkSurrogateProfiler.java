package com.vihangaaw.imperio.decisionmakingengine;


import org.json.JSONObject;

public class InvokeNetworkSurrogateProfiler{
    public void run(){
        System.out.println("InvokeNetworkSurrogateProfiler is running");
        while (true){
            if(NetworkSurrogateProfiler.receivedData==1){
                try {
                    Thread.sleep(1000);
                    JSONObject objectDecisionMakingData = new JSONObject();
                    NetworkSurrogateProfiler.offload("192.168.1.8",1237);
                    System.out.println("OUT SIDE IMPERIO");
                    System.out.println(NetworkSurrogateProfiler.receivedDataJSON);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}