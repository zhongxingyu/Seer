 package ch.hsr.waktu.controller;
 
 import java.util.List;
 
 import ch.hsr.waktu.controller.datacontroller.WorkSessionController;
 import ch.hsr.waktu.domain.Project;
 import ch.hsr.waktu.domain.Usr;
 import ch.hsr.waktu.domain.WorkPackage;
 import ch.hsr.waktu.domain.WorkSession;
 import ch.hsr.waktu.services.TimeUtil;
 import ch.hsr.waktu.services.WaktuException;
 
 import com.trolltech.qt.core.QDate;
 
 public class TimeController {
     
     private TimeController() {
         
     }
 
 	public static final double HOURS_PER_WORKDAY = 8.5;
 
 	public static double calculateWorktime(Usr usr, Project project,
 			WorkPackage workPackage, QDate start, QDate end)
 			throws WaktuException {
 		double worktime = 0;
 		List<WorkSession> workSessions = null;
 
 		if (project != null && usr == null && workPackage == null) {
 			workSessions = WorkSessionController.getInstance().getWorkSessions(
 					project);
 		} else if (project != null && usr != null && workPackage == null) {
 			workSessions = WorkSessionController.getInstance().getWorkSessions(
 					project, usr);
 		} else if (project == null && usr != null && workPackage == null) {
 			workSessions = WorkSessionController.getInstance().getWorkSessions(
 					usr);
 		} else if (usr == null && workPackage != null) {
 			workSessions = WorkSessionController.getInstance().getWorkSessions(workPackage);
 		} else if (usr != null && workPackage != null) {
 			workSessions = WorkSessionController.getInstance().getWorkSessions(workPackage, usr);
 		}
 
		if(workSessions == null) {
		    return worktime;
		}
		
 		try {
 			for (WorkSession ws : workSessions) {
 				if ((start == null || start.compareTo(TimeUtil
 								.convertGregorianToQDateTime(ws.getStart())
 								.date()) <= 0)
 						&& (end == null || end.compareTo(TimeUtil
 								.convertGregorianToQDateTime(ws.getEnd())
 								.date()) >= 0)) {
 					worktime += TimeUtil.calculateTimespanInSeconds(
 							ws.getStart(), ws.getEnd());
 				}
 			}
 		} catch (NullPointerException e) {
 			return 0;
 		}
 		return roundToTenth(worktime / 3600);
 	}
 
 	public static double calculateOvertime(Usr user, QDate fromDate,
 			QDate toDate) throws WaktuException {
 		return calculateWorktime(user, null, null, fromDate, toDate)
 				- getPlannedTime(user, fromDate, toDate);
 	}
 
 	public static double calculateWorktimeForWeek(Usr user, QDate date)
 			throws WaktuException {
 		QDate[] weekStartDateEndDate = TimeUtil.getWeekBoundaries(date);
 		return calculateWorktime(user, null, null, weekStartDateEndDate[0],
 				weekStartDateEndDate[1]);
 	}
 
 	public static double calculateWorktimeForMonth(Usr user, QDate date)
 			throws WaktuException {
 		QDate[] monthStartDateEndDate = TimeUtil.getMonthBoundaries(date);
 		return calculateWorktime(user, null, null, monthStartDateEndDate[0],
 				monthStartDateEndDate[1]);
 	}
 
 	public static double getPlannedTime(Usr user, QDate currMonth) {
 		return roundToTenth(getNumberOfWorkdaysForMonth(user, currMonth)
 				* HOURS_PER_WORKDAY);
 	}
 
 	public static double getPlannedTime(Usr user, QDate fromDate, QDate toDate) {
 		return roundToTenth(getNumberOfWorkdays(user, fromDate, toDate)
 				* HOURS_PER_WORKDAY);
 	}
 
 	private static double getNumberOfWorkdaysForMonth(Usr user, QDate currMonth) {
 		return user.getPensum()
 				/ 100.0
 				* getNumberOfWorkdays(user,
 						TimeUtil.getMonthBoundaries(currMonth)[0],
 						TimeUtil.getMonthBoundaries(currMonth)[1]);
 	}
 
 	private static double getNumberOfWorkdays(Usr user, QDate fromDate,
 			QDate toDate) {
 		int days = 0;
 		for (QDate d = fromDate; d.compareTo(toDate) <= 0; d = d.addDays(1)) {
 			if (d.dayOfWeek() != 6 && d.dayOfWeek() != 7) {
 				days++;
 			}
 		}
 		return user.getPensum() / 100.0 * days;
 	}
 
 	private static double roundToTenth(double d) {
 		return Math.round(d * 10.) / 10.;
 	}
 
 }
