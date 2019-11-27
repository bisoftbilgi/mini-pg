package com.bisoft.minipg;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import com.bisoft.minipg.service.pgwireprotocol.Util;
import org.junit.jupiter.api.Test;

public class UtilTest {

    @Test
    public void concatByteArray_returns_null_when_inputs_null() {
        byte[] result = Util.concatByteArray(null, null);
        assertEquals(null, result);
    }

    @Test
    public void concatByteArray_returns_first_param_when_second_null() {
        byte[] nonNull = new byte[] { 0x23, 0x25 };
        byte[] result = Util.concatByteArray(nonNull, null);
        assertEquals(nonNull, result);
    }

    @Test
    public void concatByteArray_returns_second_param_when_first_null() {
        byte[] nonNull = new byte[] { 0x23, 0x25 };
        byte[] result = Util.concatByteArray(null, nonNull);
        assertEquals(nonNull, result);
    }

    @Test
    public void concatByteArray_returns_combined_result() {

        byte[] result = Util.concatByteArray(new byte[] { 0x63, 0x17, 0x44 }, new byte[] { 0x23, 0x25 });
        assertArrayEquals(new byte[] { 0x63, 0x17, 0x44, 0x23, 0x25 }, result);
    }

    @Test
    public void readInt32_reads_integer_from_start() {
        byte[] buffer = new byte[] { 0x63, 0x17, 0x0, 0x0, 0x0, 0xe, 0x0, 0x25 };
        assertEquals(14, Util.readInt32(buffer, 2));
    }

    @Test
    public void readInt16_reads_2_byte_integer_from_start() {
        byte[] buffer = new byte[] { 0x63, 0x17, 0x0, 0x0, 0x0, 0x7, 0x0, 0x25 };
        assertEquals(7, Util.readInt16(buffer, 4));
    }

    @Test
    public void readInt8_reads_one_byte_integer_from_start() {
        byte[] buffer = new byte[] { 0x63, 0x17, 0x0, 0x0, 0x5, 0x0, 0x25 };
        assertEquals(5, Util.readInt8(buffer, 4));
    }

    @Test
    public void readByteArray_returns_length_characters_from_start() {
        int length = 3;
        int start = 2;
        byte[] readResult = Util.readByteArray(new byte[] { 0x63, 0x17, 0x44, 0x23, 0x25 }, start, length);
        assertArrayEquals(new byte[] { 0x44, 0x23, 0x25 }, readResult);
    }

    @Test
    public void readCString_returns_string_before_zero() {
        String result = Util.readCString(new byte[] { 'x', 'y', 'z', 'H', 'e', 'l', 'l', 'o', 0, 'W' }, 3);
        assertEquals("Hello", result);
    }

    @Test
    public void bytesBefore_returns_string_before_searchCharacter() {
        byte[] result = Util.bytesBefore(new byte[] { 'y', 'z', 'H', 'e', 'l', 'l', 'o', 'Z', 'W' }, 5, (byte) 'Z');
        assertArrayEquals(new byte[] { 'l', 'o' }, result);
    }

    @Test
    public void readInt32Array_reads_length_integers_from_start() {
        int length = 2;
        int start = 2;
        int[] result = Util.readInt32Array(new byte[] { 'y', 'z', 0x0, 0x0, 0x0, 0x7, 0x0, 0x0, 0x0, 0xa, 0x1f, 0x11 },
                start, length);
        assertArrayEquals(new int[] { 7, 10 }, result);
    }

    @Test
    public void int32ByteArray_converts_to_4_bytes() {
        assertArrayEquals(new byte[] { 0, 0, 0, 25 }, Util.int32ByteArray(25));
    }

    @Test
    public void int16ByteArray_converts_to_2_bytes() {
        assertArrayEquals(new byte[] { 0, 19 }, Util.int16ByteArray(19));
    }

    @Test
    public void int8ByteArray_converts_to_1_byte() {
        assertArrayEquals(new byte[] { 9 }, Util.int8ByteArray(9));
    }

    @Test
    public void toCString_appends_0_to_end() {
        assertArrayEquals(
                new byte[] { 's', 'e', 'l', 'e', 'c', 't', ' ', 'v', 'e', 'r', 's', 'i', 'o', 'n', '(', ')', 0 },
                Util.toCString("select version()"));
    }

    @Test
    public void toCString_return_0_when_null() {
        assertArrayEquals(new byte[] { 0 }, Util.toCString(null));
    }

    @Test
    public void caseInsensitiveContains_return_false_when_pattern_null() {
        assertEquals(false, Util.caseInsensitiveContains("test", null));
    }

    @Test
    public void caseInsensitiveContains_return_false_when_string_null() {
        assertEquals(false, Util.caseInsensitiveContains(null, "pattern"));
    }

    @Test
    public void caseInsensitiveContains_return_false_when_string_does_not_contain_pattern() {
        assertEquals(false, Util.caseInsensitiveContains("this Is a Test message", "JFk"));
    }

    @Test
    public void caseInsensitiveContains_return_false_when_string_contains_pattern() {
        assertEquals(true, Util.caseInsensitiveContains("this Is a Test message", "ESsa"));
    }

    @Test
    public void byteArrayToString_returns_UTF8_string_from_buffer() {
        assertEquals(Util.byteArrayToString(new byte[] { 's', 'e', 'l', 'e', 'c', 't' }), "select");
    }
}
