 /**
  * @author Ory Band
  * @version 1.0
  */
 public class ArrayAssignments implements Assignments {
     private Assignment[] assignments;
 
     /** @return new ArrayAssignment object with no assignments. */
     public ArrayAssignments() {
         this.assignments = new Assignment[0];
     }
 
     /**
      * @param assignments Assignment array.
      *
      * @return new ArrayAssignment object with initial assignments given as argument.
      */
     public ArrayAssignments(Assignment[] s) {
         // Basic validity tests.
         if (s == null) {
             throw new RuntimeException("Assignment[] argument is null.");
         } else if (s.length == 0) {
             throw new RuntimeException("Assignment[] argument is of length 0!");
         }
 
         // Test for valid assignments.
         for (int i=0; i<s.length; i++) {
             if (s[i] == null) {
                 throw new RuntimeException("Assignment[" + i + "] is null.");
             }
 
             // Test for duplicate variable names inside assignments.
             for (int j=i+1; j<s.length; j++) {
                if (s[j] == null) {
                    throw new RuntimeException("Assignment[" + i + "] is null.");
                } else if (s[j].getVar().getName() == s[i].getVar().getName()) {
                     throw new RuntimeException("Assigment[" + i + "] and Assignment[" + j + "] " +
                                                "are using the same variable '" +
                                                s[i].getVar().getName() + " = " + s[i].getValue());
                 }
             }
         }
 
         this.assignments = s;  // Shallow copy according to FAQ page.
 
         // Deep copy assignments.
         /*this.assignments = new Assignment[s.length];
 
           for (i=0; i < s.length; i++) {
           for (j=i+1; j<s.length;
           this.assignments[i] = s[i];
           }*/
     }
 
     public double valueOf(Variable v) {
         // Validate argument.
         if (v == null) {
             throw new RuntimeException("Variable argument is null.");
         }
 
         // Search for v in the assignments list.
         for (int i=0; i < this.assignments.length; i++) {
             if (this.assignments[i].getVar().equals(v)) {
                 return this.assignments[i].getValue();
             }
         }
 
         // If v wasn't found, throw exception.
         throw new RuntimeException("Variable " + v.getName() + " has no valid assignment!");
     }
 
     public void addAssignment(Assignment a) {
         if (a == null) {
             throw new RuntimeException("Assignment argument is null.");
         }
 
         // Update assignment if it's already in the assignments' list.
         int i;
         for (i=0; i < this.assignments.length; i++) {
             if (this.assignments[i].getVar().getName() == (a.getVar().getName())) {
                 this.assignments[i].setValue(a.getValue());
 
                 return; // No need to extend the assignments list if we only needed to update one of its variables.
             }
         }
 
         // Creates a new list, and add a single new assignment to it.
         Assignment[] copy = new Assignment[assignments.length +1];
 
         // Copies the old assignments.
         for (i=0; i < this.assignments.length; i++) {
             copy[i] = this.assignments[i];
         }
 
         // Add the new assignment to the end of the list.
         copy[copy.length -1] = a;
 
         // Replace the old assignments list with new one.
         this.assignments = copy;
     }
 }
 
