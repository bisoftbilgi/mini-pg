package com.bisoft.minipg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.bisoft.minipg.service.pgwireprotocol.server.PasswordPacket;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PasswordPacketTest {
    private PasswordPacket passwordPacket;

    @Test
    public void passwordPacketTrueTest() {
        byte[] buffer = new byte[] { 112, 0, 0, 0, 40, 109, 100, 53, 40, 50, 3, 32, 112, 103, 95, 114, 101, 119, 105, 110, 100, 40, 49,
                57, 50, 46, 49, 54, 56, 46, 53, 46, 56, 41, 0, 0, 0, 66, 0, 0, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, 68, 0, 0,
                0, 6, 80, 0, 69, 0, 0, 0, 9, 0, 0, 0, 0, 0, 83, 0, 0, 0, 4};
        boolean packetMatches = passwordPacket.packetMatches(buffer);
        assertTrue(packetMatches);
    }
}










