package com.bisoft.minipg.service;

import lombok.Data;

@Data
public class MiniUser {
    private String userName;
    private String userPassword;
    private byte[] passAndUserMd5;
}
