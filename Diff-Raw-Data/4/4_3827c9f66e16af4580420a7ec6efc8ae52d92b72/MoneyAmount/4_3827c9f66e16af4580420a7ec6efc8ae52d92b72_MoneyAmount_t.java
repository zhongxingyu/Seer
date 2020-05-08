 package ro.satrapu.homebudget.services.persistence.model;
 
 import java.math.BigDecimal;
 import java.util.Date;
 import javax.persistence.Column;
 import javax.persistence.FetchType;
 import javax.persistence.ManyToOne;
 import javax.persistence.MappedSuperclass;
 import javax.persistence.PrePersist;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 import lombok.AccessLevel;
 import lombok.Data;
 import lombok.EqualsAndHashCode;
 import lombok.Setter;
 import lombok.ToString;
 
 /**
  *
  * @author satrapu
  */
 @MappedSuperclass
 @Data
 @EqualsAndHashCode(callSuper = true)
 @ToString(callSuper = true)
 public abstract class MoneyAmount extends ManagedEntity {
 
     @NotNull
     @Size(min = 2, max = 4000)
     @Column(nullable = false, length = 4000, name = "DESCRIPTION")
     private String description;
     @NotNull
     @ManyToOne(fetch = FetchType.EAGER, optional = false)
     private Category category;
     @NotNull
     @Size(min = 3, max = 3)
     @Column(nullable = false, length = 3, name = "CURRENCY_CODE")
     /**
      * @see Represents a value from the <a href="http://www.xe.com/iso4217.php">ISO 4217 currency code list</a>.
      */
     private String currencyCode;
     @NotNull
     @Temporal(TemporalType.DATE)
     @Column(nullable = false, name = "CREATE_DATE")
    @Setter(AccessLevel.PRIVATE)
    private Date createDate;
     @NotNull
     @Temporal(TemporalType.DATE)
     @Column(nullable = false, name = "INPUT_DATE")
     private Date inputDate;
     @NotNull
     @Column(nullable = false, name = "AMOUNT")
     private BigDecimal amount;
 
     @PrePersist
     protected void onBeforePersist() {
         createDate = new Date();
     }
 }
