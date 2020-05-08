 package iou.controller;
 
 import iou.enums.TransactionType;
 import iou.model.Payment;
 import iou.model.Transaction;
 import org.apache.commons.dbcp.BasicDataSource;
 import org.apache.log4j.Logger;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import java.util.List;
 
 public class ControllerImpl implements IController {
 
     private static final Logger LOGGER = Logger.getLogger(ControllerImpl.class);
 
     private ITransactionDao dao;
 
     public boolean login(String username, String password) {
 
         // Create the application context,
        ApplicationContext springContext = new ClassPathXmlApplicationContext("/applicationContext.xml");
         dao = (ITransactionDao) springContext.getBean("txnDao");
 
         // Set the username and password, then try and login
         BasicDataSource dataSource = (BasicDataSource) springContext.getBean("dataSource");
         dataSource.setUsername(username);
         dataSource.setPassword(password);
 
         try {
             dao.testConnection();
         } catch (Exception ex) {
             LOGGER.error("DB connection test failed", ex);
             return false;
         }
 
         LOGGER.debug("Connection successfully tested");
         return true;
     }
 
     public List<Transaction> getTransactions(TransactionType type) {
         return dao.getTransactions(type);
     }
 
     public Transaction insertTransaction(Transaction tran) {
         return dao.insertTransaction(tran);
     }
 
     public boolean updateTransaction(Transaction tran) {
         return dao.updateTransaction(tran);
     }
 
     public boolean deleteTransaction(Long id) {
         return dao.deleteTransaction(id);
     }
 
     public void archiveTransactions(Float netDonalBalance) {
 
         // Archive all current transactions
         dao.archiveTransactions();
 
         if (netDonalBalance != 0) {
 
             // Carry the balance forward by inserting a new payment for the relevant amount
             Transaction balancingPayment = new Payment();
             balancingPayment.setDate(new java.util.Date());
             balancingPayment.setDescription("Balance brought forward");
 
             // A positive netDonalPaid indicates that donal owes maude and vice versa
             if (netDonalBalance > 0) {
                 balancingPayment.setDonalPaid(netDonalBalance);
             } else {
 
                 // Maude owes Donal, change the sign of the amount before inserting
                 balancingPayment.setMaudePaid(-netDonalBalance);
             }
             insertTransaction(balancingPayment);
         }
     }
 }
