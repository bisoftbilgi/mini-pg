package com.bisoft.minipg.service.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScriptExecutorTest {
    ScriptExecutor sut;


    @BeforeEach
    void init() {

        this.sut = new ScriptExecutor();
    }

    @Test
    public void execute() {

        sut.executeScript("sleep", "1");
    }


    @Test
    public void executeSync() {

        sut.executeScriptSync("sleep", "5");
//        sut.executeScriptSync("for i in {1..10}; do echo -n \"This is a test in loop $i \"; date ; sleep 5; done");
        sut.executeScript("echo","'end'");
    }

}