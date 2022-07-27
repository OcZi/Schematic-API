package me.oczi.schematic.test;

import me.oczi.schematic.MCEditSchematic;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.File;
import java.io.IOException;

public final class SchematicTest {

    public static void main(String[] args) throws IOException {
        Location location = new Location(Bukkit.getWorld("Test"), 10, 10, 10);
        File testFile = new File("");
        MCEditSchematic.parseAndPaste(location, testFile);
    }
}