package com.jlogicsoftware.kimeria.entity;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.EnumMap;
import java.util.Map;

/**
 * Port of Dart's {@code Actor}: a {@code SpriteAnimationGroupComponent} with
 * a hitbox that may differ from the drawn sprite bounds.
 *
 * <p>Flame's {@code animationTicker.completed} future (awaited all over the
 * original code to sequence things after a one-shot animation finishes) has
 * no synchronous equivalent, so subclasses instead poll
 * {@link #isCurrentAnimationFinished()} once per frame from their own
 * update() to drive their phase state machines -- see {@code Player} for
 * the respawn/checkpoint sequences.
 *
 * @param <S> the actor's state enum (idle/running/... etc.)
 */
public abstract class Actor<S extends Enum<S>> extends Entity {
    public Hitbox hitbox = Hitbox.rect(0, 0, 0, 0);
    protected final Map<S, Animation<TextureRegion>> animations;
    protected S current;
    protected float stateTime = 0f;

    protected Actor(float x, float y, float w, float h, Class<S> stateType) {
        super(x, y, w, h);
        animations = new EnumMap<>(stateType);
    }

    protected void putAnimation(S state, Animation<TextureRegion> animation, boolean loop) {
        animation.setPlayMode(loop ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL);
        animations.put(state, animation);
    }

    public void setState(S state) {
        if (state != current) {
            current = state;
            stateTime = 0f;
        }
    }

    public S getState() {
        return current;
    }

    protected void tickAnimation(float dt) {
        stateTime += dt;
    }

    public boolean isCurrentAnimationFinished() {
        Animation<TextureRegion> anim = animations.get(current);
        return anim == null || anim.isAnimationFinished(stateTime);
    }

    /**
     * Horizontal hitbox offset, adjusted for facing direction: the hitbox is
     * authored for the unflipped sprite, and mirrors around the sprite
     * center when {@link #facingRight} is false.
     */
    public float facingAwareOffsetX() {
        return facingRight ? hitbox.offsetX : size.x - hitbox.offsetX - hitbox.width;
    }

    public void collidedWithActor(boolean gotHit) {
    }

    @Override
    public void render(Batch batch) {
        Animation<TextureRegion> anim = animations.get(current);
        if (anim == null) return;
        TextureRegion frame = anim.getKeyFrame(stateTime);
        float w = facingRight ? size.x : -size.x;
        float x = facingRight ? position.x : position.x + size.x;
        batch.draw(frame, x, position.y, w, size.y);
    }
}
