 package org.sukrupa.student;
 
 import org.sukrupa.platform.RequiredByFramework;
 
 public class StudentProfile {
 
     private String studentName;
     private String dateOfBirth;
     private String gender;
     private String studentBackground;
     private String studentDisciplinary;
     private String comments;
     private String studentId;
 
 
     public StudentProfile(String studentName, String dateOfBirth, String gender, String studentBackground, String studentDisciplinary, String comments, String studentId) {
 
         this.studentName = studentName;
         this.dateOfBirth = dateOfBirth;
         this.gender = gender;
         this.studentBackground = studentBackground;
         this.studentDisciplinary = studentDisciplinary;
         this.comments = comments;
         this.studentId = studentId;
     }
 
     @RequiredByFramework
     public StudentProfile() {
     }
 
     public void setStudentName(String studentName) {
         this.studentName = studentName;
     }
 
     public void setDateOfBirth(String dateOfBirth) {
         this.dateOfBirth = dateOfBirth;
     }
 
     public void setGender(String gender) {
         this.gender = gender;
     }
 
     public void setStudentBackground(String studentBackground) {
         this.studentBackground = studentBackground;
     }
 
     public void setStudentDisciplinary(String studentDisciplinary) {
         this.studentDisciplinary = studentDisciplinary;
     }
 
     public void setComments(String comments) {
         this.comments = comments;
 
     }
 
     public String getStudentName(){
         return studentName;
     }
 
     public String getDateOfBirth(){
         return dateOfBirth;
     }
 
     public String getGender(){
         return gender;
     }
 
     public String getStudentBackground(){
         return studentBackground;
     }
 
     public String getStudentDisciplinary(){
         return studentDisciplinary;
     }
 
     public String getComments(){
         return comments;
     }
 
     public String composeHtmlMessage() {
         StringBuilder htmlMessage = new StringBuilder();
         htmlMessage.append("<html>");
         htmlMessage.append("<body>");
        comments = comments.replaceAll("\r","<br><br/>");
         htmlMessage.append("<p style=\"width:250px\">" + comments + "</p>");
         htmlMessage.append("<table border=\"0\">");
         htmlMessage.append("<tr><td colspan=\"2\">" + "<img src=\"http://localhost:8080/getstudentimage/"+studentId+"/image\">" + "</td></tr>");
         createTableRow(htmlMessage, "<b>Name:</b>", studentName);
         createTableRow(htmlMessage, "<b>Date Of Birth:</b>", dateOfBirth);
         createTableRow(htmlMessage, "<b>Gender:</b>", gender);
         createTableRow(htmlMessage, "<b>Background:</b>", studentBackground);
         createTableRow(htmlMessage, "<b>Disciplinary:</b>", studentDisciplinary);
         htmlMessage.append("</table>");
         htmlMessage.append("</body>");
         htmlMessage.append("</html>");
         return htmlMessage.toString();
     }
 
     private void createTableRow(StringBuilder htmlMessage, String attributeName, String value) {
         htmlMessage.append("<tr><td>" + attributeName + "</td><td>");
         htmlMessage.append(value);
         htmlMessage.append("</td></tr>");
     }
 
     public String getStudentId() {
         return studentId;
     }
 
     public void setStudentId(String studentId) {
         this.studentId = studentId;
     }
 }
