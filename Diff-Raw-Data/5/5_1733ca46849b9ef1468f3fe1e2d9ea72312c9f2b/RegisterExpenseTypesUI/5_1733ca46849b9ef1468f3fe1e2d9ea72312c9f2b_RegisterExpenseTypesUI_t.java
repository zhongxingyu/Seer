 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Presentation;
 
 import Controllers.ExpenseRegisterController;
 import Model.ExpenseType;
 import eapli.util.Console;
 import java.math.BigDecimal;
 import java.util.Date;
 
 /**
  *
  * @author Emanuel
  */
 public class RegisterExpenseTypesUI {
     
     public void mainLoop() {
         System.out.println("* * *  REGISTER AN EXPENSE WITH EXPENSE TYPE  * * *\n");
         
         String what = Console.readLine("Description:");
         Date date = Console.readDate("When:");
         double value = Console.readDouble("Amount:");
         BigDecimal amount = new BigDecimal(value);
        //ExpenseType type = Console.readLine("Type");
         
         ExpenseRegisterController controller = new ExpenseRegisterController();
        //controller.registerExpenseWithType(what, date, amount,type);
         
         System.out.println("expense with expense type recorded.");
     }
     
     
 }
