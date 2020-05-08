 package org.motechproject.commcare.provider.sync.web.controller;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 @Controller("commCareSyncPingController")
 @RequestMapping("/web-api")
 public class PingController {
 
    @RequestMapping(value = "/ping-commcare-provider-sync", method = RequestMethod.GET)
     @ResponseBody
     public String pingPage() {
         return "CommcareProviderSync Ping Page";
     }
 }
