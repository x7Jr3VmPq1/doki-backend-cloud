package com.megrez.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class CommandExecutor {
    /**
     * 执行外部命令
     *
     * @param command 命令列表
     * @return true 表示命令成功执行，false 表示执行失败
     */
    public static String executeCommand(List<String> command) {
        StringBuilder output = new StringBuilder();
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true); // 将 stderr 合并到 stdout
            Process process = builder.start();

            // 消费输出
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
            }

            // 等待命令执行完成
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                // 非零退出码也可以返回输出，但可以选择返回 null 或抛异常
                System.err.println("命令执行失败，exitCode=" + exitCode);
            }
            return output.toString();

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
