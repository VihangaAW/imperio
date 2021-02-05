package com.vihangaaw.imperio.decisionmakingengine;


import org.json.JSONException;
import org.json.JSONObject;

public class InvokeNetworkSurrogateProfiler{
    private double cpuUsage;
    private double ramUsage;
    private double batteryUsage;
    private String pluggedInStatus;
    private String surrogateIpAddress;

    public InvokeNetworkSurrogateProfiler(String surrogateIpAddress) {
        this.surrogateIpAddress = surrogateIpAddress;
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

    public double getRamUsage() {
        return ramUsage;
    }

    public double getBatteryUsage() {
        return batteryUsage;
    }

    public String getPluggedInStatus() {
        return pluggedInStatus;
    }

    public void run(){
        System.out.println("InvokeNetworkSurrogateProfiler is running");
        while (true){
            if(NetworkSurrogateProfiler.receivedData==1){
                try {
                    Thread.sleep(1000);
                    JSONObject objectDecisionMakingData = new JSONObject();
                    NetworkSurrogateProfiler.offload(surrogateIpAddress,1237);
                    System.out.println("OUT SIDE IMPERIO");
                    System.out.println(NetworkSurrogateProfiler.receivedDataJSON);
                    cpuUsage = NetworkSurrogateProfiler.receivedDataJSON.getDouble("CpuUsage");
                    ramUsage = NetworkSurrogateProfiler.receivedDataJSON.getDouble("RamUsage");
                    batteryUsage = NetworkSurrogateProfiler.receivedDataJSON.getDouble("BatteryUsage");
                    pluggedInStatus = NetworkSurrogateProfiler.receivedDataJSON.getString("PluggedInStatus");
                } catch (InterruptedException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}