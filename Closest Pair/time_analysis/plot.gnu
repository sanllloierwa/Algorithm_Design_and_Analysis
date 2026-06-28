set terminal png size 800,600
set output 'time_plot.png'
set title 'Closest Pair Performance'
set xlabel 'n log2 n'
set ylabel 'Time (ms)'
set key left
plot 'time_data.csv' using 2:3 with linespoints title 'D&C time'
