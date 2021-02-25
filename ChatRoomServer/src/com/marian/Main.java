package com.marian;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;


public class Main {
    public static ConcurrentHashMap<String , Socket> activeClients = new ConcurrentHashMap<>();
    public static String address ;


    public static void main(String[] args) {
        System.out.println("Server online...");
        try{
        ServerSocket serverSocket = new ServerSocket(3333);
        while (true) {
            Socket acceptedClient = serverSocket.accept();
            ServerThread serverThread = new ServerThread(acceptedClient);
            address = acceptedClient.getInetAddress().toString();
            activeClients.put(acceptedClient.getInetAddress().toString(),acceptedClient);
            Thread thread = new Thread(serverThread);
            thread.start();
            System.out.println(activeClients.toString());
        }

        }catch (IOException e){
            e.printStackTrace();
        }

    }

}
