 package com.github.ddth.plommon.bo;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.lang3.builder.EqualsBuilder;
 import org.apache.commons.lang3.builder.HashCodeBuilder;
 
 import com.fasterxml.jackson.annotation.JsonProperty;
 import com.github.ddth.plommon.utils.DPathUtils;
 import com.github.ddth.plommon.utils.JsonUtils;
 
 /**
  * Base class for application BOs.
  * 
  * @author Thanh Nguyen <btnguyen2k@gmail.com>
  * @since 0.3.0
  */
 public class BaseBo {
 
     @JsonProperty
     private Map<String, Object> attributes = new HashMap<String, Object>();
 
     /**
      * Has the BO been changed?
      * 
      * @return
      * @since 0.3.1
      */
     public boolean isDirty() {
        Boolean result = getAttribute("__dirty__", Boolean.class);
        return result != null ? result.booleanValue() : null;
    }

    /**
     * Marks that the BO is dirty.
     */
    protected void markDirty() {
        DPathUtils.setValue(attributes, "__dirty__", Boolean.TRUE);
    }

    /**
     * Marks that the BO is no longer dirty.
     */
    public void markClean() {
        DPathUtils.setValue(attributes, "__dirty__", Boolean.FALSE);
     }
 
     /**
      * Gets a BO's attribute.
      * 
      * @param dPath
      * @return
      * @see DPathUtils
      */
     protected Object getAttribute(String dPath) {
         return DPathUtils.getValue(attributes, dPath);
     }
 
     /**
      * Gets a BO's attribute.
      * 
      * @param dPath
      * @param clazz
      * @return
      * @see DPathUtils
      */
     protected <T> T getAttribute(String dPath, Class<T> clazz) {
         return DPathUtils.getValue(attributes, dPath, clazz);
     }
 
     /**
      * Sets a BO's attribute.
      * 
      * @param dPath
      * @param value
      * @return
      * @see DPathUtils
      */
     protected BaseBo setAttribute(String dPath, Object value) {
         DPathUtils.setValue(attributes, dPath, value);
        markDirty();
         return this;
     }
 
     /**
      * Populates the BO with data from a Java map.
      * 
      * @param data
      * @return
      */
     synchronized public BaseBo fromMap(Map<String, Object> data) {
         attributes = new HashMap<String, Object>();
         if (data != null) {
             attributes.putAll(data);
         }
        markClean();
         return this;
     }
 
     /**
      * Serializes the BO to a Java map.
      * 
      * @return
      */
     synchronized public Map<String, Object> toMap() {
         Map<String, Object> result = new HashMap<String, Object>();
         if (attributes != null) {
             result.putAll(attributes);
         }
         return result;
     }
 
     /**
      * Populates the BO with data from a JSON string.
      * 
      * @param jsonString
      * @return
      */
     synchronized public BaseBo fromJson(String jsonString) {
         BaseBo other = JsonUtils.fromJsonString(jsonString, BaseBo.class);
         if (other != null) {
             this.attributes = other.attributes;
         }
        markClean();
         return this;
     }
 
     /**
      * Serializes the BO to JSON string.
      * 
      * @return
      */
     synchronized public String toJson() {
         return JsonUtils.toJsonString(this);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean equals(Object obj) {
         if (!(obj instanceof BaseBo)) {
             return false;
         }
         BaseBo other = (BaseBo) obj;
         EqualsBuilder eb = new EqualsBuilder();
        eb.append(attributes, other.attributes);
         return eb.isEquals();
     }
 
     /**
      * {@inheritDoc}
      * 
      * @since 0.3.1
      */
     @Override
     public int hashCode() {
         HashCodeBuilder hcb = new HashCodeBuilder(19, 81);
        hcb.append(attributes);
         return hcb.hashCode();
     }
 }
