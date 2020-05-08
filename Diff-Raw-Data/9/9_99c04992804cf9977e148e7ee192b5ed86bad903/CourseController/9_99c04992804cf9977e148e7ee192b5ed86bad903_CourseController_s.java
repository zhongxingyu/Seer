 package com.drexelexp.course;
 
 import java.sql.Timestamp;
 import java.util.List;
 
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.drexelexp.Query;
 import com.drexelexp.baseDAO.SearchableDAO;
 import com.drexelexp.review.JdbcReviewDAO;
 import com.drexelexp.review.Review;
 import com.drexelexp.user.JdbcUserDAO;
 
 /**
  * Controller for the Course object
  * @author
  *
  */
 
 @Controller
 public class CourseController {
 	private void addUsername(Model model){
 			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
 			if (authentication.getName().equals("anonymousUser")) {
 				model.addAttribute("username","");
 			} else {
 				model.addAttribute("username",authentication.getName());
 			}
 	}
 	
 	private SearchableDAO<Course> _courseDAO;
 	private SearchableDAO<Course> getCourseDAO(){
 		if(_courseDAO!=null)
 			return _courseDAO;
 		
 		ApplicationContext context = new ClassPathXmlApplicationContext("Spring-Module.xml");
 		_courseDAO = (JdbcCourseDAO) context.getBean("courseDAO");
 		
 		return _courseDAO;
 	}
 	
 	@RequestMapping(value="/course", method = RequestMethod.GET)
 	public ModelAndView list(Model model) {		
 		return new ModelAndView("redirect:/course/1");
 	}
 	
 	@RequestMapping(value="/course/{pageNum}", method = RequestMethod.GET)
 	public ModelAndView listPage(@PathVariable String pageNum, Model model) {		
 		addUsername(model);
 		
 		List<Course> courses = getCourseDAO().getPage(Integer.parseInt(pageNum), 20);
 		
 		model.addAttribute("courses",courses);
 		model.addAttribute("pageNum",Integer.parseInt(pageNum));
 		
 		return new ModelAndView("course/list", "command", new Course());
 	}
 	
 	@RequestMapping(value="/course/show/{courseID}", method = RequestMethod.GET)
 	public ModelAndView show(@PathVariable String courseID, Model model) {
 		addUsername(model);
 		
 		Course course = getCourseDAO().getById(Integer.parseInt(courseID));
 		
 		model.addAttribute("course",course);
 		
 		if(course.getReviews().size()==0){
 			Timestamp t = new Timestamp(0);
 		
 			Review review =  new Review(1, "This is a sample review.", 2.5f, t,1, 1, 1);
 			course.getReviews().add(review);
 		}
 
 		ModelAndView mav = new ModelAndView("course/show");
 		mav.addObject("newReview", new Review());
 		return mav;
 	}
 	
 	@RequestMapping(value="course/show/{courseID}", method = RequestMethod.POST)
 	public ModelAndView review(@PathVariable String courseID,@ModelAttribute Course course, @ModelAttribute Review review, Model model) {
 		ApplicationContext context = new ClassPathXmlApplicationContext("Spring-Module.xml");
 		JdbcReviewDAO reviewDAO =  (JdbcReviewDAO) context.getBean("reviewDAO");
 		JdbcUserDAO userDAO =  (JdbcUserDAO) context.getBean("userDAO");
 		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
 		
 		review.setCourseId(Integer.parseInt(courseID));
 		review.setUser(userDAO.findByEmail(authentication.getName()));
 		
 		reviewDAO.insert(review);
 		
 		String redirectTo  ="redirect:../show/" + courseID;
 		return new ModelAndView(redirectTo);
 	}
 	
 	@RequestMapping(value="/course/search", method = RequestMethod.GET)
 	public ModelAndView search(Model model) {
 		addUsername(model);
 		
 		ModelAndView mav = new ModelAndView("course/search");
 		mav.addObject("courseQuery", new Query());
 		
 		return mav;
 	}
 	
 	@RequestMapping(value="/course/search", method = RequestMethod.POST)
 	public String search(@ModelAttribute("query") String query, Model model) {
 		System.out.println(query);
 		
 		List<Course> courses = getCourseDAO().search(query);		
 		
 		model.addAttribute("courses",courses);
 		return "course/list";
 	}
 }
