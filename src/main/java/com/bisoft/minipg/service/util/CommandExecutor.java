package com.bisoft.minipg.service.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CommandExecutor {

    public List<String> executeCommandByRuntime(String... args) {

        for (String string : args) {
            log.info("executing:" + string);
        }
        List<String> cellValues = new ArrayList<>();
        try {
            Runtime.getRuntime().exec(args);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return cellValues;
    }


    //TODO: give a try to processBuilder...
    public List<String> executeCommand(String... args) {

        ProcessBuilder processBuilder = new ProcessBuilder();

        // Run a shell command
        processBuilder.command(String.join(" ", args));

        System.out.println("executing:"+ String.join(" ", args));
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
                System.out.println("ERROR:");
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