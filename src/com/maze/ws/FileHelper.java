package com.maze.ws;

import java.io.File;
import java.io.FileInputStream;

/**
 * 字符串与文件IO相互转换处理的类
 *
 * @author Achan
 */
public class FileHelper {

    /**
     * 将指定路径的文件内容变为字符串
     *
     * @param fileName 文件路径
     * @return 文件内容的字符串形式（utf-8编码）
     */
    public static String readToString(String fileName) {
        String encoding = "UTF-8";
        File file = new File(fileName);
        long fileLength = file.length();
        byte[] fileContent = new byte[(int) fileLength];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(fileContent);
            in.close();
            return new String(fileContent, encoding);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 在指定的路径新建文件并将指定字符串输出
     *
     * @param str  字符串内容
     * @param path 文件路径
     * @throws java.io.IOException IO错误，可能文件已被锁定
     */
    public static void outputToFile(String str, String path) throws java.io.IOException {
        java.io.File file = new java.io.File(path);

        if (file.exists()) {
            System.out.println("A file has the same name,change one.");
            System.exit(1);
        }

        java.io.PrintWriter output = new java.io.PrintWriter(file);
        output.print(str);
        output.close();
    }
}
