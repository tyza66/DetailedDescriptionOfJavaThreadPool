package com.tyza66.mythreadpool;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.*;

// 自定义的七参数构造方法的线程池
// 注意这个线程池只是一个简单的实现
// Java内置的线程池其实也是纯Java实现的
public class MyThreadPoolExecutor {
    // 初始化参数
    private final int corePoolSize; // 核心线程数
    private final int maximumPoolSize; // 最大线程数
    private final long keepAliveTime; // 线程空闲时间
    private final TimeUnit unit; // 时间单位
    private final BlockingQueue<Runnable> workQueue; // 阻塞队列
    private final ThreadFactory threadFactory; // 线程工厂
    private final RejectedExecutionHandler handler; // 拒绝策略

    // 用于计数的消息传递
    private final AtomicInteger workerCount = new AtomicInteger(0); // 当前工作线程数
    private final ReentrantLock mainLock = new ReentrantLock(); // 主锁
    private final Condition termination = mainLock.newCondition(); // 通知线程的条件
    private volatile boolean isShutdown = false; // 是否关闭

    // 七参数的构造方法
    public MyThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.unit = unit;
        this.workQueue = workQueue;
        this.threadFactory = threadFactory;
        this.handler = handler;
    }

    //  提交任务的方法
    public void execute(Runnable command) {
        // 如果传入一个null的任务就抛出异常
        if (command == null) throw new NullPointerException();
        // 如果是在关闭状态也抛出异常
        if (isShutdown) throw new RejectedExecutionException("ThreadPool is shutdown");

        // 先获得当前的工作线程数
        int c = workerCount.get();
        // 如果当前的工作线程数小于核心线程数
        if (c < corePoolSize) {
            // 那就添加核心线程
            if (addWorker(command, true)) return;
        }
        // 上面可能造添加核心线程失败的原因有两个 一个是线程数已经达到了核心线程数 一个是尝试添加线程失败
        // 如果添加核心线程失败了 就尝试将任务添加到阻塞队列中
        if (workQueue.offer(command)) { // offer 就是将新的任务添加到队列中的方法 如果添加成功就返回true 如果添加失败就返回false
            // 如果添加成功就再次获得当前的工作线程数
            int recheck = workerCount.get();
            // 如果现在关闭信号置为真了
            if (isShutdown) {
                // 那就从队列中移除这个任务
                workQueue.remove(command);
                // 并且拒绝执行这个任务
                handler.rejectedExecution(command, null);
            // 如果没有关闭信号 且 当前工作线程数为0 就添加一个非核心线程
            } else if (recheck == 0) {
                // 当线程池没有关闭且当前工作线程数为0时，它会添加一个非核心线程来处理任务 这是一种特殊情况
                addWorker(null, false);
            }
        // 如果添加到队列失败了 就再次尝试添加一个非核心线程
        } else if (!addWorker(command, false)) {
            // 如果添加非核心线程也失败了 就执行拒绝策略
            handler.rejectedExecution(command, null);
        }
    }

    // 添加工作线程的方法
    private boolean addWorker(Runnable firstTask, boolean core) {
        // 这里是一个永真循环 用于保证线程安全
        for (;;) {
            // 获得当前的工作线程数
            int c = workerCount.get();
            // 如果是核心线程就取核心线程数 如果是非核心线程就取最大线程数
            int wc = core ? corePoolSize : maximumPoolSize;
            // 如果当前的工作线程数 大于当前的wc 就返回false 这个wc如果创建的是核心线程就用于限制核心线程数 如果是非核心线程就用于限制最大线程数
            if (c >= wc) return false; // false表示添加失败 跳过这次添加
            // 如果当前的工作线程数等于当前的c 就把当前的工作线程数加一 这样做是为了保证线程安全
            if (workerCount.compareAndSet(c, c + 1)) break;
        }
        // 初始化当前的工作线程的一些状态
        boolean workerStarted = false;
        boolean workerAdded = false;
        Worker w = null;
        try {
            // 创建一个工作线程
            w = new Worker(firstTask);
            // 使用线程工厂创建一个线程
            Thread t = threadFactory.newThread(w);
            // 如果创建成功就启动这个线程
            if (t != null) {
                workerAdded = true;
                t.start();
                workerStarted = true;
            }
        } finally {
            // 如果没创建成功就向失败的方法中添加这个线程
            if (!workerStarted) addWorkerFailed(w);
        }
        return workerStarted;
    }

    // 添加失败时的补偿策略
    private void addWorkerFailed(Worker w) {
        // 添加失败的时候向拿到主锁
        mainLock.lock();
        try {
            // 如果w不为空就把工作线程数减一
            if (w != null) workerCount.decrementAndGet();
            // 通知所有的线程
            termination.signalAll();
        } finally {
            // 释放主锁
            mainLock.unlock();
        }
    }

    // 关闭线程池的方法
    public void shutdown() {
        mainLock.lock();
        try {
            isShutdown = true;
            termination.signalAll();
        } finally {
            mainLock.unlock();
        }
    }

    // 这就是执行传入的任务的工作线程
    private final class Worker implements Runnable {
        private final Runnable firstTask; // 这个仅仅是初始化的任务
        private Thread thread;

        Worker(Runnable firstTask) {
            this.firstTask = firstTask;
            this.thread = Thread.currentThread();
        }

        @Override
        public void run() {
            Runnable task = firstTask;
            try {
                // 判断当前的任务是否为空 如果不为空就执行这个任务 如果当前任务为空就去阻塞队列中取任务
                // 相当于这个执行完了就尝试再次去取任务执行
                while (task != null || (task = getTask()) != null) {  // 线程会不会被销毁的关键就在getTask方法中
                    // 在Java的ThreadPoolExecutor中,线程池中的线程并不是启动其他线程,而是执行提交给线程池的任务
                    // 具体来说,当调用execute()或submit()方法向线程池提交任务时,线程池会根据当前的状态和配置来决定如何处理这个任务
                    task.run(); // 这里线程池中的线程中执行的任务本身也是一个线程 作为演示
                    task = null;
                }
            } finally {
                // 任务都执行完了就释放这个线程
                processWorkerExit(this, false);
            }
        }
    }

    // 从阻塞队列中取任务的方法
    private Runnable getTask() {
        boolean timedOut = false;
        for (;;) {
            int c = workerCount.get();
            // 默认情况下，如果有非核心线程被创建了，但是有核心线程闲置了，闲置的核心线程不会超时，之前多出来的非核心线程也不会取代它的位置
            boolean timed = (c > corePoolSize);  // 利用当前线程数是否大于核心线程数来判断是否是非核心线程
            try {
                Runnable r = timed ?             // 如果是非核心线程就使用poll方法 如果是核心线程就使用take方法
                        workQueue.poll(keepAliveTime, unit) : // poll方法是从队列中取任务 并且等待一定时间 时间到了就放弃并返回null 第一个参数是时间 第二个参数是时间单位
                        workQueue.take();                     // 而take方法是从队列中取任务 如果队列中没有任务就阻塞等待(一直等) 如果有任务就返回任务 这保证了核心线程的存活
                if (r != null) return r;
                timedOut = true;
            } catch (InterruptedException retry) {
                timedOut = false;
            }
            if (timedOut) return null;
        }
    }

    // 关闭线程池中的工作线程的方法
    private void processWorkerExit(Worker w, boolean completedAbruptly) {
        mainLock.lock();
        try {
            // 这里只演示减少了数量的操作
            workerCount.decrementAndGet();
            termination.signalAll();
        } finally {
            mainLock.unlock();
        }
    }
}
