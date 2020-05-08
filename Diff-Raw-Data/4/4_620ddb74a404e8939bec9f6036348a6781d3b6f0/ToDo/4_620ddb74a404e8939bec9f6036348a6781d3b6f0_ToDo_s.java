 package models;
 
 import org.apache.commons.lang.StringUtils;
 import org.hibernate.annotations.Cascade;
 import play.data.validation.MaxSize;
 import play.data.validation.Required;
 import play.db.jpa.Model;
 
 import javax.persistence.*;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import json.JsonExclude;
 
 /**
  * Created by IntelliJ IDEA.
  * User: super
  * Date: 3/17/12
  * Time: 3:12 PM
  * To change this template use File | Settings | File Templates.
  */
 @Entity
 @Table(name="todo")
 public class ToDo extends Model {
 
     @Required
     @MaxSize(255)
     @Column(name = "title", nullable = false)
     public String title;
 
     @Temporal(TemporalType.TIMESTAMP)
     @Column(name = "date_created", nullable = true)
     public Date dateCreated;
 
     @Temporal(TemporalType.TIMESTAMP)
     @Column(name = "last_updated", nullable = true)
     public Date lastUpdated;
 
     @Temporal(TemporalType.TIMESTAMP)
     @Column(name = "date_completed", nullable = true)
     public Date dateCompleted;
 
     /**
      * annotates at the method level so that setDateDue will be executed and so is dateDueInDays
      * @return
      */
     @Temporal(TemporalType.TIMESTAMP)
     @Column(name = "date_due", nullable = true)
     @Access(AccessType.PROPERTY)
     public Date dateDue;
 
     @Column(name = "note", nullable = true)
     public String note;
 
     @Required
     public int priority;
 
     @ManyToOne
     @JoinColumn(name="parent_id", nullable = true)
     public ToDo parent;  // for sub task feature
 
     @ManyToMany
     public List<Tag> tags;
 
     @Required
     @ManyToOne
     @JoinColumn(name="todolist_id", nullable = false)
     @JsonExclude
     public ToDoList toDoList;
 
     @Column(name = "order_index", nullable = false)
     public int orderIndex;
 
     public boolean completed;
     
     @Transient
     public Float dateDueInDays;
     
     public ToDo() {
         dateCreated = new Date();
         lastUpdated = dateCreated;
     }
     
     public void setCompleted(boolean completed) {
         this.completed = completed;
         if (completed) {
             dateCompleted = new Date();
         } else {
             dateCompleted = null;
         }
     }
     
     public void setDateDue(Date dateDue) {
         this.dateDue = dateDue;
         if (dateDue != null) {
             long now = System.currentTimeMillis();
             long diff = dateDue.getTime() - now;
             dateDueInDays = (float) diff/86400000L;
         } else {
             dateDueInDays = null;
         }
     }
 
     public Date getDateDue() {
         return dateDue;
     }
 }
