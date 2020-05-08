 package org.interactive.debts.calculator;
 
 import java.util.*;
 
 /**
  * PaymentsHandler class
  *
  * @author bogdan.solga
  *
  * Date: 20.08.2013, time: 08:47
  */
 public class PaymentsHandler {
 
     private static final Boolean DEBUG = Boolean.FALSE;
 
     private Set<Person> persons;
 
     public PaymentsHandler(Set<Person> persons) {
         this.persons = persons;
     }
 
     public void addPayment(Person payer, Payment payment) {
         final String id = "[" + payment.getId() + "] ";
 
         if (DEBUG) {
             System.out.println(id + payer + " a platit " + payment.getAmount() + " pentru " + payment.getDescription()
                     + ", ar trebui sa primeasca cate " + (payment.getAmount() / persons.size()) + " de la fiecare");
         }
 
         Integer numberOfPersons = persons.size();
 
         Double amount = payment.getAmount();
         Double amountPerPerson = amount / numberOfPersons;
 
         for (Person person : persons) {
             //System.out.println("payer: " + payer + ", person: " + person);
             if (!person.equals(payer)) {
                //System.out.println("\t\t" + id + "adaugam o datorie de " + amountPerPerson + " de la " + person + " catre " + payer);
                 person.addDebt(payer, amountPerPerson);
             } else {
                 person.addPayment(payment);
             }
         }
 
         // scade din datoriile celui care a facut plata catre ceilalti
         subtractAmountFromDebts(payment.getId(), payer, amountPerPerson);
     }
 
     public void subtractAmountFromDebts(Integer id, Person payer, /*Person receiver, */ Double amountPerPerson) {
         final String paymentId = "[" + id + "] ";
 
         Map<Person, Double> personsDebts = payer.getDebts();
 
         // daca platitorul are datorii catre ceilalti - scadem din datoriile platitorului catre ceilalti
         if (personsDebts.size() > 0) {
             if (DEBUG) {
                 System.out.println(paymentId + payer + " are datorii catre " + personsDebts.size() + " persoane");
             }
 
             Double amount, remaining;
             for (Person person : personsDebts.keySet()) {
                 if (!person.equals(payer)) {
                     amount = personsDebts.get(person);
                     if (DEBUG) {
                         System.out.println("\t" + paymentId + amount + " catre " + person.getName());
                        System.out.println("\t\t" + paymentId + "Scadem " + amountPerPerson + " din datoriile lui " + payer + " catre " + person);
                     }
 
                     remaining = amount - amountPerPerson;
 
                     if (remaining > 0) {
                         if (DEBUG) {
                             System.out.println("\t\t" + paymentId + payer + " trebuie sa-i mai dea " + remaining + " lui " + person);
                         }
 
                        payer.addDebt(person, remaining);
                     } else {
                         if (remaining < 0) {
                             remaining = Math.abs(remaining);
 
                             if (DEBUG) {
                                 System.out.println("\t\t" + paymentId + payer + " nu trebuie sa-i mai dea nimic lui " +
                                         person + ", " + person + " trebuie sa-i dea " + remaining + " lui " + payer);
                             }
 
                             person.setDebt(payer, remaining);
                             payer.removedDebt(person);
                         } else {
                             if (DEBUG) {
                                 System.out.println("\t\t" + paymentId + payer + " nu mai are datorii catre " + person);
                             }
                             payer.removedDebt(person);
                             person.removedDebt(payer);
                         }
                     }
                 }
             }
         } else {
             //System.out.println(paymentId + payerName + " nu are datorii");
         }
     }
 
     public void displayDebts() {
         for (Person person : persons) {
             person.displayDebts();
         }
     }
 
     public void displayPayments() {
         for (Person person : persons) {
             person.displayPayments();
             //System.out.println(person + " ar trebui sa primeasca cate " + (person.getTotalPayments() / persons.size()) + " de la fiecare");
         }
     }
 }
