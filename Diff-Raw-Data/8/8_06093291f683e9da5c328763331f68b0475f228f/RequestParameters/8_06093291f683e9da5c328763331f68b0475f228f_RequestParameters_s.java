 /**
  * 
  */
 package realtalk.util;
 
 /**
  * Utility class that holds Parameter Constants used for Requests and 
  * Responses from client-code.
  * 
  * @author Colin Kho
  *
  */
 public class RequestParameters {
 	// Google Cloud Messaging Registration ID
     public static final String PARAMETER_REG_ID = "PARAMETER_REG_ID";
     // User Name
     public static final String PARAMETER_USER = "PARAMETER_USER";
     // Password
     public static final String PARAMETER_PWORD = "PARAMETER_PWORD";
     // New Password - Used if user wants to change password
     public static final String PARAMETER_NEW_PWORD = "PARAMETER_NEW_PWORD";
     // New Registration ID for GCM - Used if ID is changed
     public static final String PARAMETER_NEW_REG_ID = "PARAMETER_NEW_REG_ID";
     // Parameter that indicates success - return "true" or "false" for this.
     public static final String PARAMETER_SUCCESS = "success";
     // Room Name
     public static final String PARAMETER_ROOM_NAME = "PARAMETER_ROOM_NAME";
     // Room ID
     public static final String PARAMETER_ROOM_ID = "PARAMETER_ROOM_ID";
     // Room Description
     public static final String PARAMETER_ROOM_DESCRIPTION = "PARAMETER_ROOM_DESCRIPTION";
     // Room Latitude
     public static final String PARAMETER_ROOM_LATITUDE = "PARAMETER_ROOM_LATITUDE";
     // Room Longitude
     public static final String PARAMETER_ROOM_LONGITUDE = "PARAMETER_ROOM_LONGITUDE";
     // Room list
     public static final String PARAMETER_ROOM_ROOMS = "PARAMETER_ROOM_ROOMS";
     // Message Body
     public static final String PARAMETER_MESSAGE_BODY = "PARAMETER_MESSAGE_BODY";
     // Message Sender
     public static final String PARAMETER_MESSAGE_SENDER = "PARAMETER_MESSAGE_SENDER";
     // Message List
     public static final String PARAMETER_MESSAGE_MESSAGES = "PARAMETER_MESSAGE_MESSAGES";
     // Message Timestamp
     public static final String PARAMETER_MESSAGE_TIMESTAMP = "PARAMETER_MESSAGE_TIMESTAMP";
     // Generic TimeStamp Parameter
     public static final String PARAMETER_TIMESTAMP = "PARAMETER_TIMESTAMP";
     //latitude param
    public static final String PARAMETER_USER_LONGITUDE = "PARAMETER_TIMESTAMP";
     //longitude param
    public static final String PARAMETER_USER_LATITUDE = "PARAMETER_TIMESTAMP";
     //radius param
    public static final String PARAMETER_USER_RADIUS = "PARAMETER_TIMESTAMP";
     //room creator
     public static final String PARAMETER_ROOM_CREATOR = "PARAMETER_ROOM_CREATOR";
     //num users in room
     public static final String PARAMETER_ROOM_NUM_USERS = "PARAMETER_ROOM_NUM_USERS";
     
 }
