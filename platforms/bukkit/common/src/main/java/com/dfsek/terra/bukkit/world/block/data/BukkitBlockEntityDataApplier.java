package com.dfsek.terra.bukkit.world.block.data;

import org.bukkit.World;
import org.bukkit.generator.LimitedRegion;

import com.dfsek.terra.api.block.state.BlockState;


public final class BukkitBlockEntityDataApplier {
    private static volatile Applier applier;

    private BukkitBlockEntityDataApplier() {

    }

    public static void setApplier(Applier applier) {
        BukkitBlockEntityDataApplier.applier = applier;
    }

    public static boolean apply(World world, int x, int y, int z, BlockState data, boolean physics) {
        Applier applier = BukkitBlockEntityDataApplier.applier;
        return applier != null && applier.apply(world, x, y, z, data, physics);
    }

    public static boolean apply(LimitedRegion region, int x, int y, int z, BlockState data, boolean physics) {
        Applier applier = BukkitBlockEntityDataApplier.applier;
        return applier != null && applier.apply(region, x, y, z, data, physics);
    }

    public interface Applier {
        boolean apply(World world, int x, int y, int z, BlockState data, boolean physics);

        boolean apply(LimitedRegion region, int x, int y, int z, BlockState data, boolean physics);
    }
}
