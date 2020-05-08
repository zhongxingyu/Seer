 package fi.muni.pa165.entity;
 
import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
import org.apache.commons.lang3.Validate;
 import org.joda.time.Duration;
 
 /**
  * @author Honza
  */
 @Entity
 public class Service implements Serializable {
   @Id
   @GeneratedValue
   private Long id;
 
   private String name;
   private Long price;
   private Duration duration;
   @ManyToMany
   private Employee employees;
 
   public Service(@Nonnull String name, @Nonnull Long price, @Nonnull Duration duration) {
     Validate.isTrue(!= null, "Name should not be null");
     Validate.isTrue(!= null, "Price should not be null");
     Validate.isTrue(!= null, "Duration should not be null");
 
     this.name = name;
     this.price = price;
     this.duration = duration;
   }
 
   public Long getId() {
     return id;
   }
 
   public String getName() {
     return name;
   }
 
   public Long getPrice() {
     return price;
   }
 
   public Duration getDuration() {
     return duration;
   }
 
   public void setId(Long id) {
     this.id = id;
   }
 
   public void setName(String name) {
     this.name = name;
   }
 
   public void setPrice(Long price) {
     this.price = price;
   }
 
   public void setDuration(Duration duration) {
     this.duration = duration;
   }
 
   @Override
   public int hashCode() {
     int hash = 5;
     hash = 29 * hash + (this.id != null ? this.id.hashCode() : 0);
     return hash;
   }
 
   @Override
   public boolean equals(Object obj) {
     if (obj == null) {
       return false;
     }
     if (getClass() != obj.getClass()) {
       return false;
     }
     final Service other = (Service) obj;
     if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
       return false;
     }
     return true;
   }
 }
