 package com.acme.fitness.webmvc.controllers;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 import com.acme.fitness.domain.exceptions.BasketCheckOutException;
 import com.acme.fitness.domain.exceptions.FitnessDaoException;
 import com.acme.fitness.domain.exceptions.StoreQuantityException;
 import com.acme.fitness.domain.orders.Basket;
 import com.acme.fitness.domain.products.Training;
 import com.acme.fitness.domain.users.User;
 import com.acme.fitness.products.GeneralProductsService;
 import com.acme.fitness.users.GeneralUsersService;
 import com.acme.fitness.webmvc.basket.BasketManager;
 import com.acme.fitness.webmvc.dto.AllStatedTrainings;
 
 @Controller
 @RequestMapping("/edzesek")
 public class TrainingController {
 	private static Logger logger = LoggerFactory.getLogger(TrainingController.class);
 
 	private GeneralUsersService generalUsersService;
 	private GeneralProductsService generalProductsService;
 	private BasketManager basketManager;
 
 	@Autowired
 	public TrainingController(GeneralUsersService generalUsersService, GeneralProductsService generalProductsService, BasketManager basketManager) {
 		this.generalUsersService = generalUsersService;
 		this.generalProductsService = generalProductsService;
 		this.basketManager = basketManager;
 	}
 
 	@RequestMapping(value = "")
 	public String training(HttpServletRequest request, HttpServletResponse response) {
 		request.setAttribute("trainers", generalUsersService.getAllTrainers());
 		basketManager.addBasketToSessionIfExists(request, response, new ObjectMapper());
 		basketManager.isAnonymousBasketIfUserLoggedIn(request, response, new ObjectMapper());
 		return "edzesek";
 	}
 
 	@ResponseBody
 	@RequestMapping(value = "/edzo/naptar", method = RequestMethod.POST)
 	public AllStatedTrainings getTrainerTrainings(HttpServletRequest request, @RequestParam("username") String username, @RequestParam("actualPageMonday") Date weeksMonday) {
 		AllStatedTrainings trainings = new AllStatedTrainings();
 
 		if (username != null && !username.equals("") && weeksMonday != null) {
 			try {
 				User trainer = generalUsersService.getUserByUsername(username);
 				trainings.setOrderedTrainings(generalProductsService.getTrainingsOnWeekByTrainer(trainer, weeksMonday));
 				trainings.setTrainingsInBasket(getTrainingsFromBasket(request));
 				logger.info("Ajax request to get trainer's trainings with Username: " + username + " monday: " + weeksMonday);
 			} catch (FitnessDaoException e) {
 				e.printStackTrace();
 			}
 		}
 		return trainings;
 	}
 
 	@RequestMapping(value = "/ujedzes", method = RequestMethod.POST)
 	public String newTraining(@RequestParam("trainer-username") String username, @RequestParam("training-date") Date trainingDate, HttpServletResponse response,
 			HttpServletRequest request) {
 		basketManager.addNewTraining(username, String.valueOf(trainingDate.getTime()), response, request, new ObjectMapper());
 		logger.info("Training add to basket with trainer:" + username + " , date: " + trainingDate);
 		
 		return "redirect:/edzesek";
 	}
 	
 	@RequestMapping(value = "/rendel")
 	public String confirmBasket(HttpServletResponse response, HttpServletRequest request, RedirectAttributes redirectAttributes){
 		try {
 			basketManager.checkOutBasket(response, request);
 		} catch (StoreQuantityException e) {
 			e.printStackTrace();
 		} catch (BasketCheckOutException e) {
 			return failToCheckOut(redirectAttributes);
 		}
 		return "redirect:/edzesek";
 	}
 	
 	@RequestMapping("/anonymKosar/hozzaad")
 	public String addAnonymousBasketToUser(HttpServletRequest request, HttpServletResponse response) {
 		basketManager.AddAnonymousBasketToLoggedInUserBasket(response, request, new ObjectMapper());
 		return "redirect:/edzesek";
 	}
 	
 	@RequestMapping("/torles/{trainingId}")
 	public String deleteTraining(@PathVariable long trainingId, HttpServletRequest request, HttpServletResponse response) {
 		basketManager.removeTrainingFromBasket(request, response);
 		return "redirect:/edzesek";
 	}
 
	@RequestMapping(value = "/torles/anonymous/{trainingId}")
	public String removeAnonymousTraining(@PathVariable long trainingId, HttpServletRequest request, HttpServletResponse response) {
		basketManager.removeAnonymousTraining(request, response);
 		return "redirect:/edzesek";
 	}
 	
 	private String failToCheckOut(RedirectAttributes redirectAttributes) {
 		redirectAttributes.addFlashAttribute("message", "Termék rendeléséhez be kell jelentkezni!");
 		return "redirect:/edzesek";
 	}
 	
 	private List<Training> getTrainingsFromBasket(HttpServletRequest request) {
 		List<Training> result=new ArrayList<Training>();
 		Basket basket=(Basket)request.getSession().getAttribute("basket");
 		
 		if(basket!=null){
 			result=basket.getTrainings();
 		}
 		
 		return result;
 	}
 
 }
