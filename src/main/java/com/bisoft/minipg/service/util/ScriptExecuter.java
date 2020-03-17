package com.bisoft.minipg.service.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ScriptExecuter {
 
	public List<String> executeScript(String... args) {

		System.out.println("EXECUTING THIS:" + String.join(" ", args));
		log.info("executing:" + String.join(" ", args));
		Process p;
		List<String> cellValues = new ArrayList<>();
		try {
			p = Runtime.getRuntime().exec(args);
			int resultNum = p.waitFor();

			BufferedReader in = new BufferedReader(
					new InputStreamReader(p.getInputStream()));

			BufferedReader err = new BufferedReader(
					new InputStreamReader(p.getErrorStream()));

			String line = null;

			if (resultNum == 0) {
				while ((line = in.readLine()) != null) {
					log.debug(line);
					cellValues.add(line);
				}
				// For better seeing the result
				System.out.println("SUCCESS:" + String.join("\n", cellValues));
				log.info("COMMAND OUTPUT:" + String.join("\n", cellValues));
			} else {
				while ((line = err.readLine()) != null) {
					log.debug(line);
					cellValues.add(line);
				}
				// for better seeing the errors
				System.out.println("ERROR:" + String.join("\n", cellValues));
				log.error("COMMAND OUTPUT:" + String.join("\n", cellValues));
			}


		} catch (Exception e) {
			e.printStackTrace();
		}

		return cellValues;
	}

}