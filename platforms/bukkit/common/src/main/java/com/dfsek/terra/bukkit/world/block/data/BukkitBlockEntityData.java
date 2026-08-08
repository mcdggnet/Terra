package com.dfsek.terra.bukkit.world.block.data;

import com.dfsek.terra.api.data.ExtendedData;


public record BukkitBlockEntityData(Object data) implements ExtendedData {
    @Override
    public Object getHandle() {
        return data;
    }

    @Override
    public String toString() {
        return data.toString();
    }
}
