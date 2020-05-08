 package de.flower.common.model;
 
 import javax.persistence.*;
 import java.io.Serializable;
 
 /**
  * @author flowerrrr
  */
 @MappedSuperclass
 public abstract class AbstractBaseEntity implements Serializable, IEntity, Cloneable {
 
     /**
      * This constant is only needed for serialization purposes. It overwrites the default mechanism and so makes the BE longer binary compatible
      */
     private static final long serialVersionUID = 1L;
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;
 
     protected AbstractBaseEntity() {
 
     }
 
     /**
      * Gets the id.
      *
      * @return the id
      */
     @Override
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     /**
      * Returns true if this entity is transient (not saved yet).
      *
      * @return true, if checks if is transient
      */
     @Override
     public boolean isNew() {
         return (getId() == null);
     }
 
     //---Methods
     /* (non-Javadoc)
       * @see java.lang.Object#toString()
       */
     @Override
     public String toString() {
         StringBuilder retVal = new StringBuilder();
 
         retVal.append("[" + this.getClass().getSimpleName() + "@" + this.hashCode() + "]");
 
         return retVal.toString();
     }
 
     /**
      * The @Id fields are not copied.
      * Reason: you cannot copy a persistent object
      * with already existing primary key.
      *
      * @return
      */
     @Override
     public AbstractBaseEntity clone() {
         AbstractBaseEntity clone = null;
         try {
             clone = (AbstractBaseEntity) super.clone();
         } catch (CloneNotSupportedException e) {
             throw new RuntimeException(e);
         }
         clone.setId(null);
         return clone;
     }
 
     /**
      * Update from object.
      *
      * @param other the other
      */
     public void updateFromObject(AbstractBaseEntity other) {
         throw new UnsupportedOperationException("Method must be implemented in subclass!");
     }
 
     /**
      * The equality object.
      */
     @Transient
     private Object equalityObject;
 
     /**
      * The following methods are taken from
      * http://forum.hibernate.org/viewtopic.php?p=2191778.
      *
      * @param other the other
      * @return true, if equals
      */
     @Override
     public boolean equals(Object other) {
         if ((this == other)) {
             return true;
         }
 
         if ((other == null)) {
             return false;
         }
 
         if (!(this.getClass().isInstance(other))) {
             return false;
         }
 
         AbstractBaseEntity castOther = (AbstractBaseEntity) other;
 
         if (this.getId() == null) {
             return false;
         }
 
         if (castOther.getId() == null) {
             return false;
         }
 
         boolean equals = ((this.getId() == castOther.getId()) || ((this.getId() != null)
                 && (castOther.getId() != null) && this.getId().equals(
                 castOther.getId())));
 
         return equals;
     }
 
     /* (non-Javadoc)
       * @see java.lang.Object#hashCode()
       */
     @Override
     public int hashCode() {
         Object o = getEqualityObject();
 
         if (o == this) {
             return super.hashCode();
         } else {
             return o.hashCode();
         }
     }
 
     /**
      * Gets the equality object.
      *
      * @return the equality object
      */
     private Object getEqualityObject() {
         if (equalityObject == null) {
             equalityObject = (getId() == null) ? this : getId();
         }
 
         return equalityObject;
     }
 }
