package com.jlogicsoftware.kimeria.tiled;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** A parsed XML element: tag name, attributes, child elements, and its own direct text content. */
public class XmlEl {
    public String tag = "";
    public final Map<String, String> attrs = new LinkedHashMap<>();
    public final List<XmlEl> children = new ArrayList<>();
    public String text = "";

    public String attr(String name, String fallback) {
        String v = attrs.get(name);
        return v == null ? fallback : v;
    }

    public boolean hasAttr(String name) {
        return attrs.containsKey(name);
    }

    public List<XmlEl> children(String tagName) {
        List<XmlEl> result = new ArrayList<>();
        for (XmlEl c : children) {
            if (c.tag.equals(tagName)) result.add(c);
        }
        return result;
    }

    public XmlEl firstChild(String tagName) {
        for (XmlEl c : children) {
            if (c.tag.equals(tagName)) return c;
        }
        return null;
    }
}
