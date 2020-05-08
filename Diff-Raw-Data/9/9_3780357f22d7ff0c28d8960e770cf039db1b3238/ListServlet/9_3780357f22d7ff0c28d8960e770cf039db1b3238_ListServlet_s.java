 package com.dropedit.controller;
 
 import com.dropbox.client.Authenticator;
 import com.dropbox.client.DropboxClient;
 import com.dropbox.client.DropboxException;
 import org.json.simple.JSONArray;
 //import org.json.simple.JSONValue;
 import org.json.simple.JSONObject;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.lang.reflect.Array;
 import java.util.*;
 import java.io.IOException;
 
 import org.apache.commons.lang.StringUtils;
 
 
 public class ListServlet extends HttpServlet {
     public static final String VIEW = "/WEB-INF/jsp/list.jsp";
 
 
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 
         HttpSession session = req.getSession(false);
         String currentPathName = req.getParameter("value");
         if (currentPathName == null) {
             //System.out.println(currentPathName);
             currentPathName = "";
         }
         System.out.println("currentPathName: " + currentPathName);
 
         RootPath rootPath;
         rootPath = (RootPath) session.getAttribute("parentPath");
 
         System.out.println("rootPath: " + rootPath.getRootPath());
 
 
         DropboxClient dropbox = (DropboxClient) session.getAttribute("client");
 
         req.setAttribute("user", session.getAttribute("uname"));
 
         String info = null;
         JSONObject testMap = new JSONObject();
         try {
             info = dropbox.accountInfo(false, "").toString();
             testMap = (JSONObject) dropbox.metadata("dropbox", currentPathName, 10000, "", true, false, "");
         } catch (DropboxException e) {
             info = "oh shit it failed!";
         }
 
         Set<FileDescriptor> fileDescriptors = new TreeSet<FileDescriptor>(new Comparator<FileDescriptor>() {
             @Override
             public int compare(FileDescriptor fileDescriptor, FileDescriptor fileDescriptor1) {
                 /*if(fileDescriptor.getIsDirectory() && !fileDescriptor1.getIsDirectory()){
                    return 1;
                }
                else if(!fileDescriptor.getIsDirectory() && fileDescriptor1.getIsDirectory()){
                   return -1;
                } */
                 return fileDescriptor.getName().compareToIgnoreCase(fileDescriptor1.getName());
             }
         });
 
 
         for (Object o : (JSONArray) testMap.get("contents")) {
             JSONObject jsObject = (JSONObject) o;
             /*for(Object e : jsObject.entrySet()) {
                Map.Entry<String, Object> entry = (Map.Entry<String, Object>) e;
                System.out.println("key: " + entry.getKey() + " value: " + entry.getValue());
            } */
             FileDescriptor fileDescriptor = new FileDescriptor();
             fileDescriptor.setName(StringUtils.difference(currentPathName, (String) jsObject.get("path")));
             fileDescriptor.setPath((String) jsObject.get("path"));
             fileDescriptor.setModifiedDate((String) jsObject.get("modified"));
             //fileDescriptor.setIsDirectory(jsObject.get("is_dir"));
 
             fileDescriptors.add(fileDescriptor);
         }
 
         req.setAttribute("parentPath", rootPath.getRootPath());
         System.out.println("ParentPath: " + rootPath.getRootPath());
         req.setAttribute("files", fileDescriptors);
         rootPath.addRootPath(currentPathName);
 
         req.getRequestDispatcher(VIEW).forward(req, resp);
         session.setAttribute("parentPath", rootPath);
 
         session.setAttribute("page", "list");
 
     }
 

     // path is relative to location of root (in this case, root == "dropbox")
     // path may be the location of a file or a folder
     protected int deleteFile(String path, DropboxClient dropbox) {
         try {
             //root is dropbox - alternative is sandbox
             JSONObject testMap = (JSONObject) dropbox.fileDelete("dropbox", path, null);
             if (testMap.containsValue("400")) {
                 return 400;
             } else if (testMap.containsValue("200")) {
                 return 200;
             } else if (testMap.containsValue("404")) {
                 return 404;
             }
             //NEED TO ADD CODE FOR RETURN VALUES
 
             return 1;
         }
         catch (DropboxException e) {
             System.out.println("Error deleting file " + path);
             return 0;
         }
     }
 
     public class FileDescriptor {
         private String name;
         private String path;
         private boolean directory;
         private String creationDate;
         private String modifiedDate;
 
 
         public String getPath() {
             return path;
         }
 
         public void setPath(String path) {
             this.path = path;
         }
 
         public String getName() {
             return name;
         }
 
         public void setName(String name) {
             this.name = name.substring(1);
         }
 
         public void setIsDirectory(boolean directory) {
             this.directory = directory;
         }
 
         public boolean getIsDirectory() {
             return directory;
         }
 
         public String getCreationDate() {
             return creationDate;
         }
 
         public void setCreationDate(String creationDate) {
             this.creationDate = creationDate;
         }
 
         public String getModifiedDate() {
             return modifiedDate;
         }
 
         public void setModifiedDate(String modifiedDate) {
             this.modifiedDate = modifiedDate;
         }
     }
 }
 
