package com.tyza66.mythreadpool;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FutureTest {
    public static void main(String[] args) {
        // 测一下Future
        // 实现 Callable 接口
        // 有返回值

        ExecutorService es = Executors.newCachedThreadPool();

        Future<String> submit = es.submit(() -> {
            System.out.println("任务 1 执行：" + Thread.currentThread().getName());
            Thread.sleep(2000); // 模拟任务执行时间
            return "任务 1 执行完成";
        });

        try {
            System.out.println("任务 1 的返回值：" + submit.get()); // 这里会阻塞等待任务执行完毕
            // get方法还有重载 可以设置超时时间

            // isCancelled方法用来判断任务是否被取消
            // isDone方法用来判断任务是否已经完成
            // cancel方法用来取消任务
            // get方法用来获取任务的执行结果
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

    }
}
