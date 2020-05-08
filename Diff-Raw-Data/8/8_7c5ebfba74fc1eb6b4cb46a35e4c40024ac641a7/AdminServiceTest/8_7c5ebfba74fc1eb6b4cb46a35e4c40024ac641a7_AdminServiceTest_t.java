 package com.in6k.mypal.service;
 
 
 import com.in6k.mypal.dao.CommonDao;
 import com.in6k.mypal.dao.TransactionDao;
 import com.in6k.mypal.dao.UserDao;
 import com.in6k.mypal.domain.Transaction;
 import com.in6k.mypal.domain.User;
 import com.in6k.mypal.service.CreditCard.IncreaseBalanсeService;
 import com.in6k.mypal.util.SecurityUtil;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.List;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 public class AdminServiceTest {
 
     AdminService adminService;
     User debitor = new User();
     User creditor = new User();
 
     @Before
     public  void setup() {
         adminService = new AdminService();
         CommonDao.clearTable("transactions");
         CommonDao.clearTable("users");
 
 
         debitor.setEmail("debitor@user.com");
         debitor.setFirstName("Debitor");
         debitor.setLastName("User");
         debitor.setPassword(SecurityUtil.passwordEncoder("password"));
         debitor.setActive(true);
 
         UserDao.save(debitor);
 
 
         creditor.setEmail("creditor@user.com");
         creditor.setFirstName("creditor");
         creditor.setLastName("User");
         creditor.setPassword(SecurityUtil.passwordEncoder("password"));
         creditor.setActive(true);
 
         UserDao.save(creditor);
 
         IncreaseBalanсeService.moneyFromCreditCard("5105105105105100", "100", creditor.getId(), true);
 
         try {
             TransactionService.create(creditor, debitor.getEmail(), "100");
         } catch (IOException e) {}
     }
 
     @Test
     public void shouldBanUser() {
         User user = new User();
         user.setEmail("Active@user.com");
         user.setFirstName("Active");
         user.setLastName("User");
         user.setPassword(SecurityUtil.passwordEncoder("password"));
         user.setActive(true);
 
         UserDao.save(user);
 
         adminService.banUser(1);
         user = UserDao.getById(1);
         assertTrue(!user.getActive());
     }
 
     @Test
     public void shouldUnBanUser() {
         User user = new User();
         user.setEmail("Banned@user.com");
         user.setFirstName("Banned");
         user.setLastName("User");
         user.setPassword(SecurityUtil.passwordEncoder("password"));
         user.setActive(false);
 
         UserDao.save(user);
 
         adminService.unBanUser(1);
         user = UserDao.getById(1);
         assertTrue(user.getActive());
     }
 
     @Test
     public void  shouldReturnTransactionListForUser() {
 
 
 
         Transaction transaction = (Transaction)adminService.transactionsForUserById(1).get(0);
         assertEquals(2,transaction.getId());
         assertEquals(creditor.getId(),transaction.getCredit().getId());
         assertEquals(debitor.getId(),transaction.getDebit().getId());
        assertEquals(true,transaction.getStatus());
         assertEquals("100.0",String.valueOf(transaction.getSum()));
     }
 
     @Test
     public void shouldShowAllTransactions() throws IOException, SQLException {
         List<Transaction> transactions = adminService.showAllTransactions();
 
         assertEquals(1, transactions.get(0).getId());
         assertEquals(creditor.getId(), transactions.get(0).getDebit().getId());
        assertEquals(true, transactions.get(0).getStatus());
         assertEquals("100.0", String.valueOf(transactions.get(0).getSum()));
 
         assertEquals(2,transactions.get(1).getId());
         assertEquals(creditor.getId(),transactions.get(1).getCredit().getId());
         assertEquals(debitor.getId(),transactions.get(1).getDebit().getId());
        assertEquals(true,transactions.get(1).getStatus());
         assertEquals("100.0",String.valueOf(transactions.get(1).getSum()));
     }
 
     @Test
     public void shouldRestoreTransactionfromCancelled() throws SQLException {
         adminService.restoreTransaction(1);
         Transaction transaction = TransactionDao.getById(1);
 
         assertEquals(true, transaction.getStatus());
     }
 
     @Test
     public void shouldMarkTransactionAsCancelled() throws SQLException {
         adminService.cancelTransaction(1);
         Transaction transaction = TransactionDao.getById(1);
 
         assertEquals(false, transaction.getStatus());
     }
 }
