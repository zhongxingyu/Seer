 /*
  * $Source$
  * $Revision$
  *
  * Copyright (C) 2000 William Chesters
  *
  * Part of Melati (http://melati.org), a framework for the rapid
  * development of clean, maintainable web applications.
  *
  * Melati is free software; Permission is granted to copy, distribute
  * and/or modify this software under the terms either:
  *
  * a) the GNU General Public License as published by the Free Software
  *    Foundation; either version 2 of the License, or (at your option)
  *    any later version,
  *
  *    or
  *
  * b) any version of the Melati Software License, as published
  *    at http://melati.org
  *
  * You should have received a copy of the GNU General Public License and
  * the Melati Software License along with this program;
  * if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA to obtain the
  * GNU General Public License and visit http://melati.org to obtain the
  * Melati Software License.
  *
  * Feel free to contact the Developers of Melati (http://melati.org),
  * if you would like to work out a different arrangement than the options
  * outlined here.  It is our intention to allow Melati to be used by as
  * wide an audience as possible.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * Contact details for copyright holder:
  *
  *     William Chesters <williamc@paneris.org>
  *     http://paneris.org/~williamc
  *     Obrechtstraat 114, 2517VX Den Haag, The Netherlands
  */
 
 package org.melati;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.melati.admin.AdminUtils;
 import org.melati.poem.Database;
 import org.melati.poem.Table;
 import org.melati.poem.User;
 import org.melati.poem.Persistent;
 import org.melati.poem.PoemThread;
 import org.melati.poem.NotInSessionPoemException;
 import org.melati.poem.NoAccessTokenPoemException;
 import org.melati.servlet.MelatiContext;
 import org.melati.template.TemplateContext;
 import org.melati.template.HTMLMarkupLanguage;
 import org.melati.template.WMLMarkupLanguage;
 import org.melati.template.TemplateEngine;
 import org.melati.util.HttpUtil;
 import org.melati.util.ThrowingPrintWriter;
 import org.melati.util.UnexpectedExceptionException;
 import org.melati.util.DatabaseInitException;
 import org.melati.util.StringUtils;
 import org.melati.util.MelatiWriter;
 import org.melati.util.MelatiSimpleWriter;
 import org.melati.util.MelatiStringWriter;
 import org.melati.util.MelatiBufferedWriter;
 import org.melati.util.servletcompat.HttpServletRequestCompat;
 
 /**
  * This is the main entry point for using Melati.
  *
  * You will need to have create a MelatiConfig in order to construct a Melati
  *
  * If you are using servlets, you will want to construct melati with a request
  * and response object.  Otherwise, simply pass in a Writer.
  *
  * A Melati exists once per request.
  *
  * Melati is typically used with Servlets, POEM (Persistent Object Engine for
  * Melati) and a Template Engine
  *
  * @see org.melati.MelatiConfig
  * @see org.melati.servlet.ConfigServlet
  * @see org.melati.servlet.PoemServlet
  * @see org.melati.servlet.TemplateServlet
  */
 
 public class Melati {
 
   private static String DEFAULT_ENCODING = "UTF8";
   private MelatiConfig config;
   private MelatiContext context;
   private HttpServletRequest request;
   private HttpServletResponse response;
   private Database database = null;
   private Table table = null;
   private Persistent object = null;
 
   // the template engine that is in use (if any)
   private TemplateEngine templateEngine;
   // the object that is used by the template engine to expand the template
   // against
   private TemplateContext templateContext;
   // check to see if we have got the writer
   private boolean gotwriter = false;
   // are we manually flushing the output
   private boolean flushing = false;
   // are we buffering the output
   private boolean buffered= true;
   // the output writer
   private MelatiWriter writer;
 
   private String encoding;
 
   /**
    * Construct a Melati for use with Servlets
    *
    * @param config - the MelatiConfig
    * @param request - the Servlet Request
    * @param response - the Servlet Response
    */
 
   public Melati(MelatiConfig config,
                 HttpServletRequest request,
                 HttpServletResponse response) {
     this.request = request;
     this.response = response;
     this.config = config;
   }
 
   /**
    * Construct a melati for use in 'stand alone' mode NB: you will not have
    * access to servlet related stuff (eg sessions)
    *
    * @param config - the MelatiConfig
    * @param output - the Writer that all output is written to
    */
 
   public Melati(MelatiConfig config, MelatiWriter writer) {
     this.config = config;
     this.writer = writer;
   }
 
   /**
    * Get the servlet request object
    *
    * @return the Servlet Request
    */
 
   public HttpServletRequest getRequest() {
     return request;
   }
 
   /**
    * It is sometimes convenient to reconstruct the request object and
    * reset it.  for example, when returning from a log-in page
    *
    * @see org.melati.login.HttpSessionAccessHandler
    * @param request - new request object
    */
 
   public void setRequest(HttpServletRequest request) {
     this.request = request;
   }
 
   /**
    * Get the servlet response object
    *
    * @return - the Servlet Response
    */
 
   public HttpServletResponse getResponse() {
     return response;
   }
 
   /**
    * Set the MelatiContext for this requrest.  If the Context has a
    * LogicalDatabase set, this will be used to establish a connection
    * to the database.
    *
    * @param context - a MelatiContext
    * @throws DatabaseInitException - if the database fails to initialise for
    *                                 some reason
    * @see org.melati.poem.LogicalDatabase
    * @see org.melati.servlet.PoemServlet
    */
 
   public void setContext(MelatiContext context) throws DatabaseInitException {
     this.context = context;
     if (context.logicalDatabase != null)
       database = LogicalDatabase.getDatabase(context.logicalDatabase);
   }
 
   /**
    * Load a POEM Table and POEM Object for use in this request.  This is useful
    * as often Servlet requests are relevant for a single Table and/or Object.
    *
    * The Table name and Object id are set from the MelatiContext
    *
    * @see org.melati.admin.Admin
    * @see org.melati.servlet.PoemServlet
    */
 
   public void loadTableAndObject() {
     if (context.table != null && database != null)
       table = database.getTable(context.table);
     if (context.troid != null && table != null)
       object = table.getObject(context.troid.intValue());
   }
 
   /**
    * Get the MelatiContext for this Request
    *
    * @return - the MelatiContext for this Request
    */
 
   public MelatiContext getContext() {
     return context;
   }
 
   /**
    * Get the POEM Database for this Request
    *
    * @return - the POEM Database for this Request
    * @see #setContext
    */
 
   public Database getDatabase() {
     return database;
   }
 
   /**
    * Get the POEM Table (if any) in use for this Request
    *
    * @return the POEM Table for this Request
    * @see #loadTableAndObject
    */
 
   public Table getTable() {
     return table;
   }
 
   /**
    * Get the POEM Object (if any) in use for this Request
    *
    * @return the POEM Object for this Request
    * @see #loadTableAndObject
    */
 
   public Persistent getObject() {
     return object;
   }
 
   /**
    * Get the Method (if any) that has been set for this Request
    *
    * @return the Method for this Request
    * @see org.melati.servlet.MelatiContext
    * @see org.melati.servlet.ConfigServlet#melatiContext
    * @see org.melati.servlet.PoemServlet#melatiContext
    */
 
   public String getMethod() {
     return context.method;
   }
 
   /**
    * Set the template engine to be used for this Request
    *
    * @param te - the template engine to be used
    * @see org.melati.servlet.TemplateServlet
    */
 
   public void setTemplateEngine(TemplateEngine te) {
     templateEngine = te;
   }
 
   /**
    * Get the template engine in use for this Request
    *
    * @return - the template engine to be used
    */
 
   public TemplateEngine getTemplateEngine() {
     return templateEngine;
   }
 
   /**
    * Set the TemplateContext to be used for this Request
    *
    * @param tc - the template context to be used
    * @see org.melati.servlet.TemplateServlet
    */
 
   public void setTemplateContext(TemplateContext tc) {
     templateContext = tc;
   }
 
   /**
    * Get the TemplateContext used for this Request
    *
    * @return - the template context being used
    */
 
   public TemplateContext getTemplateContext() {
     return templateContext;
   }
 
   /**
    * Get the MelatiConfig associated with this Request
    *
    * @return - the template context being used
    */
 
   public MelatiConfig getConfig() {
     return config;
   }
 
   /**
    * Get the PathInfo for this Request split into Parts by '/'
    *
    * @return - an array of the parts found on the PathInfo
    */
 
   public String[] getPathInfoParts() {
     String pathInfo = request.getPathInfo();
     if (pathInfo == null || pathInfo.length() < 1) return new String[0];
     pathInfo = pathInfo.substring(1);
     return StringUtils.split(pathInfo, '/');
   }
 
   /**
    * Get the Session for this Request
    *
    * @return - the Session for this Request
    */
 
   public HttpSession getSession() {
     return getRequest().getSession(true);
   }
 
   /**
    * Get the AdminUtils object for this Request
    *
    * @return - the AdminUtils
    * @see org.melati.admin.Admin
    */
 
   public AdminUtils getAdminUtils() {
     return new AdminUtils(
         HttpServletRequestCompat.getContextPath(getRequest()) +
             getRequest().getServletPath(),
         config.getStaticURL() + "/admin",
         context.logicalDatabase);
   }
 
   /**
    * Get the URL for the Logout Page
    *
    * @return - the URL for the Logout Page
    * @see org.melati.login.Logout
    */
 
   public String getLogoutURL() {
     StringBuffer url = new StringBuffer();
     HttpUtil.appendZoneURL(url, getRequest());
     url.append('/');
     url.append(config.logoutPageServletClassName());
     url.append('/');
     url.append(context.logicalDatabase);
     return url.toString();
   }
 
   /**
    * Get the URL for this Servlet Zone
    *
    * @return - the URL for this Servlet Zone
    * @see org.melati.util.HttpUtil#zoneURL
    */
 
   public String getZoneURL() {
     return HttpUtil.zoneURL(getRequest());
   }
 
   /**
    * Get the URL for the JavascriptLibrary
    *
    * @return - the URL for the JavascriptLibrary
    * @see org.melati.MelatiConfig#getJavascriptLibraryURL
    */
 
   public String getJavascriptLibraryURL() {
     return config.getJavascriptLibraryURL();
   }
 
   /**
    * Get a HTMLMarkupLanguage for use when generating HTML in templates
    *
    * @return - a HTMLMarkupLanguage
    * @see org.melati.template.TempletLoader
    * @see org.melati.util.Locale
    */
 
   public HTMLMarkupLanguage getHTMLMarkupLanguage() {
     return new HTMLMarkupLanguage(this,
                                  config.getTempletLoader(),
                                  config.getLocale());
   }
 
   /**
    * Get a WMLMarkupLanguage for use when generating WML in templates
    *
    * @return - a WMLMarkupLanguage
    * @see org.melati.template.TempletLoader
    * @see org.melati.util.Locale
    */
 
   public WMLMarkupLanguage getWMLMarkupLanguage() {
     return new WMLMarkupLanguage
                     (this,
                      config.getTempletLoader(),
                      config.getLocale());
   }
 
   /**
    * The URL of the servlet request associated with this <TT>Melati</TT>, with
    * a modified or added form parameter setting (query string component).
    *
    * @param field   The name of the form parameter
    * @param value   The new value for the parameter (unencoded)
    * @return        The request URL with <TT>field=value</TT>.  If there is
    *                already a binding for <TT>field</TT> in the query string
    *                it is replaced, not duplicated.  If there is no query
    *                string, one is added.
    * @see org.melati.util.MelatiUtil
    */
 
   public String sameURLWith(String field, String value) {
     return MelatiUtil.sameURLWith(getRequest(), field, value);
   }
 
   /**
    * The URL of the servlet request associated with this <TT>Melati</TT>, with
    * a modified or added form flag setting (query string component).
    *
    * @param field   The name of the form parameter
    * @return        The request URL with <TT>field=1</TT>.  If there is
    *                already a binding for <TT>field</TT> in the query string
    *                it is replaced, not duplicated.  If there is no query
    *                string, one is added.
    * @see org.melati.util.MelatiUtil
    */
 
   public String sameURLWith(String field) {
     return sameURLWith(field, "1");
   }
 
   /**
    * The URL of the servlet request associated with this <TT>Melati</TT>.
    *
    * @return - a string
    */
 
   public String getSameURL() {
     String qs = getRequest().getQueryString();
     return getRequest().getRequestURI() + (qs == null ? "" : '?' + qs);
   }
 
   /**
    * Turn off buffering of the output stream.
    *
    * By default, melati will buffer the output, which will not be written
    * to the output stream until you call melati.write();
    *
    * Buffering allows us to catch AccessPoemExceptions and redirect the user
    * to the login page.  This could not be done if any bytes have been written
    * to the client
    *
    * @see org.melati.test.FlushingServletTest
    */
 
   public void setBufferingOff() throws IOException {
     if (gotwriter)
       throw new IOException("You have already requested a Writer, " +
                             "and can't change it's properties now");
     buffered = false;
   }
 
   public void setFlushingOn() throws IOException {
     if (gotwriter)
       throw new IOException("You have already requested a Writer, " +
                             "and can't change it's properties now");
     flushing = true;
   }
 
   /**
    * Have we asked to access the Writer for this request?
    *
    * If you have not accessed the Writer, it is reasonable to assume that
    * nothing has been written to the output stream.
    *
    * @return - have we sucessfully called getWriter()?
    */
 
   public boolean gotWriter() {
     return gotwriter;
   }
 
   public String getEncoding() throws IOException {
     if (encoding == null)
       encoding = response == null ? DEFAULT_ENCODING :
                                     response.getCharacterEncoding();
 
     return encoding;
   }
 
   /**
    * get a Writer for this request
    *
    * if you have not accessed the Writer, it is reasonable to assume that
    * nothing has been written to the output stream.
    *
    * @return - one of:
    *
    * - the Writer that was used to construct the Melati
    * - the Writer associated with the Servlet Response
    * - a buffered Writer
    * - a ThrowingPrintWriter
    * @throws IOException if there is a problem with the writer
    */
 
   public MelatiWriter getWriter() throws IOException {
     if (writer == null) writer = createWriter();
     // if we have it, remember that fact
     if (writer != null) gotwriter = true;
     return writer;
   }
 
   /**
    * get a StringWriter
    *
    * @return - one of:
    *
    * - a StringWriter from the template engine
    * - a new StringWriter
    *
    * @throws IOException if there is a problem with the writer
    */
 
   public MelatiWriter getStringWriter() throws IOException {
     if (templateEngine != null) {
       return templateEngine.getStringWriter(getEncoding());
     } else {
       return new MelatiStringWriter();
     }
   }
 
 
   private MelatiWriter createWriter() throws IOException {
     // first effort is to use the writer supplied by the template engein
     MelatiWriter writer = null;
     if (response != null) {
       if (templateEngine != null) {
         writer = templateEngine.getServletWriter(response, buffered);
       } else {
         if (buffered) {
           writer = new MelatiBufferedWriter(response.getWriter());
         } else {
           writer = new MelatiSimpleWriter(response.getWriter());
         }
       }
     }
     if (flushing) writer.setFlushingOn();
     return writer;
   }
 
   /**
    * write the buffered output to the Writer
    * we also need to stop the flusher if it has started
    *
    * @throws IOException if there is a problem with the writer
    */
 
   public void write() throws IOException {
     // only write stuff if we have previously got a writer
     if (gotwriter) writer.close();
   }
 
   /**
    * get a PassbackVariableExceptionHandler for the TemplateEngine.
    * this allows an Exception to be handled inline during Template expansion
    *
    * for example, if you would like to render AccessPoemExceptions to a
    * String to be displayed on the page that is eturned to the client.
    *
    * @return - PassbackVariableExceptionHandler specific to the
    * template engine
    *
    * @see org.melati.template.MarkupLanguage#rendered(Exception e)
    */
 
   public Object getPassbackVariableExceptionHandler() {
     return templateEngine.getPassbackVariableExceptionHandler();
   }
 
   public void setVariableExceptionHandler(Object veh) {
     templateContext.setVariableExceptionHandler(veh);
   }
 
   /**
    * Get a User for this request (if they are logged in)
    *
    * @return - a User for this request
    */
 
   public User getUser() {
     // FIXME oops, POEM studiously assumes there isn't necessarily a user, only
     // an AccessToken
     try {
       return (User)PoemThread.accessToken();
     }
     catch (NotInSessionPoemException e) {
       return null;
     }
     catch (NoAccessTokenPoemException e) {
       return null;
     }
     catch (ClassCastException e) {
       return null;
     }
   }
 }
