 package com.euroit.militaryshop.web.controller;
 
 import java.util.Locale;
 
 import javax.servlet.http.HttpServletRequest;
 
 import com.euroit.common.bean.Trolley;
 import com.euroit.common.dto.TrolleyStatusDto;
 import com.euroit.militaryshop.dto.MilitaryShopItemDto;
 import com.euroit.militaryshop.dto.ProductDto;
 import com.euroit.militaryshop.web.controller.base.BaseTrolleyAwareController;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.context.MessageSource;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.LocaleResolver;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.i18n.CookieLocaleResolver;
 
 /**
  * @author EuroITConsulting
  */
 @Controller
 @RequestMapping("/trolley")
 public class TrolleyController extends BaseTrolleyAwareController {
     Logger logger = LoggerFactory.getLogger(TrolleyController.class);
     
 	private MessageSource messageSource;
     
 	@Autowired
 	public void setMessageSource(MessageSource messageSource) {
 		this.messageSource = messageSource;
 	}
 	
     @RequestMapping(value = "/add", method = RequestMethod.POST, consumes="application/json")
     public @ResponseBody void addItem(@RequestBody MilitaryShopItemDto item) {
         logger.debug(String.format("I'm trying to add item (%s) to the trolley", item));
         
         if (item == null) {
         	return;
         }
         
         trolley.addItem(item);
         
         logger.info("The item {} has been successfuly added to trolley", item);
     }
     
     @RequestMapping(value = "/status", method = RequestMethod.GET)
     public @ResponseBody TrolleyStatusDto getStatus(Locale locale) {
     	TrolleyStatusDto dto = new TrolleyStatusDto();
    	
     	dto.setItemsCount(messageSource.getMessage("trolley.itemsCount", 
    			new Object[]{trolley.getItemsCount()}, locale));
     	dto.setTotalPrice(messageSource.getMessage("trolley.totalPrice", 
     			new Object[]{ 777 }, locale));
         return dto;
     }
     
     @RequestMapping(value = "/list", method = RequestMethod.GET)
     public ModelAndView showItemsInTrolley() {
     	ModelAndView mav = new ModelAndView("trolley.list");
     	
     	return mav;
     }
 }
