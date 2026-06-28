package common;

public class HuffmanNode implements Comparable<HuffmanNode> {
    public int symbol;          // 0~255 表示叶子，-1 表示内部节点
    public long frequency;      // 出现次数
    public HuffmanNode left;
    public HuffmanNode right;

    // 叶子节点构造方法
    public HuffmanNode(int symbol, long freq) {
        this.symbol = symbol;
        this.frequency = freq;
        this.left = null;
        this.right = null;
    }

    // 内部节点构造方法
    public HuffmanNode(long freq, HuffmanNode left, HuffmanNode right) {
        this.symbol = -1;
        this.frequency = freq;
        this.left = left;
        this.right = right;
    }

    public boolean isLeaf() {
        return symbol != -1;
    }

    @Override
    public int compareTo(HuffmanNode other) {
        // 按频率比较，频率相同时为了确定性可比较 symbol
        if (this.frequency != other.frequency)
            return Long.compare(this.frequency, other.frequency);
        return Integer.compare(this.symbol, other.symbol);
    }
}