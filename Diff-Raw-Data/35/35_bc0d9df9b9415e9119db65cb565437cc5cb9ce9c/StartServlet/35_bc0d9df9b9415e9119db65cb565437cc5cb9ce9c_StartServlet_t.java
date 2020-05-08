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
        //Servlet Klasse mitschicken
        request.setAttribute("zugehoeriges Servlet", this.getClass().toString());                
        
        //konfiguration für PictureLoad mitschicken 
        File deployStream = new File("C:/Program Files/Apache Software Foundation/Tomcat 6.0/webapps/ROOT/git/UnterrichtMe/UnterrichtMeProjekt/web/pics/test2.png");       
        request.setAttribute("Pfad für die Erstellung von Bildern", deployStream.getPath());
        //Bilder laden im Hintergrund aktivieren
        request.getSession().setAttribute("task", new Task(deployStream));
        //some magic
         request.getRequestDispatcher("/git/UnterrichtMe/UnterrichtMeProjekt/build/web/jsp/index.jsp").forward(request, response);
 
     }
 
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
     }
 }
