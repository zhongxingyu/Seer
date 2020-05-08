 package is2.controller;
 
import is2.domain.Admin;
import is2.domain.Alumno;
import is2.domain.Docente;
 import is2.service.AdminService;
 import is2.service.AlumnoService;
 import is2.service.DocenteService;
 
 import java.io.IOException;
 import java.security.Principal;
 
 import javax.inject.Inject;
 import javax.servlet.http.HttpServletResponse;
 
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 @Controller
 public class HomeController {
 	
 	@Inject
 	AlumnoService alumnoService;
 	@Inject
	AdminService adminService;
 	@Inject
 	DocenteService docenteService;
 	
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public void index(Principal principal, HttpServletResponse response) throws IOException {
 		
 		if (principal==null){
 			response.sendRedirect("home.html");
 		}
 		else{
 			String username = principal.getName();
 			if(alumnoService.findByUsername(username) != null){
 				response.sendRedirect("alumno/home.html");
 			}
 			else{
 				if(docenteService.findByUsername(username) != null){
 					response.sendRedirect("docente/home.html");
 				}
 				else  response.sendRedirect("admin/home.html");
 			}
 		}
 	}
 	@RequestMapping("/home.html")
 	public ModelAndView home(HttpServletResponse response) throws IOException {
 		return new ModelAndView("home"); 
 	}
 	
 	@RequestMapping("/alumno/home.html")
 	public ModelAndView homeAlumno(HttpServletResponse response) throws IOException {
		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Alumno alumno = alumnoService.findByUsername(user.getUsername());
		ModelAndView view = new ModelAndView();
		view.addObject("alumno", alumno);
		view.setViewName("alumno/home");
		return view;
 	}
 	
 	@RequestMapping("/docente/home.html")
 	public ModelAndView homeDocente(HttpServletResponse response) throws IOException {
		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Docente docente = docenteService.findByUsername(user.getUsername());
		ModelAndView view = new ModelAndView();
		view.addObject("docente", docente);
		view.setViewName("docente/home");
		return view;
 	}
 	
 	@RequestMapping("/admin/home.html")
 	public ModelAndView homeAdministrator(HttpServletResponse response) throws IOException {
		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Admin admin = adminService.findByUsername(user.getUsername());
		ModelAndView view = new ModelAndView();
		view.addObject("admin", admin);
		view.setViewName("admin/home");
		return view;
 	}
 }
