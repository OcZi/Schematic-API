package me.oczi.schematic;

import me.oczi.schematic.utils.ChildTagUtil;
import me.oczi.schematic.utils.NMSUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.jnbt.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * Implementation of {@link Schematic} with MCEdit format.
 */
public class MCEditSchematic implements Schematic {

    protected final short width;
    protected final short length;
    protected final short height;
    protected final byte[] blocks;
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
        short width = ChildTagUtil.getChildTag(schematic, "Width", ShortTag.class).getValue();
        short length = ChildTagUtil.getChildTag(schematic, "Length", ShortTag.class).getValue();
        short height = ChildTagUtil.getChildTag(schematic, "Height", ShortTag.class).getValue();
        String materials = ChildTagUtil.getChildTag(schematic, "Materials", StringTag.class).getValue();
        if (!materials.equals("Alpha")) {
            throw new IllegalArgumentException("Schematic file is not an Alpha schematic");
        }
        byte[] blocks = ChildTagUtil.getChildTag(schematic, "Blocks", ByteArrayTag.class).getValue();
        byte[] blockData = ChildTagUtil.getChildTag(schematic, "Data", ByteArrayTag.class).getValue();
        return new MCEditSchematic(width, length, height, blocks, blockData);
    }

    @Override
    public void paste(Location location) {
        iterate(location, this::pasteIterate);
    }

    @Override
    public void iterate(Location location, SchematicIteration iteration) {
        location.subtract(width / 2.00, height / 2.00, length / 2.00);
        World world = location.getWorld();
        int blockX = location.getBlockX();
        int blockY = location.getBlockY();
        int blockZ = location.getBlockZ();
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < length; ++z) {
                    int index = y * width * length + z * width + x;
                    byte blockValue = blocks[index];
                    byte dataValue = data[index];
                    if (ignoreAir && blockValue == 0) {
                        continue;
                    }

                    Location position = new Location(world,
                        x + blockX,
                        y + blockY,
                        z + blockZ);
                    iteration.iterate(position, blockValue, dataValue);
                }
            }
        }
    }

    protected void pasteIterate(Location location, byte blockValue, byte dataValue) {
        NMSUtil.setBlockFast(location, blockValue, dataValue);
    }

    public MCEditSchematic(short width, short length, short height, byte[] blocks, byte[] data) {
        this.blocks = blocks;
        this.data = data;
        this.width = width;
        this.length = length;
        this.height = height;
    }

    public void ignoreAir(boolean ignoreAir) {
        this.ignoreAir = ignoreAir;
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
    public byte[] getBlocks() {
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