 package com.github.funreco.domain;
 
 import java.util.Date;
 
 import org.bson.types.ObjectId;
 
@com.google.code.morphia.annotations.Entity(value = "funreco", noClassnameStored = true)
 public class Action {
	@com.google.code.morphia.annotations.Id
     private ObjectId id;
 	
	private Profile profile;
	private Object object;
	
	@Column
 	private Date date = new Date();
 
 }
