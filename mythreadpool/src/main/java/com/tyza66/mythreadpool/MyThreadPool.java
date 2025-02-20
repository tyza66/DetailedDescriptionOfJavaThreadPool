package com.tyza66.mythreadpool;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MyThreadPool {
    public static void main(String[] args) {
        MyThreadPoolExecutor my = new MyThreadPoolExecutor(2, 4, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(2),
                Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
        my.execute(() -> {
            System.out.println("任务 1 执行：" + Thread.currentThread().getName());
            try {
                Thread.sleep(2000); // 模拟任务执行时间
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
}
