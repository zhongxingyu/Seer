 /**
  * 
  * @author SergiyManko
  *
  */
 
 package epam.ph.sg.controllers;
 
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.SessionAttributes;
 
 import epam.ph.sg.models.sb.SbJSLoader;
 
 @Controller
//@SessionAttributes("sbJSLoader")
 public class SBMenuController {
 	private static Logger log = Logger.getLogger(SBMenuController.class);
	@RequestMapping(value = "/Sb.html" , method = RequestMethod.GET)
 	public String SbMenu(Model model, HttpSession session) {
 		if (session.getAttribute("user") == null) {
 			new HomeController().index(session);
 			return "Login";
 		}
		SbJSLoader sbJSLoader = new SbJSLoader();
		
		sbJSLoader.addScript("jquery");
		sbJSLoader.addScript("SB/SB_coords");
		log.debug("****************"+sbJSLoader.getScripts());
 
 		log.debug("-------------------Added JavaScriptss-------------------");
		session.setAttribute("sbJSLoader", sbJSLoader);
 		return "SB/SbMenu";
 	}
 	
 	
 	@RequestMapping(value = { "/BsConectGame.html"}, method = RequestMethod.GET)
 	public String SbMenuConnection(Model model, HttpSession session) {
 		if (session.getAttribute("user") == null) {
 			new HomeController().index(session);
 			return "Login";
 		}
 		log.debug("<--Test-->");
 		return "SB/SbMenu";
 	}
 }
