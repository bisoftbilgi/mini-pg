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

    @Test
    public void execute() {

        sut.executeCommand("ls", "-alh");
        assert true;
    }

    @Test
    public void executeSync() {

        sut.executeCommand("sleep", "3");
        assert true;
    }

    @Test
    public void executeCommandByProcess() {

        List<String> result = sut.executeCommand("date");
        System.out.println(result);
        assert true;

    }
}