 
 package pt.uac.cafeteria.model;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import pt.uac.cafeteria.model.Debit.MealType;
 
 /**
  * 
  * Represents the student account
  * 
  */
 public class Account {
     
     /** Constant with maximum value to generate a number */
     private static final int MAX_VALUE = 9999;
     
     /** Constant with minimum value to generate a number*/
     private static final int MIN_VALUE = 1000;
     
     /** Enumerated type with the status of the account */
     public enum Status {
         ACTIVE { @Override public String toString() { return "Active"; } },
         BLOCKED { @Override public String toString() { return "Blocked"; } },
         CLOSED { @Override public String toString() { return "Closed"; } }
     }
     
     /** Identification number of the account */
     private int number;
     
     /**  Access code of the account */
     private int pinCode;
     
     /** Balance of the account */
     private double balance;
     
     /** State of the account */
     private Status status;
     
     /** Student of the account */
     private Student student;
     
     /** List with the transactions of the account */
     private List<Transaction> transactions;
     
     /** Recorded number of failed attempts at logging in */
     private int failedAttempts;
 
     /**
      * Default Constructor
      *    
      * @param student   student that will manage the account
      */
     public Account(Student student) {
         this.number = student.getId();
         this.pinCode = randomNumber();
         this.balance = 5.0;
         this.status = Status.ACTIVE;
         this.student = student;
         this.transactions = new ArrayList();
     }
 
     
     /** Returns the account number */
     public int getNumber() {
         return number;
     }
 
    /** Returns the pin code */
    public int getPinCode() {
        return pinCode;
    }
    
     /** 
      * Changes the pin code
      * 
      * @param pinCode   the pin code that will be defined
      */
     public void setPinCode(int pinCode) {
         this.pinCode = pinCode;
     }
     
     /** Returns the balance of the account */
     public double getBalance() {
         return balance;
     }
 
     /**
      * Changes the balance
      * 
      * @param balance   the balance that will be defined
      */
     public void setBalance(double balance) {
         this.balance = balance;
     }
     
     /** Returns the state of the account */
     public Status getStatus() {
         return status;
     }
     
     /** Checks if account is active */
     public boolean isActive() {
         return status == Status.ACTIVE;
     }
     
     /** Checks if account is blocked */
     public boolean isBlocked() {
         return status == Status.BLOCKED;
     }
     
     /** Checks if account is closed */
     public boolean isClosed() {
         return status == Status.CLOSED;
     }
     
     /**
      * Autenticate account
      * 
      * Gets blocked after three failed attempts
      * 
      * @param pinCode  Account pin code
      * @return  
      */
     public boolean authenticate(int pinCode) {
         if (this.pinCode == pinCode) {
             failedAttempts = 0;
             return true;
         }
         if (++failedAttempts == 3) {
             block();
         }
         return false;
     }
     
     /**
      * Changes the state.
      * 
      * @param status    the status that will be defined
      */
     public void setStatus(Status status) {
         this.status = status;
     }
 
     /** Returns a Student of the account */
     public Student getStudent() {
         return student;
     }
 
     /** Returns a transaction list of the account */
     public List<Transaction> getTransactions() {
         return transactions;
     }
     
     /** Returns a string that describe the account */
     @Override
     public String toString(){
         return "\nNumber: " + this.number +
                 "\nPin Code: " + this.pinCode +
                 "\nBalance: " + this.balance +
                 "\nStatus: " + this.status +
                 "\nStudent: " + this.student.getName() +
                 "\nTransactions: " + this.transactions;
     }
     
     /**
      * Method responsible for making a deposit and adding it into the transactions list
      * 
      * @param amount    the amount that is deposit into the account
      * @param administrator the administrator that does the deposit
      */
     public void deposit (double amount, String administrator) {
         this.balance = balance + amount;
         this.transactions.add(new Credit(administrator, amount));
     }
     
     /**
      * Method responsible for making a payment and adding it into the transactions list
      * 
      * @param amount    the amount of the payment
      * @param date  the date of the meal
      * @param meal  the type of meal: dinner or lunch
      * @throws Exception    exception that leads with not enough credit
      */
     public void payment (double amount, Calendar date, MealType meal) throws Exception {
         if (amount <= this.balance) {
             this.balance = balance - amount;
             this.transactions.add(new Debit(date, meal));
         }
         else {
             throw new Exception ("Not allowed. Your account doesn't have enough credit");
         }
         
     }
     
     /**
      * Method responsible for recovering the active state of the account
      * 
      * @param pinCode   the pin code of a student
      * @throws Exception    exceptions that leads with account not being blocked or having an invalid pin code
      */
     public void recoverAccountState(int pinCode) throws Exception {
         if (this.pinCode == pinCode) {
             if (this.getStatus() == Status.BLOCKED) {
                 setStatus(Status.ACTIVE);
             }
             else {
                 throw new Exception ("Account not blocked");
             }
             
         }
         else {
             throw new Exception ("Invalid pin code");
         }
         
     }
     
     
     /**
      * 
      * Method responsible for recovering the pin code of an account
      * 
      * @param id    the id of a student, to check if he's the real pin code owner
      * @return  the pin code of the account
      * @throws Exception    exception that leads with invalid id
      */
     public int recoverAccountPinCode(int id) throws Exception {
         if (this.student.getId() == id) {
             return this.pinCode;
         }
         throw new Exception ("Invalid student identification");
     }
     
     /** Sets the BLOCKED state of the account */
     public void block() {
         this.setStatus(Status.BLOCKED);
     }
     
     /** Generates a number between 1000 and 9999 */
     private static int randomNumber() {
         return MIN_VALUE + (int)(Math.random() * (MAX_VALUE - MIN_VALUE));
     }
 }
