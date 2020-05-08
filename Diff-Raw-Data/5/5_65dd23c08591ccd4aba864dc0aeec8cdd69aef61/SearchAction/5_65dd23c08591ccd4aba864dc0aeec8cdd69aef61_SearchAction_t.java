 package com.nclodger.control.action.search;
 
 import com.nclodger.control.action.Action;
 import com.nclodger.myexception.MyException;
 import com.nclodger.webservices.ExpediaSearcher;
 import com.nclodger.webservices.Hotel;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Miredean
  * Date: 31.10.13
  * Time: 12:47
  * To change this template use File | Settings | File Templates.
  */
 public class SearchAction extends Action {
 
     @Override
     public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
         //test log4j logging
         new MyException("search queried");
 
         ExpediaSearcher searcher = new ExpediaSearcher();
         // String state,String city, Date arrivalDate, Date departureDate, Integer adults, Integer response_count
         String checkin = request.getParameter("checkindate");
         String checkout = request.getParameter("checkoutdate");
 
         String city = request.getParameter("city");
         String country = request.getParameter("country");
         String min_price = request.getParameter("min_price");
         String max_price = request.getParameter("max_price");
         String guests_children = request.getParameter("guests_children");
         String guests_adults = request.getParameter("guests_adults");
         String currency = request.getParameter("currency");
 
         request.getSession().setAttribute("checkindate",checkin);
         request.getSession().setAttribute("checkoutdate",checkout);
         request.getSession().setAttribute("country",country);
         request.getSession().setAttribute("city",city);
         request.getSession().setAttribute("min_price",min_price);
         request.getSession().setAttribute("max_price",max_price);
         request.getSession().setAttribute("guests_children",guests_children);
         request.getSession().setAttribute("guests_adults",guests_adults);
         request.getSession().setAttribute("currency", currency);
 
        String results = searcher.searchHotels(country,city,checkin,checkout,currency,
                Integer.parseInt(guests_adults),Integer.parseInt(guests_children));
         JSONObject resp = searcher.parseResults(results);
         List<Hotel> hotels = searcher.getHotelsList(resp);
         request.setAttribute("hotels",hotels);
         request.getSession().setAttribute("hotels",hotels);

         return "home";
     }
 }
