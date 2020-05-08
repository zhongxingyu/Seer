 package com.acme.fitness.webmvc.controllers;
 
 import java.util.Date;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.acme.fitness.domain.exceptions.FitnessDaoException;
 import com.acme.fitness.domain.users.User;
 import com.acme.fitness.orders.GeneralOrdersService;
 import com.acme.fitness.products.GeneralProductsService;
 import com.acme.fitness.users.GeneralUsersService;
 import com.acme.fitness.webmvc.user.UserManager;
 
 @Controller
 @RequestMapping("/edzesek")
 public class TrainingController {
 
 	@Autowired
 	private GeneralProductsService gps;
 
 	@Autowired
 	private GeneralUsersService gus;
 
 	@Autowired
 	private GeneralOrdersService gos;
 
 	@RequestMapping(value = "")
 	public String training(HttpServletRequest request) {
		request.getSession().setAttribute("trainers", gus.getAllTrainers());
 		return "edzesek";
 	}
 	
 	@RequestMapping(value = "/edzo/naptar")
 	public String getTrainerTrainings(HttpServletRequest request) {
		request.getSession().setAttribute("trainers", gus.getAllTrainers());
 		request.getSession().setAttribute("activeTrainer", request.getParameter("new-active-trainer-input"));
 		return "edzesek";
 	}
 	
 	@ResponseBody
 	@RequestMapping(value = "/ujedzes", method=RequestMethod.POST)
 	public String newTraining(@RequestParam("date") long dateInMs) {
 //		try {
 //			User trainer = gus.getUserByUsername((String)request.getSession().getAttribute("activeTrainer"));
 //			User client = gus.getUserByUsername(new UserManager().getLoggedInUserName());
 //		} catch (FitnessDaoException e) {
 //		}
 		String userFullName = "";
 		try {
 			User client = gus.getUserByUsername(new UserManager().getLoggedInUserName());
 			userFullName = client.getFullName();
 		} catch (FitnessDaoException e) {
 			userFullName = "anonymousgdfsgd";
 		}
 		System.out.println(new Date(dateInMs));
 		return userFullName;
 	}
 	
 }
