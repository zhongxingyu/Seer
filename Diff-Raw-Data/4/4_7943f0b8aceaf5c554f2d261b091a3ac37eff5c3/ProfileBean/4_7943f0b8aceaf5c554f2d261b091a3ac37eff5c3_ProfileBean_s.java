 package org.sakaiproject.sdata.services.profile;
 
 import java.io.IOException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.Kernel;
 import org.sakaiproject.announcement.api.AnnouncementMessage;
 import org.sakaiproject.announcement.api.AnnouncementService;
 import org.sakaiproject.content.api.ContentHostingService;
 import org.sakaiproject.content.api.ContentResource;
 import org.sakaiproject.db.api.SqlReader;
 import org.sakaiproject.db.api.SqlService;
 import org.sakaiproject.entity.api.ResourceProperties;
 import org.sakaiproject.exception.IdUnusedException;
 import org.sakaiproject.exception.PermissionException;
 import org.sakaiproject.exception.TypeException;
 import org.sakaiproject.message.api.MessageChannel;
 import org.sakaiproject.sdata.tool.api.ServiceDefinition;
 import org.sakaiproject.search.api.SearchList;
 import org.sakaiproject.search.api.SearchResult;
 import org.sakaiproject.search.api.SearchService;
 import org.sakaiproject.site.api.Site;
 import org.sakaiproject.site.api.SiteService;
 import org.sakaiproject.site.api.SiteService.SelectionType;
 import org.sakaiproject.site.api.SiteService.SortType;
 import org.sakaiproject.tool.api.Session;
 import org.sakaiproject.tool.api.SessionManager;
 import org.sakaiproject.user.api.User;
 import org.sakaiproject.user.api.UserDirectoryService;
 import org.sakaiproject.user.api.UserEdit;
 import org.sakaiproject.user.api.UserLockedException;
 import org.sakaiproject.user.api.UserNotDefinedException;
 import org.sakaiproject.user.api.UserPermissionException;
 
 /**
  * A service definition bean for recent changes that the current user can see.
  * 
  * @author
  */
 public class ProfileBean implements ServiceDefinition
 {
 
 	private static final Log log = LogFactory.getLog(ProfileBean.class);
 
 	private SqlService sqlService = Kernel.sqlService();
 
 	Map<String, Object> resultMap = new HashMap<String, Object>();
 
 	private SessionManager sessionManager = Kernel.sessionManager();
 	
 	private UserDirectoryService userDirectoryService = Kernel.userDirectoryService();
 
 	/**
 	 * Create a recent changes bean with the number of pages.
 	 * 
 	 * @param paging
 	 */
 	public ProfileBean(String userId, HttpServletRequest request,
 			HttpServletResponse response)
 	{
 		if (request.getMethod().equalsIgnoreCase("get")){
 			if (request.getParameter("search") != null){
 				doSearch(request.getParameter("search"), request, response);
 			} else {
 				doGet(userId, request, response);
 			}
 		} else if (request.getMethod().equalsIgnoreCase("post")){
 			try {
 				doPost(userId, request, response);
 			} catch (Exception e) {
 				e.printStackTrace();
 				try {
 					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 				} catch (IOException e1) {
 					e1.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	private void doSearch(String search, HttpServletRequest request,
 			HttpServletResponse response) {
 	
 		int page = 1;
 		if (request.getParameter("page") != null){
 			try {
 				page = Integer.parseInt(request.getParameter("page"));
 			} catch (Exception ex) {
 				// Switch to default parameter
 			};
 		}
 		int items = 10;
 		if (request.getParameter("items") != null){
 			try {
 				items = Integer.parseInt(request.getParameter("items"));
 			} catch (Exception ex) {
 				// Switch to default parameter
 			};
 		}
 		
 		String[] toSearchFor = search.toUpperCase().split("[ ]");
 		ArrayList<ProfileSqlresult2> arl = new ArrayList<ProfileSqlresult2>();
 		
 		for (int i = 0; i < toSearchFor.length; i++){
			Object[] params = new Object[11];
			for (int ii = 0; ii < 11; ii++){
 				params[ii] = "%" + toSearchFor[i] + "%";
 			}
 			List <ProfileSqlresult2> lst = sqlService.dbRead("SELECT * FROM (SELECT *  FROM SAKAI_USER  LEFT OUTER JOIN sdata_profile ON SAKAI_USER.USER_ID = sdata_profile.userid) as new WHERE " +
 					"UPPER(new.USER_ID) LIKE ? OR " + 
 					"UPPER(new.EMAIL) LIKE ? OR " + 
 					"UPPER(new.FIRST_NAME) LIKE ? OR " +
 					"UPPER(new.LAST_NAME) LIKE ? OR " + 
 					"UPPER(new.basic) LIKE ? OR " + 
 					"UPPER(new.aboutme) LIKE ? OR " + 
 					"UPPER(new.contactinfo) LIKE ? OR " + 
 					"UPPER(new.education) LIKE ? OR " + 
 					"UPPER(new.job) LIKE ? OR " + 
 					"UPPER(new.academic) LIKE ? OR " + 
 					"UPPER(new.talks) LIKE ? OR " + 
 					"UPPER(new.websites) LIKE ?", params, new ProfileSqlreader2());
 			
 			for (ProfileSqlresult2 p: lst){
 				boolean alreadyIn = false;
 				for (int iii = 0; iii < arl.size(); iii++){
 					ProfileSqlresult2 a = arl.get(iii);
 					if (a.getUserid().equalsIgnoreCase(p.getUserid())){
 						alreadyIn = true;
 						arl.get(iii).setCount(arl.get(iii).getCount() + 1);
 					}
 				}
 				if (alreadyIn == false){
 					p.setCount(p.getCount() + 1);
 					arl.add(p);
 				}
 			}
 	
 		}
 		
 		// Order on count
 		
 		Collections.sort(arl);
 		
 		//Limit to paging
 		
 		HashMap<Integer, ProfileSqlresult2> newarl = new HashMap<Integer, ProfileSqlresult2>();
 		
 		int start = 0;
 		for (int i = (page - 1) * items ; i < page * items ; i++){
 			try {
 				newarl.put(start, arl.get(i));
 				start++;
 			} catch (Exception ex){
 				// This is not a valid result, continue
 			}
 		}
 		
 		resultMap.put("items", newarl);
 		resultMap.put("total", arl.size());
 		
 	}
 
 	private void doGet(String userId, HttpServletRequest request,
 			HttpServletResponse response)
 	{
 		
 		if (userId == null){
 			userId = sessionManager.getCurrentSessionUserId();
 		}
 		
 		User user = null;
 		try {
 			user = userDirectoryService.getUser(userId);
 		} catch (UserNotDefinedException e) {
 			try {
 				response.sendError(HttpServletResponse.SC_NOT_FOUND);
 			} catch (IOException e1) {
 				e1.printStackTrace();
 				return;
 			}
 		}
 		
 		resultMap.put("userId", userId);
 		resultMap.put("firstName", user.getFirstName());
 		resultMap.put("lastName", user.getLastName());
 		resultMap.put("email", user.getEmail());
 		
 		if (user.getFirstName() != null && !user.getFirstName().equals("")){
 			resultMap.put("displayName", user.getFirstName());
 		} else {
 			resultMap.put("displayName", user.getDisplayName());
 		}
 		
 		ProfileSqlresult res = new ProfileSqlresult();
 		Object[] params = new Object[1];
 		params[0] = user.getId();
 		List<ProfileSqlresult> list = (List<ProfileSqlresult>) sqlService.dbRead("SELECT * FROM sdata_profile WHERE userid=?", params , new ProfileSqlreader());
 		
 		if (list.size() > 0){
 			res = list.get(0);
 		}
 		
 		resultMap.put("academic", res.getAcademic());
 		resultMap.put("aboutme", res.getAboutme());
 		resultMap.put("contactinfo", res.getContactinfo());
 		resultMap.put("education", res.getEducation());
 		resultMap.put("job", res.getJob());
 		resultMap.put("websites", res.getWebsites());
 		resultMap.put("basic", res.getBasic());
 		resultMap.put("picture", res.getPicture());
 		resultMap.put("talks", res.getTalks());
 
 	}
 	
 	private void doPost (String userId, HttpServletRequest request,
 			HttpServletResponse response) throws Exception
 	{
 		
 		if (userId == null){
 			userId = sessionManager.getCurrentSessionUserId();
 		}
 		
 		User user = null;
 		UserEdit edit = null;
 		try {
 			user = userDirectoryService.getUser(userId);
 			edit = userDirectoryService.editUser(userId);
 		} catch (UserNotDefinedException e) {
 			try {
 				response.sendError(HttpServletResponse.SC_NOT_FOUND);
 			} catch (IOException e1) {
 				e1.printStackTrace();
 				return;
 			}
 		} catch (UserPermissionException e) {
 			try {
 				response.sendError(HttpServletResponse.SC_FORBIDDEN);
 			} catch (IOException e1) {
 				e1.printStackTrace();
 				return;
 			}
 		} catch (UserLockedException e) {
 			try {
 				response.sendError(HttpServletResponse.SC_FORBIDDEN);
 			} catch (IOException e1) {
 				e1.printStackTrace();
 				return;
 			}
 		}
 		
 		Object[] params = new Object[1];
 		params[0] = user.getId();
 		List<ProfileSqlresult> list = (List<ProfileSqlresult>) sqlService.dbRead("SELECT * FROM sdata_profile WHERE userid=?", params , new ProfileSqlreader());
 		
 		if (list.size() == 0){
 			sqlService.dbWrite("INSERT INTO sdata_profile (userid) VALUES (?)", params);
 		}
 		
 		if (request.getParameter("firstName") != null){
 			edit.setFirstName(request.getParameter("firstName"));
 			userDirectoryService.commitEdit(edit);
 		}
 		if (request.getParameter("lastName") != null){
 			edit.setLastName(request.getParameter("lastName"));
 			userDirectoryService.commitEdit(edit);
 		}
 		if (request.getParameter("email") != null){
 			edit.setEmail(request.getParameter("email"));
 			userDirectoryService.commitEdit(edit);
 		}
 		
 		if (edit.isActiveEdit()) {
 			userDirectoryService.cancelEdit(edit);
 		}
 		
 		HashMap<String,String> toUpdate = new HashMap<String, String>();
 		
 		if (request.getParameter("basic") != null){
 			toUpdate.put("basic", request.getParameter("basic"));
 		}
 		if (request.getParameter("aboutme") != null){
 			toUpdate.put("aboutme", request.getParameter("aboutme"));
 		}
 		if (request.getParameter("contactinfo") != null){
 			toUpdate.put("contactinfo", request.getParameter("contactinfo"));
 		}
 		if (request.getParameter("education") != null){
 			toUpdate.put("education", request.getParameter("education"));
 		}
 		if (request.getParameter("job") != null){
 			toUpdate.put("job", request.getParameter("job"));
 		}
 		if (request.getParameter("websites") != null){
 			toUpdate.put("websites", request.getParameter("websites"));
 		}
 		if (request.getParameter("academic") != null){
 			toUpdate.put("academic", request.getParameter("academic"));
 		}
 		if (request.getParameter("picture") != null){
 			toUpdate.put("picture", request.getParameter("picture"));
 		}
 		if (request.getParameter("talks") != null){
 			toUpdate.put("talks", request.getParameter("talks"));
 		}
 		
 		String sqlQuery = "UPDATE sdata_profile SET";
 		Object[] keySet = toUpdate.keySet().toArray();
 		
 		if (keySet.length > 0){
 			
 			Object[] updateparams = new Object[keySet.length + 1];
 			int index = 0;
 			for (int i = 0; i < keySet.length; i++){
 				sqlQuery += " " + keySet[i] + "=?";
 				updateparams[index] = toUpdate.get(keySet[i]);
 				index++;
 				if (i != keySet.length - 1){
 					sqlQuery += " AND";
 				}
 			}
 			sqlQuery += " WHERE userid=?";
 			updateparams[index] = userId;
 			
 			sqlService.dbWrite(sqlQuery, updateparams);
 		
 		}
 		
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
