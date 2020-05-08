 package sibli;
 
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.users.User;
 
 //import java.util.*;
 import java.lang.Long;
 import java.util.Date;
 import javax.jdo.annotations.IdGeneratorStrategy;
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 
 /*
   JS notation:
   url     : new db.LinkProperty({required: true}),
   added   : new db.DateTimeProperty({autoNowAdd: true}),
   updated : new db.DateTimeProperty({autoNowAdd: true}),
   status  : new db.IntegerProperty()
 */
 
 @PersistenceCapable
 public class Host {
     @PrimaryKey
     @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
     private Key key;
 
     @Persistent
     private String url;
 
     @Persistent
     private Date added;
 
     @Persistent
     private Date updated;
 
     @Persistent
     private Long status;
 
 
     /**
      * Constructor
      */
     public Host(String url,  Date added, Date updated, String status) {
         this.url = url;
         this.added = added;
         this.updated = updated;
         this.status = Long.parseLong(status);
     }
 
 
     /**
      * Getters
      */
     public Key getKey() {
         return key;
     }
 
     public String getUrl() {
         return (String) url;
     }
 
     public Date getAdded() {
         return (Date) added;
     }
 
     public Date getUpdated() {
         return (Date) updated;
     }
 
     public Long getStatus() {
         return (Long) status;
     }
 
 
     /**
      * Setters
      */
    public Host setUrl(String url) {
         this.url = url;
         return this;
     }
 
    public Host setAdded(Date added) {
         this.added = added;
         return this;
     }
 
    public Host setUpdated(Date updated) {
         this.updated = updated;
         return this;
     }
 
    public Host setStatus(String status) {
         this.status = Long.parseLong(status);
         return this;
     }
 
 } // Host class
