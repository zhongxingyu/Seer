 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package myinterface;
 
 /**
  *
  * @author Stuart - HP AMD 10
  */
 public class InterfaceTestStub {
     
    //public static void main (String[] args) {
         
         // Test the interface Employee
         // myinterface.Employee employee1 = new myinterface.Employee("12345", "John Doe", 20130126);
         // System.out.println(employee1.toString());
         
         
         // Test the interface HourlyEmployee
         myinterface.HourlyEmployee employee2 = new myinterface.HourlyEmployee("22222", "Jane Doe", 
                 20130126);
         employee2.setHourlyPayRate(25.00);
         System.out.println(employee2.toString());
         
         
         // Test the interface SalariedEmployee
         myinterface.SalariedEmployee employee3 = new myinterface.SalariedEmployee("33333", "John Smith",
                 20130126);
         employee3.setYearlySalary(75000.00);
         System.out.println(employee3.toString());
         
         
         // Test the interface Employee
         myinterface.SalaryPlusBonusEmployee employee4 = new myinterface.SalaryPlusBonusEmployee(
                 "44444", "Jane Smith", 20130126);
         employee4.setEmpBonus(5000.00);
         System.out.println(employee4.toString());
     }
 }
