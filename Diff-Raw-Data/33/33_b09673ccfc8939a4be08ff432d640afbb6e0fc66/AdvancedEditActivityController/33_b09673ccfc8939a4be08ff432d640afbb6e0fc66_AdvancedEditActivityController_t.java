 package edu.northwestern.bioinformatics.studycalendar.web.activity;
 
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityPropertyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
 import edu.northwestern.bioinformatics.studycalendar.service.GeneratedUriTemplateVariable;
 import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
 import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
 import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.beans.factory.annotation.Required;
 import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
 import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
 import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
 
 import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.BUSINESS_ADMINISTRATOR;
 
 /**
  * @author Jalpa Patel
  */
 public class AdvancedEditActivityController extends PscSimpleFormController implements PscAuthorizedHandler {
     private ActivityDao activityDao;
     Activity activity;
     private ActivityTypeDao activityTypeDao;
     private ActivityPropertyDao activityPropertyDao;
     public AdvancedEditActivityController() {
         setFormView("advancedEditActivity");
         setSuccessView("viewActivity");
         setBindOnNewForm(true);
     }
 
     public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
         return ResourceAuthorization.createCollection(BUSINESS_ADMINISTRATOR);
     }
     
     @Override
     protected Object formBackingObject(HttpServletRequest request) throws Exception {
         Integer activityId = ServletRequestUtils.getIntParameter(request, "activityId");
         if (activityId == null) {
             activity = new Activity();
         } else {
 
             activity = activityDao.getById(activityId);
         }
         return new AdvancedEditActivityCommand(activity, activityDao, activityPropertyDao);
     }
 
     protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
         super.initBinder(request, binder);
         getControllerTools().registerDomainObjectEditor(binder, "activity.type", activityTypeDao);
     }
 
     @Override
     protected Map referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
         Map<String, Object> refdata = new HashMap<String, Object>();
         refdata.put("existingList",((AdvancedEditActivityCommand) command).getExistingUri());
        refdata.put("action", "Edit");
         refdata.put("activityTypes", activityTypeDao.getAll());
         refdata.put("variables", GeneratedUriTemplateVariable.values());
         if(activity.getType()!=null)
             refdata.put("activityDefaultType" ,activity.getType().getName());
         refdata.put("activity",activity);
         refdata.put("source", activity.getSource());
         return refdata;
     }
 
     @Override
     protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
         AdvancedEditActivityCommand command = (AdvancedEditActivityCommand) oCommand;
         command.updateActivity();
         Map<String, Object> model = new HashMap<String, Object>();
         if(activity.getSource() == null) {
             model = errors.getModel();
             model.put("activity", activity);
             return new ModelAndView(getSuccessView(), model);
         } else {
             model.put("sourceId",activity.getSource().getId() );
             return new ModelAndView("redirectToActivities",model);
         }
     }
 
     /////// CONFIGURATION
 
     @Required
     public void setActivityDao(ActivityDao activityDao) {
         this.activityDao = activityDao;
     }
     
     public void setActivity(Activity activity) {
         this.activity = activity;
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
