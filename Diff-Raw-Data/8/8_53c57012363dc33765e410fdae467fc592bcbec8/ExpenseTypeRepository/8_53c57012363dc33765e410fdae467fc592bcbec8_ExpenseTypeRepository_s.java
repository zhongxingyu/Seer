 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Persistence;
 
 import Model.ExpenseType;
 import Model.ExpenseType;
import Persistence.IExpenseTypeRepository;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author Emanuel
  */
 public class ExpenseTypeRepository implements IExpenseTypeRepository {
     
     private static List<ExpenseType> listExpenseType= new ArrayList<ExpenseType>();
 
     public ExpenseTypeRepository() {}
     
     public void save(ExpenseType exp)
     {
         if (exp==null) throw new IllegalArgumentException();
         listExpenseType.add(exp);
         
     }
     
     public List ListExpenseTypes(){
         
     //   for(Object o : listExpenseType){
      //       System.out.println(o+""+((ExpenseTypeRepository)o));
     //            
      //   }
       return listExpenseType;
     }
     
        public ExpenseType FindExpenseTypes(String nome) {
         
             for(ExpenseType o : listExpenseType){
                 if(o.getType().equals(nome)){
                     return o;
                 }  
             }
             return null;
         }
     
     /**
      * @autor nbentoneves
      * 
      * @return Lista completa dos tipode de despesa
      */
     public List<ExpenseType> getAllExpenseType(){
         
         return listExpenseType;
     }
     
 }
