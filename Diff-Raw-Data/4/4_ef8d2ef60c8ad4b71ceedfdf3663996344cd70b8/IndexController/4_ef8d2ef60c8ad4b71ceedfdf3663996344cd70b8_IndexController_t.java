 package edu.mx.utvm.congreso.controlador;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import net.tanesha.recaptcha.ReCaptcha;
 import net.tanesha.recaptcha.ReCaptchaFactory;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.servlet.ModelAndView;
 
 import edu.mx.utvm.congreso.util.Util;
 
 @Controller
 @RequestMapping("/")
 public class IndexController {
 	protected final Log logger = LogFactory.getLog(getClass());
 	
 	@Value("${CAPTCHA_PUBLIC_KEY}")
 	private String captchaPublicKey;
 	
 	@Value("${CAPTCHA_PRIVATE_KEY}")
 	private String captchaPrivateKey;
 	
 	@Value("${IS_PRODUCTION_LOGIN}")
 	private boolean isProductionMode;
 	
     @RequestMapping(value="/index.htm")
     public ModelAndView handleIndexRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {      
     	
    	/*
         // Setting cookie			
 		boolean existCookie = false;
 		Cookie[] cookies = request.getCookies();
 		for(Cookie c : cookies){
 			if(c.getName().equals("1823juier9826123bd")){
 				existCookie = true;
 				break;
 			}
 		}
 		
 		if(!existCookie){
 	        Cookie cookie = new Cookie("1823juier9826123bd", Util.encodeNumber(1));
 	        cookie.setMaxAge(43200); // define seconds expire
 			response.addCookie(cookie);	
 		}
    	*/
         Map<String, Object> modelo = new HashMap<String, Object>();        
     	return new ModelAndView("index", "modelo", modelo);
     }
     
 	@RequestMapping("/login.htm")
 	public ModelAndView getLoginPage(HttpSession session, HttpServletResponse response, HttpServletRequest request) throws Exception{
 		ModelAndView model = new ModelAndView("login");
 		if(isProductionMode){			
 			ReCaptcha captcha = ReCaptchaFactory.newReCaptcha(
 					captchaPublicKey, 
 					captchaPrivateKey, 
 					false);
 			
 			String recaptchaHtml = captcha.createRecaptchaHtml(null, null);
 			model.addObject("recaptchaHtml", recaptchaHtml);
 		}		
 		return model;
 	}
 
 	@RequestMapping("/user_disabled.htm")
 	public ModelAndView getUserDisabledPage(ModelMap model) throws Exception{		
 		return new ModelAndView("error_page/user_disabled");
 	}		
 	
 }
