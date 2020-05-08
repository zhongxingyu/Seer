 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package relation;
 
 import java.util.Vector;
 
 /**
  *
  * @author kainlite
  */
 public class Course {
     private String name = new String();
     private String teacher_name = new String();
     private Inscription inscriptions = new Inscription();
     private int duration = 0;
     
     Course(String name) {
         this.name = name;
     }
     
     public String getName() {
         return name;
     }
     
     public void setName(String name) {
         this.name = name;
     }
     
     public String getTeacherName() {
         return teacher_name;
     }
     
     public void setTeacherName(String teacher_name) {
         this.teacher_name = teacher_name;
     }
     
     public void enrollStudent(Student student) throws CourseException {
         if (!this.teacher_name.equals("")) {
             inscriptions.addInscription(student.getName());
             student.incrementCourseQuantity();
         } else {
             throw new CourseException();
         }     
     }
     
     public Vector enrolledStudents() {       
         return inscriptions.getInscriptions();
     }
     
    public Vector getEnrolledDates() {
        return inscriptions.getDates();
    }
    
     public int countEnrolledStudents() {
         int sum = 0;
         
         for(Object element : inscriptions.getInscriptions()) {
          sum += 1;
         }
         
         return sum;
     }
     
 }
