package com.tyza66.mythreadpool;

public class AllFunctionTest {
    public static void main(String[] args) {
        // shutdown 方法会等待所有任务执行完毕后再关闭线程池 这时候进去的任务会被拒绝
        // shutdownNow 方法会立即关闭线程池 这时候进去的任务会被拒绝 还试图停止当前正在执行的任务 暂停处理正在等待的任务

        // submit 方法会返回一个 Future 对象 通过这个对象可以获取到任务的执行结果

        // Executors.newCachedThreadPool() 相当于核心线程数是0 用的时候才创建
        // Executors.newFixedThreadPool(10) 固定线程数的线程池
        // Executors.newSingleThreadExecutor() 单线程的线程池
        // Executors.newScheduledThreadPool(10) 定时任务的线程池
        // Executors.newWorkStealingPool() 工作窃取线程池
        // Executors.newSingleThreadScheduledExecutor() 单线程的定时任务线程池

        // 这些都是一些常用的线程池的创建方法 内置的线程池都是通过 ThreadPoolExecutor 实现的

    }
}
