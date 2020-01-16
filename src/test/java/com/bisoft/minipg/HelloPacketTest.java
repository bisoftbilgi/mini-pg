package com.bisoft.minipg;

import com.bisoft.minipg.service.pgwireprotocol.server.HelloPacket;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HelloPacketTest {

    private HelloPacket helloPacket;

    @BeforeEach
    void init() {

        this.helloPacket = new HelloPacket();
    }


    public void helloPacketTest() {

        byte[] buffer = new byte[]{80, 0, 0, 0, 33, 0, 45, 45, 32, 112, 103, 95, 114, 101, 119, 105, 110, 100, 40, 49,
            57, 50, 46, 49, 54, 56, 46, 53, 46, 56, 41, 0, 0, 0, 66, 0, 0, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, 68, 0, 0,
            0, 6, 80, 0, 69, 0, 0, 0, 9, 0, 0, 0, 0, 0, 83, 0, 0, 0, 4};
        boolean packetMatches = helloPacket.packetMatches(buffer);
        assertTrue(packetMatches);
    }
}