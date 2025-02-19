package com.tyza66.mythreadpool;


import java.util.concurrent.*;

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

        // 线程池的作用:
        // 将线程和任务分离
        // 控制住并发的数量 降低服务器的压力 统一管理所有的线程
        // 提高响应速度 提高系统的吞吐量 (相当于省去创建线程和销毁线程的时间)

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
                System.out.println(Thread.currentThread().getName() + "我是子线程");
            }
        });
        // 在这里主线程也可以执行任务

        // 上面这种方法创建的线程池本质上是
        ExecutorService executorService_real = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>());

        executorService_real.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName() + "我是子线程");
            }
        });

        // ThreadPoolExecutor 参数最全的构造方法有七个参数
        // 其中 corePoolSize 是核心线程数 就是最初始的线程数 这几个会一直驻留在线程池中
        // maximumPoolSize 是最大线程数 不够的时候最多添加到几个线程
        // keepAliveTime 是线程空闲时间 非核心线程在空闲时间超过这个时间就会被回收
        // unit 是时间单位
        // workQueue 是阻塞队列 存放提交了但是还没有执行的任务 一个缓冲区
        // threadFactory 是线程工厂
        // handler 是饱和处理机制 就是当线程池满了之后的处理策略

        // java中的线程池是如果核心线程有地方就在核心线程运行,再多的进来先存进阻塞队列,
        // 如果阻塞队列满了但是没到最大线程数就增加非核心线程,如果再超过了最大线程数就执行饱和处理机制

        // 在上面的例子中可能会误导我们 明明设置的核心线程数是0 最大线程数是Integer.MAX_VALUE
        // 然后也设置了阻塞队列 为什么任务一提交进去就执行了呢? 难道不是先阻塞队列满了才开始使用非核心线程吗?
        // 其实是因为SynchronousQueue是一个特殊的阻塞队列 它的容量是0 所以任务一提交进去就会被拿出来执行
        // 所以这个线程池的特点是 任务一提交进去就会被执行 但是如果任务提交的速度比执行的速度快 那么就会创建新的线程
        // 而且我们会发现任务执行完之后线程并没有立即被销毁 而是等待60s 如果60s内没有任务执行 线程就会被销毁
        // 记得是等待队列满了 才去创建的非核心线程的

        // handler 是最大线程数满了之后的处理策略 当然是
        // 阻塞队列满了之后才开始创建非核心线程堆满的
        // 再次提醒 记得是队列饱和了才开始创建非核心线程的 所以handler被调用的那个时候 队列也必然是满的

        // 内置拒绝策略共有这四种
        // AbortPolicy 直接抛出异常
        // CallerRunsPolicy 由调用线程处理该任务
        // DiscardOldestPolicy 抛弃队列中等待最久的任务
        // DiscardPolicy 直接抛弃任务

    }
}