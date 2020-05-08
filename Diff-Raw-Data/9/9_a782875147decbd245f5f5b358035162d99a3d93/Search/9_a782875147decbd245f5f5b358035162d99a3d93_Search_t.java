 package ufly.frs;
 
 import java.io.IOException;
 import java.text.ParsePosition;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import ufly.entities.Airport;
 import ufly.entities.Flight;
 
 @SuppressWarnings("serial")
 public class Search extends UflyServlet {
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException, ServletException {
 		resp.sendRedirect("/");
 	}
 
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException, ServletException {
 		// printParam(req,resp);
 		req.setAttribute("origin", req.getParameter("origin"));
 		req.setAttribute("destination", req.getParameter("destination"));
 		req.setAttribute("departureDate", req.getParameter("departureDate"));
 		req.setAttribute("returnDate", req.getParameter("returnDate"));
 		String s = req.getParameter("oneWayOrReturn").toString();
 		s.toString();
 		req.setAttribute("oneWayOrReturn", req.getParameter("oneWayOrReturn"));
 		req.setAttribute("numPassengers",
 				(String) req.getParameter("numPassengers"));
 
 		Date departureDate = null;
 		Airport origin = null;
 		Date returnDate = null;
 		Airport destination = null;
 		SimpleDateFormat convertToDate = new SimpleDateFormat("MM-dd-yyyy");
 		if (req.getParameter("departureDate") != null) {
 			departureDate = convertToDate.parse(
 					req.getParameter("departureDate"), new ParsePosition(0));
 		}
 		if (req.getParameter("returnDate") != null) {
 			returnDate = convertToDate.parse(req.getParameter("returnDate"),
 					new ParsePosition(0));
 		}
 		if (returnDate == null) {
 			returnDate = (Date) departureDate.clone();
 		}
 		if (req.getParameter("origin") != null) {
 			origin = Airport.getAirportByCallSign(req.getParameter("origin"));
 		}
 		if (req.getParameter("destination") != null) {
 			destination = Airport.getAirportByCallSign(req
 					.getParameter("destination"));
 		}
 		if (origin != null) {
 			Vector trips = getFlightsFromOrigToDestOnDate(origin, destination,
 					departureDate);
 			req.setAttribute("thereTrips", trips);
 			trips = getFlightsFromOrigToDestOnDate(destination, origin,
 					returnDate);
 			req.setAttribute("returnTrips", trips);
 			req.getRequestDispatcher("searchResults.jsp").forward(req, resp);
 		}
 
 	}
 
 	private Vector<Vector<HashMap<String, Object>>> getFlightsFromOrigToDestOnDate(
 			Airport origin, Airport destination, Date departureDate) {
 
 		List<Flight> flights = Flight.getFlightsOriginDate(origin,
 				departureDate);
 		Vector<Vector<HashMap<String, Object>>> toRet = new Vector<Vector<HashMap<String, Object>>>();
 		Iterator<Flight> it = flights.iterator();
 		while (it.hasNext()) {
 			Flight f = it.next();
 			if (f.getDestination().equals(destination))
 			// this flight goes right to the destination
 			{
 				Vector trip = new Vector<HashMap<String, Object>>(1);
 
 				HashMap flightAttributes = f.getHashMap();
 				trip.add(flightAttributes);
 				toRet.add(trip);
 			} else {
 				/*
 				 * This flight does not go directly there,check to see if there
 				 * are any flights going from it to our destination
 				 */
 				List<Flight> connectingFlights = Flight
 						.getFlightsConnectingDateTime(f.getDestination(),
 								destination, f.getArrival());
 				Iterator<Flight> connIt = connectingFlights.iterator();
 				HashMap<String, Object> firstLegAttr = f.getHashMap();
 
 				while (connIt.hasNext()) {
 					Vector trip = new Vector<HashMap<String, Object>>(2);
 					trip.add(firstLegAttr);
 					trip.add(connIt.next().getHashMap());
 					toRet.add(trip);
 				}
 			}
 		}
 		//Collections.sort(toRet, new TripDepartureSort());
 		return toRet;
 	}
 
 	private class TripDepartureSort implements
 			Comparator<Vector<HashMap<String, Object>>> {
 		@Override
 		public int compare(Vector<HashMap<String, Object>> o1,
 				Vector<HashMap<String, Object>> o2) {
			if (((Date) o1.get(0).get("departs")).before((Date) o2.get(0)
					.get("departs")))
 				return -1;
			if (((Date) o1.get(0).get("departs")).after((Date) o2.get(0).get(
					"departs")))
 				return 1;
 			return 0;
 		}
 	}
 
 	private class TripDurationSort implements
 			Comparator<Vector<HashMap<String, Object>>> {
 		@Override
 		public int compare(Vector<HashMap<String, Object>> o1,
 				Vector<HashMap<String, Object>> o2) {
 			long firstDuration = getTotalDuration(o1);
 			long secondDuration = getTotalDuration(o2);
 			if (firstDuration < secondDuration)
 				return -1;
 			if (firstDuration > secondDuration)
 				return 1;
 			return 0;
 		}
 
 		private Long getTotalDuration(Vector<HashMap<String, Object>> arg) {
 
 			return ((Date) arg.lastElement().get("arrives")).getTime()
 					- ((Date) arg.firstElement().get("departs")).getTime();
 
 		}
 
 	}
 
 }
