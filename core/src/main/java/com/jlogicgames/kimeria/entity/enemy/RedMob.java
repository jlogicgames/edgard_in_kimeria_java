package com.jlogicgames.kimeria.entity.enemy;

import com.badlogic.gdx.math.MathUtils;
import com.jlogicgames.kimeria.Assets;
import com.jlogicgames.kimeria.GameContext;
import com.jlogicgames.kimeria.entity.Hitbox;
import com.jlogicgames.kimeria.entity.objects.Escalator;
import com.jlogicgames.kimeria.physics.CollideBody;
import com.jlogicgames.kimeria.world.Level;

/** Port of Dart's {@code RedMob}: like {@link YellowMob} but attacks in melee range instead of just charging. */
public class RedMob extends Enemy<RedMob.State> implements CollideBody {
    public enum State {IDLE, RUN, HIT, ATTACK}

    private static final float STEP_TIME = 0.1f;
    private static final float RUN_SPEED = 80f;
    private static final float BOUNCE_HEIGHT = 260f;
    private static final float ATTACK_RANGE = 65f;

    private Level level;
    private float targetDirection = -1;
    private boolean gotStomped = false;
    private boolean isAttacking = false;
    private float attackTimer = 0f;
    private boolean clambering, inQuickSand;
    private Escalator currentEscalator;

    public RedMob(Assets assets, GameContext game, float x, float y, float w, float h,
                  float offNeg, float offPos) {
        super(assets, game, "Mobs", x, y, w, h, offNeg, offPos, State.class);
        hitbox = Hitbox.rect(10, 6, 14, 26);

        putAnimation(State.IDLE, spriteAnimation(4, STEP_TIME, 48, 32, 0, 32 * 5), true);
        putAnimation(State.RUN, spriteAnimation(4, STEP_TIME, 48, 32, 0, 32), true);
        putAnimation(State.HIT, spriteAnimation(4, STEP_TIME, 48, 32, 0, 32 * 4), false);
        putAnimation(State.ATTACK, spriteAnimation(4, STEP_TIME * 2, 48, 32, 0, 32 * 2), false);
        setState(State.IDLE);

        rangeNeg = x - offNeg * TILE_SIZE;
        rangePos = x + offPos * TILE_SIZE;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    @Override
    public void updateEnemy(float dt) {
        if (gotStomped) return;

        if (isAttacking) {
            attackTimer -= dt;
            if (attackTimer <= 0) {
                isAttacking = false;
                setState(State.IDLE);
                position.x += 300;
            }
        } else {
            updateState();
            movement(dt);
        }
        checkHorizontalCollisions(level);
        applyGravity(dt);
        checkVerticalCollisions(level, dt);
        checkAttackCollision();
    }

    private void movement(float dt) {
        velocity.x = 0;
        float playerOffset = player.facingRight ? 0 : -player.getWidth();
        float mobOffset = facingRight ? 0 : -size.x;

        if (playerInAttackRange()) {
            performAttack();
            return;
        } else if (playerInRange()) {
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

    private boolean playerInAttackRange() {
        float playerOffset = player.facingRight ? 0 : -player.getWidth();
        float playerLeft = player.getX() + playerOffset;
        float playerRight = playerLeft + player.getWidth();
        float mobLeft = position.x - ATTACK_RANGE;
        float mobRight = position.x + ATTACK_RANGE;
        float mobTop = position.y;
        float mobBottom = position.y + size.y;
        return playerLeft >= mobLeft && playerRight <= mobRight
            && player.getY() + player.getHeight() > mobTop && player.getY() < mobBottom;
    }

    private void updateState() {
        setState(velocity.x != 0 ? State.RUN : State.IDLE);
        if ((moveDirection < 0 && facingRight) || (moveDirection > 0 && !facingRight)) {
            facingRight = !facingRight;
        }
    }

    private void performAttack() {
        if (isAttacking || player.isGotHit()) return;
        isAttacking = true;
        setState(State.ATTACK);
        var anim = animations.get(State.ATTACK);
        attackTimer = anim.getAnimationDuration();
    }

    private void checkAttackCollision() {
        if (isAttacking && playerInAttackRange()) {
            player.collidedWithActor(false);
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
