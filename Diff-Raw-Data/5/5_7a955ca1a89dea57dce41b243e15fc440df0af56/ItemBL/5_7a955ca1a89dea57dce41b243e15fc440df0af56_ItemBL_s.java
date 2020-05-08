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
 
 import com.sapienter.jbilling.common.CommonConstants;
 import com.sapienter.jbilling.common.SessionInternalError;
 import com.sapienter.jbilling.server.item.db.ItemDAS;
 import com.sapienter.jbilling.server.item.db.ItemDTO;
 import com.sapienter.jbilling.server.item.db.ItemTypeDTO;
 import com.sapienter.jbilling.server.item.event.ItemDeletedEvent;
 import com.sapienter.jbilling.server.item.event.ItemUpdatedEvent;
 import com.sapienter.jbilling.server.item.event.NewItemEvent;
 import com.sapienter.jbilling.server.item.tasks.IPricing;
 import com.sapienter.jbilling.server.item.tasks.PricingResult;
 import com.sapienter.jbilling.server.metafields.MetaFieldBL;
 import com.sapienter.jbilling.server.order.Usage;
 import com.sapienter.jbilling.server.order.db.OrderDTO;
 import com.sapienter.jbilling.server.order.db.OrderLineDAS;
 import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskManager;
 import com.sapienter.jbilling.server.pricing.PriceModelBL;
 import com.sapienter.jbilling.server.pricing.db.PriceModelDTO;
 import com.sapienter.jbilling.server.pricing.db.PriceModelStrategy;
 import com.sapienter.jbilling.server.system.event.EventManager;
 import com.sapienter.jbilling.server.user.EntityBL;
 import com.sapienter.jbilling.server.user.UserBL;
 import com.sapienter.jbilling.server.user.db.CompanyDAS;
 import com.sapienter.jbilling.server.user.db.CompanyDTO;
 import com.sapienter.jbilling.server.util.Constants;
 import com.sapienter.jbilling.server.util.audit.EventLogger;
 import org.apache.log4j.Logger;
 
 import java.math.BigDecimal;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 public class ItemBL {
 
     private static final Logger LOG = Logger.getLogger(ItemBL.class);
 
     private ItemDAS itemDas = null;
     private ItemDTO item = null;
     private EventLogger eLogger = null;
     private String priceCurrencySymbol = null;
     private List<PricingField> pricingFields = null;
 
     public ItemBL(Integer itemId)
             throws SessionInternalError {
         try {
             init();
             set(itemId);
         } catch (Exception e) {
             throw new SessionInternalError("Setting item", ItemBL.class, e);
         }
     }
 
     public ItemBL() {
         init();
     }
 
     public ItemBL(ItemDTO item) {
         this.item = item;
         init();
     }
 
     public void set(Integer itemId) {
         item = itemDas.find(itemId);
     }
 
     private void init() {
         eLogger = EventLogger.getInstance();
         itemDas = new ItemDAS();
     }
 
     public ItemDTO getEntity() {
         return item;
     }
 
     public Integer create(ItemDTO dto, Integer languageId) {
         EntityBL entity = new EntityBL(dto.getEntityId());
         if (languageId == null) {
             languageId = entity.getEntity().getLanguageId();
         }
 
         if (dto.getHasDecimals() != null) {
             dto.setHasDecimals(dto.getHasDecimals());
         } else {
             dto.setHasDecimals(0);
         }
 
         if (dto.getPercentage() == null) {
             // Backwards compatible with the old ItemDTOEx Web Service API, use the
             // transient price field as the rate for a default pricing model.
             if (dto.getPrice() != null) {
                 dto.addDefaultPrice(CommonConstants.EPOCH_DATE, getDefaultPrice(dto.getPrice()));
             }
 
             // default currency for new prices (if currency is not explicitly set)
             if (dto.getDefaultPrices() != null) {
                 for (PriceModelDTO price : dto.getDefaultPrices().values()) {
                     if (price.getCurrency() == null) {
                         price.setCurrency(entity.getEntity().getCurrency());
                     }
                 }
             }
 
             // validate all pricing attributes
             if (dto.getDefaultPrices() != null) {
                 PriceModelBL.validateAttributes(dto.getDefaultPrices().values());
             }
 
         } else {
             LOG.debug("Percentage items cannot have a default price model.");
             dto.getDefaultPrices().clear();
         }
 
         dto.setDeleted(0);
 
         dto.updateMetaFieldsWithValidation(dto);
 
         item = itemDas.save(dto);
 
         if (dto.getDescription() != null) {
             item.setDescription(dto.getDescription(), languageId);
         }
         updateTypes(dto);
         updateExcludedTypes(dto);
 
         // trigger internal event
         EventManager.process(new NewItemEvent(item));
 
         return item.getId();
     }
 
     public void update(Integer executorId, ItemDTO dto, Integer languageId)  {
         eLogger.audit(executorId, null, Constants.TABLE_ITEM, item.getId(),
                 EventLogger.MODULE_ITEM_MAINTENANCE,
                 EventLogger.ROW_UPDATED, null, null, null);
 
         item.setNumber(dto.getNumber());
         item.setGlCode(dto.getGlCode());
         if (dto.getDescription() != null) {
             item.setDescription(dto.getDescription(), languageId);
         }
         item.setPercentage(dto.getPercentage());
         item.setHasDecimals(dto.getHasDecimals());
 
         updateTypes(dto);
         updateExcludedTypes(dto);
 
         if (item.getPercentage() == null) {
             updateDefaultPrice(dto);
 
             // validate all pricing attributes
             if (item.getDefaultPrices() != null && !item.getDefaultPrices().isEmpty()) {
                 PriceModelBL.validateAttributes(item.getDefaultPrices().values());
             }
 
         } else {
             LOG.debug("Percentage items cannot have a default price model.");
             item.getDefaultPrices().clear();
         }
 
        item.updateMetaFieldsWithValidation(dto);

         itemDas.save(item);
 
         // trigger internal event
         EventManager.process(new ItemUpdatedEvent(item));
     }
 
     /**
      * Constructs a METERED PriceModelDTO with the given rate to be used as
      * the default price for items. This type of price model matches the old
      * "$ per unit" style pricing for basic items.
      *
      * @param rate rate per unit
      * @return price model
      */
     private PriceModelDTO getDefaultPrice(BigDecimal rate) {
         PriceModelDTO model = new PriceModelDTO();
         model.setRate(rate);
         model.setType(PriceModelStrategy.METERED);
 
         return model;
     }
 
     /**
      * Updates the price of this item to that of the given ItemDTO. This method
      * handles updates to the price using both the items default price model, and
      * the transient price attribute.
      *
      * If the given dto has a price through {@link ItemDTO#getPrice()}, then the
      * default price model rate will be set to the price. Otherwise the given dto's
      * price model is used to update.
      *
      * @param dto item holding the updates to apply to this item
      */
     private void updateDefaultPrice(ItemDTO dto) {
         if (item.getDefaultPrices() == null || item.getDefaultPrices().isEmpty()) {
             // new default price
             if (dto.getDefaultPrices() != null || !dto.getDefaultPrices().isEmpty()) {
                 item.getDefaultPrices().clear();
                 item.getDefaultPrices().putAll(dto.getDefaultPrices());
 
             } else if (dto.getPrice() != null) {
                 item.addDefaultPrice(CommonConstants.EPOCH_DATE, getDefaultPrice(dto.getPrice()));
             }
 
         } else {
             // update existing default price
             if (dto.getDefaultPrices() != null || !dto.getDefaultPrices().isEmpty()) {
                 item.getDefaultPrices().clear();
                 item.getDefaultPrices().putAll(dto.getDefaultPrices());
 
             } else if (dto.getPrice() != null) {
                 if (dto.getDefaultPrices().size() == 1) {
                     item.getDefaultPrices().get(0).setRate(dto.getPrice());
 
                 } else {
                     // cannot use legacy price column, there is more than 1 price that can be updated
                     // we should be updating the individual price model instead.
                     throw new SessionInternalError("Item uses multiple dated prices, cannot use WS price.");
                 }
             }
         }
 
         // default price currency should always be the entity currency
         if (item.getDefaultPrices() != null) {
             for (PriceModelDTO price : item.getDefaultPrices().values()) {
                 if (price.getCurrency() == null) {
                     price.setCurrency(item.getEntity().getCurrency());
                 }
             }
         }
     }
 
     private void updateTypes(ItemDTO dto)
             {
         // update the types relationship
         Collection types = item.getItemTypes();
         types.clear();
         ItemTypeBL typeBl = new ItemTypeBL();
         // TODO verify that all the categories belong to the same
         // order_line_type_id
         for (int f=0; f < dto.getTypes().length; f++) {
             typeBl.set(dto.getTypes()[f]);
             types.add(typeBl.getEntity());
         }
     }
 
     private void updateExcludedTypes(ItemDTO dto) {
         item.getExcludedTypes().clear();
 
         ItemTypeBL itemType = new ItemTypeBL();
         for (Integer typeId : dto.getExcludedTypeIds()) {
             itemType.set(typeId);
             item.getExcludedTypes().add(itemType.getEntity());
         }
     }
 
     public void delete(Integer executorId) {
         item.setDeleted(1);
 
         eLogger.audit(executorId, null, Constants.TABLE_ITEM, item.getId(),
                 EventLogger.MODULE_ITEM_MAINTENANCE,
                 EventLogger.ROW_DELETED, null, null, null);
 
         // trigger internal event
         EventManager.process(new ItemDeletedEvent(item));
 
         itemDas.flush();
         itemDas.clear();
     }
 
     public boolean validateDecimals( Integer hasDecimals ){
         if( hasDecimals == 0 ){
             if(new OrderLineDAS().findLinesWithDecimals(item.getId()) > 0) {
                 return false;
             }
         }
         return true;
     }
 
     /**
      * Returns the basic price for an item and currency, without including purchase quantity or
      * the users current usage in the pricing calculation.
      *
      * This method does not execute any pricing plug-ins and does not use quantity or usage
      * values for {@link PriceModelDTO#applyTo(com.sapienter.jbilling.server.order.db.OrderDTO, java.math.BigDecimal, com.sapienter.jbilling.server.item.tasks.PricingResult, java.util.List, com.sapienter.jbilling.server.order.Usage, boolean, java.util.Date)}
      * price calculations.
      *
      * @param date
      * @param item item to price
      * @param currencyId currency id of requested price
      * @return The price in the requested currency
      */
     public BigDecimal getPriceByCurrency(Date date, ItemDTO item, Integer userId, Integer currencyId)  {
         if (item.getDefaultPrices() != null && !item.getDefaultPrices().isEmpty()) {
             // empty usage for default pricing
             Usage usage = new Usage();
             usage.setAmount(BigDecimal.ZERO);
             usage.setQuantity(BigDecimal.ZERO);
 
             // calculate default price from strategy
             PricingResult result = new PricingResult(item.getId(), userId, currencyId);
             List<PricingField> fields = Collections.emptyList();
 
             // price for today
             PriceModelDTO priceModel = item.getPrice(new Date());
 
              if (priceModel != null) {
                  priceModel.applyTo(null, BigDecimal.ONE, result, fields, usage, false, date);
                  return result.getPrice();
              }
         }
         return BigDecimal.ZERO;
     }
 
 
     public BigDecimal getPrice(Integer userId, BigDecimal quantity, Integer entityId) throws SessionInternalError {
         UserBL user = new UserBL(userId);
         return getPrice(userId, user.getCurrencyId(), quantity, entityId, null, false);
     }
 
     public BigDecimal getPrice(Integer userId, Integer currencyId, BigDecimal quantity, Integer entityId) throws SessionInternalError {
         UserBL user = new UserBL(userId);
         return getPrice(userId, currencyId, quantity, entityId, null, false);
     }
 
     /**
      * Will find the right price considering the user's special prices and which
      * currencies had been entered in the prices table.
      *
      * @param userId user id
      * @param currencyId currency id
      * @param entityId entity id
      * @param order order being created or edited, maybe used for additional pricing calculations
      * @return The price in the requested currency. It always returns a price,
      * otherwise an exception for lack of pricing for an item
      */
     public BigDecimal getPrice(Integer userId, Integer currencyId, BigDecimal quantity, Integer entityId, OrderDTO order, boolean singlePurchase)
             throws SessionInternalError {
 
         if (currencyId == null || entityId == null) {
             throw new SessionInternalError("Can't get a price with null parameters. currencyId = " + currencyId +
                     " entityId = " + entityId);
         }
 
         CurrencyBL currencyBL;
         try {
             currencyBL = new CurrencyBL(currencyId);
             priceCurrencySymbol = currencyBL.getEntity().getSymbol();
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
 
         // default "simple" price
         BigDecimal price = getPriceByCurrency(order != null ? order.getPricingDate() : null, item, userId, currencyId);
 
         // run a plug-in with external logic (rules), if available
         try {
             PluggableTaskManager<IPricing> taskManager
                     = new PluggableTaskManager<IPricing>(entityId, Constants.PLUGGABLE_TASK_ITEM_PRICING);
             IPricing myTask = taskManager.getNextClass();
 
             while(myTask != null) {
                 price = myTask.getPrice(item.getId(), quantity, userId, currencyId, pricingFields, price, order, singlePurchase);
                 myTask = taskManager.getNextClass();
             }
         } catch (Exception e) {
             throw new SessionInternalError("Item pricing task error", ItemBL.class, e);
         }
 
         return price;
     }
 
     /**
      * Returns an ItemDTO constructed for the given language and entity, priced for the
      * given user and currency.
      *
      * @param languageId id of the users language
      * @param userId id of the user purchasing the item
      * @param entityId id of the entity
      * @param currencyId id of the currency
      * @return item dto
      * @throws SessionInternalError if an internal exception occurs processing request
      */
     public ItemDTO getDTO(Integer languageId, Integer userId, Integer entityId, Integer currencyId)
         throws SessionInternalError {
 
         return getDTO(languageId, userId, entityId, currencyId, BigDecimal.ONE, null);
     }
 
 
     /**
      * Returns an ItemDTO constructed for the given language and entity, priced for the
      * given user, currency and the amount being purchased.
      *
      * @param languageId id of the users language
      * @param userId id of the user purchasing the item
      * @param entityId id of the entity
      * @param currencyId id of the currency
      * @param quantity quantity being purchased
      * @return item dto
      * @throws SessionInternalError if an internal exception occurs processing request
      */
     public ItemDTO getDTO(Integer languageId, Integer userId, Integer entityId, Integer currencyId, BigDecimal quantity)
         throws SessionInternalError {
         return getDTO(languageId, userId, entityId, currencyId, quantity, null);
     }
 
 
     /**
      * Returns an ItemDTO constructed for the given language and entity, priced for the
      * given user, currency and the amount being purchased.
      *
      * If an order is given, then the order quantities will impact the price calculations
      * for item prices that include usage.
      *
      * @param languageId id of the users language
      * @param userId id of the user purchasing the item
      * @param entityId id of the entity
      * @param currencyId id of the currency
      * @param quantity quantity being purchased
      * @param order order that this item is to be added to. may be null if no order operation.
      * @return item dto
      * @throws SessionInternalError if an internal exception occurs processing request
      */
     public ItemDTO getDTO(Integer languageId, Integer userId, Integer entityId, Integer currencyId, BigDecimal quantity,
                           OrderDTO order) throws SessionInternalError {
 
         ItemDTO dto = new ItemDTO(
             item.getId(),
             item.getInternalNumber(),
             item.getGlCode(),
             item.getEntity(),
             item.getDescription(languageId),
             item.getDeleted(),
             currencyId,
             null,
             item.getPercentage(),
             null, // to be set right after
             item.getHasDecimals() );
 
         dto.setDefaultPrices(item.getDefaultPrices());
 
         // calculate a true price using the pricing plug-in, pricing takes into
         // account plans, special prices and the quantity of the item being purchased.
         if (currencyId != null && dto.getPercentage() == null) {
             dto.setPrice(getPrice(userId, currencyId, quantity, entityId, order, false));
         }
 
         // set the types
         Integer types[] = new Integer[item.getItemTypes().size()];
         int n = 0;
         for (ItemTypeDTO type : item.getItemTypes()) {
             types[n++] = type.getId();
             dto.setOrderLineTypeId(type.getOrderLineTypeId());
         }
         dto.setTypes(types);
 
         // set excluded types
         Integer excludedTypes[] = new Integer[item.getExcludedTypes().size()];
         int i = 0;
         for (ItemTypeDTO type : item.getExcludedTypes()) {
             excludedTypes[i++] = type.getId();
         }
         dto.setExcludedTypeIds(excludedTypes);
 
         dto.setMetaFields(item.getMetaFields());
 
         LOG.debug("Got item: " + dto.getId() + ", price: " + dto.getPrice());
 
         return dto;
     }
 
     public ItemDTO getDTO(ItemDTOEx other) {
         ItemDTO retValue = new ItemDTO();
 
         if (other.getId() != null) {
             retValue.setId(other.getId());
         }
 
         retValue.setEntity(new CompanyDAS().find(other.getEntityId()));
         retValue.setNumber(other.getNumber());
         retValue.setGlCode(other.getGlCode());
         retValue.setPercentage(other.getPercentageAsDecimal());
         retValue.setDeleted(other.getDeleted());
         retValue.setHasDecimals(other.getHasDecimals());
         retValue.setDescription(other.getDescription());
         retValue.setTypes(other.getTypes());
         retValue.setExcludedTypeIds(other.getExcludedTypes());
         retValue.setPromoCode(other.getPromoCode());
         retValue.setCurrencyId(other.getCurrencyId());
         retValue.setPrice(other.getPriceAsDecimal());
         retValue.setOrderLineTypeId(other.getOrderLineTypeId());
 
         MetaFieldBL.fillMetaFieldsFromWS(retValue, other.getMetaFields());
 
         // convert PriceModelWS to PriceModelDTO
         retValue.setDefaultPrices(PriceModelBL.getDTO(other.getDefaultPrices()));
 
         return retValue;
     }
 
     public ItemDTOEx getWS(ItemDTO other) {
         if (other == null) {
             other = item;
         }
 
         ItemDTOEx retValue = new ItemDTOEx();
         retValue.setId(other.getId());
 
         retValue.setEntityId(other.getEntity().getId());
         retValue.setNumber(other.getInternalNumber());
         retValue.setGlCode(other.getGlCode());
         retValue.setPercentage(other.getPercentage());
         retValue.setDeleted(other.getDeleted());
         retValue.setHasDecimals(other.getHasDecimals());
         retValue.setDescription(other.getDescription());
         retValue.setTypes(other.getTypes());
         retValue.setExcludedTypes(other.getExcludedTypeIds());
         retValue.setPromoCode(other.getPromoCode());
         retValue.setCurrencyId(other.getCurrencyId());
         retValue.setPrice(other.getPrice());
         retValue.setOrderLineTypeId(other.getOrderLineTypeId());
         retValue.setMetaFields(MetaFieldBL.convertMetaFieldsToWS(other));
 
         // convert PriceModelDTO to PriceModelWS
         retValue.setDefaultPrices(PriceModelBL.getWS(other.getDefaultPrices()));
 
         // today's price
         retValue.setDefaultPrice(PriceModelBL.getWsPriceForDate(retValue.getDefaultPrices(), new Date()));
 
         return retValue;
     }
 
     /**
      * @return
      */
     public String getPriceCurrencySymbol() {
         return priceCurrencySymbol;
     }
 
     /**
      * Returns all items for the given entity.
      * @param entityId
      * The id of the entity.
      * @return an array of all items
      */
     public ItemDTOEx[] getAllItems(Integer entityId) {
         EntityBL entityBL = new EntityBL(entityId);
         CompanyDTO entity = entityBL.getEntity();
         Collection itemEntities = entity.getItems();
         ItemDTOEx[] items = new ItemDTOEx[itemEntities.size()];
 
         // iterate through returned item entities, converting them into a DTO
         int index = 0;
         for (ItemDTO item: entity.getItems()) {
             set(item.getId());
             items[index++] = getWS(getDTO(entity.getLanguageId(), null, entityId, entity.getCurrencyId()));
         }
 
         return items;
     }
 
     /**
      * Returns all items for the given item type (category) id. If no results
      * are found an empty array is returned.
      *
      * @see ItemDAS#findAllByItemType(Integer)
      *
      * @param itemTypeId item type (category) id
      * @return array of found items, empty if none found
      */
     public ItemDTOEx[] getAllItemsByType(Integer itemTypeId) {
         List<ItemDTO> results = new ItemDAS().findAllByItemType(itemTypeId);
         ItemDTOEx[] items = new ItemDTOEx[results.size()];
 
         int index = 0;
         for (ItemDTO item : results)
             items[index++] = getWS(item);
 
         return items;
     }
 
     public void setPricingFields(List<PricingField> fields) {
         pricingFields = fields;
     }
 }
