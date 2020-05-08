 package com.uc3.onlineStore.controller;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.io.*;
 import java.util.*;
import javax.servlet.annotation.WebServlet;
 
 
@WebServlet(name="SectionsController", loadOnStartup = 1, urlPatterns = {"/category","/young","/manAndWomen","/kids","/sports","informations"})
 
 public class SectionsController extends HttpServlet {
 
     private static final long serialVersionUID = 1L;
     private boolean debug = false;
 
     /**
      * @see HttpServlet#HttpServlet()
      */
     public SectionsController() {
         super();
         // TODO Auto-generated constructor stub
     }
     
     /**
      * Handles the HTTP <code>GET</code> method.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
      
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
         String pathInfo = request.getServletPath();
 	String[] urlParsed= pathInfo.split("/");
 	String url="index", section = "";
 
 	if (debug) {System.out.println("pathInfo: "+pathInfo);}
 
 	if (urlParsed.length >= 2) section = urlParsed[1];
 
         if (section.equals("manAndWomen")) {
             url = "ManWomanMain";
         } else if (section.equals("kids")) {
             url = "KidsMain";
 	} else if (section.equals("category")) {
 	    url = "category";
 	} else if (section.equals("sports")) {
 	    url = "SportsMain";
 	} else if (section.equals("young")) {
 	    url = "YoungMain";
 	} else if (section.equals("informations")) {
 	    url = "informations";
 	}
 	
         // use RequestDispatcher to forward request internally
 	url = "/" + url + ".jsp";
 	
 	try {
 	    this.getServletContext().getRequestDispatcher(url).forward(request, response);
         } catch (Exception e) {
 	    if(debug) System.out.println("Exception: " + e.getMessage());
             this.getServletContext().getRequestDispatcher("/index.jsp").forward(request, response);
             return;
 	}
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
 
         // if addToCart action is called
         if (userPath.equals("/addToCart")) {
             // TODO: Implement add product to cart action
 
 	    // if updateCart action is called
         } else if (userPath.equals("/updateCart")) {
             // TODO: Implement update cart action
 
 	    // if purchase action is called
         } else if (userPath.equals("/purchase")) {
             // TODO: Implement purchase action
 
             userPath = "/confirmation";
         }
 
         // use RequestDispatcher to forward request internally
         String url = "/WebContent/" + userPath + ".jsp";
 
         try {
             request.getRequestDispatcher(url).forward(request, response);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
     }
   
   
 }
 
