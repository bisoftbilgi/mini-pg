package com.bisoft.minipg.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubscriberDTO {
    private String datname;
    private String encoding;
    private String lc_collate;
    private String publisherAddress;
    private String publisherPort;
}
