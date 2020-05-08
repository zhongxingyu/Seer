 package com.schedulerapp.models;
 
 import java.sql.Date;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 
 public class Department {
 	
 	private int departmentId;
 	private int campusId;
 	private String departmentName;
 	private String departmentHod;
 	private int contactInfo;
 	private String departmentDescription;
 	private Date dateCreated;
 	
 	public int getDepartmentId() {
 		return departmentId;
 	}
 	public void setDepartmentId(int departmentId) {
 		this.departmentId = departmentId;
 	}
 	public int getCampusId() {
 		return campusId;
 	}
 	public void setCampusId(int campusId) {
 		this.campusId = campusId;
 	}
 	public String getDepartmentName() {
 		return departmentName;
 	}
 	public void setDepartmentName(String departmentName) {
 		this.departmentName = departmentName;
 	}
 	public String getDepartmentHod() {
 		return departmentHod;
 	}
 	public void setDepartmentHod(String departmentHod) {
 		this.departmentHod = departmentHod;
 	}
 	public int getContactInfo() {
 		return contactInfo;
 	}
 	public void setContactInfo(int contactInfo) {
 		this.contactInfo = contactInfo;
 	}
 	public String getDepartmentDescription() {
 		return departmentDescription;
 	}
 	public void setDepartmentDescription(String departmentDescription) {
 		this.departmentDescription = departmentDescription;
 	}
 	public Date getDateCreated() {
 		return dateCreated;
 	}
 	public void setDateCreated(Date dateCreated) {
 		this.dateCreated = dateCreated;
 	}
 	
 
 	public static Department getSelectPropmpt() throws JSONException {
 	Department department = new Department();
 	department.setDepartmentId(-1);
 	department.setDepartmentName("Select Department");
 	return department;
 }	
 	
 	public static Department getDepartmentFromJson(JSONObject obj) throws JSONException {
 		Department department = new Department();
 		department.setCampusId(obj.getInt("campusId"));
		//department.setContactInfo(obj.getInt("contactInfo"));
		//department.setDepartmentDescription(obj.getString("departmentDescription"));
		//department.setDepartmentHod(obj.getString("departmentHod"));
 		department.setDepartmentId(obj.getInt("departmentId"));
 		department.setDepartmentName(obj.getString("departmentName"));
 		return department;
 	}	
 }
