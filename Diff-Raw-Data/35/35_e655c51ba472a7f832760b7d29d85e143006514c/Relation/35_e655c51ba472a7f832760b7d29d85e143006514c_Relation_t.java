 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package relation;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author kainlite
  */
 public class Relation {          
     /**
      * @param args the command line arguments
         */
    public static void main(String[] args) {
        Student student_1 = new Student("student_1");
        Student student_2 = new Student("student_2");
        Teacher teacher = new Teacher("teacher");
        Course course_1 = new Course("course_1");
        Course course_2 = new Course("course_2");
 
        // Assign teacher to course
        course_1.setTeacherName(teacher.getName());
 
        // Enrolling student_1 in course_1
        try {
            course_1.enrollStudent(student_1);
        } catch (CourseException ex) {
            System.out.println("Exception in course_1.enrollStudent(student_1): " + ex);
        }
 
        // Enrolling student_2 in course_1
        try {
            course_1.enrollStudent(student_2);
        } catch (CourseException ex) {
            System.out.println("Exception in course_1.enrollStudent(student_2): " + ex);
        }
 
        // Enrolling student_1 in course_2 that does not have an assigned teacher
        try {
            course_2.enrollStudent(student_1);
        } catch (CourseException ex) {
            System.out.println("Exception in course_2.enrollStudent(student_1): " + ex);
        }
        
        // Printing enrolled students for course_1
       for(int i = 0; i <= course_1.enrolledStudents().size()-1; i++) {
           System.out.println("Enrolled in course_1: " + course_1.enrolledStudents().get(i) + 
                   " On date: " + course_1.getEnrolledDates().get(i)
                   );
        }
 
        // Printing enrolled students for course_2
       for(int i = 0; i <= course_2.enrolledStudents().size()-1; i++) {
           System.out.println("Enrolled in course_2: " + course_2.enrolledStudents().get(i) + 
                   " On date: " + course_2.getEnrolledDates().get(i));
        }
        
        // Printing counts of enrolled students in courses
        System.out.println("course_1 count:" + course_1.countEnrolledStudents());
        System.out.println("course_2 count:" + course_2.countEnrolledStudents());
        
        // Printing students count of courses
        System.out.println("student_1 count:" + student_1.getCourseQuantity());
        System.out.println("student_2 count:" + student_2.getCourseQuantity());
    }
 }
