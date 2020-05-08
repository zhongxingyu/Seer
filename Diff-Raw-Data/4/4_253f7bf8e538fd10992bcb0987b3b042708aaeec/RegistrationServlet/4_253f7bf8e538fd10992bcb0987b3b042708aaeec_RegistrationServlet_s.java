 package no.steria.swhrs;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.servlet.ServletException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.sql.DataSource;
 
 import org.joda.time.LocalDate;
 import org.json.simple.JSONObject;
 
 
 /**
  * @author xsts
  *
  */
 public class RegistrationServlet extends HttpServlet{
 	
 	private static final long serialVersionUID = -1090477374982937503L;
 	private HourRegDao db;
 	private String username = null;
 	
 	LocalDate date = LocalDate.now();
 	public void init() throws ServletException {
 		if ("true".equals(System.getProperty("swhrs.useSqlServer"))) {
 			try {
 				db = new MSSQLHourRegDao((DataSource) new InitialContext().lookup("jdbc/registerHoursDS"));
 			} catch (NamingException e) {
 				throw new ServletException(e);
 			}
 		} else {
 			db = new HibernateHourRegDao(Parameters.DB_JNDI);
 		}
 		
 	}
 	
 	
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		
 		
 		if (req.getRequestURL().toString().contains(("hours/projects"))) { 
 			resp.setContentType("application/json");
 			//TODO return a list of all projects.
 		}
 		
 	}
 
 	
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		
 
 		if(req.getRequestURL().toString().contains(("hours/daylist"))){
 			getDaylistResponseAsJSON(req, resp);
 		} else if(req.getRequestURL().toString().contains(("hours/favourite"))){
 			getFavouritesResponse(req, resp);
 		} else if (req.getRequestURL().toString().contains(("hours/registration"))) {
 			addHourRegistationToDatabase(req);
 		} else if (req.getRequestURL().toString().contains(("hours/login"))) {
 			loginUserAndSetCookies(req, resp);
 		} else if(req.getRequestURL().toString().contains(("hours/week"))){
 			getWeeklistResponseAsJSON(req, resp);
		} else if(req.getRequestURL().toString().contains(("hours/delete"))){
 			deleteHourRegistrationInDatabase(req, resp);
 		} else if(req.getRequestURL().toString().contains(("hours/setUsername"))){
 			setUsername(req, resp);
 		} else if(req.getRequestURL().toString().contains(("hours/searchFavourites"))){
 			searchFavourites(req, resp);
 		} else if(req.getRequestURL().toString().contains(("hours/addFavourites"))){
 			addFavourites(req, resp);
 		} else if(req.getRequestURL().toString().contains(("hours/submitPeriod"))){
 			submitPeriod(req, resp);
 		} else if(req.getRequestURL().toString().contains(("hours/deleteFavourite"))){
 			deleteFavourite(req, resp);
 		} else if(req.getRequestURL().toString().contains(("hours/updateRegistration"))){
 			updateRegistration(req, resp);
 		}
  	}
 
 
 	
 
 
 	private void updateRegistration(HttpServletRequest req,
 			HttpServletResponse resp) {
 		int taskNumber = Integer.parseInt(req.getParameter("taskNumber"));
 		double hours = Double.parseDouble(req.getParameter("hours"));
 		String description = req.getParameter("description");
 		
 		db.updateRegistration(taskNumber, hours, description);
 	}
 
 
 	private void deleteFavourite(HttpServletRequest req,
 			HttpServletResponse resp) throws IOException {
 		String projectNumber = req.getParameter("projectNumber");
 		String activityCode = req.getParameter("activityCode");
 		db.deleteFavourite(username, projectNumber, activityCode);
 		
 		resp.setContentType("text/plain");
 		resp.getWriter().append("Favourite is deleted");
 	}
 
 
 	private void addFavourites(HttpServletRequest req, HttpServletResponse resp) {
 		String projectNumber = req.getParameter("projectNumber");
 		String activityCode = req.getParameter("activityCode");
 		if(db.addFavourites(username, projectNumber, activityCode)){
 			System.out.println("ADDING COMPLETE, WANNA SEND SOME JSON?");
 		}
 	}
 
 
 	@SuppressWarnings("unchecked")
 	private void searchFavourites(HttpServletRequest req,
 			HttpServletResponse resp) throws IOException {
 		resp.setContentType("application/json");
 		String searchInput = req.getParameter("search");
 		List<Projects> project = db.searchProjects(searchInput);
 		int counter = 0;
 		JSONObject projectJson = new JSONObject();
 		for(Projects po: project){
 			HashMap map = new HashMap();
 			map.put("projectnumber", po.getProjectNumber());
 			map.put("activitycode", po.getActivityCode());
 			map.put("description", po.getDescription());
 			counter++;
 			projectJson.put(counter, map);
 		}
 		System.out.println(projectJson.toString());
 		PrintWriter writer = resp.getWriter();
 		String jsonText = projectJson.toString();
 		writer.append(jsonText);
 	}
 
 
 	private void setUsername(HttpServletRequest req, HttpServletResponse resp) {
 		String loginUsername = req.getParameter("UN");
 		username = loginUsername;
 		System.out.println("SKRIVER UT: "+loginUsername);
 	}
 
 
 	/**
 	 * This method will delete an hour registration from the database
 	 * @param req The HTTP request contains taskNumber which is the unique identifier for each registration in the database
 	 * @param resp The HTTP response will return plain text with either 
 	 * 			   "ERROR: Already submitted" if the deleteHourRegistration returns false, meaning that the registration is locked
 	 * 			or "success" if the deletion was successful
 	 * @throws IOException
 	 */
 	private void deleteHourRegistrationInDatabase(HttpServletRequest req,
 			HttpServletResponse resp) throws IOException {
 		String taskNumber = req.getParameter("taskNumber");
 		System.out.println(taskNumber);
 		boolean success = db.deleteHourRegistration(taskNumber);
 		System.out.println(success);
 		resp.setContentType("text/plain");
 		if (!success) {
 			resp.getWriter().append("ERROR: Already submitted");
 		}else{
 			resp.getWriter().append("success");
 		}
 	}
 	
 	private void submitPeriod(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		DatePeriod period = db.getPeriod(username, date.toString());
 		db.submitPeriod(username, period.getFromDate(), period.getToDate());
 		resp.setContentType("text/plain");
 		resp.getWriter().append("Period is submitted");
 	}
 	
 	
 	/**
 	 * This method returns a HTTP request containing JSON data for a period
 	 * @param req The HTTP request contains the week parameter which contains strings of either "thisWeek", "prevWeek" or "nextWeek"
 	 * @param resp The HTTP response will return a json object containing data about weekdays, dates and hours for the week requested
 	 * @throws IOException
 	 */
 	private void getWeeklistResponseAsJSON(HttpServletRequest req,
 			HttpServletResponse resp) throws IOException {
 		String week = req.getParameter("week");		
 		resp.setContentType("application/text");
 		DatePeriod period2 = db.getPeriod(username, date.toString());
 		
 		LocalDate localFromDate = new LocalDate(period2.getFromDate().split(" ")[0]);
 		LocalDate localToDate = new LocalDate(period2.getToDate().split(" ")[0]);
 		
 		if(week.equals("nextWeek")) date = localToDate.plusDays(1);
 		if(week.equals("prevWeek")) date = localFromDate.minusDays(1);
 		
 		DatePeriod period = db.getPeriod(username, date.toString());
 		
 		LocalDate localFromDate2 = new LocalDate(period.getFromDate().split(" ")[0]);
 		LocalDate localToDate2 = new LocalDate(period.getToDate().split(" ")[0]);
 		
 		ArrayList<String> dateArray = new ArrayList<String>();
 		while(localFromDate2.compareTo(localToDate2) <= 0  ){
 			dateArray.add(localFromDate2.toString()+":"+localFromDate2.getDayOfWeek());
 			localFromDate2 = localFromDate2.plusDays(1);
 		}
 		
 		
 		System.out.println("fromDate: "+period.getFromDate()+" toDate: "+period.getToDate()+" Description: "+period.getDescription());
 		
 		List<WeekRegistration> weeklist = db.getWeekList(username, period.getFromDate(), period.getToDate());
 		String weekDescription = period.getDescription();
 		
 		
 		JSONObject obj = new JSONObject();
 		for(int i=0; i<dateArray.size(); i++){
 			String dateArr = dateArray.get(i).toString().split(":")[0];
 			String dayOfWeek = dateArray.get(i).toString().split(":")[1];
 			boolean found = false;
 			for(WeekRegistration wr2: weeklist){
 				if(wr2.getDate().split(" ")[0].equals(dateArr)){
 					List list = new LinkedList();
 					list.add(dayOfWeek);
 					list.add(wr2.getHours());
 					obj.put(wr2.getDate().split(" ")[0], list);
 					found = true;
 					break;
 				}
 			}
 			if (!found) {
 				List list = new LinkedList();
 				list.add(dayOfWeek);
 				list.add(0);
 				obj.put(dateArr, list);
 			}
 			
 		}
 		
 		obj.put("weekNumber", weekDescription);
 		obj.put("dateHdr", date.getDayOfWeek()+" "+date.toString());
 		resp.setContentType("text/json");
 		PrintWriter writer = resp.getWriter();
 		String jsonText = obj.toJSONString();
 		writer.append(jsonText);
 	}
 
 
 	/**
 	 * Sets login cookies
 	 * @param req The HTTP request containing username and password
 	 * @param resp The HTTP request will return plain text of either "Login approved" if successful or set status to 403 if login failed
 	 * @throws IOException
 	 */
 	private void loginUserAndSetCookies(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		username = req.getParameter("username").toUpperCase();
 		String password = req.getParameter("password");
 		int autoLoginExpire = (60*60*24);
 		if(db.validateUser(username, password) == true){
 			Cookie loginCookie = new Cookie("USERNAME", username);
 			loginCookie.setMaxAge(autoLoginExpire);
 			resp.setContentType("text/plain");
 			PrintWriter writer = resp.getWriter();
 			writer.append("Login approved");
 		}else{
 			resp.setStatus(403);
 			System.out.println("You dont fool me, fool");
 		}
 	}
 
 
 	/**
 	 * Adds an hour registration the database
 	 * @param req The HTTP request containing parameters of "ProjectNr", "hours", "lunchNumber" and "description"
 	 */
 	private void addHourRegistationToDatabase(HttpServletRequest req) {
 		String projectNumber = req.getParameter("projectNr");
 		String activityCode = req.getParameter("activityCode");
 		double hours = Double.parseDouble(req.getParameter("hours"));
 		String lunchNumber = req.getParameter("lunchNumber");
 		String description = req.getParameter("description");
 		int billable = Integer.parseInt(req.getParameter("billable"));
 		int internal = Integer.parseInt(req.getParameter("internalproject"));
 		
 		
 		db.addHourRegistrations(projectNumber, activityCode, username, "", date.toString(), hours, description, 0, 0, billable, 10101, internal, 0, "HRA", "", projectNumber, "", 0, 0, "", "", "2012-05-30", "HRA", "", 0, 0);
 		if(lunchNumber.equals("1")){
 			db.addHourRegistrations("Lunsj", "LU", username, "", date.toString(), 0.5, "Lunsj", 0, 0, 1, 10101, 0, 0, "HRA", "", lunchNumber, "", 0, 0, "", "", "2012-05-30", "HRA", "", 0, 0);
 		}
 		System.out.println("Trying to save project: " + projectNumber);
 	}
 
 
 	/**
 	 * Returns a HTTP response containing a JSON object of the users favourites stored in the database
 	 * @param resp HTTP request containing a JSON object of the users favourites
 	 * @throws IOException
 	 */
 	private void getFavouritesResponse(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		resp.setContentType("application/json");
 		List<UserFavourites> userList = db.getUserFavourites(username);
 		JSONObject json = createJsonObjectFromFavourites(userList);
 
 		PrintWriter writer = resp.getWriter();
 		String jsonText = json.toString();
 		writer.append(jsonText);
 	}
 
 
 	/**
 	 * Returns a HTTP response containing all hour registrations for a certain day stored in a JSON object
 	 * @param req The HTTP request containing a parameter "day" containing either "today", "prevDay"
 	 * @param resp The HTTP response contains a json object with all data about a registration needed to display it in the app
 	 * @throws IOException
 	 */
 	private void getDaylistResponseAsJSON(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		String newDay = req.getParameter("day");
 		System.out.println("NEWDAY ="+newDay);
 		
 		resp.setContentType("application/json");
 		if(newDay.equals("prevDay")) date = date.minusDays(1);
 		else if(newDay.equals("nextDay")) date = date.plusDays(1);
 		else if(newDay.equals("today")){
 			System.out.println("getting todays daylist from server");
 		}else{
 			System.out.println("WEEKNAVIGATION");
 			LocalDate weekDate = new LocalDate(newDay);
 			date = weekDate;
 			System.out.println("NEWWEEKDATE: "+date);
 		}
 		List<HourRegistration> hrlist = db.getAllHoursForDate(username, date.toString());
 
 		String stringDate = date.toString();
 		JSONObject json = createJsonObjectFromHours(hrlist, date.getDayOfWeek()+" "+stringDate);
 
 		PrintWriter writer = resp.getWriter();
 		String jsonText = json.toString();
 		System.out.println("JSON sendt from regservlet: " +jsonText);
 		writer.append(jsonText);
 	}
 
 	/**
 	 * Helper method to make a JSON object from a list of HourRegistrations
 	 * @param hrlist the list of HourRegistration objects
 	 * @param stringDate the date of the registrations
 	 * @return A json object of the format: key: taskNumber values: [description, hours]
 	 */
 	@SuppressWarnings("unchecked")
 	private JSONObject createJsonObjectFromHours(List<HourRegistration> hrlist, String stringDate) {
 		JSONObject json = new JSONObject();
 		for (HourRegistration hr: hrlist) {
 			System.out.println(hr.getDate()+":"+hr.getDescription() + " Approved: " +hr.isApproved()+" Submitted "+ hr.isSubmitted());
 			HashMap map = new HashMap();
 			map.put("projectnumber", hr.getProjectnumber());
 			map.put("activitycode", hr.getActivityCode());
 			map.put("description", hr.getDescription());
 			map.put("approved", hr.isApproved());
 			map.put("submitted", hr.isSubmitted());
 			map.put("hours", hr.getHours());
 			json.put(hr.getTaskNumber(), map);
 		}
 		json.put("date", stringDate);
 		return json;
 	}
 	
 	/**
 	 * Helper method to create JSON object from a list of UserFavourites objects
 	 * @param userList The list containing user favourite objects stored in the database
 	 * @return JSON object with the format {"projectNumber":{"internalproject":value,"activitycode":value,"description": value,"projectname": value,"customername": value,"billable": value}
 	 *         Keys are generated from 1 and up so it's easy to sort later, they contain a map with the rest of the values
 	 */
 	@SuppressWarnings("unchecked")
 	private JSONObject createJsonObjectFromFavourites(
 			List<UserFavourites> userList) {
 		JSONObject json = new JSONObject();
 		int counter = 0;
 		for (UserFavourites uf: userList) {
 			HashMap map = new HashMap();
 			map.put("projectnumber", uf.getProjectNumber());
 			map.put("activitycode", uf.getActivityCode());
 			map.put("description", uf.getDescription());
 			map.put("billable", uf.getBillable());
 			map.put("projectname", uf.getProjectName());
 			map.put("customername", uf.getCustomer());
 			map.put("internalproject", uf.getInternalProject());
 			
 			json.put(counter++, map);
 			
 		}
 		System.out.println(json.toString());
 		return json;
 		
 	}
 
 
 	@Override
 	protected void service(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		try {
 			db.beginTransaction();
 			super.service(req, resp);
 		} finally {
 			db.endTransaction(true);
 		}
 	}
 
 }
