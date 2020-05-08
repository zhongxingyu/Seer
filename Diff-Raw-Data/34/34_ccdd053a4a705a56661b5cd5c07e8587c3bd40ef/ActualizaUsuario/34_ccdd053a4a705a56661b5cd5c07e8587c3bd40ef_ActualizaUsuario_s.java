 /*
  * To change this template, choose Tools | Templates
  * and open the templato in the editor.
  */
 package Servlet.Usuario;
 
 import Controlador.Query;
 import Helper.Validacion;
 import Modelo.Usuario;
 import java.io.IOException;
 import java.util.ArrayList;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author juanjo
  */
 public class ActualizaUsuario extends HttpServlet {
     public static final String URL_VISTA="/WEB-INF/Usuario/actualizaUsuario.jsp";
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
         Usuario usuario = (Usuario) request.getSession().getAttribute(Usuario.NOMBRE_TABLA);
         Integer id = usuario.getId();
         String password = request.getParameter("password");
         String confirmaPassword = request.getParameter("confirmaPassword");
         String nombre = request.getParameter("nombre");
         String apellidoP = request.getParameter("apellidoP");
         String apellidoM = request.getParameter("apellidoM");
         String email = request.getParameter("email");
        Integer tipo = 1;
         Query q = new Query();
         
         Usuario u = new Usuario(password, confirmaPassword, id, nombre, apellidoP, apellidoM, email, tipo);
         
         ArrayList<String> errores = Validacion.valida(u);
         if(errores.size()>0){
             request.setAttribute("errores", errores);
             request.setAttribute("usuario", u);
             request.getRequestDispatcher(URL_VISTA).forward(request, response);
             return;
         }
         
         if(q.existeUsuarioConEmail(email)!=null){
             request.setAttribute(Usuario.NOMBRE_TABLA, u);
             request.setAttribute("errores", Validacion.creaError("Ya existe un usuario con ese email"));
             request.getRequestDispatcher(URL_VISTA).forward(request, response);
         }
         
         if(q.actualizaUsuarioBD(u)){
             Usuario usuarioActualizado = q.getUsuarioBD(u.getId());
             request.getSession().setAttribute(Usuario.NOMBRE_TABLA, usuarioActualizado);
             request.setAttribute("usuario", usuarioActualizado);
             request.setAttribute("noError", "Tu cuenta ha sido actualizada exitosamente.");
             request.getRequestDispatcher(URL_VISTA).forward(request, response);
         }else{
             request.setAttribute("errores", Validacion.creaError("Lo sentimos, tu cuenta no puede ser actualizada en este momento."));
             request.getRequestDispatcher(URL_VISTA).forward(request, response);
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
         Usuario u = (Usuario) request.getSession().getAttribute(Usuario.NOMBRE_TABLA);
         request.setAttribute("usuario", u);
         request.getRequestDispatcher(URL_VISTA).forward(request, response);
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
