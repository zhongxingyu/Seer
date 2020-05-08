 package ch.hsr.waktu.controller.datacontroller;
 
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 
 import org.apache.log4j.Logger;
 
 import ch.hsr.waktu.controller.PersistenceController;
 import ch.hsr.waktu.domain.Project;
 import ch.hsr.waktu.domain.Usr;
 import ch.hsr.waktu.domain.WorkPackage;
 import ch.hsr.waktu.domain.WorkSession;
 import ch.hsr.waktu.services.TimeUtil;
 
 import com.trolltech.qt.QSignalEmitter;
 import com.trolltech.qt.core.QDate;
 import com.trolltech.qt.core.QDateTime;
 
 /**
  * @author simon.staeheli
  * @version 1.0
  * @created 01-Apr-2011 15:36:30
  */
 public class WorkSessionController extends QSignalEmitter {
 
 	private static WorkSessionController theInstance = null;
 
 	public static WorkSessionController getInstance() {
 		if (theInstance == null) {
 			theInstance = new WorkSessionController();
 		}
 		return theInstance;
 	}
 
 	private Logger logger = Logger.getLogger(UserController.class);
 	public Signal0 update = new Signal0();
 	public Signal1<WorkSession> add = new Signal1<WorkSession>();
 	public Signal1<WorkSession> removed = new Signal1<WorkSession>();
 
 	private WorkSessionController() {
 
 	}
 
 	/**
 	 * 
 	 * @param user
 	 * @throws WaktuGeneralException
 	 */
 	@SuppressWarnings("unchecked")
 	public List<WorkSession> getWorkSessions(Usr user)
 			throws WaktuGeneralException {
 		EntityManager em = PersistenceController.getInstance().getEMF()
 				.createEntityManager();
 
 		List<WorkSession> workSessionsByUser;
 		try {
 			workSessionsByUser = em.createQuery(
 					"SELECT ws FROM WorkSession ws JOIN ws.userref u WHERE u.usrid = '"
 							+ user.getId() + "'").getResultList();
 		} catch (IllegalStateException e) {
 			throw new WaktuGeneralException("Database problem");
 		} catch (IllegalArgumentException e) {
 			throw new WaktuGeneralException("Illegal Argument");
 		} catch (Exception e) {
 			throw new WaktuGeneralException("General problem");
 		} finally {
 			em.close();
 		}
 		return workSessionsByUser;
 	}
 
 	/**
 	 * 
 	 * @param user
 	 * @param date
 	 * @throws WaktuGeneralException
 	 */
 	@SuppressWarnings("unchecked")
 	public List<WorkSession> getWorkSessions(Usr user, QDate date)
 			throws WaktuGeneralException {
 		EntityManager em = PersistenceController.getInstance().getEMF()
 				.createEntityManager();
 
 		List<WorkSession> workSessionsByDate;
 		try {
			workSessionsByDate = em.createQuery(
					"SELECT ws FROM WorkSession ws JOIN ws.userRef u WHERE ws.startTime = '"
							+ date.toString("yyyy-MM-dd") + "' AND u.usrid = '"
							+ user.getId() + "'").getResultList();
 		} catch (IllegalStateException e) {
 			throw new WaktuGeneralException("Database problem");
 		} catch (IllegalArgumentException e) {
 			throw new WaktuGeneralException("Illegal Argument");
 		} catch (Exception e) {
 			throw new WaktuGeneralException("General problem");
 		} finally {
 			em.close();
 		}
 		return workSessionsByDate;
 	}
 
 	// TODO: MF: Wird diese Methode noch gebraucht?
 	public List<WorkSession> getWorkSessionsStartingAt(Usr user, QDate startDate) {
 		EntityManager em = PersistenceController.getInstance().getEMF()
 				.createEntityManager();
 
 		@SuppressWarnings("unchecked")
 		List<WorkSession> workSessionsByUser = em.createQuery(
 				"SELECT ws FROM WorkSession ws ORDER BY ws.id").getResultList();
 
 		ArrayList<WorkSession> workSessions = new ArrayList<WorkSession>();
 		for (WorkSession ws : workSessionsByUser) {
 			if (ws.getUser().equals(user)
 					&& ws.getStart().equals(
 							TimeUtil.convertQDateTimeToGregorian(new QDateTime(
 									startDate, null)))) {
 				workSessions.add(ws);
 				// logger.info("WORKSESSION: " + ws.toString());
 			}
 
 		}
 
 		em.close();
 		return workSessions;
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<WorkSession> getWorkSessions(Project project)
 			throws WaktuGeneralException {
 		EntityManager em = PersistenceController.getInstance().getEMF()
 				.createEntityManager();
 
 		List<WorkSession> workSessionsByProject;
 		try {
 			workSessionsByProject = em
 					.createQuery(
 							"SELECT ws FROM WorkSession ws JOIN ws.workpackageref wp JOIN wp.project p WHERE p.projectid = '"
 									+ project.getId() + "'").getResultList();
 		} catch (IllegalStateException e) {
 			throw new WaktuGeneralException("Database problem");
 		} catch (IllegalArgumentException e) {
 			throw new WaktuGeneralException("Illegal Argument");
 		} catch (Exception e) {
 			throw new WaktuGeneralException("General problem");
 		} finally {
 			em.close();
 		}
 		return workSessionsByProject;
 	}
 
 	/**
 	 * 
 	 * @param user
 	 * @param workPackage
 	 * @param startTime
 	 * @param endTime
 	 * @throws WaktuGeneralException
 	 */
 	public WorkSession addWorkSession(Usr user, WorkPackage workPackage,
 			GregorianCalendar startTime, GregorianCalendar endTime,
 			String description) throws WaktuGeneralException {
 		EntityManager em = PersistenceController.getInstance().getEMF()
 				.createEntityManager();
 		WorkSession newWorkSession = new WorkSession(user, workPackage,
 				startTime, endTime, description);
 		try {
 			em.getTransaction().begin();
 			em.persist(newWorkSession);
 			em.getTransaction().commit();
 		} catch (IllegalStateException e) {
 			throw new WaktuGeneralException("Database problem");
 		} catch (IllegalArgumentException e) {
 			throw new WaktuGeneralException("Illegal Argument");
 		} catch (Exception e) {
 			throw new WaktuGeneralException("General problem");
 		} finally {
 			em.close();
 		}
 
 		add.emit(newWorkSession);
 		logger.info("favorite " + newWorkSession + " deleted");
 		return newWorkSession;
 	}
 
 	public void updateWorkSession(WorkSession workSession)
 			throws WaktuGeneralException {
 		EntityManager em = PersistenceController.getInstance().getEMF()
 				.createEntityManager();
 		WorkSession updateWorkSession;
 		try {
 			em.getTransaction().begin();
 			updateWorkSession = em.find(WorkSession.class, workSession.getId());
 
 			updateWorkSession.setUser(workSession.getUser());
 			updateWorkSession.setWorkPackage(workSession.getWorkPackage());
 			updateWorkSession.setStart(workSession.getStart());
 			updateWorkSession.setEnd(workSession.getEnd());
 			updateWorkSession.setDescription(workSession.getDescription());
 
 			em.getTransaction().commit();
 		} catch (IllegalStateException e) {
 			throw new WaktuGeneralException("Database problem");
 		} catch (IllegalArgumentException e) {
 			throw new WaktuGeneralException("Illegal Argument");
 		} catch (Exception e) {
 			throw new WaktuGeneralException("General problem");
 		} finally {
 			em.close();
 		}
 		update.emit();
 		logger.info("workSession " + updateWorkSession + " updated");
 	}
 
 	/**
 	 * 
 	 * @param workSession
 	 */
 	public void removeWorkSession(WorkSession workSession)
 			throws WaktuGeneralException {
 		EntityManager em = PersistenceController.getInstance().getEMF()
 				.createEntityManager();
 		try {
 			em.getTransaction().begin();
 			em.remove(em.find(WorkSession.class, workSession.getId()));
 			em.getTransaction().commit();
 		} catch (IllegalStateException e) {
 			throw new WaktuGeneralException("Database problem");
 		} catch (IllegalArgumentException e) {
 			throw new WaktuGeneralException("Illegal Argument");
 		} catch (Exception e) {
 			throw new WaktuGeneralException("General problem");
 		} finally {
 			em.close();
 		}
 		removed.emit(workSession);
 		logger.info("workSession " + workSession + " deleted");
 	}
 }
