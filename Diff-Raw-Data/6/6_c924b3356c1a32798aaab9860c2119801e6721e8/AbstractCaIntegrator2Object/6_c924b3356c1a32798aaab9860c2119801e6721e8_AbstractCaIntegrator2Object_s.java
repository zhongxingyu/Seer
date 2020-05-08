 /**
  * Copyright 5AM Solutions Inc, ESAC, ScenPro & SAIC
  *
  * Distributed under the OSI-approved BSD 3-Clause License.
  * See http://ncip.github.com/caintegrator/LICENSE.txt for details.
  */
 package gov.nih.nci.caintegrator.domain;
 
 import java.io.Serializable;
 import java.util.Comparator;
 
 import org.apache.commons.lang3.builder.HashCodeBuilder;
 
 /**
  * Base class for all domain objects.
  */
 public abstract class AbstractCaIntegrator2Object implements Serializable, Cloneable {
 
     /**
      * Default serialize.
      */
     private static final long serialVersionUID = 1L;
 
     /**
      * Comparator that orders objects by id.
      */
     public static final Comparator<AbstractCaIntegrator2Object> ID_COMPARATOR =
         new Comparator<AbstractCaIntegrator2Object>() {
         public int compare(AbstractCaIntegrator2Object object1, AbstractCaIntegrator2Object object2) {
             return (int) (object1.getId() - object2.getId());
         }
     };
     private Long id;
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean equals(Object object) {
         if (object == this) {
             return true;
         } else if (!(getClass().isInstance(object))) {
             return false;
         } else {
             AbstractCaIntegrator2Object caIntegrator2Object = (AbstractCaIntegrator2Object) object;
             return getId() != null && getId().equals(caIntegrator2Object.getId());
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(getId()).toHashCode();
     }
 
     /**
      * @return the id
      */
     public Long getId() {
         return id;
     }
 
     /**
      * @param id the id to set
      */
     public void setId(Long id) {
         this.id = id;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected AbstractCaIntegrator2Object clone() throws CloneNotSupportedException {
         AbstractCaIntegrator2Object clone = (AbstractCaIntegrator2Object) super.clone();
         clone.setId(null);
         return clone;
     }
 }
