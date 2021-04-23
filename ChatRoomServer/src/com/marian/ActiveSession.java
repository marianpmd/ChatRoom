package com.marian;

import java.util.HashMap;
import java.util.Map;

/**
 * Utilizat in retinerea informatiilor corespunzatoare clientului
 */
public class ActiveSession {
    private HashMap<Integer,String> activeSession ;

    public ActiveSession() {
        this.activeSession =new HashMap<>();
    }

    public void addToSession(Integer key,String value){
        activeSession.put(key, value) ;
    }

    public void printAll(){
        for (Map.Entry<Integer,String> entry : activeSession.entrySet()){
            System.out.println("ID : " + entry.getKey() + "Nickname : " + entry.getValue());
        }
    }

    public HashMap<Integer,String> getActiveSession() {
        return activeSession;
    }
}
