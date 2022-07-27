package me.oczi.schematic.utils;

import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

public interface NMSUtil {

    /**
     * Set block at location with block id and data.
     * Called at NMS World level (Maybe not so fast).
     *
     * @author Solotory (Sintaxis)
     * @param location Location to place block.
     * @param blockId Block material id.
     * @param data Data of material.
     */
    static void setBlockFast(Location location, int blockId, byte data) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition pos = new BlockPosition(location.getX(), location.getY(), location.getZ());
        IBlockData blockData = Block.getByCombinedId(blockId + (data << 12));
        world.setTypeAndData(pos, blockData, 2);
    }
}