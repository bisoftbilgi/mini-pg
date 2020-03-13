package com.bisoft.minipg.service.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


public class StreamGobbler implements Runnable {

    private  InputStream      inputStream;
    private  Consumer<String> consumer;

    public StreamGobbler(final InputStream inputStream, final Consumer<String> consumer) {

        this.inputStream = inputStream;
        this.consumer = consumer;
    }

    @Override
    public void run() {

        new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumer);
    }
}