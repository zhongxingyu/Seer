 package no.niths.infrastructure;
 
 import static org.junit.Assert.assertEquals;
 import no.niths.common.config.HibernateConfig;
 import no.niths.common.config.TestAppConfig;
 import no.niths.domain.Application;
 import no.niths.domain.Developer;
 import no.niths.infrastructure.interfaces.ApplicationRepository;
 import no.niths.infrastructure.interfaces.DeveloperRepository;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(classes= { TestAppConfig.class, HibernateConfig.class})
 @Transactional 
 public class DeveloperRepositoryTest {
 	
 	@Autowired
 	private ApplicationRepository appRepo;
 	
 	@Autowired
 	private DeveloperRepository devRepo;
 	
 	@Test
 	public void testCRUD(){
 		int size = devRepo.getAll(null).size();
 		
		Developer dev = new Developer();
 		devRepo.create(dev);
 		assertEquals(size + 1, devRepo.getAll(null).size());
 		
 		dev.setEmail("nith@nith.no");
 		devRepo.update(dev);
 		assertEquals("nith@nith.no", devRepo.getById(dev.getId()).getEmail());
 		
 		assertEquals(true, devRepo.delete(dev.getId()));
 	}
 	
 	@Test
 	public void testApplicationRelation(){
		Developer dev = new Developer("mrDEv");
 		//devRepo.create(dev);
 		
 		Application app = new Application("hello",null,null,null);
 		dev.getApps().add(app);
 		
 		appRepo.create(app);
 		devRepo.create(dev);
 		
 		assertEquals(1, devRepo.getById(dev.getId()).getApps().size());
 		
 		dev.getApps().remove(app);
 		devRepo.update(dev);
 		assertEquals(0, devRepo.getById(dev.getId()).getApps().size());
 		
 	}
 
 }
