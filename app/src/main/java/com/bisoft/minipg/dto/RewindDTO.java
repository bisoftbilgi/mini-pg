package com.bisoft.minipg.dto;

import java.util.List;

import lombok.Data;

@Data
public class RewindDTO {
    private String port;
    private String user;
    private String password;
    private String masterIp;
    private List<String> tablespaceList;
}
