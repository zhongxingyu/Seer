 /**
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright (c) 2008 Sun Microsystems Inc. All Rights Reserved
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
 * $Id: CreateFedlet.java,v 1.11 2009-02-18 23:44:09 asyhuang Exp $
  *
  */
 
 package com.sun.identity.workflow;
 
 import com.iplanet.am.util.SystemProperties;
 import com.sun.identity.cot.COTException;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.ResourceBundle;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.jar.JarEntry;
 import java.util.jar.JarInputStream;
 import java.util.jar.JarOutputStream;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 import javax.servlet.ServletContext;
 
 /**
  *  * Creates Fedlet.
  *   */
 public class CreateFedlet
     extends Task
 {
     private static Map fedletBits = new HashMap();
     private static Map FedConfigTagSwap = new HashMap();
     private static List FedConfigTagSwapOrder = new ArrayList();
     private static Map jarExtracts = new HashMap();
     
     static {
         FedConfigTagSwap.put("@CONFIGURATION_PROVIDER_CLASS@", 
             "com.sun.identity.plugin.configuration.impl.FedletConfigurationImpl");
         FedConfigTagSwap.put("@DATASTORE_PROVIDER_CLASS@",
             "com.sun.identity.plugin.datastore.impl.FedletDataStoreProvider");
         FedConfigTagSwap.put("@LOG_PROVIDER_CLASS@",
             "com.sun.identity.plugin.log.impl.FedletLogger");
         FedConfigTagSwap.put("@SESSION_PROVIDER_CLASS@", 
             "com.sun.identity.plugin.session.impl.FedletSessionProvider");
         FedConfigTagSwap.put("@XML_SIGNATURE_PROVIDER@",
             "com.sun.identity.saml.xmlsig.AMSignatureProvider");
         FedConfigTagSwap.put("@XMLSIG_KEY_PROVIDER@", 
             "com.sun.identity.saml.xmlsig.JKSKeyProvider");
         FedConfigTagSwap.put("%BASE_DIR%%SERVER_URI%", "@FEDLET_HOME@");
         FedConfigTagSwap.put("%BASE_DIR%", "@FEDLET_HOME@"); 
         FedConfigTagSwap.put("com.sun.identity.common.serverMode=true",
             "com.sun.identity.common.serverMode=false");
         FedConfigTagSwap.put("@SERVER_PROTO@", "http");
         FedConfigTagSwap.put("@SERVER_HOST@", "example.identity.sun.com");
         FedConfigTagSwap.put("@SERVER_PORT@", "80");
         FedConfigTagSwap.put("/@SERVER_URI@", "/fedlet");
         
         FedConfigTagSwapOrder.add("@CONFIGURATION_PROVIDER_CLASS@"); 
         FedConfigTagSwapOrder.add("@DATASTORE_PROVIDER_CLASS@");
         FedConfigTagSwapOrder.add("@LOG_PROVIDER_CLASS@");
         FedConfigTagSwapOrder.add("@SESSION_PROVIDER_CLASS@"); 
         FedConfigTagSwapOrder.add("@XML_SIGNATURE_PROVIDER@");
         FedConfigTagSwapOrder.add("@XMLSIG_KEY_PROVIDER@");
         FedConfigTagSwapOrder.add("%BASE_DIR%%SERVER_URI%");
         FedConfigTagSwapOrder.add("%BASE_DIR%");
         FedConfigTagSwapOrder.add("com.sun.identity.common.serverMode=true");
         FedConfigTagSwapOrder.add("@SERVER_PROTO@");
         FedConfigTagSwapOrder.add("@SERVER_HOST@");
         FedConfigTagSwapOrder.add("@SERVER_PORT@");
         FedConfigTagSwapOrder.add("/@SERVER_URI@");
         
         ResourceBundle rb = ResourceBundle.getBundle("fedletBits");
         for (Enumeration e = rb.getKeys(); e.hasMoreElements(); ) {
             String k = (String)e.nextElement();
             fedletBits.put(k, rb.getObject(k));
         }
         
         rb = ResourceBundle.getBundle("fedletJarExtract");
         for (Enumeration e = rb.getKeys(); e.hasMoreElements(); ) {
             String jarName = (String)e.nextElement();
             String pkgNames = rb.getString(jarName);
             StringTokenizer st = new StringTokenizer(pkgNames, ",");
             Set set = new HashSet();
             while (st.hasMoreElements()) {
                 set.add(st.nextToken().trim());
             }
             jarExtracts.put(jarName, set);
         }
         
     }
     
     public CreateFedlet() {
     }
 
     public String execute(Locale locale, Map params)
         throws WorkflowException {
         validateParameters(params);
         String entityId = getString(params, ParameterKeys.P_ENTITY_ID);
         Pattern patern = Pattern.compile("[/:\\.?|*]");
         Matcher match = patern.matcher(entityId);
         String folderName = match.replaceAll("");
         String workDir = SystemProperties.get(SystemProperties.CONFIG_PATH) +
             "/myfedlets/" + folderName;
         File dir = new File(workDir);
         if (dir.exists()) {
             Object[] param = {workDir};
             throw new WorkflowException("directory.already.exist", param);
         }
         
         if (!dir.getParentFile().exists())  {
             dir.getParentFile().mkdir();
         }
         dir.mkdir();
         String warDir = workDir + "/war"; 
         dir = new File(warDir);
         dir.mkdir();
         String confDir = warDir + "/conf";
         dir = new File(confDir);
         dir.mkdir();
         
         loadMetaData(params, confDir);
         exportIDPMetaData(params, confDir);
         createCOTProperties(params, confDir);
         
         ServletContext servletCtx = (ServletContext)params.get(
             ParameterKeys.P_SERVLET_CONTEXT);
         copyBits(servletCtx, workDir);
         extractJars(servletCtx, warDir);
         createFederationConfigProperties(servletCtx, confDir);
         createWar(workDir);
         String zipFileName = createZip(workDir);
         
         Object[] param = {zipFileName};
         return MessageFormat.format(
             getMessage("Fedlet.created", locale), param);
     }
     
     private void copyBits(ServletContext servletCtx, String workDir)
         throws WorkflowException {
         String dir = workDir + "/war";
         (new File(dir)).mkdir();
         
         copyFile(servletCtx, "/WEB-INF/fedlet/README", workDir + "/README");
 
         for (Iterator i = fedletBits.keySet().iterator(); i.hasNext(); ) {
             String file = (String)i.next();
             String target = (String)fedletBits.get(file);
             if ((target == null) || (target.trim().length() == 0)) {
                 target = file;
             }
 
             copyFile(servletCtx, file, dir + "/" + target);
         }
     }
     
     private void extractJars(ServletContext servletCtx, String workDir)
         throws WorkflowException {
         for (Iterator i = jarExtracts.keySet().iterator(); i.hasNext(); ) {
             JarInputStream jis = null;
             JarOutputStream zipOutputStream = null;
             
             try {
                 String fileName = (String) i.next();
                 Set pkgNames = (Set) jarExtracts.get(fileName);
                 InputStream is = servletCtx.getResourceAsStream(fileName);
                 jis = new JarInputStream(is);
                 FileOutputStream fileOutputStream = new FileOutputStream(
                     workDir + fileName);
                 zipOutputStream = new JarOutputStream(fileOutputStream);
                 JarEntry entry = jis.getNextJarEntry();
 
                 while (entry != null) {
                     int size = (new Long(entry.getSize())).intValue();
                     if (size > 0) {
                         String name = entry.getName();
                         boolean bExtract = false;
                         for (Iterator j = pkgNames.iterator(); 
                             (j.hasNext() && !bExtract); 
                         ) {
                             bExtract = name.startsWith((String)j.next());
                         }
                         if (bExtract) {
                             zipOutputStream.putNextEntry(entry);
                             byte[] b = new byte[size];
                             int tobeRead = size;
                             while (true) {
                                 int readSize = jis.read(b, size - tobeRead, tobeRead);
                                 if (readSize == tobeRead) {
                                     break;
                                 } else {
                                     tobeRead -= readSize;
                                 }
                             }
                             zipOutputStream.write(b);
                         }
                     }
                     entry = jis.getNextJarEntry();
                 }
             } catch (IOException ex) {
                 throw new WorkflowException(ex.getMessage());
             } finally {
                 try {
                     if (jis != null) {
                         jis.close();
                     }
                     if (zipOutputStream != null) {
                         zipOutputStream.close();
                     }
                 } catch (IOException ex) {
                     //ignore
                 }
             }
         }
     }
     
     private void copyFile(ServletContext servletCtx, String source, String dest)
         throws WorkflowException {
         File test = new File(dest);
         File parent = test.getParentFile();
         if (!parent.exists()) {
             parent.mkdirs();
         }
         
         FileOutputStream fos = null;
         InputStream src = null;
         try {
             src = servletCtx.getResourceAsStream(source);
             if (src != null) {
                 fos = new FileOutputStream(dest);
                 int length = 0;
                 byte[] bytes = new byte[1024];
                 while ((length = src.read(bytes)) != -1) {
                     fos.write(bytes, 0, length);
                 }
             } else {
                 Object[] param = {source};
                 throw new WorkflowException("file-not-found", param);
             }
         } catch (IOException e) {
             throw new WorkflowException(e.getMessage());
         } finally {
             try {
                 if (fos != null) {
                     fos.close();
                 }
                 if (src != null) {
                     src.close();
                 }
             } catch (IOException ex) {
                 //ignore
             }
         }
     }
     
     private void createCOTProperties(Map params, String workDir)
         throws WorkflowException {
         String sp = getString(params, ParameterKeys.P_ENTITY_ID);
         String idp = getString(params, ParameterKeys.P_IDP);
         String cot = getString(params, ParameterKeys.P_COT);
 
         String content =
             "cot-name=" + cot + "\n" +
             "sun-fm-cot-status=Active\n" +
             "sun-fm-trusted-providers=" + encodeVal(idp) + "," +
             encodeVal(sp) + "\n" +
             "sun-fm-saml2-readerservice-url=\n" +
             "sun-fm-saml2-writerservice-url=\n";
         writeToFile(workDir + "/fedlet.cot", content);
     }
     
     private void exportIDPMetaData(Map params, String workDir)
         throws WorkflowException {
         String realm = getString(params, ParameterKeys.P_REALM);
         String idp = getString(params, ParameterKeys.P_IDP);
         String metadata = ExportSAML2MetaData.exportStandardMeta(
             realm, idp, false);
         String extended = ExportSAML2MetaData.exportExtendedMeta(
             realm, idp);
         
         String extendedModified = flipHostedParameter(extended, false);
         writeToFile(workDir + "/idp-extended.xml", extendedModified);
         writeToFile(workDir + "/idp.xml", metadata);
     }
     
     private void loadMetaData(Map params, String workDir)
         throws WorkflowException {
         String realm = getString(params, ParameterKeys.P_REALM);
         String entityId = getString(params, ParameterKeys.P_ENTITY_ID);
         String cot = getString(params, ParameterKeys.P_COT);
         String assertConsumer = getString(params, 
             ParameterKeys.P_ASSERT_CONSUMER);
         List attrMapping = getAttributeMapping(params);
 
         String metadata = FedletMetaData.createStandardMetaData(entityId, 
             assertConsumer);
         String extended = FedletMetaData.createExtendedMetaData(realm,
             entityId, attrMapping);
         
         ImportSAML2MetaData.importData(realm, metadata, extended);
         if ((cot != null) && (cot.length() > 0)) {
             try {
                 AddProviderToCOT.addToCOT(realm, cot, entityId);
             } catch (COTException e) {
                 throw new WorkflowException(e.getMessage());
             }
             int idx = extended.indexOf("<Attribute name=\"cotlist\">");
             idx = extended.indexOf("</Attribute>", idx);
             extended = extended.substring(0, idx) +
                 "<Value>" + cot + "</Value>" +
                 extended.substring(idx);
         }
         
         String extendedModified = flipHostedParameter(extended, true);
         writeToFile(workDir + "/sp-extended.xml", extendedModified);
         writeToFile(workDir + "/sp.xml", metadata);
     }
 
     private void validateParameters(Map params)
         throws WorkflowException {
         String entityId = getString(params, ParameterKeys.P_ENTITY_ID);
         if ((entityId == null) || (entityId.trim().length() == 0)) {
             throw new WorkflowException("entityId-required", null);
         }
         String assertConsumer = getString(params, 
             ParameterKeys.P_ASSERT_CONSUMER);
         if ((assertConsumer == null) || (assertConsumer.trim().length() == 0)) {
             throw new WorkflowException("assertion.consumer-required", null);
         }
         
         try {
             new URL(assertConsumer);
         } catch (MalformedURLException e) {
             throw new WorkflowException("assertion.consumer-invalid", null);
         }
 
         String cot = getString(params, ParameterKeys.P_COT);
         if ((cot == null) || (cot.trim().length() == 0)) {
             throw new WorkflowException("missing-cot", null);
         }
         
         String realm = getString(params, ParameterKeys.P_REALM);
         if ((realm == null) || (realm.trim().length() == 0)) {
             throw new WorkflowException("missing-realm", null);
         }
     }
     
     private void createFederationConfigProperties(
         ServletContext servletCtx,
         String workDir
     ) throws WorkflowException {
         
         String prop = getBitAsString(servletCtx,
            "/WEB-INF/fedlet/FederationConfig.properties");
         for (Iterator i = FedConfigTagSwapOrder.iterator(); i.hasNext();) {
             String k = (String) i.next();
             String v = (String) FedConfigTagSwap.get(k);
             prop = prop.replaceAll(k, v);
         }
         writeToFile(workDir + "/FederationConfig.properties", prop);
     }
     
     private String getBitAsString(ServletContext servletCtx, String bitName)
         throws WorkflowException {
         InputStream in = null;
         ByteArrayOutputStream out = null;
         try {
             in = servletCtx.getResourceAsStream(bitName);
             out = new ByteArrayOutputStream();
             byte[] buf = new byte[1024];
             int len;
             while ((len = in.read(buf)) > 0) {
                 out.write(buf, 0, len);
             }
             return out.toString();
         } catch (IOException ex) {
             throw new WorkflowException(ex.getMessage());
         } finally {
             try {
                 if (in != null) {
                     in.close();
                 }
                 if (out != null) {
                     out.close();
                 }
             } catch (IOException e) {
                 //ignore
             }
         }
     }
     
     private static void writeToFile(String fileName, String content)
         throws WorkflowException {
         FileWriter fout = null;
         try {
             fout = new FileWriter(fileName);
             fout.write(content);
         } catch (IOException e) {
             throw new WorkflowException(e.getMessage());
         } finally {
             if (fout != null) {
                 try {
                     fout.close();
                 } catch (Exception ex) {
                     //No handling requried
                 }
             }
         }
     }
 
     private static String flipHostedParameter(String xml, boolean bHosted) {
         int idx = xml.indexOf("<EntityConfig ");
         if (idx != -1) {
             idx = xml.indexOf("hosted=\"", idx);
          
             if (idx != -1) {
                 int idx2 = xml.indexOf("\"", idx+9);
                 if (bHosted) {
                     xml = xml.substring(0, idx+8) + "1" + xml.substring(idx2);
                 } else {
                     xml = xml.substring(0, idx+8) + "0" + xml.substring(idx2);
                 }
             }
         }
         return xml;
     }
     
     private void createWar(String workDir) 
         throws WorkflowException {
         JarOutputStream out = null;
         String warDir = workDir + "/war";
         int lenWorkDir = warDir.length() +1;
         List files = getAllFiles(warDir, true);
         String jarName = workDir + "/fedlet.war";
 
         try {
             out = new JarOutputStream(new FileOutputStream(jarName));
             byte[] buf = new byte[1024];
 
             for (Iterator i = files.iterator(); i.hasNext();) {
                 String fname = (String) i.next();
                 FileInputStream in = new FileInputStream(fname);
                 String jarEntryName = fname.substring(lenWorkDir);
                 // use forward slash in jar path to avoid windows issue
                 if (File.separatorChar == '\\') {
                     jarEntryName = jarEntryName.replace('\\', '/');
                 }
                 out.putNextEntry(new JarEntry(jarEntryName));
                 int len;
                 while ((len = in.read(buf)) > 0) {
                     out.write(buf, 0, len);
                 }
                 out.closeEntry();
                 in.close();
             }
 
             deleteAllFiles(warDir, files);
             (new File(warDir)).delete();
         } catch (IOException e) {
             throw new WorkflowException(e.getMessage());
         } finally {
             try {
                 if (out != null) {
                     out.close();
                 }
             } catch (IOException ex) {
             // ignore
             }
         }
     }
     
     private String createZip(String workDir)
         throws WorkflowException {
         int lenWorkDir = workDir.length() +1;
 
         ZipOutputStream out = null;
         try {
             List files = getAllFiles(workDir, true);
             String zipName = workDir + "/Fedlet.zip";
             out = new ZipOutputStream(new FileOutputStream(zipName));
             byte[] buf = new byte[1024];
 
             for (Iterator i = files.iterator(); i.hasNext(); )  {
                 String fname = (String) i.next();
                 FileInputStream in = new FileInputStream(fname);
                 out.putNextEntry(new ZipEntry(fname.substring(lenWorkDir)));
                 int len;
                 while ((len = in.read(buf)) > 0) {
                     out.write(buf, 0, len);
                 }
 
                 // Complete the entry
                 out.closeEntry();
                 in.close();
             }
 
             deleteAllFiles(workDir, files);
             return zipName;
         } catch (IOException e) {
             throw new WorkflowException(e.getMessage());
         } finally {
             try {
                 if (out != null) {
                     out.close();
                 }
             } catch (IOException ex) {
                 // ignore
             }
         }
     }
     
     private void deleteAllFiles(String workDir, List files) {
         for (Iterator i = files.iterator(); i.hasNext(); ) {
             String fname = (String)i.next();
             (new File(fname)).delete();
         }
         
         List dirs = getAllFiles(workDir, false);
         for (int i = dirs.size() -1; i >= 0; --i) {
             String dirName = (String)dirs.get(i);
             File test = new File(dirName);
             if (test.isDirectory()) {
                 test.delete();
             }
         }
     }
     
     private List getAllFiles(String dir, boolean bFileOnly) {
         List list = new ArrayList();
         File directory = new File(dir);
         String[] children = directory.list();
         
         for (int i = 0; i < children.length; i++) {
             String child = dir + "/" + children[i];
             File f = new File(child);
             if (f.isDirectory()) {
                 if (!bFileOnly) {
                     list.add(f.getAbsolutePath());
                 }
                 list.addAll(getAllFiles(f.getAbsolutePath(), bFileOnly));
             } else {
                 list.add(f.getAbsolutePath());
             }
         }
         
         return list;
     }
     
     /**
      * Encodes special characters in a value.
      * percent to %25 and comma to %2C.
      */
     private String encodeVal(String v) {
         char[] chars = v.toCharArray();
         StringBuffer sb = new StringBuffer(chars.length + 20);
         int i = 0, lastIdx = 0;
         for (i = 0; i < chars.length; i++) {
             if (chars[i] == '%') {
                 if (lastIdx != i) {
                     sb.append(chars, lastIdx, i - lastIdx);
                 }
                 sb.append("%25");
                 lastIdx = i + 1;
             } else if (chars[i] == ',') {
                 if (lastIdx != i) {
                     sb.append(chars, lastIdx, i - lastIdx);
                 }
                 sb.append("%2C");
                 lastIdx = i + 1;
             }
         }
         if (lastIdx != i) {
             sb.append(chars, lastIdx, i - lastIdx);
         }
         return sb.toString();
     }
 }
 
