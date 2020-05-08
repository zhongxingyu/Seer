 package daos;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
import java.util.LinkedList;
 import java.util.List;
 import model.Conference;
 import model.ConferenceFilters;
 import model.ConferencesUsers;
 import model.User;
 import model.UserAttendanceStatus;
 import model.UserRole;
 
 import org.hibernate.Session;
 
 import db.HibernateUtil;
 
 public class ConferencesUsersDao {
 
 	private static ConferencesUsersDao instance = null;
 
 	private ConferencesUsersDao() {
 	}
 
 	public static ConferencesUsersDao getInstance() {
 		if (instance == null) {
 			instance = new ConferencesUsersDao();
 		}
 		return instance;
 	}
 
 	/**
 	 * Assign users to a conference
 	 * @param conference
 	 * @return
 	 */
 	
 	public void assignUserToConference(Conference conference, User user, int role){
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 
 		try {
 			session.beginTransaction();
 			
 			ConferencesUsers cu = new ConferencesUsers(conference, user, role);
 			
 			session.save(cu);
 			session.getTransaction().commit();
 		}
 		catch (RuntimeException e) {
 			session.getTransaction().rollback();
 		    throw e;
 		}	
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<ConferencesUsers> getAllConferenceUsersByConference(Conference conference){
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		List<ConferencesUsers> result = null;
 		try {
 			session.beginTransaction();
 			result = (List<ConferencesUsers>)session.createQuery(
 					"select cu from  ConferencesUsers cu where cu.conference = :conf")
 	                .setEntity("conf", conference)
 	                .list();
 			session.getTransaction().commit();
 		}
 		catch (Exception e) {
 			session.getTransaction().rollback();
 		}		
 	
 		return result;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<ConferencesUsers> getAllConferenceUsersByUser(User user){
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		List<ConferencesUsers> result = null;
 		try {
 			session.beginTransaction();
 			result = (List<ConferencesUsers>)session.createQuery(
 					"select cu from  ConferencesUsers cu where cu.user = :user")
 	                .setEntity("user", user)
 	                .list();
 			session.getTransaction().commit();
 		}
 		catch (Exception e) {
 			session.getTransaction().rollback();
 		}		
 	
 		return result;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<ConferencesUsers> getConderenceUsersByType(Conference conference, UserRole ur ){	
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		List<ConferencesUsers> result = null;
 		try {
 			session.beginTransaction();
 			result = (List<ConferencesUsers>)session.createQuery(
 					"select cu from  ConferencesUsers cu where cu.conference = :conf and cu.userRole = :userRole")
 	                .setEntity("conf", conference)
 	                .setInteger("userRole", ur.getValue())
 	                .list();
 			session.getTransaction().commit();
 		}
 		catch (Exception e) {
 			session.getTransaction().rollback();
 		}
 		
 		return result;
 	}
 	
 	public ConferencesUsers getConferenceUser(Conference conference, User user ){	
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		
 		ConferencesUsers result = null;
 		try {
 			session.beginTransaction();
 			result = (ConferencesUsers)session.createQuery(
 					"select cu from  ConferencesUsers cu where cu.conference = :conf and cu.user = :user")
 	                .setEntity("conf", conference)
 	                .setEntity("user", user)
 	                .uniqueResult();
 			session.getTransaction().commit();
 		}
 		catch (Exception e) {
 			session.getTransaction().rollback();
 		}
 	
 		return result;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<User> getUsersThatNotBelongsToConference(Conference conference){	
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		List<User> result = null;
 		
 		try {
 			session.beginTransaction();
 			result = (List<User>)session.createQuery(
 					"select u from User u where u.admin != 1 and not exists (select 1 from ConferencesUsers cu where cu.user = u and cu.conference = :conf)")
 	                .setEntity("conf", conference)
 	                .list();
 			session.getTransaction().commit();
 			
 		}
 		catch (Exception e) {
 			session.getTransaction().rollback();
 		}
 		
 		return result;
 	}
 	
 	
 	public void removeUserFromConference(Conference conference, User user) throws Exception{
 		ConferencesUsers conferenceUser = getConferenceUser(conference, user);
 		
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		
 		try {
 			session.beginTransaction();
 			session.delete(conferenceUser);
 			session.getTransaction().commit();
 		}
 		catch (Exception e) {
 			session.getTransaction().rollback();
 			throw e;
 		}
 	}
 	
 	/**
 	 * update users attendance approval to the conference
 	 * @param conference
 	 * @param user
 	 * @return
 	 */
 	public void updateUserAttendanceApproval(Conference conference, User user, UserAttendanceStatus status){
 		ConferencesUsers confPart = getConferenceUser(conference, user);
 		
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		
 		try {
 			session.beginTransaction();
 			session.update(confPart.setAttendanceStatus(status));
 			session.getTransaction().commit();
 		}
 		catch (Exception e) {
 			session.getTransaction().rollback();
 		}	
 	}
 	/**
 	 * This function sends a predefined invitation to a conference email to users, 
 	 * as long as they have never gotten such mail before about this conference
 	 * @param user
 	 */
 	public void sendConferenceAssignmentNotificationEmailToUsers(Conference conference, User user){
 		
 	}
 	
 	@SuppressWarnings("unchecked")
 	public UserRole getUserHighestRole(User user){
 		
 		List<ConferencesUsers> results = null;
 		
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		try {
 			session.beginTransaction();
 			results = (List<ConferencesUsers>)session.createQuery(
 					"select cu from  ConferencesUsers cu where cu.user = :user")
 	                .setEntity("user", user)
 	                .list();
 			session.getTransaction().commit();
 		}
 		catch (Exception e) {
 			session.getTransaction().rollback();
 		}
 		
 		UserRole ur = UserRole.NONE;
 		
 		if (results.size() == 0)
 			return ur;
 		
 		for (ConferencesUsers cu : results)
 		{
 			if (cu.getUserRole() < ur.getValue())
 			{
 				ur =  UserRole.resolveUserRole(cu.getUserRole());
 			}
 		}
 		
 		return ur;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<String> getScopedConferenceByDate(User user, String filter)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		
 		List<String> confList;
 		
 		StringBuilder query = new StringBuilder();
 		String filterQuery = "";
 		
 		Date current = new Date();
 		
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 		String startDateFormatted = sdf.format(current);
 
 		ConferenceFilters.ConferenceTimeFilter enumFilter = ConferenceFilters.ConferenceTimeFilter.valueOf(filter); 
 		
 		if (enumFilter == ConferenceFilters.ConferenceTimeFilter.ALL)
 		{
 			filterQuery = "";
 		}
 		else if (enumFilter == ConferenceFilters.ConferenceTimeFilter.CURRENT)
 		{
 			filterQuery = "where '"+ startDateFormatted + "' between DATE_FORMAT(c.startDate, '%Y-%m-%d') and DATE_FORMAT(c.endDate, '%Y-%m-%d')";
 		}
 		else if (enumFilter == ConferenceFilters.ConferenceTimeFilter.FUTURE)
 		{
 			filterQuery = "where DATE_FORMAT(c.startDate,'%Y-%m-%d') > " + startDateFormatted + ")";
 		}
 		else if (enumFilter == ConferenceFilters.ConferenceTimeFilter.PAST)
 		{
 			filterQuery = "where DATE_FORMAT(c.endDate,'%Y-%m-%d') < " + startDateFormatted + ")";
 		}
 		else
 		{
 			//TODO: EXCEPTION
 		}
 
 		if (user.isAdmin())
 		{
 			query.append("select c.name from Conference c ");
 			query.append(filterQuery);
 			confList = (List<String>)session.createQuery(
 					query.toString())
 	                .list();
 		}
 		else
 		{
 			query.append("select c.name from Conference c ");
 			query.append(filterQuery);
 			if (enumFilter == ConferenceFilters.ConferenceTimeFilter.ALL)
 				query.append(" where exists (select 1 from ConferencesUsers cu where cu.user = :user and cu.conference = c)");
 			else
 				query.append(" and exists (select 1 from ConferencesUsers cu where cu.user = :user and cu.conference = c)");
 			
 			
 			confList = (List<String>)session.createQuery(
 					query.toString())
 	                .setEntity("user", user)
 	                .list();
 		}
 				
 		session.getTransaction().commit();
 		
 		return confList;	
 		
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<User> getUsersForRoleInConference(Conference conference, UserRole ur ){	
 		
 		List<User> result = null;
 		
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		try {
 			result = (List<User>)HibernateUtil.getSessionFactory().getCurrentSession().createQuery(
					"select cu from  ConferencesUsers cu where cu.conference = :conf and cu.userRole = :userRole")
 	                .setEntity("conf", conference)
 	                .setInteger("userRole", ur.getValue())
 	                .list();
 			session.getTransaction().commit();
 		}
 		catch (Exception e) {
 			session.getTransaction().rollback();
 		}
 		
 		return result;
 	}
 }
