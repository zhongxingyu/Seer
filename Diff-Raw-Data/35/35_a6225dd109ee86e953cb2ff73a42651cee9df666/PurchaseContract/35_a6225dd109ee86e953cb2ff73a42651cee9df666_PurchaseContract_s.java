 package com.pms.service.mockbean;
 
 import java.math.BigDecimal;
 
 public class PurchaseContract extends PurchaseRequestOrder{
     
     private String purchaseContractType;
     
     private String signBy;
     
     private String supplierName;
     
     private String supplierNameContact;
     
     private String signDate;
     
     private BigDecimal firstPay;
     
     
     private BigDecimal moneyProgress;
     
     private BigDecimal deposit;
     
 
     private String invoiceType;
     
     
     private String description;
 
 
     public String getPurchaseContractType() {
         return purchaseContractType;
     }
 
 
     public void setPurchaseContractType(String purchaseContractType) {
         this.purchaseContractType = purchaseContractType;
     }
 
 
     public String getSignBy() {
         return signBy;
     }
 
 
     public void setSignBy(String signBy) {
         this.signBy = signBy;
     }
 
 
     public String getSupplierName() {
         return supplierName;
     }
 
 
     public void setSupplierName(String supplierName) {
         this.supplierName = supplierName;
     }
 
 
     public String getSupplierNameContact() {
         return supplierNameContact;
     }
 
 
     public void setSupplierNameContact(String supplierNameContact) {
         this.supplierNameContact = supplierNameContact;
     }
 
 
 
     public String getSignDate() {
         return signDate;
     }
 
 
     public void setSignDate(String signDate) {
         this.signDate = signDate;
     }
 
 
     public BigDecimal getFirstPay() {
         return firstPay;
     }
 
 
     public void setFirstPay(BigDecimal firstPay) {
         this.firstPay = firstPay;
     }
 
 
     public BigDecimal getMoneyProgress() {
         return moneyProgress;
     }
 
 
     public void setMoneyProgress(BigDecimal moneyProgress) {
         this.moneyProgress = moneyProgress;
     }
 
 
     public BigDecimal getDeposit() {
         return deposit;
     }
 
 
     public void setDeposit(BigDecimal deposit) {
         this.deposit = deposit;
     }
 
 
 
 
     public String getInvoiceType() {
         return invoiceType;
     }
 
 
     public void setInvoiceType(String invoiceType) {
         this.invoiceType = invoiceType;
     }
 
 
     public String getDescription() {
         return description;
     }
 
 
     public void setDescription(String description) {
         this.description = description;
     }
     
     
     
     
 
 }
