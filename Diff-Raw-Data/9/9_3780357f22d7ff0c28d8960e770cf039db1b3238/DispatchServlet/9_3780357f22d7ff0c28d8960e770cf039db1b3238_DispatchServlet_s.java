 package com.dropedit.controller;
 
 import com.dropbox.client.DropboxClient;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.io.IOException;
 
 
 public class DispatchServlet extends HttpServlet {
     public static final String VIEW = "/WEB-INF/jsp/dispatcher.jsp";
 
 
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         HttpSession session = req.getSession(false);
         //resp.sendRedirect("edit");
         DropboxClient client = (DropboxClient) session.getAttribute("client");
         if (client == null) {
             resp.sendRedirect("/login");
         } else {
             resp.sendRedirect("/list");
         }
         /* RootPath rootpath;
       session = req.getSession(false);
       rootpath = (RootPath)session.getAttribute("parentPath");
       System.out.println("AuthServlet" + rootpath.getRootPath());
       rootpath.popRootPath();
       resp.sendRedirect("list?value=" + rootpath.getRootPath());*/
         // resp.sendRedirect("edit");
 
         //}
     }
 }
