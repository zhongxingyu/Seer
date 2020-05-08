 package edu.upc.dsbw.spring.web;
 
 import java.util.List;
 import java.lang.*;
 
 //import javax.servlet.http.HttpSession;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.servlet.ModelAndView;
 
 import edu.upc.dsbw.spring.business.model.Book;
 import edu.upc.dsbw.spring.business.model.User;
 import edu.upc.dsbw.spring.business.model.UserBook;
 
 import edu.upc.dsbw.spring.business.service.UserService;
 
 //import edu.upc.dsbw.spring.web.DTO.UserBook;
 
 
 @Controller
 @RequestMapping("/users/*")
 public class UsersController {
 	
 	//@Autowired edu.upc.dsbw.spring.business.BooksController domainController;
 	// edu.upc.dsbw.spring.business.UsersController domainController;
 	@Autowired UserService userService;
 
 //TODO prova que vaig fer jo sense ordenar
 	 @RequestMapping(value="/readingbooks/user/{id}",method=RequestMethod.GET)
 	 public ModelAndView listBooks(@PathVariable("id") Integer id){
 	 	List<Book> books = userService.getReadingBooks(id);
 	 	return new ModelAndView("books/list","books",books);
 	 }
 	 
 	 @RequestMapping(value="/readbooks/user/{id}",method=RequestMethod.GET)
 	 public ModelAndView listReadBooks(@PathVariable("id") Integer id){
 	 	List<Book> books = userService.getReadBooks(id);
 	 	return new ModelAndView("books/list","books",books);
 	 }
 	 
 // @RequestMapping(value="/by/{orderBy}",method=RequestMethod.GET)
 // public ModelAndView listBooks(@PathVariable("orderBy") ListBooksOrderBy orderBy){
 // 	List<Book> books = domainController.listBooks(orderBy);
 // 	return new ModelAndView("books/list","books",books);
 // }
 
 //	
 //	@RequestMapping(value="/list",method=RequestMethod.GET)
 //	public ModelAndView listUsers(){
 //		List<User> users = domainController.listUsers();
 //		return new ModelAndView("users/list","users",users);
 //	}
 //	
 //	// @RequestMapping(value="/{username}",method=RequestMethod.GET)
 //	// public ModelAndView viewUser(@PathVariable("username") String username){
 //	// 	User user = domainController.getUser(username);
 //	// 	return new ModelAndView("users/detail", "user", user);
 //	// }
 //	
 //	@RequestMapping(value="/{id}",method=RequestMethod.GET)
 //	public ModelAndView viewUser(@PathVariable("id") Long id){
 //		User user = domainController.getUser(id);
 //		return new ModelAndView("users/detail", "user", user);
 //	}
 //	
 
 
 
 	
 //	@RequestMapping(value="/{id}",method=RequestMethod.GET)
 //	public ModelAndView viewUser(@PathVariable("id") Long id, HttpSession session){
 //		/*if (session.getAttribute("username")!=null && session.getAttribute("username")!=""){
 //			User user = domainController.getUser((Long.valueOf(session.getAttribute("userId").toString())));
 //			return new ModelAndView("users/mybooks", "user", user);
 //		}*/
 //		User user = domainController.getUser(id);
 //		return new ModelAndView("users/detail", "user", user);
 //	}
 	
 	// AJAX
 //	@RequestMapping(value="/setReadBook",method=RequestMethod.POST)
 //	public ModelAndView setReadBook(@RequestParam("bookId") Long id, @RequestParam("status") boolean status, HttpSession session) {
 //		try {
 //			domainController.setReadBook(id, status, Long.valueOf(session.getAttribute("userId").toString()));
 //			return new ModelAndView("ajax/ok");
 //		}
 //		catch (IllegalArgumentException ex) {
 //			return new ModelAndView("ajax/error");			
 //		}
 //	}
 	
 	// AJAX
 //	@RequestMapping(value="/setReadingBook",method=RequestMethod.POST)
 //	public ModelAndView setReadingBook(@RequestParam("bookId") Long id, @RequestParam("status") boolean status, HttpSession session) {
 //		try {
 //			domainController.setReadingBook(id, status, Long.valueOf(session.getAttribute("userId").toString()));
 //			return new ModelAndView("ajax/ok");
 //		}
 //		catch (IllegalArgumentException ex) {
 //			return new ModelAndView("ajax/error");			
 //		}
 //	}
 	
 //	@RequestMapping(value="/mybooks",method=RequestMethod.GET)
 //	public ModelAndView viewMyBooks(HttpSession session){
 //		if (session.getAttribute("username")!=null && session.getAttribute("username")!=""){
 //			User user = domainController.getUser(Long.valueOf(session.getAttribute("userId").toString()));
 //			return new ModelAndView("users/mybooks", "user", user);
 //		}
 //		else{
 //			return new ModelAndView("session/notLoggedIn","message","Error: Has d'estar loguejat per tal de veure aquesta pàgina");
 //		}
 //	}
 
 //	@RequestMapping(value="/signup",method=RequestMethod.GET)
 //	public ModelAndView viewSingnup(HttpSession session){
 //		return new ModelAndView("users/singup");		
 //	}
 
 //	@RequestMapping(value="/newUser",method=RequestMethod.POST)
 //	public ModelAndView newUser(HttpSession session,  @RequestParam("username") String user, @RequestParam("password") String pass ){
 //		
 //		if(user.length() < 1){
 //			String message = "Did you forget the username? Let's try again.";
 //			return new ModelAndView("users/singup", "message", message);		
 //		}
 //		if(pass.length() < 1){
 //			String message = "Did you forget the password? Let's try again.";
 //			return new ModelAndView("users/singup", "message", message);		
 //		}
 //
 //		/*
 //		if(pass.length() < 6){
 //			String message = "The password needs more than "+ pass.length() +", at least 6 characters. Let's try again.";
 //			return new ModelAndView("users/singup", "message", message);		
 //		}*/
 //
 //		boolean res = domainController.addUser(user, user, pass);
//		if(res){
//			String message = "Would you like to login rightnow?.";
//			//return "redirect:session/login.jsp"
//			return new ModelAndView("redirect:../session/login.html", "message", message);		
//		}else{
//			String message = user + " already exists.";
//			return new ModelAndView("redirect:signup.html", "message", message);			
//		}
 //		
 //	}
 
 }
