package com.jlogicsoftware.kimeria;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Disposable;

import java.util.HashMap;
import java.util.Map;

/**
 * Central texture/sound cache. Every texture is loaded once and every
 * {@link TextureRegion} handed out is pre-flipped vertically, because the
 * whole game renders through a Y-down camera (see {@link KimeriaGame}) to
 * keep world-space math identical to the original Flame/Bevy sources, where
 * (0,0) is the top-left corner and Y grows downward.
 */
public class Assets implements Disposable {
    private final Map<String, Texture> textures = new HashMap<>();
    private final Map<String, Sound> sounds = new HashMap<>();
    private final Map<String, BitmapFont> fonts = new HashMap<>();

    public Texture texture(String path) {
        return textures.computeIfAbsent(path, p -> {
            Texture t = new Texture(Gdx.files.internal(p));
            t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            return t;
        });
    }

    public Sound sound(String path) {
        return sounds.computeIfAbsent(path, p -> Gdx.audio.newSound(Gdx.files.internal(p)));
    }

    /** A single frame region, flipped for the Y-down camera. */
    public TextureRegion region(String path, float x, float y, float w, float h) {
        TextureRegion region = new TextureRegion(texture(path), (int) x, (int) y, (int) w, (int) h);
        region.flip(false, true);
        return region;
    }

    /**
     * Builds a frame-sequenced animation exactly like Flame's
     * {@code SpriteAnimationData.sequenced}: frames run left-to-right from
     * {@code (originX, originY)} and wrap to a new row (advancing by
     * frameHeight) after {@code amountPerRow} frames.
     */
    public Animation<TextureRegion> animation(String path, int amount, float stepTime,
                                               float frameW, float frameH,
                                               float originX, float originY, int amountPerRow) {
        TextureRegion[] frames = new TextureRegion[amount];
        for (int i = 0; i < amount; i++) {
            int col = i % amountPerRow;
            int row = i / amountPerRow;
            frames[i] = region(path, originX + col * frameW, originY + row * frameH, frameW, frameH);
        }
        return new Animation<>(stepTime, frames);
    }

    public Animation<TextureRegion> animation(String path, int amount, float stepTime,
                                               float frameW, float frameH,
                                               float originX, float originY) {
        int amountPerRow = amount > 4 ? 4 : amount;
        return animation(path, amount, stepTime, frameW, frameH, originX, originY, amountPerRow);
    }

    /**
     * Every character the UI ever draws, in either language: FreeType only
     * bakes glyphs it's told about ({@link FreeTypeFontGenerator#DEFAULT_CHARS}
     * is Latin-only), so without this the Ukrainian strings in
     * {@code localization.Msg} would render as tofu/blank.
     */
    private static final String CHARSET = FreeTypeFontGenerator.DEFAULT_CHARS + buildCyrillicRange();

    private static String buildCyrillicRange() {
        StringBuilder sb = new StringBuilder();
        for (char c = 0x0400; c <= 0x04FF; c++) sb.append(c);
        return sb.toString();
    }

    public BitmapFont font(int sizePx) {
        return fonts.computeIfAbsent("QuestSquare@" + sizePx, key -> {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/QuestSquare.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
            param.size = sizePx;
            param.characters = CHARSET;
            // `mono` (1-bit, no anti-aliasing) looked crisp but let strokes
            // of adjacent letters touch/merge at these small sizes -- this
            // font's hinting isn't built for a bilevel rasterizer. Regular
            // anti-aliasing plus Nearest (not Linear) texture filtering is
            // the middle ground: edges are soft like normal text, but
            // Nearest stops the extra blur Linear added on top when the
            // logical 640x360 canvas is scaled up to the window.
            param.hinting = FreeTypeFontGenerator.Hinting.Full;
            param.minFilter = Texture.TextureFilter.Nearest;
            param.magFilter = Texture.TextureFilter.Nearest;
            BitmapFont font = generator.generateFont(param);
            generator.dispose();
            // Deliberately NOT flipped like region(): a BitmapFont's glyph
            // quads are built from Glyph.u/v/u2/v2 baked at generation time,
            // not from the page TextureRegion object, so flipping that
            // region has no effect on what's actually drawn -- flipping a
            // font for a Y-down camera needs a real UV rewrite, not this
            // trick. Text is instead drawn through a separate Y-up camera
            // pass (see Overlay/Hud), where an unmodified font is correct
            // by construction.
            return font;
        });
    }

    @Override
    public void dispose() {
        textures.values().forEach(Texture::dispose);
        sounds.values().forEach(Sound::dispose);
        fonts.values().forEach(BitmapFont::dispose);
        textures.clear();
        sounds.clear();
        fonts.clear();
    }
}
