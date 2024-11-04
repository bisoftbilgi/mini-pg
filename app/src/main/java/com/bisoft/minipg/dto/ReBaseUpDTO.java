package com.bisoft.minipg.dto;

import lombok.Data;

@Data
public class ReBaseUpDTO {
    private String masterIp;
    private String masterPort;
    private String repUser;
    private String repPassword;    
}
