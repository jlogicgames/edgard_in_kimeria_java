package com.jlogicsoftware.kimeria.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.jlogicsoftware.kimeria.effects.FogEffect;
import com.jlogicsoftware.kimeria.effects.Firefly;

import java.util.ArrayList;
import java.util.List;

/**
 * Port of the Rust menu's fog + gold "MenuFirefly" backdrop (see
 * {@code ui.rs}'s {@code sync_menu_fog}/{@code sync_menu_fireflies}): drawn
 * behind the Main Menu/About/Options screens, reusing the same
 * {@link FogEffect}/{@link Firefly} shader effects gameplay uses.
 */
public class MenuBackdrop {
    private static final Rectangle FIXED_RECT = new Rectangle(0, 0, 640, 360);

    private final FogEffect fog = new FogEffect(() -> FIXED_RECT);
    private final List<Firefly> fireflies = new ArrayList<>();

    public MenuBackdrop() {
        Vector2 area = new Vector2(FIXED_RECT.width, FIXED_RECT.height);
        for (int i = 0; i < 18; i++) {
            fireflies.add(new Firefly(area, Color.valueOf("ffcc33")));
        }
    }

    public void update(float dt) {
        fog.update(dt);
        for (Firefly f : fireflies) f.update(dt);
    }

    public void render(Batch batch) {
        for (Firefly f : fireflies) f.render(batch);
        fog.render(batch);
    }
}
