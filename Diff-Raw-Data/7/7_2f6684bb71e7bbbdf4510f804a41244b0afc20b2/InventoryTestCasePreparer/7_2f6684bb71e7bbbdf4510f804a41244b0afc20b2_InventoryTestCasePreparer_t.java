 package nl.minicom.evenexus.inventory;
 
 import java.util.List;
 
 import javax.inject.Inject;
 
 import nl.minicom.evenexus.persistence.Database;
 import nl.minicom.evenexus.persistence.dao.Profit;
 import nl.minicom.evenexus.persistence.dao.WalletTransaction;
 import nl.minicom.evenexus.persistence.interceptor.Transactional;
 
 import org.hibernate.Session;
 import org.hibernate.criterion.Order;
 import org.junit.Assert;
 
 public class InventoryTestCasePreparer {
 
 	private final Database database;
 	
 	@Inject
 	public InventoryTestCasePreparer(Database database) {
 		this.database = database;
 	}
 	
 	@Transactional
 	public void prepare(InventoryTestCase testCase) {
 		Session session = database.getCurrentSession();
 		for (WalletTransaction walletTransaction : testCase.getInitialTransactions()) {
 			session.save(walletTransaction);
 			session.flush();
 			session.evict(walletTransaction);
 		}
 	}
 	
 	@Transactional
 	@SuppressWarnings("unchecked")
 	public void checkTestCaseResults(InventoryTestCase testCase) {
 		Session session = database.getCurrentSession();
 		
 		List<WalletTransaction> transactions = session
 			.createCriteria(WalletTransaction.class)
 			.addOrder(Order.asc("transactionId"))
 			.list();
 		
 		List<Profit> profits = session.createCriteria(Profit.class)
 			.addOrder(Order.asc("id"))
 			.list();
 		
 		if (testCase.getFinalTransactions().size() != transactions.size()) {
 			Assert.fail("Unequal amount of transactions in db and calculated!");
 		}
 		
 		if (testCase.getGeneratedProfitEntries().size() != profits.size()) {
 			Assert.fail("Unequal amount of profits in db (" + profits.size() 
 					+ ") and calculated (" + testCase.getGeneratedProfitEntries().size() + ")!");
 		}
 		
 		for (WalletTransaction fileTransaction : testCase.getFinalTransactions()) {
 			boolean matched = false;
 			for (WalletTransaction dbTransaction : transactions) {
 				if (dbTransaction.equals(fileTransaction)) {
 					matched = true;
 					break;
 				}
 			}
 			
 			if (matched) {
 				transactions.remove(fileTransaction);
 			}
 			else {
 				Assert.fail("Failed to match transaction: " 
 						+ fileTransaction.getTransactionId() + "!");
 			}
 		}
 		
 		for (Profit fileProfit : testCase.getGeneratedProfitEntries()) {
 			boolean matched = false;
 			for (Profit dbProfit : profits) {
 				if (dbProfit.equals(fileProfit)) {
 					matched = true;
 				}
 			}
 			
 			if (matched) {
 				profits.remove(fileProfit);
 			}
 			else {
 				Assert.fail("Failed to match profit: (" 
 						+ fileProfit.getId().getBuyTransactionId() + "," 
 						+ fileProfit.getId().getSellTransactionId() + ")!");
 			}
 		}
 	}
 	
 	@Transactional
 	public void dropDatabase() {
 		Session session = database.getCurrentSession();
 		session.createSQLQuery("DROP ALL OBJECTS").executeUpdate();
 	}
 	
 }
