package com.maze.ws;

import java.io.IOException;
import java.util.*;

/**
 * 迷宫生成类
 *
 * @author Wshine
 */
public class MazeGenerator {
    public final int width;
    public final int height;
    public final int alpha;
    private final Maze maze;
    private final Random rand = new Random();

    private final int amountPassing;

    public MazeGenerator(int width,int height, int alpha, int startRow, int startCol, int destRow, int destCol) {
        Maze cache = new Maze(width, height, startRow, startCol, destRow, destCol);
        amountPassing = width * height - (int) (width * height * alpha / 100.0 + 0.5);
        if (amountPassing < cache.getStartPoint().distance(cache.getDestPoint())) {
            throw new IllegalArgumentException("障碍物密度过高，生成迷宫无解。");
        }

        this.width = width;
        this.height = height;
        this.alpha = alpha;
        this.maze = cache;
    }

    public static void main(String[] args) throws IOException {

        int[] alphaList = {30, 30, 35, 35, 40, 40, 45, 45, 50, 50};

        //生成10个题目所要求的迷宫在文件目录下
        for (int i = 0; i < alphaList.length; i++) {
            int alpha = alphaList[i];
            MazeGenerator generator = new MazeGenerator(20, 20, alpha, 0, 0, 19, 19);
            Maze maze = generator.generate();

            FileHelper.outputToFile(maze.toString(), "Maze" + alpha + "_" + i % 2 + ".txt");
        }


    }

    /**
     * 批量生成指定参数的迷宫
     *
     * @param count 生成数量
     * @return 一个列表，里面包含指定数量的迷宫
     */
    public List<Maze> generate(int count) {
        List<Maze> mazes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            mazes.add(generate());
        }

        return mazes;
    }

    /**
     * 生成迷宫的主方法，根据对象设置的长宽和障碍密度进行
     *
     * @return 生成的迷宫
     */
    public Maze generate() {
        MazeSolver solver;
        List<Route> answer;

        //生成迷宫，若迷宫无解或最短路径步数超过了步数限制，则重新生成
        int step;
        do {
            step = genCore();
        } while (step == -1 || step > amountPassing);

        //计算需要填充或移除的障碍数，并进行相应的操作
        int amount = amountPassing - (maze.width * maze.height - maze.getAllWalls().size());
        if (amount > 0) {
            digRoute(amount);
        } else if (amount < 0) {
            fillRoute(Math.abs(amount));
        }

        //特判，若障碍密度大于等于30%，则只允许有一个最优路径，否则重新生成
        //建议在日常使用时删除
        if (maze.width == 20 && maze.height == 20 && alpha > 29) {
            solver = new MazeSolver(maze);
            answer = solver.solve();
            if (answer.size() != 1) {
                return generate();
            }
        }

        //返回时拷贝迷宫以保证不出现引用造成的潜在问题
        return maze.getCopy();
    }

    /**
     * 生成迷宫的核心方法
     *
     * @return 生成迷宫的最短路径长度（-1则该迷宫无解）
     */
    private int genCore() {
        Random rand = new Random();
        Maze.Point start = maze.getStartPoint();
        Maze.Point dest = maze.getDestPoint();

        /*
         * 初始化迷宫，把迷宫的每个点都设为障碍物
         * 并且将颜色清除，起点与终点设置为可通行
         */
        reset();
        start.setValue(0);
        dest.setValue(0);

        List<Maze.Point> walls = new ArrayList<>(randomPoints());

        while (!walls.isEmpty()) {
            int index = rand.nextInt(walls.size());
            Maze.Point point = walls.get(index);
            walls.remove(index);

            List<Maze.Point> pointWalls = maze.getWalls(point);
            List<Maze.Point> cache = new ArrayList<>();
            for (Maze.Point p : pointWalls) {
                //对周围障碍的染色操作
                if (p.color == 0) {
                    p.color = point.color;
                } else if (p.color != point.color) {
                    p.color = 3;
                }

                if (maze.isDeadEnd(p)) {
                    //如果障碍物移除后仍不会形成通路，则加入该点
                    cache.add(p);
                }
            }

            /*
             * 对找到的可移除障碍物进行随机移除
             * 保证至少移除一个障碍物
             */
            if (cache.size() > 0) {
                index = rand.nextInt(cache.size());
                cache.get(index).setValue(0);
                walls.add(cache.get(index));
                cache.remove(index);
            }
            for (Maze.Point p : cache) {
                if (rand.nextInt(2) != 1) {
                    p.setValue(0);
                    walls.add(p);
                }
            }

        }

        //找到所有的染色障碍物，并随机移除其中一个障碍物
        List<Maze.Point> mixedWalls = maze.getAllWalls(3);
        if (mixedWalls.size() > 0) {
            mixedWalls.get(rand.nextInt(mixedWalls.size())).setValue(0);
        }

        //将起点和终点打通
        digPointToRoad(start);
        digPointToRoad(dest);

        return countStep();
    }

    /**
     * 将指定的点与其它路径连接
     *
     * @param p 指定的路径点
     */
    private void digPointToRoad(Maze.Point p) {
        while (maze.isDeadEnd(p)) {
            List<Maze.Point> walls = maze.getWalls(p);
            Maze.Point newWay = walls.get(rand.nextInt(walls.size()));
            newWay.setValue(0);
            p = newWay;
        }
    }

    /**
     * 根据设置的起点与终点位置，随机选取两个路径点
     *
     * @return 一个列表，包含两个随机选择的路径点
     */
    private List<Maze.Point> randomPoints() {
        List<Maze.Point> list = new ArrayList<>();

        Maze.Point start = maze.getStartPoint();
        Maze.Point dest = maze.getDestPoint();

        Maze.Point p1 = getRandPoint(start);
        p1.color = 1;
        p1.setValue(0);
        Maze.Point p2 = getRandPoint(dest);
        p2.color = 2;
        p1.setValue(0);

        list.add(p1);
        list.add(p2);


        return list;
    }

    /**
     * 基于指定的路径点，生成随机路径点
     *
     * @param p 指定的路径点
     * @return 随机生成的路径点
     */
    private Maze.Point getRandPoint(Maze.Point p) {
        int midRow = (maze.height - 1) / 2;
        int midCol = (maze.width - 1) / 2;

        if (p.row > midRow) {
            if (p.column > midCol) {
                return maze.getUnit(rand.nextInt(midRow), rand.nextInt(midCol) + midRow + 1);
            } else {
                return maze.getUnit(rand.nextInt(midRow), rand.nextInt(midCol));
            }
        } else {
            if (p.column > midCol) {
                return maze.getUnit(rand.nextInt(midRow) + midRow + 1, rand.nextInt(midCol) + midRow + 1);
            } else {
                return maze.getUnit(rand.nextInt(midRow) + midRow + 1, rand.nextInt(midCol));
            }
        }
    }

    /**
     * 填充死路的方法
     *
     * @param amount 需要填充的障碍物个数
     */
    private void fillRoute(int amount) {
        //规定的障碍过多时使用，先bfs寻找主路并进行保护，再寻找每个死路，然后向前填充
        List<Route> deadEnds = new ArrayList<>();
        Route rightRoute = null;

        Queue<Route> queue = new LinkedList<>();
        Route route = new Route(maze);
        route.addPoint(maze.getStartPoint());
        queue.add(route);

        while (!queue.isEmpty()) {
            Route pack = queue.poll();
            List<Maze.Point> passes = maze.getPasses(pack.getTop());
            for (Maze.Point pass : passes) {
                if (!pack.contains(pass)) {
                    route = pack.getCopy();
                    route.addPoint(pass);
                    if (maze.isDestPoint(pass)) {
                        rightRoute = route;
                        continue;
                    }
                    if (maze.isDeadEnd(pass)) {
                        deadEnds.add(route);
                    } else {
                        queue.add(route);
                    }
                }
            }
        }

        while (amount > 0 && !deadEnds.isEmpty()) {
            int index = rand.nextInt(deadEnds.size());
            Route deadRoute = deadEnds.get(index);
            if (!deadRoute.isEmpty() && !rightRoute.contains(deadRoute.getTop()) && maze.canPass(deadRoute.getTop())) {
                //将不与正确路径重合的路径点填充障碍物（调用该方法前已保证有至少一条正确路径，故rightRoute不会为null）
                deadRoute.pop().setValue(1);
                amount--;
            } else {
                deadEnds.remove(index);
            }

        }

    }

    /**
     * 清除多余障碍物的方法
     *
     * @param amount 需要清除的障碍物个数
     */
    private void digRoute(int amount) {
        /*
         * 规定的障碍不够时使用，先bfs寻找主路，将主路和周围的障碍物保护起来，再随机在其它障碍上挖洞
         * 调用方法前已保证至少拥有一条正确路径，故rightRoute最终不会为null
         */
        Route rightRoute = null;

        Queue<Route> queue = new LinkedList<>();
        Route route = new Route(maze);
        route.addPoint(maze.getStartPoint());
        queue.add(route);

        while (!queue.isEmpty()) {
            Route pack = queue.poll();
            List<Maze.Point> passes = maze.getPasses(pack.getTop());
            for (Maze.Point pass : passes) {
                if (!pack.contains(pass)) {
                    route = pack.getCopy();
                    route.addPoint(pass);
                    if (maze.isDestPoint(pass)) {
                        rightRoute = route;
                    } else {
                        queue.add(route);
                    }
                }
            }
        }

        maze.initColor(0);
        while (!rightRoute.isEmpty()) {
            Maze.Point p = rightRoute.pop();
            for (Maze.Point wall : maze.getWalls(p)) {
                //利用对障碍物进行染色的方法来标记保护
                wall.color = 1;
            }
        }

        List<Maze.Point> walls = maze.getAllWalls(0);
        while (amount > 0 && !walls.isEmpty()) {
            int index = rand.nextInt(walls.size());
            walls.get(index).setValue(0);
            walls.remove(index);
            amount--;
        }

        //若清除所有未保护障碍物后障碍物仍然过多，则在被保护的障碍物中进行随机删除
        walls = maze.getAllWalls(1);
        while (amount > 0) {
            int index = rand.nextInt(walls.size());
            walls.get(index).setValue(0);
            walls.remove(index);
            amount--;
        }
    }

    /**
     * 重置整个迷宫（将所有路径点设置成障碍物，并清除路径点颜色）
     */
    private void reset() {
        maze.init(1);
        maze.initColor(0);
    }

    /**
     * 求目前迷宫的最短路径长度
     *
     * @return 当前迷宫的最短路径长度（-1则迷宫无解）
     */
    private int countStep() {
        Queue<Maze.Point> points = new LinkedList<>();
        boolean[][] visited = new boolean[maze.height][maze.width];
        maze.initColor(1);

        points.add(maze.getStartPoint());
        while (!points.isEmpty()) {
            Maze.Point p = points.poll();
            if (maze.isDestPoint(p.row, p.column)) {
                return p.color;
            }

            for (Maze.Point pp : maze.getPasses(p)) {
                if (!visited[pp.row][pp.column]) {
                    pp.color = p.color + 1;
                    points.add(pp);
                    visited[pp.row][pp.column] = true;
                }
            }
        }
        return -1;
    }

}
