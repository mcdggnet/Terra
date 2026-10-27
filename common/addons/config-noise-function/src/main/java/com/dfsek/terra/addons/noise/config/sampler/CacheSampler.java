package com.dfsek.terra.addons.noise.config.sampler;

import com.dfsek.seismic.type.sampler.Sampler;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.Scheduler;
import org.jetbrains.annotations.ApiStatus.Experimental;

import com.dfsek.terra.api.util.cache.DoubleSeededVector3Key;
import com.dfsek.terra.api.util.generic.pair.Pair;
import com.dfsek.terra.api.util.generic.pair.Pair.Mutable;

import static com.dfsek.terra.api.util.cache.CacheUtils.CACHE_EXECUTOR;


@Experimental
public class CacheSampler implements Sampler {

    // 2D: each thread's table of recent columns, indexed by a hash of the exact coordinates. Packs use 2D caches for
    // values worked out once per column and read at every y up it, where a Caffeine lookup cost more than many of the
    // values it saved. A miss samples and overwrites the slot, so a lookup returns exactly what the sampler gives.
    private static final int COLUMN_SLOTS = 256; // a power of two: the slot is the hash's top bits

    private final Sampler sampler;
    private final ThreadLocal<Columns> cache2D;
    private final ThreadLocal<Mutable<DoubleSeededVector3Key, LoadingCache<DoubleSeededVector3Key, Double>>> cache3D;

    public CacheSampler(Sampler sampler, int dimensions) {
        this.sampler = sampler;
        if(dimensions == 2) {
            this.cache2D = ThreadLocal.withInitial(Columns::new);
            this.cache3D = null;
        } else {
            this.cache3D = ThreadLocal.withInitial(() -> {
                LoadingCache<DoubleSeededVector3Key, Double> cache = Caffeine
                    .newBuilder()
                    .executor(CACHE_EXECUTOR)
                    .scheduler(Scheduler.systemScheduler())
                    .initialCapacity(981504)
                    .maximumSize(981504)
                    .build(this::sampleNoise);
                return Pair.of(new DoubleSeededVector3Key(0, 0, 0, 0), cache).mutable();
            });
            this.cache2D = null;
        }
    }

    private Double sampleNoise(DoubleSeededVector3Key vec) {
        this.cache3D.get().setLeft(new DoubleSeededVector3Key(0, 0, 0, 0));
        return this.sampler.getSample(vec.seed, vec.x, vec.y, vec.z);
    }

    @Override
    public double getSample(long seed, double x, double y) {
        Columns columns = cache2D.get();
        long xBits = Double.doubleToRawLongBits(x);
        long yBits = Double.doubleToRawLongBits(y);
        int slot = Columns.slot(xBits, yBits, seed);
        if(columns.filled[slot] && columns.x[slot] == xBits && columns.y[slot] == yBits && columns.seed[slot] == seed) {
            return columns.value[slot];
        }
        double value = sampler.getSample(seed, x, y);
        columns.x[slot] = xBits;
        columns.y[slot] = yBits;
        columns.seed[slot] = seed;
        columns.value[slot] = value;
        columns.filled[slot] = true;
        return value;
    }

    @Override
    public double getSample(long seed, double x, double y, double z) {
        Mutable<DoubleSeededVector3Key, LoadingCache<DoubleSeededVector3Key, Double>> cachePair = cache3D.get();
        DoubleSeededVector3Key mutableKey = cachePair.getLeft();
        mutableKey.set(x, y, z, seed);
        return cachePair.getRight().get(mutableKey);
    }

    private static final class Columns {
        private final long[] x = new long[COLUMN_SLOTS];
        private final long[] y = new long[COLUMN_SLOTS];
        private final long[] seed = new long[COLUMN_SLOTS];
        private final double[] value = new double[COLUMN_SLOTS];
        private final boolean[] filled = new boolean[COLUMN_SLOTS];

        private static int slot(long xBits, long yBits, long seed) {
            long h = xBits * 0x9E3779B97F4A7C15L ^ yBits * 0xC2B2AE3D27D4EB4FL ^ seed;
            h ^= h >>> 33; // mixes high bits down: whole-number coordinates differ only in their high bits
            h *= 0xFF51AFD7ED558CCDL;
            h ^= h >>> 33;
            return (int) (h >>> (64 - Integer.numberOfTrailingZeros(COLUMN_SLOTS)));
        }
    }
}
