package com.jlogicsoftware.kimeria.entity;

/** Port of Dart's {@code Collidable} interface, used by the AABB sweep in {@link CollideMixin}. */
public interface Collidable {
    float getX();

    float getY();

    float getWidth();

    float getHeight();

    boolean isQuickSand();

    boolean isPlatform();

    boolean isWall();

    boolean isActive();

    boolean isEscalator();
}
