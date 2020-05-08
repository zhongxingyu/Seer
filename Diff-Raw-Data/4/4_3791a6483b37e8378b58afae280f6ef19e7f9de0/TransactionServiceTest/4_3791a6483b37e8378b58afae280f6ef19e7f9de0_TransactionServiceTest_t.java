 package com.in6k.mypal.service;
 
 import com.in6k.mypal.dao.UserDao;
 import com.in6k.mypal.domain.User;
 import org.junit.Test;
 
 public class TransactionServiceTest {
     @Test
     public void shouldCreateTransactionIfUserExistAndCreditUserHaveMoney() {
         User creditUser = new User();
         creditUser.setEmail("credit@gmail.com");
         creditUser.setFirstName("CreditName");
         creditUser.setLastName("CreditLastName");
         creditUser.setPassword("123456");
         creditUser.setActive(true);
 
         UserDao.save(creditUser);
         User debitUser = new User();
 
         String debitUserEmail = "debit@gmail.com";
     }
 }
