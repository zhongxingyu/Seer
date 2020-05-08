 package cours.ulaval.glo4003.controller;
 
 import java.util.List;
 
 import javax.inject.Inject;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import cours.ulaval.glo4003.domain.Course;
 import cours.ulaval.glo4003.domain.Offering;
 import cours.ulaval.glo4003.domain.repository.CourseRepository;
 import cours.ulaval.glo4003.domain.repository.OfferingRepository;
 
 @Controller
 @RequestMapping(value = "/offering")
 public class OfferingController {
 
 	@Inject
 	CourseRepository courseRepository;
 
 	@Inject
 	OfferingRepository offeringRepository;
 
 	@RequestMapping(method = RequestMethod.GET)
 	public ModelAndView offering() throws Exception {
 		ModelAndView mv = new ModelAndView("offering");
 		mv.addObject("years", offeringRepository.findYears());
 		return mv;
 	}
 
 	@RequestMapping(value = "/{year}", method = RequestMethod.GET)
 	public ModelAndView offeringByYear(@PathVariable String year) throws Exception {
 
 		Offering offering = offeringRepository.find(year);
 
 		List<Course> courses = courseRepository.findByOffering(offering);
 
 		ModelAndView mv = new ModelAndView("offeringbyyear");
 		mv.addObject("year", year);
 		mv.addObject("courses", courses);
 
 		return mv;
 	}
 
 	@RequestMapping(value = "/{year}/deletecourse")
 	public ModelAndView deleteCourse(@PathVariable String year, @RequestParam(required = true, value = "acronym") String acronym) {
 
 		ModelAndView mv = new ModelAndView("offeringbyyear");
 
 		try {
 			Offering offering = offeringRepository.find(year);
 			offering.removeCourse(acronym);
 			List<Course> courses = courseRepository.findByOffering(offering);
 
 			mv.addObject("year", year);
 			mv.addObject("courses", courses);
 			mv.addObject("error", ControllerMessages.SUCCESS);
 		} catch (Exception e) {
 			mv.addObject("error", e.getMessage());
 		}
 
 		return mv;
 	}
 
 	@RequestMapping(value = "/{year}/availablecourses")
 	public ModelAndView availableCourses(@PathVariable String year) throws Exception {
 
 		ModelAndView mv = new ModelAndView("availablecourses");
 		mv.addObject("year", year);
 		mv.addObject("courses", courseRepository.findAll());
 
 		return mv;
 	}
 
 	@RequestMapping(value = "/{year}/addcourse")
	public ModelAndView addCourseFromAvailableCourses(@PathVariable String year, @RequestParam(required = true, value = "acronym") String acronym) throws Exception {
 
 		ModelAndView mv = new ModelAndView("offeringbyyear");
		if (!offeringRepository.containsOfferingFor(year)) {
			offeringRepository.store(new Offering(year));
		}
		Offering offering = offeringRepository.find(year);
		List<Course> courses = courseRepository.findByOffering(offering);
 
 		try {
 			offering.addCourse(acronym);
 
			courses = courseRepository.findByOffering(offering);
 
 			mv.addObject("error", ControllerMessages.SUCCESS);
 		} catch (Exception e) {
 			mv.addObject("error", e.getMessage());
 		}
		mv.addObject("year", year);
		mv.addObject("courses", courses);
 
 		return mv;
 	}
 }
