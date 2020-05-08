 package com.res.controller;
 
 import java.math.BigDecimal;
import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.support.RequestContextUtils;
 
 import com.res.exception.ServiceException;
 import com.res.model.FoodCategory;
 import com.res.model.Menu;
 import com.res.service.AddressService;
 import com.res.service.CustomerService;
 import com.res.service.MenuService;
 import com.res.service.RestaurantService;
 import com.res.util.MessageLoader;
 
 @Controller
 @SessionAttributes
 public class MenuController {
 
 	private static Logger logger = Logger.getLogger(MenuController.class);
 	
 	@Autowired private MenuService menuService;
 	@Autowired private RestaurantService restaurantService;
 	@Autowired private CustomerService customerService;
 	@Autowired private AddressService addressService;
 	@Autowired private MessageLoader messageLoader;
 	
 	@RequestMapping(value="/menu", method=RequestMethod.GET)
 	public ModelAndView showMenu(HttpServletRequest request,
 			@RequestParam(value="restaurantId", required=false) Long restaurantId,
 			@RequestParam(value="restaurantName", required=false) String restaurantName) 
 					throws ServiceException{
 		
 		HttpSession session = request.getSession();
 		
 		if(restaurantId == null){
 			restaurantId = (Long) session.getAttribute("restaurantId");
 			restaurantName = (String) session.getAttribute("restaurantName");
 		}else{
 			session.setAttribute("restaurantId", restaurantId);
 			session.setAttribute("restaurantName", restaurantName);
 		}
 		
 		logger.info("restaurantId = " + restaurantId);
 		ModelAndView mav = null;
 		
 		if(restaurantId == null){
 			mav = new ModelAndView("redirect:/welcome.html");
 			return mav;
 		}
 		mav = new ModelAndView("menu");
 		
 		mav.addObject("restaurantId", restaurantId);
 		
 		List<FoodCategory> foodCategories = menuService.getFoodCategoriesFromMenu(restaurantId); 
 		mav.addObject("foodCategories", foodCategories);
		
		List<Long> foodCategoryIDs = new ArrayList<Long>();
		for(FoodCategory foodCategory : foodCategories){
			foodCategoryIDs.add(foodCategory.getFoodCategoryId());
		}
		mav.addObject("foodCategoryIDs", foodCategoryIDs);
 		
 		String lang = RequestContextUtils.getLocale(request).toString();
 		session.setAttribute("lang", lang);
 		mav.addObject("lang", lang);
 		
 		// typeaheads
 		mav.addObject("phoneNumbers", customerService.typeaheadPhoneNumber(restaurantId));
 		mav.addObject("firstNames", customerService.typeaheadFirstName(restaurantId));
 		mav.addObject("lastNames", customerService.typeaheadLastName(restaurantId));
 		mav.addObject("emails", customerService.typeaheadEmail(restaurantId));
 		mav.addObject("notes", customerService.typeaheadNote(restaurantId));
 		
 		mav.addObject("street1s", addressService.typeaheadStreet1(restaurantId));
 		mav.addObject("street2s", addressService.typeaheadStreet2(restaurantId));
 		mav.addObject("cities", addressService.typeaheadCity(restaurantId));
 		mav.addObject("states", addressService.typeaheadState(restaurantId));
 		mav.addObject("zipCodes", addressService.typeaheadZipcode(restaurantId));
 		
 		return mav;
 	}
 	
 	@RequestMapping(value="/wholeMenu", method=RequestMethod.GET)
 	public ModelAndView showMenuList(HttpServletRequest request, 
 			@RequestParam(value="restaurantId", required=false) Long restaurantId,
 			@RequestParam(value="restaurantName", required=false) String restaurantName){
 		HttpSession session = request.getSession();
 		
 		if(restaurantId == null){
 			restaurantId = (Long) session.getAttribute("restaurantId");
 			restaurantName = (String) session.getAttribute("restaurantName");
 		}else{
 			session.setAttribute("restaurantId", restaurantId);
 			session.setAttribute("restaurantName", restaurantName);
 		}
 		
 		ModelAndView mav = null;
 
 		//if no restaurantid from both request and session 
 		if(restaurantId == null){
 			mav = new ModelAndView("redirect:/welcome.html");
 			return mav;
 		}
 		
 		mav = new ModelAndView("wholeMenu");
 	
 		List<Menu> menuList = menuService.getMenu(restaurantId);
 		
 		mav.addObject("menuList", menuList);
 		return mav;
 	}
 
 	@RequestMapping(value="editMenu", method=RequestMethod.GET)
 	public ModelAndView editMenu(HttpServletRequest request, 
 			@RequestParam(value="menuId", required=false) Long menuId){
 		
 		HttpSession session = request.getSession();
 		if(menuId == null){
 			menuId = (Long) session.getAttribute("menuId");
 		}else{
 			session.setAttribute("menuId", menuId);
 		}
 		logger.info("menuId = " + menuId);
 		
 		ModelAndView mav = new ModelAndView("editMenu");
 		Menu menu = menuService.getMenuByMenuId(menuId);
 		mav.addObject("menu", menu);
 		return mav;
 	}
 	
 	@RequestMapping(value="saveMenu", method=RequestMethod.GET)
 	public String updateMenu(HttpServletRequest request, 
 			@RequestParam("menuid") long menuId, 
 			@RequestParam("menunum") String menuNum,
 			@RequestParam("small") BigDecimal small, 
 			@RequestParam(value="large", required=true) BigDecimal large,
 			@RequestParam("lunchnum") String lunchNum, 
 			@RequestParam("lunch") BigDecimal lunch, 
 			@RequestParam("combonum") String comboNum, 
 			@RequestParam("combo") BigDecimal combo,
 			@RequestParam(value="spicy", defaultValue="false") Boolean isSpicy, 
 			@RequestParam(value="rice", defaultValue="false") Boolean hasRice,
 			@RequestParam(value="sauce", defaultValue="false") Boolean hasSauce, 
 			@RequestParam(value="noodle", defaultValue="false") Boolean hasNoodles,
 			@RequestParam(value="pieces", defaultValue="0") Integer numPieces, 
 			@RequestParam(value="appetizerCombo", defaultValue="false") Boolean isAppetizerCombo){
 		
 		logger.info("new menu prices (small, large, lunch, combo) respectively " + small + ", " + large + ", " + lunch + ", " + combo);
 		
 		HttpSession session = request.getSession();
 		String lastUpdatedBy = (String) session.getAttribute("agentName");
 		
 		menuService.updateMenu(menuId, menuNum, small, large, lunchNum, lunch, comboNum, combo, 
 				isSpicy, hasRice, hasSauce, hasNoodles, numPieces, isAppetizerCombo, lastUpdatedBy);
 		
 		return "redirect:/wholeMenu.html";
 	}
 
 }
