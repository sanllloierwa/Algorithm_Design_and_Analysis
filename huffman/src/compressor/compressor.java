package compressor;

import common.*;
import java.io.*;

import static java.lang.System.exit;

public class compressor {
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: java compressor.Compressor <input> <output.huff>");
            exit(1);
        }

        String inputFile = args[0];
        String outputFile = args[1];

        // 1. 统计频率
        long[] freq = new long[256];
        long fileSize = 0;
        try (FileInputStream fis = new FileInputStream(inputFile)) {
            int b;
            while ((b = fis.read()) != -1) {
                freq[b]++;
                fileSize++;
            }
        }

        // 2. 构建哈夫曼树
        HuffmanTree tree = new HuffmanTree(freq);

        // 3. 写出压缩文件
        try (FileOutputStream fos = new FileOutputStream(outputFile);
             DataOutputStream dos = new DataOutputStream(fos);
             BitOutputStream bos = new BitOutputStream(fos)) {

            // 3.1 写原始文件大小 (4 字节，大端)
            dos.writeInt((int) fileSize);

            // 3.2 写树结构 (空文件时树可能为空，需特殊处理)
            if (fileSize > 0) {
                tree.writeTree(bos);
            }

            // 3.3 第二次读取文件，编码并写入
            try (FileInputStream fis2 = new FileInputStream(inputFile)) {
                int b;
                while ((b = fis2.read()) != -1) {
                    String code = tree.getCode(b);
                    for (char c : code.toCharArray()) {
                        bos.writeBit(c == '1' ? 1 : 0);
                    }
                }
            }

            // 3.4 填充并对齐
            bos.flush();
        }

        //System.out.println("Compressed: " + inputFile + " -> " + outputFile);
    }
}