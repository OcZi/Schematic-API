package me.sintaxis.schematic;

import org.bukkit.Location;

import java.io.File;
import java.io.IOException;

/**
 * Schematic basic model class.
 */
public interface Schematic {

    void paste(Location location);

    short getWidth();

    short getLength();

    short getHeight();

    byte[] getBlocks();

    byte[] getData();

}
