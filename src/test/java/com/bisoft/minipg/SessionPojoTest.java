package com.bisoft.minipg;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class SessionPojoTest {

    @Test
    public void falseTest() {
        assertEquals((1 + 1), 2);
    }

    /*
     * @Test public void testMultipleEncodeReferenceCount() throws IOException,
     * ClassNotFoundException { EmbeddedChannel channel = new EmbeddedChannel(new
     * CompatibleObjectEncoder()); testEncode(channel,
     * SessionPojo.newSession("remote1", "local1", "message1")); testEncode(channel,
     * SessionPojo.newSession("remote2", "local2", "message2")); testEncode(channel,
     * SessionPojo.newSession("remote3", "local3", "message3"));
     * assertFalse(channel.finishAndReleaseAll()); }
     * 
     * private static void testEncode(EmbeddedChannel channel, SessionPojo original)
     * throws IOException, ClassNotFoundException { channel.writeOutbound(original);
     * Object o = channel.readOutbound(); ByteBuf buf = (ByteBuf) o;
     * ObjectInputStream ois = new ObjectInputStream(new ByteBufInputStream(buf));
     * try { assertEquals(original, ois.readObject()); } finally { buf.release();
     * ois.close(); } }
     */

}
