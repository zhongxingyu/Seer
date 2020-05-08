 package edu.northwestern.bioinformatics.studycalendar.restlets.representations;
 
 import edu.northwestern.bioinformatics.studycalendar.domain.*;
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
 import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
 import edu.northwestern.bioinformatics.studycalendar.service.TestingTemplateService;
 import edu.northwestern.bioinformatics.studycalendar.service.presenter.UserStudySubjectAssignmentRelationship;
 import edu.nwu.bioinformatics.commons.DateUtils;
 import gov.nih.nci.cabig.ctms.lang.DateTools;
 import gov.nih.nci.cabig.ctms.lang.NowFactory;
 import gov.nih.nci.cabig.ctms.lang.StaticNowFactory;
 import org.codehaus.jackson.JsonFactory;
 import org.codehaus.jackson.JsonGenerator;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
 import static edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationScopeMappings.createSuiteRoleMembership;
 import static gov.nih.nci.cabig.ctms.lang.DateTools.createDate;
 
 /**
  * @author Jalpa Patel
  */
 public class MultipleAssignmentScheduleJsonRepresentationTest extends JsonRepresentationTestCase{
     private ScheduledActivity sa;
     private ScheduledActivityState state;
     private Activity activity;
     private List<ActivityProperty> properties;
     private List<ScheduledActivity> scheduledActivities;
     private ScheduledCalendar scheduledCalendar = new ScheduledCalendar();
     private StringWriter out;
     private Epoch epoch;
     private PlannedCalendar calendar;
     private ScheduledStudySegment scheduledSegment;
     private Subject subject;
 
     private MultipleAssignmentScheduleJsonRepresentation representation;
     private TemplateService templateService;
     private JsonGenerator generator;
 
     private final Logger log = LoggerFactory.getLogger(getClass());
     private PscUser user;
     private UserStudySubjectAssignmentRelationship rel;
     private StudySubjectAssignment ssa;
     private Notification notification;
     private Population p1;
 
     @Override
     public void setUp() throws Exception {
         super.setUp();
 
         NowFactory nowFactory = new StaticNowFactory();
         templateService = new TestingTemplateService();
         Study study = createSingleEpochStudy("S", "Treatment");
         Site site = createSite("site");
         subject = setGridId("s1", createSubject("First", "Last"));
 
         calendar = new PlannedCalendar();
         epoch = Epoch.create("Treatment", "A", "B", "C");
         epoch.setGridId("E");
         calendar.addEpoch(epoch);
         calendar.setGridId("calendarGridId");
         calendar.setId(15);
         study.setPlannedCalendar(calendar);
 
         StudySegment studySegment = createNamedInstance("Screening", StudySegment.class);
         studySegment.setGridId("S");
         studySegment.setId(100);
         studySegment.setEpoch(epoch);
         Period p = createPeriod(3, 7, 1);
         studySegment.addPeriod(p);
         PlannedActivity pa = createPlannedActivity(activity, 4);
         p.addPlannedActivity(pa);
         ssa = createAssignment(study,site,subject);
         ssa.setStudySubjectId("Study Subject Id");
         ssa.setStudySubjectCalendarManager(AuthorizationObjectFactory.createCsmUser(14L, "SammyC"));
         state = ScheduledActivityMode.SCHEDULED.createStateInstance();
         state.setDate(DateTools.createDate(2009, Calendar.APRIL, 3));
         state.setReason("Just moved by 4 days");
         sa = createScheduledActivity(pa, 2009, Calendar.APRIL, 7, state);
         sa.setGridId("1111");
         sa.setIdealDate(DateTools.createDate(2009, Calendar.APRIL, 7));
         sa.setScheduledStudySegment(scheduledSegment);
 
         out = new StringWriter();
         generator = new JsonFactory().createJsonGenerator(out);
 
         ActivityType activityType = createActivityType("Type1");
         activity = createActivity("activity1", activityType);
 
         properties = new ArrayList<ActivityProperty>();
         ActivityProperty prop1 = createActivityProperty("URI", "text", "activity defination");
         properties.add(prop1);
         properties.add(createActivityProperty("URI", "template", "activity uri"));
         SortedSet<String> labels = new TreeSet<String>();
         labels.add("label1");
         labels.add("label2");
         sa.setLabels(labels);
         sa.setActivity(activity);
         scheduledActivities =  new ArrayList<ScheduledActivity>();
         scheduledActivities.add(sa);
 
         scheduledSegment = createScheduledStudySegment(studySegment, DateTools.createDate(2009, Calendar.APRIL, 3));
         scheduledSegment.setGridId("GRID-SEG");
         scheduledCalendar.addStudySegment(scheduledSegment);
         scheduledCalendar.setAssignment(setGridId("GRID-ASSIGN", ssa));
         sa.setScheduledStudySegment(scheduledSegment);
 
         ssa.getScheduledCalendar().addStudySegment(createScheduledStudySegment(createDate(2006, Calendar.APRIL, 1), 365));
 
         user = AuthorizationObjectFactory.createPscUser("jo",
             createSuiteRoleMembership(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER).forAllStudies().forAllSites());
         user.getCsmUser().setUserId(11l);
         ssa.setStudySubjectCalendarManager(user.getCsmUser());
         notification = setGridId("n11", new Notification(sa));
         ssa.addNotification(notification);
         p1 = createPopulation("P", "Population");
         ssa.addPopulation(p1);
         ssa.addPopulation(createPopulation("T", "Test"));
         rel = new UserStudySubjectAssignmentRelationship(user, ssa);
 
         representation = new MultipleAssignmentScheduleJsonRepresentation(Arrays.asList(rel), nowFactory, templateService, request.getRootRef());
     }
 
     public void testStateInfoInJson() throws Exception {
         MultipleAssignmentScheduleJsonRepresentation.createJSONStateInfo(generator, state);
         JSONObject stateInfo = outputAsObject();
 
         assertEquals("State mode is different", "scheduled", stateInfo.get("name"));
         assertEquals("State date is different", "2009-04-03", stateInfo.get("date"));
         assertEquals("State reason is different", state.getReason(), stateInfo.get("reason"));
     }
 
     public void testStateContainsNoDate() throws Exception {
         ScheduledActivityState saState = ScheduledActivityMode.CANCELED.createStateInstance();
         MultipleAssignmentScheduleJsonRepresentation.createJSONStateInfo(generator, saState);
         JSONObject stateInfo = outputAsObject();
         assertEquals("State mode is different", "canceled", stateInfo.get("name"));
         assertFalse("State date should not be present", stateInfo.has("date"));
     }
 
     public void testActivityPropertyInJson() throws Exception {
         ActivityProperty ap = createActivityProperty("URI","text","activity defination");
         MultipleAssignmentScheduleJsonRepresentation.createJSONActivityProperty(generator, ap);
         JSONObject apJson = outputAsObject();
 
         assertEquals("Namespace is different", ap.getNamespace(), apJson.get("namespace"));
         assertEquals("Name is different", ap.getName(), apJson.get("name"));
         assertEquals("Value is different", ap.getValue(), apJson.get("value"));
     }
 
     public void testActivityInJson() throws Exception {
         activity.setProperties(properties);
         MultipleAssignmentScheduleJsonRepresentation.createJSONActivity(generator, activity);
         JSONObject activityJson = outputAsObject();
         assertEquals("No of elements are different", 3, activityJson.length());
         assertEquals("Activity name is different", activity.getName(), activityJson.get("name"));
         assertEquals("Activity Type is different", activity.getType().getName(), activityJson.get("type"));
         assertEquals("no of elements is different", 2,((JSONArray)activityJson.get("properties")).length());
     }
 
     public void testActivityWhenPropertiesAreEmpty() throws Exception {
         MultipleAssignmentScheduleJsonRepresentation.createJSONActivity(generator, activity);
         JSONObject activityJson = outputAsObject();
         assertEquals("No of elements are different", 2, activityJson.length());
         assertTrue(activityJson.isNull("properties"));
     }
 
     public void testScheduledActivityInJson() throws Exception {
         representation.createJSONScheduledActivity(generator, sa);
         JSONObject jsonSA = outputAsObject();
 
         assertEquals("Grid Id is different", sa.getGridId(), jsonSA.get("id"));
         assertEquals("Study is different", sa.getScheduledStudySegment().getStudySegment().
                 getEpoch().getPlannedCalendar().getStudy().getAssignedIdentifier(), jsonSA.get("study"));
         assertEquals("Study Segment is different", sa.getScheduledStudySegment().getName(), jsonSA.get("study_segment"));
         assertEquals("Ideal Date is different", "2009-04-07", jsonSA.get("ideal_date"));
         assertEquals("Planned day is different", "6", jsonSA.get("plan_day"));
     }
 
     public void testScheduledActivityIncludesAssignmentName() throws Exception {
         representation.createJSONScheduledActivity(generator, sa);
         JSONObject jsonSA = outputAsObject();
         assertEquals("Missing assignment name",
            "S" , jsonSA.getJSONObject("assignment").get("name"));
     }
 
     public void testScheduledActivityIncludesAssignmentId() throws Exception {
         representation.createJSONScheduledActivity(generator, sa);
         JSONObject jsonSA = outputAsObject();
         assertEquals("Missing assignment name",
             "GRID-ASSIGN", ((JSONObject) jsonSA.get("assignment")).get("id"));
     }
 
     public void testScheduledActivityHasUpdatePrivilege() throws Exception {
         representation.createJSONScheduledActivity(generator, sa);
         JSONObject jsonSA = outputAsObject();
         assertEquals("Missing update privilege",
             "update", jsonSA.getJSONObject("assignment").getJSONArray("privileges").get(0));
     }
 
     public void testScheduledActivityWithoutUpdatePrivilege() throws Exception {
         user.getMembership(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER).notForAllSites().notForAllStudies();
 
         representation.createJSONScheduledActivity(generator, sa);
         JSONObject jsonSA = outputAsObject();
         assertEquals("Should not include update privilege",
             0, jsonSA.getJSONObject("assignment").getJSONArray("privileges").length());
     }
 
     public void testScheduledActivityCurrentState() throws Exception {
         representation.createJSONScheduledActivity(generator, sa);
         JSONObject jsonSA = outputAsObject();
         JSONObject currentState = (JSONObject) jsonSA.get("current_state");
         assertNotNull("current state is missing", currentState);
         assertEquals("current state reason incorrect", "Just moved by 4 days", currentState.get("reason"));
         assertEquals("current state date incorrect", "2009-04-03", currentState.get("date"));
         assertEquals("current state name incorrect", "scheduled", currentState.get("name"));
     }
 
     public void testScheduledActivityStateHistoryContainsAllStates() throws Exception {
         representation.createJSONScheduledActivity(generator, sa);
         JSONObject actual = outputAsObject();
         JSONArray history = actual.getJSONArray("state_history");
         assertEquals(2, history.length());
     }
 
     public void testScheduledActivityStateHistoryStartsWithInitial() throws Exception {
         representation.createJSONScheduledActivity(generator, sa);
         JSONObject actual = outputAsObject();
         JSONArray history = actual.getJSONArray("state_history");
         JSONObject first = (JSONObject) history.get(0);
         assertEquals("Incorrect date", "2009-04-07", first.get("date"));
     }
 
     public void testScheduledActivityStateHistoryEndsWithCurrent() throws Exception {
         representation.createJSONScheduledActivity(generator, sa);
         JSONObject actual = outputAsObject();
         JSONArray history = actual.getJSONArray("state_history");
         JSONObject first = (JSONObject) history.get(1);
         assertEquals("Incorrect date", "2009-04-03", first.get("date"));
     }
 
     public void testWithHiddenActivities() throws Exception {
         representation.createJSONScheduledActivities(generator, true, scheduledActivities);
         JSONObject jsonScheduleActivities = outputAsObject();
         assertTrue("has no hidden activities", Boolean.valueOf(jsonScheduleActivities.getString("hidden_activities")));
     }
 
     public void testWhenHiddenActivitiesIsNull() throws Exception {
         representation.createJSONScheduledActivities(generator, null, scheduledActivities);
         JSONObject jsonScheduleActivities = outputAsObject();
         assertTrue(jsonScheduleActivities.isNull("hidden_activities"));
         assertEquals("no of elements is different", 1, jsonScheduleActivities.length());
     }
 
     public void testScheduledStudySegmentsInJson() throws Exception {
         representation.createJSONStudySegment(generator, scheduledSegment);
         JSONObject jsonSegment = outputAsObject();
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
         representation.createJSONStudySegment(generator, scheduledSegment);
         JSONObject actual = outputAsObject();
         assertEquals("Missing assignment name", "Treatment: Screening",
             actual.get("name"));
     }
 
     public void testCreateJSONStudySegment() throws Exception {
         representation.createJSONStudySegment(generator, scheduledSegment);
         JSONObject actual = outputAsObject();
         assertTrue("Missing properties", actual.has("id"));
         assertTrue("Missing properties", actual.has("name"));
     }
 
     public void testScheduledSegmentIncludesAssignmentId() throws Exception {
         representation.createJSONStudySegment(generator, scheduledSegment);
         JSONObject actual = outputAsObject();
         assertEquals("Missing assignment id", "GRID-ASSIGN",
             ((JSONObject) actual.get("assignment")).get("id"));
     }
 
     public void testDetailsInJson() throws Exception {
         sa.setDetails("Detail");
         representation.createJSONScheduledActivity(generator, sa);
         JSONObject actual = outputAsObject();
         assertEquals("Missing details", "Detail", actual.get("details"));
     }
 
     public void testMissingDetailsInJson() throws Exception {
         representation.createJSONScheduledActivity(generator, sa);
         JSONObject actual = outputAsObject();
         assertTrue(actual.isNull("details"));
     }
 
     public void testStudySubjectIdInJson() throws Exception {
         representation.createJSONScheduledActivity(generator, sa);
         JSONObject actual = outputAsObject();
         assertEquals("Missing study subject id", "Study Subject Id", actual.get("study_subject_id"));
     }
 
     public void testConditionalInJson() throws Exception {
         sa.getPlannedActivity().setCondition("Conditional Details");
         representation.createJSONScheduledActivity(generator, sa);
         JSONObject actual = outputAsObject();
         assertEquals("Missing conditions", "Conditional Details", actual.get("condition"));
     }
 
     public void testLabelsInJson() throws Exception {
         representation.createJSONScheduledActivity(generator, sa);
         JSONObject actual = outputAsObject();
         assertEquals("Missing labels", "label1 label2", actual.get("labels"));
     }
 
     public void testSubjectInfoInJson() throws Exception {
         subject.setDateOfBirth(DateUtils.createDate(1978, Calendar.MARCH, 15));
         subject.setGender(Gender.FEMALE);
         representation.createJSONSubject(generator, subject);
         JSONObject actual = outputAsObject();
         assertEquals("Missing first", subject.getFirstName(), actual.get("first_name"));
         assertEquals("Missing last", subject.getLastName(), actual.get("last_name"));
         assertEquals("Missing full", subject.getFullName(), actual.get("full_name"));
         assertEquals("Missing last-first", subject.getLastFirst(), actual.get("last_first"));
         assertEquals("Missing DOB", "1978-03-15", actual.get("birth_date"));
         assertEquals("Missing gender", "Female", actual.get("gender"));
     }
 
     public void testSubjectInfoStillGeneratedIfSubjectHasNoGender() throws Exception {
         subject.setGender(null);
         representation.createJSONSubject(generator, subject);
         JSONObject jsonSubj = outputAsObject();
         // expect no error, plus:
 
         assertFalse("Should have no gender", jsonSubj.has("gender"));
     }
 
     public void testSubjectPropertiesIncludedInJson() throws Exception {
         subject.getProperties().add(new SubjectProperty("Cube root", "9"));
 
         representation.createJSONSubject(generator, subject);
         JSONObject actual = outputAsObject();
         assertTrue("No properties", actual.has("properties"));
         assertEquals("Wrong number of properties", 1, actual.getJSONArray("properties").length());
     }
 
     public void testStudySubjectAssignmentInJson() throws Exception {
         representation.createJSONStudySubjectAssignment(generator, rel);
         JSONObject actual = outputAsObject();
         assertEquals("Missing id", ssa.getGridId(), actual.get("id"));
         assertEquals("Missing name", ssa.getName(), actual.get("name"));
         assertTrue("No privileges", actual.has("privileges"));
         JSONArray privileges = actual.getJSONArray("privileges");
         assertEquals("Wrong number of privilege", 3, actual.getJSONArray("privileges").length());
         assertEquals("Missing first priv", "visible", privileges.get(0));
         assertEquals("Missing second priv", "update-schedule", privileges.get(1));
         assertEquals("Missing third priv", "calendar-manager", privileges.get(2));
         assertTrue("No notifications", actual.has("notifications"));
         assertEquals("Wrong number of notifications", 1, actual.getJSONArray("notifications").length());
         assertTrue("No populations", actual.has("populations"));
         assertEquals("Wrong number of populations", 2, actual.getJSONArray("populations").length());
        JSONObject actualPopulation = (JSONObject) actual.getJSONArray("populations").get(0);
        assertTrue("Missing name", actualPopulation.has("name"));
        assertTrue("Missing abbreviation", actualPopulation.has("abbreviation"));
 
     }
 
     public void testAssignmentNotification() throws Exception {
         representation.createJSONNotification(generator, notification);
         JSONObject actual = outputAsObject();
         assertEquals("Missing message", notification.getMessage(), actual.get("message"));
         assertEquals("Missing title", notification.getTitle(), actual.get("title"));
         assertEquals("Missing href",
            "http://trials.etc.edu/studycalendar/api/v1/subjects/s1/assignments/GRID-ASSIGN/notifications/n11"
             , actual.get("href"));
     }
 
     private JSONObject outputAsObject() throws IOException {
         generator.close();
         try {
             return new JSONObject(out.toString());
         } catch (JSONException e) {
             log.info("Generated JSON: {}", out.toString());
             fail("Generated JSON is not valid: " + e.getMessage());
             return null; // Unreachable
         }
     }
 }
