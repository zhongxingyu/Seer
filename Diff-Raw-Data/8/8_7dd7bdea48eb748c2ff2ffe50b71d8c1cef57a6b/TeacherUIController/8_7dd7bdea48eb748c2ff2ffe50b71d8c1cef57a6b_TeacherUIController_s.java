 package net.iteach.web.ui;
 
 import java.util.Locale;
 
 import javax.servlet.http.HttpServletRequest;
 
 import net.iteach.api.SchoolService;
 import net.iteach.core.model.Ack;
 import net.iteach.core.model.ID;
 import net.iteach.core.model.SchoolForm;
 import net.iteach.core.model.SchoolSummaries;
 import net.iteach.core.security.SecurityUtils;
 import net.iteach.core.ui.TeacherUI;
 import net.iteach.utils.InputException;
 import net.iteach.web.support.ErrorHandler;
 import net.iteach.web.support.ErrorMessage;
 import net.sf.jstring.Strings;
 import net.sf.jstring.support.CoreException;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 @Controller
 @RequestMapping("/ui/teacher")
 public class TeacherUIController implements TeacherUI {
 
 	private final SchoolService schoolService;
 	private final SecurityUtils securityUtils;
 	private final ErrorHandler errorHandler;
 	private final Strings strings;
 
 	@Autowired
 	public TeacherUIController(SchoolService schoolService,
 			SecurityUtils securityUtils,
 			ErrorHandler errorHandler,
 			Strings strings) {
 		this.schoolService = schoolService;
 		this.securityUtils = securityUtils;
 		this.errorHandler = errorHandler;
 		this.strings = strings;
 	}
 
 	@ExceptionHandler(InputException.class)
 	public ResponseEntity<String> onException (HttpServletRequest request, Locale locale, InputException ex) {
 		// Returns a message to display to the user
 		String message = ex.getLocalizedMessage(strings, locale);
 		// OK
		return new ResponseEntity<String>(message, HttpStatus.INTERNAL_SERVER_ERROR);
 	}
 
 	@ExceptionHandler(CoreException.class)
 	public ResponseEntity<String> onException (HttpServletRequest request, Locale locale, CoreException ex) {
 		// Error message
 		ErrorMessage error = errorHandler.handleError (request, locale, ex);
 		// Returns a message to display to the user
 		String message = strings.get(locale, "general.error", error.getMessage(), error.getUuid());
 		// Ok
 		return new ResponseEntity<String>(message, HttpStatus.INTERNAL_SERVER_ERROR);
 	}
 
 	@Override
 	@RequestMapping(value = "/school", method = RequestMethod.GET)
 	public @ResponseBody
 	SchoolSummaries getSchools() {
 		// Gets the current teacher
 		int userId = securityUtils.getCurrentUserId();
 		// OK
 		return schoolService.getSchoolsForTeacher(userId);
 	}
 
 	@Override
 	@RequestMapping(value = "/school", method = RequestMethod.POST)
 	public @ResponseBody
 	ID createSchool(@RequestBody SchoolForm form) {
 		// Gets the current teacher
 		int userId = securityUtils.getCurrentUserId();
 		// OK
 		return schoolService.createSchoolForTeacher(userId, form);
 	}
 
 	@Override
 	@RequestMapping(value = "/school/{id}", method = RequestMethod.DELETE)
 	public @ResponseBody
 	Ack deleteSchool(@PathVariable int id) {
 		// Gets the current teacher
 		int userId = securityUtils.getCurrentUserId();
 		// OK
 		return schoolService.deleteSchoolForTeacher(userId, id);
 	}
 
 	@Override
 	@RequestMapping(value = "/school/{id}", method = RequestMethod.PUT)
 	public @ResponseBody
 	Ack editSchool(@PathVariable int id, @RequestBody SchoolForm form) {
 		// Gets the current teacher
 		int userId = securityUtils.getCurrentUserId();
 		// OK
 		return schoolService.editSchoolForTeacher(userId, id, form);
 	}
 
 }
