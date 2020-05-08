 package com.adde.webbapp.model.entity;
 
 import java.io.Serializable;
 import java.util.GregorianCalendar;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 /**
  * @author ehannes
  */
 @Entity
 public class SimpleEditorEntry extends AbstractEntity implements Serializable {
     
     @ManyToOne(cascade={CascadeType.REFRESH})
    @JoinColumn(nullable=false)
     private Person editor;
    @Temporal(TemporalType.TIMESTAMP)
     @Column(nullable=false)
     private GregorianCalendar modificationTime;
 
     public SimpleEditorEntry() {}
     
     public SimpleEditorEntry(Person person) {
         this.editor = person;
     }
 
     public Person getEditor() {
         return editor;
     }
 
     public void setEditor(Person editor) {
         this.editor = editor;
     }   
  
     public GregorianCalendar getModificationTime() {
         return modificationTime;
     }
 
     public void setModificationTime(GregorianCalendar modificationTime) {
         this.modificationTime = modificationTime;
     }
     
     @Override
     public String toString() {
         return "SimpleEditorEntry{" + super.toString() + ", Person: " + editor.toString()
                 + ", Edited: " + modificationTime.get(GregorianCalendar.DAY_OF_MONTH)
                 + "/" + (modificationTime.get(GregorianCalendar.MONTH)+1)
                 + " " + modificationTime.get(GregorianCalendar.YEAR)
                 + " at " + modificationTime.get(GregorianCalendar.HOUR)
                 + ":" + modificationTime.get(GregorianCalendar.MINUTE)
                 + ":" + modificationTime.get(GregorianCalendar.SECOND);
     }
 }
