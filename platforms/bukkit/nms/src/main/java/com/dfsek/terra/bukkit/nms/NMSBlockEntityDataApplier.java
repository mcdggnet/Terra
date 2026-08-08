package com.dfsek.terra.bukkit.nms;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.generator.CraftLimitedRegion;
import org.bukkit.generator.LimitedRegion;

import com.dfsek.terra.api.block.state.BlockState;
import com.dfsek.terra.api.block.state.BlockStateExtended;
import com.dfsek.terra.bukkit.world.block.data.BukkitBlockEntityDataApplier;
import com.dfsek.terra.bukkit.world.block.data.BukkitBlockState;


public class NMSBlockEntityDataApplier implements BukkitBlockEntityDataApplier.Applier {
    @Override
    public boolean apply(World world, int x, int y, int z, BlockState data, boolean physics) {
        if(!(data instanceof BlockStateExtended)) return false;
        Level level = ((CraftWorld) world).getHandle();
        apply(level, level.getChunkAt(new BlockPos(x, y, z)), x, y, z, data, physics);
        return true;
    }

    @Override
    public boolean apply(LimitedRegion region, int x, int y, int z, BlockState data, boolean physics) {
        if(!(data instanceof BlockStateExtended)) return false;
        WorldGenLevel level = ((CraftLimitedRegion) region).getHandle();
        BlockPos blockPos = new BlockPos(x, y, z);
        if(level instanceof WorldGenRegion worldGenRegion) {
            apply(worldGenRegion, worldGenRegion.getChunk(blockPos), x, y, z, data, physics);
        } else {
            Level serverLevel = level.getMinecraftWorld();
            apply(serverLevel, serverLevel.getChunkAt(blockPos), x, y, z, data, physics);
        }
        return true;
    }

    private void apply(Level level, ChunkAccess chunk, int x, int y, int z, BlockState data, boolean physics) {
        BlockPos blockPos = new BlockPos(x, y, z);
        net.minecraft.world.level.block.state.BlockState state = ((CraftBlockData) ((BukkitBlockState) data).getHandle()).getState();
        level.setBlock(blockPos, state, physics ? 3 : 1042);
        loadBlockEntity(level, chunk, blockPos, state, (BlockStateExtended) data);
    }

    private void apply(WorldGenRegion level, ChunkAccess chunk, int x, int y, int z, BlockState data, boolean physics) {
        BlockPos blockPos = new BlockPos(x, y, z);
        net.minecraft.world.level.block.state.BlockState state = ((CraftBlockData) ((BukkitBlockState) data).getHandle()).getState();
        level.setBlock(blockPos, state, physics ? 3 : 1042, 512);
        loadBlockEntity(level.getLevel(), chunk, blockPos, state, (BlockStateExtended) data);
    }

    private void loadBlockEntity(Level level, ChunkAccess chunk, BlockPos blockPos,
                                 net.minecraft.world.level.block.state.BlockState state, BlockStateExtended data) {
        CompoundTag nbt = ((CompoundTag) data.getData().getHandle()).copy();
        nbt.putInt("x", blockPos.getX());
        nbt.putInt("y", blockPos.getY());
        nbt.putInt("z", blockPos.getZ());
        BlockEntity blockEntity = BlockEntity.loadStatic(blockPos, state, nbt, level.registryAccess());
        if(blockEntity != null) {
            blockEntity.setLevel(level);
            chunk.setBlockEntity(blockEntity);
        }
    }
}
