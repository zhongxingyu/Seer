 package models;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import play.modules.siena.SienaFixtures;
 import play.test.UnitTest;
 import siena.Query;
 import utils.TestUtils;
 
 public class SaleTest extends UnitTest {
 
 	@Before
 	public void setUp() {
 		SienaFixtures.deleteDatabase();
 		SienaFixtures.loadModels("test-data.yml");
 	}
 
 	@After
 	public void tearDown() {
 		SienaFixtures.deleteDatabase();
 	}
 
 	@Test
 	public void count() {
 		assertEquals("sale count", 3, Sale.all().count());
 		assertEquals("base products count", 3, BaseProduct.all().count());
 		assertEquals("products count", 4, Product.all().count());
 	}
 
 	@Test
 	public void emptySale() {
 		Sale empty = TestUtils.getEmptySale();
 		assertEquals("Date: 2011-02-01 Coop: first (part of group)", empty.toString());
 		assertEquals(0, empty.products.count());
 	}
 
 	@Test
 	public void nonEmptySale() {
 		Sale sale = TestUtils.getFirstSale();
 		assertEquals("Date: 2011-02-08 Coop: first (part of group)", sale.toString());
 		assertEquals(2, sale.products.count());
 	}
 
 	private static void assertMemberOrdersCount(Sale sale, String name, int expected) {
 		assertEquals("#orders by " + name, expected, sale.getMemberOrders(TestUtils.getMember(name)).count());
 	}
 
 	@Test
 	public void getMemberOrders() {
 		Sale sale = TestUtils.getFirstSale();
 		assertMemberOrdersCount(sale, "me", 2);
 		assertMemberOrdersCount(sale, "Dayan", 2);
 		assertMemberOrdersCount(sale, "Rosen", 0);
 	}

	@Test
	public void testGetMemberDetails() {
		Sale sale = TestUtils.getFirstSale();
		MemberSaleDetails details = sale.getMemberDetails(TestUtils.getMember("me"));
		assertEquals(true, details.orderTaken);
		assertEquals("", details.comment);
	}
 }
