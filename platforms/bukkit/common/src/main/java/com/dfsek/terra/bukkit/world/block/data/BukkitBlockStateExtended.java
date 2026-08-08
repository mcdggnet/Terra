package com.dfsek.terra.bukkit.world.block.data;

import com.dfsek.terra.api.block.state.BlockState;
import com.dfsek.terra.api.block.state.BlockStateExtended;
import com.dfsek.terra.api.data.ExtendedData;


public class BukkitBlockStateExtended extends BukkitBlockState implements BlockStateExtended {
    private final BukkitBlockEntityData data;

    public BukkitBlockStateExtended(org.bukkit.block.data.BlockData delegate, Object data) {
        this(delegate, new BukkitBlockEntityData(data));
    }

    private BukkitBlockStateExtended(org.bukkit.block.data.BlockData delegate, BukkitBlockEntityData data) {
        super(delegate);
        this.data = data;
    }

    @Override
    public ExtendedData getData() {
        return data;
    }

    @Override
    public BlockStateExtended setData(ExtendedData data) {
        return new BukkitBlockStateExtended(getHandle(), new BukkitBlockEntityData(data.getHandle()));
    }

    @Override
    public BlockState getState() {
        return new BukkitBlockState(getHandle());
    }
}
