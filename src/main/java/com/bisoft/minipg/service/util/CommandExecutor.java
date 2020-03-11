package com.bisoft.minipg.service.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CommandExecutor {

    public List<String> executeCommand(String... args) {

        Process p;
        log.info("EXECUTING:", String.join(" ", args));
        System.out.println("EXECUTING:" + String.join(" ", args));
        List<String> cellValues = new ArrayList<>();
        try {
            p = Runtime.getRuntime().exec(args);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(p.getInputStream()));
            String line = null;

            while ((line = in.readLine()) != null) {
                log.debug(line);
                cellValues.add(line);
                System.out.println(line);
            }

            System.out.println("COMMAND OUTPUT:" + String.join("\n", cellValues));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return cellValues;
    }

    //TODO: give a try to processBuilder...
    public List<String> executeCommandAlter(String... args) {

        ProcessBuilder processBuilder = new ProcessBuilder();

        List<String> command = new ArrayList<>();
        command.add("bash");
        command.add("-c");
        Arrays.stream(args).forEach(i -> command.add(i));

        // Run a shell command
        processBuilder.command(command);

        System.out.println("executing:" + String.join(" ", command));
//
//        for (String string : args) {
//            log.info("executing:" + string);
//        }
        List<String> cellValues = new ArrayList<>();
        try {
            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                cellValues.add(line);
            }

            int exitVal = process.waitFor();
            if (exitVal == 0) {
                System.out.println("Success!");
                System.out.println(cellValues);
                System.exit(0);

            } else {
                System.out.println("unsuccessfull:" + cellValues);
            }

        } catch (Exception e) {
            e.printStackTrace();
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
}