 package myconcrete;
 
 /**
  *
  * @author Stuart - HP AMD 10
  */
 public class ConcreteTestStub {
     
    public static void main (String[] args) {
         
         // Test the concrete Employee
         Employee employee1 = new Employee("12345", "John Doe", 20130126);
         System.out.println(employee1.toString());
         
         
         // Test the concrete HourlyEmployee
         HourlyEmployee employee2 = new HourlyEmployee("22222", "Jane Doe", 
                 20130126);
         employee2.setHourlyPayRate(25.00);
         System.out.println(employee2.toString());
         
         
         // Test the concrete SalariedEmployee
         SalariedEmployee employee3 = new SalariedEmployee("33333", "John Smith",
                 20130126);
         employee3.setYearlySalary(75000.00);
         System.out.println(employee3.toString());
         
         
         // Test the concrete Employee
         SalaryPlusBonusEmployee employee4 = new SalaryPlusBonusEmployee(
                 "44444", "Jane Smith", 20130126);
         employee4.setEmpBonus(5000.00);
         System.out.println(employee4.toString());
     }
 }
