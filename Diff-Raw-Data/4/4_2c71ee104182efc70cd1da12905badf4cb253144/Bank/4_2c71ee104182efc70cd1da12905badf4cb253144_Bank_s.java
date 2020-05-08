 package bank;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 
 /**
  * A bank holds many accounts.
  */
 public class Bank {
     /** Accounts in this bank. */
     List<Account> accounts;
 
     /** Accounts that need processing. */
     final Queue<Account> unprocessedAccounts;
 
     /** The current year. */
     int currYear;
 
     /**
      * Creates a new banks with the given number of empty accounts.
      * @param numAccounts The number of empty accounts.
      */
     public Bank(int numAccounts) {
         // TODO To "improve" performance, spawn 2 threads to construct all the
         // accounts.  Is Account.totalAccounts() == this.accounts.size() after
         // switching to a thread model?
         accounts = new LinkedList<Account>();
         for (int i = 0; i < numAccounts; i++) {
             accounts.add(new Account(25 * i, i, i * .05));
         }
         currYear = 0;
         unprocessedAccounts = new LinkedList<Account>();
     }
 
     /**
      * Adds all the accounts to a queue to be processed.  Calls collect interest
      * on each Account and then increment the current year.
      */
     public void computeAndAddInterst() {
         // TODO Create two threads to process the accounts faster!
         unprocessedAccounts.addAll(accounts);
         while (unprocessedAccounts.peek() != null) {
             Account currAccount = unprocessedAccounts.poll();
             currAccount.collectInterest();
         }
         currYear++;
     }
 
    public void main(String[] args) {

     }
 }
