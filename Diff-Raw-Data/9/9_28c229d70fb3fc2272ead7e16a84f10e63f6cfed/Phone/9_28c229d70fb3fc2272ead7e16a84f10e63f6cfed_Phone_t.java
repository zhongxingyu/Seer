 package cs.wintoosa.domain;
 
 import java.io.Serializable;
 import java.util.List;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.validation.constraints.NotNull;
 
 /**
  *
  * @author jonimake
  */
 @Entity
 public class Phone implements Serializable{
     
     @GeneratedValue(strategy= GenerationType.AUTO)
     @Id
     private Long id;
     
     @NotNull
     private String phoneId;
     
     @OneToMany(targetEntity=SessionLog.class, mappedBy="phone")
     private List<SessionLog> sessions;
     
     private static final long serialVersionUID = 9654699l;
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public String getPhoneId() {
         return phoneId;
     }
 
     public void setPhoneId(String phoneId) {
         this.phoneId = phoneId;
     }
 
     public List<SessionLog> getSessions() {
         return sessions;
     }
 
     public void setSessions(List<SessionLog> sessions) {
         this.sessions = sessions;
     }
     
 }
