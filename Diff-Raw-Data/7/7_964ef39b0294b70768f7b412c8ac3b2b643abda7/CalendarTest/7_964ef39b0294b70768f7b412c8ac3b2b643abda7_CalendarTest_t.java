 /*
  * See the NOTICE file distributed with this work for additional
  * information regarding copyright ownership.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package com.celements.calendar;
 
 import static org.easymock.EasyMock.*;
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
import java.util.GregorianCalendar;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.xwiki.component.descriptor.DefaultComponentDescriptor;
 import org.xwiki.model.reference.DocumentReference;
 import org.xwiki.model.reference.SpaceReference;
 import org.xwiki.model.reference.WikiReference;
 
 import com.celements.calendar.api.EventApi;
 import com.celements.calendar.engine.CalendarEngineHQL;
 import com.celements.calendar.engine.CalendarEngineLucene;
 import com.celements.calendar.engine.ICalendarEngineRole;
 import com.celements.calendar.manager.IEventManager;
 import com.celements.calendar.search.DefaultEventSearchQuery;
 import com.celements.calendar.search.EventSearchResult;
 import com.celements.calendar.search.IEventSearchQuery;
 import com.celements.calendar.service.ICalendarService;
 import com.celements.common.test.AbstractBridgedComponentTestCase;
 import com.xpn.xwiki.XWiki;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.doc.XWikiDocument;
 import com.xpn.xwiki.web.Utils;
 
 public class CalendarTest extends AbstractBridgedComponentTestCase{
 
   private boolean isArchiv = false;
   private Calendar cal;
   private ArrayList<EventApi> eventList;
   private XWikiContext context;
   private IEventManager eventMgrMock;
   private ICalendarService calServiceMock;
   private DocumentReference calDocRef;
   private XWiki xwiki;
 
   @Before
   public void setUp_CalendarTest() throws Exception {
     eventList = new ArrayList<EventApi>();
     context = getContext();
     xwiki = createMockAndAddToDefault(XWiki.class);
     context.setWiki(xwiki);
     calDocRef = new DocumentReference("myWiki", "MyCalSpace", "MyCalDoc");
     eventMgrMock = createMockAndAddToDefault(IEventManager.class);
     calServiceMock = createMockAndAddToDefault(ICalendarService.class);
     cal = getInjectedCal(calDocRef, isArchiv);
   }
 
   @Test
   @Deprecated
   public void testNewCalendar_deprecated_constructor() {
     DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
         "myCalDoc");
     XWikiDocument calDoc = new XWikiDocument(calDocRef);
     cal = new Calendar(calDoc, isArchiv, context);
     assertEquals(calDocRef, cal.getDocumentReference());
   }
 
   @Test
   public void testNewCalendar() {
     DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
         "myCalDoc");
     cal = new Calendar(calDocRef, isArchiv);
     assertEquals(calDocRef, cal.getDocumentReference());
     assertEquals(isArchiv, cal.isArchive());
   }
 
   @Test
   public void testGetAllEvents_informationHidingSecurity() throws XWikiException {
     List<EventApi> list = Collections.emptyList();
     expect(eventMgrMock.getEvents(same(cal), eq(0), eq(0))).andReturn(list).once();
     replayDefault();
     List<EventApi> events = cal.getAllEvents();
     verifyDefault();
     assertNotSame("getAllEvents may not leak internal data references.",
         eventList, events);
   }
 
   @Test
   public void testGetAllEvents() throws XWikiException {
     ArrayList<EventApi> eventList = new ArrayList<EventApi>();
     IEvent event = createMockAndAddToDefault(IEvent.class);
     event.setLanguage(eq("de"));
     expectLastCall().once();
     IEvent event2 = createMockAndAddToDefault(IEvent.class);
     event2.setLanguage(eq("de"));
     expectLastCall().once();
     IEvent event3 = createMockAndAddToDefault(IEvent.class);
     event3.setLanguage(eq("de"));
     expectLastCall().once();
     IEvent event4 = createMockAndAddToDefault(IEvent.class);
     event4.setLanguage(eq("de"));
     expectLastCall().once();
     DocumentReference cal2DocRef = new DocumentReference(context.getDatabase(),
         "MyCalDoc2Space", "MyCal2Doc");
     Calendar cal2 = getInjectedCal(cal2DocRef, isArchiv);
     expect(eventMgrMock.getEvents(same(cal2), eq(0), eq(0))).andReturn(eventList);
     replayDefault();
     eventList.add(new EventApi(event, context));
     eventList.add(new EventApi(event2, context));
     eventList.add(new EventApi(event3, context));
     eventList.add(new EventApi(event4, context));
     List<EventApi> events = cal2.getAllEvents();
     verifyDefault();
     assertEquals("expecting complete eventList", eventList, events);
   }
 
   @Test
   public void testGetEvents_overEnd() throws XWikiException {
     ArrayList<EventApi> eventList = new ArrayList<EventApi>();
     IEvent event = createMockAndAddToDefault(IEvent.class);
     event.setLanguage(eq("de"));
     expectLastCall().once();
     DocumentReference cal2DocRef = new DocumentReference(context.getDatabase(),
         "MyCalDoc2Space", "MyCal2Doc");
     Calendar cal2 = getInjectedCal(cal2DocRef, isArchiv);
     expect(eventMgrMock.getEvents(same(cal2), eq(0), eq(10))).andReturn(eventList);
     replayDefault();
     eventList.add(new EventApi(event, context));
     int start = 0;
     int nb = 10;
     List<EventApi> events = cal2.getEvents(start, nb);
     verifyDefault();
     assertEquals("Expecting to get the full eventlist", eventList, events);
   }
 
   @Test
   public void testGetEvents_illegalStartValue() throws XWikiException {
     ArrayList<EventApi> eventList = new ArrayList<EventApi>();
     IEvent event = createMockAndAddToDefault(IEvent.class);
     event.setLanguage(eq("de"));
     expectLastCall().once();
     DocumentReference cal2DocRef = new DocumentReference(context.getDatabase(),
         "MyCalDoc2Space", "MyCal2Doc");
     Calendar cal2 = getInjectedCal(cal2DocRef, isArchiv);
     expect(eventMgrMock.getEvents(same(cal2), eq(5), eq(1))).andReturn(eventList);
     replayDefault();
     eventList.add(new EventApi(event, context));
     int start = 5;
     int nb = 1;
     List<EventApi> events = cal2.getEvents(start, nb);
     verifyDefault();
     assertEquals("Expecting to get the full eventlist", eventList, events);
   }
 
   @Test
   public void testGetEvents_minusOneStart() throws XWikiException {
     ArrayList<EventApi> eventList = new ArrayList<EventApi>();
     IEvent event = createMockAndAddToDefault(IEvent.class);
     event.setLanguage(eq("de"));
     expectLastCall().once();
     DocumentReference cal2DocRef = new DocumentReference(context.getDatabase(),
         "MyCalDoc2Space", "MyCal2Doc");
     Calendar cal2 = getInjectedCal(cal2DocRef, isArchiv);
     expect(eventMgrMock.getEvents(same(cal2), eq(0), eq(1))).andReturn(eventList);
     replayDefault();
     eventList.add(new EventApi(event, context));
     int start = -1;
     int nb = 1;
     List<EventApi> events = cal2.getEvents(start, nb);
     verifyDefault(event);
     assertEquals("Expecting to get the full eventlist", eventList, events);
   }
   
   @Test
   public void testSearchEvents() {
     IEventSearchQuery query = new DefaultEventSearchQuery(getContext().getDatabase());
     EventSearchResult result = createMockAndAddToDefault(EventSearchResult.class);
     
     expect(eventMgrMock.searchEvents(same(cal), same(query))).andReturn(result).once();
     
     replayDefault();
     EventSearchResult ret = cal.searchEvents(query);
     verifyDefault();
     
     assertSame(result, ret);
   }
 
   @Test
   public void testGetNrOfEvents() throws XWikiException {
     long count = 123L;
     expect(eventMgrMock.countEvents(eq(cal))).andReturn(count).once();
     replayDefault();
     long ret = cal.getNrOfEvents();
     verifyDefault();
     assertEquals(count, ret);
   }
 
   @Test
   public void testIsEmpty_true() throws XWikiException {
     long count = 0L;
     expect(eventMgrMock.countEvents(eq(cal))).andReturn(count).once();
     replayDefault();
     boolean ret = cal.isEmpty();
     verifyDefault();
     assertTrue(ret);
   }
 
   @Test
   public void testIsEmpty_false() throws XWikiException {
     long count = 123L;
     expect(eventMgrMock.countEvents(eq(cal))).andReturn(count).once();
     replayDefault();
     boolean ret = cal.isEmpty();
     verifyDefault();
     assertFalse(ret);
   }
 
   @Test
   public void testIsArchive() {
     assertEquals("isArchive must return the isArchive value given in "
         + "the constructor call", isArchiv, cal.isArchive());
   }
 
   @Test
   public void testGetDocumentReference() {
     assertEquals(calDocRef, cal.getDocumentReference());
   }
 
   @Test
   public void testGetStartDate() {
    java.util.Calendar calendar = GregorianCalendar.getInstance();
    calendar.add(java.util.Calendar.SECOND, -5);
    Date startDateBefore = calendar.getTime();
     ICalendar cal = new Calendar(calDocRef, isArchiv);
     Date startDateAfter = new Date();
     assertTrue(startDateBefore.compareTo(cal.getStartDate()) <= 0);
     assertTrue(startDateAfter.compareTo(cal.getStartDate()) >= 0);
   }
 
   @Test
   public void testSetTimestamp() {
     ICalendar cal = new Calendar(calDocRef, isArchiv);
     Date startDate = cal.getStartDate();
     Date newStartDate = new Date();
     cal.setStartTimestamp(newStartDate);
     assertNotNull(cal.getStartDate());
     assertNotSame(startDate, cal.getStartDate());
     assertEquals(newStartDate, cal.getStartDate());
   }
 
   @Test
   public void testSetStartDate() {
     ICalendar cal = new Calendar(calDocRef, isArchiv);
     Date startDate = cal.getStartDate();
     Date newStartDate = new Date();
     cal.setStartDate(newStartDate);
     assertNotNull(cal.getStartDate());
     assertNotSame(startDate, cal.getStartDate());
     assertEquals(getCalService().getMidnightDate(newStartDate), cal.getStartDate());
   }
 
   @Test
   public void testSetStartDate_null() {
     ICalendar cal = new Calendar(calDocRef, isArchiv);
     Date startDate = cal.getStartDate();
     cal.setStartDate(null);
     assertNotNull(cal.getStartDate());
     assertSame(startDate, cal.getStartDate());
   }
   
   @Test
   public void testGetLanguage() throws Exception {
     String lang = "de";
     cal.setLanguage(lang);
     
     replayDefault();
     String ret = cal.getLanguage();
     verifyDefault();
     
     assertEquals(lang, ret);
   }
   
   @Test
   public void testGetLanguage_default() throws Exception {
     String lang = "de";
     SpaceReference spaceRef = new SpaceReference("evSpace", new WikiReference("db"));
     expect(calServiceMock.getEventSpaceRefForCalendar(eq(calDocRef))).andReturn(
         spaceRef).once();
     expect(xwiki.getSpacePreference(eq("default_language"), eq(spaceRef.getName()), 
         eq(""), same(getContext()))).andReturn(lang).once();
     
     replayDefault();
     String ret = cal.getLanguage();
     verifyDefault();
     
     assertEquals(lang, ret);
   }
   
   @Test
   public void testGetEventSpaceRef() throws Exception {
     SpaceReference spaceRef = new SpaceReference("evSpace", new WikiReference("db"));
     expect(calServiceMock.getEventSpaceRefForCalendar(eq(calDocRef))).andReturn(spaceRef
         ).once();
     
     replayDefault();
     SpaceReference ret = cal.getEventSpaceRef();
     verifyDefault();
     
     assertSame(spaceRef, ret);
   }
 
   @Test
   public void testGetAllowedSpaces() throws Exception {
     String eventSpace1 = "calEventSpace1";
     String eventSpace2 = "calEventSpace2";
     expect(calServiceMock.getAllowedSpaces(eq(calDocRef))).andReturn(Arrays.asList(
         new String[] {eventSpace1, eventSpace2}));
     replayDefault();
     List<String> allowedSpaces = cal.getAllowedSpaces();
     assertEquals(2, allowedSpaces.size());
     assertEquals(eventSpace1, allowedSpaces.get(0));
     assertEquals(eventSpace2, allowedSpaces.get(1));
     verifyDefault();
   }
 
   @Test
   public void testGetEngine_HQL() throws Exception {
     expect(getContext().getWiki().getXWikiPreference(eq("calendar_engine"), 
         eq("calendar.engine"), eq("default"), same(getContext()))).andReturn("default"
         ).once();
     replayDefault();
     ICalendarEngineRole ret = cal.getEngine();
     verifyDefault();
     assertTrue(ret instanceof CalendarEngineHQL);
   }
   
   @Test
   public void testGetEngine_Lucene() throws Exception {
     String hint = CalendarEngineLucene.NAME;
     expect(getContext().getWiki().getXWikiPreference(eq("calendar_engine"), 
         eq("calendar.engine"), eq("default"), same(getContext()))).andReturn(hint).once();
     
     ICalendarEngineRole engineMock = createMockAndAddToDefault(ICalendarEngineRole.class);
     DefaultComponentDescriptor<ICalendarEngineRole> descriptor = 
         new DefaultComponentDescriptor<ICalendarEngineRole>();
     descriptor.setRole(ICalendarEngineRole.class);
     descriptor.setRoleHint(hint);
     Utils.getComponentManager().registerComponent(descriptor, engineMock);
     expect(engineMock.getName()).andReturn(hint).anyTimes();
     expect(engineMock.getEngineLimit()).andReturn(10L).once();
     expect(engineMock.countEvents(same(cal))).andReturn(5L).once();
     
     replayDefault();
     ICalendarEngineRole ret = cal.getEngine();
     verifyDefault();
     assertSame(engineMock, ret);
   }
   
   @Test
   public void testGetEngine_Lucene_overLimit() throws Exception {
     String hint = CalendarEngineLucene.NAME;
     expect(getContext().getWiki().getXWikiPreference(eq("calendar_engine"), 
         eq("calendar.engine"), eq("default"), same(getContext()))).andReturn(hint).once();
     
     ICalendarEngineRole engineMock = createMockAndAddToDefault(ICalendarEngineRole.class);
     DefaultComponentDescriptor<ICalendarEngineRole> descriptor = 
         new DefaultComponentDescriptor<ICalendarEngineRole>();
     descriptor.setRole(ICalendarEngineRole.class);
     descriptor.setRoleHint(hint);
     Utils.getComponentManager().registerComponent(descriptor, engineMock);
     expect(engineMock.getName()).andReturn(hint).anyTimes();
     expect(engineMock.getEngineLimit()).andReturn(10L).once();
     expect(engineMock.countEvents(same(cal))).andReturn(11L).once();
     
     replayDefault();
     ICalendarEngineRole ret = cal.getEngine();
     verifyDefault();
     assertTrue(ret instanceof CalendarEngineHQL);
   }
   
   private Calendar getInjectedCal(DocumentReference docRef, boolean isArchive) {
     Calendar cal = new Calendar(calDocRef, isArchiv);
     cal.injectEventManager(eventMgrMock);
     cal.injectCalService(calServiceMock);
     return cal;
   }
 
   private ICalendarService getCalService() {
     return Utils.getComponent(ICalendarService.class);
   }
 
 }
