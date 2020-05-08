 package servlets;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import system.exceptions.ItemCanNotBeDeleted;
 import utils.ProjConst;
 import com.google.gson.Gson;
 import com.google.gson.JsonObject;
 import model.Company;
 import model.Conference;
 import model.ConferencesUsers;
 import model.User;
 import model.UserAttendanceStatus;
 import model.UserRole;
 import daos.CompanyDao;
 import daos.ConferenceDao;
 import daos.ConferencesUsersDao;
 import daos.UserDao;
 
 /**
  * Servlet implementation class UsersServices
  */
 public class UsersServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	
 	private static final String DELETE_USER = "delete";
 	private static final String EDIT_USER = "edit";
 	private static final String ADD_USER = "add";
 	private static final String ADD_PARTICIPANT = "addParticipant";
 	private static final String VALIDATION_USER_NAME_UNIQUE = "validateUserName";
 	private static final String CONFIRM_CONF_PARTICIPATION = "confirmArrival";
 
 	
 	
       
     /**
      * @see HttpServlet#HttpServlet()
      */
     public UsersServlet() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		
 		String action = request.getParameter("action");
 
 		try {
 			if (action == null) {
 				throw new Exception("Critical error!!! action is NULL.");
 			}
 			else if (action.equals(DELETE_USER)) {
 				deleteUser(request, response);
 			}
 			else if (action.equals(EDIT_USER)) {
 				editUser(request, response);
 			}
 			else if (action.equals(ADD_USER)) {
 				addUser(request, response);
 			}
 			else if (action.equals(VALIDATION_USER_NAME_UNIQUE)) {
 				validate(request, response);
 			}
 			else if (action.equals(ADD_PARTICIPANT)) {
 				addParticipant(request, response);
 				
 			}else if (action.equals(CONFIRM_CONF_PARTICIPATION)) {
 				confirmArrivalToConf(request, response);
 			}
 			else {
 				throw new Exception("Unknown request");
 			}
 		}
 
 		catch (Exception e) {
 //			sendErrorPage(request, response, e.getMessage());
 		}
 	}
 
 	private void confirmArrivalToConf(HttpServletRequest request, HttpServletResponse response) throws Exception {
 		
 		JsonObject jsonObject = new JsonObject();
 
 		String userIdstr = request.getParameter(ProjConst.USER_ID);
		String confName = request.getParameter(ProjConst.CONF_NAME);
		String answer =  request.getParameter("answer");
 
 
 		Long userId;
 		boolean success;
 		
 		if ( userIdstr == null) {
 			throw new Exception("Failed to get user id");
 		}else{
 			userId = Long.valueOf(userIdstr.trim());
 		}
 		
 		if ( confName == null) {
 			throw new Exception("Failed to get conference name");
 		}
 		
 		if ( answer == null) {
 			throw new Exception("Failed to get user's answer");
 		}
 		
     	String message;
     	try 
     	{
     		User user = UserDao.getInstance().getUserById(userId);
     		Conference conf = ConferenceDao.getInstance().getConferenceByName(confName);
     		ConferencesUsersDao.getInstance().updateUserAttendanceApproval(conf, user, UserAttendanceStatus.valueOf(answer));
 
     		message = "response successfully updated";
     		success = true;
     		
     	}
     	catch (Exception e)
     	{
     		message = "Error occurred, unable to update response";
     		success = false;
     	}
     	
     	response.setContentType("application/json;charset=UTF-8");
         PrintWriter out = response.getWriter();
         try {
             Gson gson = new Gson();
            	String json;
            	if (success)	
            	{
            		jsonObject.addProperty("resultSuccess", "true");
            		jsonObject.addProperty("message", message);
            		json = gson.toJson(jsonObject);
            	}
            	else
            	{
            		jsonObject.addProperty("resultSuccess", "false");
            		jsonObject.addProperty("message", message);
            		json = gson.toJson(jsonObject);
            	}
            	out.write(json);
             out.flush();
         }
          finally {
             out.close();
         }
 		
 	}
 
 	private void validate(HttpServletRequest request, HttpServletResponse response) throws Exception {
 
 		String operation = request.getParameter(ProjConst.OPERATION);
 		if ( operation == null || operation.trim().isEmpty()) {
 			throw new Exception("Failed to get operation");
 		}
 		
 		String oldUserName = request.getParameter(ProjConst.OLD_USER_NAME);
 		if ( operation.equals(ProjConst.EDIT) && (oldUserName == null || oldUserName.trim().isEmpty())) {
 			throw new Exception("Failed to get old user name");
 		}
 				
 		String userName = request.getParameter(ProjConst.USER_NAME);
 		if (userName == null) {
 			throw new Exception("Failed to get user name");
 		}
 
 		response.setContentType("application/json;charset=UTF-8");
 		PrintWriter out = response.getWriter();
 		
 		try {
 
 			Gson gson = new Gson();
 			String json;
 			User userByUserName = UserDao.getInstance().getUserByUserName(userName);
 
 			if (operation.equals(ProjConst.EDIT)) {
 
 				if (userByUserName == null) {
 					json = gson.toJson("false");
 				} else {
 
 					if (oldUserName.equalsIgnoreCase(userByUserName.getUserName())) {
 						json = gson.toJson("false");
 					} else {
 						json = gson.toJson("true");
 					}
 				}
 
 			} else { // add
 
 				if (userByUserName == null) {
 					json = gson.toJson("false");
 				} else {
 					json = gson.toJson("true");
 				}
 
 			}
 
 			out.write(json);
 			out.flush();
 		} finally {
 			out.close();
 		}
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		doGet(request, response);
 	}
 	
 	
 	private void deleteUser(HttpServletRequest request,	HttpServletResponse response) throws Exception {
 
     	JsonObject jsonObject = new JsonObject();
 
 		String userIdstr = request.getParameter(ProjConst.USER_ID);
 		Long userId;
 		boolean success;
 		
 		if ( userIdstr == null) {
 			throw new Exception("Failed to get user id");
 		}else{
 			userId = Long.valueOf(userIdstr.trim());
 		}
 		
 		String resultSuccess;
     	String message;
     	try 
     	{
     		UserDao.getInstance().deleteUser(userId);
     		message = "User successfully deleted";
     		resultSuccess = "true";
     		success = true;
     		
     	}
     	catch (Exception e)
     	{
     		message = "Found problem while deleting user";
     		resultSuccess = "false";
     		success = false;
     	}
     	
     	response.setContentType("application/json;charset=UTF-8");
         PrintWriter out = response.getWriter();
         try {
             Gson gson = new Gson();
            	String json;
            	if (success)	
            	{
            		jsonObject.addProperty("resultSuccess", resultSuccess);
            		jsonObject.addProperty("message", message);
            		json = gson.toJson(jsonObject);
            	}
            	else
            	{
            		jsonObject.addProperty("resultSuccess", "false");
            		jsonObject.addProperty("message", "Failed to delete user");
            		json = gson.toJson(jsonObject);
            	}
            	out.write(json);
             out.flush();
         }
          finally {
             out.close();
         }
 	}
 	
 	private void editUser(HttpServletRequest request, HttpServletResponse response) throws Exception {
 
 		String userIdStr = request.getParameter(ProjConst.USER_ID);
 		if ( userIdStr == null || userIdStr.trim().isEmpty()) {
 			throw new Exception("Failed to get user Id");
 		}
 	
 		Long userId = -1L;
     	JsonObject jsonObject = new JsonObject();
     	
     	String resultSuccess="";
     	String message="";
     	
 		try {
 			userId = Long.valueOf(userIdStr.trim());
 
 			if(userId != -1){
 				User userToEdit = UserDao.getInstance().getUserById(userId);
 		
 				userToEdit.setUserName(request.getParameter(ProjConst.USER_NAME));
 				userToEdit.setName(request.getParameter(ProjConst.NAME));
 				userToEdit.setPhone1(request.getParameter(ProjConst.PHONE1));
 				userToEdit.setPhone2(request.getParameter(ProjConst.PHONE2));
 				userToEdit.setEmail(request.getParameter(ProjConst.EMAIL));
 				userToEdit.setPasportID(request.getParameter(ProjConst.PASSPORT_ID));
 				String companyIdStr = request.getParameter(ProjConst.COMPANY);
 				long companyId = new Long(companyIdStr);
 				userToEdit.setCompany(CompanyDao.getInstance().getCompanyById(companyId));
 				String pass = request.getParameter(ProjConst.PASSWORD).trim();
 				if(!pass.equals("") && !pass.equals("*****")){
 					userToEdit.setPassword(pass);
 				}
 				userToEdit.setAdmin(new Boolean(request.getParameter(ProjConst.IS_ADMIN))); 
 				
 				UserDao.getInstance().updateUser(userToEdit);
 	    		message = "User successfully edited";
 	    		resultSuccess = "true";
 			}
     	}
 		catch (Exception e) {
 			message = "Error ocurred while editing user";
 			resultSuccess = "false";
 		}
 		response.setContentType("application/json;charset=UTF-8");
 		PrintWriter out = response.getWriter();
 		try {
 			Gson gson = new Gson();
 			String json;
 
 			jsonObject.addProperty("resultSuccess", resultSuccess);
 			jsonObject.addProperty("message", message);
 			json = gson.toJson(jsonObject);
 
 			out.write(json);
 			out.flush();
 		} finally {
 			out.close();
 		}
 	}
 		
 	
 	private void addUser(HttpServletRequest request, HttpServletResponse response) throws Exception {
 
 		String userName = request.getParameter(ProjConst.USER_NAME);
 		String name = request.getParameter(ProjConst.NAME);
 		String phone1 = request.getParameter(ProjConst.PHONE1);
 		String phone2 = request.getParameter(ProjConst.PHONE2);
 		String email = request.getParameter(ProjConst.EMAIL);
 		String passportId = request.getParameter(ProjConst.PASSPORT_ID);
 		String companyIdStr = request.getParameter(ProjConst.COMPANY);
 		long companyId = new Long(companyIdStr);
 		Company company = CompanyDao.getInstance().getCompanyById(companyId);
 		String password="";
 		String pass = request.getParameter(ProjConst.PASSWORD).trim();
 		if(!pass.equals("") && !pass.equals("*****")){
 			password = pass;
 		}
 		boolean isAdmin = new Boolean(request.getParameter(ProjConst.IS_ADMIN)); 
 		
 
     	JsonObject jsonObject = new JsonObject();
     	
     	String resultSuccess;
     	String message;
     	try 
     	{
     		if(phone2 == null){
     			phone2 = "";
     		}
     		
     		User newUser = new User(userName, passportId, company, name, email, phone1, phone2, password, isAdmin);
     		UserDao.getInstance().addUser(newUser);
     		message = "User successfully added";
     		resultSuccess = "true";
     		
     	}
     	catch (Exception e)
     	{
     		message = "Problem occurred while adding user";
     		resultSuccess = "false";
     	}
     	
         response.setContentType("application/json;charset=UTF-8");
         PrintWriter out = response.getWriter();
         
         try {
         	
             Gson gson = new Gson();
            	String json;
 
        		jsonObject.addProperty("resultSuccess", resultSuccess);
        		jsonObject.addProperty("message", message);
        		json = gson.toJson(jsonObject);
 
            	out.write(json);
             out.flush();
         }
          finally {
             out.close();
         }
 		
 	}
 	
 	private void addParticipant(HttpServletRequest request, HttpServletResponse response) throws Exception {
 
 		String userName = request.getParameter(ProjConst.USER_NAME);
 		String name = request.getParameter(ProjConst.NAME);
 		String phone1 = request.getParameter(ProjConst.PHONE1);
 		String phone2 = request.getParameter(ProjConst.PHONE2);
 		String email = request.getParameter(ProjConst.EMAIL);
 		String passportId = request.getParameter(ProjConst.PASSPORT_ID);
 		String companyIdStr = request.getParameter(ProjConst.COMPANY);
 		long companyId = new Long(companyIdStr);
 		Company company = CompanyDao.getInstance().getCompanyById(companyId);
 		String password = "123456";
 		boolean isAdmin = false; 
 		
     	String confName = request.getParameter(ProjConst.CONF_NAME);
     	Conference conference = ConferenceDao.getInstance().getConferenceByName(confName);
 
     	JsonObject jsonObject = new JsonObject();
     	
     	String resultSuccess;
     	String message;
     	try 
     	{
     		if(phone2 == null){
     			phone2 = "";
     		}
     		
     		//Add user
     		UserDao.getInstance().addUser(new User(userName, passportId, company, name, email, phone1, phone2, password, isAdmin));
     		
     		//Assign to conference
     		ConferencesUsersDao.getInstance().assignUserToConference(conference, UserDao.getInstance().getUserByUserName(userName), UserRole.PARTICIPANT.getValue());
     		
     		message = "Participant successfully added";
     		resultSuccess = "true";
     		
     	}
     	catch (Exception e)
     	{
     		message = "Problem occurred while adding participant";
     		resultSuccess = "false";
     	}
     	
         response.setContentType("application/json;charset=UTF-8");
         PrintWriter out = response.getWriter();
         
         try {
         	
             Gson gson = new Gson();
            	String json;
 
        		jsonObject.addProperty("resultSuccess", resultSuccess);
        		jsonObject.addProperty("message", message);
        		json = gson.toJson(jsonObject);
 
            	out.write(json);
             out.flush();
         }
          finally {
             out.close();
         }
 		
 	}
 
 }
