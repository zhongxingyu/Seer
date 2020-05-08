 package com.nearme;
 
 import java.sql.SQLException;
 import java.util.List;
 
 public class User {
 	private int id = NO_ID;			/* Unique ID for this record, used as database key */
 	private String deviceId;		/* Unique ID of their Android device */
 	private String msisdnHash;		/* Hash of their MSISDN, used for identity purposes */
 	
 	private Position lastPosition;	/* Last reported position */
 	private UserDAO finder;			/* The UserFinder that found this user */
 
 	public static final int NO_ID = -1;
 
 	public User(int i, String d, String m) {
 		this.id = i;
 		this.deviceId = d;
 		this.msisdnHash = m;
 	}
 	
 	public User(int i, String d, String m, Position p) {
		this(i,d,m);
 		this.lastPosition = p;
 	}
 
 	public User(int i, String d, String m, Position p, UserDAO f) {
		this(i,d,m,p);
 		this.finder = f;
 	}
 	
 	public User() {
 		// TODO Auto-generated constructor stub
 	}
 
 	public List<AddressBookEntry> getAddressBook() throws SQLException {
 		if (finder==null) return null;
 		return finder.getAddressBook(this.id);
 	}
 	
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public String getDeviceId() {
 		return deviceId;
 	}
 
 	public void setDeviceId(String deviceId) {
 		this.deviceId = deviceId;
 	}
 
 	public String getMsisdnHash() {
 		return msisdnHash;
 	}
 
 	public void setMsisdnHash(String msisdnHash) {
 		this.msisdnHash = msisdnHash;
 	}
 
 	public Position getLastPosition() {
 		return lastPosition;
 	}
 
 	public void setLastPosition(Position lastPosition) {
 		this.lastPosition = lastPosition;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((deviceId == null) ? 0 : deviceId.hashCode());
 		result = prime * result + id;
 		result = prime * result
 				+ ((lastPosition == null) ? 0 : lastPosition.hashCode());
 		result = prime * result
 				+ ((msisdnHash == null) ? 0 : msisdnHash.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		User other = (User) obj;
 		if (deviceId == null) {
 			if (other.deviceId != null)
 				return false;
 		} else if (!deviceId.equals(other.deviceId))
 			return false;
 		if (id != other.id)
 			return false;
 		if (lastPosition == null) {
 			if (other.lastPosition != null)
 				return false;
 		} else if (!lastPosition.equals(other.lastPosition))
 			return false;
 		if (msisdnHash == null) {
 			if (other.msisdnHash != null)
 				return false;
 		} else if (!msisdnHash.equals(other.msisdnHash))
 			return false;
 		return true;
 	}
 
 }
