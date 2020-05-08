 package by.vsu.emdsproject.model;
 
 import java.util.HashSet;
 import java.util.Set;
 import javax.persistence.*;
 
 @Entity
 @Table(name = "Study_group")
 public class Group implements AbstractEntity {
 
     private Long id;
     private String title;
     private Speciality speciality;
     private Set<Student> students = new HashSet<Student>();
 
     public Group() {
     }
 
     public Group(String title) {
         this.title = title;
     }
 
     public Group(String title, Speciality speciality) {
         this.title = title;
         this.speciality = speciality;
     }
 
     @Id
     @Column(name = "id_group")
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     @Column(name = "title", nullable = false, length = 10)
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     @ManyToOne
     @JoinColumn(name = "speciality_id")
     public Speciality getSpeciality() {
         return speciality;
     }
 
     public void setSpeciality(Speciality speciality) {
         this.speciality = speciality;
     }
 
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch= FetchType.LAZY)
     public Set<Student> getStudents() {
         return students;
     }
 
     public void setStudents(Set<Student> students) {
         this.students = students;
     }
 }
