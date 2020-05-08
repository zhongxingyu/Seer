 package edu.northwestern.bioinformatics.studycalendar.web.schedule;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createScheduledActivity;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.setId;
 import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
 import edu.northwestern.bioinformatics.studycalendar.service.ScheduleService;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER;
import static org.easymock.classextension.EasyMock.expect;
 import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.Calendar;
 import java.util.Collection;
import java.util.TreeMap;
 import java.util.Map;
 
 /**
  * @author Rhett Sutphin
  */
 public class ScheduleActivityControllerTest extends ControllerTestCase {
     private ScheduleActivityController controller;
     private ScheduledCalendarDao scheduledCalendarDao;
     private ScheduledActivityDao scheduledActivityDao;
     private ScheduleService scheduleService;
     private ScheduledActivity event;
     private ScheduleActivityCommand command;
     protected void setUp() throws Exception {
         super.setUp();
         scheduledCalendarDao = registerDaoMockFor(ScheduledCalendarDao.class);
         scheduledActivityDao = registerDaoMockFor(ScheduledActivityDao.class);
         scheduleService = registerMockFor(ScheduleService.class);
         command = registerMockFor(ScheduleActivityCommand.class,
             ScheduleActivityCommand.class.getMethod("apply"));
 
         controller = new ScheduleActivityController() {
             protected Object formBackingObject(HttpServletRequest request) throws Exception {
                 return command;
             }
         };
         controller.setScheduleService(scheduleService);
         controller.setScheduledCalendarDao(scheduledCalendarDao);
         controller.setScheduledActivityDao(scheduledActivityDao);
         controller.setControllerTools(controllerTools);
 
         request.setMethod("GET");
     }
     
     public void testAuthorizedRoles() {
         Collection<ResourceAuthorization> actualAuthorizations = controller.authorizations(null, null);
        assertRolesAllowed(actualAuthorizations, STUDY_CALENDAR_TEMPLATE_BUILDER);
     }
 
     public void testBindEvent() throws Exception {
         expectShowFormWithNoErrors();
 
         assertSame(event, command.getEvent());
     }
 
     public void testBindMode() throws Exception {
         request.addParameter("newMode", "2");
 
         expectShowFormWithNoErrors();
 
         assertSame(ScheduledActivityMode.OCCURRED, command.getNewMode());
     }
 
     public void testBindNoMode() throws Exception {
         request.addParameter("newMode", "");
 
         expectShowFormWithNoErrors();
 
         assertNull(command.getNewMode());
     }
 
     public void testBindDate() throws Exception {
         request.addParameter("newDate", "11/02/2003");
 
         expectShowFormWithNoErrors();
 
         assertDayOfDate(2003, Calendar.NOVEMBER, 2, command.getNewDate());
     }
     
     public void testBindReason() throws Exception {
         request.addParameter("newReason", "Insisted");
 
         expectShowFormWithNoErrors();
 
         assertEquals("Insisted", command.getNewReason());
     }
 
     public void testBindReasonBlankIsNull() throws Exception {
         request.addParameter("newReason", "");
 
         expectShowFormWithNoErrors();
 
         assertNull(command.getNewReason());
     }
 
     public void testBindNotes() throws Exception {
         request.addParameter("newNotes", "Insisted");
 
         expectShowFormWithNoErrors();
 
         assertEquals("Insisted", command.getNewNotes());
     }
 
     public void testBindNotesBlankIsNull() throws Exception {
         request.addParameter("newNotes", "");
 
         expectShowFormWithNoErrors();
 
         assertNull(command.getNewNotes());
     }
 
     public void testChangeStateOnSubmit() throws Exception {
         Subject subject = edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createSubject("firstName", "lastName");
         StudySite studySite = new StudySite();
         Study study = new Study();
         study.setAssignedIdentifier("test-study");
         studySite.setStudy(study);
 
         command.setEvent(new ScheduledActivity());
         command.getEvent().setScheduledStudySegment(new ScheduledStudySegment());
         command.getEvent().getScheduledStudySegment().setScheduledCalendar(new ScheduledCalendar());
         command.getEvent().getScheduledStudySegment().getScheduledCalendar().setAssignment(Fixtures.createAssignment(studySite, subject));
         request.setMethod("POST");
         command.apply();
 
         replayMocks();
         ModelAndView mv = controller.handleRequest(request, response);
         verifyMocks();
     }
 
     private void expectShowFormWithNoErrors() throws Exception {
         event = setId(16, createScheduledActivity("SBC", 2002, Calendar.MAY, 3));
         expect(scheduledActivityDao.getById(16)).andReturn(event);
         Map<String,String> uriListMap = new TreeMap<String, String>();
         expect(scheduleService.generateActivityTemplateUri(event)).andReturn(uriListMap);
         request.setParameter("event", "16");
 
         replayMocks();
         ModelAndView mv = controller.handleRequest(request, response);
         verifyMocks();
 
         BindingResult errors = (BindingResult) mv.getModel().get("org.springframework.validation.BindingResult.command");
         assertFalse("Binding errors: " + errors.getAllErrors(), errors.hasErrors());
     }
 }
