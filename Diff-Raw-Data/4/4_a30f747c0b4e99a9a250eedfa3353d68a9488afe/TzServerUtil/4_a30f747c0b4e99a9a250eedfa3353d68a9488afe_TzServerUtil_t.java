 /* **********************************************************************
     Copyright 2006 Rensselaer Polytechnic Institute. All worldwide rights reserved.
 
     Redistribution and use of this distribution in source and binary forms,
     with or without modification, are permitted provided that:
        The above copyright notice and this permission notice appear in all
         copies and supporting documentation;
 
         The name, identifiers, and trademarks of Rensselaer Polytechnic
         Institute are not used in advertising or publicity without the
         express prior written permission of Rensselaer Polytechnic Institute;
 
     DISCLAIMER: The software is distributed" AS IS" without any express or
     implied warranty, including but not limited to, any implied warranties
     of merchantability or fitness for a particular purpose or any warrant)'
     of non-infringement of any current or pending patent rights. The authors
     of the software make no representations about the suitability of this
     software for any particular purpose. The entire risk as to the quality
     and performance of the software is with the user. Should the software
     prove defective, the user assumes the cost of all necessary servicing,
     repair or correction. In particular, neither Rensselaer Polytechnic
     Institute, nor the authors of the software are liable for any indirect,
     special, consequential, or incidental damages related to the software,
     to the maximum extent the law permits.
 */
 
 package org.bedework.timezones.server;
 
 import net.sf.ehcache.Cache;
 import net.sf.ehcache.CacheManager;
 import net.sf.ehcache.Element;
 
 import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.params.HttpMethodParams;
 import org.apache.log4j.Logger;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Properties;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 
 /** Some utility methods.
  *
  *   @author Mike Douglass
  */
 public class TzServerUtil {
   /* ======================= property names ======================= */
 
   /** Property defining location of the zipped data */
   public static final String pnameTzdataURL = "tzsvc.tzdata.url";
 
   /** Property defining integer seconds refetch of data
    */
   public static final String pnameRefetchInterval = "tzsvc.refetch.interval";
 
   /** Property defining a key to allow POST */
   public static final String pnamePostId = "tzsvc.post.id";
 
   /** name of vtimezone cache
    */
   public static final String pnameVtzCache = "tzsvc.vtimezones.cache.name";
 
   /** name of zoneinfo cache
    */
   public static final String pnameZoneInfoCache = "tzsvc.zoneinfo.cache.name";
 
   /* ======================= Error codes ======================= */
 
   /** Unable to retrieve the data */
   public static final String errorNodata = "org.tserver.no.data";
 
   private static CacheManager manager = new CacheManager();
 
   private static Cache vtzCache;
 
   private static List<String> nameList;
 
   /* ======================= Stats ======================= */
 
   static long gets;
   static long cacheHits;
   static long reads;
   static long nameLists;
   static long aliasReads;
 
   /** Set up a Properties from the resources
    *
    * @param servlet
    * @param config
    * @return Properties
    * @throws ServletException
    */
   static Properties getResources(HttpServlet servlet,
                                  ServletConfig config) throws ServletException {
     String resname = config.getInitParameter("application");
 
     Properties props = new Properties();
 
     if (resname != null) {
       InputStream is;
 
       ClassLoader classLoader =
           Thread.currentThread().getContextClassLoader();
       if (classLoader == null) {
         classLoader = servlet.getClass().getClassLoader();
       }
       is = classLoader.getResourceAsStream(resname + ".properties");
 
       try {
         props.load(is);
       } catch (IOException ie) {
         throw new ServletException(ie);
       }
     }
 
     cacheInit(props);
 
     return props;
   }
 
   /** Retrieve the data and store in a temp file. Return the file object.
    *
    * @param props
    * @return File
    * @throws ServletException
    */
   static File getdata(Properties props) throws ServletException {
     try {
       String dataUrl = props.getProperty(pnameTzdataURL);
       if (dataUrl == null) {
         throw new ServletException("No data url defined");
       }
 
       /* Fetch the data */
       HttpClient client = new HttpClient();
 
       HttpMethod get = new GetMethod(dataUrl);
 
       client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                                       new DefaultHttpMethodRetryHandler());
 
       client.executeMethod(get);
 
       InputStream is = get.getResponseBodyAsStream();
 
       File f = File.createTempFile("bwtzserver", "zip");
 
       FileOutputStream fos = new  FileOutputStream(f);
 
       byte[] buff = new byte[4096];
 
       for (;;) {
         int num = is.read(buff);
 
         if (num < 0) {
           break;
         }
 
         if (num > 0) {
           fos.write(buff, 0, num);
         }
       }
 
       fos.close();
       is.close();
 
       get.releaseConnection();
 
       logIt("Data (re)fetched");
 
       return f;
     } catch (Throwable t) {
       throw new ServletException(t);
     }
   }
 
   static List<String> getNames(ZipFile zf) throws ServletException {
     nameLists++;
 
     if (nameList != null) {
       return nameList;
     }
 
     try {
       List<String> nl = new ArrayList<String>();
 
       Enumeration<? extends ZipEntry> zes = zf.entries();
 
       while (zes.hasMoreElements()) {
         ZipEntry ze = zes.nextElement();
 
         if (!ze.isDirectory()) {
           String n = ze.getName();
 
          if (n.startsWith("zoneinfo/") && n.endsWith(".ics")) {
            nl.add(n.substring(9, n.length() - 4));
           }
         }
       }
 
       nameList = nl;
 
       return nl;
     } catch (Throwable t) {
       throw new ServletException(t);
     }
   }
 
   static String getTz(String name, ZipFile zf) throws ServletException {
     String s = getCachedVtz(name);
     if (s != null) {
       cacheHits++;
       return s;
     }
 
     try {
       reads++;
       ZipEntry ze = zf.getEntry("zoneinfo/" + name + ".ics");
 
       if (ze == null) {
         return null;
       }
 
       s = entryToString(zf, ze);
 
       putCachedVtz(name, s);
 
       return s;
     } catch (Throwable t) {
       throw new ServletException(t);
     }
   }
 
   static String getAliases(ZipFile zf) throws ServletException {
     try {
       aliasReads++;
       ZipEntry ze = zf.getEntry("aliases.txt");
 
       if (ze == null) {
         return null;
       }
 
       return entryToString(zf, ze);
     } catch (Throwable t) {
       throw new ServletException(t);
     }
   }
 
   private static String entryToString(ZipFile zf,
                                       ZipEntry ze) throws Throwable {
     InputStreamReader is = new InputStreamReader(zf.getInputStream(ze),
                                                  "UTF-8");
 
     StringWriter sw = new StringWriter();
 
     char[] buff = new char[4096];
 
     for (;;) {
       int num = is.read(buff);
 
       if (num < 0) {
         break;
       }
 
       if (num > 0) {
         sw.write(buff, 0, num);
       }
     }
 
     is.close();
 
     return sw.toString();
   }
 
   /* ====================================================================
    *                   Caching
    * ==================================================================== */
 
   static void cacheInit(Properties props) throws ServletException {
     vtzCache = manager.getCache(props.getProperty(pnameVtzCache));
   }
 
   static void cacheRefresh() throws ServletException {
     cacheRefresh(vtzCache);
   }
 
   static String getCachedVtz(String name) throws ServletException {
     Element el = vtzCache.get(name);
 
     if (el == null) {
       return null;
     }
 
     return (String)el.getValue();
   }
 
   static void putCachedVtz(String name, String vtz) throws ServletException {
     Element el = new Element(name, vtz);
 
     vtzCache.put(el);
   }
 
   /* ====================================================================
    *                   Private methods
    * ==================================================================== */
 
   private static void cacheRefresh(Cache cache) throws ServletException {
     if (cache != null) {
       cache.flush();
     }
   }
 
   static Long longProp(Properties props, String name) throws Throwable {
     String propVal = props.getProperty(name);
     if (propVal == null) {
       return null;
     }
 
     return Long.valueOf(propVal);
   }
 
   static boolean boolProp(Properties props, String name) throws Throwable {
     String propVal = props.getProperty(name);
     if (propVal == null) {
       return false;
     }
 
     return Boolean.valueOf(propVal);
   }
 
   /**
    * @return Logger
    */
   static Logger getLogger() {
     return Logger.getLogger(TzServerUtil.class);
   }
 
   /** Debug
    *
    * @param msg
    */
   static void debugMsg(String msg) {
     getLogger().debug(msg);
   }
 
   /** Info messages
    *
    * @param msg
    */
   static void logIt(String msg) {
     getLogger().info(msg);
   }
 
   static void error(String msg) {
     getLogger().error(msg);
   }
 
   static void error(Throwable t) {
     getLogger().error(TzServerUtil.class, t);
   }
 }
