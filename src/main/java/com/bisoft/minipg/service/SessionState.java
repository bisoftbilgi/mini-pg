package com.bisoft.minipg.service;


import java.util.Random;

import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@Lazy
public class SessionState {
    private String dbName = "";
    private String userName = "";
    private byte[] salt;

    public SessionState() {
        super();
        salt = new byte[4];
        new Random().nextBytes(salt);
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public byte[] getSalt() {
        return salt;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }
}
