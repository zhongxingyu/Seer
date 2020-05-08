 package server;
 
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.TimeUnit;
 
 import networking.packages.AuthenticationRequest;
 import networking.packages.AuthenticationResponse;
 import networking.packages.AuthenticationResponse.AuthenticationResponseType;
 import networking.packages.QueryRequest;
 import networking.packages.DataResponse;
 import networking.packages.DataResponse.DataResponseType;
 import networking.packages.Response;
 import networking.packages.UpdateRequest;
 import data.Alarm;
 import data.Meeting;
 import data.MeetingRoom;
 import data.Notification;
 import data.Notification;
 import data.Person;
 import data.Team;
 
 public class ServerRequestHandler implements Runnable {
 	/**
 	 * Will listen to a blockingqueue and when it is populated handle the
 	 * requests inside
 	 */
 	BlockingQueue<PendingResponse> responses;
 	BlockingQueue<ReceivedRequest> requests;
 	private DBController dbController;
 
 	public ServerRequestHandler(BlockingQueue<PendingResponse> responses,
 			BlockingQueue<ReceivedRequest> requests, DBController dbController) {
 		super();
 		this.responses = responses;
 		this.requests = requests;
 		this.dbController = dbController;
 	}
 
 	private void handleAuthenticationRequest(AuthenticationRequest aRequest,
 			Socket client) {
 		AuthenticationResponse aResponse = null;
 		try {
 			if (dbController.personExists(aRequest.getUsername())) {
 				if (dbController.authenticateUser(aRequest.getUsername(),
 						aRequest.getPassword())) {
 					aResponse = new AuthenticationResponse(
 							AuthenticationResponseType.APPROVED);
 				}
 				else{
 					aResponse = new AuthenticationResponse(
 							AuthenticationResponseType.WRONG_PASS);					
 				}
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		if(aResponse == null){
 			aResponse = new AuthenticationResponse(
 					AuthenticationResponseType.USER_NOEXIST);			
 		}
 
 		try {
 			while (!responses.offer(new PendingResponse(aResponse, client,
 					false), 200, TimeUnit.MILLISECONDS))
 				;
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	private DataResponse getEveryPerson() {
 		List<Person> data = null;
 		try {
 			data = dbController.getEveryPerson();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		DataResponse response = new DataResponse(data,
 				DataResponseType.PERSON_RESPONSE, true);
 		return response;
 	}
 
 	private DataResponse getNotificationsByMeeting(Meeting meeting) {
 		List<Notification> notifications = null;
 		try {
 			notifications = dbController.getNotifications(meeting);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		DataResponse response = new DataResponse(notifications,
 				DataResponseType.NOTIFICATION_RESPONSE, true);
 		return response;
 	}
 
 	private DataResponse getAlarms(String username) {
 		List<Alarm> data = null;
 		try {
 			data = dbController.getAlarmsOfPerson(username);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		DataResponse response = new DataResponse(data,
 				DataResponseType.ALARM_RESPONSE, true);
 		return response;
 	}
 
 	private DataResponse getMeetings() {
 		List<Meeting> data = null;
 		try {
 			data = dbController.getEveryMeeting();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		DataResponse response = new DataResponse(data,
 				DataResponseType.MEETING_RESPONSE, true);
 		return response;
 	}
 
 	private DataResponse getTeamsByMeeting(Meeting meeting) {
 		List<Team> teams = null;
 
 		try {
 			teams = dbController.getTeamsByMeeting(meeting.getMeetingID());
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return new DataResponse(teams, DataResponseType.MEETING_RESPONSE, true);
 	}
 	
 	private DataResponse getAllNotifications(){
 		List<Notification> meetings = null;
 		try {
 			meetings = dbController.getAllNotifications();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		DataResponse response = new DataResponse(meetings,
 				DataResponseType.NOTIFICATION_RESPONSE, false);
 		return response;
 	}
 
 	private void handleQueryRequest(QueryRequest request, Socket client) {
 		DataResponse response = null;
 
 		switch (request.getQueryType()) {
 		case GET_NOTIFICATIONS_BY_PERSON:
 			response = getNotificationsByPerson(request.getUsername());
 			break;
 		case GET_ALL_PERSONS:
 			response = getEveryPerson();
 			break;
 		case GET_NOTIFICATIONS_BY_MEETING:
 			response = getNotificationsByMeeting(request.getMeeting());
 			break;
 		case GET_ALARMS_BY_PERSON:
 			response = getAlarms(request.getUsername());
 		case GET_EVERY_MEETING:
 			response = getMeetings();
 			break;
 		case GET_TEAMS_BY_MEETING:
 			response = getTeamsByMeeting(request.getMeeting());
 			break;
 		case GET_ALL_NOTIFICATIONS:
 			response = getAllNotifications();
 			break;
 		case GET_ALL_MEETINGROOMS:
 			response = getAllMeetingRooms();
 			break;
 		default:
 			break;
 		}
 
 		try {
 			while (!responses.offer(
 					new PendingResponse(response, client, false), 200,
 					TimeUnit.MILLISECONDS))
 				;
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	private DataResponse getAllMeetingRooms(){
 		List<MeetingRoom> meetingRooms = null;
 		try {
 			meetingRooms = dbController.getAllMeetingRooms();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		DataResponse response = new DataResponse(meetingRooms,
 				DataResponseType.MEETINGROOM_RESPONSE, false);
 		return response;
 	}
 	
 	private DataResponse getNotificationsByPerson(String username) {
 		List<Notification> notifications = null;
 		try {
 			notifications = dbController.getNotifications(username);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		DataResponse response = new DataResponse(notifications,
 				DataResponseType.NOTIFICATION_RESPONSE, true);
 		return response;
 	}
 
 	private void processRequest(ReceivedRequest request) {
 
 		switch (request.networkRequest.getEventType()) {
 		case AUTHENTICATION:
 			handleAuthenticationRequest(
 					(AuthenticationRequest) request.networkRequest,
 					request.clientSocket);
 			break;
 		case LOGOUT:
 			break;
 		case QUERY:
 			handleQueryRequest((QueryRequest) request.networkRequest,
 					request.clientSocket);
 			break;
 		case UPDATE:
 			handleUpdateRequest((UpdateRequest) request.networkRequest,
 					request.clientSocket);
 			break;
 		default:
 			OutputController
 					.output("Received a request, but unable to determine type: "
 							+ request);
 			break;
 		}
 
 	}
 	
 	private void sendAlarmsToPerson(Socket client , String username){
 		List<Alarm> alarms = null;
 		try {
 			alarms = dbController.getAlarmsOfPerson(username);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		addResponseToQueue(new PendingResponse(new DataResponse(alarms, DataResponseType.ALARM_RESPONSE, false), client , false));
 	}
 
 	private void addAlarm(UpdateRequest request) {
 		try {
 			dbController.addAlarm(request.getAlarm());
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		 
 
 	}
 
 	private void addMeeting(Meeting meeting) {
 		try {
 			dbController.addMeeting(meeting);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void sendAllMeetings() {
 		List<Meeting> meetings = null;
 		try {
 			meetings = dbController.getEveryMeeting();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		DataResponse response = new DataResponse(meetings,
 				DataResponseType.MEETING_RESPONSE, false);
 		try {
 			while (!responses.offer(new PendingResponse(response, null, true),
 					200, TimeUnit.MILLISECONDS))
 				;
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public DataResponse editNotification(Notification notification) {
 		try {
 			dbController.updateNotification(notification);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		try {
 			return new DataResponse(dbController.getAllNotifications(),
 					DataResponseType.NOTIFICATION_RESPONSE, false);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	private void addResponseToQueue(PendingResponse pResponse){
 		if(pResponse.getResponse() != null){
 			while (!responses.offer(pResponse))
 				;				
 		}
 	}
 	
 	private void deleteMeeting(Meeting meeting){
		
 	}
 	
 	private void handleUpdateRequest(UpdateRequest request, Socket client) {
 		switch (request.getUpdateType()) {
 		case CREATE_ALARM:
 			addAlarm(request);
 			sendAlarmsToPerson(client, request.getSender().getUsername());
 			break;
 		case CREATE_MEETING:
 			addMeeting(request.getMeeting());
 			sendAllMeetings();
 			sendAllNotifications();
 			break;
 		case UPDATE_MEETING:
 			updateMeeting(request.getMeeting());
 			sendAllMeetings();
 			sendAllNotifications();
 			break;
 		case UPDATE_NOTIFICATION:
 			editNotification(request.getNotification());
 			sendAllNotifications();
 			break;
 		case DELETE_MEETING:
 			deleteMeeting(request.getMeeting());
 			sendAllMeetings();
 			sendAllNotifications();
 			break;
 		default:
 			break;
 		}
 
 		
 
 	}
 
 	private void updateMeeting(Meeting meeting) {
 		try {
 			dbController.updateMeeting(meeting);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 	}
 
 	private void sendAllNotifications() {
 		addResponseToQueue(new PendingResponse(getAllNotifications(), null, true));
 		
 	}
 
 	public void run() {
 		OutputController.output("RequestHandler initialized");
 		while (true) {
 			try {
 				processRequest(requests.take());
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
