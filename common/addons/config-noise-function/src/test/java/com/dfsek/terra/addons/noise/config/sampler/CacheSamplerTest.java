package com.dfsek.terra.addons.noise.config.sampler;

import com.dfsek.seismic.type.sampler.Sampler;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class CacheSamplerTest {
    /** A pure sampler that counts its calls. */
    private static final class Counting implements Sampler {
        private int calls;

        @Override
        public double getSample(long seed, double x, double y) {
            calls++;
            return Math.sin(x * 12.9898 + y * 78.233 + seed) * 43758.5453;
        }

        @Override
        public double getSample(long seed, double x, double y, double z) {
            calls++;
            return Math.sin(x * 12.9898 + y * 78.233 + z * 37.719 + seed) * 43758.5453;
        }
    }

    @Test
    public void returnsWhatTheSamplerGives() {
        Counting inner = new Counting();
        Counting reference = new Counting();
        CacheSampler cache = new CacheSampler(inner, 2);
        Random random = new Random(1);
        double[] special = { 0.0, -0.0, Double.NaN, Double.MAX_VALUE, -1e300, 1e-300, 1000003.0 / 1048573 };
        for(int i = 0; i < 200000; i++) {
            long seed = random.nextInt(4);
            double x, y;
            switch(i % 4) {
                case 0 -> { x = random.nextInt(64); y = random.nextInt(64); } // whole numbers, many repeats
                case 1 -> { x = (random.nextInt(4096) + 1000003.0) / 1048573; y = (random.nextInt(4096) + 1000003.0) / 1048573; }
                case 2 -> { x = special[random.nextInt(special.length)]; y = special[random.nextInt(special.length)]; }
                default -> { x = random.nextDouble() * 1e6 - 5e5; y = random.nextDouble() * 1e6 - 5e5; }
            }
            assertEquals(reference.getSample(seed, x, y), cache.getSample(seed, x, y), "seed " + seed + " at " + x + ", " + y);
        }
    }

    @Test
    public void aColumnIsSampledOnceUpItsHeight() {
        Counting inner = new Counting();
        CacheSampler cache = new CacheSampler(inner, 2);
        for(int x = 0; x < 5; x++) {
            for(int z = 0; z < 5; z++) {
                for(int y = 0; y < 97; y++) cache.getSample(42, x * 4, z * 4);
            }
        }
        assertEquals(25, inner.calls);
    }

    @Test
    public void eachThreadKeepsItsOwnColumns() throws InterruptedException {
        Counting inner = new Counting();
        CacheSampler cache = new CacheSampler(inner, 2);
        cache.getSample(7, 3, 4);
        Thread other = new Thread(() -> cache.getSample(7, 3, 4));
        other.start();
        other.join();
        cache.getSample(7, 3, 4);
        assertEquals(2, inner.calls);
    }
}
