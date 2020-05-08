 package models;
 
 import java.util.*;
 import javax.persistence.*;
 import javax.persistence.validation.*;
 
 import play.db.ebean.*;
 import play.data.format.*;
 import play.data.validation.*;
 
 @Entity
 @Table(name = "courses_enrollments")
public class CourseEnrollment extends Model {
     @Column(name = "enrolled_at")
     public Date enrolledAt;
     @Column(name = "updated_at")
     public Date updatedAt;
     public static final long serialVersionUID = 1L;
     @Id
     @NotNull
     @Column(name = "enrollment_ID")
     public Integer enrollmentID;
     @NotNull
     @Size(min = 1, max = 30)
     @Column(name = "qualification")
     public String qualification;
     @Column(name = "is_finished")
     public Boolean isFinished;
     @Column(name = "credits")
     public Integer credits;
     @JoinColumn(name = "student", referencedColumnName = "user_ID")
     @ManyToOne(optional = false)
     public Student student;
     @JoinColumn(name = "course", referencedColumnName = "course_ID")
     @ManyToOne(optional = false)
     public Course course;
 
 }
