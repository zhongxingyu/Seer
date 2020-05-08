 package com.pluralsite.repository;
 
 import java.util.List;
 import java.util.ArrayList;
 
 import com.pluralsite.model.Activity;
 
 public class ActivityRepositoryStub implements ActivityRepository {
 	
 	/* (non-Javadoc)
 	 * @see com.pluralsite.repository.ActivitiesRespository#findAllActivities()
 	 */
 	@Override
 	public List<Activity> findAllActivities() { 
 		List<Activity> activities = new ArrayList<Activity>();
 		Activity act1 = new Activity();
 		act1.setDescription("Swimming");
 		act1.setDuration(55);
 		activities.add(act1);
 		
 		Activity act2 = new Activity();
		act1.setDescription("Cycling");
		act1.setDuration(120);
 		activities.add(act2);
 		
 		return activities;
 		
 	}
 
 }
