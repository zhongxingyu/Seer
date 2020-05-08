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
 * $Id: FAMUpgrade.java,v 1.3 2008-04-14 23:17:31 bina Exp $
  *
  * Copyright 2008 Sun Microsystems Inc. All Rights Reserved
  */
 package com.sun.identity.upgrade;
 
 import com.iplanet.am.util.SystemProperties;
 import com.sun.identity.setup.Bootstrap;
 import com.sun.identity.shared.debug.Debug;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PushbackInputStream;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 
 /**
  * This class contains methods to start the upgrade process.
  * 
  */
 public class FAMUpgrade {
 
     final static String UPGRADE_CONFIG_FILE = "famUpgradeConfig.properties";
     final static String AMADMIN_USER_PROPERTY = 
         "com.sun.identity.authentication.super.user";
     final static String NEW_DIR = "s_10";
     final static String DEFAULT_VERSION = "10";
     final static String CONFIG_DIR = "configDir";
     static BufferedReader inbr = null;
     static String dsHost = "";
     static String dsPort = "";
     static String dirMgrDN = "cn=Directory Manager";
     static String dirMgrPass;
     static String amAdminPass;
     static String amAdminUser;
     static String configDir;
     static boolean enableRealms = false;
     static Debug debug = Debug.getInstance("famUpgrade");
     static String basedir = null;
     static String stagingDir = null;
 
     /**
      * Start of upgrade 
      * 
      *  TODO : 
      * 1. Localize the messages 
      * 2. Log the message
      * 3. Constants should be defined at the top.
      * 
      */
     public static void main(String args[]) {
         try {
             System.out.println("Welcome to FAM 8.0 upgrade");
             FAMUpgrade famUpgrade = new FAMUpgrade();
             famUpgrade.bootStrapNow();
             famUpgrade.initVariables();
             UpgradeUtils.setBindDN(amAdminUser);
             UpgradeUtils.setBindPass(amAdminPass);
             UpgradeUtils.setDSHost(dsHost);
             UpgradeUtils.setDirMgrDN(dirMgrDN);
             UpgradeUtils.setDSPort(new Integer(dsPort).intValue());
             UpgradeUtils.setdirPass(dirMgrPass);
             UpgradeUtils.setBaseDir(basedir);
             UpgradeUtils.setStagingDir(stagingDir);
             UpgradeUtils.setConfigDir(configDir);
             if (UpgradeUtils.isRealmMode()) {
                 System.out.println("This is Realm Mode");
             //invoke migrateToRealm
             } else {
                 System.out.println("This is Legacy Mode");
                 System.out.print("Enable Realm Mode: [y/n]");
                 enableRealms = new Boolean(readInput()).booleanValue();
                 System.out.println("isRealmEnabled: " + enableRealms);
             }
             // replace tags
             // load the properties
             Properties properties =
                     UpgradeUtils.getProperties(basedir +
                     File.separator + "upgrade" + File.separator +
                     "config" + File.separator + UPGRADE_CONFIG_FILE);
             updateProperties(properties);
             UpgradeUtils.setProperties(properties);
             String dir = getDir();
             UpgradeUtils.replaceTags(new File(dir), properties);
             copyNewXMLFiles();
             famUpgrade.startUpgrade();
             // Migrate to realms
             if (enableRealms) {
                 // migrate to realm mode
                 UpgradeUtils.doMigration70();
             }
         } catch (Exception e) {
             System.out.println("Error in main: " + e);
             e.printStackTrace();
         }
         System.exit(0);
     }
 
     /**
      * Returns the upgrade services path 
      */
     static String getDir() {
         // xml files under the upgrade directory
         return new StringBuffer().append(basedir)
                 .append(File.separator)
                 .append("upgrade")
                 .append(File.separator)
                 .append("services").toString();
     }
 
     /**
      * Loads the AMConfig.properties
      */
     void bootStrapNow() {
         try {
             if (configDir == null) {
                 configDir = System.getProperty(CONFIG_DIR);
             }
            Bootstrap.load(configDir,true);
         } catch (Exception e) {
             System.err.println("Cannot bootstrap the system" + e.getMessage());
             System.exit(1);
         }
     }
 
    /**
     * Intializes required input parameters.
     */
     public void initVariables() {
         inbr = new BufferedReader(new InputStreamReader(System.in));
         // directory where fam.zip is unzipped
         getBaseDir();
         // the cofiguration directory
         getConfigDir();
         // the staging directory
         getStagingDir();
         getDSInfo();
         getDirManagerInfo();
         getAMAdminInfo();
     }
 
     /**
      * Gets the default FAM configuration directory.
      */
     void getConfigDir() {
         System.out.print("Enter the FAM config directory : ");
         String temp = readInput();
         if (temp != null && temp.length() > 0) {
             configDir = temp;
         }
     }
 
     /**
      * Gets the location of the base directory.
      */
     void getBaseDir() {
         System.out.print("Enter the Upgrade Base Directory : ");
         String temp = readInput();
         if (temp != null && temp.length() > 0) {
             basedir = temp;
         }
     }
 
     /**
      * Gets the location of the Staging directory.
      */
     void getStagingDir() {
         System.out.print("Enter the FAM staging directory : ");
         String temp = readInput();
         if (temp != null && temp.length() > 0) {
             stagingDir = temp;
         }
     }
 
     /**
      * Gets the Directory Server hostname.
      */
     void getDSInfo() {
         System.out.print("Directory Server fully-qualified hostname [");
         System.out.print(dsHost);
         System.out.print("] :");
         String temp = readInput();
         if (temp != null && temp.length() > 0) {
             dsHost = temp;
         }
         System.out.print("Directory Server port :");
         temp = readInput();
         if (temp != null && temp.length() > 0) {
             dsPort = temp;
         }
     }
 
     /**
      * Gets Directory Manager DN password
      */
     private void getDirManagerInfo() {
         System.out.print("Directory Manager DN [");
         System.out.print(dirMgrDN);
         System.out.print("] : ");
         String temp = dirMgrDN;
         dirMgrDN = readInput();
         if (dirMgrDN != null && dirMgrDN.length() == 0) {
             dirMgrDN = temp;
         }
         try {
             char[] dirMgrPassChar =
             getPassword(System.in, "Directory Manager Password : ");
             dirMgrPass = String.valueOf(dirMgrPassChar);
         } catch (IOException ioe) {
             System.out.println("Error " + ioe.getMessage());
         }
     }
 
     /**
      * Gets the amAdmin user and password.
      */
     private void getAMAdminInfo() {
         String classMethod = "FAMUpgrade:getAMAdminInfo :";
         amAdminUser = SystemProperties.get(AMADMIN_USER_PROPERTY);
         System.out.print("Enter FAM Admin User DN [");
         System.out.print(amAdminUser);
         System.out.print("] :");
         String temp = readInput();
         if (temp != null && temp.length() > 0) {
             amAdminUser = temp;
         }
 
         try {
             char[] amAdminPassChar =
                     getPassword(System.in, "Enter FAM Admin User Password : ");
             amAdminPass = String.valueOf(amAdminPassChar);
         } catch (IOException ioe) {
             debug.error(classMethod + "Error : " ,ioe);
         }
     }
 
 
     /** 
      * Upgrades the services schema for different services .
      */
     public void startUpgrade() {
         System.out.println("Starting Upgrade: ");
         String servicesDir = basedir + File.separator 
                 + "upgrade" + File.separator + "services";
         // get file list
         File fileList = new File(servicesDir);
         String[] list = fileList.list();
         List listArray = Arrays.asList(list);
         Collections.sort(listArray, String.CASE_INSENSITIVE_ORDER);
         Iterator aa = listArray.iterator();
         while (aa.hasNext()) {
             String value = (String) aa.next();
             System.out.println(value);
             String serviceName = null;
             int in = value.indexOf("_");
             if (in != -1) {
                 serviceName = value.substring(in + 1);
             }
             // serviceName
             System.out.println("*********************************************");
             System.out.println("Migrating Service Name: " + serviceName);
             File fileL = new File(servicesDir + File.separator + value);
             String[] ll = fileL.list();
             List lArray = Arrays.asList(ll);
             Collections.sort(lArray, String.CASE_INSENSITIVE_ORDER);
             Iterator ab = lArray.iterator();
 
             int currentVersion = UpgradeUtils.getServiceRevision(serviceName);
             String currentRev = new Integer(currentVersion).toString();
 
             boolean newService = false;
             boolean isSuccess = false;
             List migrateList = new ArrayList();
             for (int k = 0; k < lArray.size(); k++) {
                 if (currentVersion != -1) {
                     String dir = (String) lArray.get(k);
                     if (dir.startsWith(currentRev)) {
                         migrateList = lArray.subList(k, lArray.size());
                         break;
                     }
                 } else {
                     migrateList.addAll(lArray);
                     if (lArray.size() == 1) {
                         newService = true;
                     }
                     migrateList.remove(NEW_DIR);
                     break;
                 }
             }
             Collections.sort(migrateList);
             boolean isNew = false;
             if (currentVersion != -1) {
                 System.out.println(serviceName + " :Current Service Revision :"+
                         currentVersion);
             } else {
                 System.out.println("New Service : " + serviceName );
                 isNew = true;
             }
 
             Iterator fileIterator = migrateList.iterator();
             // iterate through the service dirs.
             String fromVer = "";
             String endVer = "";
             try {
                 while (fileIterator.hasNext() || isNew) {
                     String dirName = null;
                     if (isNew) {
                         dirName = NEW_DIR;
                         endVer = DEFAULT_VERSION;
                         isNew = false;
                     } else {
                         isNew = false;
                         dirName = (String) fileIterator.next();
                         int index = dirName.indexOf("_");
                         if (index != -1) {
                             fromVer = dirName.substring(0, index);
                             endVer = dirName.substring(
                                     index + 1, dirName.length());
                         }
                     }
                     isSuccess = false;
                     String urlString = new StringBuffer().append("file:///")
                             .append(servicesDir)
                             .append(File.separator)
                             .append(value)
                             .append(File.separator)
                             .append(dirName)
                             .append(File.separator).toString();
                     URL url1 = new URL(urlString);
                     urlString = new StringBuffer()
                             .append("file:///")
                             .append(basedir)
                             .append(File.separator)
                             .append("upgrade")
                             .append(File.separator)
                             .append("lib")
                             .append(File.separator)
                             .append("upgrade.jar").toString();
                     URL url2 = new URL(urlString);
                     URL[] urls = {url1, url2};
                     URLClassLoader cLoader = new URLClassLoader(urls);
                     MigrateTasks mClass =
                             (MigrateTasks) 
                             cLoader.loadClass("Migrate").newInstance();
                     if (mClass.preMigrateTask() &&
                             mClass.migrateService() &&
                             mClass.postMigrateTask()) {
                         isSuccess = true;
                     }
                 } // while rev dirs.
                 if (isSuccess && !newService) {
                     UpgradeUtils.setServiceRevision(serviceName, endVer);
                 }
             } catch (Exception e) {
                 System.out.println("Error :" + e.getMessage());
                 e.printStackTrace();
             }
         }
     }
 
     /**
      * Reads the input from the user
      */
     private static String readInput() {
         String input = "";
         try {
             input = inbr.readLine();
         } catch (IOException ioe) {
             System.out.print("Error while reading input IOException :" +
                     ioe.getMessage());
         }
         return input;
     }//End of readInput method
     
     /**
      * Returns a list of files in a directory.
      * 
      * @param dirName the directory name
      * @param fileList the file list to be retrieved.
      */
     public static void getFiles(File dirName,
             LinkedList fileList) {
         File[] fromFiles = dirName.listFiles();
         for (int i = 0; i < fromFiles.length; i++) {
             fileList.addLast(fromFiles[i]);
             if (fromFiles[i].isDirectory()) {
                 getFiles(fromFiles[i], fileList);
             }
         }
     }
 
     
     /**
      * Makes a copy of new xml files for tag swapping.
      */
     static void copyNewXMLFiles() {
         String classMethod = "FAMUgprade:copyNewXMLFiles";
         String upgradeNewXMLDir =
                 basedir + File.separator + "upgrade" + File.separator + "xml";
 
         try {
             // create directory upgrade/xml
             File f = new File(upgradeNewXMLDir);
             f.mkdir();
             // copy all files from xml directory to upgrade/xml directory.
             File xmlF = new File(basedir + File.separator + "xml");
             String[] xmlfileList = xmlF.list();
             for (int i = 0; i < xmlfileList.length; i++) {
                 File source = new File(basedir + File.separator +
                         "xml" + File.separator + xmlfileList[i]);
                 String name = source.getName();
                 File target =
                         new File(upgradeNewXMLDir + File.separator + name);
                 copyFile(source, target);
             }
         } catch (Exception e) {
             UpgradeUtils.debug.error(classMethod + 
                     "Error copying new xmls" ,e );
         }
     }
 
     /**
      * Makes a copy of a file.
      */
     static void copyFile(File in, File out) throws Exception {
         try {
             FileInputStream fis = new FileInputStream(in);
             FileOutputStream fos = new FileOutputStream(out);
             byte[] buf = new byte[1024];
             int i = 0;
             while ((i = fis.read(buf)) != -1) {
                 fos.write(buf, 0, i);
             }
             fis.close();
             fos.close();
 
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Properties to be tagswapped.
      */
     private static void updateProperties(Properties p) {
         p.put("@XML_COMMENT_START@","");
         p.put("@XML_COMMENT_END@","");
         p.put("ROOT_SUFFIX",UpgradeUtils.getRootSuffix());
         p.put("SM_CONFIG_BASEDN",UpgradeUtils.getRootSuffix());
         p.put("NORMALIZED_ORGBASE",UpgradeUtils.getNormalizedRootSuffix());
         p.put("NORMALIZED_RS",UpgradeUtils.getNormalizedRootSuffix());
         p.put("DIRECTORY_SERVER",dsHost);
         p.put("DIRECTORY_PORT",dsPort);
         p.put("People_NM_ORG_ROOT_SUFFIX",
                 UpgradeUtils.getPeopleOrgRootSuffix());
         p.put("ORG_ROOT_SUFFIX",UpgradeUtils.getNormalizedRootSuffix());
         p.put("RSUFFIX_HAT",UpgradeUtils.getRootSuffix().replaceAll(",", "^"));
     }
     
     /**
      * Returns the password .
      * 
      * @param in the <code>InputStream</code>
      * @param prompt the prompt message
      * @return the password as a character array.
      * @throws java.io.IOException if there is an error.
      */
     public final char[] getPassword(InputStream in,
             String prompt) throws IOException {
         MaskingThread maskingthread = new MaskingThread(prompt);
         Thread thread = new Thread(maskingthread);
         thread.start();
 
         char[] lineBuffer;
         char[] buf;
         int i;
 
         buf = lineBuffer = new char[128];
         int room = buf.length;
         int offset = 0;
         int c;
 
         loop:
         while (true) {
             switch (c = in.read()) {
                 case -1:
                 case '\n':
                     break loop;
                 case '\r':
                     int c2 = in.read();
                     if ((c2 != '\n') && (c2 != -1)) {
                         if (!(in instanceof PushbackInputStream)) {
                             in = new PushbackInputStream(in);
                         }
                         ((PushbackInputStream) in).unread(c2);
                     } else {
                         break loop;
                     }
                 default:
                     if (--room < 0) {
                         buf = new char[offset + 128];
                         room = buf.length - offset - 1;
                         System.arraycopy(lineBuffer, 0, buf, 0, offset);
                         Arrays.fill(lineBuffer, ' ');
                         lineBuffer = buf;
                     }
                     buf[offset++] = (char) c;
                     break;
             }
         }
         maskingthread.stopMasking();
         if (offset == 0) {
             return null;
         }
         char[] ret = new char[offset];
         System.arraycopy(buf, 0, ret, 0, offset);
         Arrays.fill(buf, ' ');
         return ret;
     }
 
     /**
      * This class attempts to erase characters echoed to the console.
      */
     class MaskingThread extends Thread {
 
         private volatile boolean stop;
         private char echochar = '*';
 
         /**
          *@param prompt The prompt displayed to the user
          */
         public MaskingThread(String prompt) {
             System.out.print(prompt);
         }
 
         /**
          * Begin masking until asked to stop.
          */
         public void run() {
 
             int priority = Thread.currentThread().getPriority();
             Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
 
             try {
                 stop = true;
                 while (stop) {
                     System.out.print("\010" + echochar);
                     try {
                         // attempt masking at this rate
                         Thread.currentThread().sleep(1);
                     } catch (InterruptedException iex) {
                         Thread.currentThread().interrupt();
                         return;
                     }
                 }
             } finally { // restore the original priority
                 Thread.currentThread().setPriority(priority);
             }
         }
 
         /**
          * Instruct the thread to stop masking.
          */
         public void stopMasking() {
             this.stop = false;
         }
     }
 }
