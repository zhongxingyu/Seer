 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package se.liu.tdp024.data.test;
 
 import java.util.*;
 import org.junit.*;
 import se.liu.tdp024.entity.*;
 import se.liu.tdp024.facade.AccountFacade;
 import se.liu.tdp024.util.EMF;
 
 /**
  *
  */
 public class AccountFacadeTest {
 
     public AccountFacadeTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
     @Before
     public void setUp() {
         
     }
 
     @After
     public void tearDown() {
         EMF.close();
     }
 
     @Test
     public void testCreate() {
         // Create a new salary account
         long accountNumber = AccountFacade.create(Account.SALARY,
                                                   "personKey",
                                                   "bankKey");
         Assert.assertFalse("Accountnumber cannot be 0.", accountNumber == 0);
         
         // Create a second account for same user
         long newAccountNumber = AccountFacade.create(Account.SALARY,
                                              "personKey",
                                              "bankKey");
         Assert.assertTrue("Second account couldn't be created.", 
                             accountNumber != newAccountNumber &&
                             newAccountNumber != 0);
         
         // Create a savings account
         accountNumber = AccountFacade.create(Account.SAVINGS,
                                              "personKey",
                                              "bankKey");
         Assert.assertTrue("Couldn't create Savings account.",
                           accountNumber == 0);
     }
     
     @Test
     public void testFind() {
         long accountNumber = AccountFacade.create(Account.SALARY, "person", "bank");
         
         Account acc = AccountFacade.find(accountNumber);
         
         Assert.assertNotNull(acc);
        Assert.assertEquals(Account.SALARY, acc.getAccountType());
         Assert.assertEquals("person", acc.getPersonKey());
        Assert.assertEquals("bank", acc.getBankKey());
         
     }
     
     @Test
     public void testFindByPersonKey() {
         AccountFacade.create(Account.SALARY, "person", "bank");
         AccountFacade.create(Account.SALARY, "person", "bank1");
         AccountFacade.create(Account.SAVINGS, "person", "bank2");
         AccountFacade.create(Account.SAVINGS, "person2", "bank2");
         
         List<Account> accounts = AccountFacade.findByPersonKey("person");
         
         Assert.assertEquals(3, accounts.size());
     }
     
     @Test
     public void testFindByBankKey() {
         AccountFacade.create(Account.SALARY, "person", "bank");
         AccountFacade.create(Account.SALARY, "person", "bank1");
         AccountFacade.create(Account.SAVINGS, "person", "bank2");
         AccountFacade.create(Account.SAVINGS, "person2", "bank2");
         
         List<Account> accounts = AccountFacade.findByBankKey("bank2");
         
         Assert.assertEquals(2, accounts.size());
     }
     
     @Test
     public void testBalanceChanges() {
         long accountNumber = AccountFacade.create(Account.SALARY, "person", "bank");
         boolean status;
 
         Assert.assertEquals(0, AccountFacade.balance(accountNumber));
         
         // Deposit some
         status = AccountFacade.deposit(accountNumber, 100);
         Assert.assertTrue(status);
         Assert.assertEquals(100, AccountFacade.balance(accountNumber));
         
         // Withdraw some money
         status = AccountFacade.withdraw(accountNumber, 50);
         Assert.assertTrue(status);
         Assert.assertEquals(50, AccountFacade.balance(accountNumber));
         
         // Try to withdraw too much
         status = AccountFacade.withdraw(accountNumber, 100);
         Assert.assertFalse(status);
         Assert.assertEquals(50, AccountFacade.balance(accountNumber));
         
         // Try to deposit too much
         status = AccountFacade.deposit(accountNumber, Long.MAX_VALUE);
         Assert.assertFalse(status);
         Assert.assertEquals(50, AccountFacade.balance(accountNumber));
     }
 }
