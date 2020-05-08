 package edu.northwestern.bioinformatics.studycalendar.restlets;
 
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
 import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
 import edu.northwestern.bioinformatics.studycalendar.domain.Site;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
 import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
 import edu.northwestern.bioinformatics.studycalendar.service.RegistrationService;
 import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
 import edu.northwestern.bioinformatics.studycalendar.service.presenter.Registration;
 import edu.northwestern.bioinformatics.studycalendar.xml.CapturingStudyCalendarXmlFactoryStub;
 import edu.nwu.bioinformatics.commons.DateUtils;
 import org.restlet.data.Method;
 import org.restlet.data.Status;
 
 import java.util.Collection;
 
 import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
 import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
 import static java.util.Calendar.APRIL;
 import static org.easymock.EasyMock.expect;
 
 /**
  * @author Rhett Sutphin
  */
 public class RegistrationsResourceTest extends AuthorizedResourceTestCase<RegistrationsResource> {
     private static final String STUDY_IDENTIFIER = "EC golf";
     private static final String STUDY_IDENTIFIER_ENCODED = "EC+golf";
     private static final String SITE_NAME = "AgU";
     public static final String DESIRED_ASSIGNMENT_ID = "DC";
 
     private Study study;
     private Site site;
     private StudySite studySite;
 
     private StudyDao studyDao;
     private SiteDao siteDao;
     private SubjectService subjectService;
     private RegistrationService registrationService;
     private CapturingStudyCalendarXmlFactoryStub<StudySubjectAssignment> assignmentSerializerStub;
 
     @Override
     public void setUp() throws Exception {
         super.setUp();
         study = createBasicTemplate();
         study.setAssignedIdentifier(STUDY_IDENTIFIER);
         site = createSite(SITE_NAME, SITE_NAME);
         studySite = createStudySite(study, site);
 
         studyDao = registerDaoMockFor(StudyDao.class);
         siteDao = registerDaoMockFor(SiteDao.class);
         subjectService = registerMockFor(SubjectService.class);
         registrationService = registerMockFor(RegistrationService.class);
         assignmentSerializerStub = new CapturingStudyCalendarXmlFactoryStub<StudySubjectAssignment>();
 
         request.getAttributes().put(UriTemplateParameters.STUDY_IDENTIFIER.attributeName(), STUDY_IDENTIFIER_ENCODED);
         request.getAttributes().put(UriTemplateParameters.SITE_IDENTIFIER.attributeName(), SITE_NAME);
 
         expect(studyDao.getByAssignedIdentifier(STUDY_IDENTIFIER)).andStubReturn(study);
         expect(siteDao.getByAssignedIdentifier(SITE_NAME)).andStubReturn(site);
     }
 
     @Override
     @SuppressWarnings({ "unchecked" })
     protected RegistrationsResource createAuthorizedResource() {
         RegistrationsResource res = new RegistrationsResource();
         res.setStudyDao(studyDao);
         res.setSiteDao(siteDao);
         res.setSubjectService(subjectService);
         res.setRegistrationService(registrationService);
         res.setXmlSerializer(xmlSerializer);
         return res;
     }
 
     ////// GET
 
     @SuppressWarnings({ "unchecked", "RawUseOfParameterizedType" })
     public void testGetXmlForKnownStudySite() throws Exception {
         studySite.addStudySubjectAssignment(createAssignment());
         studySite.addStudySubjectAssignment(createAssignment());
 
         getResource().setAssignmentXmlSerializer(assignmentSerializerStub);
         doGet();
         assertResponseStatus(Status.SUCCESS_OK);
         assertResponseIsStubbedXml();
 
         Object serialized = assignmentSerializerStub.getLastObjectStringified();
         assertNotNull("Stringified object is null", serialized);
         assertTrue("Serialized object should have been a collection", serialized instanceof Collection);
         assertTrue("Serialized collection contents should be registrations",
             ((Collection) serialized).iterator().next() instanceof StudySubjectAssignment);
         assertEquals("Wrong number of entries in serialized collection", 2, ((Collection) serialized).size());
     }
 
     private StudySubjectAssignment createAssignment() {
         StudySubjectAssignment assignment = new StudySubjectAssignment();
         assignment.setStudySite(studySite);
         ScheduledCalendar cal = new ScheduledCalendar();
         assignment.setScheduledCalendar(cal);
         ScheduledStudySegment seg = new ScheduledStudySegment();
         cal.getScheduledStudySegments().add(seg);
         seg.setStudySegment(study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0));
         return assignment;
     }
 
     public void testGet404sOnMissingStudy() throws Exception {
         expect(studyDao.getByAssignedIdentifier(STUDY_IDENTIFIER)).andReturn(null);
         doGet();
         assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
     }
 
     public void testGet404sOnMissingSite() throws Exception {
         expect(siteDao.getByAssignedIdentifier(SITE_NAME)).andReturn(null);
         doGet();
         assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
     }
 
     public void testGet404sOnMissingStudySite() throws Exception {
         study.getStudySites().clear();
         site.getStudySites().clear();
         doGet();
         assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
     }
 
     public void testGetAndPostAllowed() throws Exception {
         assertAllowedMethods("POST", "GET");
     }
 
     public void testSubjectRolesMayView() {
         assertRolesAllowedForMethod(Method.GET,
             STUDY_TEAM_ADMINISTRATOR,
             STUDY_SUBJECT_CALENDAR_MANAGER,
             DATA_READER);
     }
 
     public void testOnlySscmMayPost() {
         assertRolesAllowedForMethod(Method.POST,
             STUDY_SUBJECT_CALENDAR_MANAGER);
     }
 
     ////// POST
 
     public void testPost404sOnMissingStudySite() throws Exception {
         study.getStudySites().clear();
         site.getStudySites().clear();
 
         doPost();
 
         assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
     }
 
     public void testPostAddsAssignmentAndReturnsNewUri() throws Exception {
         Registration posted = testRegistrationBuilder().toRegistration();
 
         expectReadXmlFromRequestAs(posted);
         expectResolvedRegistration(posted);
         expect(subjectService.assignSubject(studySite, posted)).
             andReturn(setGridId(DESIRED_ASSIGNMENT_ID, new StudySubjectAssignment()));
 
         doPost();
 
         assertResponseStatus(Status.SUCCESS_CREATED);
         assertEquals(ROOT_URI + "/studies/EC+golf/schedules/" + DESIRED_ASSIGNMENT_ID,
             response.getLocationRef().getTargetRef().toString());
     }
 
     public void testPostAddsUsesCurrentUserAsCoordinatorIfNoneSpecified() throws Exception {
         Registration posted = testRegistrationBuilder().
             manager(null).toRegistration();
 
         Registration expectedAssigned = testRegistrationBuilder().
             manager(getCurrentUser()).toRegistration();
 
         expectReadXmlFromRequestAs(posted);
         expectResolvedRegistration(posted);
         expect(subjectService.assignSubject(studySite, expectedAssigned)).
             andReturn(setGridId(DESIRED_ASSIGNMENT_ID, new StudySubjectAssignment()));
 
         doPost();
 
         assertResponseStatus(Status.SUCCESS_CREATED);
     }
 
    public void testPostIs422IfRegistrationInvalid() throws Exception {
        Registration posted = testRegistrationBuilder().toRegistration();
        expectReadXmlFromRequestAs(posted);
        expect(registrationService.resolveRegistration(posted, studySite)).
            andThrow(new StudyCalendarValidationException("try harder next time"));

        doPost();

        assertResponseStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY,
            "try harder next time");
    }

    public void testPostReturnsErrorCodeIfUserNotAuthorized() throws Exception {
        Registration posted = testRegistrationBuilder().toRegistration();
        expectReadXmlFromRequestAs(posted);
        expect(registrationService.resolveRegistration(posted, studySite)).
            andThrow(new StudyCalendarValidationException("jo has insufficient privilege to create new subject."));

        doPost();

        assertResponseStatus(Status.CLIENT_ERROR_FORBIDDEN, "jo has insufficient privilege to create new subject.");
    }

     private Registration.Builder testRegistrationBuilder() {
         return new Registration.Builder().
             firstStudySegment(study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0)).
             date(DateUtils.createDate(2005, APRIL, 5)).
             subject(setId(4, new Subject())).
             desiredAssignmentId(DESIRED_ASSIGNMENT_ID).
             manager(AuthorizationObjectFactory.createPscUser("jo"));
     }
 
     ////// Helper Methods
 
     private void expectResolvedRegistration(Registration registration) {
         expect(registrationService.resolveRegistration(registration, studySite)).andReturn(registration);
     }
 }
