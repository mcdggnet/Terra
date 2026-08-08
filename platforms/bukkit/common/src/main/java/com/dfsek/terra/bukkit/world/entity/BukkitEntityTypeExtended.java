package com.dfsek.terra.bukkit.world.entity;

import com.dfsek.terra.api.data.ExtendedData;
import com.dfsek.terra.api.entity.EntityType;
import com.dfsek.terra.api.entity.EntityTypeExtended;


public class BukkitEntityTypeExtended extends BukkitEntityType implements EntityTypeExtended {
    private final BukkitEntityData data;

    public BukkitEntityTypeExtended(org.bukkit.entity.EntityType delegate, String nbt) {
        this(delegate, new BukkitEntityData(nbt));
    }

    private BukkitEntityTypeExtended(org.bukkit.entity.EntityType delegate, BukkitEntityData data) {
        super(delegate);
        this.data = data;
    }

    @Override
    public ExtendedData getData() {
        return data;
    }

    @Override
    public EntityTypeExtended setData(ExtendedData data) {
        return new BukkitEntityTypeExtended(getHandle(), new BukkitEntityData(data.toString()));
    }

    @Override
    public EntityType getType() {
        return new BukkitEntityType(getHandle());
    }
}
