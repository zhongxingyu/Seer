 
 /*
  * Copyright (c) 1998, 1999 Semiotek Inc. All Rights Reserved.
  *
  * This software is the confidential intellectual property of
  * of Semiotek Inc.; it is copyrighted and licensed, not sold.
  * You may use it under the terms of the GNU General Public License,
  * version 2, as published by the Free Software Foundation. If you 
  * do not want to use the GPL, you may still use the software after
  * purchasing a proprietary developers license from Semiotek Inc.
  *
  * This software is provided "as is", with NO WARRANTY, not even the 
  * implied warranties of fitness to purpose, or merchantability. You
  * assume all risks and liabilities associated with its use.
  *
  * See the attached License.html file for details, or contact us
  * by e-mail at info@semiotek.com to get a copy.
  */
 
 
 package org.webmacro.resource;
 
 import java.lang.*;
 import java.net.*;
 import java.io.*;
 import org.webmacro.util.*;
 import org.webmacro.*;
 import org.webmacro.broker.*;
 
 /**
   * This is the canonical provider for mapping URLs to Handlers. The
   * reactor will request that the "handler" provider return a Handler
   * object when supplied with a URL.
   * <p>
   * You could implement your own version of this class to return 
   * handlers based on whatever criteria you wanted.
   */
 
 final public class UrlProvider implements ResourceProvider
 {
 
    // STATIC DATA 
 
    /**
      * Constant that contains the Log and ResourceProvider Type 
      * served by this class.
      */
    final public static String TYPE = "url";
 
    final private static Log _log = new Log("url", "URL Provider");
 
 
    // RESOURCE PROVIDER API
 
    final static private String[] _types = { TYPE };
 
    /**
      * Two thread concurrency--some concurrency is useful
      */
    final public int resourceThreads() { return 2; }
 
    /**
      * We serve up "url" type resources
      */
    final public String[] getTypes() { return _types; }
 
    /**
      * Cache fetched URLs for up to 10 minutes.
      */
    final public int resourceExpireTime() { return 1000 * 60 * 10; }
 
    /**
      * Request a handler, the supplied name is ScriptName
      */
    final public void resourceRequest(RequestResourceEvent evt) 
       throws NotFoundException
    {
 
       try {
         URL u = new URL(evt.getName());
          Reader in = new InputStreamReader(u.openStream());
 
          char buf[] = new char[512];
          StringWriter sw = new StringWriter();
          int num;
          while ( (num = in.read(buf)) != -1 ) {
             sw.write(buf, 0, num);
          }
          evt.set(sw.toString());
          in.close();
       } catch (Exception e) {
          _log.exception(e);
          throw new NotFoundException(
             "Reactor: Unable to load URL " + evt.getName() + ":" + e);
       }
    }
 
    /**
      * Stop the handler
      */
    final public boolean resourceSave(ResourceEvent evt) { 
       return false; 
    }
 
    /**
      * Unimplemented / does nothing
      */
    final public void destroy() { }
 
 
    /**
      * Resource broker will want to  init me
      */
    public void init(ResourceBroker broker)
    {
       // do nothing
    }
 
    /**
      * Unimplemented / does nothing
      */
    final public void resourceCreate(CreateResourceEvent evt) { }
 
    /**
      * Unimplemented / does nothing
      */
    final public boolean resourceDelete(ResourceEvent evt) { return false; }
 
 
 }
 
 
