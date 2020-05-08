 package ch.hslu.appe.fs1301.data;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import ch.hslu.appe.fs1301.data.shared.iAPPEEntityManager;
 import ch.hslu.appe.fs1301.data.shared.entity.Produkt;
 
 public class ProductRepositoryTest {
 	private ProductRepository fTestee;
 	private static iAPPEEntityManager fEntityManager;
 	private static List<Produkt> fCreatedProducts;
 	
 	@BeforeClass
 	public static void setUpDatabase() {
 		fCreatedProducts = new ArrayList<Produkt>();
 		fEntityManager = new APPEEntityManager();
 		fEntityManager.beginTransaction();
 		fCreatedProducts.add(createAndSaveProdukt());
 		fCreatedProducts.add(createAndSaveProdukt());
 		fCreatedProducts.add(createAndSaveProdukt());
 	}
 	
 	@AfterClass
 	public static void tearDownDatabase() {
 		fEntityManager.rollbackTransaction();
 	}
 	
 	@Before
 	public void setUp() {
 		fTestee = new ProductRepository(fEntityManager);
 	}
 	
 	@Test
 	public void returnsAllProducts() {
 		List<Produkt> allProducts = fTestee.getAllProducts();
 		
		for(Produkt product : fCreatedProducts)
 		{
			assertThat(allProducts).contains(product);
 		}
 	}
 	
 	private static Produkt createAndSaveProdukt() {
 		Produkt product = new Produkt();
 		
 		setIrrelevantValues(product);
 		fEntityManager.persist(product);
 		return product;
 	}
 
 	private static void setIrrelevantValues(Produkt product) {
 		product.setBezeichnung("");
 		product.setLagerbestand(10);
 		product.setMinimalMenge(5);
 		product.setPreis(50);
 	}
 }
