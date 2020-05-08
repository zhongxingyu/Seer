 package epam.cdp.spring.task1.controller;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import javax.validation.Valid;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 import epam.cdp.spring.task1.bean.RegistrationUserBean;
 import epam.cdp.spring.task1.bean.Ticket;
 import epam.cdp.spring.task1.bean.TicketCategory;
 import epam.cdp.spring.task1.bean.User;
 import epam.cdp.spring.task1.dao.FilterCriteria;
 import epam.cdp.spring.task1.service.TicketService;
 import epam.cdp.spring.task1.service.TicketServiceBaseImpl;
 import epam.cdp.spring.task1.service.UserService;
 import epam.cdp.spring.task1.service.UserServiceBaseImpl;
 
 @Controller
 public class MainController {
 
 	private static final Logger logger = Logger.getLogger(MainController.class);
 
 	private TicketService ticketService;
 
 	private UserService userService;
 
 	@Autowired
 	public void setTicketService(TicketServiceBaseImpl ticketService) {
 		this.ticketService = ticketService;
 	}
 
 	@Autowired
 	public void setUserService(UserServiceBaseImpl userService) {
 		this.userService = userService;
 	}
 
 	@RequestMapping("/")
 	public String showStartPage() {
 		logger.trace("showing login page");
 		return "login";
 	}
 
 	@RequestMapping("/login")
 	public String showLoginPage() {
 		logger.trace("showing login page");
 		return "login";
 	}
 
 	@RequestMapping("/tickets")
 	public ModelAndView showTicketsPage() {
 		logger.trace("showing tickets page");
 		ModelAndView ticketsPage = new ModelAndView("tickets");
 		Set<Ticket> availableTickets = ticketService.getAvailableTickets();
 		String ticketsOwner = "admin";
 		Set<Ticket> bookedTickets = ticketService.getBookedTickets(ticketsOwner, new FilterCriteria());
 		ticketsPage.addObject("availableTickets", availableTickets);
 		ticketsPage.addObject("bookedTickets", bookedTickets);
 		ticketsPage.addObject("ticketsOwner", ticketsOwner);
 		return ticketsPage;
 	}
 
 	@RequestMapping(value = "/login", method = RequestMethod.POST)
 	public String login(@RequestParam(value = "login", required = true) String login,
 			@RequestParam(value = "password", required = true) String password, HttpSession session,
 			HttpServletRequest request) {
 		User user = userService.login(login, password);
 		if (user == null) {
 			logger.info("user with login: " + login + " does not exist");
 			request.setAttribute("errorMessage", "user with such login and password does not exists");
 			return "login";
 		} else {
 			session.setAttribute("user", user);
 			logger.info("user with login: " + login + " logged in succesfully");
 			return "redirect:tickets";
 		}
 	}
 
 	@RequestMapping(value = "/registration", method = RequestMethod.GET)
 	public String shorRegistrationPage() {
 		logger.trace("showing registration page started...");
 		return "registration";
 	}
 
 	@RequestMapping(value = "/register", method = RequestMethod.POST)
 	public String register(@Valid RegistrationUserBean userBean, BindingResult result, HttpServletRequest request) {
 		logger.trace("showing registration page started...");
 		if (result.hasErrors()) {
 			logger.error(result.getAllErrors());
 			request.setAttribute("errors", result.getAllErrors());
 			return "registration";
 		}
 
 		List<String> errors = new ArrayList<String>();
 		if (!userBean.getPassword().equals(userBean.getPasswordRepeat())) {
 			errors.add("passwords do not match");
 			request.setAttribute("errors", errors);
 			logger.error(errors);
 			return "registration";
 		}
 
 		User user = new User(userBean.getLogin(), userBean.getPassword());
 		try {
 			userService.register(user);
 		} catch (Exception e) {
 			logger.error(e);
 			errors.add(e.getMessage());
 			request.setAttribute("errors", errors);
 			return "registration";
 		}
 
		return "complete";
 	}
 
 	@RequestMapping(value = "/availableTickets")
 	public String getAvailableTickets(ModelMap model,
 			@RequestParam(value = "category", required = false) TicketCategory category,
 			@RequestParam(value = "title", required = false) String title,
 			@RequestParam(value = "date", required = false) Date date) {
 		logger.trace("preparing available tickets..");
 		Set<Ticket> availableTickets = ticketService.getAvailableTickets(category, title, date);
 		model.addAttribute("availableTickets", availableTickets);
 		logger.trace("available tickets are ready");
 		return "availableTickets";
 	}
 
 	@RequestMapping(value = "/registration/checkLogin", method = RequestMethod.POST)
 	public @ResponseBody
 	String register(@RequestParam(value = "login", required = false, defaultValue = "") String login) {
 		logger.trace("login to check: " + login);
 		boolean userExists = userService.isUserExists(login);
 		if (userExists) {
 			logger.error("user with login " + login + " exists");
 			return "{\"error\":\"user with such login already exists\"}";
 		}
 		logger.trace("user with login " + login + " does not exist.");
 		return "{}";
 	}
 }
