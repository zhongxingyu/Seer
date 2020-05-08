 package edu.colorado.csci3308.inventory.servlet;
 
 import java.io.IOException;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import edu.colorado.csci3308.inventory.Rack;
 import edu.colorado.csci3308.inventory.ServerDB;
 
 /**
  * Servlet implementation class AddRackServlet
  */
 @WebServlet("/AddRackServlet")
 public class AddRackServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String rackDesc = request.getParameter("RackDesc");
 		Integer maxHeight = Integer.valueOf(request.getParameter("RackMaxHeight"));
 		Integer depth = Integer.valueOf(request.getParameter("RackDepth"));
 		Integer width = Integer.valueOf(request.getParameter("RackWidth"));
		Integer widthStart = Integer.valueOf(request.getParameter("RackWidthStart"));
		Integer depthStart = Integer.valueOf(request.getParameter("RackDepthStart"));
 		
 		Rack r = new Rack(0,rackDesc, maxHeight, width, depth, widthStart, depthStart);
 		
 		ServerDB.setPath(request.getServletContext().getRealPath("WEB-INF/db/servers.db"));
 		ServerDB.addRack(r);
 		
 		response.sendRedirect("ServerList.jsp");
 	}
 
 }
