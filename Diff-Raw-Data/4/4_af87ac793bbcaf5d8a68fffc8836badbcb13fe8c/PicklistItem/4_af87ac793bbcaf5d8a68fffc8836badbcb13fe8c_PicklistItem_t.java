 package com.orangeleap.tangerine.domain.customization;
 
 import java.io.Serializable;
 
 import com.orangeleap.tangerine.domain.AbstractCustomizableEntity;
 import com.orangeleap.tangerine.domain.Auditable;
 import com.orangeleap.tangerine.domain.GeneratedId;
 
 public class PicklistItem extends AbstractCustomizableEntity implements Auditable, GeneratedId, Serializable {
 
     private static final long serialVersionUID = 1L;
 
     private String itemName;
     private String defaultDisplayValue;
     private String referenceValue;
     private String suppressReferenceValue;
     private Integer itemOrder;
     private boolean inactive = false;
     private String picklistId;
     private Auditable originalObject;
 
     public String getItemName() {
         return itemName;
     }
 
     public void setItemName(String itemName) {
         this.itemName = itemName;
     }
 
     public String getDefaultDisplayValue() {
         return defaultDisplayValue;
     }
 
     public void setDefaultDisplayValue(String defaultDisplayValue) {
         this.defaultDisplayValue = defaultDisplayValue;
     }
 
     public String getReferenceValue() {
         return referenceValue;
     }
 
     public void setReferenceValue(String referenceValue) {
         this.referenceValue = referenceValue;
     }
 
     public Integer getItemOrder() {
         return itemOrder;
     }
 
     public void setItemOrder(Integer position) {
         this.itemOrder = position;
     }
 
     public String getPicklistId() {
         return picklistId;
     }
 
     public void setPicklistId(String picklistId) {
         this.picklistId = picklistId;
     }
 
 	public void setOriginalObject(Auditable originalObject) {
 		this.originalObject = originalObject;
 	}
 
 	public Auditable getOriginalObject() {
 		return originalObject;
 	}
 
 	public void setInactive(boolean inactive) {
 		this.inactive = inactive;
 	}
 
 	public boolean isInactive() {
 		return inactive;
 	}
 
 	public void setSuppressReferenceValue(String suppressReferenceValue) {
 		this.suppressReferenceValue = suppressReferenceValue;
 	}
 
 	public String getSuppressReferenceValue() {
 		return suppressReferenceValue;
 	}
 
 	public String getValue() {
         return itemName;
     }
 	
     public String getDisplayValue() {
         return defaultDisplayValue;
     }
     
     public String getDescription() {
         return defaultDisplayValue;
     }
     
    public String toString() {
    	return this.itemName + ":" + this.itemOrder;
    }
    
 	
 }
