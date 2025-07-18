package com.bisoft.minipg.helper;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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

    public List<String> executeIndependentCommand(String... args) {
        List<String> cellValues = new ArrayList<>();

        try {
            // Output'u yazacağı dosya
            String outputFile = "/tmp/command_output_" + System.currentTimeMillis() + ".log";

            // Komutu tek bir shell komutu olarak inşa et
            String joinedCommand = String.join(" ", args);
            String command = String.format("setsid sh -c '%s > %s 2>&1 &'", joinedCommand, outputFile);

            // Shell üzerinden çalıştır
            ProcessBuilder pb = new ProcessBuilder("sh", "-c", command);
            pb.start();

            // Bağımsız process başladıktan sonra kısa bir süre bekle
            Thread.sleep(500); // opsiyonel: biraz bekleyip dosya oluşsun diye

            // Çıktıyı dosyadan oku
            File outFile = new File(outputFile);
            if (outFile.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(outFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        cellValues.add(line);
                    }
                }
            } else {
                cellValues.add("Output file not found: " + outputFile);
            }

        } catch (Exception e) {
            e.printStackTrace();
            cellValues.add("Exception occurred: " + e.getMessage());
        }

        return cellValues;
    }
}
