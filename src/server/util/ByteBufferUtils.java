package server.util;

import java.nio.ByteBuffer;

public class ByteBufferUtils {

	public static int readSignedWord(ByteBuffer buffer) {
	/*	int value = 0;
		value |= buffer.get() & 0xff << 8;
		value |= buffer.get() & 0xff;
		return value;*/
		int i = ((buffer.get() & 0xff) << 8) + (buffer.get() & 0xff);
		if (i > 32767)
		    i -= 0x10000;
		return i;
	}

	public static long readQWord(ByteBuffer buffer) {
		long l = (long) readDWord(buffer) & 0xffffffffL;
		long l1 = (long) readDWord(buffer) & 0xffffffffL;
		return (l << 32) + l1;
	}

	public static int readDWord(ByteBuffer buffer) {
		long value = 0;
		value |= (buffer.get() & 0xff) << 24;
		value |= (buffer.get() & 0xff) << 16;
		value |= (buffer.get() & 0xff) << 8;
		value |= (buffer.get() & 0xff);
		return (int) value;
	}

	public static String readString(ByteBuffer buffer) {
		byte temp;
		StringBuilder b = new StringBuilder();
		while ((temp = (byte) buffer.get()) != 10) {
			b.append((char) temp);
		}
		return b.toString();
	}
}