 /*
  * To change this template, choose Tools | Templates
  * and open the templato in the editor.
  */
 package Servlet.Comentario;
 
 import Controlador.Query;
import Helper.Fecha;
 import Modelo.Comentario;
 import Modelo.Usuario;
 import java.io.IOException;
 import java.util.Date;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author juanjo
  */
 public class CreaComentarioNoticia extends HttpServlet {
    public static String URL_VISTA = "/VerNoticia";
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
             throws ServletException, IOException {
         response.setContentType("text/html;charset=UTF-8");
         
         String descripcion = request.getParameter(Comentario.COL_DESCRIPCION);
         Date fecha = new Date();
         Integer idPadre = Integer.parseInt(request.getParameter("id"));
         Usuario usuario = (Usuario) request.getSession().getAttribute(Usuario.NOMBRE_TABLA);
         Integer idUsuario = usuario.getId();
         String nombreUsuario = usuario.getNombreUsuario();
         Comentario c = new Comentario(descripcion, fecha, idPadre, idUsuario, nombreUsuario);
         
         Query q = new Query();
         
         if(q.insertaComentarioNoticiaBD(c)){
             response.sendRedirect(URL_VISTA+"?id="+idPadre);
             //request.getRequestDispatcher(URL_VISTA+"?id="+idPadre).forward(request, response);
         }else{
             request.setAttribute("mensaje", "Lo sentimos, su comentario no puede ser enviado.");
             request.getRequestDispatcher(descripcion).forward(request, response);
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
         processRequest(request, response);
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
         processRequest(request, response);
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
