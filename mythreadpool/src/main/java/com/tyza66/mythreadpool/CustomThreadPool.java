package com.tyza66.mythreadpool;

import java.util.concurrent.*;

public class CustomThreadPool {
    public static void main(String[] args) {
        // 核心线程数
        int corePoolSize = 2;
        // 最大线程数
        int maximumPoolSize = 4;
        // 线程空闲时间
        long keepAliveTime = 60L;
        // 时间单位
        TimeUnit unit = TimeUnit.SECONDS;
        // 阻塞队列
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(2);
        // 线程工厂
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        // 拒绝策略
        RejectedExecutionHandler handler = new ThreadPoolExecutor.AbortPolicy();

        // 创建线程池
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);

        // 提交一些任务
        for (int i = 0; i < 6; i++) {
            final int taskNumber = i;
            executor.submit(() -> {
                System.out.println("任务 " + taskNumber + " 执行：" + Thread.currentThread().getName());
                try {
                    Thread.sleep(2000); // 模拟任务执行时间
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // 关闭线程池
        executor.shutdown();

        try {
            // 等待所有任务完成
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}
