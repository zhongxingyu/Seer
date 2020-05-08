 package org.interactive.debts.calculator;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 /**
  * Person class
  *
  * @author bogdan.solga
  *
  * Date: 19.08.2013, time: 16:42
  */
 public class Person {
 
     private String name;
 
     private Map<Person, Double> debts;
 
     private List<Payment> payments;
 
     public Person(String name) {
         this.name = name;
 
         debts = new ConcurrentHashMap<>(0);
         payments = new ArrayList<>(0);
     }
 
     public String getName() {
         return name;
     }
 
     public Map<Person, Double> getDebts() {
         return debts;
     }
 
     public void addPayment(Payment payment) {
         payments.add(payment);
     }
 
     public void addDebt(Person person, Double amount) {
         if (person.equals(this)) {
             throw new RuntimeException("Nu se pot adauga datorii catre sine");
         }
 
         Double debtForPerson = 0.0;
         if (debts.containsKey(person)) {
             debtForPerson = debts.get(person);
         }
 
         debtForPerson += amount;
 
         //System.out.println("\t" + name + " are datorie " + debtForPerson + " catre " + person);
         debts.put(person, debtForPerson);
     }
 
     public void setDebt(Person person, Double amount) {
         debts.put(person, amount);
     }
 
     public Double getTotalDebts() {
         Double total = new Double(0);
 
         for (Double debt : debts.values()) {
             total += debt;
         }
 
         return total;
     }
 
     public Double getTotalPayments() {
         Double total = new Double(0);
 
         for (Payment payment : payments) {
             total += payment.getAmount();
         }
 
         return total;
     }
 
     public void displayPayments() {
        if (payments.size() > 0) {
            System.out.println(name + " a facut " + payments.size() + " plati:");
            for (Payment payment : payments) {
                System.out.println("\t - " + payment.getAmount() + " pentru " + payment.getDescription());
             }
         } else {
             System.out.println(name + " nu a facut nici o plata");
         }
     }
 
     public void displayDebts() {
         final int howMany = debts.size();
         if (howMany > 0) {
             String text;
             if (howMany == 1) {
                 text = "o datorie";
                 final Person person = debts.keySet().iterator().next();
                 System.out.println(name + " are " + text + " catre " + person.getName() + " - " + debts.get(person));
             } else {
                 text = howMany + " datorii:";
                 System.out.println(name + " are " + text);
 
                 for (Person person : debts.keySet()) {
                     System.out.println("\t - " + debts.get(person) + " catre " + person.getName());
                 }
             }
         } else {
             System.out.println(name + " nu are datorii de platit");
         }
     }
 
     @Override
     public String toString() {
         return name;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Person person = (Person) o;
 
         if (!name.equals(person.name)) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         return name.hashCode();
     }
 
     public void removedDebt(Person person) {
         if (debts.containsKey(person)) {
             debts.remove(person);
         }
     }
 }
