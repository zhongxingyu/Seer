 package drinkcounter.web.controllers.ui;
 
 import drinkcounter.DrinkCounterService;
 import drinkcounter.authentication.RegistrationService;
 import drinkcounter.model.User;
 import java.util.HashMap;
 import java.util.Map;
 import javax.servlet.ServletRequest;
 import javax.servlet.http.HttpSession;
 import org.openid4java.discovery.DiscoveryInformation;
 import org.openid4java.message.AuthRequest;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
 
 /**
  *
  * @author murgo
  */
 @Controller
 public class AuthenticationController {
     
     public static final String OPENID = "openId";
     public static final String DISCOVERYINFORMATION = "discoveryInformation";
 
     @Autowired private DrinkCounterService drinkCounterService;
     @Autowired private RegistrationService registrationService;
 
     private static final Logger log = LoggerFactory.getLogger(AuthenticationController.class);
 
     @RequestMapping("/authenticate")
     public String authenticate(HttpSession session, @RequestParam("openid") String openId) {
         log.info("Authenticating " + openId);
         try {
             DiscoveryInformation disco = registrationService.performDiscoveryOnUserSuppliedIdentifier(openId);
             session.setAttribute(DISCOVERYINFORMATION, disco);
 
             AuthRequest request = registrationService.createOpenIdAuthRequest(disco, getReturnToUrl());
 
             return "redirect:" + request.getDestinationUrl(true);
         } catch (Exception ex) {
             log.error("Error authenticating OpenId", ex);
             return "redirect: vituiksmeniautentikointi";
         }
     }
     
     @RequestMapping("/checklogin")
     public String checkLogin(HttpSession session) {
         if (session.getAttribute(OPENID) != null)
             return "redirect:user";
         return "redirect:login";
     }
     
     @RequestMapping("/login")
    public ModelAndView logout() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("login");
        
        return mav;
     }
     
     @RequestMapping("/logout")
     public String logout(HttpSession session) {
         session.removeAttribute(OPENID);
         session.removeAttribute(DISCOVERYINFORMATION);
         
         return "redirect:login";
     }
     
     @RequestMapping("/openId")
     public String openId(HttpSession session, ServletRequest request) {
         Map<String, String> pageParameters = new HashMap<String, String>();
         Map shitmap = request.getParameterMap();
         // shitty hack
         for (Object key : shitmap.keySet()) {
             pageParameters.put((String)key, ((String[])shitmap.get(key))[0]);
         }
 
         String openId = null;
 
         if (!pageParameters.isEmpty()) {
             String isReturn = (String)pageParameters.get("is_return");
             if (isReturn != null && isReturn.equals("true")) {
                 DiscoveryInformation discoveryInformation = (DiscoveryInformation)session.getAttribute("discoveryInformation");
                 openId = registrationService.processReturn(discoveryInformation, pageParameters, getReturnToUrl());
             }
         }
 
         if (openId == null)
               return "redirect:vituiksman";
         
         session.setAttribute(OPENID, openId);
 
         User user = drinkCounterService.getUserByOpenId(openId);
         if (user == null)
             return "redirect:newuser";
         
         return "redirect:user";
     }
 
     public String getReturnToUrl() {
         // TODO: fix hardcoding
         return "http://localhost:8080/ui/openId?is_return=true";
     }
 }
