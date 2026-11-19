package com.sports.fitness;

import com.sports.modules.fitness.KMeans;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class KMeansTest {
    @Test void separatesGroupsAndReturnsOriginalCenters() {
        var result = KMeans.fit(new double[][]{{1, 1000}, {2, 1100}, {9, 9000}, {10, 9100}}, 2);
        assertEquals(result.labels()[0], result.labels()[1]);
        assertEquals(result.labels()[2], result.labels()[3]);
        assertNotEquals(result.labels()[0], result.labels()[2]);
        assertArrayEquals(new int[]{2, 2}, result.counts());
        assertEquals(1.5, result.centers()[0][0], 1e-9);
        assertTrue(Double.isFinite(result.sse()));
    }
    @Test void constantColumnAndSingleCluster() {
        var result = KMeans.fit(new double[][]{{2, 7}, {4, 7}}, 1);
        assertArrayEquals(new double[]{3, 7}, result.centers()[0], 1e-9);
        assertEquals(2, result.counts()[0]);
    }
    @Test void rejectsInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> KMeans.fit(new double[0][], 1));
        assertThrows(IllegalArgumentException.class, () -> KMeans.fit(new double[][]{{1}}, 2));
        assertThrows(IllegalArgumentException.class, () -> KMeans.fit(new double[][]{{1}, {1}}, 2));
        assertThrows(IllegalArgumentException.class, () -> KMeans.fit(new double[][]{{Double.NaN}}, 1));
        assertThrows(IllegalArgumentException.class, () -> KMeans.fit(new double[][]{{1}, {2, 3}}, 1));
    }
    @Test void deterministic() {
        double[][] data = {{1}, {2}, {8}, {9}};
        assertArrayEquals(KMeans.fit(data, 2).labels(), KMeans.fit(data, 2).labels());
    }
}
