package com.example.PackWeb;

import java.sql.Timestamp;

public class PackLogin {
    private final int id;
    private final String username;
    private final Timestamp timestamp;
    public PackLogin(int id, String username, Timestamp timestamp) {
        this.id = id;
        this.username = username;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
