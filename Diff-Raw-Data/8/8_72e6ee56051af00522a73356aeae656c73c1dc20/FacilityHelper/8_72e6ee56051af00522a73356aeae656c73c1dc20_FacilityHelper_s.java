 package org.motechproject.ghana.national.web.helper;
 
 import ch.lambdaj.group.Group;
 import org.apache.commons.lang.StringUtils;
 import org.motechproject.ghana.national.domain.Constants;
 import org.motechproject.ghana.national.domain.Facility;
 import org.motechproject.ghana.national.service.FacilityService;
 import org.motechproject.ghana.national.vo.FacilityVO;
 import org.motechproject.ghana.national.web.FacilityController;
 import org.motechproject.ghana.national.web.form.FacilityForm;
 import org.motechproject.mrs.model.MRSFacility;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.FieldError;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static ch.lambdaj.Lambda.*;
 import static ch.lambdaj.group.Groups.by;
 import static ch.lambdaj.group.Groups.group;
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.not;
 import static org.hamcrest.core.Is.is;
 import static org.motechproject.ghana.national.tools.Utility.mapConverter;
 import static org.motechproject.ghana.national.tools.Utility.reverseKeyValues;
 
 @Component
 public class FacilityHelper {
 
     @Autowired
     FacilityService facilityService;
 
     public Map<String, Object> locationMap() {
         List<Facility> facilities = facilityService.facilities();
 
         final HashMap<String, Object> modelMap = new HashMap<String, Object>();
         List<Facility> withValidCountryNames = select(facilities, having(on(Facility.class).country(), is(not(equalTo(StringUtils.EMPTY)))));
         final Group<Facility> byCountryRegion = group(withValidCountryNames, by(on(Facility.class).country()), by(on(Facility.class).region()));
         final Group<Facility> byRegionDistrict = group(withValidCountryNames, by(on(Facility.class).region()), by(on(Facility.class).district()));
         final Group<Facility> byDistrictProvince = group(withValidCountryNames, by(on(Facility.class).district()), by(on(Facility.class).province()));
 
         modelMap.put(Constants.COUNTRIES, extract(selectDistinct(withValidCountryNames, "country"), on(Facility.class).country()));
         modelMap.put(Constants.REGIONS, reverseKeyValues(map(byCountryRegion.keySet(), mapConverter(byCountryRegion))));
         modelMap.put(Constants.DISTRICTS, reverseKeyValues(map(byRegionDistrict.keySet(), mapConverter(byRegionDistrict))));
         modelMap.put(Constants.PROVINCES, reverseKeyValues(map(byDistrictProvince.keySet(), mapConverter(byDistrictProvince))));
         modelMap.put(Constants.FACILITIES, facilityVOs(facilities));
         return modelMap;
     }
 
     private List<FacilityVO> facilityVOs(List<Facility> facilities) {
         List<FacilityVO> facilityVOs = new ArrayList<FacilityVO>();
         for (Facility facility : facilities) {
            if (facility.province() != null) {
                 facilityVOs.add(new FacilityVO(facility.mrsFacility().getId(), facility.name(), facility.province()));
                 continue;
             }
            if (facility.district() != null) {
                 facilityVOs.add(new FacilityVO(facility.mrsFacility().getId(), facility.name(), facility.district()));
                 continue;
             }
            if (facility.region() != null) {
                 facilityVOs.add(new FacilityVO(facility.mrsFacility().getId(), facility.name(), facility.region()));
             }
         }
         return facilityVOs;
     }
 
     public FacilityForm copyFacilityValuesToForm(Facility facility) {
         FacilityForm facilityForm = new FacilityForm();
         facilityForm.setAdditionalPhoneNumber1(facility.additionalPhoneNumber1());
         facilityForm.setAdditionalPhoneNumber2(facility.additionalPhoneNumber2());
         facilityForm.setAdditionalPhoneNumber3(facility.additionalPhoneNumber3());
         facilityForm.setPhoneNumber(facility.phoneNumber());
         facilityForm.setCountry(facility.country());
         facilityForm.setCountyDistrict(facility.district());
         facilityForm.setName(facility.name());
         facilityForm.setRegion(facility.region());
         facilityForm.setStateProvince(facility.province());
         facilityForm.setId(facility.mrsFacilityId());
         facilityForm.setFacilityId(facility.motechId());
         return facilityForm;
     }
 
     public void handleExistingFacilityError(BindingResult bindingResult, ModelMap modelMap, String message, String formName) {
         bindingResult.addError(new FieldError(formName, "name", message));
         modelMap.mergeAttributes(bindingResult.getModel());
     }
 
     public String getFacilityForId(ModelMap modelMap, Facility facility) {
         modelMap.addAttribute(FacilityController.FACILITY_FORM, copyFacilityValuesToForm(facility));
         modelMap.addAttribute("message", "facility created successfully.");
         modelMap.mergeAttributes(locationMap());
         return FacilityController.EDIT_FACILITY_VIEW;
     }
 
 
     public Facility createFacilityVO(FacilityForm updateFacilityForm) {
         MRSFacility mrsFacility = new MRSFacility(updateFacilityForm.getId(), updateFacilityForm.getName(), updateFacilityForm.getCountry(), updateFacilityForm.getRegion(),
                 updateFacilityForm.getCountyDistrict(), updateFacilityForm.getStateProvince());
         Facility facility = new Facility().mrsFacility(mrsFacility).mrsFacilityId(updateFacilityForm.getId()).motechId(updateFacilityForm.getFacilityId()).phoneNumber(updateFacilityForm.getPhoneNumber()).
                 additionalPhoneNumber1(updateFacilityForm.getAdditionalPhoneNumber1()).additionalPhoneNumber2(updateFacilityForm.getAdditionalPhoneNumber2()).additionalPhoneNumber3(updateFacilityForm.getAdditionalPhoneNumber3());
         return facility;
     }
 }
