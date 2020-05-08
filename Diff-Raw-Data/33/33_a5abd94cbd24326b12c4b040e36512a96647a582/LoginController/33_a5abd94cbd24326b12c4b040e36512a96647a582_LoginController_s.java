 package edu.upc.dsbw.spring.web;
 
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.servlet.ModelAndView;
 
 //import edu.upc.dsbw.spring.business.LoginController;
 
  
 @Controller
 @RequestMapping("/session/*")
 public class LoginController {
     
 //	@Autowired edu.upc.dsbw.spring.business.LoginController loginController;
 //	@Autowired edu.upc.dsbw.spring.business.BooksController booksController;
 	
     @RequestMapping(value = "/login")
     public String home(Model model) {
         return "session/login";
     }
     
 //    @RequestMapping(value = "/loginCheck", method = RequestMethod.POST)
 //    public ModelAndView loginCheck(HttpServletRequest req , HttpServletResponse res){
 //    	String username = req.getParameter("username");
 //    	String password = req.getParameter("password");
 //    	//Comprovar si ja existeix una sessió. Si no, es crea.
 //    	HttpSession session= req.getSession(true);
 //    	if (loginController.doLogin(username, password, session, booksController)==0){
//    		return new ModelAndView("session/welcome","message","Benvingut "+username);
//    	}
//    	else{
//    		return new ModelAndView("session/login","message","Usuari o password incorrectes");
//    	}
 //    }
     
     @RequestMapping(value = "/logout")
     public String logout(Model model) {
         return "session/logoutform";
     }
     
     @RequestMapping(value = "/dologout", method = RequestMethod.POST)
     public ModelAndView loginCheck(HttpServletRequest req , HttpServletResponse res, HttpSession session){
     	session.invalidate();
     	return new ModelAndView("session/welcome","message","Logout successful");
     }
 }
