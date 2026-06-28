package common;

import java.io.*;

public class BitInputStream implements AutoCloseable{
    private InputStream in;
    private int buffer;      // 当前字节 (0~255)
    private int bitsRemaining; // 当前字节中剩余可读位数 (0~8)

    public BitInputStream(InputStream in) {
        this.in = in;
        buffer = 0;
        bitsRemaining = 0;
    }

    /** 读取一个比特，读取失败返回 -1 */
    public int readBit() throws IOException {
        if (bitsRemaining == 0) {
            int nextByte = in.read();
            if (nextByte == -1) return -1;   // 文件结束
            buffer = nextByte;
            bitsRemaining = 8;
        }
        bitsRemaining--;
        return (buffer >> bitsRemaining) & 1;
    }

    /** 读取 8 个比特，组装成一个字节；文件结束则返回 -1 */
    public int readByte() throws IOException {
        int value = 0;
        for (int i = 0; i < 8; i++) {
            int bit = readBit();
            if (bit == -1) return -1;
            value = (value << 1) | bit;
        }
        return value;
    }

    public void close() throws IOException{
        
    }
}