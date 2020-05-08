 package com.forum.controller;
 
import com.forum.domain.Question;
 import com.forum.repository.ShowQuestions;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import java.util.List;
 
 @Controller
 public class HomePageController {
     @Autowired
     private ShowQuestions showQuestions;
     private static final int QUESTIONS_PER_PAGE=5;
     private static final int PAGE_NUMBER=1;
 
     @RequestMapping(value = "/home", method = RequestMethod.GET)
     public ModelAndView home() {
         ModelAndView homePage=new ModelAndView("home");
         List<Question> questionList = showQuestions.show(PAGE_NUMBER, QUESTIONS_PER_PAGE);
         homePage.addObject("questionList", questionList);
         return homePage;
     }
 }
