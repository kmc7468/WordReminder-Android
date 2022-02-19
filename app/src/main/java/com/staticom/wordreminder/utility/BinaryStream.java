package com.staticom.wordreminder.utility;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class BinaryStream {

    private final InputStream inputStream;
    private final OutputStream outputStream;

    public BinaryStream(InputStream inputStream) {
        this.inputStream = inputStream;
        this.outputStream = null;
    }

    public BinaryStream(OutputStream outputStream) {
        this.inputStream = null;
        this.outputStream = outputStream;
    }

    public byte[] read(int length) throws IOException {
        final byte[] bytes = new byte[length];

        inputStream.read(bytes);

        return bytes;
    }

    public int readInt() throws IOException {
        final byte[] bytes = read(4);

        return bytes[0] & 0xFF | (bytes[1] << 8) & (0xFF << 8) |
                (bytes[2] << 16) & (0xFF << 16) | (bytes[3] << 16) & (0xFF << 24);
    }

    public String readString() throws IOException {
        final int length = readInt();
        final byte[] bytes = read(length * 2);

        return new String(bytes, StandardCharsets.UTF_16LE);
    }

    public void skip(int length) throws IOException {
        inputStream.skip(length);
    }

    public void write(byte[] bytes) throws IOException {
        outputStream.write(bytes);
    }

    public void writeInt(int value) throws IOException {
        write(new byte[] { (byte)value, (byte)(value >>> 8), (byte)(value >>> 16), (byte)(value >>> 24) });
    }

    public void writeString(String value) throws IOException {
        final byte[] bytes = value.getBytes(StandardCharsets.UTF_16LE);

        writeInt(bytes.length / 2);
        write(bytes);
    }
}