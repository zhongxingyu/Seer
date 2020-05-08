 /*******************************************************************************
  * Copyright (c) May 18, 2011 NetXForge.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *  You should have received a copy of the GNU Lesser General Public License
  *   along with this program.  If not, see <http://www.gnu.org/licenses/>
  *
  * Contributors:
  *    Christophe Bouhier - initial API and implementation and/or initial documentation
  *******************************************************************************/ 
 package com.netxforge.netxstudio.ui.activities;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.activities.IActivityManager;
 import org.eclipse.ui.activities.IWorkbenchActivitySupport;
 
 import com.netxforge.netxstudio.data.fixtures.IFixtures;
 import com.netxforge.netxstudio.generics.Role;
 
 
 /**
  * Maps a user role, to it's activities. 
  * Enables/disables activities based on the role. 
  * 
  * Note: Roles to activity mapping is hard coded. 
  * 
  * FIXME It doesn't seem to be possible to "disable" activities? 
  * 
  * @author Christophe Bouhier christophe.bouhier@netxforge.com
  *
  */
 public class ActivityAndRoleService implements IActivityAndRoleService {
 
 
 	
 	public static String ACTIVITY_ADMIN = "com.netxforge.netxstudio.ui.activity.admin";
 	public static String ACTIVITY_MONITORING = "com.netxforge.netxstudio.ui.activity.monitoring";
	public static String ACTIVITY_IMPORT = "com.netxforge.netxstudio.ui.activity.wizard";
 	
 	public ActivityAndRoleService(){
 	}
 	
 	/**
 	 * Creates a map of activities versus roles. 
 	 * @param role
 	 */
 	protected Set<String> activitiesForRole(Role role){
 		Set<String> activities = new HashSet<String>();
 		
 		if( role.getName().equals(IFixtures.ROLE_ADMIN)){
 			activities.add(ACTIVITY_ADMIN);
 			activities.add(ACTIVITY_MONITORING);
			activities.add(ACTIVITY_IMPORT);
 			return activities;
 		} else if( role.getName().equals(IFixtures.ROLE_PLANNER)){
 			activities.add(ACTIVITY_MONITORING);
			activities.add(ACTIVITY_IMPORT);
 		} else if( role.getName().equals((IFixtures.ROLE_READONLY))){
 			activities.add(ACTIVITY_MONITORING);
 		}
 		
 		
 		return activities;
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.netxforge.netxstudio.ui.activities.IActivityAndRoleService#enableActivity(com.netxforge.netxstudio.generics.Role)
 	 */
 	public void enableActivity(Role role){
 		if(role == null){
 			return;
 		}
 		Set<String> enabledActivities = activitiesForRole(role);
 		if(assertDefinedActivities(enabledActivities)){
 			PlatformUI.getWorkbench().getActivitySupport().setEnabledActivityIds(enabledActivities);
 		}
 	}
 	
 	/**
 	 * Get the activity manager. 
 	 * @return
 	 */
 	protected IActivityManager getActivitiyManager(){
 		IWorkbenchActivitySupport activitySupport = PlatformUI.getWorkbench().getActivitySupport();
 		IActivityManager activityManager = activitySupport.getActivityManager();
 		return activityManager;
 	} 
 	
 	/**
 	 * False if one of the activities is not defined. 
 	 * @param activities
 	 * @return
 	 */
 	protected boolean assertDefinedActivities(Set<String> activities){
 		
 		boolean allDefined = true;
 		
 		for (String string : activities) {
 			if(!this.getActivitiyManager().getActivity(string).isDefined()){
 				allDefined = false;
 				break;
 			}	
 		}
 		return allDefined;
 	}
 	
 }
