 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.team33.controllers;
 
 import com.team33.entities.Account;
 import com.team33.entities.Orders;
 import com.team33.services.AccountService;
 import com.team33.services.VideoAccessService;
 import com.team33.services.exception.DataAccessException;
 import java.util.ArrayList;
 import java.util.Map;
 import javax.servlet.http.HttpSession;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 /**
  *
  * @author Daywalker
  */
 @Controller
 @RequestMapping(value = "/myAccountView.htm")
 public class MyAccountController {
     
     @Autowired
     private VideoAccessService videoAccessService;
     
     @Autowired
     private AccountService accountService;
     
     public void setVideoAccessService(VideoAccessService service){
         this.videoAccessService = service;
     }
     
     public void setAccountService(AccountService service){
         this.accountService = service;
     }
     
     @RequestMapping(method = RequestMethod.GET)
     public String displayAccountInfo(Map<String, Object> map, HttpSession session){
         
         Integer token = (Integer)session.getAttribute(LoginController.ACCOUNT_ATTRIBUTE);
         System.out.println("TOKEN : " + token);
         
         if (token == null){
             return "redirect:/myAccountView.htm";
         }
         
         int uuid = token.intValue();        
         
 //        List<VideoInfo> videoList = null;
 //        try{
 //            videoList = videoAccessService.getVideoInfoList(uuid);
 //        }catch(DataAccessException e){
 //            redirect.addFlashAttribute("exception", e);
 //            return "redirect:/myAccountView.htm";
 //        }catch(AccountNotActivatedException e){
 //            redirect.addFlashAttribute("exception", e);
 //            return "redirect:/myAccountView.htm";
 //        }
 //        
 //        map.put("videoList", videoList);
         
         try{
             Account account = this.accountService.getAccount(uuid);
             //Account account = new Account();
             //account.setName("Jim");
            map.put("Name", account.getName());
            map.put("Orders", (account.getOrdersCollection() == null ? new ArrayList<Orders>() : account.getOrdersCollection()));
         }catch(DataAccessException e){
             //redirect.addFlashAttribute("exception", e);
             return "redirect:/myAccountView.htm";
         }                
         return "/myAccountView";
     }
 }
