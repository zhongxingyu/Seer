 package com.thoughtworks.twu.controller;
 
 import com.thoughtworks.twu.constants.URLPaths;
 
 import com.thoughtworks.twu.domain.Country;
 import com.thoughtworks.twu.domain.LocationPresences;
 import com.thoughtworks.twu.domain.timesheet.forms.TimeRecordForm;
 import com.thoughtworks.twu.domain.validators.ActivityValidator;
 import com.thoughtworks.twu.domain.validators.LocationValidator;
 import com.thoughtworks.twu.persistence.HibernateConnection;
 import com.thoughtworks.twu.service.CountryService;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import java.util.ArrayList;
 import java.util.List;
 
 @Controller
 public class TimeRecordController {
 
     @Autowired
     private CountryService countryService;
 
 
     public TimeRecordController(CountryService countryService) {
         this.countryService = countryService;
     }
 
     public TimeRecordController() {
     }
 
     @RequestMapping(value = URLPaths.TIME_RECORD_PATH, method = RequestMethod.GET)
     public ModelAndView newTimesheet(@ModelAttribute("timeRecordForm") TimeRecordForm timeRecordForm, BindingResult errors) throws Exception {
         ModelAndView modelAndView = new ModelAndView("ui/timesheet/time_record");
 
         modelAndView.addObject("countries", loadCountryNames(countryService.loadCountryListWithTWPresence()));
         modelAndView.addObject("states", loadStateNames(countryService.getStates("USA")));
      return modelAndView;
     }
 
     @RequestMapping(value = URLPaths.TIME_RECORD_PATH, method = RequestMethod.POST)
     public ModelAndView submittedTimeSheet(@ModelAttribute("timeRecordForm") TimeRecordForm timeRecordForm, BindingResult errors) throws Exception {
 
         LocationValidator locationValidator = new LocationValidator();
         ActivityValidator activityValidator = new ActivityValidator();
 
         locationValidator.validate(timeRecordForm, errors);
         activityValidator.validate(timeRecordForm, errors);
 
         if (errors.hasErrors()) {
             return newTimesheet(timeRecordForm, errors);
         } else {
            ModelAndView modelAndView = new ModelAndView("ui/timesheet/timeSheet");
             modelAndView.addObject("timeRecordForm", timeRecordForm);
             return modelAndView;
         }
     }
 
 
     public List<String> loadCountryNames(List<Country> countries) {
         List<String> countryNames = new ArrayList<String>();
         countryNames.add("Select a country");
         for (Country country : countries) {
             countryNames.add(country.getCode() + " - " + country.getName());
         }
         return countryNames;
     }
 
     public List<String> loadStateNames(List<LocationPresences> locationPresences) {
 
         List<String> stateNames = new ArrayList<String>();
         stateNames.add("Select a state");
         for (LocationPresences state : locationPresences) {
             stateNames.add(state.getState());
         }
         return stateNames;
     }
 }
