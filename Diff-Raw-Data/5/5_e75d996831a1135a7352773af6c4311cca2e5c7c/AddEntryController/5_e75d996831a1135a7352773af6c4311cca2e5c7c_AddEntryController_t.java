 package com.delineneo.web;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 /**
  * User: deline
  * Date: 30/04/13
  * Time: 10:03 PM
  */
@Controller
 public class AddEntryController {
 
    @RequestMapping(value = "/addEntry", method = RequestMethod.GET)
     public String addEntry() {
         return "addEntry";
     }
 }
