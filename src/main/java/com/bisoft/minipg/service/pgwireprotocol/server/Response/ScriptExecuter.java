package com.bisoft.minipg.service.pgwireprotocol.server.Response;

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
		for (String string : args) {
			log.info("executing:" + string);
		}
		List<String> cellValues = new ArrayList<>();
		try {
			ProcessBuilder builder = new ProcessBuilder(args);
//			builder.directory(new File(ConfigurationService.GetValue("minipg.postgres_bin_path")));
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

}