package com.bisoft.minipg.helper;


import java.net.InetAddress;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InstructionUtil {

    @Value("${minipg.postgres_pass_file_path}")
    public String pgPassFilePath;

    @Autowired
    private MiniPGLocalSettings miniPGLocalSettings;

    private String requireSsl;

    public String getRecoveryConfTemplateForWindows() {
        // --source-server="host=192.168.2.90 port=5432 user=postgres dbname=postgres password=080419"
        String recoveryConfTemplate = null;
        String hostName             = getHostName();

        recoveryConfTemplate = "standby_mode ='on'\n"
                + " primary_conninfo = 'user={USER} host={MASTER_IP} port={MASTER_PORT} sslmode=prefer sslcompression=1 krbsrvname=postgres application_name="
                + hostName
                + "'\n"
                + " recovery_target_timeline='latest' ";

        return recoveryConfTemplate;
    }

    public String getRecoveryConfTemplateForLinux() {

        String recoveryConfTemplate = null;
        String hostName             = getHostName();

        recoveryConfTemplate = "standby_mode ='on'\n"
                + " primary_conninfo = 'user={USER}  host={MASTER_IP} port={MASTER_PORT} sslmode=prefer sslcompression=1 krbsrvname=postgres target_session_attrs=any application_name="
                + hostName
                + "'\n"
                + " recovery_target_timeline='latest' ";

        return recoveryConfTemplate;

    }

    public String getRecoveryConfTemplateV10() {
        // --source-server="host=192.168.2.90 port=5432 user=postgres dbname=postgres password=080419"
        String recoveryConfTemplate = null;
        String hostName             = getHostName();

        recoveryConfTemplate = " primary_conninfo ='user={USER} passfile=''{PG_PASS_FILE}'' host={MASTER_IP} port={MASTER_PORT} sslmode=prefer sslcompression=0 krbsrvname=postgres target_session_attrs=any application_name="
                + hostName
                + "'";

        return recoveryConfTemplate.replace("{PG_PASS_FILE}", pgPassFilePath);
    }

    public String getRecoveryConfTemplateV12() {
        // --source-server="host=192.168.2.90 port=5432 user=postgres dbname=postgres password=080419"
        String recoveryConfTemplate = null;
        String hostName             = getHostName();
        recoveryConfTemplate = " primary_conninfo ='user={USER} passfile=''{PG_PASS_FILE}'' host={MASTER_IP} port={MASTER_PORT} sslmode={SSL_MODE} sslcompression={SSL_COMPRESSION} krbsrvname=postgres target_session_attrs=any application_name="
                + hostName
                + "'";

        return recoveryConfTemplate.replace("{PG_PASS_FILE}", pgPassFilePath);
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
