package me.oczi.schematic;

import me.oczi.schematic.utils.NMSUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.jnbt.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import static me.oczi.schematic.utils.ChildTagUtil.getChildTag;

/**
 * Implementation of {@link Schematic} with MCEdit format.
 */
public class MCEditSchematic implements Schematic {

    protected final short width;
    protected final short length;
    protected final short height;
    protected final short[] blocks;
    protected final byte[] data;

    protected boolean ignoreAir;

    public static void parseAndPaste(Location location, File file) throws IOException {
        MCEditSchematic.from(file).paste(location);
    }

    public static MCEditSchematic from(File file) throws IOException {
        CompoundTag schematicTag;
        try (FileInputStream stream = new FileInputStream(file)) {
            try (NBTInputStream nbtStream = new NBTInputStream(stream)) {
                schematicTag = (CompoundTag) nbtStream.readTag();
            }
        }
        Map<String, Tag> schematic = schematicTag.getValue();
        short width = getChildTag(schematic, "Width", ShortTag.class).getValue();
        short length = getChildTag(schematic, "Length", ShortTag.class).getValue();
        short height = getChildTag(schematic, "Height", ShortTag.class).getValue();
        String materials = getChildTag(schematic, "Materials", StringTag.class).getValue();
        if (!materials.equals("Alpha")) {
            throw new IllegalArgumentException("Schematic file is not an Alpha schematic");
        }
        byte[] blocksId = getChildTag(schematic, "Blocks", ByteArrayTag.class).getValue();
        byte[] blockData = getChildTag(schematic, "Data", ByteArrayTag.class).getValue();
        byte[] addId = new byte[0];
        short[] blocks = new short[blocksId.length];

        if (schematic.containsKey("AddBlocks")) {
            addId = getChildTag(schematic, "AddBlocks", ByteArrayTag.class).getValue();
        }

        for (int index = 0; index < blocksId.length; index++) {
            if ((index >> 1) >= addId.length) {
                blocks[index] = (short) (blocksId[index] & 0xFF);
            } else {
                int left = (index & 1) == 0
                    ? (addId[index >> 1] & 0x0F) << 8
                    : (addId[index >> 1] & 0xF0) << 4;
                blocks[index] = (short) (left + (blocksId[index] & 0xFF));
            }
        }

        return new MCEditSchematic(width, length, height, blocks, blockData);
    }

    public MCEditSchematic(short width, short length, short height, short[] blocks, byte[] data) {
        this.blocks = blocks;
        this.data = data;
        this.width = width;
        this.length = length;
        this.height = height;
    }

    @Override
    public void paste(Location location) {
        iterate(location, this::pasteIterate);
    }

    @Override
    public void iterate(Location location, SchematicWorldIteration iteration) {
        location = location.clone();
        location.subtract(width / 2.00, height / 2.00, length / 2.00);
        World world = location.getWorld();
        int blockX = location.getBlockX();
        int blockY = location.getBlockY();
        int blockZ = location.getBlockZ();
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < length; ++z) {
                    int index = y * width * length + z * width + x;
                    short blockValue = blocks[index];
                    if (ignoreAir && blockValue == 0) {
                        continue;
                    }
                    byte dataValue = data[index];

                    Location position = new Location(world,
                        x + blockX,
                        y + blockY,
                        z + blockZ);
                    iteration.iterate(position, blockValue, dataValue);
                }
            }
        }
    }

    @Override
    public void iterate(SchematicRelativeIteration iteration) {
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < length; ++z) {
                    int index = y * width * length + z * width + x;
                    short blockValue = blocks[index];
                    if (ignoreAir && blockValue == 0) {
                        continue;
                    }
                    byte dataValue = data[index];

                    Vector position = new Vector(
                        x,
                        y,
                        z);
                    iteration.iterate(position, blockValue, dataValue);
                }
            }
        }
    }

    @Override
    public void setIgnoreAir(boolean ignoreAir) {
        this.ignoreAir = ignoreAir;
    }

    protected void pasteIterate(Location location, short blockValue, byte dataValue) {
        NMSUtil.setBlockFast(location, blockValue, dataValue);
    }

    @Override
    public boolean isIgnoreAir() {
        return ignoreAir;
    }

    @Override
    public short getWidth() {
        return this.width;
    }

    @Override
    public short getLength() {
        return this.length;
    }

    @Override
    public short getHeight() {
        return this.height;
    }

    @Override
    public short[] getBlocks() {
        return this.blocks;
    }

    @Override
    public byte[] getData() {
        return this.data;
    }

    @Override
    public String toString() {
        return "MCEditSchematic{" +
            "width=" + width +
            ", length=" + length +
            ", height=" + height +
            ", blocks=" + Arrays.toString(blocks) +
            ", data=" + Arrays.toString(data) +
            '}';
    }
}