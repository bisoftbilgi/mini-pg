package com.bisoft.minipg.helper;


import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class MiniPGLocalSettings {

    @Value("${minipg.postgres_bin_path:/usr/pgsql-12/bin/}")
    private String postgresBinPath;

    @Value("${minipg.pgctl_bin_path:/usr/pgsql-12/bin/}")
    private String pgCtlBinPath;

    @Value("${minipg.postgres_data_path:/var/lib/postgres/12/data/}")
    private String postgresDataPath;

    @Value("${minipg.os:linux}")
    private String os;

    @Value("${minipg.version:1}")
    private String version;

    @Value("${minipg.pg-version:V12X}")
    private String pgVersion;

    @Value("${minipg.restore-command:/bin/true}")
    private String restoreCommand;

    @Value("${application.replication-user:postgres}")
    private String replicationUser;

    @Value("${application.vip-interface:eth0}")
    private String vipInterface;

    @Value("${application.vip-ip:127.0.0.1}")
    private String vipIp;

    @Value("${application.vip-ip-netmask:255.255.255.0}")
    private String vipIpNetmask;

    @Value("${minipg.ssl-mode:prefer}")
    private String sslMode;

    @Value("${minipg.ssl-compression:0}")
    private String sslCompression;

    @Value("${minipg.pg-password-file:/var/lib/postgres/.pgpass}")
    private String pgPassFile;

    @Value("${minipg.post-vip-up:ifconfig}")
    private String postVipUp;
}