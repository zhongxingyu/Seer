 package edu.unlv.kilo.domain;
 
 import java.util.Date;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import org.springframework.format.annotation.DateTimeFormat;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
 import org.springframework.roo.addon.serializable.RooSerializable;
 import org.springframework.roo.addon.tostring.RooToString;
 
 @RooJavaBean
 @RooToString
 @RooJpaActiveRecord
 @RooSerializable
 public class TransactionDescription {
 
    private String description;
 
     @Temporal(TemporalType.TIMESTAMP)
     @DateTimeFormat(style = "M-")
     private Date timeof;
 
     private String comment;
 }
