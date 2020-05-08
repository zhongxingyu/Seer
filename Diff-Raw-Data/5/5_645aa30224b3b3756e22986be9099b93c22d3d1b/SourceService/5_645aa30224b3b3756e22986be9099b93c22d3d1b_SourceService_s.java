 package edu.northwestern.bioinformatics.studycalendar.service;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
 import edu.northwestern.bioinformatics.studycalendar.domain.Source;
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
 import org.springframework.beans.BeanUtils;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.persistence.Transient;
 import javax.transaction.Transaction;
 import java.util.*;
 
 /**
  * @author Saurabh Agrawal
  */
 @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
 public class SourceService {
     private SourceDao sourceDao;
     private ActivityService activityService;
 
 
     public Source getByName(String name) {
         return sourceDao.getByName(name);
     }
 
     /**
      * <p>  Updates the source. Following is the logic to update a source
      * <li>
      * Add any activities that do not already exist.   </li>
      * <li> Update any activities that already exist and still exist in the new representation.  Activities should be matched by their natural key -- i.e., the code. </li>
      * <li>Remove any activities that do not exist in the new representation, so long as they are not used any any existing templates.     </li>
      * </p>
      *
      * @param source       source
      * @param targetSource targetSource
      */
     @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
     public void updateSource(final Source source, final Source targetSource) {
         BeanUtils.copyProperties(source, targetSource, new String[]{"activities", "id"});
 
         //delete  or update the activity
 
         removeAndUpdateActivities(targetSource.getActivities(), source.getActivities());
 
         //add new activities
         targetSource.addNewActivities(source.getActivities());
         sourceDao.save(source);
 
 
     }
 
     /**
      * Add, update or remove activities from source.
      *
      * @param existingSource
      * @param activitiesToAddAndRemove
      */
     @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
     public void updateSource(final Source existingSource, final List<Activity> activitiesToAddAndRemove) {
         //delete  or update the activity
         removeAndUpdateActivities(existingSource.getActivities(), activitiesToAddAndRemove);
         //add new activities
         existingSource.addNewActivities(activitiesToAddAndRemove);
         sourceDao.save(existingSource);
     }
 
     /**
      * <p>Updates and remove activities from source.
      * <li> Update any activities that already exist and still exist in the new representation.  Activities should be matched by their natural key -- i.e., the code. </li>
      * <li>Remove any activities that do not exist in the new representation, so long as they are not used any any existing templates.     </li>
      * </p>
      *
      * @param targetActivities         existing activities
      * @param activitiesToAddAndRemove
      */
     @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
     private void removeAndUpdateActivities(List<Activity> targetActivities, final List<Activity> activitiesToAddAndRemove) {
         //delete  or update the activity
         List<Activity> activitiesToRemove = new ArrayList<Activity>();
         Map<Activity, Activity> activityToUpdate = new HashMap<Activity,Activity>();
         for (Activity existingActivity : targetActivities) {
 
             Activity activity = existingActivity.findActivityInCollectionWhichHasSameCode(activitiesToAddAndRemove);
 
             if (activity != null) {
                 //for the case when exist A:1 and B:2, and we import A:2 and B:1 - we need to check names
                 Activity activityWithName = existingActivity.findActivityInCollectionWhichHasSameName(activitiesToAddAndRemove);
                if (activityWithName == null || (activityWithName!= null && activityWithName.equals(activity))) {
                     activityToUpdate.put(existingActivity, activity);
                 } else {
                     activitiesToRemove.add(existingActivity);
                 }
             } else {
                 activitiesToRemove.add(existingActivity);
            }
         }
 
         targetActivities.removeAll(activitiesToRemove);
 
         for (Activity activity : activitiesToRemove) {
             boolean deleteActivity = activityService.deleteActivity(activity);
             //remove this activity only if its not used any where
             if (!deleteActivity) {
                 targetActivities.addAll(activitiesToRemove);
                 throw new StudyCalendarValidationException("Import failed. " +
                        "Activity " + activity.getName() + " with code " + activity.getCode() + " is referenced withing the study. " +
                         "Please remove those references manuall and try to import activity again "); 
             }
         }
 
         Set<Activity> existingActivities = activityToUpdate.keySet();
         for (Activity existingActivity : existingActivities) {
             Activity activity = activityToUpdate.get(existingActivity);
             existingActivity.updateActivity(activity);
         }
     }
 
 
 
     @Required
     public void setSourceDao(final SourceDao sourceDao) {
         this.sourceDao = sourceDao;
     }
 
     public void setActivityService(ActivityService activityService) {
         this.activityService = activityService;
     }
 }
