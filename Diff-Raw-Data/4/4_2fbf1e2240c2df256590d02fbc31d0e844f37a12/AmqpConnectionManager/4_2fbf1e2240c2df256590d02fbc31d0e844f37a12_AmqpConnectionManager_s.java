 /*
  * Copyright (C) 2010-2011, Zenoss Inc.  All Rights Reserved.
  */
 package org.zenoss.amqp;
 
 import com.google.protobuf.ExtensionRegistry;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.UUID;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorCompletionService;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 /**
  * Class which maintains a persistent connection to an AMQP server and allows
  * multiple consumers and producers to work in parallel off the connection. It
  * gracefully handles connection retries to the server, as well as restarting
  * all workers when the connection is torn down. After creating the connection
  * manager, call {@link #init()} to initialize the connection and when finished
  * call {@link #shutdown()} to tear down the connection manager.
  */
 public class AmqpConnectionManager {
 
     public static final int DEFAULT_RETRY_INTERVAL = 1000;
 
     private static final Logger log = LoggerFactory.getLogger(AmqpConnectionManager.class);
 
     private final long retry;
     private final AmqpServerUri uri;
     private final ExecutorCompletionService<Object> ecs;
     private final ExecutorService pool;
     private final Map<String, QueueWorker> workers = new ConcurrentHashMap<String, QueueWorker>();
 
     private volatile Connection connection;
     private final ConcurrentHashMap<String, Publisher<com.google.protobuf.Message>> publishers =
             new ConcurrentHashMap<String, Publisher<com.google.protobuf.Message>>();
 
     private final ExecutorService executor = Executors.newSingleThreadExecutor();
     private volatile ExtensionRegistry extensionRegistry;
 
     /**
      * Creates an {@link AmqpConnectionManager} which will perform operations
      * against the specified AMQP server. The connection timeout is set to the
      * default retry interval of {@link #DEFAULT_RETRY_INTERVAL}.
      * 
      * @param uri
      *            The AMQP server uri.
      */
     public AmqpConnectionManager(AmqpServerUri uri) {
         this(uri, DEFAULT_RETRY_INTERVAL);
     }
 
     /**
      * Creates an {@link AmqpConnectionManager} which performs operations
      * against the specified AMQP server with the specified connection retry
      * interval.
      * 
      * @param uri
      *            The AMQP server uri.
      * @param retry
      *            The connection retry interval.
      */
     public AmqpConnectionManager(AmqpServerUri uri, long retry) {
         this.retry = retry;
         this.uri = uri;
         this.pool = Executors.newCachedThreadPool();
         this.ecs = new ExecutorCompletionService<Object>(this.pool);
     }
 
     public void setExtensionRegistry(ExtensionRegistry extensionRegistry) {
         this.extensionRegistry = extensionRegistry;
     }
 
     private Channel openChannel() throws AmqpException {
         return this.connection.openChannel();
     }
 
     private static void getFuture(Future<?> future) {
         try {
             future.get();
         } catch (ExecutionException e) {
             log.debug("exception", e);
         } catch (InterruptedException e) {
             Thread.currentThread().interrupt();
         }
     }
 
     /**
      * Adds a queue listener for the specified queue configuration.
      * 
      * @param config
      *            Queue configuration which contains the queue and queue
      *            bindings.
      * @param listener
      *            Queue listener which is called when messages are consumed from
      *            the queue.
      * @return Unique identifier for this listener (used to disable it with {@link #removeListener(String)}.
      */
     public String addListener(QueueConfiguration config, QueueListener listener) {
         if (config == null || listener == null) {
             throw new NullPointerException();
         }
         final String uuid = UUID.randomUUID().toString();
         final QueueWorker worker = new QueueWorker(uuid, config, listener, this);
         this.workers.put(uuid, worker);
         // If we're already running throw it in the pool
         if (this.connection != null) {
             try {
                 worker.setFuture(this.ecs.submit(worker));
             } finally {
                 /* In case of error, don't keep around reference to worker */
                 this.workers.remove(uuid);
             }
         }
         return uuid;
     }
 
     /**
      * Removes and stops the listener with the specified identifier.
      *
      * @param listenerId The identifier of the listener (as returned by
      *                   {@link #addListener(QueueConfiguration, QueueListener)}).
      */
     public void removeListener(String listenerId) {
         final QueueWorker worker = this.workers.get(listenerId);
         if (worker != null) {
             worker.shutdown();
             final Future<Object> future = worker.getFuture();
             if (future != null) {
                 getFuture(future);
             }
             worker.reset();
         }
         else {
             log.info("Unknown listener: {}", listenerId);
         }
     }
 
     private Publisher<com.google.protobuf.Message> getPublisher(ExchangeConfiguration config) throws AmqpException {
         if (config == null) {
             throw new NullPointerException();
         }
         Exchange exchange = config.getExchange();
         Publisher<com.google.protobuf.Message> pub = this.publishers.get(exchange.getName());
         if (pub == null) {
             Channel channel = this.openChannel();
             channel.declareExchange(exchange);
             pub = channel.createPublisher(exchange, new ProtobufConverter(config.getMessages()));
             Publisher<com.google.protobuf.Message> previous = this.publishers.putIfAbsent(exchange.getName(), pub);
             if (previous != null) {
                 pub = previous;
                 try {
                     channel.close();
                 } catch (IOException e) {
                     throw new AmqpException(e.getLocalizedMessage(), e);
                 }
             }
         }
         return pub;
     }
 
     /**
      * Publishes the message to the specified exchange with the given routing
      * key. If the exchange does not exist it is created.
      * 
      * @param config
      *            The configuration for the exchange (including the exchange and
      *            the types of protobuf messages which can be published to the
      *            exchange).
      * @param routingKey
      *            The routing key to be used for the message.
      * @param message
      *            The message to publish.
      * @throws AmqpException
      *             If the message cannot be published to the exchange.
      */
     public void publish(ExchangeConfiguration config, String routingKey,
             com.google.protobuf.Message message) throws AmqpException {
        Publisher<com.google.protobuf.Message> pub = this.getPublisher(config);
         try {
             pub.publish(message, routingKey);
         } catch (AmqpException e) {
             // Reconnect and try one more time. If we fail the second time throw
             // the exception.
             try {
                 this.reconnect();
             } catch (InterruptedException ie) {
                 Thread.currentThread().interrupt();
                 throw e;
             }
             pub = this.getPublisher(config);
             pub.publish(message, routingKey);
         }
     }
 
     /**
      * Creates a batch publisher for the specified exchange.
      * 
      * @param config
      *            Exchange configuration.
      * @return A batch publisher for the exchange. It must be closed with
      *         {@link BatchPublisher#close()} when it is no longer needed.
      * @throws AmqpException
      *             If an exception occurs.
      */
     public BatchPublisher<com.google.protobuf.Message> createBatchPublisher(
             ExchangeConfiguration config) throws AmqpException {
         if (config == null) {
             throw new NullPointerException();
         }
         Channel channel = null;
         Exchange exchange = config.getExchange();
         try {
             channel = this.openChannel();
             channel.declareExchange(exchange);
             return channel.createBatchPublisher(exchange, new ProtobufConverter(config.getMessages()));
         } catch (AmqpException e) {
             if (channel != null) {
                 try {
                     channel.close();
                 } catch (IOException ioe) {
                     log.warn("Failed to close channel: {}", ioe);
                 }
             }
             throw e;
         }
     }
 
     /**
      * Initializes the {@link AmqpConnectionManager}. This will establish a
      * connection to the {@link AmqpServerUri} and start any configured
      * consumers. This method must be called prior to using the
      * {@link AmqpConnectionManager}.
      */
     public void init() {
         this.executor.submit(new ThreadRenamingCallable<Object>("AmqpConnectionManager") {
             @Override
             protected Object doCall() throws Exception {
                 runInternal();
                 return null;
             }
         });
     }
 
     private static boolean isAmqpException(Exception e) {
         boolean isAmqpException = false;
         Throwable current = e;
         while (current != null) {
             if (current instanceof AmqpException) {
                 isAmqpException = true;
                 break;
             }
             current = current.getCause();
         }
         return isAmqpException;
     }
 
     private void runInternal() throws InterruptedException {
         this.reconnect();
         Future<Object> future;
         while ((future = ecs.take()) != null) {
             QueueWorker worker = null;
             for (QueueWorker info : this.workers.values()) {
                 if (future.equals(info.getFuture())) {
                     worker = info;
                     break;
                 }
             }
             if (worker == null) {
                 log.debug("Unable to associate Future with QueueWorker: {}", future);
                 continue;
             }
             if (!future.isCancelled()) {
                 try {
                     future.get();
                     log.debug("Queue worker completed successfully: {}", worker.getWorkerId());
                     this.workers.remove(worker.getWorkerId());
                 } catch (ExecutionException e) {
                     if (isAmqpException(e)) {
                         log.debug("exception", e);
                         this.reconnect();
                     } else {
                         log.info("Restarting single worker due to exception.", e);
                         worker.reset();
                         worker.setFuture(this.ecs.submit(worker));
                     }
                 }
             }
             else {
                 this.workers.remove(worker.getWorkerId());
             }
         }
     }
 
     private synchronized void disconnect() {
         if (this.connection != null) {
             try {
                 /* Stop the workers if they are running */
                 for (QueueWorker worker : workers.values()) {
                     worker.shutdown();
 
                     final Future<Object> future = worker.getFuture();
                     if (future != null) {
                         getFuture(future);
                         worker.setFuture(null);
                     }
                     /* Reset the worker so it can be used again */
                     worker.reset();
                 }
 
                 /* Close and remove publishers */
                 final Iterator<Publisher<com.google.protobuf.Message>> publisherIt =
                         this.publishers.values().iterator();
                 while (publisherIt.hasNext()) {
                     try {
                         final Publisher publisher = publisherIt.next();
                         publisherIt.remove();
                         publisher.getChannel().close();
                     } catch (IOException ignored) {
                     }
                 }
             } finally {
                 try {
                     this.connection.close();
                 } catch (IOException ignored) {
                 } finally {
                     this.connection = null;
                 }
             }
         }
     }
 
     private synchronized boolean connect() {
         try {
             this.connection = ConnectionFactory.newInstance().newConnection(this.uri);
         } catch (AmqpException e) {
             log.debug("Unable to connect", e);
             return false;
         }
         log.info("Connected to message broker at {}", this.uri);
         for (QueueWorker worker : this.workers.values()) {
             worker.setFuture(this.ecs.submit(worker));
         }
         return true;
     }
 
     private void reconnect() throws InterruptedException {
         log.info("Attempting to connect to message broker at {}", this.uri);
         this.disconnect();
         while (!this.connect()) {
             Thread.sleep(retry);
         }
     }
 
     /**
      * Shuts down the {@link AmqpConnectionManager}. The connection to the
      * server is shut down and any consumers are stopped.
      */
     public void shutdown() {
         log.info("Shutting down...");
         this.disconnect();
         this.executor.shutdownNow();
         this.pool.shutdownNow();
     }
 
     private static class QueueWorker extends ThreadRenamingCallable<Object> {
 
         private static final Logger log = LoggerFactory.getLogger(QueueWorker.class);
 
         private final String workerId;
         private final QueueConfiguration config;
         private final AmqpConnectionManager manager;
         private final QueueListener listener;
         private Future<Object> future;
         private volatile boolean shutdown = false;
         private volatile Thread runningThread = null;
 
         private QueueWorker(String workerId, QueueConfiguration config, QueueListener listener,
                             AmqpConnectionManager manager) {
             super(config.getQueue().getName());
             this.workerId = workerId;
             this.manager = manager;
             this.config = config;
             this.listener = listener;
         }
 
         /**
          * The unique identifier for the worker.
          *
          * @return The unique identifier for the worker.
          */
         public String getWorkerId() {
             return workerId;
         }
 
         /**
          * Returns the configuration for the queue this worker is consuming from.
          *
          * @return The queue configuration this worker is consuming from.
          */
         public QueueConfiguration getConfig() {
             return config;
         }
 
         /**
          * Returns the future object (or null if the worker hasn't been started).
          *
          * @return The future object (or null if the worker hasn't been started).
          */
         public synchronized Future<Object> getFuture() {
             return future;
         }
 
         /**
          * Sets the future object for the worker.
          *
          * @param future The future object for the worker.
          */
         public synchronized void setFuture(Future<Object> future) {
             this.future = future;
         }
 
         /**
          * After a worker has been run, it should be reset to allow to be submitted again.
          */
         public void reset() {
             this.shutdown = false;
             this.runningThread = null;
         }
 
         @Override
         protected Object doCall() throws Exception {
             this.runningThread = Thread.currentThread();
             if (this.shutdown) {
                 throw new IllegalStateException("This worker has already been shut down");
             }
             final Channel channel = manager.openChannel();
             this.listener.configureChannel(channel);
             channel.declareQueue(config.getQueue());
             for (Binding binding : config.getBindings()) {
                 channel.declareExchange(binding.getExchange());
                 channel.bindQueue(binding);
             }
             final ProtobufConverter converter = new ProtobufConverter(this.config.getMessages());
             if (manager.extensionRegistry != null) {
                 converter.setExtensionRegistry(manager.extensionRegistry);
             }
             final Consumer<com.google.protobuf.Message> consumer = channel.createConsumer(this.config.getQueue(),
                     converter);
             log.info("Worker started, consuming messages on queue: {}", config.getQueue().getName());
             Message<com.google.protobuf.Message> message;
             try {
                 while (!this.shutdown) {
                     try {
                         while ((message = consumer.nextMessage()) != null) {
                             this.listener.receive(message, consumer);
                         }
                     } catch (MessageDecoderException e) {
                         // Unsupported message in this queue - reject the message
                         log.warn("Failed to decode message in queue: {}", e);
                         consumer.rejectMessage(e.getRawMessage(), false);
                     } catch (InterruptedException e) {
                         Thread.currentThread().interrupt();
                     }
                 }
                 return null;
             } finally {
                 log.debug("Stopping worker for queue: {}", config.getQueue().getName());
                 try {
                     consumer.cancel();
                 } catch (AmqpException ignored) {
                 }
                 try {
                     channel.close();
                 } catch (IOException ignored) {
                 }
                 this.runningThread = null;
             }
         }
 
         public void shutdown() {
             this.shutdown = true;
             if (this.runningThread != null) {
                 this.runningThread.interrupt();
             }
         }
     }
 }
