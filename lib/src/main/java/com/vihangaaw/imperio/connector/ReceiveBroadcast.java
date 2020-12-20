package com.vihangaaw.imperio.connector;

import java.io.IOException;
import java.net.*;

public class ReceiveBroadcast implements Runnable{
    public static String surrogateAddress="";

    public void run(){
        // code in the other thread, can reference "var" variable
        try{
            System.out.println("Start executing broadcast receiver");
            surrogateDeviceScanner();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void surrogateDeviceScanner() throws SocketException, UnknownHostException, IOException {
        try {
            System.out.println(InetAddress.getByName("255.255.255.255"));
            DatagramSocket socket = new DatagramSocket(37020, InetAddress.getByName("0.0.0.0"));
            socket.setBroadcast(true);
            System.out.println("Listen on " + socket.getLocalAddress() + " from " + socket.getInetAddress() + " port " + socket.getBroadcast());
            byte[] buf = new byte[512];
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            while (true) {
                System.out.println("Waiting for data");
                socket.receive(packet);
                buffer = packet.getData();
                String packetAsString=new String(buffer, 0, packet.getLength());

                System.out.println(packetAsString);
                System.out.println("Data received");
                if(buffer!=null){
                    //check whether mobile device's MAC address is in the message. If so, update surrogateAddress
                    surrogateAddress = packetAsString;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
