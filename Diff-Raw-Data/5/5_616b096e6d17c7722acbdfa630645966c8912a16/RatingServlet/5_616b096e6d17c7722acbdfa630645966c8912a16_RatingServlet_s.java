 package com.epam.lab.buyit.controller.web.servlet;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 
 import com.epam.lab.buyit.controller.service.rating.RatingService;
 import com.epam.lab.buyit.controller.service.rating.RatingServiceImpl;
 import com.epam.lab.buyit.model.Rating;
 
 public class RatingServlet extends HttpServlet {
 	private static final Logger LOGGER = Logger.getLogger(RatingServlet.class);
 	private static final long serialVersionUID = 1L;
 	private RatingService ratingService;
 	
 	public void init() {
 		ratingService = new RatingServiceImpl();
 	}
 
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		int value = Integer.parseInt(request.getParameter("rating"));
 		int id = Integer.parseInt(request.getParameter("id"));
 		int fromId = Integer.parseInt(request.getParameter("fromId"));
 		LOGGER.info("Recive mark " + value);
 		Rating rating = ratingService.findMark(fromId, id);
 		if(rating != null) {
 			LOGGER.info("Updating mark from " + fromId);
			rating.setRating(++value);
 			ratingService.updateItem(rating);
 		} else {
 			LOGGER.info("Creating mark from " + fromId);
 			rating = new Rating();
			rating.setFromId(fromId).setUserId(id).setRating(++value);
 			ratingService.createItem(rating);
 		}
 		
 		
 		int userRating = ratingService.getUserRating(id);
 		LOGGER.info("Sending updated rating " + userRating);
 		response.setContentType("text/plain");
 		response.setCharacterEncoding("UTF-8");
 		response.getWriter().write(String.valueOf(userRating));
 	}
 
 }
