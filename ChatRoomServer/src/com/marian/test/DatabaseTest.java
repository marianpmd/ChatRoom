package com.marian.test;

import com.marian.Database;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseTest {

    Database database = new Database();

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        database.open();

    }


    @org.junit.jupiter.api.Test
    void isRegisteredForLogin() {
        assertTrue(database.isRegisteredForLogin("vladdd","vladdd"));
    }

    @org.junit.jupiter.api.Test
    void checkUserAndAdd() {
        assertEquals(false,database.checkUserAndAdd("vlad","vladdd","vladdd"));
    }

    @org.junit.jupiter.api.Test
    void getId() {
         assertEquals(27,database.getId("vladdd","vladdd"));
    }

    @org.junit.jupiter.api.Test
    void getNickname() {
        assertEquals("vlad",database.getNickname("vladdd","vladdd"));
    }

    @AfterEach
    void tearDown() {
        database.close();
    }
}