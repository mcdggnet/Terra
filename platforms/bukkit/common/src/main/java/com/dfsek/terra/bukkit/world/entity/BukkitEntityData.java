package com.dfsek.terra.bukkit.world.entity;

import com.dfsek.terra.api.data.ExtendedData;


public record BukkitEntityData(String nbt) implements ExtendedData {
    @Override
    public String getHandle() {
        return nbt;
    }

    @Override
    public String toString() {
        return nbt;
    }
}
