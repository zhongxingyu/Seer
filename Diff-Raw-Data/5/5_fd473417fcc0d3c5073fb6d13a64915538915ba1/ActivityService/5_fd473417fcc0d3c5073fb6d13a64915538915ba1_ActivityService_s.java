 package no.fll.activity;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.orm.hibernate3.HibernateTemplate;
 
 import no.fll.web.JsonReaderResponse;
 
 
 public class ActivityService {
 
 	@Autowired
 	private HibernateTemplate hibernateTemplate;
 	
 	public JsonReaderResponse<Activity> getActivities() {
 		return new JsonReaderResponse(hibernateTemplate.loadAll(Activity.class));
 	}
 
 	public JsonReaderResponse<Activity> createActivity(List<Activity> activities) {
 		hibernateTemplate.saveOrUpdateAll(activities);
		return getActivities();
 	}
 
 	public JsonReaderResponse<Activity> updateActivity(List<Activity> oldValues, List<Activity> newValues) {
 		return createActivity(newValues);
 	}
 
 	public JsonReaderResponse<Activity> deleteActivity(List<Activity> activities) {
 		hibernateTemplate.deleteAll(activities);
		return getActivities();
 	}
 }
