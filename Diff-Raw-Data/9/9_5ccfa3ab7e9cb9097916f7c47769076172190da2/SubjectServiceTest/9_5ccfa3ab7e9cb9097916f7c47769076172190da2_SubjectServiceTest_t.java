 package edu.northwestern.bioinformatics.studycalendar.service;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
 import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
 import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.*;
 import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.AmendmentApproval;
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Canceled;
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Occurred;
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Scheduled;
 import edu.northwestern.bioinformatics.studycalendar.core.*;
 import edu.nwu.bioinformatics.commons.DateUtils;
 import static edu.nwu.bioinformatics.commons.DateUtils.createDate;
 import edu.nwu.bioinformatics.commons.testing.CoreTestCase;
 import gov.nih.nci.cabig.ctms.lang.DateTools;
 import org.easymock.EasyMock;
 
 import static java.util.Arrays.asList;
 import static org.easymock.EasyMock.notNull;
 import static org.easymock.classextension.EasyMock.expect;
 import static org.easymock.classextension.EasyMock.expectLastCall;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import static java.util.Calendar.*;
 import static java.util.Collections.singletonList;
 
 /**
  * @author Rhett Sutphin
  */
 public class SubjectServiceTest extends StudyCalendarTestCase {
     private SubjectService service;
 
     private SubjectDao subjectDao;
     private AmendmentService amendmentService;
 
     private User user;
 
     private StudySegment studySegment;
     private Activity a1;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         subjectDao = registerDaoMockFor(SubjectDao.class);
         amendmentService = registerMockFor(AmendmentService.class);
        
         service = new SubjectService();
         service.setSubjectDao(subjectDao);
         service.setAmendmentService(amendmentService);
 
         Epoch epoch = Epoch.create("Epoch", "A", "B", "C");
         studySegment = epoch.getStudySegments().get(0);
         Period p1 = createPeriod("P1", 1, 7, 3);
         Period p2 = createPeriod("P2", 3, 1, 1);
         Period p3 = createPeriod("P3", 8, 28, 2);
         studySegment.addPeriod(p1);
         studySegment.addPeriod(p2);
         studySegment.addPeriod(p3);
 
         a1 = new Activity();
         a1.setId(1);
         a1.setName("CBC");
                                                                                                               // days
         p1.addPlannedActivity(setId(1, createPlannedActivity("CBC", 1, "CBC Details")));                      // 1, 8, 15
         p1.addPlannedActivity(setId(2, createPlannedActivity("Vitals", 3, "Vitals Details")));                // 3, 10, 17
         p2.addPlannedActivity(setId(3, createPlannedActivity("Questionnaire", 1, "Questionnaire Details")));  // 3
         p3.addPlannedActivity(setId(4, createPlannedActivity("Infusion", 1, "Infusion Details")));            // 8, 36
         p3.addPlannedActivity(setId(5, createPlannedActivity("Infusion", 18, "Infusion Details")));           // 25, 53
 
         user = new User();
         Set<UserRole> userRoles = new HashSet<UserRole>();
         UserRole userRole = new UserRole();
         userRole.setRole(Role.SUBJECT_COORDINATOR);
         userRoles.add(userRole);
         user.setUserRoles(userRoles);
     }
 
     public void testAssignSubject() throws Exception {
         Study study = createNamedInstance("Glancing", Study.class);
         Amendment expectedAmendment = new Amendment();
         study.setAmendment(expectedAmendment);
         Site site = createNamedInstance("Lake", Site.class);
         StudySite studySite = createStudySite(study, site);
         studySite.approveAmendment(expectedAmendment, DateTools.createDate(2004, OCTOBER, 18));
         Subject subjectIn = createSubject("Alice", "Childress");
         Date startDate = DateUtils.createDate(2006, OCTOBER, 31);
         String studySubjectId = "SSId1";
         StudySegment expectedStudySegment = Epoch.create("Treatment", "A", "B", "C").getStudySegments().get(1);
         expectedStudySegment.addPeriod(createPeriod("DC", 1, 7, 1));
         expectedStudySegment.getPeriods().iterator().next().addPlannedActivity(createPlannedActivity("Any", 4));
         expect(amendmentService.getAmendedNode(expectedStudySegment, expectedAmendment)).andReturn(expectedStudySegment);
 
         Subject subjectExpectedSave = createSubject("Alice", "Childress");
 
         StudySubjectAssignment expectedAssignment = new StudySubjectAssignment();
         expectedAssignment.setStartDate(startDate);
         expectedAssignment.setSubject(subjectExpectedSave);
         expectedAssignment.setStudySite(studySite);
         expectedAssignment.setStudySubjectId(studySubjectId);
         expectedAssignment.setCurrentAmendment(expectedAmendment);
 
         subjectExpectedSave.addAssignment(expectedAssignment);
 
         subjectDao.save(subjectExpectedSave);
         expectLastCall().times(2);
         replayMocks();
 
         StudySubjectAssignment actualAssignment = service.assignSubject(subjectIn, studySite, expectedStudySegment, startDate, studySubjectId, user);
         verifyMocks();
 
         assertNotNull("Assignment not returned", actualAssignment);
         assertEquals("Assignment not added to subject", 1, subjectIn.getAssignments().size());
         assertEquals("Assignment not added to subject", actualAssignment, subjectIn.getAssignments().get(0));
 
         assertNotNull(actualAssignment.getScheduledCalendar());
         assertEquals(1, actualAssignment.getScheduledCalendar().getScheduledStudySegments().size());
         ScheduledStudySegment scheduledStudySegment = actualAssignment.getScheduledCalendar().getScheduledStudySegments().get(0);
         assertEquals(expectedStudySegment, scheduledStudySegment.getStudySegment());
         assertPositive("No scheduled events", scheduledStudySegment.getActivities().size());
     }
 
     public void testAssignSubjectRespectsCurrentApprovedAmendment() throws Exception {
         Subject subject = new Subject();
         Study study = createBasicTemplate();
         study.setAmendment(createAmendments(
             DateTools.createDate(2005, MAY, 12),
             DateTools.createDate(2005, JUNE, 13)
         ));
         Site mayo = createNamedInstance("Mayo", Site.class);
         StudySite ss = createStudySite(study, mayo);
         Amendment currentApproved = study.getAmendment().getPreviousAmendment();
         ss.approveAmendment(currentApproved, DateTools.createDate(2005, MAY,  13));
 
         StudySegment seg = study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0);
         seg.addPeriod(createPeriod("P0", 4, 55, 1));
         expect(amendmentService.getAmendedNode(seg, currentApproved)).andReturn(seg);
 
         subjectDao.save(subject);
         expectLastCall().times(2);
 
         User user=null;
 
         replayMocks();
         StudySubjectAssignment actual = service.assignSubject(subject, ss, seg, DateTools.createDate(2006, JANUARY, 11),null, null);
         verifyMocks();
 
         assertSame("Wrong amendment for new assignment", currentApproved, actual.getCurrentAmendment());
     }
 
     public void testExceptionWhenAssigningASubjectToASiteWithNoApprovedAmendments() throws Exception {
         Subject subject = new Subject();
         Study study = createBasicTemplate();
         study.setName("ECOG 2502");
         study.setAmendment(createAmendments(
             DateTools.createDate(2005, MAY, 12),
             DateTools.createDate(2005, JUNE, 13)
         ));
         Site mayo = createNamedInstance("Mayo", Site.class);
         StudySite ss = createStudySite(study, mayo);
 
         StudySegment seg = study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0);
         seg.addPeriod(createPeriod("P0", 4, 55, 1));
 
         subjectDao.save(subject);
         expectLastCall().times(2);
 
         replayMocks();
         try {
             service.assignSubject(subject, ss, seg, DateTools.createDate(2006, JANUARY, 11), null, null);
             fail("Exception not thrown");
         } catch (StudyCalendarSystemException scse) {
             assertEquals("The template for ECOG 2502 has not been approved by Mayo", scse.getMessage());
         }
     }
 
     public void testScheduleFirstStudySegment() throws Exception {
         StudySubjectAssignment assignment = new StudySubjectAssignment();
         assignment.setSubject(createSubject("Alice", "Childress"));
         subjectDao.save(assignment.getSubject());
 
         StudySite studySite = new StudySite();
         studySite.setSite(new Site());
         assignment.setStudySite(studySite);
 
         Amendment expectedAmendment = new Amendment();
         assignment.setCurrentAmendment(expectedAmendment);
         expect(amendmentService.getAmendedNode(studySegment, expectedAmendment)).andReturn(studySegment);
 
         replayMocks();
 
         ScheduledStudySegment returnedStudySegment = service.scheduleStudySegment(
                 assignment, studySegment, DateUtils.createDate(2006, APRIL, 1),
                 NextStudySegmentMode.PER_PROTOCOL);
         verifyMocks();
 
         ScheduledCalendar scheduledCalendar = assignment.getScheduledCalendar();
         assertNotNull("Scheduled calendar not created", scheduledCalendar);
         assertEquals("Study segment not added to scheduled study segments", 1, scheduledCalendar.getScheduledStudySegments().size());
         assertSame("Study segment not added to scheduled arms", returnedStudySegment, scheduledCalendar.getScheduledStudySegments().get(0));
         assertSame("Wrong study segment scheduled", studySegment, scheduledCalendar.getScheduledStudySegments().get(0).getStudySegment());
         assertEquals("Wrong start day for scheduled study segment", 1, (int) returnedStudySegment.getStartDay());
         assertDayOfDate("Wrong start date for scheduled study segment", 2006, APRIL, 1, returnedStudySegment.getStartDate());
         List<ScheduledActivity> events = scheduledCalendar.getScheduledStudySegments().get(0).getActivities();
         assertEquals("Wrong number of events added", 11, events.size());
 
         Activity a1 = createNamedInstance("CBC", Activity.class);
         Activity a2 = createNamedInstance("Vitals", Activity.class);
         Activity a3 = createNamedInstance("Questionnaire", Activity.class);
         Activity a4 = createNamedInstance("Infusion", Activity.class);
 
         assertNewlyScheduledActivity(2006, APRIL,  1, 1, "CBC Details",           a1, 0, events.get(0));
         assertNewlyScheduledActivity(2006, APRIL,  3, 2, "Vitals Details",        a2, 0, events.get(1));
         assertNewlyScheduledActivity(2006, APRIL,  3, 3, "Questionnaire Details", a3, 0, events.get(2));
         assertNewlyScheduledActivity(2006, APRIL,  8, 1, "CBC Details",           a1, 1, events.get(3));
         assertNewlyScheduledActivity(2006, APRIL,  8, 4, "Infusion Details",      a4, 0, events.get(4));
         assertNewlyScheduledActivity(2006, APRIL, 10, 2, "Vitals Details",        a2, 1, events.get(5));
         assertNewlyScheduledActivity(2006, APRIL, 15, 1, "CBC Details",           a1, 2, events.get(6));
         assertNewlyScheduledActivity(2006, APRIL, 17, 2, "Vitals Details",        a2, 2, events.get(7));
         assertNewlyScheduledActivity(2006, APRIL, 25, 5, "Infusion Details",      a4, 0, events.get(8));
         assertNewlyScheduledActivity(2006, MAY,    6, 4, "Infusion Details",      a4, 1, events.get(9));
         assertNewlyScheduledActivity(2006, MAY,   23, 5, "Infusion Details",      a4, 1, events.get(10));
 
         assertSame("Source amendment not set on SEs", expectedAmendment, events.get(7).getSourceAmendment());
     }
     
     public void testScheduleFirstStudySegmentWithNegativeDays() throws Exception {
         studySegment.getPeriods().first().setStartDay(-7);
         // this will shift the days for events in the first period:
         // event 1: -7, 0, 7
         // event 2: -5, 2, 9
 
         StudySubjectAssignment assignment = new StudySubjectAssignment();
         assignment.setSubject(createSubject("Alice", "Childress"));
         subjectDao.save(assignment.getSubject());
 
         StudySite studySite = new StudySite();
         studySite.setSite(new Site());
         assignment.setStudySite(studySite);        
 
         Amendment expectedAmendment = new Amendment();
         assignment.setCurrentAmendment(expectedAmendment);
         expect(amendmentService.getAmendedNode(studySegment, expectedAmendment)).andReturn(studySegment);
 
         replayMocks();
 
 
         ScheduledStudySegment returnedStudySegment = service.scheduleStudySegment(
             assignment, studySegment, DateUtils.createDate(2006, MARCH, 24), NextStudySegmentMode.PER_PROTOCOL);
         verifyMocks();
 
         ScheduledCalendar scheduledCalendar = assignment.getScheduledCalendar();
         assertNotNull("Scheduled calendar not created", scheduledCalendar);
         assertEquals("Study segment not added to scheduled study segments", 1, scheduledCalendar.getScheduledStudySegments().size());
         assertSame("Study segment not added to scheduled study segments", returnedStudySegment, scheduledCalendar.getScheduledStudySegments().get(0));
         assertSame("Wrong study segment scheduled", studySegment, scheduledCalendar.getScheduledStudySegments().get(0).getStudySegment());
         List<ScheduledActivity> events = scheduledCalendar.getScheduledStudySegments().get(0).getActivities();
         assertEquals("Wrong number of events added", 11, events.size());
         assertEquals("Wrong start day for study segment", -7, (int) returnedStudySegment.getStartDay());
         assertDayOfDate("Wrong start date for study segment", 2006, MARCH, 24, returnedStudySegment.getStartDate());
 
         assertNewlyScheduledActivity(2006, MARCH, 24, 1, events.get(0));
         assertNewlyScheduledActivity(2006, MARCH, 26, 2, events.get(1));
         assertNewlyScheduledActivity(2006, MARCH, 31, 1, events.get(2));
         assertNewlyScheduledActivity(2006, APRIL,  2, 2, events.get(3));
         assertNewlyScheduledActivity(2006, APRIL,  3, 3, events.get(4));
         assertNewlyScheduledActivity(2006, APRIL,  7, 1, events.get(5));
         assertNewlyScheduledActivity(2006, APRIL,  8, 4, events.get(6));
         assertNewlyScheduledActivity(2006, APRIL,  9, 2, events.get(7));
         assertNewlyScheduledActivity(2006, APRIL, 25, 5, events.get(8));
         assertNewlyScheduledActivity(2006, MAY,    6, 4, events.get(9));
         assertNewlyScheduledActivity(2006, MAY,   23, 5, events.get(10));
     }
 
     public void testUnmatchedStudySegmentThrowsException() throws Exception {
         StudySubjectAssignment assignment = new StudySubjectAssignment();
         Amendment expectedAmendment = new Amendment();
         assignment.setCurrentAmendment(expectedAmendment);
         expect(amendmentService.getAmendedNode(studySegment, expectedAmendment)).andReturn(null);
 
         replayMocks();
         try {
             service.scheduleStudySegment(
             assignment, studySegment, DateUtils.createDate(2005, SEPTEMBER, 1), NextStudySegmentMode.IMMEDIATE);
             fail("Exception not thrown");
         } catch (StudyCalendarSystemException scse) {
             assertEquals("Could not find a node " +studySegment +" in the target study", scse.getMessage());
         }
         verifyMocks();
     }
 
     public void testScheduleImmediateNextStudySegment() throws Exception {
         StudySubjectAssignment assignment = new StudySubjectAssignment();
         ScheduledCalendar calendar = new ScheduledCalendar();
         assignment.setScheduledCalendar(calendar);
         assignment.setSubject(createSubject("Alice", "Childress"));
         Amendment expectedAmendment = new Amendment();
         assignment.setCurrentAmendment(expectedAmendment);
         expect(amendmentService.getAmendedNode(studySegment, expectedAmendment)).andReturn(studySegment);
 
         ScheduledStudySegment existingStudySegment = new ScheduledStudySegment();
         existingStudySegment.addEvent(createScheduledActivity("CBC", 2005, AUGUST, 1));
         existingStudySegment.addEvent(createScheduledActivity("CBC", 2005, AUGUST, 2,
             new Occurred(null, DateUtils.createDate(2005, AUGUST, 4))));
         existingStudySegment.addEvent(createScheduledActivity("CBC", 2005, AUGUST, 3,
             new Canceled(null,DateUtils.createDate(2005, AUGUST, 4))));
 
         calendar.addStudySegment(existingStudySegment);
 
         subjectDao.save(assignment.getSubject());
 
         StudySite studySite = new StudySite();
         studySite.setSite(new Site());
         assignment.setStudySite(studySite);        
 
         replayMocks();
         ScheduledStudySegment returnedStudySegment = service.scheduleStudySegment(
             assignment, studySegment, DateUtils.createDate(2005, SEPTEMBER, 1), NextStudySegmentMode.IMMEDIATE);
         verifyMocks();
 
         ScheduledCalendar scheduledCalendar = assignment.getScheduledCalendar();
         assertEquals("Study segment not added to scheduled arms", 2, scheduledCalendar.getScheduledStudySegments().size());
         assertSame("Study segment not added to scheduled arms", returnedStudySegment, scheduledCalendar.getScheduledStudySegments().get(1));
         assertSame("Wrong study segment scheduled", studySegment, scheduledCalendar.getScheduledStudySegments().get(1).getStudySegment());
 
         List<ScheduledActivity> events = scheduledCalendar.getScheduledStudySegments().get(1).getActivities();
         assertEquals("Wrong number of events added", 11, events.size());
         assertNewlyScheduledActivity(2005, SEPTEMBER,  1, 1, events.get(0));
         assertNewlyScheduledActivity(2005, SEPTEMBER,  3, 2, events.get(1));
         assertNewlyScheduledActivity(2005, SEPTEMBER,  3, 3, events.get(2));
         assertNewlyScheduledActivity(2005, SEPTEMBER,  8, 1, events.get(3));
         assertNewlyScheduledActivity(2005, SEPTEMBER,  8, 4, events.get(4));
         assertNewlyScheduledActivity(2005, SEPTEMBER, 10, 2, events.get(5));
         assertNewlyScheduledActivity(2005, SEPTEMBER, 15, 1, events.get(6));
         assertNewlyScheduledActivity(2005, SEPTEMBER, 17, 2, events.get(7));
         assertNewlyScheduledActivity(2005, SEPTEMBER, 25, 5, events.get(8));
         assertNewlyScheduledActivity(2005, OCTOBER,    6, 4, events.get(9));
         assertNewlyScheduledActivity(2005, OCTOBER,   23, 5, events.get(10));
 
         ScheduledActivity wasScheduledActivity = existingStudySegment.getActivities().get(0);
         assertEquals("No new state in scheduled", 2, wasScheduledActivity.getAllStates().size());
         assertEquals("Scheduled event not canceled", Canceled.class, wasScheduledActivity.getCurrentState().getClass());
         assertEquals("Wrong reason for cancelation", "Immediate transition to Epoch: A", wasScheduledActivity.getCurrentState().getReason());
 
         ScheduledActivity wasOccurredEvent = existingStudySegment.getActivities().get(1);
         assertEquals("Occurred event changed", 2, wasOccurredEvent.getAllStates().size());
         assertEquals("Occurred event changed", Occurred.class, wasOccurredEvent.getCurrentState().getClass());
 
         ScheduledActivity wasCanceledEvent = existingStudySegment.getActivities().get(2);
         assertEquals("Canceled event changed", 2, wasCanceledEvent.getAllStates().size());
         assertEquals("Canceled event changed", Canceled.class, wasCanceledEvent.getCurrentState().getClass());
     }
 
     private void assertNewlyScheduledActivity(
         int expectedYear, int expectedMonth, int expectedDayOfMonth,
         int expectedPlannedActivityId, ScheduledActivity actualEvent
     ) {
         assertEquals("Wrong associated planned event", expectedPlannedActivityId, (int) actualEvent.getPlannedActivity().getId());
         assertDayOfDate("Wrong ideal date", expectedYear, expectedMonth, expectedDayOfMonth, actualEvent.getIdealDate());
         assertTrue("Wrong current state mode", actualEvent.getCurrentState() instanceof Scheduled);
         Scheduled currentState = (Scheduled) actualEvent.getCurrentState();
         assertEquals("Current and ideal date not same", actualEvent.getIdealDate(), currentState.getDate());
         assertEquals("Wrong reason", "Initialized from template", currentState.getReason());
     }
 
     private void assertNewlyScheduledActivity(
         int expectedYear, int expectedMonth, int expectedDayOfMonth,
         int expectedPlannedActivityId, String expectedDetails,
         Activity expectedActivity, int expectedRepetitionNumber,
         ScheduledActivity actualEvent
     ){
         assertNewlyScheduledActivity(expectedYear, expectedMonth, expectedDayOfMonth, expectedPlannedActivityId, actualEvent);
         assertEquals("Wrong details", expectedDetails, actualEvent.getDetails());
         assertEquals("Wrong repetition number", expectedRepetitionNumber, (int) actualEvent.getRepetitionNumber());
         assertNotNull("No activity", actualEvent.getActivity());
         assertEquals("Wrong activity", expectedActivity.getName(), actualEvent.getActivity().getName());
     }
 
     public void testFindRecurringHoliday() throws Exception {
         Calendar cal = getInstance();
         List<Date> listOfDays = new ArrayList<Date>();
         assertTrue(listOfDays.size()==0);
         int dayOfTheWeek = DAY_OF_WEEK;
         listOfDays = service.findRecurringHoliday(cal, dayOfTheWeek);
         assertTrue(listOfDays.size()>3 && listOfDays.size()<6);
     }
 
     public void testShiftDayByOne() throws Exception {
         Calendar cal = getInstance();
         Date date = cal.getTime();
         DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
         Date newDate = service.shiftDayByOne(date);
         assertNotEquals("dates are equals", df.format(newDate), df.format(date));
 
         java.sql.Timestamp timestampTo = new java.sql.Timestamp(newDate.getTime());
         long oneDay = 24 * 60 * 60 * 1000;
         timestampTo.setTime(timestampTo.getTime() - oneDay);
         assertEquals("dates are not equals", df.format(timestampTo), df.format(date));
     }
 
     public void testResetTheEvent() throws Exception {
         StudySubjectAssignment assignment = new StudySubjectAssignment();
         ScheduledCalendar scheduledCalendar = new ScheduledCalendar();
         assignment.setScheduledCalendar(scheduledCalendar);
         ScheduledStudySegment existingStudySegment = new ScheduledStudySegment();
         existingStudySegment.addEvent(createScheduledActivity("CBC", 2005, AUGUST, 1));
         scheduledCalendar.addStudySegment(existingStudySegment);
 
         StudySite studySite = new StudySite();
         studySite.setSite(new Site());
         assignment.setStudySite(studySite);
         List<ScheduledActivity> events = scheduledCalendar.getScheduledStudySegments().get(0).getActivities();
         ScheduledActivity event = events.get(0);
         Calendar holiday = getInstance();
         holiday.set(2005, AUGUST, 1);
         String description = "Closed";
         Date dateBeforeReset = event.getActualDate();
         assertNotEquals("descriptions are equals", event.getCurrentState().getReason(), description);
         service.shiftToAvoidBlackoutDate(holiday.getTime(), event,
                 scheduledCalendar.getAssignment().getStudySite().getSite(),
                 description);
         Date dateAfterReset = event.getActualDate();
 
         DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
 
         assertNotEquals("dates are equals", df.format(dateAfterReset), df.format(dateBeforeReset));
 
         java.sql.Timestamp timestampTo = new java.sql.Timestamp(dateAfterReset.getTime());
         long oneDay = 1 * 24 * 60 * 60 * 1000;
         timestampTo.setTime(timestampTo.getTime() - oneDay);
         assertEquals("dates are not equals", df.format(timestampTo), df.format(dateBeforeReset));        
         assertEquals("descriptions are not equals", event.getCurrentState().getReason(),
                 SubjectService.RESCHEDULED + description);
     }
 
     public void testAvoidWeekendsAndHolidays() throws Exception {
         StudySubjectAssignment assignment = new StudySubjectAssignment();
         ScheduledCalendar scheduledCalendar = new ScheduledCalendar();
         assignment.setScheduledCalendar(scheduledCalendar);
 
         Epoch epoch = Epoch.create("Epoch", "A", "B", "C");
         StudySegment scheduledStudySegment = epoch.getStudySegments().get(0);
         Period p1 = createPeriod("P1", 1, 7, 3);
         scheduledStudySegment.addPeriod(p1);
         p1.addPlannedActivity(setId(1, createPlannedActivity("CBC", 1)));
 
         Amendment expectedAmendment = new Amendment();
         assignment.setCurrentAmendment(expectedAmendment);
         expect(amendmentService.getAmendedNode(scheduledStudySegment, expectedAmendment))
             .andReturn(scheduledStudySegment);
 
         StudySite studySite = new StudySite();
         Site site = new Site();
 
         SpecificDateBlackout holidayOne = new SpecificDateBlackout();
         holidayOne.setDay(1);
         holidayOne.setMonth(AUGUST);
         holidayOne.setYear(2005);
         SpecificDateBlackout holidayTwo = new SpecificDateBlackout();
         holidayTwo.setDay(2);
         holidayTwo.setMonth(AUGUST);
         holidayTwo.setYear(2005);
         SpecificDateBlackout holidayThree = new SpecificDateBlackout();
         holidayThree.setDay(3);
         holidayThree.setMonth(AUGUST);
         holidayThree.setYear(2005);
         List<BlackoutDate> listOfHolidays = new ArrayList<BlackoutDate>();
         listOfHolidays.add(holidayOne);
         listOfHolidays.add(holidayTwo);
         listOfHolidays.add(holidayThree);
         site.setBlackoutDates(listOfHolidays);
 
        studySite.setSite(site);
         assignment.setStudySite(studySite);
 
         assignment.setSubject(createSubject("Alice", "Childress"));
         subjectDao.save(assignment.getSubject());
 
 
         replayMocks();
 
         ScheduledStudySegment returnedStudySegment = service.scheduleStudySegment(
                 assignment, scheduledStudySegment, DateUtils.createDate(2005, AUGUST , 1),
                 NextStudySegmentMode.PER_PROTOCOL);
         verifyMocks();
 
         List<ScheduledActivity> events = returnedStudySegment.getActivities();
 
         assertNotNull("Scheduled calendar not created", scheduledCalendar);
         assertEquals("Study segment not added to scheduled study segments", 1, scheduledCalendar.getScheduledStudySegments().size());
         assertSame("Study segment not added to scheduled study segments", returnedStudySegment, scheduledCalendar.getScheduledStudySegments().get(0));
         assertEquals("Wrong number of events added", 3, events.size());
 
         Calendar calendar = getInstance();
         calendar.setTime(events.get(0).getActualDate());
         assertEquals("Date is not reset ", calendar.get(DAY_OF_MONTH), 4);
         assertEquals("Month is not reset ", calendar.get(MONTH), AUGUST);
         assertEquals("Date is not reset ", calendar.get(YEAR), 2005);
 
         assertNewlyScheduledActivity(2005, AUGUST, 8, 1, events.get(1));
         assertNewlyScheduledActivity(2005, AUGUST, 15, 1, events.get(2));
 
 
     }
 
     public void testTakeSubjectOffStudy() throws Exception {
         Date startDate = DateUtils.createDate(2007, AUGUST, 31);
         Date expectedEndDate = DateUtils.createDate(2007, SEPTEMBER, 4);
 
         StudySubjectAssignment expectedAssignment = setId(1, new StudySubjectAssignment());
         expectedAssignment.setStartDate(startDate);
 
         ScheduledStudySegment studySegment0 = new ScheduledStudySegment();
         studySegment0.addEvent(createScheduledActivityWithStudy("ABC", 2007, SEPTEMBER, 2, new Occurred()));
         studySegment0.addEvent(createScheduledActivityWithStudy("DEF", 2007, SEPTEMBER, 4, new Canceled()));
         studySegment0.addEvent(createScheduledActivityWithStudy("GHI", 2007, SEPTEMBER, 6, new Occurred()));
         studySegment0.addEvent(createScheduledActivityWithStudy("JKL", 2007, SEPTEMBER, 8, new Scheduled()));
 
         ScheduledStudySegment studySegment1 = new ScheduledStudySegment();
         studySegment1.addEvent(createScheduledActivityWithStudy("MNO", 2007, OCTOBER, 2, new Occurred()));
         studySegment1.addEvent(createScheduledActivityWithStudy("PQR", 2007, OCTOBER, 4, new Scheduled()));
         studySegment1.addEvent(createScheduledActivityWithStudy("STU", 2007, OCTOBER, 6, new Scheduled()));
         studySegment1.addEvent(createScheduledActivityWithStudy("VWX", 2007, OCTOBER, 8, new Scheduled()));
         studySegment1.addEvent(createConditionalEventWithStudy("YZA", 2007, OCTOBER, 10));
 
         ScheduledCalendar calendar = new ScheduledCalendar();
         calendar.setAssignment(expectedAssignment);
         calendar.addStudySegment(studySegment0);
         calendar.addStudySegment(studySegment1);
         expectedAssignment.setScheduledCalendar(calendar);
 
         subjectDao.save(expectedAssignment.getSubject());
         replayMocks();
 
         StudySubjectAssignment actualAssignment = service.takeSubjectOffStudy(expectedAssignment, expectedEndDate);
         verifyMocks();
 
         CoreTestCase.assertDayOfDate("Wrong off study day", 2007, SEPTEMBER, 4, actualAssignment.getEndDate());
 
         assertEquals("Wrong Event Mode", ScheduledActivityMode.OCCURRED, studySegment0.getActivities().get(2).getCurrentState().getMode());
         assertEquals("Wrong Event Mode", ScheduledActivityMode.CANCELED, studySegment0.getActivities().get(3).getCurrentState().getMode());
         assertEquals("Wrong Event Mode", ScheduledActivityMode.OCCURRED, studySegment1.getActivities().get(0).getCurrentState().getMode());
         assertEquals("Wrong Event Mode", ScheduledActivityMode.CANCELED, studySegment1.getActivities().get(1).getCurrentState().getMode());
         assertEquals("Wrong Event Mode", ScheduledActivityMode.CANCELED, studySegment1.getActivities().get(2).getCurrentState().getMode());
         assertEquals("Wrong Event Mode", ScheduledActivityMode.CANCELED, studySegment1.getActivities().get(3).getCurrentState().getMode());
         assertEquals("Wrong Event Mode", ScheduledActivityMode.NOT_APPLICABLE, studySegment1.getActivities().get(4).getCurrentState().getMode());
     }
 
     public void testScheduleStudySegmentWithOffStudySubject() {
         StudySubjectAssignment assignment = new StudySubjectAssignment();
         assignment.setSubject(createSubject("Alice", "Childress"));
         assignment.setEndDate(DateUtils.createDate(2006, APRIL, 1));
 
         StudySite studySite = new StudySite();
         studySite.setSite(new Site());
         assignment.setStudySite(studySite);
 
         replayMocks();
 
         ScheduledStudySegment returnedStudySegment = service.scheduleStudySegment(
                 assignment, studySegment, DateUtils.createDate(2006, APRIL, 1),
                 NextStudySegmentMode.PER_PROTOCOL);
         verifyMocks();
 
         ScheduledCalendar scheduledCalendar = assignment.getScheduledCalendar();
         assertNull("Scheduled calendar not created", scheduledCalendar);
         assertSame("Study segment not added to scheduled study segments", null, returnedStudySegment);
     }
 
     public void testSchedulePlannedEventWithPopulationWhenSubjectIsInPopulation() throws Exception {
         PlannedActivity plannedActivity = Fixtures.createPlannedActivity("elph", 4);
         plannedActivity.setPopulation(createNamedInstance("H+", Population.class));
         Period period = Fixtures.createPeriod("DC", 2, 7, 1);
 
         StudySubjectAssignment assignment = new StudySubjectAssignment();
         assignment.addPopulation(plannedActivity.getPopulation());
         ScheduledStudySegment segment = createScheduledStudySegment(assignment);
 
         service.schedulePlannedActivity(plannedActivity, period, new Amendment(),
             "Initialized from template", segment);
 
         assertEquals("Wrong number of activites scheduled", 1, segment.getActivities().size());
         assertSame("Wrong activity scheduled, somehow", plannedActivity,
             segment.getActivities().get(0).getPlannedActivity());
     }
 
     public void testSchedulePlannedEventWithPopulationWhenSubjectIsInNotPopulation() throws Exception {
         PlannedActivity plannedActivity = edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createPlannedActivity("elph", 4);
         plannedActivity.setPopulation(createNamedInstance("H+", Population.class));
         Period period = Fixtures.createPeriod("DC", 2, 7, 1);
 
         StudySubjectAssignment assignment = new StudySubjectAssignment();
         assignment.addPopulation(createNamedInstance("Different population", Population.class));
         ScheduledStudySegment segment = createScheduledStudySegment(assignment);
 
         service.schedulePlannedActivity(plannedActivity, period, new Amendment(), "DC", segment);
 
         assertEquals("No activites should have been scheduled", 0, segment.getActivities().size());
     }
 
     public void testSchedulePlannedActivityCopiesLabelsForCorrectRepetitionOnly() throws Exception {
         PlannedActivity plannedActivity = Fixtures.createPlannedActivity("elph", 4);
         labelPlannedActivity(plannedActivity, "all");
         labelPlannedActivity(plannedActivity, 0, "zero");
         Period period = Fixtures.createPeriod("DC", 2, 7, 2);
 
         StudySubjectAssignment assignment = new StudySubjectAssignment();
         ScheduledStudySegment segment = createScheduledStudySegment(assignment);
 
         service.schedulePlannedActivity(plannedActivity, period, new Amendment(),
             "Initialized from template", segment);
 
         assertEquals("Wrong number of activites scheduled", 2, segment.getActivities().size());
         assertEquals("Wrong number of labels for SA 0", 2, segment.getActivities().get(0).getLabels().size());
         assertEquals("Wrong first label for SA 0", "all", segment.getActivities().get(0).getLabels().first());
         assertEquals("Wrong second label for SA 0", "zero", segment.getActivities().get(0).getLabels().last());
         assertEquals("Wrong number of labels for SA 1", 1, segment.getActivities().get(1).getLabels().size());
         assertEquals("Wrong label for SA 1", "all", segment.getActivities().get(1).getLabels().first());
     }
 
     private ScheduledStudySegment createScheduledStudySegment(StudySubjectAssignment assignment) {
         ScheduledStudySegment segment = new ScheduledStudySegment();
         segment.setScheduledCalendar(new ScheduledCalendar());
         segment.getScheduledCalendar().setAssignment(assignment);
         segment.setStartDay(1);
         segment.setStartDate(new Date());
         return segment;
     }
 
     public void testFindSubjectWithAllAttributes() {
         Subject subject = createSubject("1111", "john", "doe", createDate(1990, Calendar.JANUARY, 15, 0, 0, 0), Gender.MALE);
         expectFindSubjectByPersonId("1111", subject);
         replayMocks();
 
         Subject actual = service.findSubject(subject);
         verifyMocks();
 
         assertSame("Subjects should be the same", subject,  actual);
     }
 
     public void testReadElementByPersonId() {
         Subject subject = createSubject("1111", null, null, null, Gender.MALE);
 
         expectFindSubjectByPersonId("1111", subject);
         replayMocks();
 
         Subject actual = service.findSubject(subject);
         verifyMocks();
 
         assertSame("Subjects should be the same", subject,  actual);
     }
 
     public void testReadElementByFirstNameLastNameAndBirthDate() {
         Subject subject = createSubject(null, "john", "doe", createDate(1990, Calendar.JANUARY, 15, 0, 0, 0), Gender.MALE);
 
         expectFindSubjectByFirstNameLastNameAndBirthDate("john", "doe", createDate(1990, Calendar.JANUARY, 15, 0, 0, 0), subject);
         replayMocks();
 
         Subject actual = service.findSubject(subject);
         verifyMocks();
 
         assertSame("Subjects should be the same", subject,  actual);
     }
     
     public void testFindSubjectByNameAndDoBWhenNotPresent() throws Exception {
         Subject subject = createSubject(null, "john", "doe", createDate(1990, Calendar.JANUARY, 15, 0, 0, 0), Gender.MALE);
 
         expect(subjectDao.findSubjectByFirstNameLastNameAndDoB(subject.getFirstName(), subject.getLastName(), subject.getDateOfBirth()))
             .andReturn(null); // this is the actual behavior of SubjectDao
         replayMocks();
 
         Subject actual = service.findSubject(subject);
         verifyMocks();
 
         assertNull("Subject should not be found", actual);
     }
 
     public void testAssignSubjectWithPopulationScopedActivity() {
     Population population = createPopulation("Male", "M");
 
     Study study = createSingleEpochStudy("Study A", "Epoch A", "Segment A");
     study.addPopulation(population);
 
     StudySegment segment = study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0);
 
    Period period = createPeriod("Period A", 1, 2, 1);
     segment.addPeriod(period);
 
     PlannedActivity plannedActivity = createPlannedActivity(a1, 1);
     plannedActivity.setPopulation(population);
     period.addPlannedActivity(plannedActivity);
 
     Subject subject = createSubject("Bernie", "Mac");
 
     Site site = createSite("NU");
     StudySite studySite = createStudySite(study, site);
 
     Amendment amendment = new Amendment();
     study.setAmendment(amendment);
 
     AmendmentApproval amendmentApproval = new AmendmentApproval();
     amendmentApproval.setAmendment(amendment);
 
     studySite.addAmendmentApproval(amendmentApproval);
 
     expect(amendmentService.getAmendedNode((PlanTreeNode) notNull(), (Amendment) notNull())).andReturn((PlanTreeNode) segment);
     subjectDao.save((Subject) notNull());
     subjectDao.save((Subject) notNull());
     replayMocks();
 
     StudySubjectAssignment actual = service.assignSubject(subject, studySite, segment, createDate(1990, Calendar.JANUARY, 15, 0, 0, 0), "123", user, new HashSet(asList(population)));
     verifyMocks();
 
     assertFalse(actual.getScheduledCalendar().getScheduledStudySegments().get(0).getActivities().isEmpty());
 
 }
 
     ////// Expect Methods
     private void expectFindSubjectByPersonId(String id, Subject returned) {
         expect(subjectDao.findSubjectByPersonId(id)).andReturn(returned);
     }
 
     private void expectFindSubjectByFirstNameLastNameAndBirthDate(String firstName, String lastName, Date birthDate, Subject returned) {
         expect(subjectDao.findSubjectByFirstNameLastNameAndDoB(firstName, lastName, birthDate)).andReturn(singletonList(returned));
     }
 }
