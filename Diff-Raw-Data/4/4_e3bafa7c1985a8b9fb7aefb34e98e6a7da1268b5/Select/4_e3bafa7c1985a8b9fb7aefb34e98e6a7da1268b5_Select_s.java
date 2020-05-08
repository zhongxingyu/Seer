 package ufly.frs;
 
 import java.io.IOException;
 import java.text.ParsePosition;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Vector;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import ufly.entities.*;
 
 
 @SuppressWarnings("serial")
 public class Select extends UflyServlet {
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException,ServletException
 	{
 		/**
 		 * we probably got here because of a redirect from login,
 		 * because we were required to login to book a flight
 		 */
 		String departopt;
 		String returnopt;
 		HttpSession session= req.getSession();
 		if(session.getAttribute("departopt")!=null)
 		{
 			departopt=(String) session.getAttribute("departopt");
 			returnopt=(String) session.getAttribute("returnopt");
 			Integer numPass = (Integer) session.getAttribute("numPass");
 			session.setAttribute("departopt",null);
 			session.setAttribute("returnopt", null);
 			session.setAttribute("numPass", null);
 			buildPage(departopt,returnopt,numPass,req,resp);
 		}else{
 			resp.sendRedirect("/");
 		}
 		
 	}
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException,ServletException
 	{
 		/**
 		 * We need to be logged in at this point, If we are not logged in, redirect
 		 * them to login page. before we do that we want to save the flights that they are
 		 * trying to book. We'll do that in the session.
 		 */
 		User loggedInUser = getLoggedInUser(req.getSession());
 		
 		if(loggedInUser == null)
 		{
 //		//DEBUGGING	--remove when done
 //			//if no user logged in, log user in
 //			login("email",req.getSession());
 //		}else if (false){
 //		//ENDDEBUGGING
 			HttpSession session= req.getSession();
 			session.setAttribute("departopt",req.getParameter("departopt") );
 			session.setAttribute("returnopt", req.getParameter("returnopt"));
 			session.setAttribute("numPass", Integer.parseInt((String)req.getParameter("numPassengers")));
			resp.sendRedirect("/login?message=cantBookWithoutUser");
 			return;
 		}
 		/**
 		 * make sure the session attributes are cleared
 		 */
 		String departopt= req.getParameter("departopt");
 		String returnopt=req.getParameter("returnopt");
 		Integer numPass=Integer.parseInt((String)req.getParameter("numPassengers"));
 		buildPage(departopt,returnopt,numPass,req,resp);
 		
 	}
 
 	private void buildPage(String departopt,String returnopt,Integer numPass,HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException
 	{
 		List<Flight> FlightList=new Vector<Flight>();
 		addFlightsToFlightList(FlightList,departopt);
 		if(returnopt!=null)
 		{
 			addFlightsToFlightList(FlightList,returnopt);
 		}
 		
 		Vector<HashMap<String,Object>> allFlightsInfo = new Vector<HashMap<String,Object>>(); 
 		Integer priceInCents=0;
 		for(Flight f:FlightList)
 		{
 			HashMap<String,Object> hm = f.getHashMap();
 			allFlightsInfo.add(hm);
 			priceInCents+=f.getPriceInCents();
 		}
 		priceInCents *= numPass;
 		String price="$"+new Integer(priceInCents/100).toString()+".";
 		priceInCents%=100;
 		Customer loggedInUser=null;
 		if(priceInCents<10){
 			price+="0";
 		}
 		try{
 			loggedInUser = (Customer)getLoggedInUser(req.getSession());
 		}catch(ClassCastException e){
			resp.sendRedirect("/?errorMsg=Must%20be%20logged%20in%20as%20Customer%20to%20book");
 			return;
 		}
 		price+=priceInCents.toString();
 		req.setAttribute("TotalCostString",price);
 		req.setAttribute("loyaltyPoints",loggedInUser.getLoyaltyPoints());
 		req.setAttribute("flightInfo", allFlightsInfo);
 		req.setAttribute("numPassengers", numPass);
 		req.getRequestDispatcher("bookFlightNew.jsp").forward(req, resp);
 		
 	}
 	
 	private void addFlightsToFlightList(List<Flight> flightList,
 			String flightsStr) {
 		SimpleDateFormat convertToDate = new SimpleDateFormat("yyyy/MM/dd HH:mm");
 		String[] flights=flightsStr.split("\\|");
 
 		for(int i=0;i<flights.length;i++){
 			if(flights[i].equals("")){
 				break;
 			}
 			String flightNo = flights[i].split("_")[0];
 			Date departureTime =  convertToDate.parse(flights[i].split("_")[1], new ParsePosition(0));
 			flightList.add(Flight.getFlight(flightNo,departureTime));
 		}
 		
 	}
 
 
 }
