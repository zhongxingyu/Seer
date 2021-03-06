 /*
  * Copyright 2007 Sun Microsystems, Inc. All rights reserved
  */
 package com.sun.sgs.impl.nio;
 
 import static java.nio.channels.SelectionKey.OP_ACCEPT;
 import static java.nio.channels.SelectionKey.OP_CONNECT;
 import static java.nio.channels.SelectionKey.OP_READ;
 import static java.nio.channels.SelectionKey.OP_WRITE;
 
 import java.io.Closeable;
 import java.io.IOException;
 import java.nio.channels.AlreadyConnectedException;
 import java.nio.channels.ConnectionPendingException;
 import java.nio.channels.NotYetConnectedException;
 import java.nio.channels.SelectableChannel;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.SocketChannel;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.DelayQueue;
 import java.util.concurrent.Delayed;
 import java.util.concurrent.Executor;
 import java.util.concurrent.FutureTask;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicReference;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.sun.sgs.nio.channels.AbortedByTimeoutException;
 import com.sun.sgs.nio.channels.AcceptPendingException;
 import com.sun.sgs.nio.channels.ClosedAsynchronousChannelException;
 import com.sun.sgs.nio.channels.CompletionHandler;
 import com.sun.sgs.nio.channels.IoFuture;
 import com.sun.sgs.nio.channels.ReadPendingException;
 import com.sun.sgs.nio.channels.ShutdownChannelGroupException;
 import com.sun.sgs.nio.channels.WritePendingException;
 
 /**
  * TODO doc
  */
 class Reactor {
 
     /** The logger for this class. */
     static final Logger log = Logger.getLogger(Reactor.class.getName());
 
     /** TODO doc */
     final Object selectorLock = new Object();
 
     /** TODO doc */
     final ReactiveChannelGroup group;
 
     /** TODO doc */
     final Selector selector;
 
     /** TODO doc */
     final Executor executor;
 
     /** TODO doc */
     final DelayQueue<TimeoutHandler> timeouts =
         new DelayQueue<TimeoutHandler>();
 
     /** TODO doc */
     volatile boolean shuttingDown = false;
 
     /**
      * TODO doc
      * @param group
      * @param executor
      * @throws IOException
      */
     Reactor(ReactiveChannelGroup group, Executor executor) throws IOException {
         this.group = group;
         this.executor = executor;
         this.selector = group.selectorProvider().openSelector();
     }
 
     /**
      * TODO doc
      */
     void shutdown() {
         if (shuttingDown)
             return;
         synchronized (selectorLock) {
             shuttingDown = true;
             selector.wakeup();
         }
     }
 
     /**
      * TODO doc
      * @throws IOException 
      */
     void shutdownNow() throws IOException {
         if (shuttingDown)
             return;
         synchronized (selectorLock) {
             shuttingDown = true;
             selector.wakeup();
             for (SelectionKey key : selector.keys()) {
                 try {
                     Closeable asyncKey =
                         (Closeable) key.attachment();
                     if (asyncKey != null)
                         asyncKey.close();
                 } catch (IOException ignore) { }
             }
         }
     }
 
     /**
      * TODO doc
      * 
      * @return {@code false} if this reactor is stopped,
      *         otherwise {@code true}
      */
     boolean performWork() {
         try {
 
             if (! selector.isOpen()) {
                 log.log(Level.WARNING, "{0} selector is closed", this);
                 return false;
             }
 
             synchronized (selectorLock) {
                 // Obtain and release the guard to allow other tasks
                 // to run after waking the selector.
 
                 if (log.isLoggable(Level.FINER)) {
                     int numKeys = selector.keys().size();
                     log.log(Level.FINER, "{0} select on {1} keys",
                         new Object[] { this, numKeys });
                     if (numKeys <= 5) {
                         for (SelectionKey key : selector.keys()) {
                             log.log(Level.FINER,
                                 "{0} select interestOps {1} on {2}",
                                 new Object[] {
                                     this,
                                     Util.formatOps(key.interestOps()),
                                     key.attachment() });
                         }
                     }
                 }
             }
 
             int readyCount;
 
             final Delayed nextExpiringTask = timeouts.peek();
             if (nextExpiringTask == null) {
                 readyCount = selector.select(getSelectorTimeout(timeouts));
             } else {
                 long nextTimeoutMillis =
                     nextExpiringTask.getDelay(TimeUnit.MILLISECONDS);
                 if (nextTimeoutMillis <= 0) {
                     readyCount = selector.selectNow();
                 } else {
                     readyCount = selector.select(nextTimeoutMillis);
                 }
             }
 
             if (log.isLoggable(Level.FINER)) {
                 log.log(Level.FINER, "{0} selected {1} / {2}",
                     new Object[] { this, readyCount, selector.keys().size() });
             }
 
             if (shuttingDown) {
                 if (log.isLoggable(Level.FINE)) {
                     log.log(Level.FINE, "{0} wants shutdown, {1} keys",
                         new Object[] { this, selector.keys().size() });
                 }
                 if (selector.keys().isEmpty()) {
                     selector.close();
                     return false;
                 }
             }
 
             final Iterator<SelectionKey> keys =
                 selector.selectedKeys().iterator();
 
             while (keys.hasNext()) {
                 SelectionKey key = keys.next();
                 keys.remove();
 
                 ReactiveAsyncKey asyncKey =
                     (ReactiveAsyncKey) key.attachment();
 
                 int readyOps;
                 synchronized (asyncKey) {
                     if (! key.isValid())
                         continue;
                     readyOps = key.readyOps();
                     key.interestOps(key.interestOps() & (~ readyOps));
                 }
                 asyncKey.selected(readyOps);
             }
 
             final List<TimeoutHandler> expiredHandlers =
                 new ArrayList<TimeoutHandler>();
             timeouts.drainTo(expiredHandlers);
 
             for (TimeoutHandler expired : expiredHandlers)
                 expired.run();
 
             expiredHandlers.clear();
 
         } catch (Throwable t) {
             log.log(Level.WARNING, this.toString(), t);
             return false;
         }
 
         return true;
     }
 
     /**
      * TODO doc
      * @param ch
      * @return an {@link AsyncKey} for the given channel
      * @throws IOException
      */
     ReactiveAsyncKey
     register(SelectableChannel ch) throws IOException {
         synchronized (selectorLock) {
             if (shuttingDown)
                 throw new ShutdownChannelGroupException();
 
             selector.wakeup();
             SelectionKey key = ch.register(selector, 0);
 
             ReactiveAsyncKey asyncKey = new ReactiveAsyncKey(key);
             key.attach(asyncKey);
             return asyncKey;
         }
     }
 
     /**
      * TODO doc
      * @param asyncKey
      */
     void
     unregister(ReactiveAsyncKey asyncKey) {
         asyncKey.key.cancel();
         selector.wakeup();
     }
 
     /**
      * TODO doc
      * @param <R>
      * @param <A>
      * @param asyncKey
      * @param op
      * @param task
      */
     <R, A> void
     awaitReady(ReactiveAsyncKey asyncKey, int op, AsyncOp<R> task)
     {
         synchronized (selectorLock) {
             selector.wakeup();
             SelectionKey key = asyncKey.key;
             SelectableChannel channel = asyncKey.channel();
             int interestOps;
             synchronized (asyncKey) {
                 if (key == null || (! key.isValid())) {
                     if (log.isLoggable(Level.FINE)) {
                         log.log(Level.FINE, "{0} awaitReady {1} : invalid",
                             new Object[] { this, Util.opName(op) });
                     }
                     throw new ClosedAsynchronousChannelException();
                 }
 
                 interestOps = key.interestOps();
 
                 if (log.isLoggable(Level.FINEST)) {
                     log.log(Level.FINEST,
                         "{0} awaitReady {1} : old {2} : add {3}",
                         new Object[] { this,
                                        task,
                                        Util.formatOps(interestOps),
                                        Util.formatOps(op) });
                 }
 
                 if (channel instanceof SocketChannel) {
                     switch (op) {
                     case OP_READ:
                     case OP_WRITE:
                         if (! ((SocketChannel) channel).isConnected())
                             throw new NotYetConnectedException();
                         break;
                     case OP_CONNECT:
                         if (((SocketChannel) channel).isConnected())
                             throw new AlreadyConnectedException();
                         break;
                     default:
                         break;
                     }
                 }
 
                interestOps |= op;
                key.interestOps(interestOps);
             }
 
             if (log.isLoggable(Level.FINEST)) {
                 log.log(Level.FINEST, "{0}, awaitReady {1} : new {2} ",
                     new Object[] { this, task, Util.formatOps(interestOps) });
             }
         }
     }
 
     /**
      * TODO doc
      * @param queue
      * @return the timeout of the next operation that will expire, or
      *         {@code 0} if no timeouts are pending
      */
     static int getSelectorTimeout(DelayQueue<? extends Delayed> queue) {
         final Delayed t = queue.peek();
         return (t == null)
                    ? 0
                    : (int) (t.getDelay(TimeUnit.MILLISECONDS) - 
                             System.currentTimeMillis());
     }
 
     /**
      * TODO doc
      * @param <R> the result type
      */
     static class AsyncOp<R> extends FutureTask<R> {
 
         /**
          * TODO doc
          * @param callable
          */
         AsyncOp(Callable<R> callable) {
             super(callable);
         }
 
         /**
          * TODO doc
          */
         void timeoutExpired() {
             setException(new AbortedByTimeoutException());
         }
     }
 
     /**
      * TODO doc
      */
     class PendingOperation {
 
         /** TODO doc */
         protected final AtomicReference<AsyncOp<?>> task =
             new AtomicReference<AsyncOp<?>>();
 
         /** TODO doc */
         private volatile TimeoutHandler timeoutHandler = null;
 
         /** TODO doc */
         private final ReactiveAsyncKey asyncKey;
 
         /** TODO doc */
         private final int op;
 
         /**
          * TODO doc
          * @param asyncKey
          * @param op
          */
         PendingOperation(ReactiveAsyncKey asyncKey, int op) {
             this.asyncKey = asyncKey;
             this.op = op;
         }
 
         /**
          * TODO doc
          */
         protected void pendingPolicy() {}
 
         /**
          * TODO doc
          */
         void selected() {
             Runnable selectedTask = task.getAndSet(null);
             if (selectedTask == null) {
                 log.log(Level.FINEST,
                     "selected but nothing to do {0}", this);
                 return;
             } else {
                 log.log(Level.FINER, "selected {0}", this);
                 selectedTask.run();
             }
         }
 
         /**
          * TODO doc
          * 
          * @return {@code true} if this operation is pending, otherwise
          *         {@code false}
          */
         boolean isPending() {
             return task.get() != null;
         }
 
         /**
          * TODO doc
          */
         void timeoutExpired() {
             AsyncOp<?> expiredTask = task.getAndSet(null);
             if (expiredTask == null) {
                 log.log(Level.FINEST,
                     "timed out but nothing to do {0}", this);
                 return;
             } else {
                 log.log(Level.FINER, "timeout {0}", this);
                 expiredTask.timeoutExpired();
             }
         }
 
         /**
          * TODO doc
          */
         void cleanupTask() {
             if (timeoutHandler != null) {
                 try {
                     timeouts.remove(timeoutHandler);
                 } catch (Throwable ignore) { }
                 timeoutHandler = null;
             }
             task.set(null);
         }
 
         /**
          * TODO doc
          * @param <R>
          * @param <A>
          * @param attachment
          * @param handler
          * @param timeout
          * @param unit
          * @param callable
          * @return an {@code IoFuture} representing the pending operation
          */
         <R, A> IoFuture<R, A>
         execute(final A attachment,
                 final CompletionHandler<R, ? super A> handler,
                 long timeout,
                 TimeUnit unit,
                 Callable<R> callable)
         {
             if (timeout < 0)
                 throw new IllegalArgumentException("Negative timeout");
 
             AsyncOp<R> opTask = new AsyncOp<R>(callable) {
                 @Override
                 protected void done() {
                     cleanupTask();
                     group.executeCompletion(handler, attachment, this);
                 }};
 
             if (! task.compareAndSet(null, opTask))
                 pendingPolicy();
 
             if (timeout > 0) {
                 timeoutHandler = new TimeoutHandler(this, timeout, unit);
                 timeouts.add(timeoutHandler);
             }
 
             try {
                 Reactor.this.awaitReady(asyncKey, op, opTask);
             } catch (RuntimeException e) {
                 cleanupTask();
                 throw e;
             }
 
             return AttachedFuture.wrap(opTask, attachment);
         }
 
         /**
          * {@inheritDoc}
          */
         @Override
         public String toString() {
             return String.format("PendingOp[key=%s,op=%s]",
                 asyncKey, Util.opName(op));
         }
     }
 
     /**
      * TODO doc
      */
     class ReactiveAsyncKey implements AsyncKey {
 
         /** TODO doc */
         final SelectionKey key;
 
         /** TODO doc */
         final PendingOperation pendingAccept =
             new PendingOperation(this, OP_ACCEPT) {
                 protected void pendingPolicy() {
                     throw new AcceptPendingException();
                 }};
 
         /** TODO doc */
         final PendingOperation pendingConnect =
             new PendingOperation(this, OP_CONNECT) {
                 protected void pendingPolicy() {
                     throw new ConnectionPendingException();
                 }};
 
         /** TODO doc */
         final PendingOperation pendingRead =
             new PendingOperation(this, OP_READ) {
                 protected void pendingPolicy() {
                     throw new ReadPendingException();
                 }};
 
         /** TODO doc */
         final PendingOperation pendingWrite = 
             new PendingOperation(this, OP_WRITE) {
                 protected void pendingPolicy() {
                     throw new WritePendingException();
                 }};
 
         /**
          * TODO doc
          * @param key
          */
         ReactiveAsyncKey(SelectionKey key) {
             this.key = key;
         }
 
         /**
          * {@inheritDoc}
          */
         public void close() throws IOException {
             log.log(Level.FINER, "closing {0}", this);
             if (! key.isValid()) {
                 log.log(Level.FINE, "key is already invalid {0}", this);
             }
             Reactor.this.unregister(this);
             try {
                 key.channel().close();
             } finally {
                 selected(OP_ACCEPT | OP_CONNECT | OP_READ | OP_WRITE);
             }
         }
 
         /**
          * {@inheritDoc}
          */
         public boolean isOpPending(int op) {
             switch (op) {
             case OP_ACCEPT:
                 return pendingAccept.isPending();
             case OP_CONNECT:
                 return pendingConnect.isPending();
             case OP_READ:
                 return pendingRead.isPending();
             case OP_WRITE:
                 return pendingWrite.isPending();
             default:
                 throw new IllegalArgumentException("bad op " + op);
             }
         }
 
         /**
          * {@inheritDoc}
          */
         public SelectableChannel channel() {
             return key.channel();
         }
 
         /**
          * {@inheritDoc}
          */
         public void selected(int readyOps) {
             if ((readyOps & OP_WRITE) != 0)
                 pendingWrite.selected();
             if ((readyOps & OP_READ) != 0)
                 pendingRead.selected();
             if ((readyOps & OP_CONNECT) != 0)
                 pendingConnect.selected();
             if ((readyOps & OP_ACCEPT) != 0)
                 pendingAccept.selected();
         }
 
         /**
          * {@inheritDoc}
          */
         public <R, A> IoFuture<R, A>
         execute(int op, A attachment, CompletionHandler<R, ? super A> handler,
                 long timeout, TimeUnit unit, Callable<R> callable)
         {
             switch (op) {
             case OP_WRITE:
                 return pendingWrite.execute(
                     attachment, handler, timeout, unit, callable);
             case OP_READ:
                 return pendingRead.execute(
                     attachment, handler, timeout, unit, callable);
             case OP_CONNECT:
                 return pendingConnect.execute(
                     attachment, handler, timeout, unit, callable);
             case OP_ACCEPT:
                 return pendingAccept.execute(
                     attachment, handler, timeout, unit, callable);
             default:
                 throw new IllegalArgumentException("bad op " + op);
             }
         }
 
         /**
          * {@inheritDoc}
          */
         public void execute(Runnable command) {
             executor.execute(command);
         }
 
         /**
          * {@inheritDoc}
          */
         @Override
         public String toString() {
             return String.format(
                 "ReactiveAsyncKey[reactor=%s,channel=%s,valid=%b]",
                 Reactor.this, key.channel(), key.isValid());
         }
     }
 
     /**
      * TODO doc
      */
     static final class TimeoutHandler implements Delayed, Runnable {
 
         /** TODO doc */
         private final PendingOperation task;
 
         /** TODO doc */
         private final long deadlineMillis;
 
         /**
          * TODO doc
          * @param task
          * @param timeout
          * @param unit
          */
         TimeoutHandler(PendingOperation task, long timeout, TimeUnit unit) {
             this.task = task;
             this.deadlineMillis =
                 unit.toMillis(timeout) + System.currentTimeMillis();
         }
 
         /** {@inheritDoc} */
         public void run() {
             task.timeoutExpired();
         }
 
         /** {@inheritDoc} */
         public long getDelay(TimeUnit unit) {
             return unit.convert(
                 deadlineMillis - System.currentTimeMillis(),
                 TimeUnit.MILLISECONDS);
         }
 
         /** {@inheritDoc} */
         public int compareTo(Delayed o) {
             if (o == this)
                 return 0;
             if (o instanceof TimeoutHandler) {
                 return Long.signum(
                     deadlineMillis - ((TimeoutHandler)o).deadlineMillis);
             } else {
                 return Long.signum(getDelay(TimeUnit.MILLISECONDS) -
                                    o.getDelay(TimeUnit.MILLISECONDS));
             }
         }
 
         /** {@inheritDoc} */
         @Override
         public boolean equals(Object obj) {
             if (obj == this)
                 return true;
             if (!(obj instanceof TimeoutHandler))
                 return false;
             TimeoutHandler other = (TimeoutHandler) obj;
             return (deadlineMillis == other.deadlineMillis) &&
                    task.equals(other.task);
         }
 
         /** {@inheritDoc} */
         @Override
         public int hashCode() {
             return task.hashCode() ^ Long.valueOf(deadlineMillis).hashCode();
         }
     }
 }
