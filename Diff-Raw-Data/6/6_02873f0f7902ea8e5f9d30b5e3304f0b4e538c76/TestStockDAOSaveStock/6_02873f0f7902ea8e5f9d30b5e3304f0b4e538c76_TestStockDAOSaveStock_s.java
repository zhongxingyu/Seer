 package test.com.suncorp.cashman.persistence;
 
 import com.suncorp.cashman.infrastructure.SpringContextFactory;
 import com.suncorp.cashman.persistence.StockDAO;
 import com.suncorp.cashman.persistence.StockDAOImpl;
 import com.suncorp.cashman.persistence.StockItem;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.xml.XmlBeanFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.transaction.AfterTransaction;
 import org.springframework.test.context.transaction.BeforeTransaction;
 import org.springframework.test.context.transaction.TransactionConfiguration;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.annotation.Resource;
 import java.util.ArrayList;
 import java.util.List;
 
 import static junit.framework.Assert.*;
 import static junit.framework.Assert.assertEquals;
 
 /**
  * Unit Test for the StockDAO.saveStock() method.
  *
  * @author jean.damore
  * @since 22-Aug-2011
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations={"/spring-context-dev.xml", "/test/com/suncorp/cashman/persistence/spring-fixtures-stock-items.xml"})
 @TransactionConfiguration(transactionManager="txManager", defaultRollback=false)
 @Transactional
 public class TestStockDAOSaveStock {
 
     /** Class under test. */
     @Resource
     private StockDAO stockDAO;
 
     /** Fixtures. */
     @Resource
     private StockItem stockItem$50;
     @Resource
     private StockItem stockItem$20;
     @Resource
     private StockItem stockItem$10;
 
     /**
      * ****************************************************
      * Setup and tearDown methods
      * ****************************************************
      */
 
     @BeforeTransaction
     public void setupBeforeTransaction() {
         assertNotNull(stockDAO);
         deleteStock();
     }
 
     @Before
     public void setup() {
         assertNotNull(stockDAO);
         assertNotNull(stockItem$50);
         assertNotNull(stockItem$20);
         assertNotNull(stockItem$10);
         assertNull(stockItem$50.getId());
         assertNull(stockItem$20.getId());
         assertNull(stockItem$10.getId());
     }
 
     @After
     public void tearDown() {
     }
 
     @AfterTransaction
     public void tearDownAfterTransaction() {
         deleteStock();
         assertEquals(0, stockDAO.getStock().size());
         stockItem$50.setId(null);
         stockItem$20.setId(null);
         stockItem$10.setId(null);
     }
 
     @Test
     public void testStockSavedSuccessfully() {
         List<StockItem> stock = createStockList(stockItem$50, stockItem$20, stockItem$10);
         stockDAO.saveStock(stock);
         List<StockItem> savedStock = stockDAO.getStock();
         assertEquals(3, savedStock.size());
        assertEquals(1, (long)stockItem$50.getId());
        assertEquals(2, (long)stockItem$20.getId());
        assertEquals(3, (long)stockItem$10.getId());
         assertEquals(stockItem$50, savedStock.get(0));
         assertEquals(stockItem$20, savedStock.get(1));
         assertEquals(stockItem$10, savedStock.get(2));
     }
 
     @Test
     public void testNewStockItemAddedSuccessfully() {
         List<StockItem> stock = createStockList(stockItem$50, stockItem$20);
         stockDAO.saveStock(stock);
         List<StockItem> savedStock = stockDAO.getStock();
         assertEquals(2, savedStock.size());
         assertEquals(stockItem$50, savedStock.get(0));
         assertEquals(stockItem$20, savedStock.get(1));
         stock = createStockList(stockItem$10);
         stockDAO.saveStock(stock);
         savedStock = stockDAO.getStock();
         assertEquals(3, savedStock.size());
         assertEquals(stockItem$10, savedStock.get(2));
     }
 
     @Ignore
     @Test
     public void testExistingStockItemUpdatedSuccessfully() {
 
         List<StockItem> stock = createStockList(stockItem$50, stockItem$20, stockItem$10);
         stockDAO.saveStock(stock);
 
 
         long newQuantity = 10l;
         StockItem stockItemToUpdate = stockDAO.getStockItem(stockItem$20.getId());
         assertNotSame(stockItem$20, stockItemToUpdate);
         stockItemToUpdate.setQuantity(newQuantity);
         assertFalse(stockItem$20.getQuantity() == stockItemToUpdate.getQuantity());
         List<StockItem> stockToUpdate = createStockList(stockItemToUpdate);
         stockDAO.saveStock(stockToUpdate);
 
 
         StockItem unchangedStockItem$50 = stockDAO.getStockItem(stockItem$50.getId());
         assertNotSame(stockItem$50, unchangedStockItem$50);
         assertEquals(stockItem$50, unchangedStockItem$50);
         StockItem unchangedStockItem$10 = stockDAO.getStockItem(stockItem$10.getId());
         assertNotSame(stockItem$10, unchangedStockItem$10);
         assertEquals(stockItem$10, unchangedStockItem$10);
         StockItem updatedStockItem$20 = stockDAO.getStockItem(stockItem$20.getId());
         assertNotSame(stockItem$20, updatedStockItem$20);
         assertFalse(stockItem$20.equals(updatedStockItem$20));
         assertEquals(stockItem$20.getId(), updatedStockItem$20.getId());
         assertEquals(stockItem$20.getValue(), updatedStockItem$20.getValue());
         assertFalse(stockItem$20.getQuantity() == updatedStockItem$20.getQuantity());
         assertEquals(newQuantity, (long) updatedStockItem$20.getQuantity());
     }
 
 
     private void deleteStock() {
         for(StockItem stockItem : stockDAO.getStock()) {
             stockDAO.deleteStockItem(stockItem);
             stockItem.setId(null);
         }
     }
 
     private List<StockItem> createStockList(StockItem... stockItems) {
         List<StockItem> stock = new ArrayList<StockItem>();
         for(StockItem stockItem : stockItems) {
             stock.add(stockItem);
         }
         return stock;
     }
 
 }
