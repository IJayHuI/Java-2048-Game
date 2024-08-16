package src;
import javax.swing.*;
import java.awt.*;

public class Window {
    static Game gameInstance = new Game();
    static public JFrame frame = new JFrame();
    public static CardLayout cardLayout = new CardLayout();
    public static JLabel top = new JLabel(
            "<html><body><span style='font-size:50px; font-weight:900;'>2048</span></body></html>",
            JLabel.CENTER);
    public static JPanel middle = new JPanel(cardLayout);

    public static void window() {
        // 创建及设置窗口
        frame.setTitle("Jay的2048");
        frame.setSize(500, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);// 窗口居中
        // 布局结构
        JPanel main = new JPanel(new BorderLayout());

        main.add(top, BorderLayout.NORTH);
        main.add(middle, BorderLayout.CENTER);
        // 添加面板
        frame.add(main);

        // 首页
        index(middle, cardLayout, top);

        // 开始游戏页面
        game(middle, cardLayout, top);
        frame.setVisible(true);
    }

    // 首页
    public static void index(JPanel middle, CardLayout cardLayout, JLabel top) {
        JPanel buttons = new JPanel(); // 中间按钮
        JButton start = new JButton("开始游戏");
        start.setPreferredSize(new Dimension(150, 50));
        buttons.add(start);
        middle.add(buttons, "index");
        // 开始游戏按钮
        start.addActionListener((e) -> {
        top.setText("<html><body><span style='font-size:50px;font-weight:900;'>Game</span></body></html>");
        gameInstance.initGame();
        gameInstance.refresh();
        cardLayout.show(middle, "game");
        });
    }

    // 开始游戏
    public static void game(JPanel middle, CardLayout cardLayout, JLabel top) {
    JPanel gamePanel = new JPanel();
    gamePanel.setLayout(new BoxLayout(gamePanel, BoxLayout.Y_AXIS));
    JButton plugin = new JButton("一步到胃");
    JButton back = new JButton("返回");
    JPanel button = new JPanel();
    JPanel gameMainPanel = gameInstance.gamePanel(frame);
    JPanel gameInfo = gameInstance.gameInfo();
    gamePanel.add(gameMainPanel);
    gamePanel.add(gameInfo);
    button.add(back);
    button.add(plugin);
    gamePanel.add(new JPanel());
    gamePanel.add(button);
    middle.add(gamePanel, "game");
    // 返回按钮
    back.addActionListener((e) -> {
    top.setText("<html><body><span style='font-size:50px;font-weight:900;'>2048</span></body></html>");
    cardLayout.show(middle, "index");
    });

    // 一步到胃按钮
    plugin.addActionListener((e) -> {
    gameInstance.plugin(frame);
    });
    }
}