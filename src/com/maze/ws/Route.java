package com.maze.ws;

import java.util.Stack;

/**
 * 路径类
 * 表示迷宫中的一条路径
 *
 * @author Wshine
 */
public class Route {
    public final Maze maze;

    private Stack<Maze.Point> route = new Stack<>();

    /**
     * 构造方法，生成一个路径对象并绑定所属迷宫
     *
     * @param maze 绑定迷宫
     */
    public Route(Maze maze) {
        this.maze = maze;
    }

    /**
     * 构造方法，基于给定的路径对象拷贝一个新的路径对象
     *
     * @param route 路径对象
     */
    private Route(Route route) {
        this.route = (Stack<Maze.Point>) route.route.clone();
        this.maze = route.maze;
    }

    /**
     * 添加路径点到路径的顶端（最前端）
     * @param p 要添加的路径点
     */
    public void addPoint(Maze.Point p) {
        route.push(p);
    }

    /**
     * 获取指定下标处的路径点
     *
     * @param index 下标
     * @return 指定位置的路径点
     */
    public Maze.Point getPoint(int index) {
        return route.get(index);
    }

    /**
     * 获取该路径的长度
     *
     * @return 路径长度
     */
    public int getStep() {
        return route.size();
    }

    /**
     * 获取该路径最前端的路径点
     *
     * @return 最顶端（前端）的路径点
     */
    public Maze.Point getTop() {
        return route.peek();
    }

    /**
     * 弹出最前端的路径点
     *
     * @return 最前端的路径点
     */
    public Maze.Point pop() {
        return route.pop();
    }

    /**
     * 判断路径中是否包含指定的路径点
     *
     * @param p 要寻找的路径点
     * @return 一个布尔值，代表是否包含指定路径点
     */
    public boolean contains(Maze.Point p) {
        return route.contains(p);
    }

    /**
     * 判断该路径是否为空
     * @return 一个布尔值，代表该路径是否为空
     */
    public boolean isEmpty() {
        return route.isEmpty();
    }

    /**
     * 反转整条路径
     */
    public void reverse() {
        Stack<Maze.Point> route = new Stack<>();
        int size = this.route.size();
        for (int i = 0; i < size; i++) {
            route.add(this.route.pop());
        }

        this.route = route;
    }

    /**
     * 获取当前路径的拷贝
     *
     * @return 当前路径的拷贝
     */
    public Route getCopy() {
        return new Route(this);
    }

    /**
     * 可视化路径
     * 根据绑定的迷宫生成一张更加可读的，标注了该路径的迷宫
     *
     * @return 可视化后的迷宫字符串
     */
    public String visualize() {
        String[] symbols = {".", "#"};

        Maze copy = maze.getCopy();
        StringBuilder s = new StringBuilder();
        if (route.size() == 0) {
            return copy.toString();
        }

        for (int i = 0; i < maze.height; i++) {
            for (int j = 0; j < maze.width; j++) {
                String symbol;
                Maze.Point p = maze.getUnit(i, j);
                if (route.contains(p)) {
                    symbol = "|";
                } else {
                    symbol = symbols[p.getValue()];
                }
                s.append(symbol).append(" ");
            }
            s.append("\n");
        }

        return s.toString();
    }


}
