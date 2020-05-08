 package BackEnd.ManagerSystem;
 
 import BackEnd.EventSystem.Budget;
 import BackEnd.EventSystem.Committee;
 import BackEnd.EventSystem.Event;
 import BackEnd.EventSystem.Expense;
 import BackEnd.EventSystem.Income;
 import BackEnd.UserSystem.User;
 import EMS_Database.DoesNotExistException;
 import EMS_Database.DuplicateInsertionException;
 import EMS_Database.InputExpense;
 import EMS_Database.InputIncome;
 import EMS_Database.impl.Committees_Table;
 import EMS_Database.impl.Expense_Table;
 import EMS_Database.impl.Income_Table;
 import java.util.ArrayList;
 
 /**
  * This class serves as a liaison between the GUI and the back end and the data.
  * It checks to see whether a user has the proper privileges to change
  * something, and if the user does, then edits the database accordingly. It also
  * provides ready-to-use methods for the GUI to call.
  *
  * @author Julian Kuk
  */
 public class BudgetManager {
 
     private Committees_Table committeesTable;
     private Income_Table incomeTable;
     private Expense_Table expenseTable;
     private Budget selectedBudget;
 
     /**
      * Default, no arg constructor. Builds the budget list.
      */
     public BudgetManager(Committees_Table committeesTable, Income_Table incomeTable, Expense_Table expenseTable) {
         this.committeesTable = committeesTable;
         this.incomeTable = incomeTable;
         this.expenseTable = expenseTable;
     }
 
     /**
      * Store the budget selected by the user.
      *
      * @param budget the budget that is selected by the user
      */
     public void setSelectedBudget(Budget selectedBudget) {
         this.selectedBudget = selectedBudget;
     }
 
     /**
      * Retrieve the budget selected by the user
      *
      * @return the budget selected by the user
      */
     public Budget getSelectedBudget() {
         return selectedBudget;
     }
 
     /**
      * Add an income item to the selected budget. Verifies the user's privilege
      * level first, then edits the database if privileged.
      *
      * @param income
      * @param loggedInUser
      * @param selectedEvent
      * @param selectedCommittee
      */
     
     public Income createIncome(Income income, User loggedInUser, Event selectedEvent, Committee selectedCommittee)
             throws PrivilegeInsufficientException, DoesNotExistException, DuplicateInsertionException {
         
         Income newIncome = null;
         if (PrivilegeManager.hasBudgetPrivilege(loggedInUser, selectedEvent, selectedCommittee)) {          
             newIncome = new Income(incomeTable.insertBudgetItem(new InputIncome(
                     income.getDescription(), income.getDate(), income.getValue()))
                     , income);
             selectedBudget.getIncomeList().add(newIncome);
             ArrayList<Integer> newIncomeIDList = committeesTable.getIncome(selectedCommittee.getCOMMITTEE_ID());
             newIncomeIDList.add(newIncome.getBUDGET_ITEM_ID());
             committeesTable.setIncome(selectedCommittee.getCOMMITTEE_ID(), newIncomeIDList);
         }
         return newIncome;
     }
 
     /**
      * Remove an income item from the selected budget. Verifies the user's
      * privilege level first, then edits the database if privileged.
      *
      * @param income
      * @param loggedInUser
      * @param selectedEvent
      * @param selectedCommittee
      */
     public void deleteIncome(Income income, User loggedInUser, Event selectedEvent, Committee selectedCommittee)
             throws PrivilegeInsufficientException, DoesNotExistException {
         
         if (PrivilegeManager.hasBudgetPrivilege(loggedInUser, selectedEvent, selectedCommittee)) {
             selectedBudget.getIncomeList().remove(income);
             ArrayList<Integer> newIncomeIDList = committeesTable.getIncome(selectedCommittee.getCOMMITTEE_ID());
            newIncomeIDList.remove(income.getBUDGET_ITEM_ID());
             committeesTable.setIncome(selectedCommittee.getCOMMITTEE_ID(), newIncomeIDList);
             incomeTable.removeBudgetItem(income.getBUDGET_ITEM_ID());
         }
     }
 
     /**
      * Add an expense to the selected budget. Verifies the user's privilege
      * level first, then edits the database if privileged.
      *
      * @param expense the expense item to add
      * @param loggedInUser the currently logged in user
      * @param selectedEvent the selected event
      * @param selectedCommittee the selected committee
      */
     public Expense createExpense(Expense expense, User loggedInUser, Event selectedEvent, Committee selectedCommittee)
             throws PrivilegeInsufficientException, DoesNotExistException, DuplicateInsertionException {
         
         Expense newExpense = null;
         if (PrivilegeManager.hasBudgetPrivilege(loggedInUser, selectedEvent, selectedCommittee)) {
             newExpense = new Expense(expenseTable.insertBudgetItem(new InputExpense(
                     expense.getDescription(), expense.getDate(), expense.getValue()))
                     , expense);
             selectedBudget.getExpenseList().add(newExpense);
             ArrayList<Integer> newExpenseIDList = committeesTable.getExpense(selectedCommittee.getCOMMITTEE_ID());
             newExpenseIDList.add(newExpense.getBUDGET_ITEM_ID());
             committeesTable.setIncome(selectedCommittee.getCOMMITTEE_ID(), newExpenseIDList);
         }
         return newExpense;
     }
 
     /**
      * Remove an expense item from the selected budget. Verifies the user's
      * privilege level first, then edits the database if privileged.
      *
      * @param expense
      * @param loggedInUser
      * @param selectedEvent
      * @param selectedCommittee
      */
     public void deleteExpense(Expense expense, User loggedInUser, Event selectedEvent, Committee selectedCommittee)
             throws PrivilegeInsufficientException, DoesNotExistException {
         
         if (PrivilegeManager.hasBudgetPrivilege(loggedInUser, selectedEvent, selectedCommittee)) {
             selectedBudget.getExpenseList().remove(expense);
             ArrayList<Integer> newExpenseIDList = committeesTable.getExpense(selectedCommittee.getCOMMITTEE_ID());
             newExpenseIDList.remove(expense.getBUDGET_ITEM_ID());
             committeesTable.setExpense(selectedCommittee.getCOMMITTEE_ID(), newExpenseIDList);
             expenseTable.removeBudgetItem(expense.getBUDGET_ITEM_ID());
         }
     }
 }
