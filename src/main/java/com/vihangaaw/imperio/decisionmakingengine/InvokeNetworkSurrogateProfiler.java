package com.vihangaaw.imperio.decisionmakingengine;


import org.json.JSONException;
import org.json.JSONObject;

public class InvokeNetworkSurrogateProfiler{
    private double cpuUsage;
    private double ramUsage;
    private double batteryUsage;
    private String pluggedInStatus;
    private String surrogateIpAddress;
    private int surrogateProfilerPort;

    public InvokeNetworkSurrogateProfiler(String surrogateIpAddress, int surrogateProfilerPort) {
        this.surrogateIpAddress = surrogateIpAddress;
        this.surrogateProfilerPort = surrogateProfilerPort;
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

    /**
     * Invokes Network and Surrogate Profilers
     *
     * @return void
     */
    public void run() throws Exception {
        System.out.println("InvokeNetworkSurrogateProfiler is running");
        JSONObject objectDecisionMakingData = new JSONObject();
        NetworkSurrogateProfiler.offload(surrogateIpAddress,surrogateProfilerPort);
    }

}