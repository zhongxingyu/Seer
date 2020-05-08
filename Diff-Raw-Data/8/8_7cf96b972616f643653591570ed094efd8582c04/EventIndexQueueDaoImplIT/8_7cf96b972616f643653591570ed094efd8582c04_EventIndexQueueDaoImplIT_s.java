 /*
  * Copyright (C) 2011, Zenoss Inc.  All Rights Reserved.
  */
 package org.zenoss.zep.dao.impl;
 
 import org.junit.Ignore;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
 import org.zenoss.protobufs.zep.Zep.Event;
 import org.zenoss.protobufs.zep.Zep.EventNote;
 import org.zenoss.protobufs.zep.Zep.EventStatus;
 import org.zenoss.protobufs.zep.Zep.EventSummary;
 import org.zenoss.zep.ZepException;
 import org.zenoss.zep.dao.EventArchiveDao;
 import org.zenoss.zep.dao.EventIndexHandler;
 import org.zenoss.zep.dao.EventIndexQueueDao;
 import org.zenoss.zep.dao.EventSummaryBaseDao;
 import org.zenoss.zep.dao.EventSummaryDao;
 import org.zenoss.zep.dao.impl.compat.DatabaseCompatibility;
 import org.zenoss.zep.dao.impl.compat.TypeConverter;
 import org.zenoss.zep.impl.EventPreCreateContextImpl;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import static org.junit.Assert.*;
 
 /**
  * Integration tests for EventIndexQueueDao.
  */
 @ContextConfiguration({ "classpath:zep-config.xml" })
 public class EventIndexQueueDaoImplIT extends AbstractTransactionalJUnit4SpringContextTests {
     @Autowired
     public EventSummaryDao eventSummaryDao;
 
     @Autowired
     public EventArchiveDao eventArchiveDao;
 
     @Autowired
     @Qualifier("summary")
     public EventIndexQueueDao eventSummaryIndexQueueDao;
 
     @Autowired
     @Qualifier("archive")
     public EventIndexQueueDao eventArchiveIndexQueueDao;
 
     @Autowired
     public DatabaseCompatibility databaseCompatibility;
 
     private EventSummary create(EventSummaryBaseDao eventSummaryBaseDao, boolean archive) throws ZepException {
         Event event = EventTestUtils.createSampleEvent();
         if (archive) {
             event = Event.newBuilder(event).setStatus(EventStatus.STATUS_CLOSED).build();
         }
         String uuid = eventSummaryBaseDao.create(event, new EventPreCreateContextImpl());
         return eventSummaryBaseDao.findByUuid(uuid);
     }
 
     private static class TestEventIndexHandler implements EventIndexHandler {
         private List<EventSummary> indexed = new ArrayList<EventSummary>();
         private List<String> deleted = new ArrayList<String>();
         private AtomicBoolean completed = new AtomicBoolean(false);
 
         @Override
         public void handle(EventSummary event) throws Exception {
             this.indexed.add(event);
         }
 
         @Override
         public void handleDeleted(String uuid) throws Exception {
             this.deleted.add(uuid);
         }
 
         @Override
         public void handleComplete() throws Exception {
             if (!completed.compareAndSet(false, true)) {
                 throw new IllegalStateException("handleComplete should only be called once");
             }
         }
     }
 
     private EventSummary testCreate(EventSummaryBaseDao eventSummaryBaseDao, EventIndexQueueDao eventIndexQueueDao,
                             boolean archive) throws ZepException {
         EventSummary eventSummary = create(eventSummaryBaseDao, archive);
         TestEventIndexHandler handler = new TestEventIndexHandler();
         int numEvents = eventIndexQueueDao.indexEvents(handler, 1000);
         assertEquals(1, numEvents);
         assertTrue(handler.completed.get());
         assertEquals(1, handler.indexed.size());
         assertEquals(eventSummary, handler.indexed.get(0));
         assertTrue(handler.deleted.isEmpty());
 
         /* Run it again and verify that the event has been deleted */
         handler = new TestEventIndexHandler();
         assertEquals(0, eventIndexQueueDao.indexEvents(handler, 1000));
         assertFalse(handler.completed.get());
         assertTrue(handler.indexed.isEmpty());
         assertTrue(handler.deleted.isEmpty());
         return eventSummary;
     }
 
     @Test
     public void testIndexCreatedEvents()  throws ZepException {
         testCreate(eventSummaryDao, eventSummaryIndexQueueDao, false);
         testCreate(eventArchiveDao, eventArchiveIndexQueueDao, true);
     }
 
     private void testModify(EventSummaryBaseDao eventSummaryBaseDao, EventIndexQueueDao eventIndexQueueDao,
                             boolean archive) throws ZepException {
         EventSummary summary = testCreate(eventSummaryBaseDao, eventIndexQueueDao, archive);
         EventNote note = EventNote.newBuilder().setCreatedTime(System.currentTimeMillis())
                 .setMessage("My note").setUserName("pkw").setUserUuid(UUID.randomUUID().toString())
                 .setUuid(UUID.randomUUID().toString()).build();
 
         eventSummaryBaseDao.addNote(summary.getUuid(), note);
         EventSummary summaryWithNote = eventSummaryBaseDao.findByUuid(summary.getUuid());
 
         TestEventIndexHandler handler = new TestEventIndexHandler();
         int numEvents = eventIndexQueueDao.indexEvents(handler, 1000);
         assertEquals(1, numEvents);
         assertTrue(handler.completed.get());
         assertEquals(1, handler.indexed.size());
         assertEquals(summaryWithNote, handler.indexed.get(0));
         assertTrue(handler.deleted.isEmpty());
 
         /* Run it again and verify that the event has been deleted */
         handler = new TestEventIndexHandler();
         assertEquals(0, eventIndexQueueDao.indexEvents(handler, 1000));
         assertFalse(handler.completed.get());
         assertTrue(handler.indexed.isEmpty());
         assertTrue(handler.deleted.isEmpty());
     }
 
     @Test
     public void testIndexModifiedEvents() throws ZepException {
         testModify(eventSummaryDao, eventSummaryIndexQueueDao, false);
         testModify(eventArchiveDao, eventArchiveIndexQueueDao, true);
     }
 
     private void testDelete(EventSummaryBaseDao eventSummaryBaseDao, EventIndexQueueDao eventIndexQueueDao,
                             boolean archive) throws ZepException {
         TypeConverter<String> uuidConverter = databaseCompatibility.getUUIDConverter();
         EventSummary summary = testCreate(eventSummaryBaseDao, eventIndexQueueDao, archive);
 
         String tableName = (archive) ? EventConstants.TABLE_EVENT_ARCHIVE : EventConstants.TABLE_EVENT_SUMMARY;
         int numDeleted = this.simpleJdbcTemplate.update("DELETE FROM " + tableName + " WHERE uuid=?",
                 uuidConverter.toDatabaseType(summary.getUuid()));
         assertEquals(1, numDeleted);
 
         TestEventIndexHandler handler = new TestEventIndexHandler();
         int numEvents = eventIndexQueueDao.indexEvents(handler, 1000);
         assertEquals(1, numEvents);
         assertTrue(handler.completed.get());
         assertTrue(handler.indexed.isEmpty());
         assertEquals(1, handler.deleted.size());
         assertEquals(summary.getUuid(), handler.deleted.get(0));
 
         /* Run it again and verify that the event has been deleted */
         handler = new TestEventIndexHandler();
         assertEquals(0, eventIndexQueueDao.indexEvents(handler, 1000));
         assertFalse(handler.completed.get());
         assertTrue(handler.indexed.isEmpty());
         assertTrue(handler.deleted.isEmpty());
     }
 
     @Test
     public void testIndexDeletedEvents() throws ZepException {
         testDelete(eventSummaryDao, eventSummaryIndexQueueDao, false);
         testDelete(eventArchiveDao, eventArchiveIndexQueueDao, true);
     }
 }
