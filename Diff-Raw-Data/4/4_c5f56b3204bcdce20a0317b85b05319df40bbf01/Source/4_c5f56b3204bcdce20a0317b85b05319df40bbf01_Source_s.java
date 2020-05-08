 package ruffkat.hombucha.model;
 
 import javax.persistence.Basic;
 import javax.persistence.DiscriminatorValue;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
 
 @Entity
 @Inheritance(strategy = InheritanceType.SINGLE_TABLE)
 @DiscriminatorValue("source")
 public class Source
         implements Persistent {
 
     @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
     private Long oid;
 
     @Basic
     private String name;
 
     public Source() {
         this(null);
     }
 
     public Source(String name) {
         this.name = name;
     }
 
     public Long getOid() {
         return oid;
     }
 
     public boolean persisted() {
         return oid != null;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof Source)) return false;
 
         Source source = (Source) o;
 
         if (name != null ? !name.equals(source.name) : source.name != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         return name != null ? name.hashCode() : 0;
     }
 }
