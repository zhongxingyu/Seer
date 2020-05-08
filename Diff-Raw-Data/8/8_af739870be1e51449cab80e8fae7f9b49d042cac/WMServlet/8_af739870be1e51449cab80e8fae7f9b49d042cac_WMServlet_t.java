 /*
  * Copyright (C) 1998-2000 Semiotek Inc.  All Rights Reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted under the terms of either of the following
  * Open Source licenses:
  *
  * The GNU General Public License, version 2, or any later version, as
  * published by the Free Software Foundation
  * (http://www.fsf.org/copyleft/gpl.html);
  *
  *  or
  *
  * The Semiotek Public License (http://webmacro.org/LICENSE.)
  *
  * This software is provided "as is", with NO WARRANTY, not even the
  * implied warranties of fitness to purpose, or merchantability. You
  * assume all risks and liabilities associated with its use.
  *
  * See www.webmacro.org for more information on the WebMacro project.
  */
 
 
 package org.webmacro.servlet;
 
 import java.util.*;
 import java.io.*;
 import java.lang.reflect.*;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import org.webmacro.*;
 import org.webmacro.util.*;
 
 /**
  * This is the abstract base class used by all WebMacro servlets. You
  * can either subclass from it directly, or make use of one of the
  * generic subclasses provided.
  * <p>
  * It's primary function is to create a WebContext and manage a
  * Broker. It also provides a couple of convenience functions
  * that access the Broker and/or WebContext to make some commonly
  * accessed services more readily available.
  * <p>
  * @see org.webmacro.Handler
  * @see org.webmacro.Broker
  */
 abstract public class WMServlet extends HttpServlet implements WebMacro
 {
     
     private WebMacro _wm = null;
     private Broker _broker = null;
     private WebContext _wcPrototype;
     private boolean _started = false;
     private final static ThreadLocal servletConfigStore = new ThreadLocal ();
    /**
     * The name of the config entry we look for to find out what to
     * call the variable used in the ERROR_TEMPLATE
     */
     final static String ERROR_VARIABLE = "ErrorVariable";
     
    /**
     * The name of the error template we will use if something
     * goes wrong
     */
     final static String ERROR_TEMPLATE = "ErrorTemplate";
     
    /**
     * Defaults for error variable and error template
     */
     final static String ERROR_TEMPLATE_DEFAULT = "error.wm";
     final static String ERROR_VARIABLE_DEFAULT = "error";
     
    /**
     * Log object used to write out messages
     */
     private Log _log;
     
    /**
     * null means all OK
     */
     private String _problem = "Not yet initialized: Your servlet API tried to access WebMacro without first calling init()!!!";
     
    /**
     * This is the old-style init method, it just calls init(), after
     * handing the ServletConfig object to the superclass
     * @exception ServletException if it failed to initialize
     */
     final public synchronized void init (ServletConfig sc)
     throws ServletException
     {
         super.init (sc);
         init ();
     }
     
    /**
     * This method is called by the servlet runner--do not call it. It
     * must not be overidden because it manages a shared instance
     * of the broker--you can overide the start() method instead, which
     * is called just after the broker is initialized.
     */
     final public synchronized void init ()
     {
         
         if (_started)
         {
             return;
         }
         
         // locate a Broker
         
         if (_wm == null)
         {
             try
             {
                 _wm = initWebMacro ();
                 _broker = _wm.getBroker ();
             } catch (InitException e)
             {
                 _problem = "Could not initialize the broker!\n\n"
                 + "*** Check that WebMacro.properties was in your servlet\n"
                 + "*** classpath, in a similar place to webmacro.jar \n"
                 + "*** and that all values were set correctly.\n\n"
                 + e.getMessage ();
                 Log sysLog = LogSystem.getSystemLog ("servlet");
                 sysLog.error (_problem, e);
                 return;
             }
         }
         _log = _broker.getLog ("servlet", "WMServlet lifecycle information");
         
         // set up WebContext
         try
         {
             _wcPrototype = initWebContext ();
         } catch (InitException e)
         {
             _log.error ("Failed to initialize a WebContext, the initWebContext\n"
             + "method returned an exception", e);
             _problem = e.getMessage ();
             return;
         }
         
         try
         {
             start ();
             _problem = null;
         } catch (ServletException e)
         {
             _problem = "WebMacro application code failed to initialize: \n"
             + e + "\n" + "This error is the result of a failure in the\n"
             + "code supplied by the application programmer.\n";
             _log.error (_problem,e);
         }
         _log.notice ("started: " + this);
         _started = true;
         
     }
     
    /**
     * This method is called by the servlet runner--do not call it. It
     * must not be overidden because it manages a shared instance of
     * the broker--you can overide the stop() method instead, which
     * will be called just before the broker is shut down.
     */
     final public synchronized void destroy ()
     {
         stop ();
         _log.notice ("stopped: " + this);
         _wm.destroy ();
         _wm = null;
         _started = false;
         super.destroy ();
     }
     
    /**
     * Check whether or not the broker we are using has been shut down
     */
     final public boolean isDestroyed ()
     {
         return _wm.isDestroyed ();
     }
     
     
     // SERVLET API METHODS
     
    /**
     * Process an incoming GET request: Builds a WebContext up and then
     * passes it to the handle() method. You can overide this if you want,
     * though for most purposes you are expected to overide handle()
     * instead.
     * <p>
     * @param req the request we got
     * @param resp the response we are generating
     * @exception ServletException if we can't get our configuration
     * @exception IOException if we can't write to the output stream
     */
     final protected void doGet (HttpServletRequest req, HttpServletResponse resp)
     throws ServletException, IOException
     {
         doRequest (req,resp);
     }
     
    /**
     * Behaves exactly like doGet() except that it reads data from POST
     * before doing exactly the same thing. This means that you can use
     * GET and POST interchangeably with WebMacro. You can overide this if
     * you want, though for most purposes you are expected to overide
     * handle() instead.
     * <p>
     * @param req the request we got
     * @param resp the response we are generating
     * @exception ServletException if we can't get our configuration
     * @exception IOException if we can't read/write to the streams we got
     */
     protected void doPost (HttpServletRequest req, HttpServletResponse resp)
     throws ServletException, IOException
     {
         doRequest (req,resp);
     }
     
     final private void doRequest (
     HttpServletRequest req, HttpServletResponse resp)
     throws ServletException, IOException
     {
         
         WebContext context = null;
         
         if (_problem != null)
         {
             init ();
             if (_problem != null)
             {
                 try
                 {
                     resp.setContentType ("text/html");
                     FastWriter out = getFastWriter (resp.getOutputStream (),
                                                 resp.getCharacterEncoding ());
                     
                     out.write ("<html><head><title>WebMacro Error</title></head>");
                     out.write ("<body><h1><font color=\"red\">WebMacro Error: ");
                     out.write ("</font></h1><pre>");
                     out.write (_problem);
                     out.write ("</pre>");
                     out.write ("Please contact the server administrator");
                     out.flush ();
                     out.close ();
                 } catch (Exception e)
                 {
                     _log.error (_problem,e);
                 }
                 return;
             }
         }
         
         
         boolean timing = false;
         try
         {
             context = newContext (req,resp);
             timing = Flags.PROFILE && context.isTiming ();
             if (timing) context.startTiming ("WMServlet",req.getRequestURI ());
             
             Template t;
             try
             {
                 if (timing) context.startTiming ("handle");
                 t = handle (context);
             } finally
             {
                 if (timing) context.stopTiming ();
             }
             
             if (t != null)
             {
                 execute (t,context);
             }
             if (timing) context.startTiming ("WMServlet.destroyContext()");
             try
             { destroyContext (context); }
             finally
             {
                 if (timing) context.stopTiming ();
             }
         } catch (HandlerException e)
         {
             if (context == null)
             {
                 context = _wcPrototype.newInstance (req,resp);
             }
             _log.error ("Your handler failed to handle the request:" + this, e);
             Template tmpl = error (context,
             "Your handler was unable to process the request successfully " +
             "for some reason. Here are the details:<p>" +
             "<pre>" + e + "</pre>");
             execute (tmpl,context);
         } catch (Exception e)
         {
             if (context == null)
             {
                 context = _wcPrototype.newInstance (req,resp);
             }
             _log.error ("Your handler failed to handle the request:" + this, e);
             Template tmpl = error (context,
             "The handler WebMacro used to handle this request failed for " +
             "some reason. This is likely a bug in the handler written " +
             "for this application. Here are the details:<p>" +
             "<pre>" + e + "</pre>");
             execute (tmpl, context);
         } finally
         {
             if (timing) context.stopTiming ();
             context.recycle ();
         }
     }
     
     
     // CONVENIENCE METHODS & ACCESS TO THE BROKER
     
    /**
     * Create an error template using the built in error handler.
     * This is useful for returning error messages on failure;
     * it is used by WMServlet to display errors resulting from
     * any exception that you may throw from the handle() method.
     * @param context will add error variable to context (see Config)
     * @param error a string explaining what went wrong
     */
     final protected Template error (WebContext context, String error)
     {
         Template tmpl = null;
         Handler hand = new ErrorHandler ();
         try
         {
             context.put (getConfig (ERROR_VARIABLE, ERROR_VARIABLE_DEFAULT),
             error);
             tmpl = hand.accept (context);
         } catch(Exception e2)
         {
             _log.error ("Unable to use ErrorHandler", e2);
         }
         return tmpl;
     }
     
    /**
     * This object is used to access components that have been plugged
     * into WebMacro; it is shared between all instances of this class and
     * its subclasses. It is created when the first instance is initialized,
     * and deleted when the last instance is shut down. If you attempt to
     * access it after the last servlet has been shutdown, it will either
     * be in a shutdown state or else null.
     */
     final public Broker getBroker ()
     {
         // this method can be unsynch. because the broker manages its own
         // state, plus the only time the _broker will be shutdown or null
         // is after the last servlet has shutdown--so why would anyone be
         // accessing us then? if they do the _broker will throw exceptions
         // complaining that it has been shut down, or they'll get a null here.
         return _broker;
     }
     
    /**
     * Get a Log object which can be used to write to the log file.
     * Messages to the logfile will be associated with the supplied
     * type. The type name should be short as it may be printed on
     * every log line. The description is a longer explanation of
     * the type of messages you intend to write to this Log.
     */
     final public Log getLog (String type, String description)
     {
         return _broker.getLog (type, description);
     }
     
    /**
     * Get a Log object which can be used to write to the log file.
     * Messages to the logfile will be associated with the supplied
     * type. The type will be used as the description.
     */
     final public Log getLog (String type)
     {
         return _broker.getLog (type, type);
     }
     
    /**
     * Retrieve a template from the "template" provider. Equivalent to
     * getBroker().get(TemplateProvider.TYPE,key)
     * @exception NotFoundException if the template was not found
     * @exception ResourceException if the template coult not be loaded
     */
     final public Template getTemplate (String key)
     throws ResourceException
     {
         return _wm.getTemplate (key);
     }
     
    /**
     * Retrieve a URL. This is largely equivalent to creating a URL
     * object and requesting its content, though it will sit in
     * WebMacro's cache rather than re-requesting each time.
     * The content will be returned as an Object.
     */
     final public String getURL (String url)
     throws ResourceException
     {
         return _wm.getURL (url);
     }
     
     
    /**
     * Retrieve configuration information from the "config" provider.
     * Equivalent to getBroker().get(Config.TYPE,key)
     * @exception NotFoundException could not locate requested information
     */
     final public String getConfig (String key)
     throws NotFoundException
     {
         return _wm.getConfig (key);
     }
     
    /**
     * Retrieve configuration information from the "config" provider.
     * Return specified default if key could not be found
     */
     final public String getConfig (String key, String defaultValue)
     {
         try
         {
             return _wm.getConfig (key);
         }
         catch (NotFoundException e)
         {
             return defaultValue;
         }
     }
     
    /**
     * Create a new Context object
     */
     final public Context getContext ()
     {
         return _wm.getContext ();
     }
     
    /**
     * Create a new WebContext object
     */
     final public WebContext getWebContext (HttpServletRequest req, HttpServletResponse res)
     {
         return _wm.getWebContext (req,res);
     }
 
    /**
     * Convenience method for writing a template to an OutputStream.
     * This method takes care of all the typical work involved
     * in writing a template.<p>
     *
     * This method uses the default <code>TemplateOutputEncoding</code> specified in
     * WebMacro.defaults or your custom WebMacro.properties.
     *
     * @param templateName name of Template to write.  Must be accessible
     *                     via TemplatePath
     * @param out          where the output of the template should go
     * @param context      The Context (can be a WebContext too) used
     *                     during the template evaluation phase
     * @throws java.io.IOException if the template cannot be written to the
     *                             specified output stream
     * @throws ResourceException if the template name specified cannot be found
     * @throws PropertyException if a fatal error occured during the Template
     *                           evaluation phase
     */
    public void writeTemplate (String templateName, java.io.OutputStream out,
                               Context context)
             throws java.io.IOException, ResourceException, PropertyException {
 
       writeTemplate (templateName, out,
                      getConfig (WMConstants.TEMPLATE_OUTPUT_ENCODING), 
                      context);
    }
 
    /**
     * Convienence method for writing a template to an OutputStream.
     * This method takes care of all the typical work involved
     * in writing a template.
     *
     * @param templateName name of Template to write.  Must be accessible
     *                     via TemplatePath
     * @param out          where the output of the template should go
     * @param encoding     character encoding to use when writing the template
     *                     if the encoding is <code>null</code>, the default
     *                     <code>TemplateOutputEncoding</code> is used
     * @param context      The Context (can be a WebContext too) used
     *                     during the template evaluation phase
     * @throws java.io.IOException if the template cannot be written to the
     *                             specified output stream
     * @throws ResourceException if the template name specified cannot be found
     * @throws PropertyException if a fatal error occured during the Template
     *                           evaluation phase
     */
    public void writeTemplate (String templateName, java.io.OutputStream out,
                               String encoding, Context context)
               throws java.io.IOException, ResourceException, PropertyException {
 
       if (encoding == null)
          encoding = getConfig (WMConstants.TEMPLATE_OUTPUT_ENCODING);
 
       Template tmpl = getTemplate (templateName);
       FastWriter fw = getFastWriter (out, encoding);
       tmpl.write (fw, context);
       fw.close ();
    }
     
     
     // DELEGATE-TO METHODS -- COMMON THINGS MADE EASIER
     
    /**
     * This method takes a populated context and a template and
     * writes out the interpreted template to the context's output
     * stream.
     */
     final protected void execute (Template tmpl, WebContext c)
        throws IOException
     {
         FastWriter fw = null;
         boolean timing = Flags.PROFILE && c.isTiming ();
         try {
             if (timing) c.startTiming ("Template.write", tmpl);
             try {
                 HttpServletResponse resp= c.getResponse ();
                 
                 Locale locale = (Locale) tmpl.getParam (
                 WMConstants.TEMPLATE_LOCALE);
                 if (_log.loggingDebug ())
                     _log.debug ("TemplateLocale="+locale);
                 if (locale != null)
                 {
                     setLocale (resp, locale);
                 }
                 
                 String encoding = (String) tmpl.getParam (
                 WMConstants.TEMPLATE_OUTPUT_ENCODING);
                 if (encoding==null)
                 {
                     encoding = resp.getCharacterEncoding ();
                 }
                 
                 if (_log.loggingDebug ())
                    _log.debug ("Using output encoding "+encoding);
                 
                 // get a fastwriter with no output stream, forcing
                 // fastwriter to buffer the output internally.
                 // this is necessary to be compatible with JSDK 2.3
                 // where you can't call setContentType() after getOutputStream(),
                 // which could be happening during the template evaluation
                 fw = FastWriter.getInstance (_broker, encoding);
                 tmpl.write (fw, c);
                 
                 // now write the FW buffer to the response output stream
                 fw.writeTo (c.getResponse().getOutputStream ());
             } finally {
                if (timing) c.stopTiming ();
             }
             if (timing) c.startTiming ("FastWriter.close()");
             try { 
                fw.close (); fw = null; 
             } finally { 
                if (timing) c.stopTiming (); 
             }
        } catch (UnsupportedEncodingException e) {
            // can be thrown by FastWriter.getInstance
            // rethrow it, because otherwise it would be ignored
            // as an IOException
            _log.error("tried to use an unsupported encoding",e);
            throw e;
         } catch (IOException e) {
             // ignore disconnect
         } catch (Exception e) {
             String error =
             "WebMacro encountered an error while executing a template:\n"
             + ((tmpl != null) ?  (tmpl  + ": " + e + "\n") :
                 ("The template failed to load; double check the "
                 + "TemplatePath in your webmacro.properties file."));
             _log.error (error,e);
             try {
                 Template errorTemplate = error (c,
                 "WebMacro encountered an error while executing a template:\n"
                 + ((tmpl != null) ?  (tmpl  + ": ")
                 : ("The template failed to load; double check the "
                 + "TemplatePath in your webmacro.properties file."))
                 + "\n<pre>" + e + "</pre>\n");
                 
                 if (fw == null)
                     fw = FastWriter.getInstance(_broker);
                 fw.reset (fw.getOutputStream ());
                 errorTemplate.write (fw, c);
                 // now write the FW buffer to the response output stream
                 fw.writeTo (c.getResponse().getOutputStream ());
             } catch (Exception errExcept) { 
                 _log.error("Error writing error template!", errExcept);
             }
         } finally {
            try {
               if (fw != null) {
                  fw.flush ();
                  fw.close ();
                  fw = null;
               }
            } catch (Exception e3) {
               // ignore disconnect
            }
         }
     }
     
     // FRAMEWORK TEMPLATE METHODS--PLUG YOUR CODE IN HERE
     
     
    /**
     * This method is called at the beginning of a request and is
     * responsible for providing a Context for the request. The
     * default implementation calls WebContext.newInstance(req,resp)
     * on the WebContext prototype returned by the initWebContext() method.
     * This is probably suitable for most servlets, though you can override
     * it and do something different if you like. You can throw a
     * HandlerException if something goes wrong.
     */
     public WebContext newContext (
     HttpServletRequest req, HttpServletResponse resp)
     throws HandlerException
     {
         return _wcPrototype.newInstance (req,resp);
     }
     
    /**
     * This method is called to handle the processing of a request. It
     * should analyze the data in the request, put whatever values are
     * required into the context, and return the appropriate view.
     * @see getTemplate
     * @return the template to be rendered by the WebMacro engine
     * @exception HandlerException throw this to produce vanilla error messages
     * @param context contains all relevant data structures, incl builtins.
     */
     public abstract Template handle (WebContext context)
     throws HandlerException;
     
     
    /**
     * This method is called at the end of a request and is responsible
     * for cleaning up the Context at the end of the request. You may
     * not need to do anything here, but it is sometimes important if
     * you have an open database connection in your context that you
     * need to close. The default implementation calls wc.clear().
     */
     public void destroyContext (WebContext wc)
     throws HandlerException
     {
     }
     
     
    /**
     * Override this method to implement any startup/init code
     * you require. The broker will have been created before this
     * method is called; the default implementation does nothing.
     * This is called when the servlet environment initializes
     * the servlet for use via the init() method.
     * @exception ServletException to indicate initialization failed
     */
     protected void start () throws ServletException
     { }
     
    /**
     * Override this method to implement any shutdown code you require.
     * The broker may be destroyed just after this method exits. This
     * is called when the servlet environment shuts down the servlet
     * via the shutdown() method. The default implementation does nothing.
     */
     protected void stop ()
     { }
     
     
    /**
     * This method returns the WebMacro object which will be used to load,
     * access, and manage the Broker. The default implementation is to
     * return a new WM() object. You could override it and return a WM
     * object constructed with a particular configuration file, or some
     * other implementation of the WebMacro interface.
     */
     public WebMacro initWebMacro () throws InitException
     {
         return new WM (this);
     }
     
    /**
     * This method must return a cloneable WebContext which can be
     * cloned for use in responding to individual requests. Each
     * incoming request will receive a clone of the returned object
     * as its context. The default implementation is to return
     * a new WebContext(getBroker());
     */
     public WebContext initWebContext () throws InitException
     {
         return new WebContext (_broker);
     }
     
    /**
     * Set the locale on the response.  The reflection trickery is because
     * this is only defined for JSDK 2.2+
     */
     private void setLocale (HttpServletResponse resp, Locale locale)
     {
         try
         {
             Method m = HttpServletResponse.class.getMethod (
             "setLocale",
             new Class[]
             {Locale.class});
             m.invoke (resp, new Locale[]
             {locale});
             if (_log.loggingDebug ())
                 _log.debug ("Successfully set locale to "+locale);
         }
         catch (Exception e)
         {
             if (_log.loggingDebug ())
                 _log.debug ("Error set locale to "+locale+": "+e.getClass ());
         }
     }
     
    /**
     * Retrieve a FastWriter from WebMacro's internal pool of FastWriters.
     * A FastWriter is used when writing templates to an output stream
     *
     * @param out The output stream the FastWriter should write to.  Typically
     *           this will be your ServletOutputStream
     * @param enctype the Encoding type to use
     */
     public FastWriter getFastWriter (OutputStream out, String enctype)
                                    throws UnsupportedEncodingException {
         return _wm.getFastWriter (out, enctype);
     }
 }
