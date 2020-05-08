 package amu.licence.edt.model.beans;
 
 import java.io.Serializable;
 import java.util.Set;
 
 import javax.persistence.*;
 
 @Entity
 @Table (name="T_PROMO")
 public class Promo implements Serializable {
     private static final long serialVersionUID = 1L;
 
     @Id
     @GeneratedValue
     @Column (name="ID_PROMO")
     private int id;
 
     @Column (name="SIZE_PROMO")
     private int size;
 
     @OneToOne
     @JoinColumn (name="ID_LEVEL", nullable=false)
     private Level level;
 
     @OneToMany (mappedBy="promo")
     private Set<Group> groups;
 
     public Promo() { }
 
     public Promo(int size, Level level, Set<Group> group) {
         super();
         this.size = size;
         this.level = level;
         this.groups = group;
     }
 
     @Override
     public String toString() {
         return "Promo [id=" + id + ", size=" + size
                 + ", level=" + ((level != null) ? level.getCode() : null) + "]";
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + id;
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj)
             return true;
         if (obj == null)
             return false;
         if (getClass() != obj.getClass())
             return false;
         Promo other = (Promo) obj;
         if (id != other.id)
             return false;
         return true;
     }
 
     public int getSize() {
         return size;
     }
 
     public void setSize(int size) {
         this.size = size;
     }
 
     public Level getLevel() {
         return level;
     }
 
     public void setLevel(Level level) {
         this.level = level;
     }
 
    public Set<Group> getGroups() {
         return groups;
     }
 
     public boolean addGroup(Group group) {
         return this.groups.add(group);
     }
 
     public boolean removeGroup(Group group) {
         return this.groups.remove(group);
     }
 
     public int getId() {
         return id;
     }
 
 }
