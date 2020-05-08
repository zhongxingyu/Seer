 package no.uis.portal.student;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import javax.faces.component.UIComponent;
 import javax.faces.event.ActionEvent;
 
 import no.uis.abam.dom.Application;
 import no.uis.abam.dom.Assignment;
 
 import com.icesoft.faces.component.ext.HtmlDataTable;
 import com.icesoft.faces.component.ext.UIColumn;
 import com.icesoft.faces.context.DisposableBean;
 
 public class ApplicationBean implements DisposableBean {
 	
 	private static final Date APPLICATION_DEADLINE_MASTER = new GregorianCalendar(
 			GregorianCalendar.getInstance().get(Calendar.YEAR), Calendar.DECEMBER, 1).getTime();
 	private static final Date APPLICATION_DEADLINE_BACHELOR = new GregorianCalendar(
 			GregorianCalendar.getInstance().get(Calendar.YEAR), Calendar.NOVEMBER, 15).getTime();
 	
 	private StudentService studentService;
 	
 	private Application currentApplication;
 	private Assignment currentAssignment;
 	
 	public ApplicationBean() {}
 	
 	
 	/**
 	 * ActionListener that sets custom Assignment to existing or new Application
 	 * @param event
 	 */
 	public void actionSetCustomAssignmentToApplication(ActionEvent event) {
 		Assignment selectedAssignment = studentService.getCurrentStudent().getCustomAssignment();
 		currentAssignment = selectedAssignment;
 		if(assignmentIsAppliedFor(selectedAssignment)){
 			setCurrentApplication(studentService.getCurrentStudent().getApplicationFromAssignment(selectedAssignment));
 		} else {
 			createNewApplication(selectedAssignment);
 		}
 	}
 	
 	/**
 	 * ActionListener that sets a selected Assignment to existing or new Application
 	 * @param event
 	 */
 	public void actionSetSelectedAssignmentToApplication(ActionEvent event) {
 		UIComponent uic = event.getComponent();
 
 		Object parent = uic.getParent();
 		Assignment selectedAssignment = null;
 		if(parent instanceof UIColumn){
 			UIColumn uiColumn = (UIColumn)parent;
 			parent = uiColumn.getParent();
 			if(parent instanceof HtmlDataTable){
 				HtmlDataTable table = (HtmlDataTable)parent;
 				selectedAssignment = (Assignment)table.getRowData();
 				studentService.updateSelectedAssignmentInformation(selectedAssignment);
 			}
 		}
 		currentAssignment = studentService.getSelectedAssignment();
 		if(assignmentIsAppliedFor(currentAssignment)){
 			setCurrentApplication(studentService.getCurrentStudent().getApplicationFromAssignment(currentAssignment));
 		} else {
 			createNewApplication(currentAssignment);
 		}
 		
 	}
 	
 	private void createNewApplication(Assignment selectedAssignment) {
 		Application newApplication = new Application();
 		newApplication.setAssignment(selectedAssignment);
 		newApplication.setApplicantStudentNumber(studentService.getCurrentStudent().getStudentNumber());
 		setCurrentAssignment(selectedAssignment);
 		setCurrentApplication(newApplication);
 	}
 	
 	private boolean assignmentIsAppliedFor(Assignment selectedAssignment){
 		return studentService.getCurrentStudent().assignmentIsAlreadyAppliedFor(selectedAssignment);
 	}
 	
 	/**
 	 * ActionListener that edits an Application
 	 * @param event
 	 */
 	public void actionEditApplication(ActionEvent event){
 		UIComponent uic = event.getComponent();
 
 		Object parent = uic.getParent();
 		Application selectedApplication = null;
 		if(parent instanceof UIColumn){
 			UIColumn uiColumn = (UIColumn)parent;
 			parent = uiColumn.getParent();
 			if(parent instanceof HtmlDataTable){
 				HtmlDataTable table = (HtmlDataTable)parent;
 				selectedApplication = (Application)table.getRowData();
 				studentService.updateSelectedAssignmentInformation(selectedApplication.getAssignment());
 			}
 		}
 		currentAssignment = studentService.getSelectedAssignment();
 		if(assignmentIsAppliedFor(currentAssignment)){
 			setCurrentApplication(studentService.getCurrentStudent().getApplicationFromAssignment(currentAssignment));
 		} else {
 			createNewApplication(currentAssignment);
 		}
 	}
 	
 	
 	/**
 	 * ActionListener that saves the current Application and sets it to the Student
 	 * 
 	 * @param event
 	 */
 	public void actionSaveApplication(ActionEvent event) {
 		studentService.setApplicationToStudent(currentApplication);
 		studentService.saveApplication(currentApplication);
 	}

 	public StudentService getStudentService() {
 		return studentService;
 	}
 
 	public void setStudentService(StudentService studentService) {
 		this.studentService = studentService;
 	}
 
 	public Application getCurrentApplication() {
 		return currentApplication;
 	}
 
 	public void setCurrentApplication(Application currentApplication) {
 		this.currentApplication = currentApplication;
 	}
 
 	public Assignment getCurrentAssignment() {
 		return currentAssignment;
 	}
 
 	public void setCurrentAssignment(Assignment currentAssignment) {
 		this.currentAssignment = currentAssignment;
 	}
 	
 	public boolean isDeadlineForApplyingReached() {
 		if (studentService.getCurrentStudent().isBachelor()) {			
 			return APPLICATION_DEADLINE_BACHELOR.before(GregorianCalendar.getInstance().getTime());
 		} else {
 			return APPLICATION_DEADLINE_MASTER.before(GregorianCalendar.getInstance().getTime());
 		}		
 	}
 		
 	public void dispose() throws Exception {
 	}
 
 }
