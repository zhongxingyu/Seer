 package no.uis.portal.student;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.Map;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import javax.faces.event.ValueChangeEvent;
 
 import no.uis.abam.dom.Assignment;
 import no.uis.abam.dom.Employee;
 import no.uis.abam.dom.Student;
 import no.uis.abam.dom.Supervisor;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 import com.icesoft.faces.component.ext.HtmlDataTable;
 import com.icesoft.faces.component.inputfile.FileInfo;
 import com.icesoft.faces.component.inputfile.InputFile;
 import com.icesoft.faces.context.DisposableBean;
 
 public class StudentAssignmentBean implements DisposableBean {
 
 	private FacesContext context;
 	private StudentService studentService;
 	
 	private Logger log = Logger.getLogger(StudentAssignmentBean.class); 
 	
 	private Assignment currentAssignment;
 	
 	private String customAssignmentStudentNumber;
 	
 	private boolean renderGetCustomAssignment;
 	
 	public StudentAssignmentBean(){
 	}
 			
 	public void actionGetCustomAssignment(ActionEvent event) {
 		Assignment assignment = studentService.getCurrentStudent().getCustomAssignment();
 		Student student = studentService.getCurrentStudent(); 
 		student.setDepartmentName(studentService
 				.getDepartmentNameFromIndex(studentService
 						.findDepartmentOe2ForCurrentStudent()));
 		//student.setDepartmentCode(studentService.findDepartmentCodeForCurrentStudent());
 		if(assignment == null) {
 			assignment = new Assignment();
 			studentService.getCurrentStudent().setCustomAssignment(assignment);
 			studentService.updateStudentInWebServiceFromCurrentStudent();
 		}
 		studentService.setSelectedAssignment(assignment);
 		setCurrentAssignment(assignment);
 	}
 	
 	public void actionCreateNewAssignment(ActionEvent event) {		
 		setCurrentAssignment(new Assignment());
 		currentAssignment.setId(studentService.getNextId());
 	}
 	
 	public void actionSaveAssignment(ActionEvent event) {
 		studentService.saveAssignment(currentAssignment);
 	}
 	
 	public void actionSetSelectedAssignment(ActionEvent event){
 		UIComponent uic = event.getComponent();
 
 		HtmlDataTable table = (HtmlDataTable)uic.getParent().getParent();
 		
 		Assignment selectedAssignment = (Assignment)table.getRowData();
 		
 		setCurrentAssignment(selectedAssignment);
 		
 		studentService.updateSelectedAssignmentInformation(selectedAssignment);
 	}
 	
 	public void actionRemoveAssignment(ActionEvent event) {
 		UIComponent uic = event.getComponent();
 		HtmlDataTable table = (HtmlDataTable)uic.getParent().getParent();
 		
 		Assignment assignment = (Assignment)table.getRowData();
 		
 		studentService.removeAssignment(assignment);
 	}
 	
 	public void actionAddSupervisor(ActionEvent event) {
 		currentAssignment.getSupervisorList().add(new Supervisor());
 	}
 	
 	public void actionRemoveSupervisor(ActionEvent event) {
 		UIComponent uic = event.getComponent();		
 		HtmlDataTable table = (HtmlDataTable)uic.getParent().getParent();
 		
 		currentAssignment.getSupervisorList().remove(table.getRowData());		
 	}
 	
 	public void actionUpdateCurrentAssignment(ActionEvent event) {
 		String clientId = event.getComponent().getClientId(context);
 		clientId = clientId.replaceAll("CreateButton", "");
 		
 		Map<?,?> parameterMap = context.getExternalContext().getRequestParameterMap();
 		
 		log.setLevel(Level.DEBUG);
 		currentAssignment.setDepartmentCode(studentService.findDepartmentCodeForCurrentStudent());
 		currentAssignment.setDepartmentName(studentService
 				.getDepartmentNameFromIndex(studentService
 						.findDepartmentOe2ForCurrentStudent()));
 		currentAssignment.setStudyProgramName(studentService.getCurrentStudent().getStudyProgramName());
 		currentAssignment.setFileUploadErrorMessage("");
		
		GregorianCalendar calendar = (GregorianCalendar) GregorianCalendar.getInstance();
 		currentAssignment.setAddedDate(calendar);
		
		calendar = (GregorianCalendar) GregorianCalendar.getInstance();
 		calendar.add(Calendar.MONTH, Assignment.ACTIVE_MONTHS);
 		currentAssignment.setExpireDate(calendar);
		
 		currentAssignment.updateType(studentService.getCurrentStudent().getType());
 		
 		currentAssignment.setFacultySupervisor(
 				studentService.getEmployeeFromFullName(
 						currentAssignment.getFacultySupervisor().getName()));
 		
 		for (Supervisor supervisor : currentAssignment.getSupervisorList()) {
 			if(!supervisor.isExternal()) {
 				Employee employee = studentService.getEmployeeFromFullName(supervisor.getName());
 				supervisor.setName(employee.getName());
 			}
 		}
 		
 		String numberOfStudentsInput = (String)parameterMap.get(clientId+"numberOfStudents");
 		if (numberOfStudentsInput == null) numberOfStudentsInput = "1";
 	}
 	
 	public void actionGetCustomAssignmentFromStudentNumber(ActionEvent event) {
 		Assignment assignment = studentService.getCustomAssignmentFromStudentNumber(getCustomAssignmentStudentNumber());
 		if (assignment != null) {
 			setCurrentAssignment(assignment);
 			studentService.setSelectedAssignment(assignment);
 			//actionUpdateCurrentAssignment(event);
 			studentService.getCurrentStudent().setCustomAssignment(assignment);
 			studentService.updateStudentInWebServiceFromCurrentStudent();
 		}
 	}
 	
 	public void actionRadioListener(ValueChangeEvent event) {
 		setRenderGetCustomAssignment((Boolean) event.getNewValue());
 	}
 	
 	public void fileUploadListen(ActionEvent event){
 		InputFile inputFile =(InputFile) event.getSource();
         FileInfo fileInfo = inputFile.getFileInfo();
         //file has been saved
         if (fileInfo.isSaved()) {
         	currentAssignment.setFileUploadErrorMessage("");
         	currentAssignment.getAttachedFileList().add(fileInfo.getFileName());
         	currentAssignment.setAttachedFilePath(fileInfo.getPhysicalPath());
         	currentAssignment.getAttachedFilePath().replace(fileInfo.getFileName(), "");
         }
         //upload failed, generate custom messages
         if (fileInfo.isFailed()) {
             if(fileInfo.getStatus() == FileInfo.INVALID){
             	currentAssignment.setFileUploadErrorMessage("The attachment could not be uploaded.");
             }
             if(fileInfo.getStatus() == FileInfo.SIZE_LIMIT_EXCEEDED){
             	currentAssignment.setFileUploadErrorMessage("The attachment exceeded the size limit.");
             }
             if(fileInfo.getStatus() == FileInfo.INVALID_CONTENT_TYPE){
             	currentAssignment.setFileUploadErrorMessage("The attachment could not be uploaded.");
             }
             if(fileInfo.getStatus() == FileInfo.INVALID_NAME_PATTERN){
             	currentAssignment.setFileUploadErrorMessage("The attachment can only be a pdf, zip, gif, png, jpeg, jpg, doc or docx file.");
             }
         }
 	}
 	
 	public void radioListener(ValueChangeEvent event){
 		if (event.getNewValue().equals(false)){
 			currentAssignment.setMaster(true);
 			currentAssignment.setBachelor(false);
 			currentAssignment.setType("Master");
 		} else {
 			currentAssignment.setMaster(false);
 			currentAssignment.setBachelor(true);
 			currentAssignment.setType("Bachelor");
 			currentAssignment.setNumberOfStudentsError("");
 		}
 	}
 	
 	public void actionRemoveAttachment(ActionEvent event){
 		UIComponent uic = event.getComponent();		
 		HtmlDataTable table = (HtmlDataTable)uic.getParent().getParent();
 		
 	    currentAssignment.getAttachedFileList().remove(table.getRowData());		
 	}
 
 	public void dispose() throws Exception {
 	}
 
 	public Assignment getCurrentAssignment() {
 		return currentAssignment;
 	}
 
 	public void setCurrentAssignment(Assignment currentAssignment) {
 		this.currentAssignment = currentAssignment;
 	}
 
 	public StudentService getStudentService() {
 		return studentService;
 	}
 
 	public void setStudentService(StudentService studentService) {
 		this.studentService = studentService;
 		context = FacesContext.getCurrentInstance();
 	}
 
 	public String getCustomAssignmentStudentNumber() {
 		return customAssignmentStudentNumber;
 	}
 
 	public void setCustomAssignmentStudentNumber(
 			String customAssignmentStudentNumber) {
 		this.customAssignmentStudentNumber = customAssignmentStudentNumber;
 	}
 
 	public boolean isRenderGetCustomAssignment() {
 		return renderGetCustomAssignment;
 	}
 
 	public void setRenderGetCustomAssignment(boolean renderGetCustomAssignment) {
 		this.renderGetCustomAssignment = renderGetCustomAssignment;
 	}
 	
 }
