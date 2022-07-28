package me.oczi.schematic;

import org.bukkit.util.Vector;

public interface SchematicRelativeIteration {

    /**
     * Iterate schematic blocks around a imaginary position (0, 0).
     * @param position Position of block.
     * @param blockValue Block material value.
     * @param dataValue Block data value.
     */
    void iterate(Vector position, short blockValue, byte dataValue);
}
