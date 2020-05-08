 /*
 * Copyright 2002 - 2013 Pentaho Corporation.  All rights reserved.
 * 
 * This software was developed by Pentaho Corporation and is provided under the terms
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. TThe Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */
 
 package org.pentaho.mondrian.publish;
 
 import java.io.*;
 import java.security.spec.KeySpec;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.crypto.Cipher;
 import javax.crypto.SecretKey;
 import javax.crypto.SecretKeyFactory;
 import javax.crypto.spec.DESedeKeySpec;
 import javax.swing.JOptionPane;
 import javax.ws.rs.core.MediaType;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
 import com.sun.jersey.core.header.FormDataContentDisposition;
 import com.sun.jersey.multipart.FormDataMultiPart;
 
 import org.apache.commons.lang.StringUtils;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * User: Martin Date: 25.01.2006 Time: 11:26:24
  */
 public class PublishToServerCommand {
   
   private static final Logger LOG = Logger.getLogger(PublishToServerCommand.class.getName());
 
   private static final String DEFAULT_SERVER_LOCATION = "http://localhost:8080/pentaho/";
   private static final String PUBLISH_WEB_LOCATION = "pentahoPublishWebLocation";
   private static final String PUBLISH_WEB_LOCATIONS = "pentahoPublishWebLocations";
   private static final String PUBLISH_USER_ID = "pentahoPublishUserId";
   private static final String PUBLISH_USER_IDS = "pentahoPublishUserIds";
   private static final String PUBLISH_USER_PASSWORD = "pentahoUserPassword";
   private static final String PUBLISH_USER_PASSWORDS = "pentahoUserPasswords";
   private static final String PUBLISH_JNDI_NAME = "pentahoPublishJndiName";
   private static final String PUBLISH_ENABLE_XMLA = "pentahoPublishEnableXmla";
 
   private static final String DELIMITER = "\t";
   private static final String MONDRIAN_SCHEMA_IMPORT_URL = "plugin/data-access/api/mondrian/putSchema";
 
   private SecretKeyFactory keyFactory;
   private Cipher cipher;
   private SecretKey encryptionKey;
   
   public PublishToServerCommand() {
       try {
           byte[] keyAsBytes = "abcdefghijkPENTAHOlmnopqrstuvw5xyz".getBytes( "UTF8" );
           KeySpec keySpec = new DESedeKeySpec( keyAsBytes );
           keyFactory = SecretKeyFactory.getInstance( "DESede");
           encryptionKey = keyFactory.generateSecret( keySpec );
           cipher = Cipher.getInstance( "DESede" );
       } catch (Exception e) {
           LOG.severe("failed to initialize password encryption");
           e.printStackTrace();
       }
   }
 
   private String encryptPassword(String password) {
 			if (password == null ||
 					password.trim().length() == 0) {
 					return password;
 			}
       try {
           cipher.init( Cipher.ENCRYPT_MODE, encryptionKey );
           byte[] cleartext = password.getBytes( "UTF8" );
           byte[] ciphertext = cipher.doFinal( cleartext );
           return Base64.encodeBytes(ciphertext);
       } catch (Exception e) {
           LOG.severe("failed to encrypt password");
           e.printStackTrace();
       }
       return null;
   }
   
   private String decryptPassword(String encryptedPassword) {
   		if (encryptedPassword == null ||
   				encryptedPassword.trim().length() == 0) {
   			return encryptedPassword;
   		}
       try {
           cipher.init( Cipher.DECRYPT_MODE, encryptionKey );
           byte[] cleartext = Base64.decode(encryptedPassword);
           byte[] ciphertext = cipher.doFinal( cleartext );
           return new String(ciphertext, "UTF8");
       } catch (Exception e) {
           e.printStackTrace();
       }
       return null;
   }
   
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
 
 
   public void execute(PublishSchemaPluginParent parent) {
     String publishURL = parent.getProperty(PUBLISH_WEB_LOCATION);
     if (StringUtils.isEmpty(publishURL)) {
       publishURL = DEFAULT_SERVER_LOCATION;
     }
 
     String user = parent.getProperty(PUBLISH_USER_ID);
     String userPassword = decryptPassword(parent.getProperty(PUBLISH_USER_PASSWORD));
     boolean enableXmla = "true".equals(parent.getProperty(PUBLISH_ENABLE_XMLA));
     String jndiName = parent.getProperty(PUBLISH_JNDI_NAME);
     if (StringUtils.isEmpty(jndiName)) {
       jndiName = "FoodMart";
     }
 
     List<String> publishLocations = splitProperties(parent.getProperty(PUBLISH_WEB_LOCATIONS));
     List<String> publishUserIds = splitProperties(parent.getProperty(PUBLISH_USER_IDS));
     List<String> publishUserPasswords = splitProperties(decryptPassword(parent.getProperty(PUBLISH_USER_PASSWORDS)));
 
     final RepositoryLoginDialog loginDialog = new RepositoryLoginDialog(parent.getFrame(), publishURL, publishLocations,
                                                                          publishUserIds, publishUserPasswords, jndiName, enableXmla);
     loginDialog.setLocationRelativeTo(parent.getFrame());
     loginDialog.setVisible(true);
 
     if (loginDialog.isPublishPressed()) {
       // extract info, launch publish dialog
       publishURL = loginDialog.getServerURL();
       user = loginDialog.getUsername();
       userPassword = loginDialog.getUserPassword();
       jndiName = loginDialog.getJndiDataSourceName();
       enableXmla = loginDialog.getEnableXmla();
 
       if (loginDialog.getRememberSettings()) {
         parent.setProperty(PUBLISH_WEB_LOCATION, publishURL);
         parent.setProperty(PUBLISH_USER_ID, user);
         parent.setProperty(PUBLISH_USER_PASSWORD, encryptPassword(userPassword));
         parent.setProperty(PUBLISH_JNDI_NAME, jndiName);
         parent.setProperty(PUBLISH_ENABLE_XMLA, enableXmla ? "true" : "false");
 
         if (!publishLocations.contains(publishURL)) {
           publishLocations.add(publishURL);
           parent.setProperty(PUBLISH_WEB_LOCATIONS, getListAsString(publishLocations));
         }
 
         int index = publishLocations.indexOf(publishURL);
         // update user
         if (index >= 0 && index < publishUserIds.size()) {
           publishUserIds.remove(index);
           publishUserIds.add(index, user);
         } else {
           publishUserIds.add(user);
         }
         parent.setProperty(PUBLISH_USER_IDS, getListAsString(publishUserIds));
 
         // update user password
         if (index >= 0 && index < publishUserPasswords.size()) {
           publishUserPasswords.remove(index);
           publishUserPasswords.add(index, userPassword);
         } else {
           publishUserPasswords.add(userPassword);
         }
         parent.setProperty(PUBLISH_USER_PASSWORDS, encryptPassword(getListAsString(publishUserPasswords)));
 
         // Store properties
         parent.storeProperties();
       }
 
       try {
         try {
           String publisherUrl = publishURL;
           if (!publishURL.endsWith("/")) {
             publisherUrl += "/";
           }
           publisherUrl += MONDRIAN_SCHEMA_IMPORT_URL;
 
           String message = "";
           int statusCode = publish(publisherUrl, user, userPassword, jndiName, enableXmla, false, parent.getSchemaFile());
           if ((statusCode == 1) || (statusCode == 2)) {
             message = Messages.getString("PublishToServerCommand.Failed");
           } else if (statusCode == 3) {
             message = Messages.getString("PublishToServerCommand.Successful");
           } else if (statusCode == 4) {
             message = Messages.getString("PublishToServerCommand.InvalidPassword");
           } else if (statusCode == 5) {
             message = Messages.getString("PublishToServerCommand.InvalidUsernameOrPassword");
           } else if (statusCode == 6) {
             message = Messages.getString("PublishToServerCommand.DataSourceProblem", jndiName);
          } else if (statusCode == 7) {
             message = Messages.getString("PublishToServerCommand.XMLACatalogExists");
          } else if (statusCode == 8) {
            message = Messages.getString("PublishToServerCommand.XMLASchemaExists");

             // Based on the response, if failure was due to an overwrite, prompt user and reissue service call forcing an overwrite
             OverwriteSchemaDialog overwriteDialog = new OverwriteSchemaDialog(parent.getFrame(), parent.getSchemaFile().getName());
 
             overwriteDialog.setLocationRelativeTo(parent.getFrame());
             overwriteDialog.setVisible(true);
             boolean overwrite = overwriteDialog.isOkPressed();
             if (overwrite) {
               statusCode = publish(publisherUrl, user, userPassword, jndiName, enableXmla, true, parent.getSchemaFile());
               if (statusCode == 3) {
                 message = Messages.getString("PublishToServerCommand.Successful");
               }
             }
           } else {
             message = Messages.getString("PublishToServerCommand.Failed");
           }
 
           if (message.length() > 0) {
             JOptionPane.showMessageDialog(
                     parent.getFrame(),
                     message.trim(),
                     Messages.getString("PublishToServerCommand.Information.Title"),
                     JOptionPane.INFORMATION_MESSAGE);
           }
         } catch (PublishException e1) {
           if (LOG.isLoggable(Level.FINE)) {
             LOG.log(Level.FINE, "PublishToServerCommand.actionPerformed ", e1);
           }
           JOptionPane.showMessageDialog(
                   parent.getFrame(),
                   Messages.getString("PublishToServerCommand.Error.Message", e1.getMessage()),
                   Messages.getString("PublishToServerCommand.Error.Title"),
                   JOptionPane.ERROR_MESSAGE);
         }
       } catch (Exception e1) {
         e1.printStackTrace();
         JOptionPane.showMessageDialog(
                 parent.getFrame(),
                 Messages.getString("PublishToServerCommand.Error.Message", e1.getMessage()),
                 Messages.getString("PublishToServerCommand.Error.Title"),
                 JOptionPane.ERROR_MESSAGE);
       }
     }
   }
 
   private int publish(
           String publishURL,
           String serverUserId,
           String serverPassword,
           String jndiName,
           boolean enableXmla,
           boolean overwrite,
           File schemaFile) throws PublishException, UnsupportedEncodingException {
     try {
       InputStream inputStream = new FileInputStream(schemaFile);
 
       // Try to get schema name from xml, otherwise use filename
       String catalogName = determineDomainCatalogName(new FileInputStream(schemaFile), schemaFile.getName());
 
       FormDataMultiPart part = new FormDataMultiPart()
               .field("uploadAnalysis", inputStream, MediaType.MULTIPART_FORM_DATA_TYPE)
               .field("catalogName", catalogName, MediaType.MULTIPART_FORM_DATA_TYPE)
               .field("Datasource", jndiName, MediaType.MULTIPART_FORM_DATA_TYPE)              
               .field("overwrite", overwrite ? "true" : "false", MediaType.MULTIPART_FORM_DATA_TYPE)
               .field("xmlaEnabledFlag", enableXmla ? "true" : "false", MediaType.MULTIPART_FORM_DATA_TYPE)
              .field("parameters", "Datasource=" + jndiName, MediaType.MULTIPART_FORM_DATA_TYPE);
 
       // If the import service needs the file name do the following.
       part.getField("uploadAnalysis").setContentDisposition(FormDataContentDisposition.name("uploadAnalysis").fileName(schemaFile.getName()).build());
 
       // Credentials here
       Client client = Client.create();
       client.addFilter(new HTTPBasicAuthFilter(serverUserId, serverPassword));
 
       LOG.fine("PUBLISH URL PATH : " + publishURL);
 
       WebResource resource = client.resource(publishURL);
       ClientResponse response = resource.type(MediaType.MULTIPART_FORM_DATA_TYPE).put(ClientResponse.class, part);
 
       return response != null ? response.getStatus() : -1;
     } catch (FileNotFoundException e) {
       throw new PublishException("Unable to publish Mondrian Schema");
     }
   }
 
   /**
    * helper method to calculate the domain id from the file name, or pass catalog
    * @param dataInputStream schema file input stream
    * @param fileName name of schema file on filesystem
    * @return Look up name from XML otherwise use file name
    */
   private String determineDomainCatalogName(InputStream dataInputStream, String fileName) {
     String domainId  = "";
     final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 
     try {
       DocumentBuilder builder = factory.newDocumentBuilder();
       Document document = builder.parse(dataInputStream);
       NodeList schemas = document.getElementsByTagName("Schema");
       Node schema = schemas.item(0);
       Node name = schema.getAttributes().getNamedItem("name");
       domainId = name.getTextContent();
       dataInputStream.reset();
     } catch (Exception e) {
       LOG.fine("Problem occurred when trying to get schema name from document. Using filename instead.");
     }
 
     if("".equals(domainId)){
       domainId = fileName;
     }
 
     return domainId;
   }
 
 }
