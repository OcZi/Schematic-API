package me.sintaxis.schematic.utils;

import org.jnbt.Tag;

import java.util.Map;

public interface ChildTagUtil {

    static <T extends Tag> T getChildTag(Map<String, Tag> items, String key, Class<T> expected) {
        if (!items.containsKey(key)) {
            throw new IllegalArgumentException("Schematic file need the \"" + key + "\" tag");
        }
        Tag tag = items.get(key);
        if (!expected.isInstance(tag)) {
            throw new IllegalArgumentException(key + " tag is not of tag type " + expected.getName());
        }
        return expected.cast(tag);
    }
}