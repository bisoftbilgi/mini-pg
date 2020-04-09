package com.bisoft.minipg.service.util;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ScriptExecutor {

    private static final String NEWLINE = "\n";

    public List<String> executeScript(String... args) {
        System.out.println("sync script executing:" + String.join(" ", args));

        log.trace("async script executing:" + String.join(" ", args));

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
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return cellValues;
    }

    public List<String> executeScriptSync(String... args) {

        System.out.println("sync script executing:" + String.join(" ", args));
        log.trace("sync script executing:" + String.join(" ", args));

        List<String> cellValues = new ArrayList<>();

        try {

            ProcessBuilder builder = new ProcessBuilder(args);

            builder.redirectErrorStream(true);

            Process process = builder.start();
            log.info("waiting for command....", String.join(" ", args));
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
                System.out.println(line);
                cellValues.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cellValues;
    }

    public List<String> executeScriptWithE(String... command) {
        try {
            System.out.println("trying to  executing this script:" + String.join(" ", command));
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
//                result.append(line).append(NEWLINE);
                System.out.println(line);
            }
        }
        return result;
    }

}