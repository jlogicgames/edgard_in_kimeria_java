package com.jlogicgames.kimeria.entity.items;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.jlogicgames.kimeria.Assets;
import com.jlogicgames.kimeria.GameContext;
import com.jlogicgames.kimeria.effects.RippleEffect;
import com.jlogicgames.kimeria.effects.ShockwaveEffect;
import com.jlogicgames.kimeria.entity.Entity;
import com.jlogicgames.kimeria.entity.WorldObject;

import java.util.function.Consumer;

/** Port of Dart's {@code Collectable}: coins and hearts. */
public class Collectable extends Entity {
    private static final float STEP_TIME = 0.3f;

    private final String name;
    private final GameContext game;
    private final Consumer<WorldObject> spawner;
    private final Animation<TextureRegion> animation;
    private float stateTime = 0f;
    private boolean collected = false;

    public Collectable(Assets assets, GameContext game, Consumer<WorldObject> spawner,
                        String name, float x, float y, float w, float h) {
        super(x, y, w, h);
        this.name = name;
        this.game = game;
        this.spawner = spawner;
        float originY = "Heart".equals(name) ? 16 : 0;
        animation = assets.animation("images/Items.png", 2, STEP_TIME, 16, 16, 0, originY);
        animation.setPlayMode(Animation.PlayMode.LOOP);
    }

    public boolean isCollected() {
        return collected;
    }

    public void collideWithPlayer() {
        if (collected) return;
        collected = true;
        if (game.playSounds()) game.playSound("collect");

        if ("Coin".equals(name)) {
            game.addCoin();
            spawner.accept(new RippleEffect(centerX(), centerY(), 0.75f, 300f, 12f, 60f, 20f));
        }
        if ("Heart".equals(name)) {
            spawner.accept(new ShockwaveEffect(centerX(), centerY(), 0.6f, 64f, 8f));
        }
        removeFromParent();
    }

    @Override
    public void update(float dt) {
        stateTime += dt;
    }

    @Override
    public void render(Batch batch) {
        batch.draw(animation.getKeyFrame(stateTime), position.x, position.y, size.x, size.y);
    }
}
