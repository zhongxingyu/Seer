 package org.sola.services.ejb.search.repository.entities;
 
 import java.math.BigDecimal;
 import java.util.Date;
 import javax.persistence.Column;
 import org.sola.services.common.repository.entities.AbstractReadOnlyEntity;
 
 public class RightsExportResult extends AbstractReadOnlyEntity {
 
     public static final String PARAM_DATE_FROM = "dateFrom";
     public static final String PARAM_DATE_TO = "dateTo";
     public static final String PARAM_RIGHT_TYPE = "rightType";
     public static final String SEARCH_QUERY =
             "SELECT DISTINCT b.id AS ba_unit_id, r.status_code AS right_status, r.status_change_date AS right_status_date, "
             + "r.id AS right_id, r.type_code AS right_type, r.registration_date, r.registration_number, r.expiration_date, "
             + "r.nr AS right_tracking_number, r.ground_rent, r.land_use_code, r.lease_number, r.start_date, r.execution_date, "
             + "r.land_usable, r.personal_levy, r.stamp_duty, r.transfer_duty, r.registration_fee,"
             + "(SELECT string_agg(COALESCE(p.name, '') || ' ' || COALESCE(p.last_name, ''), ',') "
             + "FROM administrative.party_for_rrr pr INNER JOIN party.party p ON pr.party_id = p.id WHERE pr.rrr_id = r.id) AS owners, "
             + "payee.payee_id, payee.payee_name, payee.payee_last_name, payee.payee_address, payee.payee_phone, payee.payee_mobile, "
             + "payee.payee_email, payee.payee_id_number, payee.payee_id_type_code, payee.payee_birth_date, payee.payee_gender, "
             + "(SELECT name_firstpart || '-' || name_lastpart FROM cadastre.cadastre_object WHERE id = b.cadastre_object_id LIMIT 1) AS parcel_number, "
             + "(SELECT size FROM cadastre.spatial_value_area WHERE spatial_unit_id = b.cadastre_object_id AND type_code='officialArea' LIMIT 1) AS area "
             + "FROM (administrative.ba_unit b INNER JOIN "
             + "(administrative.rrr r LEFT JOIN "
            + "  (administrative.party_for_rrr prrr INNER JOIN "
             + "       ("
             + "    SELECT p.id AS payee_id, p.name AS payee_name, p.last_name AS payee_last_name, p.birth_date as payee_birth_date, "
             + "    ad.description AS payee_address, p.phone AS payee_phone, p.mobile AS payee_mobile, p.gender_code AS payee_gender, "
             + "    p.email AS payee_email, p.id_number AS payee_id_number, p.id_type_code AS payee_id_type_code "
             + "    FROM (party.party p INNER JOIN party.party_role pr ON p.id=pr.party_id) "
             + "    LEFT JOIN address.address ad ON p.address_id = ad.id "
             + "	    WHERE pr.type_code = 'accountHolder' "
             + "	    ) AS payee ON payee.payee_id = prrr.party_id) "
             + "     ON r.id = prrr.rrr_id) "
             + "   ON b.id = r.ba_unit_id) "
             + "   WHERE (r.type_code = #{" + PARAM_RIGHT_TYPE + "} OR #{" + PARAM_RIGHT_TYPE + "} = '') "
            + "   AND b.status_code != 'pending' AND r.status_code IN ('current', 'historic', 'previous') "
             + "   AND r.registration_date BETWEEN #{" + PARAM_DATE_FROM + "} AND #{" + PARAM_DATE_TO + "} "
             + "   ORDER BY r.registration_date";
     @Column(name = "ba_unit_id")
     private String baUnitId;
     @Column(name = "right_status")
     private String rightStatus;
     @Column(name = "right_status_date")
     private Date rightStatusDate;
     @Column(name = "area")
     private BigDecimal parcelArea;
     @Column(name = "right_id")
     private String rightId;
     @Column(name = "right_type")
     private String rightType;
     @Column(name = "registration_date")
     private Date rightRegistrationDate;
     @Column(name = "start_date")
     private Date startDate;
     @Column(name = "execution_date")
     private Date executionDate;
     @Column(name = "registration_number")
     private String rightRegistrationNumber;
     @Column(name = "expiration_date")
     private Date rightExpirationDate;
     @Column(name = "ground_rent")
     private BigDecimal groundRent;
     @Column(name = "stamp_duty")
     private BigDecimal stampDuty;
     @Column(name = "transfer_duty")
     private BigDecimal transferDuty;
     @Column(name = "registration_fee")
     private BigDecimal registrationFee;
     @Column(name = "personal_levy")
     private BigDecimal personalLevy;
     @Column(name = "land_usable")
     private BigDecimal landUsable;
     @Column(name = "land_use_code")
     private String landUseCode;
     @Column(name = "lease_number")
     private String leaseNumber;
     @Column(name = "right_tracking_number")
     private String rightTrackingNumber;
     @Column(name = "owners")
     private String rightHolders;
     @Column(name = "payee_id")
     private String payeeId;
     @Column(name = "payee_name")
     private String payeeName;
     @Column(name = "payee_last_name")
     private String payeeLastName;
     @Column(name = "payee_gender")
     private String payeeGender;
     @Column(name = "payee_address")
     private String payeeAddress;
     @Column(name = "payee_phone")
     private String payeePhone;
     @Column(name = "payee_mobile")
     private String payeeMobile;
     @Column(name = "payee_email")
     private String payeeEmail;
     @Column(name = "payee_id_number")
     private String payeeIdNumber;
     @Column(name = "payee_id_type_code")
     private String payeeIdTypeCode;
     @Column(name = "payee_birth_date")
     private Date payeeBirthDate;
     @Column(name = "parcel_number")
     private String parcelNumber;
 
     public RightsExportResult() {
         super();
     }
 
     public Date getExecutionDate() {
         return executionDate;
     }
 
     public void setExecutionDate(Date executionDate) {
         this.executionDate = executionDate;
     }
 
     public BigDecimal getGroundRent() {
         return groundRent;
     }
 
     public void setGroundRent(BigDecimal groundRent) {
         this.groundRent = groundRent;
     }
 
     public BigDecimal getLandUsable() {
         return landUsable;
     }
 
     public void setLandUsable(BigDecimal landUsable) {
         this.landUsable = landUsable;
     }
 
     public String getLandUseCode() {
         return landUseCode;
     }
 
     public void setLandUseCode(String landUseCode) {
         this.landUseCode = landUseCode;
     }
 
     public String getLeaseNumber() {
         return leaseNumber;
     }
 
     public void setLeaseNumber(String leaseNumber) {
         this.leaseNumber = leaseNumber;
     }
 
     public BigDecimal getPersonalLevy() {
         return personalLevy;
     }
 
     public void setPersonalLevy(BigDecimal personalLevy) {
         this.personalLevy = personalLevy;
     }
 
     public BigDecimal getRegistrationFee() {
         return registrationFee;
     }
 
     public void setRegistrationFee(BigDecimal registrationFee) {
         this.registrationFee = registrationFee;
     }
 
     public BigDecimal getStampDuty() {
         return stampDuty;
     }
 
     public void setStampDuty(BigDecimal stampDuty) {
         this.stampDuty = stampDuty;
     }
 
     public Date getStartDate() {
         return startDate;
     }
 
     public void setStartDate(Date startDate) {
         this.startDate = startDate;
     }
 
     public BigDecimal getTransferDuty() {
         return transferDuty;
     }
 
     public void setTransferDuty(BigDecimal transferDuty) {
         this.transferDuty = transferDuty;
     }
     
     public String getBaUnitId() {
         return baUnitId;
     }
 
     public void setBaUnitId(String baUnitId) {
         this.baUnitId = baUnitId;
     }
 
     public BigDecimal getParcelArea() {
         return parcelArea;
     }
 
     public void setParcelArea(BigDecimal parcelArea) {
         this.parcelArea = parcelArea;
     }
 
     public String getParcelNumber() {
         return parcelNumber;
     }
 
     public void setParcelNumber(String parcelNumber) {
         this.parcelNumber = parcelNumber;
     }
 
     public String getPayeeAddress() {
         return payeeAddress;
     }
 
     public void setPayeeAddress(String payeeAddress) {
         this.payeeAddress = payeeAddress;
     }
 
     public Date getPayeeBirthDate() {
         return payeeBirthDate;
     }
 
     public void setPayeeBirthDate(Date payeeBirthDate) {
         this.payeeBirthDate = payeeBirthDate;
     }
 
     public String getPayeeEmail() {
         return payeeEmail;
     }
 
     public void setPayeeEmail(String payeeEmail) {
         this.payeeEmail = payeeEmail;
     }
 
     public String getPayeeGender() {
         return payeeGender;
     }
 
     public void setPayeeGender(String payeeGender) {
         this.payeeGender = payeeGender;
     }
 
     public String getPayeeId() {
         return payeeId;
     }
 
     public void setPayeeId(String payeeId) {
         this.payeeId = payeeId;
     }
 
     public String getPayeeIdNumber() {
         return payeeIdNumber;
     }
 
     public void setPayeeIdNumber(String payeeIdNumber) {
         this.payeeIdNumber = payeeIdNumber;
     }
 
     public String getPayeeIdTypeCode() {
         return payeeIdTypeCode;
     }
 
     public void setPayeeIdTypeCode(String payeeIdTypeCode) {
         this.payeeIdTypeCode = payeeIdTypeCode;
     }
 
     public String getPayeeLastName() {
         return payeeLastName;
     }
 
     public void setPayeeLastName(String payeeLastName) {
         this.payeeLastName = payeeLastName;
     }
 
     public String getPayeeMobile() {
         return payeeMobile;
     }
 
     public void setPayeeMobile(String payeeMobile) {
         this.payeeMobile = payeeMobile;
     }
 
     public String getPayeeName() {
         return payeeName;
     }
 
     public void setPayeeName(String payeeName) {
         this.payeeName = payeeName;
     }
 
     public String getPayeePhone() {
         return payeePhone;
     }
 
     public void setPayeePhone(String payeePhone) {
         this.payeePhone = payeePhone;
     }
 
     public Date getRightExpirationDate() {
         return rightExpirationDate;
     }
 
     public void setRightExpirationDate(Date rightExpirationDate) {
         this.rightExpirationDate = rightExpirationDate;
     }
 
     public String getRightHolders() {
         return rightHolders;
     }
 
     public void setRightHolders(String rightHolders) {
         this.rightHolders = rightHolders;
     }
 
     public String getRightId() {
         return rightId;
     }
 
     public void setRightId(String rightId) {
         this.rightId = rightId;
     }
 
     public Date getRightRegistrationDate() {
         return rightRegistrationDate;
     }
 
     public void setRightRegistrationDate(Date rightRegistrationDate) {
         this.rightRegistrationDate = rightRegistrationDate;
     }
 
     public String getRightRegistrationNumber() {
         return rightRegistrationNumber;
     }
 
     public void setRightRegistrationNumber(String rightRegistrationNumber) {
         this.rightRegistrationNumber = rightRegistrationNumber;
     }
 
     public String getRightStatus() {
         return rightStatus;
     }
 
     public void setRightStatus(String rightStatus) {
         this.rightStatus = rightStatus;
     }
 
     public Date getRightStatusDate() {
         return rightStatusDate;
     }
 
     public void setRightStatusDate(Date rightStatusDate) {
         this.rightStatusDate = rightStatusDate;
     }
 
     public String getRightTrackingNumber() {
         return rightTrackingNumber;
     }
 
     public void setRightTrackingNumber(String rightTrackingNumber) {
         this.rightTrackingNumber = rightTrackingNumber;
     }
 
     public String getRightType() {
         return rightType;
     }
 
     public void setRightType(String rightType) {
         this.rightType = rightType;
     }
 }
