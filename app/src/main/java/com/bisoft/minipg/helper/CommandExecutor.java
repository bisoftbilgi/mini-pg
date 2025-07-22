package com.bisoft.minipg.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CommandExecutor {

    public List<String> executeCommand(String... args) throws Exception{
        log.trace("async command executing : " + String.join(" ", args));
        List<String> cellValues = new ArrayList<>();
        Runtime.getRuntime().exec(args);
        return cellValues;
    }

    public List<String> executeCommandStr(String command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
        processBuilder.inheritIO();
        Process process = processBuilder.start();

        // Read output into a List<String>
        List<String> outputLines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                outputLines.add(line); // Add each line to the list
            }
        }
        int exitCode = process.waitFor();
        
        if (exitCode != 0) {
            throw new RuntimeException("Command execute error: " + command);
        }
        return outputLines;
    }
    public List<String> executeCommandSync(String... args) {

        // log.info("EXECUTING THIS:" + String.join(" ", args));
        //log.trace("sync command executing : ", String.join(" ", args));
        Process p;
        List<String> cellValues = new ArrayList<>();

        try {
            p = Runtime.getRuntime().exec(args);
            //log.info("waiting for .... " + String.join(" ", args) + " to execute......");

            int resultNum = p.waitFor();
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String line = null;

            if (resultNum == 0) {
                while (true) {
                    line = in.readLine();
                    if (line == null)
                        break;
                    cellValues.add(line);
                    //log.info(line);
                }

                // For better seeing the result
                //log.trace("COMMAND OUTPUT:" + String.join("\n", cellValues));
            } else {
                // in case of error
                while (true) {
                    line = err.readLine();
                    if (line == null)
                        break;
                    cellValues.add(line);
                }

                // for better seeing the errors
                //log.error("COMMAND ERRORS:" + String.join("\n", cellValues));
            }

            String[] errorOutput = IOUtils.toString(p.getErrorStream()).split("\n");

            for(String errorLine : errorOutput){
                if (! errorLine.trim().equals("")){
                    log.error(errorLine);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cellValues;
    }
}
