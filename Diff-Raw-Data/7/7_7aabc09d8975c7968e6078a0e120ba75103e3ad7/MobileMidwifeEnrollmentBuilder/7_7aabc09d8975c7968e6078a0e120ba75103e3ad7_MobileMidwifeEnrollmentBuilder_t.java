 package org.motechproject.ghana.national.builder;
 
 import org.joda.time.DateTime;
 import org.motechproject.ghana.national.domain.mobilemidwife.*;
 import org.motechproject.model.DayOfWeek;
 import org.motechproject.model.Time;
 
 public class MobileMidwifeEnrollmentBuilder extends AbstractBuilder<MobileMidwifeEnrollment>{
 
     private String patientId;
     private String staffId;
     private String facilityId;
     private Boolean consent;
     private ServiceType serviceType;
     private PhoneOwnership phoneOwnership;
     private String phoneNumber;
     private Medium medium;
     private DayOfWeek dayOfWeek;
     private Time timeOfDay;
     private Language language;
     private LearnedFrom learnedFrom;
     private ReasonToJoin reasonToJoin;
     private String messageStartWeek;
     private Boolean active;
     private DateTime enrollmentDateTime;
    private DateTime scheduleStartDate;
 
     public MobileMidwifeEnrollmentBuilder patientId(String patientId) {
         this.patientId = patientId;
         return this;
     }
 
     public MobileMidwifeEnrollmentBuilder staffId(String staffId) {
         this.staffId = staffId;
         return this;
     }
 
     public MobileMidwifeEnrollmentBuilder facilityId(String facilityId) {
         this.facilityId = facilityId;
         return this;
     }
 
     public MobileMidwifeEnrollmentBuilder consent(Boolean consent) {
         this.consent = consent;
         return this;
     }
 
     public MobileMidwifeEnrollmentBuilder serviceType(ServiceType serviceType) {
         this.serviceType = serviceType;
         return this;
     }
 
     public MobileMidwifeEnrollmentBuilder phoneOwnership(PhoneOwnership phoneOwnership) {
         this.phoneOwnership = phoneOwnership;
         return this;
     }
 
     public MobileMidwifeEnrollmentBuilder phoneNumber(String phoneNumber) {
         this.phoneNumber = phoneNumber;
         return this;
     }
 
     public MobileMidwifeEnrollmentBuilder medium(Medium medium) {
         this.medium = medium;
         return this;
     }
 
     public MobileMidwifeEnrollmentBuilder dayOfWeek(DayOfWeek dayOfWeek) {
         this.dayOfWeek = dayOfWeek;
         return this;
     }
 
     public MobileMidwifeEnrollmentBuilder timeOfDay(Time timeOfDay) {
         this.timeOfDay = timeOfDay;
         return this;
     }
 
     public MobileMidwifeEnrollmentBuilder language(Language language) {
         this.language = language;
         return this;
     }
 
     public MobileMidwifeEnrollmentBuilder learnedFrom(LearnedFrom learnedFrom) {
         this.learnedFrom = learnedFrom;
         return this;
     }
 
     public MobileMidwifeEnrollmentBuilder reasonToJoin(ReasonToJoin reasonToJoin) {
         this.reasonToJoin = reasonToJoin;
         return this;
     }
 
     public MobileMidwifeEnrollmentBuilder messageStartWeek(String messageStartWeek) {
         this.messageStartWeek = messageStartWeek;
         return this;
     }
     
     public MobileMidwifeEnrollmentBuilder active(Boolean active) {
         this.active = active;
         return this;
     }
     
     public MobileMidwifeEnrollmentBuilder enrollmentDateTime(DateTime enrollmentDateTime){
         this.enrollmentDateTime = enrollmentDateTime;
         return this;
     }
 
    public MobileMidwifeEnrollmentBuilder scheduleStartDate(DateTime scheduleStartDate){
            this.scheduleStartDate  = scheduleStartDate;
            return this;
      }    
     
     @Override
     public MobileMidwifeEnrollment build() {
         return generate(MobileMidwifeEnrollment.newEnrollment());
     }
 }
