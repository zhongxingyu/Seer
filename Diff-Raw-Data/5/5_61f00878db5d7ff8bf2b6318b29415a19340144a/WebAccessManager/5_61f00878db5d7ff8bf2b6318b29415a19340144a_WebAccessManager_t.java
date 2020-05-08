 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.cismet.security;
 
 import java.awt.Component;
 
 import java.io.InputStream;
 import java.io.Reader;
 import java.io.StringReader;
 
 import java.net.URL;
 
 import java.util.ArrayList;
 import java.util.HashMap;
import java.util.Map.Entry;
 import java.util.Properties;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import de.cismet.netutil.Proxy;
 
 import de.cismet.security.AccessHandler.ACCESS_HANDLER_TYPES;
 
 import de.cismet.security.exceptions.AccessMethodIsNotSupportedException;
 import de.cismet.security.exceptions.MissingArgumentException;
 import de.cismet.security.exceptions.NoHandlerForURLException;
 import de.cismet.security.exceptions.RequestFailedException;
 
 import de.cismet.security.handler.DefaultHTTPAccessHandler;
 import de.cismet.security.handler.HTTPBasedAccessHandler;
 import de.cismet.security.handler.WSSAccessHandler;
 
 /**
  * DOCUMENT ME!
  *
  * @author   spuhl
  * @version  $Revision$, $Date$
  */
 //ToDO default Handler (HTTP)
 //ToDo Proxy
 //ToDO Http Access
 //ToDo Multithreading
 //Problematik wenn unter der url mehrere services z.B. wms wfs wss sind
 //Todo url leichen weil statisch --> wenn versucht wird eine schon vorhandene URL hinzuzufügen --> wir im Moment  überschrieben
 public class WebAccessManager implements AccessHandler {
 
     //~ Static fields/initializers ---------------------------------------------
 
    public static final String HEADER_CONTENTTYPE_KEY = "Content-Type";
    public static final String HEADER_CONTENTTYPE_VALUE_POST = "application/x-www-form-urlencoded";

     private static WebAccessManager instance = null;
     private static final ReentrantReadWriteLock reLock = new ReentrantReadWriteLock();
     private static final Lock readLock = reLock.readLock();
     private static final Lock writeLock = reLock.writeLock();
 
     //~ Instance fields --------------------------------------------------------
 
     private final HashMap<URL, AccessHandler> handlerMapping = new HashMap<URL, AccessHandler>();
     private final HashMap<ACCESS_HANDLER_TYPES, AccessHandler> allHandlers =
         new HashMap<ACCESS_HANDLER_TYPES, AccessHandler>();
     private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
     private ArrayList<ACCESS_HANDLER_TYPES> supportedHandlerTypes = new ArrayList<ACCESS_HANDLER_TYPES>();
     private AccessHandler defaultHandler;
     private Properties serverAliasProps = new Properties();
     private Component topLevelComponent = null;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new WebAccessManager object.
      */
     private WebAccessManager() {
         initHandlers();
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * Sets the Proxy-Object of the HTTP- and the WSS-AccessHandler. Does nothing if no HTTP-AccessHandler and no
      * WSS-AccessHandler exists.
      *
      * @param  proxy  DOCUMENT ME!
      */
     public void setHttpProxy(final Proxy proxy) {
         // HTTP-Handler holen
         final AccessHandler httpHandler = allHandlers.get(AccessHandler.ACCESS_HANDLER_TYPES.HTTP);
         // pruefen ob vom Typ HTTPBasedAccessHandler
         if ((httpHandler != null) && (httpHandler instanceof HTTPBasedAccessHandler)) {
             // proxy setzen
             if (log.isDebugEnabled()) {
                 log.debug("set Proxy in httpHandler"); // NOI18N
             }
             ((HTTPBasedAccessHandler)httpHandler).setProxy(proxy);
         }
 
         // WSS-Handler holen
         final AccessHandler wssHandler = allHandlers.get(AccessHandler.ACCESS_HANDLER_TYPES.WSS);
         // pruefen ob vom Typ WSSAccessHandler
         if ((wssHandler != null) && (wssHandler instanceof WSSAccessHandler)) {
             // proxy setzen
             if (log.isDebugEnabled()) {
                 log.debug("set Proxy in wssHandler"); // NOI18N
             }
             ((WSSAccessHandler)wssHandler).setProxy(proxy);
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     public void resetWSSCredentials() {
         // WSS-Handler holen
         final AccessHandler wssHandler = allHandlers.get(AccessHandler.ACCESS_HANDLER_TYPES.WSS);
         // pruefen ob vom Typ WSSAccessHandler
         if ((wssHandler != null) && (wssHandler instanceof WSSAccessHandler)) {
             // proxy setzen
             if (log.isDebugEnabled()) {
                 log.debug("reset WSS credentials"); // NOI18N
             }
             ((WSSAccessHandler)wssHandler).resetCredentials();
         }
     }
 
     /**
      * Returns the Proxy-Object of the HTTP-AccessHandler or (if it not exists) the Proxy-Object of the
      * WSS-AccessHandler or null if no proxy exists.
      *
      * @return  HTTP-AccessHandler or null
      */
     public Proxy getHttpProxy() {
         // HTTP-Handler holen
         final AccessHandler httpHandler = allHandlers.get(AccessHandler.ACCESS_HANDLER_TYPES.HTTP);
         // pruefen ob vom Typ HTTPBasedAccessHandler
         if ((httpHandler != null) && (httpHandler instanceof HTTPBasedAccessHandler)) {
             // proxy zurueckgeben
             return ((HTTPBasedAccessHandler)httpHandler).getProxy();
         } else {
             // WSS-Handler holen
             final AccessHandler wssHandler = allHandlers.get(AccessHandler.ACCESS_HANDLER_TYPES.WSS);
             // pruefen ob vom Typ WSSAccessHandler
             if ((wssHandler != null) && (wssHandler instanceof WSSAccessHandler)) {
                 return ((WSSAccessHandler)wssHandler).getProxy();
             } else {
                 return null;
             }
         }
     }
     /**
      * ToDO make configurable.
      */
     private void initHandlers() {
         if (log.isDebugEnabled()) {
             log.debug("initHandlers"); // NOI18N
         }
         final WSSAccessHandler wssHandler = new WSSAccessHandler();
         final DefaultHTTPAccessHandler httpHandler = new DefaultHTTPAccessHandler();
         // SOAPAccessHandler soapAccessHandler = new SOAPAccessHandler();
         // SanyAccessHandler sanyAccessHandler = new SanyAccessHandler();
         defaultHandler = httpHandler;
         allHandlers.put(AccessHandler.ACCESS_HANDLER_TYPES.WSS, wssHandler);
         allHandlers.put(AccessHandler.ACCESS_HANDLER_TYPES.HTTP, httpHandler);
         // allHandlers.put(AccessHandler.ACCESS_HANDLER_TYPES.SOAP, soapAccessHandler);
         // allHandlers.put(AccessHandler.ACCESS_HANDLER_TYPES.SANY, sanyAccessHandler);
         supportedHandlerTypes.add(ACCESS_HANDLER_TYPES.WSS);
         supportedHandlerTypes.add(ACCESS_HANDLER_TYPES.HTTP);
         supportedHandlerTypes.add(ACCESS_HANDLER_TYPES.SOAP);
         supportedHandlerTypes.add(ACCESS_HANDLER_TYPES.SANY);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public AccessHandler getDefaultHandler() {
         return defaultHandler;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  defaultHandler  DOCUMENT ME!
      */
     public void setDefaultHandler(final AccessHandler defaultHandler) {
         this.defaultHandler = defaultHandler;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static WebAccessManager getInstance() {
         if (instance != null) {
             return instance;
         } else {
             createInstance();
             return instance;
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     private static synchronized void createInstance() {
         if (instance == null) {
             instance = new WebAccessManager();
         }
     }
     /**
      * overwrites at the moment.
      *
      * @param   url          DOCUMENT ME!
      * @param   handlerType  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public synchronized boolean registerAccessHandler(final URL url, final ACCESS_HANDLER_TYPES handlerType) {
         writeLock.lock();
         try {
             if ((handlerMapping.get(url) == null) && (allHandlers.get(handlerType) != null)) {
                 handlerMapping.put(url, allHandlers.get(handlerType));
                 return true;
             } else {
                 // todo einfacher wäre überschreiben ohne zu deregistrieren --> ist synchronisiert
                 if (deregisterAccessHandler(url)) {
                     if (allHandlers.get(handlerType) != null) {
                         handlerMapping.put(url, allHandlers.get(handlerType));
                         return true;
                     } else {
                         return false;
                     }
                 } else {
                     return false;
                 }
             }
         } finally {
             writeLock.unlock();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   url  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public synchronized boolean deregisterAccessHandler(final URL url) {
         writeLock.lock();
         try {
             if (handlerMapping.containsKey(url)) {
                 handlerMapping.remove(url);
                 return true;
             } else {
                 return false;
             }
         } finally {
             writeLock.unlock();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   url  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isHandlerForURLRegistered(final URL url) {
         readLock.lock();
         try {
             return handlerMapping.get(url) != null;
         } finally {
             readLock.unlock();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   url  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public AccessHandler getHandlerForURL(final URL url) {
         readLock.lock();
         try {
             final AccessHandler handler = handlerMapping.get(url);
             if (handler == null) {
                 if (log.isDebugEnabled()) {
                     log.debug("no handler found  for url --> try to extract base");         // NOI18N
                 }
                 final String urlString = url.toString();
                 URL baseURL = null;
                 if (urlString.indexOf('?') != -1) {
                     if (log.isDebugEnabled()) {
                         log.debug("there are parameter appended to the url try to remove"); // NOI18N
                     }
                     try {
                         baseURL = new URL(urlString.substring(0, urlString.indexOf('?')));
                         return handlerMapping.get(baseURL);
                     } catch (Exception ex) {
                     }
                 }
             }
             return handler;
         } finally {
             readLock.unlock();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   url  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public ACCESS_HANDLER_TYPES getTypeOfHandler(final URL url) {
         final AccessHandler accessHandler = handlerMapping.get(url);
         if (accessHandler != null) {
             return accessHandler.getHandlerType();
         } else {
             return null;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   url  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  MissingArgumentException             DOCUMENT ME!
      * @throws  AccessMethodIsNotSupportedException  DOCUMENT ME!
      * @throws  RequestFailedException               DOCUMENT ME!
      * @throws  NoHandlerForURLException             DOCUMENT ME!
      * @throws  Exception                            DOCUMENT ME!
      */
     public InputStream doRequest(final URL url) throws MissingArgumentException,
         AccessMethodIsNotSupportedException,
         RequestFailedException,
         NoHandlerForURLException,
         Exception {
         if (log.isDebugEnabled()) {
             log.debug("URL: " + url + "... trying to retrieve parameters automatically by HTTP_GET");       // NOI18N
         }
         URL serviceURL;
         String requestParameter;
         try {
             final String urlString = url.toString();
             if (urlString.indexOf('?') != -1) {
                 serviceURL = new URL(urlString.substring(0, urlString.indexOf('?')));                       // NOI18N
                 if (log.isDebugEnabled()) {
                     log.debug("service URL: " + serviceURL);                                                // NOI18N
                 }
                 if ((urlString.indexOf('?') + 1) < urlString.length()) {                                    // NOI18N
                     requestParameter = urlString.substring(urlString.indexOf('?') + 1, urlString.length()); // NOI18N
                     if (requestParameter.toLowerCase().contains("service=wss")) {                           // NOI18N
                         // TODO muss auch wfs fähig sein
                         if (log.isDebugEnabled()) {
                             log.debug("query default WMS");                       // NOI18N
                         }
                         requestParameter = "REQUEST=GetCapabilities&service=WMS"; // NOI18N
                     }
                 } else {
                     requestParameter = "";                                        // NOI18N
                 }
 
                 if (log.isDebugEnabled()) {
                     log.debug("requestParameter: " + requestParameter);               // NOI18N
                 }
             } else {
                 log.warn("Not able to parse requestparameter (no ?) trying without"); // NOI18N
                 serviceURL = url;
                 requestParameter = "";                                                // NOI18N
             }
         } catch (Exception ex) {
             // final String errorMessage = "Exception während dem bestimmen der Request Parameter";
             final String errorMessage = "Request parameters coud not be parsed: " + ex.getMessage(); // NOI18N
             log.error(errorMessage);
             throw new RequestFailedException(errorMessage, ex);
         }
         return doRequest(serviceURL, new StringReader(requestParameter), AccessHandler.ACCESS_METHODS.GET_REQUEST);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   url               DOCUMENT ME!
      * @param   requestParameter  DOCUMENT ME!
      * @param   accessMethod      DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  MissingArgumentException             DOCUMENT ME!
      * @throws  AccessMethodIsNotSupportedException  DOCUMENT ME!
      * @throws  RequestFailedException               DOCUMENT ME!
      * @throws  NoHandlerForURLException             DOCUMENT ME!
      * @throws  Exception                            DOCUMENT ME!
      */
     public InputStream doRequest(final URL url,
             final String requestParameter,
             final AccessHandler.ACCESS_METHODS accessMethod) throws MissingArgumentException,
         AccessMethodIsNotSupportedException,
         RequestFailedException,
         NoHandlerForURLException,
         Exception {
         if (log.isDebugEnabled()) {
             log.debug("Requestparameter: " + requestParameter); // NOI18N
         }
         return doRequest(url, new StringReader(requestParameter), accessMethod);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   url               DOCUMENT ME!
      * @param   requestParameter  DOCUMENT ME!
      * @param   accessMethod      DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  MissingArgumentException             DOCUMENT ME!
      * @throws  AccessMethodIsNotSupportedException  DOCUMENT ME!
      * @throws  RequestFailedException               DOCUMENT ME!
      * @throws  NoHandlerForURLException             DOCUMENT ME!
      * @throws  Exception                            DOCUMENT ME!
      */
     public InputStream doRequest(final URL url,
             final Reader requestParameter,
             final AccessHandler.ACCESS_METHODS accessMethod) throws MissingArgumentException,
         AccessMethodIsNotSupportedException,
         RequestFailedException,
         NoHandlerForURLException,
         Exception {
         return doRequest(url, requestParameter, accessMethod, null);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   url               DOCUMENT ME!
      * @param   requestParameter  DOCUMENT ME!
      * @param   accessMethod      DOCUMENT ME!
      * @param   options           DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  MissingArgumentException             DOCUMENT ME!
      * @throws  AccessMethodIsNotSupportedException  DOCUMENT ME!
      * @throws  RequestFailedException               DOCUMENT ME!
      * @throws  NoHandlerForURLException             DOCUMENT ME!
      * @throws  Exception                            DOCUMENT ME!
      */
     @Override
     public InputStream doRequest(final URL url,
             final Reader requestParameter,
             final AccessHandler.ACCESS_METHODS accessMethod,
             final HashMap<String, String> options) throws MissingArgumentException,
         AccessMethodIsNotSupportedException,
         RequestFailedException,
         NoHandlerForURLException,
         Exception {
         readLock.lock();
         final AccessHandler handler;
         if (url == null) {
             // throw new MissingArgumentException("Es wurde keine URL gesetzt für das Request gesetzt");
             throw new MissingArgumentException("URL parameter is empty");                 // NOI18N
         } else if (accessMethod == null) {
             log.warn("No Access Methode available calling Defaultmethod of the Handler"); // NOI18N
         }
 
         if (log.isDebugEnabled()) {
             log.debug("Request URL: " + url.toString());                // NOI18N
         }
         try {
             handler = handlerMapping.get(url);
             if (handler != null) {
                 if (log.isDebugEnabled()) {
                     log.debug("Handler for URL " + url + " available"); // NOI18N
                 }
                 if (handler.isAccessMethodSupported(accessMethod)) {
                     if (log.isDebugEnabled()) {
                         log.debug("Handler supports access method");    // NOI18N
                     }
                     return handler.doRequest(url, requestParameter, accessMethod, options);
 //                    try {
 //                        return handler.doRequest(url, requestParameter, accessMethod, options);
 //                    } catch (Exception ex) {
 //                        //throw new RequestFailedException("Das Request konnte nicht ausgeführt werden", ex);
 //                        throw new RequestFailedException("The request cound not be performed: " + ex.getMessage(), ex);
 //                    }
                 } else {
                     // throw new AccessMethodIsNotSupportedException("Die Accesss Methode: " + accessMethod + " ist vom
                     // handler: " + handler.getClass() + " nicht unterstützt");
                     throw new AccessMethodIsNotSupportedException("The access method '" + accessMethod
                                 + "' is not supported by handler '" // NOI18N
                                 + handler.getClass() + "'");        // NOI18N
                 }
             } else {
                 // TODO Default handler
                 // throw new NoHandlerForURLException("Es ist kein Handler für die URL vorhanden");
                 if (log.isInfoEnabled()) {
                     log.info("No URL Handler available --> using DefaultHandler"); // NOI18N
                 }
                 if (defaultHandler != null) {
                     return defaultHandler.doRequest(url, requestParameter, accessMethod, options);
 //                    try {
 //                        return defaultHandler.doRequest(url, requestParameter, accessMethod, options);
 //                    } catch (Exception ex) {
 //                        //throw new RequestFailedException("Das Request konnte nicht ausgeführt werden :", ex);
 //                        throw new RequestFailedException("The request cound not be performed: " + ex.getMessage(), ex);
 //                    }
                 } else {
                     // throw new NoHandlerForURLException("Es ist kein Defaulthandler vorhanden");
                     throw new NoHandlerForURLException("No default handler available"); // NOI18N
                 }
             }
         } catch (Exception ex) {
             log.error("Error while doRequest: ", ex);                                   // NOI18N
 
             throw ex;
             // throw new RequestFailedException("Das Request konnte nicht ausgeführt werden", ex);
             // throw new RequestFailedException("The request cound not be performed: " + ex.getMessage(), ex);
         } finally {
             if (log.isDebugEnabled()) {
                 log.debug("releasing lock"); // NOI18N
             }
             readLock.unlock();
         }
     }
     /**
      * TODO keine Funktionalität --> nur dummies zur kompatibilität.
      *
      * @param  key    DOCUMENT ME!
      * @param  value  DOCUMENT ME!
      */
     public void addServerAliasProperty(final String key, final String value) {
         serverAliasProps.put(key, value);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   key  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getServerAliasProperty(final String key) {
         return serverAliasProps.getProperty(key);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Component getTopLevelComponent() {
         return topLevelComponent;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  topLevelComponent  DOCUMENT ME!
      */
     public void setTopLevelComponent(final Component topLevelComponent) {
         this.topLevelComponent = topLevelComponent;
     }
     /**
      * todo.
      *
      * @return  DOCUMENT ME!
      *
      * @throws  UnsupportedOperationException  DOCUMENT ME!
      */
     @Override
     public ACCESS_HANDLER_TYPES getHandlerType() {
         throw new UnsupportedOperationException("Not supported yet."); // NOI18N
     }
     /**
      * todo.
      *
      * @param   method  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  UnsupportedOperationException  DOCUMENT ME!
      */
     @Override
     public boolean isAccessMethodSupported(final ACCESS_METHODS method) {
         throw new UnsupportedOperationException("Not supported yet."); // NOI18N
     }
 }
