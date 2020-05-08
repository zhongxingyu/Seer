 package org.sakaiproject.sdata.services.messages;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.Kernel;
 import org.sakaiproject.db.api.SqlService;
 import org.sakaiproject.email.cover.EmailService;
 import org.sakaiproject.sdata.services.connections.ConnectionSqlreader;
 import org.sakaiproject.sdata.services.connections.ConnectionSqlresult;
 import org.sakaiproject.sdata.services.profile.ProfileSqlreader2;
 import org.sakaiproject.sdata.services.profile.ProfileSqlresult2;
 import org.sakaiproject.sdata.tool.api.ServiceDefinition;
 import org.sakaiproject.tool.api.SessionManager;
 import org.sakaiproject.user.api.User;
 import org.sakaiproject.user.api.UserDirectoryService;
 
 /**
  * A service definition bean for recent changes that the current user can see.
  * 
  * @author
  */
 public class MessageBean implements ServiceDefinition
 {
 
 	private static final Log log = LogFactory.getLog(MessageBean.class);
 
 	private SqlService sqlService = Kernel.sqlService();
 
 	Map<String, Object> resultMap = new HashMap<String, Object>();
 
 	private SessionManager sessionManager = Kernel.sessionManager();
 	
 	private UserDirectoryService userDirectoryService = Kernel.userDirectoryService();
 
 	/**
 	 * Create a recent changes bean with the number of pages.
 	 * 
 	 * @param paging
 	 */
 	public MessageBean(HttpServletRequest request, HttpServletResponse response)
 	{
 		try {
 			if (request.getMethod().equalsIgnoreCase("get")){
 				if (request.getParameter("list") != null){
 					doList(request, response);
 				} else if (request.getParameter("id") != null){
 					doMessage(request, response);
 				} else if (request.getParameter("unreadCount") != null){
 					doUnreadCount(request, response);
 				}
 			} else if (request.getMethod().equalsIgnoreCase("post")){
 				if (request.getParameter("delete") != null){
 					doDelete(request, response);
 				} else {
 					doPost(request, response);
 				}
 			} 
 		} catch (Exception ex){
 			try {
 				ex.printStackTrace();
 				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}  
 
 	private void doUnreadCount(HttpServletRequest request, HttpServletResponse response) {
 		
 		Object[] params = new Object[2];
 		params[0] = sessionManager.getCurrentSessionUserId();
 		params[1] = false;
 		
 		List <MessageSqlresult> lst = (List<MessageSqlresult>) sqlService.dbRead("SELECT * FROM sdata_messages WHERE receiver = ? AND isread = ?", params, new MessageSqlreader());
 		
 		resultMap.put("count", lst.size());
 		
 	}
 
 	private void doDelete(HttpServletRequest request, HttpServletResponse response) {
 		
 		try {
 			
 			int id = Integer.parseInt(request.getParameter("id"));
 			
 			Object[] params = new Object[1];
 			params[0] = id;
 			sqlService.dbWrite("DELETE FROM sdata_messages WHERE id = ?", params);
 			
 			resultMap.put("status", "success");
 		
 		} catch (Exception ex){
 			try {
 				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		
 	}
 
 	private void doMessage(HttpServletRequest request, HttpServletResponse response) {
 		
 		int id = Integer.parseInt(request.getParameter("id"));
 		
 		Object[] params = new Object[1];
 		params[0] = id;
 		List <MessageSqlresult> lst = (List<MessageSqlresult>) sqlService.dbRead("SELECT * FROM sdata_messages WHERE id = ?", params, new MessageSqlreader());
 		
 		try {
 			
 			MessageSqlresult res = lst.get(0);
 			
 			Object[] params3 = new Object[1];
 			params3[0] = res.getSender();
 			List <ProfileSqlresult2> lst3 = sqlService.dbRead("SELECT * FROM (SELECT *  FROM SAKAI_USER  LEFT OUTER JOIN sdata_profile ON SAKAI_USER.USER_ID = sdata_profile.userid) as new WHERE new.USER_ID = ?", params3, new ProfileSqlreader2());
 			
 			res.setProfileinfo(lst3.get(0));
 			
 			resultMap.put("item", res);
 			
 			// Update read
 			
 			Object[] params2 = new Object[2];
 			params2[0] = true;
 			params2[1] = id;
 			sqlService.dbWrite("UPDATE sdata_messages SET isread = ? WHERE id = ?", params2);
 			
 			// Check whether he is already a connection
 			
 			Object[] params4 = new Object[5];
 			params4[0] = sessionManager.getCurrentSessionUserId();
 			params4[1] = res.getSender();
 			params4[2] = res.getSender();
 			params4[3] = sessionManager.getCurrentSessionUserId();
 			params4[4] = true;
 			List <ConnectionSqlresult> lst4 = (List<ConnectionSqlresult>) sqlService.dbRead("SELECT * FROM sdata_connections WHERE ((inviter = ? AND receiver = ?) OR (inviter = ? AND receiver = ?)) AND accepted = ?", params4, new ConnectionSqlreader());
 			
 			if (lst4.size() > 0){
 				resultMap.put("status", "connected");
 			} else {
 				resultMap.put("status", "");
 			}
 			
 		} catch (Exception ex){
 			try {
 				response.sendError(HttpServletResponse.SC_NOT_FOUND);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		
 	}
 
 	private void doList(HttpServletRequest request, HttpServletResponse response)
 	{
 		
 		int page = 1;
 		if (request.getParameter("page") != null){
 			page = Integer.parseInt(request.getParameter("page"));
 		}
 		
 		int items = 10;
 		if (request.getParameter("items") != null){
 			items = Integer.parseInt(request.getParameter("items"));
 		}
 		
 		Object[] params = new Object[1];
 		params[0] = sessionManager.getCurrentSessionUserId();
 		List <MessageSqlresult> lst = sqlService.dbRead("SELECT * FROM sdata_messages WHERE receiver = ? ORDER BY datetime DESC", params, new MessageSqlreader());
 		ArrayList <MessageSqlresult> lst2 = new ArrayList<MessageSqlresult>();
 		
 		for (int i = (page - 1) * items ; i < page * items; i++){
 			try {
 				lst2.add(lst.get(i));
 			} catch (Exception ex){
 				// Ignore, continue
 			}
 		}
 		
 		ArrayList <String> usersToLookup = new ArrayList<String>();
 		ArrayList <User> lookedUpUsers = new ArrayList<User>();
 		
 		//Look for unique users in the list
 		
 		for (int i = 0; i < lst2.size(); i++){
 			if (! usersToLookup.contains(lst2.get(i).getSender())){
 				usersToLookup.add(lst.get(i).getSender());
 			}
 		}
 		
 		if (lst2.size() > 0){
 		
 			Object[] params2 = new Object[usersToLookup.size()];
 			String sql = "";
 			for (int i = 0; i < usersToLookup.size(); i++){
 				if (i != 0){
 					sql += " OR";
 				}
 				sql += " new.USER_ID = ?";
 				params2[i] = usersToLookup.get(i);
 			}
 			
 			
 			List <ProfileSqlresult2> lst3 = sqlService.dbRead("SELECT * FROM (SELECT *  FROM SAKAI_USER  LEFT OUTER JOIN sdata_profile ON SAKAI_USER.USER_ID = sdata_profile.userid) as new WHERE" + sql, params2, new ProfileSqlreader2());
 			
 			for (int i = 0; i < lst2.size(); i++){
 				MessageSqlresult res = lst2.get(i);
 				for (int ii = 0; ii < lst3.size(); ii++){
 					if (lst3.get(ii).getUserid().equalsIgnoreCase(res.getSender())){
 						lst2.get(i).setProfileinfo(lst3.get(ii));
 					}
 				}
 			}
 			
 		}
 		
 		resultMap.put("items", lst2);
 		resultMap.put("total", lst.size());
 		
 	}
 	
 	private void doPost (HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		
 		String sender = sessionManager.getCurrentSessionUserId();
 		String title = request.getParameter("title");
 		String message = request.getParameter("message");
 		String receiver = request.getParameter("receiver");
 		boolean isinvite = false;
 		if (request.getParameter("isinvite").equalsIgnoreCase("true")){
 			isinvite = true;
 		}
 		Date d = new Date();
 		
 		User fromuser = userDirectoryService.getUser(sender);
 		User touser = userDirectoryService.getUser(receiver);
 		
 		Object[] params = new Object[7];
 		params[0] = sender;
 		params[1] = receiver;
 		params[2] = title;
 		params[3] = message;
 		params[4] = isinvite;
 		params[5] = false;
		params[6] = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
 		
 		sqlService.dbWrite("INSERT INTO sdata_messages (sender, receiver, title, message, isinvite, isread, datetime) VALUES (?, ?, ?, ?, ?, ?, ?)", params);
 		
 		// Send emails
 		
 		/*
 		if (! isinvite){
 			
 			EmailService.send(fromuser.getEmail(), touser.getEmail(), "You have received a connection request on Sakai 3", "Some text and a link to your invitation", null, null, null);
 			
 		} else {
 			
 			EmailService.send(fromuser.getEmail(), touser.getEmail(), "You have received a message on Sakai 3", "Some text, the message and a link to it", null, null, null);
 			
 		}
 		*/
 		
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.sakaiproject.sdata.tool.api.ServiceDefinition#getResponseMap()
 	 */
 	public Map<String, Object> getResponseMap()
 	{
 		return resultMap;
 	}
 
 }
