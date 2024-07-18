package com.bisoft.minipg.helper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class InstructionFacate {

    @Autowired
    protected     MiniPGLocalSettings miniPGlocalSetings;
    private final ScriptExecutor      scriptExecutor;
    private final CommandExecutor     commandExecutor;
    private final LocalSqlExecutor    localSqlExecutor;
    private final InstructionUtil     instructionUtil;
    private Logger logger = LoggerFactory.getLogger(InstructionFacate.class);


    public List<String> pgCtlStop() {

        List<String> cellValues = scriptExecutor.executeScript(
                miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl",
                "stop",
                "-D" + miniPGlocalSetings.getPostgresDataPath());

        return cellValues;
    }

    public List<String> pgCtlStart() {

        List<String> cellValues = commandExecutor.executeCommandSync(
                miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl", "start",
                "-D" + miniPGlocalSetings.getPostgresDataPath());

        return cellValues;
    }

    public void checkPoint(String portNumber, String userName, String pword) {
        log.warn("checkpoint is running");
        localSqlExecutor.executeLocalSql(
                "CHECKPOINT",
                portNumber,
                userName,
                pword
        );
    }

    public void reloadConf(final String portNumber, final String userName, final String pword) {

        localSqlExecutor.executeLocalSql(
                "SELECT pg_reload_conf()",
                portNumber,
                userName,
                pword
        );

    }


    public boolean tryTouchingStandby() {

        final String touchingFileName = "standby.signal";

        return tryTouching(touchingFileName);
    }

    public boolean tryTouchingRecovery() {

        final String touchingFileName = "recovery.signal";

        return tryTouching(touchingFileName);
    }

    public boolean tryTouchingRecoveryConf() {

        final String touchingFileName = "recovery.conf";

        return tryTouching(touchingFileName);
    }

    private boolean tryTouching(final String touchingFileName) {
        final String touchPath = miniPGlocalSetings.getPostgresDataPath() + touchingFileName;
        final Path   path      = Paths.get(touchPath);

        log.warn("trying to touch {}", touchPath);

        boolean result = false;
        try {


            if (Files.exists(path)) {
                Files.delete(path);
                Files.createFile(path);
                Files.setLastModifiedTime(path, FileTime.from(Instant.now()));
            } else {
                Files.createFile(path);
            }
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    public boolean tryAppendRestoreCommandToAutoConfFile() {
        final String restoreCommand = miniPGlocalSetings.getRestoreCommand();
        return tryAppendLineToAutoConfFile(restoreCommand);
    }

    public boolean tryAppendRestoreCommandToRecoveryConfFile() {
        final String restoreCommand = miniPGlocalSetings.getRestoreCommand();
        return tryAppendLineToRecoveryConfFile(restoreCommand);
    }

    public void executeLocalTargetTimeLineLatest(final String portNumber, final String userName, final String pword) {


        localSqlExecutor.executeLocalSql(
                " alter system set recovery_target_timeline =  'latest' ",
                portNumber,
                userName,
                pword
        );
    }

    public void executeLocalRestoreCommand(final String portNumber, final String userName, final String pword) {

        final String restoreCommand = "ALTER SYSTEM SET " + miniPGlocalSetings.getRestoreCommand();

        localSqlExecutor.executeLocalSql(
                restoreCommand,
                portNumber,
                userName,
                pword
        );
    }

    public boolean tryAppendLine(final String filePath, final String newLine) {

        boolean result = false;
        try{
            Scanner scanner=new Scanner(Paths.get(filePath));
            while(scanner.hasNextLine()){
                if(newLine.equals(scanner.nextLine().trim())){
                    logger.warn(newLine+" already exists in file "+filePath);
                    break;
                }else{
                    // not found
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }


        try {
            Files.write(Paths.get(filePath), (newLine + "\n").getBytes(), StandardOpenOption.APPEND);
            result = true;

        } catch (IOException e) {
            e.printStackTrace();
        }

        log.warn("===\n trying to append file:{} \n content:{}\n result:{} \n===", filePath, newLine, result);


        return result;
    }

    public boolean tryAppendLineToAutoConfFile(final String line) {

        final String autoConfFilePath = miniPGlocalSetings.getPostgresDataPath() + "postgresql.auto.conf";
        File autoConfFile = new File(autoConfFilePath);
        if (!autoConfFile.isFile()){
            try {
                final Path path = Paths.get(autoConfFilePath);
                Files.createFile(path);   
            } catch (Exception e) {
                e.printStackTrace();
            }            
        }
        return tryAppendLine(autoConfFilePath, line);
    }

    public boolean tryAppendLineToRecoveryConfFile(final String line) {

        final String autoConfFilePath = miniPGlocalSetings.getPostgresDataPath() + "recovery.conf";
        return tryAppendLine(autoConfFilePath, line);
    }

    public boolean tryToAppendConnInfoToAutoConfFile(final String masterIp, final String masterPort, final String user) {

        String recoveryConfTemplate = instructionUtil.getRecoveryConfTemplateV12();
        final String line = recoveryConfTemplate
                .replace("{MASTER_IP}", masterIp)
                .replace("{MASTER_PORT}", masterPort)
                .replace("{USER}", user)
                .replace("{SSL_MODE}",miniPGlocalSetings.getSslMode())
                .replace("{SSL_COMPRESSION}",miniPGlocalSetings.getSslCompression());

        final String autoConfFilePath = miniPGlocalSetings.getPostgresDataPath() + "postgresql.auto.conf";
        return tryAppendLine(autoConfFilePath, line);
    }

    public boolean tryToAppendConnInfoToRecoveryConfFile(final String masterIp, final String masterPort, final String user) {

        String recoveryConfTemplate = instructionUtil.getRecoveryConfTemplateV10();
        final String line = recoveryConfTemplate
                .replace("{MASTER_IP}", masterIp)
                .replace("{MASTER_PORT}", masterPort)
                .replace("{USER}", user)
                .replace("{SSL_MODE}",miniPGlocalSetings.getSslMode())
                .replace("{SSL_COMPRESSION}",miniPGlocalSetings.getSslCompression());

        final String autoConfFilePath = miniPGlocalSetings.getPostgresDataPath() + "recovery.conf";
        return tryAppendLine(autoConfFilePath, line);
    }

    public void executeLocalPrimaryConnInfo(final String portNumber, final String userName,
                                            final String pword, final String masterIp) {
        String replicationUser = miniPGlocalSetings.getReplicationUser();
        //if user is not set use admin superuser
        if(replicationUser==null || replicationUser.equals("")){
            replicationUser = userName;
        }
        String recoveryConfTemplate = instructionUtil.getRecoveryConfTemplateV12();
        final String line = " ALTER SYSTEM SET " +
                recoveryConfTemplate
                        .replace("{MASTER_IP}", masterIp)
                        .replace("{MASTER_PORT}", portNumber)
                        .replace("{USER}", replicationUser)
                        .replace("{SSL_MODE}",miniPGlocalSetings.getSslMode())
                        .replace("{SSL_COMPRESSION}",miniPGlocalSetings.getSslCompression());
        ;
        localSqlExecutor.executeLocalSql(
                line,
                portNumber,
                userName,
                pword
        );
    }
//
//    private String getRecoveryConfTemplate() {
//        return instructionUtil.getRecoveryConfTemplateV12();
//
////        if (miniPGlocalSetings.getOs().toLowerCase().startsWith("windows")) {
////
////            return instructionUtil.getRecoveryConfTemplateForWindows();
////        } else {
////            return instructionUtil.getRecovertConfTemplateForLinux();
////        }
//
//    }


    public Boolean tryRewindSync(final String masterIp, final String port, final String user, final String password) {

        final List<String> cellValues;
        cellValues = (new ScriptExecutor()).executeScriptSync(
                "bash", "-c",
                miniPGlocalSetings.getPgCtlBinPath() + "pg_rewind --target-pgdata="
                        + miniPGlocalSetings.getPostgresDataPath()
                        + " --source-server='host="
                        + masterIp
                        + " port=" + port
                        + " user=" + user
                        + " dbname=postgres"
                        + " password=" + password
                        + "'");

        // wait for  pg_rewind to be finishes
        while (rewindContinues()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // open
        boolean result = true;
        for (String cell : cellValues) {
            if (cell.contains("no such file")) {
                result = false;
                return null;
//                break;
            } else if (cell.contains("error") || cell.contains("ERROR") || cell.contains("fatal") || cell.contains("FATAL")){
                result = false;
                return null;
            }
        }

        return result;
    }

    private boolean rewindContinues() {

        List<ProcessHandle> result = ProcessHandle.allProcesses().filter(p -> p.info().toString().contains("pg_rewind"))
                .collect(Collectors.toList());
        return (result.size() > 0);

    }

    public boolean tryStartSync() {
        boolean result = false;
        List<String> interResult =
                (new CommandExecutor()).executeCommandSync(
                        miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl", "start",
                        "-D" + miniPGlocalSetings.getPostgresDataPath());
        boolean timeout = false;
        for (String cell : interResult) {
            log.info("pg_ctl start command result:", cell);
            if (cell.contains("done")) {
                result = true;
                break;
            }
            if (cell.contains("did not start in time")) {
                timeout = true;
                break;
            }
        }

        if (timeout) {
            while (startContinues()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
//                result = true;
        } else if (!result && !timeout)
            return false;

        return result;
    }

    public boolean tryStopSync() {
        boolean result = true;
        List<String> interResult =
                (new CommandExecutor()).executeCommandSync(
                        miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl", "stop",
                        "-D" + miniPGlocalSetings.getPostgresDataPath());

        boolean timeout = true;

        for (String cell : interResult) {
            if (cell.contains("done")) {
                result = true;
                break;
            }
        }

        if (timeout) {
            while (stopContinues()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
//                result = true;
        } else if (!result && !timeout)
            return false;

        return result;
    }

    public boolean executeShellScript(String filename) {
        try {
            logger.info("executing script : "+filename);
            ProcessBuilder pb = new ProcessBuilder("/bin/bash", filename);
            pb.directory(new File(miniPGlocalSetings.getPostgresDataPath()));
            Process p = pb.start();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            int processResult = p.waitFor();
            logger.info("Process Result "+String.valueOf(processResult));
        }catch (IOException | InterruptedException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean createRewindScript(final String filename,final String masterIp,
                                      final String repUser,final String repPassword,final String masterPort){
        /*
        psql -c "checkpoint"
         */
        try {
            if (Paths.get(filename).toFile().isFile()){
                Files.delete(Paths.get(filename));
            }            
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            final String pgData = miniPGlocalSetings.getPostgresDataPath();
            final String pgCtlBinPath = miniPGlocalSetings.getPgCtlBinPath();
            final String sslMode = miniPGlocalSetings.getSslMode();
            final String pgPassFile = miniPGlocalSetings.getPgPassFile();

            FileOutputStream fos = new FileOutputStream(filename, false);
            fos.write("{PG_BIN_PATH}/pg_ctl stop -D{PG_DATA}\n"
                    .replace("{PG_DATA}",pgData)
                    .replace("{PG_BIN_PATH}",pgCtlBinPath).getBytes());

            fos.write("{PG_BIN_PATH}/pg_ctl start -D{PG_DATA}\n"
                    .replace("{PG_DATA}",pgData)
                    .replace("{PG_BIN_PATH}",pgCtlBinPath).getBytes());
            fos.write("sleep 5\n".getBytes());

            fos.write("{PG_BIN_PATH}/pg_ctl stop -D{PG_DATA}\n"
                    .replace("{PG_DATA}",pgData)
                    .replace("{PG_BIN_PATH}",pgCtlBinPath).getBytes());

            fos.write("sleep 5\n".getBytes());

            fos.write("bash -c \"{PG_BIN_PATH}/pg_rewind --target-pgdata={PG_DATA} --source-server='host={MASTER_IP} port={MASTER_PORT} user={REPLICATION_USER} dbname=postgres password={REPLICATION_PASSWORD}'\"\n"
                    .replace("{PG_DATA}",pgData)
                    .replace("{PG_BIN_PATH}",pgCtlBinPath)
                    .replace("{MASTER_IP}",masterIp)
                    .replace("{MASTER_PORT}",masterPort)
                    .replace("{REPLICATION_USER}",repUser)
                    .replace("{REPLICATION_PASSWORD}",repPassword).getBytes());

            fos.write("touch {PG_DATA}/recovery.signal\n".replace("{PG_DATA}",pgData).getBytes());

            fos.write("touch {PG_DATA}/standby.signal\n".replace("{PG_DATA}",pgData).getBytes());
            /*
            primary_conninfo ='user=bfmuser passfile=''/var/lib/pgsql/.pgpass'' host=10.108.0.3  port=5432 sslmode=prefer sslcompression=0 krbsrvname=postgres target_session_attrs=any application_name=mophhqpsmxdb02'
            standby_mode = 'on'
            recovery_target_timeline='latest

            /var/lib/pgsql/.pgpass

             */
            fos.write("echo \"primary_conninfo ='user={REPLICATION_USER} passfile=''{PG_PASS_FILE}'' host={MASTER_IP}  port={MASTER_PORT} sslmode={SSL_MODE} sslcompression={SSL_COMPRESSION} krbsrvname=postgres application_name={APPLICATION_NAME}'\" > {PG_DATA}/recovery.conf\n"
                    .replace("{REPLICATION_USER}",repUser)
                    .replace("{PG_PASS_FILE",pgPassFile)
                    .getBytes());
            fos.write("cp /etc/bfm/minipg/recovery.conf {PG_DATA}/recovery.conf\n"
                    .replace("{PG_DATA}",pgData)
                    .getBytes());

            fos.write("{PG_BIN_PATH}/pg_ctl start -D{PG_DATA}\n"
                    .replace("{PG_DATA}",pgData)
                    .replace("{PG_BIN_PATH}",pgCtlBinPath).getBytes());

            fos.write("{PG_BIN_PATH}/pg_ctl stop -D{PG_DATA}\n"
                    .replace("{PG_DATA}",pgData)
                    .replace("{PG_BIN_PATH}",pgCtlBinPath).getBytes());

            fos.write("{PG_BIN_PATH}/pg_ctl start -D{PG_DATA}\n"
                    .replace("{PG_DATA}",pgData)
                    .replace("{PG_BIN_PATH}",pgCtlBinPath).getBytes());


            fos.flush();
            fos.close();
        } catch (IOException e) {
            return false;
        }

        return true;

    }

    public boolean createRebaseScript(final String filename,final String masterIp,
                                      final String repUser,final String repPassword,final String masterPort){
        try {
            if ((Paths.get(filename)).toFile().isFile()){
                Files.delete(Paths.get(filename));
            }            
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            final String pgData = miniPGlocalSetings.getPostgresDataPath();
            final String pgCtlBinPath = miniPGlocalSetings.getPgCtlBinPath();
            final String pgPassFile = miniPGlocalSetings.getPgPassFile();

            FileOutputStream fos = new FileOutputStream(filename, false);
            // fos.write("{PG_BIN_PATH}/pg_ctl stop -D{PG_DATA}\n"
            //         .replace("{PG_DATA}",pgData)
            //         .replace("{PG_BIN_PATH}",pgCtlBinPath).getBytes());

            // fos.write("sleep 5\n".getBytes());

            // LocalDateTime ldateTime = LocalDateTime.now();
            // DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");

            // String formattedDate = ldateTime.format(dateFormatter);
            // String newPgData = pgData.replaceAll("\\b"+"data"+"\\b", "data_"+formattedDate);

            // fos.write("mv {PG_DATA} {NEW_PG_DATA}\n".replace("{PG_DATA}",pgData).replace("{NEW_PG_DATA}",newPgData).getBytes());

            fos.write("export PGPASSWORD={REPLICATION_PASSWORD}\n".replace("{REPLICATION_PASSWORD}",repPassword).getBytes());

            fos.write("bash -c \"{PG_BIN_PATH}/pg_basebackup -h {MASTER_IP} -p {MASTER_PORT} -U {REPLICATION_USER} -Fp -Xs -R -D {PG_DATA}\"\n"
                    .replace("{PG_DATA}",pgData)
                    .replace("{PG_BIN_PATH}",pgCtlBinPath)
                    .replace("{MASTER_IP}",masterIp)
                    .replace("{MASTER_PORT}",masterPort)
                    .replace("{REPLICATION_USER}",repUser).getBytes());

            // fos.write("{PG_BIN_PATH}/pg_ctl start -D{PG_DATA}\n"
            //         .replace("{PG_DATA}",pgData)
            //         .replace("{PG_BIN_PATH}",pgCtlBinPath).getBytes());

            fos.flush();
            fos.close();
        } catch (IOException e) {
            return false;
        }

        return true;

    }

    public boolean tryStartSyncForRecovery(final String portNumber, final String userName,
                                           final String pword) {
        boolean result = false;
        List<String> interResult =
                (new CommandExecutor()).executeCommandSync(
                        miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl", "start",
                        "-D" + miniPGlocalSetings.getPostgresDataPath());
        boolean timeout = false;
        for (String cell : interResult) {
            if (cell.contains("done")) {
                result = true;
                break;
            }
            if (cell.contains("did not start in time")) {
                timeout = true;
                break;
            }
        }

        if (timeout) {
            while (recoveryContinue(portNumber, userName,
                    pword)) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // if you can select 1;
            result = true;
        } else if (!result && !timeout)
            result = false;

        return result;
    }

    private boolean recoveryContinue(final String portNumber, final String userName,
                                     final String pword) {

        List<ProcessHandle> result = ProcessHandle.allProcesses().filter(p -> p.info().toString().contains("pg_ctl"))
                .collect(Collectors.toList());


        log.warn(" recovery continues.........", result.size());

        log.warn("----> I have found {} of pg_ctl process right now...", result.size());
        if (result.size() > 0)
            return false;

        try {
            localSqlExecutor.tryExecuteLocalSql("SELECT 1 ", portNumber, userName, pword);
            return false;
        } catch (Exception e) {
            if (e.getMessage().contains("FATAL: the database system is starting up")) {
                return true;
            }
            e.printStackTrace();
            return false;
        }
    }

    private boolean startContinues() {
        List<ProcessHandle> result = ProcessHandle.allProcesses().filter(p -> p.info().toString().contains("startup"))
                .collect(Collectors.toList());

        return (result.size() > 0);

    }

    private boolean stopContinues() {
        List<ProcessHandle> result = ProcessHandle.allProcesses().filter(p -> p.info().toString().contains("postgres"))
                .filter(p -> p.info().toString().contains(miniPGlocalSetings.getPostgresDataPath()))
                .collect(Collectors.toList());

        return (result.size() > 0);

    }

}
