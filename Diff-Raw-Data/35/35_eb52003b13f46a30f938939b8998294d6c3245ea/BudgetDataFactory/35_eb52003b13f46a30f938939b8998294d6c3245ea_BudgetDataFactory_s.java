 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package EMS_Database;
 
 import EMS_Database.impl.DB_BudgetData;
 import EMS_Database.impl.Dummy_BudgetData;
 
 /**
  *
  * @author mike
  */
 public class BudgetDataFactory {
 
     public static Interface_BudgetData create(String name) {
        switch (name) {
            case "db":
                return new DB_BudgetData();
            default:
                return new Dummy_BudgetData();



         }
 
     }
 }
