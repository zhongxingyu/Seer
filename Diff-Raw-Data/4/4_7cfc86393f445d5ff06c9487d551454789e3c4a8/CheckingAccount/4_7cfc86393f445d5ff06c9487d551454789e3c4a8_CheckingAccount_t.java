 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Model;
 
 import java.util.ArrayList;
import java.util.List;
 
 /**
  *
  * @author Filipe
  */
 public class CheckingAccount extends Account {
 
     List<Expense> listExpense;
 
     CheckingAccount(String ownerName, int id) {
         this.nomeOwner = ownerName;
         this.nConta = id;
         listExpense = new ArrayList<Expense>();
         listIncome = new ArrayList<Income>();
 
     }
 }
