 package models;
 
 import play.*;
 import play.data.validation.*;
 import play.db.jpa.*;
 
 import javax.persistence.*;
 import java.util.*;
import models.Logicable;
 
 
 /**
  * The Class Woman.
  */
 @Entity
 public class Woman extends Model implements Logicable {
     
     public static enum Outcome {
         NONE, ALIVE, DEAD, STILL_BIRTH, LIVE_BIRTH
     };
     
     public static enum Trigger {
         NONE, REGISTRATION, DELIVERY, OUTCOME
     };
     
     public static enum Event {
         NONE, REGISTRATION, DELIVERY, OUTCOME, LMP
         
     };
     
     
     /** The woman name. */
     @Required
     public String name;
     
     /** The woman's unique ID. */
     @Required
     public Long   UID;
     
     /** The woman's husband name. */
     @Required
     public String husbandName;
     
     /** The sector ID. */
     @Required
     public Long   sectorId;
     
     /** The house hold ID. */
     @Required
     public Long   hhId;
     
     /** The mauza name. */
     public String mauzaName;
     
     /** The ti pin cd. */
     public Long   tiPinCd;
     
     /** The ac area. */
     public Long   acArea;
     
     /** The urine positive week. */
     public Long   urinePositiveWeek;
     
     /** The lmp. */
     public Date   lmp;
     
     /** The status. */
     public Short  status;
     
     /** Registration Date. */
     public Date   registered;
     
     
     /**
      * The Constructor.
      * 
      * @param UID
      *            the woman's unique ID
      * @param name
      *            the name
      * @param husbandName
      *            the husband name
      * @param sectorId
      *            the sector ID
      * @param hhId
      *            the house hold ID
      */
     public Woman(Long UID, String name, String husbandName, Long sectorId, Long hhId) {
     
         this.UID = UID;
         this.name = name;
         this.husbandName = husbandName;
         this.sectorId = sectorId;
         this.hhId = hhId;
         
     }
     
     @Override
     public void _save() {
     
         this.registered = new Date();
         super._save();
         Form.invoke(Trigger.REGISTRATION, this);
     }
     
     @Override
     public Date getEventDate(Event event) {
     
         switch (event) {
             case REGISTRATION:
                 return this.registered;
             case LMP:
                 return this.lmp;
         }
         
         return new Date();
     }
 }
