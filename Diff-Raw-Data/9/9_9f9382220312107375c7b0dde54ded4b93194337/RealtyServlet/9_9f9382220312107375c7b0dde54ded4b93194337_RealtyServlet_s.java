 package org.realty;
 
 import org.realty.commands.Command;
 import org.realty.commands.CommandFactory;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class RealtyServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	public RealtyServlet() {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 		
 		
 		
 		service(request, response);
 	}
 
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	
 		service(request, response);
 	}
 
 	public void service(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
         String command = request.getParameter("command");
 		Command c = CommandFactory.getCommand(command);
 		String path = c.execute(request, response);
 		request.getRequestDispatcher(path).forward(request, response);
 
 	}
 
 }
