 package ufly.frs_test;
 
 import java.io.IOException;
 
 import javax.jdo.PersistenceManager;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
import ufly.entities.PMF;
 import ufly.entities.SeatingArrangement;
 
 @SuppressWarnings("serial")
 public class SeatingArrangementTest extends HttpServlet {
 
 	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
 	{
 		req.getRequestDispatcher("SeatingArrangementTest.jsp").forward(req, resp);
 	}
 	
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
 		throws IOException,ServletException
 	{
 		String aircraftModel = req.getParameter("aircraftModel");
		SeatingArrangement newSeatingArrangement = new SeatingArrangement(aircraftModel); 
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		try {
             pm.makePersistent(newSeatingArrangement);
             
         } finally {
             pm.close();
         }
 		
 		//resp.setContentType("text/plain");
 		//resp.getWriter().println("Airplane: "+aircraftmodel);
 		resp.sendRedirect("/entityTest?test=SeatingArrangement");
 	}
 }
