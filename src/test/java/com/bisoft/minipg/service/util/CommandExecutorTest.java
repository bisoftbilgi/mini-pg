package com.bisoft.minipg.service.util;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CommandExecutorTest {

    CommandExecutor sut;

    @BeforeEach
    void init() {

        this.sut = new CommandExecutor();
    }

    //    @Test
    public void execute() {

        sut.executeCommand("sleep", "10");
    }

    //    @Test
    public void executeSync() {

        sut.executeCommandSync("sleep", "3");
    }

    @Test
    public void executeCommandByProcess() {

        List<String> result = sut.executeCommandByProcess("date");
        System.out.println(result);
    }
}