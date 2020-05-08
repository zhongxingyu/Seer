 package no.uis.portal.employee;
 
 import java.util.ArrayList;
 import java.util.Map;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import javax.faces.event.ValueChangeEvent;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 import com.icesoft.faces.component.inputfile.FileInfo;
 import com.icesoft.faces.component.inputfile.InputFile;
 import com.icesoft.faces.context.DisposableBean;
 import com.liferay.portal.service.ServiceContext;
 
 public class AssignmentBean implements DisposableBean {
 
 	private static int lastId = 0; //todo: sync against database. 
 		
 	private int id;
 	
 	private boolean master;
 	private boolean bachelor;
 	
 	private String numberOfStudents;
 	private String title;
 	private ArrayList<Supervisor> supervisorList;
 	private String facultySupervisor;
 	private String description;
 	private String studyProgram;
 	private String institute;
 	private String numberOfStudentsError;
 	private String fileUploadErrorMessage;
 	private String attachedFilePath;
 	private Logger log = Logger.getLogger(AssignmentBean.class); 
 	
 	public AssignmentBean(){
 		//fileUploadErrorMessage = "";
 		supervisorList = new ArrayList<Supervisor>();
 		Supervisor hei = new Supervisor();
 		hei.setName("heihaa");
 		hei.setEmail("a@b.com");
 		hei.setExternal(true);
 		supervisorList.add(hei);
 	}
 	
 	public void actionAddSupervisor(ActionEvent event) {
 		supervisorList.add(new Supervisor());
 	}
 	
 	public void listen(ActionEvent event) {
 		UIComponent comp = event.getComponent();
 		FacesContext context = FacesContext.getCurrentInstance();
 		
 		String clientId = event.getComponent().getClientId(context);
 		clientId = clientId.replaceAll("CreateButton", "");
 		
 		Map<?,?> parameterMap = context.getExternalContext().getRequestParameterMap();
 		
 		log.setLevel(Level.ERROR);
 		if (log.isDebugEnabled()) {
 			log.debug("Title: "+parameterMap.get(clientId+"title"));
 			log.debug("Des: "+parameterMap.get(clientId+"description"));
 			log.debug("Supervisor: "+parameterMap.get(clientId+"supervisor"));
 			log.debug("FacultySupervisor: "+parameterMap.get(clientId+"facultySupervisor"));
 			log.debug("Institute: "+parameterMap.get(clientId+"institute"));
 			log.debug("StudyProgram: "+parameterMap.get(clientId+"studyProgram"));
 			log.debug("NumberOfStudents: "+parameterMap.get(clientId+"numberOfStudents"));
 			log.debug("type: "+parameterMap.get(clientId+"type"));
 		}
 		id = ++lastId;
 		title = (String)parameterMap.get(clientId+"title");
 		description = (String)parameterMap.get(clientId+"description");
 		//supervisorList = (String)parameterMap.get(clientId+"supervisor");
 		facultySupervisor = (String)parameterMap.get(clientId+"facultySupervisor");
 		institute = (String)parameterMap.get(clientId+"institute");
 		studyProgram = (String)parameterMap.get(clientId+"studyProgram");
 		numberOfStudents = (String)parameterMap.get(clientId+"numberOfStudents");
 		numberOfStudentsError = "";
 		fileUploadErrorMessage = "";
 		
		if(parameterMap.get(clientId+"type").equals("master")){
			master = true;
			bachelor = false;
		} else {
			master = false;
			bachelor = true;
		}
		
		if(parameterMap.get(clientId+"type").equals("master")){
 			if(!parameterMap.get(clientId+"numberOfStudents").equals("1")){
 				numberOfStudentsError = "Maximum number of students on a master assignment is 1.";
 				numberOfStudents = "1";
 			}
 		}
 	}
 	
 	public void fileUploadListen(ActionEvent event){
 		InputFile inputFile =(InputFile) event.getSource();
         FileInfo fileInfo = inputFile.getFileInfo();
         //file has been saved
         if (fileInfo.isSaved()) {
             fileUploadErrorMessage = "The attachment " +fileInfo.getFileName() +" was saved.";
             attachedFilePath = fileInfo.getPhysicalPath();
         }
         //upload failed, generate custom messages
         if (fileInfo.isFailed()) {
             if(fileInfo.getStatus() == FileInfo.INVALID){
                 fileUploadErrorMessage = "The attachment could not be uploaded.";
             }
             if(fileInfo.getStatus() == FileInfo.SIZE_LIMIT_EXCEEDED){
                 fileUploadErrorMessage = "The attachment exceeded the size limit.";
             }
             if(fileInfo.getStatus() == FileInfo.INVALID_CONTENT_TYPE){
                 fileUploadErrorMessage = "The attachment could not be uploaded.";
             }
             if(fileInfo.getStatus() == FileInfo.INVALID_NAME_PATTERN){
                 fileUploadErrorMessage = "The attachment can only be a pdf file.";
             }
         }
 	}
 
 	public void dispose() throws Exception {
 	}
 
 	public String getType() {
		if(master) return "Master";
		else return "Bachelor";
 	}
 		
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String getNumberOfStudents() {
 		return numberOfStudents;
 	}
 
 	public void setNumberOfStudents(String numberOfStudents) {
 		this.numberOfStudents = numberOfStudents;
 	}
 
 	public ArrayList<Supervisor> getSupervisorList() {
 		return supervisorList;
 	}
 
 	public void setSupervisorList(ArrayList<Supervisor> supervisorList) {
 		this.supervisorList = supervisorList;
 	}
 
 	public String getFacultySupervisor() {
 		return facultySupervisor;
 	}
 
 	public void setFacultySupervisor(String facultySupervisor) {
 		this.facultySupervisor = facultySupervisor;
 	}
 
 	public String getStudyProgram() {
 		return studyProgram;
 	}
 
 	public void setStudyProgram(String studyProgram) {
 		this.studyProgram = studyProgram;
 	}
 
 	public String getInstitute() {
 		return institute;
 	}
 
 	public void setInstitute(String institute) {
 		this.institute = institute;
 	}
 
 	public boolean isMaster() {
 		return master;
 	}
 
 	public void setMaster(boolean master) {
 		this.master = master;
 	}
 
 	public boolean isBachelor() {
 		return bachelor;
 	}
 
 	public void setBachelor(boolean bachelor) {
 		this.bachelor = bachelor;
 	}
 
 	public String getFileUploadErrorMessage() {
 		return fileUploadErrorMessage;
 	}
 
 	public void setFileUploadErrorMessage(String fileUploadErrorMessage) {
 		this.fileUploadErrorMessage = fileUploadErrorMessage;
 	}
 	
 	public String getNumberOfStudentsError() {
 		return numberOfStudentsError;
 	}
 
 	public void setNumberOfStudentsError(String numberOfStudentsError) {
 		this.numberOfStudentsError = numberOfStudentsError;
 	}
 
 	public String getAttachedFilePath() {
 		return attachedFilePath;
 	}
 
 	public void setAttachedFilePath(String attachedFilePath) {
 		this.attachedFilePath = attachedFilePath;
 	}
 }
