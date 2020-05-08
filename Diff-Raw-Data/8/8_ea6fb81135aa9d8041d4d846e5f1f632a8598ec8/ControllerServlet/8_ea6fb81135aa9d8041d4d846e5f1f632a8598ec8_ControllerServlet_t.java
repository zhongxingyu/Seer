 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mwr.controller;
 
 import com.mwr.database.DeviceId;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.security.NoSuchAlgorithmException;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.ejb.EJB;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.json.simple.parser.ContainerFactory;
 import org.json.simple.parser.JSONParser;
 
 /**
  *
  * @author madenem
  */
 @WebServlet(name = "ControllerServlet", 
          loadOnStartup = 1,
             urlPatterns = {"/requestRegistration"})
 public class ControllerServlet extends HttpServlet {
 
     
     @Override
     public void init() throws ServletException {
         
         // store category list in servlet context
 
         
        
         
         //System.out.println(techFacade.findAll().size());
 
     }
     
     /**
      * Processes requests for both HTTP
      * <code>GET</code> and
      * <code>POST</code> methods.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
  @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
     throws ServletException, IOException {
 
 //        String userPath = request.getServletPath();
 //
 //       
 //        if (userPath.equals("/technicians")) {
 //           
 //            userPath = "/admin_Technicians_List";        
 //        } 
 //        
 //        else if (userPath.equals("/employees")) {
 //
 //            // TODO: Implement category request
 //            getServletContext().setAttribute("employees", empFacade.findAll());
 //            userPath = "/admin_Employees_List";
 //        // if cart page is requested
 //        }
 //        
 //        else if (userPath.equals("/register")) {
 //
 //            // TODO: Implement category request
 //            userPath = "/devRegister";
 //        // if cart page is requested
 //        }
 //         else if (userPath.equals("/devices"))
 //        {
 //             getServletContext().setAttribute("devices", devFacade.findAll());
 //        }
 //        else if (userPath.equals("/waitingList")) 
 //        {
 //            getServletContext().setAttribute("waitinglist", devNotRegisteredFacade.findAll());
 //        }
 //        else if (userPath.equals("/scans")) 
 //        {
 //            getServletContext().setAttribute("scans", scanFacade.findAll());
 //            userPath = "/device_Log";
 //        }
 //
 //        // use RequestDispatcher to forward request internally
 //        String url = "/WEB-INF/view" + userPath + ".jsp";
 //
 //        try {
 //            request.getRequestDispatcher(url).forward(request, response);
 //        } catch (Exception ex) {
 //            ex.printStackTrace();
 //        }
     }
 
     /**
      * Handles the HTTP <code>POST</code> method.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
     throws ServletException, IOException {
 
         String userPath = request.getServletPath();
 
         if(userPath.equals("/requestRegistration")) 
         {        
             BufferedReader reader = request.getReader();
             String jsonText = reader.readLine();         
             JSONParser parser = new JSONParser();
             
             ContainerFactory containerFactory = new ContainerFactory(){
             public List creatArrayContainer() {
                 return new LinkedList();
               }
 
               public Map createObjectContainer() {
                 return new LinkedHashMap();
               }
 
             };         
                         
            Logger.getLogger(ControllerServlet.class.getName()).info(jsonText); 
            String mac = "";
            String serial = "";
            String androidID = "";
            String make = "";
            String model = "";
            String username = "";
            String password = "";
            String name = "";
            String surname = "";
            String id= "" ;
 
             try{
               Map json = (Map)parser.parse(jsonText, containerFactory);
               Iterator iter = json.entrySet().iterator();             
               while(iter.hasNext()){
                 Map.Entry entry = (Map.Entry)iter.next();
                 String key = entry.getKey().toString();
                 String value = entry.getValue().toString();
                 Logger.getLogger(ControllerServlet.class.getName()).info(key); 
                 Logger.getLogger(ControllerServlet.class.getName()).info(value); 
                 if (key.equals("mac"))
                     mac = value;
                 else if (key.equals("serial"))
                     serial = value;
                 else if (key.equals("android"))
                     androidID = value;
                 else if (key.equals("make"))
                     make = value;
                 else if (key.equals("model"))
                     model = value;
                 else if (key.equals("username"))
                     username = value;
                 else if (key.equals("password"))
                     password = value;
                 else if (key.equals("name"))
                     name = value;
                 else if (key.equals("surname"))
                     surname = value;
                 else if (key.equals("id"))
                     id = value;
                 
               }
               
              Logger.getLogger(ControllerServlet.class.getName()).info(mac); 
             } catch (org.json.simple.parser.ParseException ex) {
                 Logger.getLogger(ControllerServlet.class.getName()).log(Level.SEVERE, null, ex);
             }
            
             try
             {
                 DatabaseJSFManagedBean bean = (DatabaseJSFManagedBean) request.getSession().getAttribute("bean");
                 if (bean == null)
                 {
                      bean = new DatabaseJSFManagedBean();
                 }
                 bean.addToWaitingList(mac,androidID,serial,make, model, username, password, id, name,surname);
             }
             catch ( NoSuchAlgorithmException e)
             {
                  Logger.getLogger(ControllerServlet.class.getName()).log(Level.INFO, null, e);
             }
 
         
            
         }
        else if(userPath.equals("/scanResults")) 
        {
            
        }
        
 //        else if (userPath.equals(("/scanResults")))
 //        {
 //            boolean rooted = Boolean.parseBoolean(request.getParameter("rooted"));
 //            boolean debug = Boolean.parseBoolean(request.getParameter("debug"));
 //            boolean unknown = Boolean.parseBoolean(request.getParameter("unknown"));
 //            String apps = request.getParameter("apps");
 //            scanMan.addScan(rooted, debug, unknown, apps);
 //            
 //            
 //            userPath = "/devices";
 //            
 //        }
 //
 //
 //        // use RequestDispatcher to forward request internally
 //        String url = "/WEB-INF/view" + userPath + ".jsp";
 //
 //        try {
 //            request.getRequestDispatcher(url).forward(request, response);
 //        } catch (Exception ex) {
 //            ex.printStackTrace();
 //        }
     }
 
 
     // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
     /**
      * Handles the HTTP
      * <code>GET</code> method.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
 
 }
