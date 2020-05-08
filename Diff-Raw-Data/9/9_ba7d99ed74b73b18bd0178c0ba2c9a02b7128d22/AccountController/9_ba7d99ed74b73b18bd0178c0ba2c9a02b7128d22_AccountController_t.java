 package com.natepaulus.dailyemail.web.controller;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import javax.validation.Valid;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 import org.springframework.web.servlet.support.RequestContextUtils;
 
 import com.natepaulus.dailyemail.repository.entity.DeliverySchedule;
 import com.natepaulus.dailyemail.repository.entity.User;
 import com.natepaulus.dailyemail.web.domain.DeliveryTimeEntryForm;
 import com.natepaulus.dailyemail.web.exceptions.RssFeedException;
 import com.natepaulus.dailyemail.web.exceptions.ZipCodeException;
 import com.natepaulus.dailyemail.web.service.interfaces.AccountService;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class AccountController.
  * 
  * @author Nate
  */
 @Controller
 public class AccountController {
 
 	/** The account service. */
 	@Autowired
 	AccountService accountService;
 	
 	private final Logger logger = LoggerFactory
 			.getLogger(AccountController.class);
 
 	/**
 	 * Display account page.
 	 * 
 	 * @param user
 	 *            the user
 	 * @param delTimeEntry
 	 *            the del time entry
 	 * @param session
 	 *            the session
 	 * @param request
 	 *            the request
 	 * @return the model and view
 	 */
 	@RequestMapping(value = "/account", method = RequestMethod.GET)
 	public ModelAndView displayAccountPage(@ModelAttribute("user") User user,
 			@ModelAttribute("delTimeEntry") DeliveryTimeEntryForm delTimeEntry,
 			HttpSession session, HttpServletRequest request) {
 		Map<String, ?> map = RequestContextUtils.getInputFlashMap(request);
 		Map<String, Object> model = new HashMap<String, Object>();
 
 		if (map != null) {
 			user = accountService.calculateUserDisplayTime(user);
 			String confirmSave = (String) map.get("confirmSave");
 
 			for (DeliverySchedule ds : user.getDeliveryTimes()) {
 				if (ds.getDeliveryDay() == 0) {
 					delTimeEntry.setTimezone(ds.getTz());
 					delTimeEntry.setWeekDayDisabled(ds.isDisabled());
 					delTimeEntry.setWeekDayTime(ds.getDisplayTime());
 				} else {
 					delTimeEntry.setTimezone(ds.getTz());
 					delTimeEntry.setWeekEndDisabled(ds.isDisabled());
 					delTimeEntry.setWeekEndTime(ds.getDisplayTime());
 				}
 			}
 
 			session.setAttribute("user", user);
 			session.setAttribute("deliveryTimeEntry", delTimeEntry);
 			
 			model.put("confirmSave", confirmSave);			
 			model.put("user", user);
 			model.put("deliveryTimeEntry", delTimeEntry);
 
 			return new ModelAndView("account", model);
 		} else {
 			User loggedInUser = (User) session.getAttribute("user");
 			loggedInUser = accountService
 					.calculateUserDisplayTime(loggedInUser);
 
 			for (DeliverySchedule ds : loggedInUser.getDeliveryTimes()) {
 				if (ds.getDeliveryDay() == 0) {
 					delTimeEntry.setTimezone(ds.getTz());
 					delTimeEntry.setWeekDayDisabled(ds.isDisabled());
 					delTimeEntry.setWeekDayTime(ds.getDisplayTime());
 				} else {
 					delTimeEntry.setTimezone(ds.getTz());
 					delTimeEntry.setWeekEndDisabled(ds.isDisabled());
 					delTimeEntry.setWeekEndTime(ds.getDisplayTime());
 				}
 			}
 			model.put("user", loggedInUser);
 			model.put("deliveryTimeEntry", delTimeEntry);
 
 			return new ModelAndView("account", model);
 		}
 
 	}
 
 	/**
 	 * Adds the news link to the database.
 	 * 
 	 * @param name
 	 *            the name
 	 * @param url
 	 *            the url to add
 	 * @param session
 	 *            the session
 	 * @param redirect
 	 *            the redirect
 	 * @return the string which is the view to display
 	 * @throws RssFeedException
 	 *             the rss feed exception (feed url invalid or user already has
 	 *             this link in their list)
 	 */
 	@RequestMapping(value = "/account/addNews", method = RequestMethod.POST)
 	public String addNewsLink(@RequestParam String name,
 			@RequestParam String url, HttpSession session,
 			RedirectAttributes redirect) throws RssFeedException {
 		User user = (User) session.getAttribute("user");
 
 		user = accountService.addNewsLink(url, name, user);
 		
 		redirect.addFlashAttribute("confirmSave", "rssLinkSaved");
 		redirect.addFlashAttribute("user", user);
 		return "redirect:/account";
 	}
 
 	/**
 	 * Update weather delivery preference for a user.
 	 * 
 	 * @param deliver_pref
 	 *            the deliver_pref the user selected
 	 * @param redirect
 	 *            to add flashmap attributes
 	 * @param session
 	 *            the session
 	 * @return the string which is the view to display
 	 */
 	@RequestMapping(value = "/account/weather", method = RequestMethod.POST)
 	public String updateWeatherDeliveryPreference(
 			@RequestParam int deliver_pref, RedirectAttributes redirect,
 			HttpSession session) {
 		User user = (User) session.getAttribute("user");
 
 		accountService.updateWeatherDeliveryPreference(deliver_pref, user);
 
 		redirect.addFlashAttribute("user", user);
 		redirect.addFlashAttribute("confirmSave", "weatherDeliveryPreferenceSaved");
 		return "redirect:/account";
 	}
 
 	/**
 	 * Update weather zip code for a user.
 	 * 
 	 * @param zipCode
 	 *            the zip code
 	 * @param session
 	 *            the session
 	 * @param redirect
 	 *            the redirect to add flashmap attributes
 	 * @return the string which is the view to display
 	 */
 	@RequestMapping(value = "/account/changezip", method = RequestMethod.POST)
 	public String updateWeatherZipCode(@RequestParam String zipCode,
 			HttpSession session, RedirectAttributes redirect) throws ZipCodeException {
 		User user = (User) session.getAttribute("user");
 		accountService.updateUserZipCode(user, zipCode);
 		redirect.addFlashAttribute("user", user);
 		redirect.addFlashAttribute("confirmSave", "zipCodeSaved");
 		return "redirect:/account";
 	}
 
 	/**
 	 * Sets the included news information for the daily email.
 	 * 
 	 * @param news
 	 *            the news feeds the user selected
 	 * @param session
 	 *            the session
 	 * @param redirect
 	 *            the redirect to add flashmap attributes
 	 * @return the string which is the view to display
 	 */
 	@RequestMapping(value = "/account/news", method = RequestMethod.POST)
 	public String setIncludedNewsInformation(
 			@RequestParam(required = false) String[] news, HttpSession session,
 			RedirectAttributes redirect) {
 		User user = (User) session.getAttribute("user");
 		if (news == null) {
 			news = new String[1];
 			news[0] = "0";
 		}
 		user = accountService.setIncludedNewsInformation(news, user);
 
 		redirect.addFlashAttribute("user", user);
 		redirect.addFlashAttribute("confirmSave", "newsDeliverySaved");
 		return "redirect:/account";
 	}
 
 	/**
 	 * Delete a news link the user no longer wishes to have.
 	 * 
 	 * @param id
 	 *            the id of the news link
 	 * @param session
 	 *            the session
 	 * @param redirect
 	 *            the redirect to add flashmap attributes
 	 * @return the string which is the view to display
 	 */
 	@RequestMapping(value = "/account/deleteNewsLink/{id}", method = RequestMethod.GET)
 	public String deleteNewsLink(@PathVariable int id, HttpSession session,
 			RedirectAttributes redirect) {
 		User user = (User) session.getAttribute("user");
 		user = accountService.deleteNewsLink(id, user);
 
 		redirect.addFlashAttribute("user", user);
 		redirect.addFlashAttribute("confirmSave", "rssLinkDeleted");
 		return "redirect:/account";
 	}
 
 	/**
 	 * Update daily email delivery schedule.
 	 * 
 	 * @param deliveryTimeEntry
 	 *            the delivery time the user entered in HH:MM A format
 	 * @param result
 	 *            the result of the web binding of the form
 	 * @param redirect
 	 *            the redirect to add flashmap attributes
 	 * @param session
 	 *            the session
 	 * @param model
 	 *            the model
 	 * @return the string which is the view to display
 	 */
 	@RequestMapping(value = "/account/delivery", method = RequestMethod.POST)
 	public String updateDeliverySchedule(
 			@Valid @ModelAttribute("deliveryTimeEntry") DeliveryTimeEntryForm deliveryTimeEntry,
 			BindingResult result, RedirectAttributes redirect,
 			HttpSession session, Model model) {
 		User user = (User) session.getAttribute("user");
 		if (result.hasErrors()) {
 			session.setAttribute("user", user);
 			model.addAttribute("user", user);
 			return "account";
 		}
 
 		user = accountService.updateDeliverySchedule(deliveryTimeEntry, user);
 
 		redirect.addFlashAttribute("user", user);
 		redirect.addFlashAttribute("deliveryTimeEntry", deliveryTimeEntry);
 		redirect.addFlashAttribute("confirmSave", "deliveryTimesSaved");
 		return "redirect:/account";
 	}
 	
 	@RequestMapping(value = "/account/generateUrlCode", method = RequestMethod.POST)
 	public String generateUrlCode(HttpSession session, RedirectAttributes redirect){
 		User user = (User) session.getAttribute("user");
 		user = accountService.generateUrlCode(user);
 		redirect.addFlashAttribute("user", user);
 		redirect.addFlashAttribute("confirmSave", "urlGenerated");
 		return "redirect:/account";
 	}
 	
 	@RequestMapping(value = "/account/deleteUrlCode", method = RequestMethod.POST)
 	public String deleteUrlCode(HttpSession session, RedirectAttributes redirect){
 		User user = (User) session.getAttribute("user");
 		user = accountService.deleteUrlCode(user);
 		redirect.addFlashAttribute("user", user);
 		redirect.addFlashAttribute("confirmSave", "urlDeleted");
 		return "redirect:/account";
 	}
 
 	/**
 	 * Handle rss feed exception.
 	 * 
 	 * @param ex
 	 *            the exception
 	 * @param request
 	 *            the http request
 	 * @return the model and view
 	 */
 	@ExceptionHandler(RssFeedException.class)
 	public ModelAndView handleRssFeedException(RssFeedException ex,
 			HttpServletRequest request) {
 
 		HttpSession session = request.getSession();
 		User user = (User) session.getAttribute("user");
 		DeliveryTimeEntryForm delTimeEntry = (DeliveryTimeEntryForm) session
 				.getAttribute("deliveryTimeEntry");
 
 		Map<String, Object> model = new HashMap<String, Object>();
 		model.put("user", user);
 		model.put("deliveryTimeEntry", delTimeEntry);
 		model.put("rssException", ex);
 
 		return new ModelAndView("account", model);
 	}
 	
 	/**
 	 * Handle zip code exception.
 	 *
 	 * @param ex the ex
 	 * @param request the http request
 	 * @return the model and view
 	 */
 	@ExceptionHandler(ZipCodeException.class)
 	public ModelAndView handleZipCodeException(ZipCodeException ex, HttpServletRequest request){
 		HttpSession session = request.getSession();
 		User user = (User) session.getAttribute("user");
 		DeliveryTimeEntryForm delTimeEntry = (DeliveryTimeEntryForm) session
 				.getAttribute("deliveryTimeEntry");
 
 		Map<String, Object> model = new HashMap<String, Object>();
 		model.put("user", user);
 		model.put("deliveryTimeEntry", delTimeEntry);
 		model.put("zipCodeException", ex);
 
 		return new ModelAndView("account", model);
 	}
 
 }
