package com.maze.ws;

import java.util.List;

/**
 * 迷宫的调试类
 * 其中放入了一些在开发过程中编写过的调试方法
 *
 * @author Wshine
 */
public class MazeDebugger {

    /**
     * 通过指定的生成器连续生成指定数量的迷宫后对迷宫进行求解，以测试迷宫生成与求解的性能
     *
     * @param generator 迷宫生成器
     * @param cot 生成数量
     */
    public static void analyzeMazes(MazeGenerator generator, int cot) {
        int routeCount = 0;
        MazeSolver solver = new MazeSolver(generator.generate());

        long time = System.currentTimeMillis();
        List<Maze> mazeList = generator.generate(cot);
        time = System.currentTimeMillis() - time;
        System.out.println("生成完毕(" + cot + "/" + cot + ")");
        System.out.println("总耗时：" + time + "ms");
        System.out.println("平均耗时：" + time / (double) cot + "ms\n");

        List<Route> routes;

        time = System.currentTimeMillis();
        for (Maze maze : mazeList) {
            solver.changeMaze(maze);
            routes = solver.solve();
            routeCount += routes.get(0).getStep();
        }
        time = System.currentTimeMillis() - time;

        System.out.println("全部迷宫已计算完毕(" + cot + "/" + cot + ")");
        System.out.println("平均用时：" + time / (double) cot + "ms");
        System.out.println("平均最短路径长度为" + routeCount / (double) cot + "\n");
    }

    /**
     * 比较当前解决方法的正确性
     *
     * @param solver 一个迷宫求解器
     * @return 对拍方法和当前方法所得结果是否一致
     */
    public static boolean compare(MazeSolver solver) {
        return solver.solve().equals(solver.solveHistory());
    }

    /**
     * 测试生成20*20，固定起点终点，指定数量和障碍密度的迷宫生成时间
     *
     * @param count 生成数量
     * @param alpha 障碍密度
     * @return 生成时间（以毫秒为单位）
     */
    public static long testGenerateTime(int count, int alpha) {
        MazeGenerator generator = new MazeGenerator(20, 20, alpha, 0, 0, 19, 19);
        long time = System.currentTimeMillis();
        generator.generate(count);
        return System.currentTimeMillis() - time;

    }

    /**
     * 随机生成一个指定障碍密度的20*20迷宫，固定起点和终点，输出求解结果
     *
     * @param alpha 障碍密度
     */
    public static void randomMazeSolve(int alpha) {
        MazeGenerator generator = new MazeGenerator(20, 20, alpha, 0, 0, 19, 19);
        Maze m = generator.generate();
        solveMaze(m);
    }

    /**
     * 求解给定的迷宫，并对结果进行输出
     *
     * @param maze 需要求解的迷宫
     */
    public static void solveMaze(Maze maze) {
        long time = System.currentTimeMillis();
        List<Route> routes = new MazeSolver(maze).solve();
        time = System.currentTimeMillis() - time;

        System.out.println(maze.toString());

        if (routes.size() == 0) {
            System.out.println("迷宫无解");
            return;
        }

        System.out.println(routes.size() + "个解，可视化结果如下（用时" + time + "ms）：");
        for (Route route : routes) {
            System.out.println();
            System.out.println(route.visualize());
            System.out.println("路径长度："+route.getStep());
            System.out.println();
        }
    }

    /**
     * 随机生成一个20*20，固定起点终点的，指定障碍密度的迷宫
     *
     * @param alpha 障碍密度
     * @return 生成的迷宫
     */
    public static Maze generateMaze(int alpha) {
        MazeGenerator generator = new MazeGenerator(20, 20, alpha, 0, 0, 19, 19);
        return generator.generate();

    }
}
