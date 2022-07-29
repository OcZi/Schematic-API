package me.oczi.schematic.utils;

import org.jnbt.Tag;

import java.io.IOException;
import java.util.Map;

public interface ChildTagUtil {

    static <T extends Tag> T getChildTag(Map<String, Tag> items,
                                         String key,
                                         Class<T> expected) throws IOException {
        if (!items.containsKey(key)) {
            throw new IOException("Schematic file need the \"" + key + "\" tag");
        }
        Tag tag = items.get(key);
        if (!expected.isInstance(tag)) {
            throw new IOException(key + " tag is not of tag type " + expected.getName());
        }
        return expected.cast(tag);
    }
}