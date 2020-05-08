 package edu.northwestern.bioinformatics.studycalendar.restlets;
 
 import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
 import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.PopulationDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
 import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
 import edu.northwestern.bioinformatics.studycalendar.domain.Period;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivityLabel;
 import edu.northwestern.bioinformatics.studycalendar.domain.Population;
 import edu.northwestern.bioinformatics.studycalendar.domain.Role;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
 import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
 import edu.northwestern.bioinformatics.studycalendar.service.TestingTemplateService;
 import org.restlet.data.MediaType;
 import org.restlet.data.Method;
 import org.restlet.data.Status;
 
 import java.io.IOException;
 
 import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
 import static org.easymock.EasyMock.*;
 
 /**
  * @author Rhett Sutphin
  */
 public class PlannedActivityResourceTest extends AuthorizedResourceTestCase<PlannedActivityResource> {
     private AmendmentService amendmentService;
     private AmendedTemplateHelper helper;
     private ActivityDao activityDao;
     private PopulationDao populationDao;
     private PlannedActivityDao plannedActivityDao;
 
     private PlannedActivity plannedActivity;
     private Activity activity;
     private Population population;
     private Study study;
     private Period period;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
        study = createBasicTemplate("F");
         Epoch epoch = study.getPlannedCalendar().getEpochs().get(0);
         epoch.setName("testEpoch");
         StudySegment segment = epoch.getStudySegments().get(1);
         segment.setName("testSegment");
         period = setId(1, createPeriod("testPeriod", 4, 8, 12));
         segment.addPeriod(period);
         plannedActivity = createPlannedActivity("testActivity", 4);
         period.addPlannedActivity(plannedActivity);
         activity = plannedActivity.getActivity();
         population = createPopulation("T", "Squares");
 
         helper = registerMockFor(AmendedTemplateHelper.class);
         helper.setRequest(request);
         expectLastCall().atLeastOnce();
        expect(helper.getAmendedTemplateOrNull()).andStubReturn(study);
         expect(helper.getRealStudy()).andReturn(study).anyTimes();
         amendmentService = registerMockFor(AmendmentService.class);
         activityDao = registerMockFor(ActivityDao.class);
         populationDao = registerMockFor(PopulationDao.class);
         plannedActivityDao = registerMockFor(PlannedActivityDao.class);
     }
 
     @Override
     protected PlannedActivityResource createAuthorizedResource() {
         PlannedActivityResource res = new PlannedActivityResource();
         res.setAmendedTemplateHelper(helper);
         res.setAmendmentService(amendmentService);
         res.setActivityDao(activityDao);
         res.setPopulationDao(populationDao);
         res.setPlannedActivityDao(plannedActivityDao);
         res.setTemplateService(new TestingTemplateService());
         return res;
     }
 
     public void testEnabledMethods() throws Exception {
         expectSuccessfulDrillDown();
         replayMocks();
         assertAllowedMethods("PUT", "DELETE", "GET");
     }
 
     public void testPutAllowedForStudyCoordinator() throws Exception {
         expectSuccessfulDrillDown();
         replayMocks();
         assertLegacyRolesAllowedForMethod(Method.PUT, Role.STUDY_COORDINATOR);
         verifyMocks();
     }
 
     public void testDeleteAllowedForStudyCoordinator() throws Exception {
         expectSuccessfulDrillDown();
         replayMocks();
         assertLegacyRolesAllowedForMethod(Method.DELETE, Role.STUDY_COORDINATOR);
         verifyMocks();
     }
 
     public void testPutPreventedUnlessInDevelopment() throws Exception {
         expectMinimumPutEntity();
         expectSuccessfulDrillDown();
         expect(helper.isDevelopmentRequest()).andReturn(false);
 
         doPut();
 
         assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST);
         assertEntityTextContains("You can only update planned activities in the development version of the template");
     }
 
     public void testPutCreatesANewPlannedActivityIfNotPresent() throws Exception {
         String expectedIdent = "unique-in-all-the-world";
         UriTemplateParameters.PLANNED_ACTIVITY_IDENTIFIER.putIn(request, expectedIdent);
         expect(helper.drillDown(PlannedActivity.class)).andThrow(new AmendedTemplateHelper.NotFound("No such thing"));
         expect(helper.drillDown(Period.class)).andReturn(period);
         expectIsDevelopmentRequest();
 
         expectMinimumPutEntity();
         expectFindActivityByCodeAndSource();
         expect(plannedActivityDao.getByGridId(expectedIdent)).andReturn(null);
 
         PlannedActivity expectedPlannedActivity = setGridId(expectedIdent, createPlannedActivity(activity, 6, 8));
 
         expect(amendmentService.updateDevelopmentAmendmentAndSave(period, Add.create(expectedPlannedActivity))).andReturn(study);
 
         doPut();
 
         assertResponseStatus(Status.SUCCESS_OK);
         assertEquals(MediaType.APPLICATION_WWW_FORM, response.getEntity().getMediaType());
         assertEntityTextContains("day=6");
     }
 
     public void testPutFailsIfPlannedActivityExistsButDoesNotMatchTheRemainderOfTheUrl() throws Exception {
         String expectedIdent = "unique-in-all-the-world";
         UriTemplateParameters.PLANNED_ACTIVITY_IDENTIFIER.putIn(request, expectedIdent);
         expect(helper.drillDown(PlannedActivity.class)).andThrow(
             new AmendedTemplateHelper.NotFound("No such thing"));
         expectIsDevelopmentRequest();
 
         expectMinimumPutEntity();
         expect(plannedActivityDao.getByGridId(expectedIdent)).andReturn(new PlannedActivity());
 
         doPut();
 
         assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST);
         assertEntityTextContains(
             "The planned activity unique-in-all-the-world exists, but is not part of the designated study");
     }
 
     public void testPutUpdatesAllPlannedActivityProperties() throws Exception {
         expectWriteOperationMayProceed();
         expectMinimumPutEntity();
         expectRequestEntityFormAttribute("details", "sharp");
         expectRequestEntityFormAttribute("condition", "enough");
         expectRequestEntityFormAttribute("population", population.getNaturalKey());
         expectFindActivityByCodeAndSource();
         expectFindPopulation();
 
         expect(amendmentService.updateDevelopmentAmendmentAndSave(
             plannedActivity,
             PropertyChange.create("day", 4, 6),
             PropertyChange.create("activity", activity, activity),
             PropertyChange.create("population", null, population),
             PropertyChange.create("details", null, "sharp"),
             PropertyChange.create("condition", null, "enough"),
             PropertyChange.create("weight", null, 8)
         )).andReturn(study);
 
         doPut();
 
         assertResponseStatus(Status.SUCCESS_OK);
         assertEntityTextContains("day=6");
     }
 
     public void testPutRecordsAddsForNewAllRepsLabels() throws Exception {
         expectWriteOperationMayProceed();
         expectMinimumPutEntity();
         expectRequestEntityFormAttribute("label", "jones");
         expectFindActivityByCodeAndSource();
 
         expect(amendmentService.updateDevelopmentAmendmentAndSave(
             plannedActivity,
             PropertyChange.create("day", 4, 6),
             PropertyChange.create("activity", activity, activity),
             PropertyChange.create("population", null, null),
             PropertyChange.create("details", null, null),
             PropertyChange.create("condition", null, null),
             PropertyChange.create("weight", null, 8),
             Add.create(Fixtures.createPlannedActivityLabel("jones"))
         )).andReturn(study);
 
         doPut();
 
         assertResponseStatus(Status.SUCCESS_OK);
         assertEntityTextContains("day=6");
     }
 
     public void testPutRecordsAddsForNewRepsOfExistingLabels() throws Exception {
         expectWriteOperationMayProceed();
         expectMinimumPutEntity();
         expectFindActivityByCodeAndSource();
 
         expectRequestEntityFormAttribute("label", "jones;1 4");
         plannedActivity.addPlannedActivityLabel(Fixtures.createPlannedActivityLabel("jones", 4));
 
         expect(amendmentService.updateDevelopmentAmendmentAndSave(
             plannedActivity,
             PropertyChange.create("day", 4, 6),
             PropertyChange.create("activity", activity, activity),
             PropertyChange.create("population", null, null),
             PropertyChange.create("details", null, null),
             PropertyChange.create("condition", null, null),
             PropertyChange.create("weight", null, 8),
             Add.create(Fixtures.createPlannedActivityLabel("jones", 1))
         )).andReturn(study);
 
         doPut();
 
         assertResponseStatus(Status.SUCCESS_OK);
         assertEntityTextContains("day=6");
     }
 
     public void testPutRecordsRemoveWhenDeletingExistingAllRepsLabel() throws Exception {
         expectWriteOperationMayProceed();
         expectMinimumPutEntity();
         expectFindActivityByCodeAndSource();
 
         PlannedActivityLabel existing = Fixtures.createPlannedActivityLabel("thirteen");
         plannedActivity.addPlannedActivityLabel(existing);
 
         expect(amendmentService.updateDevelopmentAmendmentAndSave(
             plannedActivity,
             PropertyChange.create("day", 4, 6),
             PropertyChange.create("activity", activity, activity),
             PropertyChange.create("population", null, null),
             PropertyChange.create("details", null, null),
             PropertyChange.create("condition", null, null),
             PropertyChange.create("weight", null, 8),                
             Remove.create(existing)
         )).andReturn(study);
 
         doPut();
 
         assertResponseStatus(Status.SUCCESS_OK);
         assertEntityTextContains("day=6");
     }
 
     public void testPutRecordsRemoveWhenDeletingExistingSingleRepLabel() throws Exception {
         expectWriteOperationMayProceed();
         expectMinimumPutEntity();
         expectFindActivityByCodeAndSource();
 
         expectRequestEntityFormAttribute("label", "city;1");
         PlannedActivityLabel existing1 = Fixtures.createPlannedActivityLabel("city", 1);
         PlannedActivityLabel existing3 = Fixtures.createPlannedActivityLabel("city", 3);
         plannedActivity.addPlannedActivityLabel(existing1);
         plannedActivity.addPlannedActivityLabel(existing3);
 
         expect(amendmentService.updateDevelopmentAmendmentAndSave(
             plannedActivity,
             PropertyChange.create("day", 4, 6),
             PropertyChange.create("activity", activity, activity),
             PropertyChange.create("population", null, null),
             PropertyChange.create("details", null, null),
             PropertyChange.create("condition", null, null),
             PropertyChange.create("weight", null, 8),
             Remove.create(existing3)
         )).andReturn(study);
 
         doPut();
 
         assertResponseStatus(Status.SUCCESS_OK);
         assertEntityTextContains("day=6");
     }
 
     public void testDelete404sWhenPlannedActivityNotFound() throws Exception {
         expect(helper.drillDown(PlannedActivity.class)).
             andThrow(new AmendedTemplateHelper.NotFound("No such thing in there"));
 
         doDelete();
 
         assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
         assertEntityTextContains("No such thing in there");
     }
 
     public void testDeletePreventedUnlessInDevelopment() throws Exception {
         expectSuccessfulDrillDown();
         expect(helper.isDevelopmentRequest()).andReturn(false);
 
         doDelete();
 
         assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST);
         assertEntityTextContains("You can only delete planned activities from the development version of the template");
     }
 
     public void testDelete() throws Exception {
         expect(helper.drillDown(PlannedActivity.class)).andReturn(plannedActivity);
         expectIsDevelopmentRequest();
 
         expect(amendmentService.updateDevelopmentAmendmentAndSave(period, Remove.create(plannedActivity))).andReturn(study);
 
         doDelete();
 
         assertResponseStatus(Status.SUCCESS_NO_CONTENT);
     }
 
     private void expectMinimumPutEntity() throws IOException {
         expectRequestEntityFormAttribute("day", "6");
         expectRequestEntityFormAttribute("weight", "8");
         expectRequestEntityFormAttribute("activity-code", activity.getCode());
         expectRequestEntityFormAttribute("activity-source", activity.getSource().getNaturalKey());
     }
 
     private void expectFindActivityByCodeAndSource() {
         expect(activityDao.getByCodeAndSourceName(activity.getCode(), activity.getSource().getNaturalKey())).andReturn(activity);
     }
 
     private void expectFindPopulation() {
         expect(populationDao.getByAbbreviation(study, population.getAbbreviation())).andReturn(population);
     }
 
     ////// EXPECTATIONS
 
     private void expectSuccessfulDrillDown() {
         expect(helper.drillDown(PlannedActivity.class)).andReturn(plannedActivity);
     }
 
     private void expectIsDevelopmentRequest() {
         expect(helper.isDevelopmentRequest()).andReturn(true);
     }
 
     private void expectWriteOperationMayProceed() {
         expectSuccessfulDrillDown();
         expectIsDevelopmentRequest();
     }
 }
