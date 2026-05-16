package pixelbattle.ui;

import pixelbattle.game.*;
import pixelbattle.game.engine.Game;
import pixelbattle.game.entities.Player;
import pixelbattle.game.entities.AIPlayer;
import pixelbattle.game.terrain.TerrainGenerator;
import pixelbattle.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class GamePanel extends JPanel {
    private Game game;
    private int baseWidth;
    private int baseHeight;
    private double cameraX;
    private double cameraY;

    private boolean mouseLeftDown;
    private boolean mouseRightDown;
    private int mouseX;
    private int mouseY;

    private boolean showInventory;
    private boolean showCrafting;
    private boolean showPauseMenu;

    private int craftingScrollOffset;

    private int dragSourceIndex = -1;
    private int hotbarSwapSlot = -1;

    private Font uiFont;
    private Font smallFont;
    private Font bigFont;

    private BufferedImage offscreen;
    private Graphics2D offscreenG;

    public GamePanel(Game game, int width, int height) {
        this.game = game;
        this.baseWidth = width;
        this.baseHeight = height;
        this.cameraX = 0;
        this.cameraY = 0;
        this.mouseLeftDown = false;
        this.mouseRightDown = false;
        this.showInventory = false;
        this.showCrafting = false;
        this.showPauseMenu = false;
        this.craftingScrollOffset = 0;

        uiFont = new Font("微软雅黑", Font.PLAIN, 14);
        smallFont = new Font("微软雅黑", Font.PLAIN, 11);
        bigFont = new Font("微软雅黑", Font.BOLD, 24);

        setPreferredSize(new Dimension(width, height));
        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) { handleKeyPress(e); }
            @Override
            public void keyReleased(KeyEvent e) { handleKeyRelease(e); }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { handleMousePress(e); }
            @Override
            public void mouseReleased(MouseEvent e) { handleMouseRelease(e); }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) { mouseX = e.getX(); mouseY = e.getY(); }
            @Override
            public void mouseDragged(MouseEvent e) { mouseX = e.getX(); mouseY = e.getY(); }
        });

        addMouseWheelListener(e -> {
            if (showCrafting) {
                craftingScrollOffset += e.getUnitsToScroll() > 0 ? 1 : -1;
                if (craftingScrollOffset < 0) craftingScrollOffset = 0;
            } else {
                Player p = game.getPlayer();
                if (e.getUnitsToScroll() > 0) {
                    p.selectedSlot = Math.min(p.inventory.size() - 1, p.selectedSlot + 1);
                } else {
                    p.selectedSlot = Math.max(0, p.selectedSlot - 1);
                }
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                recreateOffscreen();
            }
        });
    }

    private void recreateOffscreen() {
        int w = getWidth();
        int h = getHeight();
        if (w > 0 && h > 0) {
            offscreen = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            offscreenG = offscreen.createGraphics();
            offscreenG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            offscreenG.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        }
    }

    private double getScaleX() { return (double)getWidth() / baseWidth; }
    private double getScaleY() { return (double)getHeight() / baseHeight; }
    private double getScale() { return Math.min(getScaleX(), getScaleY()); }

    private int toScreenX(int x) {
        double s = getScale();
        int offsetX = (getWidth() - (int)(baseWidth * s)) / 2;
        return (int)(x * s) + offsetX;
    }
    private int toScreenY(int y) {
        double s = getScale();
        int offsetY = (getHeight() - (int)(baseHeight * s)) / 2;
        return (int)(y * s) + offsetY;
    }
    private int fromScreenX(int sx) {
        double s = getScale();
        int offsetX = (getWidth() - (int)(baseWidth * s)) / 2;
        return (int)((sx - offsetX) / s);
    }
    private int fromScreenY(int sy) {
        double s = getScale();
        int offsetY = (getHeight() - (int)(baseHeight * s)) / 2;
        return (int)((sy - offsetY) / s);
    }

    private void handleKeyPress(KeyEvent e) {
        Player p = game.getPlayer();
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A: case KeyEvent.VK_LEFT:
                p.setMoveLeft(true);
                game.dismissHint("move");
                break;
            case KeyEvent.VK_D: case KeyEvent.VK_RIGHT:
                p.setMoveRight(true);
                game.dismissHint("move");
                break;
            case KeyEvent.VK_W: case KeyEvent.VK_UP: case KeyEvent.VK_SPACE:
                p.setWantJump(true);
                game.dismissHint("jump");
                break;
            case KeyEvent.VK_F: p.isFlying = !p.isFlying; break;
            case KeyEvent.VK_E:
                showInventory = !showInventory; showCrafting = false;
                dragSourceIndex = -1; hotbarSwapSlot = -1;
                game.dismissHint("inventory");
                break;
            case KeyEvent.VK_C:
                showCrafting = !showCrafting; showInventory = false;
                dragSourceIndex = -1; hotbarSwapSlot = -1;
                craftingScrollOffset = 0;
                game.dismissHint("craft");
                break;
            case KeyEvent.VK_ESCAPE:
                if (showInventory || showCrafting) {
                    showInventory = false; showCrafting = false;
                    dragSourceIndex = -1; hotbarSwapSlot = -1;
                    game.setGameState(Game.GameState.PLAYING);
                } else if (showPauseMenu) {
                    showPauseMenu = false;
                    game.setGameState(Game.GameState.PLAYING);
                } else {
                    showPauseMenu = true;
                    game.setGameState(Game.GameState.PAUSED);
                }
                break;
            case KeyEvent.VK_1: p.selectedSlot = 0; if (showInventory) hotbarSwapSlot = 0; break;
            case KeyEvent.VK_2: p.selectedSlot = Math.min(1, p.inventory.size()-1); if (showInventory) hotbarSwapSlot = 1; break;
            case KeyEvent.VK_3: p.selectedSlot = Math.min(2, p.inventory.size()-1); if (showInventory) hotbarSwapSlot = 2; break;
            case KeyEvent.VK_4: p.selectedSlot = Math.min(3, p.inventory.size()-1); if (showInventory) hotbarSwapSlot = 3; break;
            case KeyEvent.VK_5: p.selectedSlot = Math.min(4, p.inventory.size()-1); if (showInventory) hotbarSwapSlot = 4; break;
            case KeyEvent.VK_6: p.selectedSlot = Math.min(5, p.inventory.size()-1); if (showInventory) hotbarSwapSlot = 5; break;
            case KeyEvent.VK_7: p.selectedSlot = Math.min(6, p.inventory.size()-1); if (showInventory) hotbarSwapSlot = 6; break;
            case KeyEvent.VK_8: p.selectedSlot = Math.min(7, p.inventory.size()-1); if (showInventory) hotbarSwapSlot = 7; break;
            case KeyEvent.VK_9: p.selectedSlot = Math.min(8, p.inventory.size()-1); if (showInventory) hotbarSwapSlot = 8; break;
            case KeyEvent.VK_Q:
                if (showPauseMenu || game.getGameState() == Game.GameState.DEAD || game.getGameState() == Game.GameState.WON) {
                    Main.returnToMenu();
                } else {
                    game.useItem(p.getSelectedItem());
                }
                break;
            case KeyEvent.VK_R:
                if (game.getGameState() == Game.GameState.DEAD) game.respawnPlayer();
                break;
        }
    }

    private void handleKeyRelease(KeyEvent e) {
        Player p = game.getPlayer();
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A: case KeyEvent.VK_LEFT: p.setMoveLeft(false); break;
            case KeyEvent.VK_D: case KeyEvent.VK_RIGHT: p.setMoveRight(false); break;
            case KeyEvent.VK_W: case KeyEvent.VK_UP: case KeyEvent.VK_SPACE: p.setWantJump(false); break;
        }
    }

    private void handleMousePress(MouseEvent e) {
        int mx = fromScreenX(e.getX());
        int my = fromScreenY(e.getY());

        if (showCrafting && e.getButton() == MouseEvent.BUTTON1) {
            handleCraftingClick(mx, my);
            return;
        }

        if (showInventory && e.getButton() == MouseEvent.BUTTON1) {
            handleInventoryClick(mx, my);
            return;
        }

        if (e.getButton() == MouseEvent.BUTTON1) {
            mouseLeftDown = true;
            if (game.isHookPulling() || game.getActiveHook() != null) {
                game.retractHook();
                return;
            }
            Player p = game.getPlayer();
            Item sel = p.getSelectedItem();
            if (sel != null && isSword(sel.type)) {
                game.attackAtScreen(mx, my, cameraX, cameraY);
                game.dismissHint("attack");
            } else {
                int[] tilePos = game.findBlockAtScreen(mx, my, cameraX, cameraY);
                if (tilePos != null) {
                    game.mineBlock(tilePos, 0);
                    game.dismissHint("mine");
                }
            }
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            mouseRightDown = true;
            Player p = game.getPlayer();
            Item sel = p.getSelectedItem();
            if (sel != null && sel.type == ItemType.GRAPPLING_HOOK) {
                game.startHookCharge(mx, my);
                game.dismissHint("hook");
            } else if (sel != null && isRanged(sel.type)) {
                game.shootProjectile(mx, my, cameraX, cameraY);
            } else if (sel != null && isPlaceable(sel.type)) {
                game.placeBlock(mx, my, sel);
                game.dismissHint("place");
            }
        }
    }

    private void handleInventoryClick(int mx, int my) {
        Player p = game.getPlayer();
        int clickedSlot = getInventorySlotAt(mx, my);

        if (hotbarSwapSlot >= 0) {
            if (clickedSlot >= 0 && clickedSlot < p.inventory.size()) {
                swapInventorySlots(hotbarSwapSlot, clickedSlot);
            }
            hotbarSwapSlot = -1;
            return;
        }

        if (clickedSlot >= 0) {
            if (dragSourceIndex == -1) {
                dragSourceIndex = clickedSlot;
            } else {
                if (dragSourceIndex != clickedSlot) {
                    swapInventorySlots(dragSourceIndex, clickedSlot);
                }
                dragSourceIndex = -1;
            }
        } else {
            dragSourceIndex = -1;
        }
    }

    private int getInventorySlotAt(int mx, int my) {
        Player p = game.getPlayer();
        int iW = 400;
        int iH = 350;
        int iX = (baseWidth - iW) / 2;
        int iY = (baseHeight - iH) / 2;
        int cols = 9;
        int slotSize = 36;
        int y = iY + 30;

        for (int i = 0; i < p.inventory.size(); i++) {
            int col = i % cols;
            int row = i / cols;
            int sx = iX + 10 + col * (slotSize + 2);
            int sy = y + row * (slotSize + 2);
            if (sy + slotSize > iY + iH - 10) break;
            if (mx >= sx && mx <= sx + slotSize && my >= sy && my <= sy + slotSize) {
                return i;
            }
        }
        return -1;
    }

    private void swapInventorySlots(int a, int b) {
        Player p = game.getPlayer();
        if (a < 0 || a >= p.inventory.size() || b < 0 || b >= p.inventory.size()) return;
        Item temp = p.inventory.get(a);
        p.inventory.set(a, p.inventory.get(b));
        p.inventory.set(b, temp);
    }

    private void handleMouseRelease(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            mouseLeftDown = false;
            game.stopMining();
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            mouseRightDown = false;
            if (game.isHookCharging()) {
                int mx = fromScreenX(e.getX());
                int my = fromScreenY(e.getY());
                game.releaseHook();
            }
        }
    }

    private void handleCraftingClick(int mx, int my) {
        int cW = 400;
        int cH = 500;
        int cX = (baseWidth - cW) / 2;
        int cY = (baseHeight - cH) / 2;

        CraftingRecipe[] recipes = GameConstants.CRAFTING_RECIPES;
        int y = cY + 40 - craftingScrollOffset * 32;

        for (int i = 0; i < recipes.length; i++) {
            int rowY = y + i * 32;
            if (rowY < cY + 35 || rowY > cY + cH - 30) continue;

            if (mx >= cX + 5 && mx <= cX + cW - 5 && my >= rowY && my <= rowY + 28) {
                game.craftItem(i);
                break;
            }
        }
    }

    private boolean isSword(ItemType t) {
        return t == ItemType.WOODEN_SWORD || t == ItemType.IRON_SWORD || t == ItemType.LASER_SWORD;
    }

    private boolean isRanged(ItemType t) {
        return t == ItemType.BOW || t == ItemType.LASER_GUN;
    }

    private boolean isPlaceable(ItemType t) {
        return t == ItemType.DIRT_BLOCK || t == ItemType.GRASS_BLOCK || t == ItemType.STONE_BLOCK || t == ItemType.WOOD ||
               t == ItemType.WORKBENCH || t == ItemType.FURNACE;
    }

    public void update(double dt) {
        if (game.isHookCharging()) {
            int mx = fromScreenX(mouseX);
            int my = fromScreenY(mouseY);
            game.updateHookAim(mx, my);
        }

        if (mouseLeftDown && !showCrafting && !showInventory) {
            int mx = fromScreenX(mouseX);
            int my = fromScreenY(mouseY);
            int[] tilePos = game.findBlockAtScreen(mx, my, cameraX, cameraY);
            if (tilePos != null) {
                game.mineBlock(tilePos, dt);
            }
        }
    }

    public void updateGame(double dt) {
        if (game.getGameState() == Game.GameState.PLAYING) {
            game.update(dt);
            update(dt);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int panelW = getWidth();
        int panelH = getHeight();
        if (panelW <= 0 || panelH <= 0) return;

        if (offscreen == null || offscreen.getWidth() != panelW || offscreen.getHeight() != panelH) {
            recreateOffscreen();
        }

        double s = getScale();
        int drawW = (int)(baseWidth * s);
        int drawH = (int)(baseHeight * s);
        int offsetX = (panelW - drawW) / 2;
        int offsetY = (panelH - drawH) / 2;

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, panelW, panelH);

        BufferedImage gameBuffer = new BufferedImage(baseWidth, baseHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = gameBuffer.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Player p = game.getPlayer();
        cameraX = p.x - baseWidth / 2 + GameConstants.PLAYER_SIZE / 2;
        cameraY = p.y - baseHeight / 2 + GameConstants.PLAYER_SIZE / 2;
        game.setCamera(cameraX, cameraY);

        drawSky(g2d);
        drawBlocks(g2d);
        drawHookRope(g2d);
        drawEnemies(g2d);
        drawAIPlayer(g2d);
        drawPlayer(g2d);
        drawProjectiles(g2d);
        drawParticles(g2d);
        drawCrosshair(g2d);
        drawHUD(g2d);
        drawHints(g2d);

        if (game.isHookCharging()) drawHookAimIndicator(g2d);

        if (showInventory) drawInventory(g2d);
        if (showCrafting) drawCrafting(g2d);
        if (showPauseMenu) drawPauseMenu(g2d);
        if (game.getGameState() == Game.GameState.DEAD) drawDeathScreen(g2d);
        if (game.getGameState() == Game.GameState.WON) drawWinScreen(g2d);

        g2d.dispose();

        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(gameBuffer, offsetX, offsetY, drawW, drawH, null);
    }

    private void drawSky(Graphics2D g) {
        double brightness = game.getDayBrightness();
        Color dayColor = GameConstants.COLOR_SKY;
        Color nightColor = GameConstants.COLOR_SKY_NIGHT;
        int r = (int)(nightColor.getRed() + (dayColor.getRed() - nightColor.getRed()) * brightness);
        int gr = (int)(nightColor.getGreen() + (dayColor.getGreen() - nightColor.getGreen()) * brightness);
        int b = (int)(nightColor.getBlue() + (dayColor.getBlue() - nightColor.getBlue()) * brightness);
        g.setColor(new Color(r, gr, b));
        g.fillRect(0, 0, baseWidth, baseHeight);

        double dayTime = game.getDayTime();
        if (brightness > 0.3) {
            double sunAngle = dayTime * Math.PI * 2 - Math.PI / 2;
            int sunX = baseWidth / 2 + (int)(Math.cos(sunAngle) * baseWidth * 0.4);
            int sunY = baseHeight / 3 - (int)(Math.sin(sunAngle) * baseHeight * 0.3);
            g.setColor(new Color(255, 255, 100, (int)(brightness * 255)));
            g.fillOval(sunX - 20, sunY - 20, 40, 40);
        }
        if (brightness < 0.5) {
            double moonAngle = (dayTime - 0.5) * Math.PI * 2 - Math.PI / 2;
            int moonX = baseWidth / 2 + (int)(Math.cos(moonAngle) * baseWidth * 0.4);
            int moonY = baseHeight / 3 - (int)(Math.sin(moonAngle) * baseHeight * 0.3);
            g.setColor(new Color(220, 220, 255, (int)((1 - brightness) * 200)));
            g.fillOval(moonX - 15, moonY - 15, 30, 30);
        }
    }

    private void drawBlocks(Graphics2D g) {
        TerrainGenerator tgen = game.getTerrainGen();
        int ts = GameConstants.TILE_SIZE;
        int startTx = (int)(cameraX / ts) - 1;
        int endTx = (int)((cameraX + baseWidth) / ts) + 1;
        int startTy = Math.max(0, (int)(cameraY / ts) - 1);
        int endTy = Math.min(game.getWorldHeightTiles() - 1, (int)((cameraY + baseHeight) / ts) + 1);

        for (int tx = startTx; tx <= endTx; tx++) {
            for (int ty = startTy; ty <= endTy; ty++) {
                BlockType type = tgen.getBlockType(tx, ty);
                if (type == BlockType.AIR) continue;

                GameConstants.BlockProps props = GameConstants.BLOCK_PROPERTIES.get(type);
                if (props == null) continue;

                int sx = (int)(tx * ts - cameraX);
                int sy = (int)(ty * ts - cameraY);
                g.setColor(props.color);
                g.fillRect(sx, sy, ts, ts);

                if (type == BlockType.LAVA) {
                    g.setColor(new Color(255, 200, 0, 100));
                    g.fillRect(sx, sy, ts, ts / 2);
                }
                if (type == BlockType.IRON_ORE) {
                    g.setColor(new Color(200, 200, 200));
                    g.fillRect(sx + 3, sy + 3, 4, 4);
                    g.fillRect(sx + 9, sy + 7, 4, 4);
                }
                if (type == BlockType.GOLD_ORE) {
                    g.setColor(new Color(255, 255, 100));
                    g.fillRect(sx + 2, sy + 4, 5, 4);
                    g.fillRect(sx + 8, sy + 9, 5, 4);
                }
                if (type == BlockType.DIAMOND_ORE) {
                    g.setColor(new Color(100, 255, 255));
                    g.fillRect(sx + 4, sy + 3, 4, 4);
                    g.fillRect(sx + 9, sy + 8, 4, 4);
                }
                if (type == BlockType.CHEST) {
                    g.setColor(new Color(160, 82, 45));
                    g.fillRect(sx + 1, sy + 5, ts - 2, ts - 6);
                    g.setColor(new Color(218, 165, 32));
                    g.fillRect(sx + 2, sy + 6, ts - 4, ts - 8);
                    g.setColor(new Color(139, 69, 19));
                    g.fillRect(sx + 1, sy + 2, ts - 2, 4);
                    g.setColor(new Color(160, 82, 45));
                    g.fillRect(sx + 2, sy + 3, ts - 4, 2);
                    g.setColor(new Color(255, 215, 0));
                    g.fillRect(sx + ts/2 - 2, sy + 5, 4, 3);
                    g.setColor(new Color(139, 69, 19));
                    g.fillRect(sx + 2, sy + ts/2 + 1, ts - 4, 1);
                    g.setColor(new Color(218, 165, 32));
                    g.fillRect(sx + 3, sy + ts/2 + 2, ts - 6, 1);
                }

                g.setColor(new Color(0, 0, 0, 30));
                g.drawRect(sx, sy, ts, ts);
            }
        }

        int mtx = game.getMiningTargetTx();
        int mty = game.getMiningTargetTy();
        if (mtx >= 0 && mty >= 0) {
            BlockType mtype = tgen.getBlockType(mtx, mty);
            GameConstants.BlockProps mprops = GameConstants.BLOCK_PROPERTIES.get(mtype);
            if (mprops != null && mprops.hardness > 0) {
                int sx = (int)(mtx * ts - cameraX);
                int sy = (int)(mty * ts - cameraY);
                double progress = game.getMiningProgress() / mprops.hardness;
                g.setColor(new Color(255, 255, 255, 80));
                int crackWidth = (int)(ts * progress);
                g.fillRect(sx, sy, crackWidth, ts);
            }
        }
    }

    private void drawHookRope(Graphics2D g) {
        Player p = game.getPlayer();
        double playerCenterX = p.x + GameConstants.PLAYER_SIZE / 2 - cameraX;
        double playerCenterY = p.y + GameConstants.PLAYER_SIZE / 2 - cameraY;

        if (game.isHookPulling()) {
            double anchorSX = game.getHookAnchorX() - cameraX;
            double anchorSY = game.getHookAnchorY() - cameraY;
            g.setColor(new Color(180, 180, 180));
            g.setStroke(new BasicStroke(2));
            g.drawLine((int)playerCenterX, (int)playerCenterY, (int)anchorSX, (int)anchorSY);
            g.setColor(new Color(200, 200, 200));
            g.fillOval((int)anchorSX - 3, (int)anchorSY - 3, 6, 6);
            g.setStroke(new BasicStroke(1));
        }

        Projectile hook = game.getActiveHook();
        if (hook != null) {
            double hookSX = hook.x - cameraX;
            double hookSY = hook.y - cameraY;
            g.setColor(new Color(180, 180, 180));
            g.setStroke(new BasicStroke(2));
            g.drawLine((int)playerCenterX, (int)playerCenterY, (int)hookSX, (int)hookSY);
            g.setColor(new Color(220, 220, 220));
            g.fillOval((int)hookSX - 3, (int)hookSY - 3, 6, 6);
            g.setStroke(new BasicStroke(1));
        }
    }

    private void drawHookAimIndicator(Graphics2D g) {
        Player p = game.getPlayer();
        double playerCenterX = p.x + GameConstants.PLAYER_SIZE / 2 - cameraX;
        double playerCenterY = p.y + GameConstants.PLAYER_SIZE / 2 - cameraY;
        double angle = game.getHookAimAngle();
        double chargeRatio = game.getHookChargeTime() / 2.0;
        double length = 30 + chargeRatio * 80;

        int endX = (int)(playerCenterX + Math.cos(angle) * length);
        int endY = (int)(playerCenterY + Math.sin(angle) * length);

        g.setColor(new Color(255, 255, 100, 150));
        g.setStroke(new BasicStroke(2));
        g.drawLine((int)playerCenterX, (int)playerCenterY, endX, endY);

        int dotCount = 3;
        for (int i = 1; i <= dotCount; i++) {
            double t = (double)i / (dotCount + 1);
            int dx = (int)(playerCenterX + (endX - playerCenterX) * t);
            int dy = (int)(playerCenterY + (endY - playerCenterY) * t);
            g.fillOval(dx - 2, dy - 2, 4, 4);
        }
        g.setStroke(new BasicStroke(1));

        g.setColor(new Color(255, 255, 100, 200));
        g.setFont(smallFont);
        g.drawString("蓄力: " + (int)(chargeRatio * 100) + "%", (int)playerCenterX - 20, (int)playerCenterY - 20);
    }

    private void drawPlayer(Graphics2D g) {
        Player p = game.getPlayer();
        int sx = (int)(p.x - cameraX);
        int sy = (int)(p.y - cameraY);
        int ps = GameConstants.PLAYER_SIZE;

        if (p.invincibleTimer > 0 && ((int)(p.invincibleTimer * 10) % 2 == 0)) return;

        g.setColor(new Color(60, 120, 200));
        g.fillRect(sx + 8, sy + 4, 16, 12);

        g.setColor(new Color(255, 220, 180));
        g.fillRect(sx + 10, sy, 12, 10);

        g.setColor(new Color(80, 60, 40));
        g.fillRect(sx + 10, sy, 12, 3);

        g.setColor(new Color(60, 120, 200));
        g.fillRect(sx + 6, sy + 16, 8, 10);
        g.fillRect(sx + 18, sy + 16, 8, 10);

        g.setColor(new Color(80, 60, 40));
        g.fillRect(sx + 6, sy + 26, 8, 6);
        g.fillRect(sx + 18, sy + 26, 8, 6);

        Item sel = p.getSelectedItem();
        if (sel != null) {
            int handX = p.facingRight ? sx + 26 : sx - 4;
            int handY = sy + 14;
            drawItemIcon(g, sel.type, handX, handY, 16);
        }

        g.setColor(new Color(255, 220, 180));
        g.fillRect(p.facingRight ? sx + 26 : sx - 4, sy + 16, 4, 10);
    }

    private void drawAIPlayer(Graphics2D g) {
        AIPlayer ai = game.getAIPlayer();
        if (ai == null || ai.isDead) return;

        int sx = (int)(ai.x - cameraX);
        int sy = (int)(ai.y - cameraY);

        if (sx < -50 || sx > baseWidth + 50 || sy < -50 || sy > baseHeight + 50) return;

        g.setColor(new Color(200, 60, 60));
        g.fillRect(sx + 8, sy + 4, 16, 12);

        g.setColor(new Color(255, 200, 170));
        g.fillRect(sx + 10, sy, 12, 10);

        g.setColor(new Color(150, 30, 30));
        g.fillRect(sx + 10, sy, 12, 3);

        g.setColor(new Color(200, 60, 60));
        g.fillRect(sx + 6, sy + 16, 8, 10);
        g.fillRect(sx + 18, sy + 16, 8, 10);

        g.setColor(new Color(80, 60, 40));
        g.fillRect(sx + 6, sy + 26, 8, 6);
        g.fillRect(sx + 18, sy + 26, 8, 6);

        g.setColor(new Color(255, 200, 170));
        g.fillRect(ai.facingRight ? sx + 26 : sx - 4, sy + 16, 4, 10);

        g.setColor(Color.RED);
        g.setFont(smallFont);
        g.drawString("AI", sx + 8, sy - 5);

        int barW = 30;
        g.setColor(Color.RED);
        g.fillRect(sx + 1, sy - 12, barW, 4);
        g.setColor(Color.GREEN);
        g.fillRect(sx + 1, sy - 12, (int)(barW * ai.health / ai.maxHealth), 4);
    }

    private void drawEnemies(Graphics2D g) {
        for (Enemy enemy : game.getEnemies()) {
            if (enemy.isDead) continue;
            int sx = (int)(enemy.x - cameraX);
            int sy = (int)(enemy.y - cameraY);
            if (sx < -50 || sx > baseWidth + 50 || sy < -50 || sy > baseHeight + 50) continue;

            if (enemy.hurtTimer > 0) {
                g.setColor(Color.WHITE);
            } else {
                switch (enemy.type) {
                    case SLIME: g.setColor(new Color(50, 200, 50)); break;
                    case SKELETON: g.setColor(new Color(220, 220, 200)); break;
                    case GOBLIN: g.setColor(new Color(100, 160, 50)); break;
                    default: g.setColor(Color.GRAY); break;
                }
            }

            int w = enemy.getWidth();
            int h = enemy.getHeight();

            switch (enemy.type) {
                case SLIME:
                    g.fillOval(sx, sy + h - w, w, w);
                    g.setColor(Color.WHITE);
                    g.fillOval(sx + 5, sy + h - w + 5, 4, 4);
                    g.fillOval(sx + 14, sy + h - w + 5, 4, 4);
                    g.setColor(Color.BLACK);
                    g.fillOval(sx + 6, sy + h - w + 6, 2, 2);
                    g.fillOval(sx + 15, sy + h - w + 6, 2, 2);
                    break;
                case SKELETON:
                    g.fillRect(sx + 8, sy, 8, 8);
                    g.fillRect(sx + 6, sy + 8, 12, 12);
                    g.fillRect(sx + 6, sy + 20, 4, 12);
                    g.fillRect(sx + 14, sy + 20, 4, 12);
                    g.setColor(Color.BLACK);
                    g.fillRect(sx + 9, sy + 2, 2, 2);
                    g.fillRect(sx + 13, sy + 2, 2, 2);
                    break;
                case GOBLIN:
                    g.fillOval(sx + 4, sy, 20, 16);
                    g.fillRect(sx + 6, sy + 14, 6, 10);
                    g.fillRect(sx + 16, sy + 14, 6, 10);
                    g.setColor(new Color(180, 100, 50));
                    g.fillRect(sx + 10, sy + 4, 3, 3);
                    g.fillRect(sx + 17, sy + 4, 3, 3);
                    g.setColor(Color.BLACK);
                    g.fillRect(sx + 11, sy + 5, 1, 1);
                    g.fillRect(sx + 18, sy + 5, 1, 1);
                    break;
            }

            int barW = w;
            g.setColor(Color.RED);
            g.fillRect(sx, sy - 6, barW, 3);
            g.setColor(Color.GREEN);
            g.fillRect(sx, sy - 6, (int)(barW * enemy.health / enemy.maxHealth), 3);
        }
    }

    private void drawProjectiles(Graphics2D g) {
        for (Projectile proj : game.getProjectiles()) {
            if (proj.type == ProjectileType.HOOK) continue;

            int sx = (int)(proj.x - cameraX);
            int sy = (int)(proj.y - cameraY);
            if (sx < -20 || sx > baseWidth + 20 || sy < -20 || sy > baseHeight + 20) continue;

            switch (proj.type) {
                case ARROW:
                    g.setColor(new Color(139, 69, 19));
                    double angle = Math.atan2(proj.vy, proj.vx);
                    g.drawLine(sx, sy, sx - (int)(Math.cos(angle) * 10), sy - (int)(Math.sin(angle) * 10));
                    g.setColor(Color.GRAY);
                    g.fillOval(sx - 1, sy - 1, 3, 3);
                    break;
                case LASER:
                    g.setColor(new Color(0, 255, 136));
                    g.fillOval(sx - 3, sy - 3, 6, 6);
                    g.setColor(new Color(0, 255, 136, 100));
                    g.fillOval(sx - 5, sy - 5, 10, 10);
                    break;
                default:
                    g.setColor(Color.YELLOW);
                    g.fillOval(sx - 2, sy - 2, 4, 4);
                    break;
            }
        }
    }

    private void drawParticles(Graphics2D g) {
        for (Particle p : game.getParticles()) {
            int sx = (int)(p.x - cameraX);
            int sy = (int)(p.y - cameraY);
            int alpha = (int)(255 * Math.min(1, p.life * 2));
            g.setColor(new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), alpha));
            g.fillRect(sx - 2, sy - 2, 4, 4);
        }
    }

    private void drawCrosshair(Graphics2D g) {
        int mx = fromScreenX(mouseX);
        int my = fromScreenY(mouseY);
        g.setColor(new Color(255, 255, 255, 150));
        g.drawLine(mx - 8, my, mx + 8, my);
        g.drawLine(mx, my - 8, mx, my + 8);
    }

    private void drawHints(Graphics2D g) {
        int x = 10;
        int y = baseHeight / 2 - 80;
        int boxW = 160;
        int lineH = 18;

        String[][] hints = {
            {"move", "A/D 或 方向键: 移动"},
            {"jump", "空格/W/上: 跳跃"},
            {"mine", "左键: 挖掘/攻击"},
            {"place", "右键: 放置方块"},
            {"inventory", "E: 打开背包"},
            {"craft", "C: 打开合成表"},
            {"hook", "右键(钩爪): 蓄力发射"},
            {"attack", "左键(剑): 攻击"}
        };

        int visibleCount = 0;
        for (String[] hint : hints) {
            if (!game.isHintDismissed(hint[0])) visibleCount++;
        }
        if (visibleCount == 0) return;

        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(x, y, boxW, visibleCount * lineH + 10);

        g.setColor(new Color(0, 255, 136));
        g.setFont(smallFont);
        g.drawString("操作说明 (按键后消失)", x + 5, y + 12);

        int cy = y + 26;
        g.setColor(new Color(220, 220, 220));
        for (String[] hint : hints) {
            if (!game.isHintDismissed(hint[0])) {
                g.drawString(hint[1], x + 5, cy);
                cy += lineH;
            }
        }
    }

    private void drawHUD(Graphics2D g) {
        Player p = game.getPlayer();

        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(5, 5, 204, 24);
        g.setColor(Color.RED);
        g.fillRect(7, 7, 200, 9);
        g.setColor(Color.GREEN);
        g.fillRect(7, 7, (int)(200 * p.health / GameConstants.MAX_HEALTH), 9);
        g.setColor(Color.WHITE);
        g.setFont(smallFont);
        g.drawString("HP: " + (int)p.health, 10, 15);

        if (p.shield > 0) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(5, 30, 204, 14);
            g.setColor(Color.BLUE);
            g.fillRect(7, 32, (int)(200 * p.shield / GameConstants.MAX_SHIELD), 10);
            g.setColor(Color.WHITE);
            g.drawString("盾: " + (int)p.shield, 10, 40);
        }

        drawHotbar(g);

        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(baseWidth - 160, 5, 155, 50);
        g.setColor(Color.WHITE);
        g.setFont(smallFont);
        g.drawString("分数: " + game.getScore(), baseWidth - 155, 20);
        g.drawString("击杀: " + p.killCount, baseWidth - 155, 35);
        g.drawString(game.isDay() ? "白天" : "夜晚", baseWidth - 155, 50);

        if (p.isFlying) {
            g.setColor(new Color(255, 255, 0));
            g.setFont(uiFont);
            g.drawString("飞行模式", 10, baseHeight - 10);
        }
    }

    private void drawHotbar(Graphics2D g) {
        Player p = game.getPlayer();
        int slotSize = 36;
        int barWidth = 9 * slotSize;
        int startX = (baseWidth - barWidth) / 2;
        int startY = baseHeight - slotSize - 8;

        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(startX - 2, startY - 2, barWidth + 4, slotSize + 4);

        for (int i = 0; i < 9; i++) {
            int x = startX + i * slotSize;
            if (i == p.selectedSlot) {
                g.setColor(new Color(255, 255, 255, 100));
            } else {
                g.setColor(new Color(50, 50, 50, 150));
            }
            g.fillRect(x, startY, slotSize, slotSize);
            g.setColor(new Color(80, 80, 80));
            g.drawRect(x, startY, slotSize, slotSize);

            if (i < p.inventory.size()) {
                Item item = p.inventory.get(i);
                drawItemIcon(g, item.type, x + 4, startY + 4, slotSize - 8);

                GameConstants.ItemProps iprops = GameConstants.ITEM_PROPERTIES.get(item.type);
                if (iprops != null) {
                    g.setColor(Color.WHITE);
                    g.setFont(smallFont);
                    g.drawString(iprops.name, x + 2, startY + slotSize - 2);
                }
                if (item.count > 1) {
                    g.setColor(Color.YELLOW);
                    g.setFont(smallFont);
                    g.drawString("x" + item.count, x + slotSize - 20, startY + 12);
                }
            }
        }
    }

    private void drawInventory(Graphics2D g) {
        int iW = 400;
        int iH = 350;
        int iX = (baseWidth - iW) / 2;
        int iY = (baseHeight - iH) / 2;

        g.setColor(new Color(30, 30, 30, 220));
        g.fillRect(iX, iY, iW, iH);
        g.setColor(Color.WHITE);
        g.drawRect(iX, iY, iW, iH);
        g.setFont(uiFont);
        g.drawString("物品栏 (E关闭)", iX + 10, iY + 20);

        if (hotbarSwapSlot >= 0) {
            g.setColor(new Color(255, 200, 50));
            g.setFont(smallFont);
            g.drawString("点击背包物品与快捷栏" + (hotbarSwapSlot + 1) + "交换", iX + 150, iY + 20);
        }
        if (dragSourceIndex >= 0) {
            g.setColor(new Color(100, 255, 100));
            g.setFont(smallFont);
            g.drawString("点击目标位置交换", iX + 150, iY + 20);
        }

        Player p = game.getPlayer();
        int cols = 9;
        int slotSize = 36;
        int y = iY + 30;

        for (int i = 0; i < p.inventory.size(); i++) {
            int col = i % cols;
            int row = i / cols;
            int sx = iX + 10 + col * (slotSize + 2);
            int sy = y + row * (slotSize + 2);

            if (sy + slotSize > iY + iH - 10) break;

            if (i == dragSourceIndex) {
                g.setColor(new Color(100, 255, 100, 180));
            } else if (i == hotbarSwapSlot) {
                g.setColor(new Color(255, 200, 50, 180));
            } else if (i == p.selectedSlot) {
                g.setColor(new Color(100, 100, 200, 150));
            } else if (i < 9) {
                g.setColor(new Color(60, 60, 80, 150));
            } else {
                g.setColor(new Color(50, 50, 50, 150));
            }
            g.fillRect(sx, sy, slotSize, slotSize);
            g.setColor(i < 9 ? new Color(120, 120, 180) : Color.GRAY);
            g.drawRect(sx, sy, slotSize, slotSize);

            if (i < 9) {
                g.setColor(new Color(180, 180, 220, 120));
                g.setFont(smallFont);
                g.drawString(String.valueOf(i + 1), sx + 2, sy + 11);
            }

            Item item = p.inventory.get(i);
            drawItemIcon(g, item.type, sx + 4, sy + 4, slotSize - 8);

            GameConstants.ItemProps iprops = GameConstants.ITEM_PROPERTIES.get(item.type);
            if (iprops != null) {
                g.setColor(Color.WHITE);
                g.setFont(smallFont);
                g.drawString(iprops.name, sx + 2, sy + slotSize - 2);
            }
            if (item.count > 1) {
                g.setColor(Color.YELLOW);
                g.setFont(smallFont);
                g.drawString("x" + item.count, sx + slotSize - 20, sy + 12);
            }
        }
    }

    private void drawCrafting(Graphics2D g) {
        int cW = 400;
        int cH = 500;
        int cX = (baseWidth - cW) / 2;
        int cY = (baseHeight - cH) / 2;

        g.setColor(new Color(30, 30, 30, 230));
        g.fillRect(cX, cY, cW, cH);
        g.setColor(Color.WHITE);
        g.drawRect(cX, cY, cW, cH);
        g.setFont(uiFont);
        g.drawString("合成 (C关闭 | 点击合成 | 滚轮翻页)", cX + 10, cY + 20);

        Player p = game.getPlayer();
        CraftingRecipe[] recipes = GameConstants.CRAFTING_RECIPES;
        int y = cY + 40 - craftingScrollOffset * 32;

        for (int i = 0; i < recipes.length; i++) {
            int rowY = y + i * 32;
            if (rowY < cY + 35 || rowY > cY + cH - 30) continue;

            CraftingRecipe recipe = recipes[i];
            boolean canCraft = true;
            for (int j = 0; j < recipe.ingredients.length; j++) {
                if (p.getItemCount(recipe.ingredients[j]) < recipe.amounts[j]) {
                    canCraft = false;
                    break;
                }
            }

            g.setColor(canCraft ? new Color(50, 120, 50, 180) : new Color(60, 60, 60, 150));
            g.fillRect(cX + 5, rowY, cW - 10, 28);

            if (canCraft) {
                g.setColor(new Color(80, 180, 80));
                g.drawRect(cX + 5, rowY, cW - 10, 28);
            }

            GameConstants.ItemProps resultProps = GameConstants.ITEM_PROPERTIES.get(recipe.result);
            String name = resultProps != null ? resultProps.name : "?";

            drawItemIcon(g, recipe.result, cX + 10, rowY + 4, 20);

            g.setColor(canCraft ? Color.WHITE : Color.GRAY);
            g.setFont(smallFont);
            g.drawString(name + " x" + recipe.resultCount, cX + 35, rowY + 18);

            StringBuilder sb = new StringBuilder("<- ");
            for (int j = 0; j < recipe.ingredients.length; j++) {
                GameConstants.ItemProps ip = GameConstants.ITEM_PROPERTIES.get(recipe.ingredients[j]);
                if (j > 0) sb.append("+");
                String iName = ip != null ? ip.name : "?";
                int have = p.getItemCount(recipe.ingredients[j]);
                int need = recipe.amounts[j];
                sb.append(iName).append(have).append("/").append(need);
            }
            g.drawString(sb.toString(), cX + 140, rowY + 18);
        }

        g.setColor(Color.GRAY);
        g.setFont(smallFont);
        int maxScroll = Math.max(0, recipes.length - (cH - 70) / 32);
        g.drawString("页: " + (craftingScrollOffset + 1) + "/" + (maxScroll + 1), cX + cW - 60, cY + cH - 10);
    }

    private void drawPauseMenu(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, baseWidth, baseHeight);

        g.setColor(Color.WHITE);
        g.setFont(bigFont);
        g.drawString("暂停", baseWidth / 2 - 30, baseHeight / 2 - 40);
        g.setFont(uiFont);
        g.drawString("按 ESC 继续", baseWidth / 2 - 50, baseHeight / 2 + 10);
        g.drawString("按 Q 返回主菜单", baseWidth / 2 - 60, baseHeight / 2 + 40);
    }

    private void drawDeathScreen(Graphics2D g) {
        g.setColor(new Color(100, 0, 0, 150));
        g.fillRect(0, 0, baseWidth, baseHeight);
        g.setColor(Color.RED);
        g.setFont(bigFont);
        g.drawString("你死了", baseWidth / 2 - 50, baseHeight / 2 - 20);
        g.setColor(Color.WHITE);
        g.setFont(uiFont);
        g.drawString("按 R 重生", baseWidth / 2 - 40, baseHeight / 2 + 20);
        g.drawString("按 Q 返回主菜单", baseWidth / 2 - 65, baseHeight / 2 + 50);
    }

    private void drawWinScreen(Graphics2D g) {
        g.setColor(new Color(0, 80, 0, 150));
        g.fillRect(0, 0, baseWidth, baseHeight);
        g.setColor(new Color(255, 215, 0));
        g.setFont(bigFont);
        g.drawString("胜利!", baseWidth / 2 - 40, baseHeight / 2 - 20);
        g.setColor(Color.WHITE);
        g.setFont(uiFont);
        g.drawString("你消灭了AI对手!", baseWidth / 2 - 65, baseHeight / 2 + 20);
        g.drawString("按 Q 返回主菜单", baseWidth / 2 - 65, baseHeight / 2 + 50);
    }

    private void drawItemIcon(Graphics2D g, ItemType type, int x, int y, int size) {
        GameConstants.ItemProps props = GameConstants.ITEM_PROPERTIES.get(type);
        if (props == null) return;

        String icon = props.icon;
        if (icon == null) {
            g.setColor(props.color);
            g.fillRect(x, y, size, size);
            return;
        }

        if (icon.startsWith("pickaxe")) {
            g.setColor(props.color);
            g.fillRect(x + size/2 - 1, y + 2, 3, size - 4);
            int headW = size * 2 / 3;
            int headH = size / 4;
            g.fillRect(x + size/2 - headW/2, y + 1, headW, headH);
            g.setColor(new Color(0,0,0,60));
            g.drawRect(x + size/2 - headW/2, y + 1, headW, headH);
        } else if (icon.startsWith("axe")) {
            g.setColor(props.color);
            g.fillRect(x + size/2 - 1, y + 2, 3, size - 4);
            int headW = size * 2 / 3;
            int headH = size / 3;
            g.fillRect(x + size/2, y + 1, headW/2, headH);
            g.setColor(new Color(0,0,0,60));
            g.drawRect(x + size/2, y + 1, headW/2, headH);
        } else if (icon.startsWith("sword")) {
            g.setColor(props.color);
            int bladeW = size / 5;
            g.fillRect(x + size/2 - bladeW/2, y + 2, bladeW, size * 3/4);
            g.fillRect(x + size/4, y + size*3/4 - 2, size/2, 3);
            g.setColor(new Color(139,69,19));
            g.fillRect(x + size/2 - bladeW/2 - 2, y + size*3/4 + 1, bladeW + 4, size/5);
        } else if (icon.equals("bow")) {
            g.setColor(props.color);
            g.drawArc(x + size/4, y + 2, size/2, size - 4, -60, 120);
            g.setColor(Color.GRAY);
            g.drawLine(x + size/2, y + 2, x + size/2, y + size - 2);
        } else if (icon.startsWith("gun")) {
            g.setColor(props.color);
            g.fillRect(x + 2, y + size/3, size - 4, size/3);
            g.fillRect(x + size*2/3, y + size/4, size/4, size/2);
            g.setColor(new Color(139,69,19));
            g.fillRect(x, y + size/3 + 2, size/4, size/3 - 4);
        } else if (icon.equals("shield")) {
            g.setColor(props.color);
            int[] xp = {x + size/2, x + size - 2, x + size - 2, x + size/2, x + 2, x + 2};
            int[] yp = {y + 2, y + size/4, y + size*2/3, y + size - 2, y + size*2/3, y + size/4};
            g.fillPolygon(xp, yp, 6);
        } else if (icon.startsWith("potion")) {
            g.setColor(props.color);
            g.fillRect(x + size/3, y + 2, size/3, size/4);
            g.fillRect(x + size/4, y + size/4, size/2, size*3/4 - 2);
            g.setColor(new Color(255,255,255,80));
            g.fillRect(x + size/3 + 1, y + size/4 + 2, size/6, size/3);
        } else if (icon.equals("arrow")) {
            g.setColor(props.color);
            g.fillRect(x + size/2 - 1, y + 2, 2, size - 4);
            g.fillPolygon(new int[]{x + size/2, x + size/4, x + size*3/4},
                          new int[]{y + 2, y + size/3, y + size/3}, 3);
        } else if (icon.equals("hook")) {
            g.setColor(props.color);
            g.fillRect(x + size/2 - 1, y, 2, size * 2/3);
            g.drawArc(x + size/4, y + size/2, size/2, size/2, 0, -180);
            g.fillOval(x + size/4 - 2, y + size - 6, 4, 4);
        } else if (icon.equals("block")) {
            g.setColor(props.color);
            g.fillRect(x + 1, y + 1, size - 2, size - 2);
            g.setColor(new Color(0,0,0,40));
            g.drawRect(x + 1, y + 1, size - 2, size - 2);
        } else if (icon.equals("ore")) {
            g.setColor(props.color);
            g.fillRect(x + 1, y + 1, size - 2, size - 2);
            g.setColor(new Color(200,200,200,120));
            g.fillRect(x + 2, y + 2, size/3, size/3);
            g.fillRect(x + size/2, y + size/2, size/3, size/3);
        } else if (icon.equals("gem")) {
            g.setColor(props.color);
            int[] xp = {x + size/2, x + size - 2, x + size/2, x + 2};
            int[] yp = {y + 2, y + size/2, y + size - 2, y + size/2};
            g.fillPolygon(xp, yp, 4);
        } else if (icon.equals("ingot")) {
            g.setColor(props.color);
            g.fillRect(x + 2, y + size/3, size - 4, size * 2/3 - 2);
            g.setColor(new Color(255,255,255,60));
            g.fillRect(x + 3, y + size/3 + 1, size/3, size/4);
        } else if (icon.equals("workbench")) {
            g.setColor(props.color);
            g.fillRect(x + 1, y + 1, size - 2, size - 2);
            g.setColor(new Color(139,69,19));
            g.fillRect(x + 2, y + 2, size/2 - 2, size/2 - 2);
            g.fillRect(x + size/2 + 1, y + 2, size/2 - 3, size/2 - 2);
        } else if (icon.equals("furnace")) {
            g.setColor(props.color);
            g.fillRect(x + 1, y + 1, size - 2, size - 2);
            g.setColor(new Color(50,50,50));
            g.fillRect(x + size/4, y + size/3, size/2, size/2);
        } else if (icon.equals("circuit")) {
            g.setColor(props.color);
            g.fillRect(x + 2, y + 2, size - 4, size - 4);
            g.setColor(new Color(0,100,0));
            g.drawLine(x + size/2, y + 2, x + size/2, y + size - 2);
            g.drawLine(x + 2, y + size/2, x + size - 2, y + size/2);
        } else if (icon.equals("core")) {
            g.setColor(props.color);
            g.fillOval(x + 2, y + 2, size - 4, size - 4);
            g.setColor(new Color(255,255,255,100));
            g.fillOval(x + size/3, y + size/3, size/4, size/4);
        } else if (icon.equals("string")) {
            g.setColor(props.color);
            g.drawLine(x + 2, y + size/2, x + size - 2, y + size/2);
            g.drawLine(x + size/2, y + 2, x + size/2, y + size - 2);
        } else if (icon.equals("bullet")) {
            g.setColor(new Color(255,200,50));
            g.fillOval(x + size/3, y + size/6, size/3, size/3);
            g.setColor(new Color(180,140,40));
            g.fillRect(x + size/3 + 1, y + size/2, size/3 - 2, size/3);
            g.setColor(new Color(255,255,200,120));
            g.fillOval(x + size/3 + 2, y + size/6 + 2, size/6, size/6);
        } else {
            g.setColor(props.color);
            g.fillRect(x, y, size, size);
        }
    }
}
