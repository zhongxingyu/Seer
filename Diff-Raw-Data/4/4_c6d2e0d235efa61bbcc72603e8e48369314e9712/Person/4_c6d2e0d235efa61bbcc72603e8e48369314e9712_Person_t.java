 package com.orangeleap.tangerine.domain;
 
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.orangeleap.tangerine.domain.communication.AbstractCommunicatorEntity;
 import com.orangeleap.tangerine.domain.paymentInfo.Commitment;
 import com.orangeleap.tangerine.domain.paymentInfo.Gift;
 import com.orangeleap.tangerine.util.StringConstants;
 
 public class Person extends AbstractCommunicatorEntity {
 
     private static final long serialVersionUID = 1L;
 
     public static final String INDIVIDUAL = "individual";
     public static final String ORGANIZATION = "organization";
     public static final String FORMAL_SALUTATION = "formalSalutation";
     public static final String INFORMAL_SALUTATION = "informalSalutation";
     public static final String HEAD_OF_HOUSEHOLD_SALUTATION = "headOfHouseholdSalutation";
     public static final String ORGANIZATION_ELIGIBILITY = "organization.eligibility";
 
     private Site site;
     private String constituentType = INDIVIDUAL;
     private String title;
     private String firstName;
     private String middleName;
     private String lastName;
     private String suffix;
     private String recognitionName;
     private String organizationName;
     private String legalName;
     private String ncaisCode;
     private String maritalStatus = "Unknown";
     private String preferredPhoneType;
     private boolean majorDonor = false;
     private boolean lapsedDonor = false;
     private String constituentIndividualRoles = StringConstants.EMPTY;
     private String constituentOrganizationRoles = StringConstants.EMPTY;
     private String loginId;
     private Date createDate;
     private Date updateDate;
     
     private List<Gift> gifts;
     private List<Commitment> commitments;
 
     public Person() { }
 
     public Person(Long id, Site site) { 
         this();
         this.id = id;
         this.site = site;
     }
     
     @Override
     public String toString() {
         return getDisplayValue();
     }
 
     @Override
     // TODO: remove this overridden method when this class is renamed to "Constituent"
     public String getType() {
         return "person";
     }
 
     public String getDisplayValue() {
         if (isOrganization()) {
             return organizationName;
         } else {
             return createName(true);
         }
     }
 
     public String getFirstLast() {
         StringBuilder sb = new StringBuilder();
         if (isOrganization()) {
             sb.append(organizationName);
         } 
         else {
             if (firstName != null) {
                 sb.append(firstName).append(" ");
             }
             if (lastName != null) {
                 sb.append(lastName);
             }
         }
         return sb.toString();
     }
 
     public String getFullName() {
         if (isOrganization()) {
             return organizationName;
         } 
         else {
             return createName(false);
         }
     }
 
     public String createName(boolean lastFirst) {
         StringBuilder sb = new StringBuilder();
         if (lastFirst) {
             sb.append(lastName == null ? "" : lastName).append(", ");
         }
         sb.append(firstName == null ? "" : firstName);
         if (middleName != null && middleName.length() > 0) {
             sb.append(" ").append(middleName);
         }
         if (!lastFirst) {
             sb.append(" ").append(lastName == null ? "" : lastName);
         }
         if (suffix != null && suffix.length() > 0) {
             sb.append(", ").append(suffix);
         }
         return sb.toString();
     }
 
     public String getEntityName() {
         return "person";
     }
 
     public Long getAccountNumber() {
         return id;
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
 
     public String getOrganizationName() {
         return organizationName;
     }
 
     public void setOrganizationName(String organizationName) {
         this.organizationName = organizationName;
     }
 
     public List<Gift> getGifts() {
         return gifts;
     }
 
     public void setGifts(List<Gift> gifts) {
         this.gifts = gifts;
     }
 
     public List<Commitment> getCommitments() {
         return commitments;
     }
 
     public void setCommitments(List<Commitment> commitments) {
         this.commitments = commitments;
     }
 
     public boolean isMajorDonor() {
         return majorDonor;
     }
 
     public void setMajorDonor(boolean majorDonor) {
         this.majorDonor = majorDonor;
     }
 
     public boolean isLapsedDonor() {
         return lapsedDonor;
     }
 
     public void setLapsedDonor(boolean lapsedDonor) {
         this.lapsedDonor = lapsedDonor;
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
     
     public void setNcaisCode(String ncaisCode) {
         this.ncaisCode = ncaisCode;
     }
 
     public String getNcaisCode() {
         return ncaisCode;
     }
 
     public void setRecognitionName(String recognitionName) {
         this.recognitionName = recognitionName;
     }
 
     public String getRecognitionName() {
         return recognitionName;
     }
 
     public void setConstituentType(String constituentType) {
         this.constituentType = constituentType;
     }
 
     public String getConstituentType() {
         if (constituentType == null) {
             return INDIVIDUAL;
         }
         return constituentType;
     }
 
     public void setConstituentIndividualRoles(String constituentIndividualRoles) {
         this.constituentIndividualRoles = constituentIndividualRoles;
     }
 
     public String getConstituentIndividualRoles() {
         return constituentIndividualRoles;
     }
 
     public void setConstituentOrganizationRoles(String constituentOrganizationRoles) {
         this.constituentOrganizationRoles = constituentOrganizationRoles;
     }
 
     public String getConstituentOrganizationRoles() {
         return constituentOrganizationRoles;
     }
 
     public void addConstituentRole(String constituentRole) {
         if (constituentRole == null || constituentRole.equals(ORGANIZATION) || constituentRole.equals(INDIVIDUAL)) {
             return;
         }
         if (isOrganization()) {
             setConstituentOrganizationRoles(addToList(getConstituentOrganizationRoles(), constituentRole));
         } 
         else {
             setConstituentIndividualRoles(addToList(getConstituentIndividualRoles(), constituentRole));
         }
     }
 
     private String addToList(String list, String s) {
         if (list == null) {
             list = "";
         }
         if (!list.contains(s)) {
             if (list.length() > 0) {
                 list += ",";
             }
             list += s;
         }
         return list;
     }
 
     public void setConstituentAttributes(String constituentAttributes) {
         // noop - read only field for Spring MVC
     }
 
     public String getConstituentAttributes() {
         String constituentAttributes;
         if (isOrganization()) {
             constituentAttributes = ORGANIZATION;
             if (constituentOrganizationRoles != null && constituentOrganizationRoles.length() > 0) {
                 constituentAttributes = constituentAttributes + "," + constituentOrganizationRoles;
             }
         } 
         else {
             constituentAttributes = INDIVIDUAL;
             if (constituentIndividualRoles != null && constituentIndividualRoles.length() > 0) {
                 constituentAttributes = constituentAttributes + "," + constituentIndividualRoles;
             }
         }
         return constituentAttributes;
     }
 
     public void setLegalName(String legalName) {
         this.legalName = legalName;
     }
 
     public String getLegalName() {
         return legalName;
     }
 
     public void setLoginId(String loginId) {
         this.loginId = loginId;
     }
 
     public String getLoginId() {
         return loginId;
     }
 
     public boolean isOrganization() {
         return ORGANIZATION.equals(getConstituentType());
     }
 
     public boolean isIndividual() {
         return INDIVIDUAL.equals(getConstituentType());
     }
 
     @Override
     public void setDefaults() {
         super.setDefaults();
        if (isOrganization()) {
            setDefaultCustomFieldValue(ORGANIZATION_ELIGIBILITY, StringConstants.UNKNOWN_LOWER_CASE);
        }
     }
 
     @Override
     public void prePersist() {
         super.prePersist();
         if (isOrganization() && StringUtils.isBlank(getLegalName())) {
             setLegalName(getOrganizationName());
         }
         if (isIndividual() && StringUtils.isBlank(getRecognitionName())) {
             setRecognitionName(createName(false));
         }
         if (StringUtils.isBlank(getCustomFieldValue(FORMAL_SALUTATION))) {
             if (isOrganization()) {
                 setCustomFieldValue(FORMAL_SALUTATION, legalName);
             }
             else if (isIndividual()) {
                 StringBuilder sb = new StringBuilder();
                 if (StringUtils.isBlank(title) == false) {
                     sb.append(title).append(" ");
                 }
                 sb.append(getFirstLast());
                 setCustomFieldValue(FORMAL_SALUTATION, sb.toString());
             }
         }
         if (StringUtils.isBlank(getCustomFieldValue(INFORMAL_SALUTATION))) {
             if (isOrganization()) {
                 setCustomFieldValue(INFORMAL_SALUTATION, organizationName);
             }
             else if (isIndividual()) {
                 setCustomFieldValue(INFORMAL_SALUTATION, getFirstLast());
             }
         }
         if (isIndividual() && StringUtils.isBlank(getCustomFieldValue(HEAD_OF_HOUSEHOLD_SALUTATION))) {
             setCustomFieldValue(HEAD_OF_HOUSEHOLD_SALUTATION, getFirstLast());
         }
         setConstituentType(StringUtils.trimToEmpty(getConstituentType()).toLowerCase());
         setConstituentIndividualRoles(StringUtils.trimToEmpty(getConstituentIndividualRoles()).toLowerCase());
         setConstituentOrganizationRoles(StringUtils.trimToEmpty(getConstituentOrganizationRoles()).toLowerCase());
     }
 }
