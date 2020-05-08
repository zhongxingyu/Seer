 package models;
 
 import com.google.code.morphia.annotations.Embedded;
 import com.google.code.morphia.annotations.Entity;
 
 import play.modules.morphia.Model;
 @Embedded
 public class Detail {
 
 	public enum COLOR {BLACK,WHITE,BOTH}
 	public enum SIZE {SMALL,MEDIUM,LARGE}
	public int color ;
	public int size ;
 	public int number ;
 	
	public Detail(int color, int size , int number){
 		this.color = color ;
 		this.size = size ;
 		this.number = number ;
 	}
 }
