 package org.openmrs.module.hr.web.controller;
 
 import org.openmrs.api.context.Context;
 import org.openmrs.module.hr.HrCertificate;
 import org.openmrs.module.hr.HrCompetency;
 import org.openmrs.module.hr.api.HRCompetencyService;
 import org.openmrs.module.hr.api.HRQualificationService;
 import org.openmrs.module.hr.api.validator.CompetencyValidator;
 import org.openmrs.web.WebConstants;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.List;
 
 @Controller
 public class CompetencyController {
     private static final String SUCCESS_LIST_VIEW = "/module/hr/admin/competencies";
     private static final String SUCCESS_FORM_VIEW = "/module/hr/admin/competency";
 
     @RequestMapping(value="module/hr/admin/competency.form")
     @ModelAttribute("competency")
     public HrCompetency showForm(@RequestParam(value="competencyId",required=false) Integer competencyId){
         HrCompetency hrCompetency;
         HRCompetencyService hrCompetencyService = Context.getService(HRCompetencyService.class);
         if(competencyId != null)
             hrCompetency =  hrCompetencyService.getCompetencyById(competencyId);
         else
             hrCompetency = new HrCompetency();
         return hrCompetency;
     }
 
 
     @RequestMapping(value = "module/hr/admin/competencies.list")
     public String showList(ModelMap model){
         HRCompetencyService hrCompetencyService = Context.getService(HRCompetencyService.class);
         List<HrCompetency> hrCompetencies = hrCompetencyService.getCompetencies();
         model.addAttribute("competenciesList",hrCompetencies);
         return SUCCESS_LIST_VIEW;
     }
 
     @RequestMapping(value ="module/hr/admin/competency.form", method = RequestMethod.POST)
     public ModelAndView createOrUpdateCertificate(HttpServletRequest request,@ModelAttribute("competency")  HrCompetency competency, BindingResult errors){
         HRCompetencyService hrCompetencyService = Context.getService(HRCompetencyService.class);
         if(request.getParameter("unretireCompetency") != null)
             return unRetireCompetency(request, competency, hrCompetencyService, errors);
 
         new CompetencyValidator().validate(competency, errors);
         if(errors.hasErrors())
             return new ModelAndView(SUCCESS_FORM_VIEW);
 
        if(request.getParameter("retireCompetency") != null)
             return checkAndRetireCompetency(request, competency, hrCompetencyService, errors);
 
         hrCompetencyService.saveCompetency(competency);
         request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Competency saved Successfully");
         return new ModelAndView(SUCCESS_LIST_VIEW).addObject("competenciesList",hrCompetencyService.getCompetencies());
     }
 
     private ModelAndView unRetireCompetency(HttpServletRequest request, HrCompetency competency, HRCompetencyService hrCompetencyService, BindingResult errors) {
         hrCompetencyService.unretireCompetency(competency);
        request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Competency Un-Retired Successfully");
         return new ModelAndView(SUCCESS_LIST_VIEW).addObject("competenciesList", hrCompetencyService.getCompetencies());
     }
 
     private ModelAndView checkAndRetireCompetency(HttpServletRequest request, HrCompetency competency, HRCompetencyService hrCompetencyService, BindingResult errors) {
         String retireReason = request.getParameter("retireReason");
         if (competency.getId() != null && (retireReason == null || retireReason.length() == 0)) {
             errors.reject("retireReason", "Retire reason cannot be empty");
             return new ModelAndView(SUCCESS_FORM_VIEW);
         }
         hrCompetencyService.retireCompetency(hrCompetencyService.getCompetencyById(competency.getId()), retireReason, Context.getAuthenticatedUser());
         request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Competency Retired Successfully");
         return new ModelAndView(SUCCESS_LIST_VIEW).addObject("competenciesList",hrCompetencyService.getCompetencies());
     }
 
 }
