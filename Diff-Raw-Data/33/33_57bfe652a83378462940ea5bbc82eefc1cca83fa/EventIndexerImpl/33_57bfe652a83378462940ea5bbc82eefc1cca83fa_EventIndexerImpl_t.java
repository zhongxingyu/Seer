 /*
  * Copyright (C) 2010, Zenoss Inc.  All Rights Reserved.
  */
 
 package org.zenoss.zep.index.impl;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.zenoss.protobufs.zep.Zep.Event;
 import org.zenoss.protobufs.zep.Zep.EventDetail;
 import org.zenoss.protobufs.zep.Zep.EventDetailItem;
 import org.zenoss.protobufs.zep.Zep.EventSummary;
 import org.zenoss.zep.PluginService;
 import org.zenoss.zep.ZepConstants;
 import org.zenoss.zep.ZepException;
 import org.zenoss.zep.annotations.TransactionalRollbackAllExceptions;
 import org.zenoss.zep.dao.EventArchiveDao;
 import org.zenoss.zep.dao.EventDetailsConfigDao;
 import org.zenoss.zep.dao.EventIndexHandler;
 import org.zenoss.zep.dao.EventIndexQueueDao;
 import org.zenoss.zep.dao.EventSummaryBaseDao;
 import org.zenoss.zep.dao.EventSummaryDao;
 import org.zenoss.zep.dao.IndexMetadata;
 import org.zenoss.zep.dao.IndexMetadataDao;
 import org.zenoss.zep.dao.impl.DaoUtils;
 import org.zenoss.zep.index.EventIndexDao;
 import org.zenoss.zep.index.EventIndexer;
 import org.zenoss.zep.plugins.EventPostIndexPlugin;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 public class EventIndexerImpl implements EventIndexer {
     private static final Logger logger = LoggerFactory.getLogger(EventIndexerImpl.class);
 
    private int indexLimit = 1000;
     
     private EventSummaryDao eventSummaryDao;
     private EventIndexDao eventSummaryIndexDao;
     private EventIndexQueueDao eventSummaryQueueDao;
     
     private EventArchiveDao eventArchiveDao;
     private EventIndexDao eventArchiveIndexDao;
     private EventIndexQueueDao eventArchiveQueueDao;
     
     private PluginService pluginService;
     private IndexMetadataDao indexMetadataDao;
     private EventDetailsConfigDao eventDetailsConfigDao;
     private byte[] indexVersionHash;
 
     public void setEventSummaryDao(EventSummaryDao eventSummaryDao) {
         this.eventSummaryDao = eventSummaryDao;
     }
     
     public void setEventSummaryIndexDao(EventIndexDao eventSummaryIndexDao) {
         this.eventSummaryIndexDao = eventSummaryIndexDao;
     }
     
     public void setEventSummaryQueueDao(EventIndexQueueDao eventSummaryQueueDao) {
         this.eventSummaryQueueDao = eventSummaryQueueDao;
     }
     
     public void setEventArchiveDao(EventArchiveDao eventArchiveDao) {
         this.eventArchiveDao = eventArchiveDao;
     }
 
     public void setEventArchiveIndexDao(EventIndexDao eventIndexDao) {
         this.eventArchiveIndexDao = eventIndexDao;
     }
     
     public void setEventArchiveQueueDao(EventIndexQueueDao eventArchiveQueueDao) {
         this.eventArchiveQueueDao = eventArchiveQueueDao;
     }
     
     public void setPluginService(PluginService pluginService) {
         this.pluginService = pluginService;
     }
 
     public void setIndexMetadataDao(IndexMetadataDao indexMetadataDao) {
         this.indexMetadataDao = indexMetadataDao;
     }
 
     public void setEventDetailsConfigDao(EventDetailsConfigDao eventDetailsConfigDao) {
         this.eventDetailsConfigDao = eventDetailsConfigDao;
     }
 
    public void setIndexLimit(int indexLimit) {
        this.indexLimit = indexLimit;
    }

     private static byte[] calculateIndexVersionHash(Map<String,EventDetailItem> detailItems) throws ZepException {
         TreeMap<String,EventDetailItem> sorted = new TreeMap<String,EventDetailItem>(detailItems);
         StringBuilder indexConfigStr = new StringBuilder();
         for (EventDetailItem item : sorted.values()) {
             // Only key and type affect the indexing behavior - ignore changes to display name
             indexConfigStr.append('|');
             indexConfigStr.append(item.getKey());
             indexConfigStr.append('|');
             indexConfigStr.append(item.getType().name());
             indexConfigStr.append('|');
         }
         if (indexConfigStr.length() == 0) {
             return null;
         }
         return DaoUtils.sha1(indexConfigStr.toString());
     }
 
     @Override
     @TransactionalRollbackAllExceptions
     public synchronized void init() throws ZepException {
         Map<String,EventDetailItem> detailItems = this.eventDetailsConfigDao.getEventDetailItemsByName();
         for (EventDetailItem item : detailItems.values()) {
             logger.info("Indexed event detail: {}", item);
         }
         this.indexVersionHash = calculateIndexVersionHash(detailItems);
         this.eventSummaryIndexDao.setIndexDetails(detailItems);
         this.eventArchiveIndexDao.setIndexDetails(detailItems);
 
         recreateIndexIfNeeded(this.eventSummaryDao, this.eventSummaryIndexDao);
         recreateIndexIfNeeded(this.eventArchiveDao, this.eventArchiveIndexDao);
     }
 
     private void recreateIndexIfNeeded(final EventSummaryBaseDao baseDao, final EventIndexDao indexDao)
             throws ZepException {
         final IndexMetadata indexMetadata = this.indexMetadataDao.findIndexMetadata(indexDao.getName());
         final int numDocs = indexDao.getNumDocs();
         
         // Rebuild index if we detect that we have never indexed before.
         if (indexMetadata == null || numDocs == 0) {
             if (numDocs > 0) {
                 logger.info("Inconsistent state between index and database. Clearing index.");
                 indexDao.clear();
             }
             /* Recreate the index */
             recreateIndexFromDatabase(baseDao, indexDao);
         }
         // Rebuild index if the version changed.
         else if (indexVersionChanged(indexMetadata)) {
             try {
                 indexDao.reindex(indexMetadata.getIndexVersion());
                 this.indexMetadataDao.updateIndexVersion(indexDao.getName(), IndexConstants.INDEX_VERSION,
                     this.indexVersionHash);
             } catch (Exception e) {
                 logger.info("Failed to reindex old index. Clearing and reindexing events", e);
                 indexDao.clear();
                 recreateIndexFromDatabase(baseDao, indexDao);
             }
         }
     }
 
     private boolean indexVersionChanged(IndexMetadata indexMetadata) {
         boolean changed = false;
         if (IndexConstants.INDEX_VERSION != indexMetadata.getIndexVersion()) {
             logger.info("Index version changed: previous={}, new={}", indexMetadata.getIndexVersion(),
                     IndexConstants.INDEX_VERSION);
             changed = true;
         }
         else if (!Arrays.equals(this.indexVersionHash, indexMetadata.getIndexVersionHash())) {
             logger.info("Index configuration changed.");
             changed = true;
         }
         return changed;
     }
 
     private void recreateIndexFromDatabase(final EventSummaryBaseDao baseDao, final EventIndexDao indexDao) throws ZepException {
         logger.info("Recreating index for table {}", indexDao.getName());
         String startingUuid = null;
         final long throughTime = System.currentTimeMillis();
         int numIndexed = 0;
         List<EventSummary> events;
         
         do {
            events = baseDao.listBatch(startingUuid, throughTime, this.indexLimit);
             for (EventSummary event : events) {
                 indexDao.stage(event);
                 startingUuid = event.getUuid();
                 ++numIndexed;
             }
         } while (!events.isEmpty());
         
         if (numIndexed > 0) {
             logger.info("Committing changes to index on table: {}", indexDao.getName());
             indexDao.commit(true);
         }
         this.indexMetadataDao.updateIndexVersion(indexDao.getName(), IndexConstants.INDEX_VERSION,
                 this.indexVersionHash);
         logger.info("Finished recreating index for {} events on table: {}", numIndexed, indexDao.getName());
     }
     
     @Override
     public synchronized int index() throws ZepException {
         int totalIndexed = doIndex(eventSummaryQueueDao, eventSummaryIndexDao, -1L);
         totalIndexed += doIndex(eventArchiveQueueDao, eventArchiveIndexDao, -1L);
         return totalIndexed;
     }
 
     @Override
     public synchronized int indexFully() throws ZepException {
         int totalIndexed = 0;
         final long now = System.currentTimeMillis();
         int numIndexed;
         do {
             numIndexed = doIndex(eventSummaryQueueDao, eventSummaryIndexDao, now);
             numIndexed += doIndex(eventArchiveQueueDao, eventArchiveIndexDao, now);
             totalIndexed += numIndexed;
         } while (numIndexed > 0);
         
         return totalIndexed;
     }
 
     /**
      * When migrating events from Zenoss 3.1.x to ZEP, a detail is added to the event which is set to the initial
      * update_time value for the event. Since these events have already been processed by an earlier release of
      * Zenoss, we don't want to run post-processing on these events including triggers / fan-out.
      *
      * @param event The event.
      * @return True if post-processing should run on the event, false otherwise.
      */
     private static boolean shouldRunPostprocessing(EventSummary event) {
         boolean shouldRun = true;
         final Event occurrence = event.getOccurrence(0);
         final List<EventDetail> details = occurrence.getDetailsList();
         for (EventDetail detail : details) {
             if (ZepConstants.DETAIL_MIGRATE_UPDATE_TIME.equals(detail.getName())) {
                 final List<String> values = detail.getValueList();
                 if (values.size() == 1) {
                     try {
                         long migrate_update_time = Long.valueOf(values.get(0));
                         shouldRun = (migrate_update_time != event.getUpdateTime());
                     } catch (NumberFormatException nfe) {
                         logger.warn("Invalid value for detail {}: {}", detail.getName(), values);
                     }
                 }
                 break;
             }
         }
         return shouldRun;
     }
 
     private int doIndex(final EventIndexQueueDao queueDao, final EventIndexDao indexDao, long throughTime)
             throws ZepException {
         final List<EventPostIndexPlugin> plugins = this.pluginService.getPluginsByType(EventPostIndexPlugin.class);
         
         int numIndexed = queueDao.indexEvents(new EventIndexHandler() {
             @Override
             public void handle(EventSummary event) throws Exception {
                 indexDao.stage(event);
                 if (shouldRunPostprocessing(event)) {
                     for (EventPostIndexPlugin plugin : plugins) {
                         try {
                             plugin.processEvent(event);
                         } catch (Exception e) {
                             // Post-processing plug-in failures are not fatal errors.
                             logger.warn("Failed to run post-processing plug-in on event: " + event, e);
                         }
                     }
                 }
             }
 
             @Override
             public void handleDeleted(String uuid) throws Exception {
                 indexDao.stageDelete(uuid);
             }
             
             @Override
             public void handleComplete() throws Exception {
                 indexDao.commit();
             }
        }, this.indexLimit, throughTime);
         
         if (numIndexed > 0) {
             logger.debug("Completed indexing {} events on {}", numIndexed, indexDao.getName());            
         }
 
         return numIndexed;
     }
 }
