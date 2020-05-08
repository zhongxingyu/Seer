 package models;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.ManyToMany;
 
 import org.hibernate.annotations.Cascade;
 
 import play.db.jpa.Model;
 
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class Step.
  */
 @Entity
 public class Step extends Model{
 	
     /** The name. */
     public String name;
 
     /** The number. */
    public Long number;
 
     /** The templates. */
     @ManyToMany(cascade=CascadeType.ALL)
     public Set<Template> templates = new HashSet();
 
     /**
     * Instantiates a new step.
     *
     * @param name the name
     * @param number the number
     * @param templates the templates
     */
    public Step(String name, Long number, Set<Template> templates) {
         this.name = name;
         this.number = number;
         this.templates = templates;
     }
 
     public String toString() {
         return name;
     }
 }
