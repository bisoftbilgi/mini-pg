package com.bisoft.minipg.model;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "minipg")
@Data
public class MiniPGLocalSettings {

    @Value("${minipg.postgres_bin_path}")
    private String postgresBinPath;

    @Value("${minipg.pgctl_bin_path}")
    private String pgCtlBinPath;

    @Value("${minipg.postgres_data_path}")
    private String postgresDataPath;

    @Value("${minipg.os}")
    private String os;

    @Value("${minipg.version}")
    private String version;

    @Value("${minipg.pg-version}")
    private String pgVersion;

}
