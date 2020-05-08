 package models;
 
 import javax.persistence.*;
 import play.db.jpa.*;
 import play.data.validation.*;
 import models.*;
 
 import java.util.*;
 
 @Entity
 public class University extends Model {  
   
     @Required
     public String name;
     
     public University(String name) {
       this.name = name;
     }
     
     public static List<University> findBySearch(String name) {
        return find("name like ?1", "%"+name+"%").fetch();
     }
     
     public String toString() {
       return name;
     }
    
    public int getTotalUsers() {
        List<UniversityYear> uys = UniversityYear.find(this,null,null);
        return uys.size();
    }
 }
