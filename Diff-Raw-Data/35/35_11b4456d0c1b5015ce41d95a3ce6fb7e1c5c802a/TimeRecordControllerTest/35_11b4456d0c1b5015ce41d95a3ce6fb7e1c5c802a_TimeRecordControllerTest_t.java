 package com.thoughtworks.twu.controller;
 
 import com.thoughtworks.twu.domain.timesheet.forms.TimeRecordForm;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.validation.BindingResult;
 
 import static org.mockito.Mockito.mock;
 import static org.testng.Assert.assertEquals;
 
 public class TimeRecordControllerTest {
 
     TimeRecordController controller;
     private TimeRecordForm timeRecordForm;
     private BindingResult bindingResult;
 
     @Before
     public void setUp() throws Exception {
         controller = new TimeRecordController();
         timeRecordForm = new TimeRecordForm();
         bindingResult = mock(BindingResult.class);
     }
 
 
 
     @Test
     public void shouldBeAbleToGetViewNameOfController() throws Exception {
         assertEquals("ui/timesheet/timeRecord", controller.newTimesheet(timeRecordForm,bindingResult).getViewName());
     }
 

 }
