package pixelbattle.game.entities;

import pixelbattle.game.*;
import java.util.ArrayList;
import java.util.List;

public class Player {
    public double x, y;
    public double vx, vy;
    public double health;
    public double shield;
    public double speed;
    public double attackPower;
    public double defense;
    public double miningSpeed;
    public boolean isFlying;
    public boolean isDead;
    public boolean onGround;
    public boolean wasOnGround;
    public List<Item> inventory;
    public int selectedSlot;
    public int killCount;
    public double invincibleTimer;
    public double lavaDamageTimer;
    public boolean facingRight;

    private boolean moveLeft = false;
    private boolean moveRight = false;
    private boolean wantJump = false;

    public Player(double x, double y) {
        this.x = x;
        this.y = y;
        this.vx = 0;
        this.vy = 0;
        this.health = GameConstants.MAX_HEALTH;
        this.shield = 0;
        this.speed = 1.0;
        this.attackPower = 1.0;
        this.defense = 0;
        this.miningSpeed = 3.0;
        this.isFlying = false;
        this.isDead = false;
        this.onGround = false;
        this.wasOnGround = false;
        this.inventory = new ArrayList<>();
        this.selectedSlot = 0;
        this.killCount = 0;
        this.invincibleTimer = 0;
        this.lavaDamageTimer = 0;
        this.facingRight = true;

        inventory.add(new Item(ItemType.WOODEN_PICKAXE, 1));
        inventory.add(new Item(ItemType.WOODEN_SWORD, 1));
        inventory.add(new Item(ItemType.GRAPPLING_HOOK, 1));
        inventory.add(new Item(ItemType.WOOD, 20));
        inventory.add(new Item(ItemType.STRING, 5));
    }

    public void setMoveLeft(boolean v) { moveLeft = v; if (v) facingRight = false; }
    public void setMoveRight(boolean v) { moveRight = v; if (v) facingRight = true; }
    public void setWantJump(boolean v) { wantJump = v; }
    public boolean isMoveLeft() { return moveLeft; }
    public boolean isMoveRight() { return moveRight; }
    public boolean isWantJump() { return wantJump; }

    public boolean justLanded() {
        return onGround && !wasOnGround;
    }

    public void updateGroundState() {
        wasOnGround = onGround;
    }

    public void takeDamage(double amount) {
        if (invincibleTimer > 0) return;
        double actualDamage = amount * (1 - defense);
        if (shield > 0) {
            double shieldAbsorb = Math.min(shield, actualDamage);
            shield -= shieldAbsorb;
            actualDamage -= shieldAbsorb;
        }
        health -= actualDamage;
        if (health <= 0) {
            health = 0;
            isDead = true;
        }
        invincibleTimer = 0.5;
    }

    public void heal(double amount) {
        health = Math.min(GameConstants.MAX_HEALTH, health + amount);
    }

    public void respawn(double x, double y) {
        this.x = x;
        this.y = y;
        this.vx = 0;
        this.vy = 0;
        this.health = GameConstants.MAX_HEALTH;
        this.shield = 0;
        this.isDead = false;
        this.invincibleTimer = 3.0;
        this.onGround = false;
    }

    public Item getSelectedItem() {
        if (selectedSlot >= 0 && selectedSlot < inventory.size()) {
            return inventory.get(selectedSlot);
        }
        return null;
    }

    public boolean hasItem(ItemType type) {
        for (Item item : inventory) {
            if (item.type == type && item.count > 0) return true;
        }
        return false;
    }

    public int getItemCount(ItemType type) {
        for (Item item : inventory) {
            if (item.type == type) return item.count;
        }
        return 0;
    }

    public void addItem(ItemType type, int count) {
        for (Item item : inventory) {
            if (item.type == type) {
                item.count += count;
                return;
            }
        }
        inventory.add(new Item(type, count));
    }

    public void removeItem(ItemType type, int count) {
        for (int i = inventory.size() - 1; i >= 0; i--) {
            Item item = inventory.get(i);
            if (item.type == type) {
                item.count -= count;
                if (item.count <= 0) {
                    inventory.remove(i);
                    if (selectedSlot >= inventory.size()) {
                        selectedSlot = Math.max(0, inventory.size() - 1);
                    }
                }
                return;
            }
        }
    }

    public void updateTimers(double dt) {
        if (invincibleTimer > 0) invincibleTimer -= dt;
        if (lavaDamageTimer > 0) lavaDamageTimer -= dt;
    }

    public double getMiningSpeed() {
        Item selected = getSelectedItem();
        if (selected != null) {
            GameConstants.ItemProps props = GameConstants.ITEM_PROPERTIES.get(selected.type);
            if (props != null && props.miningSpeedBonus != null) {
                return miningSpeed + props.miningSpeedBonus;
            }
        }
        return miningSpeed;
    }

    public double getAttackPower() {
        Item selected = getSelectedItem();
        if (selected != null) {
            GameConstants.ItemProps props = GameConstants.ITEM_PROPERTIES.get(selected.type);
            if (props != null && props.attackPower != null) {
                return attackPower + props.attackPower;
            }
        }
        return attackPower;
    }
}
