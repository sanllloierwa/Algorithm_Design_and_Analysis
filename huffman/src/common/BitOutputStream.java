package common;

import java.io.*;

public class BitOutputStream implements AutoCloseable{
    private OutputStream out;
    private int buffer;     // 当前正在装的字节 (0~255)
    private int bitsInBuffer;  // buffer 中已有的位数 (0~7)

    public BitOutputStream(OutputStream out) {
        this.out = out;
        buffer = 0;
        bitsInBuffer = 0;
    }

    /** 写入一个比特 (0 或 1) */
    public void writeBit(int bit) throws IOException {
        buffer = (buffer << 1) | (bit & 1);
        bitsInBuffer++;
        if (bitsInBuffer == 8) {
            flushByte();
        }
    }

    /** 写入 8 个比特 (一个字节) */
    public void writeByte(int value) throws IOException {
        for (int i = 7; i >= 0; i--) {
            writeBit((value >> i) & 1);
        }
    }

    /** 将当前缓冲区不满 8 位时补零并写出 */
    public void flush() throws IOException {
        while (bitsInBuffer > 0) {
            writeBit(0);  // 填充 0
        }
        // 此时 bitsInBuffer 必为 0
    }

    private void flushByte() throws IOException {
        out.write(buffer);
        buffer = 0;
        bitsInBuffer = 0;
    }

    public void close() throws IOException {
        flush();
    }
}