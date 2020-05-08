 package by.vsu.emdsproject.model;
 
 import java.util.HashSet;
 import java.util.Set;
 import javax.persistence.*;
 
 @Entity
 @Table(name = "speciality")
 public class Speciality implements AbstractEntity {
 
     private Long id;
     private String title;
     private Set<Group> groups = new HashSet<Group>();
 
     public Speciality() {
     }
 
     public Speciality(Long id) {
         this.id = id;
     }
 
     public Speciality(String title) {
         this.title = title;
     }
 
     public Speciality(Speciality s) {
         this.title = s.getTitle();
     }
 
     @Id
     @Column(name = "id_speciality")
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     @Column(name = "title", nullable = false, length = 100)
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
    @OneToMany(mappedBy = "speciality", cascade = CascadeType.ALL, fetch= FetchType.LAZY)
     public Set<Group> getGroups() {
         return groups;
     }
 
     public void setGroups(Set<Group> groups) {
         this.groups = groups;
     }
 }
