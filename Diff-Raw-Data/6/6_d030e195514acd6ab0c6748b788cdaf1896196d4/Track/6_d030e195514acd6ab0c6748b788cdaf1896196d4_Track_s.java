 package com.socialcomputing.wps.server.persistence.hibernate;
 
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Table;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import org.hibernate.annotations.Index;
 
 @Entity
 @XmlRootElement
 public class Track {
 
     @Id @GeneratedValue(strategy=GenerationType.AUTO)
     @XmlElement
     private Long id;
     
     @XmlElement
     @Column(columnDefinition = "varchar(512)")
     private String      name;
 
     @XmlElement
     @Index(name="dateIndex")
     private Date        date;
 
     @XmlElement
     private long        duration;
     
     @XmlElement
     private boolean     success;
     
     public Track(String name) {
         super();
         this.name = name;
         this.date = new Date();
         this.duration = System.currentTimeMillis();
         this.success = false;
     }
     
     public String getName() {
         return name;
     }
     
     public Date getDate() {
         return date;
     }
     
     public long getDuration() {
         return duration;
     }
     
     public void stop( boolean success) {
         this.duration = System.currentTimeMillis() - this.duration;
         this.success = success;
     }
 }
