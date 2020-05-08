 /*
  * Copyright (c) 2013 Noveo Group
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * Except as contained in this notice, the name(s) of the above copyright holders
  * shall not be used in advertising or otherwise to promote the sale, use or
  * other dealings in this Software without prior written authorization.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
  * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package com.noveogroup.android.task;
 
 import android.os.SystemClock;
 import android.util.Log;
 
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Future;
 
 /**
  * {@link AbstractTaskHandler} is an abstract implementation of
  * the {@link TaskHandler} interface. A subclass must implement the abstract
  * methods {@link #addToQueue()}, {@link #removeFromQueue()} and
  * {@link #createTaskEnvironment()}.
  *
  * @param <T> task type.
  * @param <E> task environment type.
  */
 abstract class AbstractTaskHandler<T extends Task<? super E>, E extends TaskEnvironment> implements TaskHandler<T, E> {
 
     private final Object joinObject = new Object();
     private final ExecutorService executorService;
     private volatile Future<Throwable> taskFuture;
     private volatile boolean taskFutureCanBeInterrupted;
 
     private final TaskSet<E> owner;
     private final T task;
     private final Pack args;
     private final TaskListener[] listeners;
 
     private volatile State state;
     private volatile Throwable throwable;
     private volatile boolean interrupted;
 
     /**
      * Creates new instance of {@link AbstractTaskHandler}.
      *
      * @param executorService {@link ExecutorService} providing as working threads.
      * @param task            {@link Task} interface to execute.
      * @param owner           owner {@link TaskSet}.
      * @param args            arguments container.
      * @param listeners       a list of {@link TaskListener}.
      */
     public AbstractTaskHandler(ExecutorService executorService, T task, TaskSet<E> owner, Pack args, List<TaskListener> listeners) {
         this.executorService = executorService;
         this.taskFuture = null;
         this.taskFutureCanBeInterrupted = false;
 
         this.owner = owner;
         this.task = task;
         this.args = args.lock() == owner.lock() ? args : new Pack(owner.lock(), args);
         this.listeners = new TaskListener[listeners.size()];
         listeners.toArray(this.listeners);
 
         this.state = null;
         this.throwable = null;
         this.interrupted = false;
 
         // create task
         createTask();
     }
 
     /**
      * Creates task environment for this task.
      * <p/>
      * Created environment must use methods of this handler to implement
      * its base functionality.
      *
      * @return a {@link TaskEnvironment} object.
      */
     protected abstract E createTaskEnvironment();
 
     /**
      * Task handler will call this method when it is needed to be added to
      * task queue.
      */
     protected abstract void addToQueue();
 
     /**
      * Task handler will call this method when it is needed to be removed from
      * task queue.
      */
     protected abstract void removeFromQueue();
 
     private void createTask() {
         synchronized (lock()) {
             interrupted = false;
             state = State.CREATED;
             throwable = null;
            addToQueue();
 
             if (owner().isInterrupted()) {
                 interrupted = true;
                 state = State.CANCELED;
                 throwable = null;
             }
 
             executorService.submit(new Runnable() {
                 @Override
                 public void run() {
                     prepareTask();
                 }
             });
         }
     }
 
     private void prepareTask() {
         if (isInterrupted()) {
             // call listeners
             callOnCreate();
             callOnCanceled();
             callOnDestroy();
 
             // notify join object
             synchronized (joinObject) {
                 joinObject.notifyAll();
             }
         } else {
             callOnCreate();
             callOnQueueInsert();
 
             executorService.submit(new Runnable() {
                 @Override
                 public void run() {
                     executeTask();
                 }
             });
         }
     }
 
     private void executeTask() {
         if (isInterrupted()) {
             // call listeners
             callOnCanceled();
             callOnQueueRemove();
             callOnDestroy();
 
             // notify join object
             synchronized (joinObject) {
                 joinObject.notifyAll();
             }
         } else {
             // change state
             synchronized (lock()) {
                 state = State.STARTED;
                 throwable = null;
             }
 
             // call listeners
             callOnStart();
 
             // create task environment
             E env;
             synchronized (lock()) {
                 env = createTaskEnvironment();
             }
 
             // execute task
             Throwable t = null;
             try {
                 synchronized (lock()) {
                     // allow interruption
                     taskFutureCanBeInterrupted = true;
 
                     // check if the task has already been interrupted
                     if (isInterrupted()) {
                         throw new InterruptedException();
                     }
                 }
                 // run task
                 task.run(env);
             } catch (Throwable throwable) {
                 t = throwable;
             } finally {
                 synchronized (lock()) {
                     // deny interruption
                     taskFutureCanBeInterrupted = false;
                 }
             }
 
             // change task state and remove task from queue
             synchronized (lock()) {
                 state = t == null ? State.SUCCEED : State.FAILED;
                 throwable = t;
                 removeFromQueue();
             }
 
             // call listeners
             callOnFinish();
             if (t == null) {
                 callOnSucceed();
             } else {
                 callOnFailed();
             }
             callOnQueueRemove();
             callOnDestroy();
 
             // notify join object
             synchronized (joinObject) {
                 joinObject.notifyAll();
             }
         }
     }
 
     @Override
     public TaskSet<E> owner() {
         return owner;
     }
 
     @Override
     public Object lock() {
         return owner.lock();
     }
 
     @Override
     public T task() {
         return task;
     }
 
     @Override
     public Pack args() {
         return args;
     }
 
     @Override
     public State getState() {
         synchronized (lock()) {
             return state;
         }
     }
 
     @Override
     public Throwable getThrowable() {
         synchronized (lock()) {
             return throwable;
         }
     }
 
     @Override
     public boolean isInterrupted() {
         synchronized (lock()) {
             return interrupted;
         }
     }
 
     @Override
     public void interrupt() {
         synchronized (lock()) {
             interrupted = true;
 
             switch (state) {
                 case CREATED:
                     // remove from queue and set state to CANCELED
                     state = State.CANCELED;
                     throwable = null;
                     removeFromQueue();
                     break;
                 case STARTED:
                     // try to interrupt working thread if it exists and interruption is allowed
                     if (taskFuture != null && taskFutureCanBeInterrupted) {
                         taskFuture.cancel(true);
                     }
                     break;
                 default:
                     // in other states there are no need to do anything else
                     break;
             }
         }
     }
 
     @Override
     public void join() throws InterruptedException {
         join(0);
     }
 
     @Override
     public boolean join(long timeout) throws InterruptedException {
         synchronized (joinObject) {
             if (timeout < 0) {
                 throw new IllegalArgumentException();
             }
 
             while (!getState().isDestroyed()) {
                 if (timeout == 0) {
                     joinObject.wait();
                 } else {
                     long time = SystemClock.uptimeMillis();
                     joinObject.wait(timeout);
                     timeout -= SystemClock.uptimeMillis() - time;
 
                     if (timeout <= 0) {
                         return false;
                     }
                 }
             }
 
             return true;
         }
     }
 
     private void uncaughtListenerException(TaskListener listener, Throwable throwable) {
         Log.e(TaskExecutor.TAG, "listener " + listener + " failed", throwable);
         Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), throwable);
     }
 
     private void callOnCreate() {
         for (TaskListener listener : listeners) { // in direct order
             try {
                 listener.onCreate(this);
             } catch (Throwable throwable) {
                 uncaughtListenerException(listener, throwable);
             }
         }
     }
 
     private void callOnQueueInsert() {
         for (TaskListener listener : listeners) { // in direct order
             try {
                 listener.onQueueInsert(this);
             } catch (Throwable throwable) {
                 uncaughtListenerException(listener, throwable);
             }
         }
     }
 
     private void callOnStart() {
         for (TaskListener listener : listeners) { // in direct order
             try {
                 listener.onStart(this);
             } catch (Throwable throwable) {
                 uncaughtListenerException(listener, throwable);
             }
         }
     }
 
     private void callOnFinish() {
         for (int i = listeners.length - 1; i >= 0; i--) { // in reverse order
             TaskListener listener = listeners[i];
             try {
                 listener.onFinish(this);
             } catch (Throwable throwable) {
                 uncaughtListenerException(listener, throwable);
             }
         }
     }
 
     private void callOnQueueRemove() {
         for (int i = listeners.length - 1; i >= 0; i--) { // in reverse order
             TaskListener listener = listeners[i];
             try {
                 listener.onQueueRemove(this);
             } catch (Throwable throwable) {
                 uncaughtListenerException(listener, throwable);
             }
         }
     }
 
     private void callOnDestroy() {
         for (int i = listeners.length - 1; i >= 0; i--) { // in reverse order
             TaskListener listener = listeners[i];
             try {
                 listener.onDestroy(this);
             } catch (Throwable throwable) {
                 uncaughtListenerException(listener, throwable);
             }
         }
     }
 
     private void callOnCanceled() {
         for (int i = listeners.length - 1; i >= 0; i--) { // in reverse order
             TaskListener listener = listeners[i];
             try {
                 listener.onCanceled(this);
             } catch (Throwable throwable) {
                 uncaughtListenerException(listener, throwable);
             }
         }
     }
 
     private void callOnFailed() {
         for (int i = listeners.length - 1; i >= 0; i--) { // in reverse order
             TaskListener listener = listeners[i];
             try {
                 listener.onFailed(this);
             } catch (Throwable throwable) {
                 uncaughtListenerException(listener, throwable);
             }
         }
     }
 
     private void callOnSucceed() {
         for (int i = listeners.length - 1; i >= 0; i--) { // in reverse order
             TaskListener listener = listeners[i];
             try {
                 listener.onSucceed(this);
             } catch (Throwable throwable) {
                 uncaughtListenerException(listener, throwable);
             }
         }
     }
 
 }
