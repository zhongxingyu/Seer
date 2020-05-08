 package ufly.frs;
 
 import java.io.IOException;
 import java.util.Date;
 import java.text.ParsePosition;
 import java.text.SimpleDateFormat;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import ufly.entities.*;
 @SuppressWarnings("serial")
 public class BookCreate extends UflyServlet {
 	public void doGet(HttpServletRequest req, HttpServletResponse resp){
 		try {
 			resp.sendRedirect("/");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException,ServletException
 	{
 		//printParam(req,resp)
 		SimpleDateFormat convertToDate = new SimpleDateFormat("yyyy/MM/dd HH:mm");
 		printParam(req,resp);
//		String CreditCardNo = (String)req.getParameter("creditCard");
 		Integer numberOfFlights= Integer.parseInt(req.getParameter("numberOfFlights"));
 		Integer numberOfPassengers= Integer.parseInt(req.getParameter("numberOfPassengers"));
 		//check to see if there is a user logged in
 		Customer localUser = (Customer) getLoggedInUser(req.getSession());
 		if (localUser==null)
 		{
 			//should not happen
 			throw new NullLoginUser();
 		}
 		for(Integer paxNo=0;paxNo<numberOfPassengers;paxNo++)
 		{
 			for(Integer flightNum=0;flightNum<numberOfFlights;flightNum++)
 			{
 				//Parse all the Posted values
 				String FlightNo = req.getParameter("FlightNo"+paxNo.toString()+"_"+flightNum.toString());
 				String departureStr = req.getParameter("Date"+paxNo.toString()+"_"+flightNum.toString());
 				String[] seatStr = req.getParameter("Seat"+paxNo.toString()+"_"+flightNum.toString()).split(" ");
 				Meal meal = Meal.valueOf(req.getParameter("meal"+paxNo.toString()+"_"+flightNum.toString()));
 				
 				Date departureDate = convertToDate.parse(departureStr, new ParsePosition(0));
 				Flight f=Flight.getFlight(FlightNo, departureDate);
 				Seat seat=f.getSeatingArrangement().getSeatByRowCol(Integer.parseInt(seatStr[0]),seatStr[1].charAt(0) );
				FlightBooking fb= new FlightBooking(localUser,f,seat,meal);
 				//Now associate this flightbooking with a 
 				seat.setFlightBooking(fb.getConfirmationNumber());
 				f.addFlightBooking(fb.getConfirmationNumber());
 				localUser.addFlightBooking(fb.getConfirmationNumber());
 				localUser.addLoyaltyPoints(f.getPriceInCents()/100);
 			}
 		}
 		resp.sendRedirect("/_ah/admin/datastore?kind=Customer");
 	}
 }
