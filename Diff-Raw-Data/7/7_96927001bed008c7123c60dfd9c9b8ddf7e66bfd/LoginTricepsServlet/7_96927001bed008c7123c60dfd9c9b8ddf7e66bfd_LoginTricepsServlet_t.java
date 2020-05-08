 /* ******************************************************** 
 ** Copyright (c) 2000-2001, Thomas Maxwell White, all rights reserved. 
 ** $Header$
 ******************************************************** */ 
 
 package org.dianexus.triceps;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpSession;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpUtils;
 
 import javax.naming.*;
 import javax.sql.*;
 import java.sql.*;
 
 import java.util.Hashtable;
 import java.util.StringTokenizer;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Random;
 import java.io.PrintWriter;
 import java.util.Enumeration;
 import java.io.PrintWriter;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.text.DateFormat;
 
 public class LoginTricepsServlet extends TricepsServlet {
 	static Random random = new Random();
 	String STUDY_NAME = "";
 	String STUDY_ICON = "";
 	String SUPPORT_PHONE = "";
 	String SUPPORT_EMAIL = "";
 	String SUPPORT_PERSON = "";
 	
 	public void init(ServletConfig config) throws ServletException {
 		super.init(config);
 		
 		/* 8/20/2003 -- extract static options from LicenseIF to here */
 		STUDY_NAME = config.getInitParameter("LICENSE.STUDY_NAME");
 		STUDY_ICON = config.getInitParameter("LICENSE.STUDY_ICON");
 		
 		SUPPORT_PHONE = config.getInitParameter("SUPPORT.PHONE");
 		SUPPORT_EMAIL = config.getInitParameter("SUPPORT.EMAIL");
 		SUPPORT_PERSON = config.getInitParameter("SUPPORT.PERSON");			
 	}
 
 	public void destroy() {
 		super.destroy();
 	}
 	
 	public void doGet(HttpServletRequest req, HttpServletResponse res) {
 		doPost(req,res);
 	}
 
 	public void doPost(HttpServletRequest req, HttpServletResponse res)  {
 		try {
 		initSession(req,res);
 		int result = LOGIN_ERR_OK;
 			result = processPost(req,res);
 			if (result >= 0) { logPageHit(req,LOGIN_ERRS_BRIEF[result]); }	// way to avoid re-logging post shutdown 
 			// [XXX] so does this mean it is logging the wrong thing?
 		}
 		catch (OutOfMemoryError oome) {
 			Runtime.getRuntime().gc();
 if (DEBUG) Logger.writeln("##Exception @ Servlet.doPost()" + oome.getMessage());
 if (DEBUG) Logger.printStackTrace(oome);			
 		}		
 		catch (Exception t) {
 if (DEBUG) {
 	Logger.writeln(t.getMessage());
 	Logger.printStackTrace(t);
 }
 		}
 	}
 	
 	int processPost(HttpServletRequest req, HttpServletResponse res)  {
 		/* validate session */
 		/* Ensure that there is actually a session object  -- if not, will be created by loginPage */
 		HttpSession session = req.getSession(false);
 		
 		if (session == null || session.isNew()) {
 			/* If session is expired, give appropriiate error page */
 			if ("POST".equals(req.getMethod())) {
 				loginPage(req,res,LOGIN_ERR_EXPIRED_SESSION);
 				return -1;
 			}
 			/* if new, require login */
 			loginPage(req,res,LOGIN_ERR_NEW_SESSION);
 			return -1;
 		}
 		
 		String loginCommand = req.getParameter(LOGIN_COMMAND);
 		String loginToken = req.getParameter(LOGIN_TOKEN);	// need to way to tell whether this is a authenticated session
 		String loginIP = req.getRemoteAddr();
 		String loginBrowser = req.getHeader(USER_AGENT);
 		if (loginBrowser == null) {
 			loginBrowser = "null";
 		}
 		String loginUserName = req.getParameter(LOGIN_USERNAME);
 		String storedLoginToken = (String) session.getAttribute(LOGIN_TOKEN);
 		String storedIP = (String) session.getAttribute(LOGIN_IP);
 		String storedBrowser = (String) session.getAttribute(LOGIN_BROWSER);
 		String storedUserName = (String) session.getAttribute(LOGIN_USERNAME);
 		
 		LoginRecord loginRecord = (LoginRecord) session.getAttribute(LOGIN_RECORD);
 		
 		if (LOGIN_COMMAND_LOGON.equals(loginCommand)) {
 			/* check whether person has backed up and tried to re-login to an active session */
 			if ((storedLoginToken != null && !storedLoginToken.trim().equals("")) || loginRecord != null) {
 				/* the person is trying to login again -- don't let them continue, since have active session */
 				loginPage(req,res,LOGIN_ERR_INVALID_RELOGON);
 				return -1;
 			}
 			
 			/* then try to validate this person */
 			String uname = req.getParameter(LOGIN_USERNAME);
 			String pass = req.getParameter(LOGIN_PASSWORD);
 			if (uname == null || uname.trim().equals("") ||
 				pass == null || pass.trim().equals("")) {
 				loginPage(req,res,LOGIN_ERR_MISSING_UNAME_OR_PASS);
 				return -1;
 			}
 			
 			loginRecord = validateLogin(uname,pass);
 			if (loginRecord == LoginRecord.NULL) {
 				/* then invalid */
 				loginPage(req,res,LOGIN_ERR_INVALID_UNAME_OR_PASS);
 				return -1;
 			}
 			
 			/* create tokens needed for remainder of processing */
 			storedIP = loginIP;
 			storedBrowser = loginBrowser;
 			storedUserName = loginUserName;
 			session.setAttribute(LOGIN_IP,loginIP);
 			session.setAttribute(LOGIN_RECORD,loginRecord);	// stores the username/password info for later access
 			session.setAttribute(LOGIN_BROWSER,loginBrowser);
 			session.setAttribute(LOGIN_USERNAME,loginUserName);
 			storedLoginToken = createLoginToken();
 			loginToken = storedLoginToken;
 			
 			/* need to either create new instrument, or know to load appropriate existing one */
 			/* might like to extend this like Perl "Prefill" programs so that sets some default parameters when creates instance */
 		}
 		
 		/* validate that the session is authenticated */
 					
 		if (loginToken == null || loginToken.trim().equals("") || 
 			storedLoginToken == null || storedLoginToken.trim().equals("") ||
 			loginRecord == null) {
 			/* Then user has not logged in */
 			loginPage(req,res,LOGIN_ERR_NO_TOKEN);
 			return -1;
 		}
 		
 		/* compare login token to the one stored in the session */
 		if (storedLoginToken.equals(loginToken)) {
 			if (!(storedIP.equals(loginIP) && storedBrowser.equals(loginBrowser) && storedUserName.equals(loginUserName))) {
 				loginPage(req,res,LOGIN_ERR_ODD_CHANGE);
 				return -1;
 			}
 			
 			/* create and store new login token -- so can't re-submit */
 			loginToken = createLoginToken();
 			session.setAttribute(LOGIN_TOKEN,loginToken);
 			
 			/* ensure that this hidden parameter is sent with each new form -- this is a hack */
 			StringBuffer sb = new StringBuffer();
 			sb.append("\n<input type='hidden' name='").append(LOGIN_TOKEN).append("' value='").append(loginToken).append("'>");
 			sb.append("\n<input type='hidden' name='").append(LOGIN_BROWSER).append("' value='").append(loginBrowser).append("'>");
 			sb.append("\n<input type='hidden' name='").append(LOGIN_USERNAME).append("' value='").append(loginUserName).append("'>");
 
 			/* pass control through to main Dialogix servlet */
 			return processAuthenticatedRequest(req,res,sb.toString());
 		}
 		else {
 			/* What if try to re-submit -- what will happen? */
 			loginPage(req,res,LOGIN_ERR_INVALID);
 			return -1;
 		}
 	}
 	
 	int processAuthenticatedRequest(HttpServletRequest req, HttpServletResponse res, String hiddenLoginToken)  {
 		/* Now, must load the proper instrument from the "database" */
 		/* Might also want to warn about the unsupported browser feature earlier? */
 		if (isSupportedBrowser(req)) {
 			return okPage(req,res,hiddenLoginToken);
 		}
 		else {
 			errorPage(req,res);
 			return LOGIN_ERR_UNSUPPORTED_BROWSER;
 		}
 	}
 	
 	
 	private static String createLoginToken() {
 		return System.currentTimeMillis() + "." + Long.toString(random.nextLong());
 	}
 	
 	void loginPage(HttpServletRequest req, HttpServletResponse res, int login_err) {
 		loginPage(req,res,login_err,LOGIN_ERRS_VERBOSE[login_err]);
 	}
 	
 	void loginPage(HttpServletRequest req, HttpServletResponse res, int login_err, String message) {
 		logAccess(req, LOGIN_ERRS_BRIEF[login_err]);
 		
 		shutdown(req,LOGIN_ERRS_BRIEF[login_err],true);	// at login page
 		
 		try {
 			res.setContentType(CONTENT_TYPE);
 			PrintWriter out = res.getWriter();
 			
 			out.println("<!DOCTYPE html PUBLIC '-//W3C//DTD HTML 4.01 Transitional//EN'>");
 			out.println("<html>");
 			out.println("<head>");
 			out.println("<META HTTP-EQUIV='Content-Type' CONTENT='" + CONTENT_TYPE + "'>");
 			out.println("<title>Login</title>");
 			out.println("</head>");
 			out.println("<body bgcolor='white'>");
 			
 			out.print("<FORM method='POST' name='myForm' action='");
 			out.print(res.encodeURL(HttpUtils.getRequestURL(req).toString()));
 			out.print("'>");
 			
 			out.println(createStudyTitleBar());	// study header
 			
 			out.println("<P><font size='+1'><b>");
 			out.println(message);
 			out.println("</b></font></P>");
 			
 			out.println(createLoginForm());
 			
 			out.println(createHelpInfo());
 			
 			out.println("</form>");
 			out.println("</body>");
 			out.println("</html>");
 	
 			out.flush();
 			out.close();
 		}
 		catch (Exception t) {
 if (DEBUG) Logger.printStackTrace(t);
 		}		
 	}
 	
 	private String createStudyTitleBar() {
 		StringBuffer sb = new StringBuffer();
 
 		/* create common header row, indicating which study is being conducted */
 		sb.append("<table border='0' cellpadding='0' cellspacing='3' width='100%'>");
 		sb.append("<tr>");
 		sb.append("<td width='1%'>");
 		sb.append("<img name='icon' src='" + STUDY_ICON + "' align='top' border='0' alt='" + STUDY_NAME + "'>");
 		sb.append("</td><td>");
 		sb.append("<font size='4'>");
 		sb.append(STUDY_NAME);
 		sb.append("</font></td></tr></table>");
 		
 		return sb.toString();		
 	}
 	
 	private String createLoginForm() {
 		StringBuffer sb = new StringBuffer();
 		
 		sb.append("<table border='1' cellpadding='0' cellspacing='0' width='100%'>");
 		sb.append("<tr><td width='50%'>Username</td><td width='50%'><input type='text' name='" + LOGIN_USERNAME + "' size='25'></td></tr>");
 		sb.append("<tr><td width='50%'>Password</td><td width='50%'><input type='password' name='" + LOGIN_PASSWORD + "' size='25'></td></tr>");
 		sb.append("<tr><td colspan='2' align='center'><input type='submit' name='Submit' value='Submit'></td></tr>");
 		sb.append("</table>");
 		sb.append("<input type='hidden' name='" + LOGIN_COMMAND + "' value='" + LOGIN_COMMAND_LOGON + "'>");
 		
 		return sb.toString();
 	}
 	
 	private String createHelpInfo() {
 		StringBuffer sb = new StringBuffer();
 		boolean hasInfo = false;
 		
 		sb.append("<hr>");
 		sb.append("For assistance, please contact:<br>");
 		if (SUPPORT_PERSON != null && !SUPPORT_PERSON.trim().equals("")) {
 			sb.append(SUPPORT_PERSON);
 			sb.append("<br>");
 			hasInfo = true;
 		}
 		if (SUPPORT_EMAIL != null && !SUPPORT_EMAIL.trim().equals("")) {
 			sb.append("<a href='mailto:");
 			sb.append(SUPPORT_EMAIL);
 			sb.append("'>");
 			sb.append(SUPPORT_EMAIL);
 			sb.append("</a><br>");
 			hasInfo = true;
 		}
 		if (SUPPORT_PHONE != null && !SUPPORT_PHONE.trim().equals("")) {
 			sb.append(SUPPORT_PHONE);
 			hasInfo = true;
 		}
 		if (hasInfo) {
 			return sb.toString();
 		}
 		else {
 			return "";
 		}
 	}
 	
 	int okPage(HttpServletRequest req, HttpServletResponse res, String hiddenLoginToken) {
 		/* 2/5/03:  Explicitly ask for session info everywhere (vs passing it as needed) */
 		HttpSession session = req.getSession(false);
 		String sessionID = session.getId();
 		TricepsEngine tricepsEngine = (TricepsEngine) session.getAttribute(TRICEPS_ENGINE);
 		
 		String restoreFile = null;	// means that the file has already been loaded.  If non-null, asks the system to load it.
 		
 		/* Now ensure that using proper instrument for this authenticated subject */
 		LoginRecord loginRecord = (LoginRecord) session.getAttribute(LOGIN_RECORD);	// can't be null here		
 		
 		/* Can I assume that once the tricepsEngine is active, that the proper instrument will have been loaded? */
 		if (tricepsEngine == null) {
 			/* different behavior based upon the status of this instance of the interview */
 			if (loginRecord.isCompleted()) {
 				/* if completed, give the option to sign on as someone else */
 				loginPage(req,res,LOGIN_ERR_ALREADY_COMPLETED);
 				return -1;
 			}
 			
 			tricepsEngine = new TricepsEngine(this.getServletConfig());
 //			tricepsEngine.getTriceps().setLoginRecord(this,loginRecord);
 			session.setAttribute(TRICEPS_ENGINE, tricepsEngine);
 			
 			/* create new instance, or validate that old instance exists; then "resume" it via normal TricepsEngine mechanisms */
 			String src = null;
 			
 			if (loginRecord.isWorking()) {
 				/* then load the existing one, from where left off */
 				src = loginRecord.getFilename();
 				logAccess(req," RESTORE");
 			}
 			else {
 				/* hasn't been started yet -- load it from the instrument, and keep track of the location information (the tmp file) */
 				src = loginRecord.getInstrument();
 				logAccess(req," START");
 			}
 			
 			boolean ok = tricepsEngine.getNewTricepsInstance(tricepsEngine.getCanonicalPath(src),req);
 			if (ok) {
 				String filename = tricepsEngine.getTriceps().dataLogger.getFilename();
 				
 				if (!loginRecord.isWorking()) {
 					/* This is a new file, so add additional config information to data file */
 					tricepsEngine.setExtraParameters(loginRecord.getStartingStep(),loginRecord.getMappings());
 				}
 				
 				loginRecord.setFilename(filename);
 				loginRecord.setStatusWorking();
 				updateRecord(loginRecord);
 				
 				/* override whatever command was passed via req -- should be RESTORE */
 				restoreFile = filename;
 			}
 			else {
 				loginPage(req,res,LOGIN_ERR_UNABLE_TO_LOAD_FILE,LOGIN_ERRS_VERBOSE[LOGIN_ERR_UNABLE_TO_LOAD_FILE] + " '" + src + "'");
 				return -1;
 			}
 		}
 		else {
 			/* an active TricepsEngine */
 			logAccess(req, " OK");
 		}
 		
 		try {
 			res.setContentType(CONTENT_TYPE);
 			PrintWriter out = res.getWriter();
 			
 			tricepsEngine.doPost(req,res,out,hiddenLoginToken,restoreFile);
 			
 			/* process session before finalizing print writer */
 //			session.setAttribute(TRICEPS_ENGINE, tricepsEngine);
 			
 			out.flush();
 			out.close();
 			
 			/* disable session if completed */
 			if (tricepsEngine.isFinished()) {
 				logAccess(req, " FINISHED");
 				try {
 					shutdown(req,LOGIN_ERRS_BRIEF[LOGIN_ERR_FINISHED],false);	// if don't remove the session, can login as someone new
 					loginRecord.setStatusCompleted();
 					updateRecord(loginRecord);
 				}
 				catch (java.lang.IllegalStateException e) {
 					Logger.writeln(e.getMessage());
 				}
 				return -1;
 			}					}
 		catch (Exception t) {
 if (DEBUG) Logger.printStackTrace(t);
 		}
 		return LOGIN_ERR_OK;
 	}	
 	
 	/** 
 		These functions are pulled  from the old LoginRecords, now that a database.  Better separation of function is desirable for the future
 	**/
 	
 	LoginRecord validateLogin(String username, String password) {
 		if (username == null || username.trim().equals("") || password == null || password.trim().equals("")) {
 			return LoginRecord.NULL;
 		}
 		
 		/* query database to create a login record */
 		
 		LoginRecord lr = getRecord(username);
 		if (lr == null) {
 			return LoginRecord.NULL;
 		}
 		if (password.equals(lr.getPassword())) {
 			return lr;
 		}
 		else {
 			return LoginRecord.NULL;
 		}
 	}
 	
 	LoginRecord getRecord(String username) {
 		/* This assumes that each unique username is only assigned to a single instrument */
         LoginRecord lr = null;
         
 if (DB_FOR_LOGIN) {		
 		try {
 			StringBuffer sb = new StringBuffer();
 			sb.append("SELECT username, password, filename, instrument, status, startingStep,_clinpass, Dem1 FROM wave6users WHERE 1 AND username LIKE '");
 			sb.append(username).append("'");
 //			if (DEBUG) Logger.writeln(sb.toString());
 			if (DEBUG) Logger.writeln("LOGIN ATTEMPT: " + username);
 			
 			if (ds == null) throw new Exception("Unable to access DataSource");
 			
 	        Connection conn = ds.getConnection();
 	        
 	        if (conn == null)throw new Exception("Unable to connect to database");	// really need a way to report that there are database problems!
 	              
 	        Statement stmt = conn.createStatement();
 	        ResultSet rst = stmt.executeQuery(sb.toString());
 	        if(rst.next()) {
 	        	lr = new LoginRecord();
 	        	lr.setUsername(rst.getString(1));
 	        	lr.setPassword(rst.getString(2));
 	        	lr.setFilename(rst.getString(3));
 	        	lr.setInstrument(rst.getString(4));
 	        	lr.setStatus(rst.getString(5));
 	        	lr.setStartingStep(rst.getString(6));
 	        	lr.addMapping("_clinpass",rst.getString(7));
 	        	lr.addMapping("Dem1",rst.getString(8));
 //if (DEBUG) Logger.writeln("LOGIN: " + lr.showValue());
 	        }
 	        stmt.close();
 	        conn.close();
 	    } catch (Exception t) {
 			Logger.writeln("Error updating database \"" + t.getMessage());
 if (DEBUG) Logger.printStackTrace(t);
 	    }
 }
         
 		return lr;
 	}
 	
 	boolean updateRecord(LoginRecord lr) {
 		/* This assumes that each unique username is only assigned to a single instrument */
 if (DB_FOR_LOGIN) {		
 		StringBuffer sb = new StringBuffer();
 		sb.append("UPDATE wave6users SET ");
 		sb.append("	filename='").append(lr.getFilename());
 		sb.append("',	status='").append(lr.getStatus());
 		sb.append("'	WHERE username='").append(lr.getUsername()).append("'");
 		
 		return writeToDB(sb.toString());
 } else { return true; }		
 	}
 	
 	boolean updateStatus(HttpServletRequest req, String msg) {
 if (DB_TRACK_LOGINS) {		
 		/* 2/5/03:  Explicitly ask for session info everywhere (vs passing it as needed) */
 		HttpSession session = req.getSession(false);
 		String sessionID = session.getId();
 		TricepsEngine tricepsEngine = (TricepsEngine) session.getAttribute(TRICEPS_ENGINE);
 						
 		/* This assumes that each unique username is only assigned to a single instrument */
 		LoginRecord lr = (LoginRecord) session.getAttribute(LOGIN_RECORD);
 		if (lr == null || tricepsEngine == null) { return false; }
 
 		StringBuffer sb = new StringBuffer();
 		sb.append("UPDATE wave6users SET ");
 		sb.append("	status='").append(lr.getStatus());
 		sb.append("',	lastAccess='").append(DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM).format(new Date(System.currentTimeMillis())));
 		sb.append("',	currentStep='").append(tricepsEngine.getCurrentStep());
 		sb.append("',	currentIP='").append(req.getRemoteAddr());
 		sb.append("',	lastAction='").append(req.getParameter("DIRECTIVE"));
 		sb.append("',	sessionID='").append(sessionID);
 		sb.append("',	browser='").append(req.getHeader(USER_AGENT));
 		sb.append("',	statusMsg='").append(msg);
 		sb.append("'	WHERE username='").append(lr.getUsername()).append("'");
 		
 		return writeToDB(sb.toString());
 } else { return true; }		
 	}	
 	
 	void logAccess(HttpServletRequest req, String msg) {
 		super.logAccess(req,msg);
 		updateStatus(req,msg);
 	}
 	
 	boolean writeToDB(String command) {
 if (DB_FOR_LOGIN || DB_TRACK_LOGINS) {		
 		try {
 //if (DEBUG) Logger.writeln(command);			
 			if (ds == null) throw new Exception("Unable to access DataSource");
 			
 	        Connection conn = ds.getConnection();
 	        
 	        if (conn == null) throw new Exception("Unable to connect to database");	// really need a way to report that there are database problems!
 	        
 	        Statement stmt = conn.createStatement();
 			stmt.executeUpdate(command);
 			stmt.close();
 	
 	        conn.close();
 	        
 			return true;
 		}
 		catch (Exception t) {
 			Logger.writeln("SQL-ERROR on: " + command);
 			Logger.writeln(t.getMessage());
			Logger.printStackTrace(t);
 			return false;
 		}
 } else { return true; }		
 	}	
 }
 
 
 class LoginRecord {
 	static final String STATUS_UNSTARTED = "unstarted";
 	static final String STATUS_WORKING = "working";
 	static final String STATUS_COMPLETED = "completed";
 	
 	static LoginRecord NULL = new LoginRecord();
 	
 	String username = null;
 	String password = null;
 	String filename = null;
 	String instrument = null;
 	String status = null;
 	String startingStep = null;
 	Hashtable mappings  = new Hashtable();
 	
 	LoginRecord() {
 	}
 	
 	void setUsername(String username) {
 		this.username = username;
 	}
 	
 	String getUsername() {
 		return this.username;
 	}
 	
 	void setPassword(String password) {
 		this.password = password;
 	}
 	
 	String getPassword() {
 		return this.password;
 	}
 	
 	void setFilename(String filename) {
 		/* convert from Windows to Unix file separator */
 		this.filename = filename.replace('\\','/');
 	}
 	
 	String getFilename() {
 		return this.filename;
 	}
 	
 	void setInstrument(String instrument) {
 		this.instrument = instrument;
 	}
 	
 	String getInstrument() {
 		return this.instrument;
 	}
 	
 	String getStatus() {
 		return this.status;
 	}
 	
 	void setStatus(String val) {
 		this.status = val;
 	}
 	
 	boolean isUnstarted() {
 		return STATUS_UNSTARTED.equals(status);
 	}
 	
 	boolean isWorking() {
 		return STATUS_WORKING.equals(status);
 	}
 	
 	boolean isCompleted() {
 		return STATUS_COMPLETED.equals(status);
 	}
 	
 	void setStatusUnstarted() {
 		status = STATUS_UNSTARTED;
 	}
 	
 	void setStatusWorking() {
 		status = STATUS_WORKING;
 	}
 	
 	void setStatusCompleted() {
 		status = STATUS_COMPLETED;
 	}
 	
 	void setStartingStep(String s) {
 		this.startingStep = s;
 	}
 	
 	String getStartingStep() {
 		return this.startingStep;
 	}
 	
 	boolean addMapping(String varname, String value) {
 		if (varname == null || value == null) {
 			return false;
 		}
 		
 		mappings.put(varname,value);
 		return true;
 	}
 	
 	String getMapping(String varname) {
 		if (varname == null) {
 			return null;
 		}
 		else {
 			return (String) mappings.get(varname);
 		}
 	}
 	
 	Hashtable getMappings() {
 		/* assumes that they won't be changed */
 		return mappings;
 	}
 	
 	/* should I also validate that the status value is valid? */
 	boolean isValid() {
 		if (username == null || password == null || filename == null || instrument == null || status == null) {
 			return false;
 		}
 		else {
 			return true;
 		}
 	}
 	
 	String showValue() {
 		StringBuffer sb = new StringBuffer();
 		sb.append(username);
 		sb.append("\t");
 		sb.append(password);
 		sb.append("\t");
 		sb.append(filename);
 		sb.append("\t");
 		sb.append(instrument);
 		sb.append("\t");
 		sb.append(status);
 		sb.append("\t");
 		sb.append(startingStep);
 		
 		Enumeration keys = mappings.keys();
 		while (keys.hasMoreElements()) {
 			String key = (String) keys.nextElement();
 			sb.append("\t");
 			sb.append(key);
 			sb.append("\t");
 			sb.append((String) mappings.get(key));
 		}
 		
 		return sb.toString();
 	}
 }
