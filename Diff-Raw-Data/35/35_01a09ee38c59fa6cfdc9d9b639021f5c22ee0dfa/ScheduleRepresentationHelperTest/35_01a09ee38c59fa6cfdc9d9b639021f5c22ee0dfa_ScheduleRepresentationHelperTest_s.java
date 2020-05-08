 package edu.northwestern.bioinformatics.studycalendar.restlets;
 import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
 import edu.northwestern.bioinformatics.studycalendar.domain.*;
 import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Scheduled;
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
 import gov.nih.nci.cabig.ctms.lang.DateTools;
 import org.json.JSONArray;
 import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
 
 
 /**
  * @author Jalpa Patel
  */
 public class ScheduleRepresentationHelperTest extends StudyCalendarTestCase{
     private ScheduledActivity sa;
     private ScheduledActivityState state;
     private Activity activity;
     private ScheduledStudySegment scheduledSegment;
     private List<ActivityProperty> properties;
     private List<ScheduledActivity> scheduledActivities;
     private ScheduledCalendar scheduledCalendar = new ScheduledCalendar();
 
     public void setUp() throws Exception {
         super.setUp();
         ActivityType activityType = createActivityType("Type1");
         activity = createActivity("activity1", activityType);
         properties = new ArrayList<ActivityProperty>();
         properties.add(createActivityProperty("URI", "text", "activity defination"));
         properties.add(createActivityProperty("URI", "template", "activity uri"));
 
         Study study = createSingleEpochStudy("S", "Treatment");
         Site site = createSite("site");
         Subject subject = createSubject("First", "Last");
         Epoch epoch = study.getPlannedCalendar().getEpochs().get(0);
         epoch.setGridId("E");
         StudySegment studySegment = epoch.getStudySegments().get(0);
         studySegment.setGridId("S");
         Period p = createPeriod(3, 7, 1);
         studySegment.addPeriod(p);
         PlannedActivity pa = createPlannedActivity(activity, 4);
         p.addPlannedActivity(pa);
 
         state = new Scheduled();
         state.setDate(DateTools.createDate(2009, Calendar.APRIL, 3));
         state.setReason("Just moved by 4 days");
         sa = createScheduledActivity(pa, 2009, Calendar.APRIL, 7, state);
         sa.setIdealDate(DateTools.createDate(2009, Calendar.APRIL, 7));
         scheduledActivities =  new ArrayList<ScheduledActivity>();
         scheduledActivities.add(sa);
         sa.setGridId("1111");
         scheduledSegment = createScheduledStudySegment(studySegment, DateTools.createDate(2009, Calendar.APRIL, 3));
         scheduledSegment.setGridId("GRID-SEG");
         scheduledCalendar.addStudySegment(scheduledSegment);
         scheduledCalendar.setAssignment(setGridId("GRID-ASSIGN", createAssignment(study, site, subject)));
         sa.setScheduledStudySegment(scheduledSegment);
     }
 
     public void testStateInfoInJson() throws Exception {
         JSONObject stateInfo = ScheduleRepresentationHelper.createJSONStateInfo(state);
         assertEquals("State mode is different", "scheduled", stateInfo.get("name"));
         assertEquals("State date is different", "2009-04-03", stateInfo.get("date"));
         assertEquals("State reason is different", state.getReason(), stateInfo.get("reason"));
     }
     
     public void testActivityPropertyInJson() throws Exception {
         ActivityProperty ap = createActivityProperty("URI","text","activity defination");
         JSONObject apJson = ScheduleRepresentationHelper.createJSONActivityProperty(ap);
         assertEquals("Namespace is different", ap.getNamespace(), apJson.get("namespace"));
         assertEquals("Name is different", ap.getName(), apJson.get("name"));
         assertEquals("Value is different", ap.getValue(), apJson.get("value"));
     }
     
     public void testActivityInJson() throws Exception {
         activity.setProperties(properties);
         JSONObject activityJson = ScheduleRepresentationHelper.createJSONActivity(activity);
         assertEquals("No of elements are different", 3, activityJson.length());
         assertEquals("Activity name is different", activity.getName(), activityJson.get("name"));
         assertEquals("Activity Type is different", activity.getType().getName(), activityJson.get("type"));
         assertEquals("no of elements is different", 2,((JSONArray)activityJson.get("properties")).length());
     }
 
     public void testActivityWhenPropertiesAreEmpty() throws Exception {
         JSONObject activityJson = ScheduleRepresentationHelper.createJSONActivity(activity);
         assertEquals("No of elements are different", 2, activityJson.length());
         assertTrue(activityJson.isNull("properties"));
     }
 
     public void testScheduledActivityInJson() throws Exception {
         JSONObject jsonSA = ScheduleRepresentationHelper.createJSONScheduledActivity(sa);
         assertEquals("Grid Id is different", sa.getGridId(), jsonSA.get("id"));
         assertEquals("Study is different", sa.getScheduledStudySegment().getStudySegment().
                 getEpoch().getPlannedCalendar().getStudy().getAssignedIdentifier(), jsonSA.get("study"));
         assertEquals("Study Segment is different", sa.getScheduledStudySegment().getName(), jsonSA.get("study_segment"));
         assertEquals("Ideal Date is different", "2009-04-07", jsonSA.get("ideal_date"));
         assertEquals("Planned day is different", "6", jsonSA.get("plan_day"));
     }
 
     public void testScheduledActivityIncludesAssignmentName() throws Exception {
         JSONObject jsonSA = ScheduleRepresentationHelper.createJSONScheduledActivity(sa);
         assertEquals("Missing assignment name",
             "S", ((JSONObject) jsonSA.get("assignment")).get("name"));
     }
 
     public void testScheduledActivityIncludesAssignmentId() throws Exception {
         JSONObject jsonSA = ScheduleRepresentationHelper.createJSONScheduledActivity(sa);
         assertEquals("Missing assignment name",
             "GRID-ASSIGN", ((JSONObject) jsonSA.get("assignment")).get("id"));
     }
 
     public void testScheduledActivityCurrentState() throws Exception {
         JSONObject jsonSA = ScheduleRepresentationHelper.createJSONScheduledActivity(sa);
         JSONObject currentState = (JSONObject) jsonSA.get("current_state");
         assertNotNull("current state is missing", currentState);
         assertEquals("current state reason incorrect", "Just moved by 4 days", currentState.get("reason"));
         assertEquals("current state date incorrect", "2009-04-03", currentState.get("date"));
         assertEquals("current state name incorrect", "scheduled", currentState.get("name"));
     }
 
     public void testScheduledActivityStateHistoryContainsAllStates() throws Exception {
         assertEquals(2,
             ((JSONArray) ScheduleRepresentationHelper.createJSONScheduledActivity(sa).get("state_history")).length());
     }
 
     public void testScheduledActivityStateHistoryStartsWithInitial() throws Exception {
         JSONArray history = (JSONArray) ScheduleRepresentationHelper.createJSONScheduledActivity(sa).get("state_history");
         JSONObject first = (JSONObject) history.get(0);
         assertEquals("Incorrect date", "2009-04-07", first.get("date"));
     }
     
     public void testScheduledActivityStateHistoryEndsWithCurrent() throws Exception {
         JSONArray history = (JSONArray) ScheduleRepresentationHelper.createJSONScheduledActivity(sa).get("state_history");
         JSONObject first = (JSONObject) history.get(1);
         assertEquals("Incorrect date", "2009-04-03", first.get("date"));
     }
 
     public void testScheduleDayWiseActivitiesInJson() throws Exception {
         JSONObject jsonScheduleActivities =  ScheduleRepresentationHelper.createJSONScheduledActivities(true, scheduledActivities);
         assertEquals("no of elements is different", 2,jsonScheduleActivities.length());
         assertEquals("has no hidden activities", true, jsonScheduleActivities.get("hidden_activities"));
     }
 
     public void testWhenHiddenActivitiesIsNull() throws Exception {
         JSONObject jsonScheduleActivities =  ScheduleRepresentationHelper.createJSONScheduledActivities(null, scheduledActivities);
         assertTrue(jsonScheduleActivities.isNull("hidden_activities"));
         assertEquals("no of elements is different", 1,jsonScheduleActivities.length());
     }
 
     public void testScheduledStudySegmentsInJson() throws Exception {
         JSONObject jsonSegment = ScheduleRepresentationHelper.createJSONStudySegment(scheduledSegment);
         assertEquals("has different name", scheduledSegment.getName(), jsonSegment.get("name"));
         assertEquals("missing ID", scheduledSegment.getGridId(), jsonSegment.get("id"));
 
         JSONObject jsonRange = (JSONObject)(jsonSegment.get("range"));
         assertEquals("different start date", "2009-04-03", jsonRange.get("start_date"));
         assertEquals("different stop date", "2009-04-09", jsonRange.get("stop_date"));
 
         JSONObject jsonPlannedSegmentInfo = (JSONObject)(jsonSegment.get("planned"));
 
         JSONObject jsonPlannedSegment = (JSONObject)(jsonPlannedSegmentInfo.get("segment"));
         assertEquals("segment has different name", scheduledSegment.getStudySegment().getName(), jsonPlannedSegment.get("name"));
         assertEquals("segment has different id", "S", jsonPlannedSegment.get("id"));
 
         JSONObject jsonEpoch =  (JSONObject)(jsonPlannedSegmentInfo.get("epoch"));
         assertEquals("Epoch has different name", scheduledSegment.getStudySegment().getEpoch().getName(), jsonEpoch.get("name"));
         assertEquals("Epoch has different id", "E", jsonEpoch.get("id"));
         JSONObject jsonStudy =  (JSONObject)(jsonPlannedSegmentInfo.get("study"));
         assertEquals("Study doesn't match", scheduledSegment.getStudySegment().getEpoch().getPlannedCalendar()
             .getStudy().getAssignedIdentifier(), jsonStudy.get("assigned_identifier"));
     }
 
     public void testScheduledSegmentIncludesAssignmentName() throws Exception {
         JSONObject jsonSegment = ScheduleRepresentationHelper.createJSONStudySegment(scheduledSegment);
         assertEquals("Missing assignment name", "S",
             ((JSONObject) jsonSegment.get("assignment")).get("name"));
     }
 
     public void testScheduledSegmentIncludesAssignmentId() throws Exception {
         JSONObject jsonSegment = ScheduleRepresentationHelper.createJSONStudySegment(scheduledSegment);
         assertEquals("Missing assignment id", "GRID-ASSIGN",
             ((JSONObject) jsonSegment.get("assignment")).get("id"));
     }
 }
