 package no.niths.application.rest;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseStatus;
 
 import no.niths.application.rest.interfaces.AdminController;
 import no.niths.application.rest.lists.ListAdapter;
 import no.niths.application.rest.lists.RoleList;
 import no.niths.common.AppConstants;
 import no.niths.common.ValidationHelper;
 import no.niths.domain.Student;
 import no.niths.domain.security.Role;
 import no.niths.services.interfaces.GenericService;
 import no.niths.services.interfaces.RoleService;
 import no.niths.services.interfaces.StudentService;
 
 @Controller
 @RequestMapping(AppConstants.ADMIN)
 public class AdminControllerImpl extends AbstractRESTControllerImpl<Role> implements AdminController{
 
 	private static final Logger logger = LoggerFactory
 			.getLogger(AdminControllerImpl.class);
 
 	@Autowired
 	private RoleService roleService;
 	
 	@Autowired
 	private StudentService studentService;
 	
 	private RoleList roleList = new RoleList();
 	
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	@RequestMapping(value = { 
 			"setRole/{studentId}/{roleId}" }, method = RequestMethod.POST)
 	@ResponseStatus(value = HttpStatus.CREATED, reason = "Role added")
 	public void setStudentRole(@PathVariable Long studentId, @PathVariable Long roleId) {
 		Student stud = studentService.getById(studentId);
 		ValidationHelper.isObjectNull(stud);
 		Role role = roleService.getById(roleId);
 		ValidationHelper.isObjectNull(role);
 		stud.setRole(role);
 		studentService.update(stud);
 		logger.debug("Added role to student");
 	}
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	@RequestMapping(value = { "removeRole/{studentId}" }, method = RequestMethod.POST)
 	@ResponseStatus(value = HttpStatus.CREATED, reason = "Role removed")
 	public void removeStudentRole(Long studId) {
 		Student stud = studentService.getById(studId);
 		ValidationHelper.isObjectNull(stud);
 		stud.setRole(null);
 		studentService.update(stud);			
 		logger.debug("Removed role from student");
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public GenericService<Role> getService() {
 		return roleService;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public ListAdapter<Role> getList() {
 		return roleList;
 	}
 
 
 	
 }
