package com.marian;

import java.sql.*;

public class Database {
    private Connection connection ;
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


    public boolean open(){
        try{
            connection = DriverManager.getConnection(CONNECTION_STRING);
            return true;
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }

    }

    public boolean isRegistered(String nickname , String username){
        try(PreparedStatement insertData = connection.prepareStatement(SELECT_USER_QUERY)) {
            insertData.setString(1,nickname);
            insertData.setString(2,username);
          //  System.out.println("QUERY = "+ SELECT_USER_QUERY);
          //  System.out.println("nickname : " +nickname+" username : " + username);
            try(ResultSet results = insertData.executeQuery()) {
                if (results.isBeforeFirst())
                return true;
            }catch (SQLException e){
           //     System.out.println("No entries found !");
                return false;
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean isRegisteredByUsername(String username ,String password){
        try(PreparedStatement statement = connection.prepareStatement(SELECT_USER_AND_PASSWORD_QUERY)){
            statement.setString(1,username);
            PasswordHarsher passwordHarsher = new PasswordHarsher();
            String hashedPassword = passwordHarsher.hash(password);
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

// FIXME: 8/27/2020 im working here
    public boolean add(String nickname , String username , String password){
        if (!isRegistered(nickname,username)){
            try(PreparedStatement insertData = connection.prepareStatement(INSERT_USER_QUERY)) {
                String hashedPassword = passwordHarsher.hash(password);
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
            PasswordHarsher passwordHarsher = new PasswordHarsher();
            getID.setString(2, passwordHarsher.hash(password));
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
            PasswordHarsher passwordHarsher = new PasswordHarsher();
            getID.setString(2, passwordHarsher.hash(password));
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
