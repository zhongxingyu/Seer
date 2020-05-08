 package net;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Properties;
 
 import javax.swing.JOptionPane;
 
 import model.Event;
 
 import datapackage.*;
 import encryption.PasswordEncryption;
 
 /*
  * Takes a DataPackage as argument, and returns the Package that should be sent to the client.
  **/
 
 public class ServerPackageHandler {
 	//DataPackage pack;
 	DBConnection connection;
 	
 	public ArrayList<DataPackage> HandlePackage(DataPackage pack){
 		ArrayList<DataPackage> returnPackages = new ArrayList<DataPackage>();
 		System.out.println("Debugpoint #1");
 		if (pack instanceof LoginPackage){
 			System.out.println("Debugpoint #2");
 			try {
 				if (handleLoginPackage(pack)){
 					System.out.println("Debugpoint #3");
 					ErrorPackage errorPack = new ErrorPackage(ErrorType.OK,"All is well, user may pass",1,1);
 					returnPackages.add(errorPack);
 				}
 				else{
 					System.out.println("Debugpoint #4");
 					ErrorPackage errorPack = new ErrorPackage(ErrorType.WRONG_PASSWORD,"The user SHALL NOT PASS", 1, 1);
 					returnPackages.add(errorPack);
 				}
 			} catch (IOException e) {
 				System.out.println("Debugpoint #5");
 				System.out.println("Good God, it crashed!");
 				e.printStackTrace();
 			}
 		}
 		else if(pack instanceof CalendarRequestPackage){
 			System.out.println("CalReqPack Received");
 			try {
 				returnPackages = handleCalendarRequestPackage(pack);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		else if (pack instanceof EventPackage){
 			System.out.println("EventPackage Received");
 			try{
 				returnPackages = handleEventPackage(pack);
 			}
 			catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			
 		}else if (pack instanceof EventUpdatePackage){
 			System.out.println("EventUpdatePackage Received");
 			try {
 				returnPackages = eventUpdateHandler((EventUpdatePackage)pack);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 		}
 		return returnPackages;
 	}
 
 
 	private DBMethods connectToDB() throws IOException{
 				//load properties
 				System.out.println("Debugpoint #6");
 				Properties prop = new Properties();
 		        InputStream in = PackageSender.class.getResourceAsStream("Properties.properties");
 		        prop.load(in);
 		        //connect to DB
 				connection = new DBConnection(prop);
 				connection.connect();
 				Connection c = connection.getConnection();
 				Statement s = connection.getStatement();
 				DBMethods method = new DBMethods();
 				method.setConnection(c);
 				method.setStatement(s);
 				
 				return method;
 	}
 
 	private void disconnectFromDB() throws SQLException{
 		connection.close();
 	}
 
 	private boolean handleLoginPackage(DataPackage pack) throws IOException {
 		DBMethods method = connectToDB();
 		//store pack as LoginPackage, must be checked before method is called
 		LoginPackage loginpack = (LoginPackage)pack;
 		byte[] hash = null;
 		byte[] salt = null;
 		//exec logincheck
 		String username = loginpack.getUsername();
 		//Check to see if user exists
 		try {
 			if (method.isExcistingUser(username)){
 				try{
 					hash = method.getStoredHash(loginpack.getUsername(), "password");
 					salt = method.getStoredHash(loginpack.getUsername(), "salt");
 				}
 				catch (SQLException e){
 					System.out.println("Something went horribly wrong in the DB");
 					e.printStackTrace();
 					return false;
 				}
 			}
 			else{
 				return false;
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 		
 		try {
 			disconnectFromDB();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		if (PasswordEncryption.checkPassword(loginpack.getPassword(), salt, hash)){
 			return true;
 		}
 		else{
 			return false;
 		}
 		
 	}
 	
 	private ArrayList<DataPackage> handleCalendarRequestPackage(DataPackage pack) throws IOException, SQLException {
 		CalendarRequestPackage CalReq = (CalendarRequestPackage)pack;
 		String name = CalReq.getName();
 		Integer group = CalReq.getGroup();
 		ArrayList<Event> events = new ArrayList<Event>();
 		ArrayList<DataPackage> responseList = new ArrayList<DataPackage>();
 		if ((CalReq.getName() != null) && (CalReq.getGroup() == null)){
 			DBMethods method = connectToDB();
 			events = method.loadEvents(name);
 			disconnectFromDB();
 			
 			responseList = constructPackageListFromEvents(events);
 			return responseList;
 		}
 		else if ((CalReq.getGroup() != null) && (CalReq.getName() == null)){
 			DBMethods method = connectToDB();
 			//get groups cal
 			disconnectFromDB();
 			return null;
 		}
 		else{
 			System.out.println("MalformatedCalReqPack");
 			JOptionPane.showMessageDialog(null, "Malformated CalReqPackage");
 			return null;
 		}
 	}
 	
 	private ArrayList<DataPackage> handleEventPackage(DataPackage pack) throws IOException, SQLException {
 		EventPackage eventPack = (EventPackage)pack;
 		Event event = eventPack.getEvent();
 		//Eventprintout
 		System.out.println("Received an eventPack with the following content:");
 		System.out.println(event.getEventId());
 		System.out.println(event.getCreatedBy().getUsername());
 		System.out.println(event.getStartTime());
 		System.out.println(event.getEndTime());
 		System.out.println(event.getName());
 		System.out.println(event.getDescription());
 		System.out.println(event.getPlace());
 		System.out.println(event.getRoom());
 		
 		DBMethods method = connectToDB();
		Event returnEvent = method.createEvent(event.getCreatedBy().getUsername(), event.getStartTime(), event.getEndTime(), event.getName(), event.getDescription(), event.getPlace(), "" /*TODO*/, "" /*TODO*/, event.getRoom().getRoomNr());
 		disconnectFromDB();
 		
 		ArrayList<DataPackage> returnPackages = new ArrayList<DataPackage>();
 		
 		if (returnEvent.getEventId() != 0){
 			returnPackages.add(new EventPackage(1, 1, returnEvent));
 			return returnPackages;
 		}
 		else{
 			returnPackages.add(new ErrorPackage(ErrorType.EVENT_CREATION_ERROR, "Could not create event correctly", 1, 1));
 			return returnPackages;
 		}
 	}
 	
 	private ArrayList<DataPackage> constructPackageListFromEvents(ArrayList<Event> events){
 		ArrayList<DataPackage> returnList = new ArrayList<DataPackage>();
 		Event event;
 		for (int i=0;i<events.size();i++){
 			event = events.get(i);
 			EventPackage pack = new EventPackage(i, events.size(), event);
 			returnList.add(pack);
 		}
 		return returnList;
 	}
 	
 	//Should probably not be used, will cause massive connection problems...
 	private void sendResponsePackage(DataPackage pack) throws IOException{
 		PackageSender sender = new PackageSender();
 		sender.sendPackage(pack);
 		
 	}
 	
 	private ArrayList<DataPackage> eventUpdateHandler(EventUpdatePackage pack) throws IOException, SQLException{
 		ArrayList<DataPackage> list = new ArrayList<DataPackage>(); 
 		if (pack.getProperty().equals("Delete")){
 			DBMethods method = connectToDB(); 
 			method.deleteEvent(pack.getEventID());
 			disconnectFromDB();
 			list.add(new ErrorPackage(ErrorType.OK, "Package was deleted", 1, 1));
 		}
 		else if (pack.getProperty().equals("FULL_UPDATE")){
 			Event event = (Event)pack.getNewValue();
 			//Update DB here!
 		}
 		return list;
 	}
 }
