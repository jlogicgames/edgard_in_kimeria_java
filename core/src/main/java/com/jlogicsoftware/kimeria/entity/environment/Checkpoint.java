package com.jlogicsoftware.kimeria.entity.environment;

import com.jlogicsoftware.kimeria.entity.Entity;

/** End-of-level marker; touching it (handled in {@code Level}) advances to the next level. */
public class Checkpoint extends Entity {
    public Checkpoint(float x, float y, float w, float h) {
        super(x, y, w, h);
    }

    @Override
    public void update(float dt) {
    }
}
