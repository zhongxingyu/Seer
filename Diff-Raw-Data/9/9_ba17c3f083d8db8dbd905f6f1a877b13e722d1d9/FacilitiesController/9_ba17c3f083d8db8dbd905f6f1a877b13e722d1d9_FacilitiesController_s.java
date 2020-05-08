 package org.motechproject.ghana.national.web;
 
 import org.motechproject.ghana.national.domain.Facility;
 import org.motechproject.ghana.national.exception.FacilityAlreadyFoundException;
 import org.motechproject.ghana.national.service.FacilityService;
 import org.motechproject.ghana.national.web.form.CreateFacilityForm;
 import org.motechproject.ghana.national.web.form.SearchFacilityForm;
 import org.motechproject.openmrs.advice.ApiSession;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.MessageSource;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.apache.commons.lang.StringUtils;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.FieldError;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.validation.Valid;
 import java.util.*;
 
 @Controller
 @RequestMapping(value = "/admin/facilities")
 public class FacilitiesController {
     public static final String CREATE_FACILITY_FORM = "createFacilityForm";
     public static final String SEARCH_FACILITY_FORM = "searchFacilityForm";
     public static final String SUCCESS = "facilities/success";
     public static final String NEW_FACILITY = "facilities/new";
     public static final String SEARCH_FACILITY = "facilities/search";
 
     private FacilityService facilityService;
     private MessageSource messageSource;
 
 
     public FacilitiesController() {
     }
 
     @Autowired
     public FacilitiesController(FacilityService facilityService, MessageSource messageSource) {
         this.facilityService = facilityService;
         this.messageSource = messageSource;
     }
 
     @ApiSession
     @RequestMapping(value = "new", method = RequestMethod.GET)
     public String newFacilityForm(ModelMap modelMap) {
         modelMap.put(CREATE_FACILITY_FORM, new CreateFacilityForm());
         modelMap.mergeAttributes(facilityService.locationMap());
         return NEW_FACILITY;
     }
 
     @ApiSession
     @RequestMapping(value = "create", method = RequestMethod.POST)
     public String createFacility(@Valid CreateFacilityForm createFacilityForm, BindingResult bindingResult, ModelMap modelMap) {
         try {
             facilityService.create(createFacilityForm.getName(), createFacilityForm.getCountry(), createFacilityForm.getRegion(),
                     createFacilityForm.getCountyDistrict(), createFacilityForm.getStateProvince(), createFacilityForm.getPhoneNumber(),
                     createFacilityForm.getAdditionalPhoneNumber1(), createFacilityForm.getAdditionalPhoneNumber2(),
                     createFacilityForm.getAdditionalPhoneNumber3());
             modelMap.mergeAttributes(facilityService.locationMap());
         } catch (FacilityAlreadyFoundException e) {
             handleExistingFacilityError(bindingResult, modelMap, messageSource.getMessage("facility_already_exists", null, Locale.getDefault()));
             return NEW_FACILITY;
         }
         return SUCCESS;
     }
 
     @ApiSession
     @RequestMapping(value = "search", method = RequestMethod.GET)
     public String searchFacilityForm(ModelMap modelMap) {
         modelMap.put(SEARCH_FACILITY_FORM, new SearchFacilityForm());
         modelMap.mergeAttributes(facilityService.locationMap());
         return SEARCH_FACILITY;
     }
 
     @ApiSession
     @RequestMapping(value = "searchFacilities", method = RequestMethod.POST)
     public String searchFacility(@Valid final SearchFacilityForm searchFacilityForm, BindingResult bindingResult, ModelMap modelMap) {
         List<Facility> allFacilities = facilityService.facilities();
         List<Facility> requestedFacilities = new ArrayList<Facility>();
         Map searchFields = new HashMap() {{
             put(1, searchFacilityForm.getFacilityID());
             put(2, searchFacilityForm.getName().toLowerCase());
             put(3, searchFacilityForm.getStateProvince());
             put(4, searchFacilityForm.getCountyDistrict());
             put(5, searchFacilityForm.getRegion());
             put(6, searchFacilityForm.getCountry());
         }};
         int numberOfFieldsForSearch = 1;
         Map searchFieldsCombination = new HashMap();
         for (int loopCounter = 1; loopCounter <= 6; loopCounter++) {
             if (!searchFields.get(loopCounter).equals("")) {
                 searchFieldsCombination.put(loopCounter, searchFields.get(loopCounter));
                 numberOfFieldsForSearch++;
             } else {
                 searchFieldsCombination.put(loopCounter, "fieldNotRequiredForSearch");
             }
         }
         if (numberOfFieldsForSearch == 1) {
             modelMap.put("requestedFacilities", requestedFacilities);
             modelMap.mergeAttributes(facilityService.locationMap());
             return SEARCH_FACILITY;
         }
         for (Facility searchFacility : allFacilities) {
             org.motechproject.mrs.model.Facility thatMrsFacility = searchFacility.mrsFacility();
             try {
                 int combinationFieldSize = 1;
                 for (int fieldIndex = 1; fieldIndex <= searchFields.size(); fieldIndex++) {
                     String searchValue = (String) searchFieldsCombination.get(fieldIndex);
                    combinationFieldSize = compareSearchFields(thatMrsFacility, combinationFieldSize, searchValue);
                 }
                 if (combinationFieldSize == numberOfFieldsForSearch)
                     requestedFacilities.add(searchFacility);
             } catch (Exception e) {
             }
         }
         modelMap.put("requestedFacilities", requestedFacilities);
         modelMap.mergeAttributes(facilityService.locationMap());
         return SEARCH_FACILITY;
     }
 
     private int compareSearchFields(org.motechproject.mrs.model.Facility thatMrsFacility, int combinationFieldSize, String searchValue) {
         if (StringUtils.equals(thatMrsFacility.getId(), searchValue)
                 || thatMrsFacility.getName().toLowerCase().startsWith(searchValue)
                 || thatMrsFacility.getStateProvince().toLowerCase().startsWith(searchValue)
                 || thatMrsFacility.getRegion().toLowerCase().startsWith(searchValue)
                 || thatMrsFacility.getCountyDistrict().toLowerCase().startsWith(searchValue)
                 || thatMrsFacility.getCountry().toLowerCase().startsWith(searchValue)) {
 
             combinationFieldSize++;
         }
         return combinationFieldSize;
     }
 
 
     private void handleExistingFacilityError(BindingResult bindingResult, ModelMap modelMap, String message) {
         modelMap.mergeAttributes(facilityService.locationMap());
         bindingResult.addError(new FieldError(CREATE_FACILITY_FORM, "name", message));
         modelMap.mergeAttributes(bindingResult.getModel());
     }
 
 }
