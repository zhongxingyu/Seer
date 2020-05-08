 package com.clescot.web.debug.tag.example;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.io.IOException;
 import java.util.Random;
 
 @WebServlet(name="mytest",
         urlPatterns={"/"})
 public class DummyServlet  extends javax.servlet.http.HttpServlet {
     private static Random random = new Random();
 
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         HttpSession session = req.getSession(true);
         ServletContext servletContext = session.getServletContext();
         Long invocations = (Long) servletContext.getAttribute("invocation times");
         if(invocations==null){
             invocations = 1L;
         }else{
             invocations++;
         }
         servletContext.setAttribute("invocation times", invocations);
         session.setAttribute("dummySessionValue", random.nextLong());
         req.setAttribute("dummySessionValue", new DummyParent());
        req.setAttribute("test", new Test());
 
         RequestDispatcher rd = req.getRequestDispatcher("/index.jsp");
         rd.forward(req, resp);
     }
 }
