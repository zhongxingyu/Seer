 package realtalk.model;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import realtalk.util.MessageInfo;
 
 /**
  * This is the model class for the Chat Rooms.
  * 
  * @author Cory Shiraishi
  *
  */
 public class ChatRoomModel {
 		
 	/*
 	 * representation invariant: 
 	 * Should stay ordered such that more recent messages are at the end of the list.
 	 */
 	private List<MessageInfo> rgmi;
 	
 //	Unused, but keep this around in case we switch to exlicit users rather than # of users
 //	private Set<UserInfo> rgu;
 	
     private String stName;
     private String stId;
     private String stDescription;
     private double latitude;
     private double longitude;
     private String stCreator;
     private int iUsers;
     private Timestamp timestampCreated;
 	
     /**
      * Constructor for ChatRoomModel
      * 
      * @param stName room name
      * @param stId room id
      * @param stDescription room description
      * @param latitude room latitude
      * @param longitude room longitude
      * @param stCreator name of the creator of the room
      * @param timestampCreated timestamp of when the room was created
      */
 	public ChatRoomModel(String stName, String stId, String stDescription, double latitude,
             double longitude, String stCreator, int iUsers, Timestamp timestampCreated) {
         this.stName = stName;
         this.stId = stId;
         this.stDescription = stDescription;
         this.latitude = latitude;
         this.longitude = longitude;
         this.stCreator = stCreator;
         this.setNumUsers(iUsers);
         this.timestampCreated = new Timestamp(timestampCreated.getTime());
 				
 		rgmi = new ArrayList<MessageInfo>();
 //		Unused, but keep this around in case we switch to explicit users rather than # of users
 //		rgu = new HashSet<UserInfo>();
 	}
 	
 	/**
 	 * Adds a message to the chat room
 	 * 
 	 * @param mi information for the message being sent.
 	 */
 	public void addMi(MessageInfo mi) {
 		//If we ever decide to limit the number of messages being cached for each room, 
 		// this is where we should implement the logic of that.
 				
 		//Preserves rgmi[k].timestamp before rgmi[k].timestamp invariant
 		//It assumes existing messages are already sorted, and then basically
 		//does Insertion Sort
 		for (int i = rgmi.size()-1; i >= 0; i--) {
 			
 			//If mi is later than or at the same time as rgmi[i]
			MessageInfo miOther = rgmi.get(i);
			if (mi.compareTo(miOther) > 0) {
 				rgmi.add(i+1, mi);
 				return;
			} else if (mi.compareTo(miOther) == 0 && mi.stBody().equals(miOther.stBody()) && mi.stSender().equals(miOther.stSender())) {
				//It's a duplicate, so ignore it and return now
				return;
 			}
 		}
 		//If we get to this point, the mi hasn't been added yet and it is the earliest mi out of all of them
 		rgmi.add(0,mi);
 	}
 	
 //	Unused, but keep this around in case we switch to exlicit users rather than # of users
 //	/**
 //	 * Adds a user to the room
 //	 * 
 //	 * @param u info for the user being added
 //	 * @return whether or not the add was successful (will return false if the user is already in the room)
 //	 */
 //	public boolean fAddU(UserInfo u) {
 //		return rgu.add(u);
 //	}
 	
 //	Unused, but keep this around in case we switch to exlicit users rather than # of users
 //	/**
 //	 * Removes a user from the room
 //	 * 
 //	 * @param u info for the user being removed
 //	 * @return whether or not the remove was successful (will return false if the user was not in the room)
 //	 */
 //	public boolean fRemoveU(UserInfo u) {
 //		return rgu.remove(u);
 //	}
 	
 	/**
 	 * Gets an unmodifiable list of the messages in the room
 	 * 
 	 * @return an unmodifiable list of the messages
 	 */
 	public List<MessageInfo> rgmiGetMessages() {
 		return Collections.unmodifiableList(rgmi);
 	}
 	
 	   /**
      * @return the name
      */
     public String stName() {
         return stName;
     }
 
     /**
      * @return the id
      */
     public String stId() {
         return stId;
     }
 
     /**
      * @return the description
      */
     public String stDescription() {
     	return stDescription;
     }
 
     /**
      * @return the latitude
      */
     public double getLatitude() {
     	return latitude;
     }
 
     /**
      * @return the longitude
      */
     public double getLongitude() {
     	return longitude;
     }
 
     /**
      * @return the creator
      */
     public String stCreator() {
         return stCreator;
     }
 
 //	Unused, but keep this around in case we switch to exlicit users rather than # of users
 //    /**
 //     * @return the count of how many users are in the room
 //     */
 //    public int cu() {
 //    	return rgu.size();
 //    }
 
 	/**
 	 * @return the timeStampCreated
 	 */
 	public Timestamp timestampCreated() {
 		return new Timestamp(timestampCreated.getTime());
 	}
 
 //	Unused, but keep this around in case we switch to explicit users rather than # of users
 //    /**
 //     * @param rgu the rgu to set
 //     */
 //    public void setUsers(Set<UserInfo> rgu) {
 //        this.rgu = new HashSet<UserInfo>(rgu);
 //    }
 
     /**
      * @param stName the stName to set
      */
     public void setName(String stName) {
         this.stName = stName;
     }
 
     /**
      * @param stId the stId to set
      */
     public void setId(String stId) {
         this.stId = stId;
     }
 
     /**
      * @param stDescription the stDescription to set
      */
     public void setDescription(String stDescription) {
         this.stDescription = stDescription;
     }
 
     /**
      * @param latitude the latitude to set
      */
     public void setLatitude(double latitude) {
         this.latitude = latitude;
     }
 
     /**
      * @param longitude the longitude to set
      */
     public void setLongitude(double longitude) {
         this.longitude = longitude;
     }
 
     /**
      * @param stCreator the stCreator to set
      */
     public void setCreator(String stCreator) {
         this.stCreator = stCreator;
     }
 
     /**
      * @param timestampCreated the timestampCreated to set
      */
     public void setTimestampCreated(Timestamp timestampCreated) {
         this.timestampCreated = new Timestamp(timestampCreated.getTime());
     }
 
 //	  This function is a duplicate of numUsersGet()
     /**
      * @return the iUsers
      */
     public int getNumUsers() {
         return iUsers;
     }
 
     /**
      * @param iUsers the iUsers to set
      */
     public void setNumUsers(int iUsers) {
         this.iUsers = iUsers;
     }
 
     /**
 	 * This is a test method to ensure that the data is being stored properly.
 	 * Will throw an IllegalStateException if something is wrong.
 	 */
 	public void fCheckRep() {
 		//checks to see if the mi are properly sorted
 		//starts at 1. If there are <2 elements, then the list is properly sorted. 
 		for (int i = 1; i < rgmi.size(); i++) {
 			//If the ith timestamp is before the preceding timestamp
 			MessageInfo miLatter = rgmi.get(i);
 			MessageInfo miFormer = rgmi.get(i-1);
 			if (miLatter.timestampGet().before(miFormer.timestampGet())) {
 				throw new IllegalStateException("The rgmi is in an invalid order. The timestamp of element " 
 												+ i + " \"" + miLatter.toString() +"\" is before "+ (i-1) + 
 												" \"" + miFormer.toString()+"\". ");
 			}
 		}
 		
 	}
 	
 }
