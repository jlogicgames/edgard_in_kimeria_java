package com.jlogicgames.kimeria.entity;

/** Port of Dart's {@code CustomHitbox}: a circle (radius &gt; 0) or a rectangle. */
public class Hitbox {
    public final float offsetX, offsetY, width, height, radius;

    public Hitbox(float offsetX, float offsetY, float width, float height, float radius) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.width = width;
        this.height = height;
        this.radius = radius;
    }

    public static Hitbox rect(float offsetX, float offsetY, float width, float height) {
        return new Hitbox(offsetX, offsetY, width, height, 0);
    }

    public static Hitbox circle(float radius) {
        return new Hitbox(0, 0, 0, 0, radius);
    }
}
