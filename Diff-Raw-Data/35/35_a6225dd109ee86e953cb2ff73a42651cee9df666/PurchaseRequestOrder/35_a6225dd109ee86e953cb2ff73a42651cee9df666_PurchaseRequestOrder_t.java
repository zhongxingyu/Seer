 package com.pms.service.mockbean;
 
 import java.math.BigDecimal;
 
 public class PurchaseRequestOrder extends BaseEntity {
 
     private String status;
 
     private String approvedDate;
     
     private String backRequestCode;   
     
     private String salesContractCode;
     
 
     private String purchaseOrderCode;
 
     private String purchaseRequestCode;
     
     private String purchaseContractCode;
     
     private String purchaseBackCode;
 
     private BigDecimal requestedTotalMoney;
 
     private int requestedNumbers;
 
     private float moneyOfContract;
 
     
     
     // FIELD VAULES
 
     public String getSalesContractCode() {
         return salesContractCode;
     }
 
     public void setSalesContractCode(String salesContractCode) {
         this.salesContractCode = salesContractCode;
     }
 
     public String getStatus() {
         return status;
     }
 
     public void setStatus(String status) {
         this.status = status;
     }
     
     
 
     public String getBackRequestCode() {
         return backRequestCode;
     }
 
     public void setBackRequestCode(String backRequestCode) {
         this.backRequestCode = backRequestCode;
     }
 
     public String getApprovedDate() {
         return approvedDate;
     }
 
     public void setApprovedDate(String approvedDate) {
         this.approvedDate = approvedDate;
     }
 
     public String getPurchaseOrderCode() {
         return purchaseOrderCode;
     }
 
     public void setPurchaseOrderCode(String purchaseOrderCode) {
         this.purchaseOrderCode = purchaseOrderCode;
     }
 
     public String getPurchaseRequestCode() {
         return purchaseRequestCode;
     }
 
     public void setPurchaseRequestCode(String purchaseRequestCode) {
         this.purchaseRequestCode = purchaseRequestCode;
     }
 
     public String getPurchaseBackCode() {
         return purchaseBackCode;
     }
 
     public void setPurchaseBackCode(String purchaseBackCode) {
         this.purchaseBackCode = purchaseBackCode;
     }
 
     public BigDecimal getRequestedTotalMoney() {
         return requestedTotalMoney;
     }
 
     public void setRequestedTotalMoney(BigDecimal requestedTotalMoney) {
         this.requestedTotalMoney = requestedTotalMoney;
     }
 
     public int getRequestedNumbers() {
         return requestedNumbers;
     }
 
     public void setRequestedNumbers(int requestedNumbers) {
         this.requestedNumbers = requestedNumbers;
     }
 
     public float getMoneyOfContract() {
         return moneyOfContract;
     }
 
     public void setMoneyOfContract(float moneyOfContract) {
         this.moneyOfContract = moneyOfContract;
     }
     
     
   
     public String getPurchaseContractCode() {
         return purchaseContractCode;
     }
 
     public void setPurchaseContractCode(String purchaseContractCode) {
         this.purchaseContractCode = purchaseContractCode;
     }
 
 
 
 
     public static final String PROCESS_STATUS = "status";
     public static final String APPROVED_DATE = "approvedDate";
 
     public static final String STATUS_DRAFT = "草稿";
     public static final String STATUS_NEW = "审批中";
     public static final String STATUS_APPROVED = "审批通过";
     public static final String STATUS_REJECTED = "审批拒绝";
 
 }
