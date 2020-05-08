 package realtalk.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 
 public class ChatManager {
 	
 	public static final String url_qualifier = "http://realtalkserver.herokuapp.com/";
 	
 	//User servlets
     public static final String url_add_user = url_qualifier+"register";
     public static final String url_remove_user = url_qualifier+"unregister";
     public static final String url_authenticate = url_qualifier+"authenticate";
     public static final String url_change_password = url_qualifier+"changePwd";
     public static final String url_change_id = url_qualifier+"changeRegId";
     //Chat room servlets
     public static final String url_add_room = url_qualifier+"addRoom";
     public static final String url_join_room = url_qualifier+"joinRoom";
     public static final String url_leave_room = url_qualifier+"leaveRoom";
     public static final String url_post_message = url_qualifier+"post";
     public static final String url_get_messages = url_qualifier+"pullRecentChat";
     
     
     private static List<NameValuePair> rgparamsMessageInfo(MessageInfo message) {
         List<NameValuePair> rgparams = new ArrayList<NameValuePair>();
         rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_MESSAGE_TIMESTAMP, Long.valueOf(message.getTimeStamp().getTime()).toString()));
         rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_MESSAGE_BODY, message.getBody()));
         rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_MESSAGE_SENDER, message.getSender()));
         return rgparams;
     }
     
     private static List<NameValuePair> rgparamsUserBasicInfo(User user) {
         List<NameValuePair> rgparams = new ArrayList<NameValuePair>();
         rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_REG_ID, user.getId()));
         rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_USER, user.getUsername()));
         rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_PWORD, user.getPassword()));
         return rgparams;
     }
     
     private static List<NameValuePair> rgparamsChatRoomBasicInfo(ChatRoomInfo chatroominfo) {
         List<NameValuePair> rgparams = new ArrayList<NameValuePair>();
         rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_ROOM_NAME, chatroominfo.getName()));
         rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_ROOM_ID, chatroominfo.getId()));
         return rgparams;
     }
     
     private static RequestResultSet makePostRequest(List<NameValuePair> rgparams, String url) {
     	JSONObject json = null;
     	JSONParser jsonParser = new JSONParser();
 		json = jsonParser.makeHttpRequest(url, "POST", rgparams);
         try {
         	boolean fSucceeded = json.getString(RequestParameters.PARAMETER_SUCCESS).equals("true");
        	String stErrorCode = fSucceeded ? "NO ERROR MESSAGE" : json.getString(ResponseParameters.PARAMETER_ERROR_CODE);
        	String stErrorMessage = fSucceeded ? "NO ERROR MESSAGE" : json.getString(ResponseParameters.PARAMETER_ERROR_MSG);
            return new RequestResultSet(fSucceeded, stErrorCode, stErrorMessage);
         } catch (JSONException e) {
             e.printStackTrace();
         }
     	return null;
     }
     
     private static PullMessageResultSet rgmessagePostRequest(List<NameValuePair> rgparams, String url) {
     	JSONObject json = null;
     	JSONParser jsonParser = new JSONParser();
 		json = jsonParser.makeHttpRequest(url, "POST", rgparams);
         try {
         	boolean fSucceeded = json.getString(RequestParameters.PARAMETER_SUCCESS).equals("true");
         	if (fSucceeded) {
         		List<MessageInfo> rgmessageinfo = new ArrayList<MessageInfo>();
         		JSONArray rgmessage = json.getJSONArray(RequestParameters.PARAMETER_MESSAGE_MESSAGES);
         		for (int i = 0; i < rgmessage.length(); i++) {
         			JSONObject jsonobject = rgmessage.getJSONObject(i);
         			String stBody = jsonobject.getString(RequestParameters.PARAMETER_MESSAGE_BODY);
         			long ticks = jsonobject.getLong(RequestParameters.PARAMETER_MESSAGE_TIMESTAMP);
         			String stSender = jsonobject.getString(RequestParameters.PARAMETER_MESSAGE_SENDER);
         			rgmessageinfo.add(new MessageInfo(stBody, stSender, ticks));
         		}
         		return new PullMessageResultSet(true, rgmessageinfo, "NO ERROR CODE", "NO ERROR MESSAGE");
         	}
             return new PullMessageResultSet(false, new ArrayList<MessageInfo>(), json.getString(ResponseParameters.PARAMETER_ERROR_CODE), json.getString(ResponseParameters.PARAMETER_ERROR_MSG));
         } catch (JSONException e) {
             e.printStackTrace();
         }
     	return null;
     }
 	
 	public static RequestResultSet authenticateUser(User user) {
         List<NameValuePair> rgparams = rgparamsUserBasicInfo(user);
         return makePostRequest(rgparams, url_authenticate);
 	}
 	
 	public static RequestResultSet addUser(User user) {
         List<NameValuePair> rgparams = rgparamsUserBasicInfo(user);
         return makePostRequest(rgparams, url_add_user);
 	}
 	
 	public static RequestResultSet removeUser(User user) {
         List<NameValuePair> rgparams = rgparamsUserBasicInfo(user);
         return makePostRequest(rgparams, url_remove_user);
 	}
 	
 	public static RequestResultSet changePassword(User user, String stPasswordNew) {
         List<NameValuePair> rgparams = rgparamsUserBasicInfo(user);
         rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_NEW_PWORD, stPasswordNew));
         return makePostRequest(rgparams, url_change_password);
 	}
 	
 	public static RequestResultSet changeID(User user, String stIdNew) {
         List<NameValuePair> rgparams = rgparamsUserBasicInfo(user);
         rgparams.add(new BasicNameValuePair(RequestParameters.PARAMETER_NEW_REG_ID, stIdNew));
         return makePostRequest(rgparams, url_change_id);
 	}
 	
 	public static RequestResultSet addRoom(ChatRoomInfo chatroominfo) {
         List<NameValuePair> rgparams = rgparamsChatRoomBasicInfo(chatroominfo);
 		return makePostRequest(rgparams, url_add_room);
 	}
 	
 	public static RequestResultSet joinRoom(User user, ChatRoomInfo chatroominfo) {
         List<NameValuePair> rgparams = rgparamsUserBasicInfo(user);
         rgparams.addAll(rgparamsChatRoomBasicInfo(chatroominfo));
 		return makePostRequest(rgparams, url_join_room);
 	}
 	
 	public static RequestResultSet leaveRoom(User user, ChatRoomInfo chatroominfo) {
         List<NameValuePair> rgparams = rgparamsUserBasicInfo(user);
         rgparams.addAll(rgparamsChatRoomBasicInfo(chatroominfo));
 		return makePostRequest(rgparams, url_leave_room);
 	}
 	
 	public static RequestResultSet postMessage(User user, ChatRoomInfo chatroominfo, MessageInfo message) {
         List<NameValuePair> rgparams = rgparamsUserBasicInfo(user);
         rgparams.addAll(rgparamsChatRoomBasicInfo(chatroominfo));
         rgparams.addAll(rgparamsMessageInfo(message));
 		return makePostRequest(rgparams, url_post_message);
 	}
 	
 	public static PullMessageResultSet rgstChatLogGet(ChatRoomInfo chatroominfo) {
         List<NameValuePair> rgparams = rgparamsChatRoomBasicInfo(chatroominfo);
 		return rgmessagePostRequest(rgparams, url_add_room);
 	}
 	
 	public static RequestResultSet unregisterDevice() {
 		return null;
 	}
 	
 }
