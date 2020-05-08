 package model.bo.lib;
 
 import static cococare.common.CCLogic.isEmpty;
 
 import java.util.List;
 
 import model.dao.lib.LibBorrowingItemDao;
 import model.obj.lib.LibBook;
 import model.obj.lib.LibBorrowing;
 import model.obj.lib.LibBorrowingItem;
 import cococare.database.CCHibernateBo;
import cococare.database.CCHibernate.Transaction;
 
 /**
  * @author Yosua Onesimus
  * @since 13.03.17
  * @version 13.03.17
  */
 public class LibBorrowingBo extends CCHibernateBo {
 	private LibBorrowingItemDao borrowingItemDao;
 
 	public synchronized boolean saveOrUpdate(LibBorrowing borrowing, List<LibBorrowingItem> borrowingItems) {
 		Transaction transaction = borrowingItemDao.newTransaction();
 		if (isEmpty(borrowingItems)) {
 			borrowingItems = borrowingItemDao.getUnlimitedBorrowingItems(borrowing);
 		}
 		for (LibBorrowingItem borrowingItem : borrowingItems) {
 			LibBook book = borrowingItem.getBook();
 			if (!borrowingItem.isReturned()) {
 				book.setBorrowed(true);
 			}
 			transaction.saveOrUpdate(book);
 		}
 		return transaction.saveOrUpdate(borrowing).saveOrUpdate(borrowingItems).execute();
 	}
 }
