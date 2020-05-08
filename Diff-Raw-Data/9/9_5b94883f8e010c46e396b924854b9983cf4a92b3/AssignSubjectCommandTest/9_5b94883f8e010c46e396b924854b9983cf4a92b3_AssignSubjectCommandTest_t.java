 package edu.northwestern.bioinformatics.studycalendar.web;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.*;
 import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
 import edu.northwestern.bioinformatics.studycalendar.core.*;
 import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
 import static edu.nwu.bioinformatics.commons.DateUtils.createDate;
 import org.apache.commons.lang.StringUtils;
 import static org.apache.commons.lang.StringUtils.isEmpty;
 import org.easymock.IArgumentMatcher;
 import org.easymock.classextension.EasyMock;
 import static org.easymock.classextension.EasyMock.expect;
 import org.springframework.validation.BindException;
 import org.springframework.validation.Errors;
 
 import java.util.*;
 import java.text.SimpleDateFormat;
 
 /**
  * @author Rhett Sutphin
  */
 public class AssignSubjectCommandTest extends StudyCalendarTestCase {
     private AssignSubjectCommand command;
     private SubjectService subjectService;
     private SubjectDao subjectDao;
     private Subject subject;
     private String EXISTING = "existing";
     private String NEW = "new";
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         subjectService = registerMockFor(SubjectService.class);
         subjectDao = registerDaoMockFor(SubjectDao.class);
         command = new AssignSubjectCommand();
         command.setSubjectService(subjectService);
         command.setSubjectDao(subjectDao);
         String dateOfBirthString = "12/01/2008";
         Date dateOfBirth = command.convertStringToDate(dateOfBirthString);
         subject = createSubject("11", "Fred", "Jones", dateOfBirth, Gender.MALE);
         subject.setGridId("grid_id_123");
 
         command.setFirstName(subject.getFirstName());
         command.setLastName(subject.getLastName());
         command.setPersonId(subject.getPersonId());
         command.setDateOfBirth(dateOfBirthString);
         command.setStartDate("01/02/2008");
         command.setRadioButton(NEW);
     }
 
     public void testAssignSubject() throws Exception {
         Study study = createNamedInstance("Study A", Study.class);
         Site site = createNamedInstance("Northwestern", Site.class);
         StudySite studySite = setId(14, createStudySite(study, site));
         StudySubjectAssignment assignment = new StudySubjectAssignment();
         Set<Population> populations = Collections.singleton(new Population());
         String studySubjectId = "SSId1";
 
         Date date = new Date();
         SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
         String stringDate = formatter.format(date);
         command.setStartDate(stringDate);
         command.setStudySubjectId(studySubjectId);
         command.setStudy(study);
         command.setSite(site);
         command.setStudySegment(setId(17, edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createNamedInstance("Worcestershire", StudySegment.class)));
         command.setPopulations(populations);
         assignment.setSubject(subject);
         subjectDao.save(subjectEq(subject));
 
 
         expect(subjectService.assignSubject(subjectEq(subject), EasyMock.eq(studySite), EasyMock.eq(command.getStudySegment()), EasyMock.eq(command.convertStringToDate(command.getStartDate())),
                EasyMock.eq(command.getStudySubjectId()), EasyMock.eq((User)null), (Set<Population>) EasyMock.notNull())).andReturn(assignment);
         subjectService.updatePopulations(assignment, populations);
         replayMocks();
 
         StudySubjectAssignment actual = command.assignSubject();
         verifyMocks();
 
         assertSame("Assignment should be the same", assignment, actual);
     }
 
     public void testValidateNew() throws Exception {
         command.setRadioButton(NEW);
         expect(subjectService.findSubjects(subjectEq(subject))).andReturn(Collections.singletonList(subject));
         replayMocks();
 
         BindException errors = new BindException(subject, StringUtils.EMPTY);
         command.validate(new BindException(subject, StringUtils.EMPTY));
         verifyMocks();
 
         assertFalse(errors.hasErrors());
     }
 
     public void testValidateExisting() throws Exception {
         command.setRadioButton(EXISTING);
         command.setIdentifier(subject.getPersonId());
         expect(subjectDao.findSubjectByGridOrPersonId(subject.getPersonId())).andReturn(subject);
         replayMocks();
 
         BindException errors = new BindException(subject, StringUtils.EMPTY);
         command.validate(new BindException(subject, StringUtils.EMPTY));
         verifyMocks();
 
         assertFalse(errors.hasErrors());
     }
 
 
     public void testValidateExistingByGridId() throws Exception {
         command.setRadioButton(EXISTING);
         command.setIdentifier(subject.getGridId());
         expect(subjectDao.findSubjectByGridOrPersonId(subject.getGridId())).andReturn(subject);
         replayMocks();
 
         BindException errors = new BindException(subject, StringUtils.EMPTY);
         command.validate(new BindException(subject, StringUtils.EMPTY));
         verifyMocks();
 
         assertFalse(errors.hasErrors());
     }
 
 
     public void testValidateWithExistingSubject() throws Exception {
         expect(subjectService.findSubjects(subjectEq(subject))).andReturn(Arrays.asList(subject, new Subject()));
         replayMocks();
 
         BindException errors = new BindException(subject, StringUtils.EMPTY);
         command.validate(errors);
         verifyMocks();
 
         assertTrue(errors.hasErrors());
         assertEquals("Wrong error count", 1, errors.getErrorCount());
         assertEquals("Wrong error code", "error.person.id.already.exists", errors.getFieldError().getCode());
     }
 
     public void testValidateWithEmptyRadioButtonSelected() throws Exception {
         command.setRadioButton(null);
         replayMocks();
 
         BindException errors = new BindException(command, StringUtils.EMPTY);
         command.validate(errors);
         verifyMocks();
         assertTrue(errors.hasErrors());
         assertEquals("Wrong error count", 1, errors.getErrorCount());
         assertEquals("Wrong error code", "error.subject.please.select.a.subject", errors.getFieldError().getCode());
     }
 
     public void testValidateWithRadioButtonExistingAndNullId() throws Exception {
         command.setRadioButton("existing");
         command.setIdentifier(null);
         replayMocks();
 
         BindException errors = new BindException(command, StringUtils.EMPTY);
         command.validate(errors);
         verifyMocks();
         assertTrue(errors.hasErrors());
         assertEquals("Wrong error count", 1, errors.getErrorCount());
         assertEquals("Wrong error code", "error.subject.please.select.a.subject", errors.getFieldError().getCode());
     }
 
     public void testValidateWithRadioButtonExistingAndEmptyId() throws Exception {
         command.setRadioButton("existing");
         command.setIdentifier("");
         replayMocks();
 
         BindException errors = new BindException(command, StringUtils.EMPTY);
         command.validate(errors);
         verifyMocks();
         assertTrue(errors.hasErrors());
         assertEquals("Wrong error count", 1, errors.getErrorCount());
         assertEquals("Wrong error code", "error.subject.please.select.a.subject", errors.getFieldError().getCode());
     }
 
     public void testValidateWithRadioButtonExistingAndEmptyStartDate() throws Exception {
         command.setRadioButton("existing");
         command.setIdentifier("123");
         command.setStartDate(null);
         replayMocks();
 
         BindException errors = new BindException(command, StringUtils.EMPTY);
         command.validate(errors);
         verifyMocks();
         assertTrue(errors.hasErrors());
         assertEquals("Wrong error count", 1, errors.getErrorCount());
         assertEquals("Wrong error code", "error.subject.assignment.please.enter.a.start.date", errors.getFieldError().getCode());
     }
 
     public void testValidateNewWithEmptyPersonIdAndFirstName() throws Exception {
         command.setRadioButton("new");
         command.setPersonId("");
         command.setFirstName("");
         replayMocks();
 
         BindException errors = new BindException(command, StringUtils.EMPTY);
         command.validate(errors);
         verifyMocks();
         assertTrue(errors.hasErrors());
         assertEquals("Wrong error count", 1, errors.getErrorCount());
         assertEquals("Wrong error code", "error.subject.assignment.please.enter.person.id.and.or.first.last.birthdate", errors.getFieldError().getCode());
 
     }
 
     public void testValidateNewWithEmptyPersonIdAndLastName() throws Exception {
         command.setRadioButton("new");
         command.setPersonId("");
         command.setLastName("");
         replayMocks();
 
         BindException errors = new BindException(command, StringUtils.EMPTY);
         command.validate(errors);
         verifyMocks();
         assertTrue(errors.hasErrors());
         assertEquals("Wrong error count", 1, errors.getErrorCount());
         assertEquals("Wrong error code", "error.subject.assignment.please.enter.person.id.and.or.first.last.birthdate", errors.getFieldError().getCode());
 
     }
 
     public void testValidateNewWithEmptyPersonIdAndDateOfBirth() throws Exception {
         command.setRadioButton("new");
         command.setPersonId("");
         command.setDateOfBirth(null);
         replayMocks();
 
         BindException errors = new BindException(command, StringUtils.EMPTY);
         command.validate(errors);
         verifyMocks();
         assertTrue(errors.hasErrors());
         assertEquals("Wrong error count", 1, errors.getErrorCount());
         assertEquals("Wrong error code", "error.subject.assignment.please.enter.person.id.and.or.first.last.birthdate", errors.getFieldError().getCode());
 
     }
 
     public void testValidateNewWithEmptyPersonIdAndStartDate() throws Exception {
         command.setRadioButton("new");
         command.setPersonId("123");
         command.setStartDate(null);
         replayMocks();
 
         BindException errors = new BindException(command, StringUtils.EMPTY);
         command.validate(errors);
         verifyMocks();
         assertTrue(errors.hasErrors());
         assertEquals("Wrong error count", 1, errors.getErrorCount());
         assertEquals("Wrong error code", "error.subject.assignment.please.enter.a.start.date", errors.getFieldError().getCode());
 
     }
 
     public void testValidateWithExistingSubjectNoPersonId() throws Exception {
         subject.setPersonId(null);
         command.setPersonId(null);
 
         expect(subjectService.findSubjects(subjectEq(subject))).andReturn(Arrays.asList(subject, new Subject()));
         replayMocks();
 
         BindException errors = new BindException(subject, StringUtils.EMPTY);
         command.validate(errors);
         verifyMocks();
 
         assertTrue(errors.hasErrors());
         assertEquals("Wrong error count", 1, errors.getErrorCount());
         assertEquals("Wrong error code", "error.person.last.name.already.exists", errors.getFieldError().getCode());
     }
 
     ////// CUSTOM MATCHERS
 
     private static Subject subjectEq(Subject expectedSubject) {
         EasyMock.reportMatcher(new SubjectMatcher(expectedSubject));
         return null;
     }
 
     private static class SubjectMatcher implements IArgumentMatcher {
         private Subject expectedSubject;
 
         public SubjectMatcher(Subject expectedSubject) {
             this.expectedSubject = expectedSubject;
         }
 
         public boolean matches(Object object) {
             Subject actual = (Subject) object;
             return (expectedSubject.getPersonId() != null && actual.getPersonId() != null && expectedSubject.getPersonId().equals(actual.getPersonId())) ||
                     (expectedSubject.getFirstName().equals(actual.getFirstName()) &&
                             (expectedSubject.getLastName().equals(actual.getLastName())) &&
                             (expectedSubject.getDateOfBirth().equals(actual.getDateOfBirth())));
 
         }
         public void appendTo(StringBuffer sb) {
             sb.append("Subject=").append(expectedSubject);
         }
     }
 }
