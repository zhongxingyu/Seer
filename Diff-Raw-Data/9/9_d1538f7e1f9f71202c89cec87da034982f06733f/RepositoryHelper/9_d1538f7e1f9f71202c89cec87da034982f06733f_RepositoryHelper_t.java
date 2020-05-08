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
  */
 package org.pentaho.mondrian.publish;
 
 import java.io.InputStream;
 import java.net.URLEncoder;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.TreeNode;
 
 import org.apache.commons.httpclient.Credentials;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.UsernamePasswordCredentials;
 import org.apache.commons.httpclient.auth.AuthScope;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.dom4j.Document;
 import org.dom4j.Element;
 import org.dom4j.io.SAXReader;
 
 public class RepositoryHelper {
   private static SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
   private HashMap<DefaultMutableTreeNode, Boolean> nodeTypeMap = new HashMap<DefaultMutableTreeNode, Boolean>();
   private HashMap<DefaultMutableTreeNode, Date> nodeDateMap = new HashMap<DefaultMutableTreeNode, Date>();
   private DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Solution Repository");
 
   public static RepositoryHelper getInstance() {
     return new RepositoryHelper();
   }
 
   public void createNewFolder(String baseURL, String path, String name, String description, 
   		String serverUserId, String serverPassword) throws Exception {
     HttpClient client = new HttpClient();
     client.getParams().setSoTimeout(30000);
     if (serverUserId.length() > 0 && serverPassword.length() > 0) {
       Credentials creds = new UsernamePasswordCredentials(serverUserId, new String(serverPassword));
       client.getState().setCredentials(AuthScope.ANY, creds);
       client.getParams().setAuthenticationPreemptive(true);
      client.getParams().setCredentialCharset("UTF-8");
     }
     String fullURL = baseURL;
     if (!fullURL.endsWith("/")) {
     	fullURL += "/";
     }
   	fullURL += "SolutionRepositoryService?component=createNewFolder&path=" + URLEncoder.encode(path, "UTF-8") 
   							+ "&name=" + URLEncoder.encode(name, "UTF-8") + "&desc="
   						  + URLEncoder.encode(description, "UTF-8");
     PostMethod filePost = new PostMethod(fullURL);
     int status = client.executeMethod(filePost);
     if (status != HttpStatus.SC_OK) {
       throw new Exception("Server error: HTTP status code " + status);
     }
   }
   
   HttpMethod currentRequest;
   
   public void abortCurrentHttpRequest() {
       try {
           if (currentRequest != null) {
               currentRequest.abort();
           }
       } catch (Exception e) {
           e.printStackTrace();
       }
   }
 
   public Document getRepositoryDocument(String baseURL, String filters[], String serverUserId, String serverPassword) throws Exception {
     // If server userid/password was supplied, use basic authentication to
     // authenticate with the server.
     HttpClient client = new HttpClient();
     client.getParams().setSoTimeout(30000);
     if (serverUserId.length() > 0 && serverPassword.length() > 0) {
       Credentials creds = new UsernamePasswordCredentials(serverUserId, new String(serverPassword));
       client.getState().setCredentials(AuthScope.ANY, creds);
       client.getParams().setAuthenticationPreemptive(true);
      client.getParams().setCredentialCharset("UTF-8");
     }
     String filter = "";
     for (int i = 0; filters != null && i < filters.length; i++) {
       filter += filters[i];
       if (i < filters.length - 1) {
         filter += ",";
       }
     }
     currentRequest = new PostMethod(baseURL + "/SolutionRepositoryService?component=getSolutionRepositoryDoc&filter=" + URLEncoder.encode(filter, "UTF-8"));
     try {
         int status = client.executeMethod(currentRequest);
         if (status == HttpStatus.SC_UNAUTHORIZED) {
           throw new Exception("User authentication failed.");
         } else if (status == HttpStatus.SC_NOT_FOUND) {
           throw new Exception("Repository service not found on server.");
         } else if (status != HttpStatus.SC_OK) {
           throw new Exception("Server error: HTTP status code " + status);
         } else {
           InputStream postResult = currentRequest.getResponseBodyAsStream();
           SAXReader reader = new SAXReader();
           return reader.read(postResult);
         }
     } finally {
         try {
             currentRequest.releaseConnection();
         } catch (Exception e) {
             // ignore
         }
         currentRequest = null;
     }
   }
 
   public static boolean acceptFilter(String name, String[] filters) {
     if (filters == null || filters.length == 0) {
       return true;
     }
     for (int i = 0; i < filters.length; i++) {
       if (name.endsWith(filters[i])) {
         return true;
       }
     }
     return false;
   }
 
   public void buildRepository(DefaultMutableTreeNode parentNode, Element parentElement, String filters[], boolean showFoldersOnly) {
     List<Element> childFolders = parentElement.selectNodes("file[@isDirectory='true']");
     for (int i = 0; i < childFolders.size(); i++) {
       Element child = childFolders.get(i);
       DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child.attributeValue("name"));
       nodeTypeMap.put(childNode, Boolean.TRUE);
       try {
           nodeDateMap.put(childNode, new Date(Long.parseLong(child.attributeValue("lastModifiedDate"))));
       } catch (Exception e) {
           // ignore for backwards compatibility with 1.7
       }
       parentNode.add(childNode);
       buildRepository(childNode, childFolders.get(i), filters, showFoldersOnly);
     }
     if (!showFoldersOnly) {
       List<Element> childFiles = parentElement.selectNodes("file[@isDirectory='false']");
       for (int i = 0; i < childFiles.size(); i++) {
         Element child = childFiles.get(i);
         if (acceptFilter(child.attributeValue("name"), filters)) {
           DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child.attributeValue("name"));
           nodeTypeMap.put(childNode, Boolean.FALSE);
           try {
               nodeDateMap.put(childNode, new Date(Long.parseLong(child.attributeValue("lastModifiedDate"))));
           } catch (Exception e) {
       	    // ignore for backwards compatibility with 1.7          
           }
           parentNode.add(childNode);
         }
       }
     }
   }
 
   public void buildRepositoryTree(Document solutionRepositoryDocument, String filters[], boolean showFoldersOnly) {
     Element repository = (Element) solutionRepositoryDocument.selectSingleNode("//repository");
     rootNode.removeAllChildren();
     nodeTypeMap.clear();
     List<Element> childFolders = repository.selectNodes("file[@isDirectory='true']");
     for (int i = 0; i < childFolders.size(); i++) {
       Element child = childFolders.get(i);
       DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child.attributeValue("name"));
       nodeTypeMap.put(childNode, Boolean.TRUE);
       try {
           nodeDateMap.put(childNode, new Date(Long.parseLong(child.attributeValue("lastModifiedDate"))));
       } catch (Exception e){
           // ignore for backwards compatibility with 1.7
       }
       rootNode.add(childNode);
       buildRepository(childNode, childFolders.get(i), filters, showFoldersOnly);
     }
     if (!showFoldersOnly) {
       List<Element> childFiles = repository.selectNodes("file[@isDirectory='false']");
       for (int i = 0; i < childFiles.size(); i++) {
         Element child = childFiles.get(i);
         if (acceptFilter(child.attributeValue("name"), filters)) {
           DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child.attributeValue("name"));
           nodeTypeMap.put(childNode, Boolean.FALSE);
           try {
               nodeDateMap.put(childNode, new Date(Long.parseLong(child.attributeValue("lastModifiedDate"))));
           } catch (Exception e) {
               // ignore for backwards compatibility with 1.7
           }
           rootNode.add(childNode);
         }
       }
     }
   }
 
   public DefaultMutableTreeNode getNodeForSelection(String selection, String selectedFolder) {
     StringTokenizer st = new StringTokenizer(selectedFolder + "/" + selection, "/");
     DefaultMutableTreeNode node = rootNode;
     while (st.hasMoreTokens()) {
       String token = st.nextToken();
       DefaultMutableTreeNode child = null;
       boolean found = false;
       for (int i = 0; i < node.getChildCount(); i++) {
         child = (DefaultMutableTreeNode) node.getChildAt(i);
         if (child.getUserObject().equals(token)) {
           found = true;
           break;
         }
       }
       if (found) {
         node = child;
       }
     }
     return node;
   }
 
   public int getNumChildrenForPath(String selectedFolder, String filters[]) {
     if (selectedFolder == null) {
       return 0;
     }
     StringTokenizer st = new StringTokenizer(selectedFolder, "/");
     DefaultMutableTreeNode node = rootNode;
     while (st.hasMoreTokens()) {
       String token = st.nextToken();
       DefaultMutableTreeNode child = null;
       boolean found = false;
       for (int i = 0; node != null && i < node.getChildCount(); i++) {
         child = (DefaultMutableTreeNode) node.getChildAt(i);
         if (child.getUserObject().equals(token)) {
           found = true;
           break;
         }
       }
       if (found) {
         node = child;
       }
     }
     if (node != null) {
       // filter children
       int actualChildren = 0;
       for (int i = 0; i < node.getChildCount(); i++) {
         if (RepositoryHelper.acceptFilter(node.getChildAt(i).toString(), filters)) {
           actualChildren++;
         }
       }
       return actualChildren;
     }
     return 0;
   }
 
   public boolean exists(String path) {
     if (path == null) {
       return false;
     }
     StringTokenizer st = new StringTokenizer(path, "/");
     DefaultMutableTreeNode node = rootNode;
     while (st.hasMoreTokens()) {
       String token = st.nextToken();
       DefaultMutableTreeNode child = null;
       boolean found = false;
       for (int i = 0; node != null && i < node.getChildCount(); i++) {
         child = (DefaultMutableTreeNode) node.getChildAt(i);
         if (child.getUserObject().equals(token)) {
           found = true;
           break;
         }
       }
       if (found) {
         node = child;
       } else {
         return false;
       }
     }
     return true;
   }
 
   public boolean isFolder(TreeNode node) {
     return nodeTypeMap.get(node);
   }
 
   public boolean hasDate(TreeNode node) {
     return nodeDateMap.get(node) != null;
   }
 
   public String getDate(TreeNode node) {
     return sdf.format(nodeDateMap.get(node));
   }
 
   public DefaultMutableTreeNode getRootNode() {
     return rootNode;
   }
 
 }
