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
 
 package com.sapienter.jbilling.server.pricing.strategy;
 
 import com.sapienter.jbilling.common.SessionInternalError;
 import com.sapienter.jbilling.server.item.PricingField;
 import com.sapienter.jbilling.server.item.tasks.PricingResult;
 import com.sapienter.jbilling.server.pricing.cache.MatchType;
 import com.sapienter.jbilling.server.pricing.cache.RateCardFinder;
 import com.sapienter.jbilling.server.order.Usage;
 import com.sapienter.jbilling.server.order.db.OrderDTO;
 import com.sapienter.jbilling.server.pricing.RateCardBL;
 import com.sapienter.jbilling.server.pricing.db.AttributeDefinition;
 import com.sapienter.jbilling.server.pricing.db.ChainPosition;
 import com.sapienter.jbilling.server.pricing.db.PriceModelDTO;
 import com.sapienter.jbilling.server.pricing.util.AttributeUtils;
 import org.apache.log4j.Logger;
 import org.hibernate.ObjectNotFoundException;
 
 import java.math.BigDecimal;
 import java.util.List;
 
 import static com.sapienter.jbilling.server.pricing.db.AttributeDefinition.Type.*;
 
 /**
  * RateCardPricingStrategy
  *
  * @author Brian Cowdery
  * @since 19-02-2012
  */
 public class RateCardPricingStrategy extends AbstractPricingStrategy {
     private static final Logger LOG = Logger.getLogger(RateCardPricingStrategy.class);
 
     public RateCardPricingStrategy() {
         setAttributeDefinitions(
                 new AttributeDefinition("rate_card_id", INTEGER, true),
                 new AttributeDefinition("lookup_field", STRING, true),
                 new AttributeDefinition("match_type", STRING, true)
         );
 
         setChainPositions(
                 ChainPosition.START,
                 ChainPosition.MIDDLE,
                 ChainPosition.END
         );
 
         setRequiresUsage(false);
     }
 
 
     /**
      *
      * @param pricingOrder target order for this pricing request (not used by this strategy)
      * @param result pricing result to apply pricing to
      * @param fields pricing fields (not used by this strategy)
      * @param planPrice the plan price to apply (not used by this strategy)
      * @param quantity quantity of item being priced (not used by this strategy)
      * @param usage total item usage for this billing period
      * @param singlePurchase true if pricing a single purchase/addition to an order, false if pricing a quantity that already exists on the pricingOrder
      */
     public void applyTo(OrderDTO pricingOrder, PricingResult result, List<PricingField> fields, PriceModelDTO planPrice,
                         BigDecimal quantity, Usage usage, boolean singlePurchase) {
 
         // rate cards can exist in a chain, but we don't want to bother with another lookup
         // if a price was found earlier
         if (result.getPrice() != null && result.getPrice().compareTo(BigDecimal.ZERO) == 0) {
             LOG.debug("Price already found, skipping rate card lookup.");
             return;
         }
 
         // get and validate attributes
         Integer rateCardId = AttributeUtils.getInteger(planPrice.getAttributes(), "rate_card_id");
         MatchType matchType = MatchType.valueOf(planPrice.getAttributes().get("match_type"));
         PricingField lookupField = find(fields, "lookup_field");
 
        if (lookupField == null) {
            LOG.debug("Lookup field not found, not running in mediation or fields don't match configuration.");
            result.setPrice(BigDecimal.ZERO);
        }

         // fetch the finder bean from spring
         // and do the pricing lookup
        try {
            RateCardBL rateCard = new RateCardBL(rateCardId);
            RateCardFinder pricingFinder = rateCard.getBeanFactory().getFinderInstance();
 
            BigDecimal price = pricingFinder.findPrice(matchType, lookupField.getStrValue());
            result.setPrice(price);
 
        } catch (ObjectNotFoundException e) {
            throw new SessionInternalError("Rate card does not exist!", e,
                                           new String[] { "RateCardPricingStrategy,rate_card_id,rate.card.not.found" });
         }
     }
 }
