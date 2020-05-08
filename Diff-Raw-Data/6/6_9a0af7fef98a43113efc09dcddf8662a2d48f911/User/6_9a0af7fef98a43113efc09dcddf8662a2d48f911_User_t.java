 package dit126.group4.group4shop.core;
 
import java.io.Serializable;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 /**
  *
  * @author Group4
  */
 
 @Entity
@Table(name="USERS")
public class User implements Serializable {
     
     //private Long id; email could be id for each customer since we force them to log in 
     @Column(name="FIRSTNAME") private String firstName;
     @Column(name="LASTNAME") private String lastName;
     @Column(name="PASSWORD") private String password; // shoule be changed to use a more secure method 
      
     @Id
     @Column(name="EMAIL")
     private String email; // key when storing users in table.  
 }
