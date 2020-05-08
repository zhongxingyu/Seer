 package com.acme.domain;
 
 import javax.persistence.*;
 import javax.validation.constraints.NotNull;
 import javax.xml.bind.annotation.XmlRootElement;
 import java.io.Serializable;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * User: danny
  * Date: 2/19/12
  * Time: 11:37 PM
  */
 @XmlRootElement
 @Entity
 @NamedQueries({
         @NamedQuery(name = "findByEmail", query = "FROM Customer c WHERE c.email = :email"),
         @NamedQuery(name = "findByUsername", query = "FROM Customer c WHERE c.username = :username")})
 public class Customer implements Serializable {
 
     private static final long serialVersionUID = 1;
 
     @Id
     @GeneratedValue
    @Column(name = "CUSTOMER_ID")
     private Long customerId;
 
     private String firstName;
 
     private String lastName;
 
     @NotNull
     @Column(unique = true)
     private String email;
 
     @Column(unique = true)
     private String username;
 
     @NotNull
     private String password;
 
     @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
     @JoinColumn(name = "CUSTOMER_ID")
     private List<Address> addressList;
 
     /**
      * Default constructor.
      */
     public Customer() {
     }
 
     /**
      * Constructor.
      *
      * @param firstName the first name
      * @param lastName  the last name
      * @param email     the email address
      * @param username  the username
      * @param password  the password
      */
     public Customer(String firstName, String lastName, String email, String username, String password) {
         this.firstName = firstName;
         this.lastName = lastName;
         this.email = email;
         this.username = username;
         this.password = password;
     }
 
     /**
      * Get the username.
      *
      * @return username
      */
     public String getUsername() {
         return username;
     }
 
     /**
      * Set the username.
      *
      * @param username the username to set
      */
     public void setUsername(String username) {
         this.username = username;
     }
 
     /**
      * Get the password.
      *
      * @return password
      */
     public String getPassword() {
         return password;
     }
 
     /**
      * Set the password.
      *
      * @param password the password to set
      */
     public void setPassword(String password) {
         this.password = password;
     }
 
     /**
      * Get the customer id.
      *
      * @return customerId
      */
     public Long getCustomerId() {
         return customerId;
     }
 
     /**
      * Set the customer id.
      *
      * @param customerId the customer id to set
      */
     public void setCustomerId(Long customerId) {
         this.customerId = customerId;
     }
 
     /**
      * Get the first name.
      *
      * @return firstName
      */
     public String getFirstName() {
         return firstName;
     }
 
     /**
      * Set the first name.
      *
      * @param firstName the first name to set
      */
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
     /**
      * Get the last name.
      *
      * @return lastName
      */
     public String getLastName() {
         return lastName;
     }
 
     /**
      * Set the last name.
      *
      * @param lastName the last name to set
      */
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 
     /**
      * Get the email address.
      *
      * @return email
      */
     public String getEmail() {
         return email;
     }
 
     /**
      * Set the email address.
      *
      * @param email the email address to set
      */
     public void setEmail(String email) {
         this.email = email;
     }
 
     /**
      * Get a list of addresses.
      *
      * @return addressList
      */
     public List<Address> getAddressList() {
         if (addressList == null) {
             addressList = new ArrayList<Address>();
         }
         return addressList;
     }
 
     @Override
     public String toString() {
         return MessageFormat.format("[CustomerId: {0}, Firstname: {1}, Lastname: {2}, Username: {3}, Email: {4}}", customerId, firstName, lastName, username, email);
     }
 }
