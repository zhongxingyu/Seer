 package com.wuhanyu.kilim;
 
 
 import kilim.Pausable;
 import kilim.Task;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.servlet.AsyncContext;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class Actor extends HttpServlet {
 
 
     public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws javax.servlet.ServletException, java.io.IOException {
         final AsyncContext ac = request.startAsync(request, response);
 
         new Task(){
             public  void execute() throws Pausable, Exception {
                 try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                 }finally{
                     ac.complete();
                 }
             }
         }.start();
     }
 
     public void doPost(HttpServletRequest req, HttpServletResponse resp) throws javax.servlet.ServletException, java.io.IOException {
         doGet(req,resp);
     }
 
     public static Logger log = LoggerFactory.getLogger(Actor.class);
 
 }
 
