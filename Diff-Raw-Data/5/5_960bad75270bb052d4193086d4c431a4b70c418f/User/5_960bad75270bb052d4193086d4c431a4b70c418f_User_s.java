 package com.gardin.piazza.domain.users;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 /**
  * Represents a user that can have several roles: admin, author, reviewer,
  * meta-reviewer, chairman.
  * 
  * @author Pierre Gardin
  */
 @Entity
 @Table(name = "user")
 @Inheritance(strategy = InheritanceType.JOINED)
 public class User {
 
     // TODO bi-directional relation with Conference (User is registered with the Conference) + email is verified
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private int id;
 
     @Column(name = "first_name", nullable = true)
     private String firstName;
 
     @Column(name = "surname", nullable = true)
     private String surname;
 
     @Column(name = "email", nullable = false)
     private String email;
 
     @Column(name = "password", nullable = false)
     private String password;
 
     @Column(name = "institution", nullable = false)
     private String institution;
 
     @Column(name = "country", nullable = false)
     private String country;
 
     @ManyToOne
     @JoinColumn(name = "address_id")
     private Address address;
 
     public Address getAddress() {
         return address;
     }
 
     public String getCountry() {
         return country;
     }
 
     public String getEmail() {
         return email;
     }
 
     public String getFirstName() {
         return firstName;
     }
 
    public int getID() {
         return id;
     }
 
     public String getInstitution() {
         return institution;
     }
 
     public String getPassword() {
         return password;
     }
 
     public String getSurname() {
         return surname;
     }
 
     public void setAddress(Address address) {
         this.address = address;
     }
 
     public void setCountry(String country) {
         this.country = country;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
    public void setID(int id) {
         this.id = id;
     }
 
     public void setInstitution(String institution) {
         this.institution = institution;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public void setSurname(String surname) {
         this.surname = surname;
     }
 }
