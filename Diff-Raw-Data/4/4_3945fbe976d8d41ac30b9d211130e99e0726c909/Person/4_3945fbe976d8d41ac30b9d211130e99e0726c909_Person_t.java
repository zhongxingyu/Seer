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
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.Transient;
 
 import org.apache.commons.lang.builder.ToStringBuilder;
 
 import com.mpower.domain.listener.EmptyStringNullifyerListener;
 import com.mpower.util.AddressMap;
 import com.mpower.util.CustomFieldMap;
 import com.mpower.util.PhoneMap;
 
 @Entity
 @EntityListeners(value = { EmptyStringNullifyerListener.class })
 @Table(name = "PERSON")
 public class Person implements Customizable, Serializable {
 
     private static final long serialVersionUID = 1L;
 
     @Id
     @GeneratedValue
     @Column(name = "PERSON_ID")
     private Long id;
 
     @ManyToOne
     @JoinColumn(name = "SITE_ID")
     private Site site;
 
     @Column(name = "TITLE")
     private String title;
 
     @Column(name = "SPOUSE_NAME")
     private String spouseName;
 
     @Column(name = "FIRST_NAME")
     private String firstName;
 
     @Column(name = "MIDDLE_NAME")
     private String middleName;
 
     @Column(name = "LAST_NAME")
     private String lastName;
 
     @Column(name = "EMAIL")
     private String email;
 
     @Column(name = "SUFFIX")
     private String suffix;
 
     @Column(name = "MARITAL_STATUS")
     private String maritalStatus = "Unknown";
 
     @Column(name = "SPOUSE_FIRST_NAME")
     private String spouseFirstName;
 
     @Column(name = "ORGANIZATION_NAME")
     private String organizationName;
 
     @Column(name = "BIRTHDATE")
     @Temporal(TemporalType.DATE)
     private Date birthDate;
 
     @Column(name = "ANNIVERSARY")
     @Temporal(TemporalType.DATE)
     private Date anniversary;
 
     @Column(name = "PREFERRED_PHONE_TYPE")
     private String preferredPhoneType;
 
     @OneToMany(mappedBy = "person", cascade = CascadeType.ALL)
     private List<PersonAddress> personAddresses;
 
     @OneToMany(mappedBy = "person", cascade = CascadeType.ALL)
     private List<PersonPhone> personPhones;
 
     @OneToMany(mappedBy = "person", cascade = CascadeType.ALL)
     private List<PersonCustomField> personCustomFields;
 
     @OneToMany(mappedBy = "person", cascade = CascadeType.ALL)
     private List<PaymentSource> paymentSources;
 
     @OneToMany(mappedBy = "person", cascade = CascadeType.ALL)
     private List<Gift> gifts;
 
     @Column(name = "MAJOR_DONOR")
     private boolean majorDonor = false;
 
     @Transient
     private Map<String, Address> addressMap = null;
 
     @Transient
     private Map<String, Phone> phoneMap = null;
 
     @Transient
     private Map<String, CustomField> customFieldMap = null;
 
     public Long getId() {
         return id;
     }
    
    public Long getAccountNumber() {
        return id;
    }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public Site getSite() {
         return site;
     }
 
     public void setSite(Site site) {
         this.site = site;
     }
 
     public String getFirstName() {
         return firstName;
     }
 
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
     public String getMiddleName() {
         return middleName;
     }
 
     public void setMiddleName(String middleName) {
         this.middleName = middleName;
     }
 
     public String getLastName() {
         return lastName;
     }
 
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 
     public String getEmail() {
         return email;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     public Date getBirthDate() {
         return birthDate;
     }
 
     public void setBirthDate(Date birthDate) {
         this.birthDate = birthDate;
     }
 
     public Date getAnniversary() {
         return anniversary;
     }
 
     public void setAnniversary(Date anniversary) {
         this.anniversary = anniversary;
     }
 
     public String getPreferredPhoneType() {
         return preferredPhoneType;
     }
 
     public void setPreferredPhoneType(String preferredPhoneType) {
         this.preferredPhoneType = preferredPhoneType;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public String getSpouseName() {
         return spouseName;
     }
 
     public void setSpouseName(String spouseName) {
         this.spouseName = spouseName;
     }
 
     public List<PersonAddress> getPersonAddresses() {
         if (personAddresses == null) {
             personAddresses = new ArrayList<PersonAddress>();
         }
         return personAddresses;
     }
 
     @SuppressWarnings("unchecked")
     public Map<String, Address> getAddressMap() {
         if (addressMap == null) {
             addressMap = AddressMap.buildAddressMap(getPersonAddresses(), this);
         }
         return addressMap;
     }
 
     public List<PersonPhone> getPersonPhones() {
         if (personPhones == null) {
             personPhones = new ArrayList<PersonPhone>();
         }
         return personPhones;
     }
 
     @SuppressWarnings("unchecked")
     public Map<String, Phone> getPhoneMap() {
         if (phoneMap == null) {
             phoneMap = PhoneMap.buildPhoneMap(getPersonPhones(), this);
         }
         return phoneMap;
     }
 
     public List<PersonCustomField> getPersonCustomFields() {
         if (personCustomFields == null) {
             personCustomFields = new ArrayList<PersonCustomField>();
         }
         return personCustomFields;
     }
 
     @SuppressWarnings("unchecked")
     public Map<String, CustomField> getCustomFieldMap() {
         if (customFieldMap == null) {
             customFieldMap = CustomFieldMap.buildCustomFieldMap(getPersonCustomFields(), this);
         }
         return customFieldMap;
     }
 
     public String getSuffix() {
         return suffix;
     }
 
     public void setSuffix(String suffix) {
         this.suffix = suffix;
     }
 
     public String getMaritalStatus() {
         return maritalStatus;
     }
 
     public void setMaritalStatus(String maritalStatus) {
         this.maritalStatus = maritalStatus;
     }
 
     public String getSpouseFirstName() {
         return spouseFirstName;
     }
 
     public void setSpouseFirstName(String spouseFirstName) {
         this.spouseFirstName = spouseFirstName;
     }
 
     public String getOrganizationName() {
         return organizationName;
     }
 
     public void setOrganizationName(String organizationName) {
         this.organizationName = organizationName;
     }
 
     public List<PaymentSource> getPaymentSources() {
         return paymentSources;
     }
 
     public void setPaymentSources(List<PaymentSource> paymentSources) {
         this.paymentSources = paymentSources;
     }
 
     public List<Gift> getGifts() {
         return gifts;
     }
 
     public void setGifts(List<Gift> gifts) {
         this.gifts = gifts;
     }
 
     public boolean isMajorDonor() {
         return majorDonor;
     }
 
     public void setMajorDonor(boolean majorDonor) {
         this.majorDonor = majorDonor;
     }
 
     @Override
     public String toString() {
         return ToStringBuilder.reflectionToString(this);
     }
 }
