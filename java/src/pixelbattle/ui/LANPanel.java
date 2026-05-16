package pixelbattle.ui;

import pixelbattle.game.engine.Game;
import pixelbattle.game.network.NetworkManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LANPanel extends JPanel {
    private NetworkManager net;
    private GameStarter gameStarter;
    private CardLayout cardLayout;
    private JPanel cards;

    public interface GameStarter {
        void startLANGame(Game.GameMode mode, NetworkManager net);
    }

    public LANPanel(GameStarter starter) {
        this.gameStarter = starter;
        setLayout(new BorderLayout());
        setBackground(new Color(15, 15, 40));

        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);
        cards.setOpaque(false);

        cards.add(createMainPanel(), "main");
        cards.add(createHostPanel(), "host");
        cards.add(createJoinPanel(), "join");

        add(cards, BorderLayout.CENTER);

        JButton backBtn = new JButton("返回主菜单");
        backBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        backBtn.setBackground(new Color(231, 76, 60));
        backBtn.setForeground(Color.WHITE);
        backBtn.setOpaque(true);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> {
            if (net != null) net.stop();
            pixelbattle.Main.returnToMenu();
        });
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setOpaque(false);
        bottomPanel.add(backBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("局域网对战", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 32));
        title.setForeground(new Color(0, 255, 136));
        title.setBorder(BorderFactory.createEmptyBorder(30, 0, 20, 0));
        panel.add(title, gbc);

        gbc.gridy++;
        JButton hostBtn = createMenuButton("创建房间", new Color(46, 204, 113));
        hostBtn.addActionListener(e -> {
            net = new NetworkManager();
            if (net.startHost()) {
                cardLayout.show(cards, "host");
                updateHostInfo();
            } else {
                JOptionPane.showMessageDialog(this, net.getDisconnectReason(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(hostBtn, gbc);

        gbc.gridy++;
        JButton joinBtn = createMenuButton("加入房间", new Color(52, 152, 219));
        joinBtn.addActionListener(e -> cardLayout.show(cards, "join"));
        panel.add(joinBtn, gbc);

        return panel;
    }

    private JPanel createHostPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(6, 0, 6, 0);

        JLabel title = new JLabel("创建房间", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 24));
        title.setForeground(new Color(46, 204, 113));
        panel.add(title, gbc);

        gbc.gridy++;
        JLabel infoLabel = new JLabel("你的IP地址:", SwingConstants.CENTER);
        infoLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        infoLabel.setForeground(Color.WHITE);
        panel.add(infoLabel, gbc);

        gbc.gridy++;
        JLabel ipLabel = new JLabel("获取中...", SwingConstants.CENTER);
        ipLabel.setFont(new Font("Arial", Font.BOLD, 20));
        ipLabel.setForeground(new Color(255, 215, 0));
        ipLabel.setName("ipLabel");
        panel.add(ipLabel, gbc);

        gbc.gridy++;
        JLabel portLabel = new JLabel("端口: 25565", SwingConstants.CENTER);
        portLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        portLabel.setForeground(Color.LIGHT_GRAY);
        panel.add(portLabel, gbc);

        gbc.gridy++;
        JLabel statusLabel = new JLabel("等待其他玩家加入...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        statusLabel.setForeground(new Color(255, 200, 0));
        statusLabel.setName("statusLabel");
        panel.add(statusLabel, gbc);

        gbc.gridy++;
        JLabel hintLabel = new JLabel("请将IP地址告诉其他玩家", SwingConstants.CENTER);
        hintLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        hintLabel.setForeground(Color.GRAY);
        panel.add(hintLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(20, 0, 6, 0);
        JButton startBtn = createMenuButton("开始游戏", new Color(46, 204, 113));
        startBtn.setName("startBtn");
        startBtn.addActionListener(e -> {
            if (net != null && net.isConnected()) {
                gameStarter.startLANGame(Game.GameMode.SURVIVAL, net);
            } else {
                JOptionPane.showMessageDialog(this, "还没有玩家连接，是否以单人模式开始？", "提示", JOptionPane.YES_NO_OPTION);
            }
        });
        panel.add(startBtn, gbc);

        gbc.gridy++;
        JButton cancelBtn = createMenuButton("取消", new Color(231, 76, 60));
        cancelBtn.addActionListener(e -> {
            if (net != null) net.stop();
            cardLayout.show(cards, "main");
        });
        panel.add(cancelBtn, gbc);

        return panel;
    }

    private JPanel createJoinPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(6, 0, 6, 0);

        JLabel title = new JLabel("加入房间", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 24));
        title.setForeground(new Color(52, 152, 219));
        panel.add(title, gbc);

        gbc.gridy++;
        JLabel hint = new JLabel("输入主机IP地址:", SwingConstants.CENTER);
        hint.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        hint.setForeground(Color.WHITE);
        panel.add(hint, gbc);

        gbc.gridy++;
        JTextField ipField = new JTextField("192.168.", 20);
        ipField.setFont(new Font("Arial", Font.PLAIN, 16));
        ipField.setHorizontalAlignment(JTextField.CENTER);
        ipField.setPreferredSize(new Dimension(250, 36));
        panel.add(ipField, gbc);

        gbc.gridy++;
        JLabel portHint = new JLabel("端口: 25565", SwingConstants.CENTER);
        portHint.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        portHint.setForeground(Color.GRAY);
        panel.add(portHint, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(20, 0, 6, 0);
        JButton connectBtn = createMenuButton("连接", new Color(52, 152, 219));
        connectBtn.addActionListener(e -> {
            String ip = ipField.getText().trim();
            if (ip.isEmpty()) {
                JOptionPane.showMessageDialog(this, "请输入IP地址", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            connectBtn.setText("连接中...");
            connectBtn.setEnabled(false);
            new Thread(() -> {
                net = new NetworkManager();
                boolean ok = net.connect(ip);
                SwingUtilities.invokeLater(() -> {
                    connectBtn.setText("连接");
                    connectBtn.setEnabled(true);
                    if (ok) {
                        gameStarter.startLANGame(Game.GameMode.SURVIVAL, net);
                    } else {
                        JOptionPane.showMessageDialog(this, net.getDisconnectReason(), "连接失败", JOptionPane.ERROR_MESSAGE);
                        net.stop();
                    }
                });
            }).start();
        });
        panel.add(connectBtn, gbc);

        gbc.gridy++;
        JButton backBtn = createMenuButton("返回", new Color(149, 165, 166));
        backBtn.addActionListener(e -> cardLayout.show(cards, "main"));
        panel.add(backBtn, gbc);

        return panel;
    }

    private void updateHostInfo() {
        for (Component c : getComponents()) {
            updateHostInfoRecursive(c);
        }
        if (net != null && net.getState() == NetworkManager.NetState.WAITING) {
            new Thread(() -> {
                while (net.getState() == NetworkManager.NetState.WAITING) {
                    try { Thread.sleep(500); } catch (InterruptedException e) { break; }
                }
                if (net.getState() == NetworkManager.NetState.CONNECTED) {
                    SwingUtilities.invokeLater(() -> updateHostInfoRecursive(this));
                }
            }).start();
        }
    }

    private void updateHostInfoRecursive(Component c) {
        if (c instanceof Container) {
            for (Component child : ((Container)c).getComponents()) {
                if ("ipLabel".equals(child.getName()) && net != null) {
                    ((JLabel)child).setText(net.getHostAddress() + ":" + net.getPort());
                }
                if ("statusLabel".equals(child.getName()) && net != null) {
                    if (net.isConnected()) {
                        ((JLabel)child).setText("玩家已连接! 可以开始游戏");
                        ((JLabel)child).setForeground(new Color(46, 204, 113));
                    } else {
                        ((JLabel)child).setText("等待其他玩家加入...");
                        ((JLabel)child).setForeground(new Color(255, 200, 0));
                    }
                }
                updateHostInfoRecursive(child);
            }
        }
    }

    private JButton createMenuButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setPreferredSize(new Dimension(240, 42));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btn.setBackground(color.brighter()); }
            @Override
            public void mouseExited(MouseEvent e) { btn.setBackground(color); }
        });
        return btn;
    }

    public NetworkManager getNetworkManager() { return net; }
}
