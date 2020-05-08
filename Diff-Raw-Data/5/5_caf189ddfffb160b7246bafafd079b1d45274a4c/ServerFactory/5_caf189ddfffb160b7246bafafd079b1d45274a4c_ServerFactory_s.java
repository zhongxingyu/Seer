 package server;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Properties;
 import java.util.Set;
 import java.sql.PreparedStatement;
 
 import db.Action;
 import db.Appointment;
 import db.MeetingPoint;
 import db.User;
 import db.Status;
 
 
 public class ServerFactory {
 	// Database connection
 	DBConnection db;
 	Properties p;
 	
 	public ServerFactory() {
 		p = new Properties();
 		try {
 			p.load(new FileInputStream(new File("resources/database.properties")));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		try {
 			db = new DBConnection(p);
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}	
 	}
 	public static void main(String args[]) throws ClassNotFoundException, SQLException {
 		ServerFactory sf = new ServerFactory();
 		System.out.println("created serverFactory");
 		//System.out.println(sf.getAllUsers());
 		//User u = new User(1, "espen", "master@commander.net", "hunter2");
 		Appointment a = new Appointment(47, 1, "title", new GregorianCalendar(), new GregorianCalendar(), "update test", true);
 		//a.setMeetingPoint(new MeetingPoint(1, "papi", 1337));
 		//ArrayList<Appointment> b = sf.getAllAppointments(u);
 		//System.out.println(b);
 		
 		//Appointment a = new Appointment(0, 1, "title", new GregorianCalendar(), new GregorianCalendar(), "first test meeting", false);
 		//a.setMeetingPoint(new MeetingPoint(1, "mordi", 200));
 		//Appointment b = sf.insertAppointment(a);
 		//System.out.println(b);
 		HashMap<User, Status> u = sf.getParticipants(a);
 		//System.out.println(u);
 	}
 	
 	public User login(User u) {
 		PreparedStatement prest;
 		User result = null;
 		try {
 			System.out.println("preparing to check user");
 			//System.out.println(p);
 			Boolean shouldClose = db.initialize();
 			
 			prest = db.preparedStatement("SELECT * FROM sids.user WHERE sids.user.name = ? AND sids.user.hashedPassword = ? LIMIT 1;");
 			prest.setString(1, u.getName());
 			prest.setString(2, u.getPassword());
 			
 			ResultSet rs = prest.executeQuery();
 			
 			while (rs.next()) {
 				result = new User(rs.getInt("id"), rs.getString("name"), rs.getString("email"), rs.getString("hashedPassword"));
 			}
 			if (result != null) System.out.println("found a user with name: " + result.getName());
 			if (shouldClose) db.close();
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.out.println("something fucked up");
 		}
 		return result;
 	}
 	
 	public User createUser(String name, String password, String email) throws ClassNotFoundException, SQLException {
 
 		PreparedStatement prest;
 		ResultSet generatedKeys;
 		int userId = 0;
 		
 		Boolean shouldClose = db.initialize();
 		
 		prest = db.preparedStatement("INSERT INTO sids.user (name, hashedPassword, email) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
 		prest.setString(1, name);
 		prest.setString(2, password);
 		prest.setString(3, email);
 		prest.executeUpdate();
 		
 		generatedKeys = prest.getGeneratedKeys();
 		if (generatedKeys.next()) {
 			userId = generatedKeys.getInt(1);
 		}
 		
 		if (shouldClose) db.close();
 		
 		System.out.println("finished!");
 		
 		if (userId != 0) {
 			return new User((int)userId, name, password, email);
 		} else {
 			return null;
 		}
 		
 	}
 	
 	public ArrayList<User> getAllUsers() {
 		PreparedStatement prest;
 		ResultSet users;
 		ArrayList<User> results = new ArrayList<User>();
 		User temp;
 		try {
 			System.out.println("preparing to get users");
 			Boolean shouldClose = db.initialize();
 			prest = db.preparedStatement("Select * FROM sids.user");
 			users = prest.executeQuery();
 			
 			while (users.next()) {
 				temp = new User(users.getInt("id"), users.getString("name"), users.getString("email"), users.getString("hashedPassword"));
 				results.add(temp);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.out.println("fucked up while getting users");
 		}
 		return results;
 	}
 	
 	public HashMap<User, ArrayList<Appointment>> getAllUsersAllAppointments() {
 		ArrayList<User> allUsers = getAllUsers();
 		HashMap<User, ArrayList<Appointment>> allUserAllAppointment = new HashMap<User, ArrayList<Appointment>>();
 		for (int i = 0; i < allUsers.size(); i++) {
 			allUserAllAppointment.put(allUsers.get(i), getAllAppointments(allUsers.get(i)));
 		}
 		return allUserAllAppointment;
 	}
 
 	public ArrayList<Appointment> getAllAppointments(User u) {
 		PreparedStatement prest;
 		ResultSet apps;
 		ArrayList<Appointment> results = new ArrayList<Appointment>();
 		try {
 			System.out.println("preparing to check user");
 			//send query to db
 			Boolean shouldClose = db.initialize();
 			//prest = db.preparedStatement("SELECT * FROM sids.appointment, sids.user_appointment,sids.user WHERE sids.user.id =?;");
 			prest = db.preparedStatement(
 					"SELECT a.id, a.creatorUserId, a.start, a.end, a.description, a.isMeeting " +
 					"FROM sids.user AS u, sids.user_appointment AS ua, sids.appointment AS a " +
 					"WHERE u.id = ua.userId " +
 					"AND ua.appointmentId = a.id " +
 					"AND u.id = ?"
 					);
 			prest.setInt(1, u.getId());
 			//System.out.println(prest);
 			//returns query
 			apps = prest.executeQuery();
 			GregorianCalendar start;
 			GregorianCalendar end;
 			//makes query to a appointment object
 			while (apps.next()) {
 				start = new GregorianCalendar();
 				start.setTime(apps.getTimestamp("start"));
 				end = new GregorianCalendar();
 				end.setTime(apps.getTimestamp("end"));
 				Appointment temp = new Appointment(apps.getInt("id"), apps.getInt("creatorUserId"), apps.getString("description"), start, end, apps.getString("description"), apps.getBoolean("isMeeting"));
 				if(temp.isMeeting()){
 					temp.setParticipants(new ArrayList<User>(getParticipants(temp).keySet()));
 					temp.setMeetingPoint(getMeetingPoint(temp));
 				}
 				results.add(temp);
 			}
 			if (shouldClose) db.close();
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.out.println("something fucked up while getting appointments");
 		}
 		return results;
 	}
 	
 	private MeetingPoint getMeetingPoint(Appointment app) throws ClassNotFoundException, SQLException {
 		PreparedStatement prest;
 		ResultSet mPoint;
 		MeetingPoint results=null;
 		try {
 			System.out.println("preparing to check appointment for the meeting place");
 			//send query to db
 			Boolean shouldClose = db.initialize();
 			prest = db.preparedStatement("SELECT meetingpoint.id,name,capacity FROM " +
 					"((sids.appointment JOIN sids.appointment_meetingpoint ON appointment.id=?)JOIN sids.meetingpoint ON meetingpoint.id=meetingpointId);");
 			prest.setInt(1, app.getId());
 			//System.out.println(prest);
 			//returns query
 			mPoint = prest.executeQuery();
 			//makes query to a MeetingPoint object
 			while (mPoint.next()) {
 				results = new MeetingPoint(mPoint.getInt("id"), mPoint.getString("name"), mPoint.getInt("capacity"));
 			}
 			if (shouldClose) db.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.out.println("something fucked up while getting meeting place");
 		}
 		System.out.println(results);
 		return results;
 	}
 	
 	public HashMap<User, Status> getParticipants(Appointment app) throws ClassNotFoundException, SQLException{
 		PreparedStatement prest;
 		ResultSet ppants;
 		HashMap<User, Status> results = new HashMap<User, Status>();
 		try {
 			System.out.println("preparing to check appointment for participants");
 			//send query to db
 			Boolean shouldClose = db.initialize();
 			prest = db.preparedStatement("SELECT user.id, name, email, isGoing, hashedPassword " +
 					"FROM ((sids.appointment JOIN sids.user_appointment ON appointment.id=appointmentId)JOIN sids.user ON user.id=userId) WHERE appointment.id=?;");
 			prest.setInt(1, app.getId());
 			//System.out.println(prest);
 			//returns query
 			ppants = prest.executeQuery();
 			//makes query to a list of User
 			User utemp;
 			Status stemp;
 			Boolean btemp;
 			while (ppants.next()) {
 				utemp = new User(ppants.getInt("id"), ppants.getString("name"), ppants.getString("email"), ppants.getString("hashedPassword"));
 				btemp = ppants.getBoolean("isGoing");
 				if (btemp == true) {
 					stemp = Status.ATTENDING;
 				} else if (btemp == false) {
 					stemp = Status.NOT_ATTENDING;
 				} else {
 					stemp = Status.UNANSWERED;
 				}
 				results.put(utemp, stemp);
 			}			
 			if (shouldClose) db.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.out.println("something fucked up while getting participants");
 		}
 		System.out.println(results);
 		return results;		
 	}
 	
 	public void setStatus(Appointment appointment, User user, Action action) {
 		PreparedStatement prest;
 		try {
 			Boolean shouldClose = db.initialize();
 			prest = db.preparedStatement("UPDATE sids.user_appointment SET isGoing = ? WHERE appointmentId = ? AND userId = ?");
 			System.out.println("updating u_a to " + action);
 			prest.setBoolean(1, (action.equals(Action.SET_STATUS_ATTENDING)? true : false ));
 			prest.setInt(2, appointment.getId());
 			prest.setInt(3, user.getId());
 			System.out.println("executing u_a update");
 			prest.executeUpdate();
 			
 			if (shouldClose) db.close();
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public Appointment insertAppointment(Appointment appointment) {
 		PreparedStatement prest;
 		ResultSet generatedKeys;
 		int appointmentId = 0;
 		try {
 			Boolean shouldClose = db.initialize();
 			// create and insert new appointment into database
 			prest = db.preparedStatement("INSERT INTO sids.appointment (creatorUserId, title, start, end, description, isMeeting)"+
 			" VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
 			prest.setInt(1, appointment.getCreatorUserId());
 			prest.setString(2, appointment.getTitle());
 			prest.setTimestamp(3, new Timestamp(appointment.getStart().getTimeInMillis()));
 			prest.setTimestamp(4, new Timestamp(appointment.getEnd().getTimeInMillis()));
 			prest.setString(5, appointment.getDescription());
 			prest.setBoolean(6, appointment.isMeeting());
 			System.out.println("executing appointment insert");
 			prest.executeUpdate();
 			
 			generatedKeys = prest.getGeneratedKeys();
 			if (generatedKeys.next()) { // if successfully inserted
 				appointmentId = generatedKeys.getInt(1);
 				System.out.println("we inserted successfully and appointmentId is now " + appointmentId);
 				
 				prest = db.preparedStatement("INSERT INTO sids.user_appointment (userId, appointmentId) VALUES (?, ?)");
 				ArrayList<User> participants = appointment.getParticipants();
 				System.out.println("our participants are!");
 				
 				// create user-appointment connection for all users
 				for (int i = 0; i < participants.size(); i++) { 
 					System.out.println(" executing userAppointment nr. " + i + " " + participants.get(i).getName());
 					prest.setInt(1, participants.get(i).getId());
 					prest.setInt(2, appointmentId);
 					prest.executeUpdate();
 				}
 				System.out.println("finished adding users");
 				// TODO: implement
 				/*if (appointment.isMeeting()) { 
 					// create appointment - room connection
 					System.out.println("executing appointmentMeetingpoint");
 					prest = db.preparedStatement("INSERT INTO sids.appointment_meetingpoint (appointmentId, meetingpointId) VALUES(?, ?)");
 					prest.setInt(1, appointmentId);
 					prest.setInt(2, appointment.getMeetingPoint().getId());
 					System.out.println("executing appointmentMeetingPoint");
 					prest.executeUpdate();
 					
 				}*/
 			}
 			
 			System.out.println("finished inserting");
 			
 			if (shouldClose) db.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 
 		if (appointmentId != 0) {
 			appointment.setId(appointmentId);
 			return appointment;
 		} else {
 			return null;
 		}
 	}
 	
 	public boolean deleteAppointment(Appointment appointment) {
 		
 		PreparedStatement prest;
 	
 		try {		
 			System.out.println("preparing to check delete appointment from user_appointment");
 			//send query to db
 			Boolean shouldClose = db.initialize();
 			prest = db.preparedStatement("DELETE FROM sids.user_appointment WHERE ? = appointmentId;");
 			prest.setInt(1, appointment.getId());
 			prest.executeUpdate();
 			
 			System.out.println("preparing to check delete appointment from appointment_meetingpoint");
 			//send query to db
 			prest = db.preparedStatement("DELETE FROM sids.appointment_meetingpoint WHERE ? = appointmentId;");
 			prest.setInt(1, appointment.getId());
 			prest.executeUpdate();
 			
 			System.out.println("preparing to check delete appointment");
 			//send query to db
 			prest = db.preparedStatement("DELETE FROM sids.appointment WHERE id = ?;");
 			prest.setInt(1, appointment.getId());
 			//returns query
 			prest.executeUpdate();
 			
 			if (shouldClose) db.close();	
 		}
 		
 		catch (Exception e) {
 			e.printStackTrace();
 			System.out.println("something fucked up while deleting appointment");
 			return false;
 		}
 		return true;
 		
 	}
 	
 	private boolean editUser(User am) {
 		PreparedStatement prest;
 		
 		return false;
 	}
 	public Appointment updateAppointment(Appointment appointment) {
 		PreparedStatement prest;
 		ResultSet generatedKeys;
 		
 		try {
 			Boolean shouldClose = db.initialize();
 			if (appointment.isMeeting()) { 
 				//retreives users from new and old version and put them into a set so they are unique
 				ArrayList<User> clientParticipants = appointment.getParticipants();
 				ArrayList<User> serverParticipants = new ArrayList<User>(getParticipants(appointment).keySet());
 				System.out.println("users in server version" +
 						serverParticipants
 						+ "users in client version"
 						+ clientParticipants);
 				HashSet<User> combinedUsers = new HashSet<User>(clientParticipants); combinedUsers.addAll(serverParticipants);
 				System.out.println("all users:" + combinedUsers);
 				for (User user : combinedUsers) {
 					if(clientParticipants.contains(user)&& serverParticipants.contains(user)) {	//both have the user
 						System.out.println("both contain " + user);
 					}
 					else if(clientParticipants.contains(user) && !serverParticipants.contains(user)){//master has user but not old
 						//insert user into participants
 						System.out.println("Inserting participant" + user);
 						prest = db.preparedStatement("INSERT INTO sids.user_appointment (userId, appointmentId) VALUES (?, ?)");
 						prest.setInt(1, user.getId());
 						prest.setInt(2, appointment.getId());
 						//System.out.println(prest);
 						prest.executeUpdate();
 					}
 					else if (!clientParticipants.contains(user) && serverParticipants.contains(user)){															//old has user, but not master
 						//delete user from participants
 						System.out.println("deleting participant" + user);
 						prest = db.preparedStatement("DELETE FROM sids.user_appointment WHERE userId=? AND appointmentId=?");
 						prest.setInt(1, user.getId());
 						prest.setInt(2, appointment.getId());
 						//System.out.println(prest);
 						prest.executeUpdate();
 					}
 				}
 				
 				// update room connection
 				/*System.out.println("updating appointmentMeetingpoint");
 				prest = db.preparedStatement("UPDATE sids.appointment_meetingpoint SET meetingpointId=? WHERE appointmentId=? ");
 				prest.setInt(2, appointment.getMeetingPoint().getId());
 				prest.setInt(1, appointment.getId());
 				System.out.println("updating appointmentMeetingPoint");
 				//System.out.println(prest);
 				prest.executeUpdate();*/
 			}
 			// update appointment in database
 			prest = db.preparedStatement("UPDATE sids.appointment SET title=?, start=?, end=?, description=?, isMeeting=?"+
 					" WHERE id=?");
 			prest.setString(1, appointment.getTitle());
 			prest.setTimestamp(2, new Timestamp(appointment.getStart().getTimeInMillis()));
 			prest.setTimestamp(3, new Timestamp(appointment.getEnd().getTimeInMillis()));
 			prest.setString(4, appointment.getDescription());
 			prest.setBoolean(5, appointment.isMeeting());
 			prest.setInt(6, appointment.getId());
 			System.out.println("executing appointment update");
 			//System.out.println(prest);
 			prest.executeUpdate();
 			
 				
 			System.out.println("finished updating");
 			if (shouldClose) db.close();
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			System.out.println("problem getting participants");
 			e.printStackTrace();
 		}
 
 		return appointment;
 	}
 	
 	public User logOut(User user) {
 		return user;
 		
 	}
 	
 	public Set<MeetingPoint> getAvailableMeetingpoints(Appointment appointment){
 		int minCap=appointment.getParticipants().size();
 		PreparedStatement prest;
 		ResultSet mpResult;
 		Set<MeetingPoint> mPoints = new HashSet<MeetingPoint>();
 		Set<MeetingPoint> callback = new HashSet<MeetingPoint>();		
 		
 		//get fitting meetingpoints
 		try {
 			System.out.println("preparing to get meetingpoints");
 			Boolean shouldClose = db.initialize();
			prest = db.preparedStatement("SELECT * FROM meetingpoint WHERE capacity>=?;");
 			prest.setInt(1, minCap);
 			//puts fitting rooms in a set
 			mpResult = prest.executeQuery();
 			while(mpResult.next())
 				mPoints.add(new MeetingPoint(mpResult.getInt("id"), mpResult.getString("name"), mpResult.getInt("capacity")));
 			//runs thru each meetingpoint and compares start and end dates
 			Date start = appointment.getStartAsDate();
 			Date end = appointment.getEndAsDate();
 			Date tempStart=null;
 			Date tempEnd=null;
 			ResultSet timeRes;
 			boolean isAvailable;
 			for (MeetingPoint meetingPoint : mPoints) {
 				isAvailable = true;
				prest = db.preparedStatement("SELECT start,end FROM appointment_meetingpoint JOIN appointment ON appointmentId=id WHERE meetingpointId=?;");
 				prest.setInt(1, meetingPoint.getId());
 				timeRes=prest.executeQuery();
 				//checks alle appointments connected to this MeetingPoint for collisions with the appointment
 				while(timeRes.next() && isAvailable ){
 					tempStart = timeRes.getTimestamp("start");
 					tempEnd = timeRes.getTimestamp("end");
 					if(tempStart.after(end) || tempEnd.before(start))
 						continue;
 					else
 						isAvailable=false;
 				}
 				//if this meetingpoint is availabel it get added to the set that will be returned
 				if(isAvailable)
 					callback.add(meetingPoint);
 			}
 			
 			
 			if (shouldClose) db.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.out.println("something fucked up while getting available meetingpoints");
 		}
 		
 		return callback;
 		
 	}
 
 	
 	//Need method for something with the notifications. Not sure what, though.
 	
 }
