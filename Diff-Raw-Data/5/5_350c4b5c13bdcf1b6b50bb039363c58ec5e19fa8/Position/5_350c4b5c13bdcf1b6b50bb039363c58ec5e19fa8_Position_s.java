 package models;
 
 import play.db.jpa.Model;
 
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 import java.util.ArrayList;
 import java.util.List;
 
 @Entity
 public class Position extends Model {
     public Position(){}
     public Position(Company companies) {
        this.experience = companies;
         this.projects = new ArrayList<Project>();
     }
 
     @OneToOne
    public Company experience;
 
     @ManyToOne
     public Person websiteConfig;
 
     @OneToMany
     public List<Project> projects;
 }
