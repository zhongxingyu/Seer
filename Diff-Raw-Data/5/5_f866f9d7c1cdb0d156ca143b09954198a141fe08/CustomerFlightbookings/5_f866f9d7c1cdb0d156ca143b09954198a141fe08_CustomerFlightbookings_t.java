 package ufly.frs;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.*;
 
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 
 import ufly.entities.Customer;
 import ufly.entities.FlightBooking;
 import ufly.entities.Meal;
 
 @SuppressWarnings("serial")
 public class CustomerFlightbookings extends UflyServlet {
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 		throws IOException,ServletException
 	{	
 		
 		String pageToInclude= getServletConfig().getInitParameter("action");
 		String confirmationNumberStr = (String) req.getParameter("confirmationNumber");
		Long confirmNumber=null;
		if(confirmationNumberStr!= null){
			confirmNumber= Long.valueOf(confirmationNumberStr);
		}
 		FlightBooking editFlightbooking=null;
 		if(confirmNumber != null){
 			editFlightbooking = FlightBooking.getFlightBooking(confirmNumber);
 		}
 		
 		if(pageToInclude.equals("index") )
 		{
 			Customer loggedInCustomer=null;
 			try {
 				loggedInCustomer = Customer.getCustomer(getLoggedInUser(req.getSession()).getEmailAddr());
 			} catch (UserInactivityTimeout e) {
 				resp.sendRedirect("/?errorMsg=Sorry, you have been logged out because you have been inactive too long");
 			}
 			Vector<Key> allFlightbookings = loggedInCustomer.getFlightBookings();
 			req.setAttribute("allFlightbookings", allFlightbookings);
 			req.getRequestDispatcher("/customerFlightbookings.jsp")
 			.forward(req,resp);
 		}else if (pageToInclude.equals("edit") )
 		{
 			if(editFlightbooking != null){
 				req.setAttribute("editFlightbooking", editFlightbooking.getHashMap());
 				req.setAttribute("meals", editFlightbooking.getBookedFlight().getAllowableMeals());
 				req.setAttribute("flight",editFlightbooking.getBookedFlight().getHashMap());
 				req.getRequestDispatcher("/customerFlightbookings_edit.jsp")
 				.forward(req,resp);
 			}
 		}else if (pageToInclude.equals("delete") )
 		{
 			if(confirmNumber != null){
 				FlightBooking deleteFlightbooking = FlightBooking.getFlightBooking(confirmNumber);
 				if(deleteFlightbooking != null){
 					deleteFlightbooking.deleteFlightBooking();
 					req.getRequestDispatcher("/customerFlightbookings")
 					.forward(req,resp);
 				}
 			}
 		}
 		else if (pageToInclude.equals("show") )
 		{
 			if(confirmNumber != null){
 				FlightBooking showFlightbooking = FlightBooking.getFlightBooking(confirmNumber);
 				if(showFlightbooking != null){
 					req.setAttribute("showFlightbooking", showFlightbooking);
 
 					req.getRequestDispatcher("/customerFlightbookings_show.jsp")
 					.forward(req,resp);
 				}
 			}
 			
 		}
 		else{
 		
 		List<FlightBooking> allFlightbookings = FlightBooking.getAllFlightBookings();
 		req.setAttribute("allFlightbookings", allFlightbookings);
 			req.getRequestDispatcher("/customerFlightbookings.jsp")
 			.forward(req,resp);
 		}
 		//}
 		//else{
 		//	resp.sendRedirect("/");
 		//}
 	}
 
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
 		throws IOException,ServletException
 	{
 		resp.setContentType("text/plain");
 		String pageToInclude= getServletConfig().getInitParameter("action");
 		
 		if (pageToInclude.equals("check-in") )
 		{
 			String confirmationNumber = (String) req.getParameter("confirmationNumber");
 			Long confirmNumber = Long.valueOf(confirmationNumber);
 			if(confirmNumber != null){
 				FlightBooking showFlightbooking = FlightBooking.getFlightBooking(confirmNumber);
 				if(showFlightbooking != null){
 					showFlightbooking.checkIn();
 					req.setAttribute("showFlightbooking", showFlightbooking);
 					req.getRequestDispatcher("/customerFlightbookings_show.jsp")
 					.forward(req,resp);
 				}
 			}
 		}else if (pageToInclude.equals("edit") )
 		{
 			String confirmationNumber = (String) req.getParameter("confirmationNumber");
 			Long confirmNumber = Long.valueOf(confirmationNumber);
 			if(confirmNumber != null){
 				FlightBooking editFlightbooking = FlightBooking.getFlightBooking(confirmNumber);
 				if(editFlightbooking != null){
 					Meal newMeal = Meal.valueOf(req.getParameter("meal"));
 					editFlightbooking.changeMealChoice(newMeal);
 					req.setAttribute("editFlightbooking", editFlightbooking);
 					req.getRequestDispatcher("/customerFlightbookings_edit.jsp")
 					.forward(req,resp);
 				}
 			}
 		}
 		
 	}
 }
