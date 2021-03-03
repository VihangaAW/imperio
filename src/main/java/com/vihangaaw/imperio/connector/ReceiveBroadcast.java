package com.vihangaaw.imperio.connector;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;

import org.json.JSONObject;

import java.io.IOException;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReceiveBroadcast{
    private String surrogateIpAddress="";
    private String surrogateMACAddress="";
    private Boolean isOffloadEnabled = false;
    private Context context;

    public String getSurrogateIpAddress() {
        return surrogateIpAddress;
    }

    public String getSurrogateMACAddress() {
        return surrogateMACAddress;
    }

    public Boolean getOffloadEnabled() {
        return isOffloadEnabled;
    }

    public ReceiveBroadcast(Context context) {
        this.context = context;
    }

    public void run(){
        // code in the other thread, can reference "var" variable
        try{
            System.out.println("Start executing broadcast receiver");
            surrogateDeviceScanner();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void surrogateDeviceScanner() throws SocketException, UnknownHostException, IOException {
        // Get offload enabled status from the offload manager mobile app
        // vihangaaw content provider
        Uri offloadStatusContentUri = Uri.parse("content://com.vihangaaw.imperiooffloadmanager/cp_offload_info");
        ContentProviderClient offloadStatusContentProviderClient = context.getContentResolver().acquireContentProviderClient(offloadStatusContentUri);
        ContentResolver offloadStatusContentResolver = context.getContentResolver();

        Uri connDeviceHistoryContentUri = Uri.parse("content://com.vihangaaw.imperiooffloadmanager/cp_conn_device_history");
        ContentProviderClient connDeviceHistoryContentProviderClient = context.getContentResolver().acquireContentProviderClient(connDeviceHistoryContentUri);

        Cursor offloadStatusCursor = null;
        try {
            offloadStatusCursor = offloadStatusContentProviderClient.query(offloadStatusContentUri , null, null, null, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if(offloadStatusCursor!=null && offloadStatusCursor.getCount()>0) {
            offloadStatusCursor.moveToFirst();
            // Loop in the cursor to get each row.
            do {
                // Get column 1 value.
                int column1Index = offloadStatusCursor.getColumnIndex("offload_enabled");
                String column1Value = offloadStatusCursor.getString(column1Index);
                if(column1Value.equals("True")){
                    isOffloadEnabled = true;
                    System.out.println("INSIDE Offload Enabled Status from Offload Manager: "+isOffloadEnabled);
                }
                else{
                    isOffloadEnabled = false;
                    System.out.println("INSIDE Offload Enabled Status from Offload Manager: "+isOffloadEnabled);
                }
                // Get column 2 value.
                //        int column2Index = cursor.getColumnIndex("column2");
                //        String column2Value = cursor.getString(column2Index);
            } while (offloadStatusCursor.moveToNext());
        }

        
        //vihangaaw content provider
        if(isOffloadEnabled) {
            try {
                System.out.println(InetAddress.getByName("255.255.255.255"));
                DatagramSocket socket = new DatagramSocket(37020, InetAddress.getByName("0.0.0.0"));
                socket.setBroadcast(true);
                System.out.println("Listen on " + socket.getLocalAddress() + " from " + socket.getInetAddress() + " port " + socket.getBroadcast());
                byte[] buf = new byte[512];
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);


                //if(isOffloadEnabled){
                // Get the registered MAC address of offload manager mobile app
                // vihangaaw content provider
                Uri contentUri = Uri.parse("content://com.vihangaaw.imperiooffloadmanager/cp_mac_address");
                ContentProviderClient contentProviderClient = context.getContentResolver().acquireContentProviderClient(contentUri);
                ContentResolver contentResolver = context.getContentResolver();
                Cursor cursor = null;
                try {
                    cursor = contentProviderClient.query(contentUri, null, null, null, null);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    // Loop in the cursor to get each row.
                    do {
                        // Get column 1 value.
                        int column1Index = cursor.getColumnIndex("mac");
                        String column1Value = cursor.getString(column1Index);
                        surrogateMACAddress = column1Value;
                        System.out.println("Surrgate MAC Address from Offload Manager: " + surrogateMACAddress);
                        // Get column 2 value.
                        //        int column2Index = cursor.getColumnIndex("column2");
                        //        String column2Value = cursor.getString(column2Index);
                    } while (cursor.moveToNext());
                }
                //vihangaaw content provider


                System.out.println("Getting broadcast message");

                while (true) {
                    System.out.println("Waiting for data");
                    socket.receive(packet);
                    System.out.println("Waiting for data1");
                    buffer = packet.getData();
                    System.out.println("Waiting for data2");
                    String packetAsString = new String(buffer, 0, packet.getLength());
                    System.out.println("Waiting for data3");
                    System.out.println("Data received");
                    JSONObject objectOutput = new JSONObject(packetAsString);
                    System.out.println("DTAA JSON");
                    System.out.println(objectOutput.getString("MacAddress"));
                    System.out.println(objectOutput.get("IpAddress"));
                    if (buffer != null) {
                        //check whether mobile device's MAC address is in the message. If so, update surrogateAddress
                        System.out.println(surrogateMACAddress + "     " + objectOutput.getString("MacAddress"));
                        System.out.println("TESTING: " + String.valueOf(surrogateMACAddress.equals(objectOutput.getString("MacAddress"))));
                        if (surrogateMACAddress.equals(objectOutput.getString("MacAddress"))) {
                            surrogateIpAddress = objectOutput.getString("IpAddress");

                            try {
                                ContentValues contentValues = new ContentValues();
                                contentValues.put("device", objectOutput.getString("DeviceName"));
                                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                                LocalDateTime current_time = LocalDateTime.now();
                                contentValues.put("conn_date", dtf.format(current_time));
                                System.out.println("DEVICE DETAILS: "+dtf.format(current_time) +" : "+ objectOutput.getString("DeviceName"));
                                connDeviceHistoryContentProviderClient.insert(connDeviceHistoryContentUri, contentValues);

                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }


                            break;
                        }
                        //surrogateAddress = packetAsString;

                    }
                }
                socket.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
