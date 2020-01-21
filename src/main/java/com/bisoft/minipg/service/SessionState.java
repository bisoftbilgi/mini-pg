package com.bisoft.minipg.service;

import lombok.Data;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@Lazy
@Data
public class SessionState {

    private String dbName   = "postgres";
    private String userName = "postgres";
    private byte[] salt;
}
