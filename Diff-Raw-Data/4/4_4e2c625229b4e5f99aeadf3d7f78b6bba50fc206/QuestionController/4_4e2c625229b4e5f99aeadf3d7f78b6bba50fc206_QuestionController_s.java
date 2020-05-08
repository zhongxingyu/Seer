 package com.forum.controller;
 
 import com.forum.domain.Advice;
 import com.forum.domain.Question;
 import com.forum.repository.QuestionRepository;
 import com.forum.repository.QuestionValidation;
 import com.forum.services.AdviceService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.context.SecurityContext;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.view.RedirectView;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import java.sql.Timestamp;
 import java.util.List;
 
 
 @Controller
 public class QuestionController {
     private SecurityContext context;
     @Autowired
     private QuestionRepository questionRepository;
 
     @Autowired
     private QuestionValidation questionAdviceValidation;
     @Autowired
     private AdviceService adviceService;
 
     @RequestMapping(value = "/question_details", method = RequestMethod.GET)
     public ModelAndView questionDetails(@RequestParam("questionId") String questionId) {
         List<Advice> answers;
         ModelAndView questionDetail = new ModelAndView("questionDetails");
         Question question = questionRepository.getQuestionById(Integer.parseInt(questionId));
         answers = adviceService.getAdvices(Integer.parseInt(questionId));
         questionDetail.addObject("question", question.getQuestion());
         questionDetail.addObject("question_user", question.getUserName());
         questionDetail.addObject("answers", answers);
         questionDetail.addObject("noOfAnswer", answers.size());
         return questionDetail;
     }
 
     @RequestMapping(value = "/questions_advised", method = RequestMethod.GET)
     public ModelAndView getAdvisedQuestions(HttpServletRequest request) {
 
         context = SecurityContextHolder.getContext();
         Object userName = context.getAuthentication().getPrincipal();
         List<Question> questions = questionRepository.getQuestions(adviceService.getQuestionIdAnsweredBy(userName.toString()));
         ModelAndView myAnswers = new ModelAndView("myAnswers");
         HttpSession session = request.getSession(true);
         session.setAttribute("userName", userName);
 
         myAnswers.addObject("questions", questions);
         return myAnswers;
     }
 
     @RequestMapping(value = "/postedAdvice", method = RequestMethod.POST)
     public ModelAndView postedAdvice(@RequestParam("textareas") String textarea, @RequestParam("questionId") String questionId, HttpServletRequest request) {
         context = SecurityContextHolder.getContext();
         Object principal = context.getAuthentication().getPrincipal();
         HttpSession session = request.getSession(true);
         session.setAttribute("userName", principal);
         String path = request.getContextPath();
         ModelAndView mv;
         if (questionAdviceValidation.isQuestionValid(textarea)) {
             adviceService.save(new Advice(questionId, textarea, new Timestamp(0), principal.toString()));
             mv = new ModelAndView(new RedirectView("" + path + "/question_details?questionId=" + questionId + ""));
         } else {
             mv = questionDetails(questionId);
             mv.addObject("GivenAnswer", textarea);
             mv.addObject("error", "Advice length must be of at least 20 characters, and should not contain all spaces");
         }
         return mv;
     }
 }
