 package com.in6k.mypal.controller;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @Controller
 @RequestMapping(value = "/founds")
 public class FundsTransfer {
 
    @RequestMapping(value = "/transfer/add", method = RequestMethod.GET)
     public String showTransferPage() {
         return "founds_transfer/foundsTransfer";
     }
 
     @RequestMapping(value = "/transfer/add", method = RequestMethod.POST)
     public String addTransfer() {

         return "founds_transfer/foundsTransfer";
     }
 }
