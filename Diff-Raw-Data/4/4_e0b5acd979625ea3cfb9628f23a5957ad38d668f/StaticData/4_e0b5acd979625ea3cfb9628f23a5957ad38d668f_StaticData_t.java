 package com.java.gwt.libertycinema.server.models;
 
 
 import com.google.appengine.api.datastore.Key;
 
 import java.util.Date;
 import javax.jdo.annotations.IdGeneratorStrategy;
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 
 
 @PersistenceCapable
 public class StaticData {
 
 	@PrimaryKey
     @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
     private Key key;
 
     @Persistent
     private String menuName;
 
     @Persistent
     private String menuDescription;
 
     public StaticData(String menuName, String menuDescription) {
        this.menuName = menuName;
        this.menuDescription = menuDescription;
     }
 
     // Accessors for the fields. JDO doesn't use these, but your application does.
 
     public Key getKey() {
         return key;
     }
 
     public String getMenuName() {
         return menuName;
     }
 
     public String getMenuDescription() {
 		return menuDescription;
 	}
 
 	public void setMenuDescription(String menuDescription) {
 		this.menuDescription = menuDescription;
 	}
 
 	public void setMenuName(String menuName) {
 		this.menuName = menuName;
 	}
 }
