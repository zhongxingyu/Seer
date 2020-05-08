 package com.forum.controller;
 
 import com.forum.domain.TagValidator;
 import com.forum.repository.PostQuestion;
 import com.forum.repository.QuestionValidation;
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
 
 @Controller
 public class PostQuestionController {
     private SecurityContext context;
     @Autowired
     private QuestionValidation questionValidation;
     @Autowired
     private PostQuestion post;
     @Autowired
     private TagValidator tagValidator;
 
     @RequestMapping(value = "/postQuestion", method = RequestMethod.GET)
     public void postQuestion( HttpServletRequest request) {
         Object principal = getUserName();
         HttpSession session = request.getSession(true);
         session.setAttribute("userName", principal);
     }
 
     Object getUserName() {
         context = SecurityContextHolder.getContext();
         return context.getAuthentication().getPrincipal();
     }
 
     @RequestMapping(value = "/postedQuestion", method = RequestMethod.POST)
     public ModelAndView postedQuestion(@RequestParam("textareas") String textarea,@RequestParam("createTag") String tag, HttpServletRequest request) {
         ModelAndView mv;
         Object principal = getUserName();
         if (questionValidation.isQuestionValid(textarea)) {
             if(tagValidator.isValid(tag)){
                 tag = tagValidator.format(tag);
                 post.insert(tag,textarea, principal.toString());
                 mv = new ModelAndView(new RedirectView("activityWall"));
                 mv.addObject("pageNumber", 1);
             }else{
                 mv = new ModelAndView("postQuestion");
                mv.addObject("error", "Use single word tags and should not contains spaces");
                 mv.addObject("askedQuestion", textarea);
                mv.addObject("tag", tag);
             }
 
         } else {
             mv = new ModelAndView("postQuestion");
             mv.addObject("error", "Question length must be of at least 20 characters, and should not contain all spaces");
             mv.addObject("askedQuestion", textarea);
         }
         return mv;
     }
 }
