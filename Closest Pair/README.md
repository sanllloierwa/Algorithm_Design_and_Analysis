# Closest Pair of Points

平面上最近点对问题的 Java 实现，包含分治算法（O(n log n)）和暴力算法（O(n²)）两种解法，以及自动化验证脚本。

## 项目结构

```
Closest Pair/
├── src/
│   ├── ClosestPair.java
│   └── BruteForce.java
├── testcases/
├── time_analysis/
│   └── time_data.csv
├── verify.py
└── README.md
```

## 编译 / 运行步骤

### 环境要求

- JDK 8+
- Python 3.6+

### 方式一：验证脚本

```bash
python verify.py
```

脚本自动执行以下步骤：
1. 清理旧文件
2. 编译 Java 源文件
3. 生成七类测试用例
4. 用暴力算法验证分治算法正确性
5. 测量不同规模下的运行时间

### 方式二：手动编译并运行

```bash
# 编译
mkdir -p bin
javac -encoding UTF-8 -d bin src/*.java

# 运行分治算法
java -cp bin ClosestPair < testcases/random_100.txt

# 运行暴力算法
java -cp bin BruteForce < testcases/random_100.txt
```

### 输入格式

```
N
x1 y1
x2 y2
...
xN yN
```

第一行为点的个数 N，接下来 N 行每行两个浮点数，分别表示一个点的 x 和 y 坐标。

### 输出

程序输出最近点对的距离，保留 9 位小数。

## 测试用例说明

| 用例类型 | 文件名 | 目的 |
|---------|--------|------|
| 随机点 | `random_{100,1000}.txt` | 通用正确性验证 |
| 共线点 | `collinear_{200,500}.txt` | 测试所有点位于同一直线时的边界情况 |
| 垂直分割 | `vertical_split_{500,1000}.txt` | 所有点 x 坐标集中在中间线附近，考察 strip 合并逻辑 |
| 重复点 | `duplicates_1000.txt` | 包含两个完全相同坐标的点，距离应为 0 |
| 大坐标 | `large_coords_{600,1000}.txt` | 坐标范围 ±1e9，验证浮点精度 |
| 最坏 strip | `worstcase_strip_{800,1000}.txt` | 大量点落入 strip 区域，考察内层循环效率 |
| 性能测试 | `perf_{1024,...,262144}.txt` | 不同规模（n = 2^10 ~ 2^18），用于绘制时间曲线 |

正确性验证方式：将暴力算法（BruteForce）的结果作为标准答案，与分治算法（ClosestPair）的输出对比，误差小于 1e-9 视为通过。

## 实验设置

### 算法

- **暴力算法**：枚举所有点对，时间复杂度 O(n²)。
- **分治算法**：按 x 坐标排序后递归二分平面，合并时只检查 strip 区域内距中线 δ 以内的点。strip 内按 y 排序，对每个点最多检查其后 7 个点。时间复杂度 O(n log n)。

### 性能测试规模

性能测试在 n = 2^10, 2^12, 2^14, 2^15, 2^16, 2^17, 2^18 共 7 个规模下进行，每次运行 3 遍取平均时间。横坐标使用 n·log₂n，纵坐标为实际运行时间（ms）。若时间与 n·log₂n 呈线性关系，则验证了 O(n log n) 的理论复杂度。

### 生成时间曲线图

```bash
cd time_analysis
gnuplot plot.gnu
```

运行后生成 `time_plot.png`。
