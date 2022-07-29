package me.oczi.schematic;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.io.File;

/**
 * Schematic basic model class.
 */
public interface Schematic {

    /**
     * Factory of {@link Schematic}.
     * Useful for injection like Guice.
     */
    interface Factory {
        Schematic newSchematic(File file);
    }

    /**
     * Paste schematic at location.
     * @param location Location to paste.
     */
    void paste(Location location);

    /**
     * Iterate all blocks of the schematic
     * with provided iteration function.
     * @param location Location of schematic on iteration.
     * @param iteration Iteration function to receive blocks.
     */
    void iterate(Location location, SchematicWorldIteration iteration);

    void iterate(SchematicRelativeIteration iteration);

    void setIgnoreAir(boolean ignoreAir);

    Vector getSize();

    Vector getOffset();

    Vector getOrigin();

    boolean isIgnoreAir();

    int getWidth();

    int getLength();

    int getHeight();

    short[] getBlocks();

    byte[] getData();

}
