package pixelbattle;

import pixelbattle.game.BlockType;
import pixelbattle.game.engine.Game;
import pixelbattle.game.entities.Player;
import pixelbattle.game.entities.AIPlayer;
import pixelbattle.game.network.NetworkManager;
import pixelbattle.game.network.NetPacket;
import pixelbattle.game.terrain.TerrainGenerator;
import pixelbattle.ui.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Main {
    private static JFrame frame;
    private static CardLayout cardLayout;
    private static JPanel mainPanel;
    private static GamePanel gamePanel;
    private static Timer gameTimer;
    private static Game currentGame;
    private static JLabel loadingLabel;
    private static NetworkManager currentNet;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("像素大战 - Pixel Battle");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1280, 720);
            frame.setLocationRelativeTo(null);
            frame.setResizable(true);

            cardLayout = new CardLayout();
            mainPanel = new JPanel(cardLayout);

            MainMenuPanel menuPanel = new MainMenuPanel(mode -> {
                startGame(mode);
            });
            mainPanel.add(menuPanel, "menu");

            JPanel loadingPanel = new JPanel(new BorderLayout());
            loadingPanel.setBackground(new Color(15, 15, 40));
            loadingLabel = new JLabel("正在生成世界...", SwingConstants.CENTER);
            loadingLabel.setFont(new Font("微软雅黑", Font.BOLD, 28));
            loadingLabel.setForeground(new Color(0, 255, 136));
            loadingPanel.add(loadingLabel, BorderLayout.CENTER);
            mainPanel.add(loadingPanel, "loading");

            LANPanel lanPanel = new LANPanel((mode, net) -> {
                currentNet = net;
                startGame(mode);
            });
            mainPanel.add(lanPanel, "lan");

            frame.add(mainPanel);
            frame.setVisible(true);
        });
    }

    private static void startGame(Game.GameMode mode) {
        cardLayout.show(mainPanel, "loading");
        loadingLabel.setText("正在生成世界...");

        new Thread(() -> {
            try {
                Game game = new Game(mode);

                SwingUtilities.invokeLater(() -> {
                    try {
                        currentGame = game;

                        if (gamePanel != null) {
                            mainPanel.remove(gamePanel);
                        }

                        gamePanel = new GamePanel(currentGame, 1280, 720);
                        mainPanel.add(gamePanel, "game");
                        cardLayout.show(mainPanel, "game");
                        gamePanel.requestFocusInWindow();

                        if (gameTimer != null) {
                            gameTimer.stop();
                        }

                        gameTimer = new Timer(16, e -> {
                            gamePanel.updateGame(0.016);
                            gamePanel.repaint();

                            if (currentNet != null && currentNet.isConnected()) {
                                processNetwork();
                            }
                        });
                        gameTimer.start();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(frame, "启动游戏失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                        cardLayout.show(mainPanel, "menu");
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(frame, "生成世界失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    cardLayout.show(mainPanel, "menu");
                });
            } catch (OutOfMemoryError err) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(frame, "内存不足，无法生成世界", "错误", JOptionPane.ERROR_MESSAGE);
                    cardLayout.show(mainPanel, "menu");
                });
            }
        }).start();
    }

    private static double lastNetSendTime = 0;

    private static void processNetwork() {
        if (currentGame == null || currentNet == null) return;

        double now = System.currentTimeMillis() / 1000.0;
        if (now - lastNetSendTime > 0.05) {
            lastNetSendTime = now;
            Player p = currentGame.getPlayer();
            currentNet.send(NetPacket.playerPos(p.x, p.y, p.vx, p.vy, p.facingRight));
        }

        byte[] data;
        while ((data = currentNet.recv()) != null) {
            NetPacket.parse(data, new NetPacket.NetHandler() {
                public void onPlayerPos(double x, double y, double vx, double vy, boolean facingRight) {
                    AIPlayer ai = currentGame.getAIPlayer();
                    if (ai != null) {
                        ai.x = x; ai.y = y; ai.vx = vx; ai.vy = vy; ai.facingRight = facingRight;
                    }
                }
                public void onPlayerAction(int action, double targetX, double targetY) {}
                public void onBlockChange(int tx, int ty, int blockType) {
                    TerrainGenerator tgen = currentGame.getTerrainGen();
                    BlockType[] types = BlockType.values();
                    if (blockType >= 0 && blockType < types.length) {
                        tgen.setBlockType(tx, ty, types[blockType]);
                    }
                }
                public void onHealth(double hp, double shield) {}
                public void onChat(String message) {}
                public void onWorldSeed(long seed) {}
                public void onPing() {}
            });
        }

        if (currentNet.getState() == NetworkManager.NetState.DISCONNECTED) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(frame, "网络连接断开", "提示", JOptionPane.WARNING_MESSAGE);
            });
            currentNet = null;
        }
    }

    public static void returnToMenu() {
        if (gameTimer != null) {
            gameTimer.stop();
            gameTimer = null;
        }
        currentGame = null;
        if (currentNet != null) {
            currentNet.stop();
            currentNet = null;
        }
        cardLayout.show(mainPanel, "menu");
    }

    public static void showLANPanel() {
        cardLayout.show(mainPanel, "lan");
    }
}
