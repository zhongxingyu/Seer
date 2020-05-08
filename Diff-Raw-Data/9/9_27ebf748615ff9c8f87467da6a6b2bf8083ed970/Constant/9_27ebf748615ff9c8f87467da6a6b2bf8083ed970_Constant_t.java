 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package uk.co.mtford.abduction.logic;
 
 import java.util.Map;
 
 /**
  *
  * @author mtford
  */
 public class Constant implements Term {
     private String value;
 
     public Constant(String value) {
         this.value = value;
     }
 
     public String getValue() {
         return value;
     }
 
     public void setValue(String value) {
         this.value = value;
     }
     
 
     @Override
     public String toString() {
         return value;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
        if (obj instanceof Variable) { // Special case whereby variables have been assigned a constant value.
             Variable var = (Variable)obj;
            IUnifiable varValue = var.getValue();
            if (varValue instanceof Constant) {
                return this.equals(varValue);
             }
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final Constant other = (Constant) obj;
         if ((this.value == null) ? (other.value != null) : !this.value.equals(other.value)) {
             return false;
         }
         return true;
     }
 
     @Override
     public int hashCode() {
         int hash = 3;
         hash = 79 * hash + (this.value != null ? this.value.hashCode() : 0);
         return hash;
     }
     
     @Override
     public Object clone(){
         return new Constant(value);
     }
     
 }
