package com.bisoft.minipg.service.pgwireprotocol.server.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class ScriptExecutor {

    public static final Logger logger = LoggerFactory.getLogger(ScriptExecutor.class);

    public List<String> executeScript(String... args) {

        log.info("EXECUTING:", String.join(" ", args));

        List<String> cellValues = new ArrayList<>();
        try {
            ProcessBuilder builder = new ProcessBuilder(args);
            builder.redirectErrorStream(true);
            Process        process = builder.start();
            InputStream    is      = process.getInputStream();
            BufferedReader reader  = new BufferedReader(new InputStreamReader(is));

            String line = null;
            while ((line = reader.readLine()) != null) {
                log.trace(line);
                cellValues.add(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return cellValues;
    }

}