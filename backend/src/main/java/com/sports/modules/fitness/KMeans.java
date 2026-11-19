package com.sports.modules.fitness;

import java.util.*;

/** 无额外依赖的确定性 K-Means；标准化避免肺活量等大数值指标支配距离。 */
public final class KMeans {
    private KMeans() {}
    public record Analysis(int[] labels, double[][] centers, int[] counts,
                           double sse, int iterations) {}

    public static Analysis fit(double[][] input, int k) {
        if (input == null || input.length == 0 || k < 1 || k > input.length)
            throw new IllegalArgumentException("k必须在1和有效样本数之间");
        int n = input.length;
        if (input[0] == null || input[0].length == 0)
            throw new IllegalArgumentException("特征不能为空");
        int d = input[0].length;
        double[] mean = new double[d], scale = new double[d];
        for (double[] row : input) {
            if (row == null || row.length != d) throw new IllegalArgumentException("特征维度不一致");
            for (int j = 0; j < d; j++) {
                if (!Double.isFinite(row[j])) throw new IllegalArgumentException("特征必须为有限数值");
                mean[j] += row[j] / n;
            }
        }
        for (double[] row : input)
            for (int j = 0; j < d; j++) scale[j] += Math.pow(row[j] - mean[j], 2) / n;
        for (int j = 0; j < d; j++) scale[j] = scale[j] == 0 ? 1 : Math.sqrt(scale[j]);
        double[][] x = new double[n][d], centers = new double[k][d];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < d; j++) x[i][j] = (input[i][j] - mean[j]) / scale[j];
        centers[0] = x[0].clone();
        // 最远点初始化；不同有效特征少于k时拒绝无意义的空簇。
        for (int c = 1; c < k; c++) {
            int best = 0; double farthest = -1;
            for (int i = 0; i < n; i++) {
                double nearest = Double.POSITIVE_INFINITY;
                for (int p = 0; p < c; p++) nearest = Math.min(nearest, distance(x[i], centers[p]));
                if (nearest > farthest) { farthest = nearest; best = i; }
            }
            if (farthest == 0) throw new IllegalArgumentException("不同特征样本数小于k");
            centers[c] = x[best].clone();
        }
        int[] labels = new int[n]; Arrays.fill(labels, -1);
        int[] counts = new int[k]; int iterations = 0;
        for (; iterations < 100; ) {
            iterations++;
            boolean changed = false;
            double[][] sums = new double[k][d]; Arrays.fill(counts, 0);
            for (int i = 0; i < n; i++) {
                int c = 0;
                for (int p = 1; p < k; p++)
                    if (distance(x[i], centers[p]) < distance(x[i], centers[c])) c = p;
                changed |= labels[i] != c; labels[i] = c; counts[c]++;
                for (int j = 0; j < d; j++) sums[c][j] += x[i][j];
            }
            for (int c = 0; c < k; c++)
                if (counts[c] > 0) for (int j = 0; j < d; j++) centers[c][j] = sums[c][j] / counts[c];
            if (!changed) break;
        }
        double sse = 0;
        for (int i = 0; i < n; i++) sse += distance(x[i], centers[labels[i]]);
        for (int c = 0; c < k; c++)
            for (int j = 0; j < d; j++) centers[c][j] = centers[c][j] * scale[j] + mean[j];
        return new Analysis(labels, centers, counts, sse, iterations);
    }

    private static double distance(double[] a, double[] b) {
        double sum = 0;
        for (int j = 0; j < a.length; j++) sum += Math.pow(a[j] - b[j], 2);
        return sum;
    }
}
