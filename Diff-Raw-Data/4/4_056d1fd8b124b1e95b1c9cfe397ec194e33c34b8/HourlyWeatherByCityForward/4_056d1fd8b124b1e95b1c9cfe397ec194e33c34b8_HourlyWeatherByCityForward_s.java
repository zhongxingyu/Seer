 package com.hourlyweather.web;
 
 import java.io.IOException;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 
 @SuppressWarnings("serial")
 public class HourlyWeatherByCityForward extends HttpServlet {
     @Override
     protected void doGet(HttpServletRequest request,
 	    HttpServletResponse response) throws ServletException, IOException {
 	
 	Key cityKey = KeyFactory.createKey(City.ENTITY, request.getParameter("id"));
 
 	//query database for city geo data
 	DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
 	
 	City city;
 	try {
 	    city = new City(ds.get(cityKey));
 	} catch (EntityNotFoundException e) {
 	    throw new LocationException(cityKey + " wasn't found.");
 	} 
 		
 	request.setAttribute("city", city.getName());
 	request.setAttribute("lat", city.getLatitude());
 	request.setAttribute("lon", city.getLongitude());
 	request.setAttribute("timeOffset", city.getTimezone());
 	
 	RequestDispatcher dispatcher = getServletContext()
 		.getRequestDispatcher("/");
 	dispatcher.forward(request, response);
     }
 }
