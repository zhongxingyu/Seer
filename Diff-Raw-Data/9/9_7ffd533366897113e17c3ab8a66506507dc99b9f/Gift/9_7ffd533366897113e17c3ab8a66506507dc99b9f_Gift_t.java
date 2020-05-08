 package com.mpower.domain;
 
 import java.io.Serializable;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EntityListeners;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.Transient;
 
 import org.apache.commons.collections.FactoryUtils;
 import org.apache.commons.collections.list.LazyList;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.mpower.domain.annotation.AutoPopulate;
 import com.mpower.domain.listener.TemporalTimestampListener;
 import com.mpower.util.GiftCustomFieldMap;
 
 @Entity
 @EntityListeners(value = { TemporalTimestampListener.class })
 @Table(name = "GIFT")
 public class Gift implements Customizable, Viewable, Serializable {
 
     private static final long serialVersionUID = 1L;
 
     @SuppressWarnings("unused")
     @Transient
     private final Log logger = LogFactory.getLog(getClass());
 
     @Id
     @GeneratedValue
     @Column(name = "GIFT_ID")
     private Long id;
 
     @ManyToOne
     @JoinColumn(name = "PERSON_ID")
     private Person person;
 
     @ManyToOne(optional = true)
     @JoinColumn(name = "COMMITMENT_ID")
     private Commitment commitment;
 
     @Column(name = "COMMENTS")
     private String comments;
 
     @Column(name = "VALUE")
     private BigDecimal value;
 
     @Column(name = "PAYMENT_TYPE")
     private String paymentType;
 
     @Column(name = "CREDIT_CARD_TYPE")
     private String creditCardType;
 
     @Column(name = "CREDIT_CARD_NUMBER")
     private String creditCardNumber;
 
     @Column(name = "CREDIT_CARD_EXPIRATION")
     private Date creditCardExpiration;
 
     @Column(name = "CHECK_NUMBER")
     private Integer checkNumber;
 
     @Column(name = "ACH_TYPE")
     private String achType;
 
     @Column(name = "ACH_ROUTING_NUMBER")
     private String achRoutingNumber;
 
     @Column(name = "ACH_ACCOUNT_NUMBER")
     private String achAccountNumber;
 
     @Column(name = "TRANSACTION_DATE", updatable = false)
     @Temporal(TemporalType.TIMESTAMP)
     @AutoPopulate
     private Date transactionDate;
 
     @Column(name = "AUTH_CODE")
     private String authCode;
 
     @Column(name = "ORIGINAL_GIFT_ID")
     private Long originalGiftId;
 
     @Column(name = "REFUND_GIFT_ID")
     private Long refundGiftId;
 
     @Column(name = "REFUND_GIFT_TRANSACTION_DATE")
     @Temporal(TemporalType.TIMESTAMP)
     private Date refundGiftTransactionDate;
 
     @OneToMany(mappedBy = "gift", cascade = CascadeType.ALL)
     private List<GiftCustomField> giftCustomFields;
 
     @OneToMany(mappedBy = "gift", cascade = CascadeType.ALL)
     private List<DistributionLine> distributionLines;
 
     @Column(name = "DEDUCTIBLE")
     private boolean deductible = false;
 
     @Transient
     private Integer creditCardExpirationMonth;
 
     @Transient
     private Integer creditCardExpirationYear;
 
     @Transient
     private Map<String, CustomField> customFieldMap = null;
 
     @Transient
     private Set<String> requiredFields = null;
 
     @Transient
     private Map<String, String> validationMap = null;
 
     @Transient
     private Map<String, String> fieldLabelMap = null;
 
     @Transient
     private Map<String, Object> fieldValueMap = null;
 
     @Transient
     private List<PaymentSource> paymentSources = null;
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public Person getPerson() {
         return person;
     }
 
     public void setPerson(Person person) {
         this.person = person;
     }
 
     public Commitment getCommitment() {
         return commitment;
     }
 
     public void setCommitment(Commitment commitment) {
         this.commitment = commitment;
     }
 
     public String getComments() {
         return comments;
     }
 
     public void setComments(String comments) {
         this.comments = comments;
     }
 
     public BigDecimal getValue() {
         return value;
     }
 
     public void setValue(BigDecimal value) {
         this.value = value;
     }
 
     public String getPaymentType() {
         return paymentType;
     }
 
     public void setPaymentType(String paymentType) {
         this.paymentType = paymentType;
     }
 
     public String getCreditCardType() {
         return creditCardType;
     }
 
     public void setCreditCardType(String creditCardType) {
         this.creditCardType = creditCardType;
     }
 
     public String getCreditCardNumber() {
         return creditCardNumber;
     }
 
     public void setCreditCardNumber(String creditCardNumber) {
         this.creditCardNumber = creditCardNumber;
     }
 
     public Date getCreditCardExpiration() {
         return creditCardExpiration;
     }
 
     public void setCreditCardExpiration(Date creditCardExpiration) {
         this.creditCardExpiration = creditCardExpiration;
     }
 
     public Integer getCreditCardExpirationMonth() {
         if (creditCardExpiration != null) {
             Calendar calendar = Calendar.getInstance();
             calendar.setTime(creditCardExpiration);
             creditCardExpirationMonth = calendar.get(Calendar.MONTH) + 1;
         }
         return creditCardExpirationMonth;
     }
 
     public void setCreditCardExpirationMonth(Integer creditCardExpirationMonth) {
         setExpirationDate(creditCardExpirationMonth, null);
         this.creditCardExpirationMonth = creditCardExpirationMonth;
     }
 
     public Integer getCreditCardExpirationYear() {
         if (creditCardExpiration != null) {
             Calendar calendar = Calendar.getInstance();
             calendar.setTime(creditCardExpiration);
             creditCardExpirationYear = calendar.get(Calendar.YEAR);
         }
         return creditCardExpirationYear;
     }
 
     public void setCreditCardExpirationYear(Integer creditCardExpirationYear) {
         setExpirationDate(null, creditCardExpirationYear);
         this.creditCardExpirationYear = creditCardExpirationYear;
     }
 
     private void setExpirationDate(Integer month, Integer year) {
         Calendar calendar = Calendar.getInstance();
         if (creditCardExpiration != null) {
             calendar.setTime(creditCardExpiration);
         }
         if (month != null) {
             calendar.set(Calendar.MONTH, month - 1);
         }
         if (year != null) {
             calendar.set(Calendar.YEAR, year);
         }
        calendar.set(Calendar.DAY_OF_MONTH, 1);	// need to reset to 1 prior to getting max day
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR, calendar.getActualMaximum(Calendar.HOUR));
        calendar.set(Calendar.MINUTE, calendar.getActualMaximum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMaximum(Calendar.SECOND));
         creditCardExpiration = calendar.getTime();
     }
 
     public Date getTransactionDate() {
         return transactionDate;
     }
 
     public void setTransactionDate(Date transactionDate) {
         this.transactionDate = transactionDate;
     }
 
     public Integer getCheckNumber() {
         return checkNumber;
     }
 
     public void setCheckNumber(Integer checkNumber) {
         this.checkNumber = checkNumber;
     }
 
     public String getAchType() {
         return achType;
     }
 
     public void setAchType(String achType) {
         this.achType = achType;
     }
 
     public String getAchRoutingNumber() {
         return achRoutingNumber;
     }
 
     public void setAchRoutingNumber(String achRoutingNumber) {
         this.achRoutingNumber = achRoutingNumber;
     }
 
     public String getAchAccountNumber() {
         return achAccountNumber;
     }
 
     public void setAchAccountNumber(String achAccountNumber) {
         this.achAccountNumber = achAccountNumber;
     }
 
     public String getAuthCode() {
         return authCode;
     }
 
     public void setAuthCode(String authCode) {
         this.authCode = authCode;
     }
 
     public List<String> getExpirationMonthList() {
         List<String> monthList = new ArrayList<String>();
         for (int i = 1; i <= 12; i++) {
             String month = String.valueOf(i);
             if (month.length() == 1) {
                 month = "0" + month;
             }
             monthList.add(month);
         }
         return monthList;
     }
 
     public List<String> getExpirationYearList() {
         List<String> yearList = new ArrayList<String>();
         int year = Calendar.getInstance().get(Calendar.YEAR);
         for (int i = 0; i < 10; i++) {
             yearList.add(String.valueOf(year + i));
         }
         return yearList;
     }
 
     public Long getOriginalGiftId() {
         return originalGiftId;
     }
 
     public void setOriginalGiftId(Long originalGiftId) {
         this.originalGiftId = originalGiftId;
     }
 
     public Long getRefundGiftId() {
         return refundGiftId;
     }
 
     public void setRefundGiftId(Long refundGiftId) {
         this.refundGiftId = refundGiftId;
     }
 
     public Date getRefundGiftTransactionDate() {
         return refundGiftTransactionDate;
     }
 
     public void setRefundGiftTransactionDate(Date refundGiftTransactionDate) {
         this.refundGiftTransactionDate = refundGiftTransactionDate;
     }
 
     public List<GiftCustomField> getCustomFields() {
         if (giftCustomFields == null) {
             giftCustomFields = new ArrayList<GiftCustomField>();
         }
         return giftCustomFields;
     }
 
     @SuppressWarnings("unchecked")
     public Map<String, CustomField> getCustomFieldMap() {
         if (customFieldMap == null) {
             customFieldMap = GiftCustomFieldMap.buildCustomFieldMap(getCustomFields(), this);
         }
         return customFieldMap;
     }
 
     @SuppressWarnings("unchecked")
     public List<DistributionLine> getDistributionLines() {
         if (distributionLines == null) {
             distributionLines = LazyList.decorate(new ArrayList<DistributionLine>(), FactoryUtils.instantiateFactory(DistributionLine.class, new Class[] { Gift.class }, new Object[] { this }));
         }
         return distributionLines;
     }
 
     public void setDistributionLines(List<DistributionLine> distributionLines) {
         this.distributionLines = distributionLines;
     }
 
     public void addDistributionLine(DistributionLine distributionLine) {
         getDistributionLines().add(distributionLine);
     }
 
     @Override
     public Set<String> getRequiredFields() {
         return requiredFields;
     }
 
     @Override
     public void setRequiredFields(Set<String> requiredFields) {
         this.requiredFields = requiredFields;
     }
 
     @Override
     public Map<String, String> getValidationMap() {
         return validationMap;
     }
 
     @Override
     public void setValidationMap(Map<String, String> validationMap) {
         this.validationMap = validationMap;
     }
 
     @Override
     public Map<String, String> getFieldLabelMap() {
         return fieldLabelMap;
     }
 
     @Override
     public void setFieldLabelMap(Map<String, String> fieldLabelMap) {
         this.fieldLabelMap = fieldLabelMap;
     }
 
     @Override
     public Map<String, Object> getFieldValueMap() {
         return fieldValueMap;
     }
 
     @Override
     public void setFieldValueMap(Map<String, Object> fieldValueMap) {
         this.fieldValueMap = fieldValueMap;
     }
 
     @Override
     public Site getSite() {
         return person != null ? person.getSite() : null;
     }
 
     public boolean isDeductible() {
         return deductible;
     }
 
     public void setDeductible(boolean deductible) {
         this.deductible = deductible;
     }
 
     public List<PaymentSource> getPaymentSources() {
         return paymentSources;
     }
 
     public void setPaymentSources(List<PaymentSource> paymentSources) {
         this.paymentSources = paymentSources;
     }
 }
