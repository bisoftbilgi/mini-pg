package com.bisoft.minipg.helper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.PropertiesConfigurationLayout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.bisoft.minipg.PgVersion;
import com.bisoft.minipg.dto.PromoteDTO;
import com.bisoft.minipg.dto.ReBaseUpDTO;
import com.bisoft.minipg.dto.RewindDTO;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@RequiredArgsConstructor
@Component
public class MiniPGHelper {

    private final MiniPGLocalSettings miniPGlocalSetings;
    private  final  InstructionUtil     instructionUtil;
    private final LocalSqlExecutor localSqlExecutor;
    private final InstructionFacate instructionFacate;
    
    @Value("${minipg.pgconf_file_fullpath:/etc/postgresql/16/main/postgresql.conf}")
    private String pgconf_file_fullpath;

    private final String osDistro = System.getProperties().getProperty("java.vm.vendor", "unknown");

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
                miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl", "start",
                                "-D" + miniPGlocalSetings.getPostgresDataPath());
        }

        List<String> wal_log_result = (new CommandExecutor()).executeCommandSync(
            miniPGlocalSetings.getPgCtlBinPath()+"psql", "-t", "-A", "--no-align", "-c", "show wal_log_hints");
        
        if ((wal_log_result.get(0).toString()).equals("off")){
            log.warn("wal_log_hints is : " + wal_log_result.get(0).toString() + " Please set to ON.");
            // List<String> hot_stdby_alter_result = (new CommandExecutor()).executeCommandSync(
            //     miniPGlocalSetings.getPgCtlBinPath()+"psql", "-c", "alter system set wal_log_hints to on");
            // if (hot_stdby_alter_result.toString().indexOf("ALTER SYSTEM")>-1){
            //     log.info("wal_log_hints set to on.");
            // }
        }        

        List<String> arcmode_result = (new CommandExecutor()).executeCommandSync(
            miniPGlocalSetings.getPgCtlBinPath()+"psql", "-t", "-A", "--no-align", "-c", "show archive_mode");
        
            if ((arcmode_result.get(0).toString()).equals("off")){
                log.warn("archive_mode is : " + arcmode_result.get(0).toString() + " Please set to ON.");
                // List<String> arcmode_alter_result = (new CommandExecutor()).executeCommandSync(
                //     miniPGlocalSetings.getPgCtlBinPath()+"psql", "-c", "alter system set archive_mode to on");
                // if (arcmode_alter_result.toString().indexOf("ALTER SYSTEM")>-1){
                //     log.info("archive_mode set to on.");
                //     dbRestartRequire = Boolean.TRUE;
                // }
            }

        List<String> arcCommand_result = (new CommandExecutor()).executeCommandSync(
            miniPGlocalSetings.getPgCtlBinPath()+"psql", "-t", "-A", "--no-align", "-c", "show archive_command");
        
            if ((arcCommand_result.get(0).toString()).equals("(disabled)") || (arcCommand_result.get(0).toString()).equals("/bin/true")){
                log.warn("archive_command is : " + arcCommand_result.get(0).toString() + " Please set to valid directory properly.");
                // (new CommandExecutor()).executeCommandSync(
                //     "mkdir",
                //             "-p",
                //             miniPGlocalSetings.getPostgresDataPath()+"pg_archived_wal");
                
                // List<String> arcCommand_alter_result = (new CommandExecutor()).executeCommandSync(
                //     miniPGlocalSetings.getPgCtlBinPath()+"psql", "-c", "alter system set archive_command to 'cp %p "+miniPGlocalSetings.getPostgresDataPath()+"pg_archived_wal/%f'");
                // if (arcCommand_alter_result.toString().indexOf("ALTER SYSTEM")>-1){
                //     log.info("archive_command set to 'cp %p "+ miniPGlocalSetings.getPostgresDataPath()+"pg_archived_wal/%f"+"'");
                //     dbRestartRequire = Boolean.TRUE;
                // }
            }

        List<String> restCommand_result = (new CommandExecutor()).executeCommandSync(
            miniPGlocalSetings.getPgCtlBinPath()+"psql", "-t", "-A", "--no-align", "-c", "show restore_command");
        
            if ((restCommand_result.get(0).toString()).equals("")){
                log.warn("restore_command is : " + restCommand_result.get(0).toString() + " Please set to valid directory properly.");
                
                // List<String> arcCommand_alter_result = (new CommandExecutor()).executeCommandSync(
                //     miniPGlocalSetings.getPgCtlBinPath()+"psql", "-c", "alter system set restore_command to '"+miniPGlocalSetings.getRestoreCommand()+"'");
                // if (arcCommand_alter_result.toString().indexOf("ALTER SYSTEM")>-1){
                //     log.info("restore_command set to '"+ miniPGlocalSetings.getRestoreCommand() +"'");
                //     dbRestartRequire = Boolean.TRUE;
                // }
            }
            
        log.warn("Please set max_wal_size as large as possible.");
        log.warn("Please set min_wal_size as large as possible.");
        log.warn("Please set wal_keep_size as large as possible.");
            
        // if (dbRestartRequire == Boolean.TRUE){

        //     // (new CommandExecutor()).executeCommandSync(
        //     //     miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl", "restart","-w",
        //     //     "-D" + miniPGlocalSetings.getPostgresDataPath());
        //     log.warn("DB Parameters Changed. Please Restart DB!..");
        //     // log.warn("Until DB Restart MiniPg will not run properly.Therefore MiniPg shutting down..");
        //     // System.exit(0);
        // }
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
                    "-D" + miniPGlocalSetings.getPostgresDataPath());

        if ((result_stop.toString()).contains("error") || (result_stop.toString()).contains("fatal")){
            log.info(" Error occurrred on STOP PG, error:"+result_stop.toString());
            return result_stop.toString();
        }

        return "OK";
    }

    public String postSwitchOver(PromoteDTO promoteDTO){

        List<String> start_result = (new CommandExecutor()).executeCommandSync(
                        miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl", "start", "-w",
                        "-D" + miniPGlocalSetings.getPostgresDataPath());

        if ((start_result.toString()).contains("error") || (start_result.toString()).contains("fatal")){
            log.info(" Error occurrred on START PG, error:"+start_result.toString());
            return start_result.toString();
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
                "-D", 
                miniPGlocalSetings.getPostgresDataPath());

        (new LocalSqlExecutor()).executeLocalSql("CHECKPOINT", promoteDTO.getPort(), promoteDTO.getUser(),
               promoteDTO.getPassword());

        (new LocalSqlExecutor()).executeLocalSql("alter system set synchronous_standby_names to ''", promoteDTO.getPort(), promoteDTO.getUser(),promoteDTO.getPassword());

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
                if (osDistro.equals("Ubuntu")){
                    newDataDirFullPath = miniPGlocalSetings.getPostgresDataPath().replaceAll("\\b"+"main"+"\\b", "main_"+formattedDate);
                }

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
                    
                    List<String> result_script = (new CommandExecutor()).executeCommandSync(
                    "/bin/bash", filename);

                    if ((result_script.toString()).contains("No space left on device")){
                        (new CommandExecutor()).executeCommandSync("/bin/bash",
                                                                            "-c",
                                                                            "\"rm -rf "+
                                                                            (miniPGlocalSetings.getPostgresDataPath().endsWith("/") == Boolean.TRUE ? 
                                                                                        miniPGlocalSetings.getPostgresDataPath().substring(0, miniPGlocalSetings.getPostgresDataPath().length() - 1) +"_*" : 
                                                                                        miniPGlocalSetings.getPostgresDataPath() +"_*")
                                                                                        + "\"");
                    }else if((result_script.toString()).contains("no pg_hba.conf entry")){
                        return "pg_basebackup FAILED. Possible Reason :" + result_script.toString();
                    }

                    // 2.3 start the server
                    log.info(String.valueOf(logNumber++)+". step : start server");
                    if (osDistro.equals("Ubuntu")){
                        List<String> cellValues = (new CommandExecutor()).executeCommandSync(
                            miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl", "start", "-w",
                            "-D", miniPGlocalSetings.getPostgresDataPath() ,
                            "-o" , 
                            "\"--config-file="+pgconf_file_fullpath+"\"");
        
                        if (!(cellValues.contains("server started"))){
                            return null;
                        }
                        // if (cellValues.contains("server did not start in time")){
                        // }
                    } else {
                        List<String> cellValues = (new CommandExecutor()).executeCommandSync(
                            miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl", "start", "-w",
                            "-D" , miniPGlocalSetings.getPostgresDataPath());
                        
                        if (!(cellValues.contains("server started"))){
                                return null;
                            }    
                    }        

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

        File f = new File(miniPGlocalSetings.getPostgresDataPath());
        if (!f.exists() && !f.isDirectory()) {
            (new ScriptExecutor()).executeScript(
                    "mkdir",
                    "-p",
                    miniPGlocalSetings.getPostgresDataPath());
            log.info("Data directory " + miniPGlocalSetings.getPostgresDataPath() + " created.");
        }

        if (localVersion == PgVersion.V11X || localVersion == PgVersion.V10X) {

            log.info(String.valueOf(logNumber++)+". create rewind script");
            String filename = "/tmp/rewind.sh";
            instructionFacate.createRewindScript(filename, rewindDTO.getMasterIp(), rewindDTO.getUser(), rewindDTO.getPassword(), rewindDTO.getPort());
            log.info("2. execute rewind script");
            (new CommandExecutor()).executeCommandSync(
                    "/bin/bash", filename);

            log.info(String.valueOf(logNumber++)+". step : checkpoint");
            instructionFacate.checkPoint(rewindDTO.getPort(), rewindDTO.getUser(),rewindDTO.getPassword());

            log.info("RESULT  : rewind ended successfully");
        }

        if (localVersion == PgVersion.V12X || localVersion == PgVersion.V13X || localVersion == PgVersion.V14X) {

            log.info(String.valueOf(logNumber++)+". step : stop pg");
            (new ScriptExecutor()).executeScript(
                    miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl",
                    "stop",
                    "-D" + miniPGlocalSetings.getPostgresDataPath());

            log.info(String.valueOf(logNumber++)+". step : do rewind");
            Boolean revindSuccess = instructionFacate.tryRewindSync(rewindDTO.getMasterIp(),rewindDTO.getPort(), rewindDTO.getUser(), rewindDTO.getPassword());
            
            // TODO: check here for syntax and logical error cases
            if (revindSuccess == null) {
                return " minipg rewind failed.";
            }
            
            log.info(String.valueOf(logNumber++)+". step : Appending Restore Command");
            instructionFacate.tryAppendRestoreCommandToAutoConfFile();
            instructionFacate.tryToAppendConnInfoToAutoConfFile(rewindDTO.getMasterIp(), rewindDTO.getPort(), repUser);
            instructionFacate.tryAppendLineToAutoConfFile("recovery_target_timeline = 'latest'");

            log.info(String.valueOf(logNumber++)+". step : create standby.signal");
            instructionFacate.tryTouchingStandby();

            log.info(String.valueOf(logNumber++)+". step : create recovery.signal");
            instructionFacate.tryTouchingRecovery();

            log.info(String.valueOf(logNumber++)+". step : start server");
            if (osDistro.equals("Ubuntu")){
                List<String> cellValues = (new CommandExecutor()).executeCommandSync(
                    miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl", "start", "-w",
                    "-D", miniPGlocalSetings.getPostgresDataPath() ,
                    "-o" , 
                    "\"--config-file="+pgconf_file_fullpath+"\"");
                // log.info("Server Start Result:"+ String.join("\n", cellValues));
                    // if (!(cellValues.contains("server started"))){
                //     return null;
                // }
            } else {
                List<String> cellValues = (new CommandExecutor()).executeCommandSync(
                    miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl", "start",
                    "-D" , miniPGlocalSetings.getPostgresDataPath());

                cellValues = (new CommandExecutor()).executeCommandSync(
                    miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl", "stop", 
                    "-D" , miniPGlocalSetings.getPostgresDataPath());

                cellValues = (new CommandExecutor()).executeCommandSync(
                    miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl", "start", 
                    "-D" , miniPGlocalSetings.getPostgresDataPath());

                // log.info("Server Start Result:"+ String.join("\n", cellValues));


                // if (!(cellValues.contains("server started"))){
                //         return null;
                //     }    
            }        
            
            // if (!instructionFacate.tryStartSyncForRecovery(rewindDTO.getPort(), rewindDTO.getUser(), rewindDTO.getPassword()))
            //     return null;

            // log.info(String.valueOf(logNumber++)+". step : checkpoint");
            // instructionFacate.checkPoint(rewindDTO.getPort(), rewindDTO.getUser(), rewindDTO.getPassword());

            // log.info(String.valueOf(logNumber++)+". step : execute local restore and primary conn info");
            // instructionFacate.executeLocalRestoreCommand(rewindDTO.getPort(), rewindDTO.getUser(), rewindDTO.getPassword());
            // instructionFacate.executeLocalPrimaryConnInfo(rewindDTO.getPort(), rewindDTO.getUser(), rewindDTO.getPassword(),
            //         rewindDTO.getMasterIp()
            // );
            // instructionFacate.executeLocalTargetTimeLineLatest(
            //         rewindDTO.getPort(),
            //         rewindDTO.getUser(),
            //         rewindDTO.getPassword());

            // 12. reload configuration
            // log.info(String.valueOf(logNumber++)+". step reload pg configuration");
            // List<String> result_reload = (new CommandExecutor()).executeCommandSync(
            //     miniPGlocalSetings.getPgCtlBinPath() + "psql", "-c","SELECT pg_reload_conf();");
            
            // log.info("pg_reload result : "+ result_reload );
            // if (!(result_reload.contains("t"))){
            //     return null;
            // }

        }
        Boolean isReplicationUp  = instructionFacate.checkReplication(rewindDTO.getMasterIp(),rewindDTO.getPort(), rewindDTO.getUser(), rewindDTO.getPassword());
        if ( isReplicationUp == Boolean.FALSE){
            return null;
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
            instructionFacate.tryStartSync();

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

    public String setApplicationName(String strAppName){       
        List<String> connstr = (new CommandExecutor()).executeCommandSync(
            miniPGlocalSetings.getPgCtlBinPath() + "psql", "-t", "-A", "-c", "show primary_conninfo");
        String strConnInfo = connstr.get(0);

        if (!strConnInfo.contains(strAppName)){
            strConnInfo =  strConnInfo + " application_name=" + strAppName;
            strConnInfo = strConnInfo.replace("'","''");
            List<String> result = (new CommandExecutor()).executeCommandSync(
                miniPGlocalSetings.getPgCtlBinPath() + "psql", "-c", "ALTER SYSTEM SET primary_conninfo='"+strConnInfo+"'");
            result.addAll((new CommandExecutor()).executeCommandSync(
                miniPGlocalSetings.getPgCtlBinPath() + "psql", "-c", "SELECT pg_reload_conf()")); 
    
            for (String cell : result) {
                if (cell.contains("no such file")) {
                    return null;
                } else if (cell.contains("error") || cell.contains("ERROR") 
                            || cell.contains("fatal") || cell.contains("FATAL")){
                    return null;
                }
            }
        }
        
        return "OK";
    }  

    public String fixApplicationName(){
        String hostname = "";
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        if (hostname == " " || hostname == null){
            String[] cmd = {"hostname"};
            try {
                hostname = new BufferedReader(
                    new InputStreamReader(Runtime.getRuntime().exec(cmd).getInputStream()))
                   .readLine();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        List<String> connstr = (new CommandExecutor()).executeCommandSync(
            miniPGlocalSetings.getPgCtlBinPath() + "psql", "-t", "-A", "-c", "show primary_conninfo");
        String strConnInfo = connstr.get(0);

        if (!strConnInfo.contains(hostname)){
            strConnInfo =  strConnInfo + " application_name=" + hostname;
            strConnInfo = strConnInfo.replace("'","''");
            List<String> result = (new CommandExecutor()).executeCommandSync(
                miniPGlocalSetings.getPgCtlBinPath() + "psql", "-c", "ALTER SYSTEM SET primary_conninfo='"+strConnInfo+"'");
            result.addAll((new CommandExecutor()).executeCommandSync(
                miniPGlocalSetings.getPgCtlBinPath() + "psql", "-c", "SELECT pg_reload_conf()")); 
    
            for (String cell : result) {
                if (cell.contains("no such file")) {
                    return null;
                } else if (cell.contains("error") || cell.contains("ERROR") 
                            || cell.contains("fatal") || cell.contains("FATAL")){
                    return null;
                }
            }
        }
        
        return "OK";
    } 

    public String setRepToSync(String strAppName){       
        List<String> currvalue = (new CommandExecutor()).executeCommandSync(
            miniPGlocalSetings.getPgCtlBinPath() + "psql", "-t", "-A", "-c", "show synchronous_standby_names");
        String curr_value = currvalue.get(0).toString();
        
        if ((curr_value.indexOf("FIRST") > -1 ) || (curr_value.indexOf("ANY") > -1 )){
            curr_value = curr_value.substring(curr_value.indexOf("(")+1, curr_value.indexOf(")"));
        }
        List<String> list_new_appName = new ArrayList<String>();
        if (curr_value.indexOf(",")> -1){
            for (String appName : curr_value.split(",")){
                List<String> appname_result = (new CommandExecutor()).executeCommandSync(
                    miniPGlocalSetings.getPgCtlBinPath() + "psql", "-t", "-A", "-c", 
                    "select count(*) from pg_stat_replication where application_name='"+appName+"';");
                if (Integer.parseInt(appname_result.get(0).toString()) > 0){
                    list_new_appName.add(appName);
                };
            }
        } else {
            if (curr_value.length() > 1){
                List<String> appname_result = (new CommandExecutor()).executeCommandSync(
                    miniPGlocalSetings.getPgCtlBinPath() + "psql", "-t", "-A", "-c", 
                    "select count(*) from pg_stat_replication where application_name='"+curr_value+"';");
                if (Integer.parseInt(appname_result.get(0).toString()) > 0){
                    list_new_appName.add(curr_value);
                };
            }
        }        

        list_new_appName.add(strAppName);

        String sqlPart = "FIRST "+ list_new_appName.size() + " (";
        if (list_new_appName.size() > 1){
            sqlPart += sqlPart.join(",", list_new_appName);
        } else {
            sqlPart += list_new_appName.get(0);
        }

        sqlPart += ")"; 

        List<String> result = (new CommandExecutor()).executeCommandSync(
            miniPGlocalSetings.getPgCtlBinPath() + "psql", "-c", "ALTER SYSTEM SET synchronous_standby_names to '"+sqlPart+"'");
        
        result.addAll((new CommandExecutor()).executeCommandSync(
            miniPGlocalSetings.getPgCtlBinPath() + "psql", "-c", "SELECT pg_reload_conf()"));       

        for (String cell : result) {
            if (cell.contains("no such file")) {
                return null;
            } else if (cell.contains("error") || cell.contains("ERROR") 
                        || cell.contains("fatal") || cell.contains("FATAL")){
                return null;
            }
        }      
        return "OK";
    }
    
    public String setRepToAsync(String strAppName){
        
        List<String> currvalue = (new CommandExecutor()).executeCommandSync(
            miniPGlocalSetings.getPgCtlBinPath() + "psql", "-t", "-A", "-c", "show synchronous_standby_names");
        String curr_value = currvalue.get(0).toString();

        if ((curr_value.indexOf("FIRST") > -1 ) || (curr_value.indexOf("ANY") > -1 )){
            curr_value = curr_value.substring(curr_value.indexOf("(")+1, curr_value.indexOf(")"));
        }

        List<String> list_new_appName = new ArrayList<String>();
        if (curr_value.indexOf(",")> -1){
            for (String appName : curr_value.split(",")){
                List<String> appname_result = (new CommandExecutor()).executeCommandSync(
                    miniPGlocalSetings.getPgCtlBinPath() + "psql", "-t", "-A", "-c", 
                    "select count(*) from pg_stat_replication where application_name='"+appName+"';");
                if (Integer.parseInt(appname_result.get(0).toString()) > 0){
                    list_new_appName.add(appName);
                };
            }
        } else {
            if (curr_value.length() > 1){
                List<String> appname_result = (new CommandExecutor()).executeCommandSync(
                    miniPGlocalSetings.getPgCtlBinPath() + "psql", "-t", "-A", "-c", 
                    "select count(*) from pg_stat_replication where application_name='"+curr_value+"';");
                if (Integer.parseInt(appname_result.get(0).toString()) > 0){
                    list_new_appName.add(curr_value);
                };
            }
        }        

        list_new_appName.remove(strAppName);
        String sqlPart = "";
        if (list_new_appName.size() > 0){        
            sqlPart = "FIRST "+ list_new_appName.size() + " (";
            if (list_new_appName.size() > 1){
                sqlPart += sqlPart.join(",", list_new_appName);
            } else {
                sqlPart += list_new_appName.get(0);
            }

            sqlPart += ")"; 
        }        

        List<String> result = (new CommandExecutor()).executeCommandSync(
            miniPGlocalSetings.getPgCtlBinPath() + "psql", "-c", "ALTER SYSTEM SET synchronous_standby_names to '"+sqlPart+"'");
        result.addAll((new CommandExecutor()).executeCommandSync(
            miniPGlocalSetings.getPgCtlBinPath() + "psql", "-c", "SELECT pg_reload_conf()"));   

        for (String cell : result) {
            if (cell.contains("no such file")) {
                return null;
            } else if (cell.contains("error") || cell.contains("ERROR") 
                        || cell.contains("fatal") || cell.contains("FATAL")){
                return null;
            }
        }
        return "OK";
    }  
}
