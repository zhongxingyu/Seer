 package org.oregami.test;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.EntityManager;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.oregami.data.WebsiteDao;
 import org.oregami.dropwizard.OregamiService;
 import org.oregami.entities.Website;
 import org.oregami.util.WebsiteHelper;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.persist.PersistService;
 import com.google.inject.persist.jpa.JpaPersistModule;
 
//@Ignore
 public class WebsiteHelperTest {
 
 	private static Injector injector;
 	
 	EntityManager entityManager = null;
 	
 	public WebsiteHelperTest() {
 	}
 	
 	@BeforeClass
 	public static void init() {
 		JpaPersistModule jpaPersistModule = new JpaPersistModule(OregamiService.JPA_UNIT);
 		injector = Guice.createInjector(jpaPersistModule);
 		injector.getInstance(WebsiteHelperTest.class);
 		PersistService persistService = injector.getInstance(PersistService.class);
 		persistService.start();
 	}
 	
 	@Before
 	public void startTx() {
 		if (entityManager==null) {
 			entityManager = injector.getInstance(EntityManager.class);
 		}
 		entityManager.getTransaction().begin();
 		
 	}
 	
 	@After
 	public void rollbackTx() {
 		entityManager.getTransaction().rollback();
 	}
 	
 	@Test
 	public void testCreateWebsite() throws IOException {
 		Map<String, String> result = WebsiteHelper.instance().createWebsite("http://www.google.de", "1024*768");
 		byte[] imageBytes = WebsiteHelper.instance().readFile(result.get("filename"));
 		
 		Website website = new Website();
 		website.setImage(imageBytes);
 		
 		WebsiteDao websiteDao = injector.getInstance(WebsiteDao.class);
 		String id1 = websiteDao.save(website);
 		
 		Assert.assertNotNull(id1);
 		
 		List<Website> findAll = websiteDao.findAll();
 		Assert.assertTrue(findAll.size()>0);
 		
 		Website findOne = websiteDao.findOne(id1);
 		Assert.assertNotNull(findOne);
 		Assert.assertArrayEquals(imageBytes, findOne.getImage());
 		
 	}
 	
 }
