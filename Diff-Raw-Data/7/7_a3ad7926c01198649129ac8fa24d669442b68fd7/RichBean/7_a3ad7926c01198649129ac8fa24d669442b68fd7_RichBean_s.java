 package be.cegeka.rsvz;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.event.ActionEvent;
 import java.util.Date;
 
 @ManagedBean(name = "richBean")
 @SessionScoped
 public class RichBean {
 
     private String firstName;
     private String lastName;
     private Date calendar;
     private String text;
 
     public RichBean() {
        firstName = "Ion";
        lastName = "Vasile";
         setCalendar(new Date());
        text = "";
     }
 
     public String getFirstName() {
         return firstName;
     }
 
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
     public String getLastName() {
         return lastName;
     }
 
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 
 
     public Date getCalendar() {
         return calendar;
     }
 
     public void setCalendar(Date calendar) {
         this.calendar = calendar;
     }
 
     public String getText() {
         return text;
     }
 
     public void setText(String text) {
         this.text = text;
     }
 }
