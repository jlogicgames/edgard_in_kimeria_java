package com.jlogicgames.kimeria.tiled;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.jlogicgames.kimeria.Assets;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal, dependency-free TMX/TSX reader.
 *
 * <p>Deliberately hand-rolled instead of using gdx's Tiled extension: this
 * keeps every coordinate exactly as authored in the .tmx file (top-left
 * origin, Y grows downward) with zero row-flipping, which is what the
 * original Flame ({@code flame_tiled}) and Bevy ({@code bevy_ecs_tiled})
 * versions both do. The whole game renders through a Y-down camera
 * (see {@link com.jlogicgames.kimeria.KimeriaGame}) specifically so that
 * these coordinates can be used verbatim.
 */
public class TiledMapData {
    public int widthTiles, heightTiles, tileWidth, tileHeight;
    /** [row][col] gid, row 0 = top row of the .tmx file, 0 = empty. */
    public int[][] backgroundGrid;
    public final Map<String, List<TiledObject>> objectGroups = new HashMap<>();

    private TextureRegion[] tileRegions; // index by (gid - firstgid)
    private int firstGid;

    public float widthPx() {
        return widthTiles * tileWidth;
    }

    public float heightPx() {
        return heightTiles * tileHeight;
    }

    public List<TiledObject> layer(String name) {
        return objectGroups.getOrDefault(name, List.of());
    }

    public TextureRegion tile(int gid) {
        if (gid <= 0) return null;
        int index = gid - firstGid;
        if (tileRegions == null || index < 0 || index >= tileRegions.length) return null;
        return tileRegions[index];
    }

    public static TiledMapData load(String tmxPath, Assets assets) {
        try {
            TiledMapData data = new TiledMapData();
            FileHandle tmxFile = Gdx.files.internal(tmxPath);
            Document doc = parse(tmxFile);
            Element mapEl = doc.getDocumentElement();
            data.widthTiles = intAttr(mapEl, "width", 0);
            data.heightTiles = intAttr(mapEl, "height", 0);
            data.tileWidth = intAttr(mapEl, "tilewidth", 16);
            data.tileHeight = intAttr(mapEl, "tileheight", 16);
            data.backgroundGrid = new int[data.heightTiles][data.widthTiles];

            String tsxSource = null;
            for (Element ts : children(mapEl, "tileset")) {
                data.firstGid = intAttr(ts, "firstgid", 1);
                tsxSource = ts.getAttribute("source");
            }
            if (tsxSource != null && !tsxSource.isEmpty()) {
                String dir = tmxFile.parent().path();
                FileHandle tsxFile = Gdx.files.internal(dir + "/" + tsxSource);
                loadTileset(tsxFile, data, assets);
            }

            for (Element layerEl : children(mapEl, "layer")) {
                Element dataEl = firstChild(layerEl, "data");
                if (dataEl == null) continue;
                String csv = dataEl.getTextContent().trim();
                String[] cells = csv.split("\\s*,\\s*");
                int i = 0;
                for (int r = 0; r < data.heightTiles; r++) {
                    for (int c = 0; c < data.widthTiles; c++) {
                        if (i < cells.length && !cells[i].isEmpty()) {
                            data.backgroundGrid[r][c] = Integer.parseInt(cells[i].trim());
                        }
                        i++;
                    }
                }
            }

            for (Element groupEl : children(mapEl, "objectgroup")) {
                String groupName = groupEl.getAttribute("name");
                List<TiledObject> objects = new ArrayList<>();
                for (Element objEl : children(groupEl, "object")) {
                    TiledObject obj = new TiledObject();
                    obj.id = intAttr(objEl, "id", 0);
                    obj.name = objEl.getAttribute("name");
                    obj.type = objEl.hasAttribute("type") ? objEl.getAttribute("type") : objEl.getAttribute("class");
                    obj.x = floatAttr(objEl, "x", 0);
                    obj.y = floatAttr(objEl, "y", 0);
                    obj.width = floatAttr(objEl, "width", 0);
                    obj.height = floatAttr(objEl, "height", 0);
                    Element propsEl = firstChild(objEl, "properties");
                    if (propsEl != null) {
                        for (Element propEl : children(propsEl, "property")) {
                            obj.properties.put(propEl.getAttribute("name"), propEl.getAttribute("value"));
                        }
                    }
                    objects.add(obj);
                }
                data.objectGroups.put(groupName, objects);
            }

            return data;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load TMX: " + tmxPath, e);
        }
    }

    private static void loadTileset(FileHandle tsxFile, TiledMapData data, Assets assets) throws Exception {
        Document doc = parse(tsxFile);
        Element tsEl = doc.getDocumentElement();
        int tileWidth = intAttr(tsEl, "tilewidth", data.tileWidth);
        int tileHeight = intAttr(tsEl, "tileheight", data.tileHeight);
        int columns = intAttr(tsEl, "columns", 1);
        int tileCount = intAttr(tsEl, "tilecount", 0);
        Element imageEl = firstChild(tsEl, "image");
        String imageSource = imageEl.getAttribute("source");
        String dir = tsxFile.parent().path();
        String imagePath = normalize(dir + "/" + imageSource);

        data.tileRegions = new TextureRegion[tileCount];
        for (int i = 0; i < tileCount; i++) {
            int col = i % columns;
            int row = i / columns;
            data.tileRegions[i] = assets.region(imagePath, col * tileWidth, row * tileHeight, tileWidth, tileHeight);
        }
    }

    private static String normalize(String path) {
        // Collapse "assets/tiles/../images/x.png" style paths coming from relative TSX sources.
        List<String> parts = new ArrayList<>();
        for (String part : path.split("/")) {
            if (part.equals("..")) {
                if (!parts.isEmpty()) parts.remove(parts.size() - 1);
            } else if (!part.equals(".") && !part.isEmpty()) {
                parts.add(part);
            }
        }
        return String.join("/", parts);
    }

    private static Document parse(FileHandle file) throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(file.read());
    }

    private static List<Element> children(Element parent, String tag) {
        List<Element> result = new ArrayList<>();
        NodeList list = parent.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node n = list.item(i);
            if (n instanceof Element && n.getNodeName().equals(tag)) {
                result.add((Element) n);
            }
        }
        return result;
    }

    private static Element firstChild(Element parent, String tag) {
        List<Element> found = children(parent, tag);
        return found.isEmpty() ? null : found.get(0);
    }

    private static int intAttr(Element el, String name, int fallback) {
        String v = el.getAttribute(name);
        return v.isEmpty() ? fallback : Integer.parseInt(v);
    }

    private static float floatAttr(Element el, String name, float fallback) {
        String v = el.getAttribute(name);
        return v.isEmpty() ? fallback : Float.parseFloat(v);
    }
}
