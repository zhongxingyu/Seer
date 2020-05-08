 package com.dropedit.controller;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.*;
 
 import com.dropbox.client.Authenticator;
 import com.dropbox.client.DropboxClient;
 import com.dropbox.client.DropboxException;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
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
 
 /**
  * Created by IntelliJ IDEA.
  * User: mike
  * Date: Dec 6, 2010
  * Time: 3:18:23 PM
  * To change this template use File | Settings | File Templates.
  */
 public class EditFileServlet extends HttpServlet {
     public static final String VIEW = "/WEB-INF/jsp/edit.jsp";
 
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         HttpSession session = req.getSession();
         /**********************************************************************/
         DropboxClient dropbox = (DropboxClient) session.getAttribute("client");
         HttpResponse response;
         HttpEntity entity;
         InputStream instream = null;
         StringBuffer fileContents = new StringBuffer();
         String fileName = req.getParameter("value");
         System.out.println("filename in doGet: " + fileName);
 
         try {
             response = dropbox.getFile("dropbox", fileName);
             entity = response.getEntity();
 
             instream = entity.getContent();
             BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
             String line;
             while ((line = reader.readLine()) != null) {
                fileContents.append(line + "\n");
             }
             System.out.println(fileContents.toString());
         }
         catch (com.dropbox.client.DropboxException e) {
             System.out.println("HTTP Response failed");
         }
         finally {
             if (instream == null) instream.close();  ////////////////
         }
 
         //**************************************************************
 
         req.setAttribute("textBox", fileContents.toString());
         req.setAttribute("fileName", fileName);
 
         req.getRequestDispatcher(VIEW).forward(req, resp);
         /*************************************************************************/
     }
 
     protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 
         String fileName = req.getParameter("fileName");
         System.out.println("fileName: " + fileName);
         String[] splitFile = fileName.split("/");
 
         File file = new File(splitFile[splitFile.length - 1]);
 
         RandomAccessFile fil = new RandomAccessFile(file, "rw");
         fil.writeBytes(req.getParameter("editbox"));
         fil.close();               /////////////////////
 
         HttpSession session = req.getSession();
         DropboxClient dropbox = (DropboxClient) session.getAttribute("client");
 
         String filePath = "";      //////////////
         for (int i = 0; i < splitFile.length - 1; i++) {
             filePath += splitFile[i] +"/";        ////////////
         }
 
         int length = filePath.length();           /////////////
 
         if(length > 0){                            ///////////
             length = length - 1;                   ///////////
         }
 
         try {
            HttpResponse response = dropbox.putFile("dropbox", filePath.substring(0,length), file);
            file.delete();
         }
         catch (com.dropbox.client.DropboxException e) {
             System.out.println("HTTP Response failed");
         }
 
         resp.sendRedirect("/list");
 
     }
 
 }
