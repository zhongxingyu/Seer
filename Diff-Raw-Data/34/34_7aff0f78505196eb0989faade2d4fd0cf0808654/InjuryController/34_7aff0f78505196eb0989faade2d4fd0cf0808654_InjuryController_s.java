 package org.openmrs.module.hr.web.controller;
 
 import org.openmrs.api.context.Context;
 import org.openmrs.module.hr.HrInjury;
 import org.openmrs.module.hr.HrStaff;
 import org.openmrs.module.hr.HrStaffNote;
 import org.openmrs.module.hr.api.HRNoteService;
 import org.openmrs.module.hr.api.validator.HRStaffNoteValidator;
 import org.openmrs.web.WebConstants;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.List;
 
 @Controller
 @SessionAttributes("staff")
 public class InjuryController {
 
     private static final String SUCCESS_FORM_VIEW = "/module/hr/manager/injury";
     private static final String SUCCESS_LIST_VIEW = "/module/hr/manager/injuries";
 
 
     @RequestMapping(value="module/hr/manager/injury.form")
     @ModelAttribute("injury")
     public HrInjury showForm(@RequestParam(value="noteId",required=false) Integer noteId){
         HrInjury hrInjury;
         HRNoteService hrNoteService = Context.getService(HRNoteService.class);
         if(noteId != null)
             hrInjury = (HrInjury)hrNoteService.getStaffNoteById(noteId);
         else
             hrInjury = new HrInjury();
         return hrInjury;
     }
 
     @RequestMapping(value ="module/hr/manager/injury.form", method = RequestMethod.POST)
    public ModelAndView createOrUpdateEducation(HttpServletRequest request,@ModelAttribute("injury")  HrInjury injury, BindingResult errors,@ModelAttribute("staff") HrStaff staff){
         HRNoteService hrNoteService = Context.getService(HRNoteService.class);
         if(request.getParameter("action").equalsIgnoreCase("Delete Injury Note"))
             deleteInjury(request,hrNoteService,injury);
        new HRStaffNoteValidator().validate(injury, errors);
        if(errors.hasErrors())
            return new ModelAndView(SUCCESS_FORM_VIEW).addObject("injury",injury);
 
        injury.setHrStaff(staff);
        hrNoteService.saveNote(injury);
        request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Injury saved Successfully");
        return new ModelAndView(SUCCESS_FORM_VIEW).addObject("education",hrNoteService.getStaffNoteById(injury.getNoteId()));
     }
 
     private void deleteInjury(HttpServletRequest request, HRNoteService hrNoteService, HrInjury injury) {
         hrNoteService.deleteInjury(hrNoteService.getStaffNoteById(injury.getNoteId()));
         request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Injury deleted Successfully");
 
     }
 
     @RequestMapping(value = "module/hr/manager/injuries.list")
     public String showList(ModelMap model ,@ModelAttribute("staff") HrStaff staff){
         HRNoteService hrNoteService = Context.getService(HRNoteService.class);
         List<HrStaffNote> injuries = hrNoteService.getHeadNotesForStaff(staff, "injury");
         model.addAttribute("injuries",injuries);
         return SUCCESS_LIST_VIEW;
     }
 
 }
