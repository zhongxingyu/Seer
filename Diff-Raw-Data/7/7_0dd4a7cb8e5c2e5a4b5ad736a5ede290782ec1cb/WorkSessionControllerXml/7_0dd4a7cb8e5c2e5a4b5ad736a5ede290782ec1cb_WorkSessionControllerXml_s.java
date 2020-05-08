 package ch.hsr.waktu.controller.datacontroller;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.trolltech.qt.core.QDate;
 
import ch.hsr.waktu.controller.datacontroller.WorkSessionController;
 import ch.hsr.waktu.domain.Project;
 import ch.hsr.waktu.domain.Usr;
 import ch.hsr.waktu.domain.WorkPackage;
 import ch.hsr.waktu.domain.WorkSession;
 import ch.hsr.waktu.services.TimeUtil;
 import ch.hsr.waktu.services.WaktuException;
 import ch.hsr.waktu.services.XmlUtil;
 
 public class WorkSessionControllerXml extends WorkSessionController {
 private String workSessionFilePath = "./test/testdata/worksessions.xml";
 	
 	private static WorkSessionControllerXml theInstance = null;
 
 	public static WorkSessionControllerXml getInstance() {
 		if (theInstance == null) {
 			theInstance = new WorkSessionControllerXml();
 		}
 		return theInstance;
 	}
 	
 	public static void setInstance(
 			WorkSessionControllerXml workSessionControllerInstance) {
 		theInstance = workSessionControllerInstance;
 	}
 	
 	public WorkSessionControllerXml() {
 		
 	}
 	
 	private List<WorkSession> getAllWorkSessions() throws WaktuException {
 		return XmlUtil.getWorkSessionsFromXml(workSessionFilePath);
 	}
 	
 	@Override
 	public List<WorkSession> getWorkSessions(Usr user) throws WaktuException {
 
 		List<WorkSession> workSessionsByUser = new ArrayList<WorkSession>();
 		
 		for(WorkSession ws: getAllWorkSessions()){
 			if(ws.getUser().equals(user)) {
 				workSessionsByUser.add(ws);
 			}
 		}
 		return workSessionsByUser;
 }
 
 	@Override
 	public List<WorkSession> getWorkSessions(Usr user, QDate date)
 		throws WaktuException {
 		
 		List<WorkSession> workSessionsByDate = new ArrayList<WorkSession>();
 		
 		for(WorkSession ws: getWorkSessions(user)) {
 			if(TimeUtil.convertGregorianToQDateTime(ws.getStart()).date().equals(date)) {
 				workSessionsByDate.add(ws);
 			}
 		}
 	
 		return workSessionsByDate;
 	}
 	
 	@Override
 	public List<WorkSession> getWorkSessions(Usr user, QDate fromDate, QDate toDate) throws WaktuException {
 		List<WorkSession> workSessionsByUserInTimeRange = new ArrayList<WorkSession>();
 		
 		for(WorkSession ws: getWorkSessions(user)) {
 			if(TimeUtil.convertGregorianToQDateTime(ws.getStart()).date().daysTo(fromDate) >= 0 &&
 					TimeUtil.convertGregorianToQDateTime(ws.getEnd()).date().daysTo(toDate) <= 0) {
 				workSessionsByUserInTimeRange.add(ws);
 			}
 		}
 		return workSessionsByUserInTimeRange;
 	}
 	
 	@Override
 	public List<WorkSession> getWorkSessions(WorkPackage workPackage) throws WaktuException {
 	    List<WorkSession> workSessionsByWorkPackage = new ArrayList<WorkSession>();
         
         for(WorkSession ws: getAllWorkSessions()){
             if(ws.getWorkPackage().equals(workPackage)) {
                 workSessionsByWorkPackage.add(ws);
             }
         }
         return workSessionsByWorkPackage;
 	}
 
 	@Override
 	public List<WorkSession> getWorkSessions(WorkPackage workPackage, Usr user) throws WaktuException {
 List<WorkSession> workSessionsByWorkPackageAndUser = new ArrayList<WorkSession>();
         
         for(WorkSession ws: getWorkSessions(workPackage)){
             if(ws.getUser().equals(user)) {
                 workSessionsByWorkPackageAndUser.add(ws);
             }
         }
         return workSessionsByWorkPackageAndUser;
 	}
 
 	@Override
 	public List<WorkSession> getWorkSessions(Project project)
 		throws WaktuException {
 		
 		List<WorkSession> workSessionsByProject = new ArrayList<WorkSession>();
 
 		for(WorkSession ws: getAllWorkSessions()) {
 			if(ws.getWorkPackage().getProject().equals(project)) {
 				workSessionsByProject.add(ws);
 			}
 		}
 		return workSessionsByProject;
 	}
 
 	@Override
 	public List<WorkSession> getWorkSessions(Project project, Usr user) throws WaktuException {
 		List<WorkSession> workSessionsByProjectAndUser = new ArrayList<WorkSession>();
 		for(WorkSession ws: getWorkSessions(user)) {
 			if(ws.getWorkPackage().getProject().equals(project)) {
 				workSessionsByProjectAndUser.add(ws);
 			}
 		}
 		return workSessionsByProjectAndUser;
 	}
 	
 	@Override
 	public List<WorkSession> getWorkSessions(Project project, QDate fromDate, QDate toDate) throws WaktuException {
 		List<WorkSession> workSessionsByProjectInTimeRange = new ArrayList<WorkSession>();
 		
 		for(WorkSession ws: getWorkSessions(project)) {
 			if(TimeUtil.convertGregorianToQDateTime(ws.getStart()).date().daysTo(fromDate) >= 0 &&
 					TimeUtil.convertGregorianToQDateTime(ws.getEnd()).date().daysTo(toDate) <= 0) {
 				workSessionsByProjectInTimeRange.add(ws);
 			}
 		}
 		return workSessionsByProjectInTimeRange;
 	}
 
 }
