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
 
 package com.sapienter.jbilling.server.pricing.db;
 
 import com.sapienter.jbilling.server.item.CurrencyBL;
 import com.sapienter.jbilling.server.item.PricingField;
 import com.sapienter.jbilling.server.item.tasks.PricingResult;
 import com.sapienter.jbilling.server.order.Usage;
 import com.sapienter.jbilling.server.order.db.OrderDTO;
 import com.sapienter.jbilling.server.pricing.PriceModelWS;
 import com.sapienter.jbilling.server.pricing.strategy.PricingStrategy;
 import com.sapienter.jbilling.server.user.UserBL;
 import com.sapienter.jbilling.server.util.db.CurrencyDTO;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.hibernate.annotations.CollectionOfElements;
 import org.hibernate.annotations.Fetch;
 import org.hibernate.annotations.FetchMode;
 import org.hibernate.annotations.MapKey;
 import org.hibernate.annotations.Sort;
 import org.hibernate.annotations.SortType;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
 import javax.persistence.TableGenerator;
 import javax.persistence.Transient;
 import java.io.Serializable;
 import java.math.BigDecimal;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 /**
  * @author Brian Cowdery
  * @since 30-07-2010
  */
 @Entity
 @Table(name = "price_model")
 @TableGenerator(
         name = "price_model_GEN",
         table = "jbilling_seqs",
         pkColumnName = "name",
         valueColumnName = "next_id",
         pkColumnValue = "price_model",
         allocationSize = 100
 )
 @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
 public class PriceModelDTO implements Serializable {
 
     public static final String ATTRIBUTE_WILDCARD = "*";
 
     private Integer id;
     private PriceModelStrategy type;
     private SortedMap<String, String> attributes = new TreeMap<String, String>();
     private BigDecimal rate;
     private CurrencyDTO currency;
 
     // price model chaining
     private PriceModelDTO next;
 
     public PriceModelDTO() {
     }
 
     public PriceModelDTO(PriceModelStrategy type, BigDecimal rate, CurrencyDTO currency) {
         this.type = type;
         this.rate = rate;
         this.currency = currency;
     }
 
     public PriceModelDTO(PriceModelWS ws, CurrencyDTO currency) {
         setId(ws.getId());
         setType(PriceModelStrategy.valueOf(ws.getType()));
         setAttributes(new TreeMap<String, String>(ws.getAttributes()));
         setRate(ws.getRateAsDecimal());
         setCurrency(currency);
     }
 
     /**
      * Copy constructor.
      *
      * @param model model to copy
      */
     public PriceModelDTO(PriceModelDTO model) {
         this.id = model.getId();
         this.type = model.getType();
         this.attributes = new TreeMap<String, String>(model.getAttributes());
         this.rate = model.getRate();
         this.currency = model.getCurrency();
 
         if (model.getNext() != null) {
             this.next = new PriceModelDTO(model.getNext());
         }
     }
 
 
     @Id
     @GeneratedValue(strategy = GenerationType.TABLE, generator = "price_model_GEN")
     @Column(name = "id", unique = true, nullable = false)
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     @Enumerated(EnumType.STRING)
     @Column(name = "strategy_type", nullable = false, length = 25)
     public PriceModelStrategy getType() {
         return type;
     }
 
     public void setType(PriceModelStrategy type) {
         this.type = type;
     }
 
     @Transient
     public PricingStrategy getStrategy() {
         return getType() != null ? getType().getStrategy() : null;
     }
 
     @CollectionOfElements(fetch = FetchType.EAGER)
     @JoinTable(name = "price_model_attribute", joinColumns = @JoinColumn(name = "price_model_id"))
     @MapKey(columns = @Column(name = "attribute_name", nullable = true, length = 255))
     @Column(name = "attribute_value", nullable = true, length = 255)
     @Sort(type = SortType.NATURAL)
     @Fetch(FetchMode.SELECT)
     public SortedMap<String, String> getAttributes() {
         return attributes;
     }
 
     public void setAttributes(SortedMap<String, String> attributes) {
         this.attributes = attributes;
         setAttributeWildcards();
     }
 
     /**
      * Sets the given attribute. If the attribute is null, it will be persisted as a wildcard "*".
      *
      * @param name attribute name
      * @param value attribute value
      */
     public void addAttribute(String name, String value) {
         this.attributes.put(name, (value != null ? value : ATTRIBUTE_WILDCARD));
     }
 
     /**
      * Replaces null values in the attribute list with a wildcard character. Null values cannot be
      * persisted using the @CollectionOfElements, and make for uglier 'optional' attribute queries.
      */
     public void setAttributeWildcards() {
         if (getAttributes() != null && !getAttributes().isEmpty()) {
             for (Map.Entry<String, String> entry : getAttributes().entrySet())
                 if (entry.getValue() == null)
                     entry.setValue(ATTRIBUTE_WILDCARD);
         }
     }
 
     /**
      * Returns the pricing rate. If the strategy type defines an overriding rate, the
      * strategy rate will be returned.
      *
      * @return pricing rate.
      */
     @Column(name = "rate", nullable = true, precision = 10, scale = 22)
     public BigDecimal getRate() {
         return rate;
     }
 
     public void setRate(BigDecimal rate) {
         this.rate = rate;
     }
 
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "currency_id", nullable = true)
     public CurrencyDTO getCurrency() {
         return currency;
     }
 
     public void setCurrency(CurrencyDTO currency) {
         this.currency = currency;
     }
 
     @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
     @JoinColumn(name = "next_model_id", nullable = true)
     public PriceModelDTO getNext() {
         return next;
     }
 
     public void setNext(PriceModelDTO next) {
         this.next = next;
     }
 
     /**
      * Applies this pricing to the given PricingResult.
      *
      * This method will automatically convert the calculated price to the currency of the given
      * PricingResult if the set currencies differ.
      *
      * @see com.sapienter.jbilling.server.pricing.strategy.PricingStrategy
      *@param pricingOrder target order for this pricing request (may be null)
      * @param quantity quantity of item being priced
      * @param result pricing result to apply pricing to
      * @param usage total item usage for this billing period
      * @param singlePurchase true if pricing a single purchase/addition to an order, false if pricing a quantity that already exists on the pricingOrder.
      * @param pricingDate pricing date 
      */
     @Transient
     public void applyTo(OrderDTO pricingOrder, BigDecimal quantity, PricingResult result, List<PricingField> fields,
                         Usage usage, boolean singlePurchase, Date pricingDate) {
         // each model in the chain
         for (PriceModelDTO next = this; next != null; next = next.getNext()) {
             // apply pricing
             next.getType().getStrategy().applyTo(pricingOrder, result, fields, next, quantity, usage, singlePurchase);
 
             // convert currency if necessary
             if (result.getUserId() != null
                 && result.getCurrencyId() != null
                 && result.getPrice() != null
                 && next.getCurrency() != null
                 && next.getCurrency().getId() != result.getCurrencyId()) {
 
                 Integer entityId = new UserBL().getEntityId(result.getUserId());
                 if(pricingDate == null) {
                     pricingDate = new Date();
                 }
 
                 final BigDecimal converted = new CurrencyBL().convert(next.getCurrency().getId(), result.getCurrencyId(),
                         result.getPrice(), pricingDate, entityId);
                 result.setPrice(converted);
             }
         }
        
        if (result.getPrice() == null) {
        	result.setPrice(BigDecimal.ZERO);
        }
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         PriceModelDTO that = (PriceModelDTO) o;
 
         if (attributes != null ? !attributes.equals(that.attributes) : that.attributes != null) return false;
         if (currency != null ? !currency.equals(that.currency) : that.currency != null) return false;
         if (id != null ? !id.equals(that.id) : that.id != null) return false;
         if (rate != null ? !rate.equals(that.rate) : that.rate != null) return false;
         if (type != that.type) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = id != null ? id.hashCode() : 0;
         result = 31 * result + (type != null ? type.hashCode() : 0);
         result = 31 * result + (attributes != null ? attributes.hashCode() : 0);
         result = 31 * result + (rate != null ? rate.hashCode() : 0);
         result = 31 * result + (currency != null ? currency.hashCode() : 0);
         return result;
     }
 
     @Override
     public String toString() {
         return "PriceModelDTO{"
                + "id=" + id
                + ", type=" + type
                + ", attributes=" + attributes
                + ", rate=" + rate
                + ", currencyId=" + (currency != null ? currency.getId() : null)
                + ", next=" + next
                + '}';
     }
 }
