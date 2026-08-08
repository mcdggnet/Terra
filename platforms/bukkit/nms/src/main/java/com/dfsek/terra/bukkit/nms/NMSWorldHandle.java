package com.dfsek.terra.bukkit.nms;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.commands.arguments.blocks.BlockStateParser.BlockResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.jetbrains.annotations.NotNull;

import com.dfsek.terra.api.block.state.BlockState;
import com.dfsek.terra.bukkit.handles.BukkitWorldHandle;
import com.dfsek.terra.bukkit.world.block.data.BukkitBlockState;
import com.dfsek.terra.bukkit.world.block.data.BukkitBlockStateExtended;


public class NMSWorldHandle extends BukkitWorldHandle {
    private final BlockState air = BukkitBlockState.newInstance(Material.AIR.createBlockData());

    @Override
    public synchronized @NotNull BlockState createBlockState(@NotNull String data) {
        try {
            BlockResult blockResult = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK, data, true);
            org.bukkit.block.data.BlockData bukkitData = CraftBlockData.createData(blockResult.blockState());
            CompoundTag nbt = blockResult.nbt();
            if(nbt == null) {
                return BukkitBlockState.newInstance(bukkitData);
            }
            if(!blockResult.blockState().hasBlockEntity()) {
                return BukkitBlockState.newInstance(bukkitData);
            }
            return new BukkitBlockStateExtended(bukkitData, prepareBlockEntityTag(blockResult.blockState(), nbt));
        } catch(CommandSyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public @NotNull BlockState air() {
        return air;
    }

    private CompoundTag prepareBlockEntityTag(net.minecraft.world.level.block.state.BlockState state, CompoundTag nbt) {
        CompoundTag prepared = nbt.copy();
        if(!prepared.contains("id") && state.getBlock() instanceof EntityBlock entityBlock) {
            BlockEntity blockEntity = entityBlock.newBlockEntity(BlockPos.ZERO, state);
            if(blockEntity != null) {
                prepared.putString("id", BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType()).toString());
            }
        }
        prepared.putInt("x", 0);
        prepared.putInt("y", 0);
        prepared.putInt("z", 0);
        return prepared;
    }
}
