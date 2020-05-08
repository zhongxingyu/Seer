 package tests;
 
import java.lang.reflect.InvocationTargetException;

 import junit.framework.Assert;
 
 import org.junit.Test;
 import persistence.model.Book;
 import persistence.model.Product;
 import persistence.utils.PersistenceHelper;
 
 
 public class PersistenceTest {
 	@Test
 	public void delete() throws InterruptedException{
 	    PersistenceHelper helper=PersistenceHelper.getInstance("datanucleus.properties");
 	    helper.DeleteAllInstancesOf(Book.class);
 	    int size = helper.getInstancesWhere(Book.class, "", null).size();
 	    Assert.assertEquals(size, 0);
 	}
 	@Test
 	public void insertAndSelect() throws InterruptedException{
 		Book b= new Book(1200, "test book", "book desgined for tests", 187.98, "junit", "1092883TEST", "Tester as publisher");
 	    PersistenceHelper helper=PersistenceHelper.getInstance("datanucleus.properties");
 	    helper.DeleteAllInstancesOf(Book.class);
 	    helper.persist(b,b.getClass());
 	    int size = helper.getInstancesWhere(Book.class, "serial==1200", null).size();
 	    Assert.assertEquals(size, 1);
 	}
 	@Test
 	public void update() throws InterruptedException {
 		Book b= new Book(1200, "test book", "book desgined for tests", 1.0, "junit", "1092883TEST", "Tester as publisher");
 	    PersistenceHelper helper=PersistenceHelper.getInstance("datanucleus.properties");
 	    helper.DeleteAllInstancesOf(Book.class);
 	    helper.persist(b,b.getClass());
 	    b.setPrice(2.0);
 	    helper.update(b);
 	    int size = helper.getInstancesWhere(Book.class, "serial==1200 && price==2.0", null).size();
 	    System.out.println(size);
 	    Assert.assertEquals(size, 1);
 	}
 
 	@Test
 	public void init() throws InterruptedException{
         Product product1 = new Product(10,"PC DELL","desktop intel p4,ram 3Go,DD 250",600);
         Book book1 = new Book(11,"HP Bible","historiques des produits HP",100,"hewlet-packard", "12345678", "Hp factory");
         Product product = new Product(12,"souris logitech","souris sans fil batterie rechargeable",60);
         Book book = new Book(13,"Les miserables","un chef d'oeuvre de victor hugo",150,"Victory Hugo", "1876", "EDITIONS Blanche neige");
         PersistenceHelper helper=PersistenceHelper.getInstance("datanucleus.properties");
         helper.DeleteAllInstancesOf(Book.class);
         helper.DeleteAllInstancesOf(Product.class);
         helper.persist(book,book.getClass());
         helper.persist(book1,book1.getClass());
         helper.persist(product,product.getClass());
         helper.persist(product1,product1.getClass());
 	}
 
 }
