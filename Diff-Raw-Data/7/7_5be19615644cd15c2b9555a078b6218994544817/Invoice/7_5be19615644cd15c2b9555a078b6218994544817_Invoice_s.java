 package kabbadi.domain;
 
 import lombok.Getter;
 import lombok.Setter;
 import org.h2.util.StringUtils;
 import org.hibernate.annotations.Fetch;
 import org.hibernate.annotations.FetchMode;
 
 import javax.persistence.*;
 import java.math.BigDecimal;
 import java.util.Date;
 import java.util.Set;
 
 @Entity
 @Access(AccessType.FIELD)
 @Getter
 @Setter
 public class Invoice implements Comparable<Invoice> {
 
     public static final String INVOICE_NUMBER = "invoiceNumber";
 
     private String invoiceNumber;
 
     private String STPIApprovalNumberAndDate;
     private String descriptionOfGoods;
 
     @Embedded
     @AttributeOverrides({
             @AttributeOverride(name = "amount", column = @Column(name = "foreignValue"))
     })
     private Money foreignValue;
 
     private BigDecimal amountSTPIApproval;
 
     @Embedded
     @AttributeOverrides({
             @AttributeOverride(name = "currency", column = @Column(name = "CIFCurrency")),
             @AttributeOverride(name = "amount", column = @Column(name = "CIFValueInINR"))
     })
     private Money CIFValueInINR;
 
     private String bondNumber;
     private Date dateOfArrival;
     private Date bondDate;
     private String billOfEntryNumber;
     private Date billOfEntryDate;
     private BigDecimal assessableValueInINR;
     private BigDecimal dutyExempt;
     private BigDecimal twentyFivePercentDF;
     private BigDecimal cgApprovedInINR;
     private BigDecimal dutyForgone;
     private BigDecimal runningBalance;
     private BigDecimal outrightPurchase;
     private String loanBasis;
     private BigDecimal freeOfCharge;
     private String status;
     private String remarks;
     private String purchaseOrderNumber;
     private String location;
     private Date dateOfCommissioning;
     private String groupOfAssets;
     private String costCentre;
     private Date dateOfInvoice;
     private String supplierNameAndAddress;
     private BigDecimal openingPurchaseValueAsOnApril01;
     private BigDecimal additionsDuringTheYear;
     private BigDecimal deletionsDuringTheYear;
     private Integer quantity;
     private String identificationNumber;
 
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Integer invoice_id;
 
     @OneToMany(
         cascade = {CascadeType.ALL},
         fetch = FetchType.EAGER,
         mappedBy = "invoice"
     )
     @Fetch(FetchMode.JOIN)
     private Set<Asset> assets;
 
     public Invoice() {
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Invoice invoice = (Invoice) o;
 
         if (!invoice_id.equals(invoice.invoice_id)) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         return invoice_id.hashCode();
     }
 
     @Override
     public int compareTo(Invoice invoice) {
         return this.bondNumber.compareTo(invoice.bondNumber);
     }
 
     public boolean valid() {
         return invoiceNumber != null && !invoiceNumber.isEmpty();
     }
 
     public BigDecimal gbOnDecember31() {
         if(openingPurchaseValueAsOnApril01 == null || additionsDuringTheYear == null || deletionsDuringTheYear == null)
             return null;
 
         return openingPurchaseValueAsOnApril01.add(additionsDuringTheYear).subtract(deletionsDuringTheYear);
     }
 
     public String getCIFDisplayAmountInINR() {
         return (CIFValueInINR == null) ? "" : CIFValueInINR.displayAmount();
     }
 
     public String getForeignValueDisplayAmount() {
         return (foreignValue == null) ? "" : foreignValue.displayAmount();
     }
 
     public String getForeignCurrency() {
         return (foreignValue == null) ? "" : foreignValue.getCurrency();
     }
 
     public boolean isBonded() {
         return !StringUtils.isNullOrEmpty(bondNumber);
     }
 }
