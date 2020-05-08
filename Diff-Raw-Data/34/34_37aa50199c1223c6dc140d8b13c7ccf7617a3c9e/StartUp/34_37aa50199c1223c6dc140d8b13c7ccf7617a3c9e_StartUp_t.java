 
 package myconcrete;
 import java.util.Scanner;
 
 /**
  *
  * @author Michael Grinker
  */
 public class StartUp {
     
     public static void main(String[] args) {
         String fName,
                 lName;
         String empID;
         int hours;
        double hrRate,
                salary,
                bonus;
         
         //Create scanner object
         Scanner keyboard = new Scanner(System.in);
         
         //Create HourlyEmployee object
         //Get info & display
         
         System.out.println("You are going to enter the info for an hourly employee");
         System.out.print("What is the first name of the employee? ");
         fName = keyboard.nextLine();
         System.out.print("What is the last name of the employee? ");
         lName = keyboard.nextLine();
         System.out.print("What is the employee ID? ");
         empID = keyboard.nextLine();
         System.out.print("How many hours did the employee work? ");
         hours = keyboard.nextInt();
        System.out.print("What is the hourly rate for this employee? ");
         hrRate = keyboard.nextDouble();
        System.out.println();
         
         HourlyEmployee emp1 = new HourlyEmployee(hrRate, fName, lName, hours, empID);
         emp1.pay();
         System.out.println(emp1);
         
        //Create SalariedEmployee object
        //Get info and display
         
        System.out.println();
        keyboard.nextLine();
        System.out.println("You are going to enter the info for a salaried employee");
        System.out.print("What is the first name of the employee? ");
        fName = keyboard.nextLine();
        System.out.print("What is the last name of the employee? ");
        lName = keyboard.nextLine();
        System.out.print("What is the employee ID? ");
        empID = keyboard.nextLine();
        System.out.print("How many hours did the employee work? ");
        hours = keyboard.nextInt();
        System.out.print("What is the annual salary for this employee? ");
        salary = keyboard.nextDouble();
        System.out.println();
        SalariedEmployee sal_1 = new SalariedEmployee(salary,fName, lName, hours,
                empID);
        System.out.println(sal_1);
        System.out.println("Annual salary: " + sal_1.getAnnualSalary());
        System.out.println();
 
     }
 }
