package com.jlogicsoftware.kimeria.physics;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/** Port of Dart's {@code GravityMixin}. */
public interface GravityBody {
    Vector2 position();

    Vector2 velocity();

    boolean isOnGround();

    void setOnGround(boolean value);

    default float gravityAcceleration() {
        return 9.8f;
    }

    default float terminalVelocity() {
        return 300f;
    }

    default float jumpForce() {
        return 260f;
    }

    /** Adds gravity to vertical velocity, clamps it, and integrates position.y. */
    default void applyGravity(float dt) {
        Vector2 v = velocity();
        v.y += gravityAcceleration();
        v.y = MathUtils.clamp(v.y, -jumpForce(), terminalVelocity());
        position().y += v.y * dt;
    }

    default void resetVerticalVelocity() {
        velocity().y = 0;
    }
}
