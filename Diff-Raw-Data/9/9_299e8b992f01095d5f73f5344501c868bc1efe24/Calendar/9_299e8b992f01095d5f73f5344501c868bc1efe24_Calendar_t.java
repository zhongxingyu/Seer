 package bean;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Id;
 import javax.persistence.CascadeType;
 import javax.persistence.FetchType;
 import java.util.ArrayList;
 import java.util.Collection;
 
 @Entity
 public class Calendar implements java.io.Serializable
 {
     // this id must be added for the databasemanager - just use an autoincrement here
     @Id @GeneratedValue(strategy=GenerationType.AUTO)
     private int id;
 
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
     private Collection<Appointment> appointments;
 }
