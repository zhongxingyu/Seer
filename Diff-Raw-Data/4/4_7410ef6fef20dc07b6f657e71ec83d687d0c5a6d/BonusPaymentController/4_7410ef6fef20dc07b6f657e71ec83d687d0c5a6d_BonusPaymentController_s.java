 package com.sk.frontend.web.controller;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.sk.service.payment.VPOSResponse;
 import com.sk.service.payment.garanti.GarantiVPOSService;
 
 @Controller
 @RequestMapping("/queryBonus")
 public class BonusPaymentController {
 	
 	private GarantiVPOSService garantiVPOSService;
 
 	@Autowired
 	public BonusPaymentController(GarantiVPOSService garantiVPOSService) {
 		this.garantiVPOSService = garantiVPOSService;
 	}
 
 	@RequestMapping(method = RequestMethod.POST)
 	public ModelAndView query(@RequestParam("creditCardNumber") String creditCardNumber,HttpServletRequest request)  {
 		ModelAndView mav = new ModelAndView("bonusQuery");
 		
 		VPOSResponse vposResponse = garantiVPOSService.queryBonus(creditCardNumber);
 		mav.addObject("bonus",vposResponse.getDetailMessage());
 		return mav;
 	}
 }
