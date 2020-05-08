 package data.service;
 
 import java.sql.Date;
 import java.util.ArrayList;
 
 import models.*;
 import data.database.DatabaseHandler;
 import database.connector.Field;
 import database.connector.Row;
 
 /*TODO: Find out if I need synchronized*/
 
 public class ServiceProvider {
 	private static ServiceProvider instance;
 	private DatabaseHandler dbHandler;
 	private JSONHandler jsonHandler;
 	
 	private ServiceProvider(DatabaseHandler dbHandler){
 		this.dbHandler = dbHandler;
 		this.jsonHandler = JSONHandler.getInstance();
 	}
 	
 	public static ServiceProvider getInstance(DatabaseHandler dbHandler) {
		if(instance == null) {
			instance = new ServiceProvider(dbHandler);
		}
		return instance;
 	}
 	
 	public User verifyLogIn(String json) {
 		User logInUser = jsonHandler.userFromJSON(json);
 		if(logInUser == null) {
 			System.err.println("Error while verifying log in with data: " + json);
 			return null;
 		}
 		
 		ArrayList<Row> result = dbHandler.verifyLogIn(logInUser.getUsername(), logInUser.getPassword());
 		
 		if(result == null || result.size() > 1) //Should only return one user.
 			return new User(0, logInUser.getUsername(), null, null, null, null); //For errorhandling
 		
 		Row row = result.get(0);
 		Field userN = row.getField("Username"); Field id = row.getField("UserID");
 		Field firstN = row.getField("Firstname"); Field lastN = row.getField("Lastname"); 
 		Field adr = row.getField("Adress");
 		
 		String username = userN.getFieldAsString(), firstname = firstN.getFieldAsString(),
 			   lastname = lastN.getFieldAsString(), adress = adr.getFieldAsString();
 		int userId = id.getFieldAsInt();
 		
 		return new User(userId, username, firstname, lastname, adress, null); //password is not relevant to client
 	}
 	
 	public ArrayList<ViewBid> getUsersBids(String json) {
 		User user = jsonHandler.userFromJSON(json);
 		if(user == null) {
 			System.err.println("Error while getting usersBids - corrupt data received");
 			return null;
 		}
 		
 		ArrayList<Row> result = dbHandler.getBidsByUser(user.getUserID());
 		
 		if(result == null || result.size() == 0) //User has no leading bids.
 			return null;
 		
 		ArrayList<ViewBid> viewBids = new ArrayList<>();
 		String name = null;
 		int value = 0, itemno = 0;
 		ViewBid viewBid = null;
 		for(Row row : result) {			
 			name = row.getField("name").getFieldAsString();
 			value = row.getField("value").getFieldAsInt();
 			itemno = row.getField("itemno").getFieldAsInt();
 			viewBid = new ViewBid(name, itemno, value, String.valueOf(user.getUserID()));
 			viewBids.add(viewBid);
 		}
 		
 		return viewBids;
 	}
 	
 	public Bid placeBid(String json) {
 		Bid incoming = jsonHandler.bidFromJSON(json);
 		if(incoming == null) {
 			System.err.println("Error while placing bid - check fields");
 			return null;
 		}
 		long result = dbHandler.placeBid(incoming.getItemno(), incoming.getUserId(), incoming.getValue(), incoming.getUsername());
 		
 		if(result <= 0)
 			return null;
 		
 		incoming.setBidID((int)result);
 		return incoming;
 	}
 	
 	public boolean registerUser(String json) {
 		User user = jsonHandler.userFromJSON(json);
 		if(user == null) {
 			System.err.println("Error registering user - check fields");
 			return false;
 		}
 		
 		long result = dbHandler.registerUser(user.getUsername(), user.getFirstname(), user.getLastname(), user.getAdress(), user.getPassword());
 		
 		return result > 0;
 	}
 	
 	public Item registerItem(String json) { //The date has to be formatted like this on the client: "mar 27, 2013"
 		Item item = jsonHandler.itemFromJSON(json);
 		if(item == null) {
 			System.err.println("Error registering item - check fields");
 			return null;
 		}
 		
 		long result = dbHandler.registerItem(item.getName(), item.getPrice(), item.getExpires().toString(), item.getDescription(), item.getAddedByID());
 		
 		if(result < 0)
 			return null;
 		
 		item.setItemno((int)result);
 		return item;
 	}
 	
 	public int deleteItem(String json) {
 		Item item = jsonHandler.itemFromJSON(json);
 		if(item == null) {
 			System.err.println("Error deleting item - corrupt data received: " + json);
 			return -1;
 		}
 		
 		return dbHandler.deleteItem(item.getItemno()) ? item.getItemno() : -1;
 	}
 	
 	public ArrayList<PrettyItem> getAllItems() {
 		ArrayList<Row> result = dbHandler.getAllItems();
 		
 		if(result.size() == 0)
 			return null;
 		
 		String highestbidder = null, name = null, description = null;
 		int itemno = 0, price = 0, bid = 0, addedByID = 0;
 		Date expiredate = null;
 		
 		ArrayList<PrettyItem> items = new ArrayList<>();
 		
 		for(Row row : result) {
 			highestbidder = row.getField("highestbidder").getFieldAsString();
 			name = row.getField("name").getFieldAsString();
 			description = row.getField("description").getFieldAsString();
 			itemno = row.getField("itemno").getFieldAsInt();
 			price = row.getField("price").getFieldAsInt();
 			bid = row.getField("bid").getFieldAsInt();
 			addedByID = row.getField("addedByID").getFieldAsInt();
 			expiredate = row.getField("expiredate").getFieldAsDate();
 			items.add(new PrettyItem(itemno, name, description, highestbidder, price, bid, addedByID, expiredate));
 		}
 		
 		return items;
 		
 	}
 }
