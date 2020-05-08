 package org.sakaiproject.sdata.services.connections;
 
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
 import org.sakaiproject.sdata.services.profile.ProfileSqlreader2;
 import org.sakaiproject.sdata.services.profile.ProfileSqlresult;
 import org.sakaiproject.sdata.tool.api.ServiceDefinition;
 import org.sakaiproject.tool.api.SessionManager;
 import org.sakaiproject.user.api.User;
 import org.sakaiproject.user.api.UserDirectoryService;
 import org.sakaiproject.user.api.UserNotDefinedException;
 
 /**
  * A service definition bean for recent changes that the current user can see.
  * 
  * @author
  */
 public class ConnectionBean implements ServiceDefinition
 {
 
 	private static final Log log = LogFactory.getLog(ConnectionBean.class);
 
 	private SqlService sqlService = Kernel.sqlService();
 
 	Map<String, Object> resultMap = new HashMap<String, Object>();
 
 	private SessionManager sessionManager = Kernel.sessionManager();
 	
 	private UserDirectoryService userDirectoryService = Kernel.userDirectoryService();
 
 	/**
 	 * Create a recent changes bean with the number of pages.
 	 * 
 	 * @param paging
 	 */
 	public ConnectionBean(HttpServletRequest request, HttpServletResponse response)
 	{
 		try {
 			if (request.getMethod().equalsIgnoreCase("get")){
 				if (request.getParameter("check") != null){
 					doCheck(request, response);
 				} else {
 					doList(request, response);
 				}
 			} else if (request.getMethod().equalsIgnoreCase("post")){
 				if (request.getParameter("accept") != null){
 					doAccept(request, response);
 				} else {
 					doAddRequest(request, response);
 				}
 			} else if (request.getMethod().equalsIgnoreCase("delete")){
 				
 			} 
 		} catch (Exception ex){
 			try {
 				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 				ex.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}  
 	
 	private void doCheck(HttpServletRequest request, HttpServletResponse response) {
 		
 		String toLookFor = request.getParameter("user");
 		
 		User usr;
 		try {
 			usr = userDirectoryService.getUser(toLookFor);
 		} catch (UserNotDefinedException e) {
 			try {
 				response.sendError(HttpServletResponse.SC_NOT_FOUND);
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			}
 		}
 		
 		if (toLookFor.equalsIgnoreCase(sessionManager.getCurrentSessionUserId())){
 			resultMap.put("status", "current");
 		} else {
 			
 			// Check whether he is already a connection
 			
 			Object[] params = new Object[5];
 			params[0] = sessionManager.getCurrentSessionUserId();
 			params[1] = toLookFor;
 			params[2] = toLookFor;
 			params[3] = sessionManager.getCurrentSessionUserId();
 			params[4] = true;
 			
 			List <ConnectionSqlresult> lst = (List<ConnectionSqlresult>) sqlService.dbRead("SELECT * FROM sdata_connections WHERE ((inviter = ? AND receiver = ?) OR (inviter = ? AND receiver = ?)) AND accepted = ?", params, new ConnectionSqlreader());
 			
 			if (lst.size() > 0){
 				resultMap.put("status", "connection");
 				return;
 			}
 			
 			// Check whether I have invited him
 			
 			Object[] params2 = new Object[3];
 			params2[0] = sessionManager.getCurrentSessionUserId();
 			params2[1] = toLookFor;
 			params2[2] = false;
 			
 			List <ConnectionSqlresult> lst2 = (List<ConnectionSqlresult>) sqlService.dbRead("SELECT * FROM sdata_connections WHERE ((inviter = ? AND receiver = ?)) AND accepted = ?", params2, new ConnectionSqlreader());
 			
 			if (lst2.size() > 0){
 				resultMap.put("status", "pending");
 				return;
 			}
 			
 			// Check whether I am invited by him
 			
 			Object[] params3 = new Object[3];
 			params3[0] = toLookFor;
 			params3[1] = sessionManager.getCurrentSessionUserId();
 			params3[2] = false;
 			
 			List <ConnectionSqlresult> lst3 = (List<ConnectionSqlresult>) sqlService.dbRead("SELECT * FROM sdata_connections WHERE ((inviter = ? AND receiver = ?)) AND accepted = ?", params3, new ConnectionSqlreader());
 			
 			if (lst3.size() > 0){
 				resultMap.put("status", "invited");
 				return;
 			}
 			
 			resultMap.put("status", "");
 			
 		}
 		
 	}
 
 	private void doAddRequest(HttpServletRequest request, HttpServletResponse response) {
 		
 		String inviter = sessionManager.getCurrentSessionUserId();
 		String receiver = request.getParameter("receiver");
 		int connectionType = Integer.parseInt(request.getParameter("connectionType"));
 		
 		boolean exists = false;
 		
 		Object[] params = new Object[2];
 		params[0] = inviter;
 		params[1] = receiver;
 		
 		List <ConnectionSqlresult> lst = (List<ConnectionSqlresult>) sqlService.dbRead("SELECT * FROM sdata_connections WHERE inviter = ? AND receiver = ?", params, new ConnectionSqlreader());
 		
 		if (lst.size() > 0){
 			exists = true;
 		} 
 		
 		// Try the other way around => Become friends immediately
 		
 		params = new Object[2];
 		params[1] = inviter;
 		params[0] = receiver;
 		
 		List <ConnectionSqlresult> lst2 = (List<ConnectionSqlresult>) sqlService.dbRead("SELECT * FROM sdata_connections WHERE receiver = ? AND inviter = ?", params, new ConnectionSqlreader());
 		
 		if (lst2.size() > 0){
 			
 			exists = true;
 			
 			params = new Object[3];
 			params[0] = true;
 			params[1] = receiver;
 			params[2] = inviter;
 			
 			sqlService.dbWrite("UPDATE sdata_connections SET accepted = ? WHERE receiver = ? AND inviter = ?", params);
 			
 		} 
 		
 		if (!exists){
 			
 			Object[] params2 = new Object[4];
 			params2[0] = inviter;
 			params2[1] = receiver;
 			params2[2] = connectionType;
 			params2[3] = false;
 			
 			sqlService.dbWrite("INSERT INTO sdata_connections (inviter, receiver, connectiontype, accepted) VALUES (?,?,?,?)", params2);
 			
 		}
 		
 	}
 
 	private void doAccept(HttpServletRequest request, HttpServletResponse response) {
 		
 		String inviter = request.getParameter("inviter");
 		String receiver = sessionManager.getCurrentSessionUserId();
 		
 		Object[] params = new Object[3];
 		params[0] = true;
 		params[2] = inviter;
 		params[1] = receiver;
 		
 		sqlService.dbWrite("UPDATE sdata_connections SET accepted = ? WHERE receiver = ? AND inviter = ?", params);
 		
 	}
 
 	private void doList(HttpServletRequest request, HttpServletResponse response) {
 		
 		// Gets all , accepted , pending
 		
 		String toShow = "all";
 		List <ConnectionSqlresult> lst2 = null;
 		ArrayList <ConnectionSqlresult> lst3 = new ArrayList<ConnectionSqlresult>();
 		
 		int page = 1;
 		if (request.getParameter("page") != null){
 			page = Integer.parseInt(request.getParameter("page"));
 		}
 		int count = 10;
 		if (request.getParameter("count") != null){
 			if (request.getParameter("count").equalsIgnoreCase("all")){
 				count = -1;
 			} else {
 				count = Integer.parseInt(request.getParameter("count"));
 			}
 		}
 		
 		if (request.getParameter("show") != null){
 			toShow = request.getParameter("show");
 		}
 		
 		if (toShow.equalsIgnoreCase("all")){
 			
 			Object[] params = new Object[2];
 			params[0] = sessionManager.getCurrentSessionUserId();
 			params[1] = sessionManager.getCurrentSessionUserId();
 			
 			lst2 = (List<ConnectionSqlresult>) sqlService.dbRead("SELECT * FROM sdata_connections WHERE receiver = ? OR inviter = ?", params, new ConnectionSqlreader());
 			
 		
 		} else if (toShow.equalsIgnoreCase("accepted")){
 			
 			Object[] params = new Object[3];
 			params[0] = sessionManager.getCurrentSessionUserId();
 			params[1] = sessionManager.getCurrentSessionUserId();
 			params[2] = true;
 			
 			lst2 = (List<ConnectionSqlresult>) sqlService.dbRead("SELECT * FROM sdata_connections WHERE (receiver = ? OR inviter = ?) AND accepted = ?", params, new ConnectionSqlreader());
 			
 			
 		} else if (toShow.equalsIgnoreCase("pending")){
 			
 			Object[] params = new Object[2];
 			params[0] = sessionManager.getCurrentSessionUserId();
 			params[1] = false;
 			
 			lst2 = (List<ConnectionSqlresult>) sqlService.dbRead("SELECT * FROM sdata_connections WHERE (inviter = ?) AND accepted = ?", params, new ConnectionSqlreader());
 
 		} else if (toShow.equalsIgnoreCase("waiting")){
 			
 			Object[] params = new Object[2];
 			params[0] = sessionManager.getCurrentSessionUserId();
 			params[1] = false;
 			
 			lst2 = (List<ConnectionSqlresult>) sqlService.dbRead("SELECT * FROM sdata_connections WHERE (receiver = ?) AND accepted = ?", params, new ConnectionSqlreader());
 
 		}
 		
 		// do paging
 		
 		resultMap.put("total", lst2.size());
 		
 		if (count == -1){
 			for (int i = 0; i < lst2.size(); i++){
 				try {
 					lst3.add(lst2.get(i));
 				} catch (Exception ex){
 					// Continue
 				}
 			}
 		} else {
 			for (int i = (page - 1) * count; i < page * count; i++){
 				try {
 					lst3.add(lst2.get(i));
 				} catch (Exception ex){
 					// Continue
 				}
 			}
 		}
 			
 		String sqlString = "";
 		Object[] params2 = new Object[lst3.size()];
 		
 		boolean started = false;
 		for (int i = 0; i < lst3.size(); i++){
 			ConnectionSqlresult res = lst3.get(i);
 			if (started){
 				sqlString += " OR";
 			} else {
 				started = true;
 			}
 			if (res.getInviter().equals(sessionManager.getCurrentSessionUserId())){
 				sqlString += " new.USER_ID = ?";
 				params2[i] = res.getReceiver();
 			} else {
 				sqlString += " new.USER_ID = ?";
 				params2[i] = res.getInviter();
 			}
 		}
 	
		List<ProfileSqlresult> lst = sqlService.dbRead("SELECT * FROM (SELECT *  FROM SAKAI_USER  LEFT OUTER JOIN sdata_profile ON SAKAI_USER.USER_ID = sdata_profile.userid) as new WHERE " +
 				sqlString, params2, new ProfileSqlreader2());
 		
 		resultMap.put("items", lst);
 		
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
