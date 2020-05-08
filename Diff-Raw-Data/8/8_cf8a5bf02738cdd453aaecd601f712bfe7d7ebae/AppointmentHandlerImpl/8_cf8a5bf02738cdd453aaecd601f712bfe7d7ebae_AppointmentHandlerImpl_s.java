 package server;
 
 import static calendar.Appointment.recreateAppointment;
 import static dateutils.DateUtils.getEndOfWeek;
 import static dateutils.DateUtils.getStartOfWeek;
 
 import java.io.IOException;
 import java.rmi.RemoteException;
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import calendar.Appointment;
 import calendar.Message;
 import calendar.Person;
 import calendar.RejectedMessage;
 import client.helpers.StoopidSQLException;
 
 public class AppointmentHandlerImpl extends Handler implements AppointmentHandler {
 	private static PersonHandler personHandler;
 	private static MessageHandler msgHandler;
 	
 	public static void init(){
 		personHandler = new PersonHandlerImpl();
 		msgHandler = new MessageHandlerImpl();
 	}
 	
 	public void createAppointment(Appointment app) throws IOException, RemoteException {
 		String query = "INSERT INTO Appointment(appId, title, place, startTime, endTime, " +
 					"description, roomName, isPrivate, creatorId) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
 		Date start = app.getStartTime();
 		Date end = app.getEndTime();
		long appId = dbEngine.getUniqueId();
		app.setAppId(appId);
 		try {
 			PreparedStatement ps = dbEngine.getPreparedStatement(query);
			ps.setLong(1, appId);
 			ps.setString(2, app.getTitle());
 			ps.setString(3, app.getPlace());
 			ps.setTimestamp(4, new Timestamp(start.getTime()));
 			ps.setTimestamp(5, new Timestamp(end.getTime()));
 			ps.setString(6, app.getDescription());
 			ps.setString(7, app.getRoomName());
 			ps.setBoolean(8, app.isPrivate());
 			ps.setLong(9, app.getCreator().getId());
 			ps.executeUpdate();
 		} catch (SQLException e) {
 			throw new StoopidSQLException(e);
 		}
 		
 		Map<Person, Boolean> participants = app.getParticipants();
 		for (Person user: participants.keySet()) {
			addUserToAppointment(appId, user.getId());
 		}
 	}
 	
 	public void answerInvite(long appId, long userId, Boolean answer) throws RemoteException, IOException{
 		String query = "UPDATE UserAppointments SET hasAccepted=%b WHERE userId=? AND appId=?";
 		dbEngine.update(String.format(query, answer), userId, appId);
 		if (answer == false){
 			msgHandler.sendMessageUserHasDenied(appId, userId);
 		}
 	}
 	
 	public void updateAppointment(Appointment app) throws IOException {
 		String place = app.getPlace();
 		String title = app.getTitle();
 		Date start = app.getStartTime();
 		Date end = app.getEndTime();
 		String description = app.getDescription();
 		String roomName = app.getRoomName();
 		Map<Person, Boolean> participants = app.getParticipants();
 		boolean isPrivate = app.isPrivate();
 		long appId = app.getId();
 		long creatorId = app.getCreator() != null ? app.getCreator().getId() : 0;
 		
 		String query =
 				"UPDATE Appointment SET place=?, title='%s', startTime=?, endTime=?, " +
 				"description=?, roomName=?, isPrivate=%b, " +
 				" creatorId=%d WHERE appId=%d";		
 
 		try {
 			String formatted = String.format(query, title, isPrivate, creatorId, appId);
 			PreparedStatement ps = dbEngine.getPreparedStatement(formatted);
 			ps.setString(1, place);
 			ps.setTimestamp(2, new Timestamp(start.getTime()));
 			ps.setTimestamp(3, new Timestamp(end.getTime()));
 			ps.setString(4, description);
 			ps.setString(5, roomName);
 			ps.executeUpdate();
 		} catch (SQLException e) {
 			throw new StoopidSQLException(e);
 		}	
 		if (participants != null) {
 			for (Person user: participants.keySet()) {
 				addUserToAppointment(appId, user.getId());
 			}
 		}
 	}
 	
 	public void deleteAppointment(long appId) throws IOException {
 		Appointment app = getAppointment(appId);
 		Message msg = new Message("Slettet avtale " + app.getTitle(), "Denne avtalen er blitt slettet");
 		List<Person> receivers = Arrays.asList(app.getParticipants().keySet().toArray(new Person[0]));
 		msg.setReceivers(receivers);
 		String appQuery = "DELETE FROM Appointment WHERE appId=?";
 		String userAppQuery = "DELETE FROM UserAppointments WHERE appId=?";
 		dbEngine.update(appQuery, appId);
 		dbEngine.update(userAppQuery, appId);
 	}
 	
 	public void deleteAppointmentInvited(long appId, long userId) throws IOException {
 		Appointment app = getAppointment(appId);
 		Person rejecter = personHandler.getPerson(userId);
 		Message msg = new RejectedMessage("Avslag: " + app.getTitle(), "En person har avsltt mtet", app, rejecter);
 		List<Person> receivers = mapKeysToList(app.getParticipants());
 		receivers.add(app.getCreator());
 		msg.setReceivers(receivers);
 		String query = "DELETE FROM UserAppointments WHERE appId=? AND userId=?";
 		dbEngine.update(query, appId, userId);
 	}
 	
 	private static <T, S> List<T> mapKeysToList(Map<T, S> map){
 		List<T> list = new ArrayList<T>();
 		for(T t: map.keySet()){
 			list.add(t);
 		}
 		return list;
 	}
 	
 	public void updateUserAppointment(long appId, long userId, Boolean bool) throws IOException {
 		String query = "UPDATE UserAppointments SET hasAccepted=%s WHERE userId=? AND appId=?";
 		String boolString = bool == null ? "null" : bool ? "true" : "false";
 		dbEngine.update(String.format(query, boolString), userId, appId);
 	}
 	
 	public Map<Long, Boolean> getParticipants(long appId) throws IOException {
 		String query = "SELECT userId, hasAccepted FROM UserAppointments WHERE appId=?";
 		try {
 			PreparedStatement ps = dbEngine.getPreparedStatement(query);
 			ps.setLong(1, appId);
 			ResultSet rs = ps.executeQuery();
 			Map<Long, Boolean> output = new HashMap<Long, Boolean>();
 			while(rs.next()){
 				Boolean answer = rs.getObject(2) != null ? rs.getBoolean(2) : null;
 				output.put(rs.getLong(1), answer);	
 			}
 			return output;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			throw new RuntimeException("Feil i SQL!");
 		}
 	}
 	
 	public void addUserToAppointment(long appId, long userId) throws IOException {
 		String query = "INSERT INTO UserAppointments(appId, userId, hasAccepted) VALUES(?, ?, null)";
 		dbEngine.update(query, appId, userId);
 	}
 	
 	public void deleteUserFromAppointment(long appId, long msgId) throws IOException {
 		String query = "DELETE FROM UserAppoinments WHERE userId=? AND msgId=?";
 		dbEngine.update(query, appId, msgId);
 	}
 	
 	public void updateRoomName(long appId, String roomName) throws IOException {
 		String query = "UPDATE Appointment SET roomName=? WHERE appId=?";
 		PreparedStatement ps = dbEngine.getPreparedStatement(query);
 		try {
 			ps.setString(1, roomName);
 		} catch (SQLException e) {
 			throw new StoopidSQLException(e);
 		}
 		dbEngine.update(ps, appId);	
 	}
 	
 	public Appointment getAppointment(long appId) throws IOException {	
 		String query = "SELECT appId, title, place, startTime, endTime, description, " +
 				"roomName, isPrivate, creatorId FROM Appointment " +
 				"WHERE appId=? ORDER BY startTime";
 		return getListFromQueryAndId(query, appId).get(0);
 	}
 	
 	private List<Appointment> getAppointmentsFromPreparedStatement(PreparedStatement ps) throws IOException{
 		List<Appointment> appointments = new ArrayList<Appointment>();
 		try {
 			ResultSet rs = ps.executeQuery();
 			while(rs.next()){
 				long id = rs.getLong("appId");
 				String title = rs.getString("title");
 				String place = rs.getString("place");
 				Date startTime = new Date(rs.getTimestamp("startTime").getTime());
 				Date endTime = new Date(rs.getTimestamp("endTime").getTime());
 				String description = rs.getString("description");
 				String roomName = rs.getString("roomName");
 				boolean isPrivate = rs.getBoolean("isPrivate");
 				long creatorId = rs.getLong("creatorId");
 				Person creator = personHandler.getPerson(creatorId);
 				Map<Person, Boolean> participants = convertIdsToPersons(getParticipants(id));
 				Appointment a = recreateAppointment(id, title, startTime, endTime, isPrivate, participants, creator);
 				a.setPlace(place);
 				a.setDescription(description);
 				a.setRoomName(roomName);
 				appointments.add(a);
 			}
 		} catch (SQLException e) {
 			throw new StoopidSQLException(e);
 		}
 		return appointments;
 	}
 	
 	private static Map<Person, Boolean> convertIdsToPersons(Map<Long, Boolean> participants) throws IOException{
 		Map<Person, Boolean> out = new HashMap<Person, Boolean>();
 		for (Long i: participants.keySet()){
 			out.put(personHandler.getPerson(i), participants.get(i));
 		}
 		return out;
 	}
 	
 	public List<Appointment> getAllByUser(long userId) throws IOException {
 		String query = "SELECT appId, title, place, startTime, endTime, description, " +
 				"roomName, isPrivate, creatorId FROM Appointment WHERE creatorId=? " + 
 				"ORDER BY startTime";
 		return getListFromQueryAndId(query, userId);
 	}
 	
 	public List<Appointment> getAllInvited(long userId) throws IOException {
 		String query = "SELECT appId, title, place, startTime, endTime, description, " +
 				"roomName, isPrivate, creatorId FROM Appointment " +
 				"WHERE appId IN (SELECT appId FROM UserAppointments WHERE userId=?) ORDER BY startTime";
 		return getListFromQueryAndId(query, userId);
 	}
 	
 	public List<Appointment> getAllCreated(long userId, int weekNum) throws IOException {
 		Date startOfWeek = getStartOfWeek(weekNum);
 		Date endOfWeek = getEndOfWeek(weekNum);
 		String query = "SELECT appId, title, place, startTime, endTime, description, " +
 				"roomName, isPrivate, creatorId FROM Appointment WHERE creatorId=? " + 
 				"AND startTime > ? AND endTime < ? ORDER BY startTime";
 			try {
 				PreparedStatement ps = dbEngine.getPreparedStatement(query);
 				ps.setLong(1, userId);
 				ps.setTimestamp(2, new Timestamp(startOfWeek.getTime()));
 				ps.setTimestamp(3, new Timestamp(endOfWeek.getTime()));
 				return getAppointmentsFromPreparedStatement(ps);
 			} catch (SQLException e) {
 				throw new StoopidSQLException(e);
 			}
 	}
 	
 	public List<Appointment> getAllInvitedInWeek(long userId, int weekNum) throws IOException {
 		Timestamp startDate = new Timestamp(getStartOfWeek(weekNum).getTime());
 		Timestamp enddate = new Timestamp(getEndOfWeek(weekNum).getTime());
 		String query = "SELECT appId, title, place, startTime, endTime, description, " +
 		"roomName, isPrivate, creatorId FROM Appointment " +
 		"WHERE appId IN (SELECT appId FROM UserAppointments WHERE userId=?) AND "+
 		"startTime < ? AND endTime < ? ORDER BY startTime";
 		PreparedStatement ps = dbEngine.getPreparedStatement(query);
 		try {
 			ps.setLong(1, userId);
 			ps.setTimestamp(2, startDate);
 			ps.setTimestamp(3, enddate);
 		} catch (SQLException e) {
 			throw new StoopidSQLException(e);
 		}
 		return getAppointmentsFromPreparedStatement(ps);
 	}
 	
 	public List<Appointment> getAllUnansweredInvites(long userId) throws IOException{
 		String query = "SELECT appId, title, place, startTime, endTime, description, " +
 				"roomName, isPrivate, creatorId FROM Appointment " +
 				"WHERE appId IN (SELECT appId FROM UserAppointments WHERE userId=? AND hasAccepted IS NULL) " + 
 				"ORDER BY startTime";
 		return getListFromQueryAndId(query, userId);
 	}
 	
 	/**
 	 * Henter ut fra queryen en liste med Appointments. Queryes m ha like mange ? som 
 	 * elementer i ids. (som hrer til et id-felt) 
 	 * @param query
 	 * @param ids
 	 * @throws IOException
 	 */
 	private List<Appointment> getListFromQueryAndId(String query, long... ids) throws IOException{
 		PreparedStatement ps;
 		try {
 			ps = dbEngine.getPreparedStatement(query);
 			for(int i = 0; i < ids.length; i++){
 				ps.setLong(i + 1, ids[i]);
 			}
 		} catch (SQLException e) {
 			throw new StoopidSQLException(e);
 		}
 		return getAppointmentsFromPreparedStatement(ps);
 	}
 	
 	public List<Appointment> getWeekAppointments(long userId, int weekNum) throws IOException {
 		Date startOfWeek = getStartOfWeek(weekNum);
 		Date endOfWeek = getEndOfWeek(weekNum);
 		String query = "SELECT appId, title, place, startTime, endTime, description, " +
 				"roomName, isPrivate, creatorId FROM Appointment " +
 				"WHERE creatorId=%d AND startTime > ? AND endTime < ? ORDER BY startTime";
 		try {
 			PreparedStatement ps = dbEngine.getPreparedStatement(String.format(query, userId));
 			ps.setTimestamp(1, new Timestamp(startOfWeek.getTime()));
 			ps.setDate(2, endOfWeek);
 			return getAppointmentsFromPreparedStatement(ps);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			throw new RuntimeException("Feil i SQL, stoopid!");
 		}
 	}
 
 	@Override
 	public long getUniqueId() throws IOException, RemoteException {
 		return dbEngine.getUniqueId();
 	}
 
 	@Override
 	public void deleteParticipants(long appId, Map<Person, Boolean> participants) throws IOException, RemoteException {
 		String query = "DELETE FROM UserAppointments WHERE appId=? AND userId=?";
 		for(Person user: participants.keySet()){
 			dbEngine.update(query, appId, user.getId());
 		}
 	}
 
 }
