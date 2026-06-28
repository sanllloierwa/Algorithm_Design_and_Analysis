#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
CS208 Lab Assignment 3 - Automated Verification Script
Usage: python3 verify.py
"""

import os
import subprocess
import sys
import random
import math
import time
import glob

# ========== 配置 ==========
PROJECT_DIR = os.path.dirname(os.path.abspath(__file__))
SRC_DIR = os.path.join(PROJECT_DIR, "src")
BIN_DIR = os.path.join(PROJECT_DIR, "bin")
TESTCASES_DIR = os.path.join(PROJECT_DIR, "testcases")
OUTPUT_DIR = os.path.join(PROJECT_DIR, "output")
TIME_ANALYSIS_DIR = os.path.join(PROJECT_DIR, "time_analysis")

JAVA = "java"
JAVAC = "javac"
TOLERANCE = 1e-9

# 确保目录存在
for d in [BIN_DIR, TESTCASES_DIR, OUTPUT_DIR, TIME_ANALYSIS_DIR]:
    os.makedirs(d, exist_ok=True)

# ========== Step 1: Clean ==========
def clean():
    print("===== Step 1: Clean =====")
    for d in [BIN_DIR, TESTCASES_DIR, OUTPUT_DIR, TIME_ANALYSIS_DIR]:
        for f in os.listdir(d):
            fp = os.path.join(d, f)
            if os.path.isfile(fp):
                os.remove(fp)
        print(f"Cleaned {d}")
    print("Clean completed.\n")

# ========== Step 2: Compile ==========
def compile_java():
    print("===== Step 2: Compile =====")
    src_files = [os.path.join(SRC_DIR, f) for f in os.listdir(SRC_DIR) if f.endswith(".java")]
    if not src_files:
        print("No .java files found in src/")
        sys.exit(1)
    cmd = [JAVAC, "-encoding", "UTF-8", "-d", BIN_DIR] + src_files   # 添加 -encoding UTF-8
    result = subprocess.run(cmd, capture_output=True, text=True, encoding='utf-8', errors='ignore')
    if result.returncode != 0:
        print("Compilation failed:")
        print(result.stderr)
        sys.exit(1)
    print("Compilation successful.\n")

# ========== Step 3: Generate Test Cases ==========
def generate_test_cases():
    print("===== Step 3: Generate test cases =====")

    def write_points(filename, points):
        with open(os.path.join(TESTCASES_DIR, filename), "w", encoding='utf-8') as f:
            f.write(f"{len(points)}\n")
            for p in points:
                f.write(f"{p[0]} {p[1]}\n")

    random.seed(42)
    # 1. Random points
    sizes = [100, 1000]
    for i, n in enumerate(sizes, 1):
        points = [(random.uniform(-1000, 1000), random.uniform(-1000, 1000)) for _ in range(n)]
        write_points(f"random_{n}.txt", points)

    # 2. Collinear points
    for n in [200, 500]:
        points = [(i, 0.0) for i in range(n)]
        write_points(f"collinear_{n}.txt", points)

    # 3. Vertical split
    for n in [500, 1000]:
        points = [(100.0 + random.uniform(-0.1, 0.1), random.uniform(-1000, 1000)) for _ in range(n)]
        write_points(f"vertical_split_{n}.txt", points)

    # 4. Duplicate points
    for n in [  1000]:
        points = [(42.0, 42.0), (42.0, 42.0)]
        for _ in range(n - 2):
            points.append((random.uniform(-100, 100), random.uniform(-100, 100)))
        write_points(f"duplicates_{n}.txt", points)

    # 5. Large coordinates
    for n in [600, 1000]:
        points = [(random.uniform(-1e9, 1e9), random.uniform(-1e9, 1e9)) for _ in range(n)]
        write_points(f"large_coords_{n}.txt", points)

    # 6. Worst-case strip
    for n in [800, 1000]:
        points = []
        half = n // 2
        for i in range(half):
            y = i * 0.01
            points.append((0.0, y))
            points.append((1.0, y))
        while len(points) < n:
            points.append((0.5, random.uniform(0, half*0.01)))
        write_points(f"worstcase_strip_{n}.txt", points)

    # 7. Performance test sizes
    perf_sizes = [2**10, 2**12, 2**14, 2**15, 2**16, 2**17, 2**18]
    for size in perf_sizes:
        points = [(random.uniform(-1e6, 1e6), random.uniform(-1e6, 1e6)) for _ in range(size)]
        write_points(f"perf_{size}.txt", points)

    print(f"Generated test cases in {TESTCASES_DIR}\n")

# ========== Step 4: Correctness Verification ==========
def run_solver(program_class, input_file):
    cmd = [JAVA, "-cp", BIN_DIR, program_class]
    try:
        with open(input_file, "r", encoding='utf-8') as fin:
            proc = subprocess.run(cmd, stdin=fin, capture_output=True, text=True, timeout=30,
                                  encoding='utf-8', errors='ignore')
        if proc.returncode != 0:
            print(f"Error running {program_class} on {input_file}: {proc.stderr}")
            return None
        return proc.stdout.strip()
    except Exception as e:
        print(f"Exception in run_solver: {e}")
        return None

def correctness_verification():
    print("===== Step 4: Correctness verification =====")
    all_files = glob.glob(os.path.join(TESTCASES_DIR, "*.txt"))
    test_files = [f for f in all_files if not os.path.basename(f).startswith("perf_")]
    passed = 0
    total = 0
    for infile in sorted(test_files):
        total += 1
        try:
            brute_out = run_solver("BruteForce", infile)
            dc_out = run_solver("ClosestPair", infile)
            if brute_out is None or dc_out is None:
                print(f"[FAIL] {os.path.basename(infile)} (solver error)")
                continue
            brute_dist = float(brute_out)
            dc_dist = float(dc_out)
            if abs(brute_dist - dc_dist) < TOLERANCE:
                print(f"[PASS] {os.path.basename(infile)}")
                passed += 1
            else:
                print(f"[FAIL] {os.path.basename(infile)} brute={brute_dist:.9f} dc={dc_dist:.9f}")
        except Exception as e:
            print(f"[FAIL] {os.path.basename(infile)} exception: {e}")
    print(f"Correctness: {passed}/{total} passed.\n")
    return passed == total

# ========== Step 5: Performance Measurement ==========
def measure_performance():
    print("===== Step 5: Time measurement =====")
    perf_files = glob.glob(os.path.join(TESTCASES_DIR, "perf_*.txt"))
    sizes = []
    for f in perf_files:
        name = os.path.basename(f)
        size = int(name.split('_')[1].split('.')[0])
        sizes.append((size, f))
    sizes.sort(key=lambda x: x[0])

    csv_path = os.path.join(TIME_ANALYSIS_DIR, "time_data.csv")
    with open(csv_path, "w", encoding='utf-8') as csvfile:
        csvfile.write("n,nlogn,time_ms\n")
        print("Running performance tests...")
        for size, infile in sizes:
            times = []
            for _ in range(3):
                cmd = [JAVA, "-cp", BIN_DIR, "ClosestPair"]
                start = time.perf_counter()
                try:
                    with open(infile, "r", encoding='utf-8') as fin:
                        proc = subprocess.run(cmd, stdin=fin, capture_output=True, text=True, timeout=120,
                                              encoding='utf-8', errors='ignore')
                except subprocess.TimeoutExpired:
                    print(f"Timeout on {infile}")
                    times.append(float('inf'))
                    continue
                end = time.perf_counter()
                if proc.returncode != 0:
                    print(f"Error on {infile}: {proc.stderr}")
                    times.append(float('inf'))
                else:
                    times.append((end - start) * 1000)
            avg_ms = sum(t for t in times if t != float('inf')) / len([t for t in times if t != float('inf')]) if times else 0
            nlogn = size * math.log2(size)
            csvfile.write(f"{size},{nlogn:.2f},{avg_ms:.3f}\n")
            print(f"n={size:8d}  avg time = {avg_ms:8.3f} ms")
    print(f"Time data saved to {csv_path}")

    plot_script = os.path.join(TIME_ANALYSIS_DIR, "plot.gnu")
    with open(plot_script, "w", encoding='utf-8') as f:
        f.write("set terminal png size 800,600\n")
        f.write("set output 'time_plot.png'\n")
        f.write("set title 'Closest Pair Performance'\n")
        f.write("set xlabel 'n log2 n'\n")
        f.write("set ylabel 'Time (ms)'\n")
        f.write("set key left\n")
        f.write("plot 'time_data.csv' using 2:3 with linespoints title 'D&C time'\n")
    print(f"Gnuplot script saved to {plot_script} (run 'gnuplot {plot_script}' to generate plot)")
    print()

# ========== Main ==========
def main():
    clean()
    compile_java()
    generate_test_cases()
    all_correct = correctness_verification()
    measure_performance()
    print("===== Summary =====")
    if all_correct:
        print("All correctness tests passed.")
    else:
        print("Some correctness tests failed. Check output above.")
    print(f"Time data saved in {TIME_ANALYSIS_DIR}/time_data.csv")
    if not all_correct:
        sys.exit(1)

if __name__ == "__main__":
    main()