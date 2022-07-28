package me.oczi.schematic;

import org.bukkit.Location;

public interface SchematicWorldIteration {

    /**
     * Iterate schematic blocks around a real position.
     * @param position Block position in world.
     * @param blockValue Block material value.
     * @param dataValue Block data value.
     */
    void iterate(Location position, short blockValue, byte dataValue);
}
