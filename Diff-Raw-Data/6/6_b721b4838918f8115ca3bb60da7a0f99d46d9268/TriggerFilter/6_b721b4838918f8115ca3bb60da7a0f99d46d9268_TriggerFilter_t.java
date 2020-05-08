 package org.makumba.parade.tools;
 
 import java.io.IOException;
 import java.util.Date;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 
 import org.makumba.parade.access.ActionLogDTO;
 
 /**
  * This filter invokes a servlet before and another servlet after each access to a servlet context or an entire servlet
  * engine. The servlets can be in any of the servlet contexts. If servlets from other contexts are used, in Tomcat,
  * server.xml must include <DefaultContext crossContext="true"/>.<br>
  * When this class is used for all Tomcat contexts, it should be configured in tomcat/conf/web.xml, and should be
  * available statically (e.g. in tomcat/common/classes. The great advantage is that all servlets that it invokes can be
  * loaded dynamically. beforeServlet and afterServlet are not invoked with the original request, but with a dummy
  * request, that contains the original request, response and context as the attributes
  * "org.eu.best.tools.TriggerFilter.request", "org.eu.best.tools.TriggerFilter.response",
  * "org.eu.best.tools.TriggerFilter.context".<br>
  * The beforeServlet can indicate that it whishes the chain not to be invoked by resetting the attribute
  * "org.eu.best.tools.TriggerFilter.request" to null.<br>
  * 
  * TODO read POST parameters from the requests
  * 
  * @author Cristian Bogdan
  * @author Manuel Gay
  */
 public class TriggerFilter implements Filter {
     ServletContext context;
 
     String beforeContext, afterContext, beforeServlet, afterServlet;
 
     public void init(FilterConfig conf) {
         context = conf.getServletContext();
         if (context.getContext("/") == context) {
             LogHandler.setStaticContext(context);
         }
 
         beforeContext = conf.getInitParameter("beforeContext");
         beforeServlet = conf.getInitParameter("beforeServlet");
         afterContext = conf.getInitParameter("afterContext");
         afterServlet = conf.getInitParameter("afterServlet");
 
     }
 
     public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws java.io.IOException,
             ServletException {
 
         ServletRequest origReq = req;
 
         PerThreadPrintStream.setEnabled(true);
 
         ServletContext ctx = context.getContext(beforeContext);
 
         // we create an initial ActionLogDTO and set it to the ThreadLocal
         ActionLogDTO log = new ActionLogDTO();
         getActionContext(req, log);
         LogHandler.actionLog.set(log);
 
         // we set the original attributes to be passed on
         HttpServletRequest dummyReq = new HttpServletRequestDummy();
         dummyReq.setAttribute("org.eu.best.tools.TriggerFilter.request", req);
         dummyReq.setAttribute("org.eu.best.tools.TriggerFilter.response", resp);
         dummyReq.setAttribute("org.eu.best.tools.TriggerFilter.context", context);
         dummyReq.setAttribute("org.eu.best.tools.TriggerFilter.actionlog", log);
 
         req.setAttribute("org.eu.best.tools.TriggerFilter.dummyRequest", dummyReq);
         req.setAttribute("org.eu.best.tools.TriggerFilter.request", req);
         req.setAttribute("org.eu.best.tools.TriggerFilter.response", resp);
         req.setAttribute("org.eu.best.tools.TriggerFilter.context", context);
         req.setAttribute("org.eu.best.tools.TriggerFilter.actionlog", log);
 
         if (ctx == null) {
             checkCrossContext(req, beforeContext);
         } else {
 
             if (beforeServlet != null)
                 LogHandler.invokeServlet(beforeServlet, ctx, dummyReq, resp);
 
             // first, we ask the db servlet to log our actionlog
             dummyReq.setAttribute("org.makumba.parade.servletParam", log);
             LogHandler.invokeServlet("/servlet/org.makumba.parade.access.DatabaseLogServlet", ctx, dummyReq, resp);
 
             // now we have the user gracefully provided by the beforeServlet so we can set the prefix
             LogHandler.setPrefix();
         }
 
         req = (ServletRequest) dummyReq.getAttribute("org.eu.best.tools.TriggerFilter.request");
 
         if (req == null) {
             boolean unauthorizedAccess = (dummyReq.getAttribute("org.makumba.parade.unauthorizedAccess") != null);
             boolean directoryServerError = (dummyReq.getAttribute("org.makumba.parade.directoryAccessError") != null);
             if (unauthorizedAccess || directoryServerError) {
                 req = origReq;
                 String errorPageURI = "/unauthorized/index.jsp";
                 if (directoryServerError)
                     errorPageURI = "/unauthorized/directoryServerError.jsp";
                 try {
                    ctx.getRequestDispatcher(errorPageURI).forward(req, resp);
                    //LogHandler.getRequestDispatcher(errorPageURI).forward(req, resp);
                 } catch (ServletException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 } catch (IOException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                } catch(NullPointerException npe) {
                    npe.printStackTrace();
                 }
                 // chain.doFilter(req, resp);
                 return;
 
             } else {
                 // beforeServlet signaled closure
                 return;
             }
         }
 
         resp = (ServletResponse) dummyReq.getAttribute("org.eu.best.tools.TriggerFilter.response");
 
         chain.doFilter(req, resp);
 
         ctx = context.getContext(afterContext);
         if (ctx == null) {
             checkCrossContext(req, afterContext);
         } else {
 
             if (afterServlet != null)
                 LogHandler.invokeServlet(afterServlet, ctx, req, resp);
         }
 
         // we make sure the actionLog is null after each access
         LogHandler.actionLog.set(null);
 
     }
 
     /**
      * Tries to determine the context information of an action, meaning not only the servlet context but also other
      * relevant information.
      * 
      * @param req
      *            the ServletRequest corresponding to the access
      * @param log
      *            the ActionLogDTO which will hold the information
      * 
      *            TODO get the POST parameters as well by reading the inputstream of the request
      */
     private void getActionContext(ServletRequest req, ActionLogDTO log) {
         HttpServletRequest httpReq = ((HttpServletRequest) req);
         String contextPath = httpReq.getContextPath();
         if (contextPath.equals("")) { // FIXME heuristic
             contextPath = "parade2";
         } else {
             contextPath = contextPath.substring(1);
         }
 
         log.setContext(contextPath);
         log.setDate(new Date());
         String pathInfo = httpReq.getPathInfo();
         log.setUrl(httpReq.getServletPath() + (pathInfo == null ? "" : pathInfo));
         log.setQueryString(httpReq.getQueryString());
 
     }
 
     /**
      * Checks if the crossContext is enabled.
      * 
      * @param req
      *            the ServletRequest corresponding to the current access
      * @param ctxName
      *            the context name of the current context
      */
     private void checkCrossContext(ServletRequest req, String ctxName) {
         if (!((HttpServletRequest) req).getContextPath().equals("/manager"))
             System.out
                     .println("got null trying to search context "
                             + ctxName
                             + " from context "
                             + ((HttpServletRequest) req).getContextPath()
                             + " it may be that <DefaultContext crossContext=\"true\"/> is not configured in Tomcat's conf/server.xml, under Engine or Host");
     }
 
     public void destroy() {
     }
 
 }
