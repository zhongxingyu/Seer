 package com.algo.webshop.client.controller;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.algo.webshop.common.domain.Category;
 import com.algo.webshop.common.domain.Good;
 import com.algo.webshop.common.domain.Price;
 import com.algo.webshop.common.domainimpl.ICategory;
 import com.algo.webshop.common.domainimpl.IGood;
 import com.algo.webshop.common.domainimpl.IPrice;
 
 @Controller
 public class FullGoodServlet {
 
 	private IGood serviceGood;
 	private IPrice servicePrice;
 	private ICategory serviceCategory;
 
 	@Autowired
 	public void setUserService(@Qualifier("goodService") IGood service) {
 		this.serviceGood = service;
 	}
 
 	@Autowired
 	public void setUserService(@Qualifier("priceService") IPrice service) {
 		this.servicePrice = service;
 	}
 
 	@Autowired
 	public void setUserService(@Qualifier("categoryService") ICategory service) {
 		this.serviceCategory = service;
 	}
 
 	@RequestMapping(value = "/fullgood", method = RequestMethod.GET)
 	public ModelAndView header(Model model, @RequestParam("good") int goodId) {
 		Good good = serviceGood.getGood(goodId);
 		Price price = servicePrice.getMaxDatePriceByOneGood(goodId);
 		List<Category> categorysList = serviceCategory.getCategorys();
 		String longDesc = serviceGood.getLongDescription(goodId);
		String manufactur = serviceGood.getManufactur(good.getManufacturers_id());
 		model.addAttribute("longDesc", longDesc);
 		model.addAttribute("manufactur", manufactur);
 		model.addAttribute(good);
 		model.addAttribute(price);
 		model.addAttribute("categorysList", categorysList);
 		return new ModelAndView("fullgood");
 	}
 }
