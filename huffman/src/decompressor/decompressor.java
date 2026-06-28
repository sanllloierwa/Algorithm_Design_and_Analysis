package decompressor;

import common.*;
import java.io.*;

public class decompressor {
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: java decompressor.Decompressor <input.huff> <output>");
            System.exit(1);
        }

        String inputFile = args[0];
        String outputFile = args[1];

        try (FileInputStream fis = new FileInputStream(inputFile);
             DataInputStream dis = new DataInputStream(fis);
             BitInputStream bis = new BitInputStream(fis)) {

            // 1. 读取原始文件大小
            long originalSize = dis.readInt() & 0xFFFFFFFFL; // 转为无符号 long
            if (originalSize == 0) {
                // 空文件：直接创建空输出
                new FileOutputStream(outputFile).close();
                System.out.println("Decompressed empty file: " + outputFile);
                return;
            }

            // 2. 重建哈夫曼树
            HuffmanTree tree = new HuffmanTree(bis);

            // 3. 解码数据
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                long written = 0;
                HuffmanNode current = tree.getRoot();
                while (written < originalSize) {
                    int bit = bis.readBit();
                    if (bit == -1) throw new IOException("Corrupted file: unexpected end of bitstream");
                    if (bit == 0) {
                        current = current.left;
                    } else {
                        current = current.right;
                    }
                    if (current.isLeaf()) {
                        fos.write(current.symbol);
                        written++;
                        current = tree.getRoot();
                    }
                }
            }
        }

        //System.out.println("Decompressed: " + inputFile + " -> " + outputFile);
    }
}