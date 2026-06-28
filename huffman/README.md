# Huffman Tree Compressor and Decompressor

### Project Structure

```
huffman/
├── src/
│ ├── compressor/
│ │ └── Compressor.java # Compressor main class
│ ├── decompressor/
│ │ └── Decompressor.java # Decompressor main class
│ └── common/
│ ├── HuffmanNode.java # Huffman tree node
│ ├── HuffmanTree.java # Tree building, encoding, serialization
│ ├── BitInputStream.java # Bit-level input stream
│ └── BitOutputStream.java # Bit-level output stream
├── bin/ # Compiled .class files (created by build script)
├── testcases/ # Original input files for testing
├── output/ # Generated .huff and decoded_* files
├── run_verify.py # Automated test script
└── README.md
```

### The Script Compile and Running

To initialize the Compressor and Decompressor, you can run the automated compile and verification script:

```
python run_verify.py
```

This script will:

- Clean bin/ and output/ directories

- Compile all Java source files

- Compress every file in testcases/ into output/*.huff

- Decompress the .huff files back to output/decoded_*

- Byte‑compare original and decoded files, reporting [Pass] or [Fail]

After initialize, you can also run the compressor and decompressor in a mannual way:

```
java -cp bin compressor.compressor <input_file> <output_file.huff>

java -cp bin decompressor.decompressor <input_file.huff> <output_file>
```

### Code Design

##### Huffman Tree Serialization

The tree is stored in a pre‑order traversal (root → left → right) using the following rules:

For an internal node: write a single 0 bit.

For a leaf node: write a 1 bit followed immediately by 8 bits (1 byte) representing the original symbol (0‑255).

##### Padding Handling

Use 0 bit to fulfill the bytes. When producing output, stop after it has written exactly original size bytes, thus naturally discarding any trailing padding bits.

##### Edge cases

1. Empty File, Only the 4‑byte header (0x00000000) is written. No tree or data follows.

2. The tree contains only one leaf node. Its Huffman code is the empty string. The decompressor will repeat the byte until the original size is reached.

##### Design Correctness

1. Deterministic tree: The priority queue orders nodes first by frequency, then by symbol value.

2. Bit ordering: Bits are written and read MSB first within each byte. This is consistent across tree and data, and matches the implementation in BitOutputStream.writeBit and BitInputStream.readBit