 package com.discount.web;
 
 import java.util.Map;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 @Controller
 public class HTTPErrorController extends BaseController {
 
 	@RequestMapping(value = "/404")
 	public String handle404(Map<String, Object> map) {
 		putRootCategories(map);
 		return "/error/404";
 	}
 
 	@RequestMapping(value = "/403")
 	public String handle403(Map<String, Object> map) {
 		putRootCategories(map);
 		return "/error/403";
 	}
	

	@RequestMapping(value = "/500")
	public String handle500(Map<String, Object> map) {
		putRootCategories(map);
		return "/error/500";
	}
 }
