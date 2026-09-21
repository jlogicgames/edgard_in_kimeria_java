package com.jlogicsoftware.kimeria.entity.enemy;

import com.badlogic.gdx.math.MathUtils;
import com.jlogicsoftware.kimeria.Assets;
import com.jlogicsoftware.kimeria.GameContext;
import com.jlogicsoftware.kimeria.entity.Hitbox;
import com.jlogicsoftware.kimeria.entity.objects.Escalator;
import com.jlogicsoftware.kimeria.physics.CollideBody;
import com.jlogicsoftware.kimeria.world.Level;

/** Port of Dart's {@code YellowMob}: a ground patrol enemy that charges the player when in range. */
public class YellowMob extends Enemy<YellowMob.State> implements CollideBody {
    public enum State {IDLE, RUN, HIT}

    private static final float STEP_TIME = 0.05f;
    private static final float RUN_SPEED = 80f;
    private static final float BOUNCE_HEIGHT = 260f;

    private float targetDirection = -1;
    private boolean gotStomped = false;
    private boolean clambering, wallJumping, inQuickSand;
    private Escalator currentEscalator;
    private Level level;

    public YellowMob(Assets assets, GameContext game, float x, float y, float w, float h,
                      float offNeg, float offPos) {
        super(assets, game, "yellow_mob", x, y, w, h, offNeg, offPos, State.class);
        hitbox = Hitbox.rect(10, 6, 14, 26);

        putAnimation(State.IDLE, spriteAnimation(4, STEP_TIME, 48, 32, 0, 32 * 5), true);
        putAnimation(State.RUN, spriteAnimation(4, STEP_TIME, 48, 32, 0, 32), true);
        putAnimation(State.HIT, spriteAnimation(4, STEP_TIME, 48, 32, 0, 32 * 4), false);
        setState(State.IDLE);

        rangeNeg = x - offNeg * TILE_SIZE;
        rangePos = x + offPos * TILE_SIZE;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    @Override
    public void updateEnemy(float dt) {
        if (!gotStomped) {
            updateState();
            movement(dt);
            checkHorizontalCollisions(level);
            applyGravity(dt);
            checkVerticalCollisions(level, dt);
        }
    }

    private void movement(float dt) {
        velocity.x = 0;
        float playerOffset = player.facingRight ? 0 : -player.getWidth();
        float mobOffset = facingRight ? 0 : -size.x;

        if (playerInRange()) {
            targetDirection = (player.getX() + playerOffset < position.x + mobOffset) ? -1 : 1;
            velocity.x = targetDirection * RUN_SPEED;
        }

        moveDirection = MathUtils.lerp(moveDirection, targetDirection, 0.1f);
        position.x += velocity.x * dt;
    }

    private boolean playerInRange() {
        float playerOffset = player.facingRight ? 0 : -player.getWidth();
        return player.getX() + playerOffset >= rangeNeg
            && player.getX() + playerOffset <= rangePos
            && player.getY() + player.getHeight() > position.y
            && player.getY() < position.y + size.y;
    }

    private void updateState() {
        setState(velocity.x != 0 ? State.RUN : State.IDLE);
        if ((moveDirection < 0 && facingRight) || (moveDirection > 0 && !facingRight)) {
            facingRight = !facingRight;
        }
    }

    @Override
    public void collidedWithActor(boolean gotHit) {
        boolean stompedFromAbove = player.velocity.y > 0 && player.getY() + player.getHeight() > position.y;
        if (gotHit || stompedFromAbove) {
            if (game.playSounds()) game.playSound("bounce");
            gotStomped = true;
            setState(State.HIT);
            if (!gotHit) player.velocity.y = -BOUNCE_HEIGHT;
            pendingRemoval = true;
        } else {
            player.collidedWithActor(false);
        }
    }

    @Override
    public Hitbox hitbox() {
        return hitbox;
    }

    @Override
    public float facingAwareOffsetX() {
        return super.facingAwareOffsetX();
    }

    @Override
    public boolean isClambering() {
        return clambering;
    }

    @Override
    public void setClambering(boolean value) {
        clambering = value;
    }

    @Override
    public boolean isInQuickSand() {
        return inQuickSand;
    }

    @Override
    public void setInQuickSand(boolean value) {
        inQuickSand = value;
    }

    @Override
    public Escalator currentEscalator() {
        return currentEscalator;
    }

    @Override
    public void setCurrentEscalator(Escalator escalator) {
        currentEscalator = escalator;
    }
}
