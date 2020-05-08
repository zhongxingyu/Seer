 package org.openmrs.module.hr.web.controller;
 
 import org.openmrs.api.context.Context;
 import org.openmrs.module.hr.HrEducation;
 import org.openmrs.module.hr.HrStaff;
 import org.openmrs.module.hr.HrStaffCert;
 import org.openmrs.module.hr.api.HRQualificationService;
 import org.openmrs.module.hr.api.validator.EducationValidator;
 import org.openmrs.web.WebConstants;
 import org.springframework.beans.propertyeditors.CustomNumberEditor;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import java.text.NumberFormat;
 import java.util.List;
 
 @Controller
 @SessionAttributes("staff")
 public class EducationController {
 
     private static final String SUCCESS_FORM_VIEW = "/module/hr/manager/staffEducation";
     private static final String SUCCESS_LIST_VIEW = "/module/hr/manager/staffEducations";
 
 
     @RequestMapping(value="module/hr/manager/staffEducation.form")
     @ModelAttribute("education")
     public HrEducation showForm(@RequestParam(value="educationId",required=false) Integer educationId){
         HrEducation hrEducation;
         HRQualificationService hrQualificationService = Context.getService(HRQualificationService.class);
         if(educationId != null)
             hrEducation = hrQualificationService.getEducationById(educationId);
         else
             hrEducation = new HrEducation();
         return hrEducation;
     }
 
     @RequestMapping(value ="module/hr/manager/staffEducation.form", method = RequestMethod.POST)
     public ModelAndView createOrUpdateEducation(HttpServletRequest request,@ModelAttribute("education")  HrEducation education, BindingResult errors,@ModelAttribute("staff") HrStaff staff){
         HRQualificationService hrQualificationService = Context.getService(HRQualificationService.class);
         if(request.getParameter("action").equalsIgnoreCase("Delete Education"))
             deleteEducation(request,hrQualificationService,education);
         else{
         new EducationValidator().validate(education, errors);
         if(errors.hasErrors())
             return new ModelAndView(SUCCESS_FORM_VIEW).addObject("education",education);
 
         education.setHrStaff(staff);
         hrQualificationService.saveEducation(education);
         request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Education saved Successfully");
         }
        return new ModelAndView(SUCCESS_FORM_VIEW).addObject("education",hrQualificationService.getEducationById(education.getEducationId()));
     }
 
     private void deleteEducation(HttpServletRequest request, HRQualificationService hrQualificationService, HrEducation education) {
         hrQualificationService.deleteEducation(hrQualificationService.getEducationById(education.getEducationId()));
         request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Education deleted Successfully");
     }
 
     @RequestMapping(value = "module/hr/manager/staffEducations.list")
     public String showList(ModelMap model ,@ModelAttribute("staff") HrStaff staff){
         HRQualificationService hrQualificationService=Context.getService(HRQualificationService.class);
         List<HrEducation> hrEducationList = hrQualificationService.getEducationsForStaff(staff);
         model.addAttribute("staffEducations",hrEducationList);
         return SUCCESS_LIST_VIEW;
     }
 
 
 }
