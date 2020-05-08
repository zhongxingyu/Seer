 package no.uis.portal.employee;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import javax.faces.event.ActionEvent;
 import javax.faces.event.ValueChangeEvent;
 import no.uis.abam.dom.Assignment;
 import no.uis.abam.dom.Department;
 import no.uis.abam.dom.EditableSelectItem;
 import no.uis.abam.ws_abam.AbamWebService;
 
 
 public class EmployeeService {
 
 	private EmployeeService selectedAssignment;
 	
 	private LinkedList<Department> departmentList;
 	private List<EditableSelectItem> selectedStudyProgramList = new LinkedList<EditableSelectItem>();
 	
 	private String selectedDepartmentName;
 	
 	private int selectedDepartmentNumber;
 	private int selectedStudyProgramNumber;
 	
 	private AbamWebService abamClient;
 	
 	public EmployeeService() {
 	}
 	
 	public void getDepartmentListFromWebService() {
 		departmentList = abamClient.getDepartmentList();
 	}
 	
 
 	public int getSelectedDepartmentNumber() {
 		return selectedDepartmentNumber;
 	}
 
 	
 	public void setSelectedDepartmentNumber(int selectedDepartmentNumber) {
 		this.selectedDepartmentNumber = selectedDepartmentNumber;
 	}
 
 	
 	public int getSelectedStudyProgramNumber() {
 		return selectedStudyProgramNumber;
 	}
 
 	
 	public void setSelectedStudyProgramNumber(int selectedStudyProgramNumber) {
 		this.selectedStudyProgramNumber = selectedStudyProgramNumber;
 	}
 
 
 	public int getNextId(){
 		return abamClient.getNextId();
 	}
 	
 	
 	public void saveAssignment(Assignment assignment) {
 		abamClient.saveAssignment(assignment);
 	}
 	
 	
 	public Set<Assignment> getAssignmentList() {
 		return abamClient.getAllAssignments();
 	}
 
 	
 	public EmployeeService getSelectedAssignment() {
 		return selectedAssignment;
 	}
 
 	
 	public void setSelectedAssignment(EmployeeService selectedAssignment) {
 		this.selectedAssignment = selectedAssignment;
 	}
 	
 	
 	public void actionClearStudyProgramAndDepartmentNumber(ActionEvent event){
 		setSelectedStudyProgramNumber(0);
 		setSelectedDepartmentNumber(0);
 	}
 	
 	
 	public void actionUpdateStudyProgramList(ValueChangeEvent event){
 		selectedDepartmentNumber = Integer.parseInt(event.getNewValue().toString());
 		Department selectedDepartment = getDepartmentFromValue(Integer.parseInt(event.getNewValue().toString())); 
 		selectedDepartmentName = selectedDepartment.getLabel();
 		selectedStudyProgramList = selectedDepartment.getStudyPrograms(); 
 		
 		Set<Assignment> assignmentList = abamClient.getAllAssignments();
 		for (Assignment assignment : assignmentList) {
 			if (assignment.getDepartmentName().equals(selectedDepartmentName)
 				|| selectedDepartmentName.equals("")) 
 				assignment.setDisplayAssignment(true);
 			else assignment.setDisplayAssignment(false);
 		}
		abamClient.setAssignmentList(assignmentList);
 		setAllEditExternalExaminerToFalse();
 	}
 	
 	
 	public void actionUpdateStudyProgramListFromCreateAssignment(ValueChangeEvent event){
 		selectedDepartmentNumber = Integer.parseInt(event.getNewValue().toString());
 		getStudyProgramListFromSelectedDepartment();
 		
 	}
 	
 	private void getStudyProgramListFromSelectedDepartment() {
 		selectedStudyProgramList = getDepartmentFromValue(selectedDepartmentNumber).getStudyPrograms();
 	}
 	
 	public void actionSetDisplayAssignment(ValueChangeEvent event){
		
		if(event.getNewValue() == null) {
			selectedStudyProgramNumber = 0;
		} else {
			selectedStudyProgramNumber = Integer.parseInt(event.getNewValue().toString());
		}
		String selectedStudyProgram = getStudyProgramNameFromValue(selectedStudyProgramNumber);
 		Set<Assignment> assignmentList = abamClient.getAllAssignments();
		
 		if (selectedDepartmentName == null) setSelectedDepartmentName("");
 		for (Assignment assignment : assignmentList) {
 			if (checkIfAssignmentShouldBeDisplayed(assignment, selectedStudyProgram)) 
 				assignment.setDisplayAssignment(true);
 			else assignment.setDisplayAssignment(false);
 		}
		abamClient.setAssignmentList(assignmentList);
 		setAllEditExternalExaminerToFalse();
 	}
 	
 	private boolean checkIfAssignmentShouldBeDisplayed(Assignment abIn, String selectedStudyProgram) {
 		return (selectedStudyProgram.equals("") && abIn.getDepartmentName().equals(selectedDepartmentName)) 
 		|| abIn.getStudyProgram().equals(selectedStudyProgram);
 	}
 	
 	public void setAllEditExternalExaminerToFalse() {
 		for (Assignment assignment : abamClient.getAllAssignments()) {
 			assignment.setEditExternalExaminer(false);
 		}
 	}
 	
 	public void addNewDepartment(){
 		Department newDepartment = new Department(new Integer(departmentList.size()), "");
 		newDepartment.setEditable(true);
 		departmentList.add(newDepartment);
 		//abamClient.addNewStudyProgramListForNewDepartment();
 		//allStudyProgramsByDepartmentList.add(new LinkedList<EditableSelectItem>());
 	}
 	
 	public List<Department> getDepartmentList() {
 		return departmentList;
 	}
 	
 	public void removeDepartment(EditableSelectItem department) {
 		departmentList.remove(department);
 	}
 
 	
 	public void saveDepartmentListToWebService() {
 		abamClient.setDepartmentList(departmentList);
 	}
 	
 	public List<EditableSelectItem> getSelectedStudyProgramList() {
 		return selectedStudyProgramList;
 	}
 
 	
 	public void setSelectedStudyProgramList(List<EditableSelectItem> list) {
 		this.selectedStudyProgramList = list;
 	}
 	
 	public String getSelectedStudyProgramNameFromIndex(int index) {
 		return selectedStudyProgramList.get(index).getLabel();
 	}
 	
 	public String getDepartmentNameFromIndex(int index) {
 		return departmentList.get(index).getLabel();
 	}
 
 	public Department getDepartmentFromValue(int value){
 		for (Department department : departmentList) {
 			if(Integer.parseInt(department.getValue().toString()) == value){
 				return department;
 			}
 		}
 		return null;
 	}
 	
	public String getStudyProgramNameFromValue(int value){
		for (EditableSelectItem studyProgram : selectedStudyProgramList) {
			if(Integer.parseInt(studyProgram.getValue().toString()) == value){
				return studyProgram.getLabel();
			}
		}
		return null;
	}
	
 	public String getSelectedDepartmentName() {
 		return selectedDepartmentName;
 	}
 
 	
 	public void setSelectedDepartmentName(String selectedDepartment) {
 		this.selectedDepartmentName = selectedDepartment;
 	}
 
 	
 	public void removeAssignment(Assignment assignment) {
 		abamClient.removeAssignment(assignment);
 	}
 
 	
 	public void setStudyProgramListFromDepartmentNumber(int departmentNumber) {
		setSelectedStudyProgramList(getDepartmentFromValue(departmentNumber).getStudyPrograms());
 	}
 
 
 	public void setAbamClient(AbamWebService abamClient) {
 		this.abamClient = abamClient;
 	}
 	
 }
