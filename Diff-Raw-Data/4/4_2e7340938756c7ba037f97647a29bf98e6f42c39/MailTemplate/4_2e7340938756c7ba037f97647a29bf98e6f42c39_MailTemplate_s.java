 package com.aciertoteam.mail.entity;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import com.aciertoteam.common.entity.AbstractEntity;
 
 /**
  * @author Bogdan Nechyporenko
  */
 @Entity
 public class MailTemplate extends AbstractEntity {
 
     @Column(unique = true, nullable = false)
     private String templateName;
 
     @Column(nullable = false)
     private String subjectName;
 
     MailTemplate() {
         //hibernate
     }
 
     public MailTemplate(String templateName, String subjectName) {
         this.templateName = templateName;
         this.subjectName = subjectName;
     }
 
     public String getTemplateName() {
         return templateName;
     }
 
     public void setTemplateName(String templateName) {
         this.templateName = templateName;
     }
 
     public String getSubjectName() {
         return subjectName;
     }
 
     public void setSubjectName(String subjectName) {
         this.subjectName = subjectName;
     }
 }
