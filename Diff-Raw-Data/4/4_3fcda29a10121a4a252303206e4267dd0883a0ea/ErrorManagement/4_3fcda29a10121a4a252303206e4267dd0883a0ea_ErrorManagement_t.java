 package util.misc;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.mail.MessagingException;
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import om.Log;
 import om.OmException;
 import om.OmTransientQuestionException;
 import om.tnavigator.Mail;
 import om.tnavigator.NavigatorConfig;
 import om.tnavigator.UserSession;
 import om.tnavigator.db.DatabaseAccess;
 
 import org.apache.axis.AxisFault;
 import org.apache.commons.lang.StringUtils;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import util.xml.XHTML;
 import util.xml.XML;
 import util.xml.XMLException;
 
 public class ErrorManagement implements GracefulFinalization {
 
 	public static String ERROR_TEMPLATE = "WEB-INF/templates/errortemplate.xhtml";
	public static String ERROR_TEMPLATE_TXT = "WEB-INF/templates/errortemplate.txt";
 
 	public static String ADMINISTRATION_ERROR_TEMPLATE = "WEB-INF/templates/administrationErrorTemplate.xhtml";
 
 	/** Delay per which admin alert emails are sent (4 hours) */
 	private static final int ADMINALERTDELAY = 4 * 60 * 60 * 1000;
 
 	private static String SERVER_DOMAIN_SUFFIX = ".open.ac.uk";
 
 	private final static String ACCESSOUTOFSEQUENCE = "Access out of sequence";
 
 	/**
 	 * Buffer holding text of next admin alert to send, null if thread not
 	 * running
 	 */
 	private StringBuffer adminAlerts;
 
 	protected Log log;
 
 	/** Synch object for admin alerts */
 	private Object adminAlertSynch = new Object();
 
 	/** Time last alert was sent at */
 	private long lastAdminAlert = 0;
 
 	private NavigatorConfig nc;
 
 	public ErrorManagement(NavigatorConfig navigatorConfig, Log rootLog)
 		throws UtilityException {
 		if (null == navigatorConfig) {
 			throw new UtilityException("Unable to construct the"
 				+ " ErrorManagement with a null NavigatorConfig");
 		}
 		nc = navigatorConfig;
 		log = rootLog;
 	}
 
 	private void log(String msg, Exception x) {
 		if (null != log) {
 			if (null != x) {
 				log.logError(msg, x);
 			} else {
 				log.logDebug(msg);
 			}
 		} else {
 			if (null != x) {
 				x.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * For handling errors that arise within the Administration area.  Uses the
 	 *  template provided in the  in order to render a response.
 	 * @param request
 	 * @param response
 	 * @param visitor
 	 * @param emp
 	 * @param replacements
 	 * @throws OMException
 	 * @throws IOException
 	 * @author Trevor Hinson
 	 */
 	public void sendErrorResponse(HttpServletRequest request,
 		HttpServletResponse response, OMVisitor visitor, ErrorMessageParts emp,
 		Map<String, String> replacements) throws OmException, IOException {
 		Document d = XML.parse(new File(visitor.getServletContext()
 			.getRealPath(emp.getErrorTemplateReference())));
 		XML.replaceTokens(d, replacements);
 		XHTML.output(d, request, response, "en");
 		sendErrorMessage(request, true, replacements, visitor.getServletContext(), emp);
 	}
 
 	private void sendErrorMessage(HttpServletRequest request, boolean isBug,
 		Map<String, String> replacements, ServletContext context,
 		ErrorMessageParts emp) {
 		sendErrorMessage(request, isBug, replacements, context,
 			emp.getThrowable(), emp.getErrorTemplateReference());
 	}
 
 	/**
 	 * Sends an admin alert message by email. Messages are automatically queued
 	 * so it doesn't send more than one per ADMINALERTDELAY. Time is
 	 * automatically recorded.
 	 * 
 	 * @param sMessage
 	 *            Message text; will be included in one line after the date. Can
 	 *            include a multi-line message with \n if necessary
 	 * @param tException
 	 *            Exception to trace (optional, null if none)
 	 */
 	public void sendAdminAlert(String sMessage, Throwable tException) {
 		// Don't send any mails if nobody is receiving them!
 		if (nc.getAlertMailTo().length == 0)
 			return;
 
 		synchronized (adminAlertSynch) {
 			boolean bNew = false;
 			if (adminAlerts == null) {
 				adminAlerts = new StringBuffer();
 				bNew = true;
 			}
 			adminAlerts
 				.append("================================================\n")
 				.append(Log.DATETIMEFORMAT.format(new Date()))
 				.append("\n").append(sMessage).append("\n\n")
 				.append((tException == null ? "" 
 					: (Log.getOmExceptionString(tException) + "\n\n")));
 			if (bNew && (System.currentTimeMillis() - lastAdminAlert) > ADMINALERTDELAY) {
 				sendAdminAlertNow();
 			} else if (bNew) {
 				new AdminAlertLater();
 			}
 		}
 	}
 
 	/** @return A 'friendly' version of the server's own name, e.g. pclt166/om-tn */
 	private String getFriendlyServerName(NavigatorConfig nc) {
 		return nc.getThisTN().getHost().replaceAll(SERVER_DOMAIN_SUFFIX, "")
 				+ nc.getThisTN().getPath();
 	}
 
 	/** Sends the actual email */
 	public void sendAdminAlertNow() {
 		try {
 			synchronized (adminAlertSynch) {
 				lastAdminAlert = System.currentTimeMillis();
 				adminAlerts
 					.append("================================================\n"
 						+ "Next email will not be sent until "
 						+ Log.TIMEFORMAT.format(new Date(lastAdminAlert
 						+ ADMINALERTDELAY)) + "\n");
 
 				// Send mail now
 				Mail.send(nc.getAlertMailFrom(), null, nc.getAlertMailTo(), nc
 						.getAlertMailCC(), "Om alert: "
 						+ getFriendlyServerName(nc), adminAlerts.toString(),
 						Mail.TEXTPLAIN);
 				adminAlerts = null;
 			}
 		} catch (MessagingException me) {
 			log("Admin alerts.  Failed to send message", me);
 		}
 	}
 
 	/** Thread that hangs around for a bit then sends alert message */
 	class AdminAlertLater extends Thread {
 		
 		AdminAlertLater() {
 			start();
 		}
 
 		@Override
 		public void run() {
 			try {
 				Thread.sleep(ADMINALERTDELAY);
 			} catch (InterruptedException e) {
 			}
 
 			sendAdminAlertNow();
 		}
 	}
 
 	@Override
 	public FinalizedResponse close(Object o) throws UtilityException {
 		if (null != log) {
 			log.close();
 		}
 		return new StandardFinalizedResponse(true);
 	}
 	
 	private void handleExceptionDetails(ErrorMessageParts emp) {
 		String TEMPPROBLEM = "(This error indicates a temporary problem with our "
 			+ "systems; wait a minute then try again.)";
 		if (null != emp) {
 			if (null != emp.getThrowable() ? emp.getThrowable() instanceof SQLException : false) {
 				if (emp.getThrowable().getMessage().indexOf("Connection reset") != -1) {
 					emp.setTitle("Database connection lost");
 					emp.setMessage("A required database connection has been lost. (Normally "
 							+ "this error goes away after a reload. If it persists, it may "
 							+ "indicate a problem with our systems; try again later.)");
 					emp.setThrowable(null);
 				} else if (emp.getThrowable().getMessage().indexOf(
 					"Error establishing socket") != -1) {
 					emp.setTitle("Database connection fault");
 					emp.setMessage("Could not connect to a required database. "
 						+ TEMPPROBLEM);
 					emp.setThrowable(null);
 				}
 			} else if (null != emp.getThrowable() ? emp.getThrowable() instanceof AxisFault : false) {
 				String throwableMessage = emp.getThrowable().getMessage();
 				if (StringUtils.isNotEmpty(throwableMessage) ? throwableMessage.indexOf(
 					"java.net.SocketTimeoutException") != -1
 					|| throwableMessage.indexOf(
 						"This application is not currently available") != -1
 					|| throwableMessage.indexOf("java.net.ConnectException") != -1 : false) {
 					emp.setTitle("Question engine connection fault");
 					emp.setMessage("The system could not connect to a required component. "
 						+ TEMPPROBLEM);
 					// exception=null; Leave exception to display - we are
 					// getting inexplicable errors.
 				} else {
 					Throwable cause = emp.getThrowable().getCause();
 					if (cause != null
 							&& cause instanceof OmTransientQuestionException) {
 						emp.setTitle("Temporary problem at the question engine");
 						if (cause.getMessage() != null) {
 							emp.setMessage(cause.getMessage());
 						}
 						emp.setMessage(emp.getMessage() + TEMPPROBLEM);
 						emp.setThrowable(null);
 						emp.setResetTestPosition(true);
 					} else {
 						emp.setTitle("Question engine fault");
 						emp.setMessage("An error occurred in the question or a required system "
 							+ "component.");
 						// Leave exception to display; this could be a question
 						// developer error.
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Sends an error to the student, logs it, and kills their session. Does not
 	 * return but throws a StopException in order to abort further processing.
 	 * 
 	 * @param us
 	 *            Session to be killed (null if we should keep it)
 	 * @param request
 	 *            HTTP request
 	 * @param response
 	 *            HTTP response
 	 * @param code
 	 *            Desired HTTP status code;
 	 *            HttpServletResponse.SC_INTERNAL_SERVER_ERROR is treated
 	 *            differently in that these are logged as errors, not warnings
 	 * @param isBug
 	 *            Set true if this is a bug and should be logged as such,
 	 *            complete with the option for users to re-enter test. Generally
 	 *            I have erred on the side of setting this true...
 	 * @param keepSession
 	 *            normally, this should be false, and OM will throw away the
 	 *            user's session in case it was corrupted in a way that caused
 	 *            the error. Then on the next request it will silently give them
 	 *            a new session. But very occasionally we don't want this
 	 *            behaviour in which case this parameter can be set to true.
 	 * @param backToTest
 	 *            If set to test ID (not null), this forces the back-to-test
 	 *            option to appear.
 	 * @param title
 	 *            Title of message
 	 * @param message
 	 *            Actual message
 	 * @param exception
 	 *            Any exception that should be reported (null if none)
 	 * @throws StopException
 	 *             Always, to abort processing
 	 */
 	public void sendErrorForUserSession(UserSession us, HttpServletRequest request,
 		HttpServletResponse response, int code, boolean isBug,
 		String backToTest, OMVisitor visitor, ErrorMessageParts emp)
 		throws StopException {
 		boolean resetTestPosition = false;
 		// Detect particular errors that we want to make more friendly
 		if (null != emp ? null != emp.getThrowable() && null != emp.getMessage() : false) {
 			handleExceptionDetails(emp);
 		}
 		UsersPositionUpdate upu = new UsersPositionUpdate(backToTest, false);
 		handleUsersDatabasePosition(us, emp, resetTestPosition, visitor, isBug, upu);
 		boolean bRemovedBackto = false;
 		Map<String, String> m = new HashMap<String, String>();
 		
 		try {
 			response.setStatus(code);
 			Document d = XML.parse(new File(visitor.getServletContext().getRealPath(
 				ERROR_TEMPLATE)));
 			m.put("TITLE", emp.getTitle());
 			m.put("MESSAGE", emp.getMessage());
 			m.put("STATUSCODE", code + "");
 			String sOUCU = visitor.getAuthentication()
 				.getUncheckedUserDetails(request).getUsername();
 			m.put("OUCU", sOUCU == null ? "[not logged in]" : sOUCU);
 			m.put("REQUEST", request.getPathInfo()
 					+ (request.getQueryString() == null ? "" : "?"
 							+ request.getQueryString()));
 			m.put("TIME", Log.DATETIMEFORMAT.format(new Date()));
 			m.put("ACCESSCSS", RequestHelpers.getAccessCSSAppend(request));
 			if (emp.getTitle() != ACCESSOUTOFSEQUENCE) {
 				XML.remove(XML.find(d, "id", "accessoutofsequence"));
 			} else {
 				upu.sTestID = null != us ? us.getTestId() : backToTest;
 			}
 			if (null != us ? null != us.oss : false) {
 				m.put("QENGINE", RequestHelpers.displayServletURL(us.oss.getEngineURL())
 						+ " (" + us.oss.getQuestionID() + "."
 						+ us.oss.getQuestionVersion() + ")");
 			} else {
 				m.put("QENGINE", "n/a");
 				XML.remove(XML.find(d, "id", "qengine"));
 			}
 			if (null != us) {
 				m.put("TINDEX", "" + us.getTestPosition());
 				m.put("TSEQ", "" + us.iDBseq);
 			} else {
 				XML.remove(XML.find(d, "id", "indseq"));
 			}
 			m.put("TNAVIGATOR", RequestHelpers.displayServletURL(nc.getThisTN()));
 			String sAccess = RequestHelpers.getAccessibilityCookie(request);
 			if (sAccess.equals("")) {
 				XML.remove(XML.find(d, "id", "access"));
 				m.put("ACCESS", "n/a");
 			} else {
 				m.put("ACCESS", sAccess);
 			}
 			if (null == emp.getThrowable()) {
 				XML.remove(XML.find(d, "id", "exception"));
 			} else {
 				m.put("EXCEPTION", Log.getOmExceptionString(emp.getThrowable()));
 			}
 			m.put("TESTURL", RequestHelpers.getServletURL(request) + upu.sTestID + "/"
 					+ RequestHelpers.endOfURL(request));
 			if (backToTest == null
 					&& (upu.sTestID == null || emp.getTitle() == ACCESSOUTOFSEQUENCE)) {
 				XML.remove(XML.find(d, "id", "backtotest"));
 				bRemovedBackto = true;
 			} else {
 				if (upu.bClearedPosition) {
 					XML.remove(XML.find(d, "id", "keptposition"));
 				} else {
 					XML.remove(XML.find(d, "id", "clearedposition"));
 				}
 				if (code == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
 					String[] asOther = nc.getOtherNavigators();
 					if (asOther.length == 0)
 						XML.remove(XML.find(d, "id", "otherservers"));
 					else {
 						Element eUL = XML.find(d, "id", "otherserverlist");
 						for (int i = 0; i < asOther.length; i++) {
 							Element eLI = XML.createChild(eUL, "li");
 							XML.createText(eLI, "Alternate server: ");
 							Element eA = XML.createChild(eLI, "a");
 							XML.createText(eA, asOther[i].replaceAll(
 									"\\.open\\.ac\\.uk.*$", "").replaceAll(
 									"http://", ""));
 							eA.setAttribute("href", asOther[i] + upu.sTestID + "/"
 									+ RequestHelpers.endOfURL(request));
 						}
 					}
 				} else {
 					XML.remove(XML.find(d, "id", "otherservers"));
 				}
 			}
 			if (isBug) {
 				XML.remove(XML.find(d, "id", "notbug"));
 			} else {
 				if (!bRemovedBackto && backToTest == null)
 					XML.remove(XML.find(d, "id", "backtotest"));
 			}
 			XML.replaceTokens(d, m);
 			XHTML.output(d, request, response, "en");
 			sendErrorMessage(request, isBug, m, visitor.getServletContext(),
				emp.getThrowable(), ERROR_TEMPLATE_TXT);
 		} catch (OmException x) {
 			log.logError("The OMVisitor needs to be properly constructed.", x);
 		} catch (XMLException x) {
 			log.logError("Errors", "Error processing error template", x);
 		} catch (IOException ioe) {
 			// Ignore exception, they must have closed browser or something
 		}
 		throw new StopException();
 	}
 
 	class UsersPositionUpdate {
 		
 		String sTestID;
 		
 		boolean bClearedPosition = false;
 		
 		UsersPositionUpdate(String backToTest, boolean b) {
 			sTestID = backToTest;
 			bClearedPosition = b;
 		}
 		
 	}
 
 	/**
 	 * Legacy -> If they just came in and it crashed, and they're on a question
 	 * page, let's forget the position too. Conditions:
 	 * Error is caused by a bug, not permission failure etc
 	 * The test allows navigation (otherwise we shouldn't change their position)
 	 * @param us
 	 * @param emp
 	 * @param resetTestPosition
 	 * @param visitor
 	 * @param isBug
 	 * @param upu
 	 * @throws StopException
 	 */
 	private void handleUsersDatabasePosition(UserSession us, ErrorMessageParts emp,
 		boolean resetTestPosition, OMVisitor visitor, boolean isBug,
 		UsersPositionUpdate upu) throws StopException {
 		if (null != us && null != upu ? emp.getTitle() != ACCESSOUTOFSEQUENCE : false) {
 			if (isBug
 				&& us.getDbTi() > 0
 				&& us.getTestDefinition() != null
 				&& us.getTestDefinition().isNavigationAllowed()
 				&& ((System.currentTimeMillis() - us.lSessionStart) < 5000 || resetTestPosition)) {
 				DatabaseAccess.Transaction dat = null;
 				try {
 					dat = visitor.getDatabaseAccess().newTransaction();
 					visitor.getOmQueries().updateSetTestPosition(dat, us.getDbTi(), 0);
 					upu.bClearedPosition = true;
 				} catch (OmException x) {
 					log.logError("Problem with the OmVisitor construction.", x);
 					throw new StopException();
 				} catch (SQLException se) {
 					// Damn, something maybe wrong with database?
 				} finally {
 					if (dat != null) {
 						dat.finish();
 					}
 				}
 			}
 			if (upu.sTestID == null) {
 				upu.sTestID = us.getTestId();
 			}
 		}
 	}
 
 	/**
 	 * Legacy -> deals with sending the message via email if it is a bug or
 	 *  otherwise writes the message to the log.
 	 * @param request
 	 * @param isBug
 	 * @param m
 	 * @param context
 	 * @param exception
 	 * @param templateLocation
 	 */
 	public void sendErrorMessage(HttpServletRequest request, boolean isBug,
 		Map<String, String> m, ServletContext context, Throwable exception,
 		String templateLocation) {
 		// Get requested path too, for use in logged/displayed error
 		String sPath = request.getPathInfo();
 		if (sPath == null)
 			sPath = "";
 		if (request.getQueryString() != null)
 			sPath += "?" + request.getQueryString();
 		m.put("REQUEST", sPath);
 		String sMessageSummary = "??";
 		try {
 			sMessageSummary = IO.loadString(new FileInputStream(
 				context.getRealPath(templateLocation)));
 		} catch (IOException ioe) {
 			// Ignore
 		}
 		sMessageSummary = XML.replaceTokens(sMessageSummary, "%%", m);
 		if (isBug) {
 			log.logError("Displayed error", sMessageSummary, exception);
 			// Optionally send email - output is already sent to user so no need
 			// to worry if it takes a while
 			sendAdminAlert(sMessageSummary, exception);
 		} else
 			log.logWarning("Displayed error", sMessageSummary, exception);
 	}
 }
