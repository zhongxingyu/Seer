 package com.intexsoft.sensor.service;
 
 import com.google.gson.Gson;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 /**
  * User: sergey.berdashkevich
  * Date: 08.08.13
  */
 @Controller
 @RequestMapping("api")
 public class Service {
 
     @RequestMapping("getData")
     @ResponseBody
     public String getData() {
        Gson gson = new Gson();
 
        return gson.toJson(new User("qwe", "zxcvcv"));
     }
 
     @RequestMapping("sendData")
     @ResponseBody
     public void sendData(@RequestBody String jsonData) {
         Gson gson = new Gson();
 
         System.out.println(jsonData);
 
         User user = gson.fromJson(jsonData, User.class);
         System.out.println(user.userName);
     }
 
 
     public class User   {
         String password;
         String userName;
 
         public User(String password, String userName) {
             this.password = password;
             this.userName = userName;
         }
 
         public String getPassword() {
             return password;
         }
 
         public void setPassword(String password) {
             this.password = password;
         }
 
         public String getUserName() {
             return userName;
         }
 
         public void setUserName(String userName) {
             this.userName = userName;
         }
     }
 }
