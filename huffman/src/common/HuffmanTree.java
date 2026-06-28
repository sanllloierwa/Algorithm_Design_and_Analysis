package common;

import java.util.*;
import java.io.*;

public class HuffmanTree {
    private HuffmanNode root;
    private Map<Integer, String> codeMap; // 符号 -> 01字符串

    /** 根据频率表构建树 */
    public HuffmanTree(long[] freq) {
        PriorityQueue<HuffmanNode> pq = new PriorityQueue<HuffmanNode>();
        for (int i = 0; i < 256; i++) {
            if (freq[i] > 0) {
                pq.add(new HuffmanNode(i, freq[i]));
            }
        }
        // 处理空文件或单一符号
        if (pq.isEmpty()) {
            root = null;
        } else {
            while (pq.size() > 1) {
                HuffmanNode left = pq.poll();
                HuffmanNode right = pq.poll();
                HuffmanNode parent = new HuffmanNode(left.frequency + right.frequency, left, right);
                pq.add(parent);
            }
            root = pq.poll();
        }
        buildCodeMap();
    }

    /** 从比特流直接重建树（解压时使用） */
    public HuffmanTree(BitInputStream in) throws IOException {
        root = readTree(in);
        // 解压时不需要 codeMap，直接遍历树即可
    }

    private HuffmanNode readTree(BitInputStream in) throws IOException {
        int bit = in.readBit();
        if (bit == -1) throw new IOException("Unexpected end of file when reading tree");
        if (bit == 1) {  // 叶子节点
            int symbol = in.readByte();
            if (symbol == -1) throw new IOException("Unexpected end of file when reading symbol");
            return new HuffmanNode(symbol, 0);
        } else {         // 内部节点
            HuffmanNode left = readTree(in);
            HuffmanNode right = readTree(in);
            return new HuffmanNode(0, left, right); // 频率不重要
        }
    }

    /** 序列化树到比特流 */
    public void writeTree(BitOutputStream out) throws IOException {
        writeNode(root, out);
    }

    private void writeNode(HuffmanNode node, BitOutputStream out) throws IOException {
        if (node.isLeaf()) {
            out.writeBit(1);
            out.writeByte(node.symbol);
        } else {
            out.writeBit(0);
            writeNode(node.left, out);
            writeNode(node.right, out);
        }
    }

    /** 生成编码表 */
    private void buildCodeMap() {
        codeMap = new HashMap<>();
        if (root != null) {
            buildCode(root, new StringBuilder());
        }
    }

    private void buildCode(HuffmanNode node, StringBuilder prefix) {
        if (node.isLeaf()) {
            codeMap.put(node.symbol, prefix.toString());
        } else {
            prefix.append('0');
            buildCode(node.left, prefix);
            prefix.deleteCharAt(prefix.length()-1);

            prefix.append('1');
            buildCode(node.right, prefix);
            prefix.deleteCharAt(prefix.length()-1);
        }
    }

    /** 根据符号获取编码字符串 */
    public String getCode(int symbol) {
        return codeMap.getOrDefault(symbol, "");
    }

    /** 提供根节点用于解码遍历 */
    public HuffmanNode getRoot() {
        return root;
    }
}