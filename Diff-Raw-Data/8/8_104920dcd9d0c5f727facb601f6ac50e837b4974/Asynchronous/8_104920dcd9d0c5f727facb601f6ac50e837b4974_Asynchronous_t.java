 package org.inigma.shared.job;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
 import org.springframework.util.ClassUtils;
 
 import javax.annotation.PreDestroy;

 import java.lang.reflect.Method;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.BlockingDeque;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 import java.util.concurrent.LinkedBlockingDeque;
 import java.util.concurrent.atomic.AtomicInteger;
 
 public class Asynchronous {
     private static Logger logger = LoggerFactory.getLogger(Asynchronous.class);
     private static Timer timer = new Timer(true);
     AtomicInteger completed;
     Map<Method, ExecutionInfo> info = new HashMap<Method, ExecutionInfo>();
     private ThreadPoolTaskExecutor executor;
     private BlockingDeque<AsynchronousFutureTask<?>> workQueue;
 
     public Asynchronous() {
         this(3);
     }
 
     public Asynchronous(int workers) {
        this(workers, Integer.MAX_VALUE);
    }

    public Asynchronous(int workers, int capacity) {
        this.workQueue = new LinkedBlockingDeque<AsynchronousFutureTask<?>>(capacity);
         this.completed = new AtomicInteger();
         executor = new ThreadPoolTaskExecutor();
         executor.setCorePoolSize(workers);
         executor.setDaemon(true);
         initialize();
     }
 
     @PreDestroy
     public void close() {
         executor.shutdown();
     }
 
     public int getCompleted() {
         return completed.get();
     }
 
     public <T> Future<T> invoke(final Object instance, final Method method, final Object... args) {
         if (!info.containsKey(method)) {
             info.put(method, new ExecutionInfo());
         }
         AsynchronousFutureTask<T> task = new AsynchronousFutureTask<T>(null, instance, method, args);
         try {
             workQueue.put(task);
         } catch (InterruptedException e) {
             throw new RuntimeException(e);
         }
         return task;
     }
 
     public <T> Future<T> invoke(final Object instance, final String method, final Object... args) {
         return invoke(instance, findMethod(instance, method, args), args);
     }
 
     public void invokeWithCallback(AsynchronousCallback callback, final Object instance, final String method, final Object... args) {
         invokeWithCallback(callback, instance, findMethod(instance, method, args), args);
     }
 
     public void invokeWithCallback(AsynchronousCallback callback, final Object instance, final Method method, final Object... args) {
         if (!info.containsKey(method)) {
             info.put(method, new ExecutionInfo());
         }
         try {
             workQueue.put(new AsynchronousFutureTask(callback, instance, method, args));
         } catch (InterruptedException e) {
             throw new RuntimeException(e);
         }
     }
 
     public boolean isEmpty() {
         return this.workQueue.isEmpty();
     }
 
     public void scheduleRepeatTask(TimerTask task, long interval) {
         timer.schedule(task, interval, interval);
     }
 
     public int size() {
         return workQueue.size();
     }
 
     @Override
     protected void finalize() throws Throwable {
         close();
         super.finalize();
     }
 
     protected Method findMethod(Object instance, String method, Object... args) {
         Set<Method> methods = new HashSet<Method>(Arrays.asList(instance.getClass().getMethods()));
         methods.addAll(Arrays.asList(instance.getClass().getDeclaredMethods()));
         Method actualMethod = null;
         for (Method m : methods) {
             if (method.equals(m.getName())) {
                 Class<?>[] types = m.getParameterTypes();
                 if (args.length == types.length) {
                     boolean valid = true;
                     for (int i = 0; i < args.length; i++) {
                         Class<?> paramType = ClassUtils.resolvePrimitiveIfNecessary(types[i]);
                         if (args[i] != null && !(paramType.isAssignableFrom(args[i].getClass()))) {
                             valid = false;
                         }
                     }
                     if (valid) {
                         actualMethod = m;
                         break;
                     }
                 }
             }
         }
         if (actualMethod == null) {
             throw new IllegalStateException("Method " + method + " not found with given arguments");
         }
 
         return actualMethod;
     }
 
     private void initialize() {
         this.completed.set(0); // reset the counter too
         executor.initialize();
         for (int i = 0; i < executor.getCorePoolSize(); i++) {
             executor.execute(new Runnable() {
                 @Override
                 public void run() {
                     AsynchronousFutureTask task = null;
                     while (true) {
                         long startTime = System.currentTimeMillis();
                         try {
                             task = workQueue.take();
                             task.run();
                             completed.incrementAndGet();
                             if (task.getCallback() != null) {
                                 task.getCallback().onCompletion(task.get(), task);
                             } else {
                                 task.get(); // this is to force exception captured to be thrown. maybe better way???
                             }
                         } catch (InterruptedException e) {
                             logger.warn("Asynchronous was interrupted with queue size at {}", workQueue.size());
                         } catch (ExecutionException e) {
                             if (task.getCallback() != null) {
                                 task.getCallback().onException(e, task);
                             } else {
                                 logger.error("Unhandled Exception in {} with {}", task.getMethod(), task.getArguments(), e);
                             }
                         }
 
                         long duration = System.currentTimeMillis() - startTime;
                         ExecutionInfo executionInfo = info.get(task.getMethod());
                         executionInfo.incrementCount();
                         executionInfo.addDuration(duration);
                     }
                 }
             });
         }
     }
 }
