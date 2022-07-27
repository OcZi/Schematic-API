package me.sintaxis.schematic;

import me.sintaxis.schematic.utils.ChildTagUtil;
import me.sintaxis.schematic.utils.NMSUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
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

    private final short width;
    private final short length;
    private final short height;
    private final byte[] blocks;
    private final byte[] data;

    public static void parseAndPaste(Location location, File file) throws IOException {
        MCEditSchematic.from(file).paste(location);
    }

    public static MCEditSchematic from(File file) throws IOException {
        CompoundTag schematicTag;
        try (FileInputStream stream = new FileInputStream(file)) {
            try (NBTInputStream nbtStream = new NBTInputStream(stream)){
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
        location.subtract(width / 2.00, height / 2.00, length / 2.00);
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < length; ++z) {
                    int index = y * width * length + z * width + x;
                    Block block = new Location(location.getWorld(), x + location.getX(), y + location.getY(), z + location.getZ()).getBlock();
                    NMSUtil.setBlockFast(block, blocks[index], data[index]);
                }
            }
        }
    }

    public MCEditSchematic(short width, short length, short height, byte[] blocks, byte[] data) {
        this.blocks = blocks;
        this.data = data;
        this.width = width;
        this.length = length;
        this.height = height;
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