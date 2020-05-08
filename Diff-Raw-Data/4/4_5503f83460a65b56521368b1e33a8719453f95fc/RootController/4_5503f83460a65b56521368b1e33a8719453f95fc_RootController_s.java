 package wad.tukki.controller;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 @Controller
 public class RootController {
 
    @RequestMapping("*")
    public String root() {
         return "static/index.html";
     }
 }
