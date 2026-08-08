package com.dfsek.terra.bukkit.util;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

import com.dfsek.terra.api.entity.EntityType;
import com.dfsek.terra.bukkit.world.entity.BukkitEntityTypeExtended;
import com.dfsek.terra.bukkit.world.entity.BukkitEntityType;


public class BukkitUtils {
    private static final Logger logger = LoggerFactory.getLogger(BukkitUtils.class);

    public static boolean isLiquid(BlockData blockState) {
        Material material = blockState.getMaterial();
        return material == Material.WATER || material == Material.LAVA;
    }

    public static EntityType getEntityType(String id) {
        int nbtStart = id.indexOf('{');
        String entityIdentifier = nbtStart < 0 ? id : id.substring(0, nbtStart);
        if(!entityIdentifier.startsWith("minecraft:")) throw new IllegalArgumentException("Invalid entity identifier " + entityIdentifier);
        String entityID = entityIdentifier.toUpperCase(Locale.ROOT).substring(10);

        org.bukkit.entity.EntityType entityType = switch(entityID) {
            case "END_CRYSTAL" -> org.bukkit.entity.EntityType.END_CRYSTAL;
            case "ENDER_CRYSTAL" -> throw new IllegalArgumentException(
                "Invalid entity identifier " + entityIdentifier); // make sure this issue can't happen the other way around.
            default -> org.bukkit.entity.EntityType.valueOf(entityID);
        };

        if(nbtStart < 0) {
            return new BukkitEntityType(entityType);
        }
        return new BukkitEntityTypeExtended(entityType, createEntitySnapshotNbt(entityIdentifier, id.substring(nbtStart)));
    }

    private static String createEntitySnapshotNbt(String entityIdentifier, String nbt) {
        if(nbt.length() < 2 || nbt.charAt(0) != '{' || nbt.charAt(nbt.length() - 1) != '}') {
            throw new IllegalArgumentException("Invalid entity NBT: " + nbt);
        }
        if(nbt.length() == 2) {
            return "{id:\"" + entityIdentifier + "\"}";
        }
        return "{id:\"" + entityIdentifier + "\"," + nbt.substring(1);
    }

    public static String stripTrailingNBT(String id) {
        int nbtStart = id.indexOf('{');
        if(nbtStart < 0) {
            return id;
        }
        logger.debug("Ignoring trailing NBT in '{}'.", id);
        return id.substring(0, nbtStart);
    }
}
