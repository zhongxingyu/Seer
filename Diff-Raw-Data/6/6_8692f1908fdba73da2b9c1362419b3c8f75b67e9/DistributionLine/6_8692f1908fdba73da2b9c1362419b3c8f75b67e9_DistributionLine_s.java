 package com.orangeleap.tangerine.domain.paymentInfo;
 
 import java.math.BigDecimal;
 
 import org.springframework.core.style.ToStringCreator;
 import org.springframework.util.StringUtils;
 
 import com.orangeleap.tangerine.domain.AbstractCustomizableEntity;
 import com.orangeleap.tangerine.util.StringConstants;
 
 public class DistributionLine extends AbstractCustomizableEntity {  
  
     private static final long serialVersionUID = 1L;
     private BigDecimal amount;
     private BigDecimal percentage;
     private String projectCode;
     private String motivationCode;
     private String other_motivationCode;
     private Long giftId;
     private Long commitmentId;
 
     public DistributionLine() { }
 
     public DistributionLine(BigDecimal amount, BigDecimal percentage, String projectCode, String motivationCode, String other_motivationCode) {
         this();
         this.amount = amount;
         this.percentage = percentage;
         this.projectCode = projectCode;
         this.motivationCode = motivationCode;
         this.other_motivationCode = other_motivationCode;
     }
     
     public DistributionLine(DistributionLine otherLine, boolean isGiftId, Long id) {
         this(otherLine.getAmount(), otherLine.getPercentage(), otherLine.getProjectCode(), otherLine.getMotivationCode(), otherLine.getOther_motivationCode());
         if (isGiftId) {
             this.giftId = id;
         }
         else {
             this.commitmentId = id;
         }
     }
 
     public BigDecimal getAmount() {
         return amount;
     }
 
     public void setAmount(BigDecimal amount) {
         this.amount = amount;
     }
 
     public BigDecimal getPercentage() {
         return percentage;
     }
 
     public void setPercentage(BigDecimal percentage) {
         this.percentage = percentage;
     }
 
     public String getProjectCode() {
         return projectCode;
     }
 
     public void setProjectCode(String projectCode) {
         this.projectCode = projectCode;
     }
 
     public String getMotivationCode() {
         return motivationCode;
     }
 
     public void setMotivationCode(String motivationCode) {
         this.motivationCode = motivationCode;
     }
 
     public String getOther_motivationCode() {
         return other_motivationCode;
     }
 
     public void setOther_motivationCode(String other_motivationCode) {
         this.other_motivationCode = other_motivationCode;
     }
 
     public Long getGiftId() {
         return giftId;
     }
 
     public void setGiftId(Long giftId) {
         this.giftId = giftId;
     }
 
     public Long getCommitmentId() {
         return commitmentId;
     }
 
     public void setCommitmentId(Long commitmentId) {
         this.commitmentId = commitmentId;
     }
     
     public boolean isValid() {
         boolean valid = false;
         if (amount != null) {
             valid = true;
         }
         return valid;
     }
     
     public boolean isFieldEntered() {
         return amount != null || percentage != null || StringUtils.hasText(projectCode) || StringUtils.hasText(motivationCode) || StringUtils.hasText(other_motivationCode);
     }
     
     @Override
     public void setDefaults() {
         super.setDefaults();
         setDefaultCustomFieldValue(StringConstants.TAX_DEDUCTIBLE, "true"); 
     }
 
     @Override
     public String toString() {
         return new ToStringCreator(this).append(super.toString()).append("amount", amount).
             append(super.toString()).append("percentage", percentage).append("projectCode", projectCode).
             append(super.toString()).append("motivationCode", motivationCode).append("other_motivationCode", other_motivationCode).
             append(super.toString()).append("giftId", giftId).append("commitmentId", commitmentId).
             toString();
     }
 }
