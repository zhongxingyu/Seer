 package data.service;
 
 import models.*;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 public class JSONHandler {
 	private static JSONHandler instance;
 	private Gson gson;
 	
 	private JSONHandler() {
 		gson = new GsonBuilder().serializeNulls().create();
 	}
 	
 	public static JSONHandler getInstance() {
		return instance == null ? new JSONHandler() : instance;
 	}
 	
 	public User userFromJSON(String json) {
 		User user = gson.fromJson(json, User.class);
 		return validateUser(user) ? user : null;
 	}
 	
 	public String userToJSON(User user) {
 		return gson.toJson(user);
 	}
 	
 	public Item itemFromJSON(String json) {
 		Item item =  gson.fromJson(json, Item.class);
 		return validateItem(item) ? item : null;
 	}
 	
 	public String itemToJSON(Item item) {
 		return gson.toJson(item);
 	}
 	
 	public Bid bidFromJSON(String json) {
 		Bid bid = gson.fromJson(json, Bid.class);
 		return validateBid(bid) ? bid : null;
 	}
 	
 	public String bidToJSON(Bid bid) {
 		return gson.toJson(bid);
 	}
 	
 	public String itemsToJSON(Item[] items) {
 		return gson.toJson(items);
 	}
 	
 	private boolean validateUser(User user) {
 		return user.getUsername() != null && !user.getUsername().equals(""); //Because username will always be set.
 	}
 	
 	private boolean validateItem(Item item) {
 		return item.getAddedByID() > 0 || item.getItemno() > 0 && item.getName() == null && item.getAddedByID() == 0; //Add/delete looks different
 	}
 	
 	private boolean validateBid(Bid bid) {
 		return bid.getUserId() > 0; //Because userId will always be set for a new bid.
 	}
 	
 	
 	
 }
