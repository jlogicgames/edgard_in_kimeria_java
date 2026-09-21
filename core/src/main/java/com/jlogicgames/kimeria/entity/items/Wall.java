package com.jlogicgames.kimeria.entity.items;

import com.jlogicgames.kimeria.entity.environment.CollisionBlock;

/** A wall that disappears once a matching {@link Trigger} fires. */
public class Wall extends CollisionBlock implements Actionable {
    private final String targetId;

    public Wall(float x, float y, float w, float h, String targetId) {
        super(x, y, w, h);
        setWall(true);
        this.targetId = targetId;
    }

    @Override
    public String getTargetId() {
        return targetId;
    }

    @Override
    public void performAction() {
        if (!isActive()) return;
        setActive(false);
        removeFromParent();
    }
}
