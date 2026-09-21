package com.jlogicsoftware.kimeria.entity;

import com.badlogic.gdx.math.Vector2;

/**
 * Base of every game object with a place in the world. Position is always
 * the TOP-LEFT corner in Y-down world space (matching Flame's
 * {@code anchor: Anchor.topLeft}, used throughout the original source, and
 * the raw Tiled coordinates -- see {@link com.jlogicsoftware.kimeria.KimeriaGame}
 * for why the whole game renders through a Y-down camera).
 */
public abstract class Entity implements WorldObject {
    public final Vector2 position = new Vector2();
    public final Vector2 size = new Vector2();
    /** Mirrors Flame's {@code scale.x > 0}; false means horizontally mirrored. */
    public boolean facingRight = true;
    protected boolean removed = false;

    protected Entity(float x, float y, float w, float h) {
        position.set(x, y);
        size.set(w, h);
    }

    public float getX() {
        return position.x;
    }

    public float getY() {
        return position.y;
    }

    public float getWidth() {
        return size.x;
    }

    public float getHeight() {
        return size.y;
    }

    public float centerX() {
        return position.x + size.x / 2f;
    }

    public float centerY() {
        return position.y + size.y / 2f;
    }

    public void removeFromParent() {
        removed = true;
    }

    @Override
    public boolean isRemoved() {
        return removed;
    }
}
