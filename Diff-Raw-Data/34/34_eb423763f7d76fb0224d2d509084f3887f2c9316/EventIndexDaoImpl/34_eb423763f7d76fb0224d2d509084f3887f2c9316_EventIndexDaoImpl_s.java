 /*
  * This program is part of Zenoss Core, an open source monitoring platform.
  * Copyright (C) 2010, Zenoss Inc.
  * 
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License version 2 as published by
  * the Free Software Foundation.
  * 
  * For complete information please visit: http://www.zenoss.com/oss/
  */
 package org.zenoss.zep.index.impl;
 
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.FieldSelector;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.search.BooleanClause.Occur;
 import org.apache.lucene.search.BooleanQuery;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.MatchAllDocsQuery;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.ScoreDoc;
 import org.apache.lucene.search.Sort;
 import org.apache.lucene.search.SortField;
 import org.apache.lucene.search.TermQuery;
 import org.apache.lucene.search.TopDocs;
 import org.apache.lucene.search.TopFieldDocs;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.TaskScheduler;
 import org.zenoss.protobufs.zep.Zep.EventDetailItem;
 import org.zenoss.protobufs.zep.Zep.EventFilter;
 import org.zenoss.protobufs.zep.Zep.EventQuery;
 import org.zenoss.protobufs.zep.Zep.EventSeverity;
 import org.zenoss.protobufs.zep.Zep.EventSort;
 import org.zenoss.protobufs.zep.Zep.EventSort.Direction;
 import org.zenoss.protobufs.zep.Zep.EventSort.Field;
 import org.zenoss.protobufs.zep.Zep.EventStatus;
 import org.zenoss.protobufs.zep.Zep.EventSummary;
 import org.zenoss.protobufs.zep.Zep.EventSummaryRequest;
 import org.zenoss.protobufs.zep.Zep.EventSummaryResult;
 import org.zenoss.protobufs.zep.Zep.EventTagFilter;
 import org.zenoss.protobufs.zep.Zep.FilterOperator;
 import org.zenoss.zep.Messages;
 import org.zenoss.zep.ZepConstants;
 import org.zenoss.zep.ZepException;
 import org.zenoss.zep.ZepUtils;
 import org.zenoss.zep.impl.ThreadRenamingRunnable;
 import org.zenoss.zep.index.EventIndexDao;
 
 import java.io.Closeable;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.EnumMap;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 import static org.zenoss.zep.index.impl.IndexConstants.*;
 
 public class EventIndexDaoImpl implements EventIndexDao {
     private final IndexWriter writer;
     // Don't use searcher directly - use getSearcher()/returnSearcher()
     private IndexSearcher _searcher;
     private final String name;
 
     @Autowired
     private Messages messages;
 
     private int queryLimit = ZepConstants.DEFAULT_QUERY_LIMIT;
     
     @Autowired
     private TaskScheduler scheduler;
     private final Map<String,SavedSearch> savedSearches = new ConcurrentHashMap<String,SavedSearch>();
     protected Map<String,EventDetailItem> detailsConfig = Collections.emptyMap();
 
     private static final Logger logger = LoggerFactory.getLogger(EventIndexDaoImpl.class);
 
     public EventIndexDaoImpl(String name, IndexWriter writer) throws IOException {
         this.name = name;
         this.writer = writer;
         // Use NRT reader.
         this._searcher = new IndexSearcher(this.writer.getReader());
     }
 
     public synchronized void shutdown() throws IOException {
         for (Iterator<Map.Entry<String,SavedSearch>> it = savedSearches.entrySet().iterator(); it.hasNext(); ) {
             it.next().getValue().close();
             it.remove();
         }
         this._searcher.getIndexReader().close();
     }
 
     @Override
     public void setIndexDetails(Map<String, EventDetailItem> detailsConfig) {
         this.detailsConfig = detailsConfig;
     }
 
     /**
      * Sets the maximum number of results returned in a query from ZEP.
      *
      * @param limit Maximum number of results returned in a query from ZEP.
      */
     public void setQueryLimit(int limit) {
         if (limit > 0) {
             this.queryLimit = limit;
         }
         else {
             logger.warn("Invalid query limit: {}, using default: {}", limit, this.queryLimit);
         }
     }
 
     @Override
     public String getName() {
         return this.name;
     }
 
     @Override
     public int getNumDocs() throws ZepException {
         try {
             return this.writer.numDocs();
         } catch (IOException e) {
             throw new ZepException(e.getLocalizedMessage(), e);
         }
     }
 
     @Override
     public void index(EventSummary event) throws ZepException {
         stage(event);
         commit();
     }
 
     @Override
     public void stage(EventSummary event) throws ZepException {
         Document doc = EventIndexMapper.fromEventSummary(event, this.detailsConfig);
         logger.debug("Indexing {}", event.getUuid());
 
         try {
             this.writer.updateDocument(new Term(FIELD_UUID, event.getUuid()), doc);
         } catch (IOException e) {
             throw new ZepException(e);
         }
     }
 
     @Override
     public void stageDelete(String eventUuid) throws ZepException {
         try {
             writer.deleteDocuments(new Term(FIELD_UUID, eventUuid));
         } catch (IOException e) {
             throw new ZepException(e);
         }
     }
 
     @Override
     public void commit() throws ZepException {
         commit(false);
     }
 
     private synchronized void reopenReader() throws IOException {
         IndexReader oldReader = this._searcher.getIndexReader();
         IndexReader newReader = oldReader.reopen();
         if (oldReader != newReader) {
             this._searcher = new IndexSearcher(newReader);
             ZepUtils.close(oldReader);
         }
     }
 
     private synchronized IndexSearcher getSearcher() throws IOException {
         reopenReader();
         this._searcher.getIndexReader().incRef();
         return this._searcher;
     }
 
     private static void returnSearcher(IndexSearcher searcher) throws ZepException {
         if (searcher != null) {
             try {
                 searcher.getIndexReader().decRef();
             } catch (IOException e) {
                 throw new ZepException(e.getLocalizedMessage(), e);
             }
         }
     }
 
     @Override
     public void commit(boolean forceOptimize) throws ZepException {
         try {
             this.writer.commit();
             if (forceOptimize) {
                 this.writer.optimize();
             }
         } catch (IOException e) {
             throw new ZepException(e);
         }
     }
 
     @Override
     public void indexMany(List<EventSummary> events) throws ZepException {
         for ( EventSummary event : events ) {            
             stage(event);
         }
         commit();
     }
 
     @Override
     public void reindex() throws ZepException {
         IndexSearcher searcher = null;
         try {
             logger.info("Re-indexing all previously indexed events for: {}", this.name);
             searcher = getSearcher();
             final IndexReader reader = searcher.getIndexReader();
             int numDocs = reader.numDocs();
             int numReindexed = 0;
             for (int i = 0; i < numDocs; i++) {
                 if (!reader.isDeleted(i)) {
                     EventSummary summary = EventIndexMapper.toEventSummary(reader.document(i, PROTO_SELECTOR));
                     this.stage(summary);
                     ++numReindexed;
                 }
             }
             logger.info("Completed re-indexing {} events for: {}", numReindexed, this.name);
             this.commit(true);
         } catch (IOException e) {
             throw new ZepException(e.getLocalizedMessage(), e);
         } finally {
             returnSearcher(searcher);
         }
     }
 
     // Load the serialized protobuf (entire event)
     private static final FieldSelector PROTO_SELECTOR = new SingleFieldSelector(FIELD_PROTOBUF);
 
     // Load just the event UUID
     private static final FieldSelector UUID_SELECTOR = new SingleFieldSelector(FIELD_UUID);
     
     @Override
     public EventSummaryResult list(EventSummaryRequest request) throws ZepException {
         return listInternal(request, PROTO_SELECTOR);
     }
 
     @Override
     public EventSummaryResult listUuids(EventSummaryRequest request) throws ZepException {
         return listInternal(request, UUID_SELECTOR);
     }
 
     private EventSummaryResult listInternal(EventSummaryRequest request, FieldSelector selector) throws ZepException {
         IndexSearcher searcher = null;
         try {
             searcher = getSearcher();
             Query query = buildQuery(searcher.getIndexReader(), request.getEventFilter(), request.getExclusionFilter());
             Sort sort = buildSort(request.getSortList());
             return searchToEventSummaryResult(searcher, query, sort, selector, request.getOffset(), request.getLimit());
         } catch (IOException e) {
             throw new ZepException(e.getLocalizedMessage(), e);
         } finally {
             returnSearcher(searcher);
         }
     }
 
     private EventSummaryResult searchToEventSummaryResult(IndexSearcher searcher, Query query, Sort sort,
                                                           FieldSelector selector, int offset, int limit)
             throws IOException, ZepException {
         if (limit < 0) {
             throw new ZepException(messages.getMessage("invalid_query_limit", limit));
         }
         if (limit > queryLimit) {
             limit = queryLimit;
         }
         if ( offset < 0 ) {
             offset = 0;
         }
         logger.debug("Query: {}, Sort: {}, Offset: {}, Limit: {}", new Object[] { query, sort, offset, limit });
         final TopDocs docs;
         if (sort != null) {
            docs = searcher.search(query, null, limit + offset, sort);
         }
         else {
            docs = searcher.search(query, null, limit + offset);
         }
         logger.debug("Found {} results", docs.totalHits);
         EventSummaryResult.Builder result = EventSummaryResult.newBuilder();
         result.setTotal(docs.totalHits);
         result.setLimit(limit);
         if (docs.totalHits > limit + offset) {
             result.setNextOffset(limit + offset);
         }
 
        for (int i = offset; i < docs.scoreDocs.length; i++) {
             EventSummary summary = EventIndexMapper.toEventSummary(searcher.doc(docs.scoreDocs[i].doc, selector));
             result.addEvents(summary);
         }
         return result.build();
     }
 
     @Override
     public void delete(String uuid) throws ZepException {
         try {
             writer.deleteDocuments(new Term(FIELD_UUID, uuid));
             commit();
         } catch (IOException e) {
             throw new ZepException(e);
         }
     }
 
     @Override
     public void delete(List<String> uuids) throws ZepException {
         try {
             if (uuids.isEmpty()) {
                 return;
             }
             List<Term> terms = new ArrayList<Term>(uuids.size());
             for (String uuid : uuids) {
                 terms.add(new Term(FIELD_UUID, uuid));
             }
             writer.deleteDocuments(terms.toArray(new Term[terms.size()]));
             // Optimize only when we delete in batches as optimizing one at a time would be expensive
             commit(true);
         } catch (IOException e) {
             throw new ZepException(e);
         }
     }
 
     @Override
     public EventSummary findByUuid(String uuid) throws ZepException {
         TermQuery query = new TermQuery(new Term(FIELD_UUID, uuid));
 
         EventSummary summary = null;
         IndexSearcher searcher = null;
         try {
             searcher = getSearcher();
             TopDocs docs = searcher.search(query, 1);
             if (docs.scoreDocs.length > 0) {
                 summary = EventIndexMapper.toEventSummary(searcher.doc(docs.scoreDocs[0].doc));
             }
         } catch (IOException e) {
             throw new ZepException(e);
         } finally {
             returnSearcher(searcher);
         }
         return summary;
     }
 
     @Override
     public void clear() throws ZepException {
         logger.info("Deleting all events for: {}", this.name);
 
         try {
             this.writer.deleteAll();
             commit(true);
         } catch (IOException e) {
             throw new ZepException(e);
         }
     }
 
     @Override
     public void delete(EventSummaryRequest request) throws ZepException {
         IndexSearcher searcher = null;
         try {
             searcher = getSearcher();
             Query query = buildQuery(searcher.getIndexReader(), request.getEventFilter(), request.getExclusionFilter());
             logger.debug("Deleting events matching: {}", query);
             this.writer.deleteDocuments(query);
             commit(true);
         } catch (IOException e) {
             throw new ZepException(e);
         } finally {
             returnSearcher(searcher);
         }
     }
 
     @Override
     public void purge(int duration, TimeUnit unit) throws ZepException {
         if (duration < 0) {
             throw new IllegalArgumentException("Duration must be >= 0");
         }
         final long pruneTimestamp = System.currentTimeMillis() - unit.toMillis(duration);
 
         QueryBuilder query = new QueryBuilder();
         query.addRange(FIELD_LAST_SEEN_TIME, null, pruneTimestamp);
 
         logger.info("Purging events older than {}", new Date(pruneTimestamp));
         try {
             this.writer.deleteDocuments(query.build());
             commit(true);
         } catch (IOException e) {
             throw new ZepException(e);
         }
     }
 
     private Sort buildSort(List<EventSort> sortList) {
         if (sortList.isEmpty()) {
             return null;
         }
         List<SortField> fields = new ArrayList<SortField>(sortList.size());
         for (EventSort sort : sortList) {
             fields.add(createSortField(sort));
         }
         return new Sort(fields.toArray(new SortField[fields.size()]));
     }
 
     private SortField createSortField(EventSort sort) {
         boolean reverse = (sort.getDirection() == Direction.DESCENDING);
         switch (sort.getField()) {
             case COUNT:
                 return new SortField(FIELD_COUNT, SortField.INT, reverse);
             case ELEMENT_IDENTIFIER:
                 return new SortField(FIELD_ELEMENT_IDENTIFIER_NOT_ANALYZED, SortField.STRING, reverse);
             case ELEMENT_SUB_IDENTIFIER:
                 return new SortField(FIELD_ELEMENT_SUB_IDENTIFIER_NOT_ANALYZED, SortField.STRING, reverse);
             case EVENT_CLASS:
                 return new SortField(FIELD_EVENT_CLASS_NOT_ANALYZED, SortField.STRING, reverse);
             case EVENT_SUMMARY:
                 return new SortField(FIELD_SUMMARY_NOT_ANALYZED, SortField.STRING, reverse);
             case FIRST_SEEN:
                 return new SortField(FIELD_FIRST_SEEN_TIME, SortField.LONG, reverse);
             case LAST_SEEN:
                 return new SortField(FIELD_LAST_SEEN_TIME, SortField.LONG, reverse);
             case SEVERITY:
                 return new SortField(FIELD_SEVERITY, SortField.INT, reverse);
             case STATUS:
                 return new SortField(FIELD_STATUS, SortField.INT, reverse);
             case STATUS_CHANGE:
                 return new SortField(FIELD_STATUS_CHANGE_TIME, SortField.LONG, reverse);
             case UPDATE_TIME:
                 return new SortField(FIELD_UPDATE_TIME, SortField.LONG, reverse);
             case ACKNOWLEDGED_BY_USER_NAME:
                 return new SortField(FIELD_ACKNOWLEDGED_BY_USER_NAME, SortField.STRING, reverse);
             case AGENT:
                 return new SortField(FIELD_AGENT, SortField.STRING, reverse);
             case MONITOR:
                 return new SortField(FIELD_MONITOR, SortField.STRING, reverse);
             case UUID:
                 return new SortField(FIELD_UUID, SortField.STRING, reverse);
             case DETAIL:
                 EventDetailItem item = this.detailsConfig.get(sort.getDetailKey());
                 if (item == null) {
                     throw new IllegalArgumentException("Unknown event detail: " + sort.getDetailKey());
                 }
                 final String fieldName = EventIndexMapper.DETAIL_INDEX_PREFIX + sort.getDetailKey();
                 final int sortFieldType;
                 switch (item.getType()) {
                     case DOUBLE:
                         sortFieldType = SortField.DOUBLE;
                         break;
                     case INTEGER:
                         sortFieldType = SortField.INT;
                         break;
                     case STRING:
                         sortFieldType = SortField.STRING;
                         break;
                     case FLOAT:
                         sortFieldType = SortField.FLOAT;
                         break;
                     case LONG:
                         sortFieldType = SortField.LONG;
                         break;
                     default:
                         throw new IllegalArgumentException("Unsupported detail type: " + item.getType());
                 }
                 return new SortField(fieldName, sortFieldType, reverse);
         }
         throw new IllegalArgumentException("Unsupported sort field: " + sort.getField());
     }
 
     private Query buildQuery(IndexReader reader, EventFilter filter, EventFilter exclusionFilter) throws ZepException {
         final BooleanQuery filterQuery = buildQueryFromFilter(reader, filter);
         final BooleanQuery exclusionQuery = buildQueryFromFilter(reader, exclusionFilter);
         final Query query;
 
         if (filterQuery == null && exclusionQuery == null) {
             query = new MatchAllDocsQuery();
         }
         else if (filterQuery != null) {
             if (exclusionQuery != null) {
                 filterQuery.add(exclusionQuery, Occur.MUST_NOT);
             }
             query = filterQuery;
         }
         else {
             BooleanQuery bq = new BooleanQuery();
             bq.add(exclusionQuery, Occur.MUST_NOT);
             bq.add(new MatchAllDocsQuery(), Occur.MUST);
             query = bq;
         }
         logger.debug("Filter: {}, Exclusion filter: {}, Query: {}", new Object[] { filter, exclusionFilter, query });
         
         return query;
     }
 
     private BooleanQuery buildQueryFromFilter(IndexReader reader, EventFilter filter) throws ZepException {
         if (filter == null) {
             return null;
         }
         
         QueryBuilder qb = new QueryBuilder(filter.getOperator());
 
         qb.addRanges(FIELD_COUNT, filter.getCountRangeList());
         qb.addWildcardFields(FIELD_ACKNOWLEDGED_BY_USER_NAME, filter.getAcknowledgedByUserNameList(), false);
         qb.addIdentifierFields(FIELD_ELEMENT_IDENTIFIER, FIELD_ELEMENT_IDENTIFIER_NOT_ANALYZED,
                 filter.getElementIdentifierList(), this.writer.getAnalyzer());
         qb.addIdentifierFields(FIELD_ELEMENT_SUB_IDENTIFIER, FIELD_ELEMENT_SUB_IDENTIFIER_NOT_ANALYZED,
                 filter.getElementSubIdentifierList(), this.writer.getAnalyzer());
         qb.addField(FIELD_FINGERPRINT, filter.getFingerprintList());
         qb.addFullTextFields(FIELD_SUMMARY, filter.getEventSummaryList(), reader, this.writer.getAnalyzer());
         qb.addTimestampRanges(FIELD_FIRST_SEEN_TIME, filter.getFirstSeenList());
         qb.addTimestampRanges(FIELD_LAST_SEEN_TIME, filter.getLastSeenList());
         qb.addTimestampRanges(FIELD_STATUS_CHANGE_TIME, filter.getStatusChangeList());
         qb.addTimestampRanges(FIELD_UPDATE_TIME, filter.getUpdateTimeList());
         qb.addFieldOfEnumNumbers(FIELD_STATUS, filter.getStatusList());
         qb.addFieldOfEnumNumbers(FIELD_SEVERITY, filter.getSeverityList());
         qb.addWildcardFields(FIELD_AGENT, filter.getAgentList(), false);
         qb.addWildcardFields(FIELD_MONITOR, filter.getMonitorList(), false);
 
         qb.addEventClassFields(FIELD_EVENT_CLASS, FIELD_EVENT_CLASS_NOT_ANALYZED, filter.getEventClassList(),
                 this.writer.getAnalyzer(), reader);
 
         for (EventTagFilter tagFilter : filter.getTagFilterList()) {
             qb.addField(FIELD_TAGS, tagFilter.getTagUuidsList(), tagFilter.getOp());
         }
 
         qb.addField(FIELD_UUID, filter.getUuidList(), FilterOperator.OR);
 
         qb.addDetails(filter.getDetailsList(), this.detailsConfig);
         
         for (EventFilter subfilter : filter.getSubfilterList()) {
             qb.addSubquery(buildQueryFromFilter(reader, subfilter));
         }
 
         return qb.build();
     }
 
     private static final FieldSelector SEVERITY_SELECTOR = new SingleFieldSelector(FIELD_SEVERITY);
     
     private Map<EventSeverity,Integer> countSeveritiesForTag(String tag) throws ZepException {
         QueryBuilder builder = new QueryBuilder();
         builder.addField(IndexConstants.FIELD_TAGS, tag);
         List<EventStatus> status = Arrays.asList(EventStatus.STATUS_NEW, EventStatus.STATUS_ACKNOWLEDGED);
         builder.addFieldOfEnumNumbers(IndexConstants.FIELD_STATUS, status);
         final Query query = builder.build();
 
         IndexSearcher searcher = null;
         try {
             searcher = getSearcher();
             Map<EventSeverity, Integer> severities = null;
             int maxDoc = searcher.maxDoc();
             if (maxDoc > 0) {
                 TopDocs docs = searcher.search(query, maxDoc);
                 if (docs.scoreDocs.length > 0) {
                     severities = new EnumMap<EventSeverity,Integer>(EventSeverity.class);
                     for (ScoreDoc scoreDoc : docs.scoreDocs) {
                         Document doc = searcher.doc(scoreDoc.doc, SEVERITY_SELECTOR);
                         EventSeverity severity = EventSeverity.valueOf(Integer.valueOf(doc.get(FIELD_SEVERITY)));
                         Integer count = severities.get(severity);
                         if (count == null) {
                             count = 1;
                         } else {
                             ++count;
                         }
                         severities.put(severity, count);
                     }
                 }
             }
             return severities;
         } catch (IOException e) {
             throw new ZepException(e.getLocalizedMessage(), e);
         } finally {
             returnSearcher(searcher);
         }
     }
 
     @Override
     public Map<String,Map<EventSeverity,Integer>> countSeverities(Set<String> tags)
             throws ZepException {
         Map<String,Map<EventSeverity,Integer>> severities = new HashMap<String, Map<EventSeverity,Integer>>(tags.size());
         for (String tag : tags) {
             Map<EventSeverity,Integer> tagSeverities = countSeveritiesForTag(tag);
             if (tagSeverities != null) {
                 severities.put(tag, tagSeverities);
             }
         }
         return severities;
     }
     
     private EventSeverity findWorstSeverity(String tag) throws ZepException {
         QueryBuilder builder = new QueryBuilder();
         builder.addField(IndexConstants.FIELD_TAGS, tag);
         List<EventStatus> status = Arrays.asList(EventStatus.STATUS_NEW, EventStatus.STATUS_ACKNOWLEDGED);
         builder.addFieldOfEnumNumbers(IndexConstants.FIELD_STATUS, status);
         final Query query = builder.build();
 
         // Sort by worst severity
         EventSort.Builder eventSortBuilder = EventSort.newBuilder();
         eventSortBuilder.setDirection(Direction.DESCENDING);
         eventSortBuilder.setField(Field.SEVERITY);
         final EventSort eventSort = eventSortBuilder.build();
         final Sort sort = buildSort(Collections.singletonList(eventSort));
 
         IndexSearcher searcher = null;
 
         try {
             searcher = getSearcher();
             TopFieldDocs docs = searcher.search(query, null, 1, sort);
             EventSeverity severity = null;
             if (docs.scoreDocs.length > 0) {
                 Document doc = searcher.doc(docs.scoreDocs[0].doc, SEVERITY_SELECTOR);
                 severity = EventSeverity.valueOf(Integer.valueOf(doc.get(FIELD_SEVERITY)));
             }
             return severity;
         } catch (IOException e) {
             throw new ZepException(e.getLocalizedMessage(), e);
         } finally {
             returnSearcher(searcher);
         }
     }
 
     @Override
     public Map<String, EventSeverity> findWorstSeverity(Set<String> tags)
             throws ZepException {
         Map<String,EventSeverity> severities = new LinkedHashMap<String, EventSeverity>(tags.size());
         for (String tag : tags) {
             EventSeverity worstSeverity = findWorstSeverity(tag);
             if (worstSeverity != null) {
                 severities.put(tag, worstSeverity);
             }
         }
         return severities;
     }
 
     private static class SavedSearch implements Closeable {
         private final String uuid;
         private IndexReader reader;
         private final Query query;
         private final Sort sort;
         private final int timeout;
         private ScheduledFuture<?> timeoutFuture;
 
         public SavedSearch(String uuid, IndexReader reader, Query query, Sort sort, int timeout) {
             this.uuid = uuid;
             this.reader = reader;
             this.query = query;
             this.sort = sort;
             this.timeout = timeout;
         }
 
         public String getUuid() {
             return uuid;
         }
 
         public IndexReader getReader() {
             return this.reader;
         }
 
         public Query getQuery() {
             return this.query;
         }
 
         public Sort getSort() {
             return sort;
         }
 
         public int getTimeout() {
             return timeout;
         }
 
         public synchronized ScheduledFuture<?> getTimeoutFuture() {
             return this.timeoutFuture;
         }
 
         public synchronized void setTimeoutFuture(ScheduledFuture<?> timeoutFuture) throws ZepException {
             if (this.timeoutFuture != null) {
                 this.timeoutFuture.cancel(false);
             }
             this.timeoutFuture = timeoutFuture;
         }
 
         public synchronized void close() throws IOException {
             if (this.reader != null) {
                 this.reader.decRef();
                 this.reader = null;
             }
         }
 
         @Override
         public String toString() {
             return "SavedSearch{" +
                     "uuid='" + uuid + '\'' +
                     ", reader=" + reader +
                     ", query=" + query +
                     ", sort=" + sort +
                     ", timeout=" + timeout +
                     ", timeoutFuture=" + timeoutFuture +
                     '}';
         }
     }
 
     @Override
     public String createSavedSearch(EventQuery eventQuery) throws ZepException {
         if (eventQuery.getTimeout() < 1) {
             throw new ZepException("Invalid timeout: " + eventQuery.getTimeout());
         }
         final String uuid = UUID.randomUUID().toString();
         IndexReader reader = null;
         try {
             reader = getSearcher().getIndexReader();
             final Query query = buildQuery(reader, eventQuery.getEventFilter(), eventQuery.getExclusionFilter());
             final Sort sort = buildSort(eventQuery.getSortList());
             final SavedSearch search = new SavedSearch(uuid, reader, query, sort, eventQuery.getTimeout());
             savedSearches.put(uuid, search);
             scheduleSearchTimeout(search);
         } catch (Exception e) {
             logger.warn("Exception creating saved search", e);
             if (reader != null) {
                 try {
                     reader.decRef();
                 } catch (IOException ex) {
                     logger.warn("Exception decrementing reference count", ex);
                 }
             }
             if (e instanceof ZepException) {
                 throw (ZepException) e;
             }
             throw new ZepException(e.getLocalizedMessage(), e);
         }
         return uuid;
     }
 
     private void scheduleSearchTimeout(final SavedSearch search) throws ZepException {
         logger.info("Scheduling saved search {} for expiration in {} seconds", search.getUuid(), search.getTimeout());
         Date d = new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(search.getTimeout()));
         search.setTimeoutFuture(scheduler.schedule(new ThreadRenamingRunnable(new Runnable() {
             @Override
             public void run() {
                 logger.info("Saved search timed out: {}", search.getUuid());
                 savedSearches.remove(search.getUuid());
                 try {
                     search.close();
                 } catch (IOException e) {
                     logger.warn("Failed closing saved search", e);
                 }
             }
         }, "ZEP_SAVED_SEARCH_TIMEOUT"), d));
     }
 
     @Override
     public EventSummaryResult savedSearch(String uuid, int offset, int limit) throws ZepException {
         return savedSearchInternal(uuid, offset, limit, PROTO_SELECTOR);
     }
 
     @Override
     public EventSummaryResult savedSearchUuids(String uuid, int offset, int limit) throws ZepException {
         return savedSearchInternal(uuid, offset, limit, UUID_SELECTOR);
     }
 
     private EventSummaryResult savedSearchInternal(String uuid, int offset, int limit, FieldSelector selector)
             throws ZepException {
         final SavedSearch search = savedSearches.get(uuid);
         if (search == null) {
             throw new ZepException(messages.getMessage("saved_search_not_found", uuid));
         }
 
         /* Reset the timeout for the saved search to prevent it expiring while in use */
         scheduleSearchTimeout(search);
 
         IndexSearcher searcher = null;
         try {
             IndexReader reader = search.getReader();
             reader.incRef();
             
             searcher = new IndexSearcher(reader);
             return searchToEventSummaryResult(searcher, search.getQuery(), search.getSort(), selector, offset, limit);
         } catch (IOException e) {
             throw new ZepException(e.getLocalizedMessage(), e);
         } finally {
             returnSearcher(searcher);
         }
     }
 
     @Override
     public String deleteSavedSearch(String uuid) throws ZepException {
         final SavedSearch search = savedSearches.remove(uuid);
         if (search == null) {
             return null;
         }
         logger.info("Deleting saved search: {}", uuid);
         try {
             search.close();
         } catch (IOException e) {
             logger.warn("Failed closing reader", e);
         }
         search.getTimeoutFuture().cancel(false);
         return search.getUuid();
     }
 }
