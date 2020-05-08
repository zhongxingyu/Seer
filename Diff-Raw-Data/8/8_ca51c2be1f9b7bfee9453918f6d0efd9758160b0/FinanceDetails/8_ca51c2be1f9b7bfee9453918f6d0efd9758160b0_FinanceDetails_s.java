 package kabbadi.domain;
 
 import lombok.Getter;
 import lombok.Setter;
 
 import javax.persistence.Embeddable;
 import java.math.BigDecimal;
 import java.util.Date;
 
 @Embeddable
 @Getter
 @Setter
 public class FinanceDetails {
 
     private Date dateOfInvoice;
     private String supplierNameAndAddress;
     private BigDecimal openingPurchaseValueAsOnApril01;
     private BigDecimal additionsDuringTheYear;
     private BigDecimal deletionsDuringTheYear;
 
     public BigDecimal totalPurchaseValue() {
         if(openingPurchaseValueAsOnApril01 == null || additionsDuringTheYear == null || deletionsDuringTheYear == null)
             return null;
 
         return openingPurchaseValueAsOnApril01.add(additionsDuringTheYear).subtract(deletionsDuringTheYear);
     }


 }
