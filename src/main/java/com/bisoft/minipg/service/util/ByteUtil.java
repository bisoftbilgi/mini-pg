package com.bisoft.minipg.service.util;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;

public class ByteUtil {
	public static final Logger logger = LoggerFactory.getLogger(ByteUtil.class);
	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static String byteArrayToAsciiDump(byte[] bytes) {

		try {
			return new String(bytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return null;
		}

	}

	public static String byteArrayToAsciiDumpTemp(byte[] bytes) {
		StringBuffer asciiBuffer = new StringBuffer();

		for (int j = 0; j < bytes.length; j++) {
			if ((bytes[j] & 0xff) >= 32 && (bytes[j] & 0xff) <= 255) {
				asciiBuffer.append((char) (bytes[j] & 0xff));
			} else {
				asciiBuffer.append("\u0000");
			}

		}

		return asciiBuffer.toString();

	}

	public static String byteArrayToHexAndAsciiDump(byte[] bytes) {
		StringBuffer dumpBuffer = new StringBuffer();
		StringBuffer hexBuffer = new StringBuffer();
		StringBuffer asciiBuffer = new StringBuffer();

		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexBuffer.append(hexArray[v >>> 4]);
			hexBuffer.append(hexArray[v & 0x0F]);
			hexBuffer.append(' ');

			if (bytes[j] >= 32 && bytes[j] <= 127) {
				asciiBuffer.append((char) bytes[j]);
			} else {
				asciiBuffer.append(".");
			}

			if (((j + 1) % 16 == 0) || ((j + 1) == bytes.length)) {
				dumpBuffer.append(String.format("%4s", Integer.toHexString(j - j % 16)).replace(" ", "0"));
				dumpBuffer.append("   ");
				dumpBuffer.append(String.format("%-48s", hexBuffer.toString().toLowerCase()));
				dumpBuffer.append("  ");
				dumpBuffer.append(String.format("%-16s", asciiBuffer));

				dumpBuffer.append("\r\n");
				hexBuffer.setLength(0);
				asciiBuffer.setLength(0);
			}
		}
		return dumpBuffer.toString();
	}

	public static String byteArrayToHexAndAsciiAndDecDump(byte[] bytes) {
		StringBuffer dumpBuffer = new StringBuffer();
		StringBuffer hexBuffer = new StringBuffer();
		StringBuffer asciiBuffer = new StringBuffer();
		StringBuffer decBuffer = new StringBuffer();

		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexBuffer.append(hexArray[v >>> 4]);
			hexBuffer.append(hexArray[v & 0x0F]);
			hexBuffer.append(' ');

			if (bytes[j] >= 32 && bytes[j] < 127) {
				asciiBuffer.append((char) bytes[j]);
			} else {
				asciiBuffer.append(".");
			}

			decBuffer.append(String.format("%4s", getShort(bytes, j, 1)));

			if (((j + 1) % 16 == 0) || ((j + 1) == bytes.length)) {
				dumpBuffer.append(String.format("%4s", Integer.toHexString(j - j % 16)).replace(" ", "0"));
				dumpBuffer.append("   ");
				dumpBuffer.append(String.format("%-48s", hexBuffer.toString().toLowerCase()));
				dumpBuffer.append("  ");
				dumpBuffer.append(String.format("%-16s", asciiBuffer));
				dumpBuffer.append("   ");
				dumpBuffer.append(String.format("%5s", String.valueOf(j - j % 16)).replace(" ", "0"));
				dumpBuffer.append("  ");
				dumpBuffer.append(String.format("%-64s", decBuffer));

				dumpBuffer.append("\r\n");
				hexBuffer.setLength(0);
				asciiBuffer.setLength(0);
				decBuffer.setLength(0);
			}
		}
		return dumpBuffer.toString();
	}

	public static String byteArrayToHexAndAsciiAndDecDumpWithTab(byte[] bytes) {
		StringBuffer dumpBuffer = new StringBuffer();
		StringBuffer hexBuffer = new StringBuffer();
		StringBuffer asciiBuffer = new StringBuffer();
		StringBuffer decBuffer = new StringBuffer();

		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexBuffer.append(hexArray[v >>> 4]);
			hexBuffer.append(hexArray[v & 0x0F]);
			hexBuffer.append('\t');

			if (bytes[j] >= 32 && bytes[j] < 127) {
				asciiBuffer.append((char) bytes[j]);
			} else {
				asciiBuffer.append(".");
			}

			decBuffer.append(getShort(bytes, j, 1)).append("\t");

			if (((j + 1) % 16 == 0) || ((j + 1) == bytes.length)) {
				dumpBuffer.append(String.format("%4s", Integer.toHexString(j - j % 16)).replace(" ", "0"));
				dumpBuffer.append("\t");
				dumpBuffer.append(hexBuffer.toString().toLowerCase());

				if ((j + 1) % 16 != 0) {
					for (int k = (j + 1) % 16; k < 16; k++) {
						dumpBuffer.append("\t");
					}
				}

				dumpBuffer.append(asciiBuffer);
				dumpBuffer.append("\t");
				dumpBuffer.append(j - j % 16);
				dumpBuffer.append("\t");
				dumpBuffer.append(decBuffer);

				dumpBuffer.append("\r\n");
				hexBuffer.setLength(0);
				asciiBuffer.setLength(0);
				decBuffer.setLength(0);
			}
		}
		return dumpBuffer.toString();
	}

	public static String tnsDataByteArrayToHexAndAsciiDump(byte[] bytes) {
		if (bytes.length <= 10) {
			return "";
		}

		byte[] dataBytes = new byte[bytes.length - 10];
		System.arraycopy(bytes, 10, dataBytes, 0, bytes.length - 10);
		return byteArrayToHexAndAsciiDump(dataBytes);
	}

	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	public static byte[] hexAndAsciiDumpToByteArray(String dump) {
		String[] lines = dump.split("\\r|\\n");
		StringBuffer buffer = new StringBuffer();
		for (String line : lines) {
			buffer.append(line.substring(7, 54).replaceAll(" ", ""));
		}
		return hexStringToByteArray(buffer.toString());
	}

	public static byte[] decodeAsBytes(ByteBuf buf) {
		byte[] bytes;
		int length = buf.readableBytes();

		if (buf.hasArray()) {
			bytes = buf.array();
		} else {
			bytes = new byte[length];
			buf.getBytes(buf.readerIndex(), bytes);
		}
		return bytes;
	}

	public static short getShort(byte[] bytes, int offset, int length) {
		if (offset < 0 || offset + length > bytes.length || length < 1 || length > 2) {
			return 0;
		}

		byte[] temp = new byte[2];
		if (length == 2) {
			temp[0] = bytes[offset++];
		}
		temp[1] = bytes[offset];

		return ByteBuffer.wrap(temp).getShort();
	}

	public static byte[] excelTnsToByteArray(String excelTnsData) {
		String[] decArray = excelTnsData.split("\\t|\\n");
		byte[] buffer = new byte[decArray.length];
		for (int i = 0; i < decArray.length; i++) {
			buffer[i] = (byte) Integer.parseInt(decArray[i]);
		}
		return buffer;
	}

	public static byte[] excelTnsDataToByteArray(String excelTnsData) {
		String[] decArray = excelTnsData.split("(\\t|\\n)+");
		byte[] buffer = new byte[10 + decArray.length];
		buffer[0] = (byte) (buffer.length / 256);
		buffer[1] = (byte) (buffer.length % 256);
		buffer[4] = (byte) 6;
		for (int i = 0; i < decArray.length; i++) {
			buffer[i + 10] = (byte) Integer.parseInt(decArray[i]);
		}
		return buffer;
	}

	public static byte[] getByteBufferFromHexString(String rawBuffer) {
		rawBuffer = rawBuffer.replaceAll(" ", "");

		return ByteUtil.hexStringToByteArray(rawBuffer);

	}

	public static boolean isEqualBytes(byte[] a, byte[] b) {
		return Arrays.equals(a, b);
	}

	public static byte[] convertStringToByteArraySimple(String s) {

		char[] chars = s.toCharArray();

		byte[] bytes = new byte[chars.length];
		for (int i = 0; i < chars.length; i++) {
			bytes[i] = (byte) (chars[i] & 0xff);
		}

		return bytes;
	}

	public static byte[] convertStringToByteArray(String s) {
		try {
			return s.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	public static int getIntFromByteArray(byte[] byteArr, int offset) {
		int result = 0;
		if (offset + 4 >= byteArr.length) {
			// int fromByteArray(byte[] bytes) {
			result = byteArr[offset] << 24 | (byteArr[offset + 1] & 0xFF) << 16 | (byteArr[offset + 2] & 0xFF) << 8
					| (byteArr[offset + 3] & 0xFF);
			// }

			// logger.trace(byteArr[offset + 3]);
		}
		// else {
		//// throw new Exception("offset out of bound..");
		// }
		return result;
	}

	public static int fromByteArray(byte[] bytes) {
		byte[] subArray = Arrays.copyOfRange(bytes, 0, 4);
		return ByteBuffer.wrap(subArray).getInt();
	}
}
