package com.bisoft.minipg.service.pgwireprotocol.instruction.util;

import java.net.InetAddress;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class InstructionUtil {

    public String getRecoveryConfTemplateForWindows() {
        // --source-server="host=192.168.2.90 port=5432 user=postgres dbname=postgres password=080419"
        String recoveryConfTemplate = null;
        String hostName             = getHostName();

        recoveryConfTemplate = "standby_mode ='on'\n"
            + " primary_conninfo = 'user=postgres host={MASTER_IP} port={MASTER_PORT} sslmode=prefer sslcompression=1 krbsrvname=postgres application_name="
            + hostName
            + "'\n"
            + " recovery_target_timeline='latest' ";

        return recoveryConfTemplate;
    }

    public String getRecovertConfTemplateForLinux() {

        String recoveryConfTemplate = null;
        String hostName             = getHostName();

        recoveryConfTemplate = "standby_mode ='on'\n"
            + " primary_conninfo = 'user=postgres host={MASTER_IP} port={MASTER_PORT} sslmode=prefer sslcompression=1 krbsrvname=postgres target_session_attrs=any application_name="
            + hostName
            + "'\n"
            + " recovery_target_timeline='latest' ";

        return recoveryConfTemplate;

    }

    public String getRecoveryConfTemplateV12() {
        // --source-server="host=192.168.2.90 port=5432 user=postgres dbname=postgres password=080419"
        String recoveryConfTemplate = null;
        String hostName             = getHostName();

        recoveryConfTemplate = " primary_conninfo ='user=postgres passfile=''/var/lib/pgsql/.pgpass'' host={MASTER_IP} port={MASTER_PORT} sslmode=prefer sslcompression=0 krbsrvname=postgres target_session_attrs=any application_name="
            + hostName
            + "'";

        return recoveryConfTemplate;
    }

    public String getHostName() {

        InetAddress ip;
        String      hostname = UUID.randomUUID().toString();
        try {
            ip = InetAddress.getLocalHost();
            hostname = ip.getHostName();

        } catch (Exception e) {

            e.printStackTrace();
        }
        return hostname;
    }

}
