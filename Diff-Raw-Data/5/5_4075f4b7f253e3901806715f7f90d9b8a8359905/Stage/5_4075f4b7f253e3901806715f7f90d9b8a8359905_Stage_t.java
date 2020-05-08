 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package entities;
 
 import java.io.Serializable;
 import java.util.Objects;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
 
 /**
  *
  * @author dmontanor
  */
 @Entity
 @Table(name = "stage")
 public class Stage implements Serializable {
 
     @Id
     @GeneratedValue
     private Integer id;
 
     @Column(name = "name")
     private String name;
 
    @ManyToOne
     @JoinColumn(name="flow_id")
     private Flow flow;
 
     @OneToOne
     @JoinColumn(name = "next_stage_id")
     private Stage next;
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public void setFlow(Flow flow) {
         this.flow = flow;
     }
 
     public Flow getFlow() {
         return flow;
     }
 
     public Stage getNext() {
         return next;
     }
 
     public void setNext(Stage next) {
         this.next = next;
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 89 * hash + Objects.hashCode(this.id);
         return hash;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final Stage other = (Stage) obj;
         if (!Objects.equals(this.id, other.id)) {
             return false;
         }
         return true;
     }
 
 
 }
