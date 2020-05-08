 package models;
 
 import java.util.List;
 
 import javax.persistence.*;
 import play.db.jpa.*;
 
 @Entity
 public class UniversityYear extends Model {  
   @OneToOne
   public University university;
   
   public Long year;
   
   @OneToOne
   public Speciality speciality;
   
   @OneToOne
   public Diploma diploma; // Null if no diploma this year, else we have a promotion this year
   
   @ManyToOne
   public User user;
   
   public UniversityYear(University university, Long year, Speciality speciality, Diploma diploma, User user) {
     this.university = university;
     this.year = year;
     this.speciality = speciality;
     this.diploma = diploma;
     this.user = user;
   }
   
   public static List<UniversityYear> find
      (University university, Long year, Speciality speciality) {
    return find(university, year, speciality, null);
      }  
  public static List<UniversityYear> find
  (University university, Long year, Speciality speciality, User user) {
    return find(
              (university==null? "true=true" : "university.id="+university.id)+" and "+
        (year==null? "true=true" : "year="+year)+" and "+
        (speciality==null? "true=true" : "speciality.id="+speciality.id)+" and "+
        (user==null? " true=true" : "user.id="+user.id)+
      " order by year desc").fetch();
    }
   
   
   public static List<UniversityYear> findForUser(User user) {
     return find("user = ?1 order by year desc", user).fetch();
   }
 }
