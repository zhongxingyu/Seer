 package edu.rpi.rocs.client.services.schedulemanager;
 
 import java.util.List;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.rpc.RemoteService;
 import com.google.gwt.user.client.rpc.ServiceDefTarget;
 
 import edu.rpi.rocs.client.objectmodel.SchedulerManager;
 
 public interface ScheduleManagerService extends RemoteService {
 	public List<String> getScheduleList(String user);
 	public void saveSchedule(String name, SchedulerManager schedule);
	public SchedulerManager loadSchedule(String user, String name, int semesterid);
 	public String getMOTD();
 	
 	public static class Singleton {
 		private static ScheduleManagerServiceAsync instance = null;
 		public static ScheduleManagerServiceAsync getInstance() {
 			if (instance == null) {
 				instance = (ScheduleManagerServiceAsync)GWT.create(ScheduleManagerService.class);
 				((ServiceDefTarget)instance).setServiceEntryPoint(GWT.getModuleBaseURL() + "ScheduleManagerService");
 			}
 			return instance;
 		}
 	}
 }
