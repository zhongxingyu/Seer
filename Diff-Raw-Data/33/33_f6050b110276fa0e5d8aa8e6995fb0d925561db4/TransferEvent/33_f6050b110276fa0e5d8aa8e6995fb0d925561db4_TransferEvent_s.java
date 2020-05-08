 package com.galineer.suzy.accountsim.framework;
 
 import org.joda.money.CurrencyMismatchException;
 import org.joda.money.Money;
 
 public class TransferEvent implements AccountEvent {
   private Account sourceAccount;
   private Account targetAccount;
   private Money amount;
 
   public TransferEvent(
       Account sourceAccount, Account targetAccount, Money amount)
       throws NullPointerException, IllegalArgumentException,
              CurrencyMismatchException {
 
     if (sourceAccount == null && targetAccount == null) {
       throw new NullPointerException(
           "At least one of source account and target account must not be null");
     }
     if (amount == null) {
       throw new NullPointerException("Payment amount must not be null");
     }
     if (amount.isNegative()) {
       throw new IllegalArgumentException("Payment amount must not be negative");
     }
     if (sourceAccount != null &&
         !sourceAccount.getCurrency().equals(amount.getCurrencyUnit())) {
       throw new CurrencyMismatchException(sourceAccount.getCurrency(),
                                           amount.getCurrencyUnit());
     }
     if (targetAccount != null &&
         !targetAccount.getCurrency().equals(amount.getCurrencyUnit())) {
       throw new CurrencyMismatchException(targetAccount.getCurrency(),
                                           amount.getCurrencyUnit());
     }
 
     this.sourceAccount = sourceAccount;
     this.targetAccount = targetAccount;
     this.amount = amount;
   }
 
   public void run() throws EventExecutionException {
     try {
       Money canTransfer = this.transferDryRun(this.amount);
       this.transfer(canTransfer);
 
     } catch (IllegalArgumentException cause) {
      String error = "Payment of " + this.amount.toString() + " from ";
      if (this.sourceAccount == null) {
        error += "external account";
      } else {
        error += this.sourceAccount.toString();
      }
      error += " into ";
      if (this.targetAccount == null) {
        error += "external account";
      } else {
        error += this.targetAccount.toString();
      }
      error += " failed";
      throw new EventExecutionException(error, cause);
     }
   }
 
   private Money transferDryRun(Money amount) throws IllegalArgumentException {
 
     Money canTransfer = amount;
     if (this.sourceAccount != null) {
       canTransfer = this.sourceAccount.addDryRun(canTransfer.negated())
                     .negated();
     }
     if (this.targetAccount != null) {
       canTransfer = this.targetAccount.addDryRun(canTransfer);
     }
     return canTransfer;
   }
 
   private void transfer(Money amount) throws IllegalArgumentException {
     if (this.sourceAccount != null) {
       this.sourceAccount.add(amount.negated());
     }
     if (this.targetAccount != null) {
       this.targetAccount.add(amount);
     }
   }
 }
