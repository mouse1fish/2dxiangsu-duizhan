package pixelbattle.game.engine;

import pixelbattle.game.*;
import pixelbattle.game.entities.Player;

public class Physics {
    private static final double MAX_FALL_SPEED = 12;

    public void updatePlayer(Player player, Game game, double dt) {
        if (player.isDead) return;

        player.updateGroundState();

        double moveX = 0;
        if (player.isMoveLeft()) moveX -= 1;
        if (player.isMoveRight()) moveX += 1;

        player.vx = moveX * GameConstants.BASE_SPEED * player.speed;

        if (player.isFlying) {
            double flyY = 0;
            if (player.isWantJump()) flyY = -1;
            player.vy = flyY * GameConstants.BASE_SPEED * player.speed;
        } else {
            player.vy += GameConstants.GRAVITY;
            if (player.vy > MAX_FALL_SPEED) player.vy = MAX_FALL_SPEED;

            if (player.isWantJump() && player.onGround) {
                player.vy = -GameConstants.JUMP_FORCE;
                player.onGround = false;
            }
        }

        double cw = GameConstants.PLAYER_COLLISION_WIDTH;
        double ch = GameConstants.PLAYER_COLLISION_HEIGHT;
        double ps = GameConstants.PLAYER_SIZE;
        double ts = GameConstants.TILE_SIZE;
        double colOffX = (ps - cw) / 2;
        double colOffY = ps - ch;

        double colX = player.x + colOffX;
        double colY = player.y + colOffY;

        player.onGround = false;

        double newColX = colX + player.vx;
        int hStartTx = (int)(Math.min(colX, newColX) / ts) - 1;
        int hEndTx = (int)(Math.max(colX + cw, newColX + cw) / ts) + 1;
        int hStartTy = Math.max(0, (int)(colY / ts) - 1);
        int hEndTy = Math.min(game.getWorldHeightTiles() - 1, (int)((colY + ch) / ts) + 1);

        for (int tx = hStartTx; tx <= hEndTx; tx++) {
            for (int ty = hStartTy; ty <= hEndTy; ty++) {
                if (!game.isBlockSolid(tx, ty)) continue;

                double bx = tx * ts;
                double by = ty * ts;

                if (newColX + cw > bx && newColX < bx + ts && colY + ch > by && colY < by + ts) {
                    if (player.vx > 0) {
                        newColX = bx - cw;
                    } else if (player.vx < 0) {
                        newColX = bx + ts;
                    }
                    player.vx = 0;
                }
            }
        }

        double newColY = colY + player.vy;
        int vStartTx = (int)(newColX / ts) - 1;
        int vEndTx = (int)((newColX + cw) / ts) + 1;
        int vStartTy = Math.max(0, (int)(Math.min(colY, newColY) / ts) - 1);
        int vEndTy = Math.min(game.getWorldHeightTiles() - 1, (int)(Math.max(colY + ch, newColY + ch) / ts) + 1);

        for (int tx = vStartTx; tx <= vEndTx; tx++) {
            for (int ty = vStartTy; ty <= vEndTy; ty++) {
                if (!game.isBlockSolid(tx, ty)) continue;

                double bx = tx * ts;
                double by = ty * ts;

                if (newColX + cw > bx && newColX < bx + ts && newColY + ch > by && newColY < by + ts) {
                    if (player.vy > 0) {
                        newColY = by - ch;
                        player.vy = 0;
                        player.onGround = true;
                    } else if (player.vy < 0) {
                        newColY = by + ts;
                        player.vy = 0;
                    }
                }
            }
        }

        player.x = newColX - colOffX;
        player.y = newColY - colOffY;
    }
}
