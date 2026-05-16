package pixelbattle.ui;

import pixelbattle.game.engine.Game;
import pixelbattle.Main;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainMenuPanel extends JPanel {
    private GameStarter gameStarter;

    public interface GameStarter {
        void startGame(Game.GameMode mode);
    }

    public MainMenuPanel(GameStarter starter) {
        this.gameStarter = starter;
        setLayout(new GridBagLayout());
        setBackground(new Color(15, 15, 40));

        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("像素大战", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 48));
        title.setForeground(new Color(0, 255, 136));
        title.setBorder(BorderFactory.createEmptyBorder(40, 0, 5, 0));
        content.add(title, gbc);

        gbc.gridy++;
        JLabel subtitle = new JLabel("Pixel Battle", SwingConstants.CENTER);
        subtitle.setFont(new Font("Arial", Font.PLAIN, 20));
        subtitle.setForeground(new Color(100, 200, 150));
        subtitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        content.add(subtitle, gbc);

        gbc.gridy++;
        content.add(createButton("生存模式", new Color(46, 204, 113), e -> gameStarter.startGame(Game.GameMode.SURVIVAL)), gbc);

        gbc.gridy++;
        content.add(createButton("人机对战", new Color(52, 152, 219), e -> gameStarter.startGame(Game.GameMode.PVE)), gbc);

        gbc.gridy++;
        content.add(createButton("创造模式", new Color(155, 89, 182), e -> gameStarter.startGame(Game.GameMode.CREATIVE)), gbc);

        gbc.gridy++;
        content.add(createButton("局域网对战", new Color(241, 196, 15), e -> Main.showLANPanel()), gbc);

        gbc.gridy++;
        content.add(createButton("在线对战", new Color(230, 126, 34), e -> JOptionPane.showMessageDialog(this, "在线对战功能开发中，敬请期待！", "提示", JOptionPane.INFORMATION_MESSAGE)), gbc);

        gbc.gridy++;
        content.add(createButton("设置", new Color(149, 165, 166), e -> JOptionPane.showMessageDialog(this, "设置功能开发中", "提示", JOptionPane.INFORMATION_MESSAGE)), gbc);

        gbc.gridy++;
        content.add(createButton("退出游戏", new Color(231, 76, 60), e -> System.exit(0)), gbc);

        add(content);
    }

    private JButton createButton(String text, Color color, ActionListener action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setPreferredSize(new Dimension(280, 48));
        btn.setMinimumSize(new Dimension(280, 48));
        btn.setMaximumSize(new Dimension(280, 48));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(color.brighter());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(color);
            }
        });

        return btn;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        int w = getWidth();
        int h = getHeight();
        for (int y = 0; y < h; y += 4) {
            float ratio = (float) y / h;
            Color c = new Color(
                (int)(15 + ratio * 10),
                (int)(15 + ratio * 15),
                (int)(40 + ratio * 30)
            );
            g2d.setColor(c);
            g2d.fillRect(0, y, w, 4);
        }
    }
}
