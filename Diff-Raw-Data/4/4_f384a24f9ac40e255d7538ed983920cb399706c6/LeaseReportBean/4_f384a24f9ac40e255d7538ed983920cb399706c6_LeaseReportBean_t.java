 /**
  * ******************************************************************************************
  * Copyright (c) 2013 Food and Agriculture Organization of the United Nations (FAO)
  * and the Lesotho Land Administration Authority (LAA). All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *
  *    1. Redistributions of source code must retain the above copyright notice,this list
  *       of conditions and the following disclaimer.
  *    2. Redistributions in binary form must reproduce the above copyright notice,this list
  *       of conditions and the following disclaimer in the documentation and/or other
  *       materials provided with the distribution.
  *    3. Neither the names of FAO, the LAA nor the names of its contributors may be used to
  *       endorse or promote products derived from this software without specific prior
  * 	  written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
  * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT
  * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,STRICT LIABILITY,OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
  * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * *********************************************************************************************
  */
 package org.sola.clients.beans.administrative;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import org.sola.clients.beans.AbstractBindingBean;
 import org.sola.clients.beans.application.ApplicationBean;
 import org.sola.clients.beans.application.ApplicationServiceBean;
 import org.sola.clients.beans.cadastre.CadastreObjectBean;
 import org.sola.clients.beans.party.PartyBean;
 import org.sola.common.DateUtility;
 import org.sola.common.NumberToWords;
 import org.sola.common.StringUtility;
 
 /**
  * Provides data to be used for generating reports.
  */
 public class LeaseReportBean extends AbstractBindingBean {
 
     private ApplicationBean application;
     private ApplicationServiceBean service;
     private RrrBean lease;
     private String freeText;
     private CadastreObjectBean cadastreObject;
 
     /**
      * Default constructor.
      */
     public LeaseReportBean() {
         super();
     }
 
     /**
      * Class constructor with initial values for BaUnit, RRR, Application and
      * ApplicationService object.
      */
     public LeaseReportBean(RrrBean lease, CadastreObjectBean cadastreObject, ApplicationBean application, ApplicationServiceBean service) {
         super();
         this.lease = lease;
         this.cadastreObject = cadastreObject;
         this.application = application;
         this.service = service;
     }
 
     public String getFreeText() {
         if (freeText == null) {
             freeText = "";
         }
         return freeText;
     }
 
     public void setFreeText(String freeText) {
         if (freeText == null) {
             freeText = "";
         }
         this.freeText = freeText;
     }
 
     public RrrBean getLease() {
         if (lease == null) {
             lease = new RrrBean();
         }
         return lease;
     }
 
     public void setLease(RrrBean lease) {
         if (lease == null) {
             lease = new RrrBean();
         }
         this.lease = lease;
     }
 
     public CadastreObjectBean getCadastreObject() {
         if (cadastreObject == null) {
             cadastreObject = new CadastreObjectBean();
         }
         return cadastreObject;
     }
 
     public void setCadastreObject(CadastreObjectBean cadastreObject) {
         this.cadastreObject = cadastreObject;
     }
 
     public ApplicationBean getApplication() {
         if (application == null) {
             application = new ApplicationBean();
         }
         return application;
     }
 
     public void setApplication(ApplicationBean application) {
         if (application == null) {
             application = new ApplicationBean();
         }
         this.application = application;
     }
 
     public ApplicationServiceBean getService() {
         if (service == null) {
             service = new ApplicationServiceBean();
         }
         return service;
     }
 
     public void setService(ApplicationServiceBean service) {
         if (service == null) {
             service = new ApplicationServiceBean();
         }
         this.service = service;
     }
 
     /**
      * Shortcut for application number.
      */
     public String getApplicationNumber() {
         return StringUtility.empty(getApplication().getApplicationNumberFormatted());
     }
 
     /**
      * Shortcut for application date, converted to string.
      */
     public String getApplicationDate() {
         return DateUtility.getShortDateString(getApplication().getLodgingDatetime(), true);
     }
 
     /**
      * Shortcut for applicant's full name.
      */
     public String getApplicantName() {
         if (getApplication().getContactPerson() != null && getApplication().getContactPerson().getFullName() != null) {
             return getApplication().getContactPerson().getFullName();
         }
         return "";
     }
 
     /**
      * Returns total payment for the lease including service fee, stamp duty, 
      * remaining ground rent and registration fee. 
      */
     public String getTotalLeaseFee(){
         BigDecimal serviceFee = BigDecimal.ZERO;
         BigDecimal regFee = BigDecimal.ZERO;
         BigDecimal stampDuty = BigDecimal.ZERO;
         BigDecimal remGroundRent = BigDecimal.valueOf(getLease().getGroundRentRemaining());
         
        if(getService()!=null && getLease().getServiceFee()!=null){
            serviceFee = getLease().getServiceFee();
         }
         if(getLease()!=null && getLease().getRegistrationFee()!=null){
             regFee = getLease().getRegistrationFee();
         }
         if(getLease()!=null && getLease().getStampDuty()!=null){
             stampDuty = getLease().getStampDuty();
         }
         
         BigDecimal totalFee = serviceFee.add(regFee).add(stampDuty).add(remGroundRent);
         if(totalFee.compareTo(BigDecimal.ZERO)!=0){
             return "M " + totalFee.setScale(2, RoundingMode.HALF_UP).toPlainString();
         } else {
             return "NIL";
         }
     }
     
     /** Shortcut to the service fee. */
     public String getServiceFee(){
         if(getService()!=null && getLease().getServiceFee()!=null){
             return "M " + getLease().getServiceFee().setScale(2, RoundingMode.HALF_UP).toPlainString();
         }
         return "NIL";
     }
     
     /** Shortcut to the registration fee. */
     public String getRegistrationFee(){
         if(getLease()!=null && getLease().getRegistrationFee()!=null){
             return "M " + getLease().getRegistrationFee().setScale(2, RoundingMode.HALF_UP).toPlainString();
         }
         return "NIL";
     }
     
     /** Shortcut to stamp duty. */
     public String getStampDuty(){
         if(getLease()!=null && getLease().getStampDuty()!=null){
             return "M " + getLease().getStampDuty().setScale(2, RoundingMode.HALF_UP).toPlainString();
         }
         return "NIL";
     }
     
     /**
      * Shortcut for the ground rent.
      */
     public String getGroundRent() {
         if (getLease().getGroundRent() != null && getLease().getGroundRent().compareTo(BigDecimal.ZERO) > 0) {
             return "M " + getLease().getGroundRent().setScale(2, RoundingMode.HALF_UP).toPlainString();
         } else {
             return "NIL";
         }
     }
     
     /**
      * Calculated remaining ground rent.
      */
     public String getGroundRentRemaining() {
         if (getLease().getGroundRentRemaining() > 0) {
             return "M " + String.valueOf(getLease().getGroundRentRemaining());
         } else {
             return "NIL";
         }
     }
     
     /**
      * Shortcut for service name.
      */
     public String getServiceName() {
         if (getService() != null && getService().getRequestType() != null
                 && getService().getRequestType().getDisplayValue() != null) {
             return getService().getRequestType().getDisplayValue();
         }
         return "";
     }
 
     /**
      * Shortcut for the parcel first/last name parts.
      */
     public String getParcelCode() {
         if (getCadastreObject() != null) {
             return getCadastreObject().toString();
         }
         return "";
     }
 
     /**
      * Shortcut for the parcel type.
      */
     public String getParcelType() {
         if (getCadastreObject() != null) {
             if (getCadastreObject().getCadastreObjectType() != null) {
                 return getCadastreObject().getCadastreObjectType().toString();
             }
         }
         return "";
     }
 
     /**
      * Shortcut for the parcel official area.
      */
     public String getParcelOfficialArea() {
         if (getCadastreObject() != null) {
             if (getCadastreObject().getOfficialAreaSize() != null) {
                 return getCadastreObject().getOfficialAreaSize().toPlainString();
             }
         }
         return "";
     }
 
     /**
      * Shortcut for the parcel land use.
      */
     public String getParcelLandUse() {
         if (getLease().getLandUseType() != null) {
             return getLease().getLandUseType().getDisplayValue().toUpperCase();
         }
         return "";
     }
 
     /**
      * Shortcut for the parcel address.
      */
     public String getParcelAddress() {
         if (getCadastreObject() != null) {
             return getCadastreObject().getAddressString().toUpperCase();
         }
         return "";
     }
 
     /**
      * Shortcut for the first parcel map reference number.
      */
     public String getParcelMapRef() {
         if (getCadastreObject() != null && getCadastreObject().getSourceReference() != null) {
             return getCadastreObject().getSourceReference();
         }
         return "";
     }
 
     /**
      * Shortcut for the lease registration number.
      */
     public String getLeaseRegistrationNumber() {
         if (getLease().getLeaseNumber() != null) {
             return getLease().getLeaseNumber();
         }
         return "";
     }
 
     /**
      * Shortcut for the lease execution date in MEDIUM format without time.
      */
     public String getLeaseExecutionDate() {
         return DateUtility.getMediumDateString(getLease().getExecutionDate(), false);
     }
 
     /**
      * Shortcut for the lease execution day.
      */
     public String getLeaseExecutionDay() {
         if (getLease().getExecutionDate() == null) {
             return "";
         }
 
         Calendar cal = Calendar.getInstance();
         cal.setTime(getLease().getExecutionDate());
         return String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
     }
 
     /**
      * Shortcut for the lease execution year and month.
      */
     public String getLeaseExecutionMonthAndYear() {
         if (getLease().getExecutionDate() == null) {
             return "";
         }
 
         Calendar cal = Calendar.getInstance();
         cal.setTime(getLease().getExecutionDate());
         return new SimpleDateFormat("MMMMM").format(cal.getTime()) + " " + String.valueOf(cal.get(Calendar.YEAR));
     }
 
     /**
      * Shortcut for the lease expiration date in MEDIUM format without time.
      */
     public String getLeaseExpirationDate() {
         return DateUtility.getMediumDateString(getLease().getExpirationDate(), false);
     }
 
     /**
      * Shortcut for the lease expiration day.
      */
     public String getLeaseExpirationDay() {
         if (getLease().getExpirationDate() == null) {
             return "";
         }
 
         Calendar cal = Calendar.getInstance();
         cal.setTime(getLease().getExpirationDate());
         return String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
     }
 
     /**
      * Shortcut for the lease expiration year and month.
      */
     public String getLeaseExpirationMonthAndYear() {
         if (getLease().getExpirationDate() == null) {
             return "";
         }
 
         Calendar cal = Calendar.getInstance();
         cal.setTime(getLease().getExpirationDate());
         return new SimpleDateFormat("MMMMM").format(cal.getTime()) + " " + String.valueOf(cal.get(Calendar.YEAR));
     }
 
     /**
      * Shortcut for the lease start date in MEDIUM format without time.
      */
     public String getLeaseStartDate() {
         return DateUtility.getMediumDateString(getLease().getStartDate(), false);
     }
 
     /**
      * Shortcut for the lease start day.
      */
     public String getLeaseStartDay() {
         if (getLease().getStartDate() == null) {
             return "";
         }
 
         Calendar cal = Calendar.getInstance();
         cal.setTime(getLease().getStartDate());
         return String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
     }
 
     /**
      * Shortcut for the lease start year and month.
      */
     public String getLeaseStartMonthAndYear() {
         if (getLease().getStartDate() == null) {
             return "";
         }
 
         Calendar cal = Calendar.getInstance();
         cal.setTime(getLease().getStartDate());
         return new SimpleDateFormat("MMMMM").format(cal.getTime()) + " " + String.valueOf(cal.get(Calendar.YEAR));
     }
 
     /**
      * Shortcut to lessee address.
      */
     public String getLesseeAddress() {
         String address = "";
         if (getLease().getRightHolderList().size() > 0) {
             for (PartyBean party : getLease().getFilteredRightHolderList()) {
                 if (party.getAddress() != null && !StringUtility.empty(party.getAddress().getDescription()).equals("")) {
                     address = party.getAddress().getDescription();
                     break;
                 }
             }
         }
         return address.toUpperCase();
     }
 
     /**
      * Shortcut to lessees marital status.
      */
     public String getLesseeMaritalStatus() {
         String legalStatus = "";
         if (getLease().getRightHolderList().size() > 0) {
             for (PartyBean party : getLease().getFilteredRightHolderList()) {
                 if (!StringUtility.empty(party.getLegalType()).equals("")) {
                     legalStatus = party.getLegalType();
                     break;
                 }
             }
         }
         return legalStatus.toUpperCase();
     }
 
     /**
      * Shortcut to lessees names.
      */
     public String getLessees() {
         String lessess = "";
         if (getLease().getFilteredRightHolderList() != null && getLease().getFilteredRightHolderList().size() > 0) {
             for (PartyBean party : getLease().getFilteredRightHolderList()) {
                 if (lessess.equals("")) {
                     lessess = party.getFullName();
                 } else {
                     lessess = lessess + " AND " + party.getFullName();
                 }
             }
         }
         return lessess.toUpperCase();
     }
 
     /**
      * Returns true is lessee is private, otherwise false
      */
     public boolean isLesseePrivate() {
         boolean result = true;
         if (getLease().getRightHolderList() != null && getLease().getRightHolderList().size() > 0) {
             result = getLease().getRightHolderList().get(0).getTypeCode().equalsIgnoreCase("naturalPerson");
         }
         return result;
     }
 
     /**
      * Shortcut to lessees and marital status.
      */
     public String getLesseesAndMaritalStatus() {
         String result = getLessees();
         if (!result.equals("") && !getLesseeMaritalStatus().equals("")) {
             result = result + " - " + getLesseeMaritalStatus();
         }
         return result;
     }
 
     /**
      * Calculates and returns lease term in years.
      */
     public String getLeaseTerm() {
         return String.valueOf(getLease().getLeaseTerm());
     }
 
     /**
      * Calculates and returns lease term in years transformed into words.
      */
     public String getLeaseTermWord() {
         NumberToWords.DefaultProcessor processor = new NumberToWords.DefaultProcessor();
         return processor.getName(getLeaseTerm());
     }
 }
