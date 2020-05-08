 package edu.northwestern.bioinformatics.studycalendar.web.template;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.*;
 import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
 import edu.northwestern.bioinformatics.studycalendar.domain.Role;
 import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
 import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
 import edu.northwestern.bioinformatics.studycalendar.web.activity.AdvancedEditActivityCommand;
 import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindException;
 import org.springframework.validation.Errors;
 import org.springframework.web.bind.ServletRequestDataBinder;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author Jaron Sampson
  * @author Jalpa Patel
  */
 @AccessControl(roles = Role.STUDY_COORDINATOR)
 public class NewActivityController extends PscSimpleFormController {
     public static final String PSC_CREATE_NEW_ACTIVITY_SOURCE_NAME = "PSC - Manual Activity Creation";
 
     private ActivityDao activityDao;
     Activity activity;
     private SourceDao sourceDao;
     private ActivityTypeDao activityTypeDao;
     private ActivityPropertyDao activityPropertyDao;
 
     public NewActivityController() {
         setCommandClass(AdvancedEditActivityCommand.class);
         setValidator(new ValidatableValidator());
         setBindOnNewForm(true);
         setFormView("advancedEditActivity");
         setSuccessView("viewActivity");
     }
 
     @Override
     protected Map referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
         Map<String, Object> refdata = new HashMap<String, Object>();
         refdata.put("activityTypes", activityTypeDao.getAll());
         refdata.put("action", "New");
         return refdata;
     }
 
     protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
         super.initBinder(request, binder);
         getControllerTools().registerDomainObjectEditor(binder, "activity.type", activityTypeDao);
     }
 
     @Override
     protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
         AdvancedEditActivityCommand command = (AdvancedEditActivityCommand) oCommand;
         command.getActivity().setSource(sourceDao.getByName(PSC_CREATE_NEW_ACTIVITY_SOURCE_NAME));
         Activity activity = command.updateActivity();
 
         if (request.getParameter("returnToPeriod") == null) {
             Map<String, Object> model = errors.getModel();
             model.put("activity", activity);
             return new ModelAndView(getSuccessView(), model);
         } else {
             ModelMap model = new ModelMap("period",Integer.parseInt(request.getParameter("returnToPeriod")))
                     .addObject("selectedActivity", activity.getId());
               return new ModelAndView("redirectToManagePeriod", model);
         }
     }
 
     protected Object formBackingObject(HttpServletRequest request) throws Exception {
         Activity activity = new Activity();
         return new AdvancedEditActivityCommand(activity, activityDao, activityPropertyDao);
     }
 
     ////// CONFIGURATION
 
     @Required
     public void setActivityDao(ActivityDao activityDao) {
         this.activityDao = activityDao;
     }
 
     @Required
     public void setSourceDao(SourceDao sourceDao) {
         this.sourceDao = sourceDao;
     }
 
     @Required
     public void setActivityTypeDao(ActivityTypeDao activityTypeDao) {
         this.activityTypeDao = activityTypeDao;
     }
 
     @Required
     public void setActivityPropertyDao(ActivityPropertyDao activityPropertyDao) {
         this.activityPropertyDao = activityPropertyDao;
     }
 
 }
