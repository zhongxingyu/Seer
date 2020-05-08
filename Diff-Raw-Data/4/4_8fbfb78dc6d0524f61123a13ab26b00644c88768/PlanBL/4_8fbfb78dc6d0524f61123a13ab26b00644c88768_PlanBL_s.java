 /*
  jBilling - The Enterprise Open Source Billing System
  Copyright (C) 2003-2011 Enterprise jBilling Software Ltd. and Emiliano Conde
 
  This file is part of jbilling.
 
  jbilling is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  jbilling is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.
 
  You should have received a copy of the GNU Affero General Public License
  along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.sapienter.jbilling.server.item;
 
 import com.sapienter.jbilling.common.SessionInternalError;
 import com.sapienter.jbilling.server.item.db.ItemDTO;
 import com.sapienter.jbilling.server.item.db.PlanDAS;
 import com.sapienter.jbilling.server.item.db.PlanDTO;
 import com.sapienter.jbilling.server.item.db.PlanItemDTO;
 import com.sapienter.jbilling.server.item.event.NewPlanEvent;
 import com.sapienter.jbilling.server.item.event.PlanDeletedEvent;
 import com.sapienter.jbilling.server.item.event.PlanUpdatedEvent;
 import com.sapienter.jbilling.server.order.db.OrderPeriodDAS;
 import com.sapienter.jbilling.server.order.db.OrderPeriodDTO;
 import com.sapienter.jbilling.server.pricing.PriceModelBL;
 import com.sapienter.jbilling.server.pricing.PriceModelWS;
 import com.sapienter.jbilling.server.pricing.db.PriceModelDTO;
 import com.sapienter.jbilling.server.pricing.db.PriceModelStrategy;
 import com.sapienter.jbilling.server.pricing.util.AttributeUtils;
 import com.sapienter.jbilling.server.system.event.EventManager;
 import com.sapienter.jbilling.server.user.CustomerPriceBL;
 import com.sapienter.jbilling.server.user.db.CustomerDTO;
 import com.sapienter.jbilling.server.user.db.CustomerPriceDTO;
 import org.apache.log4j.Logger;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * Business Logic for PlanDTO CRUD operations and for subscribing and un-subscribing a
  * customer to a given plan. This class should be used for all Plan/Customer interactions.
  *
  * @author Brian Cowdery
  * @since 30-08-2010
  */
 public class PlanBL {
     private static final Logger LOG = Logger.getLogger(PlanBL.class);
 
     private PlanDAS planDas;
 
     private PlanDTO plan;
 
     public PlanBL() {
         _init();
     }
 
     public PlanBL(Integer planId) {
         _init();
         set(planId);
     }
 
     public PlanBL(PlanDTO plan) {
         _init();
         this.plan = plan;
     }
 
     public void set(Integer planId) {
         this.plan = planDas.find(planId); 
     }
 
     private void _init() {
         this.planDas = new PlanDAS();
     }
 
     public PlanDTO getEntity() {
         return plan;
     }
 
     /**
      * Convert this plan into a PlanWS web-service object
      * @return this plan as a web-service object
      */
     public PlanWS getWS() {
         return PlanBL.getWS(plan);
     }
 
     /**
      * Convert a given PlanDTO into a PlanWS web-service object
      * @param dto dto to convert
      * @return converted web-service object
      */
     public static PlanWS getWS(PlanDTO dto) {
         if (dto != null) {
             return new PlanWS(dto, PlanItemBL.getWS(dto.getPlanItems()));
         }
         return null;
     }
 
     /**
      * Convert a given PlanWS web-service object into a PlanDTO
      * @param ws ws object to convert
      * @return converted DTO object
      */
     public static PlanDTO getDTO(PlanWS ws) {
         if (ws != null) {
             if (ws.getItemId() == null)
                 throw new SessionInternalError("PlanDTO must have a plan subscription item.");
 
             if (ws.getPeriodId() == null)
                 throw new SessionInternalError("PlanDTO must have an applicable order period.");
 
             // subscription plan item
             ItemDTO item = new ItemBL(ws.getItemId()).getEntity();
 
             // plan period
             OrderPeriodDTO period = new OrderPeriodDAS().find(ws.getPeriodId());
 
             return new PlanDTO(ws, item, period, PlanItemBL.getDTO(ws.getPlanItems()));
         }
         return null;
     }
 
     /**
      * Validates all pricing models within the plan to ensure that they have the
      * correct attributes.
      *
      * @param plan plan to validate
      * @throws SessionInternalError if attributes are missing or of an incorrect type
      */
     public static void validateAttributes(PlanDTO plan) throws SessionInternalError {
         List<String> errors = new ArrayList<String>();
 
         for (PlanItemDTO planItem : plan.getPlanItems()) {
             for (PriceModelDTO model : planItem.getModels().values()) {
                 for (PriceModelDTO next = model; next != null; next = next.getNext()) {
                     try {
                         AttributeUtils.validateAttributes(next.getAttributes(), next.getStrategy());
                     } catch (SessionInternalError e) {
                         errors.addAll(Arrays.asList(e.getErrorMessages()));
                     }
                 }
             }
         }
 
         if (!errors.isEmpty()) {
             throw new SessionInternalError("Plan pricing attributes failed validation.",
                                            errors.toArray(new String[errors.size()]));
         }
     }
 
     // todo: add event logging for plans
 
     public Integer create(PlanDTO plan) {
         if (plan != null) {
             validateAttributes(plan);
 
             this.plan = planDas.save(plan);
 
             // trigger internal event
             EventManager.process(new NewPlanEvent(plan));
 
             return this.plan.getId();
         }
 
         LOG.error("Cannot save a null PlanDTO!");
         return null;
     }
 
     public void update(PlanDTO dto) {
         if (plan != null) {
 
             // un-subscribe existing customers before updating
             List<CustomerDTO> subscribers = getCustomersByPlan(plan.getId());
             for (CustomerDTO customer : subscribers) {
                 unsubscribe(customer.getBaseUser().getUserId());
             }
 
             // clean all remaining prices just-in-case there's an orphaned record
            purgeCustomerPrices();
 
             // do update
             validateAttributes(dto);
 
             plan.setDescription(dto.getDescription());
             plan.setItem(dto.getItem());
 
             plan.getPlanItems().clear();
             plan.getPlanItems().addAll(dto.getPlanItems());
 
             LOG.debug("Saving updates to plan " + plan.getId());
             this.plan = planDas.save(plan);
 
             // re-subscribe customers after plan has been saved
             for (CustomerDTO customer : subscribers) {
                 subscribe(customer.getBaseUser().getUserId());
             }
 
             // trigger internal event
             EventManager.process(new PlanUpdatedEvent(plan));
 
         } else {
             LOG.error("Cannot update, PlanDTO not found or not set!");
         }
     }
 
     public void addPrice(PlanItemDTO planItem) {
         if (plan != null) {
             PriceModelBL.validateAttributes(planItem.getModels().values());
 
             plan.addPlanItem(planItem);
 
             LOG.debug("Saving updates to plan " + plan.getId());
             this.plan = planDas.save(plan);
 
             refreshCustomerPrices();
 
             // trigger internal event
             EventManager.process(new PlanUpdatedEvent(plan));
             
         } else {
             LOG.error("Cannot add price, PlanDTO not found or not set!");
         }
     }
 
     public void delete() {
         if (plan != null) {
             purgeCustomerPrices();
             planDas.delete(plan);
 
             // trigger internal event
             EventManager.process(new PlanDeletedEvent(plan));
         } else {
             LOG.error("Cannot delete, PlanDTO not found or not set!");
         }
     }
 
     /**
      * Refreshes the customer plan item price mappings for all customers that have
      * subscribed to this plan. This method will remove all existing prices for the plan
      * and insert the current list of plan items into the customer price map.
      */
     public void refreshCustomerPrices() {
         if (plan != null) {
             LOG.debug("Refreshing customer prices for subscribers to plan " + plan.getId());
 
             for (CustomerDTO customer : getCustomersByPlan(plan.getId())) {
                 CustomerPriceBL bl = new CustomerPriceBL(customer);
                 bl.removePrices(plan.getId());
                 bl.addPrices(plan.getPlanItems());
             }
         } else {
             LOG.error("Cannot update customer prices, PlanDTO not found or not set!");
         }
     }
 
     /**
      * Removes all customer prices for the plan's current set of plan items. This will remove
      * prices for subscribed customers AND orphaned prices where the customers order has been
      * deleted in a non-standard way (DB delete, non API usage).
      */
     public void purgeCustomerPrices() {
         if (plan != null) {
             LOG.debug("Removing ALL remaining customer prices for plan " + plan.getId());
             new CustomerPriceBL().removeAllPrices(plan.getPlanItems());
         } else {
             LOG.error("Cannot purge customer prices, PlanDTO not found or not set!");
         }
 
     }
 
     /**
      * Subscribes a customer to all plans held by the given "plan subscription" item, adding all
      * plan item prices to a customer price map. 
      *
      * @param userId user id of the customer to subscribe
      * @param itemId item representing the subscription to a plan
      * @return list of saved customer price entries, empty if no prices applied to customer.
      */
     public static List<CustomerPriceDTO> subscribe(Integer userId, Integer itemId) {
         LOG.debug("Subscribing customer " + userId + " to plan subscription item " + itemId);
 
         List<CustomerPriceDTO> saved = new ArrayList<CustomerPriceDTO>();
 
         CustomerPriceBL customerPriceBl = new CustomerPriceBL(userId);
         for (PlanDTO plan : new PlanBL().getPlansBySubscriptionItem(itemId))
             saved.addAll(customerPriceBl.addPrices(plan.getPlanItems()));
 
         return saved;
     }
 
     /**
      * Subscribes a customer to this plan, adding all plan item prices to the customer price map.
      *
      * @param userId user id of the customer to subscribe
      * @return list of saved customer price entries, empty if no prices applied to customer.
      */
     public List<CustomerPriceDTO> subscribe(Integer userId) {
         LOG.debug("Subscribing customer " + userId + " to plan " + plan.getId());
 
         List<CustomerPriceDTO> saved = new ArrayList<CustomerPriceDTO>();
 
         CustomerPriceBL customerPriceBl = new CustomerPriceBL(userId);
             saved.addAll(customerPriceBl.addPrices(plan.getPlanItems()));
 
         return saved;
     }
 
     /**
      * Un-subscribes a customer from all plans held by the given "plan subscription" item,
      * removing all plan item prices from the customer price map.
      *
      * @param userId user id of the customer to un-subscribe
      * @param itemId item representing the subscription to a plan
      */
     public static void unsubscribe(Integer userId, Integer itemId) {
         LOG.debug("Un-subscribing customer " + userId + " from plan subscription item " + itemId);
 
         CustomerPriceBL customerPriceBl = new CustomerPriceBL(userId);
         for (PlanDTO plan : new PlanBL().getPlansBySubscriptionItem(itemId))
             customerPriceBl.removePrices(plan.getId());
     }
 
     /**
      * Un-subscribes a customer from this plan, removing all plan item prices from the customer price map.
      *
      * @param userId user id of the customer to un-subscribe
      */
     public void unsubscribe(Integer userId) {
         LOG.debug("Un-subscribing customer " + userId + " from plan " + plan.getId());
         new CustomerPriceBL(userId).removePrices(plan.getId());
     }
 
     /**
      * Returns true if the customer is subscribed to a plan held by the given "plan subscription" item.
      *
      * @param userId user id of the customer to check
      * @param itemId plan subscription item id
      * @return true if customer is subscribed, false if not
      */
     public static boolean isSubscribed(Integer userId, Integer itemId) {
         // items can have multiple plans, but it's possible that a customer may only
         // be subscribed to 1 of the plans depending on where we are in the workflow
         for (PlanDTO plan : new PlanBL().getPlansBySubscriptionItem(itemId))
             if (new PlanDAS().isSubscribed(userId, plan.getId()))
                 return true; // only return true if subscribed to one of the plans, otherwise keep checking.
 
         return false;
     }
 
     /**
      * Returns true if the customer is subscribed to this plan.
      *
      * @param userId user id of the customer to check
      * @return true if customer is subscribed, false if not
      */
     public boolean isSubscribed(Integer userId) {
         return planDas.isSubscribed(userId, plan.getId());
     }
 
     /**
      * Returns a list of all customers that have subscribed to the given plan. A customer
      * subscribes to a plan by adding the plan subscription item to a recurring order.
      *
      * @param planId id of plan
      * @return list of customers subscribed to the plan, empty if none found
      */
     public List<CustomerDTO> getCustomersByPlan(Integer planId) {
         return planDas.findCustomersByPlan(planId);
     }
 
     /**
      * Returns all plans that use the given item as the "plan subscription" item.
      *
      * @param itemId item id
      * @return list of plans, empty list if none found
      */
     public List<PlanDTO> getPlansBySubscriptionItem(Integer itemId) {
         return planDas.findByPlanSubscriptionItem(itemId);
     }
 
     /**
      * Returns all plans that affect the pricing of the given item, or that include
      * the item in a bundle.
      *
      * @param itemId item id
      * @return list of plans, empty list if none found
      */
     public List<PlanDTO> getPlansByAffectedItem(Integer itemId) {
         return planDas.findByAffectedItem(itemId);
     }
 }
