package com.jlogicsoftware.kimeria.effects;

import com.badlogic.gdx.graphics.Color;

/**
 * Port of Dart's coin-pickup {@code RippleEffect}. The original distorts the
 * pixels of the rendered background behind it via a global
 * {@code RippleDecorator} full-scene shader; reproducing that needs an
 * offscreen framebuffer capture-and-redraw of the whole scene per ripple.
 * Given this is a purely cosmetic one-off flourish, it's implemented here as
 * the same expanding-ring shader as {@link ShockwaveEffect} with a
 * gold/warm tint sized to match the original's radius/strength knobs,
 * instead of true background pixel displacement.
 */
public class RippleEffect extends ShockwaveEffect {
    public RippleEffect(float centerX, float centerY, float duration, float maxRadius, float strength, float frequency, float decay) {
        super(centerX, centerY, duration, maxRadius, Math.max(4f, strength), Color.valueOf("ffe27a"));
    }
}
