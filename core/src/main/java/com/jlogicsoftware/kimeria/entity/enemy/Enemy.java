package com.jlogicsoftware.kimeria.entity.enemy;

import com.badlogic.gdx.math.Vector2;
import com.jlogicsoftware.kimeria.Assets;
import com.jlogicsoftware.kimeria.GameContext;
import com.jlogicsoftware.kimeria.entity.Actor;
import com.jlogicsoftware.kimeria.entity.player.Player;
import com.jlogicsoftware.kimeria.physics.GravityBody;

/** Port of Dart's {@code Enemy} base class. */
public abstract class Enemy<S extends Enum<S>> extends Actor<S> implements GravityBody {
    protected static final float TILE_SIZE = 16f;

    protected final Assets assets;
    protected final GameContext game;
    protected final String spriteName;
    protected final float offNeg, offPos;
    public final Vector2 velocity = new Vector2();
    private boolean onGround = false;
    protected float rangeNeg, rangePos;
    protected float moveDirection = 0;
    protected Player player;
    /** Set true once a hit animation starts; removes the enemy once it finishes playing. */
    protected boolean pendingRemoval = false;

    protected Enemy(Assets assets, GameContext game, String spriteName, float x, float y, float w, float h,
                     float offNeg, float offPos, Class<S> stateType) {
        super(x, y, w, h, stateType);
        this.assets = assets;
        this.game = game;
        this.spriteName = spriteName;
        this.offNeg = offNeg;
        this.offPos = offPos;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    protected com.badlogic.gdx.graphics.g2d.Animation<com.badlogic.gdx.graphics.g2d.TextureRegion> spriteAnimation(
        int amount, float stepTime, float frameW, float frameH, float originX, float originY) {
        return assets.animation("images/enemy/" + spriteName + ".png", amount, stepTime, frameW, frameH, originX, originY);
    }

    @Override
    public Vector2 position() {
        return position;
    }

    @Override
    public Vector2 velocity() {
        return velocity;
    }

    @Override
    public boolean isOnGround() {
        return onGround;
    }

    @Override
    public void setOnGround(boolean value) {
        onGround = value;
    }

    /** Subclasses implement their own AI/physics tick here. */
    public abstract void updateEnemy(float dt);

    @Override
    public void update(float dt) {
        if (!game.isGameStarted()) return;

        if (pendingRemoval) {
            tickAnimation(dt);
            if (isCurrentAnimationFinished()) {
                removeFromParent();
            }
            return;
        }
        updateEnemy(dt);
        tickAnimation(dt);
    }
}
