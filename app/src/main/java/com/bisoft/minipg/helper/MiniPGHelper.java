package com.bisoft.minipg.helper;

import com.bisoft.minipg.PgVersion;
import com.bisoft.minipg.dto.PromoteDTO;
import com.bisoft.minipg.dto.ReBaseUpDTO;
import com.bisoft.minipg.dto.RewindDTO;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.PropertiesConfigurationLayout;
import org.springframework.stereotype.Component;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

@Data
@Slf4j
@RequiredArgsConstructor
@Component
public class MiniPGHelper {

    private final MiniPGLocalSettings miniPGlocalSetings;
    private  final  InstructionUtil     instructionUtil;
    private final LocalSqlExecutor localSqlExecutor;
    private final InstructionFacate instructionFacate;

    @PostConstruct
    public void init() throws Exception {
        List<String> postmaster_status = (new CommandExecutor()).executeCommandSync(
            miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl", "status",
            "-D" + miniPGlocalSetings.getPostgresDataPath());

        if ((postmaster_status.toString()).contains("PID:") && (postmaster_status.toString()).contains("server is running")){
            log.info("Postmaster is running with:"+postmaster_status.toString().substring((postmaster_status.toString().indexOf("PID:")-1), (postmaster_status.toString().indexOf(")")+1)));
        } else {
            log.info("Postmaster auto starting...");
            (new CommandExecutor()).executeCommandSync(
                miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl", "start","-w",
                                "-D" + miniPGlocalSetings.getPostgresDataPath());
        }

        StringBuilder result = new StringBuilder();
        String[] cmd = {miniPGlocalSetings.getPgCtlBinPath()+"psql", "-t", "-A", "--no-align", "-c", "show wal_log_hints"};

        ArrayList<String> cellValues = new ArrayList<>();

        Process pb = Runtime.getRuntime().exec(cmd);
        int resultNum = pb.waitFor();

        String line;

        BufferedReader input = new BufferedReader(new InputStreamReader(pb.getInputStream()));
        BufferedReader error = new BufferedReader(new InputStreamReader(pb.getErrorStream()));


        while ((line = input.readLine()) != null) {
            cellValues.add(line);
        }
        input.close();

        while ((line = error.readLine()) != null) {
            cellValues.add(line);
        }
        error.close();

        for (String s : cellValues)
        {
            result.append(s);
            result.append("\n");
        }

        if (result.indexOf("off") > -1){
            log.warn("Wal Log Hints is "+result);
            String wal_result = "";
            String[] cmd_wal = {miniPGlocalSetings.getPgCtlBinPath()+"psql", "-c", "alter system set wal_log_hints to on"};

            ArrayList<String> cellValues_wal = new ArrayList<>();

            Process pb_wal = Runtime.getRuntime().exec(cmd_wal);
            int exit_code = pb_wal.waitFor();

            String line_wal;

            BufferedReader input_wal = new BufferedReader(new InputStreamReader(pb_wal.getInputStream()));
            BufferedReader error_wal = new BufferedReader(new InputStreamReader(pb_wal.getErrorStream()));

            while ((line_wal = input_wal.readLine()) != null) {
                cellValues_wal.add(line_wal);
            }
            input_wal.close();

            while ((line = error_wal.readLine()) != null) {
                cellValues_wal.add(line_wal);
            }
            error_wal.close();

            for (String s : cellValues_wal)
            {
                wal_result += s +"\n";
            }
            log.info("Wal Log Hints SET result:"+wal_result);

            (new CommandExecutor()).executeCommandSync(
                    miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl", "restart","-w",
                    "-D" + miniPGlocalSetings.getPostgresDataPath());

        } else {
            log.info("wal_log_hints already set to on");
        }
    
    }

    public String getEmbeddedSystemValue(String key) {

        Properties properties = new Properties();

        String propFileName = "embedded_version.properties";

        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(propFileName);

        try {

            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties.getProperty(key);
    }

    public String prepareForSwitchOver(){
        List<String> result_ro = (new CommandExecutor()).executeCommandSync(
            miniPGlocalSetings.getPgCtlBinPath() + "psql", "-c","ALTER SYSTEM SET default_transaction_read_only TO on;");

        if ((result_ro.toString()).contains("error") || (result_ro.toString()).contains("fatal")){
            log.info(" Error occurrred on altering Master Pg to Read Only, error:"+result_ro.toString());
            return result_ro.toString();
        } 

        List<String> result_reload = (new CommandExecutor()).executeCommandSync(
            miniPGlocalSetings.getPgCtlBinPath() + "psql", "-c","SELECT pg_reload_conf();");

        if ((result_reload.toString()).contains("error") || (result_reload.toString()).contains("fatal")){
            log.info(" Error occurrred on pg_reload_conf, error:"+result_reload.toString());
            return result_reload.toString();
        } 

        (new CommandExecutor()).executeCommandSync(
            miniPGlocalSetings.getPgCtlBinPath() + "psql", "-c","select pg_terminate_backend(pid) from pg_stat_activity;");

        List<String> result_walSW = (new CommandExecutor()).executeCommandSync(
            miniPGlocalSetings.getPgCtlBinPath() + "psql", "-c","SELECT  pg_switch_wal();");

        if ((result_walSW.toString()).contains("error") || (result_walSW.toString()).contains("fatal")){
            log.info(" Error occurrred on pg_switch_wal, error:"+result_walSW.toString());
            return result_walSW.toString();
        } 

        List<String> result_checkpoint = (new CommandExecutor()).executeCommandSync(
            miniPGlocalSetings.getPgCtlBinPath() + "psql", "-c","CHECKPOINT ;");

        if ((result_checkpoint.toString()).contains("error") || (result_checkpoint.toString()).contains("fatal")){
            log.info(" Error occurrred on CHECKPOINT, error:"+result_checkpoint.toString());
            return result_checkpoint.toString();
        }
        
        List<String> result_stop = (new ScriptExecutor()).executeScript(
                    miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl",
                    "stop",
                    "-D" + miniPGlocalSetings.getPostgresDataPath(),
                    "-mi");

        if ((result_stop.toString()).contains("error") || (result_stop.toString()).contains("fatal")){
            log.info(" Error occurrred on STOP PG, error:"+result_stop.toString());
            return result_stop.toString();
        }

        return "OK";
    }

    public String postSwitchOver(PromoteDTO promoteDTO){
        Boolean revindSuccess = instructionFacate.tryRewindSync(promoteDTO.getMasterIp(),promoteDTO.getPort(), promoteDTO.getUser(), promoteDTO.getPassword());
        if (!revindSuccess)
            return null;

        instructionFacate.tryAppendRestoreCommandToAutoConfFile();
        
        String repUser = miniPGlocalSetings.getReplicationUser();
        if (repUser == null || repUser.equals("")) {
            repUser = promoteDTO.getUser();
        }

        instructionFacate.tryToAppendConnInfoToAutoConfFile(promoteDTO.getMasterIp(), promoteDTO.getPort(), repUser);
        instructionFacate.tryAppendLineToAutoConfFile("recovery_target_timeline = 'latest'");

        List<String> result_start1 = (new ScriptExecutor()).executeScript(
                    miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl",
                    "start",
                    "-w",
                    "-D" + miniPGlocalSetings.getPostgresDataPath());

        if ((result_start1.toString()).contains("error") || (result_start1.toString()).contains("fatal")){
            log.info(" Error occurrred on START PG, error:"+result_start1.toString());
            return result_start1.toString();
        }

        List<String> result_rw = (new CommandExecutor()).executeCommandSync(
            miniPGlocalSetings.getPgCtlBinPath() + "psql", "-c","ALTER SYSTEM SET default_transaction_read_only TO off;");

        if ((result_rw.toString()).contains("error") || (result_rw.toString()).contains("fatal")){
            log.info(" Error occurrred on altering Pg to Read Only to R/W, error:"+result_rw.toString());
            return result_rw.toString();
        } 

        List<String> result_reload = (new CommandExecutor()).executeCommandSync(
            miniPGlocalSetings.getPgCtlBinPath() + "psql", "-c","SELECT pg_reload_conf();");

        if ((result_reload.toString()).contains("error") || (result_reload.toString()).contains("fatal")){
            log.info(" Error occurrred on pg_reload_conf, error:"+result_reload.toString());
            return result_reload.toString();
        }
        
        List<String> result_stop = (new ScriptExecutor()).executeScript(
            miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl",
            "stop",
            "-D" + miniPGlocalSetings.getPostgresDataPath(),
            "-mi");

        if ((result_stop.toString()).contains("error") || (result_stop.toString()).contains("fatal")){
            log.info(" Error occurrred on STOP PG, error:"+result_stop.toString());
            return result_stop.toString();
        }

        List<String> result_start = (new ScriptExecutor()).executeScript(
            miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl",
            "start",
            "-D" + miniPGlocalSetings.getPostgresDataPath(),
            "-mi");

        if ((result_start.toString()).contains("error") || (result_start.toString()).contains("fatal")){
            log.info(" Error occurrred on START PG, error:"+result_start.toString());
            return result_start.toString();
        }

        return "OK";
    }

    public List<String> promoteV10() {

        return (new ScriptExecutor()).executeScriptSync(
                miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl",
                "promote",
                "-D" + miniPGlocalSetings.getPostgresDataPath());
    }

    public List<String> promoteV12(final PromoteDTO promoteDTO) {

        List<String> result = (new ScriptExecutor()).executeScriptSync(
                miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl",
                "promote",
                "-D" + miniPGlocalSetings.getPostgresDataPath());

        (new LocalSqlExecutor()).executeLocalSql("CHECKPOINT", promoteDTO.getPort(), promoteDTO.getUser(),
               promoteDTO.getPassword());

        // 4. reload conf
        localSqlExecutor.executeLocalSql("SELECT pg_reload_conf()",  promoteDTO.getPort(), promoteDTO.getUser(),promoteDTO.getPassword());

        return result;
    }

    private void runReload() throws Exception{
        (new CommandExecutor()).executeCommand(miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl", "reload",
                "-D" + miniPGlocalSetings.getPostgresDataPath());
    }

    public Boolean checkIfMaster() {

        Boolean res = true;
        try {
            File file = new File(miniPGlocalSetings.getPostgresDataPath()+ "recovery.conf");
            if (file.exists() && !file.isDirectory()) {
                res = true;
            } else {
                res = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public void removeSyncNode(String syncName) throws Exception {
        if (checkIfMaster()) {
            removeSubProp(miniPGlocalSetings.getPostgresDataPath() + "data/postgresql.auto.conf",
                    "synchronous_standby_names", syncName);
            runReload();
        } else {
            throw new Exception("I'm not a master!");
        }
    }

    public void removeSubProp(String propertyFile, String propertyKey, String propValue) throws Exception {

        PropertiesConfiguration config = new PropertiesConfiguration();
        PropertiesConfigurationLayout layout = new PropertiesConfigurationLayout();
        config.setLayout(layout);
        layout.load(config, new FileReader(propertyFile));

        // remove comments if exists
        String syncNodes = config.getProperty(propertyKey).toString().replace("'", "");
        int    offset    = syncNodes.indexOf("#");

        if (-1 != offset) {
            syncNodes = syncNodes.substring(0, offset);
        }

        String[]     syncNodesArr = syncNodes.split(",");
        List<String> ipList       = new LinkedList<>(Arrays.asList(syncNodesArr));

        List<String> newList = ipList.stream() // convert list to stream
                .filter(line -> !propValue.equals(line) && line.trim().length() > 0).collect(Collectors.toList());

        String line = "'" + newList.stream().collect(Collectors.joining(",")) + "'";
        config.setProperty("synchronous_standby_names", line);

        layout.save(config, new FileWriter(propertyFile));
    }

    public String doReBaseUp(ReBaseUpDTO rebaseUpDTO) {
        Integer logNumber = 1;

        PgVersion localVersion = PgVersion.valueOf(miniPGlocalSetings.getPgVersion());
        log.info("Verison of Postgresql Dedected As " + localVersion);

        if (localVersion == PgVersion.V12X || localVersion == PgVersion.V13X || localVersion == PgVersion.V14X) {
            //1. stepstop the pg but it's supposed to already stopped by watcher
            log.info(String.valueOf(logNumber++)+". step : stop pg");
            (new ScriptExecutor()).executeScript(
                    miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl",
                    "stop",
                    "-D" + miniPGlocalSetings.getPostgresDataPath(),
                    "-mi");

            
            // 2. rejoin to cluster with pg_basebackup
            log.info(String.valueOf(logNumber++)+". step : rejoining with pg_basebackup..");
            try {
                // 2.1. move broken data folder and backup
                LocalDateTime ldateTime = LocalDateTime.now();
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");

                String formattedDate = ldateTime.format(dateFormatter);
                String newDataDirFullPath = miniPGlocalSetings.getPostgresDataPath().replaceAll("\\b"+"data"+"\\b", "data_"+formattedDate);
                File f = new File(miniPGlocalSetings.getPostgresDataPath());
                if (f.exists() && f.isDirectory()) {
                    log.info(String.valueOf(logNumber++)+". step : move broken data directory with new name:", newDataDirFullPath);
                    (new ScriptExecutor()).executeScript(
                            "mv", 
                            miniPGlocalSetings.getPostgresDataPath(),
                            newDataDirFullPath);
                } else {
                    log.info("data dir not found:"+miniPGlocalSetings.getPostgresDataPath());
                }

                try {
                    //2.2 copy master db with pg_basebackup
                    log.info("1. create pg_basebackup rejoin script");
                    String filename = "/tmp/rejoin.sh";
                    instructionFacate.createRebaseScript(filename, rebaseUpDTO.getMasterIp(), rebaseUpDTO.getRepUser(), rebaseUpDTO.getRepPassword(), rebaseUpDTO.getMasterPort());
                    log.info("2. execute rejoin script");
                    (new CommandExecutor()).executeCommandSync(
                    "/bin/bash", filename);

                    // 2.3 start the server
                    log.info(String.valueOf(logNumber++)+". step : start server");
                    Boolean start_result = instructionFacate.tryStartSync();
                    log.info("start Server result", start_result);
                    if (!instructionFacate.tryStartSync())
                        return null;

                } catch (Exception ex) {
                    ex.printStackTrace();
                    (new ScriptExecutor()).executeScript(
                        "mv", 
                        newDataDirFullPath,
                        miniPGlocalSetings.getPostgresDataPath());
                        return null;
                }
            } 
            catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }            
        }

        return "OK";
    }

    public String doRewind(RewindDTO rewindDTO) {

        Integer logNumber = 1;

        String repUser = miniPGlocalSetings.getReplicationUser();
        if (repUser == null || repUser.equals("")) {
            repUser = rewindDTO.getUser();
        }


        // TODO: server is open now you can get the exact version from the server.
        PgVersion localVersion = PgVersion.valueOf(miniPGlocalSetings.getPgVersion());
        log.info("Verison of Postgresql Dedected As " + localVersion);

        if (localVersion == PgVersion.V11X || localVersion == PgVersion.V10X) {

            log.info("1. create rewind script");
            String filename = "/tmp/rewind.sh";
            instructionFacate.createRewindScript(filename, rewindDTO.getMasterIp(), rewindDTO.getUser(), rewindDTO.getPassword(), rewindDTO.getPort());
            log.info("2. execute rewind script");
            (new CommandExecutor()).executeCommandSync(
                    "/bin/bash", filename);

            log.info("3. step : checkpoint");
            instructionFacate.checkPoint(rewindDTO.getPort(), rewindDTO.getUser(),rewindDTO.getPassword());

            log.info("RESULT  : rewind ended successfully");


        }

        if (localVersion == PgVersion.V12X || localVersion == PgVersion.V13X || localVersion == PgVersion.V14X) {
            //1. stepstop the pg but it's supposed to already stopped by watcher
            log.info(String.valueOf(logNumber++)+". step : stop pg");
            (new ScriptExecutor()).executeScript(
                    miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl",
                    "stop",
                    "-D" + miniPGlocalSetings.getPostgresDataPath(),
                    "-mi");

            // 2. touch standby.signal
            log.info(String.valueOf(logNumber++)+". step : create standby.signal");
            instructionFacate.tryTouchingStandby();

            // 3. append restore_command=''
            log.info(String.valueOf(logNumber++)+". step : Appending Restore Command to conf");
          //  instructionFacate.tryAppendLineToAutoConfFile("restore_command = ' '");
           // instructionFacate.tryAppendLineToAutoConfFile("recovery_target_timeline = 'current'");

            // 4 .start the server
            // log.info(String.valueOf(logNumber++)+". step : start server");
            // Boolean start_result = instructionFacate.tryStartSync();
            // log.info("start Server result", start_result);
            // if (!instructionFacate.tryStartSync())
            //     return null;
            // 5. stop the server...
            // log.info(String.valueOf(logNumber++)+". step : stop server");
            // (new CommandExecutor()).executeCommandSync(
            //         miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl", "stop",
            //         "-D" + miniPGlocalSetings.getPostgresDataPath());

            // 6 . do rewind...
            log.info(String.valueOf(logNumber++)+". step : do rewind");
            Boolean revindSuccess = instructionFacate.tryRewindSync(rewindDTO.getMasterIp(),rewindDTO.getPort(), rewindDTO.getUser(), rewindDTO.getPassword());
            if (!revindSuccess)
                return null;

            // TODO: check here for syntax and logical error cases
            if (revindSuccess == null) {
                return " no way for successful completing the recovery";
            }


            // 7.append parameters to pg auto conf
            log.info(String.valueOf(logNumber++)+". step : Appending Restore Command");
            instructionFacate.tryAppendRestoreCommandToAutoConfFile();
            instructionFacate.tryToAppendConnInfoToAutoConfFile(rewindDTO.getMasterIp(), rewindDTO.getPort(), repUser);
            instructionFacate.tryAppendLineToAutoConfFile("recovery_target_timeline = 'latest'");
            


            // 8. create recovery.signal and standby.signal files
            log.info("10. step : create recovery.signal and standby.signal");
            instructionFacate.tryTouchingRecovery();
            instructionFacate.tryTouchingStandby();

            // 9 .start the server
            log.info(String.valueOf(logNumber++)+". step : start server");
            if (!instructionFacate.tryStartSyncForRecovery(rewindDTO.getPort(), rewindDTO.getUser(), rewindDTO.getPassword()))
                return null;

            // 10. checkpoint
            log.info(String.valueOf(logNumber++)+". step : checkpoint");
            instructionFacate.checkPoint(rewindDTO.getPort(), rewindDTO.getUser(), rewindDTO.getPassword());

            //11. execute local restore and primary conn info
            log.info(String.valueOf(logNumber++)+". step : execute local restore and primary conn info");
            instructionFacate.executeLocalRestoreCommand(rewindDTO.getPort(), rewindDTO.getUser(), rewindDTO.getPassword());
            instructionFacate.executeLocalPrimaryConnInfo(rewindDTO.getPort(), rewindDTO.getUser(), rewindDTO.getPassword(),
                    rewindDTO.getMasterIp()
            );
            instructionFacate.executeLocalTargetTimeLineLatest(
                    rewindDTO.getPort(),
                    rewindDTO.getUser(),
                    rewindDTO.getPassword());

            // 12. reload configuration
            log.info(String.valueOf(logNumber++)+". step reload pg configuration");
            instructionFacate.reloadConf(
                    rewindDTO.getPort(),
                    rewindDTO.getUser(),
                    rewindDTO.getPassword());

        }
        return "OK";
    }

    public boolean startContinues() {

        List<ProcessHandle> result = ProcessHandle.allProcesses().filter(p -> p.info().toString().contains("startup"))
                .collect(Collectors.toList());
        return (result.size() > 0);

    }

    private boolean rewindContinues() {

        List<ProcessHandle> result = ProcessHandle.allProcesses().filter(p -> p.info().toString().contains("pg_rewind"))
                .collect(Collectors.toList());
        return (result.size() > 0);

    }

    // @Deprecated
    private void reGenerateRecoveryConfByEditingConfFile(RewindDTO rewindDTO) {

        String repUser = miniPGlocalSetings.getReplicationUser();
        if (repUser == null || repUser.equals("")) {
            repUser = rewindDTO.getUser();
        }

        log.info("REGENERATING recovery.conf with " + rewindDTO.getMasterIp() + ":" + rewindDTO.getPort());

        String recoveryConfTemplate = getRecoveryConfTemplate();

        Writer writer = null;

        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(miniPGlocalSetings.getPostgresDataPath() + "recovery.conf"), "utf-8"));
            writer.write(
                    recoveryConfTemplate
                            .replace("{MASTER_IP}", rewindDTO.getMasterIp())
                            .replace("{MASTER_PORT}", rewindDTO.getPort())
//                    .replace("{USER}", rewindDTO.getUser())
                            .replace("{USER}", repUser)
            );
        } catch (IOException ex) {

            ex.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // @Deprecated
    private boolean reGenerateRecoveryConfByExecutingStatement(RewindDTO rewindDTO) {

        String repUser = miniPGlocalSetings.getReplicationUser();
        if (repUser == null || repUser.equals("")) {
            repUser = rewindDTO.getUser();
        }

        log.info("REGENERATING recovery.conf with " + rewindDTO.getMasterIp() + ":" + rewindDTO.getPort());
        String recoveryConfTemplate = instructionUtil.getRecoveryConfTemplateV12();
        String recoveryConfSql = "alter system set "
                + recoveryConfTemplate
                .replace("{MASTER_IP}", rewindDTO.getMasterIp())
                .replace("{MASTER_PORT}", rewindDTO.getPort())
                .replace("{USER}", repUser)
                .replace("{SSL_MODE}",miniPGlocalSetings.getSslMode())
                .replace("{SSL_COMPRESSION}",miniPGlocalSetings.getSslCompression());
//        PgVersion localVersion = localSqlExecutor.getPgVersion(rewindDTO.getPort(), rewindDTO.getUser(), rewindDTO.getPassword());

        try {
            //step:3  1. touch <data>/standby.signal
            (new CommandExecutor()).executeCommandSync("touch",
                    miniPGlocalSetings.getPostgresDataPath() + "standby.signal");

            //step:3  1. touch <data>/standby.signal
            (new CommandExecutor()).executeCommandSync("touch",
                    miniPGlocalSetings.getPostgresDataPath() + "recovery.signal");

            //step 4:  2. start the server
            (new CommandExecutor()).executeCommandSync(
                    miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl", "start",
                    "-D" + miniPGlocalSetings.getPostgresDataPath());

            //step 5:  3. execute sql
            localSqlExecutor.executeLocalSql(recoveryConfSql, rewindDTO.getPort(), rewindDTO.getUser(), rewindDTO.getPassword());

            // 4. reload conf
            localSqlExecutor.executeLocalSql("SELECT pg_reload_conf()", rewindDTO.getPort(), rewindDTO.getUser(), rewindDTO.getPassword());
            //step 4-a:  2. start the server
            (new CommandExecutor()).executeCommandSync(
                    miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl", "stop",
                    "-D" + miniPGlocalSetings.getPostgresDataPath());

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;

    }

    private String getRecoveryConfTemplate() {

        if (miniPGlocalSetings.getOs().toLowerCase().startsWith("windows")) {

            return instructionUtil.getRecoveryConfTemplateForWindows();
        } else {
            return instructionUtil.getRecoveryConfTemplateForLinux();
        }

    }
}
