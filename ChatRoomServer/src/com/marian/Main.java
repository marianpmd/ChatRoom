package com.marian;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Clasa responsabila pentru pornirea serverului pe un port anume
 */
public class Main {

    /**
     * HashMap utilizat in retinerea clientilor care acceseaza serverul
     *
     * !!! Nu functioneaza pentru utilizarea ca si host local deoarece cheia reprezinta adresa IP , astfel o singura instanta a unui client este
     * lasata sa se conecteze !!!
     */
    public static Vector<Socket> activeClients = new Vector<>();
    public static Set<String> online = new HashSet<>();
    //TODO : replace here when in production
    //public static ConcurrentHashMap<String , Socket> activeClients = new ConcurrentHashMap<>();
    public static String address ;


    /**
     * Se asculta dupa request-uri de conectare la server , iar in momentul in care apare una ,
     * se porneste un nou thread specific utilizatorului respectiv
     *
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("Server online...");
        try{
        ServerSocket serverSocket = new ServerSocket(3333);
        while (true) {
            Socket acceptedClient = serverSocket.accept();
            ServerThread serverThread = new ServerThread(acceptedClient);
            address = acceptedClient.getInetAddress().toString();
            //activeClients.put(acceptedClient.getInetAddress().toString(),acceptedClient);
            activeClients.add(acceptedClient);
            Thread thread = new Thread(serverThread);
            thread.start();
            System.out.println(activeClients.toString());
            System.out.println("online : " + online.toString());
        }

        }catch (IOException e){
            e.printStackTrace();
        }

    }

}
