 /*
  * Copyright (C) 2010-2011, Zenoss Inc.  All Rights Reserved.
  */
 package org.zenoss.zep.dao.impl;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.dao.DuplicateKeyException;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
 import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
 import org.springframework.jdbc.support.MetaDataAccessException;
 import org.springframework.util.StringUtils;
 import org.zenoss.protobufs.JsonFormat;
 import org.zenoss.protobufs.model.Model.ModelElementType;
 import org.zenoss.protobufs.zep.Zep.Event;
 import org.zenoss.protobufs.zep.Zep.EventAuditLog;
 import org.zenoss.protobufs.zep.Zep.EventDetail;
 import org.zenoss.protobufs.zep.Zep.EventDetailSet;
 import org.zenoss.protobufs.zep.Zep.EventNote;
 import org.zenoss.protobufs.zep.Zep.EventSeverity;
 import org.zenoss.protobufs.zep.Zep.EventStatus;
 import org.zenoss.protobufs.zep.Zep.EventSummary;
 import org.zenoss.zep.UUIDGenerator;
 import org.zenoss.zep.ZepConstants;
 import org.zenoss.zep.ZepException;
 import org.zenoss.zep.annotations.TransactionalReadOnly;
 import org.zenoss.zep.annotations.TransactionalRollbackAllExceptions;
 import org.zenoss.zep.dao.EventSummaryDao;
 import org.zenoss.zep.plugins.EventPreCreateContext;
 
 import javax.sql.DataSource;
 import java.io.IOException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 import java.util.concurrent.TimeUnit;
 
 import static org.zenoss.zep.dao.impl.EventConstants.*;
 
 public class EventSummaryDaoImpl implements EventSummaryDao {
 
     private static final Logger logger = LoggerFactory.getLogger(EventSummaryDaoImpl.class);
 
     private final DataSource dataSource;
 
     private final SimpleJdbcTemplate template;
 
     private final SimpleJdbcInsert insert;
     
     private volatile List<String> archiveColumnNames;
 
     private EventDaoHelper eventDaoHelper;
 
     private UUIDGenerator uuidGenerator;
 
     public EventSummaryDaoImpl(DataSource dataSource) throws MetaDataAccessException {
         this.dataSource = dataSource;
         this.template = new SimpleJdbcTemplate(dataSource);
         this.insert = new SimpleJdbcInsert(dataSource).withTableName(TABLE_EVENT_SUMMARY);
     }
 
     public void setEventDaoHelper(EventDaoHelper eventDaoHelper) {
         this.eventDaoHelper = eventDaoHelper;
     }
 
     public void setUuidGenerator(UUIDGenerator uuidGenerator) {
         this.uuidGenerator = uuidGenerator;
     }
 
     @Override
     @TransactionalRollbackAllExceptions
     public String create(Event event, EventPreCreateContext context) throws ZepException {
         final Map<String, Object> occurrenceFields = eventDaoHelper.createOccurrenceFields(event);
         final Map<String, Object> fields = new HashMap<String,Object>(occurrenceFields);
         final long created = event.getCreatedTime();
         final long updateTime = System.currentTimeMillis();
         final int eventCount = 1;
         
         /*
          * Clear events are dropped if they don't clear any corresponding events.
          */
         final List<String> clearedEventUuids;
         if (event.getSeverity() == EventSeverity.SEVERITY_CLEAR) {
             clearedEventUuids = this.clearEvents(event, context);
             if (clearedEventUuids.isEmpty()) {
                 logger.debug("Clear event didn't clear any events, dropping: {}", event);
                 return null;
             }
             // Clear events always get created in CLOSED status
             if (event.getStatus() != EventStatus.STATUS_CLOSED) {
                 event = Event.newBuilder(event).setStatus(EventStatus.STATUS_CLOSED).build();
             }
         }
         else {
             clearedEventUuids = Collections.emptyList();
             fields.put(COLUMN_CLEAR_FINGERPRINT_HASH, EventDaoUtils.createClearHash(event,
                     context.getClearFingerprintGenerator()));
         }
 
         fields.put(COLUMN_STATUS_ID, event.getStatus().getNumber());
         fields.put(COLUMN_UPDATE_TIME, updateTime);
         fields.put(COLUMN_FIRST_SEEN, created);
         fields.put(COLUMN_STATUS_CHANGE, created);
         fields.put(COLUMN_LAST_SEEN, created);
         fields.put(COLUMN_EVENT_COUNT, eventCount);
 
         /*
          * Closed events have a unique fingerprint_hash in summary to allow multiple rows
          * but only allow one active event (where the de-duplication occurs).
          */
         if (ZepConstants.CLOSED_STATUSES.contains(event.getStatus())) {
             String uniqueFingerprint = (String) fields.get(COLUMN_FINGERPRINT) + '|' + updateTime;
             fields.put(COLUMN_FINGERPRINT_HASH, DaoUtils.sha1(uniqueFingerprint));
         }
         else {
             fields.put(COLUMN_FINGERPRINT_HASH, DaoUtils.sha1((String)fields.get(COLUMN_FINGERPRINT)));
         }
 
         String uuid;
         try {
             final UUID createdUuid = this.uuidGenerator.generate();
             fields.put(COLUMN_UUID, DaoUtils.uuidToBytes(createdUuid));
             this.insert.execute(fields);
             uuid = createdUuid.toString();
         } catch (DuplicateKeyException e) {
             final String sql = "SELECT event_count,first_seen,last_seen,details_json,status_id,uuid FROM event_summary" +
                     " WHERE fingerprint_hash=? FOR UPDATE";
             final EventSummary oldSummary = this.template.queryForObject(sql, new RowMapper<EventSummary>() {
                 @Override
                 public EventSummary mapRow(ResultSet rs, int rowNum) throws SQLException {
                     final EventSummary.Builder oldSummaryBuilder = EventSummary.newBuilder();
                     oldSummaryBuilder.setCount(rs.getInt(COLUMN_EVENT_COUNT));
                     oldSummaryBuilder.setFirstSeenTime(rs.getLong(COLUMN_FIRST_SEEN));
                     oldSummaryBuilder.setLastSeenTime(rs.getLong(COLUMN_LAST_SEEN));
                     oldSummaryBuilder.setStatus(EventStatus.valueOf(rs.getInt(COLUMN_STATUS_ID)));
                     oldSummaryBuilder.setUuid(DaoUtils.uuidFromBytes(rs.getBytes(COLUMN_UUID)));
                     
                     final Event.Builder occurrenceBuilder = oldSummaryBuilder.addOccurrenceBuilder(0);
                     final String detailsJson = rs.getString(COLUMN_DETAILS_JSON);
                     if (detailsJson != null) {
                         try {
                             occurrenceBuilder.addAllDetails(JsonFormat.mergeAllDelimitedFrom(detailsJson,
                                     EventDetail.getDefaultInstance()));
                         } catch (IOException e) {
                             throw new SQLException(e.getLocalizedMessage(), e);
                         }
                     }
                     return oldSummaryBuilder.build();
                 }
             }, fields.get(COLUMN_FINGERPRINT_HASH));
 
             final Map<String,Object> updateFields = getUpdateFields(oldSummary, event, fields);
             final StringBuilder updateSql = new StringBuilder("UPDATE event_summary SET ");
             for (Iterator<String> it = updateFields.keySet().iterator(); it.hasNext(); ) {
                 final String fieldName = it.next();
                 updateSql.append(fieldName).append("=:").append(fieldName);
                 if (it.hasNext()) {
                     updateSql.append(',');
                 }
             }
             updateSql.append(" WHERE fingerprint_hash=:fingerprint_hash");
             updateFields.put(COLUMN_FINGERPRINT_HASH, fields.get(COLUMN_FINGERPRINT_HASH));
 
             this.template.update(updateSql.toString(), updateFields);
             uuid = oldSummary.getUuid();
         }
 
         // Mark cleared events as cleared by this event
         if (!clearedEventUuids.isEmpty()) {
             final EventSummaryUpdateFields updateFields = new EventSummaryUpdateFields();
             updateFields.setClearedByEventUuid(uuid);
             update(clearedEventUuids, EventStatus.STATUS_CLEARED, updateFields, ZepConstants.OPEN_STATUSES);
         }
         return uuid;
     }
 
     /**
      * When an event is de-duped, if the event occurrence has a created time greater than or equal to the current
      * last_seen for the event summary, these fields from the event summary row are overwritten by values from the new
      * event occurrence. Special handling is performed when de-duping for event status and event details.
      */
     private static final List<String> UPDATE_FIELD_NAMES = Arrays.asList(COLUMN_EVENT_GROUP_ID,
             COLUMN_EVENT_CLASS_ID, COLUMN_EVENT_CLASS_KEY_ID, COLUMN_EVENT_CLASS_MAPPING_UUID, COLUMN_EVENT_KEY_ID,
             COLUMN_SEVERITY_ID, COLUMN_ELEMENT_UUID, COLUMN_ELEMENT_TYPE_ID, COLUMN_ELEMENT_IDENTIFIER,
             COLUMN_ELEMENT_TITLE, COLUMN_ELEMENT_SUB_UUID, COLUMN_ELEMENT_SUB_TYPE_ID, COLUMN_ELEMENT_SUB_IDENTIFIER,
             COLUMN_ELEMENT_SUB_TITLE, COLUMN_LAST_SEEN, COLUMN_MONITOR_ID, COLUMN_AGENT_ID, COLUMN_SYSLOG_FACILITY,
             COLUMN_SYSLOG_PRIORITY, COLUMN_NT_EVENT_CODE, COLUMN_CLEAR_FINGERPRINT_HASH, COLUMN_SUMMARY, COLUMN_MESSAGE,
             COLUMN_TAGS_JSON);
 
     private Map<String,Object> getUpdateFields(EventSummary oldSummary, Event occurrence,
                                                Map<String,Object> occurrenceFields) throws ZepException {
         Map<String,Object> updateFields = new HashMap<String, Object>();
         
         // Always increment count
         updateFields.put(COLUMN_EVENT_COUNT, oldSummary.getCount() + 1);
         
         updateFields.put(COLUMN_UPDATE_TIME, occurrenceFields.get(COLUMN_UPDATE_TIME));
             
         if (occurrence.getCreatedTime() >= oldSummary.getLastSeenTime()) {
 
             for (String fieldName : UPDATE_FIELD_NAMES) {
                 updateFields.put(fieldName, occurrenceFields.get(fieldName));
             }
 
             // Update status except for ACKNOWLEDGED -> {NEW|SUPPRESSED}
             // Stays in ACKNOWLEDGED in these cases
             boolean updateStatus = true;
             EventStatus oldStatus = oldSummary.getStatus();
             EventStatus newStatus = EventStatus.valueOf((Integer) occurrenceFields.get(COLUMN_STATUS_ID));
             switch (oldStatus) {
                 case STATUS_ACKNOWLEDGED:
                     switch (newStatus) {
                         case STATUS_NEW:
                         case STATUS_SUPPRESSED:
                             updateStatus = false;
                             break;
                     }
                     break;
             }
             if (updateStatus && oldStatus != newStatus) {
                 updateFields.put(COLUMN_STATUS_ID, occurrenceFields.get(COLUMN_STATUS_ID));
                 updateFields.put(COLUMN_STATUS_CHANGE, occurrence.getCreatedTime());
             }
 
             // Merge event details
             List<EventDetail> newDetails = occurrence.getDetailsList();
             if (!newDetails.isEmpty()) {
                 Collection<EventDetail> merged = EventDaoHelper.mergeDetails(
                         oldSummary.getOccurrence(0).getDetailsList(), newDetails);
                 try {
                     updateFields.put(COLUMN_DETAILS_JSON, JsonFormat.writeAllDelimitedAsString(merged));
                 } catch (IOException e) {
                     throw new ZepException(e.getLocalizedMessage(), e);
                 }
             }
         }
         else {
             // Merge event details - order swapped b/c of out of order event
             List<EventDetail> oldDetails = occurrence.getDetailsList();
             if (!oldDetails.isEmpty()) {
                 Collection<EventDetail> merged = EventDaoHelper.mergeDetails(oldDetails,
                         oldSummary.getOccurrence(0).getDetailsList());
                 try {
                     updateFields.put(COLUMN_DETAILS_JSON, JsonFormat.writeAllDelimitedAsString(merged));
                 } catch (IOException e) {
                     throw new ZepException(e.getLocalizedMessage(), e);
                 }
             }
         }
 
         if (occurrence.getCreatedTime() < oldSummary.getLastSeenTime()) {
             updateFields.put(COLUMN_FIRST_SEEN, occurrence.getCreatedTime());
         }
         return updateFields;
     }
 
     private List<String> clearEvents(Event event, EventPreCreateContext context)
             throws ZepException {
         final List<byte[]> clearHashes = EventDaoUtils.createClearHashes(event, context);
         if (clearHashes.isEmpty()) {
             logger.debug("Clear event didn't contain any clear hashes: {}, {}", event, context);
             return null;
         }
         final long lastSeen = event.getCreatedTime();
 
         /* Find events that this clear event would clear. */
         final String sql = "SELECT uuid FROM event_summary WHERE " +
                 "last_seen <= :_clear_created_time AND " +
                 "clear_fingerprint_hash IN (:_clear_hashes) AND " +
                 "status_id NOT IN (:_closed_status_ids) FOR UPDATE";
 
         Map<String,Object> fields = new HashMap<String,Object>(2);
         fields.put("_clear_created_time", lastSeen);
         fields.put("_clear_hashes", clearHashes);
         fields.put("_closed_status_ids", CLOSED_STATUS_IDS);
         return this.template.query(sql, new RowMapper<String>() {
             @Override
             public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                 return DaoUtils.uuidFromBytes(rs.getBytes(COLUMN_UUID));
             }
         }, fields);
     }
 
     @Override
     @TransactionalRollbackAllExceptions
     public int reidentify(ModelElementType type, String id, String uuid, String title, String parentUuid)
             throws ZepException {
         long updateTime = System.currentTimeMillis();
 
         Map<String, Object> fields = new HashMap<String, Object>();
         fields.put("_uuid", DaoUtils.uuidToBytes(uuid));
         fields.put("_uuid_str", uuid);
         fields.put("_type_id", type.getNumber());
         fields.put("_id", id);
        fields.put("_title", DaoUtils.truncateStringToUtf8(title, EventConstants.MAX_ELEMENT_TITLE));
         fields.put(COLUMN_UPDATE_TIME, updateTime);
 
         int numRows = 0;
         String updateSql = "UPDATE event_summary SET element_uuid=:_uuid, element_title=:_title, update_time=:update_time "
                 + "WHERE element_uuid IS NULL AND element_type_id=:_type_id AND element_identifier=:_id";
         numRows += this.template.update(updateSql, fields);
 
         if (parentUuid != null) {
             fields.put("_parent_uuid", DaoUtils.uuidToBytes(parentUuid));
             updateSql = "UPDATE event_summary es INNER JOIN event_class ON es.event_class_id = event_class.id " +
                     "LEFT JOIN event_key ON es.event_key_id = event_key.id " +
                     "SET element_sub_uuid=:_uuid, element_sub_title=:_title, update_time=:update_time, " +
                     "clear_fingerprint_hash=UNHEX(SHA1(CONCAT_WS('|',:_uuid_str,event_class.name,IFNULL(event_key.name,'')))) " +
                     "WHERE es.element_uuid=:_parent_uuid AND es.element_sub_uuid IS NULL AND " +
                     "es.element_sub_type_id=:_type_id AND es.element_sub_identifier=:_id";
             numRows += this.template.update(updateSql, fields);
         }
         return numRows;
     }
 
     @Override
     @TransactionalRollbackAllExceptions
     public int deidentify(String uuid) throws ZepException {
         long updateTime = System.currentTimeMillis();
 
         Map<String,Object> fields = new HashMap<String,Object>();
         fields.put("_uuid", DaoUtils.uuidToBytes(uuid));
         fields.put(COLUMN_UPDATE_TIME, updateTime);
 
         int numRows = 0;
         String updateSql = "UPDATE event_summary SET element_uuid=NULL, update_time=:update_time "
                 + "WHERE element_uuid=:_uuid";
         numRows += this.template.update(updateSql, fields);
 
         updateSql = "UPDATE event_summary es INNER JOIN event_class ON es.event_class_id = event_class.id " +
                 "LEFT JOIN event_key ON es.event_key_id = event_key.id " +
                 "SET element_sub_uuid=NULL, update_time=:update_time, " +
                 "clear_fingerprint_hash=UNHEX(SHA1(CONCAT_WS('|',element_identifier,IFNULL(element_sub_identifier,''),event_class.name,IFNULL(event_key.name,'')))) " +
                 "WHERE element_sub_uuid=:_uuid";
         numRows += this.template.update(updateSql, fields);
         return numRows;
     }
 
     @Override
     @TransactionalReadOnly
     public EventSummary findByUuid(String uuid) throws ZepException {
         final Map<String,byte[]> fields = Collections.singletonMap(COLUMN_UUID, DaoUtils.uuidToBytes(uuid));
         List<EventSummary> summaries = this.template.query("SELECT * FROM event_summary WHERE uuid=:uuid",
                 new EventSummaryRowMapper(this.eventDaoHelper), fields);
         return (summaries.size() > 0) ? summaries.get(0) : null;
     }
 
     @Override
     @TransactionalReadOnly
     public List<EventSummary> findByUuids(final List<String> uuids)
             throws ZepException {
         if (uuids.isEmpty()) {
             return Collections.emptyList();
         }
         Map<String, List<byte[]>> fields = Collections.singletonMap("uuids", DaoUtils.uuidsToBytes(uuids));
         return this.template.query("SELECT * FROM event_summary WHERE uuid IN(:uuids)",
                 new EventSummaryRowMapper(this.eventDaoHelper), fields);
     }
     
     @Override
     @TransactionalReadOnly
     public List<EventSummary> listBatch(String startingUuid, long maxUpdateTime, int limit) throws ZepException {
         return this.eventDaoHelper.listBatch(this.template, TABLE_EVENT_SUMMARY, startingUuid, maxUpdateTime, limit);
     }
 
     private static final EnumSet<EventStatus> AUDIT_LOG_STATUSES = EnumSet.of(
             EventStatus.STATUS_NEW, EventStatus.STATUS_ACKNOWLEDGED, EventStatus.STATUS_CLOSED,
             EventStatus.STATUS_CLEARED);
 
     private static final List<Integer> CLOSED_STATUS_IDS = Arrays.asList(
             EventStatus.STATUS_AGED.getNumber(),
             EventStatus.STATUS_CLEARED.getNumber(),
             EventStatus.STATUS_CLOSED.getNumber());
 
     @Override
     @TransactionalRollbackAllExceptions
     public int ageEvents(long agingInterval, TimeUnit unit,
             EventSeverity maxSeverity, int limit, boolean inclusiveSeverity) throws ZepException {
         long agingIntervalMs = unit.toMillis(agingInterval);
         if (agingIntervalMs < 0 || agingIntervalMs == Long.MAX_VALUE) {
             throw new ZepException("Invalid aging interval: " + agingIntervalMs);
         }
         if (limit <= 0) {
             throw new ZepException("Limit can't be negative: " + limit);
         }
         List<Integer> severityIds = EventDaoHelper.getSeverityIdsLessThan(maxSeverity);
         if (inclusiveSeverity) {
             severityIds.add(maxSeverity.getNumber());
         }
         if (severityIds.isEmpty()) {
             logger.debug("Not aging events - max severity specified");
             return 0;
         }
         long now = System.currentTimeMillis();
         long ageTs = now - agingIntervalMs;
 
         Map<String, Object> fields = new HashMap<String, Object>();
         fields.put(COLUMN_STATUS_ID, EventStatus.STATUS_AGED.getNumber());
         fields.put(COLUMN_STATUS_CHANGE, now);
         fields.put(COLUMN_UPDATE_TIME, now);
         fields.put(COLUMN_LAST_SEEN, ageTs);
         fields.put("_severity_ids", severityIds);
         fields.put("_closed_status_ids", CLOSED_STATUS_IDS);
         fields.put("_limit", limit);
 
         String updateSql = "UPDATE event_summary SET status_id=:status_id, "
                 + "status_change=:status_change, update_time=:update_time "
                 + "WHERE last_seen < :last_seen AND "
                 + "severity_id IN (:_severity_ids) AND "
                 + "status_id NOT IN (:_closed_status_ids) LIMIT :_limit";
         return this.template.update(updateSql, fields);
     }
 
     @Override
     @TransactionalRollbackAllExceptions
     public int addNote(String uuid, EventNote note) throws ZepException {
         return this.eventDaoHelper.addNote(TABLE_EVENT_SUMMARY, uuid, note, template);
     }
 
     @Override
     @TransactionalRollbackAllExceptions
     public int updateDetails(String uuid, EventDetailSet details)
             throws ZepException {
         return EventDaoHelper.updateDetails(TABLE_EVENT_SUMMARY, uuid, details.getDetailsList(), template);
     }
 
     private static class EventSummaryUpdateFields {
         private String currentUserUuid;
         private String currentUserName;
         private String clearedByEventUuid;
 
         public static final EventSummaryUpdateFields EMPTY_FIELDS = new EventSummaryUpdateFields();
 
         public Map<String,Object> toMap() {
             Map<String,Object> m = new HashMap<String,Object>();
             byte[] currentUuidBytes = null;
             if (this.currentUserUuid != null) {
                 currentUuidBytes = DaoUtils.uuidToBytes(this.currentUserUuid);
             }
             m.put(COLUMN_CURRENT_USER_UUID, currentUuidBytes);
             m.put(COLUMN_CURRENT_USER_NAME, currentUserName);
 
             byte[] clearedUuidBytes = null;
             if (this.clearedByEventUuid != null) {
                 clearedUuidBytes = DaoUtils.uuidToBytes(this.clearedByEventUuid);
             }
             m.put(COLUMN_CLEARED_BY_EVENT_UUID, clearedUuidBytes);
             return m;
         }
 
         public void setCurrentUserUuid(String currentUserUuid) {
             this.currentUserUuid = currentUserUuid;
         }
 
         public void setCurrentUserName(String currentUserName) {
             this.currentUserName = DaoUtils.truncateStringToUtf8(currentUserName, MAX_CURRENT_USER_NAME);
         }
 
         public void setClearedByEventUuid(String clearedByEventUuid) {
             this.clearedByEventUuid = clearedByEventUuid;
         }
     }
 
     private int update(List<String> uuids, EventStatus status, EventSummaryUpdateFields updateFields,
                        Collection<EventStatus> currentStatuses)
             throws ZepException {
         if (uuids.isEmpty()) {
             return 0;
         }
 
         final long now = System.currentTimeMillis();
         Map<String,Object> updateFieldsMap = updateFields.toMap();
         updateFieldsMap.put(COLUMN_STATUS_ID, status.getNumber());
         updateFieldsMap.put(COLUMN_STATUS_CHANGE, now);
         updateFieldsMap.put(COLUMN_UPDATE_TIME, now);
         updateFieldsMap.put("_uuids", DaoUtils.uuidsToBytes(uuids));
 
         /*
          * IGNORE duplicate key errors on update. This will occur if there is an active
          * event with the same fingerprint.
          */
         StringBuilder sb = new StringBuilder("UPDATE IGNORE event_summary SET status_id=:status_id")
                 .append(",status_change=:status_change,update_time=:update_time")
                 .append(",current_user_uuid=:current_user_uuid")
                 .append(",current_user_name=:current_user_name")
                 .append(",cleared_by_event_uuid=:cleared_by_event_uuid");
         // When closing an event, give it a unique fingerprint hash
         if (ZepConstants.CLOSED_STATUSES.contains(status)) {
             sb.append(",fingerprint_hash=UNHEX(SHA1(CONCAT_WS('|',fingerprint,:update_time)))");
         }
         /*
          * When reopening an event, give it the true fingerprint_hash. This is required
          * to correctly de-duplicate events.
          */
         else {
             sb.append(",fingerprint_hash=UNHEX(SHA1(fingerprint))");
         }
 
         /*
          * If this is a significant status change, also add an audit note
          */
         if (AUDIT_LOG_STATUSES.contains(status)) {
             EventAuditLog.Builder builder = EventAuditLog.newBuilder();
             builder.setTimestamp(now);
             builder.setNewStatus(status);
             byte[] event_user_uuid_bytes = (byte[])updateFieldsMap.get(COLUMN_CURRENT_USER_UUID);
             if (event_user_uuid_bytes != null) {
                 builder.setUserUuid(DaoUtils.uuidFromBytes(event_user_uuid_bytes));
             }
             String event_user = (String)updateFieldsMap.get(COLUMN_CURRENT_USER_NAME);
             if (event_user != null) {
                 builder.setUserName(event_user);
             }
 
             try {
                 updateFieldsMap.put(COLUMN_AUDIT_JSON, JsonFormat.writeAsString(builder.build()));
             } catch (IOException e) {
                 throw new ZepException(e);
             }
             sb.append(",audit_json=CONCAT_WS(',\n',:audit_json,audit_json)");
         }
 
         sb.append(" WHERE uuid IN (:_uuids)");
 
         /*
          * This is required to support well-defined transitions between states. We only allow
          * updates to move events between states that make sense.
          */
         if (!currentStatuses.isEmpty()) {
             final List<Integer> currentStatusIds = new ArrayList<Integer>(currentStatuses.size());
             for (EventStatus currentStatus : currentStatuses) {
                 currentStatusIds.add(currentStatus.getNumber());
             }
             sb.append(" AND status_id IN (");
             sb.append(StringUtils.collectionToCommaDelimitedString(currentStatusIds));
             sb.append(')');
         }
 
         final String updateSql = sb.toString();
         return this.template.update(updateSql, updateFieldsMap);
     }
 
     @Override
     @TransactionalRollbackAllExceptions
     public int acknowledge(List<String> uuids, String userUuid, String userName)
             throws ZepException {
         /* NEW | SUPPRESSED -> ACKNOWLEDGED */
         List<EventStatus> currentStatuses = Arrays.asList(EventStatus.STATUS_NEW, EventStatus.STATUS_SUPPRESSED);
         EventSummaryUpdateFields userfields = new EventSummaryUpdateFields();
         userfields.setCurrentUserName(userName);
         userfields.setCurrentUserUuid(userUuid);
         return update(uuids, EventStatus.STATUS_ACKNOWLEDGED, userfields, currentStatuses);
     }
 
     @Override
     @TransactionalRollbackAllExceptions
     public int archive(long duration, TimeUnit unit, int limit)
             throws ZepException {
         final long updateTime = System.currentTimeMillis();
         final long lastSeen = updateTime - unit.toMillis(duration);
         Map<String, Object> fields = new HashMap<String, Object>();
         fields.put("_last_seen", lastSeen);
         fields.put("_closed_status_ids", CLOSED_STATUS_IDS);
         fields.put("_limit", limit);
 
         final String sql = "SELECT uuid FROM event_summary WHERE status_id IN (:_closed_status_ids) AND "
                 + "last_seen < :_last_seen LIMIT :_limit FOR UPDATE";
         final List<String> uuids = this.template.query(sql, new RowMapper<String>() {
                     @Override
                     public String mapRow(ResultSet rs, int rowNum)
                             throws SQLException {
                         return DaoUtils.uuidFromBytes(rs.getBytes(COLUMN_UUID));
                     }
                 }, fields);
         return archive(uuids);
     }
 
     @Override
     @TransactionalRollbackAllExceptions
     public int close(List<String> uuids, String userUuid, String userName) throws ZepException {
         /* NEW | ACKNOWLEDGED | SUPPRESSED -> CLOSED */
         List<EventStatus> currentStatuses = Arrays.asList(EventStatus.STATUS_NEW, EventStatus.STATUS_ACKNOWLEDGED,
                 EventStatus.STATUS_SUPPRESSED);
         EventSummaryUpdateFields userfields = new EventSummaryUpdateFields();
         userfields.setCurrentUserName(userName);
         userfields.setCurrentUserUuid(userUuid);
         return update(uuids, EventStatus.STATUS_CLOSED, userfields, currentStatuses);
     }
 
     @Override
     @TransactionalRollbackAllExceptions
     public int reopen(List<String> uuids, String userUuid, String userName) throws ZepException {
         /* CLOSED | CLEARED | AGED | ACKNOWLEDGED -> NEW */
         List<EventStatus> currentStatuses = Arrays.asList(EventStatus.STATUS_CLOSED, EventStatus.STATUS_CLEARED,
                 EventStatus.STATUS_AGED, EventStatus.STATUS_ACKNOWLEDGED);
         EventSummaryUpdateFields userfields = new EventSummaryUpdateFields();
         userfields.setCurrentUserName(userName);
         userfields.setCurrentUserUuid(userUuid);
         return update(uuids, EventStatus.STATUS_NEW, userfields, currentStatuses);
     }
 
     @Override
     @TransactionalRollbackAllExceptions
     public int suppress(List<String> uuids) throws ZepException {
         /* NEW -> SUPPRESSED */
         List<EventStatus> currentStatuses = Arrays.asList(EventStatus.STATUS_NEW);
         return update(uuids, EventStatus.STATUS_SUPPRESSED, EventSummaryUpdateFields.EMPTY_FIELDS, currentStatuses);
     }
 
     @Override
     @TransactionalRollbackAllExceptions
     public int archive(List<String> uuids) throws ZepException {
         if (uuids.isEmpty()) {
             return 0;
         }
         if (this.archiveColumnNames == null) {
             try {
                 this.archiveColumnNames = DaoUtils.getColumnNames(this.dataSource, TABLE_EVENT_ARCHIVE);
             } catch (MetaDataAccessException e) {
                 throw new ZepException(e.getLocalizedMessage(), e);
             }
         }
 
         Map<String, Object> fields = new HashMap<String,Object>();
         fields.put(COLUMN_UPDATE_TIME, System.currentTimeMillis());
         fields.put("_uuids", DaoUtils.uuidsToBytes(uuids));
         fields.put("_closed_status_ids", CLOSED_STATUS_IDS);
         StringBuilder selectColumns = new StringBuilder();
 
         for (Iterator<String> it = this.archiveColumnNames.iterator(); it.hasNext();) {
             String columnName = it.next();
             if (fields.containsKey(columnName)) {
                 selectColumns.append(':').append(columnName);
             } else {
                 selectColumns.append(columnName);
             }
             if (it.hasNext()) {
                 selectColumns.append(',');
             }
         }
 
         String insertSql = String.format("INSERT INTO event_archive (%s) SELECT %s FROM event_summary" +
                 " WHERE uuid IN (:_uuids) AND status_id IN (:_closed_status_ids)",
                 StringUtils.collectionToCommaDelimitedString(this.archiveColumnNames), selectColumns);
 
         final int updated = this.template.update(insertSql, fields);
         this.template.update("DELETE FROM event_summary WHERE uuid IN (:_uuids) AND status_id IN (:_closed_status_ids)",
                 fields);
         return updated;
     }
 
     @Override
     @TransactionalRollbackAllExceptions
     public void importEvent(EventSummary eventSummary) throws ZepException {
         final long updateTime = System.currentTimeMillis();
         final EventSummary.Builder summaryBuilder = EventSummary.newBuilder(eventSummary);
         final Event.Builder eventBuilder = summaryBuilder.getOccurrenceBuilder(0);
         summaryBuilder.setUpdateTime(updateTime);
         EventDaoHelper.addMigrateUpdateTimeDetail(eventBuilder, updateTime);
 
         final EventSummary summary = summaryBuilder.build();
         final Map<String,Object> fields = this.eventDaoHelper.createImportedSummaryFields(summary);
 
         /*
          * Closed events have a unique fingerprint_hash in summary to allow multiple rows
          * but only allow one active event (where the de-duplication occurs).
          */
         if (ZepConstants.CLOSED_STATUSES.contains(eventSummary.getStatus())) {
             String uniqueFingerprint = (String) fields.get(COLUMN_FINGERPRINT) + '|' + updateTime;
             fields.put(COLUMN_FINGERPRINT_HASH, DaoUtils.sha1(uniqueFingerprint));
         }
         else {
             fields.put(COLUMN_FINGERPRINT_HASH, DaoUtils.sha1((String)fields.get(COLUMN_FINGERPRINT)));
         }
 
         if (eventSummary.getOccurrence(0).getSeverity() != EventSeverity.SEVERITY_CLEAR) {
             fields.put(COLUMN_CLEAR_FINGERPRINT_HASH, EventDaoUtils.createClearHash(eventSummary.getOccurrence(0)));
         }
 
         this.insert.execute(fields);
     }
 }
