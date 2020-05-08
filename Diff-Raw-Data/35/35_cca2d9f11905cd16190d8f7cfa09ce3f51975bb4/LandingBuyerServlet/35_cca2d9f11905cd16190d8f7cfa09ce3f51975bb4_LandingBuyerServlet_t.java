 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package servlets;
 
 import db.*;
 import helpers.PageHelper;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.SQLException;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author Samuele
  */
 //pagina iniziale per un buyer dopo essersi loggato, mostra le categorie dei prodotti e gli ordini precedenti
 public class LandingBuyerServlet extends HttpServlet {
     
     private DBManager manager;
 
     @Override
     public void init() throws ServletException {
         // inizializza il DBManager dagli attributi di Application
         this.manager = (DBManager) super.getServletContext().getAttribute("dbmanager");
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
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException, SQLException {
         response.setContentType("text/html;charset=UTF-8");
         List<Category> categorie = manager.getCategories();
         PrintWriter out = response.getWriter();
         PageHelper page = new PageHelper("Landing page - Buyer");
         String body = "";
         body += "<div class=\"container-fluid\" >\n";
         body += "<div class=\"row-fluid\">\n";
         body += "<div class=\"span4\"></div>\n";
         body += "<div class=\"span6 \">\n";
         body += "<h1>\n";
         body += "SiteName\n";
         body += "</h1>\n";
         body += "</div>\n";
         body += "<div class=\"span2\">\n";
         body += "<a href=\"#\">Sign out</a>\n";
         body += "</div>\n";
         body += "</div>\n";
         body += "<!-- end of row-fluid -->\n";
         body += "<div class=\"row-fluid\">\n";
         body += "<div class=\"span10 offset1\">\n";
         body += "<div class=\"navbar\">\n";
         body += "<div class=\"navbar-inner \">\n";
         body += "<div class=\"container\">\n";
         body += "<!-- .btn-navbar is used as the toggle for collapsed navbar content -->\n";
         body += "<a class=\"btn btn-navbar\" data-toggle=\"collapse\" data-target=\".nav-collapse\">\n";
         body += "<span class=\"icon-bar\"></span>\n";
         body += "<span class=\"icon-bar\"></span>\n";
         body += "<span class=\"icon-bar\"></span>\n";
         body += "</a>\n";
         body += "<a class=\"brand\" href=\"#\">PRODOTTI</a>\n";
         body += "<!-- Everything you want hidden at 940px or less, place within here -->\n";
         body += "<div class=\"nav-collapse\">\n";
         body += "<ul class=\"nav\">\n";
         /*
          * Inizio del ciclo per mostrare le categorie
          * <li><a href=\"">Categoria</a></li>
          */
         for(Category categoria:categorie){
                 String relPath = "ProductsBuyer?cat=" + categoria.getId();
                         
                 body+="<li>"
                         + "<a href='"
                         + getServletContext().getContextPath()+"/"+relPath
                         + "'>"
                         + categoria.getName()
                         +"</a>"
                         + "</li>\n";
             }            
         /*
          * fine del ciclo
          */
         body += "</ul>\n";
         body += "</div>\n";
         body += "</div>\n";
         body += "</div>\n";
         body += "</div>\n";
         body += "</div>\n";
         body += "</div>\n";
        body += "<!-- end of row-fluid -->\n"; 
        body += "<div class=\"row-fluid\">";
        body += "<div class=\"span2\"></div>";
        body += "<div class=\"span10\">";
        body += "<h5>Username - Receipt List </h5>";
        body += "</div>";
        body += "</div>	";
        body += "<div class=\"row-fluid\">";
        body += "<div class=\"span10 offset1\">";
        body += "<table class=\"table table-striped\">";
        body += "<thead>";
        body += "<tr>";
        body += "<td>ID</td><td>RECEIPT</td><td>PRICE</td><td>DATE</td></b>";
        body += "</tr>";
        body += "</thead>";
        body += "<tbody>";
        /*
         * Inizio del ciclo per popolare la tabella delle ricevute
         * "<tr>";
         *  body += "<td>0100</td><td><a href='#'>url receipt</a></td><td>55e</td><td>10/12/2012</td>";
         *  body += "</tr>			
         */
        body += "<tr>";
        body += "<td>0100</td><td><a href='#'>url receipt</a></td><td>55e</td><td>10/12/2012</td>";
        body += "</tr>							";
        body += "</tbody>";
        body += "</table>";
        body += "</div>";
        body += "</div>	";
        body += "<!-- end of row-fluid -->";
        body += "</div>";
        body += "<!-- end of container-fluid -->";
 
         page.addContent(body);
 
         try {
             /* TODO output your page here. You may use following sample code. */
 
             out.println(page.getFullPage());
 
         } finally {
             out.close();
         }
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
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         try {
             processRequest(request, response);
         } catch (SQLException ex) {
             Logger.getLogger(LandingBuyerServlet.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     /**
      * Handles the HTTP
      * <code>POST</code> method.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         try {
             processRequest(request, response);
         } catch (SQLException ex) {
             Logger.getLogger(LandingBuyerServlet.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     /**
      * Returns a short description of the servlet.
      *
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "Short description";
     }// </editor-fold>
 }
