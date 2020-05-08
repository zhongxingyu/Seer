 package net.sf.laja.example.person.behaviour;
 
 import net.sf.laja.example.person.state.PersonState;
 
 public class Person extends PersonFactory {
     private final BodyMassIndex bmi;
 
     public Person(PersonState state) {
         super(state);
        bmi = new BodyMassIndex(state.getBmiState());
     }
 
     public double calculateBmi() {
         return bmi.calculateBmi();
     }
 
     public boolean hasNormalWeight() {
         return bmi.hasNormalWeight();
     }
 
     // (factory)
     public Person asPerson() {
         return new Person(state);
     }
 }
