 package gov.nih.nci.cma.clinical;
 
 public class TARGETClinicalReportBean {
   
   private String patientId;                  
   private String targetId;              
   private String gender;    
   private String race;
   private String ethnicity;
   private Integer age; 
   private String wbc;
   private String cns; 
   private String testicular; 
   private String mrdDay8;
   private String mrdDay29; 
   private String event;
   private Integer timeToEvent;
   private String vitalStatus; 
   private Integer timeToDeath; 
   private String trisomies_4_10;
   private String mllStatus; 
   private String e2aStatus; 
   private String bcrStatus; 
   
	public String getId() {
		return patientId;
	}
  
 	public String getPatientId() {
 		return patientId;
 	}
 	
 	public void setPatientId(String patientId) {
 		this.patientId = patientId;
 	}
 	
 	public String getTargetId() {
 		return targetId;
 	}
 	
 	public void setTargetId(String targetId) {
 		this.targetId = targetId;
 	}
 	
 	public String getGender() {
 		return gender;
 	}
 	public void setGender(String gender) {
 		this.gender = gender;
 	}
 	
 	public String getRace() {		
 		return race;
 	}
 	
 	public void setRace(String race) {
 		this.race = race;
 	}
 	
 	public String getEthnicity() {	
 		return ethnicity;
 	}
 	
 	public void setEthnicity(String ethnicity) {
 		this.ethnicity = ethnicity;
 	}
 	
 	public Integer getAge() {
 		return age;
 	}
 	
 	public void setAge(Integer age) {
 		this.age = age;
 	}
 	
 	public String getWbc() {
 		return wbc;
 	}
 	
 	public void setWbc(String wbc) {
 		this.wbc = wbc;
 	}
 	
 	public String getCns() {
 		return cns;
 	}
 	
 	public void setCns(String cns) {
 		this.cns = cns;
 	}
 	
 	public String getTesticular() {
 		return testicular;
 	}
 	
 	public void setTesticular(String testicular) {
 		this.testicular = testicular;
 	}
 	
 	public String getMrdDay8() {
 		return mrdDay8;
 	}
 	
 	public void setMrdDay8(String mrdDay8) {
 		this.mrdDay8 = mrdDay8;
 	}
 	
 	public String getMrdDay29() {
 		return mrdDay29;
 	}
 	
 	public void setMrdDay29(String mrdDay29) {
 		this.mrdDay29 = mrdDay29;
 	}
 		
 	public String getEvent() {
 		return event;
 	}
 	
 	public void setEvent(String event) {
 		this.event = event;
 	}
 	
 	public Integer getTimeToEvent() {
 		return timeToEvent;
 	}
 	
 	public void setTimeToEvent(Integer timeToEvent) {
 		this.timeToEvent = timeToEvent;
 	}
 	
 	public String getVitalStatus() {
 		return vitalStatus;
 	}
 	
 	public void setVitalStatus(String vitalStatus) {
 		this.vitalStatus = vitalStatus;
 	}
 	
 	public Integer getTimeToDeath() {
 		return timeToDeath;
 	}
 	
 	public void setTimeToDeath(Integer timeToDeath) {
 		this.timeToDeath = timeToDeath;
 	}
 	
 	public String getTrisomies_4_10() {
 		return trisomies_4_10;
 	}
 	
 	public void setTrisomies_4_10(String trisomies_4_10) {
 		this.trisomies_4_10 = trisomies_4_10;
 	}
 	
 	public String getMllStatus() {
 		return mllStatus;
 	}
 	
 	public void setMllStatus(String mllStatus) {
 		this.mllStatus = mllStatus;
 	}
 	
 	public String getE2aStatus() {
 		return e2aStatus;
 	}
 	
 	public void setE2aStatus(String status) {
 		e2aStatus = status;
 	}
 	
 	public String getBcrStatus() {
 		return bcrStatus;
 	}
 	
 	public void setBcrStatus(String bcrStatus) {
 		this.bcrStatus = bcrStatus;
 	}
 }
