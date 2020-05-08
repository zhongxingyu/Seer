 package com.github.funreco.domain;
 
 import java.util.Date;
 
 import org.bson.types.ObjectId;
 
import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Property;

@Entity(value = "action", noClassnameStored = true)
 public class Action {
	@Id
     private ObjectId id;

    @Embedded
    private Profile profile;

    @Embedded
    private Object object;
 	
	@Property
 	private Date date = new Date();
 

 }
