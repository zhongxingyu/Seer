 package com.epicamble.tip.model;
 
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.ManyToOne;
 import org.hibernate.annotations.Type;
 import org.springframework.data.jpa.domain.AbstractPersistable;
 
 /**
  *
  * @author Ollie Edwards <oliver.s.edwards@gmail.com>
  */
 @Entity
 public class Units extends AbstractPersistable<Long> {
     
     public enum UNIT_TYPE {
         SPACE_DOCK,
         CARRIER,
         GROUND_FORCE,
         DREADNOUGHT,
         FIGHTER,
         PDS
     }
     
     @Enumerated(EnumType.STRING) 
     protected UNIT_TYPE type;
     protected Integer count;
    @ManyToOne
     protected Race owningRace;
 
     public UNIT_TYPE getType() {
         return type;
     }
 
     public void setType(UNIT_TYPE type) {
         this.type = type;
     }
 
     public Integer getCount() {
         return count;
     }
 
     public void setCount(Integer count) {
         this.count = count;
     }
 
     public Race getOwningRace() {
         return owningRace;
     }
 
     public void setOwningRace(Race owningRace) {
         this.owningRace = owningRace;
     }
     
 }
