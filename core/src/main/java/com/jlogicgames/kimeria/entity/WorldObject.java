package com.jlogicgames.kimeria.entity;

import com.badlogic.gdx.graphics.g2d.Batch;

/**
 * The tree of things a {@code Level} owns and drives, mirroring Flame's
 * {@code Component} tree ({@code add()}/{@code update()}/{@code render()}
 * /{@code removeFromParent()}) closely enough that the original Dart logic
 * translates near line-for-line.
 */
public interface WorldObject {
    void update(float dt);

    default void render(Batch batch) {
    }

    /** Equivalent of Flame's {@code removeFromParent()} having been called. */
    default boolean isRemoved() {
        return false;
    }
}
