 package edu.northwestern.bioinformatics.studycalendar.web.activity;
 
 import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractController;
 import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
 import edu.northwestern.bioinformatics.studycalendar.service.ActivityService;
 import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
 import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
 import edu.northwestern.bioinformatics.studycalendar.domain.Source;
 import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.bind.ServletRequestUtils;
 import org.springframework.beans.factory.annotation.Required;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: nshurupova
  * Date: Jul 29, 2008
  * Time: 10:38:17 AM
  * To change this template use File | Settings | File Templates.
  */
 public class DeleteController extends PscAbstractController {
     private ActivityDao activityDao;
     private PlannedActivityDao plannedActivityDao;
     private ActivityService activityService;
 
     public DeleteController() {
         setCrumb(new DefaultCrumb("Activities"));
     }
 
     @Override
      protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
         Map<String, Object> model = new HashMap<String, Object>();
         //deleteAction
         Integer activityId = ServletRequestUtils.getRequiredIntParameter(request, "activityId");
         Activity a = activityDao.getById(activityId);
         Source source = a.getSource();
         String message = "Can not delete the activity because activity is used by other planned activities";
         boolean deleted= activityService.deleteActivity(a);
         if (!deleted){
             log.error(message);
             model.put("error", message);
         }
         List<Activity> activities = activityDao.getBySourceId(source.getId());
         Map<Integer, Boolean> enableDelete = new HashMap<Integer, Boolean>();
         for (Activity activity : activities) {
             if (plannedActivityDao.getPlannedActivitiesForAcivity(activity.getId()).size()>0) {
                 enableDelete.put(activity.getId(), false);
             } else {
                 enableDelete.put(activity.getId(), true);
             }
         }
         model.put("enableDeletes", enableDelete);
         model.put("activitiesPerSource", activities);
         model.put("activityTypes", ActivityType.values());
        model.put("displayCreateNewActivity", Boolean.TRUE);
         return new ModelAndView("template/ajax/activityTableUpdate", model);
     }
 
    //// CONFIGURATION
 
     @Required
     public void setActivityDao(ActivityDao activityDao) {
         this.activityDao = activityDao;
     }
 
     @Required
     public void setPlannedActivityDao(PlannedActivityDao plannedActivityDao) {
         this.plannedActivityDao = plannedActivityDao;
     }
 
     @Required
     public void setActivityService(final ActivityService activityService) {
         this.activityService = activityService;
     }
     
 }
