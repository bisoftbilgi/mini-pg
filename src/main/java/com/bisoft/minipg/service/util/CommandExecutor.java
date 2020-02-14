package com.bisoft.minipg.service.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommandExecutor {

    public List<String> executeCommand(String... args) {

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