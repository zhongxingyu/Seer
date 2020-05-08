 package com.thoughtworks.twu.controller;
 
 import com.thoughtworks.twu.domain.Country;
 import com.thoughtworks.twu.domain.timesheet.forms.TimeRecordForm;
 import com.thoughtworks.twu.service.CountryService;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.servlet.ModelAndView;
 
 import java.util.List;
 
 import static junit.framework.Assert.assertEquals;
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.mock;
 
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
     public void shouldReceiveACountryList() throws Exception {
         //Given
         CountryService countryService = new CountryService();
         //When
         List<Country> countries = countryService.getCountries();
         //Then
         assertThat(countries.size(), is(239));
     }
 
     @Test
     public void shouldBeAbleToGetViewNameOfController() throws Exception {
        assertEquals("ui/timesheet/time_record", controller.newTimesheet(timeRecordForm,bindingResult).getViewName());
     }
 
 
     @Ignore
     @Test
     public void shouldShowNewFavoriteForm() throws Exception {
         TimeRecordController controller = new TimeRecordController();
         ModelAndView modelAndView = controller.newTimesheet(timeRecordForm,bindingResult);
         List<String> countries = (List<String>) modelAndView.getModel().get("countries");
 
         assertThat(countries.size(), is(240));
     }
 
 
 }
