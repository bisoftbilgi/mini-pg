package com.bisoft.minipg.helper;

import com.bisoft.minipg.PgVersion;
import com.bisoft.minipg.dto.PromoteDTO;
import com.bisoft.minipg.dto.RewindDTO;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.PropertiesConfigurationLayout;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Data
@Slf4j
@RequiredArgsConstructor
@Component
public class MiniPGHelper {

    private final MiniPGLocalSettings miniPGlocalSetings;
    private  final  InstructionUtil     instructionUtil;
    private final LocalSqlExecutor localSqlExecutor;
    private final InstructionFacate instructionFacate;

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
            instructionFacate.tryAppendLineToAutoConfFile("restore_command = ' '");
            instructionFacate.tryAppendLineToAutoConfFile("recovery_target_timeline = 'current'");

            // 4 .start the server
            log.info(String.valueOf(logNumber++)+". step : start server");
            if (!instructionFacate.tryStartSync())
                return null;
            // 5. stop the server...
            log.info(String.valueOf(logNumber++)+". step : stop server");
            (new CommandExecutor()).executeCommandSync(
                    miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl", "stop",
                    "-D" + miniPGlocalSetings.getPostgresDataPath());

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

    private boolean startContinues() {

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
