 package com.alextim.diskarchive.controllers;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
 
 public class IndexController extends MultiActionController{
	public ModelAndView index(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mv = new ModelAndView("WEB-INF/jsp/index.jsp");
 		return mv;
 	}
 	public ModelAndView main(HttpServletRequest request, HttpServletResponse response) {
 		ModelAndView mv = new ModelAndView("WEB-INF/jsp/main.jsp");
 		return mv;
 	}
 }
