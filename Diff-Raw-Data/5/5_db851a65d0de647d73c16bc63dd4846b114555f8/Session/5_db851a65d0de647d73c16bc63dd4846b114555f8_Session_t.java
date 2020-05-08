 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: Session.java,v 1.11 2007-12-11 22:03:55 subashvarma Exp $
  *
  * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.iplanet.dpro.session;
 
 import com.iplanet.am.util.SystemProperties;
 import com.iplanet.dpro.session.service.SessionService;
 import com.iplanet.dpro.session.share.SessionBundle;
 import com.iplanet.dpro.session.share.SessionEncodeURL;
 import com.iplanet.dpro.session.share.SessionInfo;
 import com.iplanet.dpro.session.share.SessionRequest;
 import com.iplanet.dpro.session.share.SessionResponse;
 import com.iplanet.services.comm.client.PLLClient;
 import com.iplanet.services.comm.share.Request;
 import com.iplanet.services.comm.share.RequestSet;
 import com.iplanet.services.comm.share.Response;
 import com.iplanet.services.naming.WebtopNaming;
 import com.sun.identity.common.SearchResults;
 import com.sun.identity.shared.Constants;
 import com.sun.identity.shared.debug.Debug;
 import com.sun.identity.session.util.RestrictedTokenAction;
 import com.sun.identity.session.util.RestrictedTokenContext;
 import com.sun.identity.session.util.SessionUtils;
 import java.net.InetAddress;
 import java.net.URL;
 import java.util.Hashtable;
 import java.util.Vector;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * The <code>Session</code> class represents a session. It contains session
  * related information such as session ID, session type (user/application),
  * client ID (user ID or application ID), session idle time, time left on the
  * session, and session state. It also allows applications to add listener for
  * session events.
  * 
  * <pre>
  *  The following is the state diagram for a session:
  * 
  *                     |
  *                     |
  *                     |
  *                     V
  *       ---------- invalid
  *      |              |  
  *      |              |creation (authentication OK)
  *      |              |
  *      |max login time|   max idle time
  *      |destroy       V  ---------------&gt;
  *      |            valid              inactive --
  *      |              |  &lt;--------------           |
  *      |              |       reactivate           |
  *      |              |                            |
  *      |              | logout                     | destroy
  *      |              | destroy                    | max session time
  *      |              | max session time           | 
  *      |              V                            |
  *       ---------&gt;  destroy  &lt;---------------------       
  * 
  * </pre>
  * 
  * @see com.iplanet.dpro.session.SessionID
  * @see com.iplanet.dpro.session.SessionListener
  */
 
 public class Session {
 
     /**
      * Used for uniquely referencing this Session object.
      */
     private SessionID sessionID;
 
     /**
      * Defines the type of Session that has been created. Where 0 for User
      * Session; and 1 for Application Session.
      */
     private int sessionType;
 
     /**
      * Identification string of the Client using this Session.
      */
     private String clientID;
 
     /**
      * Name of the Domain or Organization through which the User/Client has been
      * Authenticated.
      */
     private String clientDomain;
 
     /**
      * Total Maximum time allowed for the session, in minutes.
      */
     private long maxSessionTime;
 
     /**
      * Maximum idle time allowed for the session, in minutes.
      */
     private long maxIdleTime;
 
     /**
      * Maximum time for which the cached session is used, in minutes.
      */
     private long maxCachingTime;
 
     /**
      * The time for which the session has been idle, in seconds.
      */
     private long sessionIdleTime;
 
     /**
      * Total time left for the session, in seconds.
      */
     private long sessionTimeLeft;
 
     /*
      * This is the time value (computed as System.currentTimeMillis()/1000) when
      * the session timed out. Value zero means the session has not timed out.
      */
     private long timedOutAt = 0;
 
     /**
      * Four possible values for the state of the session 0 - Invalid 1 - 
      * Valid 2 - Inactive 3 - Destroyed
      */
     private int sessionState;
 
     /**
      * All session related properties are stored as key-value pair in this
      * table.
      */
     private Hashtable sessionProperties = new Hashtable();
 
     /**
      * URL of the Session Server, where this session resides.
      */
     private URL sessionServiceURL;
 
     /**
      * Type of the Event
      */
     private int eventType = -1;
 
     /**
      * Last time the client sent a request associated with this session, as the
      * number of seconds since midnight January 1, 1970 GMT.
      */
     private long latestRefreshTime;
 
     /**
      * Session Tracking Cookie Name
      */
     private static final String httpSessionTrackingCookieName = SystemProperties
             .get(Constants.AM_SESSION_HTTP_SESSION_TRACKING_COOKIE_NAME,
                     "JSESSIONID");
 
     /**
      * Indicates whether the latest access time need to be reset on the session.
      */
     boolean needToReset = false;
 
     private SessionService sessionService = null;
 
     /*
      * indicates whether to use local or remote calls to Session Service
      * 
      */
     private boolean sessionIsLocal = false;
 
     public static final int INVALID = 0;
 
     public static final int VALID = 1;
 
     public static final int INACTIVE = 2;
 
     public static final int DESTROYED = 3;
 
     public static final int USER_SESSION = 0;
 
     public static final int APPLICATION_SESSION = 1;
 
     public static final String SESSION_HANDLE_PROP = "SessionHandle";
 
     public static final String TOKEN_RESTRICTION_PROP = "TokenRestriction";
 
     public static final String SESSION_SERVICE = "session";
 
     private static String cookieName = 
 	SystemProperties.get("com.iplanet.am.cookie.name");
 
     public static final String lbCookieName = 
         SystemProperties.get(Constants.AM_LB_COOKIE_NAME,"amlbcookie");
 
     private static final boolean resetLBCookie = 
         Boolean.valueOf(SystemProperties.
                  get("com.sun.identity.session.resetLBCookie", "false"))
                   .booleanValue();
     
     private String cookieStr;
 
     Boolean cookieMode = null;
 
     private TokenRestriction restriction = null;
 
     private Object context = null;
 
     /**
      * This is the maximum extra time for which the timed out sessions can live
      * in the session server
      */
     private static long purgeDelay;
 
     // Debug instance
     private static Debug sessionDebug = Debug.getInstance("amSession");
 
     /**
      * Indicates whether session to use polling or notifications to clear the
      * client cache
      */
     private static boolean pollingEnabled = Boolean.valueOf(SystemProperties
                      .get("com.iplanet.am.session.client.polling.enable"))
                      .booleanValue();
 
     /**
      * Session Poller. Invoke this only when the users choose polling method for
      * the session cache updation
      */
     private static Thread sp;
 
     /**
      * The session table indexed by Session ID objects.
      */
     private static Hashtable sessionTable = new Hashtable();
 
     /**
      * The session service URL table indexed by server address contained in the
      * Session ID object.
      */
     private static Hashtable sessionServiceURLTable = new Hashtable();
 
     /**
      * Vector of session event listeners for THIS session only
      */
     private Vector sessionEventListeners = new Vector();
 
     /**
      * Vector of session event listeners for ALL sessions
      */
     private static Vector allSessionEventListeners = new Vector();
 
     /**
      * This is used only in polling mode to find the polling state of this
      * session.
      */
     private boolean isPolling = false;
 
     private static String serverID = null;
 
     static {
         try {
             serverID = WebtopNaming.getAMServerID();
         } catch (Exception le) {
             serverID = null;
         }
 
         String purgeDelayProperty = SystemProperties.get(
                 "com.iplanet.am.session.purgedelay", "120");
         try {
             purgeDelay = Long.parseLong(purgeDelayProperty);
         } catch (Exception le) {
             purgeDelay = 120;
         }
 
         if (pollingEnabled) {
             synchronized (Session.class) {
                 if (sp == null) {
                     sp = new SessionPoller(sessionTable);
                     sp.setName("amSessionPollerMain");
                     sp.setDaemon(true);
                     sp.start();
                 }
             }
         }
     }
 
     /**
      * Enables the Session Polling
      * @param b if <code>true</code> polling is enabled, disabled otherwise
      */
     protected void setIsPolling(boolean b) {
         isPolling = b;
 
     }
 
     /**
      * Checks if Polling is enabled
      * @return <code> true if polling is enabled , <code>false<code> otherwise
      */
     protected boolean getIsPolling() {
         return isPolling;
     }
 
     /*
      * Used in this package only.
      */
     Session(SessionID sid) {
         sessionID = sid;
         if (isServerMode()) {
             sessionService = SessionService.getSessionService();
         }
         latestRefreshTime = System.currentTimeMillis() / 1000;
     }
 
     /**
      * Returns cookie name for the Session
      * @return cookie name
      */
     public static String getCookieName() {
         return cookieName;
     }
 
     /**
      * Returns load balancer cookie value for the Session.
      *
      * @param sid Session string for load balancer cookie.
      * @return load balancer cookie value.
      * @throws SessionException if session is invalid.
      */
     public static String getLBCookie(String sid) 
              throws SessionException {
         return getLBCookie(new SessionID(sid));
     }
 
     /**
      * Returns load balancer cookie value for the Session.
      * @param  sid Session ID for load balancer cookie.
      * @return load balancer cookie value.
      * @throws SessionException if session is invalid.
      */
     public static String getLBCookie(SessionID sid)
              throws SessionException {
         String cookieValue = null;
         if(sid == null || sid.toString() == null || 
             sid.toString().length() == 0) {
             throw new SessionException(SessionBundle.rbName, 
         	    "invalidSessionID", null);
         }
          
         if(resetLBCookie) {
             if (isServerMode()) {
                 SessionService ss = SessionService.getSessionService();            
                if (ss.isSessionFailoverEnabled() && ss.isLocalSite(sid)) {
                     cookieValue = ss.getCurrentHostServer(sid);
                 }    
             } else {            
                 Session sess = (Session) sessionTable.get(sid);
                 if (sess != null) {
                     cookieValue = sess.getProperty(lbCookieName);
                 }
             }
         }    
         
         if(cookieValue == null || cookieValue.length() == 0) {
             cookieValue = sid.getExtension(SessionID.PRIMARY_ID);
         }
         return lbCookieName + "=" + cookieValue;
     }
     
     /**
      * Returns the session ID.
      * @return The session ID.
      */
     public SessionID getID() {
         return sessionID;
     }
 
     /**
      * Returns the session type.
      * 
      * @return The session type.
      */
     public int getType() {
         return sessionType;
     }
 
     /**
      * Returns the client ID in the session.
      * 
      * @return The client ID in the session.
      */
     public String getClientID() {
         return clientID;
     }
 
     /**
      * Returns the client domain in the session.
      * 
      * @return The client domain in the session.
      */
     public String getClientDomain() {
         return clientDomain;
     }
 
     /**
      * Returns the maximum session time in minutes.
      * 
      * @return The maximum session time.
      */
     public long getMaxSessionTime() {
         return maxSessionTime;
     }
 
     /**
      * Returns the maximum session idle time in minutes.
      * 
      * @return The maximum session idle time.
      */
     public long getMaxIdleTime() {
         return maxIdleTime;
     }
 
     /**
      * Returns true if the session has timed out.
      * @return <code>true</code> if session timed out,
      *         <code>false</code>otherwise
      * @exception SessionException
      */
     public boolean isTimedOut() throws SessionException {
         /**
          * Before going to the server, check if the session has been already
          * marked TimedOut or not.
          */
         if (timedOutAt > 0) {
             return true;
         }
         if (maxCachingTimeReached()) {
             try {
                 refresh(false);
             } catch (SessionTimedOutException e) {
                 latestRefreshTime = System.currentTimeMillis() / 1000;
                 timedOutAt = latestRefreshTime; //
             }
         }
         return timedOutAt > 0;
     }
 
     /**
      * Returns the extra time left(in seconds) for the client Session after the
      * session timed out. If extra time left is zero, it means the session is
      * ready to be removed permanently. If it returns -1 it means the session
      * did not even reached the time out state.
      * @return <code>Session</code> Purge time left
      * @exception <code>SessionException</code>
      */
     public long getTimeLeftBeforePurge() throws SessionException {
         /**
          * Return -1 if the session has not timed out due to idle/max timeout
          * period.
          */
         if (!isTimedOut()) {
             return -1;
         }
         /**
          * Return the extra time left, if the session has timed out due to
          * idle/max time out period
          */
         long now = System.currentTimeMillis() / 1000;
         long left = (timedOutAt + purgeDelay * 60 - now);
         return (left > 0) ? left : 0;
     }
 
     /**
      * Returns the maximum session caching time in minutes.
      * 
      * @return The maximum session caching time.
      */
     public long getMaxCachingTime() {
         return maxCachingTime;
     }
 
     /**
      * Returns the session idle time in seconds.
      * 
      * @return The session idle time.
      * @exception SessionException if the session reached its maximum session
      *            time, or the session was destroyed, or there was an error
      *            during communication with session service.
      */
     public long getIdleTime() throws SessionException {
         if (maxCachingTimeReached())
             refresh(false);
         return sessionIdleTime;
     }
 
     /**
      * Returns the time left for this session in seconds.
      * 
      * @return The time left for this session.
      * @exception SessionException is thrown if the session reached its
      *            maximum session time, or the session was destroyed, or
      *            there was an error during communication with session
      *            service.
      */
     public long getTimeLeft() throws SessionException {
         if (maxCachingTimeReached())
             refresh(false);
         return sessionTimeLeft;
     }
 
     /**
      * Returns the state of the session.
      * 
      * @param reset
      *            This parameter indicates that whether the Session Service
      *            needs to reset the latest access time on this session.
      * @return The state of the session. The session state is one of the
      *         following: <code>INVALID, VALID, INACTIVE, and DESTROYED</code>.
      * @exception SessionException is thrown if the session reached its
      *            maximum session time, or the session was destroyed, or
      *            there was an error during communication with session
      *            service.
      */
     public int getState(boolean reset) throws SessionException {
         if (maxCachingTimeReached()) {
             refresh(reset);
         } else {
             if (reset) {
                 needToReset = true;
             }
         }
         return sessionState;
     }
 
     private void setState(int state) {
         sessionState = state;
     }
 
     /**
      * Returns the type of the event which caused the state change of this
      * session.
      * 
      * @return The type of the event. The event types are defined in class
      *         SessionEvent as static integers : SESSION_CREATION, IDLE_TIMEOUT,
      *         MAX_TIMEOUT, LOGOUT, ACTIVATION, REACTIVATION, and DESTROY.
      */
     public int getEventType() {
         return eventType;
     }
 
     /**
      * Gets the property stored in this session.
      * 
      * @param name The property name.
      * @return The property value in String format.
      * @exception SessionException is thrown if the session reached its
      *            maximum session time, or the session was destroyed, or
      *            there was an error during communication with session
      *            service.
      */
     public String getProperty(String name) throws SessionException {
         if (name != lbCookieName) {
             if (maxCachingTimeReached() || 
         	!sessionProperties.containsKey(name)) {
                 refresh(false);
             }
         } 
         
         return (String) sessionProperties.get(name);
     }
 
     /**
      * Sets a property for this session.
      * 
      * @param name The property name.
      * @param value The property value.
      * @exception SessionException if the session reached its maximum session
      *            time, or the session was destroyed, or there was an error
      *            during communication with session service.
      */
     public void setProperty(String name, String value) throws SessionException {
         try {
             if (isLocal()) {
                 sessionService.setProperty(sessionID, name, value);
             } else {
                 SessionRequest sreq = new SessionRequest(
                        SessionRequest.SetProperty, sessionID.toString(), false);
                 sreq.setPropertyName(name);
                 sreq.setPropertyValue(value);
                 getSessionResponse(getSessionServiceURL(), sreq);
             }
             sessionProperties.put(name, value);
         } catch (Exception e) {
             throw new SessionException(e);
         }
     }
 
     /**
      * Gets the Session Service URL for this session object.
      * 
      * @return The Session Service URL for this session.
      * @exception SessionException when cannot get Session URL.
      */
     public URL getSessionServiceURL() throws SessionException {
         if (isServerMode()) {
             return getSessionServiceURL(sessionID);
         }
 
         // we can cache the result because in client mode
         // session service location does not change
         // dynamically
         if (sessionServiceURL == null) {
             sessionServiceURL = getSessionServiceURL(sessionID);
         }
 
         return sessionServiceURL;
     }
 
     /**
      * Destroys a session.
      * 
      * @param session The session to be destroyed.
      * @exception SessionException if there was an error during
      *            communication with session service, or the corresponding
      *            session reached its maximum session/idle time, or the
      *            session was destroyed.
      */
     public void destroySession(Session session) throws SessionException {
         try {
             if (session.isLocal()) {
                 sessionService.destroySession(this, session.getID());
                 removeSID(session.getID());
             } else {
                 SessionRequest sreq = new SessionRequest(
                         SessionRequest.DestroySession, sessionID.toString(),
                         false);
                 sreq.setDestroySessionID(session.getID().toString());
                 session
                         .getSessionResponse(session.getSessionServiceURL(),
                                 sreq);
                 removeSID(session.getID());
             }
 
         } catch (Exception e) {
             throw new SessionException(e);
         }
     }
 
     /**
      * Logs out a session.
      * 
      * @throws SessionException if there was an error during communication
      *         with session service. If the session logged out already,
      *         no exception will be thrown.
      */
     public void logout() throws SessionException {
         try {
             if (isLocal()) {
                 sessionService.logout(sessionID);
                 removeSID(sessionID);
             } else {
                 SessionRequest sreq = new SessionRequest(SessionRequest.Logout,
                         sessionID.toString(), false);
                 getSessionResponse(getSessionServiceURL(), sreq);
                 removeSID(sessionID);
             }
 
         } catch (Exception e) {
             throw new SessionException(e);
         }
     }
 
     /**
      * Removes the <code>SessionID</code> from session table.
      *
      * @param sid Session ID.
      */
     protected static void removeSID(SessionID sid) {
 
         Session session = null;
         session = (Session) sessionTable.remove(sid);
 
         if (session != null) {
             session.setState(Session.DESTROYED);
             long eventTime = System.currentTimeMillis();
             SessionEvent event = new SessionEvent(session,
                     SessionEvent.DESTROY, eventTime);
             invokeListeners(event);
         }
     }
 
    /** 
     * Invokes the Session Listener.
     * @param evt Session Event.
     */
    protected static void invokeListeners(SessionEvent evt) {
         Session session = evt.getSession();
         Vector sess_listeners = session.getSessionEventListeners();
         Vector all_listeners = Session.getAllSessionEventListeners();
 
         // THIS SESSION FIRST ...
         for (int i = 0; i < sess_listeners.size(); i++) {
             SessionListener listener = (SessionListener) sess_listeners
                     .elementAt(i);
             listener.sessionChanged(evt);
         }
 
         // ALL SESSIONS
         for (int i = 0; i < all_listeners.size(); i++) {
             SessionListener listener = (SessionListener) all_listeners
                     .elementAt(i);
             listener.sessionChanged(evt);
         }
     }
 
     /**
      * Adds a session listener for session change events.
      * 
      * @param listener Session Listener object.
      * @exception SessionException if the session state is not valid.
      */
     public void addSessionListener(SessionListener listener)
             throws SessionException {
         if (sessionState != Session.VALID) {
             throw new SessionException(SessionBundle.rbName,
                     "invalidSessionState", null);
         }
         sessionEventListeners.addElement(listener);
     }
 
     /**
      * Returns a session based on a Session ID object.
      * 
      * @param sid Session ID.
      * @return A Session object.
      * @throws SessionException if the Session ID object does not contain a
      *         valid session string, or the session string was valid before
      *         but has been destroyed, or there was an error during
      *         communication with session service.
      */
     public static Session getSession(SessionID sid) throws SessionException {
         if (sid.toString() == null || sid.toString().length() == 0) {
             throw new SessionException(SessionBundle.rbName,
                     "invalidSessionID", null);
         }
 
         Session session = (Session) sessionTable.get(sid);
         if (session != null) {
             TokenRestriction restriction = session.getRestriction();
             Object context = RestrictedTokenContext.getCurrent();
             if (context == null) {
                 if (session.context != null) {
                     context = session.context;
                 } else {
                     /*
                      * In cookie hijacking mode...
                      * After the server remove the agent token id from the
                      * user token id. server needs to create the agent token
                      * from this agent token id. Now, the restriction context
                      * required for session creation is null, so we added it
                      * to get the agent session created.*/
                     try {
                         context = InetAddress.getLocalHost();
                         session.context = context;
                         sessionDebug.message("Session:getSession : context: "
                             + context);
                     } catch (java.net.UnknownHostException e) {
                         sessionDebug.warning("Session:getSession : ", e);
                     }
                 }
             }
             
             try {
                 if ((restriction != null) && !restriction.isSatisfied(context)){
                     throw new SessionException(SessionBundle.rbName,
                             "restrictionViolation", null);
                 }
             } catch (SessionException se) {
                 throw se;
             } catch (Exception e) {
                 throw new SessionException(e);
             }
 
             if (session.maxCachingTimeReached())
                 session.refresh(false);
             return session;
         }
         session = new Session(sid);
         session.refresh(true);
 
         session.context = RestrictedTokenContext.getCurrent();
 
         sessionTable.put(sid, session);
         if (!pollingEnabled) {
             session.addInternalSessionListener();
         }
         return session;
     }
 
     /**
      * Returns a Session Response object based on the XML document received from
      * remote Session Server. This is in response to a request that we send to
      * the session server.
      * 
      * @param svcurl The URL of the Session Service.
      * @param sreq The Session Request XML document.
      * @return a Vector of responses from the remote server
      * @exception SessionException if there was an error in sending the XML
      *            document or if the response has multiple components.
      */
     public static SessionResponse sendPLLRequest(URL svcurl, 
             SessionRequest sreq) throws SessionException {
         try {
 
             String cookies = cookieName + "=" + sreq.getSessionID();
             if (!isServerMode()) {
                 SessionID sessionID = new SessionID(sreq.getSessionID());
                 cookies = cookies + ";" + Session.getLBCookie(sessionID);
             }
  
             Request req = new Request(sreq.toXMLString());
             RequestSet set = new RequestSet(SESSION_SERVICE);
             set.addRequest(req);
             Vector responses = PLLClient.send(svcurl, cookies, set);
             if (responses.size() != 1) {
                 throw new SessionException(SessionBundle.rbName,
                         "unexpectedResponse", null);
             }
             Response res = (Response) responses.elementAt(0);
             return SessionResponse.parseXML(res.getContent());
         } catch (Exception e) {
             throw new SessionException(e);
         }
     }
 
     /**
      * Gets all valid sessions from the specified session server. This session
      * is subject to access control in order to get all sessions.
      * 
      * @param server
      *            The session server name. If the server name contains protocol
      *            and port, the protocol and port will be used. Otherwise, the
      *            server protocol and port is default to the same protocol and
      *            port of the calling session.
      * @return A Vector of Session objects.
      * @exception SessionException if there was an error during
      *                communication with session service.
      */
     public SearchResults getValidSessions(String server, String pattern)
             throws SessionException {
         String protocol = sessionID.getSessionServerProtocol();
         String host = server;
         String port = sessionID.getSessionServerPort();
         String uri = sessionID.getSessionServerURI();
         int pos = host.indexOf("://");
         if (pos != -1) {
             protocol = host.substring(0, pos);
             host = host.substring(pos + 3);
         }
         pos = host.indexOf(":");
         if (pos != -1) {
             port = host.substring(pos + 1);
             host = host.substring(0, pos);
             int pos1 = port.indexOf("/");
             if (pos1 != -1 ) {
                 uri = port.substring(pos1);
                 port = port.substring(0, pos1);
             }
         }
         
         URL svcurl = getSessionServiceURL(protocol, host, port, uri);
         return getValidSessions(svcurl, pattern);
     }
 
     /**
      * Get all the event listeners for this Session.
      * 
      * @return SessionEventListener vector
      */
     Vector getSessionEventListeners() {
         return sessionEventListeners;
     }
 
     /**
      * Get all the event listeners for all the Sessions.
      * 
      * @return SessionEventListener vector
      */
     static Vector getAllSessionEventListeners() {
         return allSessionEventListeners;
     }
 
     /**
      * Returns Session Service URL for a Session ID.
      * 
      * @param sid Session ID
      * @return Session Service URL.
      * @exception SessionException
      */
     public static URL getSessionServiceURL(SessionID sid)
             throws SessionException {
         String primary_id = null;
 
         if (isServerMode()) {
             SessionService ss = SessionService.getSessionService();
             if (ss.isSiteEnabled() && ss.isLocalSite(sid)) {
                 if (ss.isSessionFailoverEnabled()) {
                     return getSessionServiceURL(ss.getCurrentHostServer(sid));
                 } else {
                     primary_id = sid.getExtension(SessionID.PRIMARY_ID);
                     return getSessionServiceURL(primary_id);
                 }
             }
         } else {
             primary_id = sid.getExtension(SessionID.PRIMARY_ID);
             if (primary_id != null) {
                 String secondarysites = WebtopNaming
                         .getSecondarySites(primary_id);
                 if ((secondarysites != null) && (serverID != null)) {
                     if (secondarysites.indexOf(serverID) != -1) {
                         return getSessionServiceURL(serverID);
                     }
                 }
             }
         }
 
         return getSessionServiceURL(sid.getSessionServerProtocol(), 
             sid.getSessionServer(), sid.getSessionServerPort(), 
             sid.getSessionServerURI());
     }
 
     /**
      * Returns Session Service URL.
      * 
      * @param protocol Session Server protocol.
      * @param server Session Server host name.
      * @param port Session Server port.
      * @param uri Session Server URI.
      * @return Session Service URL.
      * @exception SessionException.
      */
     static public URL getSessionServiceURL(
         String protocol, 
         String server,
         String port, 
         String uri
     ) throws SessionException {
         String key = protocol + "://" + server + ":" + port + uri;
         URL url = (URL) sessionServiceURLTable.get(key);
         if (url == null) {
             try {
                 url = WebtopNaming.getServiceURL(SESSION_SERVICE, protocol,
                     server, port, uri);
                 sessionServiceURLTable.put(key, url);
                 return url;
             } catch (Exception e) {
                 throw new SessionException(e);
             }
         }
         return url;
     }
 
     /**
      * Returns Session Service URL for a given server ID.
      * 
      * @param serverID server ID from the platform server list.
      * @return Session Service URL.
      * @exception SessionException.
      */
     static public URL getSessionServiceURL(String serverID)
             throws SessionException {
         try {
             URL parsedServerURL = new URL(WebtopNaming
                     .getServerFromID(serverID));
             return getSessionServiceURL(
                 parsedServerURL.getProtocol(),
                 parsedServerURL.getHost(), 
                 Integer.toString(parsedServerURL.getPort()),
                 parsedServerURL.getPath());
         } catch (Exception e) {
             throw new SessionException(e);
         }
     }
 
     /**
      * Used to find out if the maximum caching time has reached or not.
      */
     protected boolean maxCachingTimeReached() {
         long cachingtime = System.currentTimeMillis() / 1000
                 - latestRefreshTime;
         if (cachingtime > maxCachingTime * 60)
             return true;
         else
             return false;
     }
 
     /**
      * Returns all the valid sessions for a particular Session Service URL. If a
      * user is not allowed to access the Sessions of the input Session Server,
      * it will return null.
      * 
      * @param svcurl Session Service URL.
      * @exception SessionException.
      */
     private SearchResults getValidSessions(URL svcurl, String pattern)
             throws SessionException {
         try {
             int status[] = { 0 };
             Vector infos = null;
 
             if (sessionService != null
                     && sessionService.isLocalSessionService(svcurl)) {
                 infos = sessionService.getValidSessions(this, pattern, status);
             } else {
                 SessionRequest sreq = new SessionRequest(
                         SessionRequest.GetValidSessions, sessionID.toString(),
                         false);
                 if (pattern != null) {
                     sreq.setPattern(pattern);
                 }
                 SessionResponse sres = getSessionResponseWithoutRetry(svcurl,
                         sreq);
                 infos = sres.getSessionInfoVector();
                 status[0] = sres.getStatus();
             }
 
             Hashtable sessions = new Hashtable();
             int infoSize = infos.size();
             Session session = null;
             for (int i = 0; i < infoSize; i++) {
                 SessionInfo info = (SessionInfo) infos.elementAt(i);
                 SessionID sid = new SessionID(info.sid);
                 session = new Session(sid);
                 session.sessionServiceURL = svcurl;
                 session.update(info);
                 sessions.put(info.sid, session);
             }
             return new SearchResults(sessions.size(), sessions.keySet(),
                     status[0], sessions);
         } catch (Exception e) {
             sessionDebug.error("Session:getValidSession : ", e);
             throw new SessionException(SessionBundle.rbName,
                     "getValidSessionsError", null);
         }
     }
 
     /**
      * Adds a session listener for all sessions residing on the same session
      * server as this session object resides. This session is subject to access
      * control in order to receive session events on all sessions.
      * 
      * @param listener A reference to the Session Listener object.
      * @exception SessionException if there was an error.
      */
     public void addSessionListenerOnAllSessions(SessionListener listener)
             throws SessionException {
         if (!pollingEnabled) {
             try {
                 String url = WebtopNaming.getNotificationURL().toString();
                 if (sessionService != null) {
                     sessionService.addSessionListenerOnAllSessions(this, url);
                 } else {
                     throw new SessionException(SessionBundle.rbName,
                             "unsupportedFunction", null);
                 }
             } catch (Exception e) {
                 throw new SessionException(e);
             }
         }
         allSessionEventListeners.addElement(listener);
     }
 
     /**
      * Gets the latest session from session server and updates the local cache
      * of this session.
      * 
      * @param reset The flag to indicate whether to reset the latest session
      *        access time in the session server.
      * @exception SessionException if the session reached its
      *            maximum session time, or the session was destroyed, or
      *            there was an error during communication with session
      *            service.
      */
     public void refresh(final boolean reset) throws SessionException {
         // recalculate whether session is local or remote on every refresh
         // this is just an optmization
         // it is functionally safe to always use remote mode
         // but it is not efficient
         // this check takes care of migration "remote -> local"
         // reverse migration "local - > remote" will be
         // done by calling Session.markNonLocal() from
         // SessionService.handleReleaseSession()
         sessionIsLocal = checkSessionLocal();
 
         Object activeContext = RestrictedTokenContext.getCurrent();
         if (activeContext == null) {
             activeContext = this.context;
         }
         try {
             RestrictedTokenContext.doUsing(activeContext,
                     new RestrictedTokenAction() {
                         public Object run() throws Exception {
                             doRefresh(reset);
                             return null;
                         }
                     });
 
         } catch (Exception e) {
             Session.removeSID(sessionID);
             if (sessionDebug.messageEnabled()) {
                 sessionDebug.message("session.Refresh " + "Removed SID:"
                         + sessionID);
             }
             throw new SessionException(e);
         }
     }
 
    /*
     * Refreshes the Session Information
     * @param <code>true</code> refreshes the Session Information
     */
    private void doRefresh(boolean reset) throws SessionException {
         SessionInfo info = null;
         boolean flag = reset || needToReset;
         needToReset = false;
         if (isLocal()) {
             info = sessionService.getSessionInfo(sessionID, flag);
         } else {
             SessionRequest sreq = new SessionRequest(SessionRequest.GetSession,
                     sessionID.toString(), flag);
             SessionResponse sres = getSessionResponse(getSessionServiceURL(),
                     sreq);
             if (sres.getException() != null) {
                 throw new SessionException(SessionBundle.rbName,
                         "invalidSessionState", null);
             }
             Vector infos = sres.getSessionInfoVector();
             if (infos.size() != 1) {
                 throw new SessionException(SessionBundle.rbName,
                         "unexpectedSession", null);
             }
             info = (SessionInfo) infos.elementAt(0);
         }
         update(info);
         latestRefreshTime = System.currentTimeMillis() / 1000;
     }
 
     /**
      * Updates the session from the session information server.
      * 
      * @param info Session Information.
      */
     synchronized void update(SessionInfo info) throws SessionException {
         if (info.stype.equals("user"))
             sessionType = USER_SESSION;
         else if (info.stype.equals("application"))
             sessionType = APPLICATION_SESSION;
         clientID = info.cid;
         clientDomain = info.cdomain;
         maxSessionTime = Long.parseLong(info.maxtime);
         maxIdleTime = Long.parseLong(info.maxidle);
         maxCachingTime = Long.parseLong(info.maxcaching);
         sessionIdleTime = Long.parseLong(info.timeidle);
         sessionTimeLeft = Long.parseLong(info.timeleft);
         if (info.state.equals("invalid"))
             sessionState = INVALID;
         else if (info.state.equals("valid"))
             sessionState = VALID;
         else if (info.state.equals("inactive"))
             sessionState = INACTIVE;
         else if (info.state.equals("destroyed"))
             sessionState = DESTROYED;
         sessionProperties = info.properties;
         if (timedOutAt <= 0) {
             String sessionTimedOutProp = (String) sessionProperties
                     .get("SessionTimedOut");
             if (sessionTimedOutProp != null) {
                 try {
                     timedOutAt = Long.parseLong(sessionTimedOutProp);
                 } catch (NumberFormatException e) {
                     sessionDebug.error("Invalid timeout value "
                             + sessionTimedOutProp, e);
                 }
             }
         }
         latestRefreshTime = System.currentTimeMillis() / 1000;
         // note : do not use getProperty() call here to avoid unexpected
         // recursion via
         // refresh()
         String restrictionProp = (String) sessionProperties
                 .get(TOKEN_RESTRICTION_PROP);
         if (restrictionProp != null) {
             try {
                 restriction = TokenRestrictionFactory
                         .unmarshal(restrictionProp);
             } catch (Exception e) {
                 throw new SessionException(e);
             }
         }
     }
 
     /**
      * Sends remote session request without retries.
      * 
      * @param svcurl Session Service URL.
      * @param sreq Session Request object.
      * @exception SessionException.
      */
     private SessionResponse getSessionResponseWithoutRetry(URL svcurl,
             SessionRequest sreq) throws SessionException {
         try {
             Object context = RestrictedTokenContext.getCurrent();
             if (context != null) {
                 sreq.setRequester(RestrictedTokenContext.marshal(context));
             }
             SessionResponse sres = sendPLLRequest(svcurl, sreq);
             if (sres.getException() != null) {
                 // Check if this exception was thrown due to Session Time out or
                 // not
                 // If yes then set the private variable timedOutAt to the
                 // current time
                 // But before that check if this timedOutAt is already set or
                 // not. No need of
                 // setting it again
                 if (timedOutAt <= 0) {
                     String exceptionMessage = sres.getException();
                     if (exceptionMessage.indexOf("SessionTimedOutException") 
                             != -1) 
                     {
                         timedOutAt = System.currentTimeMillis() / 1000;
                     }
                 }
                 throw new SessionException(sres.getException());
             }
             return sres;
         } catch (Exception e) {
             throw new SessionException(e);
         }
     }
 
     /**
      * When used in internal request routing mode, it sends remote session
      * request with retries. If not in internal request routing mode simply
      * calls <code>getSessionResponseWithoutRetry</code>.
      * 
      * @param svcurl Session Service URL.
      * @param sreq Session Request object.
      * @exception SessionException 
      */
     private SessionResponse getSessionResponse(URL svcurl, SessionRequest sreq)
             throws SessionException {
         if (isServerMode() && SessionService.getUseInternalRequestRouting()) {
             try {
                 return getSessionResponseWithoutRetry(svcurl, sreq);
             } catch (SessionException e) {
                 // attempt retry if appropriate
                 String hostServer = sessionService
                         .getCurrentHostServer(sessionID);
                 if (!sessionService.checkServerUp(hostServer)) {
                     // proceed with retry
                     // Note that there is a small risk of repeating request
                     // twice (e.g., normal exception followed by server failure)
                     // This danger is insignificant because most of our requests
                     // are idempotent. For those which are not (e.g.,
                     // logout/destroy)
                     // it is not critical if we get an exception attempting to
                     // repeat this type of request again.
 
                     URL retryURL = getSessionServiceURL();
                     if (!retryURL.equals(svcurl)) {
                         return getSessionResponseWithoutRetry(retryURL, sreq);
                     }
                 }
                 throw e;
             }
         } else {
             return getSessionResponseWithoutRetry(svcurl, sreq);
         }
     }
 
     /**
      * Add listener to Internal Session.
      */
     private void addInternalSessionListener() {
         try {
             if (SessionNotificationHandler.handler == null) {
                 SessionNotificationHandler.handler = 
                     new SessionNotificationHandler(sessionTable);
                 PLLClient.addNotificationHandler(Session.SESSION_SERVICE,
                         SessionNotificationHandler.handler);
             }
             String url = WebtopNaming.getNotificationURL().toString();
             if (isLocal()) {
                 sessionService.addSessionListener(sessionID, url);
             } else {
                 SessionRequest sreq = new SessionRequest(
                         SessionRequest.AddSessionListener,
                         sessionID.toString(), false);
                 sreq.setNotificationURL(url);
                 getSessionResponse(getSessionServiceURL(), sreq);
             }
         } catch (Exception e) {
         }
     }
 
     /**
      * Returns the encoded URL , rewriten to include the session id. cookie will
      * be rewritten in the URL as a query string with entity escaping of
      * ampersand before appending session ID if other query parameters exists in
      * the URL.
      * <p>
      * 
      * @param res HTTP Servlet Response.
      * @param url the URL to be encoded.
      * @return the encoded URL if cookies are not supported and URL if cookies
      *         are supported
      */
     public String encodeURL(HttpServletResponse res, String url) {
         return encodeURL(res, url, cookieName);
     }
 
     /**
      * Returns the encoded URL , rewriten to include the session id. cookie will
      * be rewritten in the URL as a query string with entity escaping of
      * ampersand before appending session id if other query parameters exists in
      * the URL.
      * <p>
      * 
      * @param res HTTP Servlet Response.
      * @param url  the URL to be encoded
      * @param cookieName AM cookie name
      * @return the encoded URL if cookies are not supported and URL if cookies
      *         are supported
      */
     public String encodeURL(HttpServletResponse res, String url,
             String cookieName) {
         String httpEncodeUrl = res.encodeURL(url);
         return encodeURL(httpEncodeUrl, SessionUtils.QUERY, true, cookieName);
     }
 
     /**
      * Returns the encoded URL , rewritten to include the session id. Cookie
      * will be written to the URL in as a query string.
      * 
      * @param url the URL to be encoded.
      * @param escape true if ampersand entity escaping needs to done
      *        else false. This parameter is valid only when encoding scheme
      *        is <code>SessionUtils.QUERY</code>.
      * @return the encoded URL if cookies are not supported or the URL if
      *         cookies are supported.
      */
 
     public String encodeURL(String url, boolean escape) {
         return encodeURL(url, escape, cookieName);
     }
 
     /**
      * Returns the encoded URL , rewritten to include the session id. Cookie
      * will be written to the URL in as a query string.
      * 
      * @param url the URL to be encoded
      * @param escape true if ampersand entity escaping needs to 
      *        done else false.This parameter is valid only when encoding 
      *        scheme is <code>SessionUtils.QUERY</code>.
      * @param cookieName cookie name.
      * @return the encoded URL if cookies are not supported or the URL if
      *         cookies are supported.
      */
     public String encodeURL(String url, boolean escape, String cookieName) {
         return encodeURL(url, SessionUtils.QUERY, escape);
     }
 
     /**
      * Returns the encoded URL , rewritten to include the session id in the
      * query string with entity escaping
      * 
      * @param url the URL to be encoded
      * @return the encoded URL if cookies are not supported or the URL if
      *         cookies are supported.
      */
     public String encodeURL(String url) {
         return encodeURL(url, cookieName);
     }
 
     /**
      * Returns the encoded URL , rewritten to include the session id in the
      * query string with entity escaping
      * 
      * @param url the URL to be encoded.
      * @param cookieName the cookie name.
      * @return the encoded URL if cookies are not supported or the URL if
      *         cookies are supported.
      */
     public String encodeURL(String url, String cookieName) {
         return encodeURL(url, SessionUtils.QUERY, true, cookieName);
     }
 
     /**
      * Returns the encoded URL , rewritten to include the session id.
      * 
      * @param url the URL to be encoded.
      * @param encodingScheme the scheme to rewrite the cookie value in URL as
      *        a Query String or Path Info (Slash or Semicolon separated.
      *        Allowed values are <code>SessionUtils.QUERY</code>,
      *        <code>SessionUtils.SLASH</code> and
      *        <code>SessionUtils.SEMICOLON</code>
      * @param escape true if ampersand entity escaping needs to done
      *        else false. This parameter is valid only when encoding scheme
      *        is <code>SessionUtils.QUERY</code>.
      * @return the encoded URL if cookies are not supported or the URL if
      *         cookies are supported.
      */
 
     public String encodeURL(String url, short encodingScheme, boolean escape) {
         return encodeURL(url, encodingScheme, escape, cookieName);
     }
 
     /**
      * Returns the encoded URL , rewritten to include the session id.
      * 
      * @param url the URL to be encoded.
      * @param encodingScheme the scheme to rewrite the cookie value in URL as
      *        a Query String or Path Info (Slash or Semicolon separated. Allowed
      *        values are <code>SessionUtils.QUERY</code>,
      *        <code>SessionUtils.SLASH</code> and 
      *        <code>SessionUtils.SEMICOLON</code>.
      * @param escape true if ampersand entity escaping needs to done
      *        else false. This parameter is valid only when encoding scheme
      *        is <code>SessionUtils.QUERY</code>.
      * @param cookieName name of the cookie.
      * @return the encoded URL if cookies are not supported or the URL if
      *         cookies are supported.
      */
     public String encodeURL(String url, short encodingScheme, boolean escape,
             String cookieName) {
         String encodedURL = url;
         if (((url != null) && (url.length() > 0)) && (!getCookieSupport())) {
             if ((cookieStr != null) && (cookieStr.length() > 0)
                     && (foundCookieName(cookieStr, cookieName))) {
                 encodedURL = SessionEncodeURL.buildCookieString(url, cookieStr,
                         encodingScheme, escape);
             } else { // cookie str not set so call encodeURL
                 if (sessionID != null) {
                     cookieStr = SessionEncodeURL.createCookieString(cookieName,
                             sessionID.toString());
                     encodedURL = SessionEncodeURL.encodeURL(cookieStr, url,
                             encodingScheme, escape);
                 }
             }
         }
         return encodedURL;
     }
 
     /**
      * Returns true if cookies are supported else false. The
      * <code>cookieSupport</code> value is first determined from the Session ID
      * object , if that is null then it is determined based on the cookie mode
      * value set in the Session object else <code>cookieSupport</code> value is
      * retrieved from the session property <code>cookieSupport</code>. If
      * cookie Support value is not determined then the the default "false" is
      * assumed.
      */
     private boolean getCookieSupport() {
         boolean cookieSupport = false;
         try {
             Boolean cookieMode = sessionID.getCookieMode();
             if (cookieMode != null) {
                 cookieSupport = cookieMode.booleanValue();
             } else if (this.cookieMode != null) {
                 cookieSupport = this.cookieMode.booleanValue();
             } else {
                 String cookieSupportStr = getProperty("cookieSupport");
                 if (cookieSupportStr != null) {
                     cookieSupport = cookieSupportStr.equals("true");
                 }
             }
         } catch (Exception ex) {
             sessionDebug.error("Error getting cookieSupport value: ", ex);
             cookieSupport = true;
         }
         if (sessionDebug.messageEnabled()) {
             sessionDebug.message("Session: getCookieSupport: " + cookieSupport);
         }
         return cookieSupport;
     }
 
     /**
      * Set the cookie Mode based on whether the request has cookies or not. This
      * method is called from <code>createSSOToken(request)</code> method in
      * <code>SSOTokenManager</code>.
      * 
      * @param cookieMode whether request has cookies or not.
      */
     public void setCookieMode(Boolean cookieMode) {
         if (sessionDebug.messageEnabled()) {
             sessionDebug.message("CookieMode is:" + cookieMode);
         }
         if (cookieMode != null) {
             this.cookieMode = cookieMode;
         }
     }
 
     /**
      * Indicates whether local or remote invocation of Sesion Service should be
      * used
      * 
      * @return true if local invocation should be used, false otherwise
      */
     boolean isLocal() {
         return sessionIsLocal;
     }
 
     /**
      * Marks session referenced by Session ID as non-local so that remote
      * invocations of Session Service methods are to be used.
      * 
      * @param sid session ID.
      */
     public static void markNonLocal(SessionID sid) {
         Session sess = (Session) sessionTable.get(sid);
         if (sess != null) {
             sess.sessionIsLocal = false;
         }
     }
 
     /**
      * Actively checks whether current session should be considered local (so
      * that local invocations of Session Service methods are to be used)
      * 
      * @return true if the session local.
      */
     private boolean checkSessionLocal() throws SessionException {
         if (isServerMode()) {
             return sessionService.checkSessionLocal(sessionID);
         } else {
             return false;
         }
     }
 
     private TokenRestriction getRestriction() throws SessionException {
         return restriction;
     }
 
     Object getContext() {
         return context;
     }
 
     /**
      * Determines whether session code runs in core server or client SDK
      * run-time mode
      * 
      * @return true if running in core server mode, false otherwise
      */
     static boolean isServerMode() {
         return SystemProperties.isServerMode();
     }
 
     /**
      * Checks if the cookie name is in the cookie string.
      * 
      * @param cookieStr cookie string (<code>cookieName=cookieValue</code>).
      * @param cookieName name of the cookie.
      * @return true if <code>cookieName</code> is in the <code>cookieStr</code>.
      */
     public static boolean foundCookieName(String cookieStr, String cookieName) {
         boolean foundCookieName = false;
         String cookieNameInStr = null;
         if (sessionDebug.messageEnabled()) {
             sessionDebug.message("CookieNameStr is :" + cookieNameInStr);
             sessionDebug.message("cookieName is :" + cookieName);
         }
         if (cookieStr != null && cookieStr.length() != 0) {
             cookieNameInStr = cookieStr.substring(0, cookieStr.indexOf("="));
         }
         if ((cookieNameInStr != null) && (cookieNameInStr.equals(cookieName))) {
             foundCookieName = true;
         }
         return foundCookieName;
     }
 }
