package com.sports.fitness;

import com.sports.modules.fitness.KMeans;

/** 可在无Maven依赖的环境用 javac/java 运行。 */
public class KMeansSmoke {
    public static void main(String[] args) {
        var a = KMeans.fit(new double[][]{{1, 1000}, {2, 1100}, {9, 9000}, {10, 9100}}, 2);
        check(a.labels()[0] == a.labels()[1] && a.labels()[2] == a.labels()[3]
                && a.labels()[0] != a.labels()[2]);
        check(a.counts()[0] == 2 && a.counts()[1] == 2);
        check(Math.abs(a.centers()[0][0] - 1.5) < 1e-9 && Double.isFinite(a.sse()));
        var constant = KMeans.fit(new double[][]{{2, 7}, {4, 7}}, 1);
        check(constant.centers()[0][0] == 3 && constant.centers()[0][1] == 7);
        check(java.util.Arrays.equals(a.labels(), KMeans.fit(
                new double[][]{{1, 1000}, {2, 1100}, {9, 9000}, {10, 9100}}, 2).labels()));
        rejects(new double[0][], 1);
        rejects(new double[][]{{1}}, 2);
        rejects(new double[][]{{1}, {1}}, 2);
        rejects(new double[][]{{Double.NaN}}, 1);
        rejects(new double[][]{{1}, {2, 3}}, 1);
        System.out.println("PASS: groups, counts, centers, constant column, determinism, invalid inputs");
    }
    private static void rejects(double[][] x, int k) {
        try { KMeans.fit(x, k); }
        catch (IllegalArgumentException expected) { return; }
        throw new AssertionError("Expected invalid input rejection");
    }
    private static void check(boolean value) {
        if (!value) throw new AssertionError("Algorithm check failed");
    }
}
