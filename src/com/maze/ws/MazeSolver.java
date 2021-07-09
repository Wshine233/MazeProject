package com.maze.ws;

import java.util.*;

/**
 * 迷宫求解类
 *
 * @author Wshine
 */
public class MazeSolver {
    private Maze maze;
    private final List<Route> routes = new ArrayList<>();

    public MazeSolver(Maze maze) {
        this.maze = maze;
    }

    /**
     * 切换待解决的迷宫
     *
     * @param maze 切换的迷宫
     */
    public void changeMaze(Maze maze) {
        this.maze = maze;
    }

    public static void main(String[] args) {
        //MazeDebugger.randomMazeSolve(35);

        int[] alphaList = {30, 30, 35, 35, 40, 40, 45, 45, 50, 50};

        //读入文件并解决迷宫
        for (int i = 0; i < alphaList.length; i++) {
            int alpha = alphaList[i];
            String s = FileHelper.readToString("Maze" + alpha + "_" + i % 2 + ".txt");
            Maze maze = Maze.fromString(s, 0, 0, 19, 19);
            System.out.println("解决迷宫：" + "Maze" + alpha + "_" + i % 2 + ".txt");
            MazeDebugger.solveMaze(maze);
        }

        //性能测试用，生成指定数量的迷宫计算时间（密度较小时需要等待生成）
        for (int alpha : alphaList) {
            System.out.println("alpha = " + alpha);
            MazeGenerator generator = new MazeGenerator(20, 20, alpha, 0, 0, 19, 19);
            MazeDebugger.analyzeMazes(generator, 1000);
        }
    }

    /**
     * 求解迷宫的主方法
     *
     * @return 一个路径列表，包含所有可行的最短路径
     */
    public List<Route> solve() {
        routes.clear();

        int[][] minStep = new int[maze.height][maze.width];
        Maze.Point start = maze.getStartPoint();

        //第一部分：求出起点到每个点的最短路径长度
        Queue<Maze.Point> queue = new LinkedList<>();
        queue.add(start);
        minStep[start.row][start.column] = 1;
        while (!queue.isEmpty()) {
            Maze.Point point = queue.poll();
            if (maze.isDestPoint(point)) {
                //特判，若已经到了终点则没必要继续让终点入队
                continue;
            }

            /*
             * 找到这个点周围的可通行路径点，如果路径点没有被走过，则记录步数，
             * 该步数即为从起点到这个点的最短路径长度
             */
            int cache = minStep[point.row][point.column];
            List<Maze.Point> passes = maze.getPasses(point);
            for (Maze.Point pass : passes) {
                if (minStep[pass.row][pass.column] == 0) {
                    minStep[pass.row][pass.column] = cache + 1;
                    queue.add(pass);
                }
            }
        }

        //第二部分：从终点根据最短路径长度向前记录所有路径
        Maze.Point dest = maze.getDestPoint();
        int step;
        Queue<Route> routeQueue = new LinkedList<>();

        Route route = new Route(maze);
        route.addPoint(dest);
        routeQueue.add(route);

        while (!routeQueue.isEmpty()) {
            route = routeQueue.poll();
            Maze.Point point = route.getTop();

            /*
             * step确定前一个点应该显示的步数（当前点的步数-1）
             * 使用step变量中转提升代码可读性
             */
            step = minStep[point.row][point.column] - 1;
            if (maze.isStartPoint(point)) {
                //特判，若路径已经到达起点，则先将路径倒转，再加入到结果列表
                route.reverse();
                this.routes.add(route);
                continue;
            }

            for (Maze.Point p : maze.getPasses(point)) {
                if (minStep[p.row][p.column] == step) {
                    Route cache = route.getCopy();
                    cache.addPoint(p);
                    routeQueue.add(cache);
                }
            }
        }

        return this.routes;
    }

    /**
     * 求目前迷宫的最短路径长度
     *
     * @return 当前迷宫的最短路径长度（-1则迷宫无解）
     */
    public int countStep() {
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


    /**
     * 最初的解决方法，现已不再使用，在验证新方法正确性时使用
     *
     * @return 求出的最短路径列表
     * @deprecated 已经找到了更优化的方法，该方法仅应作为验证正确性的方法被调试器使用，请使用solve()
     */
    @Deprecated
    public List<Route> solveHistory() {
        routes.clear();

        int step = countStep();
        if (step == -1) {
            System.out.println("错误！没有正确路径！");
            return null;
        }

        Queue<Route> queue = new LinkedList<>();

        Route route = new Route(maze);
        route.addPoint(maze.getStartPoint());
        queue.add(route);

        while (!queue.isEmpty()) {
            route = queue.poll();

            if (route.getStep() > step) {
                //边界，若当前路径步数大于最短路径步数则退出循环
                break;
            }

            List<Maze.Point> passes = maze.getPasses(route.getTop());
            for (Maze.Point pass : passes) {
                if (!route.contains(pass)) {
                    route = route.getCopy();
                    route.addPoint(pass);
                    if (maze.isDestPoint(pass)) {
                        routes.add(route);
                    } else if (!maze.isDeadEnd(pass)) {
                        queue.add(route);
                    }
                }
            }
        }

        return routes;
    }



}
