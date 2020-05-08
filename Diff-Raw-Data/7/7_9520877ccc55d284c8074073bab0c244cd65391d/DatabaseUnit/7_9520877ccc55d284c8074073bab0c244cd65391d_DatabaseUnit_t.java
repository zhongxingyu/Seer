 package synclogic;
 
 import java.net.ConnectException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Properties;
 import java.util.List;
 import model.Appointment;
 import model.Invitation;
 import model.InvitationStatus;
 import model.Meeting;
 import model.Notification;
 import model.NotificationType;
 import model.Room;
 import model.SaveableClass;
 import model.User;
 
 public class DatabaseUnit {
 
 	private  Connection conn = null;
 	private ArrayList<User> userArray;
 	private ArrayList<Appointment> eventArray;
 	private int eventCount = 0;
 	private int invitationCount = 0;
 	private int notificationCount = 0;
 	private int roomCount = 0;
 	
 	public DatabaseUnit() throws ConnectException {
 		try {
 			conn = null;
 			dbConnect();
 		} catch (Exception e) {
 			throw new ConnectException();
 		} 
 	}
 	
 	public void dbConnect() throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException{
 		//method to connect to the database with username and password
 		Properties props = new Properties();
 		props.put("user","hannekot_26");
 		props.put("password","gruppe26");
 			
 		Class.forName("com.mysql.jdbc.Driver").newInstance();
 		conn = DriverManager.getConnection("jdbc:mysql://mysql.stud.ntnu.no/hannekot_X-cal", props);
 	}
 	
 	public void objectsToDb(List<SyncListener> objects) throws SQLException{		
 		eventCount = 0;
 		invitationCount = 0;
 		notificationCount = 0;
 		roomCount = 0;
 		//iterates the list of objects that are to be put i the database, checks what type of SyncListener it is
 		for (int i = 0; i < objects.size(); i++) {
 			if(objects.get(i) instanceof User){
 				User user = (User) objects.get(i);
 				Statement stmt = conn.createStatement();
 				// Execute a query to find out if the user is already in the database
 				ResultSet rs = stmt.executeQuery("SELECT Username FROM User WHERE Username ='" + user.getUsername() +"';");
 				String dateOfBirth = (User.getDateFormat().format(user.getDateOfBirth()));
 				if (rs.next()) {	
 					//the user is in the database, update with new user information
 					Statement update = conn.createStatement();
 					int del = (user.isDeleted()) ? 1 : 0;							
 					update.executeUpdate("UPDATE User SET Password='" +user.getPassword()+"' , Email='" +user.getEmail() + "' , DateOfBirth='" + dateOfBirth +"', Phone='" + user.getPhone() +"' , Surname= '" + user.getSurname() + "' , Firstname='" + user.getFirstname() +"', Deleted='"+del +"' WHERE Username='" + user.getUsername() +"';");
 					//Update table UserNotification with the notification in the Notification List
 					for (int j = 0; j < user.getNotifications().size(); j++) {
 						Statement stmt1 = conn.createStatement();
 						//Execute a query to find out if the Notification is already in the database
 						ResultSet rs1 = stmt1.executeQuery("SELECT NotificationID FROM UserNotification WHERE Username='"+ user.getUsername()+"'");
 						boolean rs1empty = true;
 						while(rs1.next()){
 							//The selected notification is in the database, update the database
 							int notID = rs1.getInt("NotificationID");
 							int read = (user.getNotifications().get(j).isRead()) ? 1 : 0;	
 							Statement stmt2 = conn.createStatement();
 							stmt2.executeUpdate("UPDATE UserNotification SET isRead ='" + read +"' WHERE  Username='"+ user.getUsername() + "' AND NotificationID='" + notID + "';");
 							rs1empty = false;
 						}
 						if(rs1empty){
 							//The selected notification is NOT in the database, insert the connection between user and notification into the database
 							int read = (user.getNotifications().get(j).isRead()) ? 1 : 0;	
 							Statement stmt3 = conn.createStatement();
 							stmt3.executeUpdate("INSERT INTO UserNotification VALUES('" + user.getUsername() + "','" + user.getNotifications().get(j).getId() + "','" + read + "');" );
 						}
 					}
 					//Updates SubscribesTo table by first deleting the objects in the database with the users username then inserting all connections between the subscriber and the authors into UserSubscription
 					for (int j = 0; j < user.getSubscribesTo().size(); j++) {
 						Statement delete = conn.createStatement();
 						delete.executeUpdate("DELETE FROM UserSubscription WHERE SubscriberUsername ='" +user.getUsername() + "'");
 						Statement insertInto1 = conn.createStatement();
 						insertInto1.executeUpdate("INSERT INTO UserSubscription Values('" + user.getUsername() +"','" 
 								+ user.getSubscribesTo().get(j).getUsername() + "');");	
 					}
 				}
 				else{
 					//The user is not in the database, inserts the user information into the database
 					Statement insertInto = conn.createStatement();
 					insertInto.executeUpdate("INSERT INTO User VALUES('" + user.getUsername()+"','"
 							+ user.getPassword() +"','" + user.getEmail() + "','" + dateOfBirth + "','"
 							+ user.getPhone() + "','" + user.getSurname() + "','" + user.getFirstname() + "','0');");
 					//insert the connection between users and notification into UserNotification
 					for (int j = 0; j < user.getNotifications().size(); j++) { 
 						Statement insertInto1 = conn.createStatement();
 						insertInto1.execute("INSERT INTO UserNotification Values('" + user.getUsername() +"','" 
 								+ user.getNotifications().get(j).getId() + "','" + user.getNotifications().get(j).isRead() +"');");
 					}
 					//insert the connection between subscriber and authors into UserSubscription
 					for (int j = 0; j < user.getSubscribesTo().size(); j++) {
 						Statement insertInto1 = conn.createStatement();
 						insertInto1.executeUpdate("INSERT INTO UserSubscription Values('" + user.getUsername() +"','" 
 								+ user.getSubscribesTo().get(j).getUsername() + "');");	
 					}
 				}
 			}
 			else if(objects.get(i) instanceof Meeting){
 				Meeting meet = (Meeting) objects.get(i);
 				Statement stmt = conn.createStatement();
 				//Execute a query to find out if the meeting is already in the database
 				ResultSet rs = stmt.executeQuery("SELECT * FROM Event WHERE EventID='" + meet.getId() + "';");
 				// getting the rigth usable format of the time of the meeting
 				String date = (Meeting.getDateFormat().format(meet.getDate()));
 				String start = (Meeting.getTimeformat().format(meet.getStartTime()));
 				String end = (Meeting.getTimeformat().format(meet.getEndTime()));
 				if(rs.next()){
 					//the meeting is in the database, update the information in the database
 					Statement update = conn.createStatement();
 					int del = (meet.isDeleted()) ? 1 : 0;	
 					//if the meeting is not connected to a room then there is not need to update RoomEvent
 					if(meet.getRoom() != null){
						update.executeUpdate("UPDATE RoomEvent SET RoomID='" + meet.getRoom().getId() + "' WHERE EventID='" + meet.getId() + "';");
 					}
 					update.executeUpdate("UPDATE Event SET Date='" + date + "', Start='" + start + "', End='" + end +"', Description='" + meet.getDescription() +"', Location='" + meet.getLocation() + "', Type='1', Deleted='" + del + "' WHERE EventID='" + meet.getId() +"';");	
 					for (int j = 0; j < meet.getInvitations().size(); j++) {
 						Statement stm = conn.createStatement();
 						ResultSet set = stm.executeQuery("SELECT * FROM InvitationTo WHERE EventID='" + meet.getId()+ "'");
 						if(!set.next()){
 							update.executeUpdate("INSERT INTO InvitationTo VALUES('" + meet.getInvitations().get(j) + "','" + meet.getId() + "');");
 						}
 					}
 				}
 				else{
 					//the meeting is not in the database, insert the information about the meeting into Event, RoomEvent, UserEvent and InvitationTo 
 					Statement update = conn.createStatement();
 					int del = (meet.isDeleted()) ? 1 : 0;	
 					//if the meeting is not connected to a room then there is not need to update RoomEvent
 					Statement rsstmt = conn.createStatement();
 					ResultSet room = rsstmt.executeQuery("SELECT * FROM RoomEvent WHERE EventID='" + meet.getId() + "' AND RoomID='" + meet.getRoom() + "'");
 					if(room.first()){
 						update.executeUpdate("INSERT INTO RoomEvent VALUES('" + meet.getId() + "','" + meet.getRoom().getId() + "');");
 					}
 					update.executeUpdate("INSERT INTO Event VALUES('" + meet.getId() + "','" + date + "','" + start +"','" + end + "','" + meet.getDescription() + "','" + meet.getLocation() + "','1','" + del + "');");
 					update.execute("INSERT INTO UserEvent VALUES('" + meet.getOwner().getUsername() + "','" + meet.getId() + "');" );
 					for (int j = 0; j < meet.getInvitations().size(); j++) {
 						update.executeUpdate("INSERT INTO InvitationTo VALUES('" + meet.getInvitations().get(j) + "','" + meet.getId() + "');");
 					}
 				}
 			}
 			else if(objects.get(i) instanceof Appointment){
 				Appointment appment = (Appointment) objects.get(i);
 				Statement stmt = conn.createStatement();
 				//Execute a query to find out if the appointment is already in the database
 				ResultSet rs = stmt.executeQuery("SELECT * FROM Event WHERE EventID='" + appment.getId() + "';");
 				//getting the rigth usable format of the time of the appointment
 				String date = (Appointment.getDateFormat().format(appment.getDate()));
 				String start = (Appointment.getTimeformat().format(appment.getStartTime()));
 				String end = (Appointment.getTimeformat().format(appment.getEndTime()));
 				if(rs.next()){
 					//the appointment is in the database, update the information
 					Statement update = conn.createStatement();
 					int del = (appment.isDeleted()) ? 1 : 0;	
 					//if the appointment is not connected to a room then there is not need to update RoomEvent
 					if(appment.getRoom() != null){
 						//Updates the room connected to the appointment incase there is a room change
 						update.executeUpdate("UPDATE RoomEvent SET RoomID='" + appment.getRoom().getId() + "' WHERE EventID='" + appment.getId() + "';");
 					}
 					update.executeUpdate("UPDATE Event SET Date='" + date + "', Start='" + start + "', End='" + end +"', Description='" + appment.getDescription() +"', Location='" + appment.getLocation() + "', Type='0', Deleted='" + del + "' WHERE EventID='" + appment.getId() +"';");
 				}
 				else{
 					//the appointment is not in the database, insert the appointment in the database, insert the room connection and the UserEvent to connect the user to the appointment
 					Statement update = conn.createStatement();
 					int del = (appment.isDeleted()) ? 1 : 0;	
 					//if the appointment is not connected to a room then there is not need to update RoomEvent
 					if(appment.getRoom() != null){
						System.out.println("AppID: " + appment.getId());
						System.out.println("RoomID: " + appment.getRoom().getId());
						update.executeUpdate("INSERT INTO RoomEvent VALUES('" + appment.getId() + "','" + appment.getRoom().getId() + "');");
 					}
 					update.executeUpdate("INSERT INTO Event VALUES('" + appment.getId() + "','" + date + "','" + start +"','" + end + "','" + appment.getDescription() + "','" + appment.getLocation() + "','0','" + del + "');");
 					update.execute("INSERT INTO UserEvent VALUES('" + appment.getOwner().getUsername() + "','" + appment.getId() + "');" );
 				}
 			}
 			else if(objects.get(i) instanceof Notification){
 				Notification not = (Notification) objects.get(i);
 				Statement stmt = conn.createStatement();
 				//Execute a query to find out if the Notification already is in the database
 				ResultSet rs = stmt.executeQuery("SELECT * FROM Notification WHERE NotificationID ='" + not.getId() +"';");
 				if(rs.next()){
 					//the notification is already in the database, update the database with the new information
 					Statement update = conn.createStatement();
 					update.executeUpdate("UPDATE Notification SET type='" + not.getType().ordinal()+ "', TriggeredBy='" + not.getTriggeredBy().getUsername() + "' WHERE NotificationID='" + not.getId() +"';");		
 				}
 				else{
 					//the notification is not in the database, insert the notification information into the database
 					Statement insertInto = conn.createStatement();
 					insertInto.executeUpdate("INSERT INTO Notification VALUES('"+ not.getId() + "','" + not.getType().ordinal() + "','" + not.getTriggeredBy().getUsername() +"');");
 					insertInto.executeUpdate("INSERT INTO BelongsTo VALUES('" + not.getInvitation().getID() + "','" + not.getId() + "');");
 				}
 			}
 			else if(objects.get(i)instanceof Invitation){
 				Invitation inv = (Invitation) objects.get(i);
 				Statement stmt = conn.createStatement();
 				//Execute a query to find out if the Invitation already is in the database
 				ResultSet rs = stmt.executeQuery("SELECT * FROM Invitation WHERE InvitationID ='" + inv.getID() + "';");
 				if(rs.next()){
 					//the Invitation is already in the database, update the information
 					Statement update = conn.createStatement();
 					update.executeUpdate("UPDATE Invitation SET Status='" + inv.getStatus().ordinal() + "' WHERE InvitationID='" + inv.getID() + "';");
 				}
 				else{
 					System.out.println("HEI!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
 					//the Invitation is not in the database, insert the information into the database
 					Statement insertInto = conn.createStatement();
 					insertInto.executeUpdate("INSERT INTO Invitation VALUES('" + inv.getStatus().ordinal() + "','" + inv.getID() + "');");
 //					insertInto.executeUpdate("INSERT INTO InvitationTo VALUES('" + inv.getID() + "','" + inv.getMeeting().getId() + "');");
 				}
 			}	
 		}
 	}	
 	
 
 	public ArrayList<User> loadUser() throws SQLException{
 		//method to load user information from database
 		Statement stmt = conn.createStatement();
 		//Selects all information about the user in the database and make User objects out of the information
 		ResultSet rs = stmt.executeQuery("SELECT * FROM User");
 		ArrayList<User> userArray = new ArrayList<User>();
 		while(rs.next()){
 			String Username = rs.getString("Username");
 			String Password = rs.getString("Password");
 			String Email = rs.getString("Email");
 			Date date = rs.getDate("DateOfBirth");
 			int Phone = rs.getInt("Phone");
 			String Surname = rs.getString("Surname");
 			String Firstname = rs.getString("Firstname");
 			int deleted = rs.getInt("Deleted");
 			User user = new User(Firstname, Surname, Username,Password, Email, date, Phone);
 			if(deleted == 1){
 				user.setDeleted(true);
 			}
 			else{user.setDeleted(false);}
 			
 			userArray.add(user);
 		}
 		return userArray;
 	}
 	
 
 	public ArrayList<Room> loadRoom() throws SQLException{
 		//method to load room
 		Statement stmt = conn.createStatement();
 		//selects all information on Room in the database and make Room objects
 		ResultSet rs = stmt.executeQuery("SELECT * FROM Room");
 		ArrayList<Room> roomArray = new ArrayList<Room>();
 		while(rs.next()){
 			int RoomID = rs.getInt("RoomID");
 			String RoomName = rs.getString("Name");
 			int Capacity = rs.getInt("Capacity");
 			Room room = new Room(RoomID, RoomName, Capacity);
 			roomArray.add(room);
 		}	
 		return roomArray;
 	}
 
 	public ArrayList<Appointment> loadEvent() throws SQLException{
 		Statement stmt = conn.createStatement();
 		ResultSet rs = stmt.executeQuery("SELECT * FROM Event");
 		ArrayList<Appointment> eventArray = new ArrayList<Appointment>();
 		while(rs.next()){
 			int type = rs.getInt("Type");
 			int eventID = rs.getInt("EventID");
 			Date date = rs.getDate("Date");
 			Date start = rs.getTime("Start");
 			Date end = rs.getTime("End");
 			String description = rs.getString("Description");
 			String roomName = rs.getString("Location");
 			int deleted = rs.getInt("Deleted");
 			Statement sstm = conn.createStatement();
 			ResultSet rss1 = sstm.executeQuery("SELECT Username FROM UserEvent WHERE EventID ='" +eventID +"'" );
 			if(rss1.next()){
 				rss1.getString("Username");
 				String usser = rss1.getString("Username");
 				int UserIndex = 0;
 				for (int i = 0; i < userArray.size(); i++) {
 					if(userArray.get(i).getUsername().equalsIgnoreCase(usser)){
 						UserIndex = i;
 						i=userArray.size();
 					}
 				}
 				ArrayList<Room> room = loadRoom();
 				int RoomIndex = room.size() + 5;
 				for (int i = 0; i < room.size(); i++) {
 					if(room.get(i).getName().equalsIgnoreCase(roomName)){
 						RoomIndex = i;
 						i=room.size();
 					}
 				}
 			
 				if(type == 1 && deleted == 0 && RoomIndex<room.size()){
 					Meeting meeting = new Meeting(date, start, end, description, roomName, room.get(RoomIndex), (Integer.toString(eventID)), userArray.get(UserIndex), false );
 					eventArray.add(meeting);
 				}
 				else if(type == 1 && deleted == 0 && RoomIndex>room.size()){
 					Meeting meeting = new Meeting(date, start, end, description, roomName, null, (Integer.toString(eventID)), userArray.get(UserIndex), false );
 					eventArray.add(meeting);
 				}
 				else if(type == 1 && deleted == 1 && RoomIndex<room.size()){
 					Meeting meeting = new Meeting(date, start, end, description, roomName,room.get(RoomIndex), (Integer.toString(eventID)), userArray.get(UserIndex), true );
 					eventArray.add(meeting);
 				}
 				else if(type == 1 && deleted == 1 && RoomIndex>room.size()){
 					Meeting meeting = new Meeting(date, start, end, description, roomName, null, (Integer.toString(eventID)), userArray.get(UserIndex), true );
 					eventArray.add(meeting);
 				}
 				else if(type == 0 && deleted == 0 && RoomIndex<room.size()){
 					Appointment appment = new Appointment(date, start, end, description, roomName, room.get(RoomIndex), (Integer.toString(eventID)), userArray.get(UserIndex), false);
 					eventArray.add(appment);
 				}
 				else if(type == 0 && deleted == 0 && RoomIndex>room.size()){
 					Appointment appment = new Appointment(date, start, end, description, roomName, null, (Integer.toString(eventID)), userArray.get(UserIndex), false);
 					eventArray.add(appment);
 				}
 				else if(type == 0 && deleted == 1 && RoomIndex<room.size()){
 					Appointment appment = new Appointment(date, start, end, description, roomName, room.get(RoomIndex), (Integer.toString(eventID)), userArray.get(UserIndex), true);
 					eventArray.add(appment);
 				}
 				else if(type == 0 && deleted == 0 && RoomIndex>room.size()){
 					Appointment appment = new Appointment(date, start, end, description, roomName, null, (Integer.toString(eventID)), userArray.get(UserIndex), false);
 					eventArray.add(appment);
 				}
 			}
 		}	
 		return eventArray;
 	}
 	
 
 	public ArrayList<Invitation> loadInvitation() throws SQLException{
 		//method to load invitations
 		Statement stmt = conn.createStatement();
 		//selects all information from the invitation database and for each element make a Invitation object
 		ResultSet rs = stmt.executeQuery("SELECT * FROM Invitation");
 		ArrayList<Invitation> invitationArray = new ArrayList<Invitation>();
 		while(rs.next()){
 			int invitationID = rs.getInt("InvitationID");
 			int status = rs.getInt("Status");
 			Statement stm = conn.createStatement();
 			//Execute a select query to find out witch event the chosen invitation is connected to
 			ResultSet rset = stm.executeQuery("SELECT EventID FROM InvitationTo WHERE InvitationID='" + invitationID +"'");
 			rset.first();
 			int eventID = rset.getInt("EventID");
 			int index = 0;
 			//find the corresponding event to the eventID and makes a Invitation object
 			for (int i = 0; i < eventArray.size(); i++) {
 				Appointment obj = eventArray.get(i);
 				int getObjectID = Integer.parseInt(obj.getId());
 				if( getObjectID == eventID){
 					index = i;
 					i = eventArray.size();
 				}	
 				if(eventArray.get(index) instanceof Meeting){
 				Meeting obj1 = (Meeting) eventArray.get(index);
 				// switch for the different types of invitation status
 					switch (status) {
 					case 0:{	
 						Invitation invitation = new Invitation(InvitationStatus.NOT_ANSWERED, obj1 ,(Integer.toString(invitationID)));
 						invitationArray.add(invitation);
 						break;
 					}
 					case 1:{
 						Invitation invitation = new Invitation(InvitationStatus.ACCEPTED, obj1 ,(Integer.toString(invitationID)));
 						invitationArray.add(invitation);
 						break;
 					}
 					case 2:{
 						Invitation invitation = new Invitation(InvitationStatus.REJECTED, obj1,(Integer.toString(invitationID)));
 						invitationArray.add(invitation);
 						break;	
 					}
 					case 3:{
 						Invitation invitation = new Invitation(InvitationStatus.REVOKED, obj1,(Integer.toString(invitationID)));
 						invitationArray.add(invitation);
 						break;	
 					}
 					case 4:{
 						Invitation invitation = new Invitation(InvitationStatus.NOT_ANSWERED_TIME_CHANGED, obj1,(Integer.toString(invitationID)));
 						invitationArray.add(invitation);
 						break;	
 					}
 					default:
 						break;
 					}		
 				}
 			}
 		}
 		return invitationArray;
 	}
 
 	public ArrayList<Notification> loadNotifcation() throws SQLException{
 		//method for load notifications 
 		ArrayList<Notification> notificationArray = new ArrayList<Notification>();
 		java.sql.PreparedStatement pstmt;
 		//execute query to find out witch notification is connected to witch user
 		String sel = "SELECT Notification.NotificationID, type, TriggeredBy, Username " +
 		"FROM Notification JOIN UserNotification ON " +
 		"Notification.NotificationID = UserNotification.NotificationID " + 
 		"WHERE Username = ?";
 		pstmt = conn.prepareStatement(sel);
 		for (int i = 0; i < userArray.size(); i++) {
 			pstmt.setString(1, userArray.get(i).getUsername());
 			ResultSet rs = pstmt.executeQuery();
 			while(rs.next()){
 				int notificationID = rs.getInt("NotificationiD");
 				int type = rs.getInt("type");
 				String triggeredBy = rs.getString("TriggeredBy");
 				int userIndex = 0;
 				//find the TriggeredBy user
 				for (int j = 0; j < userArray.size(); j++) {
 					if(userArray.get(j).getUsername().equalsIgnoreCase(triggeredBy)){
 						userIndex = j;
 						j  = userArray.size();
 					}
 				}
 				//Connect Invitations and Notificaiton 
 				ArrayList<Invitation> invitationArray = loadInvitation();
 				int invitationIndex = 0;
 				for (int j = 0; j < invitationArray.size(); j++) {
 					Statement sstm = conn.createStatement();
 					ResultSet rs1 = sstm.executeQuery("SELECT InvitationID FROM BelongsTo WHERE NotificationID='" + notificationID + "'");
 					rs1.first();
 					int invitationID = rs1.getInt("InvitationID");
 					if(invitationArray.get(j).getID().equalsIgnoreCase((Integer.toString(invitationID)))){
 						invitationIndex = j;
 						j = invitationArray.size();
 					}
 				}
 				//Makes the notification objects with the information found over and with different notifiction types
 				switch (type) {
 				case 0:{
 					Notification not = new Notification(invitationArray.get(invitationIndex), NotificationType.MEETING_CANCELLED, (Integer.toString(notificationID)), userArray.get(userIndex));				
 					(userArray.get(i)).addNotification(not);
 					notificationArray.add(not);					
 					break;
 				}
 				case 1:{
 					Notification not = new Notification(invitationArray.get(invitationIndex), NotificationType.INVITATION_RECEIVED, (Integer.toString(notificationID)), userArray.get(userIndex));
 					(userArray.get(i)).addNotification(not);
 					notificationArray.add(not);
 					break;				
 				}
 				case 2:{
 					Notification not = new Notification(invitationArray.get(invitationIndex), NotificationType.INVITATION_ACCEPTED, (Integer.toString(notificationID)), userArray.get(userIndex));
 					(userArray.get(i)).addNotification(not);
 					notificationArray.add(not);
 					break;
 				}
 				case 3:{
 					Notification not = new Notification(invitationArray.get(invitationIndex), NotificationType.INVITATION_REJECTED, (Integer.toString(notificationID)), userArray.get(userIndex));
 					(userArray.get(i)).addNotification(not);
 					notificationArray.add(not);
 					break;				
 				}
 				case 4:{
 					Notification not = new Notification(invitationArray.get(invitationIndex), NotificationType.INVITATION_REVOKED, (Integer.toString(notificationID)), userArray.get(userIndex));
 					(userArray.get(i)).addNotification(not);
 					notificationArray.add(not);
 					break;				
 				}
 				case 5:{
 					Notification not = new Notification(invitationArray.get(invitationIndex), NotificationType.MEETING_TIME_CHANGED, (Integer.toString(notificationID)), userArray.get(userIndex));
 					(userArray.get(i)).addNotification(not);
 					notificationArray.add(not);
 					break;				
 				}
 				case 6:{
 					Notification not = new Notification(invitationArray.get(invitationIndex), NotificationType.MEETING_CHANGE_REJECTED, (Integer.toString(notificationID)), userArray.get(userIndex));
 					(userArray.get(i)).addNotification(not);
 					notificationArray.add(not);
 					break;				
 				}
 				default:
 					break;
 				}
 			}
 		}
 		return notificationArray;
 	}
 	
 	public void addParticipants() throws SQLException{
 		//method to addParticipants to a meeting
 		for (int i = 0; i < eventArray.size(); i++) {
 			//goes throu the eventArray to add participants to only the meetings
 			if(eventArray.get(i) instanceof Meeting){				
 				java.sql.PreparedStatement pstmt;
 				//execute query that finds witch meeting is connected to witch user
 				String sel = "SELECT UserNotification.Username, Notification.NotificationID, Invitation.InvitationID, Invitation.Status, Event.EventID"
 											+" FROM UserNotification JOIN Notification ON Notification.NotificationID = UserNotification.NotificationID"
 											+" JOIN BelongsTo ON Notification.NotificationID = BelongsTo.NotificationID"
 											+" JOIN Invitation ON BelongsTo.InvitationID = Invitation.InvitationID"
 											+" JOIN InvitationTo ON Invitation.InvitationID = InvitationTo.InvitationID"
 											+" JOIN Event ON Event.EventID = InvitationTo.EventID"
 											+" WHERE Event.EventID = ? AND Invitation.Status ='1';";
 				pstmt = conn.prepareStatement(sel);
 				pstmt.setString(1, ((Meeting)(eventArray.get(i))).getId());
 				ResultSet rs = pstmt.executeQuery();
 				while(rs.next()){
 					//goes throu all the elements in the resultset and finds the rigth participants and add to the list connected to a meeting
 					String username = rs.getString("Username");
 					for (int j = 0; j < userArray.size(); j++) {
 						if(userArray.get(j).getUsername().equalsIgnoreCase(username)){
 							((Meeting)(eventArray.get(i))).addParticipant(userArray.get(j));
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	
 	public void addInvitation() throws SQLException{
 		for (int i = 0; i < eventArray.size(); i++) {
 			if(eventArray.get(i) instanceof Meeting){				
 			Statement stmt = conn.createStatement();
 			ResultSet rs = stmt.executeQuery("SELECT InvitationID FROM InvitationTo WHERE EventID='" + ((Meeting)eventArray.get(i)).getId()+"';");
 			while(rs.next()){
 				int invitationID = rs.getInt("InvitationID");
 				((Meeting)eventArray.get(i)).addInvitation((Integer.toString(invitationID)));
 				}
 			}	
 		}
 	}
 	
 	public void addUserSubscription() throws SQLException{
 		//method to add usersubscription to other users
 		for (int i = 0; i < userArray.size(); i++) {
 			Statement stmt = conn.createStatement();
 			//execute a query to find out witch user the chosen user subscribes to
 			ResultSet rs = stmt.executeQuery("SELECT AuthorUsername FROM UserSubscription WHERE SubscriberUsername='"+ userArray.get(i).getUsername()+ "';");
 			while(rs.next()){
 				String username = rs.getString("AuthorUsername");
 				for (int j = 0; j < userArray.size(); j++) {
 					if(userArray.get(j).getUsername().equalsIgnoreCase(username)){
 						userArray.get(i).addSubscription(userArray.get(j));
 						j = userArray.size();
 					}
 				}
 			}
 		}
 	}
 	
 
 	public String getNewKey(SaveableClass type){
 		//method to get the chosen type key from the database, gets the next vacant key in those tables with auto increment
 		if(type.equals(SaveableClass.Appointment) || type.equals(SaveableClass.Meeting)){
 			//the type is either an appointment or a meeting, select eventIDs from the event table
 			String AppKey = "";
 			eventCount +=1;
 			try {
 				Statement stmt = conn.createStatement();
 				ResultSet rs = stmt.executeQuery("SELECT EventID FROM Event");
 				int appKey = 0 + eventCount;
 				if(rs.last()){
 					int eventID = rs.getInt("EventID");
 					appKey += eventID;
 				}
 				AppKey = Integer.toString(appKey);
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 			return AppKey;
 		}
 		else if(type.equals(SaveableClass.Invitation)){
 			//the type is a invitation, select invitationIDs from the invitaiton table
 			String InvtKey = "";
 			invitationCount +=1;
 			int invtKey = 0 + invitationCount;
 			try{
 				Statement stmt = conn.createStatement();
 				ResultSet rs = stmt.executeQuery("SELECT InvitationID FROM Invitation");
 				if(rs.last()){
 					int invtID = rs.getInt("InvitationID");
 					invtKey += invtID;
 				}
 				InvtKey = Integer.toString(invtKey);
 			} catch(SQLException e){
 				e.printStackTrace();
 			}
 			return InvtKey;
 		}
 		else if(type.equals(SaveableClass.Notification)){
 			//the type is a notification, select notificationIDs from the notfication table
 			String NotKey = "";
 			notificationCount +=1;
 			int notKey = 0 + notificationCount;
 			try {
 				Statement stmt = conn.createStatement();
 				ResultSet rs = stmt.executeQuery("SELECT NotificationID FROM Notification");
 				if(rs.last()){
 					int notID = rs.getInt("NotificationID");
 					notKey += notID;
 				}
 				NotKey = Integer.toString(notKey);
 			} catch(SQLException e){
 				e.printStackTrace();
 			}
 			return NotKey;
 		}
 		else{
 			//the type is a room, select the roomIDs from the room table
 			String RoomKey = "";
 			roomCount += 1;
 			int roomKey = 0 + roomCount;
 			try{
 				Statement stmt = conn.createStatement();
 				ResultSet rs = stmt.executeQuery("SELECT RoomID FROM Room");
 				if(rs.last()){
 					int roomID = rs.getInt("RoomID");
 					roomKey += roomID;
 				}
 				RoomKey = Integer.toString(roomKey);
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 			return RoomKey;
 		}
 	}
 	
 	
 	public ArrayList<SyncListener> load() throws SQLException{
 		//method to load all the elements in the database
 		ArrayList<SyncListener> loadArray = new ArrayList<SyncListener>();
 		userArray = loadUser();
 		eventArray = loadEvent();
 		addParticipants();
 		addUserSubscription();
 		addInvitation();
 		loadArray.addAll(userArray);
 		loadArray.addAll(loadRoom());
 		loadArray.addAll(eventArray);
 		loadArray.addAll(loadInvitation());
 		loadArray.addAll(loadNotifcation());
 		return loadArray;	
 	} 
 	
 	public void closeConnection() throws SQLException{
 		conn.close();
 	}
 
 }
