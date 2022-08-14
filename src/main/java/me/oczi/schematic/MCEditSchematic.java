package me.oczi.schematic;

import me.oczi.schematic.utils.NMSUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.jnbt.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Map;

import static me.oczi.schematic.utils.ChildTagUtil.getChildTag;

/**
 * Implementation of {@link Schematic} with MCEdit format.
 */
public class MCEditSchematic implements Schematic {

    protected final Vector size;
    protected final Vector offset;
    protected final Vector origin;
    protected final short[] blocks;
    protected final byte[] data;

    protected boolean ignoreAir;

    public static void parseAndPaste(Location location, File file) throws IOException {
        new MCEditSchematic(file).paste(location);
    }

    public MCEditSchematic(File file) throws IOException {
        this(Files.newInputStream(file.toPath()));
    }

    public MCEditSchematic(InputStream stream) throws IOException {
        CompoundTag schematicTag;
        try (NBTInputStream nbtStream = new NBTInputStream(stream)) {
            schematicTag = (CompoundTag) nbtStream.readTag();
        } finally {
            stream.close();
        }

        Map<String, Tag> schematic = schematicTag.getValue();
        short width = getChildTag(schematic, "Width", ShortTag.class).getValue();
        short height = getChildTag(schematic, "Height", ShortTag.class).getValue();
        short length = getChildTag(schematic, "Length", ShortTag.class).getValue();
        Vector size = new Vector(width, height, length);
        Vector offset;
        Vector origin;

        try {
            int originX = getChildTag(schematic, "WEOriginX", IntTag.class).getValue();
            int originY = getChildTag(schematic, "WEOriginY", IntTag.class).getValue();
            int originZ = getChildTag(schematic, "WEOriginZ", IntTag.class).getValue();
            origin = new Vector(originX, originY, originZ);

            int offsetX = getChildTag(schematic, "WEOffsetX", IntTag.class).getValue();
            int offsetY = getChildTag(schematic, "WEOffsetY", IntTag.class).getValue();
            int offsetZ = getChildTag(schematic, "WEOffsetZ", IntTag.class).getValue();
            offset = new Vector(offsetX, offsetY, offsetZ);
        } catch (IOException ignored) {
            origin = new Vector(0, 0, 0);
            offset = origin.clone();
        }
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
                short left = (short) ((index & 1) == 0
                    ? (addId[index >> 1] & 0x0F) << 8
                    : (addId[index >> 1] & 0xF0) << 4);
                blocks[index] = (short) (left + (blocksId[index] & 255));
            }
        }

        this.blocks = blocks;
        this.data = blockData;
        this.size = size;
        this.offset = offset;
        this.origin = origin;
    }

    public MCEditSchematic(Vector size, Vector offset, Vector origin, short[] blocks, byte[] data) {
        this.blocks = blocks;
        this.data = data;
        this.size = size;
        this.offset = offset;
        this.origin = origin;
    }

    @Override
    public void paste(Location location) {
        iterate(location, this::pasteIterate);
    }

    @Override
    public void iterate(Location location, SchematicWorldIteration iteration) {
        int width = this.size.getBlockX();
        int height = this.size.getBlockY();
        int length = this.size.getBlockZ();

        location = location.clone().add(offset);
        // location.subtract(width / 2.00, height / 2.00, length / 2.00);
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
        int width = this.size.getBlockX();
        int height = this.size.getBlockY();
        int length = this.size.getBlockZ();
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < length; ++z) {
                    int index = y * width * length + z * width + x;
                    short blockValue = blocks[index];
                    if (ignoreAir && blockValue == 0) {
                        continue;
                    }
                    byte dataValue = data[index];

                    Vector position = new Vector(x, y, z);
                    iteration.iterate(position, blockValue, dataValue);
                }
            }
        }
    }

    @Override
    public void setIgnoreAir(boolean ignoreAir) {
        this.ignoreAir = ignoreAir;
    }

    protected void pasteIterate(Location location, int blockValue, byte dataValue) {
        NMSUtil.setBlockFast(location, blockValue, dataValue);
    }

    @Override
    public Vector getSize() {
        return size.clone();
    }

    @Override
    public Vector getOffset() {
        return offset.clone();
    }

    @Override
    public Vector getOrigin() {
        return origin.clone();
    }

    @Override
    public boolean isIgnoreAir() {
        return ignoreAir;
    }

    @Override
    public int getWidth() {
        return size.getBlockX();
    }

    @Override
    public int getHeight() {
        return size.getBlockY();
    }

    @Override
    public int getLength() {
        return size.getBlockZ();
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
            "size=" + size +
            ", offset=" + offset +
            ", origin=" + origin +
            ", blocks=" + Arrays.toString(blocks) +
            ", data=" + Arrays.toString(data) +
            ", ignoreAir=" + ignoreAir +
            '}';
    }
}