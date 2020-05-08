 package com.example.payback;
 
 import java.util.*;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 abstract class Account
 {
 	String fName;
 	String lName;
 	String email;
 	String displayName;
 	
 	public String getfName() {
 		return fName;
 	}
 	public void setfName(String fName) {
 		this.fName = fName;
 	}
 	public String getlName() {
 		return lName;
 	}
 	public void setlName(String lName) {
 		this.lName = lName;
 	}
 	public String getEmail() {
 		return email;
 	}
 	public void setEmail(String email) {
 		this.email = email;
 	}
 	public String getDisplayName() {
 		return displayName;
 	}
 	public void setDisplayName(String displayName) {
 		this.displayName = displayName;
 	}
 }
 
 public class User extends Account{
 	private ArrayList<Friend> friends; //updated when the User logs in
 	private ArrayList<Notification> notifications;
 	private ArrayList<ResolveTransaction> transactions;
 	private ArrayList<BaseTransaction> transAsLend;
 	private ArrayList<BaseTransaction> transAsBorrow;
 	private String password;
 	
 	/* Only called when creating a brand new account! */
 	User(String fName, String lName, String email) 
 	{
 		this.fName = fName;
 		this.lName = lName;
 		this.email = email;
 		this.friends = new ArrayList<Friend>();
 		this.notifications = new ArrayList<Notification>();
 		this.transactions = new ArrayList<ResolveTransaction>();
 		this.transAsLend = new ArrayList<BaseTransaction>();
 		this.transAsBorrow = new ArrayList<BaseTransaction>();
 	}
 	User(String email, String password)
 	{
 		this.email = email;
 		this.password = password;
 		this.friends = new ArrayList<Friend>();
 		this.notifications = new ArrayList<Notification>();
 		this.transactions = new ArrayList<ResolveTransaction>();
 		this.transAsLend = new ArrayList<BaseTransaction>();
 		this.transAsBorrow = new ArrayList<BaseTransaction>();
 	}
 	static void setTransLendList(JSONObject obj, String uEmail) throws JSONException{
 		JSONArray arr = obj.getJSONArray("transactions");
 		for(int i = 0; i < arr.length(); i++){
 			BaseTransaction trans = new BaseTransaction();
 			trans.setLenderEmail(uEmail);
 			trans.setBorrowerEmail(arr.getJSONObject(i).get("Email").toString());
 			trans.setAmount(Double.parseDouble(arr.getJSONObject(i).get("Amount").toString()));
 			if(arr.getJSONObject(i).has("Comment"))
 				trans.setComment(arr.getJSONObject(i).get("Comment").toString());
 			else
 				trans.setComment("No Comment");
 			trans.setResolved(arr.getJSONObject(i).getBoolean("ResolvedFlag"));
 		}
 	}
 	public ArrayList<BaseTransaction> getTransLend(){
 		return transAsLend;
 	}
 	static void setTransBorrowList(JSONObject obj, String uEmail) throws JSONException{
 		JSONArray arr = obj.getJSONArray("transactions");
 		for(int i = 0; i < arr.length(); i++){
 			BaseTransaction trans = new BaseTransaction();
 			trans.setLenderEmail(arr.getJSONObject(i).get("Email").toString());
 			trans.setBorrowerEmail(uEmail);
 			trans.setAmount(Double.parseDouble(arr.getJSONObject(i).get("Amount").toString()));
 			trans.setComment(arr.getJSONObject(i).get("Description").toString());
 			trans.setResolved(arr.getJSONObject(i).getBoolean("ResolvedFlag"));
 		}
 	}
 	public ArrayList<BaseTransaction> getTransBorrow(){
 		return transAsBorrow;
 	}	
 	public void setFriends(ArrayList<Friend> friends) {
 		this.friends = friends;
 	}
 	public ArrayList<Friend> getFriends() {
 		return friends;
 	}	
 	public void setNotifications(ArrayList<Notification> notifications) {
 		this.notifications = notifications;
 	}
 	public ArrayList<Notification> getNotifications() {
 		return notifications;
 	}
 	public void setTransactions(ArrayList<ResolveTransaction> transactions) {
 		this.transactions = transactions;
 	}
 	public ArrayList<ResolveTransaction> getTransactions() {
 		return transactions;
 	}
 	public ArrayList<Friend> parseFriends(JSONObject friends){
 		ArrayList<Friend> list = new ArrayList<Friend>();	
 		try {
 			JSONArray array = friends.getJSONArray("friendOfMatches");
 			for(int i = 0; i < array.length(); i++){
 				JSONObject obj = array.getJSONObject(i);
 				Friend f = new Friend();
 				f.setfName(obj.getString("Fname"));
 				f.setlName(obj.getString("Lname"));
 				f.setEmail(obj.getString("Email"));
 				list.add(f);
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return list;
 	}
 
 	public String getPassword(){
 		return password;
 	}
 	public void setPassword(String password) {
 		this.password = password;
 	}
 	
 	public void setnoneselected(){
 		for(int i = 0; i < friends.size(); i++){
 			friends.get(i).setSelected(false);
 		}
 	}
 	public ArrayList<Notification> parseNotifs(JSONObject obj, String email) {
 		ArrayList<Notification> list = new ArrayList<Notification>();	
 		try {
 			JSONArray array = obj.getJSONArray("notifications");
 			for(int i = 0; i < array.length(); i++){
 				Notification n = new Notification();
 				n.setMessage(array.getJSONObject(i).getString("comment"));
 				n.setToEmail(email);
 				n.setFromEmail(array.getJSONObject(i).getString("email"));
 				n.setDate(array.getJSONObject(i).getString("date"));
 				list.add(n);
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return list;
 	}	
 		
 }
 class Friend extends Account implements Parcelable {
 	boolean selected;
 	int amounttosel;
 	
 	//default constructor
 	Friend(){
 		this.fName = null;
 		this.lName = null;
 		this.email = null;
 		this.selected = false;
 		this.amounttosel = 0;
 	}
 	
 	Friend(String fName, String lName, String email){
 		this.fName = fName;
 		this.lName = lName;
 		this.email = email;
 		this.displayName = fName + " " + lName;
 		this.selected = false;
 		
 	}
 	
 	Friend(String fName, String lName, String email, String displayName){
 		this.fName = fName;
 		this.lName = lName;
 		this.email = email;
 		this.displayName = displayName;
 		this.selected = false;
 
 	}
 	
 	//blank friend. FOR TESTING ONLY
 	Friend(String fName, String lName){
 		this.fName = fName;
 		this.lName = lName;
 		this.email = "warandpeace@lotr.pre";
 		this.selected = false;
 		this.amounttosel = 0;
 
 	}
 	
 	//Getters and Setters
 	public boolean isSelected() {
 		return selected;
 	}
 
 	public void setSelected(boolean selected) {
 		this.selected = selected;
 	}
 	
 	public int getamounttosel() {
 	    return amounttosel;
 	}
 
 	public void setamounttosel(int amounttosel) {
 		this.amounttosel = amounttosel;
 	}
 	/*
 	static ArrayList<Friend> updateFriends(String email) // Used for existing users
 	{
 	    Friend test1 = new Friend("Price", "Gutierrez");
 	    Friend test2 = new Friend("Vanna", "Mccullough");
 	    Friend test3 = new Friend("Wyatt", "Paul");
 	    Friend test4 = new Friend("Thaddeus", "Robbins");
 	    Friend test5 = new Friend("Rooney", "Dejesus");
 	    Friend test6 = new Friend("Xavier", "Wolfe");
 	    Friend test7 = new Friend("Byron", "Raymond");
 	    Friend test8 = new Friend("Quinn", "Whitfield");
 	    Friend test9 = new Friend("Farrah", "Moon");
 	    Friend test10 = new Friend("Ainsley", "Whitehead");
 	    Friend test11 = new Friend("Josephine", "Patton");
 	    Friend test12 = new Friend("Mariko", "Patton");
 	    Friend test13 = new Friend("Raphael", "Fitzgerald");
 	    Friend test14 = new Friend("Deacon", "Daniels");
 	    Friend test15 = new Friend("Delilah", "Fletcher");
 	    Friend test16 = new Friend("Robin", "Andrews");
 	    Friend test17 = new Friend("Melvin", "Price");
 		
 		ArrayList<Friend> f = new ArrayList<Friend>();
 		
 		f.add(test1);
 		f.add(test2);
 		f.add(test3);
 		f.add(test4);
 		f.add(test5);
 		f.add(test6);
 		f.add(test7);
 		f.add(test8);
 		f.add(test9);
 		f.add(test10);
 		f.add(test11);
 		f.add(test12);
 		f.add(test13);
 		f.add(test14);
 		f.add(test15);
 		f.add(test16);
 		f.add(test17);
  
 		return f;
 	}
 	  */
 	//Methods
 
 	public String extractEmail(String friend){
 		int startID = friend.indexOf("(");
 		int endID = friend.indexOf(")");
 		return friend.substring(startID+1, endID);
 	}
 	
 	public String toString() {
 		return  getfName() + " " + getlName() + " (" + getEmail() + ")";
 	}
 
 	public String getDisplayName(){
 		return displayName;
 	}
 
 	@Override
 	public int describeContents() {
 		return 0;
 	}
 
 	@Override
 	public void writeToParcel(Parcel dest, int flags) {
 		dest.writeString(fName);
 		dest.writeString(lName);
 		dest.writeString(email);
 	
 	}
 	
 	public Friend(Parcel source){
 		 this.fName = source.readString();
 		 this.lName = source.readString();
 		 this.email = source.readString();
 	}
 	
     public static final Parcelable.Creator<Friend> CREATOR = new Parcelable.Creator<Friend>() {
     	 
         @Override
         public Friend createFromParcel(Parcel source) {
             return new Friend(source);
         }
  
         @Override
         public Friend[] newArray(int size) {
             return new Friend[size];
         }
     };
 }
