 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.missingfeatures.bibtext.controller;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 /**
  *
  * @author Jami
  */
 @Controller
 public class AngularController {
     
     @RequestMapping(value ="/", method = RequestMethod.GET)
     public String redirectToFront() { 
         return "redirect:/front/index.html";
     }
     
//    @RequestMapping(value ="/test", method = RequestMethod.GET)
//    public String redirectToTestRunner() { 
//        return "redirect:/testing/";
//    }
     
 }
