package com.jlogicsoftware.kimeria.tiled;

import java.util.HashMap;
import java.util.Map;

/** One <object> entry from an object-group layer. */
public class TiledObject {
    public int id;
    public String name = "";
    /** Tiled calls this "type" (or "class" in 1.9+); we accept either attribute. */
    public String type = "";
    public float x, y, width, height;
    public final Map<String, String> properties = new HashMap<>();

    public String prop(String key, String fallback) {
        return properties.getOrDefault(key, fallback);
    }

    public float propFloat(String key, float fallback) {
        String v = properties.get(key);
        if (v == null || v.isEmpty()) return fallback;
        try {
            return Float.parseFloat(v);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public boolean propBool(String key, boolean fallback) {
        String v = properties.get(key);
        if (v == null || v.isEmpty()) return fallback;
        return Boolean.parseBoolean(v);
    }
}
