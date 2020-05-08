 package org.homebudget.services.utils;
 
 import java.util.Date;
 import javax.annotation.PostConstruct;
 
 import javax.annotation.Resource;
 
 import org.homebudget.model.Account;
 import org.homebudget.model.Category;
 import org.homebudget.model.Transaction;
 import org.homebudget.model.Transaction.TransactionType;
 import org.homebudget.model.UserDetails;
 import org.homebudget.model.UserRole;
 import org.homebudget.model.UserRole.Role;
import org.homebudget.services.AccountManagementService;
 import org.homebudget.services.PasswordService;
 import org.homebudget.services.UserManagementService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Service;
 
 @Service
 public class HomeBudgetInitializationService {
 
     public final String HIBERNATE_CONFIG_FILE = "org/homebudget/utils/hibernate.cfg.xml";
 //    private org.hibernate.classic.Session session;
     private final Logger logger = LoggerFactory.getLogger(getClass());
  
     @Resource
     UserManagementService userManagementService;
   
    @Resource
    AccountManagementService accountManagementService;
     
     
     // TODO: replace with userManagementService.getRole();
     UserRole userRole;
 
     @PostConstruct
     public void executePopulation() {
         logger.info("Create initial Population !!!!!!!!!!!!!!!!!!!!!!!!!!!!");
         //        initSession();
         populateUsers(10);
        
     }
 
 //    private void initSession() {
 //        System.out.println("File: " + (new File(".")).getAbsolutePath());
 //        SessionFactory sessionFactory = new Configuration().configure(
 //                HIBERNATE_CONFIG_FILE).buildSessionFactory();
 //        session = sessionFactory.openSession();
 //    }
 
     private void populateUsers(int number) {
         logger.info("initialize database with " + number + " users");
         initUserRoles();
 
         for (int i = 0; i < number; i++) {
             UserDetails user = createTestUser(i);
             logger.info("Creating user: " + user.getUserName());
 
             Account account = createTestAccount(user);
             logger.info("Creating account: " + account.getAccountName());
 
             Category category = createTestCategory();
             logger.info("Creating category: " + category.getCategory());
 
             Transaction transaction = createTestTransaction(category);
             logger.info("Creating transaction: " + transaction.getAmount());
 
             account.getTransactions().add(transaction);
 
             // UserRole role =
             // userManagementService.getRole(UserRole.Authority.USER_ROLE);
             user.getUserRoles().add(userRole);
            
             
             userManagementService.saveUserDetails(user);
             
 //            session.beginTransaction();
 //            session.save(user);
 //            session.save(category);
 //            session.save(account);
 //
 //            session.getTransaction().commit();
         }
 
 //        session.close();
 
     }
 
     private void initUserRoles() {
 
         for (Role role : Role.values()) {
 //            session.beginTransaction();
             UserRole uRole = new UserRole();
             uRole.setUserRole(role);
             if (role == Role.USER_ROLE) {
                 userRole = uRole;
             }
             logger.info("Creating role: " + uRole.getUserRole());
             userManagementService.saveUserRole(uRole);
 //            session.save(uRole);
 //            session.getTransaction().commit();
         }
     }
 
     private UserDetails createTestUser(int i) {
         final UserDetails user = new UserDetails();
         user.setUserName("Dmitry" + i);
         user.setUserUsername(user.getUserName() + "_nick");
         user.setUserSurname("Zakharov");
         user.setEmail("some" + i + "@email.com");
         Date birthday = new Date();
         user.setUserBirthday(birthday);
         user.setEnabled(1);
         try {
             user.setPassword(PasswordService.getHash("123" + i));
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         return user;
     }
 
     private Transaction createTestTransaction(final Category category) {
         final Transaction transaction = new Transaction();
         transaction.setAmount(10);
         transaction.setCategory(category);
         transaction.setDateOFTransaction(new Date());
         transaction.setTransactionType(TransactionType.INCOME);
 
         return transaction;
     }
 
     private Account createTestAccount(UserDetails user) {
         final Account account = new Account();
         account.setDateOfCreation(new Date());
         account.setOwner(user);
         account.setStartingBalance(0);
         return account;
     }
 
     private Category createTestCategory() {
         Category category = new Category();
         category.setCategory("work");
         return category;
     }
 }
