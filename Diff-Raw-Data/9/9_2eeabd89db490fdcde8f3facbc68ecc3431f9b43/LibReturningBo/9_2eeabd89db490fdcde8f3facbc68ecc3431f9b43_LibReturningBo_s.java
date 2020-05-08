 package model.bo.lib;
 
 import static cococare.common.CCLogic.isEmpty;
 
 import java.util.List;
 
 import model.dao.lib.LibBorrowingItemDao;
 import model.dao.lib.LibReturningItemDao;
 import model.obj.lib.LibBook;
 import model.obj.lib.LibBorrowingItem;
 import model.obj.lib.LibMember;
 import model.obj.lib.LibReturning;
 import model.obj.lib.LibReturningItem;
 import cococare.database.CCHibernateBo;
import cococare.database.CCHibernateDao.Transaction;
 
 /**
  * @author Yosua Onesimus
  * @since 13.03.17
  * @version 13.03.17
  */
 public class LibReturningBo extends CCHibernateBo {
 	private LibBorrowingItemDao borrowingItemDao;
 	private LibReturningItemDao returningItemDao;
 
 	public synchronized List<LibMember> getUnlimitedBorrowingMembers() {
 		return borrowingItemDao.getUnlimitedBorrowingMembers();
 	}
 
 	public synchronized boolean saveOrUpdate(LibReturning returning, List<LibReturningItem> returningItems) {
 		Transaction transaction = returningItemDao.newTransaction();
 		if (isEmpty(returningItems)) {
 			returningItems = returningItemDao.getUnlimitedReturningItems(returning);
 		}
 		for (LibReturningItem returningItem : returningItems) {
 			LibBorrowingItem borrowingItem = returningItem.getBorrowingItem();
 			borrowingItem.setReturned(true);
 			transaction.saveOrUpdate(borrowingItem);
 			LibBook book = borrowingItem.getBook();
 			book.setBorrowed(false);
 			transaction.saveOrUpdate(book);
 		}
 		return transaction.saveOrUpdate(returning).saveOrUpdate(returningItems).execute();
 	}
 }
