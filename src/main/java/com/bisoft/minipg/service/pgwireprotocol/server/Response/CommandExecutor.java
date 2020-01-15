package com.bisoft.minipg.service.pgwireprotocol.server.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class CommandExecutor {
	public static final Logger logger = LoggerFactory.getLogger(CommandExecutor.class);

	public List<String> executeCommand(String... args) {
		// logger.info("this command is executing:" );
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

}