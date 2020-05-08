 package com.celements.calendar.navigation.factories;
 
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.xwiki.model.reference.DocumentReference;
 
 import com.celements.calendar.Calendar;
 import com.celements.calendar.ICalendar;
 import com.celements.calendar.IEvent;
 import com.celements.calendar.manager.IEventManager;
 import com.celements.calendar.search.EventSearchQuery;
 import com.celements.calendar.search.EventSearchResult;
 import com.xpn.xwiki.web.Utils;
 
 public class CalendarNavigationFactory implements ICalendarNavigationFactory {
 
   private static final Log LOGGER = LogFactory.getFactory().getInstance(
       CalendarNavigationFactory.class);
 
   private static final Date DATE_LOW = new Date(-62135773200000L);
   private static final Date DATE_HIGH = new Date(253402297140000L);
 
   private IEventManager eventMgr;
 
   private INavigationDetailsFactory navDetailsFactory;
 
   public CalendarNavigation getCalendarNavigation(DocumentReference calDocRef,
       NavigationDetails navDetails, int nb) {
     ICalendar cal = getCalendar(calDocRef, false, navDetails.getStartDate());
     NavigationDetails startNavDetails = null;
     NavigationDetails endNavDetails = null;
     try {
       startNavDetails = getStartNavDetails(calDocRef);
       endNavDetails = getEndNavDetails(calDocRef, nb);
       if (isInvalidNavDetails(navDetails, cal)) {
         LOGGER.debug("isInvalidNavDetails true for '" + navDetails + "'");
         navDetails = endNavDetails;
         cal = getCalendar(calDocRef, false, navDetails.getStartDate());
       } else {
         LOGGER.debug("isInvalidNavDetails false for '" + navDetails + "'");
       }
     } catch (EmptyCalendarListException emptyListExp) {
       LOGGER.warn("getCalendarNavigation encountered emptyListExp for calDocRef ["
           + calDocRef + "] and nb [" + nb + "] cal.getNrOfEvents [" + cal.getNrOfEvents()
           + "].", emptyListExp);
     }
     ICalendar calArchive = getCalendar(calDocRef, true, navDetails.getStartDate());
     UncertainCount[] counts = getCounts((int) getEventMgr().countEvents(cal),
         (int) getEventMgr().countEvents(calArchive), navDetails.getOffset(), nb, false);
 
     CalendarNavigation calendarNavigation = new CalendarNavigation(
         counts[0], counts[1], counts[2], navDetails, startNavDetails, endNavDetails,
         getPrevNavDetails(cal, calArchive, navDetails, nb),
         getNextNavDetails(cal, navDetails, nb));
     LOGGER.debug("getCalendarNavigation: return '" + calendarNavigation + "' for cal '"
         + calDocRef + "' and navDetails '" + navDetails + "'");
     return calendarNavigation;
   }
 
   private boolean isInvalidNavDetails(NavigationDetails navDetails, ICalendar cal) {
     return (getEventMgr().getEventsInternal(cal, navDetails.getOffset(), 1).size() == 0)
         && (getEventMgr().countEvents(getCalendar(cal.getDocumentReference(), false,
             DATE_LOW)) > 0);
   }
 
   NavigationDetails getStartNavDetails(DocumentReference calDocRef
       ) throws EmptyCalendarListException {
     LOGGER.debug("getStartNavDetails for calDocRef [" + calDocRef + "].");
     ICalendar calAll = getCalendar(calDocRef, false, DATE_LOW);
     if (getEventMgr().countEvents(calAll) > 0) {
       return new NavigationDetails(getEventMgr().getFirstEvent(calAll).getEventDate(), 0);
     }
     throw new EmptyCalendarListException("getStartNavDetails failes on empty calendar ["
         + calDocRef + "].");
   }
 
   NavigationDetails getEndNavDetails(DocumentReference calDocRef, int nb
       ) throws EmptyCalendarListException {
     LOGGER.debug("getEndNavDetails for calDocRef [" + calDocRef + "] and nb [" + nb
         + "].");
     ICalendar calAll = getCalendar(calDocRef, false, DATE_LOW);
     int countAll = (int) getEventMgr().countEvents(calAll);
     if (countAll > 0) {
      int endOffset = countAll - nb;
       return getFirstNavDetails(calAll, endOffset > 0 ? endOffset : 0);
     }
     throw new EmptyCalendarListException("getEndNavDetails failes on empty calendar" +
     		" part [" + calDocRef + "] nb [" + nb + "].");
   }
 
   private NavigationDetails getPrevNavDetails(ICalendar cal, ICalendar calArchive,
       NavigationDetails navDetails, int nb) {
     NavigationDetails prevNavDetails = null;
     int prevOffset = navDetails.getOffset() - nb;
     LOGGER.debug("getPrevNavDetails for calDocRef [" + cal.getDocumentReference()
         + "] and nb [" + nb + "] prevOffset [" + prevOffset + "].");
     try {
       if ((prevOffset >= 0) && (getEventMgr().countEvents(cal) > 0)) {
         prevNavDetails = getFirstNavDetails(cal, prevOffset);
       } else if ((prevOffset < 0) && (getEventMgr().countEvents(calArchive) > 0)) {
         prevNavDetails = getLastNavDetails(calArchive, prevOffset);
       }
     } catch (EmptyCalendarListException emptyListExp) {
       LOGGER.warn("getPrevNavDetails encountered emptyListExp for cal ["
           + cal.getDocumentReference() + "] and nb [" + nb + "] prevOffset [" + prevOffset
           + "].", emptyListExp);
     }
     return prevNavDetails;
   }
 
   private NavigationDetails getNextNavDetails(ICalendar cal, NavigationDetails navDetails,
       int nb) {
     NavigationDetails nextNavDetails = navDetails;
     int nextOffset = navDetails.getOffset() + nb;
     LOGGER.debug("getNextNavDetails for calDocRef [" + cal.getDocumentReference()
         + "] and nb [" + nb + "] nextOffset [" + nextOffset + "].");
     try {
       if (getEventMgr().countEvents(cal) > nextOffset) {
         nextNavDetails = getFirstNavDetails(cal, nextOffset);
       }
     } catch (EmptyCalendarListException emptyListExp) {
       LOGGER.warn("getNextNavDetails encountered emptyListExp for calDocRef ["
           + cal.getDocumentReference() + "] and nb [" + nb + "] nextOffset [" + nextOffset
           + "].", emptyListExp);
     }
     return nextNavDetails;
   }
 
   private NavigationDetails getFirstNavDetails(ICalendar cal, int offset
       ) throws EmptyCalendarListException {
     LOGGER.debug("getFirstNavDetails for cal [" + cal.getDocumentReference()
         + "] and offset [" + offset + "].");
     IEvent firstEvent = getFirstElement(getEventMgr().getEventsInternal(cal, offset, 1));
     return getNavDetailsFactory().getNavigationDetails(cal.getDocumentReference(),
         firstEvent);
   }
 
   private NavigationDetails getLastNavDetails(ICalendar cal, int offset
       ) throws EmptyCalendarListException {
     LOGGER.debug("getLastNavDetails for cal [" + cal.getDocumentReference()
         + "] and offset [" + offset + "].");
     offset = Math.abs(offset);
     IEvent lastEvent = getLastElement(getEventMgr().getEventsInternal(cal, 0, offset));
     return getNavDetailsFactory().getNavigationDetails(cal.getDocumentReference(),
         lastEvent);
   }
 
   public CalendarNavigation getCalendarNavigation(DocumentReference calDocRef,
       NavigationDetails navDetails, int nb, EventSearchQuery query) {
     EventSearchResult calAllResult = getCalendar(calDocRef, false, DATE_LOW
         ).searchEvents(query);
     EventSearchResult calResult = getCalendar(calDocRef, false, navDetails.getStartDate()
         ).searchEvents(query);
     NavigationDetails startNavDetails = null;
     NavigationDetails endNavDetails = null;
     try {
       startNavDetails = getStartNavDetails(calAllResult);
       endNavDetails = getEndNavDetails(calDocRef, nb, query);
       int check = checkInvalidNavDetails(navDetails, query.getFromDate(), calResult,
           calAllResult);
       LOGGER.debug("checkInvalidNavDetails is '" + check + "' for '" + navDetails + "'");
       if (check != 0) {
         navDetails = check > 0 ? endNavDetails : startNavDetails;
         calResult = getCalendar(calDocRef, false, navDetails.getStartDate()).searchEvents(
             query);
       }
     } catch (EmptyCalendarListException emptyListExp) {
       LOGGER.warn("getCalendarNavigation encountered emptyListExp for calDocRef ["
           + calDocRef + "] and nb [" + nb + "] calResult.getSize [" + calResult.getSize()
           + "].", emptyListExp);
     }
     EventSearchResult calArchiveResult = getCalendar(calDocRef, true,
         navDetails.getStartDate()).searchEvents(query);
     UncertainCount[] counts = getCounts(calResult.getSize(), calArchiveResult.getSize(),
         navDetails.getOffset(), nb, query != null);
 
     CalendarNavigation calendarNavigation = new CalendarNavigation(
         counts[0], counts[1], counts[2], navDetails, startNavDetails, endNavDetails,
         getPrevNavDetails(calDocRef, navDetails, nb, query, calResult, calArchiveResult),
         getNextNavDetails(calDocRef, navDetails, nb, query, calResult));
     LOGGER.debug("getCalendarNavigation: return '" + calendarNavigation + "' for cal '"
         + calDocRef + "', navDetails '" + navDetails + "' and query '" + query + "'");
     return calendarNavigation;
   }
 
   private int checkInvalidNavDetails(NavigationDetails navDetails, Date fromDate,
       EventSearchResult calResult, EventSearchResult calAllResult) {
     if ((fromDate != null) && navDetails.getStartDate().before(fromDate)) {
       return -1;
     } else if ((calResult.getEventList(navDetails.getOffset(), 1).size() == 0)
         && (calAllResult.getSize() > 0)) {
       return 1;
     }
     return 0;
   }
 
   private NavigationDetails getStartNavDetails(EventSearchResult calAllResult
       ) throws EmptyCalendarListException {
     NavigationDetails startNavDetails = null;
     if (calAllResult.getSize() > 0) {
       IEvent startDate = getFirstElement(calAllResult.getEventList(0, 1));
       startNavDetails = new NavigationDetails(startDate.getEventDate(), 0);
     }
     return startNavDetails;
   }
 
   private NavigationDetails getEndNavDetails(DocumentReference calDocRef, int nb,
       EventSearchQuery query) throws EmptyCalendarListException {
     NavigationDetails endNavDetails = null;
     EventSearchResult calAllArchiveResult = getCalendar(calDocRef, true, DATE_HIGH
         ).searchEvents(query);
     if ((calAllArchiveResult.getSize() > 0)) {
       endNavDetails = getLastNavDetails(calDocRef, nb, query, calAllArchiveResult);
     }
     return endNavDetails;
   }
 
   private NavigationDetails getPrevNavDetails(DocumentReference calDocRef,
       NavigationDetails navDetails, int nb, EventSearchQuery query, EventSearchResult
       calSearchResult, EventSearchResult calArchiveSearchResult) {
     NavigationDetails prevNavDetails = null;
     int prevOffset = navDetails.getOffset() - nb;
     try {
       if ((prevOffset >= 0) && (calSearchResult.getSize() > 0)) {
         prevNavDetails = getFirstNavDetails(calDocRef, prevOffset, query, calSearchResult);
       } else if ((prevOffset < 0) && (calArchiveSearchResult.getSize() > 0)) {
         prevNavDetails = getLastNavDetails(calDocRef, prevOffset, query,
             calArchiveSearchResult);
       }
     } catch (EmptyCalendarListException emptyListExp) {
       LOGGER.warn("getPrevNavDetails encountered emptyListExp for calDocRef ["
           + calDocRef + "] and nb [" + nb + "] prevOffset [" + prevOffset
           + "].", emptyListExp);
     }
     return prevNavDetails;
   }
 
   private NavigationDetails getNextNavDetails(DocumentReference calDocRef,
       NavigationDetails navDetails, int nb, EventSearchQuery query,
       EventSearchResult calSearchResult) {
     NavigationDetails nextNavDetails = navDetails;
     int nextOffset = navDetails.getOffset() + nb;
     try {
       if (calSearchResult.getSize() > nextOffset) {
         nextNavDetails = getFirstNavDetails(calDocRef, nextOffset, query, calSearchResult);
       }
     } catch (EmptyCalendarListException emptyListExp) {
       LOGGER.warn("getNextNavDetails encountered emptyListExp for calDocRef ["
           + calDocRef + "] and nb [" + nb + "] nextOffset [" + nextOffset
           + "].", emptyListExp);
     }
     return nextNavDetails;
   }
 
   private NavigationDetails getFirstNavDetails(DocumentReference calDocRef, int offset,
       EventSearchQuery query, EventSearchResult searchResult
       ) throws EmptyCalendarListException {
     IEvent firstEvent = getFirstElement(searchResult.getEventList(offset, 1));
     return getNavDetailsFactory().getNavigationDetails(calDocRef, firstEvent, query);
   }
 
   private NavigationDetails getLastNavDetails(DocumentReference calDocRef, int offset,
       EventSearchQuery query, EventSearchResult searchResult
       ) throws EmptyCalendarListException {
     IEvent lastEvent = getLastElement(searchResult.getEventList(0, Math.abs(offset)));
     return getNavDetailsFactory().getNavigationDetails(calDocRef, lastEvent, query);
   }
 
   private UncertainCount[] getCounts(int calSize, int calArchiveSize, int offset, int nb,
       boolean isSearch) {
     UncertainCount[] counts = new UncertainCount[3];
     counts[0] = new UncertainCount(calArchiveSize + offset, isSearch
         && (calArchiveSize >= 1000));
     counts[1] = new UncertainCount(calSize - offset - nb, isSearch
         && (calSize >= 1000));
     counts[2] = new UncertainCount(calSize + calArchiveSize, isSearch
         && ((calSize >= 1000) || (calArchiveSize >= 1000)));
     return counts;
   }
 
   private ICalendar getCalendar(DocumentReference calConfigDocRef,
       boolean isArchive, Date startDate) {
     ICalendar cal = new Calendar(calConfigDocRef, isArchive);
     cal.setStartDate(startDate);
     return cal;
   }
 
   private <T> T getFirstElement(List<T> list) throws EmptyCalendarListException {
     if ((list != null) && (list.size() > 0)) {
       return list.get(0);
     } else if (list == null) {
       LOGGER.warn("getFirstElement called with null-list.");
       throw new EmptyCalendarListException("getFirstElement called with null-list.");
     } else if (list.isEmpty()) {
       LOGGER.warn("getFirstElement called with empty-list.");
       throw new EmptyCalendarListException("getFirstElement called with empty-list.");
     }
     throw new IllegalArgumentException("getFirstElement called with illegal list ["
         + list + "].");
   }
 
   private <T> T getLastElement(List<T> list) throws EmptyCalendarListException {
     if ((list != null) && (list.size() > 0)) {
       return list.get(list.size() - 1);
     } else if (list == null) {
       LOGGER.warn("getLastElement called with null-list.");
       throw new EmptyCalendarListException("getLastElement called with null-list.");
     } else if (list.isEmpty()) {
       LOGGER.warn("getLastElement called with empty-list.");
       throw new EmptyCalendarListException("getLastElement called with empty-list.");
     }
     throw new IllegalArgumentException("getLastElement called with illegal list [" + list
         + "].");
   }
 
   private IEventManager getEventMgr() {
     if (eventMgr == null) {
       eventMgr = Utils.getComponent(IEventManager.class);
     }
     return eventMgr;
   }
 
   void injectEventMgr(IEventManager eventMgr) {
     this.eventMgr = eventMgr;
   }
 
   private INavigationDetailsFactory getNavDetailsFactory() {
     if (navDetailsFactory == null) {
       navDetailsFactory = new NavigationDetailsFactory();
     }
     return navDetailsFactory;
   }
 
   void injectNavDetailsFactory(INavigationDetailsFactory navDetailsFactory) {
     this.navDetailsFactory = navDetailsFactory;
   }
 
 }
