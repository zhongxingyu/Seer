 package com.aciertoteam.common.entity;
 
 import com.aciertoteam.common.interfaces.IAbstractEntity;
 import org.apache.commons.lang3.ObjectUtils;
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.hibernate.annotations.Filter;
 import org.hibernate.annotations.FilterDef;
 import org.hibernate.annotations.ParamDef;
 
 import javax.persistence.Column;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.MappedSuperclass;
 import java.util.Date;
 
 /**
  * @author Bogdan Nechyporenko
  */
 @MappedSuperclass
@FilterDef(name = AbstractEntity.VALID_THRU_FILTER, parameters = @ParamDef(name = AbstractEntity.NOW_PARAM, type = "timestamp"), defaultCondition = "(valid_thru is null or valid_thru > :now)")
 @Filter(name = AbstractEntity.VALID_THRU_FILTER)
 public abstract class AbstractEntity<T extends AbstractEntity> implements IAbstractEntity, Cloneable {
 
     private static final long serialVersionUID = 1L;
 
     public static final String VALID_THRU_FILTER = "validThruFilter";
     public static final String NOW_PARAM = "now";
 
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Long id;
 
     @Column
     private Date validFrom;
 
     @Column
     private Date validThru;
 
     @Column
     private Date timestamp;
 
     @Column
     private Long updatedBy;
 
     protected AbstractEntity() {
         this.validFrom = new Date();
         this.timestamp = new Date();
     }
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public Date getValidFrom() {
         return validFrom;
     }
 
     public void setValidFrom(Date validFrom) {
         this.validFrom = validFrom;
     }
 
     public Date getValidThru() {
         return validThru;
     }
 
     public Date getTimestamp() {
         return timestamp;
     }
 
     public void setTimestamp(Date timestamp) {
         this.timestamp = timestamp;
     }
 
     public boolean isDeleted() {
         return this.validThru != null;
     }
 
     public void closeEndPeriod() {
         validThru = new Date();
     }
 
     @Override
     public void openEndPeriod() {
         validThru = null;
     }
 
     public Long getUpdatedBy() {
         return updatedBy;
     }
 
     public void setUpdatedBy(Long updatedBy) {
         this.updatedBy = updatedBy;
     }
 
     @JsonIgnore
     public IAbstractEntity getEntity() {
         return this;
     }
 
     public boolean isNew() {
         return getId() == null;
     }
 
     /**
      * Additional options that will be added to the json object
      * 
      * @return String[]
      */
     public String[] getSelectItemOptions() {
         return new String[0];
     }
 
     public void check() {
         /* By default do not check the internal state of the current entity */
     }
 
     public void closeEndPeriod(Date date) {
         this.validThru = date;
     }
 
     @SuppressWarnings({ "unchecked", "CloneDoesntDeclareCloneNotSupportedException" })
     @Override
     public T clone() {
         try {
             return (T) super.clone();
         } catch (CloneNotSupportedException e) {
             throw new IllegalArgumentException(e.getMessage(), e);
         }
     }
 
     @Override
     public String getStringId() {
         return ObjectUtils.toString(id);
     }
 }
