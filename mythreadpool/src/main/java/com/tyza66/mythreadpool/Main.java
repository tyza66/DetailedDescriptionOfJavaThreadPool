package com.tyza66.mythreadpool;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {

        // 虽然我们可以弄出来十多种创建多线程的方法
        // 常规的线程池创建方法主要是这几种:
        // 1. 继承Thread类重写Run方法
        // 2. 实现Runnable接口重写Run方法
        // 3. 实现Callable接口重写Call方法
        // 4. 使用线程池

        // 线程池是管理线程用的 在java中创建线程是要消耗资源的
        // 如果有线程池 任务来了就可以去线程池中拿一个线程来执行任务 用完再放回去 实现线程的复用
        // 可以根据任务的多少来动态的调整线程的数量 还可以同一管控线程 减少系统的消耗
        // 而且线程池方便了我们对线程的调度和管控

        // 试一下java自带的线程池
        // 通过一个队列先缓存到任务再使用复用（阻塞队列）机制，再交给我们当前的消费者从队列中拿到任务再进行执行
        // 这就是线程池原理

        // 使用最简单的线程池
        // 新建一个线程池
        ExecutorService executorService = Executors.newCachedThreadPool();
        // 往线程池中提交任务
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName()+"我是子线程");
            }
        });
        // 在这里主线程也可以执行任务


    }
}