package com.marian;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Clasa ce implementeaza interfata Runnable(Thread)
 * Este un fir de executie , responsabil pentru procesarea request-urilor venite de la client , in functie de protocoale;
 */
public class ServerThread implements Runnable {

    private Socket socket;
    private Database database;
    static ActiveSession activeSession;
    private User user;

    /**
     * Initializeaza socketul , baza de date si sesiunea curenta
     * @param socket socket-ul aferent clientului (adresa ip , input stream , etc..)
     */
    public ServerThread(Socket socket) {
        this.socket = socket;
        this.database = new Database();
        activeSession = new ActiveSession();
    }

    /**
     * run() este o functie apelata automat la pornirea unui thread (printr-un apel la start())
     *
     * Se asculta inputul venit de la client , si in functie de protocol de ex : 1 pentru inregistare ,
     * se efectueaza inregistrarea , logarea si trimiterea mesajelor (ce presupune o logare aferenta a clientului)
     */
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
                    //TODO : replace here when in production
                    /*System.out.println(Main.activeClients.get(this.socket.getInetAddress().toString()+" has disconnected ."));
                    Main.activeClients.remove(this.socket.getInetAddress().toString());*/
                    break;

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Se trimit informatiile precum id-ul si nickename-ul clientului , catre client
     * @return true daca operatiunea a reusit , false in caz contrar
     */
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

    /**
     * Handler pentru primirea mesajelor , si distribuirea acestora , la toti clientii logati
     *
     * @param inputToServer input stream pentru mesaje
     * @throws IOException
     */
    private void message(Scanner inputToServer) throws IOException {
        String received=null;
        while (inputToServer.hasNextLine()) {
            received = inputToServer.nextLine();

            System.out.println("I got this message from" +socket.getInetAddress() +" : "+received);
            if (received.equals("/exit")){
                Main.activeClients.remove(this.socket.getInetAddress().toString());
                System.out.println("User"+this.socket.getInetAddress().toString()+"out!");
                this.socket.close();
            }
            //TODO : for (Socket client : Main.activeClients.values()){
            for (Socket client : Main.activeClients){
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

    /**
     * Handler pentru operatiunea de logare
     * se deschide baza de date , se realizeaza interogarea corespunzatoare datelor primite
     * daca rezultatul exista , se trimite confirmarea inapoi la client
     *
     * @param inputToServer
     * @return true daca utilizatorul a fost logat , false in caz contrar
     * @throws IOException
     */
    private boolean login(Scanner inputToServer) throws IOException {
        String username = inputToServer.nextLine();
        String password = inputToServer.nextLine();

        if (!database.open()){
            System.out.println("Can't open the Database");
            return false;
        }

        if (database.isRegisteredForLogin(username,password)){
            System.out.println("User "+ username + " has logged in.");

            int id =  database.getId(username,password);
            String nickname = database.getNickname(username,password);

            user = new User(id,nickname);

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
    /**
     * Handler pentru operatiunea de inregistrare
     * se deschide baza de date , se realizeaza interogarea corespunzatoare datelor primite
     * daca rezultatul nu exista , se trimite confirmarea inapoi la client si se inregistreaza in baza de date
     *
     * @param inputToServer
     * @return true daca utilizatorul a fost inregistrat , false in caz contrar
     */
    private boolean register(Scanner inputToServer){
        String nickName = inputToServer.nextLine();
        String userName = inputToServer.nextLine();
        String password = inputToServer.nextLine();

        if (!database.open()){
            System.out.println("Can't open the DB!");
            return false;
        }

        if(database.checkUserAndAdd(nickName,userName,password)) {
            System.out.println("Adding user data to database " + userName);

            try{//TODO repl try with resources
                OutputStreamWriter outputFromServer = new OutputStreamWriter(socket.getOutputStream());
                System.out.println("Sending back...");

                outputFromServer.write(1);
                outputFromServer.flush();
            }catch (IOException e){
                e.printStackTrace();
            }
        }else {
            System.out.println("User"+userName+"already registered!");
            try(OutputStreamWriter outputFromServer = new OutputStreamWriter(socket.getOutputStream())){
                outputFromServer.write(0);
                outputFromServer.flush();
            }catch (IOException e){
                e.printStackTrace();
            }
            return true;
        }

        database.close();

        return false;
    }


}
