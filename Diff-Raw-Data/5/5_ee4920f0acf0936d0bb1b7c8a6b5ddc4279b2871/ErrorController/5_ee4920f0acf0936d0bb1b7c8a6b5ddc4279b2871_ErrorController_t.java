 package com.kerat.playground.web.controller;
 
 import com.kerat.playground.domain.exception.BusinessException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.http.HttpStatus;
 import org.springframework.web.bind.annotation.ControllerAdvice;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseStatus;
 import org.springframework.web.servlet.ModelAndView;
 
 @ControllerAdvice
 @RequestMapping("/errors")
 public class ErrorController {
 
 	private static final Logger log = LoggerFactory.getLogger(ErrorController.class);
 
 	@ExceptionHandler(BusinessException.class)
	@ResponseStatus(HttpStatus.OK)
 	public ModelAndView handleException(final BusinessException exception) {
 		log.info("handleException({})", exception.getExceptionType());
 
 		final ModelAndView modelAndView = new ModelAndView();
 
 		modelAndView.setViewName("errors");
 
 		modelAndView.addObject("exception", exception);
 
 		return modelAndView;
 	}
 
 }
