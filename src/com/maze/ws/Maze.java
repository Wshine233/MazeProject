package com.maze.ws;

import java.util.ArrayList;
import java.util.List;

/**
 * 迷宫类
 * 包含简化迷宫处理相关的方法
 *
 * @author Wshine
 */
public class Maze {
    static final int MAX_SIZE = 50000;
    public final int width;
    public final int height;
    private final Point[][] data;
    private Point start;
    private Point dest;

    /**
     * 路径点类
     * 迷宫子类，表示一个迷宫中对应的路径点
     *
     * @author Wshine
     */
    class Point {
        public final int row;
        public final int column;
        public final Maze maze;
        public int color = 0;
        private int value;

        /**
         * 构造方法，构造迷宫中的一个路径点对象
         *
         * @param maze   绑定的迷宫
         * @param row    行位置
         * @param column 列位置
         * @param value  值（0为可通行，1为障碍物）
         */
        private Point(Maze maze, int row, int column, int value) {
            this.row = row;
            this.column = column;
            setValue(value);
            this.maze = maze;
        }

        /**
         * 设置该路径点的值
         *
         * @param value 需要设置的值（0或1）
         */
        public void setValue(int value) {
            if (!validate(value, 0, 1)) {
                throw new IllegalArgumentException("迷宫块的数字只能为0和1");
            }
            this.value = value;
        }

        /**
         * 获取该路径点的值
         *
         * @return 该路径点的值
         */
        public int getValue() {
            return this.value;
        }

        /**
         * 获取两个路径点之间的曼哈顿距离
         *
         * @param p 另一个路径点
         * @return 两点之间的曼哈顿距离
         */
        public int distance(Point p) {
            return Math.abs(this.row - p.row) + Math.abs(this.column - p.column) + 1;
        }

        /**
         * 获得该路径点的一个拷贝
         *
         * @param maze 拷贝后所在的迷宫
         * @return 该路径点的一个拷贝
         */
        private Point getCopy(Maze maze) {
            Point copy = new Point(maze, row, column, value);
            copy.color = color;
            return copy;
        }
    }

    /**
     * 构造方法，构造一个空迷宫（迷宫的所有点均为可通行路径点）
     *
     * @param width    迷宫的宽度
     * @param height   迷宫的高度
     * @param startRow 起点所在行
     * @param startCol 起点所在列
     * @param destRow  终点所在行
     * @param destCol  终点所在列
     */
    public Maze(int width, int height, int startRow, int startCol, int destRow, int destCol) {
        if (!validate(width, 1, MAX_SIZE) || !validate(height, 1, MAX_SIZE)) {
            throw new IllegalArgumentException("迷宫宽高过大，宽度和高度只能为1~50000之间的整数");
        }

        this.width = width;
        this.height = height;
        this.data = new Point[height][width];
        init(0);
        setStartPoint(startRow, startCol);
        setDestPoint(destRow, destCol);

    }

    /**
     * 构造方法，根据现有的整数矩阵构造迷宫对象
     * 请用Maze.fromString(String, int, int, int, int)进行生成
     *
     * @param rawMaze  迷宫的整数矩阵
     * @param startRow 起点所在行
     * @param startCol 起点所在列
     * @param destRow  终点所在行
     * @param destCol  终点所在列
     */
    private Maze(int[][] rawMaze, int startRow, int startCol, int destRow, int destCol) {
        if (!validate(rawMaze.length, 1, MAX_SIZE) || !validate(rawMaze[0].length, 1, MAX_SIZE)) {
            throw new IllegalArgumentException("迷宫宽高超限，宽度和高度只能为1~50000之间的整数");
        }

        this.width = rawMaze[0].length;
        this.height = rawMaze.length;
        this.data = new Point[height][width];

        for (int i = 0; i < rawMaze.length; i++) {
            for (int j = 0; j < rawMaze[i].length; j++) {
                this.data[i][j] = new Point(this, i, j, rawMaze[i][j]);
            }
        }

        setStartPoint(startRow, startCol);
        setDestPoint(destRow, destCol);
    }

    /**
     * 给拷贝用的构造方法
     *
     * @param maze 用于拷贝的迷宫对象
     */
    private Maze(Maze maze) {
        this.width = maze.width;
        this.height = maze.height;
        this.data = new Point[height][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                data[i][j] = maze.data[i][j].getCopy(this);
            }
        }

        setStartPoint(maze.start.row, maze.start.column);
        setDestPoint(maze.dest.row, maze.dest.column);

    }

    /**
     * 判断一个整数是否在指定的闭区间内
     *
     * @param num  待判断的数
     * @param down 下界
     * @param up   上界
     * @return 判断结果
     */
    private boolean validate(int num, int down, int up) {
        return num >= down && num <= up;
    }

    /**
     * 初始化迷宫中的所有路径点为指定值
     *
     * @param value 初始化的值（0或1）
     */
    public void init(int value) {
        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                if (data[i][j] == null) {
                    this.data[i][j] = new Point(this, i, j, value);
                } else {
                    this.data[i][j].setValue(value);
                }
            }
        }
    }

    /**
     * 初始化迷宫中的所有路径点为指定颜色
     *
     * @param color 指定的颜色
     */
    public void initColor(int color) {
        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                if (data[i][j] == null) {
                    this.data[i][j] = new Point(this, i, j, 0);
                }
                this.data[i][j].color = color;
            }
        }
    }

    /**
     * 取得迷宫中指定行列的路径点
     *
     * @param row 指定行
     * @param col 指定列
     * @return 获取的路径点
     */
    public Point getUnit(int row, int col) {
        if (!validate(row, 0, height - 1) || !validate(col, 0, width - 1)) {
            return null;
        }
        return this.data[row][col];
    }

    /**
     * 取得该迷宫的起点
     *
     * @return 一个路径点，表示该迷宫的起点
     */
    public Point getStartPoint() {
        return start;
    }

    /**
     * 设置该迷宫的起点
     *
     * @param row 起点行
     * @param col 起点列
     */
    public void setStartPoint(int row, int col) {
        if (!validate(row, 0, height - 1) || !validate(col, 0, width - 1)) {
            throw new IllegalArgumentException("指定坐标超出迷宫的位置");
        }

        if (data[row][col] == null) {
            data[row][col] = new Point(this, row, col, 0);
        }
        start = data[row][col];
    }

    /**
     * 获取该迷宫的终点
     *
     * @return 一个路径点，表示该迷宫的终点
     */
    public Point getDestPoint() {
        return dest;
    }

    /**
     * 设置该迷宫的终点
     *
     * @param row 终点行
     * @param col 终点列
     */
    public void setDestPoint(int row, int col) {
        if (!validate(row, 0, height - 1) || !validate(col, 0, width - 1)) {
            throw new IllegalArgumentException("指定坐标超出迷宫的位置");
        }

        if (data[row][col] == null) {
            data[row][col] = new Point(this, row, col, 0);
        }
        dest = data[row][col];
    }

    /**
     * 判断指定行列是否为迷宫的起点
     *
     * @param row 指定行
     * @param col 指定列
     * @return 一个布尔值，代表是否为起点
     */
    public boolean isStartPoint(int row, int col) {
        return row == start.row && col == start.column;
    }

    /**
     * 判断指定的路径点是否为该迷宫的起点
     * 注意，如果该路径点绑定的迷宫不同，就算坐标正确也并非是起点
     *
     * @param p 用于判断的路径点
     * @return 一个布尔值，代表是否为起点
     */
    public boolean isStartPoint(Point p) {
        return p.equals(start);
    }

    /**
     * 判断指定行列是否为迷宫的终点
     *
     * @param row 指定行
     * @param col 指定列
     * @return 一个布尔值，代表是否为终点
     */
    public boolean isDestPoint(int row, int col) {
        return row == dest.row && col == dest.column;
    }

    /**
     * 判断指定的路径点是否为该迷宫的终点
     * 注意，如果该路径点绑定的迷宫不同，就算坐标正确也并非是终点
     *
     * @param p 用于判断的路径点
     * @return 一个布尔值，代表是否为终点
     */
    public boolean isDestPoint(Point p) {
        return p.equals(dest);
    }

    /**
     * 判断指定的路径点是否可通行
     *
     * @param p 用于判断的路径点
     * @return 一个布尔值，代表该点是否可通行
     * @throws IllegalArgumentException 路径点不属于该迷宫时抛出
     */
    public boolean canPass(Point p) {
        if (!p.maze.equals(this)) {
            throw new IllegalArgumentException("路径点不属于该迷宫");
        }

        return p.getValue() != 1;
    }

    /**
     * 获得指定路径点四周的所有障碍物
     *
     * @param p 指定的路径点
     * @return 一个列表，包含指定路径点四周所有被设置为障碍物的路径点
     * @throws IllegalArgumentException 路径点不属于该迷宫时抛出
     */
    public List<Point> getWalls(Point p) {
        if (!p.maze.equals(this)) {
            throw new IllegalArgumentException("路径点不属于该迷宫");
        }

        List<Point> walls = new ArrayList<>();
        int[][] directions = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};

        for (int[] direction : directions) {
            Point nextPoint = getUnit(p.row + direction[0], p.column + direction[1]);
            if (nextPoint != null && !canPass(nextPoint)) {
                walls.add(nextPoint);
            }
        }

        return walls;
    }

    /**
     * 获得整个迷宫的障碍物
     *
     * @return 一个列表，包含整个迷宫被设置为障碍物的路径点
     */
    public List<Point> getAllWalls() {
        List<Point> walls = new ArrayList<>();

        for (Point[] points : data) {
            for (Point point : points) {
                if (!canPass(point)) {
                    walls.add(point);
                }
            }
        }

        return walls;
    }

    /**
     * 获得整个迷宫的障碍物（指定颜色）
     *
     * @return 一个列表，包含整个迷宫被设置为障碍物的路径点（指定颜色）
     */
    public List<Point> getAllWalls(int color) {
        List<Point> walls = new ArrayList<>();

        for (Point[] points : data) {
            for (Point point : points) {
                if (!canPass(point) && point.color == color) {
                    walls.add(point);
                }
            }
        }

        return walls;
    }

    /**
     * 获得指定路径点四周的所有可通行路径点
     *
     * @param p 指定的路径点
     * @return 一个列表，包含指定路径点四周所有被设置为可通行的路径点
     * @throws IllegalArgumentException 路径点不属于该迷宫时抛出
     */
    public List<Point> getPasses(Point p) {
        if (!p.maze.equals(this)) {
            throw new IllegalArgumentException("路径点不属于该迷宫");
        }

        List<Point> walls = new ArrayList<>();
        int[][] directions = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};

        for (int[] direction : directions) {
            Point nextPoint = getUnit(p.row + direction[0], p.column + direction[1]);
            if (nextPoint != null && canPass(nextPoint)) {
                walls.add(nextPoint);
            }
        }

        return walls;
    }

    /**
     * 获得整个迷宫的可通行路径点
     *
     * @return 一个列表，包含整个迷宫被设置为可通行的路径点
     */
    public List<Point> getAllPasses() {
        List<Point> walls = new ArrayList<>();

        for (Point[] points : data) {
            for (Point point : points) {
                if (canPass(point)) {
                    walls.add(point);
                }
            }
        }

        return walls;
    }

    /**
     * 获得整个迷宫的可通行路径点（指定颜色）
     *
     * @return 一个列表，包含整个迷宫被设置为可通行的路径点（指定颜色）
     */
    public List<Point> getAllPasses(int color) {
        List<Point> walls = new ArrayList<>();

        for (Point[] points : data) {
            for (Point point : points) {
                if (canPass(point) && point.color == color) {
                    walls.add(point);
                }
            }
        }

        return walls;
    }

    /**
     * 计算该迷宫的障碍密度
     *
     * @return 一个浮点数，代表该迷宫的障碍密度
     */
    public double countAlpha() {
        return getAllWalls().size() * 100.0 / width / height;
    }

    /**
     * 判断指定的路径点是否为死路（即无法继续通行）
     *
     * @param p 指定的路径点
     * @return 一个布尔值，代表是否为死路
     * @throws IllegalArgumentException 路径点不属于该迷宫时抛出
     */
    public boolean isDeadEnd(Point p) {
        if (!p.maze.equals(this)) {
            throw new IllegalArgumentException("路径点不属于该迷宫");
        }

        return getPasses(p).size() <= 1;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        String[] x = {"0", "1"};
        for (Point[] datum : data) {
            for (int j = 0; j < data[0].length; j++) {
                s.append(x[datum[j].value]).append(" ");
            }
            s.append("\n");
        }

        return s.toString();
    }

    /**
     * 获取当前迷宫的一个拷贝
     *
     * @return 当前迷宫的一个拷贝
     */
    public Maze getCopy() {
        return new Maze(this);
    }

    /**
     * 通过以空格分隔的01矩阵字符串生成迷宫对象
     *
     * @param s        以空格分隔的01矩阵字符串
     * @param startRow 起点所在行
     * @param startCol 起点所在列
     * @param destRow  终点所在行
     * @param destCol  终点所在列
     * @return 生成的迷宫对象
     */
    public static Maze fromString(String s, int startRow, int startCol, int destRow, int destCol) {
        s = s.replace("\r\n", "\n");
        s = s.replace("\r", "\n");
        String[] rawRow = s.split("\n");
        int i = 0;
        int j;
        int height = rawRow.length;
        int width = rawRow[0].split(" ").length;
        int[][] rawMaze = new int[height][width];

        for (String row : s.split("\n")) {
            j = 0;
            for (String col : row.split(" ")) {
                rawMaze[i][j] = Integer.parseInt(col);
                j++;
            }
            i++;
        }

        return new Maze(rawMaze, startRow, startCol, destRow, destCol);
    }


}
