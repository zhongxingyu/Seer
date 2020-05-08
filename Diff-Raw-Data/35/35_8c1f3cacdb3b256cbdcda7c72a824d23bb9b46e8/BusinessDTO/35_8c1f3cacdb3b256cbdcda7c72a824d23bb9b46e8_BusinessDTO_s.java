 package com.okaru.dtomapper.dto;
 
 import java.util.Date;
 
 import com.okaru.dtomapper.annotation.MappedField;
 
 public class BusinessDTO {
 	@MappedField(mappedObjectKey="business")
 	private String name;
 	
 	@MappedField(mappedObjectKey="business", field="founded")
 	private Date dateFounded;
 	
 	@MappedField(mappedObjectKey="business", field="industry")
 	private String industryName;
 
 	public String getIndustryName() {
 		return industryName;
 	}
 	public void setIndustryName(String industryName) {
 		this.industryName = industryName;
 	}
 	public Date getDateFounded() {
 		return dateFounded;
 	}
 	public void setDateFounded(Date dateFounded) {
 		this.dateFounded = dateFounded;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 }
