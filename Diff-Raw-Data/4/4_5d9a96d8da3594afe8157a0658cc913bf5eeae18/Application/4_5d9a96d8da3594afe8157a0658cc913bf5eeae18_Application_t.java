 /*
  * Copyright (C) 2010-2011, Zenoss Inc.  All Rights Reserved.
  */
 package org.zenoss.zep.impl;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.BeansException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.context.ApplicationListener;
 import org.springframework.dao.TransientDataAccessException;
 import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
 import org.zenoss.amqp.AmqpConnectionManager;
 import org.zenoss.amqp.QueueConfig;
 import org.zenoss.amqp.QueueConfiguration;
 import org.zenoss.amqp.ZenossQueueConfig;
 import org.zenoss.protobufs.zep.Zep.EventSeverity;
 import org.zenoss.protobufs.zep.Zep.ZepConfig;
 import org.zenoss.zep.HeartbeatProcessor;
 import org.zenoss.zep.PluginService;
 import org.zenoss.zep.ZepException;
 import org.zenoss.zep.dao.ConfigDao;
 import org.zenoss.zep.dao.EventArchiveDao;
 import org.zenoss.zep.dao.EventDetailsConfigDao;
 import org.zenoss.zep.dao.EventStoreDao;
 import org.zenoss.zep.dao.Purgable;
 import org.zenoss.zep.events.IndexDetailsUpdatedEvent;
 import org.zenoss.zep.events.PluginServiceStartedEvent;
 import org.zenoss.zep.events.ZepConfigUpdatedEvent;
 import org.zenoss.zep.events.ZepEvent;
 import org.zenoss.zep.index.EventIndexer;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
import java.util.concurrent.CancellationException;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Future;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 /**
  * Represents core application logic for ZEP, including the scheduled aging and
  * purging of events.
  */
 public class Application implements ApplicationContextAware, ApplicationListener<ZepEvent> {
 
     private static final Logger logger = LoggerFactory.getLogger(Application.class);
 
     private ThreadPoolTaskScheduler scheduler;
     private ScheduledFuture<?> eventSummaryAger = null;
     private ScheduledFuture<?> eventSummaryArchiver = null;
     private ScheduledFuture<?> eventArchivePurger = null;
     private ScheduledFuture<?> eventIndexerFuture = null;
     private ScheduledFuture<?> heartbeatFuture = null;
     private ZepConfig oldConfig = null;
     private ZepConfig config;
 
     private long indexIntervalMilliseconds = 1000L;
     private int heartbeatIntervalSeconds = 60;
 
     private long agingIntervalMilliseconds = TimeUnit.MINUTES.toMillis(1);
     private int agingLimit = 100;
 
     private long archiveIntervalMilliseconds = TimeUnit.MINUTES.toMillis(1);
     private int archiveLimit = 100;
 
     private final boolean enableIndexing;
 
     private AmqpConnectionManager amqpConnectionManager;
     private ConfigDao configDao;
     private EventStoreDao eventStoreDao;
     private EventArchiveDao eventArchiveDao;
     private EventIndexer eventIndexer;
     private EventDetailsConfigDao eventDetailsConfigDao;
     private HeartbeatProcessor heartbeatProcessor;
     private PluginService pluginService;
     private ApplicationContext applicationContext;
     private ExecutorService queueExecutor;
 
     private List<String> queueListeners = new ArrayList<String>();
 
     public Application(boolean enableIndexing) {
         this.enableIndexing = enableIndexing;
     }
 
     public void setEventStoreDao(EventStoreDao eventStoreDao) {
         this.eventStoreDao = eventStoreDao;
     }
 
     public void setEventArchiveDao(EventArchiveDao eventArchiveDao) {
         this.eventArchiveDao = eventArchiveDao;
     }
 
     public void setAmqpConnectionManager(AmqpConnectionManager amqpConnectionManager) {
         this.amqpConnectionManager = amqpConnectionManager;
     }
 
     public void setConfigDao(ConfigDao configDao) {
         this.configDao = configDao;
     }
 
     public void setEventIndexer(EventIndexer eventIndexer) {
         this.eventIndexer = eventIndexer;
     }
 
     public void setIndexIntervalMilliseconds(long indexIntervalMilliseconds) {
         this.indexIntervalMilliseconds = indexIntervalMilliseconds;
     }
 
     public void setEventDetailsConfigDao(EventDetailsConfigDao eventDetailsConfigDao) {
         this.eventDetailsConfigDao = eventDetailsConfigDao;
     }
 
     public void setHeartbeatProcessor(HeartbeatProcessor heartbeatProcessor) {
         this.heartbeatProcessor = heartbeatProcessor;
     }
     
     public void setHeartbeatIntervalSeconds(int heartbeatIntervalSeconds) {
         this.heartbeatIntervalSeconds = heartbeatIntervalSeconds;
     }
 
     public void setPluginService(PluginService pluginService) {
         this.pluginService = pluginService;
     }
 
     public void setQueueExecutor(ExecutorService queueExecutor) {
         this.queueExecutor = queueExecutor;
     }
 
     public void setScheduler(ThreadPoolTaskScheduler scheduler) {
         this.scheduler = scheduler;
     }
 
     public void setAgingIntervalMilliseconds(long agingIntervalMilliseconds) {
         this.agingIntervalMilliseconds = agingIntervalMilliseconds;
     }
 
     public void setAgingLimit(int agingLimit) {
         this.agingLimit = agingLimit;
     }
 
     public void setArchiveIntervalMilliseconds(long archiveIntervalMilliseconds) {
         this.archiveIntervalMilliseconds = archiveIntervalMilliseconds;
     }
 
     public void setArchiveLimit(int archiveLimit) {
         this.archiveLimit = archiveLimit;
     }
 
     @Override
     public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
         this.applicationContext = applicationContext;
     }
 
     private void init() throws ZepException {
         logger.info("Initializing ZEP");
         this.config = configDao.getConfig();
 
         // Initialize the default event details
         this.eventDetailsConfigDao.init();
         
         /*
          * We must initialize partitions first to ensure events have a partition
          * where they can be created before we start processing the queue. This
          * init method is run before the event processor starts due to a hard
          * dependency in the Spring config on this.
          */
         initializePartitions();
         startEventSummaryAging();
         startEventSummaryArchiving();
         startEventArchivePurging();
         startEventIndexer();
         startHeartbeatProcessing();
         startQueueListeners();
         logger.info("Completed ZEP initialization");
     }
 
     public void shutdown() throws ZepException {
         this.scheduler.shutdown();
 
         try {
             this.scheduler.getScheduledExecutor().awaitTermination(0L, TimeUnit.SECONDS);
             logger.info("Scheduled tasks finished");
         } catch (InterruptedException e) {
             Thread.currentThread().interrupt();
         }
 
         stopQueueListeners();
 
         this.queueExecutor.shutdown();
         try {
             this.queueExecutor.awaitTermination(0L, TimeUnit.SECONDS);
         } catch (InterruptedException e) {
             Thread.currentThread().interrupt();
             this.queueExecutor.shutdownNow();
         }
 
         this.amqpConnectionManager.shutdown();
 
         this.pluginService.shutdown();
     }
 
     private void cancelFuture(Future<?> future) {
         if (future != null) {
             future.cancel(true);
             try {
                 future.get();
             } catch (ExecutionException e) {
                 logger.warn("exception", e);
             } catch (InterruptedException e) {
                 logger.debug("Interrupted", e);
            } catch (CancellationException e) {
                /* Expected - we just canceled above */
             }
         }
     }
 
     private void initializePartitions() throws ZepException {
         eventArchiveDao.initializePartitions();
     }
 
     private void startEventIndexer() throws ZepException {
         cancelFuture(this.eventIndexerFuture);
         this.eventIndexerFuture = null;
 
         // Rebuild the index if necessary.
         this.eventIndexer.init();
 
         if (this.enableIndexing) {
             logger.info("Starting event indexing at interval: {} millisecond(s)", this.indexIntervalMilliseconds);
             Date startTime = new Date(System.currentTimeMillis() + this.indexIntervalMilliseconds);
             this.eventIndexerFuture = scheduler.scheduleWithFixedDelay(
                     new ThreadRenamingRunnable(new Runnable() {
                         @Override
                         public void run() {
                             try {
                                 final int numIndexed = eventIndexer.index();
                                 if (numIndexed > 0) {
                                     logger.debug("Indexed {} events", numIndexed);
                                 }
                             } catch (TransientDataAccessException e) {
                                 logger.debug("Transient failure indexing events", e);
                             } catch (Exception e) {
                                 logger.warn("Failed to index events", e);
                             }
                         }
                     }, "ZEP_EVENT_INDEXER"), startTime, this.indexIntervalMilliseconds);
         }
     }
 
     private void startEventSummaryAging() {
         final int duration = config.getEventAgeIntervalMinutes();
         final EventSeverity severity = config.getEventAgeDisableSeverity();
         if (oldConfig != null
                 && duration == oldConfig.getEventAgeIntervalMinutes()
                 && severity == oldConfig.getEventAgeDisableSeverity()) {
             logger.info("Event aging configuration not changed.");
             return;
         }
 
         cancelFuture(this.eventSummaryAger);
         this.eventSummaryAger = null;
 
         if (duration > 0) {
             logger.info("Starting event aging at interval: {} milliseconds(s)", this.agingIntervalMilliseconds);
             Date startTime = new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1));
             this.eventSummaryAger = scheduler.scheduleWithFixedDelay(
                     new ThreadRenamingRunnable(new Runnable() {
                         @Override
                         public void run() {
                             logger.info("Aging events");
                             try {
                                 final int numAged = eventStoreDao.ageEvents(duration, TimeUnit.MINUTES, severity,
                                         agingLimit);
                                 if (numAged > 0) {
                                     logger.debug("Aged {} events", numAged);
                                 }
                             } catch (Exception e) {
                                 logger.warn("Failed to age events", e);
                             }
                         }
                     }, "ZEP_EVENT_AGER"), startTime, this.agingIntervalMilliseconds);
         } else {
             logger.info("Event aging disabled");
         }
     }
 
     private void startEventSummaryArchiving() {
         final int duration = config.getEventArchiveIntervalMinutes();
         if (oldConfig != null && duration == oldConfig.getEventArchiveIntervalMinutes()) {
             logger.info("Event archiving configuration not changed.");
             return;
         }
 
         final Date startTime = new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1));
         cancelFuture(this.eventSummaryArchiver);
         this.eventSummaryArchiver = null;
         if (duration > 0) {
             logger.info("Starting event archiving at interval: {} milliseconds(s)", this.archiveIntervalMilliseconds);
             this.eventSummaryArchiver = scheduler.scheduleWithFixedDelay(
                     new ThreadRenamingRunnable(new Runnable() {
                         @Override
                         public void run() {
                             try {
                                 final int numArchived = eventStoreDao.archive(duration, TimeUnit.MINUTES, archiveLimit);
                                 if (numArchived > 0) {
                                     logger.debug("Archived {} events", numArchived);
                                 }
                             } catch (Exception e) {
                                 logger.warn("Failed to archive events", e);
                             }
                         }
                     }, "ZEP_EVENT_ARCHIVER"), startTime, this.archiveIntervalMilliseconds);
         } else {
             logger.info("Event archiving disabled");
         }
     }
 
     private ScheduledFuture<?> purge(final Purgable purgable,
                                      final int purgeDuration, final TimeUnit purgeUnit, long delayInMs,
                                      String threadName) {
         final Date startTime = new Date(System.currentTimeMillis() + 60000L);
         return scheduler.scheduleWithFixedDelay(
                 new ThreadRenamingRunnable(new Runnable() {
                     @Override
                     public void run() {
                         try {
                             purgable.purge(purgeDuration, purgeUnit);
                         } catch (Exception e) {
                             logger.warn("Failed purging", e);
                         }
                     }
                 }, threadName), startTime, delayInMs);
     }
 
     private void startEventArchivePurging() {
         final int duration = config.getEventArchivePurgeIntervalDays();
         if (oldConfig != null
                 && duration == oldConfig.getEventArchivePurgeIntervalDays()) {
             logger.info("Event archive purging configuration not changed.");
             return;
         }
         cancelFuture(this.eventArchivePurger);
         this.eventArchivePurger = purge(eventStoreDao, duration,
                 TimeUnit.DAYS,
                 eventArchiveDao.getPartitionIntervalInMs(),
                 "ZEP_EVENT_ARCHIVE_PURGER");
     }
 
     private void startHeartbeatProcessing() {
         cancelFuture(this.heartbeatFuture);
         Date startTime = new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1));
         this.heartbeatFuture = scheduler.scheduleWithFixedDelay(new ThreadRenamingRunnable(new Runnable() {
             @Override
             public void run() {
                 logger.debug("Processing heartbeats");
                 try {
                     heartbeatProcessor.sendHeartbeatEvents();
                 } catch (Exception e) {
                     logger.warn("Failed to process heartbeat events", e);
                 }
             }
         }, "ZEP_HEARTBEAT_PROCESSOR"), startTime, TimeUnit.SECONDS.toMillis(this.heartbeatIntervalSeconds));
     }
 
     private void startQueueListeners() throws ZepException {
         QueueConfig queueConfig;
         try {
             queueConfig = ZenossQueueConfig.getConfig();
         } catch (IOException e) {
             throw new ZepException(e.getLocalizedMessage(), e);
         }
         Collection<AbstractQueueListener> queueListeners =
                 applicationContext.getBeansOfType(AbstractQueueListener.class).values();
         for (AbstractQueueListener queueListener : queueListeners) {
             QueueConfiguration queue = queueConfig.getQueue(queueListener.getQueueIdentifier());
             this.queueListeners.add(this.amqpConnectionManager.addListener(queue, queueListener));
         }
     }
 
     private void stopQueueListeners() {
         for (String listenerId : this.queueListeners) {
             this.amqpConnectionManager.removeListener(listenerId);
         }
         this.queueListeners.clear();
     }
 
     @Override
     public synchronized void onApplicationEvent(ZepEvent event) {
         if (event instanceof ZepConfigUpdatedEvent) {
             ZepConfigUpdatedEvent configUpdatedEvent = (ZepConfigUpdatedEvent) event;
             this.config = configUpdatedEvent.getConfig();
             logger.info("Configuration changed: {}", this.config);
             startEventSummaryAging();
             startEventSummaryArchiving();
             startEventArchivePurging();
             this.oldConfig = config;
         }
         else if (event instanceof IndexDetailsUpdatedEvent) {
             try {
                 startEventIndexer();
             } catch (ZepException e) {
                 logger.warn("Failed to restart event indexing", e);
             }
         }
         else if (event instanceof PluginServiceStartedEvent) {
             try {
                 init();
             } catch (ZepException e) {
                 logger.error("Failed to initialize ZEP", e);
                 throw new RuntimeException(e.getLocalizedMessage(), e);
             }
         }
     }
 }
