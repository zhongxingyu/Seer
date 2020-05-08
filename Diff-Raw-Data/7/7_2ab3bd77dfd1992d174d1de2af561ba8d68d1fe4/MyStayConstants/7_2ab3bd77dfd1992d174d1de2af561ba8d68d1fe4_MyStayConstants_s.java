 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.utility;
 
 import java.util.HashMap;
 
 /**
  *
  * @author palashb
  */
 public class MyStayConstants {
 
 public static HashMap<String, String> autoReplyMsgLst = new HashMap();
 
 static{
     autoReplyMsgLst.put("hi", "Hello!");
     System.out.println("autoReplyMsgLst is iniialized.");
 }
 
 public static final String AUTO_RLY_USR="autoreply";
 
 public static final String FIRST_NAME="firstName";
 public static final String LAST_NAME="lastName";
 public static final String ROOM_NUMBER="roomNumber";
 public static final String CHECKINDATE="checkInDate";
 public static final String CHECKOUTDATE="checkOutDate";
 public static final String CONFIRMATION_ID="confirmationId";
 public static final String HOTEL_NAME="hotelName";
 public static final String EMAIL_ADDRESS="emailAddress";
 public static final String MOBILE_NUMBER="mobileNumber";
 public static final String VISIT_ID="visitId";
 public static final String PROPERTY_ID="propertyId";
 public static final String MY_CHATS="myChats";
 public static final String REWARDS_PROGRAM_ID="rewardsProgramId";
        
public static final String USER_ID="USER_ID";

public static final String PASSWORD="PASSWORD";
 
 public static final String MSG_LIST="MSG_LIST";
 
 public static final String MSG="MSG";
 
 public static final String XMPP_CONN="XMPP_CONN";
 
 public static final String CHAT_OBJ="CHAT_OBJ";
 
 public static final String REQ_MSG="REQ_MSG";
 
 public static final String USERNAME="USERNAME";
 
 public static final String LOGIN_ERROR_MSG="LOGIN_ERROR_MSG";
 
 public static final String MY_SELF = "Me";
 
 public static final String CHAT_WITH = "chatWith";
 
 public static final String ACTIVE_USERS = "ACTIVE_USERS";
 //Single User Configuration
 public static final String MY_MESSAGE_LIST = "MY_MESSAGE_LIST";
 
 
 //Chat Server Configuration
 //public static final String  CHAT_DOMAIN = "@palash-vaio";
 public static final String CHAT_DOMAIN = "@WWHQ282K";
 
 public static final String CHAT_SERVICE = "conference";
 public static final int CHAT_PORT = 5222;
 
 public static final String  CHAT_SERVER = "WWHQ282K";
 //public static final String  CHAT_SERVER = "palash-vaio";
 //public static String CHAT_SERVER = "ec2-50-17-28-118.compute-1.amazonaws.com";
 
 }
