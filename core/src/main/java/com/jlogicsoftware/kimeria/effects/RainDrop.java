package com.jlogicsoftware.kimeria.effects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.jlogicsoftware.kimeria.entity.WorldObject;

import java.util.function.Supplier;

/** Port of Dart's {@code RainDrop}: a falling line particle that respawns above the camera once it lands. */
public class RainDrop implements WorldObject {
    private static Float stableWind;

    private final Vector2 area;
    private final Supplier<Rectangle> visibleWorldRect;
    private final Vector2 start = new Vector2();
    private final Vector2 end = new Vector2();
    private float duration;
    private float elapsed;

    public RainDrop(Vector2 area, Supplier<Rectangle> visibleWorldRect) {
        this.area = area;
        this.visibleWorldRect = visibleWorldRect;
        startRain();
    }

    private void startRain() {
        float xStart = MathUtils.random(area.x) + visibleWorldRect.get().x;
        start.set(xStart, -20f);
        if (stableWind == null) stableWind = MathUtils.random(-24f, 24f);
        end.set(start.x + stableWind, area.y + 40f);
        float fallDistance = end.y - start.y;
        float speed = 400f + MathUtils.random(80f);
        duration = fallDistance / speed;
        elapsed = 0f;
    }

    @Override
    public void update(float dt) {
        elapsed += dt;
        if (elapsed >= duration) {
            startRain();
        }
    }

    @Override
    public void render(Batch batch) {
        float t = MathUtils.clamp(elapsed / duration, 0f, 1f);
        float x = MathUtils.lerp(start.x, end.x, t);
        float y = MathUtils.lerp(start.y, end.y, t);
        batch.setColor(Color.BLACK);
        batch.draw(Shaders.whiteQuad(), x - 0.6f, y - 7f, 1.2f, 14f);
        batch.setColor(Color.WHITE);
    }
}
