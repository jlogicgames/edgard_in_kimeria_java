package com.jlogicsoftware.kimeria.effects;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.jlogicsoftware.kimeria.entity.Entity;

/** Port of Dart's {@code BombExplosionEffect}. */
public class BombExplosionEffect extends Entity {
    private final float duration;
    private float elapsed = 0f;

    public BombExplosionEffect(float centerX, float centerY, float size, float duration) {
        super(centerX - size / 2f, centerY - size / 2f, size, size);
        this.duration = duration;
    }

    @Override
    public void update(float dt) {
        elapsed += dt;
        if (elapsed >= duration) removeFromParent();
    }

    @Override
    public void render(Batch batch) {
        float progress = MathUtils.clamp(elapsed / duration, 0f, 1f);
        ShaderProgram shader = Shaders.load("bomb_explosion.frag");
        ShaderProgram previous = batch.getShader();
        batch.setShader(shader);
        shader.setUniformf("uSize", size.x, size.y);
        shader.setUniformf("uTime", elapsed);
        shader.setUniformf("uProgress", progress);
        batch.draw(Shaders.whiteQuad(), position.x, position.y, size.x, size.y);
        batch.setShader(previous);
    }
}
