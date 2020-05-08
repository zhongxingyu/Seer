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
 
 /*
  * Created on Jan 25, 2005
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package com.sapienter.jbilling.server.user;
 
 import com.sapienter.jbilling.server.user.contact.db.ContactFieldDTO;
 import com.sapienter.jbilling.server.util.api.validation.EntitySignupValidationGroup;
import org.hibernate.validator.constraints.Email;
 import org.hibernate.validator.constraints.NotEmpty;
 
 import javax.validation.constraints.Size;
 import java.io.Serializable;
 import java.util.Date;
 import java.util.Iterator;
 
 /** @author Emil */
 public class ContactWS implements Serializable {
 
     private Integer id;
     @NotEmpty(message = "validation.error.notnull", groups = EntitySignupValidationGroup.class)
     @Size(min = 0, max = 200, message = "validation.error.size,0,200")
     private String organizationName;
     @NotEmpty(message = "validation.error.notnull", groups = EntitySignupValidationGroup.class)
     @Size(min = 0, max = 100, message = "validation.error.size,0,100")
     private String address1;
     @Size(min = 0, max = 100, message = "validation.error.size,0,100")
     private String address2;
     @Size(min = 0, max = 50, message = "validation.error.size,0,50")
     private String city;
     @NotEmpty(message = "validation.error.notnull", groups = EntitySignupValidationGroup.class)
     @Size(min = 0, max = 30, message = "validation.error.size,0,30")
     private String stateProvince;
     @NotEmpty(message = "validation.error.notnull", groups = EntitySignupValidationGroup.class)
     @Size(min = 0, max = 15, message = "validation.error.size,0,15")
     private String postalCode;
     @NotEmpty(message = "validation.error.notnull", groups = EntitySignupValidationGroup.class)
     private String countryCode;
     @NotEmpty(message = "validation.error.notnull", groups = EntitySignupValidationGroup.class)
     @Size(min = 0, max = 30, message = "validation.error.size,0,30")
     private String lastName;
     @NotEmpty(message = "validation.error.notnull", groups = EntitySignupValidationGroup.class)
     @Size(min = 0, max = 30, message = "validation.error.size,0,30")
     private String firstName;
     @Size(min = 0, max = 30, message = "validation.error.size,0,30")
     private String initial;
     private String title;
     private Integer phoneCountryCode;
     private Integer phoneAreaCode;
     @NotEmpty(message = "validation.error.notnull", groups = EntitySignupValidationGroup.class)
     private String phoneNumber;
     private Integer faxCountryCode;
     private Integer faxAreaCode;
     private String faxNumber;
     @NotEmpty(message = "validation.error.notnull")
    @Email(message = "validation.error.email")
     private String email;
     private Date createDate;
     private int deleted;
     private Boolean include;
 
     private Integer[] fieldIDs = null;
     private String[] fieldNames = null;
     private String[] fieldValues = null;
     private Integer type = null; // the contact type
 
     private Integer contactTypeId = null;
     private String contactTypeDescr = null;
 
     public ContactWS() {
         super();
     }
 
     public ContactWS(Integer id, String address1,
             String address2, String city, String stateProvince,
             String postalCode, String countryCode, int deleted) {
             this.id = id;
             this.address1 = address1;
             this.address2 = address2;
             this.city = city;
             this.stateProvince = stateProvince;
             this.postalCode = postalCode;
             this.countryCode = countryCode;
             this.deleted = deleted;
         }
    
     public ContactWS(Integer id, String organizationName, String address1,
                      String address2, String city, String stateProvince,
                      String postalCode, String countryCode, String lastName,
                      String firstName, String initial, String title,
                      Integer phoneCountryCode, Integer phoneAreaCode,
                      String phoneNumber, Integer faxCountryCode, Integer faxAreaCode,
                      String faxNumber, String email, Date createDate, Integer deleted,
                      Boolean include) {
         this.id = id;
         this.organizationName = organizationName;
         this.address1 = address1;
         this.address2 = address2;
         this.city = city;
         this.stateProvince = stateProvince;
         this.postalCode = postalCode;
         this.countryCode = countryCode;
         this.lastName = lastName;
         this.firstName = firstName;
         this.initial = initial;
         this.title = title;
         this.phoneCountryCode = phoneCountryCode;
         this.phoneAreaCode = phoneAreaCode;
         this.phoneNumber = phoneNumber;
         this.faxCountryCode = faxCountryCode;
         this.faxAreaCode = faxAreaCode;
         this.faxNumber = faxNumber;
         this.email = email;
         this.createDate = createDate;
         this.deleted = deleted;
         this.include = include;
     }
 
     public ContactWS(ContactWS other) {
         setId(other.getId());
         setOrganizationName(other.getOrganizationName());
         setAddress1(other.getAddress1());
         setAddress2(other.getAddress2());
         setCity(other.getCity());
         setStateProvince(other.getStateProvince());
         setPostalCode(other.getPostalCode());
         setCountryCode(other.getCountryCode());
         setLastName(other.getLastName());
         setFirstName(other.getFirstName());
         setInitial(other.getInitial());
         setTitle(other.getTitle());
         setPhoneCountryCode(other.getPhoneCountryCode());
         setPhoneAreaCode(other.getPhoneAreaCode());
         setPhoneNumber(other.getPhoneNumber());
         setFaxCountryCode(other.getFaxCountryCode());
         setFaxAreaCode(other.getFaxAreaCode());
         setFaxNumber(other.getFaxNumber());
         setEmail(other.getEmail());
         setCreateDate(other.getCreateDate());
         setDeleted(other.getDeleted());
         setInclude(other.getInclude());
     }
 
     public ContactWS(ContactDTOEx other) {
         setId(other.getId());
         setOrganizationName(other.getOrganizationName());
         setAddress1(other.getAddress1());
         setAddress2(other.getAddress2());
         setCity(other.getCity());
         setStateProvince(other.getStateProvince());
         setPostalCode(other.getPostalCode());
         setCountryCode(other.getCountryCode());
         setLastName(other.getLastName());
         setFirstName(other.getFirstName());
         setInitial(other.getInitial());
         setTitle(other.getTitle());
         setPhoneCountryCode(other.getPhoneCountryCode());
         setPhoneAreaCode(other.getPhoneAreaCode());
         setPhoneNumber(other.getPhoneNumber());
         setFaxCountryCode(other.getFaxCountryCode());
         setFaxAreaCode(other.getFaxAreaCode());
         setFaxNumber(other.getFaxNumber());
         setEmail(other.getEmail());
         setCreateDate(other.getCreateDate());
         setDeleted(other.getDeleted());
         setInclude(other.getInclude() != null && other.getInclude().equals(1) );
         setType(other.getType());
         fieldIDs = new Integer[other.getFieldsTable().size()];
         fieldNames = new String[other.getFieldsTable().size()];
         fieldValues = new String[other.getFieldsTable().size()];
         int index = 0;
 
         for (Iterator it = other.getFieldsTable().keySet().iterator(); it.hasNext();) {
             fieldIDs[index] = new Integer((String) it.next());
             ContactFieldDTO fieldDto = (ContactFieldDTO) other.getFieldsTable().get(fieldIDs[index].toString());
             fieldNames[index] = fieldDto.getType().getPromptKey();
             fieldValues[index] = fieldDto.getContent();
             index++;
         }
 
         //set Contact Type Name
         if (null != other.getContactMap() && null != other.getContactMap().getContactType()) {
             setContactTypeId(other.getContactMap().getContactType().getId());
         }
     }
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public String getOrganizationName() {
         return organizationName;
     }
 
     public void setOrganizationName(String organizationName) {
         this.organizationName = organizationName;
     }
 
     public String getAddress1() {
         return address1;
     }
 
     public void setAddress1(String address1) {
         this.address1 = address1;
     }
 
     public String getAddress2() {
         return address2;
     }
 
     public void setAddress2(String address2) {
         this.address2 = address2;
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
 
     public String getPostalCode() {
         return postalCode;
     }
 
     public void setPostalCode(String postalCode) {
         this.postalCode = postalCode;
     }
 
     public String getCountryCode() {
         return countryCode;
     }
 
     public void setCountryCode(String countryCode) {
         this.countryCode = countryCode;
     }
 
     public String getLastName() {
         return lastName;
     }
 
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 
     public String getFirstName() {
         return firstName;
     }
 
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
     public String getInitial() {
         return initial;
     }
 
     public void setInitial(String initial) {
         this.initial = initial;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public Integer getPhoneCountryCode() {
         return phoneCountryCode;
     }
 
     public void setPhoneCountryCode(Integer phoneCountryCode) {
         this.phoneCountryCode = phoneCountryCode;
     }
 
     public Integer getPhoneAreaCode() {
         return phoneAreaCode;
     }
 
     public void setPhoneAreaCode(Integer phoneAreaCode) {
         this.phoneAreaCode = phoneAreaCode;
     }
 
     public String getPhoneNumber() {
         return phoneNumber;
     }
 
     public void setPhoneNumber(String phoneNumber) {
         this.phoneNumber = phoneNumber;
     }
 
     public Integer getFaxCountryCode() {
         return faxCountryCode;
     }
 
     public void setFaxCountryCode(Integer faxCountryCode) {
         this.faxCountryCode = faxCountryCode;
     }
 
     public Integer getFaxAreaCode() {
         return faxAreaCode;
     }
 
     public void setFaxAreaCode(Integer faxAreaCode) {
         this.faxAreaCode = faxAreaCode;
     }
 
     public String getFaxNumber() {
         return faxNumber;
     }
 
     public void setFaxNumber(String faxNumber) {
         this.faxNumber = faxNumber;
     }
 
     public String getEmail() {
         return email;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     public Date getCreateDate() {
         return createDate;
     }
 
     public void setCreateDate(Date createDate) {
         this.createDate = createDate;
     }
 
     public int getDeleted() {
         return deleted;
     }
 
     public void setDeleted(int deleted) {
         this.deleted = deleted;
     }
 
     public Boolean getInclude() {
         return include == null ? new Boolean(false) : include;
     }
 
     public void setInclude(Boolean include) {
         this.include = include;
     }
 
     public Integer[] getFieldIDs() {
         return fieldIDs;
     }
 
     public void setFieldIDs(Integer[] fieldIDs) {
         this.fieldIDs = fieldIDs;
     }
 
     public String[] getFieldNames() {
         return fieldNames;
     }
 
     public void setFieldNames(String[] fieldNames) {
         this.fieldNames = fieldNames;
     }
 
     public String[] getFieldValues() {
         return fieldValues;
     }
 
     public void setFieldValues(String[] fieldValues) {
         this.fieldValues = fieldValues;
     }
 
     public Integer getType() {
         return type;
     }
 
     public void setType(Integer type) {
         this.type = type;
     }
 
     public Integer getContactTypeId() {
         return contactTypeId;
     }
 
     public void setContactTypeId(Integer contactTypeId) {
         this.contactTypeId = contactTypeId;
     }
 
     public String getContactTypeDescr() {
         return contactTypeDescr;
     }
 
     public void setContactTypeDescr(String contactTypeDescr) {
         this.contactTypeDescr = contactTypeDescr;
     }
 
     @Override
     public String toString() {
         return "ContactWS{"
                + "id=" + id
                + ", type=" + type
                + ", title='" + title + '\''
                + ", lastName='" + lastName + '\''
                + ", firstName='" + firstName + '\''
                + ", initial='" + initial + '\''
                + ", organization='" + organizationName + '\''
                + ", address1='" + address1 + '\''
                + ", address2='" + address2 + '\''
                + ", city='" + city + '\''
                + ", stateProvince='" + stateProvince + '\''
                + ", postalCode='" + postalCode + '\''
                + ", countryCode='" + countryCode + '\''
                + ", phone='" + (phoneCountryCode != null ? phoneCountryCode : "")
                              + (phoneAreaCode != null ? phoneAreaCode : "")
                              + (phoneNumber != null ?  phoneNumber : "") + '\''
                + ", fax='" + (faxCountryCode != null ? faxCountryCode : "")
                            + (faxAreaCode != null ? faxAreaCode : "")
                            + (faxNumber != null ? faxNumber : "") + '\''
                + ", email='" + email + '\''
                + ", type='" + type + '\''
                + ", include='" + include + '\''
                + '}';
     }
 }
