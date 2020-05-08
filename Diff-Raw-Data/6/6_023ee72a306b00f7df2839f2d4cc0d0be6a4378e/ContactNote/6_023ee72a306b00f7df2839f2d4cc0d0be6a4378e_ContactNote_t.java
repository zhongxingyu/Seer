 package no.dusken.annonseweb.models;
 
 import no.dusken.common.model.DuskenObject;
 
 import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
 import javax.persistence.ManyToOne;
 
 import static javax.persistence.CascadeType.ALL;
 
 enum ContactMethod{
     EMAIL,
     TLFNUMBER,
     INPERSON,
     OTHER
 }
 
 @Entity
 public class ContactNote extends DuskenObject{
 
     @ManyToOne(cascade = ALL)
     private Customer customer;
 
     @ManyToOne(cascade = ALL)
     private ContactPerson contactPerson;
 
     //private String method; //email,telephonenumber or in person
    @Enumerated(EnumType.STRING)
     private ContactMethod method;

     private String text;
 
     public ContactNote() {}
 
     public ContactNote(String text, ContactMethod method, ContactPerson contactPerson, Customer customer) {
         this.text = text;
         this.method = method;
         this.contactPerson = contactPerson;
         this.customer = customer;
     }
 
     public Customer getCustomer() {
         return customer;
     }
 
     public void setCustomer(Customer customer) {
         this.customer = customer;
     }
 
     public ContactPerson getContactPerson() {
         return contactPerson;
     }
 
     public void setContactPerson(ContactPerson contactPerson) {
         this.contactPerson = contactPerson;
     }
 
     public ContactMethod getMethod() {
         return this.method;
     }
 
     public void setMethod(ContactMethod method) {
         this.method = method;
     }
 
     public String getText() {
         return text;
     }
 
     public void setText(String text) {
         this.text = text;
     }
 }
 
 
