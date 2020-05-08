 package com.forum.web.controller;
 
 import com.forum.domain.Advice;
 import com.forum.domain.User;
 import com.forum.service.AdviceService;
 import com.forum.service.UserService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import javax.validation.Valid;
 import java.security.Principal;
 import java.util.Date;
 import java.util.Map;
 
 @Controller
 public class AdviceController {
     public static final String USERNAME = "username";
     public static final String DESCRIPTION = "description";
     public static final String INVALID_PAGE = "invalid";
     private AdviceService adviceService;
     public static final String SHOW_QUESTION_DETAILS = "question/view/";
     public static final String ERROR_PAGE = "500";
     public static final String QUESTION_ID = "questionId";
     private UserService userService;
 
     @Autowired
     public AdviceController(AdviceService adviceService) {
         this.adviceService = adviceService;
     }
 
     @Autowired
     public void setUserService(UserService userService){
         this.userService = userService;
     }
 
 
     @RequestMapping(value = "/postAdvice", method = RequestMethod.POST)
     public String saveAdvice(@Valid Advice advice, BindingResult result, Map model, Principal principal)  {
         if(principal == null) return "redirect: " + INVALID_PAGE;
         if (result.hasErrors()) {
             return "redirect:" + SHOW_QUESTION_DETAILS + advice.getQuestionId();
         } else {
             advice.setCreatedAt(new Date());
             String name = principal.getName();
             User user = userService.getByUserName(name);
             advice.setUser(user);
             int succeed = adviceService.save(advice);
             if (succeed != 0) {
                 return "redirect:" + SHOW_QUESTION_DETAILS + advice.getQuestionId();
             } else {
                 return "redirect:" + ERROR_PAGE;
             }
         }
     }
 
 }
