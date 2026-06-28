import os
import shutil
import subprocess

SRC_DIR = "src"
BIN_DIR = "bin"
TEST_DIR = "testcases"
OUT_DIR = "output"

# Step 1: Clean
print("\n====Step 1: Clean====")
if os.path.exists(BIN_DIR):
    shutil.rmtree(BIN_DIR)
if os.path.exists(OUT_DIR):
    shutil.rmtree(OUT_DIR)
os.makedirs(BIN_DIR)
os.makedirs(OUT_DIR)
print("Step 1: Clean finished")

# Step 2: Compile
print("\n====Step 2: Compile====")
compile_cmd = ["javac", "-encoding", "utf-8", "-d", BIN_DIR,
               os.path.join(SRC_DIR, "common", "*.java"),
               os.path.join(SRC_DIR, "compressor", "*.java"),
               os.path.join(SRC_DIR, "decompressor", "*.java")]
subprocess.run(compile_cmd, check=True)
print("Step 2: Compile finished")

# Step 3: Compress all testcases
print("\n====Step 3: Compress====")
for fname in os.listdir(TEST_DIR):
    src = os.path.join(TEST_DIR, fname)
    if not os.path.isfile(src):
        continue
    dst = os.path.join(OUT_DIR, fname + ".huff")
    print(f"  Compressing {src}")
    subprocess.run(["java", "-cp", BIN_DIR, "compressor.compressor", src, dst], check=True)

# Step 4: Decompress
print("\n====Step 4: Decompress====")
for fname in os.listdir(OUT_DIR):
    if fname.endswith(".huff"):
        src = os.path.join(OUT_DIR, fname)
        orig_name = fname[:-5]  # 去掉 .huff
        dst = os.path.join(OUT_DIR, "decoded_" + orig_name)
        print(f"  Decompressing {src} -> {dst}")
        subprocess.run(["java", "-cp", BIN_DIR, "decompressor.decompressor", src, dst], check=True)

# Step 5: Byte-byte comparison
print("\n====Step 5: Verification====")
all_pass = True
for fname in os.listdir(TEST_DIR):
    orig = os.path.join(TEST_DIR, fname)
    decoded = os.path.join(OUT_DIR, "decoded_" + fname)
    if not os.path.isfile(decoded):
        print(f"  [Fail] {fname} vs decoded_{fname} (file missing)")
        all_pass = False
        continue
    with open(orig, "rb") as f1, open(decoded, "rb") as f2:
        if f1.read() == f2.read():
            print(f"  [Pass] {fname} vs decoded_{fname}")
        else:
            print(f"  [Fail] {fname} vs decoded_{fname}")
            all_pass = False

print("DONE")
if not all_pass:
    exit(1)