package com.bisoft.minipg.service.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CommandExecutor {


    public List<String> executeCommandEx1(String... args) {

        System.out.println("EXECUTING THIS:" + String.join(" ", args));
        log.info("executing:" + String.join(" ", args));
        Process      p;
        List<String> cellValues = new ArrayList<>();
        try {
            p = Runtime.getRuntime().exec(args);
            p.waitFor();
            BufferedReader in = new BufferedReader(
                new InputStreamReader(p.getInputStream()));
            String line = null;

            while ((line = in.readLine()) != null) {
                log.debug(line);
                cellValues.add(line);
            }

            log.info("COMMAND OUTPUT:" + String.join("\n", cellValues));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cellValues;
    }

    //TODO: give a try to processBuilder...
    public List<String> executeCommandPs(String... args) {

        ProcessBuilder processBuilder = new ProcessBuilder();

        List<String> command = new ArrayList<>();
//        command.add("/bin/sh");
//        command.add("-c");
        Arrays.stream(args).forEach(i -> command.add(i));

        // Run a shell command
        processBuilder.command(command);

        System.out.println("executing:" + String.join(" ", command));
        log.info("executing:" + String.join(" ", command));
        List<String> cellValues = new ArrayList<>();
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
//            pb.directory(new File("/usr/local/bin/"));
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            int    exitVal = process.waitFor();
            String line;

            while ((line = reader.readLine()) != null) {
                cellValues.add(line);
            }

            if (exitVal == 0) {
                System.out.println("Success!");
                System.out.println(String.join("\n ", cellValues));
                System.exit(0);
            } else {
                System.out.println("unsuccessfull:" + cellValues);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return cellValues;
    }

    public List<String> executeCommandSync(String... args) {

        for (String string : args) {
            log.info("executing:" + string);
        }
        List<String> cellValues = new ArrayList<>();
        try {
            Process p = Runtime.getRuntime().exec(args);
            log.debug("waiting for .... " + args);
            p.waitFor();
            log.debug(args + " is done");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cellValues;
    }

    public List<String> executeCommand(String... args) {

        ProcessBuilder builder   = new ProcessBuilder();
        Boolean        isWindows = false;


        List<String> command = new ArrayList<>();
//        command.add("/bin/sh");
//        command.add("-c");
        Arrays.stream(args).forEach(i -> command.add(i));


        System.out.println("executing:" + String.join(" ", command));
        log.info("executing:" + String.join(" ", command));


        if (isWindows) {
            builder.command("cmd.exe", "/c", "dir");
        } else {
            builder.command(args);
        }
        builder.directory(new File(System.getProperty("user.home")));
        int exitCode = 1;
        try {
            Process process = builder.start();
            StreamGobbler streamGobbler =
                new StreamGobbler(process.getInputStream(), System.out::println);
            Executors.newSingleThreadExecutor().submit(streamGobbler);
            exitCode = process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert exitCode == 0;
        return null;
    }
}