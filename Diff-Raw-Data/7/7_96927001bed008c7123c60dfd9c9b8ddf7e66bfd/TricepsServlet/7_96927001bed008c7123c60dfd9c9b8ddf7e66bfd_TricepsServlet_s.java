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
 
 import java.io.PrintWriter;
 import java.util.Date;
 import java.util.StringTokenizer;
 
 
 import javax.naming.*;
 import javax.sql.*;
 import java.sql.*;
 
 public class TricepsServlet extends HttpServlet implements VersionIF {
 	static final String TRICEPS_ENGINE = "TricepsEngine";
 	static final String USER_AGENT = "User-Agent";
 	static final String ACCEPT_LANGUAGE = "Accept-Language";
 	static final String ACCEPT_CHARSET = "Accept-Charset";
 	static final String CONTENT_TYPE = "text/html; charset=UTF-8";	// can make UTF-8 by default?
 	
 	/* Strings for storing / retrieving state of authentication */
 	static final String LOGIN_TOKEN = "_DlxLTok";
 	static final String LOGIN_COMMAND = "_DlxLCom";
 	static final String LOGIN_COMMAND_LOGON = "logon";
 	static final String LOGIN_IP = "_DlxLIP";
 	static final String LOGIN_USERNAME = "_DlxUname";
 	static final String LOGIN_PASSWORD = "_DlxPass";
 	static final String LOGIN_RECORD = "_DlxLRec";	
 	static final String LOGIN_BROWSER = "_DlxBrws";
 	
 	/* Strings serving as messages for login error pages - these should really be JSP */
 	static final int LOGIN_ERR_NO_TOKEN = 0;
 	static final int LOGIN_ERR_NEW_SESSION = 1;
 	static final int LOGIN_ERR_MISSING_UNAME_OR_PASS = 2;
 	static final int LOGIN_ERR_INVALID_UNAME_OR_PASS = 3;
 	static final int LOGIN_ERR_INVALID = 4;
 	static final int LOGIN_ERR_ALREADY_COMPLETED = 5;
 	static final int LOGIN_ERR_UNABLE_TO_LOAD_FILE = 6;
 	static final int LOGIN_ERR_EXPIRED_SESSION = 7;
 	static final int LOGIN_ERR_INVALID_RELOGON = 8;
 	static final int LOGIN_ERR_UNSUPPORTED_BROWSER = 9;
 	static final int LOGIN_ERR_OK = 10;
 	static final int LOGIN_ERR_FINISHED = 11;
 	static final int LOGIN_ERR_ODD_CHANGE = 12;
 		
 	static final String[] LOGIN_ERRS_BRIEF = {
 		" LOGIN_ERR_NO_TOKEN",
 		" LOGIN_ERR_NEW_SESSION",
 		" LOGIN_ERR_MISSING_UNAME_OR_PASS",
 		" LOGIN_ERR_INVALID_UNAME_OR_PASS",
 		" LOGIN_ERR_INVALID",
 		" LOGIN_ERR_ALREADY_COMPLETED",
 		" LOGIN_ERR_UNABLE_TO_LOAD_FILE", 
 		" LOGIN_ERR_EXPIRED_SESSION", 
 		" LOGIN_ERR_INVALID_RELOGON",
 		" LOGIN_ERR_UNSUPPORTED_BROWSER",
 		" OK",
 		" FINISHED",
 		" LOGIN_ERR_ODD_CHANGE",
 	};
 	
 	static final String[] LOGIN_ERRS_VERBOSE = {
 		"Please login",
 		"Please login",
 		"Please enter both your username and password",
 		"The username or password you entered was incorrect",
 		"Please login again --  You will resume from where you left off.<br><br>(Your login session was invalidated either because you accidentally pressed the browser's back button instead of the 'previous' button; or you attempted to use a bookmarked page from the instrument; or you triple-clicked an icon or button)",
 		"Thank you!  You have already completed this instrument.",
 		"Please contact the administrator -- the program was unable to load the interview: ",
 		"Please login again -- You will resume from where you left off.<br><br>(Your session expired, either because of prolonged inactivity, or because the server was restarted)",
 		"Please login again --  You will resume from where you left off.<br><br>(Your login session was invalidated because the login page was submitted twice)",
 		"Please login again -- You will resume from where you left off.<br><br>There was an unexpected error from your browser",
 		"OK",
 		"Thank you for completing this instrument",
 		"Please login again -- You will resume from where you left off.<br><br>(There was an unexpected network error)",
 	};	
 	
 	int accessCount = 0;
 
 	public void init(ServletConfig config) throws ServletException {
 		super.init(config);
 		Logger.init(config.getInitParameter("dialogix.dir"));
 		
 		if (!initDBLogging()) {
 			Logger.writeln("Unable to initialize DBLogging");
 		}
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
 			if (isSupportedBrowser(req)) {
 				result = okPage(req,res);
 			}
 			else {
 				result = errorPage(req,res);
 			}
 			if (result >= 0) { logPageHit(req,LOGIN_ERRS_BRIEF[result]); }	// way to avoid re-logging post shutdown
 		}
 		catch (OutOfMemoryError oome) {
 			Runtime.getRuntime().gc();
 if (DEBUG) Logger.writeln("##OutOfMemoryError @ Servlet.doPost()" + oome.getMessage());
 if (DEBUG) Logger.printStackTrace(oome);			
 		}
 		catch (Exception t) {
 if (DEBUG) Logger.writeln("##Exception @ Servlet.doPost()" + t.getMessage());
 if (DEBUG) Logger.printStackTrace(t);
 //			errorPage(req,res);
 		}
 	}
 	
 	boolean isSupportedBrowser(HttpServletRequest req) {
 		String userAgent = req.getHeader(USER_AGENT);
 		
 		if (userAgent == null) {
 			return false;
 		}
 		
 		if ((userAgent.indexOf("Mozilla/4") != -1)) {
 			if (userAgent.indexOf("MSIE") != -1) {
 				return true;	// IE masquerading as Netscape - finally fixed so that works OK
 			}
 			else if (userAgent.indexOf("Opera") != -1) {
 				return true;	// false;	// Opera masquerading as Netscape - problem with the event model
 			}
 			else {
 				return true;	// true for Netscape 4.x
 			}
 		}
 		else if (userAgent.indexOf("Netscape6") != -1) {
 			return true;	// false;	// does not work with Netscape6 - lousy layout, repeat calls to GET (not POST), so re-starts on each screen.  Why?
 		}
 		else if (userAgent.indexOf("Opera") != -1) {
 			return true;	// false;	// Opera - problem with the event model
 		}
 		else {
 			return true;	// false;
 		}
 	}
 
 	private int okPage(HttpServletRequest req, HttpServletResponse res) {
 		HttpSession session = req.getSession(false);
 		String sessionID = session.getId();
 		
 		TricepsEngine tricepsEngine = (TricepsEngine) session.getAttribute(TRICEPS_ENGINE);
 		if (tricepsEngine == null) {
 			tricepsEngine = new TricepsEngine(this.getServletConfig());
 			session.setAttribute(TRICEPS_ENGINE, tricepsEngine);
 		}
 		
 		logAccess(req, " OK");
 		
 		try {
 			res.setContentType(CONTENT_TYPE);
 			PrintWriter out = res.getWriter();
 			
 			tricepsEngine.doPost(req,res,out,null,null);
 						
 //			session.setAttribute(TRICEPS_ENGINE, tricepsEngine);
 
 			out.flush();
 			out.close();
 			
 			/* disable session if completed */
 			if (tricepsEngine.isFinished()) {
 				logAccess(req, " FINISHED");
 				shutdown(req,LOGIN_ERRS_BRIEF[LOGIN_ERR_FINISHED],false);	// if don't remove the session, can't login as someone new
 				return -1;
 			}			
 		}
 		catch (Exception t) {
 if (DEBUG) Logger.printStackTrace(t);
 		}
 		return LOGIN_ERR_OK;
 	}
 	
 	void logAccess(HttpServletRequest req, String msg) {
 if (DEBUG) {
 	/* 2/5/03:  Explicitly ask for session info everywhere (vs passing it as needed) */
 	HttpSession session = req.getSession(false);
 	String sessionID = session.getId();
 	TricepsEngine tricepsEngine = (TricepsEngine) session.getAttribute(TRICEPS_ENGINE);
 	Runtime rt = Runtime.getRuntime();
 	
 	/* standard Apache log format (after the #@# prefix for easier extraction) */
 	Logger.writeln("#@#(" + req.getParameter("DIRECTIVE") + ") [" + new Date(System.currentTimeMillis()) + "] " + 
 		sessionID + 
 		((WEB_SERVER) ? (" " + req.getRemoteAddr() + " \"" +
 		req.getHeader(USER_AGENT) + "\" \"" + req.getHeader(ACCEPT_LANGUAGE) + "\" \"" + req.getHeader(ACCEPT_CHARSET) + "\"") : "") +
 		((tricepsEngine != null) ? tricepsEngine.getScheduleStatus() : "") + msg + " " + (req.isSecure() ? "HTTPS" : "HTTP") +
 		" [" + rt.totalMemory() + ", " + rt.freeMemory() + "]"
 		);
 		
 // User-Agent = Mozilla/4.73 [en] (Win98; U)
 // Accept-Language = en
 // Accept-Charset = iso-8859-1,*,utf-8
 
 // User-Agent = Mozilla/4.0 (compatible; MSIE 5.5; Windows 98)
 // Accept-Language = en-us
 // User-Agent = Mozilla/5.0 (Windows; U; Win98; en-US; m18) Gecko/20001108 Netscape6/6.0
 }
 if (DEBUG && false) {
 	/* catch all sent parameters */
 	Logger.writeln("##########");
 	java.util.Enumeration params = req.getParameterNames();
 	
 	while(params.hasMoreElements()) {
 		String param = (String) params.nextElement();
 		String vals[] = req.getParameterValues(param);
 		for (int i=0;i<vals.length;++i) {
 			Logger.writeln(param + "=" + vals[i]);
 		}
 	}
 	Logger.writeln("##########");
 }
 	}
 	
 
 	int errorPage(HttpServletRequest req, HttpServletResponse res) {
 		logAccess(req, " UNSUPPORTED BROWSER");
 		try {
 			res.setContentType(CONTENT_TYPE);
 			PrintWriter out = res.getWriter();
 			
 			out.println("<!DOCTYPE html PUBLIC '-//W3C//DTD HTML 4.01 Transitional//EN'>");
 			out.println("<html>");
 			out.println("<head>");
 			out.println("<META HTTP-EQUIV='Content-Type' CONTENT='" + CONTENT_TYPE + "'>");
 			out.println("<title>Triceps Error-Unsupported Browser</title>");
 			out.println("</head>");
 			out.println("<body bgcolor='white'>");
 			out.println("   <table border='0' cellpadding='0' cellspacing='3' width='100%'>");
 			out.println("      <tr>");
 			out.println("         <td width='1%'><img name='icon' src='/images/trilogo.jpg' align='top' border='0' alt='Logo' /> </td>");
 			out.println("         <td align='left'><font SIZE='4'>Sorry for the inconvenience, but Triceps currently only works with Netscape 4.xx. and Internet Explorer 5.x<br />Please email <a href='mailto:tw176@columbia.edu'>me</a> to be notified when other browsers are supported.<br />In the meantime, Netscape 4.75 can be downloaded <a href='http://home.netscape.com/download/archive/client_archive47x.html'>here</a></font></td>");
 			out.println("      </tr>");
 			out.println("   </table>");
 			out.println("</body>");
 			out.println("</html>");
 	
 			out.flush();
 			out.close();
 		}
 		catch (Exception t) {
 if (DEBUG) Logger.printStackTrace(t);
 		}
 		return LOGIN_ERR_UNSUPPORTED_BROWSER;
 	}
 	
 	void expiredSessionErrorPage(HttpServletRequest req, HttpServletResponse res) {
 		logAccess(req, " EXPIRED SESSION");
 		try {
 			res.setContentType(CONTENT_TYPE);
 			PrintWriter out = res.getWriter();
 			
 			out.println("<!DOCTYPE html PUBLIC '-//W3C//DTD HTML 4.01 Transitional//EN'>");
 			out.println("<html>");
 			out.println("<head>");
 			out.println("<META HTTP-EQUIV='Content-Type' CONTENT='" + CONTENT_TYPE + "'>");
 			out.println("<title>Triceps Error-Expired Session</title>");
 			out.println("</head>");
 			out.println("<body bgcolor='white'>");
 			out.println("   <table border='0' cellpadding='0' cellspacing='3' width='100%'>");
 			out.println("      <tr>");
 			out.println("         <td width='1%'><img name='icon' src='/images/dialogo.jpg' align='top' border='0' alt='Logo' /> </td>");
 			out.println("         <td align='left'><font SIZE='4'>Sorry for the inconvenience, but web session you were using is no longer valid.  Either you finished an instrument, the session ran out of time, or the server was restarted.");
 			if (!WEB_SERVER) {
 				out.print("  You can resume the instrument from where you left off by clicking <a href=\"JavaScript:void;\"");
 				out.print(" onclick=\"JavaScript:window.top.open('");
 				out.print(res.encodeURL(HttpUtils.getRequestURL(req).toString()));
 				out.print("','_blank','resizable=yes,scrollbars=yes');JavaScript:top.close();\">here</a>");
 				out.print(" and selecting it from the RESTORE list.");
 			}
 			out.println("</font></td>");
 			out.println("      </tr>");
 			out.println("   </table>");
 			out.println("</body>");
 			out.println("</html>");
 	
 			out.flush();
 			out.close();
 		}
 		catch (Exception t) {
 if (DEBUG) Logger.printStackTrace(t);
 		}		
 	}
 	
 	void shutdown(HttpServletRequest req, String msg, boolean createNewSession) {
 		/* want to invalidate sessions -- even though this confuses the log issue on who is accessing from where, multiple sessions can indicate problems with user interface */
 		/* 2/5/03:  Explicitly ask for session info everywhere (vs passing it as needed) */
 		HttpSession session = req.getSession(false);
 		String sessionID = session.getId();
 		TricepsEngine tricepsEngine = (TricepsEngine) session.getAttribute(TRICEPS_ENGINE);
 				
 		Logger.writeln("...discarding session: " + sessionID + ":  " + msg);
 		
 		logPageHit(req,msg);
 		
 		if (tricepsEngine != null) {
 			tricepsEngine.getTriceps().shutdown();
 		}
 		tricepsEngine = null;
 		
 		try {
 			if (session != null) {
 				session.invalidate();	// so that retrying same session gives same message
 			}
 			
 			sessionID = null;
 			
 			if (createNewSession) {
 				/* Finally, create a new session so that session so that it is available, and so that session time-outs can be detected */
 				/* this cannot be done after the page is sent? */
 				session = req.getSession(true);	// the only place to create new sessions
 			}
 		}
 		catch (java.lang.IllegalStateException e) {
 			Logger.writeln(e.getMessage());
 		}
 	}
 	
 	/** This part is to ensure that there is an active session available */
 	
 	boolean initSession(HttpServletRequest req, HttpServletResponse res) {
 		try {
 			HttpSession session = req.getSession(true);
 			
 			if (session == null || session.isNew()) {
 				if ("POST".equals(req.getMethod())) {
 					/* an expired session */
 					logAccess(req,LOGIN_ERRS_BRIEF[LOGIN_ERR_EXPIRED_SESSION]);
 					expiredSessionErrorPage(req,res);	// this should really be a redirect to a language neutral page
 					return false;				
 				}
 				/* otherwise this is a session that requires a login page? */
 			}
 			return true;
 		} catch (Exception e) {
 if (DEBUG) Logger.printStackTrace(e);
 			return false;
 		}
 	}
 	
 	/** This part is for logging to a database **/
 	
 	protected Context ctx = null;	// this ok as global, since used on servlet-by-servlet basis
 	protected DataSource ds = null;	// this ok as global, since used on servlet-by-servlet basis
 	
 	boolean initDBLogging() {
 if (DB_FOR_LOGIN || DB_TRACK_LOGINS || DB_LOG_RESULTS) {
 		/* Load login info file from init param */
 	    try {
 	      ctx = new InitialContext();
 	      if(ctx == null ) 
 	          throw new Exception("Boom - No Context");
 	
 	      ds = (DataSource)ctx.lookup("java:comp/env/jdbc/dialogix");
 	      if(ds == null ) 
 	          throw new Exception("Boom - No DataSource");	      
 	    }catch(Exception e) {
 if (DEBUG) Logger.printStackTrace(e);
 	      return false;
 	    }
 }	    
 	    return true;
 	}	
 	
 	boolean logPageHit(HttpServletRequest req, String msg) {
 if (DB_LOG_RESULTS) {
 		HttpSession session = req.getSession(false);
 		String sessionID = null;
 		TricepsEngine tricepsEngine = null;
 		String workingFile = "null";
 		String currentStep = "null";
 		String displayCount = "null";
 		
 		if (session != null) {
 			sessionID = session.getId();
 			tricepsEngine = (TricepsEngine) session.getAttribute(TRICEPS_ENGINE);
 			if (tricepsEngine != null) {
 				currentStep = tricepsEngine.getCurrentStep();
 				
 				Triceps triceps = tricepsEngine.getTriceps();
 				if (triceps != null) {
 					displayCount = triceps.getDisplayCount();
 					
 					Logger dl = triceps.dataLogger;
 					if (dl != null) {
 						String fn = dl.getFilename();
 						if (fn != null) {
 							workingFile = fn.replace('\\','/');
 						}
 					}
 				}
 			}
 		}
 		
 		StringBuffer pageHit = new StringBuffer("INSERT INTO pageHits VALUES ");
 		StringBuffer pageHitEvents = new StringBuffer("INSERT INTO pageHitEvents VALUES ");
 		int numPageHitEvents = 0;
 		StringBuffer pageHitDetails = new StringBuffer("INSERT INTO pageHitDetails VALUES ");
 		int numPageHitDetails = 0;
 		String displayCountStr = null;
 		Runtime rt = Runtime.getRuntime();
 		synchronized(this) {
 			++accessCount;
 		}
 		int lastPageHitIndex=-1;	
 
 		pageHit.append("( NULL, NULL, '").append(accessCount).append("'");
 		pageHit.append(", '").append(req.getRemoteAddr()).append("'");
 		pageHit.append(", '").append(req.getParameter(LOGIN_USERNAME)).append("'");	
 		pageHit.append(", '").append((sessionID == null) ? "null" : sessionID).append("'");
 		pageHit.append(", '").append(workingFile).append("'");
 		pageHit.append(", '").append((tricepsEngine == null) ? "null" : tricepsEngine.getHashCode()).append("'");
 		pageHit.append(", '").append(req.getHeader(USER_AGENT)).append("'");
 		pageHit.append(", '").append((tricepsEngine == null) ? "null" : tricepsEngine.getInstrumentName().replace('\\','/')).append("'");
 		pageHit.append(", '").append(currentStep).append("'");
 		pageHit.append(", '").append(displayCount).append("'");
 		pageHit.append(", '").append(req.getParameter("DIRECTIVE")).append("'");
 		pageHit.append(", '").append(msg).append("'");
 		pageHit.append(", '").append(rt.totalMemory()).append("'");
 		pageHit.append(", '").append(rt.freeMemory()).append("'");
 		pageHit.append(")");
 		
 		// Also want to log raw input parameters to a separate database 
 		try {
 			if (ds == null) throw new Exception("Unable to access DataSource");
 			
 	        Connection conn = ds.getConnection();
 	        
 	        if (conn == null) throw new Exception("Unable to connect to database");	// really need a way to report that there are database problems!
 	        
 	        Statement stmt = conn.createStatement();
 	        int pageHitID = stmt.executeUpdate(pageHit.toString(), Statement.RETURN_GENERATED_KEYS);
 	        
 	        // extract the value of the last insert 
 	        ResultSet rst =  stmt.getGeneratedKeys();
         	rst.first();
         	lastPageHitIndex = rst.getInt(1);
         	stmt.close();
         	
 	        // now log the raw input 
 	        stmt = conn.createStatement();
 	        
 			java.util.Enumeration params = req.getParameterNames();
 			
 			while(params.hasMoreElements()) {
 				String param = (String) params.nextElement();
 				String vals[] = req.getParameterValues(param);
 				if (param.startsWith("DIRECTIVE_") || param.equals("next") || param.equals("previous")) {
 					// ignore these 
 					;
 				}
 				else if (param.equals("EVENT_TIMINGS")) {
 					for (int i=0;i<vals.length;++i) {
 						StringTokenizer lines = new StringTokenizer(vals[i],"\t",false);
 						StringTokenizer toks = null;
 						String line = null;
 						String token = null;
 						int tokenCount = 0;
 						
 						while(lines.hasMoreTokens()) {
 							if (numPageHitEvents++ > 0) {
 								pageHitEvents.append(",\n");
 							}
 							pageHitEvents.append("	(NULL, '").append(lastPageHitIndex).append("', '");
 							line = (String) lines.nextToken();
 							toks = new StringTokenizer(line,",",true);
 							tokenCount = toks.countTokens();
 							
 							tokenCount = 0;
 							while(toks.hasMoreTokens()) {
 								token = (String) toks.nextToken();
 								if (tokenCount >= 6) {
 									StringBuffer answer = new StringBuffer(token);
 									// remaining contents may contain commas, and thus be incorrectly treated as tokens
 									// so, merge remaining contents into a single value
 									while (toks.hasMoreTokens()) {
 										answer.append((String) toks.nextToken());
 									}
 									token = answer.toString();
 								}
 								else if (token.equals(",")) {
 									pageHitEvents.append("', '");
 									++tokenCount;
 									continue;
 								}
 								if (tokenCount >= 5) {
 									pageHitEvents.append(token.replace('\\','/').replace('\'','_'));
 								}
 								else {
 									pageHitEvents.append(token);
 								}
 							}
 							for (int j=tokenCount;j<6;++j) {
 								pageHitEvents.append("','");	// add any extra empty elements needed.
 							}
 							pageHitEvents.append("')");
 						}
 					}
 					pageHitEvents.append("\n");
 				}
 				else {
 					for (int i=0;i<vals.length;++i) {
 						if (i > 0 || numPageHitDetails > 0) {
 							pageHitDetails.append(",\n");	// so that can do extended inserts
 						}
 						pageHitDetails.append("	(NULL, '").append(lastPageHitIndex).append("'");
 						pageHitDetails.append(", '").append(param).append("'");
 						pageHitDetails.append(", '").append(vals[i].replace('\\','/').replace('\'','_')).append("')");
 						++numPageHitDetails;
 					}
 				}
 			}
 			if (numPageHitDetails > 0) {
 				pageHitDetails.append("\n");
 //Logger.writeln(pageHitDetails.toString());				
 				stmt.addBatch(pageHitDetails.toString());
 			}
 			if (numPageHitEvents > 0) {
 //Logger.writeln(pageHitEvents.toString());				
 				stmt.addBatch(pageHitEvents.toString());
 			}
 			stmt.executeBatch();
 			stmt.close();
 	
 	        conn.close();
 	        
 			return true;
 		}
 		catch (Exception t) {
 			Logger.writeln("SQL-ERROR: ");
 			Logger.writeln(t.getMessage());
 			return false;
 		}
 } else { return true; }		
 	}	
 }
