package me.oczi.schematic;

import org.bukkit.Location;

public interface SchematicIteration {

    void iterate(Location location, byte blockValue, byte dataValue);
}
