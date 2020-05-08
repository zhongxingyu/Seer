 package org.melati;
 
 import java.util.*;
 import java.io.*;
 import org.melati.util.*;
 import org.melati.poem.*;
 import org.webmacro.*;
 import org.webmacro.util.*;
 import org.webmacro.engine.*;
 import org.webmacro.servlet.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 public abstract class MelatiServlet extends MelatiWMServlet {
 
   /**
   * You must use a hacked version of <TT>org.melati.engine.Variable</TT> with
   * Melati.  Sorry this has to go into <TT>org.melati.engine</TT>: lobby Justin
   * to stop making everything final or package-private or static!  It will not
   * break your existing webmacro code if you put it in your <TT>CLASSPATH</TT>
   * since its semantics are only different when configured so to be.  You can
   * get the hacked version by anonymous CVS from melati.org (see the <A
  * HREF=http://paneris.org/cgi-bin/cvsweb.cgi/~checkout~/org/melati/qa/Installation.html>Installation
   * guide</A>).
   */
 
   public static final Object check =
      org.webmacro.engine.Variable.youNeedToBeUsingAVersionOfVariableHackedForMelati;
 
   static final String
       OVERLAY_PARAMETERS = "org.melati.MelatiServlet.overlayParameters",
       USER = "org.melati.MelatiServlet.user";
 
   /**
    * Melati's main entry point.  Override this to do WebMacro-like things and
    * return a WebMacro template---like <TT>WMServlet.handle</TT>.
    *
    * <UL>
    * <LI>
    *
    * The `logical name' of the Melati POEM database to which the servlet should
    * connect is determined from the first component of the pathinfo.  It is
    * mapped onto JDBC connection details via the config file
    * <TT>org.melati.LogicalDatabase.properties</TT>, of which there is an
    * example in the source tree.
    *
    * <LI>
    *
    * Any POEM database operations you perform will be done with the access
    * rights of the POEM <TT>User</TT> associated with the servlet session.  If
    * there is no established servlet session, the current user will be set to
    * the default `guest' user.  If this method terminates with an
    * <TT>AccessPoemException</TT>, indicating that you have attempted something
    * which you aren't entitled to do, the <TT>loginTemplate</TT> method will be
    * invoked instead; once the user has logged in, the original request will be
    * retried.
    *
    * <LI>
    *
    * No changes made to the database by other concurrently executing threads
    * will be visible to you (in the sense that once you have seen a particular
    * version of a record, you will always subsequently see the same one), and
    * your own changes will not be made permanent until this method completes
    * successfully or you perform an explicit <TT>PoemThread.commit()</TT>.  If
    * it terminates with an exception or you issue a
    * <TT>PoemThread.rollback()</TT>, your changes will be lost.
    *
    * </UL>
    *
    * @see org.melati.poem.Database#guestUser
    * @see #loginTemplate
    * @see org.melati.poem.PoemThread#commit
    * @see org.melati.poem.PoemThread#rollback
    */
 
   protected abstract Template melatiHandle(WebContext context)
       throws PoemException, WebMacroException;
 
   protected String loginPageServletClassName() {
     return "org.melati.Login";
   }
 
   protected String loginPageURL(HttpServletRequest request) {
     StringBuffer url = new StringBuffer();
     url.append(request.getScheme());
     url.append("://");
     url.append(request.getServerName());
     if (request.getScheme().equals("http") && request.getServerPort() != 80 ||
         request.getScheme().equals("https") && request.getServerPort() != 443) {
       url.append(':');
       url.append(request.getServerPort());
     }
 
     String servlet = request.getServletPath();
     if (servlet != null)
       url.append(servlet.substring(0, servlet.lastIndexOf('/') + 1));
 
     url.append(loginPageServletClassName());
     url.append('/');
     // FIXME cut the front off the pathinfo to retrieve the DB name
     String pathInfo = request.getPathInfo();
     url.append(pathInfo.substring(1, pathInfo.indexOf('/', 1) + 1));
 
     return url.toString();
   }
 
   protected Template handleException(WebContext context, Exception exception)
       throws Exception {
 
     Exception underlying =
         exception instanceof VariableException ?
           ((VariableException)exception).problem : exception;
 
     if (underlying == null || !(underlying instanceof AccessPoemException))
       super.handleException(context, exception);
 
     AccessPoemException accessException = (AccessPoemException)underlying;
 
     HttpServletRequest request = context.getRequest();
     HttpServletResponse response = context.getResponse();
 
     HttpSession session = request.getSession(true);
 
     session.putValue(Login.TRIGGERING_REQUEST_PARAMETERS,
                      new HttpServletRequestParameters(request));
 
     if (accessException != null)
       session.putValue(Login.TRIGGERING_EXCEPTION, accessException);
     else
       session.removeValue(Login.TRIGGERING_EXCEPTION);
 
     try {
       response.sendRedirect(loginPageURL(request));
     }
     catch (IOException e) {
       throw new HandlerException(e.toString());
     }
 
     return null;
   }
 
   /**
    * WebMacro's main entry point, overridden to set up Melati's environment and
    * invoke its entry point, `<TT>template</TT>'.  Don't override this unless
    * you really know what you're doing.
    *
    * @see #template
    */
 
   protected Template handle(WebContext context) throws WebMacroException {
     context.put("melati", new Melati(context));
     context.put(Variable.EXCEPTION_HANDLER,
                 PropagateVariableExceptionHandler.it);
 
     HttpSession session = context.getSession();
     User user = (User)session.getValue(USER);
     PoemThread.setAccessToken(
         user == null ? PoemThread.database().guestAccessToken() : user);
 
     return melatiHandle(context);
   }
 
   private void reallyService(ServletRequest request, ServletResponse response)
       throws ServletException, IOException {
     super.service(request, response);
   }
 
   /**
    * Overrides main servlet entry point to allow Melati to set up its
    * environment before WebMacro takes over.  We have to take control very
    * early, since the POEM database session must be wrapped around the whole
    * WebMacro logic: the session must be active while the template is expanded.
    * NB the application programmer's entry point to Melati is
    * <TT>melatiHandle</TT>, above.
    *
    * @see #melatiHandle
    */
 
   public void service(final ServletRequest plainRequest,
                       final ServletResponse response)
       throws ServletException, IOException {
 
     final String[] problem = new String[1];
 
     try {
       HttpServletRequest incoming = (HttpServletRequest)plainRequest;
       HttpSession session = incoming.getSession(true);
 
       // First off, is the user continuing after a login?  If so, we want to
       // recover any POSTed fields from the request that triggered it.
 
       HttpServletRequestParameters oldParams;
       final HttpServletRequest request;
 
       synchronized (session) {
         oldParams =
             (HttpServletRequestParameters)session.getValue(OVERLAY_PARAMETERS);
         if (oldParams != null) {
           session.removeValue(OVERLAY_PARAMETERS);
           HttpServletRequest req;
           try {
             req = new ReconstructedHttpServletRequest(oldParams, incoming);
           }
           catch (ReconstructedHttpServletRequestMismatchException e) {
             req = incoming;
           }
           request = req;
         }
         else
           request = incoming;
       }
 
       // Now, get the logical database name from the initial section of PATH_INFO
 
       String pathInfo = request.getPathInfo();
       String subPathInfo = null;
       String logicalDatabaseName = null;
       if (pathInfo != null) {
         int slash = pathInfo.indexOf('/');
         if (slash != -1) {
           int slash2 = pathInfo.indexOf('/', slash + 1);
           logicalDatabaseName =
               slash2 == -1 ? pathInfo.substring(slash + 1) :
                              pathInfo.substring(slash + 1, slash2);
           subPathInfo = slash2 == -1 ? null : pathInfo.substring(slash2 + 1);
         }
       }
 
       // Set up a Poem session and call the application code
 
       // dearie me, what a lot of hoops to jump through
       // at the end of the day Java is terribly poorly suited to this kind of
       // lambda idiom :(
 
       final MelatiServlet _this = this;
 
       Database database;
       try {
         database = LogicalDatabase.named(logicalDatabaseName);
       }
       catch (DatabaseInitException e) {
         e.printStackTrace();
         throw new ServletException(e.toString());
       }
 
       database.inSession(
           AccessToken.root,
           new PoemTask() {
             public void run() {
               try {
                 _this.reallyService(request, response);
               }
               catch (Exception e) {
                 // FIXME oops we have to do this in-session!  This is because
                 // some PoemExceptions generate their messages on the fly from
                 // Persistents that can't be interrogated outside a database
                 // session.  Indeed the toString() can actually generate a
                 // further exception.  Not very satisfactory.
 
                 problem[0] = e.toString();
                 e.printStackTrace();
               }
             }
           });
     }
     catch (Exception e) {
       e.printStackTrace();
       throw new ServletException(e.toString());
     }
 
     if (problem[0] != null)
       throw new ServletException(problem[0]);
   }
 }
