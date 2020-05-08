 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: LogConfigReader.java,v 1.5 2006-08-28 18:50:41 veiming Exp $
  *
  * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  */
 
 
 
 package com.sun.identity.log.s1is;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.security.AccessController;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import com.sun.identity.shared.debug.Debug;
 import com.sun.identity.shared.datastruct.CollectionHelper;
 import com.iplanet.am.util.SystemProperties;
 import com.iplanet.sso.SSOException;
 import com.iplanet.sso.SSOToken;
 import com.iplanet.sso.SSOTokenManager;
 import com.sun.identity.authentication.internal.AuthPrincipal;
 import com.sun.identity.log.LogConstants;
 import com.sun.identity.log.LogManager;
 import com.sun.identity.log.LogManagerUtil;
 import com.sun.identity.security.AdminDNAction;
 import com.sun.identity.security.AdminPasswordAction;
 import com.sun.identity.shared.Constants;
 import com.sun.identity.sm.SMSException;
 import com.sun.identity.sm.ServiceListener;
 import com.sun.identity.sm.ServiceSchema;
 import com.sun.identity.sm.ServiceSchemaManager;
 
 /**
  * <tt>LogConfigReader</TT> is used to load the configuration from the
  * Directory Server and store the configuration as Properties of
  * <tt>LogManager</tt>. <p>
  * <tt>LogConfigReader</TT> is very Speicific to DSAME. <p>
  * java.util.logging.config.class System property should point to this class,
  * since LogManager uses this property instantiates this class to load
  * the configuration.
  */
 public class LogConfigReader implements ServiceListener{
     
     private static Debug debug = Debug.getInstance("amLog");
     private static ServiceSchema smsLogSchema           = null;
     private static ServiceSchema smsPlatformSchema      = null;
     private static ServiceSchema smsNamingSchema        = null;
     private static Map logAttributes                    = null;
     private static Map platformAttributes               = null;
     private static Map namingAttributes                 = null;
     
     private String localProtocol = null;
     private String localHost = null;
     private String localPort = null;
     /**
      * Local Log service identifier
      */
     public static String localLogServiceID = null;
 
     private static boolean isRegisteredForDSEvents = false;
     private LogManager manager;
     
     /**
      * The constructor loads the configuration from the DS using
      * DSAME SDK. Constructs a String as "key=value CRLF" for each
      * AttributeSchema in the amLogging.xml. In case the AttributeSchema
      * has multiple values or a List, it gets converted to a "," seperated
      * String.
      * <p> Example1: iplanet-am-logging-backend=FILE \r\n
      * <p> Example2: iplanet-am-logging-logfields=TIME, DOMAIN, IPADDR, 
      * HOSTNAME, DATA, LEVEL, LOGINID \r\n
      * <p> The input stream hence constructed is converted into a
      * ByteArrayInputStream and is loaded into LogManager.
      * @throws IOException
      */
     public LogConfigReader() throws IOException {
         localProtocol = SystemProperties.get("com.iplanet.am.server.protocol");
         localHost = SystemProperties.get("com.iplanet.am.server.host");
         localPort = SystemProperties.get(Constants.AM_SERVER_PORT);
         localLogServiceID = localProtocol + "://" + localHost + ":" + localPort;
 
         SSOToken ssoToken;
         try {
             ssoToken = getSSOToken();
         } catch (SSOException ssoe) {
             debug.error("LogConfigReader: Could not get proper SSOToken", ssoe);
             return;
         }
         if (debug.messageEnabled()) {
             debug.message("LogConfigReader: ssoToken obtained" + ssoToken);
         }
         try {
             getDefaultAttributes(ssoToken);
         } catch (SMSException smse) {
             debug.warning("LogConfigReader: Could not " +
                 "get defaultAttributes", smse);
             return;
         } catch (SSOException ssoe) {
             debug.error("LogConfigReader: Could not " +
                 "get defaultAttributes", ssoe);
             return;
         }
         String configString = constructInputStream();
         ByteArrayInputStream inputStream = null;
         try {
             inputStream = 
                 new ByteArrayInputStream(configString.getBytes("ISO8859-1"));
         } catch (UnsupportedEncodingException unse) {
             debug.error("LogConfigReader: unsupported Encoding" + unse);
         }
         manager = 
             (com.sun.identity.log.LogManager) LogManagerUtil.getLogManager();
         try {
             manager.readConfiguration(inputStream);
         } catch (IOException ioe) {
             debug.error("LogConfigReader: Can not load configuration" + ioe);
             throw new IOException(ioe.toString());
         }
         setLocalFlag();
     }
     
     /**
      * LogManager needs inputStream in the form of " Key = Value \r\n ". 
      * so to get that we need to get the keys of the default attributs append 
      * a "=", get the value for that key and append a CRLF. This input stream 
      * will then be loaded into the logmanager via properties API.
      */
     private String constructInputStream() {
         StringBuffer sbuffer = new StringBuffer(2000);
         String key = null;
         String value = null;
         Set set;
         Iterator it;
         String tempBuffer;
         // processing logging attributes.
         try {
             logAttributes = smsLogSchema.getAttributeDefaults();
             // File/jdbc
             key = LogConstants.BACKEND;
             value = CollectionHelper.getMapAttr(logAttributes, key);
             if ((value == null) || (value.length() ==0)) {
                 debug.warning("LogConfigReader: Backend string is null");
             } else {
                 sbuffer.append(key).append("=")
                        .append(value).append(LogConstants.CRLF);
             }
         } catch (Exception e) {
             debug.error("LogConfigReader: Could not read Backend ", e);
         }
         // Database Driver
         try {
             key = LogConstants.DB_DRIVER;
             value = CollectionHelper.getMapAttr(logAttributes, key);
             if ((value == null) || (value.length() ==0)) {
                 debug.warning("LogConfigReader: Driver string is null");
             } else {
                 sbuffer.append(key).append("=").append(value)
                        .append(LogConstants.CRLF);
             }
         }  catch (Exception e) {
             debug.error("LogConfigReader: Could not read driver ", e);
         }
         // Database Password
         try {
             key = LogConstants.DB_PASSWORD;
             value = CollectionHelper.getMapAttr(logAttributes, key);
             if ((value == null) || (value.length() ==0)) {
                 debug.warning("LogConfigReader: Password string is null");
             } else {
                 sbuffer.append(key).append("=")
                        .append(value).append(LogConstants.CRLF);
             }
         }   catch (Exception e) {
             debug.error("LogConfigReader: Could not read password ", e);
         }
         // Database USER
         try {
             key = LogConstants.DB_USER;
             value = CollectionHelper.getMapAttr(logAttributes, key);
             if ((value == null) || (value.length() ==0)) {
                 debug.warning("LogConfigReader: DB_USER string is null");
             } else {
                 sbuffer.append(key).append("=")
                        .append(value).append(LogConstants.CRLF);
             }
         }   catch (Exception e) {
             debug.error("LogConfigReader: Could not read db user ", e);
         }
         // all Fields
         try {
             key = LogConstants.ALL_FIELDS;
             tempBuffer = "time, Data, ";
             set = (Set) logAttributes.get(key);
             it = set.iterator();
             tempBuffer += (String) it.next();
             while(it.hasNext()) {
                 tempBuffer += ", " + (String) it.next();
             }
             sbuffer.append(key).append("=")
                    .append(tempBuffer).append(LogConstants.CRLF);
         }   catch (Exception e) {
             debug.error("LogConfigReader: Could not read all field  ", e);
         }
         // Selected Log Fields
         try {
             key = LogConstants.LOG_FIELDS;
             set = (Set) logAttributes.get(key);
             if ((set != null) && (set.size()!=0)) {
                 it = set.iterator();
                 tempBuffer = (String) it.next();
                 while(it.hasNext()) {
                     tempBuffer += ", " + (String) it.next();
                 }
                 sbuffer.append(key).append("=")
                        .append(tempBuffer).append(LogConstants.CRLF);
             }
         }   catch (Exception e) {
             debug.error("LogConfigReader: Could not read log-field ", e);
         }
         // Max file size
         try {
             key = LogConstants.MAX_FILE_SIZE;
             value = CollectionHelper.getMapAttr(logAttributes, key);
             if ((value == null) || (value.length() ==0)) {
                 debug.warning("LogConfigReader: Max File Size string is null");
             } else {
                 sbuffer.append(key).append("=")
                        .append(value).append(LogConstants.CRLF);
             }
         }   catch (Exception e) {
             debug.error("LogConfigReader: Could not read maxfilesize ", e);
         }
         // number of history files
         try {
             key = LogConstants.NUM_HISTORY_FILES;
             value = CollectionHelper.getMapAttr(logAttributes, key);
             if ((value == null) || (value.length() ==0)) {
                 debug.warning("LogConfigReader: NUM_HIST_FILES string is null");
             } else {
                 sbuffer.append(key).append("=")
                        .append(value).append(LogConstants.CRLF);
             }
         }   catch (Exception e) {
             debug.error("LogConfigReader: Could not read numhistfiles ", e);
         }
         // archiver  class
         try {
             key = LogConstants.ARCHIVER;
             value = CollectionHelper.getMapAttr(logAttributes, key);
             if ((value == null) || (value.length() ==0)) {
                 debug.warning("LogConfigReader: Archiver string is null");
             } else {
                 sbuffer.append(key).append("=")
                        .append(value).append(LogConstants.CRLF);
             }
         }   catch (Exception e) {
             debug.error("LogConfigReader: Could not read filehandler ", e);
         }
         // file handler class
         try {
             key = LogConstants.FILE_HANDLER;
             value = CollectionHelper.getMapAttr(logAttributes, key);
             if ((value == null) || (value.length() ==0)) {
                 debug.warning(
                     "LogConfigReader: FileHandler class string is null");
             } else {
                 sbuffer.append(key).append("=")
                        .append(value).append(LogConstants.CRLF);
             }
         }   catch (Exception e) {
             debug.error("LogConfigReader: Could not read filehandler ", e);
         }
         // secure File handler class
         try {
             key = LogConstants.SECURE_FILE_HANDLER;
             value = CollectionHelper.getMapAttr(logAttributes, key);
             if ((value == null) || (value.length() ==0)) {
                 debug.warning("LogConfigReader: Secure FH string is null");
             } else {
                 sbuffer.append(key).append("=")
                        .append(value).append(LogConstants.CRLF);
             }
         }   catch (Exception e) {
             debug.error("LogConfigReader: Could not read " +
                 "secure filehandler ", e);
         }
         // db handler class
         try {
             key = LogConstants.DB_HANDLER;
             value = CollectionHelper.getMapAttr(logAttributes, key);
             if ((value == null) || (value.length() ==0)) {
                 debug.warning("LogConfigReader: DBHandler string is null");
             } else {
                 sbuffer.append(key).append("=")
                        .append(value).append(LogConstants.CRLF);
             }
         }   catch (Exception e) {
             debug.error("LogConfigReader: Could not read dbhandler ", e);
         }
         // remote handler class
         try {
             key = LogConstants.REMOTE_HANDLER;
             value = CollectionHelper.getMapAttr(logAttributes, key);
             if ((value == null) || (value.length() ==0)) {
                 debug.warning("LogConfigReader: Remote Handler string is null");
             } else {
                 sbuffer.append(key).append("=")
                        .append(value).append(LogConstants.CRLF);
             }
         }   catch (Exception e) {
             debug.error("LogConfigReader: Could not read remotehandler ", e);
         }
         
         // elf formatter class
         try {
             key = LogConstants.ELF_FORMATTER;
             value = CollectionHelper.getMapAttr(logAttributes, key);
             if ((value == null) || (value.length() ==0)) {
                 debug.warning("LogConfigReader:ELFFormatter string is null");
             } else {
                 sbuffer.append(key).append("=")
                        .append(value).append(LogConstants.CRLF);
             }
         }   catch (Exception e) {
             debug.error("LogConfigReader: Could not read elfformatter ", e);
         }
         // secure elf formatter class
         try {
             key = LogConstants.SECURE_ELF_FORMATTER;
             value = CollectionHelper.getMapAttr(logAttributes, key);
             if ((value == null) || (value.length() ==0)) {
                 debug.warning("LogConfigReader: Secure " +
                     "ELFFormatter string is null");
             } else {
                 sbuffer.append(key).append("=")
                        .append(value).append(LogConstants.CRLF);
             }
         }   catch (Exception e) {
             debug.error("LogConfigReader: Could not read secure formatter ", e);
         }
         
         // db formatter class
         try {
             key = LogConstants.DB_FORMATTER;
             value = CollectionHelper.getMapAttr(logAttributes, key);
             sbuffer.append(key).append("=")
                    .append(value).append(LogConstants.CRLF);
         }   catch (Exception e) {
             debug.error("LogConfigReader: Could not read dbformatter ", e);
         }
         // db formatter class
         try {
             key = LogConstants.REMOTE_FORMATTER;
             value = CollectionHelper.getMapAttr(logAttributes, key);
             sbuffer.append(key).append("=")
                    .append(value).append(LogConstants.CRLF);
         }   catch (Exception e) {
             debug.error("LogConfigReader: Could not read remoteformatter ", e);
         }
         // authz class
         try {
             key = LogConstants.AUTHZ;
             value = CollectionHelper.getMapAttr(logAttributes, key);
             if ((value == null) || (value.length() ==0)) {
                 debug.warning("LogConfigReader: AUTHZ string is null");
             } else {
                 sbuffer.append(key).append("=")
                        .append(value).append(LogConstants.CRLF);
             }
         }   catch (Exception e) {
             debug.error("LogConfigReader: Could not read authz class", e);
         }
         // log location
         try {
             key = LogConstants.LOG_LOCATION;
             value = CollectionHelper.getMapAttr(logAttributes, key);
             if ((value == null) || (value.length() ==0)) {
                 debug.warning("LogConfigReader: LogLocation string is null");
             } else {
                 value = value.replace('\\','/');
                 sbuffer.append(key).append("=")
                        .append(value).append(LogConstants.CRLF);
             }
         }   catch (Exception e) {
             debug.error("LogConfigReader: Could not read loglocation ", e);
         }
         // security status (on or off)
         try {
             key = LogConstants.SECURITY_STATUS;
             value = CollectionHelper.getMapAttr(logAttributes, key);
             if ((value == null) || (value.length() ==0)) {
                 debug.warning(
                     "LogConfigReader: Security status string is null");
             } else {
                 sbuffer.append(key).append("=")
                        .append(value).append(LogConstants.CRLF);
             }
         } catch (Exception e) {
             debug.error("LogConfigReader: Could not read security status ", e);
         }
 
         // secure log signing algorithm name 
         // MD2withRSA, MD5withRSA, SHA1withDSA, SHA1withRSA
         try {
             key = LogConstants.SECURITY_SIGNING_ALGORITHM;
             value = CollectionHelper.getMapAttr(logAttributes, key, 
                 LogConstants.DEFAULT_SECURITY_SIGNING_ALGORITHM);
             sbuffer.append(key).append("=")
                 .append(value).append(LogConstants.CRLF);
         } catch (Exception e) {
             debug.error("LogConfigReader: Could not read secure " +
                 "log signing alogorithm ", e);
         }
 
         // secure log helper class name 
         // com.sun.identity.log.secure.impl.SecureLogHelperJSSImpl or 
         // com.sun.identity.log.secure.impl.SecureLogHelperJCEImpl
         try {
             key = LogConstants.SECURE_LOG_HELPER;
             value = CollectionHelper.getMapAttr(logAttributes, key, 
                 LogConstants.SECURE_DEFAULT_LOG_HELPER);
             sbuffer.append(key).append("=")
                    .append(value).append(LogConstants.CRLF);
         } catch (Exception e) {
             debug.error("LogConfigReader: Could not read secure " +
                 "log helper class name ", e);
         }
         
         // secure logger certificate store
         try {
             key = LogConstants.LOGGER_CERT_STORE;
             value = CollectionHelper.getMapAttr(logAttributes, key);
             if ((value == null) || (value.length() ==0)) {
                 debug.warning("LogConfigReader: secure logger " +
                     "certificate store is null");
             } else {
                 sbuffer.append(key).append("=")
                        .append(value).append(LogConstants.CRLF);
             }
         } catch (Exception e) {
             debug.error("LogConfigReader: Could not read secure " +
                 "logger certificate store ", e);
         }
         // log verification period in seconds
         try {
             key = LogConstants.LOGVERIFY_PERIODINSECONDS;
             value = CollectionHelper.getMapAttr(logAttributes, key);
             if ((value == null) || (value.length() ==0)) {
                 debug.warning("LogConfigReader: Verify period string is null");
             } else {
                 sbuffer.append(key).append("=")
                        .append(value).append(LogConstants.CRLF);
             }
         }   catch (Exception e) {
             debug.error("LogConfigReader: Could not read logverify period", e);
         }
         // log signing period in seconds
         try {
             key = LogConstants.LOGSIGN_PERIODINSECONDS;
             value = CollectionHelper.getMapAttr(logAttributes, key);
             if ((value == null) || (value.length() ==0)) {
                 debug.warning("LogConfigReader: sign period string is null");
             } else {
                 sbuffer.append(key).append("=")
                        .append(value).append(LogConstants.CRLF);
             }
         }   catch (Exception e) {
             debug.error("LogConfigReader: Could not read sign fieldname ", e);
         }
         // file read handler class
         try {
             key = LogConstants.FILE_READ_HANDLER;
             value = CollectionHelper.getMapAttr(logAttributes, key);
             if ((value == null) || (value.length() ==0)) {
                 debug.warning("LogConfigReader: file readhandler " +
                     "string is null");
             } else {
                 sbuffer.append(key).append("=")
                        .append(value).append(LogConstants.CRLF);
             }
         }   catch (Exception e) {
             debug.error("LogConfigReader: Could not read " +
                 "filehandler class ", e);
         }
         // DB read handler class
         try {
             key = LogConstants.DB_READ_HANDLER;
             value = CollectionHelper.getMapAttr(logAttributes, key);
             if ((value == null) || (value.length() ==0)) {
                 debug.warning("LogConfigReader: DB readhandler string is null");
             } else {
                 sbuffer.append(key).append("=")
                        .append(value).append(LogConstants.CRLF);
             }
         }   catch (Exception e) {
             debug.error("LogConfigReader: could not read DBreadhandler class ",
                 e);
         }
         // MAX_RECORDS
         try {
             key = LogConstants.MAX_RECORDS;
             value = CollectionHelper.getMapAttr(logAttributes, key);
             if ((value == null) || (value.length() ==0)) {
                 debug.warning("LogConfigReader: Max records string is null");
             } else {
                 sbuffer.append(key).append("=")
                        .append(value).append(LogConstants.CRLF);
             }
         }   catch (Exception e) {
             debug.error("LogConfigReader: Could not read max-records ", e);
         }
         // FILES_PER_KEYSTORE
         try {
             key = LogConstants.FILES_PER_KEYSTORE;
             value = CollectionHelper.getMapAttr(logAttributes, key);
             if ((value == null) || (value.length() ==0)) {
                 debug.warning("LogConfigReader: filesper " +
                     "keystore string is null");
             } else {
                 sbuffer.append(key).append("=")
                        .append(value).append(LogConstants.CRLF);
             }
         }   catch (Exception e) {
             debug.error("LogConfigReader:Could not read files per keystore", e);
         }
         // Token Generating Class
         try {
             key = LogConstants.TOKEN_PROVIDER;
             value = CollectionHelper.getMapAttr(logAttributes, key);
             if ((value == null) || (value.length() ==0)) {
                 debug.warning("LogConfigReader: token provider string is null");
             } else {
                 sbuffer.append(key).append("=")
                        .append(value).append(LogConstants.CRLF);
             }
         } catch(Exception e) {
             debug.error("LogConfigReader:Could not read Token " +
                 "Generation Class name");
         }
         // Secure Timestamp generator class
         try {
             key = LogConstants.SECURE_TIMESTAMP_GENERATOR;
             value = CollectionHelper.getMapAttr(logAttributes, key);
             if ((value == null) || (value.length() ==0)) {
                 debug.warning("LogConfigReader: timestamp " +
                     "generator string is null");
             } else {
                 sbuffer.append(key).append("=")
                        .append(value).append(LogConstants.CRLF);
             }
         } catch(Exception e) {
             debug.error("LogConfigReader:Could not read Token " +
                 "Generation Class name");
         }
         // Verifier Action Output Class
         try {
             key = LogConstants.VERIFIER_ACTION_CLASS;
             value = CollectionHelper.getMapAttr(logAttributes, key);
             if ((value == null) || (value.length() ==0)) {
                 debug.warning("LogConfigReader: verifier " +
                     "actionclass string is null");
             } else {
                 sbuffer.append(key).append("=")
                        .append(value).append(LogConstants.CRLF);
             }
         } catch(Exception e) {
             debug.error("LogConfigReader:Could not read verifier " +
                 "output Class name");
         }
         // filter class name
         try {
             key = LogConstants.FILTER_CLASS_NAME;
             value = CollectionHelper.getMapAttr(logAttributes, key);
             if ((value == null) || (value.length() ==0)) {
                 debug.warning("LogConfigReader: filter class " +
                     "name string is null");
             } else {
                 sbuffer.append(key).append("=")
                        .append(value).append(LogConstants.CRLF);
             }
         } catch(Exception e) {
             debug.error("LogConfigReader:Could not read filter class");
         }
         // debug Implementation Class
         try {
             key = LogConstants.DEBUG_IMPL_CLASS;
             value = CollectionHelper.getMapAttr(logAttributes, key);
             if ((value == null) || (value.length() ==0)) {
                 debug.error("LogConfigReader: debug implclass string is null");
             } else {
                 sbuffer.append(key).append("=")
                        .append(value).append(LogConstants.CRLF);
             }
         } catch(Exception e) {
             debug.error("LogConfigReader:Could not read debug Impl Class name");
         }
         
         // Buffer size
         try {
             key = LogConstants.BUFFER_SIZE;
             value = CollectionHelper.getMapAttr(logAttributes, key);
             if ((value == null) || (value.length() ==0)) {
                 debug.warning("LogConfigReader: buffer size string is null");
             } else {
                 sbuffer.append(key).append("=")
                        .append(value).append(LogConstants.CRLF);
             }
         } catch(Exception e) {
             debug.error("LogConfigReader:Could not read buf size");
         }
 
         // Max DB Mem Buffer size
         try {
             key = LogConstants.DB_MEM_MAX_RECS;
             value = CollectionHelper.getMapAttr(logAttributes, key);
             if ((value == null) || (value.length() == 0)) {
                 debug.warning(
                 "LogConfigReader: Max DB mem buffer size string is null");
             } else {
                 sbuffer.append(key).append("=").append(value).
                     append(LogConstants.CRLF);
             }
         } catch(Exception e) {
             debug.error("LogConfigReader:Could not read max db mem buf size");
         }
 
         // Buffer Time
         try {
             key = LogConstants.BUFFER_TIME;
             value = CollectionHelper.getMapAttr(logAttributes, key);
             if ((value == null) || (value.length() ==0)) {
                 debug.warning("LogConfigReader: buffer time string is null");
             } else {
                 sbuffer.append(key).append("=")
                        .append(value).append(LogConstants.CRLF);
             }
         } catch(Exception e) {
             debug.error("LogConfigReader:Could not read buf time");
         }
         // Time Buffering Status
         try {
             key = LogConstants.TIME_BUFFERING_STATUS;
             value = CollectionHelper.getMapAttr(logAttributes, key);
             if ((value == null) || (value.length() ==0)) {
                 debug.warning("LogConfigReader: time " +
                     "buffering status string is null");
             } else {
                 sbuffer.append(key).append("=")
                        .append(value).append(LogConstants.CRLF);
             }
         } catch(Exception e) {
             debug.error("LogConfigReader:Could not read time " +
                 "buffering status ");
         }
 
         // Log status from the AMConfig.properties file
         try {
             key = LogConstants.LOG_STATUS;
             value = SystemProperties.get(key);
             if ((value == null) || (value.length() == 0)) {
                 debug.warning("LogConfigReader: Log Status string is null");
             } else {
                 sbuffer.append(key).append("=").append(value).
                     append(LogConstants.CRLF);
             }
         } catch(Exception e) {
             debug.error("LogConfigReader:Could not read Log Status");
         }
 
         // processing platform attributes
         try {
             platformAttributes = smsPlatformSchema.getAttributeDefaults();
             key = LogConstants.LOCALE;
             value = CollectionHelper.getMapAttr(platformAttributes, key);
             if ((value == null) || (value.length() ==0)) {
                 debug.warning("LogConfigReader: locale string is null");
             } else {
                 sbuffer.append(key).append("=")
                        .append(value).append(LogConstants.CRLF);
             }
         }   catch (Exception e) {
             debug.error("LogConfigReader: Could not read platform ", e);
         }
         // processing naming attributes
         try {
             namingAttributes = smsNamingSchema.getAttributeDefaults();
             key = LogConstants.LOGGING_SERVICE_URL;
             value = CollectionHelper.getMapAttr(namingAttributes, key);
             if ((value == null) || (value.length() ==0)) {
                 debug.warning("LogConfigReader: loggins " +
                     "service url string is null");
             } else {
                 sbuffer.append(key).append("=")
                        .append(value).append(LogConstants.CRLF);
             }
         } catch (Exception e) {
             debug.error("LogConfigReader: could not get from DS", e);
         }
         return sbuffer.toString();
     }
     
     /**
      * This method is used to get the global schemas of Logging, Platform
      * and Naming Services. Platform service schema is used to determine the
      * platform locale, naming service is used to get the logging service url.
      */
     private void getDefaultAttributes(SSOToken ssoToken)
     throws SMSException, SSOException {
         
         ServiceSchemaManager schemaManager =
         new ServiceSchemaManager("iPlanetAMLoggingService", ssoToken);
         smsLogSchema = schemaManager.getGlobalSchema();
         if (!isRegisteredForDSEvents) {
             schemaManager.addListener(this);
         }
         schemaManager =
         new ServiceSchemaManager("iPlanetAMPlatformService", ssoToken);
         if (!isRegisteredForDSEvents) {
             schemaManager.addListener(this);
         }
         smsPlatformSchema = schemaManager.getGlobalSchema();
         schemaManager =
         new ServiceSchemaManager("iPlanetAMNamingService", ssoToken);
         if (!isRegisteredForDSEvents) {
             schemaManager.addListener(this);
             isRegisteredForDSEvents = true;
         }
         smsNamingSchema = schemaManager.getGlobalSchema();
         
         // get the default attributes of each service(Logging, Platform and
         // Naming).
         logAttributes           = smsLogSchema.getAttributeDefaults();
         platformAttributes      = smsPlatformSchema.getAttributeDefaults();
         namingAttributes        = smsNamingSchema.getAttributeDefaults();
     }
     
     /**
      * This method is used for gettting the SSOToken from the
      * TokenManager using Principal and defaultOrg. Need to
      * figure out a better method and/or to confirm
      * whether the existing method is good enough.
      * This method of obtaining token has problems in DSAME5.2 alpha(hanging)
      * Written on 25/4/2002 for DSAME6.0
      * @throws SMSException
      * @throws SSOException
      */
     private SSOToken getSSOToken() throws SSOException {
         SSOTokenManager mgr = SSOTokenManager.getInstance();
         String adminDN = 
             (String)AccessController.doPrivileged(new AdminDNAction());
         String adminPassword =
             (String)AccessController.doPrivileged(new AdminPasswordAction());
         return mgr.createSSOToken(new AuthPrincipal(adminDN), adminPassword);
     }
     
     /**
      * This method checks whether the logging service url is explicitly 
      * mentioned in the naming service. If yes then validates the URL against 
      * the platform server list of trusted servers. if the logging service 
      * url is not mentioned explicitly it sets the local flag to true.
      */
     private void setLocalFlag() {
         if (debug.messageEnabled()) {
             debug.message("LogConfigReader: logserviceID is" 
                 + localLogServiceID);
         }
         try{
             // can't do this here because NamingService is not
             // dynamically updated.
             // URL url =  WebtopNaming.getServiceURL(LOGGING_SERVICE,
             // protocol, host, port);
             
             String urlString = 
                 manager.getProperty(LogConstants.LOGGING_SERVICE_URL);
             String logHost = null;
             if (urlString.indexOf("%") == -1) {
                 logHost = urlString;
             } else {
                 logHost = localLogServiceID;
             }
             if ((localLogServiceID) != null && (logHost != null)) {
                 if (logHost.startsWith(localLogServiceID))
                 {
                     LogManager.isLocal = true;
                 }
                 else {
                     LogManager.isLocal = false;
                 }
             }
         } catch (Exception e) {
             debug.error("LogConfigReader: Error setting localFlag: ",e);
         }
     }
     
     // following methods
     // to implement ServiceListener
     
     public void globalConfigChanged(
         String servName,
         String ver,
         String frpName,
         String servComp,
         int type
     ) {
         debug.message("Global config change");
     }
     
     public void organizationConfigChanged(
         String servName,
         String ver,
         String orgName,
         String grpName,
         String servComp,
         int type
     ) {
         debug.message("Org config change");
     }
     
     public void schemaChanged(String servName,String ver) {
         
         if (debug.messageEnabled()) {
             debug.message("LogService schemaChanged(): ver = " + ver);
         }
         //shifting to LogManager according to review.
         try{
             manager.readConfiguration();
         } catch (Exception e) {
             debug.error("Error in readConfiguration()",e);
         }
     }
 }
