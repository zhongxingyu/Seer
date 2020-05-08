 package com.epam.memegen;
 
 import java.io.FileReader;
 import java.io.IOException;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.io.IOUtils;
 
 @SuppressWarnings("serial")
 public class MainServlet extends HttpServlet {
   public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
     FileReader fr = new FileReader("list.html");
     String content = IOUtils.toString(fr);
     resp.setContentType("text/html");
    resp.setCharacterEncoding("UTF-8");
     resp.getWriter().write(content);
   }
 }
