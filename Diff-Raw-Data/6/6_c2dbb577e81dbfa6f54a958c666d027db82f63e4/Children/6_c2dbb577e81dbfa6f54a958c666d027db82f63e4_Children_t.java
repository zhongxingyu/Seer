 package circlespaymentclasses;
 
import java.text.SimpleDateFormat;
import java.util.Date;

 public class Children {
     
     private String childName;
     private String age;
     private String dateJoined;
     
     public Children() {
        this("John", "1", new SimpleDateFormat("dd/MM/yyyy").format(new Date()).toString());
     }
 
     public Children(String childName, String age, String dateJoined) {
         this.childName = childName;
         this.age = age;
         this.dateJoined = dateJoined;
     }
 
     public String getAge() {
         return age;
     }
 
     public void setAge(String age) {
         this.age = age;
     }
 
     public String getChildName() {
         return childName;
     }
 
     public void setChildName(String childName) {
         this.childName = childName;
     }
 
     public String getDateJoined() {
         return dateJoined;
     }
 
     public void setDateJoined(String dateJoined) {
         this.dateJoined = dateJoined;
     }
     
 }
