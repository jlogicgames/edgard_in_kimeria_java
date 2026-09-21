package com.jlogicsoftware.kimeria.tiled;

/**
 * A tiny hand-rolled XML parser, just capable enough for the .tmx/.tsx files
 * this game reads (elements, attributes, plain text content, comments, an
 * optional {@code <?xml ...?>} prolog -- no namespaces, no CDATA, no DTDs).
 *
 * <p>Written instead of using {@code javax.xml.parsers}/{@code org.w3c.dom}
 * specifically so {@code TiledMapData} compiles unchanged for the GWT/web
 * build: GWT's JRE emulation doesn't include {@code javax.xml.parsers} or
 * DOM at all, and pulling in a JDK-only API here would force two separate
 * map-loading implementations (one per platform) with a real risk of the
 * web one drifting from the coordinate semantics the desktop one is careful
 * about (see {@code TiledMapData}'s class doc). Pure {@code String}/{@code
 * char} scanning is portable by construction.
 */
public final class MiniXml {
    private final String src;
    private int pos;

    private MiniXml(String src) {
        this.src = src;
    }

    public static XmlEl parse(String xml) {
        MiniXml p = new MiniXml(xml);
        p.skipProlog();
        return p.parseElement();
    }

    private void skipProlog() {
        skipWhitespace();
        while (pos < src.length()) {
            if (src.startsWith("<?", pos)) {
                pos = src.indexOf("?>", pos) + 2;
            } else if (src.startsWith("<!--", pos)) {
                pos = src.indexOf("-->", pos) + 3;
            } else if (src.startsWith("<!", pos)) {
                pos = src.indexOf(">", pos) + 1;
            } else {
                break;
            }
            skipWhitespace();
        }
    }

    private void skipWhitespace() {
        while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) pos++;
    }

    private XmlEl parseElement() {
        // pos is at '<' of the opening tag.
        pos++; // consume '<'
        int nameStart = pos;
        while (pos < src.length() && !isNameEnd(src.charAt(pos))) pos++;
        XmlEl el = new XmlEl();
        el.tag = src.substring(nameStart, pos);

        parseAttributes(el);

        skipWhitespace();
        if (src.startsWith("/>", pos)) {
            pos += 2;
            return el; // self-closing, no children/text
        }
        if (src.charAt(pos) != '>') {
            throw new IllegalStateException("Malformed XML near position " + pos + ": expected '>'");
        }
        pos++; // consume '>'

        StringBuilder text = new StringBuilder();
        while (true) {
            skipCommentsOnly();
            if (src.startsWith("</", pos)) {
                pos = src.indexOf('>', pos) + 1;
                break;
            }
            if (src.charAt(pos) == '<') {
                el.children.add(parseElement());
            } else {
                int textStart = pos;
                while (pos < src.length() && src.charAt(pos) != '<') pos++;
                text.append(src, textStart, pos);
            }
        }
        el.text = unescape(text.toString());
        return el;
    }

    private void skipCommentsOnly() {
        while (src.startsWith("<!--", pos)) {
            pos = src.indexOf("-->", pos) + 3;
        }
    }

    private void parseAttributes(XmlEl el) {
        while (true) {
            skipWhitespace();
            char c = src.charAt(pos);
            if (c == '/' || c == '>') return;
            int nameStart = pos;
            while (pos < src.length() && src.charAt(pos) != '=' && !Character.isWhitespace(src.charAt(pos))) pos++;
            String name = src.substring(nameStart, pos);
            skipWhitespace();
            if (src.charAt(pos) != '=') {
                throw new IllegalStateException("Malformed XML attribute near position " + pos);
            }
            pos++; // consume '='
            skipWhitespace();
            char quote = src.charAt(pos);
            pos++; // consume opening quote
            int valueStart = pos;
            while (src.charAt(pos) != quote) pos++;
            String value = unescape(src.substring(valueStart, pos));
            pos++; // consume closing quote
            el.attrs.put(name, value);
        }
    }

    private static boolean isNameEnd(char c) {
        return Character.isWhitespace(c) || c == '>' || c == '/';
    }

    private static String unescape(String s) {
        if (s.indexOf('&') < 0) return s;
        StringBuilder sb = new StringBuilder(s.length());
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == '&') {
                int semi = s.indexOf(';', i);
                if (semi > i) {
                    String entity = s.substring(i + 1, semi);
                    String replacement = switch (entity) {
                        case "amp" -> "&";
                        case "lt" -> "<";
                        case "gt" -> ">";
                        case "quot" -> "\"";
                        case "apos" -> "'";
                        default -> entity.startsWith("#") ? decodeNumericEntity(entity) : null;
                    };
                    if (replacement != null) {
                        sb.append(replacement);
                        i = semi + 1;
                        continue;
                    }
                }
            }
            sb.append(c);
            i++;
        }
        return sb.toString();
    }

    private static String decodeNumericEntity(String entity) {
        try {
            int code = entity.startsWith("#x") || entity.startsWith("#X")
                ? Integer.parseInt(entity.substring(2), 16)
                : Integer.parseInt(entity.substring(1));
            return String.valueOf((char) code);
        } catch (NumberFormatException e) {
            return "&" + entity + ";";
        }
    }
}
