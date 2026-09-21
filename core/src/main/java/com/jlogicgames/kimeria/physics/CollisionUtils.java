package com.jlogicgames.kimeria.physics;

import com.jlogicgames.kimeria.entity.Actor;
import com.jlogicgames.kimeria.entity.Collidable;
import com.jlogicgames.kimeria.entity.Entity;

/** Port of Dart's {@code utils.dart} {@code checkCollision} plus small AABB helpers. */
public final class CollisionUtils {
    private CollisionUtils() {
    }

    public static boolean aabb(float x1, float y1, float w1, float h1,
                                float x2, float y2, float w2, float h2) {
        return x1 < x2 + w2 && x1 + w1 > x2 && y1 < y2 + h2 && y1 + h1 > y2;
    }

    /** Simple entity-vs-entity overlap using the actor's hitbox (facing-aware on X). */
    public static boolean overlaps(Actor<?> actor, Entity other) {
        float hbW = actor.hitbox.radius > 0 ? actor.hitbox.radius * 2 : actor.hitbox.width;
        float hbH = actor.hitbox.radius > 0 ? actor.hitbox.radius * 2 : actor.hitbox.height;
        float ax = actor.position.x + actor.facingAwareOffsetX();
        float ay = actor.position.y + actor.hitbox.offsetY;
        return aabb(ax, ay, hbW, hbH, other.getX(), other.getY(), other.getWidth(), other.getHeight());
    }

    /**
     * Direct port of {@code checkCollision(Actor, Collidable)}: true when the
     * actor's hitbox overlaps a collision block, with the same quicksand /
     * platform special-casing on which edge counts as the actor's "front".
     */
    public static boolean checkCollision(CollideBody actor, Collidable block) {
        var hitbox = actor.hitbox();
        float playerX = actor.position().x + hitbox.offsetX;
        float playerY = actor.position().y + hitbox.offsetY;
        float playerWidth = hitbox.width;
        float playerHeight = hitbox.height;

        float blockX = block.getX();
        float blockY = block.getY();
        float blockWidth = block.getWidth();
        float blockHeight = block.getHeight();

        float fixedX = actor.position().x + actor.facingAwareOffsetX();
        fixedX = block.isQuickSand() ? playerX + hitbox.offsetX : fixedX;

        float fixedY = block.isPlatform() ? playerY + playerHeight : playerY;

        // A one-way platform snaps the actor's feet exactly onto its top
        // edge on contact, zeroing velocity.y. If the platform itself is
        // also moving down (FallingPlatform mid-fall, a vertical Escalator),
        // it can outrun the actor's own next-frame gravity-driven drop
        // (which restarts from zero each time), so the very next frame's
        // exact-contact check fails, the actor free-falls until gravity
        // catches back up, and re-snaps -- a visible stutter/bounce instead
        // of riding smoothly down. This margin keeps contact across a frame
        // or two of that gap. It's only applied once the actor is already
        // nearly at rest (small velocity.y, i.e. it was already standing on
        // something last frame): applying it unconditionally also made a
        // fast landing from an ordinary jump register up to a margin's
        // worth early, snapping the actor sharply upward onto the surface
        // from a few pixels above it -- visible as a one-frame camera/sprite
        // jump right on touchdown.
        boolean atRest = Math.abs(actor.velocity().y) < 20f;
        float catchUpMargin = (block.isPlatform() && atRest) ? 24f : 0f;

        return fixedY < blockY + blockHeight
            && playerY + playerHeight + catchUpMargin > blockY
            && fixedX < blockX + blockWidth
            && fixedX + playerWidth > blockX;
    }
}
