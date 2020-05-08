 package edu.northwestern.bioinformatics.studycalendar.service;
 
 import edu.northwestern.bioinformatics.studycalendar.domain.*;
 import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
 
 import java.util.*;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.text.ParseException;
 
 import org.springframework.beans.factory.annotation.Required;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 public class SubjectCoordinatorDashboardService {
 
     private ScheduledActivityDao scheduledActivityDao;
 
     final String dayNames[] =
     {"Sunday",
     "Monday",
     "Tuesday",
     "Wednesday",
     "Thursday",
     "Friday",
     "Saturday"
     };
 
     private static final Logger log = LoggerFactory.getLogger(SubjectCoordinatorDashboardService.class.getName());
 
     public Map<String, Object> getMapOfCurrentEvents(List<StudySubjectAssignment> studySubjectAssignments, int initialShiftDate) {
         Date startDate = new Date();
         Collection<ScheduledActivity> collectionOfEvents;
         Map<String, Object> mapOfUserAndCalendar = new LinkedHashMap<String, Object>();
 
         Map <String, Object> subjectAndEvents;
 
         for (int i =0; i< initialShiftDate; i++) {
             Date tempStartDate = shiftStartDayByNumberOfDays(startDate, i);
             subjectAndEvents = new HashMap<String, Object>();
             for (StudySubjectAssignment studySubjectAssignment : studySubjectAssignments) {
 
                 List<ScheduledActivity> events = new ArrayList<ScheduledActivity>();
                 ScheduledCalendar calendar = studySubjectAssignment.getScheduledCalendar();
                 collectionOfEvents = getScheduledActivityDao().getEventsByDate(calendar, tempStartDate, tempStartDate);
 
                 Subject subject = studySubjectAssignment.getSubject();
                 String subjectName = subject.getFullName();
                 if (collectionOfEvents.size()>0) {
                     for (ScheduledActivity event : collectionOfEvents) {
                         if (event.getCurrentState().getMode().getId() == 1 || event.getCurrentState().getMode().getId() == 4 ) {
                             events.add(event);
                         }
                     }
                 }
                 if (events != null && events.size() > 0) {
                     subjectAndEvents.put(subjectName, events);
                 }
             }
             String keyDate = formatDateToString(tempStartDate);
             keyDate = keyDate + " - " + convertDateKeyToString(tempStartDate);
             if (subjectAndEvents!= null && subjectAndEvents.size()>0) {
                 mapOfUserAndCalendar.put(keyDate, subjectAndEvents);
             }
         }
        return mapOfUserAndCalendar;
     }
 
     public Map<String, Object> getMapOfCurrentEventsForSpecificActivity(
         List<StudySubjectAssignment> studySubjectAssignments, int initialShiftDate, Map<ActivityType, Boolean> activities) {
         Date startDate = new Date();
         Collection<ScheduledActivity> collectionOfEvents;
         Map<String, Object> mapOfUserAndCalendar = new LinkedHashMap<String, Object>();
 
         Map <String, Object> subjectAndEvents;
 
         for (int i = 0; i< initialShiftDate; i++) {
             Date tempStartDate;
             if (i == 0) {
                 //need to get activities for the startDate itself
                 tempStartDate = shiftStartDayByNumberOfDays(startDate, i);
             } else {
                 tempStartDate = shiftStartDayByNumberOfDays(startDate, 1);
             }
             subjectAndEvents = new HashMap<String, Object>();
             for (StudySubjectAssignment studySubjectAssignment : studySubjectAssignments) {
                 List<ScheduledActivity> events = new ArrayList<ScheduledActivity>();
                 ScheduledCalendar calendar = studySubjectAssignment.getScheduledCalendar();
                 collectionOfEvents = getScheduledActivityDao().getEventsByDate(calendar, tempStartDate, tempStartDate);
                 Subject subject = studySubjectAssignment.getSubject();
                 String subjectName = subject.getFullName();
 
                 if (collectionOfEvents.size()>0) {
                     for (ScheduledActivity event : collectionOfEvents) {
                         ActivityType eventActivityType = event.getActivity().getType();
                         Boolean value;
                         if(!activities.containsKey(eventActivityType)) {
                             value = false;
                         } else {
                             value = activities.get(eventActivityType);
                         }
                         if (value) {
                             if (event.getCurrentState().getMode().getId() == 1 || event.getCurrentState().getMode().getId() == 4 ) {
                                 events.add(event);
                             }
                         }
                     }
                 }
                 if (events != null && events.size()> 0)  {
                     subjectAndEvents.put(subjectName, events);
                 }
             }
             String keyDate = formatDateToString(tempStartDate);
             keyDate = keyDate + " - " + convertDateKeyToString(tempStartDate);
             if (subjectAndEvents!= null && subjectAndEvents.size()>0) {
                 mapOfUserAndCalendar.put(keyDate, subjectAndEvents);
             }
             startDate = tempStartDate;
         }
         return mapOfUserAndCalendar;
     }
 
     public String convertDateKeyToString(Date date) {
         Calendar c = Calendar.getInstance();
         c.setTime(date);
         int dayOfTheWeek = c.get(Calendar.DAY_OF_WEEK);
         return dayNames[dayOfTheWeek-1];
     }
 
 
     public Map<Object, Object> getMapOfOverdueEvents(List<StudySubjectAssignment> studySubjectAssignments) {
         Date currentDate = new Date();
         Date endDate = shiftStartDayByNumberOfDays(currentDate, -1);
 
         //list of events overtue
         Map <Object, Object> subjectAndOverDueEvents = new HashMap<Object, Object>();
         for (StudySubjectAssignment studySubjectAssignment : studySubjectAssignments) {
             List<ScheduledActivity> events = new ArrayList<ScheduledActivity>();
 
             Map<Object, Integer> key = new HashMap<Object, Integer>();
             Map<Object, Object> value = new HashMap<Object, Object>();
 
             ScheduledCalendar calendar = studySubjectAssignment.getScheduledCalendar();
 
             //start Date is giving back an epoch beginning date, but since the activities can be rescheduled,
            // the search should be made by "ideal_date" = "start_epoch_date"
            Date startDate = studySubjectAssignment.getStartDateEpoch();
            Collection<ScheduledActivity> collectionOfEvents = getScheduledActivityDao().getEventsByIdealDate(calendar, startDate, endDate);

             Subject subject = studySubjectAssignment.getSubject();
 
             for (ScheduledActivity event : collectionOfEvents) {
                 if(event.getCurrentState().getMode().equals(ScheduledActivityMode.SCHEDULED)) {
                     events.add(event);
                 }
             }
             if (events.size()>0) {
                 key.put(subject, events.size());
                 ScheduledActivity earliestEvent = getEarliestEvent(events);
                 value.put(studySubjectAssignment, earliestEvent);
                 subjectAndOverDueEvents.put(key, value);
             }
         }
         return subjectAndOverDueEvents;
     }
 
 
     public ScheduledActivity getEarliestEvent(List<ScheduledActivity> events) {
         Date earliestDate = events.get(0).getActualDate();
         ScheduledActivity activity = events.get(0);
         for (ScheduledActivity event : events){
             if (event.getActualDate().compareTo(earliestDate) < 0){
                 earliestDate = event.getActualDate();
                 activity = event;
             }
         }
         return activity;
     }
 
     public Date shiftStartDayByNumberOfDays(Date startDate, Integer numberOfDays) {
         java.sql.Timestamp timestampTo = new java.sql.Timestamp(startDate.getTime());
         long numberOfDaysToShift = numberOfDays * 24 * 60 * 60 * 1000;
         timestampTo.setTime(timestampTo.getTime() + numberOfDaysToShift);
         Date d = timestampTo;
         DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
         String dateString = df.format(d);
         Date d1;
         try {
             d1 = df.parse(dateString);
         } catch (ParseException e) {
             log.debug("Exception " + e);
             d1 = startDate;
         }
         return d1;
     }
 
     public String formatDateToString(Date date) {
         DateFormat df = new SimpleDateFormat("MM/dd");
         return df.format(date);
     }
 
 
     @Required
     public void setScheduledActivityDao(ScheduledActivityDao scheduledActivityDao) {
         this.scheduledActivityDao = scheduledActivityDao;
     }
     public ScheduledActivityDao getScheduledActivityDao() {
         return scheduledActivityDao;
     }
 }
