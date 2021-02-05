package com.vihangaaw.imperio.connector;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;

import org.json.JSONObject;

import java.io.IOException;
import java.net.*;

public class ReceiveBroadcast{
    private String surrogateIpAddress="";
    private String surrogateMACAddress="";
    private Context context;

    public String getSurrogateIpAddress() {
        return surrogateIpAddress;
    }

    public String getSurrogateMACAddress() {
        return surrogateMACAddress;
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
        try {
            System.out.println(InetAddress.getByName("255.255.255.255"));
            DatagramSocket socket = new DatagramSocket(37020, InetAddress.getByName("0.0.0.0"));
            socket.setBroadcast(true);
            System.out.println("Listen on " + socket.getLocalAddress() + " from " + socket.getInetAddress() + " port " + socket.getBroadcast());
            byte[] buf = new byte[512];
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            // Get the registered MAC address of offload manager mobile app
            // vihangaaw content provider
                Uri contentUri = Uri.parse("content://com.vihangaaw.imperiooffloadmanager/cp_mac_address");
                ContentProviderClient contentProviderClient = context.getContentResolver().acquireContentProviderClient(contentUri);
                ContentResolver contentResolver = context.getContentResolver();
                Cursor cursor = null;
                try {
                  cursor = contentProviderClient.query(contentUri , null, null, null, null);
                } catch (RemoteException e) {
                  e.printStackTrace();
                }
                if(cursor!=null && cursor.getCount()>0) {
                  cursor.moveToFirst();
                  // Loop in the cursor to get each row.
                  do {
                    // Get column 1 value.
                    int column1Index = cursor.getColumnIndex("mac");
                    String column1Value = cursor.getString(column1Index);
                      surrogateMACAddress = column1Value;
                    System.out.println("Surrgate MAC Address from Offload Manager: "+surrogateMACAddress);
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
                String packetAsString=new String(buffer, 0, packet.getLength());
                System.out.println("Waiting for data3");
//                System.out.println(packetAsString);
                System.out.println("Data received");
                JSONObject objectOutput = new JSONObject(packetAsString);
                System.out.println("DTAA JSON");
                System.out.println(objectOutput.getString("MacAddress"));
                System.out.println(objectOutput.get("IpAddress"));
                if(buffer!=null){
                    //check whether mobile device's MAC address is in the message. If so, update surrogateAddress
                    System.out.println(surrogateMACAddress+"     "+objectOutput.getString("MacAddress"));
                    if(surrogateMACAddress.equals(objectOutput.getString("MacAddress"))){
                        surrogateIpAddress = objectOutput.getString("IpAddress");
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
