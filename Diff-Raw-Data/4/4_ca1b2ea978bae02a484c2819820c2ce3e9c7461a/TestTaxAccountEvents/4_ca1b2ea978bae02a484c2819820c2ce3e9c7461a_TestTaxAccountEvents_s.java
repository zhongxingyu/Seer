 package com.galineer.suzy.accountsim.framework;
 
 import org.junit.*;
 import static org.junit.Assert.*;
 
 import java.util.LinkedList;
 import java.util.List;
 import org.joda.money.BigMoney;
 import org.joda.money.CurrencyMismatchException;
 import org.joda.money.Money;
 
 import com.galineer.suzy.accountsim.framework.Account.Policy;
 
 public class TestTaxAccountEvents {
  List<Account> accounts;
  Account a, b, c, d, e;
 
   @Before
   public void setUp() {
     accounts = new LinkedList<Account>();
     a = new Account("A", Money.parse("AUD 0"));
     b = new Account("B", Money.parse("AUD 15"));
     c = new Account("C", Money.parse("AUD -100"));
     d = new Account("D", Money.parse("AUD 7.50"));
     e = new Account("E", Money.parse("AUD -2.50"));
 
     a.addToTaxOwing(BigMoney.parse("AUD 10.39"));
     // leave b with 0 tax owing
     c.addToTaxOwing(BigMoney.parse("AUD 50.2159"));
     d.addToTaxOwing(BigMoney.parse("AUD -3.597"));
     e.addToTaxOwing(BigMoney.parse("AUD -8.70"));
 
     accounts.add(a);
     accounts.add(b);
     accounts.add(c);
     accounts.add(d);
     accounts.add(e);
   }
 
   @After
   public void tearDown() {
   }
 
 
 
   // Calculate tax bill
 
   @Test
   public void testCalculateTaxBill()
       throws EventExecutionException {
 
     Account taxDebt = new Account("Debt", Money.parse("AUD 0"));
     AccountEvent event = new CalculateTaxBillEvent(this.accounts, taxDebt);
     assertEquals(Money.parse("AUD 0"), taxDebt.getBalance());
 
     event.run();
     // all amounts owing are added together before rounding down
     assertEquals(Money.parse("AUD -48.30"), taxDebt.getBalance());
     for (Account account : accounts) {
       assertTrue(account.getTaxOwing().isZero());
     }
   }
 
   @Test(expected=EventExecutionException.class)
   public void testCalculateTaxBillNegativeError()
       throws EventExecutionException {
 
     Account taxDebt = new Account(
         "Error if going negative", Money.parse("AUD 0"), Policy.ALLOW,
         Policy.ERROR);
     AccountEvent event = new CalculateTaxBillEvent(this.accounts, taxDebt);
     assertEquals(Money.parse("AUD 0"), taxDebt.getBalance());
     event.run();
   }
 
   @Test(expected=CurrencyMismatchException.class)
   public void testCalculateTaxBillTaxDebtAccountWrongCurrency()
       throws EventExecutionException {
 
     Account taxDebt = new Account("Wrong currency", Money.parse("USD 0"));
     AccountEvent event = new CalculateTaxBillEvent(this.accounts, taxDebt);
   }
 
   @Test(expected=CurrencyMismatchException.class)
   public void testCalculateTaxBillMixedCurrencies()
       throws EventExecutionException {
 
     Account taxDebt = new Account("Debt", Money.parse("AUD 0"));
     Account b2 = new Account("B prime", Money.parse("USD 5"));
     List<Account> accounts = new LinkedList<Account>();
     accounts.add(a);
     accounts.add(b2);
     accounts.add(c);
     AccountEvent event = new CalculateTaxBillEvent(accounts, taxDebt);
   }
 
 
 
   // Pay tax bill
 
   @Test
   public void testPayTaxBill()
       throws EventExecutionException {
 
     Account paymentAccount = new Account("Savings", Money.parse("AUD 1000"));
     Account taxDebtAccount = new Account("Debt", Money.parse("AUD -750"));
     AccountEvent event = new PayTaxBillEvent(paymentAccount, taxDebtAccount);
     assertEquals(Money.parse("AUD 1000"), paymentAccount.getBalance());
     assertEquals(Money.parse("AUD -750"), taxDebtAccount.getBalance());
 
     event.run();
     assertEquals(Money.parse("AUD 250"), paymentAccount.getBalance());
     assertEquals(Money.parse("AUD 0"), taxDebtAccount.getBalance());
   }
 
   @Test
   public void testPayTaxBillRefund()
       throws EventExecutionException {
 
     Account paymentAccount = new Account("Savings", Money.parse("AUD 1000"));
     Account taxDebtAccount = new Account("Refund", Money.parse("AUD 750"));
     AccountEvent event = new PayTaxBillEvent(paymentAccount, taxDebtAccount);
     assertEquals(Money.parse("AUD 1000"), paymentAccount.getBalance());
     assertEquals(Money.parse("AUD 750"), taxDebtAccount.getBalance());
 
     event.run();
     assertEquals(Money.parse("AUD 1750"), paymentAccount.getBalance());
     assertEquals(Money.parse("AUD 0"), taxDebtAccount.getBalance());
   }
 
   @Test(expected=EventExecutionException.class)
   public void testPayTaxBillInsufficientFunds()
       throws EventExecutionException {
 
     Account paymentAccount = new Account(
         "Error if going negative", Money.parse("AUD 500"), Policy.ALLOW,
         Policy.ERROR);
     Account taxDebtAccount = new Account("Debt", Money.parse("AUD -750"));
     AccountEvent event = new PayTaxBillEvent(paymentAccount, taxDebtAccount);
     assertEquals(Money.parse("AUD 500"), paymentAccount.getBalance());
     assertEquals(Money.parse("AUD -750"), taxDebtAccount.getBalance());
 
     event.run();
   }
 
   @Test(expected=EventExecutionException.class)
   public void testPayTaxBillRefundPositiveError()
       throws EventExecutionException {
 
     Account paymentAccount = new Account(
         "Error if going positive", Money.parse("AUD -500"), Policy.ERROR,
         Policy.ALLOW);
     Account taxDebtAccount = new Account("Refund", Money.parse("AUD 750"));
     AccountEvent event = new PayTaxBillEvent(paymentAccount, taxDebtAccount);
     assertEquals(Money.parse("AUD -500"), paymentAccount.getBalance());
     assertEquals(Money.parse("AUD 750"), taxDebtAccount.getBalance());
 
     event.run();
   }
 }
