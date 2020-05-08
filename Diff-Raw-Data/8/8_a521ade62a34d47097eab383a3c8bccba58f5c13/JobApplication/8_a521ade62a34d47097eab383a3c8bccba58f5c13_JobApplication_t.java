 package models;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.Lob;
 import javax.persistence.OneToMany;
 
 import play.data.validation.Email;
 import play.db.jpa.Model;
 import play.utils.Utils;
 
 @Entity
 public class JobApplication extends Model {
 
     public static enum JobStatus {
         NEW, INPROGRESS, ARCHIVED
     }
 
     public String name;
 
     @Email
     public String email;
 
     public Date submitted;
 
     public String phone;
     
     @Lob
     public String message;
 
     // Separated by commas
     public String tags;
 
    public JobStatus status = JobStatus.NEW;
 
     @OneToMany
     public List<Attachment> attachments;
     
     public JobApplication(String name, String email, String message, List<Attachment> attachments) {
         this.name = name;
         this.email = email;
         this.message = message;
         //this.attachments = attachments;
     }
 
     @OneToMany
     public List<Note> notes;
 
     public List<String> tagsList() {
         if (tags == null) {
             return new ArrayList<String>();
         }
         return Arrays.asList(tags.split(","));
     }
 
     public void changeTags(List<String> tagsList) {
         tags = Utils.join(tagsList, ",");
     }
 
     @Override
     public String toString() {
         return name + " <" + email + ">";
     }
 
 }
