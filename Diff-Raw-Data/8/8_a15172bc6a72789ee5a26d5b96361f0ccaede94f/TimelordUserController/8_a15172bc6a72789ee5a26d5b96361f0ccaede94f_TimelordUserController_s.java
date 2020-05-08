 /*
  *   This file is part of Timelord.
  *
  *   Timelord is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU Affero General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   Timelord is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU Affero General Public License for more details.
  *
  *   You should have received a copy of the Affero GNU General Public License
  *   along with Timemord.  If not, see <http://www.gnu.org/licenses/>.
  */
 package eu.enhan.timelord.web.controller;
 
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.google.common.collect.Lists;
 
 import eu.enhan.timelord.domain.core.TimelordUser;
 import eu.enhan.timelord.domain.core.UserRepository;
 
 /**
  * @author Emmanuel Nhan
  *
  */
 @Controller
 @RequestMapping("/user")
 public class TimelordUserController {
 
     private static final Logger logger = LoggerFactory.getLogger(TimelordUserController.class);
     
     private UserRepository repo;
     
     
     
     @Autowired
     public TimelordUserController(UserRepository repo) {
 	super();
 	this.repo = repo;
     }
 
 
 
 
     
     @Transactional
     @RequestMapping(method=RequestMethod.POST)
     public String create(@RequestParam String registrationUsername, @RequestParam String registrationEmail, @RequestParam String registrationPassword){
 	TimelordUser user = new TimelordUser(registrationUsername, registrationPassword, registrationEmail);
 	
	user.persist();
 	
 	return "redirect:/";
     }
     
     @Transactional
     @RequestMapping(method=RequestMethod.GET)
     public String list(Model model){
 	Iterable<TimelordUser> u = repo.findAll();
 	List<TimelordUser> users = Lists.newArrayList(u);
 	model.addAttribute("users", users );
 	return "user/list";
     }
     
     
 }
