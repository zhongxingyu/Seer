 package net.java.dev.weblets;
 
 import net.java.dev.weblets.util.IWebletUtils;
 import net.java.dev.weblets.util.ServiceLoader;
 
 import java.io.InputStream;
 
 /**
  * @author Werner Punz Date: 30.12.2007 Time: 18:00:04
  */
 public class WebletUtils {
     /**
      * constractual method
      *
      * @param weblet   the weblet name
      * @param pathInfo the path info to the resource
      * @return the resource path for the resource
      */
     public static String getResource(String weblet, String pathInfo) {
         return instance.getResource(weblet, pathInfo);
     }
 
     /**
      * constractual method
      *
      * @param weblet   the weblet name
      * @param pathInfo the path info to the resource
      * @return the resource path for the resource
      */
     public static String getURL(String weblet, String pathInfo) {
         return instance.getURL(weblet, pathInfo);
     }
 
     /**
      * constractual method
      *
      * @param weblet   the weblet name
      * @param pathInfo the path info to the resource
      * @return the resource path for the resource
      */
     public static String getResource(Object requestSingletonHolder, String weblet, String pathInfo, boolean suppressDuplicates) {
         return instance.getResource(requestSingletonHolder, weblet, pathInfo, suppressDuplicates);
     }
 
     /**
      * constractual method
      *
      * @param weblet   the weblet name
      * @param pathInfo the path info to the resource
      * @return the resource path for the resource
      */
     public static String getURL(Object requestSingletonHolder, String weblet, String pathInfo, boolean suppressDuplicates) {
         return instance.getURL(requestSingletonHolder, weblet, pathInfo, suppressDuplicates);
     }
 
     public static boolean isResourceLoaded(Object requestSingletonHolder, String weblet, String pathInfo) {
         return instance.isResourceLoaded(requestSingletonHolder, weblet, pathInfo);
     }
 
     /**
      * Reporting case for weblets
      * this methid must be able to deal with requests being null
      * (Special testcases have to be applied to check this!
      *
      * @param weblet   the weblet name
      * @param pathInfo the pathinfo
      * @param mimeType the mimetype
      * @return
      */
     public static InputStream getResourceAsStream(String weblet, String pathInfo, String mimeType) {
         return instance.getResourceStream(weblet, pathInfo, mimeType);
     }
 

     /**
      * kind of a weird construct but definitely faster than doing all the calls over introspection, the internal contract is defined by the IJSFWebletsUtils
      * interface
      */
     static IWebletUtils instance = getInstance();
 
     static IWebletUtils getInstance() throws WebletException {
         synchronized (FacesWebletUtils.class) {
             if (instance == null) {
                 Class instantiation = ServiceLoader.loadService(WebletUtils.class.getName());
                 try {
                     instance = (IWebletUtils) instantiation.newInstance();
                 } catch (InstantiationException e) {
                     throw new WebletException("Error instantiating WebletsUtil", e);
                 } catch (IllegalAccessException e) {
                     throw new WebletException("Error instantiating WebletsUtil", e);
                 }
             }
         }
         return instance;
     }
 }
