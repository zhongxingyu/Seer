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
 
 package org.webmacro.resource;
 
 import java.util.*;
 import java.io.*;
 import org.webmacro.util.*;
 import org.webmacro.servlet.LocaleTool;
 import org.webmacro.*;
 import org.webmacro.engine.*;
 import java.net.*;
 
 /**
  * FileTemplate objects read their template data from a text file.
  */
 
 public class URLTemplate extends WMTemplate
 {
     /**
      * CVS revision
      */
 
     public static final String RCS = "@(#) $Id$";
 
     /**
      * The location of the resourse (file) this template was read from.
      */
 
     private final URL _url;
 
     /**
      * The physical file referred to by "file:" and "jar:" URLs
      */
 
     private File underLyingFile = null;
 
     /**
      * The time the underlying file was last modified
      */
 
     private long underLyingFileLastModTime = 0;
 
     /**
      * Cache for any per-directory encoding files
      */
 
     private final HashMap propertiesCache = new HashMap();
 
     private String _inputEncoding = null;
     private String _outputEncoding = null;
     private Locale _outputLocale = null;
 
     /**
      * A dummy object used as a place holder in the encoding cache
      */
 
     private static final Object dummy = new Object();
 
     /**
      * Instantiate a template based on the specified file
      *
      * We use can use the special case or URLs like
      *
      *        file:xxxxxx
      * or
      *        jar:xxxxxx!yyyyyy
      *
      * extracting the xxxxxx.  In the latter case the reference is to the jar containing the
      * template.
      */
 
     public URLTemplate(
         Broker broker,
         URL templateURL
         )
     {
         super(broker);
         _url = templateURL;
         String _u = _url.toExternalForm();
         
         _log.debug("URLTemplate: "+_u);
 
         if (_u.startsWith("jar:"))
         {
             int p = _u.indexOf("!");
             _u = _u.substring(4,p);
         }
 
         if (_u.startsWith("file:"))
         {
             underLyingFile = new File(_u.substring(5));
             underLyingFileLastModTime = underLyingFile.lastModified();
         }
 
         setupLocalProperties();
     }
 
     /**
      * URL based templates are difficult to detect as changed in general, but the two
      * special cases for "file:[path]" and "jar:file:[jarpath]![path]" mean we have reference
      * to the actual file
      */
 
     public boolean shouldReload()
     {
         if (underLyingFile == null) return false;
         long lastMod = underLyingFile.lastModified();
 
         return ((lastMod > 0) && (lastMod> underLyingFileLastModTime));
     }
 
 
     /**
      * Look for TemplateEncoding in a file WebMacro.local in the same
      * directory as the template
      *
      * TODO - should make the encoding cache a bit smarter- doesn't detect
      * if the file changes
      */
 
     private void setupLocalProperties()
     {
         InputStream is = null;
         URL u = null;
 
         try
         {
             u = new URL(_url, WMConstants.WEBMACRO_LOCAL_FILE);
             _log.debug("Looking for encodings file: "+u);
             Object obj = propertiesCache.get(u);
 
             if (obj != null)
             {
                 return;
             }
 
             Properties p = new Properties();
             is = u.openStream();
             p.load(is);
             _inputEncoding = p.getProperty(WMConstants.TEMPLATE_INPUT_ENCODING);
             if (_inputEncoding == null)
             {
                _inputEncoding = getDefaultEncoding();
             }
             _outputEncoding = p.getProperty(WMConstants.TEMPLATE_OUTPUT_ENCODING);
             
             String loc = p.getProperty(WMConstants.TEMPLATE_LOCALE);
             if (loc != null)
             {
                 _outputLocale = LocaleTool.buildLocale(loc);
             }
 
             propertiesCache.put(u,dummy);
             
             
         }
         catch (Exception e)
         {
             // if there's an Exception here, put a dummy object here so
             // we don't try to look for the file every time
             //
             propertiesCache.put(u, dummy);
         }
         finally
         {
             if (is != null)
             {
                 try
                 {
                     is.close();
                 }
                 catch (Exception ignore) {}
             }
         }
     }
 
     /**
      * Get the stream the template should be read from. Parse will
      * call this method in order to locate a stream.
      *
      */
     protected Reader getReader() throws IOException
     {
         _log.debug("Using encoding "+_inputEncoding);
 
         if (_inputEncoding.equals("native_ascii"))
         {
             return new NativeAsciiReader(
                 new InputStreamReader(_url.openStream(),"ASCII"));
         }
         else
         {
             return new BufferedReader(
                 new InputStreamReader(_url.openStream(),_inputEncoding));
         }
     }
 
     /**
      * return the URL for the current template.
      */
     public URL getURL()
     {
         return _url;
     }
 
     /**
      * Return a name for this template. For example, if the template reads
      * from a file you might want to mention which it is--will be used to
      * produce error messages describing which template had a problem.
      */
     public String toString()
     {
         return "URLTemplate:" + _url;
     }
     
     public void parse() throws IOException, TemplateException
     {
         super.parse();
         if ((_outputEncoding != null)
             && (getParam(WMConstants.TEMPLATE_OUTPUT_ENCODING) == null)) 
         {
             _log.debug("Setting output encoding to "+_outputEncoding);
             setParam(WMConstants.TEMPLATE_OUTPUT_ENCODING,_outputEncoding);
         }
         if ((_outputLocale != null)
             && (getParam(WMConstants.TEMPLATE_LOCALE) == null)) 
         {
             _log.debug("Setting output locale to "+_outputLocale);
             setParam(WMConstants.TEMPLATE_LOCALE,_outputLocale);
         }
     }
 }
