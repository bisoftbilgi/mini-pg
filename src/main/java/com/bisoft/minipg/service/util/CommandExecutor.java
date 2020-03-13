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

    public List<String> executeCommand(String... args) {

        System.out.println("EXECUTING THIS:" + String.join(" ", args));
        log.info("executing:" + String.join(" ", args));
        Process p;
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