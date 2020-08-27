package com.marian;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ServerThread implements Runnable {

    private Socket socket;
    private Database database;
    static ActiveSession activeSession;
    private User user;

    public ServerThread(Socket socket) {
        this.socket = socket;
        this.database = new Database();
        activeSession = new ActiveSession();
        this.user=new User();
    }

    @Override
    public void run() {
        System.out.println("Accepted user : " + socket.getInetAddress());

        byte requestType = 0;
        try {
            Scanner inputToServer = new Scanner(new InputStreamReader(socket.getInputStream()));
            if (inputToServer.hasNextLine()){
                requestType=Byte.parseByte(inputToServer.nextLine());
            }

            switch (requestType){
                case 1 :
                    System.out.println("Requested registration by : "+socket.getInetAddress());
                    register(inputToServer);
                    break;
                case 2: System.out.println("Requested login by : "+socket.getInetAddress());
                    if(login(inputToServer)){
                        System.out.println("Login permitted ..." +
                                "\nSending data back ...");
                        sendUserData();
                    }else {
                        System.out.println("Did not log in");
                       return;
                    }
                    message(inputToServer);
                    break;
                default:
                    System.out.println(Main.activeClients.get(this.socket.getInetAddress().toString()+" has disconnected ."));
                    Main.activeClients.remove(this.socket.getInetAddress().toString());
                    break;

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private boolean sendUserData(){
        System.out.println(user.toString());
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            out.write(user.getId() /*+ "\n"*/);
            out.flush();
            out.write(user.getName() + "\n");
            out.flush();
            return true;
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }
    }

    private void message(Scanner inputToServer) throws IOException {
        String received=null;
        while (inputToServer.hasNextLine()) {
            received = inputToServer.nextLine();
          /*  received = received.replaceAll("\n","");*/
            System.out.println("I got this message from" +socket.getInetAddress() +" : "+received);
            if (received.equals("/exit")){
                Main.activeClients.remove(this.socket.getInetAddress().toString());
                this.socket.close();
                System.out.println("User out!");
            }

            for (Socket client : Main.activeClients.values()){
                if (!client.isClosed()) {
                    if (!client.equals(this.socket)) {
                        System.out.println("Im printing for : " + client.getInetAddress());
                        try {
                            PrintWriter out = new PrintWriter(client.getOutputStream());
                            out.write(received + "\n");
                            out.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

    }

    private boolean login(Scanner inputToServer) throws IOException {
        inputToServer.reset();
        String username = inputToServer.nextLine();
        String password = inputToServer.nextLine();

        if (!database.open()){
            System.out.println("Can't open the Database");
            return false;
        }

        if (database.isRegisteredByUsername(username,password)){
            System.out.println("User "+ username + " has logged in.");

            int id =  database.getId(username,password);
            String nickname = database.getNickname(username,password);
            user.setId(id);
            user.setName(nickname);
            activeSession.addToSession(id,nickname);

            try{
                PrintWriter outputFromServer = new PrintWriter(socket.getOutputStream());
                outputFromServer.write(1);
                outputFromServer.flush();
                return true;

            }catch (IOException e){
                e.printStackTrace();
            }
        }else {
            System.out.println("User "+username+ " NOT registered!");
            try{
                PrintWriter outputFromServer = new PrintWriter(socket.getOutputStream());

                outputFromServer.write(0);
                outputFromServer.flush();


            }catch (IOException e){
                e.printStackTrace();
            }
        }

        activeSession.printAll();

        database.close();
        return false;

    }

    private boolean register(Scanner inputToServer){
        String nickName = inputToServer.nextLine();
        String userName = inputToServer.nextLine();
        String password = inputToServer.nextLine();

        if (!database.open()){
            System.out.println("Can't open the DB!");
            return false;
        }

        if(database.add(nickName,userName,password)) {
            System.out.println("Adding user data to database " + userName);

            try(OutputStreamWriter outputFromServer = new OutputStreamWriter(socket.getOutputStream())){
                System.out.println("Sending back...");
                outputFromServer.write(1);
            }catch (IOException e){
                e.printStackTrace();
            }
        }else {
            System.out.println("User"+userName+"already registered!");
            try(OutputStreamWriter outputFromServer = new OutputStreamWriter(socket.getOutputStream())){
                outputFromServer.write(0);
            }catch (IOException e){
                e.printStackTrace();
            }
            return true;
        }

        database.close();

        return false;
    }


}
