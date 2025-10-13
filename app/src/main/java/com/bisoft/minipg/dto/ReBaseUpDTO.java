package com.bisoft.minipg.dto;

import java.util.List;

import lombok.Data;

@Data
public class ReBaseUpDTO {
    private String masterIp;
    private String masterPort;
    private String repUser;
    private String repPassword;    
    private List<String> tablespaceList;
}
