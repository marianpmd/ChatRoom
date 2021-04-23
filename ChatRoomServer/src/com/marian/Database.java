package com.marian;

import java.sql.*;

/**
 * Clasa care se ocupa de accesul si interoagarea bazei de date asociate aplicatiei
 *
 * field-urile public static final String , reprezinta interogarile necesare aplicatiei , astfel fiind scrise ca si constante
 * ele pot fi schimbate o singura data in cazul in care este nevoie fara sa mai trebuiasca sa se rescrie in diverse locuri
 */
public class Database {
    /**
     * Obiect ce relaizaeaza conexiunea la baza de date , este necesar un driver JDBC
     */
    private Connection connection ;
    /**
     * Obiect utilizat in criptarea parolelor pentru inserarea si verificarea acestora in baza de date
     */
    private PasswordHarsher passwordHarsher = new PasswordHarsher();

    public static final String DB_NAME= "ChatRoomDatabase.db";
    public static final String CONNECTION_STRING = "jdbc:sqlite:C:\\Users\\Marian\\Desktop\\Server-Client App\\ChatRoomServer\\src\\" + DB_NAME;
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID="id";
    public static final String COLUMN_NICKNAME = "nickname";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String INSERT_USER_QUERY = "INSERT INTO " + TABLE_USERS +
            "(" + COLUMN_NICKNAME + " , " + COLUMN_USERNAME + " , " + COLUMN_PASSWORD + ")"+
            " VALUES (?,?,?)";


    public static final String SELECT_USER_QUERY =  "SELECT " + COLUMN_USERNAME + " , " +  COLUMN_NICKNAME+
            " FROM " + TABLE_USERS +
            " WHERE " + COLUMN_NICKNAME+" =? AND "+ COLUMN_USERNAME + " = ?";


    public static final String SELECT_USER_AND_PASSWORD_QUERY = "SELECT " + COLUMN_USERNAME + " , " + COLUMN_PASSWORD +
        " FROM " + TABLE_USERS +
        " WHERE " + COLUMN_USERNAME +  " =?" + " AND " + COLUMN_PASSWORD + " =?";

    public static final String SELECT_ID_FROM_USERS = "SELECT " + COLUMN_ID +
            " FROM " + TABLE_USERS +
            " WHERE " + COLUMN_USERNAME +  " =?" + " AND " + COLUMN_PASSWORD + " =?";

    public static final String  SELECT_NICKNAME_FROM_USERS = "SELECT " + COLUMN_NICKNAME +
            " FROM " + TABLE_USERS +
            " WHERE " + COLUMN_USERNAME +  " =?" + " AND " + COLUMN_PASSWORD + " =?";


    /**
     * Realizeaza conexiune la baza de date , in functie de un CONNECTION_STRING ce contine locaia la care se afla baza de date
     *
     * @return true daca conexiunea la baza de date se poate realiza , false in caz contrar
     */
    public boolean open(){
        try{
            connection = DriverManager.getConnection(CONNECTION_STRING);
            return true;
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Functie care verifica daca un utilizator este sau nu inregistrat in baza de date
     *
     * @param nickname numele care se afiseaza
     * @param username numele de utilizator
     * @return true daca este inregistrat , false in caz contrar
     */
    private boolean isRegistered(String nickname , String username){
        try(PreparedStatement insertData = connection.prepareStatement(SELECT_USER_QUERY)) {
            insertData.setString(1,nickname);
            insertData.setString(2,username);
            try(ResultSet results = insertData.executeQuery()) {
                if (results.isBeforeFirst())
                return true;
            }catch (SQLException e){
                e.printStackTrace();
                return false;
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Functie care verifica daca un utilizator este inregistrat in baza de date , in functie de numele de utilizator si parola
     *
     * @param username numele de utilizator
     * @param password parola
     * @return true daca este inregistrat , false in caz contrar
     */
    public boolean isRegisteredForLogin(String username , String password){
        try(PreparedStatement statement = connection.prepareStatement(SELECT_USER_AND_PASSWORD_QUERY)){
            statement.setString(1,username);
            String hashedPassword = passwordHarsher.hashWithSalt(password);
            statement.setString(2,hashedPassword);
            try(ResultSet results = statement.executeQuery()){

                if (results.getString("username").equals(username) && results.getString("password").equals(hashedPassword)){
                    return true;
                }
            }catch (SQLException e){
                return false;
            }


        }catch (SQLException e){
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Functie care verifica daca utlilizatorul este eligibil pentru a se conecta si de asemenea , il adauga in baza de date
     *
     *
     * @param nickname numele care se afiseaza
     * @param username numele de utilizator
     * @param password parola
     * @return true daca utilizatorul a fost adaugat , false in caz contrar
     */
    public boolean checkUserAndAdd(String nickname , String username , String password){
        if (!isRegistered(nickname,username)){
            try(PreparedStatement insertData = connection.prepareStatement(INSERT_USER_QUERY)) {
                String hashedPassword = passwordHarsher.hashWithSalt(password);
                insertData.setString(1,nickname);
                insertData.setString(2,username);
                insertData.setString(3,hashedPassword);
                insertData.execute();
                return true;

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }else {
            return false;
        }
        return false;
    }


    /**
     * Inchiderea conexiunii cu baza de date
     */
    public void close(){
        try{
            if (connection!=null){
                connection.close();
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public int getId(String username, String password) {

        try(PreparedStatement getID = connection.prepareStatement(SELECT_ID_FROM_USERS)) {
            getID.setString(1,username);
            getID.setString(2, passwordHarsher.hashWithSalt(password));
            getID.execute();
            try(ResultSet results = getID.executeQuery() ){
                int id = results.getInt(1);
                return id;

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;

    }

    public String getNickname(String username, String password) {

        try(PreparedStatement getID = connection.prepareStatement(SELECT_NICKNAME_FROM_USERS)) {
            getID.setString(1,username);
            getID.setString(2, passwordHarsher.hashWithSalt(password));
            getID.execute();
            try(ResultSet results = getID.executeQuery() ){
                String value = results.getString(1);
                return value;

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
