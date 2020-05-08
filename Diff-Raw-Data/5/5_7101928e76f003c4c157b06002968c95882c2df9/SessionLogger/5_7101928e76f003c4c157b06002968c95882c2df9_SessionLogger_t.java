 package com.fatwire.gst.web.servlet.profiling.servlet;
 
 import java.util.Date;
 
 import javax.servlet.http.HttpSession;
 import javax.servlet.http.HttpSessionActivationListener;
 import javax.servlet.http.HttpSessionAttributeListener;
 import javax.servlet.http.HttpSessionBindingEvent;
 import javax.servlet.http.HttpSessionEvent;
 import javax.servlet.http.HttpSessionListener;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * 
  * <p>To deploy this listener you need to:<br/>
  * <br/>
  * Add the compiled class to WEB-INF/classes, and add the following in web.xml to activate our SessionListener.<br/>
  *
  * &lt;listener&gt;<br/>
  *    &lt;listener-class&gt;com.fatwire.gst.web.servlet.profiling.servlet.SessionLogger&lt;/listener-class&gt;<br/>
  * &lt;/listener&gt;<br/>
  *<br/>
  *<br/>
  * To see any messages in the log file the 'com.fatwire.gst.web.servlet.profiling.servlet.SessionLogger' loggger needs to be set to DEBUG level.<br/>
  * <br/>
  * 
  */
 
 public class SessionLogger implements HttpSessionAttributeListener,
         HttpSessionListener {
     private final Log log = LogFactory.getLog(this.getClass());
 
     private final HttpSessionActivationListener distributionListener = new MyHttpSessionActivationListener();
 
    private static class MyHttpSessionActivationListener implements
             HttpSessionActivationListener, java.io.Serializable {
        private static final Log log = LogFactory.getLog(SessionLogger.class);
         /**
          * 
          */
         private static final long serialVersionUID = 1L;
 
         public void sessionDidActivate(final HttpSessionEvent event) {
             if (log.isDebugEnabled()) {
                 log.debug(buildDebugInfo(event.getSession(),
                         "sessionDidActivate"));
             }
 
         }
 
         public void sessionWillPassivate(final HttpSessionEvent event) {
             if (log.isDebugEnabled()) {
                 log.debug(buildDebugInfo(event.getSession(),
                         "sessionWillPassivate"));
             }
 
         }
 
         /* (non-Javadoc)
          * @see java.lang.Object#toString()
          */
         @Override
         public String toString() {
             return "MyHttpSessionActivationListener";
         }
 
     };
 
     public SessionLogger() {
         log.debug("SessionLogger started");
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see javax.servlet.http.HttpSessionAttributeListener#attributeAdded(javax.servlet.http.HttpSessionBindingEvent)
      */
     public void attributeAdded(final HttpSessionBindingEvent event) {
         if (log.isDebugEnabled()) {
             log.debug("sessionAttributeAdded: '" + event.getSession().getId()
                     + "' " + event.getName() + "="
                     + String.valueOf(event.getValue()));
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see javax.servlet.http.HttpSessionAttributeListener#attributeRemoved(javax.servlet.http.HttpSessionBindingEvent)
      */
     public void attributeRemoved(final HttpSessionBindingEvent event) {
         if (log.isDebugEnabled()) {
             log.debug("sessionAttributeRemoved: '" + event.getSession().getId()
                     + "' " + event.getName());
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see javax.servlet.http.HttpSessionAttributeListener#attributeReplaced(javax.servlet.http.HttpSessionBindingEvent)
      */
     public void attributeReplaced(final HttpSessionBindingEvent event) {
         if (log.isDebugEnabled()) {
             log.debug("sessionAttributeReplaced '" + event.getSession().getId()
                     + "' " + event.getName() + "="
                     + String.valueOf(event.getValue()));
         }
     }
 
     public void sessionCreated(final HttpSessionEvent event) {
         if (log.isDebugEnabled()) {
             log.debug(buildDebugInfo(event.getSession(), "sessionCreated"));
             event.getSession().setAttribute("distributionListener",
                     distributionListener);
         }
 
     }
 
     public void sessionDestroyed(final HttpSessionEvent event) {
         if (log.isDebugEnabled()) {
             log.debug(buildDebugInfo(event.getSession(), "sessionDestroyed"));
         }
 
     }
 
     static String buildDebugInfo(final HttpSession session, final String method) {
         if (session == null || method == null) {
             return "";
         }
         final StringBuilder b = new StringBuilder(method);
         b.append(": '");
         b.append(session.getId()).append("', lastAccessed: ").append(
                 new Date(session.getLastAccessedTime()) + ", created:").append(
                 new Date(session.getCreationTime())).append(
                 ", maxInactiveInterval: ").append(
                 session.getMaxInactiveInterval());
         return b.toString();
 
     }
 }
