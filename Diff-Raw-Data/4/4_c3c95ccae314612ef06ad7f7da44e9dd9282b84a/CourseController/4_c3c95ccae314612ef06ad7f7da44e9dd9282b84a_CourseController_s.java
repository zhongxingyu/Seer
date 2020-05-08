 package cz.cvut.fel.bupro.controller;
 
 import java.util.Locale;
 
 import javax.validation.Valid;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.ObjectError;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import cz.cvut.fel.bupro.model.ProjectCourse;
 import cz.cvut.fel.bupro.service.CourseService;
 
 @Controller
 public class CourseController {
 	private final Log log = LogFactory.getLog(getClass());
 
 	@Autowired
 	private CourseService courseService;
 
 	@RequestMapping({ "/course/list" })
 	@Transactional
 	public String list(Model model, Locale locale) {
 		model.addAttribute("courseList", courseService.getProjectCourses(locale));
 		return "course-list";
 	}
 
 	@RequestMapping({ "/course/view/{id}" })
 	@Transactional
 	public String view(Model model, Locale locale, @PathVariable Long id) {
 		ProjectCourse course = courseService.getProjectCourse(id);
 		model.addAttribute("course", course);
 		return "course-view";
 	}
 
 	@RequestMapping({ "/course/edit/{id}" })
 	@Transactional
 	public String edit(Model model, Locale locale, @PathVariable Long id) {
		ProjectCourse course = courseService.getProjectCourse(id);
		model.addAttribute("course", course);
 		return "course-edit";
 	}
 
 	@RequestMapping({ "/course/create" })
 	@Transactional
 	public String create(Model model, Locale locale) {
 		log.trace("SubjectController.createProject()");
 		model.addAttribute("projectCourse", new ProjectCourse());
 		return "course-edit";
 	}
 
 	@RequestMapping({ "/course/save" })
 	@Transactional
 	public String save(@Valid ProjectCourse projectCourse, BindingResult bindingResult, Model model) {
 		if (bindingResult.hasErrors()) {
 			for (ObjectError error : bindingResult.getAllErrors()) {
 				log.info(error.toString());
 			}
 			return "course-edit";
 		}
 		if(courseService.getCourse(projectCourse) == null) {
 			bindingResult.rejectValue("code", "invalid.code", "No such course in KOS");
 			log.info("has errors " + bindingResult.hasErrors());
 			return "course-edit";
 		}
 		projectCourse = courseService.save(projectCourse);
 		log.info("Project saved " + projectCourse);
 		return "redirect:/course/list/";
 	}
 }
