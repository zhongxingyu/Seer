 /*
  * JBILLING CONFIDENTIAL
  * _____________________
  *
  * [2003] - [2012] Enterprise jBilling Software Ltd.
  * All Rights Reserved.
  *
  * NOTICE:  All information contained herein is, and remains
  * the property of Enterprise jBilling Software.
  * The intellectual and technical concepts contained
  * herein are proprietary to Enterprise jBilling Software
  * and are protected by trade secret or copyright law.
  * Dissemination of this information or reproduction of this material
  * is strictly forbidden.
  */
 
 package com.sapienter.jbilling.server.item;
 
 import com.sapienter.jbilling.server.item.db.PlanDTO;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Brian Cowdery
  * @since 20-09-2010
  */
 public class PlanWS implements Serializable {
 
     private Integer id;
     private Integer itemId; // plan subscription item
     private Integer periodId; // plan item period
     private String description;
     private List<PlanItemWS> planItems = new ArrayList<PlanItemWS>();
 
     public PlanWS() {
     }
 
     public PlanWS(PlanDTO dto, List<PlanItemWS> planItems) {
         this.id = dto.getId();
         this.description = dto.getDescription();
         this.planItems = planItems;
 
         if (dto.getItem() != null) this.itemId = dto.getItem().getId();
         if (dto.getPeriod() != null) this.periodId = dto.getPeriod().getId();
     }
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public Integer getItemId() {
         return itemId;
     }
 
     public void setItemId(Integer itemId) {
         this.itemId = itemId;
     }
 
     public Integer getPlanSubscriptionItemId() {
         return getItemId();
     }
 
     public void setPlanSubscriptionItemId(Integer planSubscriptionItemId) {
         setItemId(planSubscriptionItemId);
     }
 
     public Integer getPeriodId() {
         return periodId;
     }
 
     public void setPeriodId(Integer periodId) {
         this.periodId = periodId;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public List<PlanItemWS> getPlanItems() {
         return planItems;
     }
 
     public void setPlanItems(List<PlanItemWS> planItems) {
         this.planItems = planItems;
     }
 
     public void addPlanItem(PlanItemWS planItem) {
         getPlanItems().add(planItem);
     }
 
     @Override
     public String toString() {
         return "PlanWS{"
                + "id=" + id
                + ", itemId=" + itemId
                + ", periodId=" + periodId
                + ", description='" + description + '\''
                + ", planItems=" + planItems
                + '}';
     }
 }
