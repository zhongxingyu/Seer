 /*
  * Copyright (C) 2010, Zenoss Inc.  All Rights Reserved.
  */
 package org.zenoss.zep.index.impl;
 
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.FieldSelector;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.search.BooleanClause.Occur;
 import org.apache.lucene.search.BooleanQuery;
 import org.apache.lucene.search.Collector;
 import org.apache.lucene.search.DocIdSetIterator;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.MatchAllDocsQuery;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.Scorer;
 import org.apache.lucene.search.Sort;
 import org.apache.lucene.search.SortField;
 import org.apache.lucene.search.TermQuery;
 import org.apache.lucene.search.TopDocs;
 import org.apache.lucene.util.OpenBitSet;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.TaskScheduler;
 import org.zenoss.protobufs.zep.Zep.Event;
 import org.zenoss.protobufs.zep.Zep.EventActor;
 import org.zenoss.protobufs.zep.Zep.EventDetailItem;
 import org.zenoss.protobufs.zep.Zep.EventFilter;
 import org.zenoss.protobufs.zep.Zep.EventQuery;
 import org.zenoss.protobufs.zep.Zep.EventSeverity;
 import org.zenoss.protobufs.zep.Zep.EventSort;
 import org.zenoss.protobufs.zep.Zep.EventSort.Direction;
 import org.zenoss.protobufs.zep.Zep.EventStatus;
 import org.zenoss.protobufs.zep.Zep.EventSummary;
 import org.zenoss.protobufs.zep.Zep.EventSummaryRequest;
 import org.zenoss.protobufs.zep.Zep.EventSummaryResult;
 import org.zenoss.protobufs.zep.Zep.EventTag;
 import org.zenoss.protobufs.zep.Zep.EventTagFilter;
 import org.zenoss.protobufs.zep.Zep.EventTagSeverities;
 import org.zenoss.protobufs.zep.Zep.EventTagSeveritiesSet;
 import org.zenoss.protobufs.zep.Zep.EventTagSeverity;
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
 import java.util.List;
 import java.util.Map;
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
 
         // Lucene doesn't like querying for 0 documents - search for at least one here
         final int numDocs = Math.max(limit + offset, 1);
         if (sort != null) {
             docs = searcher.search(query, null, numDocs, sort);
         }
         else {
             docs = searcher.search(query, null, numDocs);
         }
         logger.debug("Found {} results", docs.totalHits);
         EventSummaryResult.Builder result = EventSummaryResult.newBuilder();
         result.setTotal(docs.totalHits);
         result.setLimit(limit);
         if (docs.totalHits > limit + offset) {
             result.setNextOffset(limit + offset);
         }
 
         // Return the number of results they asked for (the query has to return at least one match
         // but the request may specified a limit of zero).
         final int lastDocument = Math.min(limit + offset, docs.scoreDocs.length);
         for (int i = offset; i < lastDocument; i++) {
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
             fields.addAll(createSortField(sort));
         }
         return new Sort(fields.toArray(new SortField[fields.size()]));
     }
 
     private List<SortField> createSortField(EventSort sort) {
         final List<SortField> sortFields = new ArrayList<SortField>(2);
         boolean reverse = (sort.getDirection() == Direction.DESCENDING);
         switch (sort.getField()) {
             case COUNT:
                 sortFields.add(new SortField(FIELD_COUNT, SortField.INT, reverse));
                 break;
             case ELEMENT_IDENTIFIER:
                 sortFields.add(new SortField(FIELD_ELEMENT_IDENTIFIER_NOT_ANALYZED, SortField.STRING, reverse));
                 break;
             case ELEMENT_SUB_IDENTIFIER:
                 sortFields.add(new SortField(FIELD_ELEMENT_SUB_IDENTIFIER_NOT_ANALYZED, SortField.STRING, reverse));
                 break;
             case EVENT_CLASS:
                 sortFields.add(new SortField(FIELD_EVENT_CLASS_NOT_ANALYZED, SortField.STRING, reverse));
                 break;
             case EVENT_SUMMARY:
                 sortFields.add(new SortField(FIELD_SUMMARY_NOT_ANALYZED, SortField.STRING, reverse));
                 break;
             case FIRST_SEEN:
                 sortFields.add(new SortField(FIELD_FIRST_SEEN_TIME, SortField.LONG, reverse));
                 break;
             case LAST_SEEN:
                 sortFields.add(new SortField(FIELD_LAST_SEEN_TIME, SortField.LONG, reverse));
                 break;
             case SEVERITY:
                 sortFields.add(new SortField(FIELD_SEVERITY, SortField.INT, reverse));
                 break;
             case STATUS:
                 sortFields.add(new SortField(FIELD_STATUS, SortField.INT, reverse));
                 break;
             case STATUS_CHANGE:
                 sortFields.add(new SortField(FIELD_STATUS_CHANGE_TIME, SortField.LONG, reverse));
                 break;
             case UPDATE_TIME:
                 sortFields.add(new SortField(FIELD_UPDATE_TIME, SortField.LONG, reverse));
                 break;
             case CURRENT_USER_NAME:
                 sortFields.add(new SortField(FIELD_CURRENT_USER_NAME, SortField.STRING, reverse));
                 break;
             case AGENT:
                 sortFields.add(new SortField(FIELD_AGENT, SortField.STRING, reverse));
                 break;
             case MONITOR:
                 sortFields.add(new SortField(FIELD_MONITOR, SortField.STRING, reverse));
                 break;
             case UUID:
                 sortFields.add(new SortField(FIELD_UUID, SortField.STRING, reverse));
                 break;
            case FINGERPRINT:
                sortFields.add(new SortField(FIELD_FINGERPRINT, SortField.STRING, reverse));
                break;
             case DETAIL:
                 EventDetailItem item = this.detailsConfig.get(sort.getDetailKey());
                 if (item == null) {
                     throw new IllegalArgumentException("Unknown event detail: " + sort.getDetailKey());
                 }
                 final String fieldName = EventIndexMapper.DETAIL_INDEX_PREFIX + sort.getDetailKey();
                 switch (item.getType()) {
                     case DOUBLE:
                         sortFields.add(new SortField(fieldName, SortField.DOUBLE, reverse));
                         break;
                     case INTEGER:
                         sortFields.add(new SortField(fieldName, SortField.INT, reverse));
                         break;
                     case STRING:
                         sortFields.add(new SortField(fieldName, SortField.STRING, reverse));
                         break;
                     case FLOAT:
                         sortFields.add(new SortField(fieldName, SortField.FLOAT, reverse));
                         break;
                     case LONG:
                         sortFields.add(new SortField(fieldName, SortField.LONG, reverse));
                         break;
                     case IP_ADDRESS:
                         // Sort IPv4 before IPv6
                         sortFields.add(new SortField(fieldName + IP_ADDRESS_TYPE_SUFFIX, SortField.STRING, reverse));
                         sortFields.add(new SortField(fieldName + IP_ADDRESS_SORT_SUFFIX, SortField.STRING, reverse));
                         break;
                     default:
                         throw new IllegalArgumentException("Unsupported detail type: " + item.getType());
                 }
                 break;
         }
         if (sortFields.isEmpty()) {
             throw new IllegalArgumentException("Unsupported sort field: " + sort.getField());
         }
         return sortFields;
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
         qb.addWildcardFields(FIELD_CURRENT_USER_NAME, filter.getCurrentUserNameList(), false);
 
 
         qb.addIdentifierFields(FIELD_ELEMENT_IDENTIFIER, FIELD_ELEMENT_IDENTIFIER_NOT_ANALYZED,
                 filter.getElementIdentifierList(), this.writer.getAnalyzer());
 
         qb.addIdentifierFields(FIELD_ELEMENT_SUB_IDENTIFIER, FIELD_ELEMENT_SUB_IDENTIFIER_NOT_ANALYZED,
                 filter.getElementSubIdentifierList(), this.writer.getAnalyzer());
        qb.addWildcardFields(FIELD_FINGERPRINT, filter.getFingerprintList(), false);
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
 
        qb.addWildcardFields(FIELD_UUID, filter.getUuidList(), true);
 
         qb.addDetails(filter.getDetailsList(), this.detailsConfig, reader);
         
         for (EventFilter subfilter : filter.getSubfilterList()) {
             qb.addSubquery(buildQueryFromFilter(reader, subfilter));
         }
 
         return qb.build();
     }
     
     private static class TagSeveritiesCount {
         private int count = 0;
         private int acknowledged_count = 0;
     }
 
     private static class TagSeverities {
         final String tagUuid;
         private final Map<EventSeverity, TagSeveritiesCount> severityCount =
                 new EnumMap<EventSeverity,TagSeveritiesCount>(EventSeverity.class);
         private int total = 0;
 
         public TagSeverities(String tagUuid) {
             this.tagUuid = tagUuid;
         }
 
         public void updateSeverityCount(final EventSeverity severity, final int count, boolean isAcknowledged) {
             this.total += count;
             TagSeveritiesCount severitiesCount = severityCount.get(severity);
             if (severitiesCount == null) {
                 severitiesCount = new TagSeveritiesCount();
                 this.severityCount.put(severity, severitiesCount);
             }
             ++severitiesCount.count;
             if (isAcknowledged) {
                 ++severitiesCount.acknowledged_count;
             }
         }
 
         public EventTagSeverities toEventTagSeverities() {
             EventTagSeverities.Builder builder = EventTagSeverities.newBuilder();
             builder.setTagUuid(tagUuid);
             for (Map.Entry<EventSeverity, TagSeveritiesCount> entry : severityCount.entrySet()) {
                 TagSeveritiesCount severitiesCount = entry.getValue();
                 builder.addSeverities(EventTagSeverity.newBuilder().setSeverity(entry.getKey())
                         .setCount(severitiesCount.count).setAcknowledgedCount(severitiesCount.acknowledged_count).build());
             }
             builder.setTotal(this.total);
             return builder.build();
         }
     }
 
     private EventTagSeveritiesSet tagSeveritiesMapToSet(Map<String, TagSeverities> tagSeveritiesMap) {
         EventTagSeveritiesSet.Builder builder = EventTagSeveritiesSet.newBuilder();
         for (TagSeverities tagSeverities : tagSeveritiesMap.values()) {
             builder.addSeverities(tagSeverities.toEventTagSeverities());
         }
         return builder.build();
     }
 
     @Override
     public EventTagSeveritiesSet getEventTagSeverities(EventFilter filter)
             throws ZepException {
         final Map<String, TagSeverities> tagSeveritiesMap = new HashMap<String, TagSeverities>();
         final boolean hasTagsFilter = filter.getTagFilterCount() > 0;
         for (EventTagFilter eventTagFilter: filter.getTagFilterList()) {
             for (String eventTagUuid : eventTagFilter.getTagUuidsList()) {
                 tagSeveritiesMap.put(eventTagUuid, new TagSeverities(eventTagUuid));
             }
         }
         IndexSearcher searcher = null;
         try {
             searcher = getSearcher();
             final Query query = buildQueryFromFilter(searcher.getIndexReader(), filter);
             final OpenBitSet docs = new OpenBitSet(searcher.maxDoc());
             searcher.search(query, new Collector() {
                 private int docBase;
 
                 @Override
                 public void setScorer(Scorer scorer) throws IOException {
                 }
 
                 @Override
                 public void collect(int doc) throws IOException {
                     docs.set(docBase + doc);
                 }
 
                 @Override
                 public void setNextReader(IndexReader reader, int docBase) throws IOException {
                     this.docBase = docBase;
                 }
 
                 @Override
                 public boolean acceptsDocsOutOfOrder() {
                     return true;
                 }
             });
             int docId;
             final DocIdSetIterator it = docs.iterator();
             while ((docId = it.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
                 final Document doc = searcher.doc(docId, PROTO_SELECTOR);
                 final EventSummary summary = EventIndexMapper.toEventSummary(doc);
                 final boolean isAcknowledged = (summary.getStatus() == EventStatus.STATUS_ACKNOWLEDGED);
                 final Event occurrence = summary.getOccurrence(0);
                 final EventActor actor = occurrence.getActor();
                 // Build tags from element_uuids - no tags specified in filter
                 if (!hasTagsFilter) {
                     if (actor.hasElementUuid()) {
                         TagSeverities tagSeverities = tagSeveritiesMap.get(actor.getElementUuid());
                         if (tagSeverities == null) {
                             tagSeverities = new TagSeverities(actor.getElementUuid());
                             tagSeveritiesMap.put(tagSeverities.tagUuid, tagSeverities);
                         }
                         tagSeverities.updateSeverityCount(occurrence.getSeverity(), summary.getCount(), isAcknowledged);
                     }
                 }
                 // Build tag severities from passed in filter
                 else {
                     List<String> uuids = Arrays.asList(actor.getElementUuid(), actor.getElementSubUuid());
                     for (String uuid : uuids) {
                         TagSeverities tagSeverities = tagSeveritiesMap.get(uuid);
                         if (tagSeverities != null) {
                             tagSeverities.updateSeverityCount(occurrence.getSeverity(), summary.getCount(),
                                     isAcknowledged);
                         }
                     }
                     for (EventTag tag : occurrence.getTagsList()) {
                         for (String tagUuid : tag.getUuidList()) {
                             TagSeverities tagSeverities = tagSeveritiesMap.get(tagUuid);
                             if (tagSeverities != null) {
                                 tagSeverities.updateSeverityCount(occurrence.getSeverity(), summary.getCount(),
                                         isAcknowledged);
                             }
                         }
                     }
                 }
             }
             return tagSeveritiesMapToSet(tagSeveritiesMap);
         } catch (IOException e) {
             throw new ZepException(e.getLocalizedMessage(), e);
         } finally {
             returnSearcher(searcher);
         }
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
         logger.debug("Scheduling saved search {} for expiration in {} seconds", search.getUuid(), search.getTimeout());
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
         logger.debug("Deleting saved search: {}", uuid);
         try {
             search.close();
         } catch (IOException e) {
             logger.warn("Failed closing reader", e);
         }
         search.getTimeoutFuture().cancel(false);
         return search.getUuid();
     }
 }
