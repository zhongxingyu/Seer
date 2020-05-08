 package edu.northwestern.bioinformatics.studycalendar.web.activity;
 
 import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
 import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
 import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
 import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.setId;
 import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createNamedInstance;
 import edu.northwestern.bioinformatics.studycalendar.domain.Source;
 import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
 import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
 import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.BUSINESS_ADMINISTRATOR;
 import static org.easymock.EasyMock.expect;
 import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
 import org.displaytag.properties.SortOrderEnum;
 import java.util.*;
 
 /**
  * @author Nataliya Shurupova
  */
 public class ActivityControllerTest extends ControllerTestCase {
 
     private ActivityDao activityDao;
     private SourceDao sourceDao;
     private PlannedActivityDao plannedActivityDao;
     private ActivityTypeDao activityTypeDao;
     private ActivityController controller;
     private final static Integer pageIncrementor =100;
 
     Activity a1, a2, a3, a4;
     List<Activity> listOfActivities;
 
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         activityDao = registerDaoMockFor(ActivityDao.class);
         sourceDao = registerDaoMockFor(SourceDao.class);
         plannedActivityDao = registerDaoMockFor(PlannedActivityDao.class);
         activityTypeDao = registerDaoMockFor(ActivityTypeDao.class);
 
         controller = new ActivityController();
 
         controller.setSourceDao(sourceDao);
         controller.setActivityDao(activityDao);
         controller.setPlannedActivityDao(plannedActivityDao);
         controller.setActivityTypeDao(activityTypeDao);
 
         a1 = Fixtures.createActivity("Activity One", "Code1", setId(123, createNamedInstance("Test Source 1", Source.class)), Fixtures.createActivityType("Desease Measure"));
         a2 = Fixtures.createActivity("Two Activity", "Code2", setId(126, createNamedInstance("Test Source 2", Source.class)), Fixtures.createActivityType("Lab Test"));
         a3 = Fixtures.createActivity("My Activity Three", "Code3", setId(128, createNamedInstance("Test Source 3", Source.class)), Fixtures.createActivityType("Intervention"));
         a4 = Fixtures.createActivity("My Forth Activity", "Code4", setId(129, createNamedInstance("Test Source 4", Source.class)), Fixtures.createActivityType("Another Type"));
         listOfActivities = new ArrayList<Activity>();
         listOfActivities.add(a1);
         listOfActivities.add(a2);
         listOfActivities.add(a3);
         listOfActivities.add(a4);
     }
 
     public void testAuthorizedRoles() {
         Collection<ResourceAuthorization> actualAuthorizations = controller.authorizations(null, null);
         assertRolesAllowed(actualAuthorizations, BUSINESS_ADMINISTRATOR);
     }
 
     public void testSortingActivitiesAccordingToActivitiesComparator() throws Exception {
         List<Activity> justPlainSortedByTypeAndName = controller.sortListBasedOnRequest(listOfActivities, null, null);
         assertEquals("First activity in the list is wrong ", a4, justPlainSortedByTypeAndName.get(0));
         assertEquals("Second activity in the list is wrong ", a1, justPlainSortedByTypeAndName.get(1));
         assertEquals("Third activity in the list is wrong ", a3, justPlainSortedByTypeAndName.get(2));
         assertEquals("Forth activity in the list is wrong ", a2, justPlainSortedByTypeAndName.get(3));
     }
 
     public void testSortingActivitiesAccordingToNameAscending() throws Exception {
         SortOrderEnum sortOrderEnum = SortOrderEnum.ASCENDING;
 
         List<Activity> justPlainSortedByTypeAndName = controller.sortListBasedOnRequest(listOfActivities, sortOrderEnum, "name");
         assertEquals("First activity in the list is wrong ", a1, justPlainSortedByTypeAndName.get(0));
         assertEquals("Second activity in the list is wrong ", a3, justPlainSortedByTypeAndName.get(1));
         assertEquals("Third activity in the list is wrong ", a4, justPlainSortedByTypeAndName.get(2));
         assertEquals("Forth activity in the list is wrong ", a2, justPlainSortedByTypeAndName.get(3));
     }
 
     public void testSortingActivitiesAccordingToNameDescending() throws Exception {
         SortOrderEnum sortOrderEnum = SortOrderEnum.DESCENDING;
 
         List<Activity> justPlainSortedByTypeAndName = controller.sortListBasedOnRequest(listOfActivities, sortOrderEnum, "name");
         assertEquals("First activity in the list is wrong ", a2, justPlainSortedByTypeAndName.get(0));
         assertEquals("Second activity in the list is wrong ", a4, justPlainSortedByTypeAndName.get(1));
         assertEquals("Third activity in the list is wrong ", a3, justPlainSortedByTypeAndName.get(2));
         assertEquals("Forth activity in the list is wrong ", a1, justPlainSortedByTypeAndName.get(3));
     }
 
 
     public void testSortingActivitiesAccordingToTypeComparatorDescending() throws Exception {
         SortOrderEnum sortOrderEnum = SortOrderEnum.DESCENDING;
 
         List<Activity> justPlainSortedByTypeAndName = controller.sortListBasedOnRequest(listOfActivities, sortOrderEnum, "type");
         System.out.println(" justPlainSortedByTypeAndName " + justPlainSortedByTypeAndName);
         assertEquals("First activity in the list is wrong ", a2, justPlainSortedByTypeAndName.get(0));
         assertEquals("Second activity in the list is wrong ", a3, justPlainSortedByTypeAndName.get(1));
         assertEquals("Third activity in the list is wrong ", a1, justPlainSortedByTypeAndName.get(2));
         assertEquals("Forth activity in the list is wrong ", a4, justPlainSortedByTypeAndName.get(3));
     }
 
     @SuppressWarnings({ "unchecked" })
     public void testHandleRequestInternal() throws Exception {
         Activity a11, a12, a13, a14;
         ActivityType at11, at12, at13, at14;
         at11 = Fixtures.createActivityType("Desease Measure");
         at12 = Fixtures.createActivityType("Lab Test");
         at13 = Fixtures.createActivityType("Intervention");
         at14 = Fixtures.createActivityType("Another Type");
         a11 = Fixtures.createActivity("Activity One", "Code1", setId(123, createNamedInstance("Test Source 1", Source.class)), at11);
         a12 = Fixtures.createActivity("Two Activity", "Code2", setId(126, createNamedInstance("Test Source 2", Source.class)), at12);
         a13 = Fixtures.createActivity("My Activity Three", "Code3", setId(128, createNamedInstance("Test Source 3", Source.class)), at13);
         a14 = Fixtures.createActivity("My Forth Activity", "Code4", setId(129, createNamedInstance("Test Source 4", Source.class)), at14);
         List<Activity> listOfActivitiesLocal = new ArrayList<Activity>();
         listOfActivitiesLocal.add(a11);
         listOfActivitiesLocal.add(a12);
         listOfActivitiesLocal.add(a13);
         listOfActivitiesLocal.add(a14);
         List<ActivityType> listOfActivityTypes = new ArrayList<ActivityType>();
         listOfActivityTypes.add(at11);
         listOfActivityTypes.add(at12);
         listOfActivityTypes.add(at13);
         listOfActivityTypes.add(at14);
 
         Source source = setId(12, Fixtures.createSource("My Test Source"));
         request.setParameter("d-16544-p","1");
         request.addParameter("d-16544-n","1");
         request.addParameter("d-16544-s","name");
         request.addParameter("d-16544-o","1");
         request.setMethod("POST");
         request.addParameter("sourceId", source.getId().toString());
         request.addParameter("index", "0");
 
         Map<String, Object> actualModel;
         expect(activityDao.getBySourceId(12)).andReturn(listOfActivitiesLocal);
         expect(activityTypeDao.getAll()).andReturn(listOfActivityTypes);
 
         replayMocks();
         actualModel = controller.handleRequestInternal(request, response).getModel();
         verifyMocks();
 
         assertTrue("Missing model object", actualModel.containsKey("activitiesPerSource"));
         List<Activity> activitiesFromModel = (List<Activity>) actualModel.get("activitiesPerSource");
         assertEquals("Sorting didn't work on activities", a12, activitiesFromModel.get(3));
         assertEquals("Sorting didn't work on activities", a14, activitiesFromModel.get(2));
         assertEquals("Sorting didn't work on activities", a13, activitiesFromModel.get(1));
         assertEquals("Sorting didn't work on activities", a11, activitiesFromModel.get(0));
         assertTrue("Missing model object", actualModel.containsKey("activityTypes"));
         List<ActivityType> types = (List<ActivityType>) actualModel.get("activityTypes");
         assertEquals("Activity type wasn't added ", 4, types.size());
         assertEquals("Incorrect showNext model object ", false, actualModel.get("showNext"));
         assertEquals("Incorrect numberOfPages model object ", 1, actualModel.get("numberOfPages"));
     }
 
     @SuppressWarnings({"unchecked"})
    public void testSourceAllForGET() throws Exception {
         Activity a11, a12;
         ActivityType at11, at12;
         at11 = Fixtures.createActivityType("Desease Measure");
         at12 = Fixtures.createActivityType("Lab Test");
         a11 = Fixtures.createActivity("Activity One", "Code1", setId(123, createNamedInstance("Test Source 1", Source.class)), at11);
         a12 = Fixtures.createActivity("Two Activity", "Code2", setId(126, createNamedInstance("Test Source 2", Source.class)), at12);
         List<Activity> listOfActivitiesLocal = new ArrayList<Activity>();
         listOfActivitiesLocal.add(a11);
         listOfActivitiesLocal.add(a12);
         List<ActivityType> listOfActivityTypes = new ArrayList<ActivityType>();
         listOfActivityTypes.add(at11);
         listOfActivityTypes.add(at12);
 
        Source s = Fixtures.createSource("All sources");

        request.addParameter("sourceId", "selectAll");
         request.addParameter("index", "0");
         request.setMethod("GET");
 
         List<Source> sources = new ArrayList<Source>();
         sources.add(s);
         expect(activityDao.getAll()).andReturn(listOfActivitiesLocal);
         expect(activityTypeDao.getAll()).andReturn(listOfActivityTypes);
         expect(sourceDao.getAll()).andReturn(sources);
         Map<String, Object> actualModel;
 
         replayMocks();
             actualModel = controller.handleRequestInternal(request, response).getModel();
         verifyMocks();
 
         assertTrue("Missing model object", actualModel.containsKey("activitiesPerSource"));
         assertTrue("Missing source object", actualModel.containsKey("sourceId"));
        assertEquals("Wrong source", "selectAll", actualModel.get("sourceId"));
     }
 
     @SuppressWarnings({ "unchecked" })
     public void testHandleRequestInternalWithNegativeIndex() throws Exception {
         Activity a11, a12, a13, a14;
         ActivityType at11, at12, at13, at14;
         at11 = Fixtures.createActivityType("Desease Measure");
         at12 = Fixtures.createActivityType("Lab Test");
         at13 = Fixtures.createActivityType("Intervention");
         at14 = Fixtures.createActivityType("Another Type");
         a11 = Fixtures.createActivity("Activity One", "Code1", setId(123, createNamedInstance("Test Source 1", Source.class)), at11);
         a12 = Fixtures.createActivity("Two Activity", "Code2", setId(126, createNamedInstance("Test Source 2", Source.class)), at12);
         a13 = Fixtures.createActivity("My Activity Three", "Code3", setId(128, createNamedInstance("Test Source 3", Source.class)), at13);
         a14 = Fixtures.createActivity("My Forth Activity", "Code4", setId(129, createNamedInstance("Test Source 4", Source.class)), at14);
         List<Activity> listOfActivitiesLocal = new ArrayList<Activity>();
         listOfActivitiesLocal.add(a11);
         listOfActivitiesLocal.add(a12);
         listOfActivitiesLocal.add(a13);
         listOfActivitiesLocal.add(a14);
         List<ActivityType> listOfActivityTypes = new ArrayList<ActivityType>();
         listOfActivityTypes.add(at11);
         listOfActivityTypes.add(at12);
         listOfActivityTypes.add(at13);
         listOfActivityTypes.add(at14);
 
         Source source = setId(12, Fixtures.createSource("My Test Source"));
         request.setParameter("d-16544-p","1");
         request.addParameter("d-16544-n","1");
         request.addParameter("d-16544-s","type");
         request.addParameter("d-16544-o","2");
         request.setMethod("POST");
         request.addParameter("sourceId", source.getId().toString());
         request.addParameter("index", "-200");
 
         Map<String, Object> actualModel;
         expect(activityDao.getBySourceId(12)).andReturn(listOfActivitiesLocal);
         expect(activityTypeDao.getAll()).andReturn(listOfActivityTypes);
 
         replayMocks();
         actualModel = controller.handleRequestInternal(request, response).getModel();
         verifyMocks();
 
         assertTrue("Missing model object", actualModel.containsKey("activitiesPerSource"));
         List<Activity> activitiesFromModel = (List<Activity>) actualModel.get("activitiesPerSource");
         assertEquals("Sorting didn't work on activities", a12, activitiesFromModel.get(0));
         assertEquals("Sorting didn't work on activities", a13, activitiesFromModel.get(1));
         assertEquals("Sorting didn't work on activities", a11, activitiesFromModel.get(2));
         assertEquals("Sorting didn't work on activities", a14, activitiesFromModel.get(3));
         assertTrue("Missing model object", actualModel.containsKey("activityTypes"));
         List<ActivityType> types = (List<ActivityType>) actualModel.get("activityTypes");
         assertEquals("Activity type wasn't added ", 4, types.size());
         assertEquals("Incorrect showNext model object ", false, actualModel.get("showNext"));
         assertEquals("Incorrect numberOfPages model object ", 1, actualModel.get("numberOfPages"));
         assertTrue("Missing model object", actualModel.containsKey("showtable"));
     }
 
 }
