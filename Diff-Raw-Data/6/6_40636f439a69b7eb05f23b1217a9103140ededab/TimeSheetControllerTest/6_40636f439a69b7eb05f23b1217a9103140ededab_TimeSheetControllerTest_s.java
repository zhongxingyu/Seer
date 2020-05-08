 package com.thoughtworks.twu.controller;
 
 
 import com.thoughtworks.twu.domain.Employee;
 import com.thoughtworks.twu.domain.Message;
 import com.thoughtworks.twu.domain.Timesheet;
 import com.thoughtworks.twu.domain.timesheet.forms.TimesheetForm;
 import com.thoughtworks.twu.service.DatePickerService;
 import com.thoughtworks.twu.service.EmployeeService;
 import com.thoughtworks.twu.service.MessageService;
 import com.thoughtworks.twu.service.TimesheetService;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.validation.BindException;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.*;
 import static org.testng.Assert.assertEquals;
 
 
 public class TimeSheetControllerTest {
 
     private TimeSheetController controller;
     private Employee expectedEmployee;
     private Timesheet expectedTimesheet;
     private HttpServletRequest request;
     private Timesheet timesheet;
     private MessageService messageService;
     private EmployeeService employeeService;
     private TimesheetService timesheetService;
     private DatePickerService datepickerService;
     private BindException errors;
     private TimesheetForm timesheetForm;
     private HttpSession session;
 
     @Before
     public void setUp() throws Exception {
         mockMessageService();
         employeeService = mockEmployee();
         timesheetService = mockTimesheetService();
         datepickerService = mock(DatePickerService.class);
         request = mock(HttpServletRequest.class);
         session = mock(HttpSession.class);
         when(request.getRemoteUser()).thenReturn("batman");
         when(request.getSession()).thenReturn(session);
         when(request.getSession().getAttribute("weekEndingDate")).thenReturn("10-SEP-2012");
 
         controller = new TimeSheetController(datepickerService, employeeService, timesheetService, messageService);
 
         timesheetForm = new TimesheetForm();
         timesheetForm.setWeekEndingDate("15-Sep-12");
 
         expectedTimesheet = new Timesheet();
         expectedTimesheet.setWeekEndingDate(new SimpleDateFormat("dd-MMM-yy").parse("15-Sep-12"));
         expectedTimesheet.setEmployeeNumber(String.valueOf(expectedEmployee.getEmployeeNumber()));
         expectedTimesheet.setIsSubmitted(true);
 
         employeeService = mock(EmployeeService.class);
         when(employeeService.getEmployeeByLogin("batman")).thenReturn(expectedEmployee);
         errors = new BindException(timesheetForm, "timesheetForm");
 
 
     }
 
     private TimesheetService mockTimesheetService() {
         timesheetService = mock(TimesheetService.class);
         when(timesheetService.createNewTimesheet()).thenReturn(expectedTimesheet);
         return timesheetService;
     }
 
     private EmployeeService mockEmployee() {
         expectedEmployee = new Employee();
         expectedEmployee.setEmployeeNumber("5678");
 
         EmployeeService employeeService = mock(EmployeeService.class);
         when(employeeService.getEmployeeByLogin("batman")).thenReturn(expectedEmployee);
         return employeeService;
     }
 
     private void mockMessageService() {
         messageService = mock(MessageService.class);
         Message duplicateTimesheetForWeek = new Message("Duplicated week ending date", "DuplicateTimesheetForWeek");
         Message canNotBeBlank = new Message("Week ending date is required.", "WeekCannotBeUnspecified");
         when(messageService.getMessageById("DuplicateTimesheetForWeek")).thenReturn(duplicateTimesheetForWeek);
         when(messageService.getMessageById("WeekCannotBeUnspecified")).thenReturn(canNotBeBlank);
     }
 
     @Test
     public void shouldDisplayNewTimeSheetView() throws ParseException {
         assertEquals("ui/timesheet/newtimesheet", controller.newTimeSheet(request, timesheetForm, errors).getViewName());
     }
 
     @Test
     public void shouldAddEmployeeToModel() throws ParseException {
 
         ModelAndView modelAndView = controller.newTimeSheet(request, timesheetForm, errors);
         Employee actualEmployee = (Employee) modelAndView.getModel().get("employee");
 
         assertThat(actualEmployee, is(expectedEmployee));
     }
     @Test
     public void shouldSaveTimeSheet() throws Exception {
         expectedTimesheet.setWeekEndingDate(new SimpleDateFormat("dd-MMM-yy").parse("15-Sep-12"));
         expectedTimesheet.setEmployeeNumber(String.valueOf(expectedEmployee.getEmployeeNumber()));
 
         employeeService = mockEmployee();
 
         controller.submitTimesheet(timesheetForm, errors, request);
 
         verify(timesheetService).saveTimesheet(expectedTimesheet);
     }
 
     @Test
     public void shouldNotifyWhenCreatingDuplicateTimeSheetForAWeek() throws Exception {
         when(datepickerService.hasWeekEndingDate("15-Sep-12", expectedEmployee)).thenReturn(true);
 
         ModelAndView modelAndView = controller.submitTimesheet(timesheetForm, errors, request);
 
         BindingResult errors = (BindingResult) modelAndView.getModel().get("errors");
 
         assertThat(errors.hasErrors(), is(true));
     }
 
     @Test
    public void shouldBeAbleToSaveTimesheet() throws Exception {
        controller.saveTimesheet(timesheetForm, request);
        verify(timesheetService).saveTimesheet(eq(expectedTimesheet));

    }
    @Test
     public void shouldSubmitTimesheet() throws Exception {
         controller.submitTimesheet(timesheetForm, errors, request);
         verify(timesheetService).saveTimesheet(expectedTimesheet);
     }
 
 }
 
