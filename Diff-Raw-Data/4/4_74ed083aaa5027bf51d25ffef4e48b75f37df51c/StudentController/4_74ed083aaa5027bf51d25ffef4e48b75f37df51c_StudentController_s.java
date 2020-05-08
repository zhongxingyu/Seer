 package net.iteach.web.student;
 
 import javax.servlet.http.HttpSession;
 
 import net.iteach.core.model.StudentLessons;
 import net.iteach.core.ui.TeacherUI;
 import net.iteach.web.support.AbstractGUIController;
 import net.iteach.web.support.ErrorHandler;
 import net.iteach.web.support.UserSession;
 
 import org.joda.time.LocalDate;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 @Controller
 @RequestMapping("/gui/student")
 public class StudentController extends AbstractGUIController {
 
 	private final TeacherUI ui;
 	private final UserSession userSession;
 	
 	@Autowired
 	public StudentController(ErrorHandler errorHandler, TeacherUI ui, UserSession userSession) {
 		super(errorHandler);
 		this.ui = ui;
 		this.userSession = userSession;
 	}
 
 	@RequestMapping("/{id:\\d+}")
 	public String details (@PathVariable int id, Model model, HttpSession session) {
 		// Loads the student details
 		model.addAttribute("student", ui.getStudent(id));
 		// Current date
 		model.addAttribute("currentDate", userSession.getCurrentDate(session).toString());
 		// View
 		return "student";
 	}
 	
 	@RequestMapping(value = "/{id:\\d+}/lessons", method = RequestMethod.GET)
 	public @ResponseBody StudentLessons getStudentLessons (@PathVariable int id, HttpSession session) {
 		// Gets the current date
 		LocalDate date = userSession.getCurrentDate(session);
 		// Gets the list of lessons for this month
 		return ui.getStudentLessons (id, date);
 	}
 	
 	@RequestMapping(value = "/{id:\\d+}/lessons/nextMonth", method = RequestMethod.GET)
 	public @ResponseBody StudentLessons getStudentLessonsForNextMonth (@PathVariable int id, HttpSession session) {
 		// Gets the current date
 		LocalDate date = userSession.getCurrentDate(session);
 		// Adds one month
 		userSession.setCurrentDate(session, date.plusMonths(1));
 		// Gets the list of lessons for this month
 		return getStudentLessons(id, session);
 	}
 	
 	@RequestMapping(value = "/{id:\\d+}/lessons/previousMonth", method = RequestMethod.GET)
 	public @ResponseBody StudentLessons getStudentLessonsForPreviousMonth (@PathVariable int id, HttpSession session) {
 		// Gets the current date
 		LocalDate date = userSession.getCurrentDate(session);
 		// Adds one month
 		userSession.setCurrentDate(session, date.minusMonths(1));
 		// Gets the list of lessons for this month
 		return getStudentLessons(id, session);
 	}
 
 }
