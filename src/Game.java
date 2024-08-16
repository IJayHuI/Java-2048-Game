package src;
import javax.swing.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Game {
    static Integer[] mapSizeChoose = { 4, 5, 6 };
    public static int mapSize = mapSizeChoose[0];
    public static int step = 0;
    public static int grade = 0;
    public static long time = 0;
    private Timer timer;
    private long startTime;
    private long currentTime;
    public static JPanel gamePanel = new JPanel();
    JLabel gameInfoLabel = new JLabel("步数：" + step + "  分数：" + grade + "  用时：" + currentTime + "秒");

    // 用于储存颜色的实体类
    private static class Color {
        public Color(int fc, int bgc) {
            fontColor = fc;// 字体颜色
            bgColor = bgc;// 背景颜色
        }

        public int fontColor;// 字体颜色
        public int bgColor;// 背景颜色
    }

    JLabel[] jLabels;// 方块，用jlabel代替

    // 每个方块上的数值
    int[] datas = new int[mapSize * mapSize];

    int[] temp = new int[mapSize];// 方块移动算法中抽离的的临时数组
    int[] temp2 = new int[mapSize * mapSize];// 用于检测方块是否有合并

    List emptyBlocks = new ArrayList<Integer>(mapSize * mapSize);// 在生成新方块时用到的临时list，用以存放空方块

    // 存放颜色的map
    static HashMap<Integer, Color> colorMap = new HashMap<Integer, Color>() {
        {
            put(0, new Color(0x776e65, 0xCDC1B4));
            put(2, new Color(0x776e65, 0xeee4da));
            put(4, new Color(0x776e65, 0xede0c8));
            put(8, new Color(0xf9f6f2, 0xf2b179));
            put(16, new Color(0xf9f6f2, 0xf59563));
            put(32, new Color(0xf9f6f2, 0xf67c5f));
            put(64, new Color(0xf9f6f2, 0xf65e3b));
            put(128, new Color(0xf9f6f2, 0xedcf72));
            put(256, new Color(0xf9f6f2, 0xedcc61));
            put(512, new Color(0xf9f6f2, 0xe4c02a));
            put(1024, new Color(0xf9f6f2, 0xe2ba13));
            put(2048, new Color(0xf9f6f2, 0xecc400));
        }
    };

    // 初始化游戏
    public void initGame() {
        Object a = JOptionPane.showInputDialog(null, "请输入你想要的地图大小，地图越大越简单", "新游戏",
                JOptionPane.INFORMATION_MESSAGE,
                null, mapSizeChoose, mapSizeChoose[0]);
        if (a != null) {
            mapSize = (Integer) a;
            gamePanel.removeAll();
            gamePanel(Window.frame);
            temp = new int[mapSize];
            temp2 = new int[mapSize * mapSize];
            GameTimer();
            datas = new int[mapSize * mapSize];
            for (int i = 0; i < mapSize * mapSize; i++)
                datas[i] = 0;
            generateBlock(datas, 2);
            generateBlock(datas, 4);
            step = 0;
            grade = 0;
            currentTime = 0;
            startTime = System.currentTimeMillis();
            timer.start();
        } else {
            SwingUtilities.invokeLater(() -> {
                Window.top.setText(
                        "<html><body><span style='font-size:50px; font-weight:900;'>2048</span></body></html>");
                Window.cardLayout.show(Window.middle, "index");
            });
        }
    }

    // 随机生成4或者2的方块
    public void randomGenerate(int arr[]) {
        int ran = (int) (Math.random() * 10);
        if (ran > 5)
            generateBlock(arr, 4);
        else
            generateBlock(arr, 2);
    }

    // 随机生成新的方块，参数：要生成的方块数值
    public void generateBlock(int arr[], int num) {
        emptyBlocks.clear();
        for (int i = 0; i < mapSize * mapSize; i++) {
            if (arr[i] == 0)
                emptyBlocks.add(i);
        }
        int len = emptyBlocks.size();
        if (len == 0) {
            return;
        }
        int pos = (int) (Math.random() * 100) % len;
        arr[(int) emptyBlocks.get(pos)] = num;
        refresh();
    }

    // 胜负判定并做终局处理
    public void judge(int arr[]) {
        if (isWin(arr)) {
            timer.stop();
            JOptionPane.showMessageDialog(null, "恭喜，你已经成功凑出2048的方块", "你赢了", JOptionPane.PLAIN_MESSAGE);
            // 初始化
            initGame();
            refresh();
        }
        if (isEnd(arr)) {
            timer.stop();
            int max = getMax(datas);
            JOptionPane.showMessageDialog(null, "抱歉，你没有凑出2048的方块,你的最大方块是：" + max, "游戏结束", JOptionPane.PLAIN_MESSAGE);
            initGame();
            refresh();
        }
    }

    // 判断玩家是否胜利，只要有一个方块大于等于2048即为胜利
    public boolean isWin(int arr[]) {
        for (int i : arr) {
            if (i >= 2048)
                return true;
        }
        return false;
    }

    // 此函数用于判断游戏是否结束，如上下左右移后均无法产生空块，即代表方块已满，则返回真，表示游戏结束
    public boolean isEnd(int arr[]) {

        int[] tmp = new int[mapSize * mapSize];
        int isend = 0;

        System.arraycopy(arr, 0, tmp, 0, mapSize * mapSize);
        checkCombineLeft(tmp);
        if (isNoBlank(tmp))
            isend++;

        System.arraycopy(arr, 0, tmp, 0, mapSize * mapSize);
        checkCombineRight(tmp);
        if (isNoBlank(tmp))
            isend++;

        System.arraycopy(arr, 0, tmp, 0, mapSize * mapSize);
        checkCombineUp(tmp);
        if (isNoBlank(tmp))
            isend++;

        System.arraycopy(arr, 0, tmp, 0, mapSize * mapSize);
        checkCombineDown(tmp);
        if (isNoBlank(tmp))
            isend++;
        if (isend == 4)
            return true;
        else
            return false;
    }

    // 判断是否无空方块
    public boolean isNoBlank(int arr[]) {
        for (int i : arr) {
            if (i == 0)
                return false;
        }
        return true;
    }

    // 获取最大的方块数值
    public int getMax(int arr[]) {
        int max = arr[0];
        for (int i : arr) {
            if (i >= max)
                max = i;
        }
        return max;
    }

    // 刷新每个方块显示的数据
    public void refresh() {
        JLabel j;
        for (int i = 0; i < mapSize * mapSize; i++) {
            int arr = datas[i];
            j = jLabels[i];
            if (arr == 0) {
                j.setText("");
            } else if (arr >= 1024) {
                j.setFont(new java.awt.Font("Dialog", 1, 42));
                j.setText(String.valueOf(datas[i]));
            } else {
                j.setFont(new java.awt.Font("Dialog", 1, 50));
                j.setText(String.valueOf(arr));
            }

            Color currColor = colorMap.get(arr);
            j.setBackground(new java.awt.Color(currColor.bgColor));
            j.setForeground(new java.awt.Color(currColor.fontColor));
            gameInfoLabel.setText("步数：" + step + "  分数：" + grade + "  用时：" + currentTime + "秒");
        }
    }

    public void left(int arr[]) {
        moveLeft(arr);
        combineLeft(arr);
        moveLeft(arr);// 合并完后会产生空位，所以要再次左移
    }

    // 向左合并方块
    public void combineLeft(int arr[]) {
        for (int l = 0; l < mapSize; l++) {
            // l 为行
            for (int i = 0; i < mapSize - 1; i++) {
                if ((arr[l * mapSize + i] != 0 && arr[l * mapSize + i + 1] != 0)
                        && arr[l * mapSize + i] == arr[l * mapSize + i + 1]) {
                    arr[l * mapSize + i] *= 2;
                    arr[l * mapSize + i + 1] = 0;
                    updateScore(arr[l * mapSize + i]);
                }
            }
        }
    }

    public void checkCombineLeft(int[] arr) {
        for (int l = 0; l < mapSize; l++) {
            for (int i = 0; i < mapSize - 1; i++) {
                if ((arr[l * mapSize + i] != 0 && arr[l * mapSize + i + 1] != 0)
                        && arr[l * mapSize + i] == arr[l * mapSize + i + 1]) {
                    arr[l * mapSize + i] *= 2;
                    arr[l * mapSize + i + 1] = 0;
                }
            }
        }
    }

    // 方块左移，针对每一行利用临时数组实现左移
    public void moveLeft(int arr[]) {
        for (int l = 0; l < mapSize; l++) {// line
            int z = 0, fz = 0;// z(零）;fz（非零）
            for (int i = 0; i < mapSize; i++) {
                if (arr[l * mapSize + i] == 0) {
                    z++;
                } else {
                    temp[fz] = arr[l * mapSize + i];
                    fz++;
                }
            }
            for (int i = fz; i < mapSize; i++) {
                temp[i] = 0;
            }
            for (int j = 0; j < mapSize; j++) {
                arr[l * mapSize + j] = temp[j];
            }
        }
    }

    public void right(int arr[]) {
        moveRight(arr);
        combineRight(arr);
        moveRight(arr);
    }

    public void combineRight(int arr[]) {
        for (int l = 0; l < mapSize; l++) {
            for (int i = mapSize - 1; i > 0; i--) {
                if ((arr[l * mapSize + i] != 0 && arr[l * mapSize + i - 1] != 0)
                        && arr[l * mapSize + i] == arr[l * mapSize + i - 1]) {
                    arr[l * mapSize + i] *= 2;
                    arr[l * mapSize + i - 1] = 0;
                    updateScore(arr[l * mapSize + i]);
                }
            }
        }
    }

    public void checkCombineRight(int arr[]) {
        for (int l = 0; l < mapSize; l++) {
            for (int i = mapSize - 1; i > 0; i--) {
                if ((arr[l * mapSize + i] != 0 && arr[l * mapSize + i - 1] != 0)
                        && arr[l * mapSize + i] == arr[l * mapSize + i - 1]) {
                    arr[l * mapSize + i] *= 2;
                    arr[l * mapSize + i - 1] = 0;
                }
            }
        }
    }

    public void moveRight(int arr[]) {
        for (int l = 0; l < mapSize; l++) {
            int z = mapSize - 1, fz = mapSize - 1;// z(零）;fz（非零）
            for (int i = mapSize - 1; i >= 0; i--) {
                if (arr[l * mapSize + i] == 0) {
                    z--;
                } else {
                    temp[fz] = arr[l * mapSize + i];
                    fz--;
                }
            }
            for (int i = fz; i >= 0; i--) {
                temp[i] = 0;
            }
            for (int j = mapSize - 1; j >= 0; j--) {
                arr[l * mapSize + j] = temp[j];
            }
        }
    }

    public void up(int arr[]) {
        moveUp(arr);
        combineUp(arr);
        moveUp(arr);
    }

    public void combineUp(int arr[]) {
        for (int r = 0; r < mapSize; r++) {
            for (int i = 0; i < mapSize - 1; i++) {
                if ((arr[r + mapSize * i] != 0 && arr[r + mapSize * (i + 1)] != 0)
                        && arr[r + mapSize * i] == arr[r + mapSize * (i + 1)]) {
                    arr[r + mapSize * i] *= 2;
                    arr[r + mapSize * (i + 1)] = 0;
                    updateScore(arr[r + mapSize * i]);
                }
            }
        }
    }

    public void checkCombineUp(int arr[]) {
        for (int r = 0; r < mapSize; r++) {
            for (int i = 0; i < mapSize - 1; i++) {
                if ((arr[r + mapSize * i] != 0 && arr[r + mapSize * (i + 1)] != 0)
                        && arr[r + mapSize * i] == arr[r + mapSize * (i + 1)]) {
                    arr[r + mapSize * i] *= 2;
                    arr[r + mapSize * (i + 1)] = 0;
                }
            }
        }
    }

    public void moveUp(int arr[]) {
        for (int r = 0; r < mapSize; r++) {
            int z = 0, fz = 0;// z(零）;fz（非零）
            for (int i = 0; i < mapSize; i++) {
                if (arr[r + mapSize * i] == 0) {
                    z++;
                } else {
                    temp[fz] = arr[r + mapSize * i];
                    fz++;
                }
            }
            for (int i = fz; i < mapSize; i++) {
                temp[i] = 0;
            }
            for (int j = 0; j < mapSize; j++) {
                arr[r + mapSize * j] = temp[j];
            }
        }
    }

    public void down(int arr[]) {
        moveDown(arr);
        combineDown(arr);
        moveDown(arr);
    }

    public void combineDown(int arr[]) {
        for (int r = 0; r < mapSize; r++) {
            for (int i = mapSize - 1; i > 0; i--) {
                if ((arr[r + mapSize * i] != 0 && arr[r + mapSize * (i - 1)] != 0)
                        && arr[r + mapSize * i] == arr[r + mapSize * (i - 1)]) {
                    arr[r + mapSize * i] *= 2;
                    arr[r + mapSize * (i - 1)] = 0;
                    updateScore(arr[r + mapSize * i]);
                }
            }
        }
    }

    public void checkCombineDown(int arr[]) {
        for (int r = 0; r < mapSize; r++) {
            for (int i = mapSize - 1; i > 0; i--) {
                if ((arr[r + mapSize * i] != 0 && arr[r + mapSize * (i - 1)] != 0)
                        && arr[r + mapSize * i] == arr[r + mapSize * (i - 1)]) {
                    arr[r + mapSize * i] *= 2;
                    arr[r + mapSize * (i - 1)] = 0;
                }
            }
        }
    }

    public void moveDown(int arr[]) {
        for (int r = 0; r < mapSize; r++) {
            int z = mapSize - 1, fz = mapSize - 1;// z(零）;fz（非零）
            for (int i = mapSize - 1; i >= 0; i--) {
                if (arr[r + mapSize * i] == 0) {
                    z--;
                } else {
                    temp[fz] = arr[r + mapSize * i];
                    fz--;
                }
            }
            for (int i = fz; i >= 0; i--) {
                temp[i] = 0;
            }
            for (int j = mapSize - 1; j >= 0; j--) {
                arr[r + mapSize * j] = temp[j];
            }
        }
    }

    public JPanel gamePanel(JFrame frame) {
        gamePanel.setLayout(new GridLayout(mapSize, mapSize));
        gamePanel.setBackground(new java.awt.Color(0xCDC1B4));

        keyDownListener(frame);

        // 使用JLabel代表数字块
        jLabels = new JLabel[mapSize * mapSize];
        JLabel j; // 引用复用，避免for里创建过多引用
        for (int i = 0; i < mapSize * mapSize; i++) {
            jLabels[i] = new JLabel("0", JLabel.CENTER);
            j = jLabels[i];
            j.setOpaque(true);
            // 设置边界，参数：上，左，下，右，边界颜色
            j.setBorder(BorderFactory.createMatteBorder(6, 6, 6, 6, new java.awt.Color(0xBBADA0)));

            j.setForeground(new java.awt.Color(0x776E65));
            j.setFont(new java.awt.Font("Dialog", 1, 52));
            gamePanel.add(j);
        }
        return gamePanel;
    }

    public JPanel gameInfo() {
        JPanel gameInfo = new JPanel();

        gameInfo.add(gameInfoLabel);
        return gameInfo;
    }

    public void keyDownListener(JFrame frame) {
        // 按键监听
        frame.setFocusable(true);
        frame.requestFocusInWindow();
        // 获取并移除所有现有的按键监听器
        KeyListener[] listeners = frame.getKeyListeners();
        for (KeyListener listener : listeners) {
            frame.removeKeyListener(listener);
        }
        frame.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {
            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {

                System.arraycopy(datas, 0, temp2, 0, mapSize * mapSize);
                // 根据按键的不同调用不同的处理函数
                if (keyEvent.getKeyCode() == 40 || keyEvent.getKeyCode() == 83) {
                    step++;
                    down(datas);
                } else if (keyEvent.getKeyCode() == 38 || keyEvent.getKeyCode() == 87) {
                    step++;
                    up(datas);
                } else if (keyEvent.getKeyCode() == 37 || keyEvent.getKeyCode() == 65) {
                    step++;
                    left(datas);
                } else if (keyEvent.getKeyCode() == 39 || keyEvent.getKeyCode() == 68) {
                    step++;
                    right(datas);
                }

                // 判断移动后是否有方块合并，若有，生成新方块，若无，不产生新方块
                if (!Arrays.equals(datas, temp2)) {
                    randomGenerate(datas);
                }

                refresh();
                judge(datas);
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {
            }
        });
    }

    // 一步到胃
    public void plugin(JFrame frame) {
        datas = new int[mapSize * mapSize];
        for (int i = 0; i < mapSize * mapSize; i++)
            datas[i] = 1024;
        keyDownListener(frame);
        refresh();
    }

    public void GameTimer() {
        // 创建定时器，每1000毫秒（1秒）触发一次
        timer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                currentTime = elapsedTime / 1000;
                gameInfoLabel.setText("步数：" + step + " 分数：" + grade + " 用时：" + currentTime + "秒");
            }
        });
    }

    public void updateScore(int newValue) {
        switch (newValue) {
            case 4:
                grade += 4;
                break;
            case 8:
                grade += 8;
                break;
            case 16:
                grade += 16;
                break;
            case 32:
                grade += 32;
                break;
            case 64:
                grade += 64;
                break;
            case 128:
                grade += 128;
                break;
            case 256:
                grade += 256;
                break;
            case 512:
                grade += 512;
                break;
            case 1024:
                grade += 1024;
                break;
            case 2048:
                grade += 2048;
                break;
            default:
                break;
        }
    }
}
