package com.bisoft.minipg.helper;


import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ScriptExecutor {

    private static final String NEWLINE = "\n";

    public List<String> executeScript(String... args) {
        log.info("sync script executing:" + String.join(" ", args));

        List<String> cellValues = new ArrayList<>();

        try {

            ProcessBuilder builder = new ProcessBuilder(args);

            builder.redirectErrorStream(true);

            Process process = builder.start();
            InputStream is = process.getInputStream();
            InputStream erIs = process.getErrorStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            BufferedReader errReader = new BufferedReader(new InputStreamReader(erIs));

//            int resultNum = process.exitValue();

            String line = null;
//            if (resultNum == 0) {
            // get normal output
            log.info("COMMAND OUTPUT FOR:" + String.join("\n", cellValues));
            while ((line = reader.readLine()) != null) {
                log.trace(line);
                cellValues.add(line);
            }
//            } else {
            // get error output
            log.info("COMMAND ERROR OUTPUT FOR :" + String.join("\n", cellValues));
            while ((line = errReader.readLine()) != null) {
                log.trace(line);
                cellValues.add(line);
            }

            Arrays.stream(IOUtils.toString(process.getErrorStream()).split("\n")).forEach(errorLine -> log.error(errorLine));

//            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return cellValues;
    }

    public List<String> executeScriptSync(String... args) {

        String command = String.join(" ", args)+" ";

        String commadForLog = command.replaceAll(" password=[^\\s]* "," password=******* ");

        log.info("sync script executing:" + commadForLog);

        List<String> cellValues = new ArrayList<>();

        try {
            // Process process = Runtime.getRuntime().exec(command);

            ProcessBuilder builder = new ProcessBuilder(args);

            builder.redirectErrorStream(true);

            Process process = builder.start();

            log.info("waiting for command....", commadForLog);

            int resultNum = process.waitFor();

            InputStream is = process.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            String line = null;

            // get normal output
            log.info("SYNC SCRIPT OUTPUT FOR:" + String.join("\n", cellValues));
            while (true) {
                line = reader.readLine();
                if (line == null)
                    break;
                log.trace(line);
                cellValues.add(line);
            }

            Arrays.stream(IOUtils.toString(process.getErrorStream()).split("\n")).forEach(errorLine -> log.error(errorLine));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return cellValues;
    }

    public List<String> executeScriptWithE(String... command) {
        try {
            log.info("trying to  executing this script:" + String.join(" ", command));
            return execute(command);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private List<String> execute(String... command) throws Exception {

        ProcessBuilder pb = new ProcessBuilder(command).redirectErrorStream(true);
        Process process = pb.start();
//        StringBuilder result = new StringBuilder();
        List<String> result = new ArrayList<>();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            while (true) {
                String line = in.readLine();
                if (line == null)
                    break;
                result.add(line);
            }
        }

        Arrays.stream(IOUtils.toString(process.getErrorStream()).split("\n")).forEach(line -> log.error(line));

        return result;
    }

}