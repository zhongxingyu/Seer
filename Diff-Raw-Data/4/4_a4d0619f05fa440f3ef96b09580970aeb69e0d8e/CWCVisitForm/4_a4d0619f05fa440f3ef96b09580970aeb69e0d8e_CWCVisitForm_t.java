 package org.motechproject.ghana.national.bean;
 
 import org.apache.commons.lang.StringUtils;
 import org.motechproject.ghana.national.validator.field.MotechId;
 import org.motechproject.mobileforms.api.domain.FormBean;
 import org.motechproject.mobileforms.api.validator.annotations.MaxLength;
 import org.motechproject.mobileforms.api.validator.annotations.RegEx;
 import org.motechproject.mobileforms.api.validator.annotations.Required;
 import org.motechproject.openmrs.omod.validator.MotechIdVerhoeffValidator;
 
 import java.util.*;
 
 import static org.motechproject.ghana.national.FormFieldRegExPatterns.MOTECH_ID_PATTERN;
 
 public class CWCVisitForm extends FormBean {
 
 
     public static final String NUMERIC_OR_NOTAPPLICABLE_PATTERN = "([0-9]+(.[0-9]+)?|[nN][aA])";
 
     @Required
     @MaxLength(size = 50)
     @RegEx(pattern = NUMERIC_OR_NOTAPPLICABLE_PATTERN)
     private String staffId;
 
     @Required
     @MaxLength(size = 50)
     @RegEx(pattern = NUMERIC_OR_NOTAPPLICABLE_PATTERN)
     private String facilityId;
 
     private Date date;
 
     @Required
     @RegEx(pattern = MOTECH_ID_PATTERN)
     @MotechId(validator = MotechIdVerhoeffValidator.class)
     private String motechId;
 
     private String serialNumber;
 
     @Required
     private String immunizations;
     private String opvdose;
     private String pentadose;
     private String iptidose;
     @Required
     private Double weight;
     @Required
     private Double muac;
     @Required
     private Double height;
     @Required
     private Boolean maleInvolved;
     @Required
     private String cwcLocation;
     private String house;
     private String community;

     private String comments;
 
     public String getStaffId() {
         return staffId;
     }
 
     public void setStaffId(String staffId) {
         this.staffId = staffId;
     }
 
     public String getFacilityId() {
         return facilityId;
     }
 
     public void setFacilityId(String facilityId) {
         this.facilityId = facilityId;
     }
 
     public Date getDate() {
         return date;
     }
 
     public void setDate(Date date) {
         this.date = date;
     }
 
     public String getMotechId() {
         return motechId;
     }
 
     public void setMotechId(String motechId) {
         this.motechId = motechId;
     }
 
     public String getSerialNumber() {
         return serialNumber;
     }
 
     public void setSerialNumber(String serialNumber) {
         this.serialNumber = serialNumber;
     }
 
     public String getImmunizations() {
         return immunizations;
     }
 
     public List<String> immunizations() {
         return StringUtils.isNotEmpty(this.immunizations) ? Arrays.asList(this.immunizations.split(" ")) : null;
     }
 
     public void setImmunizations(String immunizations) {
         this.immunizations = immunizations;
     }
 
     public String getOpvdose() {
         return opvdose;
     }
 
     public void setOpvdose(String opvdose) {
         this.opvdose = opvdose;
     }
 
     public String getPentadose() {
         return pentadose;
     }
 
     public void setPentadose(String pentadose) {
         this.pentadose = pentadose;
     }
 
     public String getIptidose() {
         return iptidose;
     }
 
     public void setIptidose(String iptidose) {
         this.iptidose = iptidose;
     }
 
     public Double getWeight() {
         return weight;
     }
 
     public void setWeight(Double weight) {
         this.weight = weight;
     }
 
     public Double getMuac() {
         return muac;
     }
 
     public void setMuac(Double muac) {
         this.muac = muac;
     }
 
     public Double getHeight() {
         return height;
     }
 
     public void setHeight(Double height) {
         this.height = height;
     }
 
     public Boolean getMaleInvolved() {
         return maleInvolved;
     }
 
     public void setMaleInvolved(Boolean maleInvolved) {
         this.maleInvolved = maleInvolved;
     }
 
     public String getCwcLocation() {
         return cwcLocation;
     }
 
     public void setCwcLocation(String cwcLocation) {
         this.cwcLocation = cwcLocation;
     }
 
     public String getHouse() {
         return house;
     }
 
     public void setHouse(String house) {
         this.house = house;
     }
 
     public String getCommunity() {
         return community;
     }
 
     public void setCommunity(String community) {
         this.community = community;
     }
 
     public String getComments() {
         return comments;
     }
 
     public void setComments(String comments) {
         this.comments = comments;
     }
 
     @Override
     public String groupId() {
         return motechId;
     }
 }
