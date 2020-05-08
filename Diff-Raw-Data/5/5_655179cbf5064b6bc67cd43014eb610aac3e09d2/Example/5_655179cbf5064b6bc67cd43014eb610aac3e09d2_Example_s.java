 package aima.core.learning.framework;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 /**
  * Represents a learning example; according to page 699, in a Boolean decision 
  * tree it is the pair (x, y), where "x is a vector of values for the input 
  * attributes and y is a single boolean output value". This definition has been
  * abstracted to produce this generic class.
  * @author Ravi Mohan
  * @author Andrew Brown
  */
 public class Example<E> {
 
     /**
      * The input attributes for this example
      */
     ArrayList<Attribute> inputAttributes = new ArrayList<Attribute>();
     /**
      * The output value for this example
      */
     E outputValue;
 
     /**
      * Constructor
      */
     public Example() {
     }
 
     /**
      * Constructor
      * @param inputAttributes
      * @param outputValue
      */
     public Example(ArrayList<Attribute> inputAttributes, E outputValue) {
         this.inputAttributes = inputAttributes;
         this.outputValue = outputValue;
     }
 
     /**
      * Adds an attribute to this example
      * @param attribute 
      */
     public void add(Attribute attribute) {
         this.inputAttributes.add(attribute);
     }
 
     /**
      * Returns the specified attribute by name
      * @param attributeName
      * @return 
      */
     public Attribute get(String attributeName) {
         for (Attribute a : inputAttributes) {
             if (a.getName().equals(attributeName)) {
                 return a;
             }
         }
         throw new RuntimeException("Could not find attribute: " + attributeName);
     }
 
     /**
      * Returns the output value
      * @return 
      */
     public E getOutput() {
         return this.outputValue;
     }
    
     /**
      * Returns the list of attributes
      * @return 
      */
    public Attribute[] getAttributes(){
         return this.inputAttributes.toArray(new Attribute[0]);
     }
 
     /**
      * Returns string representation
      * @return 
      */
     @Override
     public String toString() {
         StringBuilder s = new StringBuilder();
         s.append(inputAttributes.toString());
         s.append(" --> ");
         s.append(outputValue);
         return s.toString();
     }
 
     /**
      * Overrides equality test
      * @param o
      * @return 
      */
     @Override
     public boolean equals(Object o) {
         if (this == o) {
             return true;
         }
         if ((o == null) || (this.getClass() != o.getClass())) {
             return false;
         }
         Example other = (Example) o;
         return inputAttributes.equals(other.inputAttributes);
     }
 
     /**
      * For use with equals()
      * @return 
      */
     @Override
     public int hashCode() {
         return inputAttributes.hashCode();
     }
 }
