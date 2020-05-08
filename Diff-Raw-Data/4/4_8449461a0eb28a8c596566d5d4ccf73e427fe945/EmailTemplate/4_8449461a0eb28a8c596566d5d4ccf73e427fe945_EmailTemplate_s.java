 package com.archer.livequote.db.domain;
 
 import com.fasterxml.jackson.annotation.JsonIgnore;
 import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
 import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
 import org.bson.types.ObjectId;
 import org.springframework.data.annotation.Id;
 import org.springframework.data.mongodb.core.mapping.Document;
 import org.springframework.data.mongodb.core.mapping.Field;
 
 import java.io.Serializable;
 
 /**
  * @author Aaron Yang
  */
 @JsonIgnoreProperties(ignoreUnknown = true)
 @Document(collection = "email_templates")
 public class EmailTemplate implements Serializable, Cloneable {
     @Id
     @JsonIgnore
     private ObjectId id;
 
     @JsonProperty("guid")
     private String guid;
 
     private String description;
 
     @JsonProperty("template_file")
     @Field("template_file")
     private String templateFile;
 
     private String from;
 
     private String subject;
 
     private boolean html;
 
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
 
     public String getTemplateFile() {
         return templateFile;
     }
 
     public void setTemplateFile(String templateFile) {
         this.templateFile = templateFile;
     }
 
     public String getFrom() {
         return from;
     }
 
     public void setFrom(String from) {
         this.from = from;
     }
 
     public String getSubject() {
         return subject;
     }
 
     public void setSubject(String subject) {
         this.subject = subject;
     }
 
     public boolean isHtml() {
         return html;
     }
 
     public void setHtml(boolean html) {
         this.html = html;
     }
 
     public ObjectId getId() {
         return id;
     }
 
     public void setId(ObjectId id) {
         this.id = id;
     }
 
     public String getGuid() {
         return guid;
     }
 
     public void setGuid(String guid) {
         this.guid = guid;
     }
 
     @Override
     public String toString() {
         return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE, true, true);
     }
 }
