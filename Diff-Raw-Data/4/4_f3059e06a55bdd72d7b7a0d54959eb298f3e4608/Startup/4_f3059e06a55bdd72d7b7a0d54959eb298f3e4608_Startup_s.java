 package dip.lab1.instructor.solution1;
 
 import java.text.NumberFormat;
 
 /**
  * Just a start and test class for this program.
  *
  * @author jlombardo
  */
 public class Startup {
 
     public static void main(String[] args) {
         /*
          * We'll just use this class for testing our ccde.
          * We'll provide input and get some output...
          */
 
         //Low-level modules
         HourlyEmployee emp1 = new HourlyEmployee(10.50, 2020);
         SalariedEmployee emp2 = new SalariedEmployee(45000, 1250);
         SalariedEmployee emp3 = new SalariedEmployee(90000,0);
         
         // Create a collection that we can process as a group --
         // demands polymorphic behavior
         Employee[] employees = {emp1, emp2, emp3};
 
         // High-level module
         HRService hr = new HRService();
 
         // Just utility code to format numbers nice.
         NumberFormat nf = NumberFormat.getCurrencyInstance();
 
         // Test input/output by looping over collection in a way that
         // doesn't break if we add/subtract employees from array
         for(int i=0; i < employees.length; i++) {
            System.out.println("Employee " + (i+1) + " compensation:");
            hr.getAnnualCompensationForEmployee(employees[i]);
         }
         
         // Or, we could do this...but this is fragile
         System.out.println("Employee 1 annual compensation: " +
             nf.format(hr.getAnnualCompensationForEmployee(emp1)));
         System.out.println("Employee 2 annual compensation: " +
             nf.format(hr.getAnnualCompensationForEmployee(emp2)));
         System.out.println("Employee 3 annual compensation: " +
             nf.format(hr.getAnnualCompensationForEmployee(emp3)));
         
     }
 
 }
