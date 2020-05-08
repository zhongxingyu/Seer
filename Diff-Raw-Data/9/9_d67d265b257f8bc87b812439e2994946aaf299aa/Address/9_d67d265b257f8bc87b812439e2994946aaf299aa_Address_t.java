 package com.mpower.domain;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EntityListeners;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.PrePersist;
 import javax.persistence.PreUpdate;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.Transient;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.mpower.domain.annotation.AutoPopulate;
 import com.mpower.domain.listener.TemporalTimestampListener;
 import com.mpower.util.AddressCustomFieldMap;
 
 @Entity
 @EntityListeners(value = { TemporalTimestampListener.class })
 @Table(name = "ADDRESS")
 public class Address implements SiteAware, Customizable, ConstituentInfo, Inactivatible, Serializable {
 
     private static final long serialVersionUID = 1L;
 
     @SuppressWarnings("unused")
     @Transient
     private final Log logger = LogFactory.getLog(getClass());
 
     @Id
     @GeneratedValue
     @Column(name = "ADDRESS_ID")
     private Long id;
 
     @ManyToOne
     @JoinColumn(name = "PERSON_ID")
     private Person person;
 
     @Column(name = "ADDRESS_TYPE")
    private String addressType = "unknown";
 
     @Column(name = "ADDRESS_LINE_1")
     private String addressLine1;
 
     @Column(name = "ADDRESS_LINE_2")
     private String addressLine2;
 
     @Column(name = "ADDRESS_LINE_3")
     private String addressLine3;
 
     @Column(name = "CITY")
     private String city;
 
     @Column(name = "STATE_PROVINCE")
     private String stateProvince;
 
     @Column(name = "COUNTRY")
     private String country;
 
     @Column(name = "POSTAL_CODE")
     private String postalCode;
 
     @Column(name = "CREATE_DATE", updatable = false)
     @Temporal(TemporalType.TIMESTAMP)
     @AutoPopulate
     private Date createDate;
 
     @Column(name = "UPDATE_DATE", updatable = true)
     @Temporal(TemporalType.TIMESTAMP)
     @AutoPopulate
     private Date updateDate;
 
     @Column(name = "RECEIVE_CORRESPONDENCE")
     private boolean receiveMail = false;
 
     // either permanent, temporary, or seasonal
     @Column(name = "ACTIVATION_STATUS")
     private String activationStatus;
 
     // only care about month/day/year, not time
     @Column(name = "TEMPORARY_START_DATE")
     @Temporal(TemporalType.TIMESTAMP)
     private Date temporaryStartDate;
 
     // only care about month/day/year, not time
     @Column(name = "TEMPORARY_END_DATE")
     @Temporal(TemporalType.TIMESTAMP)
     private Date temporaryEndDate;
 
     // only care about month/day, not year or time
     @Column(name = "SEASONAL_START_DATE")
     @Temporal(TemporalType.TIMESTAMP)
     private Date seasonalStartDate;
 
     // only care about month/day, not year or time
     @Column(name = "SEASONAL_END_DATE")
     @Temporal(TemporalType.TIMESTAMP)
     private Date seasonalEndDate;
 
     @Column(name = "INACTIVE")
     private boolean inactive = false;
 
     @Column(name = "COMMENT")
     private String comments;
 
     @OneToMany(mappedBy = "address", cascade = CascadeType.ALL)
     private List<AddressCustomField> addressCustomFields;
 
     // only meaningful for Permanent addresses, and indicates when date becomes effective (ex. they are moving the first of next month)
     @Column(name = "EFFECTIVE_DATE")
     @Temporal(TemporalType.TIMESTAMP)
     private Date effectiveDate;
 
     @Transient
     private Map<String, CustomField> customFieldMap = null;
 
     @Transient
     private Map<String, String> fieldLabelMap = null;
 
     @Transient
     private Map<String, Object> fieldValueMap = null;
 
     @Transient
     private boolean userCreated = false;
 
     public Address() {
     }
 
     public Address(Person person) {
         this.person = person;
         this.addressType = "unknown";  // defaulting to 'home' would change the home address on the constituent whenever a new payment type is created with a new address.
         this.activationStatus = "permanent";
     }
 
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
 
     public String getAddressLine1() {
         return addressLine1;
     }
 
     public void setAddressLine1(String addressLine1) {
         this.addressLine1 = addressLine1;
     }
 
     public String getAddressLine2() {
         return addressLine2;
     }
 
     public void setAddressLine2(String addressLine2) {
         this.addressLine2 = addressLine2;
     }
 
     public String getAddressLine3() {
         return addressLine3;
     }
 
     public void setAddressLine3(String addressLine3) {
         this.addressLine3 = addressLine3;
     }
 
     public String getCity() {
         return city;
     }
 
     public void setCity(String city) {
         this.city = city;
     }
 
     public String getStateProvince() {
         return stateProvince;
     }
 
     public void setStateProvince(String stateProvince) {
         this.stateProvince = stateProvince;
     }
 
     public String getCountry() {
         return country;
     }
 
     public void setCountry(String country) {
         this.country = country;
     }
 
     public String getPostalCode() {
         return postalCode;
     }
 
     public void setPostalCode(String postalCode) {
         this.postalCode = postalCode;
     }
 
     public String getAddressType() {
         return addressType;
     }
 
     public void setAddressType(String addressType) {
         this.addressType = addressType;
     }
 
     public Date getCreateDate() {
         return createDate;
     }
 
     public void setCreateDate(Date createDate) {
         this.createDate = createDate;
     }
 
     public Date getUpdateDate() {
         return updateDate;
     }
 
     public void setUpdateDate(Date updateDate) {
         this.updateDate = updateDate;
     }
 
     public boolean isReceiveMail() {
         return receiveMail;
     }
 
     public void setReceiveMail(boolean receiveMail) {
         this.receiveMail = receiveMail;
     }
 
     public String getActivationStatus() {
         return activationStatus;
     }
 
     public void setActivationStatus(String activationStatus) {
         this.activationStatus = activationStatus;
     }
 
     public Date getTemporaryStartDate() {
         return temporaryStartDate;
     }
 
     public void setTemporaryStartDate(Date temporaryStartDate) {
         this.temporaryStartDate = temporaryStartDate;
     }
 
     public Date getTemporaryEndDate() {
         return temporaryEndDate;
     }
 
     public void setTemporaryEndDate(Date temporaryEndDate) {
         this.temporaryEndDate = temporaryEndDate;
     }
 
     public Date getSeasonalStartDate() {
         return seasonalStartDate;
     }
 
     public void setSeasonalStartDate(Date seasonalStartDate) {
         this.seasonalStartDate = seasonalStartDate;
     }
 
     public Date getSeasonalEndDate() {
         return seasonalEndDate;
     }
 
     public void setSeasonalEndDate(Date seasonalEndDate) {
         this.seasonalEndDate = seasonalEndDate;
     }
 
     public boolean isInactive() {
         return inactive;
     }
 
     public void setInactive(boolean inactive) {
         this.inactive = inactive;
     }
 
     public String getComments() {
         return comments;
     }
 
     public void setComments(String comments) {
         this.comments = comments;
     }
 
     public List<AddressCustomField> getAddressCustomFields() {
         if (addressCustomFields == null) {
             addressCustomFields = new ArrayList<AddressCustomField>();
         }
         return addressCustomFields;
     }
 
     @SuppressWarnings("unchecked")
     public Map<String, CustomField> getCustomFieldMap() {
         if (customFieldMap == null) {
             customFieldMap = AddressCustomFieldMap.buildCustomFieldMap(getAddressCustomFields(), this);
         }
         return customFieldMap;
     }
 
     public Date getEffectiveDate() {
         return effectiveDate;
     }
 
     public void setEffectiveDate(Date effectiveDate) {
         this.effectiveDate = effectiveDate;
     }
 
     public Site getSite() {
         return person.getSite();
     }
 
     public Map<String, String> getFieldLabelMap() {
         return fieldLabelMap;
     }
 
     public void setFieldLabelMap(Map<String, String> fieldLabelMap) {
         this.fieldLabelMap = fieldLabelMap;
     }
 
     public Map<String, Object> getFieldValueMap() {
         return fieldValueMap;
     }
 
     public void setFieldValueMap(Map<String, Object> fieldValueMap) {
         this.fieldValueMap = fieldValueMap;
     }
 
     public boolean isUserCreated() {
         return userCreated;
     }
 
     public void setUserCreated(boolean userCreated) {
         this.userCreated = userCreated;
     }
 
     public String getShortDisplay() {
         String shortDisplay = null;
         if (isValid()) {
             shortDisplay = StringUtils.substring(addressLine1, 0, 10) + " ... " + StringUtils.substring(postalCode, 0, 5);
         }
         return shortDisplay;
     }
 
     /**
      * Check if this is a dummy object; This is not a dummy object all required fields (addressLine1, city, stateProvince, postalCode, country) are populated
      * @return true if this Address has all required fields populated
      */
     public boolean isValid() {
         return (org.springframework.util.StringUtils.hasText(addressLine1) &&
                 org.springframework.util.StringUtils.hasText(city) &&
                 org.springframework.util.StringUtils.hasText(stateProvince) &&
                 org.springframework.util.StringUtils.hasText(postalCode) &&
                 org.springframework.util.StringUtils.hasText(country));
     }
 
     @PrePersist
     @PreUpdate
     public void normalize() {
         if (activationStatus != null) {
             if ("permanent".equals(getActivationStatus())) {
                 setSeasonalEndDate(null);
                 setSeasonalStartDate(null);
                 setTemporaryEndDate(null);
                 setTemporaryStartDate(null);
             } else if ("seasonal".equals(getActivationStatus())) {
                 setEffectiveDate(null);
                 setTemporaryEndDate(null);
                 setTemporaryStartDate(null);
             } else if ("temporary".equals(getActivationStatus())) {
                 setEffectiveDate(null);
                 setSeasonalEndDate(null);
                 setSeasonalStartDate(null);
             }
         }
     }
 
     @Override
     public boolean equals(Object obj) {
         if (!(obj instanceof Address)) {
             return false;
         }
         Address a = (Address) obj;
         EqualsBuilder eb = new EqualsBuilder();
         eb.append(person.getId(), a.getPerson().getId()).append(addressType, a.getAddressType()).append(activationStatus, a.getActivationStatus()).append(addressLine1, a.getAddressLine1()).append(addressLine2, a.getAddressLine2()).append(addressLine3, a.getAddressLine3()).append(city, a.getCity())
         .append(country, a.getCountry()).append(stateProvince, a.getStateProvince()).append(postalCode, a.getPostalCode());
         return eb.isEquals();
     }
 
     @Override
     public int hashCode() {
         HashCodeBuilder hcb = new HashCodeBuilder();
         hcb.append(person.getId()).append(addressType).append(activationStatus).append(addressLine1).append(addressLine2).append(addressLine3).append(city).append(country).append(stateProvince).append(postalCode);
         return hcb.hashCode();
     }
 }
