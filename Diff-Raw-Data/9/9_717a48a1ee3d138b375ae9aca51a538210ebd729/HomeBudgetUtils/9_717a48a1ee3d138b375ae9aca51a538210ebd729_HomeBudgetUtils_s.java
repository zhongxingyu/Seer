package utils.org.homebudget.utils;
 
 import java.util.Date;
 
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.cfg.Configuration;
 import org.homebudget.model.Account;
 import org.homebudget.model.Category;
 import org.homebudget.model.Transaction;
 import org.homebudget.model.Transaction.TransactionType;
 import org.homebudget.model.UserDetails;
 import org.homebudget.model.UserRole;
 
 public class HomeBudgetUtils {
 
 	public static final String HIBERNATE_CONFIG_FILE = "org/homebudget/utils/hibernate.cfg.xml";
 
 	public static void populateUsers(int number) {
 
 		final SessionFactory sessionFactory = new Configuration().configure(
 				HIBERNATE_CONFIG_FILE).buildSessionFactory();
 		
 		final Session session = sessionFactory.openSession();
 
 		
 		for (int i = 0; i < number; i++) {
 
 			final UserDetails user = new UserDetails();
 			user.setUserName("Dmitry" + i);
 			user.setUserNickName(user.getUserName() + "_nick");
 			user.setUserSurname("Zakharov");
 			Date birthday = new Date();
 			user.setUserBirthday(birthday);
 			user.setEnabled(1);
 			user.setPassword("123" + i);
 
 			final Account account = new Account();
 			account.setDateOfCreation(new Date());
 			account.setOwner(user);
 			account.setStartingBalance(0);
 
 			final Transaction transaction = new Transaction();
 			transaction.setAmount(10);
 
 			final Category category = new Category();
 			category.setCategory("work");
 
 			transaction.setCategory(category);
 			transaction.setDateOFTransaction(new Date());
 			transaction.setTransactionType(TransactionType.INCOME);
 
 			account.getTransactions().add(transaction);
 
 			final UserRole role = new UserRole();
 
 			role.setAuthority("ROLE_USER");
 			user.setUserRole(role);
 
 			session.beginTransaction();
 			session.save(user);
 			session.save(category);
 			session.save(account);
 			session.save(role);
 			session.getTransaction().commit();
 		}
 
 		session.close();
 
 	}
 
 }
