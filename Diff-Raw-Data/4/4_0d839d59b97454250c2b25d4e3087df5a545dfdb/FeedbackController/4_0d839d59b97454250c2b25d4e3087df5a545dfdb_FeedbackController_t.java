 package com.thoughtworks.twu.controller;
 
 import com.thoughtworks.twu.domain.Feedback;
 import com.thoughtworks.twu.service.FeedbackService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import java.util.ArrayList;
 
 @Controller
 public class FeedbackController {
 
     private FeedbackService feedbackService;
 
     @Autowired
     public FeedbackController(FeedbackService feedbackService) {
         this.feedbackService = feedbackService;
     }
 
     @RequestMapping(value = "feedback", method = RequestMethod.POST)
     public ModelAndView enterFeedback(int talkId, String feedbackComment) {
         feedbackService.enterFeedback(talkId, feedbackComment, "feedback giver name", "caroline@example.com");
         ModelAndView modelAndView = new ModelAndView();
         modelAndView.addObject("result-message", "Thank you for the feedback");
         return modelAndView;
     }
 
     @RequestMapping(value = "/add_feedback.htm*", method = RequestMethod.GET)
    public ModelAndView getAddFeedbackPage(@RequestParam(value = "talkId", defaultValue = "-1") int talkId) {
         ArrayList<Feedback> feedbackArrayList=new ArrayList<Feedback>();
         feedbackArrayList=feedbackService.retrieveFeedbackByTalkId(talkId);
         ModelAndView modelAndView= new ModelAndView("add_feedback");
         modelAndView.addObject("retrieved_feedback_list", feedbackArrayList);
         return modelAndView;
     }
 }
