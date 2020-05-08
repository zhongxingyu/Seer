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
 * $Id: DSConfigInfo.java,v 1.3 2008-07-25 05:43:17 kanduls Exp $
  */
 
 package com.sun.identity.tune.config;
 
 import com.sun.identity.tune.common.MessageWriter;
 import com.sun.identity.tune.common.AMTuneException;
 import com.sun.identity.tune.common.AMTuneLogger;
 import com.sun.identity.tune.constants.DSConstants;
 import com.sun.identity.tune.util.AMTuneUtil;
 import java.io.File;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 
 public class DSConfigInfo implements DSConstants {
     private String dsDirMgrPassword;
     private String dsInstanceDir;
     private String dsHost;
     private String dsPort;
     private String rootSuffix;
     private String dirMgrUid;
     private String dsVersion;
     private String dsToolsDir;
     private String perlBinDir;
     private AMTuneLogger pLogger;
     private MessageWriter mWriter;
     private boolean isRemoteDS;
     public DSConfigInfo(ResourceBundle confBundle, boolean isSM) 
     throws AMTuneException {
         pLogger = AMTuneLogger.getLoggerInst();
         mWriter = MessageWriter.getInstance();
         if (!isSM) {
             setDsHost(confBundle.getString(DS_HOST));
             checkIsDSHostRemote();
             setDsPort(confBundle.getString(DS_PORT));
             setRootSuffix(confBundle.getString(ROOT_SUFFIX));
             setDirMgrUid(confBundle.getString(DIRMGR_UID));
             setDsDirMgrPassword(confBundle.getString(DIRMGR_PASSWORD));
             setDsVersion(confBundle.getString(DS_VERSION));
             if (!isRemoteDS) {
                 setDsInstanceDir(confBundle.getString(DS_INSTANCE_DIR));
                 if (getDsVersion().indexOf(DS5_VERSION) != -1) {
                     setPerlBinDir(confBundle.getString(PERL_BIN_DIR));
                 }
                 if (getDsVersion().indexOf(DS63_VERSION) != -1) {
                     setDSToolsBinDir(confBundle.getString(DS_TOOLS_DIR));
                 }
             }
         } else {
             setSMDSHost(confBundle.getString(SM_DS_HOST));
             checkIsDSHostRemote();
             setSMDSVersion(confBundle.getString(SM_DS_VERSION));
             setSMDSPort(confBundle.getString(SM_DS_PORT));
             setSMRootSuffix(confBundle.getString(SM_ROOT_SUFFIX));
             setSMDirMgrUid(confBundle.getString(SM_DS_DIRMGR_UID));
             setSMDSDirMgrPassword(confBundle.getString(SM_DIRMGR_PASSWORD));
             if (!isRemoteDS && getDsVersion().indexOf(DS63_VERSION) != -1) {
                 setSMDSInstanceDir(confBundle.getString(SM_DS_INSTANCE_DIR));
                 setSMDSToolsBinDir(confBundle.getString(SM_DS_TOOLS_DIR));
             }
         }
     }
 
     private void checkIsDSHostRemote() {
         pLogger.log(Level.FINEST, "checkIsDSHostRemote", "DS host is " +
                 getDsHost());
         pLogger.log(Level.FINEST, "checkIsDSHostRemote", "Local host is " +
                 AMTuneUtil.getHostName());
         if (AMTuneUtil.getHostName().equalsIgnoreCase(getDsHost())) {
             isRemoteDS = false;
         } else {
             isRemoteDS = true;
         }
     }
     /**
      * Set Directory Server administrator password.
      * @param dsDirMgrPassword
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private void setDsDirMgrPassword(String dsDirMgrPassword) 
     throws AMTuneException {
         if (dsDirMgrPassword != null && dsDirMgrPassword.trim().length() > 0) {
             this.dsDirMgrPassword = dsDirMgrPassword.trim();
         } else {
             mWriter.writeLocaleMsg("pt-inval-val-msg");
             AMTuneUtil.printErrorMsg(DIRMGR_PASSWORD);
             pLogger.log(Level.SEVERE, "setDsDirMgrPassword", 
                     "Error setting Directory Server Manager Password. " +
                     "Please check the value for the property " + 
                     DIRMGR_PASSWORD);
             throw new AMTuneException("Invalid value for " + DIRMGR_PASSWORD);
         }
     }
     
     public String getDsDirMgrPassword() {
         return dsDirMgrPassword;
     }
     
     /**
      * Set directory Server instance Directory.
      * @param dsInstanceDir
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private void setDsInstanceDir(String dsInstanceDir) 
     throws AMTuneException {
         if (dsInstanceDir != null && dsInstanceDir.trim().length() >0){
             File dirTest = new File(dsInstanceDir);
             if (dirTest.isDirectory()) {
                 this.dsInstanceDir = dsInstanceDir.trim();
             } else {
                 mWriter.writelnLocaleMsg("pt-not-valid-dir");
                 AMTuneUtil.printErrorMsg(DS_INSTANCE_DIR);
                 pLogger.log(Level.SEVERE, "setDsInstanceDir", 
                         "Directory instance path is not valid directory");
                 throw new AMTuneException("Invalid Directory Path.");
             }
         } else {
             mWriter.writeLocaleMsg("pt-inval-val-msg");
             AMTuneUtil.printErrorMsg(DS_INSTANCE_DIR);
             pLogger.log(Level.SEVERE, "setDsInstanceDir", 
                         "Error setting Directory Instance Path. " +
                         "Please check the value for " + DS_INSTANCE_DIR);
                 throw new AMTuneException("Invalid Directory Path.");
         }
     }
     
     public String getDsInstanceDir() {
         return dsInstanceDir;
     }
     
     /**
      * Set Directory Server Host name.
      * @param dsHost
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private void setDsHost(String dsHost) 
     throws AMTuneException {
         if (dsHost != null && dsHost.trim().length() > 0) {
             this.dsHost = dsHost.trim();
         } else {
             mWriter.writeLocaleMsg("pt-inval-val-msg");
             AMTuneUtil.printErrorMsg(DS_HOST);
             pLogger.log(Level.SEVERE, "setDsHost", 
                     "Error setting Directory Server Host Name. " +
                     "Please check the value for the property " + DS_HOST);
             throw new AMTuneException("Invalid value for " + DS_HOST);
         }
     }
     
     public String getDsHost() {
         return dsHost;
     }
     
     /**
      * Set Directory server port.
      * @param dsPort
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private void setDsPort(String dsPort) 
     throws AMTuneException {
         if (dsPort != null && dsPort.trim().length() > 0) {
             this.dsPort = dsPort.trim();
         } else {
             mWriter.writeLocaleMsg("pt-inval-val-msg");
             AMTuneUtil.printErrorMsg(DS_PORT);
             pLogger.log(Level.SEVERE, "setDsPort", 
                     "Error setting Directory Server Port. " +
                     "Please check the value for the property " + DS_PORT);
             throw new AMTuneException("Invalid value for " + DS_PORT);
         }
     }
     
     public String getDsPort() {
         return dsPort;
     }
     
     /**
      * Set Root suffix.
      * @param rootSuffix
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private void setRootSuffix(String rootSuffix) 
     throws AMTuneException {
         if (rootSuffix != null && rootSuffix.trim().length() > 0) {
             this.rootSuffix = rootSuffix.trim();
         } else {
             mWriter.writeLocaleMsg("pt-inval-val-msg");
             AMTuneUtil.printErrorMsg(ROOT_SUFFIX);
             pLogger.log(Level.SEVERE, "setRootSuffix",
                     "Error setting Root Suffix. " +
                     "Please check the value for the property " + ROOT_SUFFIX);
             throw new AMTuneException("Invalid value for " + ROOT_SUFFIX);
         }
     }
     
     public String getRootSuffix() {
         return rootSuffix;
     }
     
     /**
      * Set Directory server Manager UID.
      * @param dirMgrUid
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private void setDirMgrUid(String dirMgrUid) 
     throws AMTuneException {
         if (dirMgrUid != null && dirMgrUid.trim().length() > 0) {
             this.dirMgrUid = dirMgrUid.trim();
         } else {
             mWriter.writeLocaleMsg("pt-inval-val-msg");
             AMTuneUtil.printErrorMsg(DIRMGR_UID);
             pLogger.log(Level.SEVERE, "setDirMgrUid", 
                     "Error setting Directory Managre UID. " +
                     "Please check the value for the property " + DIRMGR_UID);
             throw new AMTuneException("Invalid value for " + DIRMGR_UID);
         }
     }
     public String getDirMgrUid() {
         return dirMgrUid;
     }
     
     /**
      * Set Directory server version.
      * @param dsVersion
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private void setDsVersion(String dsVersion) 
     throws AMTuneException { 
         if (dsVersion != null && dsVersion.trim().length() > 0) {
             this.dsVersion = dsVersion.trim();
         } else {
             mWriter.writeLocaleMsg("pt-inval-val-msg");
             AMTuneUtil.printErrorMsg(DS_VERSION);
             pLogger.log(Level.SEVERE, "setDsVersion", 
                     "Error setting Directory server version " +
                     "Please check the value for " + DS_VERSION);
             throw new AMTuneException("Invalid value for " + DS_VERSION);
         }
     }
     
     public String getDsVersion() {
         return dsVersion;
     }
     
     /**
      * Set Directory server version.
      * @param dsVersion
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private void setSMDSVersion(String dsVersion) 
     throws AMTuneException { 
         if (dsVersion != null && dsVersion.trim().length() > 0) {
             this.dsVersion = dsVersion.trim();
             if (!AMTuneUtil.isSupportedSMDSVersion(dsVersion)) {
                 throw new AMTuneException("Unsupported SM DS Version" + 
                         SM_DS_VERSION);
             }
         } else {
             mWriter.writeLocaleMsg("pt-inval-val-msg");
             AMTuneUtil.printErrorMsg(SM_DS_VERSION);
             pLogger.log(Level.SEVERE, "setSMDSVersion", 
                     "Error setting Directory server version " +
                     "Please check the value for " + SM_DS_VERSION);
             throw new AMTuneException("Invalid value for " + SM_DS_VERSION);
         }
     }
     
     /**
      * Set Directory server Manager UID.
      * @param dirMgrUid
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private void setSMDirMgrUid(String dirMgrUid) 
     throws AMTuneException {
         if (dirMgrUid != null && dirMgrUid.trim().length() > 0) {
             this.dirMgrUid = dirMgrUid.trim();
         } else {
             mWriter.writeLocaleMsg("pt-inval-val-msg");
             AMTuneUtil.printErrorMsg(SM_DS_DIRMGR_UID);
             pLogger.log(Level.SEVERE, "setSMDirMgrUid", 
                     "Error setting Directory Managre UID. " +
                     "Please check the value for the property " + 
                     SM_DS_DIRMGR_UID);
             throw new AMTuneException("Invalid value for " + 
                     SM_DS_DIRMGR_UID);
         }
     }
     /**
      * Set Root suffix.
      * @param rootSuffix
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private void setSMRootSuffix(String rootSuffix) 
     throws AMTuneException {
         if (rootSuffix != null && rootSuffix.trim().length() > 0) {
             this.rootSuffix = rootSuffix.trim();
         } else {
             mWriter.writeLocaleMsg("pt-inval-val-msg");
             AMTuneUtil.printErrorMsg(SM_ROOT_SUFFIX);
             pLogger.log(Level.SEVERE, "setSMRootSuffix",
                     "Error setting Root Suffix. " +
                     "Please check the value for the property " + 
                     SM_ROOT_SUFFIX);
             throw new AMTuneException("Invalid value for " + SM_ROOT_SUFFIX);
         }
     }
     
     /**
      * Set Directory Server administrator password.
      * @param dsDirMgrPassword
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private void setSMDSDirMgrPassword(String dsDirMgrPassword) 
     throws AMTuneException {
         if (dsDirMgrPassword != null && dsDirMgrPassword.trim().length() > 0) {
             this.dsDirMgrPassword = dsDirMgrPassword.trim();
         } else {
             mWriter.writeLocaleMsg("pt-inval-val-msg");
             AMTuneUtil.printErrorMsg(SM_DIRMGR_PASSWORD);
             pLogger.log(Level.SEVERE, "setDsDirMgrPassword", 
                     "Error setting Directory Server Manager Password. " +
                     "Please check the value for the property " + 
                     SM_DIRMGR_PASSWORD);
            if(dsVersion.equalsIgnoreCase(OPEN_DS)) {
                mWriter.writelnLocaleMsg("pt-fam-password-same");
            }
             throw new AMTuneException("Invalid value for " + 
                     SM_DIRMGR_PASSWORD);
         }
     }
     
     /**
      * Set directory Server instance Directory.
      * @param dsInstanceDir
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private void setSMDSInstanceDir(String dsInstanceDir) 
     throws AMTuneException {
         if (dsInstanceDir != null && dsInstanceDir.trim().length() >0){
             File dirTest = new File(dsInstanceDir);
             if (dirTest.isDirectory()) {
                 this.dsInstanceDir = dsInstanceDir.trim();
             } else {
                 mWriter.writelnLocaleMsg("pt-not-valid-dir");
                 AMTuneUtil.printErrorMsg(SM_DS_INSTANCE_DIR);
                 pLogger.log(Level.SEVERE, "setSMDSInstanceDir", 
                         "Directory instance path is not valid directory");
                 throw new AMTuneException("Invalid Directory Path.");
             }
         } else {
             mWriter.writeLocaleMsg("pt-inval-val-msg");
             AMTuneUtil.printErrorMsg(SM_DS_INSTANCE_DIR);
             pLogger.log(Level.SEVERE, "setSMDSInstanceDir", 
                         "Error setting Directory Instance Path. " +
                         "Please check the value for " + SM_DS_INSTANCE_DIR);
                 throw new AMTuneException("Invalid Directory Path.");
         }
     }
     
     /**
      * Set Directory Server Host name.
      * @param dsHost
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private void setSMDSHost(String dsHost) 
     throws AMTuneException {
         if (dsHost != null && dsHost.trim().length() > 0) {
             this.dsHost = dsHost.trim();
         } else {
             mWriter.writeLocaleMsg("pt-inval-val-msg");
             AMTuneUtil.printErrorMsg(SM_DS_HOST);
             pLogger.log(Level.SEVERE, "setSMDSHost", 
                     "Error setting Directory Server Host Name. " +
                     "Please check the value for the property " + SM_DS_HOST);
             throw new AMTuneException("Invalid value for " + SM_DS_HOST);
         }
     }
     
     /**
      * Set Directory server port.
      * @param dsPort
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private void setSMDSPort(String dsPort) 
     throws AMTuneException {
         if (dsPort != null && dsPort.trim().length() > 0) {
             this.dsPort = dsPort.trim();
         } else {
             mWriter.writeLocaleMsg("pt-inval-val-msg");
             AMTuneUtil.printErrorMsg(SM_DS_PORT);
             pLogger.log(Level.SEVERE, "setSMDSPort", 
                     "Error setting Directory Server Port. " +
                     "Please check the value for the property " + SM_DS_PORT);
             throw new AMTuneException("Invalid value for " + SM_DS_PORT);
         }
     }
     
     /**
      * Set DSEE 6.X bin directory.
      * @param dsToolsBinDir
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private void setDSToolsBinDir(String dsToolsDir) 
     throws AMTuneException {
         if (dsToolsDir != null && dsToolsDir.trim().length() > 0) {
             this.dsToolsDir = dsToolsDir.trim();
             File dir = new File(dsToolsDir);
             if (!dir.isDirectory()) {
                 mWriter.writelnLocaleMsg("pt-not-valid-dir");
                 AMTuneUtil.printErrorMsg(DS_TOOLS_DIR);
                 pLogger.log(Level.SEVERE, "setDS6BinDir",
                         "Invalid Bin Directory Path. " +
                         "Please check the value for " + DS_TOOLS_DIR);
                 throw new AMTuneException("Invalid directory path for " +
                         DS_TOOLS_DIR);
             }
         } else {
             mWriter.writeLocaleMsg("pt-inval-val-msg");
             AMTuneUtil.printErrorMsg(DS_TOOLS_DIR);
             pLogger.log(Level.SEVERE, "setDS6BinDir",
                     "Error setting Directory 6 bin directory " +
                     "Please check the value for " + DS_TOOLS_DIR);
             throw new AMTuneException("Invalid value for " + DS_TOOLS_DIR);
         }
     }
     
     public String getDSToolsBinDir() {
         return dsToolsDir;
     }
     
     /**
      * Set DSEE 6.X bin directory.
      * @param dsToolsBinDir
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private void setSMDSToolsBinDir(String dsToolsDir) 
     throws AMTuneException {
         if (dsToolsDir != null && dsToolsDir.trim().length() > 0) {
             this.dsToolsDir = dsToolsDir.trim();
             File dir = new File(dsToolsDir);
             if (!dir.isDirectory()) {
                 mWriter.writelnLocaleMsg("pt-not-valid-dir");
                 AMTuneUtil.printErrorMsg(SM_DS_TOOLS_DIR);
                 pLogger.log(Level.SEVERE, "setSMDSToolsBinDir",
                         "Invalid Bin Directory Path. " +
                         "Please check the value for " + SM_DS_TOOLS_DIR);
                 throw new AMTuneException("Invalid directory path for " +
                         SM_DS_TOOLS_DIR);
             }
         } else {
             mWriter.writeLocaleMsg("pt-inval-val-msg");
             AMTuneUtil.printErrorMsg(SM_DS_TOOLS_DIR);
             pLogger.log(Level.SEVERE, "setDS6BinDir",
                     "Error setting Directory 6 bin directory " +
                     "Please check the value for " + SM_DS_TOOLS_DIR);
             throw new AMTuneException("Invalid value for " + SM_DS_TOOLS_DIR);
         }
     }
     
     private void setPerlBinDir(String perlBinDir) {
         this.perlBinDir = perlBinDir;
     }
     
     public String getPerlBinDir() {
         return perlBinDir;
     }
 
     public boolean isRemoteDS() {
         return isRemoteDS;
     }
 }
