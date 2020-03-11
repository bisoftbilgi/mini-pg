package com.bisoft.minipg.service.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ScriptExecutor {
 
	public List<String> executeScriptNormal(String... args) {
		for (String string : args) {
			log.info("executing:" + string);
		}
		List<String> cellValues = new ArrayList<>();
		try {
			ProcessBuilder builder = new ProcessBuilder(args);
			builder.redirectErrorStream(true);

			Process process = builder.start();
			InputStream is = process.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));

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


	//TODO: give a try to processBuilder...
	public List<String> executeScript(String... args) {

		ProcessBuilder processBuilder = new ProcessBuilder();

		// Run a shell command
		processBuilder.command("bash", "-c", String.join(" ", args));

		System.out.println("executing: bash -c "+ String.join(" ", args));

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

}