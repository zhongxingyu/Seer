 package model;
 
 import static org.junit.Assert.assertTrue;
 
 import java.util.Date;
 
 import mocks.MockItemManager;
 import mocks.MockProductContainerManager;
 import mocks.MockProductManager;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 @SuppressWarnings("deprecation")
 public class ItemTest {
 
 	private Barcode validUPCABarcode;
 	private ItemManager itemManager;
 	private ProductManager productManager;
 	private ProductQuantity pq;
 	private Product product;
 	private ProductGroup productGroup;
 	private Date entryDateLastMonth;
 	private Item item;
 
 	@Before
 	public void setUp() throws Exception {
 		ProductContainerManager pcManager = new MockProductContainerManager();
 		validUPCABarcode = new Barcode("494180175762");
 		itemManager = new MockItemManager();
 		productManager = new MockProductManager();
 		pq = new ProductQuantity(2.2f, Unit.FLUID_OUNCES);
 		product = new Product("validBarcode", "A product", 3, 3, pq, productManager);
 		productGroup = new ProductGroup("Test product group", pq, Unit.GALLONS,
 				new StorageUnit("Test storage unit", pcManager), pcManager);
 		entryDateLastMonth = new Date(113, 0, 1, 12, 45, 45);
 		item = new Item(validUPCABarcode, product, productGroup, entryDateLastMonth,
 				itemManager);
 
 		// test Invariants
 		assertTrue(item.getProduct() != null);
 		assertTrue(item.getBarcode() != null);
 		assertTrue(item.getEntryDate() != null);
 		assertTrue(item.getExpirationDate() != null);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		// test Invariants
 		assertTrue(item.getProduct() != null);
 		assertTrue(item.getBarcode() != null);
 		assertTrue(item.getEntryDate() != null);
 		assertTrue(item.getExpirationDate() != null);
 	}
 
 	@Test
 	public void testCompareTo() {
 		Item sameItem = new Item(validUPCABarcode, product, productGroup, itemManager);
 		Item newItem = new Item(new Barcode("412345688919"), new Product("abc", "abcd", 3, 3,
 				pq, productManager), productGroup, itemManager);
 		assertTrue(item.compareTo(sameItem) == 0);
 		assertTrue(item.compareTo(newItem) != 0);
 	}
 
 	@Test
 	public void testItem() {
 		assertTrue(item.getBarcode().equals(validUPCABarcode.toString()));
 		assertTrue(item.getProduct().compareTo(product) == 0);
 		assertTrue(item.getContainer().equals(productGroup));
 
 		Date expiration = item.getExpirationDate();
 		Date entry = item.getEntryDate();
 		assertTrue(expiration != null);
 		assertTrue(entry != null);
 
 		assertTrue(entry.getYear() == entryDateLastMonth.getYear());
 		assertTrue(entry.getMonth() == entryDateLastMonth.getMonth());
 		assertTrue(entry.getDate() == entryDateLastMonth.getDate());
 		assertTrue(entry.getHours() == entryDateLastMonth.getHours());
 		assertTrue(entry.getMinutes() == entryDateLastMonth.getMinutes());
 
 		assertTrue(expiration.getYear() == entryDateLastMonth.getYear());
 		assertTrue(expiration.getMonth() == entryDateLastMonth.getMonth()
 				+ product.getShelfLife());
 		assertTrue(expiration.getDate() == entryDateLastMonth.getDate());
 		assertTrue(expiration.getHours() == entryDateLastMonth.getHours());
 		assertTrue(expiration.getMinutes() == entryDateLastMonth.getMinutes());
 	}
 
 	@Test
 	public void testItemInvalidBarcode() {
 		item = new Item(validUPCABarcode, product, productGroup, entryDateLastMonth,
 				itemManager);
 	}
 
 	@Test
 	public void testItemNoDateNoBarcode() {
 		item = new Item(product, productGroup, itemManager);
 		assertTrue(!item.getBarcode().equals(validUPCABarcode));
 		assertTrue(item.getProduct().compareTo(product) == 0);
 		assertTrue(item.getContainer().equals(productGroup));
 
 		Date expiration = item.getExpirationDate();
 		Date entry = item.getEntryDate();
 		assertTrue(expiration != null);
 		assertTrue(entry != null);
 
 		assertTrue(entry.after(entryDateLastMonth));
 
 		assertTrue(expiration.getYear() == entry.getYear());
 		assertTrue(expiration.getMonth() == entry.getMonth() + product.getShelfLife());
 		assertTrue(expiration.getDate() == entry.getDate());
 		assertTrue(expiration.getHours() == entry.getHours());
 		assertTrue(expiration.getMinutes() == entry.getMinutes());
 	}
 
 	@Test
 	public void testRemove() {
 		Date exitTime = new Date();
 		item.remove();
 		assertTrue(item.getContainer() == null);
 		assertTrue(item.getExitTime().equals(exitTime));
 
 		item.remove();
 		assertTrue(item.getContainer() == null);
 		assertTrue(item.getExitTime().equals(exitTime));
 	}
 }
