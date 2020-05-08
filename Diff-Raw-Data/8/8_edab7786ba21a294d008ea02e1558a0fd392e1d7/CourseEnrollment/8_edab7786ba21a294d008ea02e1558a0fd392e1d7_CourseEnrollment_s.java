 package models;
 
 import java.util.*;
 import javax.persistence.*;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 
 import play.db.ebean.*;
 import play.data.format.*;
 import play.data.validation.*;
 
 @Entity
 @Table(name = "courses_enrollments")
 public class CourseEnrollment extends Model {
     public static final long serialVersionUID = 1L;
     @Id
     @Column(name = "enrollment_ID")
     public Integer enrollmentID;
     @Size(min = 1, max = 30)
     @Column(name = "qualification")
     public String qualification;
     @Column(name = "is_finished")
     public Boolean isFinished;
     @Column(name = "credits")
     public Integer credits;
     @Column(name = "enrolled_at")
     public Date enrolledAt;
     @Column(name = "updated_at")
     public Date updatedAt;
     @JoinColumn(name = "student", referencedColumnName = "user_ID")
     @ManyToOne(optional = false)
     public Student student;
     @JoinColumn(name = "course", referencedColumnName = "course_ID")
     @ManyToOne(optional = false)
     public Course course;
 
     public static Finder<Long,CourseEnrollment> find = new Finder(
       Long.class, CourseEnrollment.class
     );
 
     public static List<CourseEnrollment> all() {
       return find.all();
     }
   
     public static void create(CourseEnrollment courseenrollment) {
       courseenrollment.save();
     }
 
     public static void delete(Long id) {
       find.ref(id).delete();
     }
 
     public Course getCourse()
     {
       Course c = this.course;
       Integer a = c.credits; //does nothing, force fetching from db
       return c;
     }
 }
