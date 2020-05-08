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
 package org.zenoss.zep.dao.impl;
 
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
 import org.zenoss.protobufs.model.Model.ModelElementType;
 import org.zenoss.protobufs.zep.Zep.Event;
 import org.zenoss.protobufs.zep.Zep.EventActor;
 import org.zenoss.protobufs.zep.Zep.EventDetail;
 import org.zenoss.protobufs.zep.Zep.EventDetail.EventDetailMergeBehavior;
 import org.zenoss.protobufs.zep.Zep.EventDetailSet;
 import org.zenoss.protobufs.zep.Zep.EventNote;
 import org.zenoss.protobufs.zep.Zep.EventSeverity;
 import org.zenoss.protobufs.zep.Zep.EventStatus;
 import org.zenoss.protobufs.zep.Zep.EventSummary;
 import org.zenoss.protobufs.zep.Zep.EventTag;
 import org.zenoss.protobufs.zep.Zep.SyslogPriority;
 import org.zenoss.zep.ZepException;
 import org.zenoss.zep.dao.EventArchiveDao;
 import org.zenoss.zep.dao.EventDao;
 import org.zenoss.zep.dao.EventSummaryDao;
 
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 import java.util.UUID;
 import java.util.concurrent.TimeUnit;
 
 import static org.junit.Assert.*;
 import static org.zenoss.zep.dao.impl.EventConstants.*;
 
 @ContextConfiguration({ "classpath:zep-config.xml" })
 public class EventSummaryDaoImplIT extends
         AbstractTransactionalJUnit4SpringContextTests {
 
     private static Random random = new Random();
 
     @Autowired
     public EventSummaryDao eventSummaryDao;
 
     @Autowired
     public EventArchiveDao eventArchiveDao;
 
     @Autowired
     public EventDao eventDao;
 
     private EventSummary createSummaryNew(Event event) throws ZepException {
         return createSummary(event, EventStatus.STATUS_NEW);
     }
 
     private EventSummary createSummary(Event event, EventStatus status)
             throws ZepException {
         String uuid = eventSummaryDao.create(event, status);
         return eventSummaryDao.findByUuid(uuid);
     }
 
     private EventSummary createSummaryClear(Event event,
             Set<String> clearClasses) throws ZepException {
         String uuid = eventSummaryDao.createClearEvent(event, clearClasses);
         return eventSummaryDao.findByUuid(uuid);
     }
 
     private static void compareEvents(Event event, Event eventFromDb) {
         Event event1 = Event.newBuilder().mergeFrom(event).clearUuid()
                 .clearCreatedTime().build();
         Event event2 = Event.newBuilder().mergeFrom(eventFromDb).clearUuid()
                 .clearCreatedTime().build();
         assertEquals(event1, event2);
     }
 
     private static void compareSummary(EventSummary expected,
             EventSummary actual) {
         expected = EventSummary.newBuilder(expected).clearStatus()
                 .clearStatusChangeTime().clearUpdateTime().build();
         actual = EventSummary.newBuilder(expected).clearStatus()
                 .clearStatusChangeTime().clearUpdateTime().build();
         assertEquals(expected, actual);
     }
 
     @Test
     public void testSummaryInsert() throws ZepException, InterruptedException {
         Event event = EventDaoImplIT.createSampleEvent();
         EventSummary eventSummaryFromDb = createSummaryNew(event);
         Event eventFromSummary = eventSummaryFromDb.getOccurrence(0);
         compareEvents(event, eventFromSummary);
         assertEquals(1, eventSummaryFromDb.getCount());
         assertEquals(EventStatus.STATUS_NEW, eventSummaryFromDb.getStatus());
         assertEquals(eventFromSummary.getCreatedTime(),
                 eventSummaryFromDb.getFirstSeenTime());
         assertEquals(eventFromSummary.getCreatedTime(),
                 eventSummaryFromDb.getStatusChangeTime());
         assertEquals(eventFromSummary.getCreatedTime(),
                 eventSummaryFromDb.getLastSeenTime());
         assertFalse(eventSummaryFromDb.hasCurrentUserUuid());
         assertFalse(eventSummaryFromDb.hasCurrentUserName());
         assertFalse(eventSummaryFromDb.hasClearedByEventUuid());
 
         /*
          * Create event with same fingerprint but again with new message,
          * summary, details.
          */
         Event.Builder newEventBuilder = Event.newBuilder().mergeFrom(event);
         newEventBuilder.setUuid(UUID.randomUUID().toString());
         newEventBuilder.setCreatedTime(System.currentTimeMillis());
         newEventBuilder.setMessage(event.getMessage() + random.nextInt(500));
         newEventBuilder.setSummary(event.getSummary() + random.nextInt(1000));
         newEventBuilder.clearDetails();
         newEventBuilder.addDetails(createDetail("newname1", "newvalue1", "newvalue2"));
         Event newEvent = newEventBuilder.build();
 
         EventSummary newEventSummaryFromDb = createSummaryNew(newEvent);
 
         assertEquals(eventSummaryFromDb.getUuid(),
                 newEventSummaryFromDb.getUuid());
         Event newEventFromSummary = newEventSummaryFromDb.getOccurrence(0);
         assertTrue(newEventSummaryFromDb.getLastSeenTime() > newEventSummaryFromDb
                 .getFirstSeenTime());
         assertEquals(eventSummaryFromDb.getFirstSeenTime(),
                 newEventSummaryFromDb.getFirstSeenTime());
         assertEquals(newEventFromSummary.getCreatedTime(),
                 newEventSummaryFromDb.getLastSeenTime());
         assertEquals(newEvent.getCreatedTime(),
                 newEventSummaryFromDb.getLastSeenTime());
         // Verify status didn't change (two NEW events)
         assertEquals(event.getCreatedTime(),
                 newEventSummaryFromDb.getStatusChangeTime());
         assertEquals(newEvent.getMessage(), newEventFromSummary.getMessage());
         assertEquals(newEvent.getSummary(), newEventFromSummary.getSummary());
 
         List<EventDetail> combined = new ArrayList<EventDetail>();
         combined.addAll(event.getDetailsList());
         combined.addAll(newEvent.getDetailsList());
         assertEquals(combined, newEventFromSummary.getDetailsList());
 
         eventSummaryDao.delete(eventSummaryFromDb.getUuid());
         assertNull(eventSummaryDao.findByUuid(eventSummaryFromDb.getUuid()));
     }
 
     @Test
     public void testAcknowledgedToNew() throws ZepException {
         /*
          * Verify acknowledged events aren't changed to new with a new
          * occurrence.
          */
         Event event = EventDaoImplIT.createSampleEvent();
         eventSummaryDao.create(event, EventStatus.STATUS_ACKNOWLEDGED);
         Event.Builder newEventBuilder = Event.newBuilder().mergeFrom(event);
         newEventBuilder.setUuid(UUID.randomUUID().toString());
         newEventBuilder.setCreatedTime(event.getCreatedTime() + 50L);
         Event newEvent = newEventBuilder.build();
 
         EventSummary newEventSummaryFromDb = createSummaryNew(newEvent);
 
         assertEquals(EventStatus.STATUS_ACKNOWLEDGED,
                 newEventSummaryFromDb.getStatus());
         assertEquals(event.getCreatedTime(),
                 newEventSummaryFromDb.getStatusChangeTime());
     }
 
     @Test
     public void testAcknowledgedToSuppressed() throws ZepException {
         /*
          * Verify acknowledged events aren't changed to suppressed with a new
          * occurrence.
          */
         Event event = EventDaoImplIT.createSampleEvent();
         eventSummaryDao.create(event, EventStatus.STATUS_ACKNOWLEDGED);
         Event.Builder newEventBuilder = Event.newBuilder().mergeFrom(event);
         newEventBuilder.setUuid(UUID.randomUUID().toString());
         newEventBuilder.setCreatedTime(event.getCreatedTime() + 50L);
         Event newEvent = newEventBuilder.build();
 
         EventSummary newEventSummaryFromDb = createSummary(newEvent,
                 EventStatus.STATUS_SUPPRESSED);
 
         assertEquals(EventStatus.STATUS_ACKNOWLEDGED,
                 newEventSummaryFromDb.getStatus());
         assertEquals(event.getCreatedTime(),
                 newEventSummaryFromDb.getStatusChangeTime());
     }
 
     private static String createRandomMaxString(int length) {
         final String alphabet = "abcdefghijklmnopqrstuvwxyz";
         final char[] chars = new char[length];
         for (int i = 0; i < chars.length; i++) {
             chars[i] = alphabet.charAt(random.nextInt(alphabet.length()));
         }
         return new String(chars);
     }
 
     @Test
     public void testSummaryMaxInsert() throws ZepException,
             InterruptedException {
         Event.Builder eventBuilder = Event.newBuilder(createUniqueEvent());
         EventActor.Builder actorBuilder = EventActor.newBuilder(eventBuilder.getActor());
         eventBuilder.setFingerprint(createRandomMaxString(MAX_FINGERPRINT + 1));
         actorBuilder.setElementIdentifier(createRandomMaxString(MAX_ELEMENT_IDENTIFIER + 1));
         actorBuilder.setElementSubIdentifier(createRandomMaxString(MAX_ELEMENT_SUB_IDENTIFIER + 1));
         eventBuilder.setEventClass(createRandomMaxString(MAX_EVENT_CLASS + 1));
         eventBuilder.setEventClassKey(createRandomMaxString(MAX_EVENT_CLASS_KEY + 1));
         eventBuilder.setEventKey(createRandomMaxString(MAX_EVENT_KEY + 1));
         eventBuilder.setMonitor(createRandomMaxString(MAX_MONITOR + 1));
         eventBuilder.setAgent(createRandomMaxString(MAX_AGENT + 1));
         eventBuilder.setEventGroup(createRandomMaxString(MAX_EVENT_GROUP + 1));
         eventBuilder.setSummary(createRandomMaxString(MAX_SUMMARY + 1));
         eventBuilder.setMessage(createRandomMaxString(MAX_MESSAGE + 1));
         eventBuilder.setActor(actorBuilder.build());
         final Event event = eventBuilder.build();
         final EventSummary summary = createSummaryNew(event);
         final Event eventFromDb = summary.getOccurrence(0);
         final EventActor actorFromDb = eventFromDb.getActor();
         assertEquals(MAX_FINGERPRINT, eventFromDb.getFingerprint().length());
         assertEquals(MAX_ELEMENT_IDENTIFIER, actorFromDb.getElementIdentifier().length());
         assertEquals(MAX_ELEMENT_SUB_IDENTIFIER, actorFromDb.getElementSubIdentifier().length());
         assertEquals(MAX_EVENT_CLASS, eventFromDb.getEventClass().length());
         assertEquals(MAX_EVENT_CLASS_KEY, eventFromDb.getEventClassKey().length());
         assertEquals(MAX_EVENT_KEY, eventFromDb.getEventKey().length());
         assertEquals(MAX_MONITOR, eventFromDb.getMonitor().length());
         assertEquals(MAX_AGENT, eventFromDb.getAgent().length());
         assertEquals(MAX_EVENT_GROUP, eventFromDb.getEventGroup().length());
         assertEquals(MAX_SUMMARY, eventFromDb.getSummary().length());
         assertEquals(MAX_MESSAGE, eventFromDb.getMessage().length());
 
         this.eventSummaryDao.acknowledge(Collections.singletonList(summary.getUuid()), UUID.randomUUID().toString(),
                 createRandomMaxString(MAX_CURRENT_USER_NAME + 1));
         final EventSummary summaryFromDb = this.eventSummaryDao.findByUuid(summary.getUuid());
         assertEquals(MAX_CURRENT_USER_NAME, summaryFromDb.getCurrentUserName().length());
     }
 
     public static Event createUniqueEvent() {
         Random r = new Random();
         Event.Builder eventBuilder = Event.newBuilder();
         eventBuilder.setUuid(UUID.randomUUID().toString());
         eventBuilder.setCreatedTime(System.currentTimeMillis());
         eventBuilder.addDetails(createDetail("foo", "bar", "baz"));
         eventBuilder.addDetails(createDetail("foo2", "bar2", "baz2"));
         eventBuilder.setActor(EventDaoImplIT.createSampleActor());
         eventBuilder.setAgent("agent");
         eventBuilder.setEventClass("/Unknown");
         eventBuilder.setEventClassKey("eventClassKey");
         eventBuilder.setEventClassMappingUuid(UUID.randomUUID().toString());
         eventBuilder.setEventGroup("event group");
         eventBuilder.setEventKey("event key");
         eventBuilder.setFingerprint("my|dedupid|foo|" + r.nextInt());
         eventBuilder.setMessage("my message");
         eventBuilder.setMonitor("monitor");
         eventBuilder.setNtEventCode(r.nextInt(50000));
         eventBuilder.setSeverity(EventSeverity.SEVERITY_CRITICAL);
         eventBuilder.setSummary("summary message");
         eventBuilder.setSyslogFacility(11);
         eventBuilder.setSyslogPriority(SyslogPriority.SYSLOG_PRIORITY_DEBUG);
         return eventBuilder.build();
     }
 
     @Test
     public void testListByUuid() throws ZepException {
         Set<String> uuidsToSearch = new HashSet<String>();
         for (int i = 0; i < 10; i++) {
             String uuid = createSummaryNew(createUniqueEvent()).getUuid();
             if ((i % 2) == 0) {
                 uuidsToSearch.add(uuid);
             }
         }
 
         List<EventSummary> result = eventSummaryDao
                 .findByUuids(new ArrayList<String>(uuidsToSearch));
         assertEquals(uuidsToSearch.size(), result.size());
         for (EventSummary event : result) {
             assertTrue(uuidsToSearch.contains(event.getUuid()));
         }
     }
 
     @Test
     public void testReopen() throws ZepException {
         EventSummary summary = createSummaryNew(createUniqueEvent());
         long origStatusChange = summary.getStatusChangeTime();
         long origUpdateTime = summary.getUpdateTime();
         assertEquals(EventStatus.STATUS_NEW, summary.getStatus());
         assertFalse(summary.hasCurrentUserUuid());
         assertFalse(summary.hasCurrentUserName());
 
         String userUuid = UUID.randomUUID().toString();
         String userName = "user" + random.nextInt(500);
 
         int numUpdated = eventSummaryDao.close(Collections.singletonList(summary.getUuid()), userUuid, userName);
         assertEquals(1, numUpdated);
         summary = eventSummaryDao.findByUuid(summary.getUuid());
         assertEquals(EventStatus.STATUS_CLOSED, summary.getStatus());
         assertTrue(summary.getStatusChangeTime() > origStatusChange);
         assertTrue(summary.getUpdateTime() > origUpdateTime);
         origStatusChange = summary.getStatusChangeTime();
         origUpdateTime = summary.getUpdateTime();
 
         /* Now reopen event */
         numUpdated = eventSummaryDao.reopen(Collections.singletonList(summary.getUuid()), userUuid, userName);
         assertEquals(1, numUpdated);
         EventSummary origSummary = summary;
         summary = eventSummaryDao.findByUuid(summary.getUuid());
        assertEquals(userUuid, summary.getCurrentUserUuid());
        assertEquals(userName, summary.getCurrentUserName());
         assertEquals(EventStatus.STATUS_NEW, summary.getStatus());
         assertTrue(summary.getStatusChangeTime() > origStatusChange);
         assertTrue(summary.getUpdateTime() > origUpdateTime);
 
         compareSummary(origSummary, summary);
     }
 
     @Test
     public void testAcknowledge() throws ZepException {
         EventSummary summary = createSummaryNew(createUniqueEvent());
         long origStatusChange = summary.getStatusChangeTime();
         long origUpdateTime = summary.getUpdateTime();
         assertEquals(EventStatus.STATUS_NEW, summary.getStatus());
         assertFalse(summary.hasCurrentUserUuid());
         assertFalse(summary.hasCurrentUserName());
 
         String userUuid = UUID.randomUUID().toString();
         String userName = "user" + random.nextInt(500);
         EventSummary origSummary = summary;
         int numUpdated = eventSummaryDao.acknowledge(
                 Collections.singletonList(summary.getUuid()), userUuid, userName);
         assertEquals(1, numUpdated);
         summary = eventSummaryDao.findByUuid(summary.getUuid());
         assertEquals(userUuid, summary.getCurrentUserUuid());
         assertEquals(userName, summary.getCurrentUserName());
         assertEquals(EventStatus.STATUS_ACKNOWLEDGED, summary.getStatus());
         assertTrue(summary.getStatusChangeTime() > origStatusChange);
         assertTrue(summary.getUpdateTime() > origUpdateTime);
 
         compareSummary(origSummary, summary);
     }
 
     @Test
     public void testSuppress() throws ZepException {
         EventSummary summary = createSummaryNew(createUniqueEvent());
         long origStatusChange = summary.getStatusChangeTime();
         long origUpdateTime = summary.getUpdateTime();
         assertEquals(EventStatus.STATUS_NEW, summary.getStatus());
         assertFalse(summary.hasCurrentUserUuid());
         assertFalse(summary.hasCurrentUserName());
 
         EventSummary origSummary = summary;
         int numUpdated = eventSummaryDao.suppress(Collections
                 .singletonList(summary.getUuid()));
         assertEquals(1, numUpdated);
         summary = eventSummaryDao.findByUuid(summary.getUuid());
         assertEquals(EventStatus.STATUS_SUPPRESSED, summary.getStatus());
         assertTrue(summary.getStatusChangeTime() > origStatusChange);
         assertTrue(summary.getUpdateTime() > origUpdateTime);
 
         compareSummary(origSummary, summary);
     }
 
     @Test
     public void testClose() throws ZepException {
         EventSummary summary = createSummaryNew(createUniqueEvent());
         long origStatusChange = summary.getStatusChangeTime();
         long origUpdateTime = summary.getUpdateTime();
         assertEquals(EventStatus.STATUS_NEW, summary.getStatus());
         assertFalse(summary.hasCurrentUserUuid());
         assertFalse(summary.hasCurrentUserName());
 
         String userUuid = UUID.randomUUID().toString();
         String userName = "user" + random.nextInt(500);
         EventSummary origSummary = summary;
         int numUpdated = eventSummaryDao.close(Collections
                 .singletonList(summary.getUuid()), userUuid, userName);
         assertEquals(1, numUpdated);
         summary = eventSummaryDao.findByUuid(summary.getUuid());
         assertEquals(EventStatus.STATUS_CLOSED, summary.getStatus());
         assertTrue(summary.getStatusChangeTime() > origStatusChange);
         assertTrue(summary.getUpdateTime() > origUpdateTime);
 
         compareSummary(origSummary, summary);
     }
 
     @Test
     public void testAddNote() throws ZepException {
         EventSummary summary = createSummaryNew(createUniqueEvent());
         assertEquals(0, summary.getNotesCount());
         EventNote note = EventNote.newBuilder().setMessage("My Note")
                 .setUserName("pkw").setUserUuid(UUID.randomUUID().toString())
                 .build();
         assertEquals(1, eventSummaryDao.addNote(summary.getUuid(), note));
         EventNote note2 = EventNote.newBuilder().setMessage("My Note 2")
                 .setUserName("kww").setUserUuid(UUID.randomUUID().toString())
                 .build();
         assertEquals(1, eventSummaryDao.addNote(summary.getUuid(), note2));
         summary = eventSummaryDao.findByUuid(summary.getUuid());
         assertEquals(2, summary.getNotesCount());
         // Notes returned in descending order to match previous behavior
         assertEquals("My Note 2", summary.getNotes(0).getMessage());
         assertEquals("kww", summary.getNotes(0).getUserName());
         assertEquals("My Note", summary.getNotes(1).getMessage());
         assertEquals("pkw", summary.getNotes(1).getUserName());
     }
 
     @Test
     public void testUpdateDetailsReplace() throws ZepException {
         Event newEvent = createUniqueEvent();
         Event.Builder builder = Event.newBuilder(newEvent).clearDetails();
         builder.addDetails(createDetail("A", "A"));
         builder.addDetails(createDetail("B", "B"));
         builder.addDetails(createDetail("C", "C"));
         newEvent = builder.build();
 
         EventSummary summary = createSummaryNew(newEvent);
         Event storedEvent = summary.getOccurrence(0);
         assertEquals(3, storedEvent.getDetailsCount());
 
         List<EventDetail> newDetailsList = new ArrayList<EventDetail>();
         newDetailsList.add(createDetail("B", "B1"));
         newDetailsList.add(createDetail("C"));
         newDetailsList.add(createDetail("D", "D"));
         EventDetailSet newDetails = EventDetailSet.newBuilder().addAllDetails(newDetailsList).build();
 
         // ensure update works correctly
         assertEquals(1, eventSummaryDao.updateDetails(summary.getUuid(), newDetails));
 
         // verify new contents of details
         summary = eventSummaryDao.findByUuid(summary.getUuid());
 
         List<EventDetail> resultDetails = summary.getOccurrence(0).getDetailsList();
         assertEquals(3, resultDetails.size());
 
         Map<String, List<String>> resultDetailsMap = detailsToMap(resultDetails);
         assertEquals(Collections.singletonList("A"), resultDetailsMap.get("A"));
         assertEquals(Collections.singletonList("B1"), resultDetailsMap.get("B"));
         assertEquals(Collections.singletonList("D"), resultDetailsMap.get("D"));
     }
 
     @Test
     public void testUpdateDetailsAppend() throws ZepException {
         Event newEvent = createUniqueEvent();
         Event.Builder builder = Event.newBuilder(newEvent).clearDetails();
         builder.addDetails(createDetail("A", "A"));
         builder.addDetails(createDetail("B", "B"));
         builder.addDetails(createDetail("C", "C"));
         newEvent = builder.build();
 
         EventSummary summary = createSummaryNew(newEvent);
         Event storedEvent = summary.getOccurrence(0);
         assertEquals(3, storedEvent.getDetailsCount());
 
         List<EventDetail> newDetailsList = new ArrayList<EventDetail>();
         newDetailsList.add(createDetail("A", EventDetailMergeBehavior.APPEND, "A1", "A2"));
         EventDetailSet newDetails = EventDetailSet.newBuilder().addAllDetails(newDetailsList).build();
 
         // ensure update works correctly
         assertEquals(1, eventSummaryDao.updateDetails(summary.getUuid(), newDetails));
 
         // verify new contents of details
         summary = eventSummaryDao.findByUuid(summary.getUuid());
 
         List<EventDetail> resultDetails = summary.getOccurrence(0).getDetailsList();
         assertEquals(3, resultDetails.size());
 
         Map<String, List<String>> resultDetailsMap = detailsToMap(resultDetails);
         assertEquals(Arrays.asList("A", "A1", "A2"), resultDetailsMap.get("A"));
         assertEquals(Collections.singletonList("B"), resultDetailsMap.get("B"));
         assertEquals(Collections.singletonList("C"), resultDetailsMap.get("C"));
     }
 
     @Test
     public void testUpdateDetailsUnique() throws ZepException {
         Event newEvent = createUniqueEvent();
         Event.Builder builder = Event.newBuilder(newEvent).clearDetails();
         builder.addDetails(createDetail("A", "A1", "A2", "A3"));
         builder.addDetails(createDetail("B", "B"));
         builder.addDetails(createDetail("C", "C"));
         newEvent = builder.build();
 
         EventSummary summary = createSummaryNew(newEvent);
         Event storedEvent = summary.getOccurrence(0);
         assertEquals(3, storedEvent.getDetailsCount());
 
         List<EventDetail> newDetailsList = new ArrayList<EventDetail>();
         newDetailsList.add(createDetail("A", EventDetailMergeBehavior.UNIQUE, "A4", "A1", "A5", "A2"));
         EventDetailSet newDetails = EventDetailSet.newBuilder().addAllDetails(newDetailsList).build();
 
         // ensure update works correctly
         assertEquals(1, eventSummaryDao.updateDetails(summary.getUuid(), newDetails));
 
         // verify new contents of details
         summary = eventSummaryDao.findByUuid(summary.getUuid());
 
         List<EventDetail> resultDetails = summary.getOccurrence(0).getDetailsList();
         assertEquals(3, resultDetails.size());
 
         Map<String, List<String>> resultDetailsMap = detailsToMap(resultDetails);
         assertEquals(Arrays.asList("A1", "A2", "A3", "A4", "A5"), resultDetailsMap.get("A"));
         assertEquals(Collections.singletonList("B"), resultDetailsMap.get("B"));
         assertEquals(Collections.singletonList("C"), resultDetailsMap.get("C"));
     }
 
     private Event createOldEvent(long duration, TimeUnit unit,
             EventSeverity severity) {
         Event.Builder eventBuilder = Event.newBuilder(createUniqueEvent());
         eventBuilder.setCreatedTime(System.currentTimeMillis()
                 - unit.toMillis(duration));
         eventBuilder.setSeverity(severity);
         return eventBuilder.build();
     }
 
     @Test
     public void testAgeEvents() throws ZepException {
         EventSummary warning = createSummaryNew(createOldEvent(5,
                 TimeUnit.MINUTES, EventSeverity.SEVERITY_WARNING));
         EventSummary error = createSummaryNew(createOldEvent(5,
                 TimeUnit.MINUTES, EventSeverity.SEVERITY_ERROR));
         EventSummary info = createSummaryNew(createOldEvent(5,
                 TimeUnit.MINUTES, EventSeverity.SEVERITY_INFO));
 
         /* Aging should have aged WARNING and INFO events and left ERROR */
         int numAged = eventSummaryDao.ageEvents(4, TimeUnit.MINUTES,
                 EventSeverity.SEVERITY_ERROR, 100);
         assertEquals(2, numAged);
         assertEquals(error, eventSummaryDao.findByUuid(error.getUuid()));
         /* Compare ignoring status change time */
         EventSummary summaryWarning = eventSummaryDao.findByUuid(warning
                 .getUuid());
         assertTrue(summaryWarning.getStatusChangeTime() > warning
                 .getStatusChangeTime());
         assertEquals(EventStatus.STATUS_AGED, summaryWarning.getStatus());
         EventSummary summaryInfo = eventSummaryDao.findByUuid(info.getUuid());
         assertTrue(summaryInfo.getStatusChangeTime() > info
                 .getStatusChangeTime());
         assertEquals(EventStatus.STATUS_AGED, summaryInfo.getStatus());
         compareSummary(summaryWarning, warning);
         compareSummary(summaryInfo, info);
 
         int archived = eventSummaryDao.archive(4, TimeUnit.MINUTES, 100);
         assertEquals(2, archived);
         assertNull(eventSummaryDao.findByUuid(warning.getUuid()));
         assertNull(eventSummaryDao.findByUuid(info.getUuid()));
         /* Compare ignoring status change time */
         EventSummary archivedWarning = eventArchiveDao.findByUuid(warning
                 .getUuid());
         assertTrue(archivedWarning.getStatusChangeTime() > warning
                 .getStatusChangeTime());
         EventSummary archivedInfo = eventArchiveDao.findByUuid(info.getUuid());
         assertTrue(archivedInfo.getStatusChangeTime() > info
                 .getStatusChangeTime());
         compareSummary(archivedWarning, summaryWarning);
         compareSummary(archivedInfo, summaryInfo);
     }
 
     @Test
     public void testArchiveEventsEmpty() throws ZepException {
         // Make sure we don't fail if there are no events to archive
         int numArchived = this.eventSummaryDao.archive(0L, TimeUnit.SECONDS,
                 500);
         assertEquals(0, numArchived);
     }
 
     @Test
     public void testClearEvents() throws ZepException {
         Event event = createUniqueEvent();
         EventSummary normalEvent = createSummaryNew(Event.newBuilder(event)
                 .setSeverity(EventSeverity.SEVERITY_WARNING)
                 .setEventKey("MyKey1").build());
         EventSummary clearEvent = createSummaryClear(
                 Event.newBuilder(createUniqueEvent())
                         .setSeverity(EventSeverity.SEVERITY_CLEAR)
                         .setActor(event.getActor()).setEventKey("MyKey1").build(),
                 Collections.singleton(normalEvent.getOccurrence(0)
                         .getEventClass()));
         assertEquals(EventStatus.STATUS_CLOSED, clearEvent.getStatus());
         EventSummary normalEventSummary = eventSummaryDao
                 .findByUuid(normalEvent.getUuid());
         assertEquals(EventStatus.STATUS_CLEARED, normalEventSummary.getStatus());
         assertEquals(clearEvent.getUuid(), normalEventSummary.getClearedByEventUuid());
         assertTrue(normalEventSummary.getStatusChangeTime() > normalEvent.getStatusChangeTime());
         compareSummary(normalEvent, normalEventSummary);
     }
 
     @Test
     public void testMergeDuplicateDetails() throws ZepException {
         String name = "dup1";
         String val1 = "dupval";
         String val2 = "dupval2";
         String val3 = "dupval3";
         Event.Builder eventBuilder = Event.newBuilder(createUniqueEvent())
                 .clearDetails();
         eventBuilder.addDetails(createDetail(name, val1));
         eventBuilder.addDetails(createDetail(name, val2, val3));
         Event event = eventBuilder.build();
         EventSummary summary = createSummaryNew(event);
         assertEquals(1, summary.getOccurrence(0).getDetailsCount());
         EventDetail detailFromDb = summary.getOccurrence(0).getDetails(0);
         assertEquals(name, detailFromDb.getName());
         assertEquals(Arrays.asList(val1, val2, val3),
                 detailFromDb.getValueList());
     }
 
     @Test
     public void testFilterDuplicateTags() throws ZepException {
         Event.Builder eventBuilder = Event.newBuilder(createUniqueEvent())
                 .clearTags();
         String uuid = UUID.randomUUID().toString();
         eventBuilder.addTags(EventTag.newBuilder()
                 .setType(ModelElementType.DEVICE.name()).setUuid(uuid).build());
         eventBuilder.addTags(EventTag.newBuilder()
                 .setType(ModelElementType.DEVICE.name()).setUuid(uuid).build());
         eventBuilder.addTags(EventTag.newBuilder()
                 .setType(ModelElementType.DEVICE.name()).setUuid(uuid).build());
         Event event = eventBuilder.build();
         EventSummary summary = createSummaryNew(event);
         int numFound = 0;
         for (EventTag tag : summary.getOccurrence(0).getTagsList()) {
             if (tag.getUuid().equals(uuid)) {
                 numFound++;
             }
         }
         assertEquals(1, numFound);
     }
 
     @Test
     public void testChangeSeverity() throws ZepException {
         // Verify that severity changes when new event with same fingerprint comes in with new severity
         Event.Builder firstBuilder = Event.newBuilder(createUniqueEvent());
         firstBuilder.setSeverity(EventSeverity.SEVERITY_WARNING);
         Event first = firstBuilder.build();
 
         EventSummary firstSummary = createSummaryNew(first);
 
         Event.Builder secondBuilder = Event.newBuilder(first);
         secondBuilder.setCreatedTime(first.getCreatedTime()+10);
         secondBuilder.setSeverity(EventSeverity.SEVERITY_INFO);
         Event second = secondBuilder.build();
 
         EventSummary secondSummary = createSummaryNew(second);
         assertEquals(firstSummary.getUuid(), secondSummary.getUuid());
         assertEquals(2, secondSummary.getCount());
         assertEquals(EventSeverity.SEVERITY_INFO, secondSummary.getOccurrence(0).getSeverity());
     }
 
     @Test
     public void testReidentifyDevice() throws ZepException {
         Event.Builder builder = Event.newBuilder(createUniqueEvent());
         EventActor.Builder actorBuilder = EventActor.newBuilder(builder.getActor());
         actorBuilder.clearElementSubIdentifier().clearElementSubTypeId().clearElementSubUuid();
         actorBuilder.clearElementUuid();
         builder.setActor(actorBuilder.build());
 
         EventSummary summary = createSummaryNew(builder.build());
         Event occurrence = summary.getOccurrence(0);
         assertFalse(occurrence.getActor().hasElementUuid());
 
         final String elementUuid = UUID.randomUUID().toString();
         int numRows = this.eventSummaryDao.reidentify(occurrence.getActor().getElementTypeId(),
                 occurrence.getActor().getElementIdentifier(), elementUuid, null);
         assertEquals(1, numRows);
         EventSummary summaryFromDb = this.eventSummaryDao.findByUuid(summary.getUuid());
         assertTrue(summaryFromDb.getUpdateTime() > summary.getUpdateTime());
         assertEquals(elementUuid, summaryFromDb.getOccurrence(0).getActor().getElementUuid());
     }
 
     @Test
     public void testReidentifyComponent() throws ZepException, NoSuchAlgorithmException, UnsupportedEncodingException {
         Event.Builder builder = Event.newBuilder(createUniqueEvent());
         EventActor.Builder actorBuilder = EventActor.newBuilder(builder.getActor());
         actorBuilder.clearElementSubUuid();
         builder.setActor(actorBuilder.build());
 
         EventSummary summary = createSummaryNew(builder.build());
         Event occurrence = summary.getOccurrence(0);
         EventActor actor = occurrence.getActor();
         assertFalse(occurrence.getActor().hasElementSubUuid());
 
         final String elementSubUuid = UUID.randomUUID().toString();
         int numRows = this.eventSummaryDao.reidentify(actor.getElementSubTypeId(),
                 actor.getElementSubIdentifier(), elementSubUuid, actor.getElementUuid());
         assertEquals(1, numRows);
         EventSummary summaryFromDb = this.eventSummaryDao.findByUuid(summary.getUuid());
         assertTrue(summaryFromDb.getUpdateTime() > summary.getUpdateTime());
         assertEquals(elementSubUuid, summaryFromDb.getOccurrence(0).getActor().getElementSubUuid());
         // Ensure clear_fingerprint_hash was updated
         String clearHashString = EventDaoUtils.join('|', elementSubUuid, occurrence.getEventClass(),
                 occurrence.getEventKey());
         byte[] clearHash = MessageDigest.getInstance("SHA-1").digest(clearHashString.getBytes("UTF-8"));
         Map<String,byte[]> fields = Collections.singletonMap(COLUMN_UUID, DaoUtils.uuidToBytes(summary.getUuid()));
         byte[] clearHashFromDb = this.simpleJdbcTemplate.query(
                 "SELECT clear_fingerprint_hash FROM event_summary WHERE uuid=:uuid",
                 new RowMapper<byte[]>() {
                     @Override
                     public byte[] mapRow(ResultSet rs, int rowNum) throws SQLException {
                         return rs.getBytes("clear_fingerprint_hash");
                     }
                 }, fields).get(0);
         assertArrayEquals(clearHash, clearHashFromDb);
     }
 
     @Test
     public void testDeidentifyDevice() throws ZepException {
         EventSummary summary = createSummaryNew(createUniqueEvent());
         Event occurrence = summary.getOccurrence(0);
         EventActor actor = occurrence.getActor();
         assertTrue(occurrence.getActor().hasElementUuid());
 
         int numRows = this.eventSummaryDao.deidentify(actor.getElementUuid());
         assertEquals(1, numRows);
         EventSummary summaryFromDb = this.eventSummaryDao.findByUuid(summary.getUuid());
         assertTrue(summaryFromDb.getUpdateTime() > summary.getUpdateTime());
         assertFalse(summaryFromDb.getOccurrence(0).getActor().hasElementUuid());
     }
 
     @Test
     public void testDeidentifyComponent() throws ZepException, NoSuchAlgorithmException, UnsupportedEncodingException {
         EventSummary summary = createSummaryNew(createUniqueEvent());
         Event occurrence = summary.getOccurrence(0);
         EventActor actor = occurrence.getActor();
         assertTrue(occurrence.getActor().hasElementSubUuid());
 
         int numRows = this.eventSummaryDao.deidentify(actor.getElementSubUuid());
         assertEquals(1, numRows);
         EventSummary summaryFromDb = this.eventSummaryDao.findByUuid(summary.getUuid());
         assertTrue(summaryFromDb.getUpdateTime() > summary.getUpdateTime());
         assertFalse(summaryFromDb.getOccurrence(0).getActor().hasElementSubUuid());
         // Ensure clear_fingerprint_hash was updated
         String clearHashString = EventDaoUtils.join('|', actor.getElementIdentifier(),
                 actor.getElementSubIdentifier(), occurrence.getEventClass(),
                 occurrence.getEventKey());
         byte[] clearHash = MessageDigest.getInstance("SHA-1").digest(clearHashString.getBytes("UTF-8"));
         Map<String,byte[]> fields = Collections.singletonMap(COLUMN_UUID, DaoUtils.uuidToBytes(summary.getUuid()));
         byte[] clearHashFromDb = this.simpleJdbcTemplate.query(
                 "SELECT clear_fingerprint_hash FROM event_summary WHERE uuid=:uuid",
                 new RowMapper<byte[]>() {
                     @Override
                     public byte[] mapRow(ResultSet rs, int rowNum) throws SQLException {
                         return rs.getBytes("clear_fingerprint_hash");
                     }
                 }, fields).get(0);
         assertArrayEquals(clearHash, clearHashFromDb);
     }
 
     private static Map<String,List<String>> detailsToMap(List<EventDetail> details) {
         Map<String,List<String>> detailsMap = new HashMap<String,List<String>>(details.size());
         for (EventDetail detail : details) {
             detailsMap.put(detail.getName(), detail.getValueList());
         }
         return detailsMap;
     }
 
     private static EventDetail createDetail(String name, String... values) {
         return createDetail(name, null, values);
     }
 
     private static EventDetail createDetail(String name, EventDetailMergeBehavior mergeBehavior, String... values) {
         EventDetail.Builder detailBuilder = EventDetail.newBuilder();
         detailBuilder.setName(name);
         if (mergeBehavior != null) {
             detailBuilder.setMergeBehavior(mergeBehavior);
         }
         for (String value : values) {
             detailBuilder.addValue(value);
         }
         return detailBuilder.build();
     }
 
     @Test
     public void testMergeDetailsReplace() throws ZepException {
         Event.Builder eventBuilder = Event.newBuilder(createUniqueEvent());
         eventBuilder.clearDetails();
         eventBuilder.addDetails(createDetail("foo", "bar", "baz"));
         eventBuilder.addDetails(createDetail("foo2", "bar2", "baz2"));
         final Event event = eventBuilder.build();
 
         EventSummary summary = createSummaryNew(event);
         compareEvents(event, summary.getOccurrence(0));
 
         /* Add a new detail, don't specify an old one, and replace an existing one */
         Event.Builder newEventBuilder = Event.newBuilder(event);
         newEventBuilder.clearDetails();
         /* Update foo */
         newEventBuilder.addDetails(createDetail("foo", "foobar", "foobaz"));
         /* Don't specify foo2 */
         /* Add a new detail foo3 */
         newEventBuilder.addDetails(createDetail("foo3", "foobar3", "foobaz3"));
         final Event newEvent = newEventBuilder.build();
 
         EventSummary newSummary = createSummaryNew(newEvent);
         assertEquals(2, newSummary.getCount());
         Map<String,List<String>> detailsMap = detailsToMap(newSummary.getOccurrence(0).getDetailsList());
         assertEquals(Arrays.asList("foobar","foobaz"), detailsMap.get("foo"));
         assertEquals(Arrays.asList("bar2", "baz2"), detailsMap.get("foo2"));
         assertEquals(Arrays.asList("foobar3", "foobaz3"), detailsMap.get("foo3"));
     }
 
     @Test
     public void testMergeDetailsAppend() throws ZepException {
         Event.Builder eventBuilder = Event.newBuilder(createUniqueEvent());
         eventBuilder.clearDetails();
         eventBuilder.addDetails(createDetail("foo", "bar", "baz"));
         eventBuilder.addDetails(createDetail("foo2", "bar2", "baz2"));
         final Event event = eventBuilder.build();
 
         EventSummary summary = createSummaryNew(event);
         compareEvents(event, summary.getOccurrence(0));
 
         /* Add a new detail, don't specify an old one, and replace an existing one */
         Event.Builder newEventBuilder = Event.newBuilder(event);
         newEventBuilder.clearDetails();
         /* Update foo */
         newEventBuilder.addDetails(createDetail("foo", EventDetailMergeBehavior.APPEND, "bar", "foobar", "foobaz"));
         /* Don't specify foo2 */
         /* Add a new detail foo3 */
         newEventBuilder.addDetails(createDetail("foo3", "foobar3", "foobaz3"));
         final Event newEvent = newEventBuilder.build();
 
         EventSummary newSummary = createSummaryNew(newEvent);
         assertEquals(2, newSummary.getCount());
         Map<String,List<String>> detailsMap = detailsToMap(newSummary.getOccurrence(0).getDetailsList());
         assertEquals(Arrays.asList("bar", "baz", "bar", "foobar","foobaz"), detailsMap.get("foo"));
         assertEquals(Arrays.asList("bar2", "baz2"), detailsMap.get("foo2"));
         assertEquals(Arrays.asList("foobar3", "foobaz3"), detailsMap.get("foo3"));
     }
 
     @Test
     public void testMergeDetailsUnique() throws ZepException {
         Event.Builder eventBuilder = Event.newBuilder(createUniqueEvent());
         eventBuilder.clearDetails();
         eventBuilder.addDetails(createDetail("foo", "bar", "baz"));
         eventBuilder.addDetails(createDetail("foo2", "bar2", "baz2"));
         final Event event = eventBuilder.build();
 
         EventSummary summary = createSummaryNew(event);
         compareEvents(event, summary.getOccurrence(0));
 
         /* Add a new detail, don't specify an old one, and replace an existing one */
         Event.Builder newEventBuilder = Event.newBuilder(event);
         newEventBuilder.clearDetails();
         /* Update foo */
         newEventBuilder.addDetails(createDetail("foo", EventDetailMergeBehavior.UNIQUE, "baz", "foobar", "foobaz"));
         /* Don't specify foo2 */
         /* Add a new detail foo3 */
         newEventBuilder.addDetails(createDetail("foo3", "foobar3", "foobaz3"));
         final Event newEvent = newEventBuilder.build();
 
         EventSummary newSummary = createSummaryNew(newEvent);
         assertEquals(2, newSummary.getCount());
         Map<String,List<String>> detailsMap = detailsToMap(newSummary.getOccurrence(0).getDetailsList());
         assertEquals(Arrays.asList("bar", "baz", "foobar","foobaz"), detailsMap.get("foo"));
         assertEquals(Arrays.asList("bar2", "baz2"), detailsMap.get("foo2"));
         assertEquals(Arrays.asList("foobar3", "foobaz3"), detailsMap.get("foo3"));
     }
 
     @Test
     public void testArchive() throws ZepException {
         EventSummary summary1 = createSummaryNew(createUniqueEvent());
         EventSummary summary2 = createSummaryNew(createUniqueEvent());
         EventSummary summary3 = createSummaryNew(createUniqueEvent());
 
         List<String> uuids = Arrays.asList(summary1.getUuid(), summary2.getUuid(), summary3.getUuid());
         int numArchived = this.eventSummaryDao.archive(uuids);
         assertEquals(uuids.size(), numArchived);
         assertEquals(0, this.eventSummaryDao.findByUuids(uuids).size());
         assertEquals(uuids.size(), this.eventArchiveDao.findByUuids(uuids).size());
     }
 }
