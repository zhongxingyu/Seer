 package controller;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import picture.CreatePicture;
 
 /**
  *
  * @author Julian
  */
 public class StartServlet extends HttpServlet {
 
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
        request.setAttribute("hw", "examplestring hello");
        PrintWriter out = response.getWriter();
//
////        switch of cache
//        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
//        response.setHeader("Pragma", "no-cache"); // HTTP 1.0
//        response.setDateHeader("Expires", 0); // Proxies.
//
//        File deployStream = new File("C:/Program Files/Apache Software Foundation/Tomcat 6.0/webapps/ROOT/git/UnterrichtMe/UnterrichtMeProjekt/web/pics/test2.png");
//        CreatePicture instance = new CreatePicture();
//        instance.paintPicture(deployStream);
////                        
//        try {
//            out.println("<!DOCTYPE html>");  // HTML 5
//            out.println("<html><head>");
//            out.println("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>");
//            String title = "helloworld 22";
//            out.println("<title>" + title + "</title></head>");
//            out.println("<body>");
//            out.println("<h1>" + title + "</h1>");  // Prints "Hello, world!"
//            // Set a hyperlink image to refresh this page
//            out.println("<a href='" + request.getRequestURI() + "'><img src='images/return.gif'></a>");
//            out.println("</body></html>");
//        } finally {
//            out.close();  // Always close the output writer
//        }        
         request.getRequestDispatcher("/git/UnterrichtMe/UnterrichtMeProjekt/build/web/jsp/index.jsp").forward(request, response);
 
     }
 
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
     }
 }
