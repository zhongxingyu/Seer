 /**
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright (c) 2008 Sun Microsystems, Inc. All Rights Reserved.
  *
  * The contents of this file are subject to the terms
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
 * $Id: TuneFAM8Impl.java,v 1.4 2008-07-25 06:06:21 kanduls Exp $
  */
 
 package com.sun.identity.tune.impl;
 
 import com.sun.identity.tune.base.AMTuneFAMBase;
 import com.sun.identity.tune.common.FileHandler;
 import com.sun.identity.tune.common.AMTuneException;
 import com.sun.identity.tune.config.AMTuneConfigInfo;
 import com.sun.identity.tune.util.AMTuneUtil;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 import java.util.logging.Level;
 
 /**
  * <code>TuneFAM8Impl<\code> Tunes OpenSSO Enterprise Server.
  * 
  */
 public class TuneFAM8Impl extends AMTuneFAMBase {
     
     /**
      * Initializes the configuration information.
      * 
      * @param confInfo Configuration information used for computing the tuning 
      *   parameters for OpenSSO Enterprise.
      * 
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     public void initialize(AMTuneConfigInfo configInfo)
     throws AMTuneException {
         super.initialize(configInfo);
     }
     
     /**
      * This method performs the sequence of operations for tuning 
      * OpenSSO Enterprise server.
      * 
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     public void startTuning() 
     throws AMTuneException {
         try {
             pLogger.log(Level.FINE, "startTuning",
                 "Start tuning OpenSSO Enterprise.");
             mWriter.writeln(CHAPTER_SEP);
             mWriter.writelnLocaleMsg("pt-fam-tuning-msg");
             mWriter.writeln(CHAPTER_SEP);
             mWriter.writelnLocaleMsg("pt-init");
             mWriter.writeln(LINE_SEP);
             if (!isFAMServerUp()) {
                 mWriter.writelnLocaleMsg("pt-fam-server-down-msg");
                 return;
             }
             tuneFAMServerConfig();
             tuneServerConfig();
             tuneLDAPConnPool();
             if (configInfo.getDefaultOrgPeopleContainer() != null &&
                     configInfo.getDefaultOrgPeopleContainer().trim().
                     length() > 0) {
                 tuneLDAPSearchCriteriaForDefaultOrg();
             }
             if (configInfo.getFAMTuneDontTouchSessionParameters()) {
                 tuneSessionTimeouts();
             }
             List realmList = configInfo.getRealms();
             Iterator realmItr = realmList.iterator();
             while (realmItr.hasNext()) {
                 String curRealm = realmItr.next().toString();
                 if (curRealm != null & curRealm.trim().length() > 0) {
                     List dataStoreList = getDataStoreList(curRealm);
                     Iterator dtItr = dataStoreList.iterator();
                     while (dtItr.hasNext()) {
                         tuneRealmDataStoreConfig(dtItr.next().toString(), 
                                 curRealm);
                     }
                 }
             }
             mWriter.writeln(PARA_SEP);
             mWriter.writelnLocaleMsg("pt-done");
         } catch (Exception ex) {
             pLogger.logException("startTuning", ex);
             mWriter.writelnLocaleMsg("pt-error-tuning-msg");
             mWriter.writelnLocaleMsg("pt-manual-msg");
         } finally {
             deletePasswordFile();
         }
     }
     
     /**
      * This method provides the recommendation for tuning 
      * OpenSSO Enterprise server and apply those recommendations if run in
      * "CHANGE" mode.
      * 
      */
     protected void tuneFAMServerConfig() {
         List newAttrList = new ArrayList();
         Map curCfgMap = getFAMServerConfig();
         String ATTR1 = SDK_CACHE_MAXSIZE + "=" + configInfo.getNumSessions();
         String ATTR2 = NOTIFICATION_THREADPOOL_SIZE + "=" +
                 configInfo.getNumNotificationThreads();
         String ATTR3 = NOTIFICATION_THREADPOOL_THRESHOLD + "=" + 
                 configInfo.getNumNotificationQueue();
         String ATTR4 = MAX_SESSIONS + "=" + configInfo.getNumSessions();
         String ATTR5 = HTTP_SESSION_ENABLED + "=false";
         String ATTR6 = SESSION_PURGE_DELAY + "=0";
        String ATTR7 = INVALID_SESSION_MAX_TIME + "=2";
         String curVal = null;
         mWriter.writeln(LINE_SEP);
         mWriter.writelnLocaleMsg("pt-fam-rec-parm-tune-msg");
         mWriter.writeln(" ");
         mWriter.writelnLocaleMsg("pt-param-tuning");
         mWriter.writeln(" ");
         mWriter.writeln("1.    " + SDK_CACHE_MAXSIZE);
         mWriter.writeLocaleMsg("pt-cur-val");
         if (curCfgMap.get(SDK_CACHE_MAXSIZE) != null) {
             curVal = curCfgMap.get(SDK_CACHE_MAXSIZE).toString();
               if (ATTR1.indexOf(curVal) == -1 ) {
                 newAttrList.add(ATTR1);
             }
         } else {
             curVal = NO_VAL_SET;
             newAttrList.add(ATTR1);
         }
         mWriter.writeln(SDK_CACHE_MAXSIZE + "=" + curVal);
         mWriter.writeLocaleMsg("pt-rec-val");
         mWriter.writeln(ATTR1);
         mWriter.writeln(" ");
         
         mWriter.writeln("2.    " + NOTIFICATION_THREADPOOL_SIZE);
         mWriter.writeLocaleMsg("pt-cur-val");
         if (curCfgMap.get(NOTIFICATION_THREADPOOL_SIZE) != null) {
             curVal = curCfgMap.get(NOTIFICATION_THREADPOOL_SIZE).toString();
             if (ATTR2.indexOf(curVal) == -1) {
                 newAttrList.add(ATTR2);
             }
         } else {
             curVal = NO_VAL_SET;
             newAttrList.add(ATTR2);
         }
         mWriter.writeln(NOTIFICATION_THREADPOOL_SIZE + "=" + curVal);
         mWriter.writeLocaleMsg("pt-rec-val");
         mWriter.writeln(ATTR2);
         mWriter.writeln(" ");
         
         mWriter.writeln("3.    " + NOTIFICATION_THREADPOOL_THRESHOLD);
         mWriter.writeLocaleMsg("pt-cur-val");
         if (curCfgMap.get(NOTIFICATION_THREADPOOL_THRESHOLD) != null) {
             curVal = 
                     curCfgMap.get(NOTIFICATION_THREADPOOL_THRESHOLD).toString();
             if (ATTR3.indexOf(curVal) == -1) {
                 newAttrList.add(ATTR3);
             }
         } else {
             curVal = NO_VAL_SET;
             newAttrList.add(ATTR3);
         }
         mWriter.writeln(NOTIFICATION_THREADPOOL_THRESHOLD + "=" + curVal);
         mWriter.writeLocaleMsg("pt-rec-val");
         mWriter.writeln(ATTR3);
         mWriter.writeln(" ");
         
         mWriter.writeln("4.    " + MAX_SESSIONS);
         mWriter.writeLocaleMsg("pt-cur-val");
         if (curCfgMap.get(MAX_SESSIONS) != null ) {
             curVal = curCfgMap.get(MAX_SESSIONS).toString();
             if (ATTR4.indexOf(curVal) == -1) {
                 newAttrList.add(ATTR4);
             }
         } else {
             curVal = NO_VAL_SET;
             newAttrList.add(ATTR4);
         }
         mWriter.writeln(MAX_SESSIONS + "=" + curVal);
         mWriter.writeLocaleMsg("pt-rec-val");
         mWriter.writeln(ATTR4);
         mWriter.writeln(" ");
         
         mWriter.writeln("5.    " + HTTP_SESSION_ENABLED);
         mWriter.writeLocaleMsg("pt-cur-val");
         if (curCfgMap.get(HTTP_SESSION_ENABLED) != null) {
             curVal = curCfgMap.get(HTTP_SESSION_ENABLED).toString();
             if (ATTR5.indexOf(curVal) == -1) {
                 newAttrList.add(ATTR5);
             }
         } else {
             curVal = NO_VAL_SET;
             newAttrList.add(ATTR5);
         }
         mWriter.writeln(HTTP_SESSION_ENABLED + "=" + curVal);
         mWriter.writeLocaleMsg("pt-rec-val");
         mWriter.writeln(ATTR5);
         mWriter.writeln(" ");
         
         mWriter.writeln("6.    " + SESSION_PURGE_DELAY);
         mWriter.writeLocaleMsg("pt-cur-val");
         if (curCfgMap.get(SESSION_PURGE_DELAY) != null ) {
             curVal = curCfgMap.get(SESSION_PURGE_DELAY).toString();
             if (Integer.parseInt(curVal) != 0) {
                 newAttrList.add(ATTR6);
             }
         } else {
             curVal = NO_VAL_SET;
             newAttrList.add(ATTR6);
         }
         mWriter.writeln(SESSION_PURGE_DELAY + "=" +curVal);
         mWriter.writeLocaleMsg("pt-rec-val");
         mWriter.writeln(ATTR6);
         mWriter.writeln(" ");
         
         mWriter.writeln("7.    " + INVALID_SESSION_MAX_TIME);
         mWriter.writeLocaleMsg("pt-cur-val");
         if (curCfgMap.get(INVALID_SESSION_MAX_TIME) != null) {
             curVal = curCfgMap.get(INVALID_SESSION_MAX_TIME).toString();
            if (Integer.parseInt(curVal) != 2) {
                 newAttrList.add(ATTR7);
             }
         } else {
             curVal = NO_VAL_SET;
             newAttrList.add(ATTR7);
         }
         mWriter.writeln(INVALID_SESSION_MAX_TIME + "=" + curVal);
         mWriter.writeLocaleMsg("pt-rec-val");
         mWriter.writeln(ATTR7);
         mWriter.writeln(" ");
         if (configInfo.isReviewMode()) {
             return;
         }
         mWriter.writelnLocaleMsg("pt-fam-tuning-server-config");
         if (newAttrList != null && newAttrList.size() > 0) {
             updateFAMServiceCfg(newAttrList);
         } else {
             mWriter.writelnLocaleMsg("pt-fam-cur-rec-val-same");
         }
         mWriter.writeln(" ");
     }
     
     /**
      * This method provides the recommendations for tuning
      * serverconfig LDAP connection pool.
      */
     protected void tuneServerConfig() 
     throws AMTuneException {
         String tuneFile = AMTuneUtil.TMP_DIR + "serverconfig.xml";
         try {
             String newMinPool = "8";
             String newMaxPool = "32";
             StringBuffer getCmd = new StringBuffer(famCmdPath);
             getCmd.append(GET_SVRCFG_XML_SUB_CMD);
             getCmd.append(famadmCommonParamsNoServer);
             getCmd.append(" ");
             getCmd.append(FAMADM_SERVER);
             getCmd.append(" ");
             getCmd.append(configInfo.getFAMServerUrl());
             getCmd.append(" ");
             getCmd.append(OUTFILE_OPT);
             getCmd.append(" ");
             getCmd.append(tuneFile);
             StringBuffer rBuff = new StringBuffer();
             int extVal = AMTuneUtil.executeCommand(getCmd.toString(), rBuff);
             if (extVal == -1) {
                 pLogger.log(Level.SEVERE, "tuneServerConfig", "Couldn't get " +
                         "serverconfig.xml file");
                 throw new AMTuneException("Error exectuing ssoadm " + 
                         GET_SVRCFG_XML_SUB_CMD);
             }
             FileHandler fh = new FileHandler(tuneFile);
             String reqLine = fh.getLine(SMS_ELEMENT);
             StringTokenizer strT = new StringTokenizer(reqLine, " ");
             String curMinConnPool = "";
             String curMaxConnPool = "";
             while (strT.hasMoreTokens()) {
                 String token = strT.nextToken();
                 if (token.indexOf(MIN_CONN_POOL) != -1) {
                     curMinConnPool = token.replace(MIN_CONN_POOL + "=", 
                             "").replace("\"", "").replace(">", "").trim();
                 }
                 if (token.indexOf(MAX_CONN_POOL) != -1) {
                     curMaxConnPool = token.replace(MAX_CONN_POOL + "=",
                             "").replace("\"", "").replace(">", "").trim();
                 }
             }
             mWriter.writeln(LINE_SEP);
             mWriter.writelnLocaleMsg("pt-fam-tuning-ldap-con-pool");
             mWriter.writelnLocaleMsg("pt-param-tuning");
             mWriter.writeln(" ");
             mWriter.writelnLocaleMsg("pt-fam-refer-guide-msg");
             mWriter.writeln(" ");
             mWriter.writelnLocaleMsg("pt-minconpool-msg");
             mWriter.writeLocaleMsg("pt-cur-val");
             mWriter.writeln(MIN_CONN_POOL + "=" + curMinConnPool);
             mWriter.writeLocaleMsg("pt-rec-val");
             mWriter.writeln(MIN_CONN_POOL + "=" + newMinPool);
             mWriter.writeln(" ");
             mWriter.writelnLocaleMsg("pt-maxconpool-msg");
             mWriter.writeLocaleMsg("pt-cur-val");
             mWriter.writeln(MAX_CONN_POOL + "=" + curMaxConnPool);
             mWriter.writeLocaleMsg("pt-rec-val");
             mWriter.writeln(MAX_CONN_POOL + "=" + newMaxPool);
             mWriter.writeln(" ");
             mWriter.writeln(" ");
             if (configInfo.isReviewMode() || 
                     (curMinConnPool.equals(newMinPool) && 
                     curMaxConnPool.equals(newMaxPool))) {
                 return;
             }
             AMTuneUtil.backupConfigFile(tuneFile, "conf-fam-backup");
             int lineNo = fh.getLineNum(SMS_ELEMENT);
             reqLine = fh.getLine(SMS_ELEMENT);
             reqLine = reqLine.replace(MIN_CONN_POOL + "=\"" + curMinConnPool + 
                     "\"", MIN_CONN_POOL + "=\"" + newMinPool + "\"");
             reqLine = reqLine.replace(MAX_CONN_POOL + "=\"" + curMaxConnPool + 
                     "\"", MAX_CONN_POOL + "=\"" + newMaxPool + "\"");
             fh.replaceLine(lineNo, reqLine);
             //Replace the default server value as well.
             lineNo = fh.getLineNum(DEFAULT_SERVER_ELEMENT);
             reqLine = fh.getLine(DEFAULT_SERVER_ELEMENT);
             String curDefaultMinConPoolVal = "";
             String curDefaultMaxConPoolVal = "";
             strT = new StringTokenizer(reqLine, " ");
             while (strT.hasMoreTokens()) {
                 String token = strT.nextToken();
                 if (token.indexOf(MIN_CONN_POOL) != -1) {
                     curDefaultMinConPoolVal = token.replace(MIN_CONN_POOL + "=", 
                             "").replace("\"", "").replace(">", "").trim();
                 }
                 if (token.indexOf(MAX_CONN_POOL) != -1) {
                     curDefaultMaxConPoolVal = token.replace(MAX_CONN_POOL + "=",
                             "").replace("\"", "").replace(">", "").trim();
                 }
             }
             reqLine = reqLine.replace(MIN_CONN_POOL + "=\"" + 
                     curDefaultMinConPoolVal + 
                     "\"", MIN_CONN_POOL + "=\"" + newMinPool + "\"");
             reqLine = reqLine.replace(MAX_CONN_POOL + "=\"" + 
                     curDefaultMaxConPoolVal + 
                     "\"", MAX_CONN_POOL + "=\"" + newMaxPool + "\"");
             fh.replaceLine(lineNo, reqLine);
             fh.close();
             rBuff.setLength(0);
             StringBuffer setCmd = new StringBuffer(famCmdPath);
             setCmd.append(SET_SVRCFG_XML_SUB_CMD);
             setCmd.append(famadmCommonParamsNoServer);
             setCmd.append(" ");
             setCmd.append(FAMADM_SERVER);
             setCmd.append(" ");
             setCmd.append(configInfo.getFAMServerUrl());
             setCmd.append(" ");
             setCmd.append(XML_FILE_OPT);
             setCmd.append(" ");
             setCmd.append(tuneFile);
             extVal = AMTuneUtil.executeCommand(setCmd.toString(), rBuff);
             if (extVal == -1) {
                 pLogger.log(Level.SEVERE, "tuneServerConfig", "Couldn't set " +
                         "serverconfig.xml file");
                 throw new AMTuneException("Error exectuing ssoadm " + 
                         SET_SVRCFG_XML_SUB_CMD);
             }
             
         } catch (Exception ex) {
             throw new AMTuneException("Error while tuning server config. " +
                     ex.getMessage());
         } finally {
             File f = new File(tuneFile);
             if (f.isFile()) {
                 f.delete();
             }
         }
     }
     
     /**
      * This method provides the recommendations for LDAP Connection Pool in 
      * Global iPlanetAMAuthService...
      */
     protected void tuneLDAPConnPool() 
     throws AMTuneException {
         try {
             String minMaxRatio = "8:32";
             StringBuffer getCmd = new StringBuffer(famCmdPath);
             getCmd.append(GET_ATTR_DEFS_SUB_CMD);
             getCmd.append(famadmCommonParamsNoServer);
             getCmd.append(" ");
             getCmd.append(SERVICE_NAME_OPT);
             getCmd.append(" ");
             getCmd.append(AUTH_SVC);
             getCmd.append(" "); 
             getCmd.append(SCHEMA_TYPE_OPT);
             getCmd.append(" "); 
             getCmd.append(GLOBAL_SCHEMA);
             getCmd.append(" ");
             getCmd.append(ATTR_NAMES_OPT);
             getCmd.append(" ");
             getCmd.append(LDAP_CONNECTION_POOL_SIZE);
             StringBuffer rBuff = new StringBuffer();
             int extVal = AMTuneUtil.executeCommand(getCmd.toString(), rBuff);
             if (extVal == -1) {
                 pLogger.log(Level.SEVERE, "tuneLDAPConnPool", "Error getting " +
                         "ldapconnpool size.");
                 throw new AMTuneException("Error executing ssoadm cmd " +
                         GET_ATTR_DEFS_SUB_CMD);
             }
             StringTokenizer str = new StringTokenizer(rBuff.toString(), "\n");
             String curLdapConVal = "";
             while (str.hasMoreTokens()) {
                 String reqLine = str.nextToken();
                 if (reqLine.indexOf(LDAP_CONNECTION_POOL_SIZE) != -1) {
                     curLdapConVal = AMTuneUtil.getLastToken(reqLine, "=");
                 }
             }
             mWriter.writeln(LINE_SEP);
             mWriter.writelnLocaleMsg("pt-fam-tuning-global-auth-svc");
             mWriter.writeln(" ");
             mWriter.writeln("Service              : iPlanetAMAuthService");
             mWriter.writeln("SchemaType           : global");
             mWriter.writeln(" ");
             mWriter.writelnLocaleMsg("pt-param-tuning");
             mWriter.writelnLocaleMsg("pt-fam-refer-guide-msg");
             mWriter.writeln(" ");
             mWriter.writeln("1.   " + LDAP_CONNECTION_POOL_SIZE);
             mWriter.writeLocaleMsg("pt-cur-val");
             mWriter.writelnLocaleMsg(LDAP_CONNECTION_POOL_SIZE + "=" +
                     curLdapConVal);
             mWriter.writeLocaleMsg("pt-rec-val");
             mWriter.writeln(LDAP_CONNECTION_POOL_SIZE + "=" + minMaxRatio);
             mWriter.writeln(" ");
             if (configInfo.isReviewMode() || 
                     minMaxRatio.equals(curLdapConVal)) {
                 return;
             }
             StringBuffer setCmd = new StringBuffer(famCmdPath);
             setCmd.append(SET_ATTR_DEFS_SUB_CMD);
             setCmd.append(famadmCommonParamsNoServer);
             setCmd.append(" ");
             setCmd.append(SERVICE_NAME_OPT);
             setCmd.append(" ");
             setCmd.append(AUTH_SVC);
             setCmd.append(" "); 
             setCmd.append(SCHEMA_TYPE_OPT);
             setCmd.append(" "); 
             setCmd.append(GLOBAL_SCHEMA);
             setCmd.append(" ");
             setCmd.append(ATTR_VALUES_OPT);
             if (!AMTuneUtil.isWindows()) {
                 setCmd.append(" ");
                 setCmd.append(LDAP_CONNECTION_POOL_SIZE);
                 setCmd.append("=");
                 setCmd.append(minMaxRatio);
             } else {
                 setCmd.append(" \"");
                 setCmd.append(LDAP_CONNECTION_POOL_SIZE);
                 setCmd.append("=");
                 setCmd.append(minMaxRatio);
                 setCmd.append("\"");
             }
             rBuff.setLength(0);
             extVal = AMTuneUtil.executeCommand(setCmd.toString(), rBuff);
             if (extVal == -1) {
                 pLogger.log(Level.SEVERE, "tuneLDAPConnPool", "Error setting " +
                         "ldapconnpool size.");
                 throw new AMTuneException("Error executing ssoadm cmd " +
                         SET_ATTR_DEFS_SUB_CMD);
             }
         } catch (Exception ex) {
             throw new AMTuneException(ex.getMessage());
         }
     }
 
     /**
      * This method provides recommendations for LDAP Search Criteria in 
      * iPlanetAMAuthLDAPService for Default Org
      * 
      */
     protected void tuneLDAPSearchCriteriaForDefaultOrg() {
 
         mWriter.writeln(LINE_SEP);
         mWriter.writelnLocaleMsg("pt-fam-tuning-ldap-search-criteria");
         mWriter.writeln(" ");
 
         mWriter.writeln("Service              : iPlanetAMAuthLDAPService for " +
                 "Org");
         mWriter.writeln("SchemaType           : organization");
         mWriter.writelnLocaleMsg("pt-param-tuning");
         mWriter.writeln(" ");
         mWriter.writelnLocaleMsg("pt-fam-rec-tuning-msg");
         mWriter.writelnLocaleMsg("pt-fam-tuning-ldap-search-change-step");
         mWriter.writeln(" ");
         mWriter.writeln("1.   " + LDAP_SEARCH_SCOPE);
         mWriter.writeLocaleMsg("pt-rec-val");
         mWriter.writeln(LDAP_SEARCH_SCOPE + "=OBJECT");
         mWriter.writeln(" ");
         mWriter.writeln(" ");
 
     }
     
     /**
      * This method provides recommendations for Session Timeouts in Global 
      * iPlanetAMSessionService...
      */
     protected void tuneSessionTimeouts() {
         mWriter.writeln(LINE_SEP);
         mWriter.writelnLocaleMsg("pt-fam-tuning-session-svc");
         mWriter.writeln(" ");
         mWriter.writeln("Service              : iPlanetAMSessionService");
         mWriter.writeln("SchemaType           : Dynamic");
         mWriter.writelnLocaleMsg("pt-param-tuning");
         mWriter.writeln(" ");
         mWriter.writelnLocaleMsg("pt-fam-rec-tuning-msg");
         mWriter.writelnLocaleMsg("pt-fam-tuning-session-change-step");
 
         mWriter.writeln("1.   " + MAX_SESSION_TIME);
         mWriter.writeLocaleMsg("pt-rec-val");
         mWriter.writeln(MAX_SESSION_TIME + "=" +
                 configInfo.getFAMTuneSessionMaxSessionTimeInMts());
         mWriter.writeln(" ");
         mWriter.writeln("2.   " + MAX_IDLE_TIME);
         mWriter.writeLocaleMsg("pt-rec-val");
         mWriter.writeln(MAX_IDLE_TIME + "=" +
                 configInfo.getFAMTuneSessionMaxIdleTimeInMts());
         mWriter.writeln(" ");
         mWriter.writeln("3.   " + MAX_CACHING_TIME);
         mWriter.writeLocaleMsg("pt-rec-val");
         mWriter.writeln(MAX_CACHING_TIME + "=" +
                 configInfo.getFAMTuneSessionMaxCachingTimeInMts());
         mWriter.writeln(" ");
         mWriter.writeln(" ");
 
     }
     
     protected void tuneRealmDataStoreConfig(String dataStoreType, String realm)
     throws AMTuneException {
         String dataFile = AMTuneUtil.TMP_DIR + "attrvals.txt";
         try {
             String poolMin = "8";
             String poolMax = "32";
             mWriter.writeln(LINE_SEP);
             StringBuffer dataStoreInfoCmd = new StringBuffer(famCmdPath);
             dataStoreInfoCmd.append(SHOW_DATASTORE_SUB_CMD);
             dataStoreInfoCmd.append(" -e ");
             dataStoreInfoCmd.append("REALM_NAME");
             dataStoreInfoCmd.append(" -m \"");
             dataStoreInfoCmd.append(dataStoreType);
             dataStoreInfoCmd.append("\"");
             dataStoreInfoCmd.append(famadmCommonParamsNoServer);
             
             StringBuffer dataStoreUpdateCmd = new StringBuffer(famCmdPath);
             dataStoreUpdateCmd.append(UPDATE_DATASTORE_SUB_CMD);
             dataStoreUpdateCmd.append(" -e ");
             dataStoreUpdateCmd.append("REALM_NAME");
             dataStoreUpdateCmd.append(" -m \"");
             dataStoreUpdateCmd.append(dataStoreType);
             dataStoreUpdateCmd.append("\"");
             dataStoreUpdateCmd.append(famadmCommonParamsNoServer);
 
             StringBuffer attrVals = new StringBuffer(LDAP_CONN_POOL_MIN);
             attrVals.append("=");
             attrVals.append(poolMin);
             attrVals.append("\n");
             attrVals.append(LDAP_CONN_POOL_MAX);
             attrVals.append("=");
             attrVals.append(poolMax);
             AMTuneUtil.writeResultBufferToTempFile(attrVals, dataFile);
             dataStoreUpdateCmd.append(" -D ");
             dataStoreUpdateCmd.append(dataFile);
             mWriter.writeLocaleMsg("pt-tuning");
             mWriter.write("DataStore \"" + dataStoreType + "\" ");
             mWriter.writelnLocaleMsg("pt-fam-realm-ldapconn-tuning");
             mWriter.writelnLocaleMsg("pt-param-tuning");
             mWriter.writeln(" ");
             mWriter.writeln("Realm Name : " + realm + ":");
             mWriter.writelnLocaleMsg("pt-min-max-ldap-conn-pool");
             mWriter.writelnLocaleMsg("pt-cur-val");
             StringBuffer rBuf = new StringBuffer();
             int extVal = 0;
             if (!AMTuneUtil.isWindows()) {
                 //write the command to file and then execute the file
                 //workaround as ssoadm is not working directly from
                 //runtime in *unix if any option contains space character.
                 // -m "Sun DS with AM Schema"
                 extVal = AMTuneUtil.executeScriptCmd(
                         dataStoreInfoCmd.toString().replace(
                         "REALM_NAME", realm), rBuf);
             } else {
                 extVal = AMTuneUtil.executeCommand(
                         dataStoreInfoCmd.toString().replace(
                         "REALM_NAME", realm), rBuf);
             }
             if (extVal == -1) {
                 pLogger.log(Level.SEVERE, "tuneRealmDataStoreConfig",
                         "Error finding realm ldap config info ");
             }
             String dsFile = AMTuneUtil.TMP_DIR + realm.replace("/", "rdelim") +
                     "datastore.txt";
             AMTuneUtil.writeResultBufferToTempFile(rBuf, dsFile);
             FileHandler fh = new FileHandler(dsFile);
             String curMax = AMTuneUtil.getLastToken(
                     fh.getLine(LDAP_CONN_POOL_MAX), "=");
             String curMin = AMTuneUtil.getLastToken(
                     fh.getLine(LDAP_CONN_POOL_MIN), "=");
             
             mWriter.writeln(LDAP_CONN_POOL_MIN + "=" + curMin);
             mWriter.writeln(LDAP_CONN_POOL_MAX + "=" + curMax);
             mWriter.writeln(" ");
             rBuf.setLength(0);
             mWriter.writelnLocaleMsg("pt-rec-val");
             mWriter.writeln(LDAP_CONN_POOL_MIN + "=" + poolMin);
             mWriter.writeln(LDAP_CONN_POOL_MAX + "=" + poolMax);
             if (configInfo.isReviewMode() || (curMin.equals(poolMin) &&
                     curMax.equals(poolMax))) {
                 return;
             }
             AMTuneUtil.backupConfigFile(dsFile, "conf-fam-backup");
             if (!AMTuneUtil.isWindows()) {
                 extVal = AMTuneUtil.executeScriptCmd(
                         dataStoreUpdateCmd.toString().replace(
                         "REALM_NAME", realm), rBuf);
             } else {
                 extVal = AMTuneUtil.executeCommand(
                         dataStoreUpdateCmd.toString().replace(
                         "REALM_NAME", realm), rBuf);
             }
             if (extVal == -1) {
                     pLogger.log(Level.SEVERE, "tuneRealmDataStoreConfig",
                         "Error setting realm ldap config info.");
             }
             File dsF = new File(dsFile);
             if (dsF.isFile()) {
                 dsF.delete();
             }
         } catch (Exception ex) {
             pLogger.log(Level.SEVERE, "tuneRealmDataStoreConfig",
                     "Error tuning data store values. ");
             throw new AMTuneException(ex.getMessage());
         } finally {
             File delFile = new File(dataFile);
             if (delFile.isFile()) {
                 delFile.delete();
             }
         }
     }
  }
