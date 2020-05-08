 package it.sevenbits.conferences.domain;
 
 import it.sevenbits.conferences.utils.converter.DateTimeConverter;
 
 import javax.persistence.*;
 
 /**
  * POJO class for Conferences model.
  */
 @Entity
 @Table(name = "conference")
 public class Conference {
 
     private Long id;
     private Company company;
     private Long ordinalNumber;
     private Long date;
     private boolean registration;
 
     @Id
     @GeneratedValue
     @Column(name = "id")
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     @ManyToOne
     @JoinColumn(name = "company_id")
     public Company getCompany() {
         return company;
     }
 
     public void setCompany(Company company) {
         this.company = company;
     }
 
     @Column(name = "ordinal_number")
     public Long getOrdinalNumber() {
         return ordinalNumber;
     }
 
     public void setOrdinalNumber(Long ordinalNumber) {
         this.ordinalNumber = ordinalNumber;
     }
 
     @Column(name = "date")
     public Long getDate() {
         return date;
     }
 

    @Column(name="registration")
     public boolean isRegistration() {
         return registration;
     }
 
     public void setRegistration(boolean registration) {
         this.registration = registration;
     }
 
     public void setDate(Long date) {
         this.date = date;
     }
 
 
 
     @Transient
     public String getHumanReadableDate() {
 
         return DateTimeConverter.fromLong(date);
     }
 }
