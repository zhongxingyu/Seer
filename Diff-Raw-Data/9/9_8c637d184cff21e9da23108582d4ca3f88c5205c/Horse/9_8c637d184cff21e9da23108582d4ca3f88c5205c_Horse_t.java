 package models;
 
import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 
 import play.db.jpa.Model;
 
 @Entity
 public class Horse extends Model {
 
 	public String name;
 
 	public Horse() {
 	}
 	
 	public Horse(String name) {
 		this.name = name;
 	}
 
 }
