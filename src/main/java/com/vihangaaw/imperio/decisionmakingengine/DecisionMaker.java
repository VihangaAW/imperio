package com.vihangaaw.imperio.decisionmakingengine;

import android.app.ActivityManager;
import android.content.Context;

import com.vihangaaw.imperio.decisionmakingengine.historicaldatadb.ImperioSQLiteDBHelper;

import static android.content.Context.ACTIVITY_SERVICE;

public class DecisionMaker {
    private Context context;
    //In order to use lowMemory variable's value, IsDeviceInLowMemory() should be executed before using the lowMemory variable's value
    private boolean lowMemory;
    private double avaiableMemoryPerc = 0;
    private int offloadCount = 0;
    private int localCount = 0;
    private double offloadMAD = 0;
    private double localMAD = 0;
    private ImperioSQLiteDBHelper imperioSQLiteDBHelper;
    private String taskId;

    public boolean isLowMemory() {
        return lowMemory;
    }

    public double getAvaiableMemoryPerc() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        double availableMegs = mi.availMem / 0x100000L;

        //Avaiable Memory
        double percentAvail = mi.availMem / (double)mi.totalMem * 100.0;
        avaiableMemoryPerc = percentAvail;

        return avaiableMemoryPerc;
    }

    public int getOffloadCount() {
        return offloadCount;
    }

    public int getLocalCount() {
        return localCount;
    }

    public double getOffloadMAD() {
        return offloadMAD;
    }

    public double getLocalMAD() {
        return localMAD;
    }

    public DecisionMaker(Context context, String taskId) {
        this.context = context;
        this.taskId = taskId;
        this.lowMemory = this.IsDeviceInLowMemory();
        this.imperioSQLiteDBHelper = new ImperioSQLiteDBHelper(this.context);
        initialization();
    }

    public void initialization(){
        localCount = (imperioSQLiteDBHelper.getTaskLocal(taskId)).getCount();
        offloadCount = (imperioSQLiteDBHelper.getTaskOffload(taskId)).getCount();
        System.out.println("LOCAL COUNT: "+localCount+" OFFLOAD COUNT: "+offloadCount);
        if(localCount>0){
            localMAD = imperioSQLiteDBHelper.getLocalMad(taskId);
        }
        if(offloadCount>0){
            offloadMAD = imperioSQLiteDBHelper.getOffloadMad((taskId));
        }
    }
    public boolean IsDeviceInLowMemory(){
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        double availableMegs = mi.availMem / 0x100000L;

        //Avaiable Memory
        double percentAvail = mi.availMem / (double)mi.totalMem * 100.0;
        avaiableMemoryPerc = percentAvail;

        //Check whether the device is low memory status
        // Device in a low memory situation - TRUE
        //Device currently not in a low memory situation - FALSE
        if(mi.lowMemory){
            lowMemory = true;
            return true;
            //set to false in order to offload everytime. Original value is true
            //lowMemory = false;
            //return false;
        }
        else {
            lowMemory = false;
            return false;
            //set to false in order to offload everytime. Original value is false
            //lowMemory = true;
            //return true;
        }
    }



    public int makeDecision(){
        double offloadSurrogateTimeExecute = 0;
        int didOffload = 0;

        if(offloadCount>0){
            System.out.println("DECISION_MAKER_CLASS: 1: "+taskId);
            offloadSurrogateTimeExecute = imperioSQLiteDBHelper.getOffloadAverage(taskId);
        }
        System.out.println("offloadSurrogateTimeExecute: "+offloadSurrogateTimeExecute);
        double localTimeExecute = 0;
        if(localCount>0){
            localTimeExecute = imperioSQLiteDBHelper.getLocalAverage(taskId);
        }
        System.out.println("localTimeExecute: "+localTimeExecute);


        if(isLowMemory()){
            if(offloadSurrogateTimeExecute!=0){
                //offload
                didOffload = 1;
            }
            else{
                if(localTimeExecute!=0){
                    //offload
                    didOffload = 1;
                }
                else{
                    //local execute
                    didOffload = 0;
                }
            }
        }
        else{
            if(localTimeExecute==0){
                //Execute locally
                didOffload = 0;
            }
            else if(offloadSurrogateTimeExecute==0){
                if(NetworkSurrogateProfiler.getAvailableBattery()<=10 && NetworkSurrogateProfiler.getBatteryStatus()=="Not Connected to a Power Source"){
                    //Execute task locally
                    didOffload = 0;
                }
                else{
                    if(NetworkSurrogateProfiler.getCurrentCpuUsage()<95 && NetworkSurrogateProfiler.getCurrentMemoryUsage()<95){
                        //Offload
                        didOffload = 1;
                    }
                    else{
                        //Execute task locally
                        didOffload = 0;
                    }
                }
            }
            else{
                //Decision Making Process using RTT
                long rttTime = NetworkSurrogateProfiler.getRoundTripTime();
                System.out.println("RTT TIME: "+String.valueOf(rttTime));
                double offloadTotalTimeElapsed =  rttTime + offloadSurrogateTimeExecute;
                if(localTimeExecute<offloadTotalTimeElapsed){
                    //Execute task locally
                    didOffload = 0;
                }
                else{
                    //Offload
                    if(NetworkSurrogateProfiler.getAvailableBattery()<=10 && NetworkSurrogateProfiler.getBatteryStatus()=="Not Connected to a Power Source"){
                        //Execute task locally
                        didOffload = 0;
                    }
                    else{
                        if(NetworkSurrogateProfiler.getCurrentCpuUsage()<95 && NetworkSurrogateProfiler.getCurrentMemoryUsage()<95){
                            //Offload
                            didOffload = 1;
                        }
                        else{
                            //Execute task locally
                            didOffload = 0;
                        }
                    }
                }
            }
        }
        return didOffload;
    }
}
