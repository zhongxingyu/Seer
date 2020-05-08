 package com.acme.dao.jdbc;
 
 import com.acme.dao.AccountDao;
 import com.acme.model.Account;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.stereotype.Repository;
 
 @Repository("accountDao")
public class JdbcAccountDao extends JdbcDao implements AccountDao {
 
     private JdbcTemplate jdbcTemplate;
     private static final String CREATE_ACCOUNT = "INSERT INTO ACCOUNT  (accountId, userName, lastName, firstName, emailAddress, homePhone, cellPhone)"
             + " values ?, ?, ?, ?, ?, ?, ?";
     private static final String DELETE_ACCOUNT = "DELETE FROM ACCOUNT WHERE accountId = ?";
     private static final String MODIFY_ACCOUNT = "UPDATE ACCOUNT SET accountId = ?, " +
             "                                                       userName = ?, " +
             "                                                       lastName = ?, " +
             "                                                       firstName = ?, " +
             "                                                       emailAddress = ?, " +
             "                                                       homePhone = ?, " +
             "                                                       cellPhone= ? " +
             "                                                       WHERE accountId = ?";
 
     public Account createAccount(Account account) {
 
         this.jdbcTemplate.update(CREATE_ACCOUNT, new Object[]{account.getAccountId(),
                 account.getUserName(),
                 account.getUserName(),
                 account.getLastName(),
                 account.getFirstName(),
                 account.getEmailAddress(),
                 account.getHomePhone(),
                 account.getCellPhone(),
                 account.getAccountId()});
         return account;
     }
 
     public String deleteAccount(Account account) {
         this.jdbcTemplate.update(DELETE_ACCOUNT, new Object[]{account.getAccountId()});
         return "deleted";
     }
 
     public Account modifyAccount(Account account) {
         this.jdbcTemplate.update(MODIFY_ACCOUNT, new Object[]{account.getAccountId(),
                 account.getUserName(),
                 account.getUserName(),
                 account.getLastName(),
                 account.getFirstName(),
                 account.getEmailAddress(),
                 account.getHomePhone(),
                 account.getCellPhone()});
 
         return account;
     }
 
 }
