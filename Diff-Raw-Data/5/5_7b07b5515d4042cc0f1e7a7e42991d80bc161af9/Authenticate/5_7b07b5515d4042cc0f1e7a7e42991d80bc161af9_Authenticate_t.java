 package pl.dagguh.soccerfrontend.web.controllers;
 
 import pl.dagguh.soccerfrontend.web.forms.Credentials;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import pl.dagguh.soccerfrontend.backend.Account;
 
 /**
  *
  * @author Maciej Kwidziński <maciek.kwidzinski@gmail.com>
  */
 @Controller
@SessionAttributes("userError")
 public class Authenticate {
 
 	@RequestMapping(value = "/authenticateForm", method = RequestMethod.GET)
 	public void authenticateForm(Model model) {
 		model.addAttribute(new Credentials());
 	}
 
 	@RequestMapping(value = "/authenticate", method = RequestMethod.GET)
 	public String authenticate() {
 		return "redirect:authenticateForm";
 	}
 
 	@RequestMapping(value = "/authenticate", method = RequestMethod.POST)
 	public String authenticate(@ModelAttribute Credentials credentials, Model model) {
 		String ticket = Account.authenticate(credentials);
 		if (ticket.equals("")) {
			model.addAttribute("userError", "Podane hasło jest nieprawidłowe lub konto nie istnieje.");
 			return "redirect:authenticateForm";
 		} else {
 			model.addAttribute("nick", credentials.getNick());
 			model.addAttribute("ticket", ticket);
 			return "redirect:matchList";
 		}
 	}
 }
