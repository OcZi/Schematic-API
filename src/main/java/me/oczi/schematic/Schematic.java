package me.oczi.schematic;

import org.bukkit.Location;

import java.io.File;

/**
 * Schematic basic model class.
 */
public interface Schematic {

    interface Factory {
        Schematic newSchematic(File file);
    }

    void paste(Location location);

    void iterate(Location location, SchematicIteration iteration);

    short getWidth();

    short getLength();

    short getHeight();

    byte[] getBlocks();

    byte[] getData();

}
