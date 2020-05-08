 package edu.northwestern.bioinformatics.studycalendar.domain.auditing;
 
 import org.hibernate.annotations.Type;
 
import javax.persistence.Embeddable;
 import java.util.Date;
 import java.sql.Timestamp;
 
 /**
  * Subclass of core-commons' DataAuditInfo that aliases the "on" property as "time".
  * This is so that it can be used in HQL queries ("on" is apparently a reserved word).
  *
  * @author Rhett Sutphin
  */
 public class DataAuditInfo extends edu.nwu.bioinformatics.commons.DataAuditInfo {
     public DataAuditInfo() { }
 
     public DataAuditInfo(String by, String ip) {
         super(by, ip);
     }
 
     public DataAuditInfo(String by, String ip, Date on) {
         super(by, ip, on);
     }
 
     public static DataAuditInfo copy(edu.nwu.bioinformatics.commons.DataAuditInfo source) {
         return new DataAuditInfo(
             source.getBy(), source.getIp(), source.getOn()
         );
     }
 
     public Date getTime() {
         return getOn();
     }
 
     public void setTime(Date on) {
         setOn(on);
     }
 
     /* Have to duplicate other accessors so that it will work with annotations without
        annotating the superclass. */
     public String getUsername() { return super.getBy(); }
     public void setUsername(String by) { super.setBy(by); }
     public String getIp() { return super.getIp(); }
     public void setIp(String ip) { super.setIp(ip); }
 }
