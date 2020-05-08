 package com.UBC417.A1.Tests;
 
 import java.io.IOException;
 import java.util.Random;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.UBC417.A1.Data.Seat;
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 
 @SuppressWarnings("serial")
 public class TestSeatReservation extends HttpServlet {
 
 	static Random r = new Random();
 
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException, ServletException {
 
 		// Get parameters
		//Key FlightKey = KeyFactory.stringToKey(req.getParameter("FlightName"));
		String FlightID = req.getParameter("FlightName");
 		String FirstName = req.getParameter("FirstName");
 		String LastName = req.getParameter("LastName");
 
 		// use random seat
 		int i = r.nextInt(200);
 
 		// get seat leter
 		char c = 'A';
 		c += i % 4;
 
 		// get seat row
 		int j = i / 4 + 1;
 
 		// create seatID
 		String SeatID = String.format("%d%c", j, c);
 
 		try {
			if (!Seat.ReserveSeat(SeatID, FirstName, LastName)) {
 				// Didn't reserve seat, o well, don't show errors for test
 			}
 		} catch (EntityNotFoundException e) {
 			// Assume this wont happen, as long as tester provides proper data.
 		}
 
 	}
 }
