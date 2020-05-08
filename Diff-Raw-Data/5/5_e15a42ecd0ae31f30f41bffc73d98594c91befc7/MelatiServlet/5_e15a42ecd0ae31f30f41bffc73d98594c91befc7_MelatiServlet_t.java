 /*
  * $Source$
  * $Revision$
  *
  * Part of Melati (http://melati.org), a framework for the rapid
  * development of clean, maintainable web applications.
  *
  * -------------------------------------
  *  Copyright (C) 2000 William Chesters
  * -------------------------------------
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * A copy of the GPL should be in the file org/melati/COPYING in this tree.
  * Or see http://melati.org/License.html.
  *
  * Contact details for copyright holder:
  *
  *     William Chesters <williamc@paneris.org>
  *     http://paneris.org/~williamc
  *     Obrechtstraat 114, 2517VX Den Haag, The Netherlands
  *
  *
  * ------
  *  Note
  * ------
  *
  * I will assign copyright to PanEris (http://paneris.org) as soon as
  * we have sorted out what sort of legal existence we need to have for
  * that to make sense.  When WebMacro's "Simple Public License" is
  * finalised, we'll offer it as an alternative license for Melati.
  * In the meantime, if you want to use Melati on non-GPL terms,
  * contact me!
  */
 
 package org.melati;
 
 import java.util.*;
 import java.io.*;
 import org.melati.util.*;
 import org.melati.poem.*;
 import org.melati.templets.*;
 import org.webmacro.*;
 import org.webmacro.util.*;
 import org.webmacro.engine.*;
 import org.webmacro.servlet.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 public abstract class MelatiServlet extends MelatiWMServlet {
 
   private static Class clazz;
 
   static {
     try {
       clazz = Class.forName("org.melati.MelatiServlet");
     }
     catch (ClassNotFoundException e) {
       throw new MelatiBugMelatiException("Out of date Class.forName", e);
     }
   }
 
   /**
    * <A NAME=hackedVariable>You must use a hacked version of
    * <TT>org.webmacro.engine.Variable</TT> with Melati.</A> Sorry this has to go
    * into <TT>org.webmacro.engine</TT>: lobby Justin to stop making everything
    * final or package-private or static!  It will probably not break your
    * existing WebMacro code if you put it in your <TT>CLASSPATH</TT> since its
    * semantics are essentially the same as the traditional ones until configured
    * otherwise.  You can get the hacked version by anonymous CVS from melati.org
    * (see the <A
    * HREF=http://melati.org/cgi-bin/cvsweb.cgi/~checkout~/org/melati/qa/Installation.html>Installation
    * guide</A>).
    */
 
   public static final Object check =
       org.webmacro.engine.Variable.youNeedToBeUsingAVersionOfVariableHackedForMelati;
 
   private Properties configuration = null;
   private AccessHandler accessHandler = null;
   private TempletLoader templetLoader = null;
   private MelatiLocale locale = MelatiLocale.here;
 
   /**
    * Melati's main entry point.  Override this to do WebMacro-like things and
    * return a WebMacro template---like <TT>WMServlet.handle</TT>.
    *
    * <UL>
    * <LI>
    *
    * <A NAME=pathinfoscan>By default, the path info of the URL by which the
    * servlet was called up is examined to determine the `logical name' of the
    * Melati POEM database to which the servlet should connect, and possibly a
    * table within that database, an object within that table, and a `method' to
    * apply to that object.</A>  The URL is expected to take one of the following
    * forms:
    *
    * <BLOCKQUOTE><TT>
    *     http://<I>h</I>/<I>s</I>/<I>db</I>/
    * <BR>http://<I>h</I>/<I>s</I>/<I>db</I>/<I>meth</I>
    * <BR>http://<I>h</I>/<I>s</I>/<I>db</I>/<I>tbl</I>/<I>meth</I> 
    * <BR>http://<I>h</I>/<I>s</I>/<I>db</I>/<I>tbl</I>/<I>troid</I>/<I>meth</I>
    * </TT></BLOCKQUOTE>
    *
    * and the following components are broken out of the path info and passed to
    * your application code in the <TT>melati</TT> parameter (which is also
    * copied automatically into <TT>context</TT> so that it is easily available
    * in templates):
    *
    * <TABLE>
    *   <TR>
    *     <TD><TT><I>h</I></TT></TD>
    *     <TD>host name, such as <TT>www.melati.org</TT></TD>
    *   </TR>
    *   <TR>
    *     <TD><TT><I>s</I></TT></TD>
    *     <TD>
    *       servlet-determining part, such as
    *       <TT>melati/org.melati.admin.Admin</TT>
    *     </TD>
    *   </TR>
    *   <TR>
    *     <TD><TT><I>db</I></TT></TD>
    *     <TD>
    *       The first element of the path info is taken to be the `logical name'
    *       of the Melati POEM database to which the servlet should connect.  It
    *       is mapped onto JDBC connection details via the config file
    *       <TT>org.melati.LogicalDatabase.properties</TT>, of which there is an
    *       example in the source tree.  This is automatically made available in
    *       templates as <TT>$melati.Database</TT>.
    *     </TD>
    *   <TR>
    *     <TD><TT><I>tbl</I></TT></TD>
    *     <TD>
    *       The DBMS name of a table with which the servlet is concerned:
    *       perhaps it is meant to list its contents.  This is automatically
    *       made available in templates as <TT>$melati.Table</TT>.
    *     </TD>
    *   </TR>
    *   <TR>
    *     <TD><TT><I>troid</I></TT></TD>
    *     <TD>
    *       The POEM `troid' (table row identifier, or row-unique integer) of a
    *       row within <TT><I>tbl</I></TT> with which the servlet is concerned:
    *       perhaps it is meant to display it.  This is automatically made
    *       available in templates as <TT>$melati.Object</TT>.
    *     </TD>
    *   </TR>
    *   <TR>
    *     <TD><TT><I>meth</I></TT></TD>
    *     <TD>
    *       A freeform string telling your servlet what it is meant to do.  This
    *       is automatically made available in templates as
    *       <TT>$melati.Method</TT>.
    *     </TD>
    *   </TR>
    * </TABLE>
    *
    * You can change the way these things are determined by overriding
    * <TT>melatiContext</TT>.
    *
    * <LI>
    *
    * Any POEM database operations you perform will be done with the access
    * rights of the POEM <TT>User</TT> associated with the servlet session.  If
    * there is no established servlet session, the current user will be set to
    * the default `guest' user.  If this method terminates with an
    * <TT>AccessPoemException</TT>, indicating that you have attempted something
    * which you aren't entitled to do, the user will be prompted to log in, and
    * the original request will be retried.  The precise mechanism used for
    * login is <A HREF=#loginmechanism>configurable</A>.
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
    * <LI>
    *
    * <A NAME=loginmechanism>It's possible to configure how your
    * <TT>MelatiServlet</TT>-derived servlets implement user login.</A> If the
    * properties file <TT><A
    * HREF=../../../../org.melati.MelatiServlet.properties>org.melati.MelatiServlet.properties</A></TT>
    * exists and contains a setting
    * <TT>org.melati.MelatiServlet.accessHandler=<I>foo</I></TT>, then
    * <TT><I>foo</I></TT> is taken to be the name of a class implementing the
    * <TT>AccessHandler</TT> interface.  The default is
    * <TT>HttpSessionAccessHandler</TT>, which stores the user id in the servlet
    * session, and redirects to the <TT>Login</TT> servlet to throw up
    * WebMacro-templated login screens.  If instead you specify
    * <TT>HttpBasicAuthenticationAccessHandler</TT>, the user id is maintained
    * using HTTP Basic Authentication (RFC2068 11.1, the mechanism commonly
    * used to password-protect static pages), and the task of popping up login
    * dialogs is delegated to the browser.  The advantage of the former method
    * is that the user gets a more informative interface which is more under the
    * designer's control; the advantage of the latter method is that no cookies
    * or URL rewriting are required---for instance it is probably more
    * appropriate for WAP phones.  Both methods involve sending the user's
    * password in plain text across the public network.
    *
    * </UL>
    *
    * @param context     a WebMacro `context' object, representing the
    *                    template expansion namespace and carrying the servlet
    *                    request, session <I>etc.</I>
    * @param melati      a source of information about the Melati database
    *                    context (database, table, object) and utility objects
    *                    like error handlers
    *
    * @see org.melati.poem.Database#guestAccessToken
    * @see org.melati.poem.PoemThread#commit
    * @see org.melati.poem.PoemThread#rollback
    * @see org.webmacro.servlet.WMServlet#handle
    * @see #melatiContext
    * @see AccessHandler
    * @see HttpSessionAccessHandler
    * @see Login
    * @see HttpBasicAuthenticationAccessHandler
    */
 
   protected Template handle(WebContext context, Melati melati)
       throws PoemException, WebMacroException {
     return null;
   }
 
   /**
    * Provided for drop-in compatibility with servlets derived from
    * <TT>WMServlet</TT>.  You probably mean to use <TT>handle(WebContext,
    * Melati)</TT>.
    *
    * @see #handle(org.webmacro.servlet.WebContext, org.melati.Melati)
    */
 
   protected Template handle(WebContext context)
       throws PoemException, WebMacroException {
     return handle(context, (Melati)context.get("melati"));
   }
 
   protected final Template _handle(WebContext context)
       throws Exception {
     try {
       return handle(context);
     }
     catch (Exception e) {
       PoemThread.rollback();
       e.printStackTrace();
       throw e;
     }
   }
 
   /**
    * Handle an exception that occurs during the execution of <TT>handle</TT> or
    * during the expansion of the template it returns.  The base version returns
    * the standard WebMacro error template as defined in your
    * <TT>WebMacro.properties</TT>, except if the problem was an access failure
    * (<TT>AccessPoemException</TT>), in which case
    * <TT>accessHandler().handleAccessException</TT> is invoked instead.
    *
    * @param context     the <TT>WebContext</TT> of the original template
    *
    * @param exception   what went wrong: for problems expanding template
    *                    variables when variable error propagation is enabled,
    *                    you will see a <TT>VariableException</TT> (available in
    *                    the <A HREF=#hackedVariable>Melati-hacked version of
    *                    WebMacro</A> against which you must compile Melati)
    *                    whose <TT>subException</TT> is what you are interested
    *                    in
    *
    * @return a template to expand, or <TT>null</TT> if you have already
    *         sent something (like a redirect) back to the client
    *
    * @see #handle(org.webmacro.servlet.WebContext)
    * @see #handle(org.webmacro.servlet.WebContext, org.melati.Melati)
    * @see org.melati.poem.AccessPoemException
    * @see org.webmacro.util.VariableException
    * @see AccessHandler#handleAccessException
    */
 
   protected Template handleException(WebContext context, Exception exception)
       throws Exception {
 
     Exception underlying =
         exception instanceof VariableException ?
           ((VariableException)exception).subException : exception;
 
     if (underlying == null || !(underlying instanceof AccessPoemException))
       throw exception;
     else
       return accessHandler().handleAccessException(
                  context, (AccessPoemException)underlying);
   }
 
   protected final Template _handleException(WebContext context, Exception exception)
       throws Exception {
     try {
       return handleException(context, exception);
     }
     catch (Exception e) {
       PoemThread.rollback();
       e.printStackTrace();
       throw e;
     }
   }
 
   private void superDoRequest(WebContext context)
       throws ServletException, IOException {
     super.doRequest(context);
   }
 
   private static class TrappedException extends RuntimeException {
     public TrappedException(String message) {
       super(message);
     }
   }
 
   /**
    * Implements the path info scanning process described <A
    * HREF=#pathinfoscan>above</A>.  Override this to do it differently,
    * <I>e.g.</I> using named fields rather than path info.  Note that you must
    * whatever you do return a legal <TT>MelatiContext</TT>, and its
    * <TT>logicalDatabase</TT> field must be filled in.
    *
    * @see MelatiContext#logicalDatabase
    */
 
   protected MelatiContext melatiContext(WebContext context)
       throws MelatiException {
 
     String pathInfo = context.getRequest().getPathInfo();
     String[] parts = StringUtils.split(pathInfo, '/');
 
     if (parts.length < 2)
       // FIXME make this nicer since users will see it if they play around
       // with URLs
       throw new PathInfoException(
           "The servlet expects to see pathinfo in the form " +
           "/db/, /db/method, /db/table/method or /db/table/troid/method");
 
     try {
       MelatiContext it = new MelatiContext();
 
       it.method = parts[parts.length - 1];
 
       switch (parts.length - 1) {
         case 4:
           it.troid =
               parts[3].equals("new") ? // see Add.wm
                   new Integer(-1) : new Integer(parts[3]);
         case 3:
           it.table = parts[2];
         default:
           it.logicalDatabase = parts[1];  // provoke exception if 0-length
       }
 
       return it;
     }
     catch (Exception e) {
       throw new PathInfoException(pathInfo, e);
     }
   }
 
   /**
    * Overrides a basic WMServlet entry point to allow Melati to set up its
    * environment before WebMacro takes over.  We have to take control very
    * early, since the POEM database session must be wrapped around the whole
    * WebMacro logic: the session must be active while the template is expanded.
    * NB the application programmer's entry point to Melati is
    * <TT>handle</TT>, above.
    *
    * @see #handle(org.webmacro.servlet.WebContext)
    * @see #handle(org.webmacro.servlet.WebContext, org.melati.Melati)
    */
 
   protected void doRequest(final WebContext contextIn)
       throws ServletException, IOException {
 
     try {
       // Set up a POEM session and call the application code
 
       final MelatiContext melatiContext = melatiContext(contextIn);
 
       // dearie me, what a lot of hoops to jump through
       // at the end of the day Java is terribly poorly suited to this kind of
       // lambda idiom :(
 
       final MelatiServlet _this = this;
 
       final Database database;
       try {
         database = LogicalDatabase.named(melatiContext.logicalDatabase);
       }
       catch (DatabaseInitException e) {
         e.printStackTrace();
         throw new ServletException(e.toString());
       }
 
       database.logSQL = true;
 
       database.inSession(
           AccessToken.root,
           new PoemTask() {
             public void run() {
               try {
                 WebContext context =
                     accessHandler().establishUser(contextIn, database);
                 if (context != null) {
                   context.put("melati",
                               new Melati(context, database, melatiContext,
                                          locale(), templetLoader()));
                   context.put(Variable.EXCEPTION_HANDLER,
                               PropagateVariableExceptionHandler.it);
                   _this.superDoRequest(context);
                 }
               }
               catch (Exception e) {
                 // FIXME oops we have to do this in-session!  This is because
                 // some PoemExceptions (might?) generate their messages on the
                 // fly from Persistents that can't be interrogated outside a
                 // database session.  Indeed the toString() can actually
                 // generate a further exception.  Not very satisfactory.
 
                 e.printStackTrace();
                 throw new TrappedException(e.toString());
               }
             }
           });
     }
     catch (TrappedException e) {
      throw new ServletException(e.toString());
     }
     catch (MelatiException e) {
      throw new ServletException(e.toString());
     }
   }
 
   protected AccessHandler accessHandler() {
     return accessHandler;
   }
 
   protected TempletLoader templetLoader() {
     return templetLoader;
   }
 
   protected MelatiLocale locale() {
     return locale;
   }
 
   /**
    * Initialise a <TT>MelatiServlet</TT>.  Loads
    * <TT>org.melati.MelatiServlet.properties</TT> and reads the access handler
    * setting out.  If you override this method to do application-specific
    * initialisation in a subclass servlet, you must make sure to call
    * <TT>super.init()</TT>.
    */
 
   protected void init() throws ServletException {
     super.init();
 
     // Load org.melati.MelatiServlet.properties, or set blank configuration
 
     String pref = clazz.getName() + ".";
     String accessHandlerProp = pref + "accessHandler";
     String templetLoaderProp = pref + "templetLoader";
 
     try {
       configuration =
           PropertiesUtils.fromResource(clazz, pref + "properties");
     }
     catch (FileNotFoundException e) {
       configuration = new Properties();
     }
     catch (IOException e) {
       throw new ServletException(e.toString());
     }
 
     try {
       accessHandler = (AccessHandler)PropertiesUtils.instanceOfNamedClass(
 	  configuration, accessHandlerProp, "org.melati.AccessHandler",
 	  "org.melati.HttpSessionAccessHandler");
 
       templetLoader = (TempletLoader)PropertiesUtils.instanceOfNamedClass(
 	  configuration, templetLoaderProp, "org.melati.templets.TempletLoader",
 	  "org.melati.templets.ClassNameTempletLoader");
     }
     catch (Exception e) {
       throw new ServletException(e.toString());
     }
   }
 }
