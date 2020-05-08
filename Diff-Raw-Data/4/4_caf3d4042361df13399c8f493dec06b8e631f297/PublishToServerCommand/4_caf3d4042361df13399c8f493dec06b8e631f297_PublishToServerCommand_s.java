 /*
  * Copyright 2008 Pentaho Corporation.  All rights reserved.
  * This software was developed by Pentaho Corporation and is provided under the terms
  * of the Mozilla Public License, Version 1.1, or any later version. You may not use
  * this file except in compliance with the license. If you need a copy of the license,
  * please go to http://www.mozilla.org/MPL/MPL-1.1.txt.
  *
  * Software distributed under the Mozilla Public License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
  * the license for the specific language governing your rights and limitations.
  *
  * Additional Contributor(s): Martin Schmid gridvision engineering GmbH
  */
 package org.pentaho.mondrian.workbench.publish;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.security.MessageDigest;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 
 import mondrian.gui.SchemaExplorer;
 import mondrian.gui.Workbench;
 
 import org.apache.commons.httpclient.Credentials;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.UsernamePasswordCredentials;
 import org.apache.commons.httpclient.auth.AuthScope;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.methods.multipart.FilePart;
 import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
 import org.apache.commons.httpclient.methods.multipart.Part;
 import org.apache.commons.lang.StringUtils;
 import org.dom4j.Document;
 
 /**
  * User: Martin Date: 25.01.2006 Time: 11:26:24
  */
 public class PublishToServerCommand {
   
   
   private static final Logger LOG = Logger.getLogger(PublishToServerCommand.class.getName());
 
   
   private static final String DEFAULT_SERVER_LOCATION = "http://localhost:8080/pentaho/";
 
   public PublishToServerCommand() {
   }
   
   private static final String PUBLISH_WEB_LOCATION = "pentahoPublishWebLocation";
   private static final String PUBLISH_WEB_LOCATIONS = "pentahoPublishWebLocations";
   private static final String PUBLISH_USER_ID = "pentahoPublishUserId";
   private static final String PUBLISH_USER_IDS = "pentahoPublishUserIds";
   private static final String PUBLISH_PASSWORD = "pentahoPublishPassword";
   private static final String PUBLISH_PASSWORDS = "pentahoPublishPasswords";
   private static final String PUBLISH_USER_PASSWORD = "pentahoPublishPassword";
   private static final String PUBLISH_USER_PASSWORDS = "pentahoPublishPasswords";
   private static final String PUBLISH_JNDI_NAME = "pentahoPublishJndiName";
   private static final String PUBLISH_ENABLE_XMLA = "pentahoPublishEnableXmla";
   private static final String PUBLISH_LOCATION = "pentahoPublishLocation";
   
   private static final String DELIMITER = "\t"; 
       
   private List<String> splitProperties(String value) {
       List<String> list = new ArrayList<String>();
       if (value == null) {
         return list;
       }
       String items[] = value.split(DELIMITER);
       for (int i = 0; i < items.length; i++) {
           if (items[i] != null && items[i].trim().length() > 0) {
               list.add(items[i]);
           }
       }
       return list;
   }
   
   private String getListAsString(List<String> list) {
       StringBuilder sb = new StringBuilder();
       for (int i = 0; i < list.size(); i++) {
           if (i != 0) {
               sb.append(DELIMITER);
           }
           sb.append(list.get(i));
       }
       return sb.toString();
   }
   
   public static class Worker extends Thread {
      
     IndeterminateProgressDialog progressDialog;
     String webPublishURL;
     String filters[];
     String userid;
     String password;
     RepositoryHelper repositoryHelper;
     
     Document document;
     Exception exception;
       
     public Worker(IndeterminateProgressDialog progressDialog, String webPublishURL, String filters[], String userid, String password) {
       this.progressDialog = progressDialog;
       this.webPublishURL = webPublishURL;
       this.filters = filters;
       this.userid = userid;
       this.password = password;
       this.repositoryHelper = new RepositoryHelper();
     }
       
     public void run() {
       try {
         document = repositoryHelper.getRepositoryDocument(webPublishURL, filters, userid, password);
       } catch (Exception e) {
         exception = e;
       } finally {
         SwingUtilities.invokeLater(new Runnable() {
           public void run() {
             progressDialog.setVisible(false);
           }
         });
       }
     }
       
     public void abortCurrentHttpRequest() {
         repositoryHelper.abortCurrentHttpRequest();
     }
     
     public Document getDocument() {
       return document;
     }
       
     public Exception getException() {
       return exception;
     }
   };
   
   public void execute(Workbench workbench) {
       SchemaExplorer schemaExplorer = workbench.getCurrentSchemaExplorer();
       if (schemaExplorer == null) {
           // log a message
           return;
       }
      if (schemaExplorer.isDirty()) {
       int option = JOptionPane.showConfirmDialog(workbench, Messages.getString("SchemaModifiedWarning.Message"), Messages.getString("SchemaModifiedWarning.Title"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
       if (option == JOptionPane.YES_OPTION) {
           workbench.saveMenuItemActionPerformed(null);
       } else if (option == JOptionPane.CANCEL_OPTION) {
           return;
       }
     }
 
     String publishURL = workbench.getWorkbenchProperty(PUBLISH_WEB_LOCATION);
     if (StringUtils.isEmpty(publishURL)) {
       publishURL = DEFAULT_SERVER_LOCATION;
     }
     String user = workbench.getWorkbenchProperty(PUBLISH_USER_ID);
             
     String publishPassword = workbench.getWorkbenchProperty(PUBLISH_PASSWORD);
     String userPassword = workbench.getWorkbenchProperty(PUBLISH_USER_PASSWORD);
 
     List<String> publishLocations = splitProperties(workbench.getWorkbenchProperty(PUBLISH_WEB_LOCATIONS));
     List<String> publishUserIds = splitProperties(workbench.getWorkbenchProperty(PUBLISH_USER_IDS));
     List<String> publishUserPasswords = splitProperties(workbench.getWorkbenchProperty(PUBLISH_USER_PASSWORDS));
     List<String> publishPasswords = splitProperties(workbench.getWorkbenchProperty(PUBLISH_PASSWORDS));
 
     final RepositoryLoginDialog loginDialog = new RepositoryLoginDialog(workbench, publishURL, publishLocations, publishUserIds, publishUserPasswords, publishPasswords);
     WindowUtils.setLocationRelativeTo(loginDialog, workbench);
     loginDialog.setVisible(true);
 
     if (loginDialog.isOkPressed()) {
       // extract info, launch publish dialog
       publishURL = loginDialog.getServerURL();
       user = loginDialog.getUsername();
       userPassword = loginDialog.getUserPassword();
       publishPassword = loginDialog.getPublishPassword();
 
       if (loginDialog.getRememberSettings()) {
         workbench.setWorkbenchProperty(PUBLISH_WEB_LOCATION, publishURL);
         workbench.setWorkbenchProperty(PUBLISH_PASSWORD, publishPassword);
         workbench.setWorkbenchProperty(PUBLISH_USER_ID, user);
         workbench.setWorkbenchProperty(PUBLISH_USER_PASSWORD, userPassword);
 
         if (!publishLocations.contains(publishURL)) {
           publishLocations.add(publishURL);
           workbench.setWorkbenchProperty(PUBLISH_WEB_LOCATIONS, getListAsString(publishLocations));
         }
         int index = publishLocations.indexOf(publishURL);
         // update user
         if (index >= 0 && index < publishUserIds.size()) {
           publishUserIds.remove(index);
           publishUserIds.add(index, user);
         } else {
           publishUserIds.add(user);
         }
         workbench.setWorkbenchProperty(PUBLISH_USER_IDS, getListAsString(publishUserIds));
         
         // update user password
         if (index >= 0 && index < publishUserPasswords.size()) {
           publishUserPasswords.remove(index);
           publishUserPasswords.add(index, userPassword);
         } else {
           publishUserPasswords.add(userPassword);
         }
         workbench.setWorkbenchProperty(PUBLISH_USER_PASSWORDS, getListAsString(publishUserPasswords));
         
         // update publish password
         if (index >= 0 && index < publishPasswords.size()) {
           publishPasswords.remove(index);
           publishPasswords.add(index, publishPassword);
         } else {
           publishPasswords.add(publishPassword);
         }
         workbench.setWorkbenchProperty(PUBLISH_PASSWORDS, getListAsString(publishPasswords));
         workbench.storeWorkbenchProperties();
       }
 
       try {
 
         String schemaName = schemaExplorer.getSchema().name;
         String fileName = schemaExplorer.getSchemaFile().getName(); 
 
         String publishPath = workbench.getWorkbenchProperty(PUBLISH_LOCATION);
         if (StringUtils.isEmpty(publishPath)) {
           publishPath = "/samples/analysis";
         }
         
         String jndiName = workbench.getWorkbenchProperty(PUBLISH_JNDI_NAME);
         if (StringUtils.isEmpty(jndiName)) {
             jndiName = "SampleData";
         }
         boolean enableXmla = "true".equals(workbench.getWorkbenchProperty(PUBLISH_ENABLE_XMLA));
 
         boolean okPressed = true;
         while (okPressed) {
             
           // creating the repository browser object make take some time,
           // display an indeterminate progress dialog if necessary
 
           // note: the HTTPClient connection in the repositorybrowser
           // is set to time out at 30 seconds and will retry 3 times.  
           // If the dialog is closed while the worker thread is still 
           // running, the thread will continue to attempt connecting
             
           final IndeterminateProgressDialog progressDialog = 
               new IndeterminateProgressDialog(workbench, "Progress Dialog", "Connecting to Repository...");
           WindowUtils.setLocationRelativeTo(progressDialog, workbench);
 
           Worker worker = new Worker(progressDialog, publishURL, null, user, userPassword);
           worker.start();
             
           // wait a little bit to see if we need to display a wait dialog
           try { Thread.sleep(500); } catch (Exception e) {}
             
           // if we still haven't connected and there are no errors,
           // display progress dialog
           if (worker.getDocument() == null && worker.getException() == null) {
                 progressDialog.setVisible(true);
           }
             
           if (worker.getException() != null) {
             // throw any exceptions generated by initializing the repository browser
             throw worker.getException();
           } else if (worker.getDocument() == null) {
             if (worker.isAlive()) {
               // we've got a problem, we need to kill off the thread somehow
               worker.abortCurrentHttpRequest();
             }
             throw new PublishException("Failed to connect to repository");
           }
             
           PublishToRepositoryDialog repositoryBrowserDialog = 
               new PublishToRepositoryDialog(workbench, worker.getDocument(), false, 
                       publishURL, publishPath, user, userPassword, null, schemaName, 
                       fileName, jndiName, enableXmla);
           WindowUtils.setLocationRelativeTo(repositoryBrowserDialog, workbench);
           repositoryBrowserDialog.setVisible(true);
           
           publishPath = repositoryBrowserDialog.getPublishLocation();          
           jndiName = repositoryBrowserDialog.getJndiDataSourceName();
           enableXmla = repositoryBrowserDialog.getEnableXmla();
           
           workbench.setWorkbenchProperty(PUBLISH_LOCATION, publishPath);
           workbench.setWorkbenchProperty(PUBLISH_JNDI_NAME, jndiName);
           workbench.setWorkbenchProperty(PUBLISH_ENABLE_XMLA, "" + enableXmla);
           
           okPressed = repositoryBrowserDialog.isOkPressed();
           if (okPressed) {
             boolean overwrite = true;
             if (repositoryBrowserDialog.doesFileExist(publishPath + "/" + fileName)) {
               OverwriteSchemaDialog overwriteDialog = new OverwriteSchemaDialog(workbench, fileName);
               WindowUtils.setLocationRelativeTo(overwriteDialog, workbench);
               overwriteDialog.setVisible(true);
               overwrite = overwriteDialog.isOkPressed();
             }
             if (overwrite) {
               try {
                   String publisherUrl = publishURL;
                   if (!publishURL.endsWith("/")) {
                       publisherUrl += "/";
                   }
                   publisherUrl += "MondrianCatalogPublisher";
                   String message = publish(publisherUrl, user, userPassword, publishPassword, publishPath, schemaExplorer.getSchemaFile(), jndiName, enableXmla);
                   JOptionPane.showMessageDialog(
                       workbench, 
                       message.trim(), 
                       Messages.getString("PublishToServerCommand.Information.Title"), 
                       JOptionPane.INFORMATION_MESSAGE);
                   
               } catch (PublishException e1) {
                 if (LOG.isLoggable(Level.FINE)) {
                   LOG.log(Level.FINE, "PublishToServerCommand.actionPerformed ", e1);
                 }
                 JOptionPane.showMessageDialog(
                         workbench, 
                         Messages.getString("PublishToServerCommand.Error.Message", e1.getMessage()), 
                         Messages.getString("PublishToServerCommand.Error.Title"), 
                         JOptionPane.ERROR_MESSAGE);
               }
 
               break;
             }
           } else {
             break;
           }
         }
       } catch (Exception e1) {
         e1.printStackTrace();
         JOptionPane.showMessageDialog(
                 workbench, 
                 Messages.getString("PublishToServerCommand.Error.Message", e1.getMessage()), 
                 Messages.getString("PublishToServerCommand.Error.Title"),
                 JOptionPane.ERROR_MESSAGE);
       }
     }
   }
 
   private String publish(
           String publishURL,
           String serverUserId,
           String serverPassword,          
           String publishPassword,
           String publishPath,
           File publishFile, 
           String jndiName,
           boolean enableXmla) throws PublishException, UnsupportedEncodingException
   {
       String fullURL = publishURL + "?publishPath=" + URLEncoder.encode(publishPath, "UTF-8");// NON-NLS
       fullURL += "&publishKey=" + getPasswordKey(new String(publishPassword)); //$NON-NLS-1$
       fullURL += "&overwrite=true"; //$NON-NLS-1$
       fullURL += "&jndiName=" + jndiName;
       fullURL += "&enableXmla=" + enableXmla;
       
       LOG.fine("PUBLISH URL PATH : " + fullURL);
       
       PostMethod filePost = new PostMethod(fullURL);
       ArrayList<Part> parts = new ArrayList<Part>();
       try {
           parts.add(new FilePart(publishFile.getName(), publishFile));
       } catch (FileNotFoundException e) {
           // file is not existing or not readable, this should not happen
           e.printStackTrace();
       }
       filePost.setRequestEntity(new MultipartRequestEntity(parts.toArray(new Part[parts.size()]), filePost.getParams()));
       HttpClient client = new HttpClient();
       // If server userid/password was supplied, use basic authentication to
       // authenticate with the server.
       if (serverUserId.length() > 0 && serverPassword.length() > 0) {
           Credentials creds = new UsernamePasswordCredentials(serverUserId, serverPassword);
           client.getState().setCredentials(AuthScope.ANY, creds);
           client.getParams().setAuthenticationPreemptive(true);
       }
       int status;
       try {
           status = client.executeMethod(filePost);
       } catch (IOException e) {
           throw new PublishException(e.getMessage(), e);
       }
       if (status != HttpStatus.SC_OK) {
           if (status == HttpStatus.SC_MOVED_TEMPORARILY) {
               throw new PublishException(Messages.getString("PublishToServerCommand.InvalidUsernameOrPassword"));
           } else {
               throw new PublishException("Unknown server error: HTTP status code " + status);
           }
       } else {
           String message = null;
           try {
               String postResult = filePost.getResponseBodyAsString();
               int rtn = Integer.parseInt(postResult.trim());
               if (rtn == 3) {
                   message = Messages.getString("PublishToServerCommand.Successful");
               } else if (rtn == 2) {
                   message = Messages.getString("PublishToServerCommand.Failed");
               } else if (rtn == 4) {
                   message = Messages.getString("PublishToServerCommand.InvalidPassword");
               } else if (rtn == 5) {
                   message = Messages.getString("PublishToServerCommand.InvalidUsernameOrPassword");
               } else if (rtn == 1) {
                   message = Messages.getString("PublishToServerCommand.FileExistsOverride");
               }
           } catch (IOException e) {
               throw new PublishException(e);
           }
           return message;
       }
   }
 
   /**
    * Utility for getting the MD5 hash from the provided key for sending the publishPassword.
    *
    * @param passWord The password to get an MD5 hash of
    * @return zero-padded MD5 hash of the password
    */
   public static String getPasswordKey(String passWord) {
       try {
           MessageDigest md = MessageDigest.getInstance("MD5");// NON-NLS
           md.reset();
           md.update(passWord.getBytes("UTF-8"));// NON-NLS
           byte[] digest = md.digest("P3ntah0Publ1shPa55w0rd".getBytes("UTF-8"));// NON-NLS
           StringBuilder buf = new StringBuilder(digest.length + 1);
           String s;
           for (byte aDigest : digest)
           {
               s = Integer.toHexString(0xFF & aDigest);
               buf.append((s.length() == 1) ? "0" : "").append(s);
           }
           return buf.toString();
       } catch (Exception ex) {
           ex.printStackTrace();
       }
       return null;
   }
   
 }
