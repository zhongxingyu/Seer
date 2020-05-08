 /**
  * Tri-Replicator Application
  * 
  * To learn more about the app, visit this blog:
  * http://kharkovski.blogspot.com/2013/01/tri-replicator-free-app-on-google-app.html
  * 
  *  @author Roman Kharkovski, http://kharkovski.blogspot.com
  *  Created: December 19, 2012
  */
 
 package com.trireplicator.server;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.trireplicator.db.DatabaseAccess;
 import com.trireplicator.db.User;
 import com.trireplicator.db.Workout;
 import com.trireplicator.shared.SynchronizerService;
 import com.trireplicator.shared.TrainingLogException;
 import com.trireplicator.shared.Utils;
 import com.trireplicator.shared.WorkoutSession;
 import com.trireplicator.shared.WorkoutSession.WorkoutType;
 
 /**
  * Admin servlet is not accessible to anyone, but administrator of the system
  * Provides browser based UI to do admin tasks (delete users, etc.) 
  * 
  * @author Roman Kharkovski, http://kharkovski.blogspot.com
  */
 @SuppressWarnings("serial")
 public class AdminServlet extends HttpServlet {
 	private static final Logger log = Logger.getLogger(AdminServlet.class.getName());
 
 	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		resp.setContentType("text/plain");
 		String action = null;
 		String defaultAction = "view_stats";
 		if (req != null) {
 			action = (String) req.getParameter("action");
 		}
 		if (action == null)
 			action = defaultAction;
 		;
 		log.info("ACTION=" + action);
 		printVersion(resp);
 
 		// Since GAE wants me to run this on JDK 1.6, I can not use normal switch statement here .....
 		if (action.equals("delete_all")) {
 			deleteAllData(resp);
 		} else if (action.equals("end2end")) {
 			end2end(resp);
 		} else if (action.equals("basicTest")) {
 			doBasicTest(resp);
 		} else if (action.equals("view_stats")) {
 			viewStats(resp);
 		} else if (action.equals("delete_users")) {
 			deleteAllData(resp);
 		} else if (action.equals("delete_workouts")) {
 			deleteAllWorkouts(resp);
 		} else if (action.equals("delete_admin_events")) {
 			deleteAllAdminEvents(resp);
 		} else if (action.equals("delete_one_user")) {
 			// TODO - need to do something
 		} else {
 			log.info("There was nothing for this servlet to do?????????????");
 		}
 	}
 
 	private void viewStats(HttpServletResponse resp) {
 		DatabaseAccess database = new DatabaseAccess();
 
 		try {
 			resp.getWriter().println("Current date: " + new Date().toString());
 			resp.getWriter().println("Total number of registered users: " + database.listUsers().size());
 			// resp.getWriter().println("Total number of replicated workouts: " + database.listWorkouts().size());
 			// Iterator<AdminEvents> iterator = database.findLast50AdminEvents();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void end2end(HttpServletResponse resp) {
 		SynchronizerService server = new SynchronizerServiceImpl();
 
 		@SuppressWarnings("unused")
 		Date startDate = new Date();
 		@SuppressWarnings("unused")
 		Date endDate = new Date();
 
 		 String nameUSAT = "USAT-Peter225555555";
 		 String passwordUSAT = "USAT-password";
 		 String nameTP = "TP-Peter336666666";
 		 String passwordTP = "TP-password";
 
//		String nameUSAT = "romankharkovski@yahoo.com";
//		String passwordUSAT = "Romeo519";
//		String nameTP = "romankhar";
//		String passwordTP = "everflow";

 		// Add new active user
 		try {
 			// Test for existing user in the local database
 			boolean loginResult = server.checkExistingUser(nameTP, passwordTP, nameUSAT, passwordUSAT);
 			log.info("--- login result = " + loginResult);
 
 			// Now add new user and check if he can remotely login into USAT and tp sites
 			if (server.addUser(nameTP, passwordTP, nameUSAT, passwordUSAT)) {
 				log.info("--- new user has been added");
 			} else {
 				log.info("--- new user has not been added");
 			}
 		} catch (TrainingLogException e) {
 			e.printStackTrace();
 			log.info("Error adding user");
 		}
 
 		// Delete existing user
 		// if (server.removeUser(nameTP, passwordTP, nameUSAT, passwordUSAT) > 0 ) {
 		// log.info("--- user(s) has been deleted");
 		// } else {
 		// log.info("--- user has not been deleted");
 		// }
 
 		// Try replication for one user
 		// try {
 		// server.replicateWorkoutsForUser(new Long(0), nameTP, passwordTP, nameUSAT, passwordUSAT, startDate, endDate);
 		// log.info("Finished replicating workouts for one user");
 		// } catch (TrainingLogException e) {
 		// e.printStackTrace();
 		// log.info("Error replicating workouts for one user");
 		// }
 
 		// Try replicating workouts for all users
 		// try {
 		// server.replicateWorkoutsForAllUsers();
 		// log.info("Finished replicating workouts for all users");
 		// } catch (TrainingLogException e) {
 		// log.info("Error replicating workouts for all users");
 		// }
 
 	}
 
 	private void doBasicTest(HttpServletResponse resp) throws IOException {
 		log.info("================================ basic TEST");
 		Workout workout;
 		User user;
 
 		user = new User();
 		String name = "Peter" + Math.random();
 		user.setNameUSAT("USAT-" + name);
 		user.setPlainPasswordUSAT("USAT-password");
 		user.setNameTP("TP-" + name);
 		user.setPlainPasswordTP("TP-password" + System.currentTimeMillis());
 		user.setRegistrationDate(new Date());
 		user.setLastVisitDate(new Date());
 		user.setActive(true);
 
 		DatabaseAccess database = new DatabaseAccess();
 		List<User> allUsers = null;
 		try {
 			database.addUser(user);
 			allUsers = database.listUsers();
 		} finally {
 		}
 
 		resp.setContentType("text/plain");
 		if (allUsers != null) {
 			resp.getWriter().println("Hello, JPA. We have " + allUsers.size() + " number of entries.");
 			Iterator<User> iterator = allUsers.iterator();
 			while (iterator.hasNext()) {
 				User userTT = iterator.next();
 				resp.getWriter().println(userTT.toString());
 
 				workout = new Workout(new WorkoutSession(WorkoutType.Swim, "My swim for  user " + userTT.getUserId(), new Date(), 1000),
 						userTT.getUserId());
 				database.addWorkout(workout);
 
 				workout = new Workout(new WorkoutSession(WorkoutType.Bike, "My bike for user " + userTT.getUserId(), new Date(), 10000),
 						userTT.getUserId());
 				database.addWorkout(workout);
 
 				// Now print all workouts for this user
 				List<Workout> workoutsList = database.findWorkoutsForUser(userTT.getUserId());
 				Iterator<Workout> workoutIterator = workoutsList.iterator();
 				while (workoutIterator.hasNext()) {
 					Workout userWorkout = (Workout) workoutIterator.next();
 					resp.getWriter().println(userWorkout.toString());
 				}
 			}
 		} else {
 			resp.getWriter().println("Should not happen");
 		}
 	}
 
 	private void deleteAllData(HttpServletResponse resp) throws IOException {
 		deleteAllUsers(resp);
 		deleteAllWorkouts(resp);
 		deleteAllAdminEvents(resp);
 	}
 
 	private void deleteAllUsers(HttpServletResponse resp) throws IOException {
 		DatabaseAccess database = new DatabaseAccess();
 		resp.getWriter().println("Deleted all users: " + database.deleteAllUsers());
 	}
 
 	private void deleteAllWorkouts(HttpServletResponse resp) throws IOException {
 		DatabaseAccess database = new DatabaseAccess();
 		resp.getWriter().println("Deleted all user workouts: " + database.deleteAllWorkouts());
 	}
 
 	private void deleteAllAdminEvents(HttpServletResponse resp) throws IOException {
 		DatabaseAccess database = new DatabaseAccess();
 		resp.getWriter().println("Deleted all admin events: " + database.deleteAllAdminEvents());
 	}
 	
 	private void printVersion(HttpServletResponse resp) {
 		try {
 			resp.getWriter().println("<p>Application Version: "+Utils.VERSION+"</p>");
 		} catch (IOException e) {
 		}
 	}
 }
