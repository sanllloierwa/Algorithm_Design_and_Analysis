import java.io.*;
import java.util.*;

public class ClosestPair {
    static class Point {
        double x, y;
        Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    static Comparator<Point> xComparator = Comparator.comparingDouble(p -> p.x);
    static Comparator<Point> yComparator = Comparator.comparingDouble(p -> p.y);

    static double dist2(Point a, Point b) {
        double dx = a.x - b.x;
        double dy = a.y - b.y;
        return dx * dx + dy * dy;
    }

    public static double closestPair(Point[] points) {
        if (points.length < 2) return Double.POSITIVE_INFINITY;
        Point[] pointsByX = points.clone();
        Arrays.sort(pointsByX, xComparator);
        Point[] pointsByY = points.clone();
        Arrays.sort(pointsByY, yComparator);
        return closestPairRec(pointsByX, pointsByY, 0, pointsByX.length - 1);
    }

    private static double closestPairRec(Point[] pointsByX, Point[] pointsByY, int left, int right) {
        if (right - left < 3) {
            double min = Double.POSITIVE_INFINITY;
            for (int i = left; i <= right; i++) {
                for (int j = i + 1; j <= right; j++) {
                    double d2 = dist2(pointsByX[i], pointsByX[j]);
                    if (d2 < min) min = d2;
                }
            }
            return Math.sqrt(min);
        }

        int mid = (left + right) / 2;
        Point midPoint = pointsByX[mid];

        // Split pointsByY into left and right subarrays based on x coordinate
        Point[] leftY = new Point[mid - left + 1];
        Point[] rightY = new Point[right - mid];
        int li = 0, ri = 0;
        for (Point p : pointsByY) {
            if (p.x <= midPoint.x && li < leftY.length) {
                leftY[li++] = p;
            } else if (ri < rightY.length) {
                rightY[ri++] = p;
            }
        }

        double dLeft = closestPairRec(pointsByX, leftY, left, mid);
        double dRight = closestPairRec(pointsByX, rightY, mid + 1, right);
        double delta = Math.min(dLeft, dRight);

        // Build strip (points within delta of midline) from pointsByY (already sorted by y)
        List<Point> strip = new ArrayList<>();
        for (Point p : pointsByY) {
            if (Math.abs(p.x - midPoint.x) < delta) {
                strip.add(p);
            }
        }

        for (int i = 0; i < strip.size(); i++) {
            Point p1 = strip.get(i);
            for (int j = i + 1; j < strip.size(); j++) {
                Point p2 = strip.get(j);
                if (p2.y - p1.y >= delta) break;
                double d2 = dist2(p1, p2);
                if (d2 < delta * delta) {
                    delta = Math.sqrt(d2);
                }
            }
        }
        return delta;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int n = Integer.parseInt(br.readLine().trim());
        Point[] points = new Point[n];
        for (int i = 0; i < n; i++) {
            String[] parts = br.readLine().trim().split("\\s+");
            double x = Double.parseDouble(parts[0]);
            double y = Double.parseDouble(parts[1]);
            points[i] = new Point(x, y);
        }
        double minDist = closestPair(points);
        System.out.printf("%.9f\n", minDist);
    }
}