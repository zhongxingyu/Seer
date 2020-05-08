 package aider.org.pmsiadmin.controller;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import javax.validation.Valid;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.support.SessionStatus;
 
 import aider.org.pmsiadmin.config.Configuration;
 import aider.org.pmsiadmin.connector.AdAuthenticator;
 import aider.org.pmsiadmin.model.form.AuthenticationForm;
 import aider.org.pmsiamin.model.ldap.DtoSession;
 
 @Controller
 @RequestMapping("/Authentification/Form")
 public class AuthenticationFormController {
 	
 	@Resource(name="configuration")
 	Configuration configuration = null;
 	
 	@InitBinder
     public void initBinder(WebDataBinder binder) throws SQLException {
 		// On recherche l'objet authentificationForm
 		if (binder.getTarget() != null &&
 			binder.getTarget() instanceof AuthenticationForm) {
 			
 			AuthenticationForm authenticationForm = (AuthenticationForm) binder.getTarget();
 			// Si il n'y a pas d'objet de transfert de données de session dans l'objet
 			// il faut le créer
 			if (authenticationForm.getDtoSession() == null) {
 				AdAuthenticator adAuthentificator = new AdAuthenticator(
 						configuration.getLdapDomain(),
 						configuration.getLdapHost(),
 						configuration.getLdapDn());
 				
 				DtoSession dtoSession = new DtoSession(adAuthentificator);
 				
 				authenticationForm.setDtoSession(dtoSession);
 			}
 		}
     }
 	
 	@RequestMapping(method = RequestMethod.GET)
 	public String initForm(
 			HttpServletRequest request,
 			ModelMap model){
  
 		AuthenticationForm authenticationForm = new AuthenticationForm();
 
 		request.getSession().removeAttribute("session");
 		
 		// command object
 		model.addAttribute("authenticationform", authenticationForm);
  
 		//return form view
		return "AuthenticationForm";
 	}
 
 	@RequestMapping(method = RequestMethod.POST)
 	public String processSubmit(
 		@Valid @ModelAttribute("authenticationform") AuthenticationForm authenticationForm,
 		BindingResult result,
 		SessionStatus status,
 		HttpServletRequest request,
 		ModelMap model) throws ClassNotFoundException, SQLException, IOException {
  
 		if (result.hasErrors()) {
 			//if validator failed
 			request.getSession().removeAttribute("session");
 			return "AuthenticationForm";
 		} else {
 			// La validation a réussi :
 			// User + pass est accepté par ldap
 			status.setComplete();
 			
 			// Définition de cet objet de session pour la session de l'utilisateur
 			// qui devra systématiquement se connecter avec son jsessionid
 			HttpSession httpSession = request.getSession(true);
 			httpSession.setAttribute("session", authenticationForm.getSession());
 			
 			return "redirect:/Pmsi/Insert/Form";
 		} 
 	}
 }
