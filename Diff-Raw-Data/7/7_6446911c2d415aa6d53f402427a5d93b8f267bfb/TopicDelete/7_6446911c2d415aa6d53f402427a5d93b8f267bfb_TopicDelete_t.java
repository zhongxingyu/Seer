 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package processors;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author seth
  */
 public class TopicDelete extends HttpServlet {
 
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
 	PrintWriter out = response.getWriter();
 	try {
 	    int id = Integer.parseInt(request.getParameter("id"));
 	    Connection cn = dbutils.DBUtils.conn();
	    PreparedStatement st = cn.prepareStatement("DELETE t "
		    + "FROM topic t "
		    + "LEFT OUTER JOIN question q ON q.topic_id = t.id "
		    + "LEFT OUTER JOIN answer a ON a.question_id = q.id "
		    + "WHERE t.id = ?");
 	    st.setInt(1, id);
 	    st.executeUpdate();
 	    st.close();
 	    cn.close();
 	} catch (SQLException ex) {
 	    Logger.getLogger(TopicDelete.class.getName()).log(Level.SEVERE, null, ex);
 	} finally {	    
 	    response.sendRedirect("admin_dashboard.jsp");
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
