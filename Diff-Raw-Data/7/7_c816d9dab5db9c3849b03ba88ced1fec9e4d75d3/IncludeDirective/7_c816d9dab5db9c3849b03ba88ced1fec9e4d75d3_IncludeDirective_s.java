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
 
 package org.webmacro.directive;
 
 import org.webmacro.*;
 import org.webmacro.engine.BuildContext;
 import org.webmacro.engine.BuildException;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.StringTokenizer;
 
 /**
  * IncludeDirective allows you to include other text files or Templates into
  * the current Template.<p>
  *
  * Syntax:<pre>
  *    #include [as text|template|macro] quoted-string-variable
  *</pre>
  *
  * <code>IncludeDirective</code> has 4 modes of operation which are detailed
  * below.<p>
  *
  * Please note that #include is now the preferred way to include files or
  * templates.  Although you can still use #parse (in the same manner as
  * described below), its use has been deprecated.<p>
  *
  * <b>Including as text</b><br>
  * Including a file as text causes the raw bytes of the file to be included
  * in the current template.  The file is <b>not</b> considered to be a Template,
  * and is not parsed by WebMacro.  This allows you to include external files
  * that contain javascript or stylesheets without fear of mangling by WebMacro's
  * parser.<p>
  *
  * Files included as text are found using the default <code>URLProvider</code>.
  * If the URLProvider cannot find the file, the Broker is asked to find it.
  * This allows you to include files from any of the following places:
  * <li>other websites
  * <li>local filesystem
  * <li>active classpath<p>
  *
  * Files included as text are located once.  Changes to the included file
  * will not be found by WebMacro!<p>
  *
  * Examples:<pre>
  *
  *   // include your system password file (not a good idea! :)
  *   #include as text "/etc/password"
  *
  *   // include the WebMacro homepage
  *   #include as text "http://www.webmacro.org"
  *
  *   // inlude a file that is in your classpath
  *   #include as text "somefile.txt"
  *
  * </pre><br>
  *
  * <b>Including as template</b><br>
  * Including a file as template causes the file to be parsed and evaluated
  * by WebMacro using the current <code>Context</code>.  The evaluated output of
  * the Template is included in your main template.<p>
  *
  * Please note that files included "as template" that contains #macro's <b>DO NOT</b>
  * make their #macro's available to the outer template.  See "Including as macro" below
  * for this.<p>
  *
  * Files included as template are found in your <code>TemplatePath</code>.
  * This path, if not explicitly set in <code>WebMacro.properties</code>, defaults
  * to the system classpath (standalone and JSDK 1.0), or the "web-app" directory
  * (JSDK 2.x).<p>
  *
  * Files included as templates are located and conditionally reloaded/parsed when
  * they change.<p>
  *
  * Examples:<pre>
  *
  *   // include a global header template
  *   #include as template "header.wm"
  *
  *   // include a header template from a subdirectory of your TemplatePath
  *   #include as template "common/header.wm"
  *
  *   // include a header template using an absolute path in your TemplatePath
  *   #include as template "/mysite/common/header.wm"
  *
  * </pre><br>
  *
  * <b>Including as macro</b><br>
  * Including a file as a macro is similiar to the above, except that #macro's
  * defined in the included file <b>are</b> made available to the outer template.
  * "Macro" templates are found using the same TemplatePath settings as
  * "#include as template".<p>
  *
  * Files included as macros are located once.  Changes to the included template
  * will not be found by WebMacro!<p>
  *
  * Exapmles:<pre>
  *
  *   // include a template with macros
  *   #include as macro "my_macros.wm"
  *
  * </pre><br>
  *
  *
  * <b>Including without qualification</b><br></a>
  * Including a file without first qualifing it as a <i>text</i> file or
  * <i>template</i> causes <code>IncludeDirective</code> to make some educated
  * guesses about what type of file it is.<p>
  *
  * If the filename contains <code>://</code>, it is assumed to be a URL and is
  * treated as if it were a <code>text</code> file.<p>
  *
  * Else if the filename ends with any of the configured template file extensions
  * (see below), it is assumed to be a template.<p>
  *
  * Else it is assumed to be <code>text</code>.<p>
  *
  * Examples:<pre>
  *
  *    // include homepage of WebMacro -- assumed to be "as text"
  *    #include "http://www.webmacro.org/"
  *
  *    // include a template -- assumed to be "as template"
  *    #include "header.wm"
  *
  *    // include a text file from local filesystem -- assumed to be "as text"
  *    #include "somefile.txt"
  *
  * </pre>
  *
  *
  * <b><code>WebMacro.properties</code> Configuration Options</b><br>
  *
  * <code>include.TemplateExtensions</code>: list of file extensions<br>
  * This list of file extensions is used to determine the file type of an
  * unqualified #include'd file.<p>
  *
  * The default set of extensions are: <code>wm, wmt, tml</code>
  *
  * @author <a href=mailto:ebr@tcdi.com>Eric B. Ridge</a>
  * @since the beginning, but consolidated with #parse post 0.97
  *
  */
 public class IncludeDirective extends Directive
 {
 
     /** the file to include is a Template */
     public static final int TYPE_TEMPLATE = 0;
     /** the file to include as a Template containing #macro's */
     public static final int TYPE_MACRO = 1;
     /** the file to include is a static file. */
     public static final int TYPE_TEXT = 2;
     /** the file to include is unknown */
     public static final int TYPE_DYNAMIC = 3;
 
 
     private static final int PARSE_AS_K = 1;
     private static final int PARSE_TEMPLATE_K = 2;
     private static final int PARSE_TEXT_K = 3;
     private static final int PARSE_MACRO_K = 4;
     private static final int PARSE_FILENAME = 5;
 
     private static final ArgDescriptor[] _args = new ArgDescriptor[]{
         new OptionalGroup(4),
         new KeywordArg(PARSE_AS_K, "as"),
         new OptionalGroup(1),
         new KeywordArg(PARSE_TEMPLATE_K, "template"),
         new OptionalGroup(1),
         new KeywordArg(PARSE_TEXT_K, "text"),
         new OptionalGroup(1),
         new KeywordArg(PARSE_MACRO_K, "macro"),
         new QuotedStringArg(PARSE_FILENAME),
     };
     private static final DirectiveDescriptor _desc = new DirectiveDescriptor("include", null, _args, null);
 
     public static DirectiveDescriptor getDescriptor ()
     {
         return _desc;
     }
 
 
     //
     // these values are customized for this directive during build()
     //
 
     /** place holder for the TemplateExtensions configuration key name */
     private String TEMPLATE_EXTENSIONS_NAME = ".TemplateExtensions";
 
 
     /** Logging can be good */
     protected Log _log;
     /** the included file type.  one of TYPE_TEMPLATE, TYPE_STATIC, TYPE_MACRO, or TYPE_DYNAMIC */
     protected int _type;
     /** the filename as a Macro, if the filename arg is a Macro */
     protected Macro _macFilename;
     /**
      * the filename as a String, if it is something we can determine during
      * build()
      */
     protected String _strFilename;
     /**
      * the name given to the directive by webmacro configuration.  Used in
      * conjuction with the <code>StrictCompatibility</code>  configuration
      * and to make determinitaions on how the #included file should be included.
      */
     protected String _directiveName;
 
     /**
      * Build this use of the directive.<p>
      *
      * The specified file to include is included during <b>build/parse</b>
      * time if:<br>
      *    1) The specified filename is a String, not a $Variable; and<br>
      *    2) The "as" keyword is <b>"macro"</b>; or<br>
      *    3) if the <code>Lazy</code> configuration option is set for
      *       this directive.<p>
      *
      * Otherwise, template is found and including during runtime evaluation
      * of this directive.
      */
     public final Object build (DirectiveBuilder builder, BuildContext bc) throws BuildException
     {
         Broker broker = bc.getBroker();
         _log = bc.getLog("IncludeDirective");
         // build configuration key names, since they're based
         // on the configured name of this directive
         _directiveName = builder.getName();
         TEMPLATE_EXTENSIONS_NAME = _directiveName + TEMPLATE_EXTENSIONS_NAME;
 
         // determine what type of file we need to deal with
         if (builder.getArg(PARSE_TEXT_K, bc) != null)
         {
             _type = TYPE_TEXT;
         }
         else if (builder.getArg(PARSE_TEMPLATE_K, bc) != null)
         {
             _type = TYPE_TEMPLATE;
         }
         else if (builder.getArg(PARSE_MACRO_K, bc) != null)
         {
             _type = TYPE_MACRO;
         }
         else
         {
             _type = TYPE_DYNAMIC;
         }
 
 
         // if the filename passed to us was a Macro (needs to be evaluated later)
         // then store it as _macFilename.  Otherwise, assume it's a String
         // and we'll just use that string as the filename
         Object o = builder.getArg(PARSE_FILENAME, bc);
         if (o instanceof Macro)
         {
             if (_type == TYPE_TEXT || _type == TYPE_MACRO)
             {
                 _log.warning("Included a 'static' file type using a dynamic filename. "
                         + "File will be included, but any included #macro's will not at " + bc.getCurrentLocation());
             }
             _macFilename = (Macro) o;
         }
         else
         {
             _strFilename = o.toString();
             if (_strFilename == null || _strFilename.length() == 0)
                 throw makeBuildException("Filename cannot be null or empty");
 
 
             if (_type == TYPE_TEXT)
             {
                 // we're a static type, need to
                 // include the file (by returning it) now,
                 // during build time
                 try
                 {
                     return getThingToInclude(broker, _type, _strFilename);
                 }
                 catch (Exception e)
                 {
                     throw makeBuildException("Unable to include as text", e);
                 }
             }
             else if (_type == TYPE_MACRO)
             {
                 // we're a template type.  ned to get the template (already parsed)
                 // and merge its macros into our build context.
                 // then we return the template so its contents can also be included
                 Template t = null;
                 try
                 {
                     t = getTemplate(broker, _strFilename);
                     bc.mergeConstants(t);
                 }
                 catch (Exception e)
                 {
                     throw makeBuildException("Unable to include as macro", e);
                 }
                return t;
             }
             else if (_type == TYPE_DYNAMIC)
             {
                 // being dynamic means we need to guess the
                 // file type based on the file's extension
                 // and take care of finding the file during runtime
                 _type = guessType(broker, _strFilename);
             }
 
         }
 
         // we are configured to be lazy, or we couldn't determine the filename
         // during the build() process (b/c it is a Macro)
         return this;
     }
 
     /**
      * Write out the included file to the specified FastWriter.  If the
      * included file is actually a template, it is evaluated against the
      * <code>context</code> parameter before being written to the FastWriter
      */
     public void write (FastWriter out, Context context) throws PropertyException, IOException
     {
         Broker broker = context.getBroker();
 
         // the filename arg passed to us was a Macro, so
         // evaluate and check it now
         if (_macFilename != null)
         {
             _strFilename = _macFilename.evaluate(context).toString();
             if (_strFilename == null || _strFilename.length() == 0)
             {
                 throw makePropertyException("Filename cannot be null or empty");
             }
         }
 
         if (_log.loggingDebug() && context.getCurrentLocation().indexOf(_strFilename) > -1)
         {
             // when in debug mode, output a warning if a template tries to include itself
             // there are situtations where this is desired, but it's good to make
             // the user aware of what they're doing
             _log.warning(context.getCurrentLocation() + " includes itself.");
         }
 
         // this should only be true if StrictCompatibility is set to false
         // and "as <something>" wasn't specified in the arg list
         if (_type == TYPE_DYNAMIC)
             _type = guessType(broker, _strFilename);
 
         if (_log.loggingDebug())
         {
             _log.debug("Including '" + _strFilename + "' as "
                     + ((_type == TYPE_MACRO) ? "MACRO"
                     : (_type == TYPE_TEMPLATE) ? "TEMPLATE"
                     : (_type == TYPE_TEXT) ? "TEXT"
                     : "UNKNOWN.  Throwing exception"));
         }
 
         Object toInclude = getThingToInclude(broker, _type, _strFilename);
         switch (_type)
         {
             case TYPE_MACRO:
                 // during runtime evaluation of a template,
                 // a TYPE_MACRO doesn't really work as expected.
                 // we logged a warning above in build(), but
                 // we still need to write it out as a template
                 // so just fall through
             case TYPE_TEMPLATE:
                 out.write(((Template) toInclude).evaluateAsBytes(out.getEncoding(), context));
                 break;
 
             case TYPE_TEXT:
                 // static types are strings
                 out.write(toInclude.toString());
                 break;
 
             default:
                 // should never happen
                 throw makePropertyException("Unrecognized file type: " + _type);
         }
     }
 
     /**
      * get an array of Template file extensions we should use, if type==dynamic,
      * to decide if the specified file is a template or not
      */
     protected String[] getTemplateExtensions (Broker b)
     {
         String[] ret = (String[]) b.getBrokerLocal(TEMPLATE_EXTENSIONS_NAME);
 
         if (ret == null)
         {
             String prop = b.getSettings().getSetting(TEMPLATE_EXTENSIONS_NAME, "wm");
             StringTokenizer st = new StringTokenizer(prop, ",; ");
             ret = new String[st.countTokens()];
             int x = 0;
             while (st.hasMoreElements())
             {
                 ret[x] = st.nextToken();
                 x++;
             }
 
             b.setBrokerLocal(TEMPLATE_EXTENSIONS_NAME, ret);
         }
 
         return ret;
     }
 
     /**
      * if the filename contains <i>://</i> assume it's a file b/c it's probably
      * a url.<p>
      *
      * If the filename ends with any of the configured
      * <code>ParseDirective.TemplateExtensions</code>, assume it's a template.<p>
      *
      * Otherwise, it must be a file
      */
     protected int guessType (Broker b, String filename)
     {
         if (filename.indexOf("://") > -1)
         {
             return TYPE_TEXT;
         }
         else
         {
             String[] extensions = getTemplateExtensions(b);
             for (int x = 0; x < extensions.length; x++)
             {
                 if (filename.endsWith(extensions[x]))
                     return TYPE_TEMPLATE;
             }
         }
 
         return TYPE_TEXT;
     }
 
     /**
      * get the template or file that the user wants to include, based on the
      * specified type.  This method does not know how to get a file whose type
      * is "TYPE_DYNAMIC".
      */
     protected Object getThingToInclude (Broker b, int type, String filename) throws PropertyException
     {
         switch (type)
         {
             case TYPE_TEMPLATE:
                 // wants to include a template
                 return getTemplate(b, filename);
 
             case TYPE_MACRO:
                 // wants to include a template
                 return getTemplate(b, filename);
 
             case TYPE_TEXT:
                 // wants to include a static file
                 return getFile(b, filename);
 
             case TYPE_DYNAMIC:
                 // this should never happen
                 throw makePropertyException("Internal Error.  Never guessed file type");
 
             default:
                 // default case should never happen b/c we take care of this
                 // during build()
                 throw makePropertyException("Internal Error.  Unrecognized file type: " + type);
         }
     }
 
     /**
      * get a Template via the "template" provider known by the specified broker
      */
     protected Template getTemplate (Broker b, String name) throws PropertyException
     {
         try
         {
             return (Template) b.get("template", name);
         }
         catch (NotFoundException nfe)
         {
             throw makePropertyException("Not found by template provider");
         }
         catch (ResourceException re)
         {
             throw makePropertyException("Unable to get template", re);
         }
         catch (Exception e)
         {
             throw makePropertyException("Unexpected ectpion while getting template");
         }
     }
 
     /**
      * get the contents of a file (local file or url) via the "url" provider
      * known by the specified broker.  If the url provider can't find it
      * we check the Broker (Broker.getResource).
      */
     protected String getFile (Broker b, String name) throws PropertyException
     {
         try
         {
             // first, ask the URL provider (if we have one) to find the file for us
             return b.get("url", name).toString();
         }
         catch (Exception e)
         {
             // for whatever reason, the URL provider couldn't find the file
             // (maybe we don't have a URL provider?).  No matter, directly
             // ask the Broker to load it as a resource
             URL url = null;
 
             try
             {
                 url = b.getResource(name);
                 if (url == null) // doh!  the Broker couldn't find it either.  Guess it doesn't exist
                     throw makePropertyException("Resource not found by URL provider or Broker");
 
                 // open a URLConnection...
                 URLConnection conn = url.openConnection();
                 StringBuffer sb = new StringBuffer();
                 InputStream in = conn.getInputStream();
                 String enc = conn.getContentEncoding();
                 if (enc == null)
                     enc = b.getSetting("TemplateEncoding");
 
                 // ...and stream the contents of the URL into a String
                 int cnt = 0;
                 byte[] buff = new byte[4096];
                 while ((cnt = in.read(buff)) > 0)
                 {
                     sb.append(new String(buff, 0, cnt, enc));
                 }
                 in.close();
 
                 // return the string form of the resource.
                 // This is what will be included in the template
                 return sb.toString();
             }
             catch (IOException ioe)
             {
                 throw makePropertyException("Error streaming file from: " + url, ioe);
             }
         }
     }
 
     public void accept (TemplateVisitor v)
     {
         v.beginDirective(_desc.name);
         v.visitDirectiveArg("IncludeDirective", _strFilename);
         v.endDirective();
     }
 
     //
     // helper methods for throwing exceptions
     //
 
     private BuildException makeBuildException (String message)
     {
         return makeBuildException(message, null);
     }
 
     private BuildException makeBuildException (String message, Exception cause)
     {
         message = "#" + _directiveName + " " + _strFilename + ": " + message;
         if (cause != null)
             return new BuildException(message, cause);
         else
             return new BuildException(message);
     }
 
     private PropertyException makePropertyException (String message)
     {
         return makePropertyException(message, null);
     }
 
     private PropertyException makePropertyException (String message, Exception cause)
     {
         message = "#" + _directiveName + " " + _strFilename + ": " + message;
         if (cause != null)
             return new PropertyException(message, cause);
         else
             return new PropertyException(message);
     }
 }
